package org.tensorflow.lite.examples.poseestimation.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.pinneapple.dojocam_app.R
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.examples.poseestimation.VisualizationUtils
import org.tensorflow.lite.examples.poseestimation.YuvToRgbConverter
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.examples.poseestimation.ml.PoseClassifier
import org.tensorflow.lite.examples.poseestimation.ml.PoseDetector
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow


class CameraSource(
    private val surfaceView: SurfaceView,
    private val listener: CameraSourceListener? = null,
) {

    companion object {
        public const val PREVIEW_WIDTH = 640
        public const val PREVIEW_HEIGHT = 480

        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .6f
        private const val TAG = "Camera Source"
    }
    private var current = "0"


    private val lock = Any()
    private var detector: PoseDetector? = null
    private var classifier: PoseClassifier? = null
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    private var person: Person? = null
    private lateinit var outputBitmap:Bitmap

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Readers used as buffers for camera still shots */
    private var imageReader: ImageReader? = null

    /** The [CameraDevice] that will be opened in this fragment */
    private var camera: CameraDevice? = null

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private var session: CameraCaptureSession? = null

    /** [HandlerThread] where all buffer reading operations run */
    private var imageReaderThread: HandlerThread? = null

    /** [Handler] corresponding to [imageReaderThread] */
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

    /** feedback for training process **/
    private var feedbackPose: Int = 0

    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    imageBitmap =
                        Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                }
                yuvConverter.yuvToRgb(image, imageBitmap)
                // Create rotated version for portrait display
                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(270.0f)
                val cx = PREVIEW_WIDTH / 2f
                val cy = PREVIEW_HEIGHT / 2f
                rotateMatrix.postScale(-1f, 1f, cx, cy);

                val rotatedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, false
                )

                processImage(rotatedBitmap)
                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }

    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // We don't use a front facing camera in this sample.
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null &&
                cameraDirection == CameraCharacteristics.LENS_FACING_BACK
            ) {
                continue
            }
            this.cameraId = cameraId
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }

    fun setClassifier(classifier: PoseClassifier?) {
        synchronized(lock) {
            if (this.classifier != null) {
                this.classifier?.close()
                this.classifier = null
            }
            this.classifier = classifier
        }
    }

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        classifier?.close()
        classifier = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    // process image
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processImage(bitmap: Bitmap) {
        outputBitmap = bitmap

        var classificationResult: List<Pair<String, Float>>? = null

        synchronized(lock) {
            detector?.estimateSinglePose(bitmap)?.let {
                person = it
                classifier?.run {
                    //classificationResult = classify(person)
                    outputBitmap = drawExpectedBody(bitmap, current, person!!)
                }
            }
        }
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            // send fps to view
            listener?.onFPSListener(framesPerSecond)
        }
        listener?.onDetectedInfo(person?.score, classificationResult)
        person?.let {
            visualize(it, outputBitmap)
        }
    }

    private var drawingOnScreen = false
    @RequiresApi(Build.VERSION_CODES.O)
    private fun visualize(person: Person, bitmap: Bitmap) {
        var outputBitmap: Bitmap

        if (person.score > MIN_CONFIDENCE) {
            outputBitmap = VisualizationUtils.drawBodyKeypoints(bitmap, person)
        }else{
            val leftEye = person.keyPoints[1].coordinate
            val rightEye = person.keyPoints[2].coordinate
            val eyedist = ( (leftEye.x-rightEye.x).toDouble().pow(2) + (leftEye.y-rightEye.y).toDouble().pow(2) ).toDouble().pow(0.5)

            val context = surfaceView.context
            outputBitmap =
                if( person.keyPoints[1].score > 0.3f && eyedist > 0.05f ){
                    VisualizationUtils.drawBodyKeypointsError(bitmap, context.getString(R.string.close_to_camera))
                }else {
                    VisualizationUtils.drawBodyKeypointsError(bitmap, context.getString(R.string.body_not_found))
                }
        }
        if( feedbackPose != 0 ) {
            outputBitmap = VisualizationUtils.drawFeedback(outputBitmap, feedbackPose)
        }

        if( drawingOnScreen ){
            val paint: Paint = Paint().apply {
                strokeWidth = 700f
                color = Color.argb(onScreenAlpha, 1f, 0f, 0f)
                style = Paint.Style.FILL
                textSize = onScreenSize
                textAlign = Paint.Align.CENTER
            }
            outputBitmap = VisualizationUtils.drawTextOnScreen( outputBitmap, onScreenText, paint )
        }

        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left = 0
            val top = 0

            if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                screenHeight = (canvas.width * ratio).toInt()
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                screenWidth = (canvas.height * ratio).toInt()
            }

            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, screenWidth, screenHeight), null
            )

            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    public fun tootgleDrawOnScreen(boolean: Boolean){
        drawingOnScreen = boolean
        onScreenText = ""
    }

    private var onScreenText: String = ""
    private var onScreenSize: Float = 0f
    private var onScreenAlpha: Float = 1f
    public fun setDrawOnScreen( text: String, size: Float, alpha: Float = 1f ){
        onScreenText = text
        onScreenSize = size
        onScreenAlpha = if ( alpha > 1 ) 1f else alpha
    }


    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    interface CameraSourceListener {
        fun onFPSListener(fps: Int)

        fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<String, Float>>?)
    }

    fun getFeedbackStatus(): Boolean {
        return feedbackPose != 0
    }

    fun enableFeedbackPose() {
        feedbackPose = 3
    }

    fun disableFeedbackPose() {
        feedbackPose = 0
    }

    fun checkPose( target: String ): Boolean {
        current = target

        var difference : Int = 99999999
        classifier?.run {
            difference = PoseClassifier.getExpectedBody(current)?.let { person?.getDiference(it) }
                ?:99999999
            if( PoseClassifier.getExpectedBody(current) == null ) {
                Log.wtf("CLASIFIER", "NO EXPECTED BODY: $current")
            }
        }
        if( difference < 100 ) {
            feedbackPose = 1
            Toast.makeText(surfaceView.context, "Aproved!", Toast.LENGTH_SHORT ).show()
        } else if( difference < 300 ) {
            feedbackPose = 2
        } else {
            feedbackPose = 3
        }
        //outputBitmap = VisualizationUtils.drawBodyKeypointsError(outputBitmap, "Difference: "+difference+"")
        Log.wtf(TAG, "checkPose: Difference:" + difference+"")
        return difference < 100;

    }

    fun scorePose( target: String ): Int {
        current = target

        var difference : Int = 99999999
        classifier?.run {
            difference = PoseClassifier.getExpectedBody(current)?.let { person?.getDiference(it) }
                ?:99999999
            if( PoseClassifier.getExpectedBody(current) == null ) {
                Log.wtf("CLASIFIER", "NO EXPECTED BODY: $current")
            }
        }
        if( difference < 100 ) {
            feedbackPose = 1
            Toast.makeText(surfaceView.context, "Aproved!", Toast.LENGTH_SHORT ).show()
        } else if( difference < 300 ) {
            feedbackPose = 2
        } else {
            feedbackPose = 3
        }
        //outputBitmap = VisualizationUtils.drawBodyKeypointsError(outputBitmap, "Difference: "+difference+"")
        Log.wtf(TAG, "checkPose: Difference:" + difference+"")
        val score = 300-difference
        return if( score < 0 ) 0 else score;

    }
}
