package com.elex.im.ui.view.actionbar;

import java.util.Timer;
import java.util.TimerTask;

import net.londatiga.android.PopupWindows;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.im.CokChannelDef;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.event.WSStatusEvent;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.event.ScreenStatusEvent;
import com.elex.im.ui.util.CompatibleApiUtil;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.util.MsgUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public abstract class MyActionBarActivity extends FragmentActivity
{
	private View mContentView;
	public Button				backButton;
	public TextView				titleLabel;
	public Button				optionButton;
	public Button				editButton;
	public Button				writeButton;
	public Button				returnButton;
	public Button				showFriend;
	public Button				allianceShareBtn;
	public Button				allianceShareSend;
	public Button				imageDelButton;
	public Button				imageChooseComfirmButton;
	public RelativeLayout		actionbarLayout;
	protected int				fragmentHolderId;
	public ActionBarFragment	fragment;
	protected Bundle			bundle;
	protected RelativeLayout	fragmentLayout;
	public FrameLayout			fragment_holder;
	public ProgressBar			activityProgressBar;
	public LinearLayout			reward_loading_layout;
	public TextView				loading_textview;
	public FrameLayout			reward_fly_layout;
	private LayoutInflater		inflater;
	// 红包面板
	private FrameLayout			red_package_root_layout;
	private TextView			red_package_sendername;
	private TextView			red_package_sendertip;
	// private TextView red_package_msg;
	private ImageView			red_package_senderHeaderPic;
	private ImageView			red_package_HeaderPicContainer;
	private TextView			red_package_detail;
	private TextView			red_package_warning;
	private LinearLayout		red_package_warning_layout;
	private ImageView			red_package_open_btn;
	private LinearLayout		red_package_unhandlelayout;
	private View				red_package_background_layout;
	private Msg				redPackageItem = null;
	private PopupWindows		popupMenu				= null;
	private ImageView			network_state_view;
	private FrameLayout			network_state_layout;
	private AnimationDrawable	network_state_animation	= null;
	private FrameLayout			mail_state_layout;
	private ImageView			mail_pull1;
	private ImageView			mail_pull2;
	private AnimatorSet			networkErrorAnimSet;
	private Animation			mail_state_anim2;
	private FrameLayout			actionbar_layout;
	
	protected boolean			hasTab;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		UIManager.setCurrentActivity(this);
		UIManager.pushActivity(this);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// actionBarHelper.onCreate(savedInstanceState);
		// actionBarHelper.setDisplayHomeAsUpEnabled(true);
		
		if(!hasTab)
			setContentView(R.layout.cs__chat_activity);
		else
			setContentView(R.layout.cs__chat_activity_tab);
		
		getWindow().setBackgroundDrawable(null);
		
		if (ConfigManager.getInstance().scaleFontandUI)
		{
			ConfigManager.calcScale(this);
		}

		actionbar_layout = (FrameLayout) findViewById(R.id.actionbar_layout);
		backButton = (Button) findViewById(R.id.cs__actionbar_backButton);
		titleLabel = (TextView) findViewById(R.id.cs__actionbar_titleLabel);
		optionButton = (Button) findViewById(R.id.cs__actionbar_optionButton);
		editButton = (Button) findViewById(R.id.cs__actionbar_editButton);
		writeButton = (Button) findViewById(R.id.cs__actionbar_writeButton);
		returnButton = (Button) findViewById(R.id.cs__actionbar_returnButton);
		showFriend = (Button) findViewById(R.id.cs__actionbar_showFriendButton);
		allianceShareBtn = (Button) findViewById(R.id.cs__actionbar_allianceCircleButton);
		imageChooseComfirmButton = (Button) findViewById(R.id.cs__actionbar_imageChooseComfirmButton);
		imageDelButton = (Button) findViewById(R.id.cs__actionbar_imageDelButton);
		allianceShareSend = (Button) findViewById(R.id.cs__actionbar_allianceShareSendButton);
		actionbarLayout = (RelativeLayout) findViewById(R.id.cs__actionbar_layout);
		fragmentLayout = (RelativeLayout) findViewById(R.id.cs__activity_fragment_layout);
		network_state_view = (ImageView) findViewById(R.id.network_state_view);
		network_state_layout = (FrameLayout) findViewById(R.id.network_state_layout);

		mail_state_layout = (FrameLayout) findViewById(R.id.mail_state_layout);
		mail_pull1 = (ImageView) findViewById(R.id.mail_pull1);
		mail_pull2 = (ImageView) findViewById(R.id.mail_pull2);

		// ConfigManager.network_state = ConfigManager.NETWORK_CONNECTING;

		if(allianceShareBtn!=null)
		{
			allianceShareBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
//					if (popupMenu == null)
//						popupMenu = new PopupWindows(MyActionBarActivity.this);
//					if (!popupMenu.isShowing())
//						popupMenu.showAtLocation(v, Gravity.BOTTOM, 0, 0);
				}
			});
		}

		if (red_package_root_layout != null)
			red_package_root_layout.setVisibility(View.GONE);

		if(backButton!=null)
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

		this.fragmentHolderId = R.id.cs__activity_fragment_holder;
		fragment_holder = (FrameLayout) findViewById(fragmentHolderId);
		
		if(fragment_holder != null && mContentView != null)
		{
			fragment_holder.addView(mContentView);
		}

		activityProgressBar = (ProgressBar) findViewById(R.id.cs__activity_progress_bar);
		hideProgressBar();

		reward_loading_layout = (LinearLayout) findViewById(R.id.reward_loading_layout);
		ViewHelper.setAlpha(reward_loading_layout, 0.4f);
		hideRewardLoadingPopup();

		loading_textview = (TextView) findViewById(R.id.loading_textview);
		// loading_textview.setText(LanguageManager.getLangByKey(LanguageKeys.));

		reward_fly_layout = (FrameLayout) findViewById(R.id.reward_fly_layout);

		if (fragmentClass != null)
			showFragment(fragmentClass.getName());

		showBackground();
		ScaleUtil.initialize(this);

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustSize();
			}
		};
		actionbar_layout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
	}

	public void hidePopupMenu()
	{
//		if (popupMenu != null && popupMenu.isShowing())
//			popupMenu.dismiss();
	}

	/**
	 * 在加载fragment前先在fragment容器上显示背景
	 * <p>
	 * 以防止因为fragment初始化较慢，内容区出现黑屏
	 */
	protected void showBackground()
	{
	}

	private Timer rewardTimer;
	private TimerTask rewardTimerTask;
	
	private void stopRewardTimer()
	{
		if(rewardTimerTask!=null)
			rewardTimerTask.cancel();
		if(rewardTimer!=null)
		{
			rewardTimer.cancel();
			rewardTimer.purge();
			rewardTimer = null;
		}
	}
	
	public void showRewardLoadingPopup()
	{
		reward_loading_layout.setVisibility(View.VISIBLE);
		stopRewardTimer();
		rewardTimer = new Timer();
		rewardTimerTask = new TimerTask()
		{
			
			@Override
			public void run()
			{
				if(UIManager.getCurrentActivity()!=null)
				{
					UIManager.getCurrentActivity().runOnUiThread(new Runnable()
					{
						
						@Override
						public void run()
						{
							hideRewardLoadingPopup();
						}
					});
				}
				
			}
		};
		rewardTimer.schedule(rewardTimerTask, 5000);
	}

	public void hideRewardLoadingPopup()
	{
		if (reward_loading_layout != null && reward_loading_layout.getVisibility() != View.GONE)
			reward_loading_layout.setVisibility(View.GONE);
		stopRewardTimer();
	}

	private OnClickListener createOnClickLinstener(final String actionName, final Msg msgItem)
	{
		return new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (actionName.equals("pickRedPackage"))
				{
					MsgUtil.handleRedPackageFinishState(msgItem);
					if (msgItem.sendState == Msg.FINISH)
					{
						if (UIManager.getChatFragment() != null)
							UIManager.getChatFragment().showRedPackageConfirm(msgItem);
						// ServiceInterface.notifyDataSetChangedChatFragment();
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod("getRedPackageStatus", new Object[] { msgItem.attachmentId });
					}
				}
				else
				{
					hideRedPackagePopup();
					GSController.doHostAction(actionName, "", "", msgItem.attachmentId, true);
				}
			}
		};
	}

	public Msg getRedPackagePopItem()
	{
		return redPackageItem;
	}

	private void showRedPackageBtnAnimation()
	{
		if (red_package_open_btn == null)
			return;
		final RotateAnimation rotate = new RotateAnimation(-15, 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.6f);
		rotate.setRepeatCount(3);
		rotate.setDuration(100);
		rotate.setRepeatMode(Animation.RESTART);

		final RotateAnimation rotate2 = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.43f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate2.setDuration(2000);
		rotate2.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				rotate.reset();
				red_package_open_btn.setAnimation(rotate);
				rotate.startNow();
				rotate2.cancel();
			}
		});

		rotate.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				rotate2.reset();
				red_package_open_btn.setAnimation(rotate2);
				rotate2.startNow();
				rotate.cancel();
			}
		});

		red_package_open_btn.setAnimation(rotate);
		rotate.startNow();
	}

	private void setHeadImageLayoutParams()
	{
		int headWidthDP = 42;
		int headWidthDP2 = 39;
		int length1 = (int) (ScaleUtil.dip2px(this, headWidthDP) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
		int length2 = (int) (ScaleUtil.dip2px(this, headWidthDP2) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
		if (red_package_senderHeaderPic != null)
		{
			FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) red_package_senderHeaderPic.getLayoutParams();
			layoutParams2.width = length2;
			layoutParams2.height = length2;
			red_package_senderHeaderPic.setLayoutParams(layoutParams2);
		}

		if (red_package_HeaderPicContainer != null)
		{
			FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) red_package_HeaderPicContainer.getLayoutParams();
			layoutParams1.width = length1;
			layoutParams1.height = length1;
			red_package_HeaderPicContainer.setLayoutParams(layoutParams1);
		}

	}

	public void showRedPackagePopup(Msg msgItem)
	{
		if (red_package_root_layout == null)
			red_package_root_layout = (FrameLayout) findViewById(R.id.red_package_root_layout);
		if (red_package_background_layout == null)
			red_package_background_layout = findViewById(R.id.red_package_background_layout);
		if (red_package_sendername == null)
			red_package_sendername = (TextView) findViewById(R.id.red_package_sendername);
		if (red_package_sendertip == null)
			red_package_sendertip = (TextView) findViewById(R.id.red_package_sendertip);
		// red_package_msg = (TextView) findViewById(R.id.red_package_msg);
		if (red_package_senderHeaderPic == null)
			red_package_senderHeaderPic = (ImageView) findViewById(R.id.red_package_senderHeaderPic);
		if (red_package_HeaderPicContainer == null)
			red_package_HeaderPicContainer = (ImageView) findViewById(R.id.red_package_HeaderPicContainer);
		if (red_package_detail == null)
			red_package_detail = (TextView) findViewById(R.id.red_package_detail);
		if (red_package_warning == null)
			red_package_warning = (TextView) findViewById(R.id.red_package_warning);
		if (red_package_warning_layout == null)
			red_package_warning_layout = (LinearLayout) findViewById(R.id.red_package_warning_layout);
		if (red_package_open_btn == null)
			red_package_open_btn = (ImageView) findViewById(R.id.red_package_open_btn);
		if (red_package_unhandlelayout == null)
			red_package_unhandlelayout = (LinearLayout) findViewById(R.id.red_package_unhandlelayout);
		if (red_package_root_layout != null)
			red_package_root_layout.setVisibility(View.GONE);
		adjustRedPackageSize();

		if (red_package_background_layout != null)
		{
			red_package_background_layout.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					hideRedPackagePopup();
				}
			});
			red_package_background_layout.setVisibility(View.VISIBLE);
		}

		if (red_package_root_layout != null && red_package_root_layout.getVisibility() != View.VISIBLE)
			red_package_root_layout.setVisibility(View.VISIBLE);
		if (red_package_root_layout != null)
		{
			ViewHelper.setAlpha(red_package_root_layout, 1.0f);
			ViewHelper.setScaleX(red_package_root_layout, 1.0f);
			ViewHelper.setScaleY(red_package_root_layout, 1.0f);
		}

		showRedPackageBtnAnimation();
		redPackageItem = msgItem;
		if (red_package_open_btn != null)
			red_package_open_btn.setOnClickListener(createOnClickLinstener("pickRedPackage", msgItem));
		if (red_package_detail != null)
			red_package_detail.setOnClickListener(createOnClickLinstener("viewRedPackage", msgItem));

		if (red_package_senderHeaderPic != null)
			ImageUtil.setHeadImage(this, msgItem.getHeadPic(), red_package_senderHeaderPic, msgItem.getUser());

		setHeadImageLayoutParams();

		if (red_package_sendername != null)
			red_package_sendername.setText(msgItem.getName());
		if (red_package_sendertip != null)
			red_package_sendertip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SEND_RED_PACKAGE,
					LanguageManager.getLangByKey(LanguageKeys.ITEM_RED_PACKAGE)));

		if (red_package_detail != null)
			red_package_detail.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_DETAIL));

		if (red_package_unhandlelayout != null && red_package_warning_layout != null)
			if (msgItem.sendState == Msg.UNHANDLE)
			{

				red_package_unhandlelayout.setVisibility(View.VISIBLE);
				red_package_warning_layout.setVisibility(View.GONE);
			}
			else if (msgItem.sendState == Msg.NONE_MONEY)
			{
				red_package_unhandlelayout.setVisibility(View.GONE);
				red_package_warning_layout.setVisibility(View.VISIBLE);
				if (red_package_warning != null)
					red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_NO_MONEY));
			}
			else if (msgItem.sendState == Msg.FINISH)
			{
				red_package_unhandlelayout.setVisibility(View.GONE);
				red_package_warning_layout.setVisibility(View.VISIBLE);
				if (red_package_warning != null)
					red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_FINISH,
							LanguageManager.getLangByKey(LanguageKeys.ITEM_RED_PACKAGE)));
			}
	}

	public void hideRedPackagePopup()
	{
		Animator scaleX = ObjectAnimator.ofFloat(red_package_root_layout, "scaleX", 0);
		Animator scaleY = ObjectAnimator.ofFloat(red_package_root_layout, "scaleY", 0);
		Animator alpha = ObjectAnimator.ofFloat(red_package_root_layout, "alpha", 0);
		red_package_background_layout.setVisibility(View.GONE);
		AnimatorSet animator = new AnimatorSet();
		animator.playTogether(scaleX, scaleY, alpha);
		animator.setInterpolator(new AnticipateInterpolator(0.8f));
		animator.setDuration(300);
		animator.start();
		animator.addListener(new AnimatorListener()
		{

			@Override
			public void onAnimationStart(Animator animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animator animation)
			{
			}

			@Override
			public void onAnimationEnd(Animator animation)
			{
				red_package_root_layout.setVisibility(View.GONE);
				redPackageItem = null;
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});

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

	// ---------------------------------------------------
	// Chat Activity相关
	// ---------------------------------------------------

	protected Class<?>	fragmentClass;

	public void showFragment(String className)
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.replace(this.fragmentHolderId, Fragment.instantiate(this, className, this.bundle));
		ft.commitAllowingStateLoss();
	}

	/**
	 * 会先于fragment的onDestroy调用
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void onDestroy()
	{
		beforeExit();
		super.onDestroy();

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			actionbarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
		}
		onGlobalLayoutListener = null;
		backButton.setOnClickListener(null);
		hideProgressBar();
		hideRewardLoadingPopup();
		backButton = null;
		titleLabel = null;
		optionButton = null;
		editButton = null;
		writeButton = null;
		returnButton = null;
		showFriend = null;
		allianceShareBtn = null;
		allianceShareSend = null;
		imageChooseComfirmButton = null;
		imageDelButton = null;
		actionbarLayout = null;
		fragment_holder = null;
		activityProgressBar = null;
		reward_loading_layout = null;
		loading_textview = null;
		
		IMCore.getInstance().removeAllEventListener(this);
	}

	private ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;
	private boolean									adjustSizeCompleted				= false;
	private boolean									adjustRedPackageSizeCompleted	= false;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void adjustSize()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (backButton.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (backButton.getWidth() != 0 && !adjustSizeCompleted)
		{
			actualAdjustSize();
		}
	}

	protected void actualAdjustSize()
	{
		int height = (int) (88 * ConfigManager.scaleRatioButton);
		actionbarLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) actionbarLayout.getWidth(),
				height));
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) backButton.getLayoutParams();
		param.width = height;
		param.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		}
		backButton.setLayoutParams(param);

		RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) optionButton.getLayoutParams();
		param2.width = height;
		param2.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param2.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		optionButton.setLayoutParams(param2);

		RelativeLayout.LayoutParams param3 = (RelativeLayout.LayoutParams) editButton.getLayoutParams();
		param3.width = (int) (124 * ConfigManager.scaleRatioButton);
		param3.height = (int) (48 * ConfigManager.scaleRatioButton);
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param3.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		editButton.setLayoutParams(param3);
		
		RelativeLayout.LayoutParams returnButtonParam = (RelativeLayout.LayoutParams) returnButton.getLayoutParams();
		returnButtonParam.width = (int) (124 * ConfigManager.scaleRatioButton);
		returnButtonParam.height = (int) (48 * ConfigManager.scaleRatioButton);
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			returnButtonParam.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			returnButtonParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		returnButton.setLayoutParams(returnButtonParam);
		
		RelativeLayout.LayoutParams param4 = (RelativeLayout.LayoutParams) writeButton.getLayoutParams();
		param4.width = (int) (124 * ConfigManager.scaleRatioButton);
		param4.height = (int) (48 * ConfigManager.scaleRatioButton);
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param4.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		writeButton.setLayoutParams(param4);

		RelativeLayout.LayoutParams param5 = (RelativeLayout.LayoutParams) showFriend.getLayoutParams();
		param5.width = height;
		param5.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param5.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		showFriend.setLayoutParams(param5);

		RelativeLayout.LayoutParams param6 = (RelativeLayout.LayoutParams) allianceShareBtn.getLayoutParams();
		param6.width = height;
		param6.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param6.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param6.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		allianceShareBtn.setLayoutParams(param6);

		RelativeLayout.LayoutParams param7 = (RelativeLayout.LayoutParams) imageChooseComfirmButton.getLayoutParams();
		param7.height = (int) (60 * ConfigManager.scaleRatioButton);
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param7.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param7.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		imageChooseComfirmButton.setLayoutParams(param7);

		RelativeLayout.LayoutParams param8 = (RelativeLayout.LayoutParams) allianceShareSend.getLayoutParams();
		param8.width = height;
		param8.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param8.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param8.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		allianceShareSend.setLayoutParams(param8);
		
		RelativeLayout.LayoutParams param9 = (RelativeLayout.LayoutParams) imageDelButton.getLayoutParams();
		param9.width = height;
		param9.height = height;
//		if(ConfigManager.getInstance().isArabLang())
//		{
//			param9.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			param9.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		}
		imageDelButton.setLayoutParams(param9);

		ScaleUtil.adjustTextSize(titleLabel, ConfigManager.scaleRatio);

		adjustSizeCompleted = true;
	}

	protected void adjustRedPackageSize()
	{
		if (!ConfigManager.getInstance().scaleFontandUI || adjustRedPackageSizeCompleted)
			return;
		ScaleUtil.adjustTextSize(red_package_sendername, ConfigManager.scaleRatio);
		ScaleUtil.adjustTextSize(red_package_sendertip, ConfigManager.scaleRatio);
		// ScaleUtil.adjustTextSize(red_package_msg,
		// ConfigManager.scaleRatio);
		ScaleUtil.adjustTextSize(red_package_detail, ConfigManager.scaleRatio);
		ScaleUtil.adjustTextSize(red_package_warning, ConfigManager.scaleRatio);
		adjustRedPackageSizeCompleted = true;
	}

	/**
	 * 高ppi手机的缩放修正因子
	 */
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
		fragment_holder.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(this, 5);
	}

	// ---------------------------------------------------
	// 锁屏超过1分钟，返回后自动退出
	// ---------------------------------------------------

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
		UIManager.setCurrentActivity(this);
		if (isScreenLocked)
		{
			isScreenLocked = false;
			// 锁屏返回，超时，退出聊天界面
			// 仅调用2dx的onResume
			if ((System.currentTimeMillis() - screenLockTime) > (1000 * 60))
			{
				isReturnFromScreenLock = true;
				UIManager.showGameActivity(UIManager.getCurrentActivity());
			}
			// 锁屏返回，未超时，不退出聊天界面
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "ScreenStatusEvent.GS_RESUME");
				IMCore.getInstance().dispatch(new ScreenStatusEvent(ScreenStatusEvent.GS_RESUME));
			}
		}
		else if (isSwitchingScreenState())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "ScreenStatusEvent.GS_RESUME");
			IMCore.getInstance().dispatch(new ScreenStatusEvent(ScreenStatusEvent.GS_RESUME));
		}

		UIManager.isNativeOpenning = false;
		refreshNetWorkState();
		
		IMCore.getInstance().addEventListener(WSStatusEvent.STATUS_CHANGE, this, true, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof WSStatusEvent)
				{
//					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "subType", ((WSStatusEvent) event).subType);
					refreshStatusLabel((WSStatusEvent) event);
					refreshStatusIcon((WSStatusEvent) event);
				}
			};
		});
	}

	protected void refreshStatusIcon(WSStatusEvent event)
	{
		int type;

		switch (event.subType)
		{
			case WSStatusEvent.RECONNECTING:
				type = ConfigManager.WEBSOCKET_NETWORK_CONNECTING;
				break;
			case WSStatusEvent.CONNECTING:
				type = ConfigManager.WEBSOCKET_NETWORK_CONNECTING;
				break;
			case WSStatusEvent.CONNECTED:
				type = ConfigManager.WEBSOCKET_NETWORK_CONNECTED;
				break;
			case WSStatusEvent.DISCONNECTED:
				type = ConfigManager.WEBSOCKET_SERVER_DISCONNECTED;
				break;
			case WSStatusEvent.CONNECTION_FAILED:
				type = ConfigManager.WEBSOCKET_NETWORK_CONNECTE_FAILED;
				break;
			case WSStatusEvent.AUTHORISING:
				type = ConfigManager.WEBSOCKET_NETWORK_CONNECTING;
				break;
			default:
				return;
		}
		
		if (ConfigManager.websocket_network_state != type)
		{
			ConfigManager.websocket_network_state = type;
			if (CokChannelDef.isInBasicChat())
			{
				UIManager.getCurrentActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (UIManager.getCurrentActivity() != null)
							{
								refreshNetWorkState();
							}
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

	protected void refreshStatusLabel(final WSStatusEvent event)
	{
		String status = "";

		switch (event.subType)
		{
			case WSStatusEvent.RECONNECTING:
				if(event.reconnectCountDown > 0){
					status = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECT_FAIL, event.reconnectCountDown + "");
				}else{
					status = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_RECONNECTING);
				}
				break;
			case WSStatusEvent.CONNECTING:
				status = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING);
				break;
			case WSStatusEvent.CONNECTED:
				status = "";
				break;
			case WSStatusEvent.DISCONNECTED:
				status = LanguageManager.getLangByKey(LanguageKeys.ANOTHER_LOGIN);
				break;
			case WSStatusEvent.CONNECTION_FAILED:
				// TODO 增加多语言
				status = "";
				break;
			case WSStatusEvent.AUTHORISING:
				status = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_AUTHORISING);
				break;
			default:
				return;
		}

		final String s = status;
		if (!CokChannelDef.isInMailDialog() && UIManager.getChatFragment() != null)
		{
			IMCore.hostActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					if (UIManager.getChatFragment() != null)
					{
						UIManager.getChatFragment().refreshStatusLabel(s);
					}
				}
			});
		}
	}

	/**
	 * 是否正在锁屏或解锁
	 * 仅在onPause或onResume中时用，帮助判断当前的onPause或onResume是否是因为屏幕操作导致的
	 */
	private boolean isSwitchingScreenState()
	{
		return !UIManager.isNativeOpenning && !GSController.isReturningToGame;
	}

	@Override
	protected void onPause()
	{
		// 当打开其它原生activity时，会出现并非锁屏的onPause，需要直接判断是否锁屏
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "this", this, "isScreenOn", isScreenOn);

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
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "ScreenStatusEvent.GS_PAUSE");
			IMCore.getInstance().dispatch(new ScreenStatusEvent(ScreenStatusEvent.GS_PAUSE));
		}

		hideAllStateAnimation();
	}

	private void beforeExit()
	{
//		if (fragment != null)
//		{
//			fragment.saveState();
//		}
		
		saveState();

		isExiting = true;
		UIManager.stopFlyHintTimer();
//		// 极少情况fragment为null
//		if (this instanceof ChatActivity && fragment != null && fragment instanceof ChatFragmentNew)
//		{
//			((ChatFragmentNew) fragment).resetChannelView();
//		}
	}

	protected void saveState()
	{
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
		UIManager.popActivity(this);
		if (UIManager.getNativeActivityCount() == 0)
		{
			GSController.isReturningToGame = true;
		}
		else
		{
			UIManager.isNativeOpenning = true;
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

//	public void showFlyMutiReward(FlyMutiRewardInfo mutiRewardInfo)
//	{
//		reward_fly_layout.removeAllViews();
//		reward_fly_layout.setVisibility(View.VISIBLE);
//		if (mutiRewardInfo != null)
//		{
//			List<FlyRewardInfo> flyToolRewardArray = mutiRewardInfo.getFlyToolReward();
//			createFlyNodeByType(flyToolRewardArray, 1);
//			List<FlyRewardInfo> flyRewardArray = mutiRewardInfo.getFlyReward();
//			createFlyNodeByType(flyRewardArray, 2);
//			hideRewardLoadingPopup();
//			createFlyAnimationForAllNode();
//		}
//	}
//
//	public void createFlyAnimationForAllNode()
//	{
//		if (reward_fly_layout.getChildCount() <= 0)
//			return;
//		int flyToolRewardIndex = 0;
//		int flyRewardIndex = 0;
//		int totalFlyToolRewardNum = getChildNumByType("1");
//		int totalFlyRewardNum = getChildNumByType("2");
//		for (int i = 0; i < reward_fly_layout.getChildCount(); i++)
//		{
//			View view = reward_fly_layout.getChildAt(i);
//			String tag = view.getTag().toString();
//			if (tag.equals("1"))
//			{
//				createFlyToolAnimationForSingleNode(view, totalFlyToolRewardNum, flyToolRewardIndex, 0);
//				flyToolRewardIndex++;
//			}
//			else if (tag.equals("2"))
//			{
//				createFlyToolAnimationForSingleNode(view, totalFlyRewardNum, flyRewardIndex, 1600);
//				flyRewardIndex++;
//			}
//		}
//	}

	private int getChildNumByType(String type)
	{
		int num = 0;
		for (int i = 0; i < reward_fly_layout.getChildCount(); i++)
		{
			View view = reward_fly_layout.getChildAt(i);
			String tag = view.getTag().toString();
			if (tag.equals(type))
				num++;
		}
		return num;
	}

//	private Animator createflyOutAnimation(View view, int totalNum, int index, float centerY, long durationTime, long delayTime)
//	{
//		float scaleFactor = ((ScaleUtil.getScreenWidth() - ScaleUtil.dip2px(70)) / 4.0f - ScaleUtil.dip2px(20)) / ScaleUtil.dip2px(45);
//		scaleFactor = scaleFactor > 2 ? 2 : scaleFactor;
//		float cellW = ScaleUtil.dip2px(45) * scaleFactor + ScaleUtil.dip2px(20);
//		float lineH = ScaleUtil.dip2px(60) * scaleFactor + ScaleUtil.dip2px(20);
//		int lineCount = totalNum / 4 + 1;
//		float delta = lineCount * lineH - ScaleUtil.getScreenHeight() / 2.0f;
//
//		// LogUtil.printVariablesWithFuctionName("lineCount",lineCount,"scaleFactor",scaleFactor,"delta",delta,"ScaleUtil.getScreenHeight()",ScaleUtil.getScreenHeight());
//		if (delta > 0)
//		{
//			centerY = -delta;
//		}
//		else
//		{
//			centerY = -ScaleUtil.dip2px(50);
//		}
//
//		int line = totalNum / 4 - index / 4;
//		float endX = 0;
//		if (line > 0)
//		{
//			endX = (index % 4 - 2) * cellW + cellW / 2;
//		}
//		else
//		{
//			if (totalNum % 4 == 1)
//			{
//				endX = 0;
//			}
//			else if (totalNum % 4 == 2)
//			{
//				endX = (index % 2 - 1) * cellW + cellW / 2;
//			}
//			else if (totalNum % 4 == 3)
//			{
//				endX = ((index % 3 == 0 ? 3 : index % 3) - 2) * cellW;
//			}
//			else
//			{
//				endX = (index % 4 - 2) * cellW + cellW / 2;
//			}
//		}
//		AnimatorSet animSet = new AnimatorSet();
//		Animator transAnimatorX = ObjectAnimator.ofFloat(view, "translationX", 0, endX);
//		Animator transAnimatorY = ObjectAnimator.ofFloat(view, "translationY", centerY, -(line * lineH + centerY));
//
//		if (delayTime > 0)
//		{
//			ViewHelper.setScaleX(view, 0);
//			ViewHelper.setScaleY(view, 0);
//			animSet.setStartDelay(delayTime);
//		}
//
//		Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, scaleFactor);
//		Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, scaleFactor);
//		animSet.playTogether(transAnimatorX, transAnimatorY, scaleX, scaleY);
//		animSet.setDuration(durationTime);
//		animSet.setInterpolator(new DecelerateInterpolator());
//		return animSet;
//	}
//
//	private Animator createFlyToolAnimationForSingleNode(final View view, int totalNum, int index, int delayTime)
//	{
//		AnimatorSet animSet2 = new AnimatorSet();
//		Animator transAnimatorX2 = ObjectAnimator.ofFloat(view, "translationX", 0);
//		Animator transAnimatorY2 = ObjectAnimator.ofFloat(view, "translationY", ScaleUtil.getScreenHeight() / 2.0f);
//		Animator scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 0);
//		Animator scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 0);
//		animSet2.playTogether(transAnimatorX2, transAnimatorY2, scaleX2, scaleY2);
//		animSet2.setInterpolator(new AnticipateInterpolator(0.5f));
//		animSet2.setDuration(600);
//		animSet2.setStartDelay(400);
//
//		AnimatorSet animator = new AnimatorSet();
//		animator.playSequentially(createflyOutAnimation(view, totalNum, index, 0, 500, delayTime), animSet2);
//		animator.start();
//		animator.addListener(new AnimatorListener()
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
//				reward_fly_layout.removeView(view);
//				if (reward_fly_layout.getChildCount() <= 0)
//					hideFlyReward();
//			}
//
//			@Override
//			public void onAnimationCancel(Animator animation)
//			{
//			}
//		});
//		return animator;
//	}
//
//	private void createFlyAnimationForSingleNode(final View view, int totalNum, int index)
//	{
//		Animator animator = createflyOutAnimation(view, totalNum, index, 100, 1200, 0);
//		animator.start();
//		animator.addListener(new AnimatorListener()
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
//				reward_fly_layout.removeView(view);
//				if (reward_fly_layout.getChildCount() <= 0)
//					hideFlyReward();
//			}
//
//			@Override
//			public void onAnimationCancel(Animator animation)
//			{
//			}
//		});
//	}
//
//	public void createFlyNodeByType(List<FlyRewardInfo> flyRewardArray, int type)
//	{
//		if (flyRewardArray != null)
//		{
//			for (int i = 0; i < flyRewardArray.size(); i++)
//			{
//				FlyRewardInfo rewardInfo = flyRewardArray.get(i);
//				createSingleRewardNode(rewardInfo, "" + type);
//			}
//		}
//	}

//	public void hideFlyReward()
//	{
//		reward_fly_layout.removeAllViews();
//		reward_fly_layout.setVisibility(View.GONE);
//		showMoveToTrashToast();
//	}
//	
//	private void showMoveToTrashToast()
//	{
//		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
//		boolean changed = false;
//		if(config!=null && !config.isFirstRewardTipShowed())
//		{
//			config.setFirstRewardTipShowed(true);
//			changed = true;
//		}
//		else if(config == null)
//		{
//			config = new LocalConfig();
//			config.setFirstRewardTipShowed(true);
//			ConfigManager.getInstance().setLocalConfig(config);
//			changed = true;
//		}
//		
//		if(changed)
//		{
//			ConfigManager.getInstance().saveLocalConfig();
//			if(GSController.getChannelListFragment()==null)
//				return;
//			MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_MOVE_TO_TRASH));
//		}
//	}
//
//	private void createSingleRewardNode(FlyRewardInfo rewardInfo, String type)
//	{
//		if (rewardInfo == null)
//			return;
//		String iconName = rewardInfo.getItemPic();
//		String pic = iconName.toLowerCase();
//		if (StringUtils.isNotEmpty(pic) && pic.endsWith(".png"))
//		{
//			pic = pic.substring(0, pic.indexOf(".png"));
//		}
//		int num = rewardInfo.getItemNum();
//		boolean isNotExistIcon = false;
//		int picId = ResUtil.getId(this, "drawable", pic);
//		if (picId == 0)
//		{
//			picId = R.drawable.no_iconflag;
//			if (picId == 0)
//				return;
//			isNotExistIcon = true;
//		}
//		View view = inflater.inflate(R.layout.item_reward, null);
//		if (view != null)
//		{
//			ImageView rewardImage = (ImageView) view.findViewById(R.id.reward_img);
//			TextView rewardNum = (TextView) view.findViewById(R.id.reward_num);
//			if (rewardImage != null)
//			{
//				rewardImage.setImageDrawable(getResources().getDrawable(picId));
//				if (isNotExistIcon)
//					ImageUtil.setDynamicImage(this, iconName, rewardImage);
//			}
//			if (rewardNum != null)
//			{
//				rewardNum.setText("+" + num);
//			}
//			view.setTag(type);
//			addViewOnParent(view);
//		}
//	}

	public void addViewOnParent(View view)
	{
		android.widget.FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		view.setLayoutParams(layoutParams);
		reward_fly_layout.addView(view);
	}

	public boolean	isSoftKeyBoardVisibile	= false;

	public void hideSoftKeyBoard()
	{
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager != null && fragmentLayout != null && fragmentLayout.getWindowToken() != null)
		{
			inputManager.hideSoftInputFromWindow(fragmentLayout.getWindowToken(), 0);
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
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				hideMailStateAnimation();
				if (network_state_layout.getVisibility() != View.VISIBLE)
					network_state_layout.setVisibility(View.VISIBLE);
				stopNetworkConnectAnimation();
				stopNetworkErrorAnimation();
				network_state_view.setImageResource(R.drawable.network_connect_anim);
				startNetworkConnectAnimation();
			}
		});

	}

	public void showNetwrokErrorAnimation()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				hideMailStateAnimation();
				if (network_state_layout.getVisibility() != View.VISIBLE)
					network_state_layout.setVisibility(View.VISIBLE);
				stopNetworkConnectAnimation();
				stopNetworkErrorAnimation();
				network_state_view.setImageResource(R.drawable.network_error);
				startNetworkErrorAnimation();
			}
		});

	}

	public void startMailStateAnimation()
	{
		System.out.println("startMailStateAnimation");
		Drawable drawable = mail_pull1.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			AnimationDrawable mail_state_2Animation = (AnimationDrawable) mail_pull1.getDrawable();
			if (mail_state_2Animation != null && !mail_state_2Animation.isRunning())
				mail_state_2Animation.start();
		}

		if (mail_state_anim2 == null)
			mail_state_anim2 = AnimationUtils.loadAnimation(this, R.anim.mail_state_anim1);
		else
			mail_state_anim2.reset();
		mail_pull2.startAnimation(mail_state_anim2);
	}

	public void startNetworkErrorAnimation()
	{
		ViewHelper.setAlpha(network_state_view, 0);
		if (networkErrorAnimSet == null)
		{
			networkErrorAnimSet = new AnimatorSet();
			Animator showAnim = ObjectAnimator.ofFloat(network_state_view, "alpha", 0, 1.0f);
			showAnim.setDuration(500);
			Animator hideAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 1.0f, 0);
			hideAnimator.setStartDelay(500);
			Animator delayAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 0);
			delayAnimator.setDuration(300);

			networkErrorAnimSet.playSequentially(showAnim, hideAnimator, delayAnimator);
		}

		networkErrorAnimSet.start();
		networkErrorAnimSet.addListener(new AnimatorListener()
		{

			@Override
			public void onAnimationStart(Animator animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animator animation)
			{
			}

			@Override
			public void onAnimationEnd(Animator animation)
			{
				animation.start();
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
				animation.removeAllListeners();
			}
		});

	}

	public void startNetworkConnectAnimation()
	{
		Drawable drawable = network_state_view.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
			if (network_state_animation != null && !network_state_animation.isRunning())
				network_state_animation.start();
		}
	}

	public void stopNetworkErrorAnimation()
	{
		if (networkErrorAnimSet != null)
			networkErrorAnimSet.cancel();
	}

	public void stopNetworkConnectAnimation()
	{
		Drawable drawable = network_state_view.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
			if (network_state_animation != null && network_state_animation.isRunning())
				network_state_animation.stop();
		}
	}

	public void hideNetworkStateAnimation()
	{
		stopNetworkConnectAnimation();
		stopNetworkErrorAnimation();
		if (network_state_layout.getVisibility() != View.GONE)
			network_state_layout.setVisibility(View.GONE);
	}

	public void hideAllStateAnimation()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		hideNetworkStateAnimation();
		hideMailStateAnimation();
	}

	public void showMailStateAnimation()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				hideNetworkStateAnimation();
				if (mail_state_layout.getVisibility() != View.VISIBLE)
					mail_state_layout.setVisibility(View.VISIBLE);
				mail_pull1.setImageResource(R.drawable.mail_pull_anim);
				startMailStateAnimation();
			}
		});
	}

	public void stopMailStateAnimation()
	{
		Drawable drawable = mail_pull1.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			AnimationDrawable pull_anim1 = (AnimationDrawable) drawable;
			if (pull_anim1 != null && pull_anim1.isRunning())
				pull_anim1.stop();
		}

		if (mail_state_anim2 != null)
			mail_state_anim2.cancel();
	}

	public void hideMailStateAnimation()
	{
		stopMailStateAnimation();
		if (mail_state_layout.getVisibility() != View.GONE)
			mail_state_layout.setVisibility(View.GONE);
	}

	public void refreshNetWorkState()
	{
		if (ConfigManager.isNetWorkConnecting() || (ConfigManager.isWebSocketNetWorkConnecting() && CokChannelDef.isInBasicChat()))
			showNetworkConnectAnimation();
		else if (ConfigManager.isNetWorkError() || (ConfigManager.isWebSocketNetWorkError() && CokChannelDef.isInBasicChat()))
			showNetwrokErrorAnimation();
		else if (ConfigManager.mail_pull_state == ConfigManager.MAIL_PULLING
				&& CokChannelDef.isInSystemMail())
			showMailStateAnimation();
		else
			hideAllStateAnimation();
		// if(ConfigManager.isNetWorkNormal() >
		// ConfigManager.ACTIVITY_STATE_NORMAL)
		// {
		// if(ConfigManager.network_state == ConfigManager.NETWORK_CONNECTING)
		// showNetworkConnectAnimation();
		// else if(ConfigManager.network_state == ConfigManager.NETWORK_ERROR)
		// showNetwrokErrorAnimation();
		// }
		// else
		// {
		// if(ConfigManager.mail_pull_state >
		// ConfigManager.ACTIVITY_STATE_NORMAL)
		// showMailStateAnimation();
		// else
		// hideAllStateAnimation();
		// }
	}
	
	public void setChildContentView(int layoutId)
	{
		mContentView = LayoutInflater.from(this).inflate(layoutId, null);
		if (mContentView == null) {
			return;
		}
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mContentView.setLayoutParams(lp);
	}
}