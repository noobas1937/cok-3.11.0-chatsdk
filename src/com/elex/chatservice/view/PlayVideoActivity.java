package com.elex.chatservice.view;

import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.image.AsyncImageLoader;
import com.elex.chatservice.image.ImageLoaderListener;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.util.ScaleUtil;
import com.mi.mimsgsdk.video.MiVideoCallback;
import com.mi.mimsgsdk.video.MiVideoManager;
import com.mi.mimsgsdk.video.VideoBody;
import com.xiaomi.channel.common.audio.MessageType;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class PlayVideoActivity extends Activity {
	private VideoView mVideoView;
	private ImageView rewardImage;
	private MiVideoManager mVideoManager;
	private TextView load_txt;
	private int videoW = 0;
	private int videoH = 0;
	private boolean havePlayVideo = false;
	private String urlVideo = "";
	private String urlThumb = "";
	private MediaController mController = null;
	private View videoBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "video activity create");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_play_video);
		XiaoMiToolManager.setPlayVideoActivity(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			urlVideo = extras.getString("urlVideo");
			urlThumb = extras.getString("urlThumb");
		}

		videoBar = (View) findViewById(R.id.cs_video_title);
		
		rewardImage = (ImageView) findViewById(R.id.thumbnail_img);
		load_txt = (TextView) findViewById(R.id.load_txt);
		load_txt.setText(LanguageManager.getLangByKey("132144"));

		TextView txt_video_close = (TextView) findViewById(R.id.txt_video_close);
		txt_video_close.setText(LanguageManager.getLangByKey("133109"));
		txt_video_close.setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						onBackPressed();
					}
				});
		mVideoView = (VideoView) findViewById(R.id.player_view);
		mVideoManager = new MiVideoManager(getApplicationContext());
		mVideoManager.initVideoManager(new MiVideoCallback() {
			@Override
			public void onRecordFinished(VideoBody body) {

			}

			@Override
			public void onRecordCanceled() {

			}

			@Override
			public void onWriteLog(String arg0) {
			}

			@Override
			public void onRecordInterrupted() {
			}

			@Override
			public void onPlayBegin(String arg0) {
				// TODO Auto-generated method stub
				mVideoView.setVisibility(View.VISIBLE);
				rewardImage.setVisibility(View.GONE);
				load_txt.setVisibility(View.GONE);
				videoBar.setVisibility(View.VISIBLE);
				havePlayVideo = true;
				playOrStopBgMuisc(false);
			}

			@Override
			public void onPlayEnd(String arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(mController!=null){
					mController.show();
				}
				playOrStopBgMuisc(true);
			}

			@Override
			public void onRecordInitializationFailed() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onRecordInitializationSucceed() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onRecordStart() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}

		});
		mVideoManager.setMaxRecordDuration(5 * 60 * 1000);
		mVideoManager.setVideoView(mVideoView);

		rewardImage.setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(!havePlayVideo){
							if(videoBar.getVisibility()==View.VISIBLE){
								videoBar.setVisibility(View.GONE);
							}else{
								videoBar.setVisibility(View.VISIBLE);
							}
						}else{
							if(mController!=null){
								videoBar.setVisibility(mController.isShowing()?View.VISIBLE:View.GONE);
							}
						}
					}
				});
		
	
		mController = new MediaController(this);
		mVideoView.setMediaController(mController);
		mVideoView.requestFocus();
		//mController.show(0);
		mVideoView.setVisibility(View.VISIBLE);
		

		if (urlThumb.indexOf("http") != -1) {
			XiaoMiToolManager.getInstance().playThumUrl = urlThumb;
			XiaoMiToolManager.getInstance().downloadMediaFile(
					MessageType.IMAGE, urlThumb);
		} else {
			showThumb(urlThumb);
		}
		if (urlVideo.indexOf("http") != -1) {
			XiaoMiToolManager.getInstance().playVideoUrl = urlVideo;
			XiaoMiToolManager.getInstance().downloadMediaFile(
					MessageType.VIDEO, urlVideo);
		} else {
			mVideoManager.playVideoWithUrl(urlVideo);
		}
		updateBarStatus();
	}

    private void updateBarStatus(){
		new java.util.Timer().schedule(new TimerTask()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						if(!havePlayVideo){
							return ;
						}
						if(mController!=null){
							videoBar.setVisibility(mController.isShowing()?View.VISIBLE:View.GONE);
						}
					}
				});

			}
			
		},500,200);
    }

	public void showThumb(final String iconURL) {
		Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "showThumb=" + iconURL);
		ScaleUtil.initialize(this);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					AsyncImageLoader.getInstance().loadBitmapFromStore(iconURL,
							new ImageLoaderListener() {
								@Override
								public void onImageLoaded(Bitmap bitmap) {
									if (bitmap != null) {
										rewardImage.setImageBitmap(bitmap);
										if (videoW <= 0) {
											videoW = 480;
										}
										if (videoH <= 0) {
											videoH = 640;
										}
										videoW = bitmap.getWidth();
										videoH = bitmap.getHeight();

										changeComponentSize();

										mVideoView.setVisibility(View.VISIBLE);
										if (!havePlayVideo) {
											rewardImage
													.setVisibility(View.VISIBLE);
										}
									} else {
										Log.d("video", "thumb Bitmap null");
									}
								}
							});
				} catch (Exception e) {

				}
			}
		});
	}

	public void playVideo(final String url) {
		urlVideo = url;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					mVideoManager.playVideoWithUrl(urlVideo);
				} catch (Exception e) {

				}
			}
		});
	}

	private void changeComponentSize() {
		if (videoH == 0 || videoW == 0) {
			return;
		}
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels; // 屏幕宽度
												// ScaleUtil.getScreenWidth()
		int screenHeight = metrics.heightPixels; // 屏幕高度
		float s1 = (float) (screenWidth * 1.0 / videoW);
		float s2 = (float) (screenHeight * 1.0 / videoH);
		float useScale = Math.min(s1, s2);
		int useWidth = (int) (useScale * videoW);
		int useHeight = (int) (useScale * videoH);
		FrameLayout.LayoutParams param3 = (FrameLayout.LayoutParams) rewardImage
				.getLayoutParams();
		param3.width = useWidth;
		param3.height = useHeight;
		rewardImage.setLayoutParams(param3);
		
	}

	public void onVideoRecordClick(View view) {
		if (view.getId() == R.id.start_record) {
			mVideoManager.startRecordVideo(getWindowManager(), 1);
		} else if (view.getId() == R.id.stop_record) {
			mVideoManager.stopRecordVideo();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
		// Log.d("xiaomi", "ORIENTATION_LANDSCAPE");
		// }else{
		// Log.d("xiaomi", "ORIENTATION_---");
		// }
		changeComponentSize();
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
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		XiaoMiToolManager.setPlayVideoActivity(null);
		mVideoManager.destroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// 亲测在子线程开预览(也就是打开相机比较好用)
		super.onResume();
	}

	@Override
	protected void onPause() {
		mVideoManager.closePreview();
		super.onPause();
	}
}
