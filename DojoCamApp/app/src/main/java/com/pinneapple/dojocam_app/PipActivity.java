package com.pinneapple.dojocam_app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class PipActivity extends AppCompatActivity {
 
    private Uri videoUri;
    private static final String Tag = "PIP_TAG";
    public static Activity pip;

    private int vid_dur;

    private VideoView videoView;
    private ImageButton piptn;
    private ActionBar actionBar;
    private PictureInPictureParams.Builder pictureInPictureParams;

    private Ml_model ml_model = new Ml_model();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        actionBar = getSupportActionBar();

        pip = this;

        videoView = findViewById(R.id.videoView);
        piptn = findViewById(R.id.piptn);

        setVideoView(getIntent());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pictureInPictureParams = new PictureInPictureParams.Builder();
        }

        piptn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureInPictureMode();
            }
        });
    }

    private void setVideoView(Intent intent) {
        String videoUrl = intent.getStringExtra("videoUrl");
        Log.d(Tag,"setVideoView: URL" + videoUrl);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoUri = Uri.parse(videoUrl);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
                pictureInPictureMode();
                vid_dur = videoView.getDuration();
                //ml_model.initTimer();
                timerCounter();
            }
        });
    }

    private void pictureInPictureMode(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Rational aspectRation = new Rational(videoView.getWidth(),videoView.getHeight());
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }
        else {
            Log.d(Tag,"pictureAndPictureMode: Doesn't support PIP");
        }
    }

    private Timer timer;
    private void timerCounter(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        };
        timer.schedule(task, 0, 1);
    }

    private void updateUI(){
        int current = videoView.getCurrentPosition();

        //Log.d(Tag,"Tiempo: "+current+"");
        if ( current  >= vid_dur) {
            timer.cancel();
        }
        if( current % (vid_dur/5) == 0 && current != 0 ) {
            videoView.pause();
        }
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if(!isInPictureInPictureMode()){
                pictureInPictureMode();
            }
            else{
                Log.d(Tag,"onUserLeaveHint: Already in PIP");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(isInPictureInPictureMode()){
                piptn.setVisibility(View.GONE);
                /*actionBar.hide();*/
            }
            else{
                piptn.setVisibility(View.VISIBLE);
                /*actionBar.show();*/
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setVideoView(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(videoView.isPlaying()){
            videoView.stopPlayback();
        }
    }

    public void pauseVideo(){
        videoView.pause();
    }

    public void continueVideo(){
        videoView.start();
    }

    public Integer getVideoDuration() {
        return videoView.getDuration();
    }

    public Integer getVideoTime() {
        return videoView.getCurrentPosition();
    }

}