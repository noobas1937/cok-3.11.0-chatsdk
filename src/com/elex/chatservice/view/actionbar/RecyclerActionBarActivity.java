package com.elex.chatservice.view.actionbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.ICocos2dxScreenLockListener;

public abstract class RecyclerActionBarActivity extends FragmentActivity
{
	public Button				backButton;
	public TextView				titleLabel;
	public RelativeLayout		actionbarLayout;
	public FrameLayout			channel_list_layout;
	public ProgressBar			activityProgressBar;
	private RelativeLayout		actionbar_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate start");
		super.onCreate(savedInstanceState);
		ChatServiceController.setCurrentActivity(this);
		ServiceInterface.pushActivity(this);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate 1");

		setContentView(R.layout.cs__actionbar_activity2);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate 2");
		getWindow().setBackgroundDrawable(null);

		if (ConfigManager.getInstance().scaleFontandUI)
			ConfigManager.calcScale(this);
		ScaleUtil.initialize(this);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate 3");

		channel_list_layout = (FrameLayout) findViewById(R.id.channel_list_layout);
		actionbar_layout = (RelativeLayout) findViewById(R.id.actionbar_layout);
		backButton = (Button) findViewById(R.id.cs__actionbar_backButton);
		titleLabel = (TextView) findViewById(R.id.cs__actionbar_titleLabel);
		actionbarLayout = (RelativeLayout) findViewById(R.id.cs__actionbar_layout);

		activityProgressBar = (ProgressBar) findViewById(R.id.cs__activity_progress_bar);
		hideProgressBar();
		showBackground();

		// nearby_btn_layout = (ViewStub) findViewById(R.id.nearby_btn_layout_stub);
		//
		// mail_state_layout = (ViewStub) findViewById(R.id.mail_state_layout_stub);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate 4");

		if (backButton != null)
		{
			backButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onBackButtonClick();
				}
			});
		}

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustSize();
			}
		};
		actionbar_layout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate END");
	}

	public void showRightBtn(View button)
	{
		// optionButton.setVisibility(View.GONE);
		// returnButton.setVisibility(View.GONE);
		// editButton.setVisibility(View.GONE);
		// writeButton.setVisibility(View.GONE);
		// showFriend.setVisibility(View.GONE);
		// showNearbyPeopleBtn.setVisibility(View.GONE);
		// nearby_btn_layout.setVisibility(View.GONE);
		// realtime_btn.setVisibility(View.GONE);
		// imageDelButton.setVisibility(View.GONE);
		// imageChooseComfirmButton.setVisibility(View.GONE);
		// allianceShareBtn.setVisibility(View.GONE);
		// allianceShareSend.setVisibility(View.GONE);
		// if (button != null)
		// button.setVisibility(View.VISIBLE);
	}

	protected void showBackground()
	{
	}

	public void showProgressBar()
	{
		activityProgressBar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar()
	{
		if (activityProgressBar != null && activityProgressBar.getVisibility() != View.GONE)
			activityProgressBar.setVisibility(View.GONE);
	}

	protected void onDestroy()
	{
		beforeExit();
		super.onDestroy();
	}

	private ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;
	private boolean									adjustSizeCompleted	= false;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void adjustSize()
	{
		if (!ConfigManager.getInstance().scaleFontandUI || adjustSizeCompleted)
		{
			if (backButton.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (backButton.getWidth() != 0 && !adjustSizeCompleted)
		{

			int height = (int) (88 * ConfigManager.scaleRatioButton);
			RelativeLayout.LayoutParams actionbarLayoutparam = (RelativeLayout.LayoutParams) actionbarLayout.getLayoutParams();
			actionbarLayoutparam.height = height;
			actionbarLayout.setLayoutParams(actionbarLayoutparam);

			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) backButton.getLayoutParams();
			param.width = height;
			param.height = height;
			backButton.setLayoutParams(param);

			ScaleUtil.adjustTextSize(titleLabel, ConfigManager.scaleRatio);

			adjustSizeCompleted = true;
		}
	}

	public double getScreenCorrectionFactor()
	{
		int density = getResources().getDisplayMetrics().densityDpi;

		if (density >= DisplayMetrics.DENSITY_XXHIGH)
		{
			// 小米note3是640，大于DENSITY_XXHIGH
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	}

	public int getToastPosY()
	{
		int[] location = { 0, 0 };
		channel_list_layout.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(this, 5);
	}

	// ---------------------------------------------------
	// 锁屏超过1分钟，返回后自动退出
	// ---------------------------------------------------

	public static ICocos2dxScreenLockListener	previousActivity;
	public static boolean						isReturnFromScreenLock	= false;
	private long								screenLockTime;
	/**
	 * TODO 无用变量 是否是主动触发的退出（否则可能是锁屏）
	 */
	protected boolean							isExiting				= false;
	private boolean								isScreenLocked			= false;

	@Override
	protected void onResume()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "this", this);

		super.onResume();
		ChatServiceController.setCurrentActivity(this);
		if (isScreenLocked)
		{
			isScreenLocked = false;
			// 锁屏返回，超时，退出聊天界面
			// 仅调用2dx的onResume
			if ((System.currentTimeMillis() - screenLockTime) > (1000 * 60))
			{
				isReturnFromScreenLock = true;
				ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity());
			}
			// 锁屏返回，未超时，不退出聊天界面
			else
			{
				if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener))
				{
					previousActivity.handle2dxResume();
				}
			}
		}
		else if (isSwitchingScreenState())
		{
			if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener))
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "previousActivity.handle2dxResume()");
				previousActivity.handle2dxResume();
			}
		}

		ChatServiceController.isNativeOpenning = false;
		refreshNetWorkState();
		WebRtcPeerManager.getInstance().showVoiceFloatWindow();
	}

	/**
	 * 是否正在锁屏或解锁
	 * 仅在onPause或onResume中时用，帮助判断当前的onPause或onResume是否是因为屏幕操作导致的
	 */
	private boolean isSwitchingScreenState()
	{
		return !ChatServiceController.isNativeOpenning && !ChatServiceController.isReturningToGame;
	}

	@Override
	protected void onPause()
	{
		// 当打开其它原生activity时，会出现并非锁屏的onPause，需要直接判断是否锁屏
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "this", this, "isScreenOn", isScreenOn, "previousActivity",
				previousActivity);

		super.onPause();
		if (!isScreenOn) // !isExiting &&
		{
			// 聊天界面锁屏
			isScreenLocked = true;
			screenLockTime = System.currentTimeMillis();
		}

		// 仅调用2dx的onPause
		if (isSwitchingScreenState())
		{
			if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener))
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "previousActivity.handle2dxPause()");
				previousActivity.handle2dxPause();
			}
		}
		WebRtcPeerManager.getInstance().hideVoiceFloatWindow();
		hideAllStateAnimation();
	}

	private void beforeExit()
	{
		isExiting = true;
		ServiceInterface.stopFlyHintTimer();
	}

	public void onBackButtonClick()
	{
		exitActivity();
	}

	public void onBackPressed()
	{
		exitActivity();
	}

	public void exitActivity()
	{
		ServiceInterface.popActivity(this);
		if (ServiceInterface.getNativeActivityCount() == 0)
		{
			ChatServiceController.isReturningToGame = true;
		}
		else
		{
			ChatServiceController.isNativeOpenning = true;
		}

		try
		{
			// 从onResume()调用时，可能在FragmentManagerImpl.checkStateLoss()出异常
			// java.lang.RuntimeException Unable to resume activity
			// {com.hcg.cok.gp/com.elex.chatservice.view.ChatActivity}:
			// java.lang.IllegalStateException: Can not perform this action
			// after onSaveInstanceState
			super.onBackPressed();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finish();
	}

	/**
	 * 因退栈被销毁的话，不会被调用
	 */
	public void finish()
	{
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	public boolean isSoftKeyBoardVisibile = false;

	public void hideSoftKeyBoard()
	{
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager != null && channel_list_layout != null && channel_list_layout.getWindowToken() != null)
		{
			inputManager.hideSoftInputFromWindow(channel_list_layout.getWindowToken(), 0);
			isSoftKeyBoardVisibile = false;
		}
	}

	public void showSoftKeyBoard(View view)
	{
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, 0);
		isSoftKeyBoardVisibile = true;
	}

	public void setSendBtnEnable(View view, boolean isEnable)
	{
		view.setEnabled(isEnable);
		CompatibleApiUtil.getInstance().setButtonAlpha(view, isEnable);
	}

	public void showNetworkConnectAnimation()
	{
		if (RecyclerActionBarActivity.this != null)
			RecyclerActionBarActivity.this.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
//					hideMailStateAnimation();
//					if (network_state_layout.getVisibility() != View.VISIBLE)
//						network_state_layout.setVisibility(View.VISIBLE);
//					stopNetworkConnectAnimation();
//					stopNetworkErrorAnimation();
//					network_state_view.setImageResource(R.drawable.network_connect_anim);
//					startNetworkConnectAnimation();
				}
			});

	}

	public void showNetwrokErrorAnimation()
	{
		if (RecyclerActionBarActivity.this != null)
			RecyclerActionBarActivity.this.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
//					hideMailStateAnimation();
//					if (network_state_layout.getVisibility() != View.VISIBLE)
//						network_state_layout.setVisibility(View.VISIBLE);
//					stopNetworkConnectAnimation();
//					stopNetworkErrorAnimation();
//					network_state_view.setImageResource(R.drawable.network_error);
//					startNetworkErrorAnimation();
				}
			});

	}

	public void startNetworkErrorAnimation()
	{
//		ViewHelper.setAlpha(network_state_view, 0);
//		if (networkErrorAnimSet == null)
//		{
//			networkErrorAnimSet = new AnimatorSet();
//			Animator showAnim = ObjectAnimator.ofFloat(network_state_view, "alpha", 0, 1.0f);
//			showAnim.setDuration(500);
//			Animator hideAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 1.0f, 0);
//			hideAnimator.setStartDelay(500);
//			Animator delayAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 0);
//			delayAnimator.setDuration(300);
//
//			networkErrorAnimSet.playSequentially(showAnim, hideAnimator, delayAnimator);
//		}
//
//		networkErrorAnimSet.start();
//		networkErrorAnimSet.addListener(new AnimatorListener()
//		{
//
//			@Override
//			public void onAnimationStart(Animator animation)
//			{
//			}
//
//			@Override
//			public void onAnimationRepeat(Animator animation)
//			{
//			}
//
//			@Override
//			public void onAnimationEnd(Animator animation)
//			{
//				animation.start();
//			}
//
//			@Override
//			public void onAnimationCancel(Animator animation)
//			{
//				animation.removeAllListeners();
//			}
//		});

	}

	public void startNetworkConnectAnimation()
	{
//		Drawable drawable = network_state_view.getDrawable();
//		if (drawable != null && drawable instanceof AnimationDrawable)
//		{
//			network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
//			if (network_state_animation != null && !network_state_animation.isRunning())
//				network_state_animation.start();
//		}
	}

	public void stopNetworkErrorAnimation()
	{
//		if (networkErrorAnimSet != null)
//			networkErrorAnimSet.cancel();
	}

	public void stopNetworkConnectAnimation()
	{
//		Drawable drawable = network_state_view.getDrawable();
//		if (drawable != null && drawable instanceof AnimationDrawable)
//		{
//			network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
//			if (network_state_animation != null && network_state_animation.isRunning())
//				network_state_animation.stop();
//		}
	}

	public void hideNetworkStateAnimation()
	{
		stopNetworkConnectAnimation();
		stopNetworkErrorAnimation();
//		if (network_state_layout.getVisibility() != View.GONE)
//			network_state_layout.setVisibility(View.GONE);
	}

	public void hideAllStateAnimation()
	{
		hideNetworkStateAnimation();
		hideMailStateAnimation();
	}

	public void hideMailStateAnimation()
	{
//		if (mail_state_layout != null && mail_state_layout.getVisibility() != View.GONE)
//			mail_state_layout.setVisibility(View.GONE);
	}

	public void refreshNetWorkState()
	{
		if (ConfigManager.isNetWorkConnecting() || ConfigManager.isWebSocketNetWorkConnecting())
			showNetworkConnectAnimation();
		else if (ConfigManager.isNetWorkError() || ConfigManager.isWebSocketNetWorkError())
			showNetwrokErrorAnimation();
		else
			hideAllStateAnimation();
	}

	@SuppressLint("Override")
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		PermissionManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}