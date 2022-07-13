package com.elex.im.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.im.CokChannelDef;
import com.elex.im.CokConfig;
import com.elex.im.core.event.ChannelChangeEvent;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.LocalConfig;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.CallBack;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.adaptor.CustomExpandableListAdapter;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.controller.MenuController;
import com.elex.im.ui.model.ChannelView;
import com.elex.im.ui.model.CustomChannelData;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.util.CompatibleApiUtil;
import com.elex.im.ui.util.FixedAspectRatioFrameLayout;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.util.MsgUtil;
import com.elex.im.ui.util.RoundImageView;
import com.elex.im.ui.view.actionbar.ActionBarFragment;
import com.elex.im.ui.view.autoscroll.ScrollText;
import com.elex.im.ui.view.autoscroll.ScrollTextManager;
import com.elex.im.ui.view.inputfield.SlideInputField;
import com.elex.im.ui.view.listview.ListViewLoadListener;
import com.elex.im.ui.view.listview.PullDownToLoadMoreView;
import com.elex.im.ui.view.misc.messenger.AndroidUtilities;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.ViewProxy;

public class ChatFragment extends ActionBarFragment implements SensorEventListener
{
	public static final String			ARG_OBJECT					= "object";
	protected RelativeLayout			messagesListFrameLayout;
	private RelativeLayout				messagesListLayout;
	private FrameLayout					noContentLayout;
	private TextView					noContentTextView;
	private FrameLayout					noContentBtnLayout;
	private Button						noContentButton;
	private LinearLayout				relativeLayout1;
	public EditText					replyField;
	private MenuItem					attachScreenshotMenu;
	private TextView					wordCount;
	protected Button					addReply;
	private ImageView					imageView1;
	protected ImageView					imageView2;
	private CheckBox					horn_checkbox;
	// private LinearLayout horn_tip_layout;
	private RelativeLayout				horn_scroll_layout;
	// private TextView horn_text_tip;
	private TextView					horn_name;
	private ScrollText					horn_scroll_text;
	private LinearLayout				tooltipLayout;
	private TextView					tooltipLabel;
	private ImageView					tooltipArrow;
	private ImageView					horn_close_btn;
	private LinearLayout				custom_setting_layout;
	private TextView					add_title;
	private TextView					add_tip;
	private RoundImageView				customChannelHeadImage;
	private TextView					customChannelName;
	private ImageView					custom_mod_image;
	private FrameLayout					custom_head_layout;
	private FixedAspectRatioFrameLayout	headImageContainer;
	private ExpandableListView			custom_expand_listview;
	private CustomExpandableListAdapter	customChannelListAdapter;
	private RelativeLayout				custom_channel_setting_layout;
	private TextView					custom_channel_name;
	private ImageView					custom_channel_settting_btn;
	private int							loadMoreCount				= 0;
	protected int						loadingHeaderHeight;
	protected boolean					isKeyBoardFirstShowed		= false;
	private int							curMaxInputLength			= 500;
	private LinearLayout				custom_settting_btn_layout;
	private TextView					custom_setting_confim;
	public static Channel				showingCustomChannel		= null;

	public static boolean				rememberPosition			= false;

	private boolean						isJoinAlliancePopupShowing	= false;

	public boolean						isKeyBoradShowing			= false;
	public boolean						isKeyBoradChange			= false;

	private boolean						isSelectMemberBtnEnable		= false;
	private ChannelView					channelView					= null;

	public boolean						isSettingCustomChannel		= false;

	public boolean isSelectMemberBtnEnable()
	{
		return isSelectMemberBtnEnable;
	}

	public Channel getCurrentChannel()
	{
		return channelView.channel;
	}

	public ChatFragment(ChannelView channelView)
	{
		this.channelView = channelView;
		isKeyBoardFirstShowed = false;
	}
	
	public void afterSendMsgShowed()
	{
		if(getCurrentChannel() != null)
		{
			notifyDataSetChanged(true);
			scrollToLastLine();
		}
	}

	public void refreshMemberSelectBtn()
	{
		boolean isAllAllianceMember = StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid) &&
				StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().uid) &&
				UserManager.getInstance().getCurrentUser().uid.equals(UserManager.getInstance().getCurrentMail().opponentUid);
		if (!((CokChannelDef.isInMailDialog() && !isAllAllianceMember) || GSController.isCreateChatRoom || CustomChannelData.getInstance().hasCustomChannel()))
		{
			isSelectMemberBtnEnable = false;
			return;
		}

		try
		{
			if ((CokChannelDef.isInChatRoom() || (CokChannelDef.isInChat() && CokChannelDef.isInChatRoom(CustomChannelData.getInstance().customChannelType)))
					&& !CokChannelDef.getIsMemberFlag(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				isSelectMemberBtnEnable = false;
				return;
			}
			ArrayList<String> memberUidArray = GSController.getSelectMemberUidArr();
			if (memberUidArray == null)
			{
				isSelectMemberBtnEnable = false;
				return;
			}

			HashMap<String, User> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			isSelectMemberBtnEnable = true;
			if (memberUidArray == null
					|| memberUidArray.size() <= 0
					|| (memberUidArray != null && memberUidArray.size() > 0 && !memberUidArray.contains(UserManager.getInstance()
							.getCurrentUser().uid)))
				isSelectMemberBtnEnable = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void setSelectMemberBtnState()
	{
		if (getMemberSelectButton() != null)
		{
			getMemberSelectButton().setVisibility(isSelectMemberBtnEnable && !CokChannelDef.isInBasicChat() ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	private void showMessageBox()
	{
		relativeLayout1.setVisibility(View.VISIBLE);
		refreshWordCount();

		if (this.attachScreenshotMenu != null)
		{
			this.attachScreenshotMenu.setVisible(true);
		}
	}

	public void saveState()
	{
		saveState(channelView);
	}
	
	public static void saveState(ChannelView channelView)
	{
		if (channelView != null)
		{
			Channel channel = channelView.channel;
			if (channel != null && channelView.messagesListView != null)
			{
				channel.lastPosition.x = channelView.messagesListView.getFirstVisiblePosition();
				View v = channelView.messagesListView.getChildAt(0);
				channel.lastPosition.y = (v == null) ? 0 : (v.getTop() - channelView.messagesListView.getPaddingTop());
			}
		}
	}

	protected boolean	isJustCreated	= true;

	public void checkFirstGlobalLayout()
	{
		if (isJustCreated)
		{
			isJustCreated = false;
			refreshTab();
		}

		if (oldChatFragmentHeight == -1 && computeUsableHeight() > 0)
		{
			oldChatFragmentHeight = computeUsableHeight();

		}
		else if (oldChatFragmentHeight > computeUsableHeight())
		{
			oldChatFragmentHeight = computeUsableHeight();
			if (isKeyBoardFirstShowed)
				isKeyBoradShowing = true;
			if (!rememberPosition)
			{
				gotoLastLine();
			}
			else
			{
				rememberPosition = false;
			}
		}
		else if (oldChatFragmentHeight == computeUsableHeight())
		{
			if (isKeyBoradChange)
			{
				keyBoardChangeCount++;
			}
			if (keyBoardChangeCount == 2)
			{
				isKeyBoradChange = false;
			}
		}
		else if (oldChatFragmentHeight < computeUsableHeight())
		{
			keyBoardChangeCount = 0;
			isKeyBoradChange = true;
			oldChatFragmentHeight = computeUsableHeight();
			isKeyBoradShowing = false;
			isKeyBoardFirstShowed = true;
		}

		int usableHeightNow = computeUsableHeight();

		if (usableHeight == -1 && usableHeightNow > 0)
		{
			usableHeight = usableHeightNow;
		}

		if (usableHeight != -1 && usableHeight > usableHeightNow)
		{
			if (!isSystemBarResized)
			{
				isSystemBarResized = true;
				return;
			}
			if (channelView != null && channelView.channel != null && channelView.channel.lastPosition.x == -1
					&& channelView.messagesListView != null && channelView.getMessagesAdapter() != null)
			{
				channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
			}
			usableHeight = usableHeightNow;
		}
	}

	protected int		keyBoardChangeCount		= 0;
	protected int		oldChatFragmentHeight	= -1;
	protected boolean	isSystemBarResized		= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((ChatActivity) getActivity());
		
		return inflater.inflate(R.layout.cs__messages_fragment, container, false);
	}

	private FrameLayout.LayoutParams getLayoutParams()
	{
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER;
		return param;
	}

	public void refreshIsInLastScreen()
	{
		try
		{
			inLastScreen = isInLastScreen();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private boolean isInLastScreen()
	{
		// messagesListView存在时messagesListView.getChildAt(0)也可能为0
		if (channelView == null || channelView.getMessagesAdapter() == null
				|| channelView.getMessagesAdapter().getCount() == 0 || channelView.messagesListView == null)
		{
			return true;
		}
		// 遍历从view.getFirstVisiblePosition()可见高度及到最下方的各个item的高度，计算这高度和是否小于一定的值（1.6屏）
		View v = channelView.messagesListView.getChildAt(0);
		if (v == null)
		{
			return true;
		}

		// 第一个item被上方盖住的部分
		int firstOffset = v.getTop() - channelView.messagesListView.getPaddingTop();

		int totalHeight = v.getHeight() + firstOffset;
		if ((channelView.getMessagesAdapter().getCount() - channelView.messagesListView.getFirstVisiblePosition()) > 20)
		{
			return false;
		}

		for (int i = (channelView.messagesListView.getFirstVisiblePosition() + 1); i < channelView.getMessagesAdapter()
				.getCount(); i++)
		{
			View listItem = channelView.getMessagesAdapter().getView(i, null, channelView.messagesListView);
			listItem.measure(MeasureSpec.makeMeasureSpec(channelView.messagesListView.getWidth(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = listItem.getMeasuredHeight();
			totalHeight += h + channelView.messagesListView.getDividerHeight();
		}

		if (totalHeight <= (channelView.messagesListView.getHeight() * 1.75))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	boolean	inLastScreen	= false;
	private boolean isAudioBtnDown = false;
	
	public void updateListPositionForNewMsg(boolean isSelfMsg)
	{
		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
			gotoLastLine();
		inLastScreen = false;
	}
	
	public void smoothUpdateListPositionForNewMsg(boolean isSelfMsg)
	{
		if (channelView.messagesListView == null || channelView.getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
		{
			scrollToLastLine();
		}
		inLastScreen = false;
	}

	public void updateListPositionForOldMsg(int loadCount, final boolean needMergeSendTime)
	{
		loadMoreCount = loadCount;
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ListView listView = channelView.messagesListView;
					if(listView!=null)
					{
						if (!getCurrentChannel().isLoadingAllNew)
						{
							int heightOffest = channelView.pullDownToLoadListView.getPullDownHeight();
							if (needMergeSendTime)
							{
								if (GSController.sendTimeTextHeight != 0)
									heightOffest += GSController.sendTimeTextHeight + ScaleUtil.dip2px(activity, 15);
								else
									heightOffest += ScaleUtil.dip2px(activity, 44);
							}
							listView.setSelectionFromTop(loadMoreCount, heightOffest);
						}
						else
						{
							listView.setSelectionFromTop(0, 0);
						}
					}
					
					refreshToolTip();
					channelView.pullDownToLoadListView.hideProgressBar();
					channelView.stopTimerTask();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void changeChatRoomName(String name)
	{
		if (!name.equals(""))
		{
			getTitleLabel().setText(name);
		}
	}

	public void setEditText(String text)
	{
		if (replyField != null)
			replyField.setText(text);
	}

	public static void onMsgAdd(int channelType,String channelId, boolean needCalculateShowTimeIndex)
	{
		dataChanged = true;
		if (UIManager.getChatFragment() != null)
		{
			UIManager.getChatFragment().notifyDataSetChanged(needCalculateShowTimeIndex);
			dataChanged = false;
		}
	}

	public void notifyDataSetChanged(final boolean needCalculateShowTimeIndex)
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ChannelView curChannelView = channelView;
					if(curChannelView!=null)
					{
						MessagesAdapter adapter = curChannelView.getMessagesAdapter();
						if (adapter != null)
						{
							if (needCalculateShowTimeIndex)
							{
								Channel channel = ChannelManager.getInstance().getChannel(curChannelView.channelType);
								if (channel != null)
									channel.getTimeNeedShowMsgIndex();
							}

							adapter.notifyDataSetChanged();
						}
						refreshHasMoreData();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}


	public void resetMoreDataStart()
	{
		channelView.setLoadingStart(false);
	}

	public void gotoLastLine()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (channelView != null && channelView.messagesListView != null
							&& channelView.getMessagesAdapter() != null)
					{
						LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "gotoLastLine", channelView.getMessagesAdapter().getCount() - 1);
						channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	protected void scrollToLastLine()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (channelView != null && channelView.messagesListView != null
							&& channelView.getMessagesAdapter() != null)
					{
						LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "scrollToLastLine", channelView.getMessagesAdapter().getCount() - 1);
						channelView.messagesListView
								.smoothScrollToPosition(channelView.getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}
	
	// 让只有对应channel的事件才传递进来，可以省掉这个判断
//	protected boolean isSameChannel(int channelType,String channelId)
//	{
//		if (channelView == null)
//			return false;
//		System.out.println("isSameChannel  channelType:"+channelView.channelType);
//		if (CokChannelDef.isInBasicChat(channelView.channelType))
//			return channelView.channelType == channelType;
//		else
//		{
//			if (getCurrentChannel() != null)
//			{
//				return (channelView.channelType == channelType || getCurrentChannel().getChannelType() == channelType)
//						&& StringUtils.isNotEmpty(channelId)
//						&& StringUtils.isNotEmpty(getCurrentChannel().getChannelID())
//						&& getCurrentChannel().getChannelID().equals(channelId);
//			}
//						
//			else
//				return false;
//		}
//	}
	
	public void refreshCustomChannelImage(Channel channel)
	{
		showingCustomChannel = channel;
		if(activity!=null && CokChannelDef.isInCustomChat() && isSettingCustomChannel)
		{
			ImageUtil.setChannelImage(activity, channel, customChannelHeadImage);
			refreshSettingCustomChannelName(channel);
		}
		if(showingCustomChannel!=null)
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP2));
		else
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP));
	}
	
	private void prepareCustomChannelData()
	{
		if (activity != null && CokChannelDef.isInChat(channelView.channelType))
		{
			customChannelListAdapter = new CustomExpandableListAdapter(activity, CustomChannelData.getInstance().prepareCustomChannelData());
			custom_expand_listview.setAdapter(customChannelListAdapter);
			custom_expand_listview.expandGroup(0);
			custom_expand_listview.expandGroup(1);
		}
	}
	
	private SlideInputField slideInputField;

	public void onRecordPanelShown(boolean b)
	{
        if(isHornUI)
        {
//        	horn_tip_layout.setVisibility(b ? View.GONE : View.VISIBLE);
        }else{
//        	imageView1.setVisibility(b ? View.GONE : View.VISIBLE);
        }
	}
    
	public void exitRecordingUI()
	{
		slideInputField.exitRecordingUI();
	}

	public LinearLayout sendMessageLayout;
	public LinearLayout voice_rec_button_layout;
	private Button voice_rec_button;
	
	private boolean hornTextHidden = false; 
	private boolean isFirstInflate = true;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		System.out.println("chatfragmentnew onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		try
		{
			onViewCreatedImpl(view);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void onViewCreatedImpl(View view)
	{
		if(messagesListLayout != null)
			isFirstInflate = false;
		
		if(!CokChannelDef.isInChat())
			refreshMemberSelectBtn();
		
		this.relativeLayout1 = (LinearLayout) view.findViewById(R.id.relativeLayout1);
		this.messagesListFrameLayout = (RelativeLayout) view.findViewById(R.id.gs__messagesListLayout);
		messagesListLayout = (RelativeLayout) view.findViewById(R.id.messagesListLayout);
		ImageUtil.setYRepeatingBG(activity, messagesListLayout, R.drawable.mail_list_bg);
		
		imageView1 = (ImageView) view.findViewById(R.id.imageView1);
		imageView2 = (ImageView) view.findViewById(R.id.imageView2);
		horn_checkbox = (CheckBox) view.findViewById(R.id.horn_checkbox);
//		horn_tip_layout = (LinearLayout) view.findViewById(R.id.horn_tip_layout);
//		horn_text_tip = (TextView) view.findViewById(R.id.horn_text_tip);
//		horn_text_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_HORN_TEXT));
		horn_scroll_text = (ScrollText) view.findViewById(R.id.horn_scroll_text);
		horn_scroll_text.setChannelType(CokConfig.getHornChannelType());
		horn_name = (TextView) view.findViewById(R.id.horn_name);
		horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.horn_scroll_layout);
		horn_scroll_layout.setVisibility(View.GONE);
		horn_close_btn = (ImageView) view.findViewById(R.id.horn_close_btn);

		noContentLayout = (FrameLayout) view.findViewById(R.id.noContentLayout);
		noContentTextView = ((TextView) view.findViewById(R.id.noContentTextView));
		noContentBtnLayout = (FrameLayout) view.findViewById(R.id.noContentBtnLayout);
		noContentButton = (Button) view.findViewById(R.id.noContentButton);
		
		custom_setting_layout = (LinearLayout) view.findViewById(R.id.custom_setting_layout);
		add_title = (TextView) view.findViewById(R.id.add_title);
		add_title.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD));
		add_tip = (TextView) view.findViewById(R.id.add_tip);
		if(CustomChannelData.getInstance().hasCustomChannel())
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP2));
		else
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP));
		customChannelHeadImage = (RoundImageView) view.findViewById(R.id.headImage);
		customChannelHeadImage.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				refreshCustomChannelImage(null);
				if(customChannelListAdapter!=null)
					customChannelListAdapter.notifyDataSetChanged();
			}
		});
		
		custom_setting_layout.setVisibility(View.GONE);
		customChannelName = (TextView) view.findViewById(R.id.name);
		custom_mod_image = (ImageView) view.findViewById(R.id.custom_mod_image);
		
		custom_head_layout = (FrameLayout) view.findViewById(R.id.member_head_layout);
		custom_expand_listview = (ExpandableListView) view.findViewById(R.id.custom_expand_listview);
		custom_expand_listview.setGroupIndicator(null);

		prepareCustomChannelData();
		
		refreshNoContentUI();
		
		headImageContainer = (FixedAspectRatioFrameLayout) view.findViewById(R.id.headImageContainer);
		
		sendMessageLayout = (LinearLayout) view.findViewById(R.id.gs__sendMessageLayout);
		voice_rec_button_layout = (LinearLayout) view.findViewById(R.id.voice_rec_button_layout);
		voice_rec_button = (Button) view.findViewById(R.id.voice_rec_button);
		
		slideInputField = new SlideInputField();
		slideInputField.init(activity, view, this);
		
		custom_channel_setting_layout = (RelativeLayout) view.findViewById(R.id.custom_channel_setting_layout);
		custom_channel_name = (TextView) view.findViewById(R.id.custom_channel_name);
		custom_channel_settting_btn = (ImageView) view.findViewById(R.id.custom_channel_settting_btn);
		
		custom_channel_settting_btn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
			}
		});
		
		custom_settting_btn_layout = (LinearLayout) view.findViewById(R.id.custom_settting_btn_layout);
		custom_setting_confim = (TextView) view.findViewById(R.id.custom_setting_confim);
		custom_setting_confim.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
		custom_setting_confim.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
				LocalConfig config = ConfigManager.getInstance().getLocalConfig();
				if(config == null)
				{
					config = new LocalConfig();
					ConfigManager.getInstance().setLocalConfig(config);
				}
				if(showingCustomChannel == null && (config.getCustomChannelType()!=-1 || StringUtils.isNotEmpty(config.getCustomChannelId())))
				{
					config.setCustomChannelType(-1);
					config.setCustomChannelId("");
					if(CustomChannelData.getInstance().hasCustomChannel())
					{
						LogUtil.trackAction("custom_channel_removed");
						CokChannelDef.postLatestCustomChatMessage(null);
						CustomChannelData.getInstance().customChannelChange = true;
					}
					refreshCustomChatChannel();
				}
				else if(showingCustomChannel != null && (config.getCustomChannelType()!=showingCustomChannel.getChannelType() || !config.getCustomChannelId().equals(showingCustomChannel.getChannelID())))
				{
					config.setCustomChannelType(showingCustomChannel.getChannelType());
					config.setCustomChannelId(showingCustomChannel.getChannelID());
					Channel channel = ChannelManager.getInstance().getChannel(showingCustomChannel.getChannelType(),showingCustomChannel.getChannelID());
					if(!CustomChannelData.getInstance().hasCustomChannel() && showingCustomChannel.getChannelType()!=-1)
					{
						LogUtil.trackAction("custom_channel_added");
						CustomChannelData.getInstance().customChannelChange = true;
						if(channel!=null)
							CokChannelDef.sendChatLatestMessage(channel);
					}
					else if(CustomChannelData.getInstance().hasCustomChannel() && showingCustomChannel.getChannelType()!=-1 && (CustomChannelData.getInstance().customChannelType!=showingCustomChannel.getChannelType() || (!CustomChannelData.getInstance().customChannelId.equals(showingCustomChannel.getChannelID()))))
					{
						LogUtil.trackAction("custom_channel_changed");
						CustomChannelData.getInstance().customChannelChange = true;
						if(channel!=null)
							CokChannelDef.sendChatLatestMessage(channel);
					}
					refreshCustomChatChannel();
				}
				
				hideCustomChannelSetting();
				
				refreshNoContentUI();
			}
		});

		if(isCustom())
		{
			refreshCustomChannelName(channelView.channel);
		}

		if (!lazyLoading || this.pullDownToLoadListView != null)
			renderList();

		this.replyField = ((EditText) view.findViewById(ResUtil.getId(this.activity, "id", "gs__messageText")));
		this.wordCount = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		if (CokChannelDef.isInMailDialog())
		{
			String title = UserManager.getInstance().getCurrentMail().opponentName;
			if (CokChannelDef.isInUserMail())
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(UserManager.getInstance().getCurrentMail().opponentUid);
				if (StringUtils.isNotEmpty(fromUid))
				{
					if (fromUid.equals(UserManager.getInstance().getCurrentUser().uid))
					{
						title = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
					}
					else
					{
						User fromUser = UserManager.getInstance().getUser(fromUid);
						if (fromUser != null && StringUtils.isNotEmpty(fromUser.userName))
						{
							title = fromUser.userName;
						}
					}
				}

			}

			if (CokChannelDef.isInChatRoom() && title.length() > 16)
			{
				title = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHATROOM);
				if (title.equals(""))
					title = "Group";
			}
			title += CokChannelDef.getChannelNamePostfix(UserManager.getInstance().getCurrentMail().opponentUid);
			getTitleLabel().setText(title);
		}

		refreshTextField(true);

		addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "gs__sendMessageBtn"));

		getMemberSelectButton().setVisibility(isSelectMemberBtnEnable && !CokChannelDef.isInBasicChat() ? View.VISIBLE : View.GONE);

		tooltipLayout = ((LinearLayout) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLayout")));
		tooltipLabel = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLabel")));
		tooltipArrow = ((ImageView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipArrow")));
		showToolTip(false);
		tooltipLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickToolTip();
			}
		});

		horn_close_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				hideHornScrollText();
				hornTextHidden = true;
			}
		});

		refreshSendButton();

		getMemberSelectButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				hideSoftKeyBoard();
				UIManager.showChatRoomSettingActivity(activity);
//				GSIM.showMemberSelectorActivity(activity, true);
			}
		});

		addReply.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onSendButtonClick();
			}
		});

		replyField.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == 4)
				{
					addReply.performClick();
				}
				return false;
			}
		});
		
		replyField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				EditText editText = (EditText) v;
				if(editText!=null)
				{
					String hint;
		             if (hasFocus) {
		                 hint = editText.getHint().toString();
		                 editText.setTag(hint);
		                 editText.setHint("");
		             } else {
		                 hint = editText.getTag().toString();
		                 editText.setHint(hint);
		             }
				}
			}
		});
		
		replyField.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (CokChannelDef.isChatRestrict())
				{
					MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
				}
			}
		});

		textChangedListener = new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				replyField.post(new Runnable()
				{
					@Override
					public void run()
					{
						refreshWordCount();
					}
				});
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		};
		this.replyField.addTextChangedListener(textChangedListener);

		getShowFriendButton().setVisibility(CokChannelDef.isInMailDialog() ? View.GONE : View.VISIBLE);
		getShowFriendButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				GSController.doHostAction("showFriend", "", "", "", false);
			}
		});

		showMessageBox();

		horn_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				refreshBottomUI(isChecked);
				if (isChecked)
					ConfigManager.isHornBtnEnable = true;
				else
					ConfigManager.isHornBtnEnable = false;
			}
		});

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if(isSelectedInViewPager())
				{
					checkFirstGlobalLayout();
					adjustHeight();
				}
			}
		};
		messagesListFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		((ChatActivity) getActivity()).fragment = this;

		if (!lazyLoading)
		{
			refreshToolTip();
			refreshHasMoreData();
		}

		Msg msgItem = ScrollTextManager.getInstance().getNextText(GSController.getCurrentChannelType());
		if (msgItem != null)
			showHornScrollText(msgItem);
		else
		{
			horn_scroll_layout.setVisibility(View.GONE);
		}
		
		initSensorListener();
		
		onNetworkConnectionChanged();
		
		imageView2.setVisibility(View.VISIBLE);

		horn_checkbox.setVisibility((CokChannelDef.canSendHorn(channelView.channelType) && ConfigManager.enableChatHorn) ? View.VISIBLE : View.GONE);
		
		initCustomUI();
		
		if (canShowJoinAlliancePopup())
		{
			try
			{
				showJoinAlliancePopup();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		
		adjustSizeCompleted = false;
	}
	
	protected boolean isSelectedInViewPager()
	{
		return this == ((ChatActivity) getActivity()).getCurrentFragment();
	}
	
	protected void refreshNoContentUI()
	{
		noContentLayout.setVisibility(CokChannelDef.getInstance().getChannelDef(channelView.channelType).canShowNoContentUI() ? View.VISIBLE : View.GONE);
		noContentTextView.setText(CokChannelDef.getInstance().getChannelDef(channelView.channelType).getNoContentLabel());
		noContentBtnLayout.setVisibility(CokChannelDef.getInstance().getChannelDef(channelView.channelType).canShowNoContentButton() ? View.VISIBLE : View.GONE);
		noContentButton.setText(CokChannelDef.getInstance().getChannelDef(channelView.channelType).noContentButtonLabel);
		noContentButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				CokChannelDef.getInstance().getChannelDef(channelView.channelType).noContentButtonHandler();
			}
		});

		noContentLayout.setVisibility(CokChannelDef.getInstance().getChannelDef(channelView.channelType)
				.canShowNoContentUI() ? View.VISIBLE : View.GONE);
		showMessageLayout(!CokChannelDef.getInstance().getChannelDef(channelView.channelType).canShowNoContentUI());
	}

	private void showMessageLayout(boolean show)
	{
		relativeLayout1.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	private boolean canShowJoinAlliancePopup()
	{
		return CokChannelDef.getInstance().getChannelDef(channelView.channelType).canShowJoinAlliancePopup() && !isJoinAlliancePopupShowing;
	}
	
	private boolean isCustom()
	{
		return CokChannelDef.isInCustomChat(channelView.channelType);
	}
	
	private boolean isInMail()
	{
		return CokChannelDef.isInUserMail(channelView.channelType) || isCustom();
	}
	
	private void onSendButtonClick()
	{
		final String message = replyField.getText().toString().trim();
		if (TextUtils.isEmpty(message))
			return;

		// if(replyText.endsWith("png"))
		// {
		// System.out.println("setCommonImage");
		// ImageUtil.setCommonImage(activity, replyText, imageView2);
		// }

		if (horn_checkbox.isChecked() && CokChannelDef.canSendHorn(channelView.channelType))
		{
			int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
			if (hornBanedTime == 0)
			{
				int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
				String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
				if (price == 0)
				{
					if (ConfigManager.isFirstUserHorn)
					{
						MenuController.showSendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn),
								new CallBack()
								{
									public void onCallback()
									{
										sendMsg(message, true, false, null);
									};
								});
					}
					else
					{
						sendMsg(message, true, false, null);
					}
				}
				else if (price > 0)
				{
					if (ConfigManager.isFirstUserCornForHorn)
					{
						MenuController.showSendHornWithCornConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn),
								price, new CallBack()
								{
									public void onCallback()
									{
										sendMsg(message, true, true, null);
									};
								});
					}
					else
					{
						boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough",
								new Object[] { Integer.valueOf(price) });
						if (isCornEnough)
						{
							sendMsg(message, true, true, null);
						}
						else
						{
							MenuController.showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
						}
					}
				}
			}
		}
		else
		{
			sendMsg(message, false, false, null);
		}
	}
	
	private void sendMsg(String messageText, boolean isHornMsg, boolean usePoint, String audioUrl)
	{
		if (!checkSendRestrict())
			return;
		
		clearInput();

		getCurrentChannel().sendMsg(messageText, isHornMsg, usePoint, audioUrl);
	}

	public void onDataSetChanged(ChannelChangeEvent event)
	{
		notifyDataSetChanged(false);
	}

	// 重发消息
	public void resendMsg(Msg msgItem, boolean isHornMsg, boolean usePoint)
	{
		if (!checkSendRestrict())
			return;

		getCurrentChannel().resendMsg(msgItem, isHornMsg, usePoint);
	}

	// 重发消息
	public void resendAudioMsg(Msg msgItem)
	{
		if (!checkSendRestrict())
			return;

		getCurrentChannel().resendAudioMsg(msgItem);
	}
	
	private boolean checkSendRestrict()
	{
		if (GSController.getCurrentChannelType() < 0 || !GSController.isSendIntervalValid())
			return false;
		if (CokChannelDef.isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return false;
		}
		if(getCurrentChannel() == null)
			return false;
		
		return true;
	}
	
	public void notifyCustomChannelDataSetChanged()
	{
		if(customChannelListAdapter!=null)
			customChannelListAdapter.notifyDataSetChanged();
	}

	public void showHornScrollText(Msg msgItem)
	{
		if (!(msgItem.isHornMessage() || msgItem.isStealFailedMessage()))
			return;
		
		if(!CokChannelDef.canSendHorn(channelView.channelType))
		{
			showHornScrollLayout(msgItem, false);
		}
		else
		{
			if(CokChannelDef.canSendHorn(msgItem.channelType))
			{
				hornTextHidden = false;
				showHornScrollLayout(msgItem, true);
			}
			else
			{
				if(msgItem.canEnterScrollTextQueue())
				{
					ScrollTextManager.getInstance().clear(msgItem.channelType);
					ScrollTextManager.getInstance().push(msgItem,msgItem.channelType);
				}
			}
		}
	}
	
	private void showHornScrollLayout(Msg msgItem, boolean visible)
	{
		if (horn_scroll_layout != null)
		{
			horn_scroll_layout.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (visible)
			{
				ScrollTextManager.getInstance().showScrollText(msgItem, horn_scroll_text, horn_name, horn_scroll_layout,
						msgItem.channelType);
			}
		}
	}

	private void refreshCustomChannelName(Channel channel)
	{
		if(channel == null)
		{
			if(custom_channel_setting_layout.getVisibility()!=View.GONE)
				custom_channel_setting_layout.setVisibility(View.GONE);
		}
		else
		{
			if(custom_channel_setting_layout.getVisibility()!=View.VISIBLE)
				custom_channel_setting_layout.setVisibility(View.VISIBLE);
			
			custom_channel_name.setText(CokChannelDef.getCustomChannelName(channel));
		}
	}
	
	private void refreshSettingCustomChannelName(Channel channel)
	{
		custom_mod_image.setVisibility(View.GONE);
		if(channel == null)
		{
			if(customChannelName.getVisibility()!=View.INVISIBLE)
				customChannelName.setVisibility(View.INVISIBLE);
		}
		else
		{
			if(customChannelName.getVisibility()!=View.VISIBLE)
				customChannelName.setVisibility(View.VISIBLE);
			
			if(CokChannelDef.isInUserMail(channel.getChannelType()))
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
				if(StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					User userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null)
					{
						nameText = userInfo.userName;
						if(StringUtils.isNotEmpty(userInfo.asn))
							nameText = "("+userInfo.asn+")"+nameText;
					}
					else
						nameText = channel.getCustomName();
					if(StringUtils.isNotEmpty(nameText) && StringUtils.isNotEmpty(CokChannelDef.getChannelNamePostfix(channel.getChannelID())))
					{
						custom_mod_image.setVisibility(View.VISIBLE);
					}
					customChannelName.setText(nameText);
				}
				else
				{
					if(StringUtils.isNotEmpty(channel.getCustomName()))
						customChannelName.setText(channel.getCustomName());
					else
						customChannelName.setText(channel.getChannelID());
				}
			}
			else if(CokChannelDef.isInChatRoom(channel.getChannelType()))
			{
				if(StringUtils.isNotEmpty(channel.getCustomName()))
					customChannelName.setText(channel.getCustomName());
				else
					customChannelName.setText(channel.getChannelID());
			}
		}
	}
	
	public void hideHornScrollText()
	{
		if(CokChannelDef.canSendHorn(channelView.channelType))
			ScrollTextManager.getInstance().shutDownScrollText(horn_scroll_text, GSController.getCurrentChannelType());
	}

	private boolean	lazyLoading	= true;

	protected void onBecomeVisible()
	{
		if (inited)
			return;
		timerDelay = 500;
		startTimer();
	}

	protected void onRecieveNewMsg(ChannelChangeEvent event)
	{
		onDataSetChanged(event);
		
		Channel c = event.channel;
		refreshIsInLastScreen();

		for (int i = 0; i < event.chatInfoArr.length; i++)
		{
			Msg recievedMsg = event.chatInfoArr[i];
			if(recievedMsg.isHornMessage())
				showHornScrollText(recievedMsg);
			if(recievedMsg.isAudioMessage())
				updateAudioHint();
		}
		
		if (event.chatInfoArr.length == 1)
			smoothUpdateListPositionForNewMsg(event.hasSelfMsg);
		else
			updateListPositionForNewMsg(event.hasSelfMsg);
	}

	protected void onRecieveOldMsg(ChannelChangeEvent event)
	{
		int loadCount = event.loadCount;
		Msg oldFirstItem = event.oldFirstItem;
		Msg[] chatInfoArr = event.chatInfoArr;
		Channel c = event.channel;
		
		if (loadCount > 0)
			c.getLoadedTimeNeedShowMsgIndex(loadCount);

		if (CokChannelDef.canSendHorn(c.getChannelType())
				&& !ScrollTextManager.getInstance().getHandler(c.getChannelType()).oldHornMsgPushed)
		{
			if (c.msgList != null && c.msgList.size() > 0)
			{
				for (int i = 0; i < c.msgList.size(); i++)
				{
					Msg msgItem = c.msgList.get(i);
					if (msgItem != null && msgItem.canEnterScrollTextQueue())
					{
						ScrollTextManager.getInstance().clear(c.getChannelType());
						ScrollTextManager.getInstance().push(msgItem, c.getChannelType());
						ScrollTextManager.getInstance().getHandler(c.getChannelType()).oldHornMsgPushed = true;
					}
				}
			}
		}

		onDataSetChanged(event);
		refreshIsInLastScreen();
		updateListPositionForOldMsg(loadCount, !MsgUtil.isDifferentDate(oldFirstItem, c.msgList));
		resetMoreDataStart();
	}
	
	private void initCustomUI()
	{
		custom_channel_setting_layout.setVisibility((isCustom() && !isSettingCustomChannel && !CokChannelDef.getInstance()
				.getChannelDef(channelView.channelType).canShowNoContentUI()) ? View.VISIBLE : View.GONE);
		custom_setting_layout.setVisibility(isCustom() && isSettingCustomChannel ? View.VISIBLE : View.GONE);
		custom_settting_btn_layout.setVisibility(isCustom() && isSettingCustomChannel ? View.VISIBLE : View.GONE);
	}
	
	public void showCustomChannelSetting()
	{
		custom_setting_layout.setVisibility(View.VISIBLE);
		activity.hideSoftKeyBoard();
		replyField.clearFocus();
		isSettingCustomChannel = true;
		relativeLayout1.setVisibility(View.GONE);
		custom_settting_btn_layout.setVisibility(View.VISIBLE);
		if (channelView != null && isCustom())
		{
			refreshCustomChannelImage(channelView.channel);
			if(customChannelListAdapter!=null)
				customChannelListAdapter.notifyDataSetWithSort();
		}
	}
	
	public void hideCustomChannelSetting()
	{
		custom_setting_layout.setVisibility(View.GONE);
		isSettingCustomChannel = false;
		relativeLayout1.setVisibility(CustomChannelData.getInstance().hasCustomChannel()? View.VISIBLE : View.GONE);
		custom_settting_btn_layout.setVisibility(View.GONE);
		custom_channel_setting_layout.setVisibility(CustomChannelData.getInstance().hasCustomChannel() ? View.VISIBLE : View.GONE);
		
		horn_scroll_layout.setVisibility(View.GONE);
	}

	PullDownToLoadMoreView pullDownToLoadListView;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void renderList()
	{
		if(channelView == null)
			return;
		
		if(pullDownToLoadListView == null){
			pullDownToLoadListView = new PullDownToLoadMoreView(activity);
			pullDownToLoadListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	
			pullDownToLoadListView.setTopViewInitialize(true);
			pullDownToLoadListView.setAllowPullDownRefersh(false);
			pullDownToLoadListView.setBottomViewWithoutScroll(false);
			pullDownToLoadListView.setListViewLoadListener(mListViewLoadListener);
			pullDownToLoadListView.setListViewTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					onContentAreaTouched();
					return false;
				}
			});
	
			ListView messagesListView = new ListView(activity);
			messagesListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			messagesListView.setVerticalFadingEdgeEnabled(false);
			messagesListView.setCacheColorHint(Color.TRANSPARENT);
			messagesListView.setDivider(null);
	
			List<Msg> msgList = null;
			if(channelView.channel != null)
				msgList = channelView.channel.msgList;
			if(msgList == null)
				msgList = new ArrayList<Msg>();
			
			if (msgList != null)
			{
				MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
				channelView.setMessagesAdapter(adapter);
			}
			if(CokChannelDef.canSendAudio(channelView.channelType))
				XiaoMiToolManager.getInstance().addAudioListener(channelView.getMessagesAdapter());
			messagesListView.setAdapter(channelView.getMessagesAdapter());
	
			messagesListView.setOnScrollListener(mOnScrollListener);
			messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
			messagesListView.setKeepScreenOn(true);
	
			pullDownToLoadListView.addView(messagesListView);
	
			channelView.pullDownToLoadListView = pullDownToLoadListView;
			channelView.messagesListView = messagesListView;
	
			if (channelView.channel!=null && channelView.channel.lastPosition.x != -1 && rememberPosition)
			{
				channelView.messagesListView.setSelectionFromTop(channelView.channel.lastPosition.x, channelView.channel.lastPosition.y);
			}
			else
			{
				if(channelView.getMessagesAdapter()!=null)
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
			}
		}
		
		if(pullDownToLoadListView.getParent() != null)
		{
			((RelativeLayout) pullDownToLoadListView.getParent()).removeView(pullDownToLoadListView);
		}
		messagesListFrameLayout.addView(pullDownToLoadListView);
		
		messagesListFrameLayout.setOnHierarchyChangeListener(new OnHierarchyChangeListener(){
			@Override
			public void onChildViewAdded(View parent, View child)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "child", child);
			}

			@Override
			public void onChildViewRemoved(View parent, View child)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "child", child);
			}});
		
		if (lazyLoading)
		{
			System.out.println("lazyLoading refreshTab");
			refreshTab();
		}
		activity.hideProgressBar();
	}

	protected void refreshTab()
	{
		refreshWordCount();
	}
	
	protected void onNetworkConnectionChanged()
	{
		if(voice_rec_button_layout.getVisibility() == View.VISIBLE)
		{
			// 在切换tab后，可能会被重刷成alpha为1；在连接恢复后，可能因为不可见而没有设为enable；还需要增加调用
			refreshAudioButton();
		}
		else
		{
			refreshSendButton();
		}
	}
	
	private boolean isWSConnectionAvailable()
	{
		boolean result = true;
		Channel channel = null;
		if(CokChannelDef.isInCustomChat())
			channel = ChannelManager.getInstance().getChannel(CustomChannelData.getInstance().customChannelType, CustomChannelData.getInstance().customChannelId);
		else
			channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel == null)
		{
			result = false;
		}
		else //if (WebSocketManager.isRecieveFromWebSocket(channel.getChannelType()) || WebSocketManager.isSendFromWebSocket(channel.getChannelType()))
		{
			result = ConfigManager.isWebSocketNetWorkNormal();
		}
		return result;
	}

	protected void refreshAudioButton()
	{
		if (!isWSConnectionAvailable())
		{
			voice_rec_button_layout.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(voice_rec_button_layout, false);
		}
		else
		{
			voice_rec_button_layout.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(voice_rec_button_layout, true);
		}
	}

	protected void refreshSendButton()
	{
		if (this.replyField.getText().length() == 0 || !isWSConnectionAvailable())
		{
			addReply.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, false);
		}
		else
		{
			addReply.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, true);
		}
	}

	public void showToolTip(boolean b)
	{
		tooltipLayout.setVisibility(b ? View.VISIBLE : View.GONE);
	}

	private void onClickToolTip()
	{
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel == null) return;
		
		if (false)//!WebSocketManager.isRecieveFromWebSocket(channel.getChannelType()))
		{
//			if (channel.canLoadAllNew())
//			{
//				getCurrentChannel().setLoadingStart(true);
//				loadMoreCount = 0;
//				channel.isLoadingAllNew = true;
//				channel.hasLoadingAllNew = true;
//				ChannelManager.getInstance().loadAllNew(channel);
//
//				refreshToolTip();
//			}
		}
		else
		{
			if (channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
			{
				channel.wsNewMsgCount = 0;
				updateListPositionForOldMsg(0, false);
			}
		}
	}

	public void refreshToolTip()
	{
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		// 未加入联盟时，channel不存在
		if (channel == null || isInMail())
		{
			showToolTip(false);
			return;
		}

		refreshToolTipInWSServer(channel);
	}

	private void refreshToolTipInWSServer(Channel channel)
	{
		// 第一次加载历史消息后，重置channel.wsNewMsgCount
		// TODO 应该改为显示到第一条消息后重置
		// if(channel.wsNewMsgCount > 0 && channel.msgList.size() !=
		// channel.wsNewMsgCount)
		// {
		// channel.wsNewMsgCount = 0;
		// }

		if (channel != null && channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
		{
			String newMsgCount = channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.wsNewMsgCount + ""
					: ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
			tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
			showToolTip(true);
		}
		else
		{
			showToolTip(false);
		}
	}

	public void clearInput()
	{
		replyField.setText("");
	}

	private boolean isHornUI;
	private void refreshBottomUI(boolean isChecked)
	{
		if (!isChecked)
			GSController.isHornItemUsed = false;
		isHornUI = isChecked && CokChannelDef.canSendHorn(channelView.channelType) && ConfigManager.enableChatHorn;
		String background = "btn_chat_send";
		String bottomBg = "chuzheng_frame02";
		String lineBg = "line_grey02";
		if(isHornUI)
		{
			background = "btn_chat_send_horn";
			bottomBg = "bottom_bg";
			lineBg = "line_brown";
		}
		else
		{
			if(CokChannelDef.isInBattleField())
			{
				background = "btn_battle_chat_send";
				bottomBg = "battle_bottom_bg";
				lineBg = "line_battle_bottom";
			}
		}
		
		String inputBg = isHornUI ? "text_field_horn" : "text_field_bg2";
		addReply.setBackgroundResource(ResUtil.getId(activity, "drawable", background));
		if(isHornUI){
			relativeLayout1.setBackgroundColor(0xffffcb64);
		}else{
			relativeLayout1.setBackgroundResource(ResUtil.getId(activity, "drawable", bottomBg));
		}
		replyField.setBackgroundResource(ResUtil.getId(activity, "drawable", inputBg));
//		horn_tip_layout.setVisibility(isHornUI ? View.VISIBLE : View.GONE);
//		imageView1.setVisibility(isHornUI ? View.GONE : View.VISIBLE);
		imageView1.setImageResource(ResUtil.getId(activity, "drawable", lineBg));
		setMaxInputLength(isHornUI);
		resetInputButton(false);
	}

	private void setMaxInputLength(boolean isHornUI)
	{
		curMaxInputLength = isHornUI && ConfigManager.maxHornInputLength > 0 ? ConfigManager.maxHornInputLength : 500;
		replyField.setFilters(new InputFilter[] { new InputFilter.LengthFilter(curMaxInputLength) });
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void showJoinAlliancePopup()
	{
		final AlertDialog dlg = new AlertDialog.Builder(activity).create();
		dlg.setCancelable(true);
		dlg.setCanceledOnTouchOutside(true);
		dlg.show();
		isJoinAlliancePopupShowing = true;
		Window window = dlg.getWindow();
		window.setBackgroundDrawable(new ColorDrawable());
		window.setContentView(R.layout.cs__first_alliance_popup);
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		TextView first_join_title = (TextView) window.findViewById(R.id.first_join_title);
		first_join_title.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_JOIN_ALLIANCE));

		TextView first_join_title_tip = (TextView) window.findViewById(R.id.first_join_title_tip);
		first_join_title_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE_TITLE, "200"));

		TextView first_join_alliance_text1 = (TextView) window.findViewById(R.id.first_join_alliance_text1);
		first_join_alliance_text1.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_1));

		TextView first_join_alliance_text2 = (TextView) window.findViewById(R.id.first_join_alliance_text2);
		first_join_alliance_text2.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_2));

		TextView first_join_alliance_text3 = (TextView) window.findViewById(R.id.first_join_alliance_text3);
		first_join_alliance_text3.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_3));

		TextView first_join_alliance_text4 = (TextView) window.findViewById(R.id.first_join_alliance_text4);
		first_join_alliance_text4.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_4));

		RelativeLayout firstJoinAllianceLayout = (RelativeLayout) window.findViewById(R.id.firstAllianceLayout);

		firstJoinAllianceLayout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				dlg.cancel();
				isJoinAlliancePopupShowing = false;
				return false;
			}
		});

		Button joinAllianceBtn = (Button) window.findViewById(R.id.joinAllianceBtn);
		joinAllianceBtn.setText(LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));
		joinAllianceBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				isJoinAlliancePopupShowing = false;

				GSController.doHostAction("joinAllianceBtnClick", "", "", "", true);
			}
		});

		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(joinAllianceBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_title_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text1, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text2, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text3, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text4, ConfigManager.scaleRatio);
		}
	}

	public class LoadMoreMsgParam
	{
		public long		requestMinTime;
		public long		requestMaxTime;

		public int		requestMinSeqId;
		public int		requestMaxSeqId;

		public boolean	useTime;

		public boolean	fetchFromServer;

		public LoadMoreMsgParam(int minSeqId, int maxSeqId, boolean fetchFromServer)
		{
			useTime = false;
			this.requestMinSeqId = minSeqId;
			this.requestMaxSeqId = maxSeqId;
			this.fetchFromServer = fetchFromServer;
		}

		public LoadMoreMsgParam(long requestMinTime, long requestMaxTime, boolean fetchFromServer)
		{
			useTime = true;
			this.requestMinTime = requestMinTime;
			this.requestMaxTime = requestMaxTime;
			this.fetchFromServer = fetchFromServer;
		}

		public int getRequestCount()
		{
			return requestMaxSeqId - requestMinSeqId + 1;
		}
	}

	/**
	 * 获取加载区间的逻辑，也是检查能否加载的逻辑
	 */
	private LoadMoreMsgParam getLoadMoreMsgParam(Channel channel)
	{
		if (!CokChannelDef.isInChat())
		{
			return null;
		}
		if (channel.msgList == null)
		{
			return null;
		}

		return getLoadMoreMsgParamByTime(channel);
	}

	private LoadMoreMsgParam getLoadMoreMsgParamByTime(Channel channel)
	{
		Pair<Long, Long> range = channel.getLoadMoreTimeRange();
		if (range != null)
		{
			return new LoadMoreMsgParam(range.first, range.second, false);
		}

		return null;
	}

//	private LoadMoreMsgParam getLoadMoreMsgParamBySeqId(Channel channel)
//	{
//		int viewMinSeqId = channel.getMinSeqId();
//
//		// 不能加载: 没有消息时viewMinSeqId为0，有消息时seqId最小为1
//		if (viewMinSeqId <= 1)
//		{
//			return null;
//		}
//
//		// desireMaxSeqId可能等于desireMinSeqId，仅当二者都为1时
//		int desireMaxSeqId = DBManager.getInstance().getLoadMoreMaxSeqId(channel.getChatTable(), viewMinSeqId);
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "desireMaxSeqId", desireMaxSeqId, "viewMinSeqId", viewMinSeqId);
//		int desireMinSeqId = (desireMaxSeqId - 19) > 1 ? (desireMaxSeqId - 19) : 1;
//
//		// 如果desireMaxSeqId在本地db中有，就从db加载（不一定能满20条）
//		if (DBManager.getInstance().isMsgExists(channel.getChatTable(), desireMaxSeqId, -1))
//		{
//			return new LoadMoreMsgParam(desireMinSeqId, desireMaxSeqId, false);
//		}
//
//		// 否则，如果在server范围内，从server加载
//		// server中seqId连续，可以用交集判断
//		Point inter = getIntersection(new Point(channel.serverMinSeqId, channel.serverMaxSeqId), new Point(desireMinSeqId, desireMaxSeqId));
//		if (inter != null)
//		{
//			return new LoadMoreMsgParam(inter.x, inter.y, true);
//		}
//
//		// 既不在db，又不在server（再往前的也肯定不在server），则找到db中最早的，加载之
//		Point range = DBManager.getInstance().getHistorySeqIdRange(channel.getChatTable(), desireMaxSeqId, ChannelManager.LOAD_MORE_COUNT);
//		if (range != null)
//		{
//			return new LoadMoreMsgParam(range.x, range.y, false);
//		}
//
//		return null;
//	}

	/**
	 * 计算两段连续区间的交集
	 * 
	 * @param sec1
	 *            [sec1.x, sec1.y]组成的区间
	 * @param sec2
	 *            [sec2.x, sec2.y]组成的区间
	 * @return null，如果无交集
	 */
	public static Point getIntersection(Point sec1, Point sec2)
	{
		int[] fourValue = { sec1.x, sec1.y, sec2.x, sec2.y };
		Arrays.sort(fourValue); // 升序排序
		int lower = -1;
		int upper = -1;
		for (int i = 0; i < fourValue.length; i++)
		{
			if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y)
			{
				lower = fourValue[i];
				break;
			}
		}
		for (int i = fourValue.length - 1; i >= 0; i--)
		{
			if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y)
			{
				upper = fourValue[i];
				break;
			}
		}
		if (lower != -1 && upper != -1)
		{
			return new Point(lower, upper);
		}
		else
		{
			return null;
		}
	}

	private boolean checkMessagesAdapter()
	{
		if (channelView == null || channelView.getMessagesAdapter() == null)
		{
			// 初始化时showTab肯定会发生，此时getCurrentChannel().getMessagesAdapter()为null
//			LogUtil.trackMessage("checkMessagesAdapter() fail: currentChannel = " + getCurrentChannel() + " messagesAdapter = "
//					+ (getCurrentChannel() == null ? "null" : getCurrentChannel().getMessagesAdapter()) + " currentChatType = "
//					+ GSController.getCurrentChannelType() + " chatActivity = " + GSIM.getChatActivity()
//					+ " chatFragment = " + GSIM.getChatFragment());
			return false;
		}
		return true;
	}

	private void loadMoreMsg()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		// 极少情况下会发生
		if (channel == null)
			return;
		LoadMoreMsgParam loadMoreMsgParam = getLoadMoreMsgParam(channel);

		if (!channelView.getLoadingStart() && loadMoreMsgParam != null)
		{
			LogUtil.trackPageView("LoadMoreMsg");
			channelView.setLoadingStart(true);
			// 可能有异常 getCount() on a null object reference
			loadMoreCount = 0;
			channel.isLoadingAllNew = false;
			if (loadMoreMsgParam.fetchFromServer)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从server加载消息");
				JniController.getInstance().excuteJNIVoidMethod(
						"getMsgBySeqId",
						new Object[] {
								Integer.valueOf(loadMoreMsgParam.requestMinSeqId),
								Integer.valueOf(loadMoreMsgParam.requestMaxSeqId),
								Integer.valueOf(channel.getChannelType()),
								channel.getChannelID() });
			}
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从db加载消息");
//				if (!loadMoreMsgParam.useTime)
//				{
//					ChannelManager.getInstance().loadMoreMsgFromDB(channel, loadMoreMsgParam.requestMinSeqId,
//							loadMoreMsgParam.requestMaxSeqId, -1, false);
//				}
//				else
//				{
					ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
//				}
			}
		}

	}

	private void loadMoreMail()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		if (!channelView.getLoadingStart() && hasMoreData())
		{
			LogUtil.trackPageView("LoadMoreMail");
			if (GSController.isNewMailListEnable)
			{
				Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
				ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
			}
			else
			{
				channelView.setLoadingStart(true);
				loadMoreCount = 0;

				JniController.getInstance().excuteJNIVoidMethod(
						"requestMoreMail",
						new Object[] {
								UserManager.getInstance().getCurrentMail().opponentUid,
								UserManager.getInstance().getCurrentMail().mailUid,
								Integer.valueOf(channelView.getMessageCount()) });
			}
		}
	}

	/**
	 * 时机：各个参数变化时、初始化时 server数据变化时：GetNewMsg返回时 view数据变化时：获取到新消息时
	 */
	public void refreshHasMoreData()
	{
		if (!isInMail())
		{
			if (ChannelManager.getInstance().isGetingNewMsg)
			{
				hasMoreData = false;
			}
			else
			{
				if(channelView.channel == null)
					hasMoreData = false;
				else
					hasMoreData = getLoadMoreMsgParam(channelView.channel) != null;
			}
		}
		else if (GSController.isNewMailListEnable)
		{
			Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
			if (channel == null)
			{
				hasMoreData = false;
				return;
			}
			// TODO
//			List<Msg> dbUserMails = DBManager.getInstance().getMsgsByTime(channel.getChatTable(), channel.getMinCreateTime(), 1);
//			hasMoreData = dbUserMails.size() > 0;
		}
	}

	public void refreshStatusLabel(String status)
	{
		if (StringUtils.isNotEmpty(status))
		{
			getTitleLabel().setText(status);
		}
		else
		{
			getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
		}
		onNetworkConnectionChanged();
	}

	public boolean hasMoreData()
	{
		return hasMoreData;
	}

	private ListViewLoadListener	mListViewLoadListener	= new ListViewLoadListener()
															{
																@Override
																public void refreshData()
																{
																	if (isInMail())
																	{
																		loadMoreMail();
																	}
																	else
																	{
																		loadMoreMsg();
																	}
																}

																@Override
																public boolean getIsListViewToTop()
																{
																	if (channelView == null
																			|| channelView.messagesListView == null)
																		return false;
																	ListView listView = channelView.messagesListView;

																	View topListView = listView.getChildAt(listView
																			.getFirstVisiblePosition());
																	if ((topListView == null) || (topListView.getTop() != 0))
																	{
																		return false;
																	}
																	else
																	{
																		return true;
																	}
																}

																@Override
																public boolean getIsListViewToBottom()
																{
																	if (channelView == null
																			|| channelView.messagesListView == null)
																		return false;
																	ListView listView = channelView.messagesListView;
																	View bottomView = listView.getChildAt(-1 + listView.getChildCount());
																	if (bottomView == null)
																		return false;
																	if (bottomView.getBottom() > listView.getHeight()
																			|| (listView.getLastVisiblePosition() != -1
																					+ listView.getAdapter().getCount()))
																	{
																		return false;
																	}
																	else
																	{
																		return true;
																	}
																}
															};

	private void createTimerTask()
	{
		if(channelView == null)
			return;
		channelView.mTimer = new Timer();
		channelView.mTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (activity == null)
					return;
				activity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (channelView.pullDownToLoadListView != null && channelView.channel!=null)
							{
								channelView.pullDownToLoadListView.hideProgressBar();
								resetMoreDataStart();
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}
		};
		if (channelView.mTimer != null)
			channelView.mTimer.schedule(channelView.mTimerTask, 5000);
	}

	private boolean				hasMoreData			= true;

	private OnScrollListener	mOnScrollListener	= new AbsListView.OnScrollListener()
													{

														@Override
														public void onScrollStateChanged(AbsListView view, int scrollState)
														{
															if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
															{
																if (channelView != null
																		&& channelView.messagesListView != null)
																{
																	View topView = channelView.messagesListView
																			.getChildAt(channelView.messagesListView
																					.getFirstVisiblePosition());
																	if ((topView != null) && (topView.getTop() == 0)
																			&& !channelView.getLoadingStart())
																	{
																		channelView.pullDownToLoadListView.startTopScroll();
																	}
																}

															}

															if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
																GSController.isListViewFling = true;
															else
																GSController.isListViewFling = false;
														}

														@Override
														public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
																int totalItemCount)
														{
															if (channelView != null
																	&& channelView.pullDownToLoadListView != null
																	&& channelView.pullDownToLoadListView.getVisibility() == View.VISIBLE)
															{
																if (hasMoreData())
																{
																	if (!channelView.getLoadingStart())
																	{
																		channelView.pullDownToLoadListView
																				.setAllowPullDownRefersh(false);
																	}
																	else
																	{
																		channelView.pullDownToLoadListView
																				.setAllowPullDownRefersh(true);
																	}
																}
																else
																{
																	channelView.pullDownToLoadListView
																			.setAllowPullDownRefersh(true);
																}
															}
														}
													};

	public int getToastPosY()
	{
		int[] location = { 0, 0 };
		messagesListFrameLayout.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(activity, 5);
	}
	
	public void onSelected(boolean tabChanged)
	{
		saveDraft();
		
		if(tabChanged)
		{
			activity.hideSoftKeyBoard();
			replyField.clearFocus();
		}
		
		GSController.setCurrentChannelType(channelView.channelType);

		refreshHornState();
		
		refreshTextField(false);

		if (isSettingCustomChannel)
			refreshCustomChannelImage(channelView.channel);
		
		refreshInputButton();
//		getShowFriendButton().setVisibility(CokChannelDef.isInMailDialog() || (CokChannelDef.isInCustomChat()) ? View.GONE : View.VISIBLE);
//		setSelectMemberBtnState();
		
		if(channelView == null)
			return;
		
		if (channelView != null && channelView.getVisibility() == View.VISIBLE)
		{
			refreshToolTip();
			if (CokChannelDef.canSendHorn(channelView.channelType) && !hornTextHidden)
			{
				Msg msgItem = ScrollTextManager.getInstance().getNextText(GSController.getCurrentChannelType());
				if (msgItem != null)
					showHornScrollText(msgItem);
				else
				{
					horn_scroll_layout.setVisibility(View.GONE);
				}
			}
			else
			{
				horn_scroll_layout.setVisibility(View.GONE);
			}
			refreshHasMoreData();

			if (channelView.channel != null)
			{
				channelView.channel.getTimeNeedShowMsgIndex();
				channelView.channel.markAsRead();
			}
		}
		else
		{
			showToolTip(false);
			horn_scroll_layout.setVisibility(View.GONE);
		}
	}
	
	private void refreshHornState()
	{
		if (CokChannelDef.canSendHorn(channelView.channelType))
		{
			if (GSController.isHornItemUsed && ConfigManager.enableChatHorn)
			{
				horn_checkbox.setChecked(true);
				refreshBottomUI(true);
				ConfigManager.isHornBtnEnable = true;
			}
			else
			{
				horn_checkbox.setChecked(ConfigManager.isHornBtnEnable);
				refreshBottomUI(ConfigManager.isHornBtnEnable);
			}
		}
		else
		{
			refreshBottomUI(false);
		}
	}
	
	/**
	 * 仅在初始化时才可以显示联盟分享，退出界面后不再显示
	 */
	private void refreshTextField(boolean showAllianceShareText)
	{
		if (CokChannelDef.isChatRestrictForLevel(channelView.channelType))
		{
			replyField.setEnabled(false);
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
					"" + CokChannelDef.getChatRestrictLevel()));
		}
		else
		{
			updateAudioHint();
			replyField.setEnabled(true);
			
			if (showAllianceShareText && GSController.needShowAllianceDialog){
				replyField.setText(LanguageManager.getLangByKey(LanguageKeys.INPUT_ALLIANCE_DIALOG));
			}else
			{
				restoreDraft(channelView.channel);
			}
		}
	}
	
	private void restoreDraft(Channel channel)
	{
		if (channel != null && StringUtils.isNotEmpty(channel.getDraft()))
		{
			replyField.setText(channel.getDraft());
			replyField.setSelection(channel.getDraft().length());
		}
		else
		{
			replyField.setText("");
		}
	}

	public void updateAudioHint()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if(!CokChannelDef.canSendHorn(channelView.channelType) && (config == null || (config!=null && !config.isAudioUsed())))
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_USE));
		else
			replyField.setHint("");
	}
	
	private void updateHornLayoutHeight(RelativeLayout horn_scroll_layout)
	{
		if(horn_scroll_layout!=null)
		{
			int length = CokConfig.needShowBattleFieldChannel() ? (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio
					* activity.getScreenCorrectionFactor() + ScaleUtil.dip2px(6)) : ScaleUtil.dip2px(10);
			RelativeLayout.LayoutParams horn_scroll_layoutLayoutParams = (RelativeLayout.LayoutParams) horn_scroll_layout.getLayoutParams();

			if (horn_scroll_layoutLayoutParams.topMargin != length)
			{
				horn_scroll_layoutLayoutParams.topMargin = length;
				horn_scroll_layout.setLayoutParams(horn_scroll_layoutLayoutParams);
			}
		}
	}

	private int previousTextCount = 0;
	/**
	 * 没有文字时，设置初始状态
	 */
	private void resetInputButton(boolean resetVisibility)
	{
		if(resetVisibility)
		{
			sendMessageLayout.setVisibility(canShowRecordButton() ? View.GONE : View.VISIBLE);
			voice_rec_button_layout.setVisibility(canShowRecordButton() ? View.VISIBLE : View.GONE);
		}
		
		// 在联盟输入文字再删除，返回国家，按钮会不显示，因为被动画代码改变了alpha和缩放
        ViewProxy.setAlpha(voice_rec_button_layout, 1.0f);
        ViewProxy.setScaleX(voice_rec_button_layout, 1.0f);
        ViewProxy.setScaleY(voice_rec_button_layout, 1.0f);
        ViewProxy.setAlpha(sendMessageLayout, 1.0f);
        ViewProxy.setScaleX(sendMessageLayout, 1.0f);
        ViewProxy.setScaleY(sendMessageLayout, 1.0f);
	}
	
	public void saveDraft()
	{
		if(replyField == null)
			return;
		String replyFieldText = replyField.getText().toString();
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if(channel!=null && replyFieldText!=null)
		{
			channel.setDraft(replyFieldText);
			channel.setDraftTime(TimeManager.getInstance().getCurrentTimeMS());
			channel.updateDB();
		}
	}
	
	@Override
	public void onPause()
	{
		System.out.println("onPause saveDraft");
		saveDraft();
		super.onPause();
	}
	
	private boolean canShowRecordButton()
	{
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN 
				&& ConfigManager.isXMEnabled && ConfigManager.isXMAudioEnabled 
				&& !CokChannelDef.canSendHorn(channelView.channelType) && !isHornUI;
	}
	
	private void refreshInputButton()
	{
		if(canShowRecordButton())
		{
			slideInputField.checkSendButton(true);
		}else{
			resetInputButton(true);
		}
	}
	
	private void refreshWordCount()
	{
		if (replyField == null || wordCount == null)
			return;

		// 有文字与没文字之间发生切换时
		if((previousTextCount == 0 && replyField.getText().length() > 0) || (previousTextCount > 0 && replyField.getText().length() == 0))
		{
			previousTextCount = replyField.getText().length();
			
			refreshInputButton();
		}
		
		if (replyField.getLineCount() >= 2)
		{
			wordCount.setVisibility(View.VISIBLE);
		}
		else
		{
			wordCount.setVisibility(View.GONE);
		}
		wordCount.setText(replyField.getText().length() + "/" + curMaxInputLength);
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	public void onBackClicked()
	{
	}

	private final static int	sendButtonBaseWidth		= 60;
	private final static int	sendButtonBaseHeight	= 61;
	private final static int	hornCheckBoxWidth		= 70;
	int targetButtonWidth;
	int targetButtonHeight;
	
	public static double getAudioUIScale()
	{
		return ConfigManager.getInstance().needScaleInputPanel() ? ConfigManager.scaleRatio : 1;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (addReply.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (addReply.getWidth() != 0 && !adjustSizeCompleted)
		{
	        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	        if (resourceId > 0) {
	            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
	        }
	        
	        int originalWidth = addReply.getWidth();
        	int scaleWidth = (int) Math.floor(ScaleUtil.getScreenWidth() / 14);
	        if(ConfigManager.getInstance().needScaleInputPanel() && scaleWidth > originalWidth){
	        	targetButtonWidth = scaleWidth;
	        }else{
	        	targetButtonWidth = originalWidth;
	        }
	        
	        double editTextRatio = (double) targetButtonWidth / (double) originalWidth;
	        
			// S3手机上的尺寸(目标效果是在S3手机上调的好，界面、文字都相对于它进行缩放)
			// addReply宽度是宽度的1/4，让其高度保持长宽比，然后再计算出缩放的倍率（textRatio）
			double sendButtonRatio = (double) sendButtonBaseHeight / (double) sendButtonBaseWidth;
	        targetButtonHeight = (int) (targetButtonWidth * sendButtonRatio);
	        
	        updateHornLayoutHeight(horn_scroll_layout);

	        LinearLayout.LayoutParams relativeLayoutLayoutParams = (LinearLayout.LayoutParams) relativeLayout1.getLayoutParams();
			relativeLayoutLayoutParams.height = targetButtonHeight + ScaleUtil.dip2px(5);
			relativeLayout1.setLayoutParams(relativeLayoutLayoutParams);
	        
			LinearLayout.LayoutParams checkboxParams = (LinearLayout.LayoutParams) horn_checkbox.getLayoutParams();
			checkboxParams.width = targetButtonHeight;
			checkboxParams.height = targetButtonHeight;
			horn_checkbox.setLayoutParams(checkboxParams);

			LinearLayout.LayoutParams addReplyParams = (LinearLayout.LayoutParams)addReply.getLayoutParams();
			addReplyParams.width = targetButtonWidth;
			addReplyParams.height = targetButtonHeight;
			addReply.setLayoutParams(addReplyParams);
			
			LinearLayout.LayoutParams replyFieldParams = (LinearLayout.LayoutParams)replyField.getLayoutParams();
			replyFieldParams.height = targetButtonHeight;
			replyField.setLayoutParams(replyFieldParams);
			
			LinearLayout.LayoutParams recordButtonParams = (LinearLayout.LayoutParams) voice_rec_button.getLayoutParams();
			recordButtonParams.width = targetButtonWidth;
			recordButtonParams.height = targetButtonHeight;
			voice_rec_button.setLayoutParams(recordButtonParams);
			
			LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams((int) (13 * ConfigManager.scaleRatio),
					(int) (17 * ConfigManager.scaleRatio), 1);
			param3.gravity = Gravity.CENTER_VERTICAL;
			tooltipArrow.setLayoutParams(param3);

			ScaleUtil.adjustTextSize(addReply, ConfigManager.scaleRatio);
			if(ConfigManager.getInstance().needScaleInputPanel()){
				ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio * editTextRatio * 0.9);
			}else{
				ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio);
			}
			
			
			int length = (int) (ScaleUtil.dip2px(activity, 65) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_head_layout != null)
			{
				FrameLayout.LayoutParams custom_head_layoutLayoutParams = (FrameLayout.LayoutParams) custom_head_layout.getLayoutParams();
				custom_head_layoutLayoutParams.width = length;
				custom_head_layoutLayoutParams.height = length;
				custom_head_layout.setLayoutParams(custom_head_layoutLayoutParams);
			}
			
			int length2 = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (headImageContainer != null)
			{
				FrameLayout.LayoutParams headImageContainerLayoutParams = (FrameLayout.LayoutParams) headImageContainer.getLayoutParams();
				headImageContainerLayoutParams.width = length2;
				headImageContainerLayoutParams.height = length2;
				headImageContainer.setLayoutParams(headImageContainerLayoutParams);
			}
			
			int length3 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_channel_setting_layout != null)
			{
				FrameLayout.LayoutParams custom_channel_setting_layoutLayoutParams = (FrameLayout.LayoutParams) custom_channel_setting_layout.getLayoutParams();
				custom_channel_setting_layoutLayoutParams.height = length3;
				custom_channel_setting_layout.setLayoutParams(custom_channel_setting_layoutLayoutParams);
			}
			
			int length4 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (add_title != null)
			{
				LinearLayout.LayoutParams add_titleLayoutParams = (LinearLayout.LayoutParams) add_title.getLayoutParams();
				add_titleLayoutParams.height = length4;
				add_title.setLayoutParams(add_titleLayoutParams);
			}
			
			int length4_1 = (int) (ScaleUtil.dip2px(activity, 35) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if(custom_channel_settting_btn!=null)
			{
				RelativeLayout.LayoutParams custom_channel_settting_btn_Layout = (RelativeLayout.LayoutParams)custom_channel_settting_btn.getLayoutParams();
				custom_channel_settting_btn_Layout.width = length4_1;
				custom_channel_settting_btn_Layout.height = length4_1;
				if(ConfigManager.getInstance().needRTL())
				{
					custom_channel_settting_btn_Layout.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					custom_channel_settting_btn_Layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				custom_channel_settting_btn.setLayoutParams(custom_channel_settting_btn_Layout);
			}
				
			ScaleUtil.adjustTextSize(wordCount, ConfigManager.scaleRatio);

			ScaleUtil.adjustTextSize(noContentButton, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noContentTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tooltipLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_scroll_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(customChannelName, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_channel_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_setting_confim, ConfigManager.scaleRatio);

			adjustSizeCompleted = true;
			
			this.resetInputButton(true);

			if (lazyLoading)
			{
				// 以前只创建一次xml，这么调没问题
				// 创建两次之后，adjust需要多调，而renderList()调的位置发生了变化，导致时序有问题没有hideProgressBar
				// 这时候应该也不需要showProgressBar了，因为最重的listView不用重新创建
				// ProgressBar还有用吗？
				if(isFirstInflate)
					activity.showProgressBar();
				onBecomeVisible();
			}
		}
	}
	
	private void refreshCustomChatChannel()
	{
		Channel chatChannel = CustomChannelData.getInstance().getCustomChannel();
		
		refreshCustomChatChannel(chatChannel);
		
		if(chatChannel!=null && StringUtils.isNotEmpty(chatChannel.getDraft()))
		{
			replyField.setText(chatChannel.getDraft());
			replyField.setSelection(chatChannel.getDraft().length());
		}
		else
			replyField.setText("");
	}
	
	private void refreshCustomChatChannel(Channel channel)
	{
		if(activity == null || !CokChannelDef.isInChat())
			return;
		
		if(channelView == null || !isCustom())
			return;
		
		if(channel!=null)
		{
			if(!channel.hasInitLoaded())
				channel.loadMoreMsg();
			channel.clearFirstNewMsg();
			channel.setChannelView(channelView);
			int mailType = CokConfig.getInstance().isModChannel(channel) ? MailManager.MAIL_MOD_PERSONAL : MailManager.MAIL_USER;
			GSController.setMailInfo(channel.getChannelID(), channel.latestId, channel.getCustomName(), mailType);
		}
		else
		{
			GSController.setMailInfo("", "", "", -1);
		}
//		refreshMemberSelectBtn();
		channelView.channel = channel;
		
		List<Msg> msgList = null;
		
		if(channelView.channel!=null)
		{
			msgList = channelView.channel.msgList;
			channelView.setVisibility(View.VISIBLE);
		}
		else
		{
			channelView.setVisibility(View.GONE);
		}
		
		if(msgList == null)
			msgList = new ArrayList<Msg>();
		
		if (msgList != null)
		{
			MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
			channelView.setMessagesAdapter(adapter);
		}
		activity.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				if(channelView.messagesListView!=null)
					channelView.messagesListView.setAdapter(channelView.getMessagesAdapter());
				if (channelView.channel!=null && channelView.channel.lastPosition.x != -1 && rememberPosition)
				{
					channelView.messagesListView.setSelectionFromTop(channelView.channel.lastPosition.x, channelView.channel.lastPosition.y);
				}
				else
				{
					if(channelView.getMessagesAdapter()!=null)
						channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
			}
		});
		refreshCustomChannelName(channel);
	}

	public void showRedPackageConfirm(final Msg msgItem)
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					activity.showRedPackagePopup(msgItem);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void hideRedPackageConfirm()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					activity.hideRedPackagePopup();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public Msg getCurrentRedPackageItem()
	{
		if (activity != null)
		{
			return activity.getRedPackagePopItem();
		}
		return null;
	}

	protected void onContentAreaTouched()
	{
		hideSoftKeyBoard();
		replyField.clearFocus();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		CustomChannelData.getInstance().resetState();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		System.out.println("chatfragment new onDestroy");
		CustomChannelData.getInstance().resetState();
		if(CokChannelDef.canSendAudio(channelView.channelType))
			GSController.getInstance().setGameMusiceEnable(true);
		GSController.isContactMod = false;
		GSController.needShowAllianceDialog = false;
		if (tooltipLayout != null)
			tooltipLayout.setOnClickListener(null);
		if (noContentButton != null)
			noContentButton.setOnClickListener(null);

		try
		{
			getMemberSelectButton().setOnClickListener(null);
			if (getShowFriendButton() != null)
			{
				getShowFriendButton().setOnClickListener(null);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		if (addReply != null)
		{
			addReply.setOnClickListener(null);
			addReply = null;
		}

		if (replyField != null)
		{
			replyField.setOnEditorActionListener(null);
			replyField.removeTextChangedListener(textChangedListener);
			replyField = null;
		}
		textChangedListener = null;

		if (horn_checkbox != null)
		{
			horn_checkbox.setOnCheckedChangeListener(null);
			horn_checkbox = null;
		}

		if (messagesListFrameLayout != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			if (messagesListFrameLayout.getViewTreeObserver() != null)
			{
				messagesListFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
			}
			messagesListFrameLayout.removeAllViews();
			messagesListFrameLayout = null;
		}
		onGlobalLayoutListener = null;

		mOnScrollListener = null;
		mListViewLoadListener = null;

		noContentLayout = null;
		relativeLayout1 = null;
		imageView1 = null;
		imageView2 = null;
		wordCount = null;
		tooltipLayout = null;
		tooltipLabel = null;
		tooltipArrow = null;
		noContentButton = null;
		noContentTextView = null;

        if(mManager != null){
            mManager.unregisterListener(this);//注销传感器监听
            mManager = null;
        }
        
		((ChatActivity) getActivity()).fragment = null;

		super.onDestroy();
	}

	protected ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;
	private TextWatcher									textChangedListener;

	private SensorManager								mManager;				// 传感器管理对象

	private void initSensorListener()
	{
		mManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),// 距离感应器
				SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型

	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		float[] values = event.values;
		if (values != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY)
		{
			boolean oldValue = ConfigManager.playAudioBySpeaker;
			// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
			if (values[0] == 0.0)
			{
				// 贴近手机
				System.out.println("hands up");
				ConfigManager.playAudioBySpeaker = false;
			}
			else
			{
				// 远离手机
				System.out.println("hands moved");
				ConfigManager.playAudioBySpeaker = true;
			}
			if (oldValue != ConfigManager.playAudioBySpeaker)
			{
				XiaoMiToolManager.getInstance().refreshSpeakerphoneState();
			}
		}

	}
	
	protected static boolean	dataChanged	= false;

	@Override
	public void onResume()
	{
		System.out.println("onResume");
		super.onResume();
		if (CokChannelDef.isInChatRoom())
			getTitleLabel().setText(UserManager.getInstance().getCurrentMail().opponentName);
		else if(CokChannelDef.isInCustomChat())
			custom_channel_name.setText(UserManager.getInstance().getCurrentMail().opponentName);
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if(channel!=null && StringUtils.isNotEmpty(channel.getDraft()))
		{
			activity.showSoftKeyBoard(replyField);
			replyField.requestFocus();
		}
		if (dataChanged && getCurrentChannel() != null)
		{
			System.out.println("onResume chatfragment onMsgAdd");
			notifyDataSetChanged(true);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}
}
