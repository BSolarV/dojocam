/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/
package com.pinneapple.dojocam_app
//package org.tensorflow.lite.examples.poseestimation

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.pinneapple.dojocam_app.poseestimation.FloatingVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.poseestimation.camera.CameraSource
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.ml.ModelType
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import org.tensorflow.lite.examples.poseestimation.ml.PoseClassifier
import org.tensorflow.lite.examples.poseestimation.ml.PoseNet
import java.util.*

import android.content.ComponentName

import android.content.ServiceConnection
import android.graphics.PointF
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pinneapple.dojocam_app.objets.UserData
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.KeyPoint
import org.tensorflow.lite.examples.poseestimation.data.Person
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import kotlin.math.floor


class Ml_model : AppCompatActivity(){
    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
    }

    /** A [SurfaceView] for camera preview.   */
    private lateinit var surfaceView: SurfaceView

    /** Default pose estimation model is 1 (MoveNet Thunder)
     * 0 == MoveNet Lightning model
     * 1 == MoveNet Thunder model
     * 2 == PoseNet model
     **/
    private var modelPos = 2

    /** Default device is GPU */
    private var device = Device.GPU

    //windu
    private lateinit var id_ejercicio: String
    private lateinit var namefile: String
    private lateinit var vid_path: String
    private lateinit var videoPip: Intent
    private var init: Boolean = false

    private val db = FirebaseFirestore.getInstance()

    //receiver
    private var serviceUpdateReceiver: ServiceUpdateReceiver? = null

    private var current  = 0
    private lateinit var floatingVideoVideo: VideoView

    private var LABELS_FILENAME = ""


    private lateinit var tvScore: TextView
    private lateinit var tvFPS: TextView
    private lateinit var spnDevice: Spinner
    private lateinit var spnModel: Spinner
    private lateinit var tvClassificationValue1: TextView
    private lateinit var tvClassificationValue2: TextView
    private lateinit var tvClassificationValue3: TextView
    private lateinit var swClassification: SwitchCompat
    private lateinit var mlPlayButton: Button
    private lateinit var mlPauseButton: Button

    private lateinit var backwardBtn: androidx.appcompat.widget.AppCompatImageButton
    private lateinit var forwardBtn: androidx.appcompat.widget.AppCompatImageButton
    private lateinit var pauseBtn: androidx.appcompat.widget.AppCompatImageButton

    private var fbUser : FirebaseUser? = null

    private var cameraSource: CameraSource? = null
    private var isClassifyPose = true
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }
    private var changeModelListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            changeModel(position)
        }
    }

    private var changeDeviceListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            changeDevice(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }
    } 

    private var setClassificationListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            showClassificationInfo(isChecked)
            isClassifyPose = true
            isPoseClassifier()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ml)

        fbUser = FirebaseAuth.getInstance().currentUser
        if( fbUser == null ) finish()

        //windu
        val b = intent.extras
        id_ejercicio = b!!.getString("id_ejercicio").toString()
        namefile = b!!.getString("namefile").toString()
        vid_path = b!!.getString("vid_path").toString()

        //kuro
        videoPip = Intent(this, FloatingVideo::class.java)
        videoPip.putExtra(
            "videoUrl",
            vid_path
        )
        //startActivity(videoPip)
        if( !Settings.canDrawOverlays(this) ){
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
            finish()
        }

        // keep screen on while app is running
        LABELS_FILENAME = "models-data/$namefile-labels.txt"
        read()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        tvScore = findViewById(R.id.tvScore)
        tvFPS = findViewById(R.id.tvFps)
        spnModel = findViewById(R.id.spnModel)
        spnDevice = findViewById(R.id.spnDevice)
        surfaceView = findViewById(R.id.surfaceView)
        tvClassificationValue1 = findViewById(R.id.tvClassificationValue1)
        tvClassificationValue2 = findViewById(R.id.tvClassificationValue2)
        tvClassificationValue3 = findViewById(R.id.tvClassificationValue3)
        swClassification = findViewById(R.id.swPoseClassification)
        initSpinner()
        spnModel.setSelection(modelPos)
        swClassification.setOnCheckedChangeListener(setClassificationListener)

        isClassifyPose = true
        isPoseClassifier()

        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
        /*********************************************************
         *
         * HandMade MediaPlayer Listeners
         *
         ********************************************************/
        backwardBtn = findViewById(R.id.mlBackButton)
        forwardBtn = findViewById(R.id.mlSkipButton)
        pauseBtn = findViewById(R.id.mlPlayPauseButton)

        backwardBtn.setOnClickListener{
            /*mService.pauseVideo()
            mService.videoView.seekTo(mService.videoView.currentPosition - 1000)
            Toast.makeText(this, "-1sec", Toast.LENGTH_SHORT).show()
            mService.videoView.start()*/
            mService.bwdVideo()
        }
        forwardBtn.setOnClickListener{
            /*mService.pauseVideo()
            mService.videoView.seekTo(mService.videoView.currentPosition + 1000)
            Toast.makeText(this, "+1sec", Toast.LENGTH_SHORT).show()
            mService.videoView.start()*/
            mService.fwdVideo()
        }
        pauseBtn.setOnClickListener{
            if (mService.videoView.isPlaying) {
                mService.pauseVideo()
            }
            else {
                mService.startVideo()
            }
        }

    }

    //service android app
    private lateinit var mService:FloatingVideo
    private var mBound = false

    /******************************************************************
     *
     * Defines callbacks for service binding, passed to bindService()
     *
     * **************************************************************  */
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            //val binder = service as LocalBinder<*>
            val binder: FloatingVideo.LocalBinder = service as FloatingVideo.LocalBinder
            mService = binder.service
            mBound = true

            floatingVideoVideo = mService.videoView

            floatingVideoVideo.setOnPreparedListener(MediaPlayer.OnPreparedListener {
                timerCounter()
                it.start()
                videoDuration = it.duration
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }


    }


    override fun onStart() {
        super.onStart()

        // Bind to LocalService
        val intent = Intent(this, FloatingVideo::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        openCamera()
        cameraSource?.resume()

        videoPip = Intent(this, FloatingVideo::class.java)

        videoPip.putExtra(
            "videoUrl",
            vid_path
        )
        startService( videoPip )

        /*
        //Consulta a Bd para obtener antScore
        val userReference = Objects.requireNonNull(FirebaseAuth.getInstance().currentUser!!.email)?.let { db.collection("Users").document(it) }

        userReference?.get()?.addOnSuccessListener(OnSuccessListener { command: DocumentSnapshot ->
            antScore = command.get("score") as List<*>
            //antScore = user.score as List<*>
        })
        */


        //Service listener
        /*if (serviceUpdateReceiver == null) serviceUpdateReceiver = ServiceUpdateReceiver()
        val intentFilter = IntentFilter("RefreshTask.REFRESH_DATA_INTENT")
        registerReceiver(serviceUpdateReceiver, intentFilter)*/

        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null

        videoPip = Intent(this, FloatingVideo::class.java)
        videoPip.putExtra(
            "videoUrl",
            vid_path
        )
        stopService( videoPip )

        //Service listener
        /*if (serviceUpdateReceiver != null) unregisterReceiver(serviceUpdateReceiver);*/
        super.onPause()
    }
    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

/*
    //Media Controller binder

    private var first = true
    private lateinit var mc: MediaController

    private fun showMediaControllerHere() {
        if (mBound) {
            mc = MediaController(this)
            mc.setAnchorView(mService.videoView);
            //mc.setMediaPlayer(mService.videoView)
            //mc.setEnabled(true)
            mService.videoView.setMediaController(mc)
            //mc.show(0)
            //mService.initVideo()
            //Toast.makeText(this, "MC DONE", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        if (first) {
            if (mBound) {
                //mService.initVideo()
                showMediaControllerHere()
                first = false
                mService.Make()
            }
        } else {
            if (mBound) {
                mc.show(0)
            }
        }

        return false
    }*/


    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    // open camera
    @RequiresApi(Build.VERSION_CODES.O)
    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                        override fun onFPSListener(fps: Int) {
                            tvFPS.text = getString(R.string.tfe_pe_tv_fps, fps)
                        }

                        override fun onDetectedInfo(
                            personScore: Float?,
                            poseLabels: List<Pair<String, Float>>?
                        ) {
                            tvScore.text = getString(R.string.tfe_pe_tv_score, personScore ?: 0f)
                            poseLabels?.sortedByDescending { it.second }?.let {
                                tvClassificationValue1.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.isNotEmpty()) it[0] else null)
                                )
                                tvClassificationValue2.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 2) it[1] else null)
                                )
                                tvClassificationValue3.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 3) it[2] else null)
                                )
                            }
                        }

                    }).apply {
                        prepareCamera()
                    }
                isPoseClassifier()
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }

    private fun convertPoseLabels(pair: Pair<String, Float>?): String {
        if (pair == null){
            //Toast.makeText(this, "No te veo compare, avispate", Toast.LENGTH_SHORT).show()
            return "empty"}

        return "${pair.first} (${String.format("%.2f", pair.second)})"
    }

    private fun isPoseClassifier() {
        cameraSource?.setClassifier(if (isClassifyPose) PoseClassifier.create(this, namefile) else null)
    }

    // Init spinner that user can choose model and device they want.
    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.tfe_pe_models_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spnModel.adapter = adapter
            spnModel.onItemSelectedListener = changeModelListener
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.tfe_pe_device_name, android.R.layout.simple_spinner_item
        ).also { adaper ->
            adaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spnDevice.adapter = adaper
            spnDevice.onItemSelectedListener = changeDeviceListener
        }
    }

    // change model when app is running
    private fun changeModel(position: Int) {
        if (modelPos == position) return
        modelPos = position
        createPoseEstimator()
    }

    // change device type when app is running
    private fun changeDevice(position: Int) {
        val targetDevice = when (position) {
            0 -> Device.CPU
            1 -> Device.GPU
            else -> Device.NNAPI
        }
        if (device == targetDevice) return
        device = targetDevice
        createPoseEstimator()
    }

    private fun createPoseEstimator() {
        val poseDetector = when (modelPos) {
            0 -> {
                MoveNet.create(this, device)
            }
            1 -> {
                MoveNet.create(this, device, ModelType.Thunder)
            }
            else -> {
                PoseNet.create(this, device)
            }
        }
        cameraSource?.setDetector(poseDetector)
    }

    private fun showClassificationInfo(isChecked: Boolean) {
        tvClassificationValue1.visibility = if (isChecked) View.VISIBLE else View.GONE
        tvClassificationValue2.visibility = if (isChecked) View.VISIBLE else View.GONE
        tvClassificationValue3.visibility = if (isChecked) View.VISIBLE else View.GONE
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private var timer: Timer? = null
    private fun timerCounter() {
        timer = Timer()
        val task: TimerTask = object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun run() {
                runOnUiThread { updateUI() }
            }
        }

        timer!!.schedule(task, 0, 100)
    }

    private var videoDuration = 99999
    private var currentTime = 0
    private var lastSec: Int? = null
    private var keepAsking: Boolean = false
    private var counterTime = 0
    private val NUM_DURATION = 10
    private var textIndex = 1
    private var learning = 0
    private var showed = false
    private var alphaFactor = 1f
    private var total = 0
    private var divisor = 0

    private var labels: MutableList<Int> = mutableListOf()


    private fun read() {
        /*
        try {
            Log.wtf("READ: ", LABELS_FILENAME)
            val myObj = File(LABELS_FILENAME)
            val myReader = Scanner(myObj)
            while (myReader.hasNextLine()) {
                val data = myReader.nextLine()
                labels.add(data.toInt())
            }
            myReader.close()
            Log.wtf("READ: ", labels.toString())
        } catch (e: FileNotFoundException) {
            println("An error occurred.")
            e.printStackTrace()
        }*/
        try{
            val bufferReader = this.assets.open(LABELS_FILENAME).bufferedReader()
            while (bufferReader.ready()) {
                val line: String = bufferReader.readLine()
                labels.add(line.toInt())
            }
            bufferReader.close()
            labels.sort()
            labels.removeAt(0)
            Log.wtf("READ: ", labels.toString())
        } catch (e: Exception) {
            e.message?.let { Log.i("Error", it) }
        }
    }

    private var index = 0


    fun updateUI() {
        if( labels.isEmpty() ) return;

        //sendBroadcast(Intent("RefreshTask.PAUSE_VIDEO"))
        currentTime = floatingVideoVideo.currentPosition
        //current = currentTime / 1000

        if( currentTime + 20 >= videoDuration ){

            if( counterTime == 0 ){
                cameraSource?.tootgleDrawOnScreen( true )
                learning++
            }
            if( learning >= 2 ){
                cameraSource?.tootgleDrawOnScreen( true )
                alphaFactor =  1f
                total /= 3
                total = if (divisor == 0) 0 else total/divisor

                //Corro función que envía el puntaje a BD
                putScoreBD(total)

                cameraSource?.setDrawOnScreen("Bien Hecho!! \n $total", 48f, alphaFactor )
                keepAsking = false
                counterTime++
            } else {
                val text = when (textIndex) {
                    1 -> " Bien Hecho"
                    2 -> "Ahora todo junto"
                    3 -> "¿Listo?"
                    4 -> "3"
                    5 -> "2"
                    6 -> "1"
                    else -> "¡Vamos!"
                }

                alphaFactor = 1f
                cameraSource?.setDrawOnScreen(text, 48f, alphaFactor)

                if (counterTime >= NUM_DURATION * (1 + textIndex)) {
                    if (textIndex == 7) {
                        floatingVideoVideo.stopPlayback()
                        textIndex = 0
                        showed = true
                        keepAsking = true
                        index = 0
                        cameraSource?.tootgleDrawOnScreen(false)
                        floatingVideoVideo.resume()
                    } else {
                        textIndex++
                    }
                }

                counterTime++
            }
        }

        if( cameraSource?.getFeedbackStatus() != true ){
            cameraSource?.enableFeedbackPose()
        }

        if( index < labels.size && learning == 0 && floatingVideoVideo.isPlaying && labels[index] - 100 < currentTime && currentTime < labels[index] + 300 ) {
            if( labels[index] != lastSec ){
                floatingVideoVideo.pause()
                keepAsking = true
                Log.wtf("PAUSED: ", "At "+ currentTime)
                lastSec = labels[index]
            }
        }
        if ( keepAsking && index < labels.size ){
            if( learning != 0 ) {
                if( labels[index] - 100 < currentTime && currentTime < labels[index] + 300  ){
                    total += cameraSource?.scorePose(labels[index].toString()) ?: 0
                    divisor++
                    if (currentTime > labels[index]) {
                        index++
                    }
                }
            }else{
                val result = cameraSource?.checkPose(labels[index].toString())
                if ( result == true ) {
                    //sendBroadcast(Intent("RefreshTask.START_VIDEO"))
                    floatingVideoVideo.start()
                    keepAsking = false
                    index++
                }
            }
        }
    }


    private lateinit var user: UserData;
    private var added = false
    private var aux:List<Map<String,*>>? = null
    private var ind = 0



    private fun putScoreBD(total: Int) {

        if( added ) return
        added = true

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val dateNow = simpleDateFormat.format(Date())

        //Consulta a Bd para obtener Scores actuales
        if( fbUser?.email == null ) finish()

        val userReference = fbUser?.email.let {
            if (it != null) {
                db.collection("Users").document(it)
            } else null
        }

        if (userReference != null) {
            userReference.get().addOnSuccessListener(OnSuccessListener { command: DocumentSnapshot ->
                try {
                    user = command.toObject(UserData::class.java)!!
                    var scores = user.scores
                    if( scores == null ) {
                        scores = hashMapOf<String, HashMap<String, List<Int>>>()
                    }
                    if( !scores.containsKey(this.id_ejercicio) ){
                        scores[id_ejercicio] = hashMapOf<String, List<Int>>()
                    }
                    if( !scores[id_ejercicio]?.containsKey(dateNow)!! ){
                        scores[id_ejercicio]!![dateNow] = mutableListOf();
                    }
                    scores[id_ejercicio]!![dateNow]!!.add(total)

                    userReference.update("scores", scores)
                    Toast.makeText(this, "done",Toast.LENGTH_SHORT).show()

                } catch (e: java.lang.Exception) {
                    Log.wtf("PUT DB", e.message)
                }
            })
        } else {
            finish()
        }

        /*
        val timestamp = Timestamp.now()
        antScore?.forEachIndexed { index, any ->
            if(antScore!![index] == id_ejercicio) {

                aux = antScore!![index+1] as List<Map<String,*>>
                ind = index+1
                return@forEachIndexed
            }
        }

        var none = true
        if(aux == null){
            aux = listOf()
            none = false
        }
        aux!!.toMutableList().add(mapOf("timestamp" to timestamp, "score" to total))

        if(!none) {

            antScore!!.toMutableList().add(id_ejercicio)
            antScore!!.toMutableList().add(aux)
        } else{
           antScore!!.toMutableList()[ind] = aux
        }

        userReference?.update("score", antScore)
        */

    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {

            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }
}


