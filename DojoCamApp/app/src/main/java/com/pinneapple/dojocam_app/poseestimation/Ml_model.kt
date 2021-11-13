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
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.poseestimation.camera.CameraSource
import org.tensorflow.lite.examples.poseestimation.data.Device
import org.tensorflow.lite.examples.poseestimation.ml.ModelType
import org.tensorflow.lite.examples.poseestimation.ml.MoveNet
import org.tensorflow.lite.examples.poseestimation.ml.PoseClassifier
import org.tensorflow.lite.examples.poseestimation.ml.PoseNet
import java.util.*

import com.pinneapple.dojocam_app.objets.LocalBinder

import android.os.IBinder

import android.os.Binder
import kotlin.properties.Delegates
import android.view.MotionEvent
import android.content.ComponentName

import android.content.ServiceConnection
import android.media.MediaPlayer


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
    private lateinit var namefile: String
    private lateinit var vid_path: String
    private lateinit var videoPip: Intent
    private var init: Boolean = false

    //receiver
    private var serviceUpdateReceiver: ServiceUpdateReceiver? = null

    private var current  = 0
    private lateinit var floatingVideoVideo: VideoView


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

    private var cameraSource: CameraSource? = null
    private var isClassifyPose = false
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
            isClassifyPose = isChecked
            isPoseClassifier()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ml)


        //windu
        val b = intent.extras
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

        if (!isCameraPermissionGranted()) {
            requestPermission()
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
                //timerCounter()
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

    override fun onResume() {
        openCamera()
        cameraSource?.resume()

        videoPip = Intent(this, FloatingVideo::class.java)

        videoPip.putExtra(
            "videoUrl",
            vid_path
        )
        startService( videoPip )


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
            override fun run() {
                runOnUiThread { updateUI() }
            }
        }

        timer!!.schedule(task, 0, 100)
    }

    private var isChecking: Boolean = false
    private var isPaused: Boolean = false
    fun updateUI() {
        //sendBroadcast(Intent("RefreshTask.PAUSE_VIDEO"))
        current = floatingVideoVideo.currentPosition

        if( cameraSource?.getFeedbackStatus() != true ){
            cameraSource?.enableFeedbackPose()
        }

        if( current % 2 == 0 ){
            floatingVideoVideo.pause()
            isPaused = true
        }

        if( isPaused ){
            var result = cameraSource?.checkPose(current.toString())
            if ( result == true ) {

                //sendBroadcast(Intent("RefreshTask.START_VIDEO"))

                mService.startVideo()
            }
        }

        //Teminar con el timer

        /*if ( current -1 == mService.videoDuration) {
            timer!!.cancel()
        }*/
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
