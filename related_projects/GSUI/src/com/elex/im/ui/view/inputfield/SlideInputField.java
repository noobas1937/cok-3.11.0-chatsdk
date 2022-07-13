package com.elex.im.ui.view.inputfield;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.view.ChatFragment;
import com.elex.im.ui.view.misc.messenger.AndroidUtilities;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.AnimatorSetProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.ObjectAnimatorProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.ViewProxy;
import com.elex.im.ui.view.misc.ui.Components.FrameLayoutFixed;
import com.elex.im.ui.view.misc.ui.Components.LayoutHelper;

public class SlideInputField
{
    private RecordCircle recordCircle;
    private RecordDot recordDot;
    private LinearLayout slideLayout;
    private TextView recordTimeText;
    private FrameLayout recordPanel;
	private FrameLayout	inputFrameLayout;
	private FrameLayout	popFrameLayout;

	private final int COLOR_RECORD_BACK = 0xff1f2020; //0xffffffff;
	private final int COLOR_RECORD_DOT_BACK = 0xff1f2020; //0xffffffff;
	private final int COLOR_RECORD_SLIDE_TEXT = 0xffa69279; //0xff999999;
	private final int COLOR_RECORD_TIME_TEXT = 0xffa69279; //0xff4d4c4b;
	private ChatFragment	chatFragment;
	private Activity	activity;
	
	public void init(final Activity activity, final View view, final ChatFragment chatFragment)
	{
		this.activity = activity;
		this.chatFragment = chatFragment;

		inputFrameLayout = (FrameLayout) view.findViewById(R.id.inputFrameLayout);
        recordPanel = new FrameLayoutFixed(activity);
        recordPanel.setVisibility(View.GONE);
        recordPanel.setBackgroundColor(COLOR_RECORD_BACK);
        inputFrameLayout.addView(recordPanel, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM));

        slideLayout = new LinearLayout(activity);
        slideLayout.setOrientation(LinearLayout.HORIZONTAL);
        recordPanel.addView(slideLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 30, 0, 0, 0));

        ImageView slideArrowImageView = new ImageView(activity);
        slideArrowImageView.setImageResource(R.drawable.voice_slidearrow);
        slideLayout.addView(slideArrowImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

        TextView slideTextView = new TextView(activity);
        slideTextView.setText(LanguageManager.getLangByKey(LanguageKeys.AUDIO_SLIDE_TO_CANCEL));
        slideTextView.setTextColor(COLOR_RECORD_SLIDE_TEXT);
        slideTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		ScaleUtil.adjustTextSize(slideTextView, ChatFragment.getAudioUIScale());
        slideLayout.addView(slideTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(AndroidUtilities.dp(13), 0, 0, 0);
        linearLayout.setBackgroundColor(COLOR_RECORD_DOT_BACK);
        recordPanel.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        recordDot = new RecordDot(activity);
        linearLayout.addView(recordDot, LayoutHelper.createLinear(11, 11, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

        recordTimeText = new TextView(activity);
        recordTimeText.setText("00:00");
        recordTimeText.setTextColor(COLOR_RECORD_TIME_TEXT);
        recordTimeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		ScaleUtil.adjustTextSize(recordTimeText, ChatFragment.getAudioUIScale());
		
        linearLayout.addView(recordTimeText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

		recordCircle = new RecordCircle(activity);
		recordCircle.setVisibility(View.GONE);
		popFrameLayout = (FrameLayout) view.findViewById(R.id.popFrameLayout);
//		sizeNotifierFrameLayout = new SizeNotifierFrameLayout(activity);
//		ColorDrawable back = new ColorDrawable(0x000000);
//		back.setAlpha(0);
//		sizeNotifierFrameLayout.setBackgroundImage(back);
//		popFrameLayout.addView(sizeNotifierFrameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//		sizeNotifierFrameLayout.addView(recordCircle, LayoutHelper.createFrame(124, 124, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -36, -38));
		popFrameLayout.addView(recordCircle, LayoutHelper.createFrame((int) (124 * ChatFragment.getAudioUIScale()), (int) (124 * ChatFragment.getAudioUIScale()), Gravity.BOTTOM | Gravity.RIGHT, 0, 0, (int) (-36 * ChatFragment.getAudioUIScale()), (int) (-38 * ChatFragment.getAudioUIScale())));
		
//		fragmentLayout.addView(recordCircle, LayoutHelper.createFrame(124, 124, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -36, -38));

		chatFragment.voice_rec_button_layout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                	
                	if (!PermissionManager.checkXMRecordPermission(UIManager.getCurrentActivity()))
					{
						return false;
					}
                	
                	recordBtnUp = false;
                	
                	Timer timer = new Timer();
                	TimerTask timerTask = new TimerTask()
					{
						
						@Override
						public void run()
						{
		                    System.out.println("voice_rec_button_layout onTouch ACTION_DOWN");
		                    if(activity!=null)
		                    {
		                    	activity.runOnUiThread(new Runnable()
								{
									
									@Override
									public void run()
									{
										if(!recordBtnUp)
										{
											startedDraggingX = -1;
											chatFragment.gotoLastLine();
						                    XiaoMiToolManager.getInstance().startRecord();
						                    startRecordTimer();

						                    //  录音开始的回调中的处理
						                    if (!recordingAudio) {
						                        recordingAudio = true;
						                        updateAudioRecordIntefrace();
						                    }
						                    
						                    updateAudioRecordIntefrace();
						                    chatFragment.voice_rec_button_layout.getParent().requestDisallowInterceptTouchEvent(true);
										}
									}
								});
		                    }
		                    
						}
					};
					timer.schedule(timerTask, 300);
					
                } else if ((motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL)) {
                	recordBtnUp = true;
                	startedDraggingX = -1;
                	if(recordingAudio)
                	{
                		XiaoMiToolManager.getInstance().stopRecord(recordingAudio);
                    	stopRecordTimer();
                	}
                    exitRecordingUI();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && recordingAudio) {
                    float x = motionEvent.getX();
                    if (x < -distCanMove) {
                        if(recordingAudio){
                			LogUtil.trackPageView("Audio-cancelRecord");
                        	XiaoMiToolManager.getInstance().stopRecord(false);
                        	stopRecordTimer();
                        }
                        
                        exitRecordingUI();
                    }

                    x = x + ViewProxy.getX(chatFragment.voice_rec_button_layout);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        ViewProxy.setTranslationX(recordCircle, dist);
                        params.leftMargin = AndroidUtilities.dp(30) + (int) dist;
                        slideLayout.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        ViewProxy.setAlpha(slideLayout, alpha);
                    }
                    if (x <= ViewProxy.getX(slideLayout) + slideLayout.getWidth() + AndroidUtilities.dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (recordPanel.getMeasuredWidth() - slideLayout.getMeasuredWidth() - AndroidUtilities.dp(48)) / 2.0f;
                            if (distCanMove <= 0) {
                                distCanMove = AndroidUtilities.dp(80);
                            } else if (distCanMove > AndroidUtilities.dp(80)) {
                                distCanMove = AndroidUtilities.dp(80);
                            }
                        }
                    }
                    if (params.leftMargin > AndroidUtilities.dp(30)) {
                        params.leftMargin = AndroidUtilities.dp(30);
                        ViewProxy.setTranslationX(recordCircle, 0);
                        slideLayout.setLayoutParams(params);
                        ViewProxy.setAlpha(slideLayout, 1);
                        startedDraggingX = -1;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
	}
	

    private String lastTimeString;
    private float startedDraggingX = -1;
    private boolean recordingAudio = false;
    private boolean recordBtnUp = true;
    private float distCanMove = AndroidUtilities.dp(80);
    private int audioInterfaceState;
    private PowerManager.WakeLock mWakeLock;
    
    private void updateAudioRecordIntefrace() {
        if (recordingAudio) {
            if (audioInterfaceState == 1) {
                return;
            }
            audioInterfaceState = 1;
            try {
                if (mWakeLock == null) {
                    PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "audio record lock");
                    mWakeLock.acquire();
                }
            } catch (Exception e) {
//                FileLog.e("tmessages", e);
            }
            AndroidUtilities.lockOrientation(activity);

    		chatFragment.onRecordPanelShown(true);
            recordPanel.setVisibility(View.VISIBLE);
            recordCircle.setVisibility(View.VISIBLE);
            recordCircle.setAmplitude(0);
            recordTimeText.setText("00:00");
            recordDot.resetAlpha();
            lastTimeString = null;

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
            params.leftMargin = AndroidUtilities.dp(30);
            slideLayout.setLayoutParams(params);
            ViewProxy.setAlpha(slideLayout, 1);
            ViewProxy.setX(recordPanel, AndroidUtilities.displaySize.x);
            ViewProxy.setTranslationX(recordCircle, 0);
            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSetProxy();
            runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", 0),
                    ObjectAnimatorProxy.ofFloat(recordCircle, "scale", (float) ChatFragment.getAudioUIScale()),
                    ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "alpha", 0));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animator) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animator)) {
                        ViewProxy.setX(recordPanel, 0);
                        runningAnimationAudio = null;
                    }
                }
            });
            runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
            runningAnimationAudio.start();
        } else {
            if (mWakeLock != null) {
                try {
                    mWakeLock.release();
                    mWakeLock = null;
                } catch (Exception e) {
//                    FileLog.e("tmessages", e);
                }
            }
            AndroidUtilities.unlockOrientation(activity);
            if (audioInterfaceState == 0) {
                return;
            }
            audioInterfaceState = 0;

            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSetProxy();
            runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", AndroidUtilities.displaySize.x),
                    ObjectAnimatorProxy.ofFloat(recordCircle, "scale", 0.0f),
                    ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "alpha", 1.0f));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animator) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animator)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
                        params.leftMargin = AndroidUtilities.dp(30);
                        slideLayout.setLayoutParams(params);
                        ViewProxy.setAlpha(slideLayout, 1);
                        chatFragment.onRecordPanelShown(false);
                        recordPanel.setVisibility(View.GONE);
                        recordCircle.setVisibility(View.GONE);
                        runningAnimationAudio = null;
                    }
                }
            });
            runningAnimationAudio.setInterpolator(new AccelerateInterpolator());
            runningAnimationAudio.start();
        }
    }

	private Timer recordTimer;
	private TimerTask recordTimerTask;
	private long recordStartTime;
    private void startRecordTimer()
	{
    	stopRecordTimer();
		recordTimer = new Timer();
		recordTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				long timePassed = System.currentTimeMillis() - recordStartTime;
	            Long time = (Long) timePassed / 1000;
	            final String str = String.format("%02d:%02d", time / 60, time % 60);
	            if (lastTimeString == null || !lastTimeString.equals(str)) {
	            	lastTimeString = str;
	                if (time % 5 == 0) {
//	                    MessagesController.getInstance().sendTyping(dialog_id, 1, 0);
	                }
	                if (recordTimeText != null && activity != null) {
	    				activity.runOnUiThread(new Runnable()
	    				{
	    					@Override
	    					public void run()
	    					{
	    						try
	    						{
	    		                    recordTimeText.setText(str);
	    		                    
//	    		    	            if (recordCircle != null) {
//	    		    	                recordCircle.setAmplitude((Double) 100.0);
//	    		    	            }
	    						}
	    						catch (Exception e)
	    						{
	    							LogUtil.printException(e);
	    						}
	    					}
	    				});
	                }
	            }
			}
		};
		if (recordTimer != null){
			recordTimer.schedule(recordTimerTask, 500, 100);
			recordStartTime = System.currentTimeMillis();
		}
	}

	private void stopRecordTimer()
	{
		if (recordTimer != null)
		{
			recordTimer.cancel();
			recordTimer.purge();
			recordTimer = null;
		}
	}
    
    public void didReceivedNotification(String id, Object... args) {
        if (id.equals("NotificationCenter.recordProgressChanged")) {
            Long time = (Long) args[0] / 1000;
            String str = String.format("%02d:%02d", time / 60, time % 60);
            if (lastTimeString == null || !lastTimeString.equals(str)) {
                if (time % 5 == 0) {
//                    MessagesController.getInstance().sendTyping(dialog_id, 1, 0);
                }
                if (recordTimeText != null) {
                    recordTimeText.setText(str);
                }
            }
            if (recordCircle != null) {
                recordCircle.setAmplitude((Double) args[1]);
            }
        } else if (id.equals("NotificationCenter.recordStartError") || id.equals("NotificationCenter.recordStopped")) {
            if (recordingAudio) {
                exitRecordingUI();
            }
        } else if (id.equals("NotificationCenter.recordStarted")) {
            if (!recordingAudio) {
                recordingAudio = true;
                updateAudioRecordIntefrace();
            }
        } else if (id.equals("NotificationCenter.audioDidSent")) {
//            if (delegate != null) {
//                delegate.onMessageSend(null);
//            }
        }
    }
    
	public void exitRecordingUI()
	{
		recordingAudio = false;
		updateAudioRecordIntefrace();
	}


    private AnimatorSetProxy runningAnimation;
    private AnimatorSetProxy runningAnimation2;
    private AnimatorSetProxy runningAnimationAudio;
    private int runningAnimationType;
    /*
     * 语音和发送按钮的动画切换
     */
	public void checkSendButton(final boolean animated) {
        if (chatFragment.replyField.getText().length() > 0) {
            if (chatFragment.voice_rec_button_layout.getVisibility() == View.VISIBLE) {
                if (animated) {
                    if (runningAnimationType == 1) {
                        return;
                    }
                    if (runningAnimation != null) {
                        runningAnimation.cancel();
                        runningAnimation = null;
                    }
                    if (runningAnimation2 != null) {
                        runningAnimation2.cancel();
                        runningAnimation2 = null;
                    }

                    chatFragment.sendMessageLayout.setVisibility(View.VISIBLE);
                    runningAnimation = new AnimatorSetProxy();
                    runningAnimationType = 1;

                    runningAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "scaleX", 0.1f),
                            ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "scaleY", 0.1f),
                            ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "alpha", 0.0f),
                            ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "alpha", 1.0f)
                    );

                    runningAnimation.setDuration(150);
                    runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (runningAnimation != null && runningAnimation.equals(animation)) {
                                chatFragment.sendMessageLayout.setVisibility(View.VISIBLE);
                                chatFragment.voice_rec_button_layout.setVisibility(View.GONE);
                                chatFragment.voice_rec_button_layout.clearAnimation();
                                runningAnimation = null;
                                runningAnimationType = 0;
                            }
                        }
                    });
                    runningAnimation.start();
                } else {
                    ViewProxy.setScaleX(chatFragment.voice_rec_button_layout, 0.1f);
                    ViewProxy.setScaleY(chatFragment.voice_rec_button_layout, 0.1f);
                    ViewProxy.setAlpha(chatFragment.voice_rec_button_layout, 0.0f);
                    ViewProxy.setScaleX(chatFragment.sendMessageLayout, 1.0f);
                    ViewProxy.setScaleY(chatFragment.sendMessageLayout, 1.0f);
                    ViewProxy.setAlpha(chatFragment.sendMessageLayout, 1.0f);
                    chatFragment.sendMessageLayout.setVisibility(View.VISIBLE);
                    chatFragment.voice_rec_button_layout.setVisibility(View.GONE);
                    chatFragment.voice_rec_button_layout.clearAnimation();
                }
            }
        } else {
            if (animated) {
                if (runningAnimationType == 2) {
                    return;
                }

                if (runningAnimation != null) {
                    runningAnimation.cancel();
                    runningAnimation = null;
                }
                if (runningAnimation2 != null) {
                    runningAnimation2.cancel();
                    runningAnimation2 = null;
                }

                chatFragment.voice_rec_button_layout.setVisibility(View.VISIBLE);
                runningAnimation = new AnimatorSetProxy();
                runningAnimationType = 2;

                runningAnimation.playTogether(
                        ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "scaleX", 0.1f),
                        ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "scaleY", 0.1f),
                        ObjectAnimatorProxy.ofFloat(chatFragment.sendMessageLayout, "alpha", 0.0f),
                        ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "scaleX", 1.0f),
                        ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "scaleY", 1.0f),
                        ObjectAnimatorProxy.ofFloat(chatFragment.voice_rec_button_layout, "alpha", 1.0f)
                );

                runningAnimation.setDuration(150);
                runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (runningAnimation != null && runningAnimation.equals(animation)) {
                            chatFragment.sendMessageLayout.setVisibility(View.GONE);
                            chatFragment.sendMessageLayout.clearAnimation();
                            chatFragment.voice_rec_button_layout.setVisibility(View.VISIBLE);
                            runningAnimation = null;
                            runningAnimationType = 0;
                        }
                    }
                });
                runningAnimation.start();
            } else {
                ViewProxy.setScaleX(chatFragment.sendMessageLayout, 0.1f);
                ViewProxy.setScaleY(chatFragment.sendMessageLayout, 0.1f);
                ViewProxy.setAlpha(chatFragment.sendMessageLayout, 0.0f);
                ViewProxy.setScaleX(chatFragment.voice_rec_button_layout, 1.0f);
                ViewProxy.setScaleY(chatFragment.voice_rec_button_layout, 1.0f);
                ViewProxy.setAlpha(chatFragment.voice_rec_button_layout, 1.0f);
                chatFragment.sendMessageLayout.setVisibility(View.GONE);
                chatFragment.sendMessageLayout.clearAnimation();
                chatFragment.voice_rec_button_layout.setVisibility(View.VISIBLE);
            }
        }
    }
}
