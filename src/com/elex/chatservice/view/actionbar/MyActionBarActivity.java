package com.elex.chatservice.view.actionbar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.FlyMutiRewardInfo;
import com.elex.chatservice.model.FlyRewardInfo;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.ChatActivity;
import com.elex.chatservice.view.ChatFragment;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.ICocos2dxScreenLockListener;
import com.elex.chatservice.view.allianceshare.PopupWindows;
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
	public Button				showNearbyPeopleBtn;
	public TextView				showNearbyMsgBtn;
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
	private MsgItem				redPackageItem = null;
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
	public FrameLayout			nearby_btn_layout;
	private ImageView			nearby_unread_bg;
	private TextView			nearby_unread_num;
	
	public LinearLayout 		realtime_btn;
	private ImageView			realtime_btn_image;
	public TextView			realtime_btn_text;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ChatServiceController.setCurrentActivity(this);
		ServiceInterface.pushActivity(this);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// actionBarHelper.onCreate(savedInstanceState);
		// actionBarHelper.setDisplayHomeAsUpEnabled(true);
		

		setContentView(R.layout.cs__chat_activity);
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
		showNearbyPeopleBtn = (Button) findViewById(R.id.cs__actionbar_showNearbyButton);
		showNearbyMsgBtn = (TextView) findViewById(R.id.cs__actionbar_showNearbyMsgButton);
		allianceShareBtn = (Button) findViewById(R.id.cs__actionbar_allianceCircleButton);
		imageChooseComfirmButton = (Button) findViewById(R.id.cs__actionbar_imageChooseComfirmButton);
		imageDelButton = (Button) findViewById(R.id.cs__actionbar_imageDelButton);
		allianceShareSend = (Button) findViewById(R.id.cs__actionbar_allianceShareSendButton);
		actionbarLayout = (RelativeLayout) findViewById(R.id.cs__actionbar_layout);
		fragmentLayout = (RelativeLayout) findViewById(R.id.cs__activity_fragment_layout);
		network_state_view = (ImageView) findViewById(R.id.network_state_view);
		network_state_layout = (FrameLayout) findViewById(R.id.network_state_layout);
		nearby_btn_layout = (FrameLayout) findViewById(R.id.nearby_btn_layout);
		nearby_unread_bg = (ImageView) findViewById(R.id.nearby_unread_bg);
		nearby_unread_num = (TextView) findViewById(R.id.nearby_unread_num);
		
		realtime_btn = (LinearLayout) findViewById(R.id.realtime_btn);
		realtime_btn_image = (ImageView) findViewById(R.id.realtime_btn_image);
		realtime_btn_text = (TextView) findViewById(R.id.realtime_btn_text);

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
					if (popupMenu == null)
						popupMenu = new PopupWindows(MyActionBarActivity.this);
					if (!popupMenu.isShowing())
						popupMenu.showAtLocation(v, Gravity.BOTTOM, 0, 0);
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
		
		if(mContentView!=null)
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
	
	public void showRightBtn(View button)
	{
		optionButton.setVisibility(View.GONE);
		returnButton.setVisibility(View.GONE);
		editButton.setVisibility(View.GONE);
		writeButton.setVisibility(View.GONE);
		showFriend.setVisibility(View.GONE);
		showNearbyPeopleBtn.setVisibility(View.GONE);
		nearby_btn_layout.setVisibility(View.GONE);
		realtime_btn.setVisibility(View.GONE);
		imageDelButton.setVisibility(View.GONE);
		imageChooseComfirmButton.setVisibility(View.GONE);
		allianceShareBtn.setVisibility(View.GONE);
		allianceShareSend.setVisibility(View.GONE);
		if(button!=null)
			button.setVisibility(View.VISIBLE);
	}
	
	public void refreshNearbyUnreadCount()
	{
		nearby_unread_bg.setVisibility(View.GONE);
		nearby_unread_num.setVisibility(View.GONE);
		if(MailManager.nearbyEnable && ChatServiceController.getNearByListActivity()!=null)
		{
			ChatChannel channel = ChannelManager.getInstance().getNearbyChannel();
			if(channel!=null)
			{
				if(channel.unreadCount>0)
				{
					nearby_unread_bg.setVisibility(View.VISIBLE);
					nearby_unread_num.setVisibility(View.VISIBLE);
					nearby_unread_num.setText(""+channel.unreadCount);
				}
			}
		}
	}

	public void hidePopupMenu()
	{
		if (popupMenu != null && popupMenu.isShowing())
			popupMenu.dismiss();
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
				if(ChatServiceController.getCurrentActivity()!=null)
				{
					ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
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

	private OnClickListener createOnClickLinstener(final String actionName, final MsgItem msgItem)
	{
		return new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "pickRedPackage", this);
				
				if (actionName.equals("pickRedPackage"))
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "actionName.equals pickRedPackage", this);
					msgItem.handleRedPackageFinishState();
					if (msgItem.sendState == MsgItem.FINISH)
					{
						LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "msgItem.sendState == MsgItem.FINISH", this);
						if (ChatServiceController.getChatFragment() != null)
							ChatServiceController.getChatFragment().showRedPackageConfirm(msgItem);
						// ServiceInterface.notifyDataSetChangedChatFragment();
					}
					else
					{
						LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "msgItem.sendState ！= MsgItem.FINISH", this);
						// 在点击收取红包之前，拿到城堡等级
						JniController.getInstance().excuteJNIVoidMethod("setRedPackageOpenEnable", null);
						JniController.getInstance().excuteJNIVoidMethod("getRedPackageStatus", new Object[] { msgItem.attachmentId });
					}
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "actionName.not equals pickRedPackage", this);
					hideRedPackagePopup();
					ChatServiceController.doHostAction(actionName, "", "", msgItem.attachmentId, true);
				}
			}
		};
	}

	public MsgItem getRedPackagePopItem()
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

	public void showRedPackagePopup(MsgItem msgItem)
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
			if (msgItem.sendState == MsgItem.UNHANDLE)
			{

				red_package_unhandlelayout.setVisibility(View.VISIBLE);
				red_package_warning_layout.setVisibility(View.GONE);
			}
			else if (msgItem.sendState == MsgItem.NONE_MONEY)
			{
				red_package_unhandlelayout.setVisibility(View.GONE);
				red_package_warning_layout.setVisibility(View.VISIBLE);
				if (red_package_warning != null)
					red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_NO_MONEY));
			}
			else if (msgItem.sendState == MsgItem.FINISH)
			{
				red_package_unhandlelayout.setVisibility(View.GONE);
				red_package_warning_layout.setVisibility(View.VISIBLE);
				if (red_package_warning != null)
					red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_FINISH,
							LanguageManager.getLangByKey(LanguageKeys.ITEM_RED_PACKAGE)));
			}
			else if (msgItem.sendState == MsgItem.HANDLEDISABLE)
			{
				red_package_unhandlelayout.setVisibility(View.GONE);
				red_package_warning_layout.setVisibility(View.VISIBLE);
				if (red_package_warning != null){
// 获取城堡等级
					String citylevel = String.valueOf(ChatServiceController.redEnvelopesCastleLevel);
					red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_EnvelopesCastleLevel ,citylevel));

				}
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
				// 在这里将msgitem状态重置
				if (redPackageItem != null && redPackageItem.sendState == MsgItem.HANDLEDISABLE){
					redPackageItem.sendState = MsgItem.UNHANDLE;
				}
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
			int height = (int) (88 * ConfigManager.scaleRatioButton);
			actionbarLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) actionbarLayout.getWidth(),
					height));
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) backButton.getLayoutParams();
			param.width = height;
			param.height = height;
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			}
			backButton.setLayoutParams(param);

			RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) optionButton.getLayoutParams();
			param2.width = height;
			param2.height = height;
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param2.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			optionButton.setLayoutParams(param2);

			RelativeLayout.LayoutParams param3 = (RelativeLayout.LayoutParams) editButton.getLayoutParams();
			param3.width = (int) (124 * ConfigManager.scaleRatioButton);
			param3.height = (int) (48 * ConfigManager.scaleRatioButton);
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param3.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			editButton.setLayoutParams(param3);
			
			RelativeLayout.LayoutParams returnButtonParam = (RelativeLayout.LayoutParams) returnButton.getLayoutParams();
			returnButtonParam.width = (int) (124 * ConfigManager.scaleRatioButton);
			returnButtonParam.height = (int) (48 * ConfigManager.scaleRatioButton);
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				returnButtonParam.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				returnButtonParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			returnButton.setLayoutParams(returnButtonParam);
			
			RelativeLayout.LayoutParams param4 = (RelativeLayout.LayoutParams) writeButton.getLayoutParams();
			param4.width = (int) (124 * ConfigManager.scaleRatioButton);
			param4.height = (int) (48 * ConfigManager.scaleRatioButton);
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param4.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			writeButton.setLayoutParams(param4);

			RelativeLayout.LayoutParams param5 = (RelativeLayout.LayoutParams) showFriend.getLayoutParams();
			param5.width =  (int) (ScaleUtil.dip2px(120) * ConfigManager.scaleRatioButton);
			param5.height = (int) (48 * ConfigManager.scaleRatioButton);
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param5.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			showFriend.setLayoutParams(param5);

			RelativeLayout.LayoutParams param6 = (RelativeLayout.LayoutParams) allianceShareBtn.getLayoutParams();
			param6.width = height;
			param6.height = height;
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param6.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param6.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			allianceShareBtn.setLayoutParams(param6);

			RelativeLayout.LayoutParams param7 = (RelativeLayout.LayoutParams) imageChooseComfirmButton.getLayoutParams();
			param7.height = (int) (60 * ConfigManager.scaleRatioButton);
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param7.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param7.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			imageChooseComfirmButton.setLayoutParams(param7);

			RelativeLayout.LayoutParams param8 = (RelativeLayout.LayoutParams) allianceShareSend.getLayoutParams();
			param8.width = height;
			param8.height = height;
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param8.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param8.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			allianceShareSend.setLayoutParams(param8);
			
			RelativeLayout.LayoutParams param9 = (RelativeLayout.LayoutParams) imageDelButton.getLayoutParams();
			param9.width = height;
			param9.height = height;
//			if(ConfigManager.getInstance().isArabLang())
//			{
//				param9.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				param9.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			}
			imageDelButton.setLayoutParams(param9);
			
			
			RelativeLayout.LayoutParams param10 = (RelativeLayout.LayoutParams) showNearbyPeopleBtn.getLayoutParams();
			param10.width = height;
			param10.height = height;
			showNearbyPeopleBtn.setLayoutParams(param10);
			
			RelativeLayout.LayoutParams param11 = (RelativeLayout.LayoutParams) nearby_btn_layout.getLayoutParams();
			param11.width = (int) (129 * ConfigManager.scaleRatioButton);
			param11.height = (int) (53 * ConfigManager.scaleRatioButton);
			nearby_btn_layout.setLayoutParams(param11);
			
			
			FrameLayout.LayoutParams param12 = (FrameLayout.LayoutParams) showNearbyMsgBtn.getLayoutParams();
			param12.width = (int) (124 * ConfigManager.scaleRatioButton);
			param12.height = (int) (48 * ConfigManager.scaleRatioButton);
			showNearbyMsgBtn.setLayoutParams(param12);
			
			FrameLayout.LayoutParams param13 = (FrameLayout.LayoutParams) nearby_unread_bg.getLayoutParams();
			param13.width = (int) (30 * ConfigManager.scaleRatioButton);
			param13.height = (int) (30 * ConfigManager.scaleRatioButton);
			nearby_unread_bg.setLayoutParams(param13);
			
			RelativeLayout.LayoutParams param14 = (RelativeLayout.LayoutParams) realtime_btn.getLayoutParams();
			param14.height = (int) (53 * ConfigManager.scaleRatioButton);
			realtime_btn.setLayoutParams(param14);
			
			LinearLayout.LayoutParams param15 = (LinearLayout.LayoutParams) realtime_btn_image.getLayoutParams();
			param15.width = (int) (30 * ConfigManager.scaleRatioButton);
			param15.height = (int) (30 * ConfigManager.scaleRatioButton);
			realtime_btn_image.setLayoutParams(param15);

			ScaleUtil.adjustTextSize(titleLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(nearby_unread_num, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(realtime_btn_text, ConfigManager.scaleRatio);

			adjustSizeCompleted = true;
		}
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
		if (fragment != null)
		{
			fragment.saveState();
		}

		isExiting = true;
		ServiceInterface.stopFlyHintTimer();
//		// 极少情况fragment为null
//		if (this instanceof ChatActivity && fragment != null && fragment instanceof ChatFragmentNew)
//		{
//			((ChatFragmentNew) fragment).resetChannelView();
//		}
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

	public void showFlyMutiReward(FlyMutiRewardInfo mutiRewardInfo)
	{
		reward_fly_layout.removeAllViews();
		reward_fly_layout.setVisibility(View.VISIBLE);
		if (mutiRewardInfo != null)
		{
			List<FlyRewardInfo> flyToolRewardArray = mutiRewardInfo.getFlyToolReward();
			createFlyNodeByType(flyToolRewardArray, 1);
			List<FlyRewardInfo> flyRewardArray = mutiRewardInfo.getFlyReward();
			createFlyNodeByType(flyRewardArray, 2);
			hideRewardLoadingPopup();
			createFlyAnimationForAllNode();
		}
	}

	public void createFlyAnimationForAllNode()
	{
		if (reward_fly_layout.getChildCount() <= 0)
			return;
		int flyToolRewardIndex = 0;
		int flyRewardIndex = 0;
		int totalFlyToolRewardNum = getChildNumByType("1");
		int totalFlyRewardNum = getChildNumByType("2");
		for (int i = 0; i < reward_fly_layout.getChildCount(); i++)
		{
			View view = reward_fly_layout.getChildAt(i);
			String tag = view.getTag().toString();
			if (tag.equals("1"))
			{
				createFlyToolAnimationForSingleNode(view, totalFlyToolRewardNum, flyToolRewardIndex, 0);
				flyToolRewardIndex++;
			}
			else if (tag.equals("2"))
			{
				createFlyToolAnimationForSingleNode(view, totalFlyRewardNum, flyRewardIndex, 1600);
				flyRewardIndex++;
			}
		}
	}

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

	private Animator createflyOutAnimation(View view, int totalNum, int index, float centerY, long durationTime, long delayTime)
	{
		float scaleFactor = ((ScaleUtil.getScreenWidth() - ScaleUtil.dip2px(70)) / 4.0f - ScaleUtil.dip2px(20)) / ScaleUtil.dip2px(45);
		scaleFactor = scaleFactor > 2 ? 2 : scaleFactor;
		float cellW = ScaleUtil.dip2px(45) * scaleFactor + ScaleUtil.dip2px(20);
		float lineH = ScaleUtil.dip2px(60) * scaleFactor + ScaleUtil.dip2px(20);
		int lineCount = totalNum / 4 + 1;
		float delta = lineCount * lineH - ScaleUtil.getScreenHeight() / 2.0f;

		// LogUtil.printVariablesWithFuctionName("lineCount",lineCount,"scaleFactor",scaleFactor,"delta",delta,"ScaleUtil.getScreenHeight()",ScaleUtil.getScreenHeight());
		if (delta > 0)
		{
			centerY = -delta;
		}
		else
		{
			centerY = -ScaleUtil.dip2px(50);
		}

		int line = totalNum / 4 - index / 4;
		float endX = 0;
		if (line > 0)
		{
			endX = (index % 4 - 2) * cellW + cellW / 2;
		}
		else
		{
			if (totalNum % 4 == 1)
			{
				endX = 0;
			}
			else if (totalNum % 4 == 2)
			{
				endX = (index % 2 - 1) * cellW + cellW / 2;
			}
			else if (totalNum % 4 == 3)
			{
				endX = ((index % 3 == 0 ? 3 : index % 3) - 2) * cellW;
			}
			else
			{
				endX = (index % 4 - 2) * cellW + cellW / 2;
			}
		}
		AnimatorSet animSet = new AnimatorSet();
		Animator transAnimatorX = ObjectAnimator.ofFloat(view, "translationX", 0, endX);
		Animator transAnimatorY = ObjectAnimator.ofFloat(view, "translationY", centerY, -(line * lineH + centerY));

		if (delayTime > 0)
		{
			ViewHelper.setScaleX(view, 0);
			ViewHelper.setScaleY(view, 0);
			animSet.setStartDelay(delayTime);
		}

		Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, scaleFactor);
		Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, scaleFactor);
		animSet.playTogether(transAnimatorX, transAnimatorY, scaleX, scaleY);
		animSet.setDuration(durationTime);
		animSet.setInterpolator(new DecelerateInterpolator());
		return animSet;
	}

	private Animator createFlyToolAnimationForSingleNode(final View view, int totalNum, int index, int delayTime)
	{
		AnimatorSet animSet2 = new AnimatorSet();
		Animator transAnimatorX2 = ObjectAnimator.ofFloat(view, "translationX", 0);
		Animator transAnimatorY2 = ObjectAnimator.ofFloat(view, "translationY", ScaleUtil.getScreenHeight() / 2.0f);
		Animator scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 0);
		Animator scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 0);
		animSet2.playTogether(transAnimatorX2, transAnimatorY2, scaleX2, scaleY2);
		animSet2.setInterpolator(new AnticipateInterpolator(0.5f));
		animSet2.setDuration(600);
		animSet2.setStartDelay(400);

		AnimatorSet animator = new AnimatorSet();
		animator.playSequentially(createflyOutAnimation(view, totalNum, index, 0, 500, delayTime), animSet2);
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
				reward_fly_layout.removeView(view);
				if (reward_fly_layout.getChildCount() <= 0)
					hideFlyReward();
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});
		return animator;
	}

	private void createFlyAnimationForSingleNode(final View view, int totalNum, int index)
	{
		Animator animator = createflyOutAnimation(view, totalNum, index, 100, 1200, 0);
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
				reward_fly_layout.removeView(view);
				if (reward_fly_layout.getChildCount() <= 0)
					hideFlyReward();
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});
	}

	public void createFlyNodeByType(List<FlyRewardInfo> flyRewardArray, int type)
	{
		if (flyRewardArray != null)
		{
			for (int i = 0; i < flyRewardArray.size(); i++)
			{
				FlyRewardInfo rewardInfo = flyRewardArray.get(i);
				createSingleRewardNode(rewardInfo, "" + type);
			}
		}
	}

	public void hideFlyReward()
	{
		reward_fly_layout.removeAllViews();
		reward_fly_layout.setVisibility(View.GONE);
		showMoveToTrashToast();
	}
	
	private void showMoveToTrashToast()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		boolean changed = false;
		if(config!=null && !config.isFirstRewardTipShowed())
		{
			config.setFirstRewardTipShowed(true);
			changed = true;
		}
		else if(config == null)
		{
			config = new LocalConfig();
			config.setFirstRewardTipShowed(true);
			ConfigManager.getInstance().setLocalConfig(config);
			changed = true;
		}
		
		if(changed)
		{
			ConfigManager.getInstance().saveLocalConfig();
			if(ChatServiceController.getChannelListFragment()==null)
				return;
			MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_MOVE_TO_TRASH));
		}
	}

	private void createSingleRewardNode(FlyRewardInfo rewardInfo, String type)
	{
		if (rewardInfo == null)
			return;
		String iconName = rewardInfo.getItemPic();
		String pic = iconName.toLowerCase();
		if (StringUtils.isNotEmpty(pic) && pic.endsWith(".png"))
		{
			pic = pic.substring(0, pic.indexOf(".png"));
		}
		int num = rewardInfo.getItemNum();
		boolean isNotExistIcon = false;
		int picId = ResUtil.getId(this, "drawable", pic);
		if (picId == 0)
		{
			picId = R.drawable.no_iconflag;
			if (picId == 0)
				return;
			isNotExistIcon = true;
		}
		View view = inflater.inflate(R.layout.item_reward, null);
		if (view != null)
		{
			ImageView rewardImage = (ImageView) view.findViewById(R.id.reward_img);
			TextView rewardNum = (TextView) view.findViewById(R.id.reward_num);
			if (rewardImage != null)
			{
				rewardImage.setImageDrawable(getResources().getDrawable(picId));
				if (isNotExistIcon)
					ImageUtil.setDynamicImage(this, iconName, rewardImage);
			}
			if (rewardNum != null)
			{
				rewardNum.setText("+" + num);
			}
			view.setTag(type);
			addViewOnParent(view);
		}
	}

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
		hideNetworkStateAnimation();
		hideMailStateAnimation();
	}

	public void showMailStateAnimation()
	{
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
		if (ConfigManager.isNetWorkConnecting() || ConfigManager.isWebSocketNetWorkConnecting())
			showNetworkConnectAnimation();
		else if (ConfigManager.isNetWorkError() || ConfigManager.isWebSocketNetWorkError())
			showNetwrokErrorAnimation();
		else if (ConfigManager.mail_pull_state == ConfigManager.MAIL_PULLING
				&& ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_OFFICIAL)
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
	
	public void refreshRealtimeBtnText()
	{
		if(ChatServiceController.getCurrentActivity()!=null)
		{
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(!WebRtcPeerManager.published)
						realtime_btn_text.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_REALTIME_VOICE));
					else
						realtime_btn_text.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_VOICE_SETTING));
				}
			});
		}
		
	}

	@SuppressLint("Override")
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		PermissionManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}