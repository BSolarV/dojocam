package com.pinneapple.dojocam_app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.VideoView;

public class PipActivity extends AppCompatActivity {
 
    private Uri videoUri;
    private static final String Tag = "PIP_TAG";

    private VideoView videoView;
    private ImageButton piptn;
    private ActionBar actionBar;
    private PictureInPictureParams.Builder pictureInPictureParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        actionBar = getSupportActionBar();

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
                Log.d(Tag,"onprepared video prepared, playing ....");
                mp.start();
            }
        });
    }

    private void pictureInPictureMode(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(Tag,"pictureAndPictureMode: supports PIP");
            Rational aspectRation = new Rational(videoView.getWidth(),videoView.getHeight());
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }
        else {
            Log.d(Tag,"pictureAndPictureMode: Doesn't support PIP");
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if(!isInPictureInPictureMode()){
                Log.d(Tag,"onUserLeaveHint: Was not in PIP");
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
                Log.d(Tag,"onPictureAndPictureModeChanged: entered Pip");
                piptn.setVisibility(View.GONE);
                /*actionBar.hide();*/
            }
            else{
                Log.d(Tag,"onPictureAndPictureModeChanged: exited Pip");
                piptn.setVisibility(View.VISIBLE);
                /*actionBar.show();*/
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(Tag,"onNewIntent: play new video");
        setVideoView(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(videoView.isPlaying()){
            videoView.stopPlayback();
        }
    }
}