package com.elex.chatservice.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.util.FileVideoUtils;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.mi.milink.sdk.client.ClientLog;
import com.mi.milink.sdk.util.FileUtils;
import com.mi.mimsgsdk.IMXMsgCallback;
import com.mi.mimsgsdk.MsgSdkManager;
import com.mi.mimsgsdk.controller.MessageController;
import com.mi.mimsgsdk.message.AudioBody;
import com.mi.mimsgsdk.message.CustomBody;
import com.mi.mimsgsdk.message.MiMsgBody;
import com.mi.mimsgsdk.message.TextBody;
import com.mi.mimsgsdk.service.aidl.MiMessage;
import com.mi.mimsgsdk.service.aidl.RetValue;
import com.mi.mimsgsdk.utils.GlobalData;
import com.mi.mimsgsdk.video.MiVideoCallback;
import com.mi.mimsgsdk.video.MiVideoManager;
import com.mi.mimsgsdk.video.VideoBody;
import com.xiaomi.channel.common.audio.MessageType;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class RecordVideoActivity extends Activity{
		public static  boolean sendFlag = false;
		private Random rand = new Random();
		private int randNum = rand.nextInt(3);
		private int msgId = randNum * 10000; // 生成msgId
		
	    private SurfaceView mPreviewView;
	    private VideoView mVideoView;
	    private MiVideoManager mVideoManager;
	    private String mCurrentUser;
	    private String mTargetUser;
	    private RelativeLayout record_layout;
	    
	    // mLocal前缀的只负责发送视频和上传视频
	    private VideoBody mLocalBody;
	    
	    private TextView mLogout;
	    public Button backButton;
	    private Button btnRecord;
	    private Button btnStopRecord;
	    private Button btnReview;
	    private Button sendRecord;
	    private Button cancelRecord;
	    private View btnControlView;
	    private TextView txtVideoTime;
	    private ImageView leftProgress;
	    private TextView txtTitle;
	    public Button btnCamera;
	    private ImageView recordBg;
	    private View videoView;
	    private View playView;
	    private View recordControlView;
	    private View recordBtnView;
	    private View timeRedPot;
	    private View record_press_view;
	    private TextView record_txt;
	    
	    private static long recordStartTime;
	    private boolean initFlag;
	    private boolean startRecordFlag;
	    private static int recordLen = 15;
	    private int proW = 0;
		private Timer				mTimer;
		private TimerTask			mTimerTask;
		private boolean onRecordRight;
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	
	    	ConfigManager.calcScale(this);
	    	requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.activity_record_video);
	        XiaoMiToolManager.getInstance().setCurrentRecordActivity(this);
	        mCurrentUser = XiaoMiToolManager.gUid;
	        mTargetUser = XiaoMiToolManager.gUid2;
	        
	        record_layout = (RelativeLayout)findViewById(R.id.record_layout);
	        
	        mPreviewView = (SurfaceView) findViewById(R.id.face_view);
	        mPreviewView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//mVideoManager.switchCamera();
				}
			});
	        mPreviewView.getHolder().addCallback(new SurfaceHolder.Callback() {
	            @Override
	            public void surfaceCreated(SurfaceHolder surfaceHolder) {
	            	
	            }

	            @Override
	            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	            }

	            @Override
	            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

	            }
	        });
	        mVideoView = (VideoView) findViewById(R.id.player_view);
		    videoView = (View) findViewById(R.id.video_view);
		    playView = (View) findViewById(R.id.play_view);
		    recordBtnView = (View) findViewById(R.id.record_btn_view);
		    recordControlView = (View) findViewById(R.id.record_control);
		    timeRedPot = (View) findViewById(R.id.time_red_pot);
	        btnControlView = (View) findViewById(R.id.control_btn_view);
	        txtVideoTime = (TextView) findViewById(R.id.txt_video_time);
	        //txtVideoTime.setText(LanguageManager.getLangByKey("132153"));
	        txtTitle = (TextView) findViewById(R.id.cs_txt_title);
	        txtTitle.setText(LanguageManager.getLangByKey("132163"));
	        
	        record_press_view = (View) findViewById(R.id.record_press_view);
	        record_txt = (TextView) findViewById(R.id.record_txt);
	        record_txt.setText(LanguageManager.getLangByKey("132153"));
	        
	        leftProgress = (ImageView) findViewById(R.id.left_progress);
	        btnRecord = (Button) findViewById(R.id.start_record);
	        btnStopRecord = (Button) findViewById(R.id.btn_record_stop);
	        btnRecord.setOnTouchListener(mTouchL);
	        
	        recordBg = (ImageView) findViewById(R.id.record_bg);
	        
	        btnReview = (Button) findViewById(R.id.review_video);
	        cancelRecord = (Button) findViewById(R.id.cancel_record);
	        sendRecord = (Button) findViewById(R.id.send_video);
	        btnCamera = (Button) findViewById(R.id.change_camera);
	        
	        //btnReview.setText( LanguageManager.getLangByKey("132146"));
	        cancelRecord.setText( LanguageManager.getLangByKey("132166"));
	        sendRecord.setText( LanguageManager.getLangByKey("132145"));
	
	        
	        backButton = (Button) findViewById(R.id.view_back_button);
	        backButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});

	        mVideoManager = new MiVideoManager(getApplicationContext());
	        mVideoManager.initVideoManager(mVideoCallBack);
	        mVideoManager.setMaxRecordDuration(recordLen * 1000);
	        mVideoManager.setSurfaceView(mPreviewView);
	        mVideoManager.setVideoView(mVideoView);
	        mLogout = (TextView) findViewById(R.id.log_show);
	        mLogout.setMovementMethod(new ScrollingMovementMethod());
	        //mLogout.setText(LanguageManager.getLangByKey("132153"));
	        ChatServiceController.toggleFullScreen(false, true, this);
	        reposition();
	        initFlag = true;
	        startRecordFlag = false;
	        delayInitStatus();
	        recordStartTime = System.currentTimeMillis();

	        Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "begin request right");
	        boolean flag = PermissionManager.getInstance().checkXMVideoPermission();
	        Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "end request right flag"+flag);
	        
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Boolean flag = JniController.getInstance().excuteJNIMethod("getShowVideoTipFlag",
			                new Object[] {});
					boolean flageV = true;
					if(flag!=null){
						flageV = flag.booleanValue();
					}
					record_press_view.setVisibility(flageV?View.VISIBLE:View.GONE);
				}
			});
	    }
	    
	    private MiVideoCallback mVideoCallBack = new MiVideoCallback() {
            @Override
            public void onRecordFinished(VideoBody body) {
                mLocalBody = body;
                XiaoMiToolManager.getInstance().mUrlPath = mLocalBody.getUrl();
                XiaoMiToolManager.getInstance().mUrlLength = mLocalBody.getLength();
                XiaoMiToolManager.getInstance().mUrlThumbPath = mLocalBody.getThumbnailUrl();
                
                XiaoMiToolManager.getInstance().uploadFileClocalVideoPath = mLocalBody.getUrl();
                XiaoMiToolManager.getInstance().uploadFileClocalThumPath = mLocalBody.getThumbnailUrl();
                Log.d("xiaomi", XiaoMiToolManager.getInstance().mUrlThumbPath);
                //Toast.makeText(getBaseContext(), "path: " + mLocalBody.getUrl() + " length: " + mLocalBody.getLength(), Toast.LENGTH_SHORT).show();
                updateUI("录制成功 path: " + mLocalBody.getUrl() + " length: " + mLocalBody.getLength() + " thumbPath " + mLocalBody.getThumbnailUrl());
                mVideoManager.openPreview();
                startRecordFlag = false;
                playOrStopBgMuisc(true);

	            videoRecordStop();
	    		if(leftProgress.getVisibility()==View.VISIBLE){
	    			leftProgress.setVisibility(View.GONE);
	    		}
	    		btnRecord.setVisibility(View.GONE);
            }

			@Override
			public void onRecordCanceled() {
				// TODO Auto-generated method stub
				 updateUI("录制取消");
				 mVideoManager.openPreview();
			}

			@Override
			public void onWriteLog(String arg0) {
				// TODO Auto-generated method stub
				updateUI(arg0);
			}

			@Override
			public void onRecordInterrupted() {
				// TODO Auto-generated method stub
				updateUI("录制异常中断");
				startRecordFlag = false;
				mVideoManager.openPreview();
			}

			@Override
			public void onPlayBegin(String arg0) {
				// TODO Auto-generated method stub
				updateUI("onPlayBegin path=" + arg0);
	    		if(btnReview.getVisibility()==View.VISIBLE){
	    			btnReview.setVisibility(View.GONE);
	    		}
	    		playOrStopBgMuisc(false);
			}

			@Override
			public void onPlayEnd(String arg0, boolean arg1) {
				// TODO Auto-generated method stub
				updateUI("onPlayEnd path=" + arg0 + " isSuccess=" + arg1);
	    		if(btnReview.getVisibility()==View.GONE && btnControlView.getVisibility()==View.VISIBLE){
	    			btnReview.setVisibility(View.VISIBLE);
	    		}
	    		playOrStopBgMuisc(true);
			}

			@Override
			public void onRecordInitializationFailed() {
				// TODO Auto-generated method stub
				updateUI("onRecordInitializationFailed");
			}

			@Override
			public void onRecordInitializationSucceed() {
				// TODO Auto-generated method stub
				updateUI("onRecordInitializationSucceed");
				initFlag = false;
			}

			@Override
			public void onRecordStart() {
				// TODO Auto-generated method stub
				startRecordFlag = true;
				recordStartTime = System.currentTimeMillis();
				updateUI("onRecordStart");
				playOrStopBgMuisc(false);
				btnCamera.setVisibility(View.GONE);
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}	
        };
	    		
	    private void delayInitStatus(){
			new java.util.Timer().schedule(new TimerTask()
			{
				public void run()
				{
						runOnUiThread(new Runnable() {
							public void run() {
								if(mVideoManager==null){
									return ;
								}
								 long tempTime = System.currentTimeMillis();
								 long millScend = tempTime - recordStartTime;
								 int gap = (int) ((tempTime - recordStartTime)/1000); 
								 if(initFlag && gap>=1){
									 boolean flag = checkPerssion();
									 if(flag){
										 mVideoManager.openPreview();
										 initFlag = false;
									 }
								 }
								 if(startRecordFlag){
									 if(gap>recordLen){
										 mVideoManager.stopRecordVideo();
										 gap = recordLen;
										 startRecordFlag = false;
									 }
									float leftTime = (float) ((recordLen*1000 - millScend)*1.0/(recordLen*1000));
									Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "task open preview leftTime= "+leftTime);
									updateProgress(leftTime);
									 int sec =  gap%60;
									 gap = gap/60;
									 int min =  gap%60;
									 gap = gap/60;
									 int hour =  gap%60;
									 String tempStr = "";
									 if(hour<=0){
										 tempStr = "00:";
									 }else if(hour<10){
										 tempStr += "0"+String.valueOf(hour)+":";
									 }else{
										 tempStr += String.valueOf(hour)+":";
									 }
									 if(min<=0){
										 tempStr += "00:";
									 }else if(min<10){
										 tempStr += "0"+String.valueOf(min)+":";
									 }else{
										 tempStr += String.valueOf(min)+":";
									 }
									 if(sec<=0){
										 tempStr += "00";
									 }else if(sec<10){
										 tempStr += "0"+String.valueOf(sec);
									 }else{
										 tempStr += String.valueOf(sec);
									 }
									 txtVideoTime.setText(tempStr);
								 }
							}
						});
				}
			}, 500,100);
	    }
	    
	    private void reposition(){
			ScaleUtil.initialize(this);
			proW = ScaleUtil.getScreenWidth();
			
			RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams)backButton.getLayoutParams();
			param2.width = (int) (56 * ConfigManager.scaleRatioButton);
			param2.height = (int) (67 * ConfigManager.scaleRatioButton);
			backButton.setLayoutParams(param2);

			RelativeLayout.LayoutParams param3 = (RelativeLayout.LayoutParams)btnCamera.getLayoutParams();
			param3.width = (int) (56 * ConfigManager.scaleRatioButton);
			param3.height = (int) (67 * ConfigManager.scaleRatioButton);
			btnCamera.setLayoutParams(param3);
			int swidth = ScaleUtil.getScreenWidth();
			int sHeigth = ScaleUtil.getScreenHeight();
			float rate = swidth/sHeigth;
			int viewW = swidth;
			int viewH = sHeigth;
			if(rate>0.75){
				viewH = (int) (swidth*1.33);
			}else{
				viewW = (int) (sHeigth * 0.75);
			}
			
			FrameLayout.LayoutParams previewParams = (FrameLayout.LayoutParams) mPreviewView.getLayoutParams();
			previewParams.width = viewW;
			previewParams.height = viewH;
			mPreviewView.setLayoutParams(previewParams);
			
			
			viewW = (int) (swidth * 0.7) - ScaleUtil.dip2px(this, 20);
			viewH = (int) ((ScaleUtil.getScreenHeight() - ScaleUtil.dip2px(this, 51))*0.7) - ScaleUtil.dip2px(this, 20);
			rate = viewW/viewH;
			if(rate>0.75){
				int tempH = (int) (viewW*1.33);
				if(tempH>viewH){
					viewW = (int) (viewH*0.75);
				}else{
					viewH = tempH;
				}
			}else{
				int tempW = (int) (viewH * 0.75);
				if(tempW>viewW){
					viewH = (int) (viewW*1.33);
				}else{
					viewW = tempW;
				}	
			}
			FrameLayout.LayoutParams recordBgParams = (FrameLayout.LayoutParams) recordBg.getLayoutParams();
			recordBgParams.width = viewW;
			recordBgParams.height = viewH;
			recordBg.setLayoutParams(recordBgParams);
			
			viewW = viewW - ScaleUtil.dip2px(this, 10);
			viewH = viewH - ScaleUtil.dip2px(this, 10);
			FrameLayout.LayoutParams videoParams = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
			videoParams.width = viewW;
			videoParams.height = viewH;
			mVideoView.setLayoutParams(videoParams);
			
	    }
	    
	    private void updateProgress(float percent){
	    	if(percent<0) percent = 0;
	    	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) leftProgress.getLayoutParams();
            params.width = (int) (percent*proW);
            leftProgress.setLayoutParams(params);
	    }
	    public boolean checkPerssion(){
	    	boolean flag = PermissionManager.isXMVideoPermissionsAvaiable();
	    	return flag;
	    }
	    
	    public void playOrStopBgMuisc(final boolean flag){
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                    	ChatServiceController.getInstance().setGameMusiceEnable(flag);
                    }
                    catch (Exception e)
                    {
                        
                    }
                }
            });
	    }
	    
	    public void onVideoRecordClick(View view) {
	    	if(view.getId()==R.id.view_back_button){
	    		super.onBackPressed();
	    	}else if(view.getId()==R.id.play_back_button){
	    		super.onBackPressed();
	    	}else if(view.getId()==R.id.change_camera){
	    		mVideoManager.switchCamera();
	    	}else if(view.getId()==R.id.cancel_record){
	    		videoView.setVisibility(View.VISIBLE);
	    		playView.setVisibility(View.GONE);
            	btnRecord.setVisibility(View.VISIBLE);
            	btnReview.setVisibility(View.GONE);
            	btnCamera.setVisibility(View.VISIBLE);
            	mPreviewView.setVisibility(View.VISIBLE);
            	mVideoView.setVisibility(View.GONE);
            	recordBtnView.setVisibility(View.VISIBLE);
            	txtVideoTime.setText("00:00:00");
            	mVideoManager.openPreview();
	    	}else if(view.getId()==R.id.review_video){
	    		txtVideoTime.setText("");
	    		btnReview.setVisibility(View.GONE);
            	mPreviewView.setVisibility(View.GONE);
            	mVideoView.setVisibility(View.VISIBLE);
	    		mVideoManager.playVideoWithUrl(XiaoMiToolManager.getInstance().mUrlPath);
	    		//mVideoManager.cancelRecordVideo();
	    	}else if(false){//view.getId()==R.id.stop_video
	    		mVideoManager.stopVideo();
	    	}else if(view.getId()==R.id.send_video){
            	if (mLocalBody != null && !TextUtils.isEmpty(mLocalBody.getUrl()) && mLocalBody.getLength() != 0 && mLocalBody.getSize() != 0) {
            		MiMessage message = getMessage(mCurrentUser,
							mTargetUser, mLocalBody, msgId++,
							MessageController.BODY_TYPE_VIDEO);
					updateUI("send message from: " + message.from + " to: "
							+ message.to + " message type video, url "
							+ mLocalBody.getUrl() + " length " + mLocalBody.getLength() + " thumbPath " + mLocalBody.getThumbnailUrl());

					XiaoMiToolManager.getInstance().recordVideo = mLocalBody;
					onBackPressed();
					XiaoMiToolManager.getInstance().sendRecordVideo();
            	}else{
            		Toast.makeText(getBaseContext(), "no video", Toast.LENGTH_SHORT).show();
            	}
	    	}else if(view.getId()==R.id.start_record && btnRecord.getVisibility()==View.VISIBLE){

	    		//recordBg.setVisibility(View.VISIBLE);
	    	}else if(view.getId()==R.id.btn_record_stop && btnStopRecord.getVisibility()==View.VISIBLE){

	    		//recordBg.setVisibility(View.GONE);
	    	}
	    }
	    
	    private MiMessage getMessage(String fromId, String toId, MiMsgBody body,
				long msgId, int bodyType) {
			MiMessage mMiMessage = new MiMessage();
			mMiMessage.from = fromId;
			mMiMessage.to = toId;
			mMiMessage.body = body;
			mMiMessage.msgId = msgId;
			mMiMessage.bodyType = bodyType;
			mMiMessage.sendTime = (int) (System.currentTimeMillis() / 1000);
			return mMiMessage;
		}
	    
	    private void updateUI(final String str) {
	    	runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					//mLogout.append(str + "\n");
				}
			});
	    }
	    
		private View.OnTouchListener mTouchL = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
		        if(v.getId() == R.id.start_record){  
		            if(event.getAction() == MotionEvent.ACTION_UP){  
		            	if(!onRecordRight){
		            		return false;
		            	}  
		                long tempTime = System.currentTimeMillis();
		                long gap = tempTime - recordStartTime;
			    		btnRecord.setVisibility(View.GONE);
		                if(gap<1000){
		                    createTimerTask();
		                    btnRecord.setEnabled(false);
		                }else{
		                	videoRecordStop();
		                }
			    		if(leftProgress.getVisibility()==View.VISIBLE){
			    			leftProgress.setVisibility(View.GONE);
			    		}
		            }   
		            if(event.getAction() == MotionEvent.ACTION_DOWN){  
		            	Log.d("xiaomi", "xiaomi record button ---> down"); 
		            	txtVideoTime.setText("00:00:00");
		            	onRecordRight = checkPerssion();
		            	if(!onRecordRight){
		            		PermissionManager.getInstance().checkXMVideoPermission();
		            	}else{
			                mVideoManager.startRecordVideo(getWindowManager(),1);
				    		if(leftProgress.getVisibility()==View.GONE){
				    			leftProgress.setVisibility(View.VISIBLE);
				    			updateProgress(1);
				    		} 
				    		btnRecord.setVisibility(View.VISIBLE);
				    		recordBtnView.setVisibility(View.GONE);
				    		timeRedPot.setVisibility(View.VISIBLE);
		            	}	
		            	record_press_view.setVisibility(View.GONE);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								JniController.getInstance().excuteJNIVoidMethod("addEventToFaceBook",
						                new Object[] {"pressRecordVideoBtn"});
							}
						});
		            }  
		        } 
				return false;
			}
		};
		
	private void videoRecordStop(){
    	mVideoManager.stopRecordVideo();
    	btnControlView.setVisibility(View.VISIBLE);
    	startRecordFlag = false;
    	playView.setVisibility(View.VISIBLE);
    	videoView.setVisibility(View.GONE);
    	txtVideoTime.setText("");
		btnReview.setVisibility(View.GONE);
    	mPreviewView.setVisibility(View.GONE);
    	mVideoView.setVisibility(View.VISIBLE);
    	timeRedPot.setVisibility(View.GONE);
    	mVideoManager.playVideoWithUrl(XiaoMiToolManager.getInstance().mUrlPath);
    	//record_press_view.setVisibility(View.VISIBLE);
		//recordBg.setLayoutParams(recordBgParams);
	}
	
	   @Override
	protected void onDestroy() {
	// TODO Auto-generated method stub
		   XiaoMiToolManager.getInstance().setCurrentRecordActivity(null);
		   mVideoManager.destroy();
		   //mSdkManager.destroy();
		   super.onDestroy();  
	}
	   
	@Override
	protected void onResume() {
		// 亲测在子线程开预览(也就是打开相机比较好用)
	       new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//mVideoManager.openPreview();
				Log.d("xiaomi", "onResume");
			}
	       }).start();
		   super.onResume();
	}
	   
	@Override
	protected void onPause() {
		mVideoManager.closePreview();
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		onPhotoActivityResult(requestCode, resultCode, data);
	}
	
  	protected void onPhotoActivityResult(int requestCode, int resultCode, Intent intent) {
  		Uri originalUri = intent.getData();
  		String path = FileVideoUtils.getPath(this, originalUri);
  		Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, path);
  		XiaoMiToolManager.getInstance().uploadLocalVideoToXiaoMi(path);
	}
  	
	// 点击视频按钮时调用
	public void showVideoPicker(int uid, int type){
		Intent openAlbumIntent;
		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
			openAlbumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI); 
		}else{
			openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
		} 
		openAlbumIntent.setType("video/*");
		startActivityForResult(openAlbumIntent, 2);
	}
	
    private void createTimerTask()
	{
	    stopTimerTask();
	    mTimer = new Timer();
	    mTimerTask = new TimerTask()
	    {
	        @Override
	        public void run()
	        {
	            runOnUiThread(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                    try
	                    {
			                long tempTime = System.currentTimeMillis();
			                float gap = (float) ((tempTime - recordStartTime)/1000);
	                    	if(startRecordFlag && gap>1.2){
		                    	stopTimerTask();
		                    	videoRecordStop();
		                    	btnRecord.setEnabled(true);
		                    	btnRecord.setVisibility(View.GONE);
	                    	}
	                    }
	                    catch (Exception e)
	                    {
	                        
	                    }
	                }
	            });
	        }
	    };
	    if (mTimer != null)
	        mTimer.schedule(mTimerTask, 500,500);
	}
    
    private void stopTimerTask()
    {
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

}

