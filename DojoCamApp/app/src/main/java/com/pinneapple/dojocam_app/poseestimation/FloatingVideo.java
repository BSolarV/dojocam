package com.pinneapple.dojocam_app.poseestimation;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;

import com.pinneapple.dojocam_app.R;

public class FloatingVideo extends Service {

	private WindowManager windowManager;
	private FrameLayout videoContainer;
	private VideoView chatHead;
	private int windowWidth;
	private int windowHeigth;
	private String vid_path;

	private static final String Tag = "Floating_Video_TAG";

	private ServiceUpdateReceiver serviceUpdateReceiver;

	private final int MARGIN = 50;

	private final IBinder mBinder = (IBinder) new LocalBinder();

	public class LocalBinder extends Binder {
		public FloatingVideo getService() {
			// Return this instance of LocalService so clients can call public methods
			return FloatingVideo.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		//videoContainer = new FrameLayout(this);
		//videoContainer.setBackgroundColor(Color.rgb(0, 86, 88));

		vid_path = intent.getStringExtra("videoUrl");
		chatHead = new VideoView(this);

		initVideo();

		/*MediaController mediaController = new MediaController(this);
		chatHead.setMediaController(mediaController);

		//videoContainer.addView(chatHead);

		//String vid_path = "android.resource://" + getPackageName() + "/" + R.raw.braceadas_defensivas1
		chatHead.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d(Tag,"onprepared video prepared, playing ....");
				mp.setLooping(true);
				mp.start();
			}
		});*/


		//sendBroadcast(new Intent("RefreshTask.REFRESH_DATA_INTENT"));


		//Service listener
		if (serviceUpdateReceiver == null) serviceUpdateReceiver = new ServiceUpdateReceiver();

		IntentFilter intentFilterStart = new IntentFilter("RefreshTask.START_VIDEO");
		IntentFilter intentFilterStop = new IntentFilter("RefreshTask.PAUSE_VIDEO");

		registerReceiver(serviceUpdateReceiver, intentFilterStart);
		registerReceiver(serviceUpdateReceiver, intentFilterStop);


		int LAYOUT_FLAG;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				LAYOUT_FLAG,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);


		params.gravity = 51;
		params.x = 0;
		params.y = 100;

		windowManager.addView(chatHead, params);

		windowWidth = windowManager.getDefaultDisplay().getWidth();
		windowHeigth = windowManager.getDefaultDisplay().getHeight();

		try {
			chatHead.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;

				private boolean resize = false;

				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;
				private int finalX;
				private int finalY;

				private float offsetHeight;
				private float offsetWidth;

				@SuppressLint("ClickableViewAccessibility")
				@Override public boolean onTouch(View v, MotionEvent event) {
					int offset = 15;
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							initialX = paramsF.x;
							initialY = paramsF.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							if( initialX < event.getX() && event.getX() < initialX+MARGIN){
								if( initialY < event.getY() && event.getY() < initialY+MARGIN ){
									//resize = true;
								}else if(  initialY+v.getHeight()-MARGIN < event.getY() && event.getY() < initialY+v.getHeight() ){
									//resize = true;
								}
							}else if( initialX+v.getWidth()-MARGIN < initialTouchX && initialTouchX < initialX+v.getWidth()){
								if( initialY < event.getY() && event.getY() < initialY+MARGIN ){
									//resize = true;
								}else if(  initialY+v.getHeight()-MARGIN < event.getY() && event.getY() < initialY+v.getHeight() ){
									offsetHeight = event.getRawY() - v.getMeasuredHeight();
									offsetWidth = event.getRawX() - v.getMeasuredWidth();
									resize = true;
								}
							}
							break;

						case MotionEvent.ACTION_UP:
							if( resize ) {
								resize = false;
							} else {
								if( event.getRawX() > (float)windowWidth/2 ){
									if( event.getRawY() > (float)windowHeigth/2 ){
										finalX = windowWidth - chatHead.getWidth() - offset;
										finalY = windowHeigth - chatHead.getHeight() - offset;
									} else {
										finalX = windowWidth - chatHead.getWidth() - offset;
										finalY = offset;
									}
								} else {
									if( event.getRawY() > (float)windowHeigth/2 ){
										finalX = offset;
										finalY = windowHeigth - chatHead.getHeight() - offset;
									} else {
										finalX = offset;
										finalY = offset;
									}
								}
								animate(chatHead, paramsF.x, finalX, paramsF.y, finalY);
							}
							break;

						case MotionEvent.ACTION_MOVE:
							if( resize ){
								int dx = (int) (event.getX() - offsetWidth);
								int dy = (int) (event.getY() - offsetHeight);
								if( dx > dy ) {
									dy = (int) dx * 9 / 16;
								} else {
									dx = (int) dy * 16 / 9;
								}
								paramsF.width = Math.max(256, Math.min(854, dx));
								paramsF.height = Math.max(144, Math.min(480, dy));
								paramsF.horizontalWeight = 0;
								paramsF.verticalWeight = 0;
							} else {
								paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
								paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
							}
							windowManager.updateViewLayout(chatHead, paramsF);
							break;
					}

					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

		return START_NOT_STICKY;
	}




	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) windowManager.removeView(chatHead);

		//Service listener
		if (serviceUpdateReceiver != null) unregisterReceiver(serviceUpdateReceiver);
	}

	public void animate(final View v, int startX, int endX, int startY, int endY) {

		PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", startX, endX);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", startY, endY);

		ValueAnimator translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY);

		translator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) v.getLayoutParams();
				layoutParams.x = (Integer) valueAnimator.getAnimatedValue("x");
				layoutParams.y = (Integer) valueAnimator.getAnimatedValue("y");
				windowManager.updateViewLayout(v, layoutParams);
			}
		});

		translator.setDuration(100);
		translator.start();
	}

	public VideoView getVideoView() {
		return chatHead;
	}

	public void Make() {
		Toast.makeText(getApplicationContext(),"PRINT", Toast.LENGTH_SHORT).show();
	}

	public void initVideo() {

		Uri uri = Uri.parse(vid_path);
		/*MediaController mediaController = new MediaController(getApplicationContext());
		mediaController.setAnchorView(chatHead);
		chatHead.setMediaController(mediaController);*/
		chatHead.setVideoURI(uri);
		chatHead.start();
	}



	private void startVideo(){
		chatHead.start();
	}


	private void resumeVideo(){
		chatHead.resume();
	}

	private void pauseVideo(){
		chatHead.pause();
	}


	private class ServiceUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case "RefreshTask.START_VIDEO":
					startVideo();
					break;
				case "RefreshTask.PAUSE_VIDEO":
					pauseVideo();
					break;
			}
		}
	}

}
