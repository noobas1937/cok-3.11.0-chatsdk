package com.elex.chatservice.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
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

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChannelView;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.InputAtContent;
import com.elex.chatservice.model.InputDraft;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.FixedAspectRatioFrameLayout;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.SlidingUpPanelLayout.PanelSlideListener;
import com.elex.chatservice.view.SlidingUpPanelLayout.PanelState;
import com.elex.chatservice.view.actionbar.ActionBarFragment;
import com.elex.chatservice.view.adapter.CustomExpandableListAdapter;
import com.elex.chatservice.view.autoscroll.ScrollText;
import com.elex.chatservice.view.autoscroll.ScrollTextManager;
import com.elex.chatservice.view.emoj.EmojIcon;
import com.elex.chatservice.view.emoj.EmojPanel;
import com.elex.chatservice.view.emoj.EmojPanel.EmojMenuListener;
import com.elex.chatservice.view.listview.ListViewLoadListener;
import com.elex.chatservice.view.listview.PullDownToLoadMoreView;
import com.elex.chatservice.view.misc.messenger.AndroidUtilities;
import com.elex.chatservice.view.misc.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import com.elex.chatservice.view.misc.messenger.AnimationCompat.AnimatorSetProxy;
import com.elex.chatservice.view.misc.messenger.AnimationCompat.ObjectAnimatorProxy;
import com.elex.chatservice.view.misc.messenger.AnimationCompat.ViewProxy;
import com.elex.chatservice.view.misc.ui.Components.FrameLayoutFixed;
import com.elex.chatservice.view.misc.ui.Components.LayoutHelper;
import com.elex.chatservice.view.misc.ui.Components.SizeNotifierFrameLayout;

public class ChatFragmentNew extends ActionBarFragment implements SensorEventListener
{
	protected RelativeLayout						messagesListFrameLayout;
	private RelativeLayout							messagesListLayout;
	private FrameLayout								noAllianceFrameLayout;
	private LinearLayout							relativeLayout1;
	protected LinearLayout							buttonsLinearLayout;
	protected AutoCompleteTextView					replyField;
	// protected LinearLayout header;
	private MenuItem								attachScreenshotMenu;
	private TextView								wordCount;
	protected Button								addReply;
	private Button									buttonCountry;
	private Button									buttonAlliance;
	private Button									buttonCustom;
	private ArrayList<Button>						channelButton;
	private ImageView								imageView1;
	protected ImageView								imageView2;
	private Button									buttonJoinAlliance;
	private TextView								noAllianceTipText;
	private Timer									mTimer;
	private TimerTask								mTimerTask;
	private CheckBox								horn_checkbox;
	// private LinearLayout horn_tip_layout;
	private RelativeLayout							horn_scroll_layout;
	// private TextView horn_text_tip;
	private TextView								horn_name;
	private ScrollText								horn_scroll_text;
	private RelativeLayout							battle_horn_scroll_layout;
	// private TextView horn_text_tip;
	private TextView								battle_horn_name;
	private ScrollText								battle_horn_scroll_text;
	private ImageView								battle_horn_close_btn;
	private LinearLayout							tooltipLayout;
	private TextView								tooltipLabel;
	private ImageView								tooltipArrow;

	private SlidingUpPanelLayout					alliance_sliding_layout;
	private TextView								new_alliance_sys_message;
	private ImageView								new_alliance_sys_sliding_btn;
	private RelativeLayout							alliance_sys_top_layout;
	private TextView								alliance_name;
	private ImageView								alliance_sys_top_icon;
	private RelativeLayout							alliance_msg_layout;
	private PullDownToLoadMoreView					alliance_sys_list;
	private ListView								alliance_sys_msg_listview;
	private RelativeLayout							alliance_drag_layout;
	private TextView								alliance_null_sys_tip;

	private SlidingUpPanelLayout					country_sliding_layout;
	private TextView								new_country_sys_message;
	private ImageView								new_country_sys_sliding_btn;
	private ImageView								country_sys_top_icon;
	private RelativeLayout							country_msg_layout;
	private PullDownToLoadMoreView					country_sys_list;
	private ListView								country_sys_msg_listview;
	private RelativeLayout							country_drag_layout;
	private TextView								country_null_sys_tip;

	private ImageView								battle_horn_image;
	private ImageView								horn_image;
	private ImageView								horn_close_btn;
	private LinearLayout							hs__dragon_chat_tip_layout;
	private TextView								dragon_chat_tip_text;
	private RelativeLayout							custom_chat_tip_layout;
	private TextView								addCustomChatBtn;
	private TextView								custom_chat_tip_text;

	private RelativeLayout							random_chat_destory_tip_layout;
	private TextView								joinCustomChatBtn;
	private TextView								random_chat_destory_tip_text;

	private LinearLayout							random_chat_tip_layout;
	private TextView								addLocalRandomChatBtn;
	private TextView								local_random_chat_tip_text;
	private TextView								global_random_chat_tip_text;

	private RelativeLayout							random_room_join_tip_layout;
	private TextView								random_room_join_tip;
	private TextView								retry_btn;
	private FrameLayout								addLocalRandomChat_layout;
	private FixedAspectRatioFrameLayout				addLocalRandomChat_Container;
	private RoundImageView							addLocalRandomChat_langImage;
	private FrameLayout								addGlobalRandomChat_layout;
	private FixedAspectRatioFrameLayout				addGlobalRandomChat_Container;
	private RoundImageView							addGlobalRandomChat_langImage;

	private RelativeLayout							battle_field_tip_layout;
	private TextView								battle_field_btn;
	private TextView								battle_field_tip_text;
	private LinearLayout							custom_setting_layout;
	private TextView								add_title;
	private TextView								add_tip;
	private RoundImageView							customChannelHeadImage;
	private TextView								customChannelName;
	private ImageView								custom_mod_image;
	private FrameLayout								custom_head_layout;
	private FixedAspectRatioFrameLayout				headImageContainer;
	private ExpandableListView						custom_expand_listview;
	private CustomExpandableListAdapter				customChannelListAdapter;
	private RelativeLayout							custom_channel_setting_layout;
	private TextView								custom_channel_name;
	private ImageView								custom_channel_settting_btn;
	private RelativeLayout							country_exchange_layout;
	private TextView								country_channel_name;
	private ImageView								country_exchange_btn;
	private int										loadMoreCount				= 0;
	protected int									loadingHeaderHeight;
	protected boolean								isKeyBoardFirstShowed		= false;
	private int										curMaxInputLength			= 500;
	private LinearLayout							custom_settting_btn_layout;
	private TextView								custom_setting_confim;
	public static ChatChannel						showingCustomChannel		= null;
	private int										countryExchangeFlag			= 0;

	private CheckBox								emoj_checkbox;
	private EmojPanel								emoj_panel;

	private Timer									allianceSlideTimer			= null;
	private Timer									countrySlideTimer			= null;

	public static boolean							rememberPosition			= false;

	private FilteredArrayAdapter<UserInfo>			allianceAutoCompleteAdapter	= null;
	private FilteredArrayAdapter<UserInfo>			countryAutoCompleteAdapter	= null;
	private CopyOnWriteArrayList<InputAtContent>	allianceInputAtList			= new CopyOnWriteArrayList<InputAtContent>();
	private CopyOnWriteArrayList<InputAtContent>	countryInputAtList			= new CopyOnWriteArrayList<InputAtContent>();
	private static String							currentInputText			= "";
	private static int								currentCursorPos			= 0;
	private static int								beforeCursorPos				= 0;
	private static String							savedText					= "";
	private boolean									isJoinAlliancePopupShowing	= false;
	// public static String gmailAccount = "";

	public boolean									isKeyBoradShowing			= false;
	public boolean									isKeyBoradChange			= false;

	private boolean									isSelectMemberBtnEnable		= false;
	private List<ChannelView>						channelViews				= null;
	private int										customChannelType			= -1;
	private String									customChannelId;
	public boolean									isSettingCustomChannel		= false;
	private boolean									customChannelChange			= false;
	private List<ChatChannel>						friendList					= null;
	private List<ChatChannel>						chatroomChannelList			= null;

	public static String							currentAtText				= "";
	private AnimationDrawable						sys_new_tip_animation		= null;
	private OnClickListener							reJoinDragonObserver		= null;

	public boolean isSelectMemberBtnEnable()
	{
		return isSelectMemberBtnEnable;
	}

	public int getCustomChannelType()
	{
		return customChannelType;
	}

	public String getCustomChannelId()
	{
		return customChannelId;
	}

	public ChannelView getCurrentChannel()
	{
		return getCurrentChannelView();
	}

	public ChatFragmentNew()
	{
		isKeyBoardFirstShowed = false;
	}

	public boolean isInCountrySysView()
	{
		return getCurrentChannel() != null && getCurrentChannel().chatChannel != null && getCurrentChannel().chatChannel.isCountrySysChannel();
	}

	public void afterSendMsgShowed()
	{
		if (getCurrentChannel() != null && getCurrentChannel().chatChannel != null)
		{
			notifyDataSetChanged(ChatServiceController.getCurrentChannelType(), getCurrentChannel().chatChannel.channelID, true);
			scrollToLastLine();
		}
	}
	
	public void showSendMsgCode(final String errorCode)
	{
		if(activity!=null)
		{
			activity.runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					MenuController.showContentConfirm(LanguageManager.getLangByKey(errorCode));
				}
			});
		}
	}

	private void initSavedInputAtList()
	{
		if (countryInputAtList == null)
			countryInputAtList = new CopyOnWriteArrayList<InputAtContent>();
		else
			countryInputAtList.clear();

		if (allianceInputAtList == null)
			allianceInputAtList = new CopyOnWriteArrayList<InputAtContent>();
		else
			allianceInputAtList.clear();

		ChatChannel countryChannel = ChannelManager.getInstance().getCountryChannel();
		if (countryChannel != null && countryChannel.draftAt != null)
		{
			for (InputAtContent at : countryChannel.draftAt)
				countryInputAtList.add(at);
		}

		ChatChannel allianceChannel = ChannelManager.getInstance().getAllianceChannel();
		if (allianceChannel != null && allianceChannel.draftAt != null)
		{
			for (InputAtContent at : allianceChannel.draftAt)
				allianceInputAtList.add(at);
		}
	}

	public void refreshRealtimeBtnVisible()
	{
		String rtcUrl = WebRtcPeerManager.getInstance().getWebRtcUrl();
		getRealtimeRightBtn().setVisibility(
				!ChatServiceController.realtime_voice_enable || StringUtils.isEmpty(rtcUrl) || ChatServiceController.isInMailDialog() || UserManager.getInstance().getCurrentUser() == null ||
						!UserManager.getInstance().getCurrentUser().isInAlliance() ? View.GONE : View.VISIBLE);
	}

	public void refreshMemberSelectBtn()
	{
		boolean isAllAllianceMember = StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid)
				&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
				&& UserManager.getInstance().getCurrentUserId().equals(UserManager.getInstance().getCurrentMail().opponentUid);
		boolean isDriftingBottleChannel = StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid)
				&& UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE);
		if (!((ChatServiceController.isInMailDialog() && !isAllAllianceMember && !isDriftingBottleChannel)
				|| ChatServiceController.isCreateChatRoom || customChannelType != -1))
		{
			isSelectMemberBtnEnable = false;
			return;
		}

		try
		{
			if ((ChatServiceController.isInChatRoom() || (ChatServiceController.isInChat() && customChannelType == DBDefinition.CHANNEL_TYPE_CHATROOM))
					&& !ChannelManager.getInstance().getIsMemberFlag(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				isSelectMemberBtnEnable = false;
				return;
			}
			ArrayList<String> memberUidArray = UserManager.getInstance().getSelectMemberUidArr();
			if (memberUidArray == null)
			{
				isSelectMemberBtnEnable = false;
				return;
			}

			ConcurrentHashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			isSelectMemberBtnEnable = true;
			if (memberUidArray == null
					|| memberUidArray.size() <= 0
					|| (memberUidArray != null && memberUidArray.size() > 0 && !memberUidArray.contains(UserManager.getInstance()
							.getCurrentUserId())))
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
			getMemberSelectButton()
					.setVisibility(
							isSelectMemberBtnEnable
									&& (ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_COUNTRY
											&& ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && ChatServiceController
													.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_ALLIANCE) ? View.VISIBLE : View.GONE);
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
		this.buttonsLinearLayout.setVisibility(View.VISIBLE);
		refreshWordCount();

		if (this.attachScreenshotMenu != null)
		{
			this.attachScreenshotMenu.setVisible(true);
		}
	}

	public void saveState()
	{
		if (channelViews == null)
			return;
		for (int i = 0; i < channelViews.size(); i++)
		{
			ChannelView channelView = channelViews.get(i);
			if (channelView != null)
			{
				ChatChannel channel = channelView.chatChannel;
				if (channel != null && channelView.messagesListView != null)
				{
					channel.lastPosition.x = channelView.messagesListView.getFirstVisiblePosition();
					View v = channelView.messagesListView.getChildAt(0);
					channel.lastPosition.y = (v == null) ? 0 : (v.getTop() - channelView.messagesListView.getPaddingTop());
				}
			}
		}
	}

	protected boolean isJustCreated = true;

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
			for (int i = 0; i < channelViews.size(); i++)
			{
				ChannelView channelView = channelViews.get(i);
				if (channelView != null && channelView.chatChannel != null && channelView.chatChannel.lastPosition.x == -1
						&& channelView.messagesListView != null && channelView.getMessagesAdapter() != null)
				{
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
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
		initChannelViews();
		return inflater.inflate(ResUtil.getId(this, "layout", "cs__messages_fragment_new"), container, false);
	}

	private FrameLayout.LayoutParams getLayoutParams()
	{
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER;
		return param;
	}

	public void refreshIsInLastScreen(final int channelType, final String channelId)
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
						if (channel!=null && isSameChannel(channelType, channel.channelID))
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
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	private boolean isInLastScreen()
	{
		// messagesListView存在时messagesListView.getChildAt(0)也可能为0
		if (getCurrentChannel() == null || getCurrentChannel().getMessagesAdapter() == null
				|| getCurrentChannel().getMessagesAdapter().getCount() == 0 || getCurrentChannel().messagesListView == null)
		{
			return true;
		}
		// 遍历从view.getFirstVisiblePosition()可见高度及到最下方的各个item的高度，计算这高度和是否小于一定的值（1.6屏）
		View v = getCurrentChannel().messagesListView.getChildAt(0);
		if (v == null)
		{
			return true;
		}

		// 第一个item被上方盖住的部分
		int firstOffset = v.getTop() - getCurrentChannel().messagesListView.getPaddingTop();

		int totalHeight = v.getHeight() + firstOffset;
		if ((getCurrentChannel().getMessagesAdapter().getCount() - getCurrentChannel().messagesListView.getFirstVisiblePosition()) > 20)
		{
			return false;
		}

		for (int i = (getCurrentChannel().messagesListView.getFirstVisiblePosition() + 1); i < getCurrentChannel().getMessagesAdapter()
				.getCount(); i++)
		{
			View listItem = getCurrentChannel().getMessagesAdapter().getView(i, null, getCurrentChannel().messagesListView);
			if (listItem == null)
				continue;
			listItem.measure(MeasureSpec.makeMeasureSpec(getCurrentChannel().messagesListView.getWidth(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = listItem.getMeasuredHeight();
			totalHeight += h + getCurrentChannel().messagesListView.getDividerHeight();
		}

		if (totalHeight <= (getCurrentChannel().messagesListView.getHeight() * 1.75))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	boolean			inLastScreen	= false;
	private boolean	isAudioBtnDown	= false;

	public void updateListPositionForNewMsg(int channelType, String channelId, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType, channelId))
			return;

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
			gotoLastLine();
		inLastScreen = false;
	}

	public static boolean	hasNewSystemAllianceMsg	= false;
	public static boolean	hasNewSystemCountryMsg	= false;

	public void postNewAllianceSystemMsg()
	{
		if (ChatServiceController.allianceSysChannelEnable && getCurrentChannel() != null && getCurrentChannel().tab != TAB_ALLIANCE_SYS)
		{
			if (getCurrentChannel().tab == TAB_ALLIANCE)
			{
				hasNewSystemAllianceMsg = false;
				if (alliance_sliding_layout != null)
				{
					if (alliance_sliding_layout.getPanelState() != PanelState.COLLAPSED)
						alliance_sliding_layout.setPanelState(PanelState.COLLAPSED);
					else
					{
						if (allianceSlideTimer != null)
						{
							allianceSlideTimer.cancel();
							allianceSlideTimer.purge();
						}
						setSlideLayoutCollapsed();
					}
				}
			}
			else
				hasNewSystemAllianceMsg = true;
		}
	}

	public void postNewCountrySystemMsg()
	{
		if (ChatServiceController.countrySysChannelEnable && getCurrentChannel() != null && getCurrentChannel().tab != TAB_COUNTRY_SYS)
		{
			if (getCurrentChannel().tab == TAB_COUNTRY)
			{
				hasNewSystemCountryMsg = false;
				if (country_sliding_layout != null)
				{
					if (country_sliding_layout.getPanelState() != PanelState.COLLAPSED)
						country_sliding_layout.setPanelState(PanelState.COLLAPSED);
					else
					{
						if (countrySlideTimer != null)
						{
							countrySlideTimer.cancel();
							countrySlideTimer.purge();
						}
						setCountrySlideLayoutCollapsed();
					}
				}
			}
			else
				hasNewSystemCountryMsg = true;
		}
	}

	public void smoothUpdateListPositionForNewMsg(int channelType, String channelId, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType, channelId) || getCurrentChannel().messagesListView == null
				|| getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
		{
			scrollToLastLine();
		}
		inLastScreen = false;
	}

	public void updateListPositionForOldMsg(int channelType, String channelId, int loadCount, final boolean needMergeSendTime)
	{
		if (!isSameChannel(channelType, channelId) || getCurrentChannel().getMessagesAdapter() == null)
			return;
		System.out.println("updateListPositionForOldMsg 1");
		loadMoreCount = loadCount;
		ListView listView = getCurrentChannel().messagesListView;
		if (listView != null)
		{
			System.out.println("updateListPositionForOldMsg 3");
			if (getCurrentChannel().chatChannel != null && !getCurrentChannel().chatChannel.isLoadingAllNew && !getCurrentChannel().chatChannel.isLoadingEarliestAtMeMsg)
			{
				System.out.println("updateListPositionForOldMsg 4");
				int heightOffest = getCurrentChannel().pullDownToLoadListView.getPullDownHeight();
				if (needMergeSendTime)
				{
					System.out.println("updateListPositionForOldMsg 5");
					if (ChatServiceController.sendTimeTextHeight != 0)
						heightOffest += ChatServiceController.sendTimeTextHeight + ScaleUtil.dip2px(activity, 15);
					else
						heightOffest += ScaleUtil.dip2px(activity, 44);
				}
				listView.setSelectionFromTop(loadMoreCount, heightOffest);
			}
			else
			{
				System.out.println("updateListPositionForOldMsg 5");
				listView.setSelectionFromTop(0, 0);
			}
		}

		refreshToolTip();
		getCurrentChannel().pullDownToLoadListView.hideProgressBar();
		getCurrentChannel().stopTimerTask();
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

	public static void onMsgAdd(int channelType, String channelId, boolean needCalculateShowTimeIndex)
	{
		dataChanged = true;
		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, channelId, needCalculateShowTimeIndex);
			dataChanged = false;
		}
	}

	public static void setNewAllianceSystemMsg()
	{
		System.out.println("setNewAllianceSystemMsg");
		hasNewSystemAllianceMsg = true;
	}

	public static void setNewCountrySystemMsg()
	{
		System.out.println("setNewCountrySystemMsg");
		hasNewSystemAllianceMsg = true;
	}

	public void notifyDataSetChanged(final int channelType, final String channelId, final boolean needCalculateShowTimeIndex)
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
					if (!isSameChannel(channelType, channelId))
						return;
					ChannelView curChannelView = getCurrentChannelView();
					if (curChannelView != null)
					{
						MessagesAdapter adapter = curChannelView.getMessagesAdapter();
						if (adapter != null)
						{
							if (needCalculateShowTimeIndex)
							{
								ChatChannel channel = ChannelManager.getInstance().getChannel(curChannelView.channelType);
								if (channel != null)
									channel.getTimeNeedShowMsgIndex();
							}

							adapter.notifyDataSetChanged();
						}
						refreshHasMoreData(curChannelView);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void resetMoreDataStart(int channelType, String channelId)
	{
		if (StringUtils.isEmpty(channelId))
		{
			if (getCurrentChannel() != null && getCurrentChannel().chatChannel != null)
				channelId = getCurrentChannel().chatChannel.channelID;
		}
		if (isSameChannel(channelType, channelId))
		{
			getCurrentChannel().setLoadingStart(false);
		}
	}

	protected void gotoLastLine()
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
					if (getCurrentChannel() != null && getCurrentChannel().messagesListView != null
							&& getCurrentChannel().getMessagesAdapter() != null)
					{
						getCurrentChannel().messagesListView.setSelection(getCurrentChannel().getMessagesAdapter().getCount() - 1);
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
					if (getCurrentChannel() != null && getCurrentChannel().messagesListView != null
							&& getCurrentChannel().getMessagesAdapter() != null)
					{
						getCurrentChannel().messagesListView
								.smoothScrollToPosition(getCurrentChannel().getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	protected boolean isSameChannel(int channelType, String channelId)
	{
		if (getCurrentChannel() == null)
			return false;
		if (getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
				|| getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			return getCurrentChannel().channelType == channelType;
		else
		{
			if (getCurrentChannel().chatChannel != null)
			{
				return (getCurrentChannel().channelType == channelType || getCurrentChannel().chatChannel.channelType == channelType)
						&& StringUtils.isNotEmpty(channelId) && StringUtils.isNotEmpty(getCurrentChannel().chatChannel.channelID)
						&& getCurrentChannel().chatChannel.channelID.equals(channelId);
			}

			else
				return false;
		}
	}

	private final int	COLOR_RECORD_BACK			= 0xff1f2020;	// 0xffffffff;
	private final int	COLOR_RECORD_DOT_BACK		= 0xff1f2020;	// 0xffffffff;
	private final int	COLOR_RECORD_CIRCLE_BACK	= 0xff407448;	// 0xff5795cc;
	private final int	COLOR_RECORD_CIRCLE_PAINT	= 0x0d000000;
	private final int	COLOR_RECORD_SLIDE_TEXT		= 0xffa69279;	// 0xff999999;
	private final int	COLOR_RECORD_TIME_TEXT		= 0xffa69279;	// 0xff4d4c4b;

	private class RecordCircle extends View
	{

		private Paint		paint		= new Paint(Paint.ANTI_ALIAS_FLAG);
		private Paint		paintRecord	= new Paint(Paint.ANTI_ALIAS_FLAG);
		private Drawable	micDrawable;
		private float		scale;
		private float		amplitude;
		private float		animateToAmplitude;
		private float		animateAmplitudeDiff;
		private long		lastUpdateTime;

		public RecordCircle(Context context)
		{
			super(context);
			paint.setColor(COLOR_RECORD_CIRCLE_BACK);
			paintRecord.setColor(COLOR_RECORD_CIRCLE_PAINT);
			micDrawable = getResources().getDrawable(R.drawable.voice_mic_pressed);
		}

		public void setAmplitude(double value)
		{
			animateToAmplitude = (float) Math.min(100, value) / 100.0f;
			animateAmplitudeDiff = (animateToAmplitude - amplitude) / 150.0f;
			lastUpdateTime = System.currentTimeMillis();
			invalidate();
		}

		public float getScale()
		{
			return scale;
		}

		public void setScale(float value)
		{
			scale = value;
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			int cx = getMeasuredWidth() / 2;
			int cy = getMeasuredHeight() / 2;
			float sc;
			float alpha;
			if (scale <= 0.5f)
			{
				alpha = sc = scale / 0.5f;
			}
			else if (scale <= 0.75f)
			{
				sc = 1.0f - (scale - 0.5f) / 0.25f * 0.1f;
				alpha = 1;
			}
			else
			{
				sc = 0.9f + (scale - 0.75f) / 0.25f * 0.1f;
				alpha = 1;
			}
			long dt = System.currentTimeMillis() - lastUpdateTime;
			if (animateToAmplitude != amplitude)
			{
				amplitude += animateAmplitudeDiff * dt;
				if (animateAmplitudeDiff > 0)
				{
					if (amplitude > animateToAmplitude)
					{
						amplitude = animateToAmplitude;
					}
				}
				else
				{
					if (amplitude < animateToAmplitude)
					{
						amplitude = animateToAmplitude;
					}
				}
				invalidate();
			}
			lastUpdateTime = System.currentTimeMillis();
			if (amplitude != 0)
			{
				canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, (AndroidUtilities.dp(42)
						* (float) getAudioUIScale() + AndroidUtilities.dp(20) * (float) getAudioUIScale() * amplitude)
						* scale, paintRecord);
			}
			canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, AndroidUtilities.dp(42) * (float) getAudioUIScale()
					* sc, paint);
			int micWidth = (int) (micDrawable.getIntrinsicWidth() * getAudioUIScale());
			int micHeight = (int) (micDrawable.getIntrinsicHeight() * getAudioUIScale());
			micDrawable.setBounds(cx - micWidth / 2, cy - micHeight / 2, cx + micWidth / 2, cy + micHeight / 2);
			micDrawable.setAlpha((int) (255 * alpha));
			micDrawable.draw(canvas);
		}
	}

	private class RecordDot extends View
	{

		private Drawable	dotDrawable;
		private float		alpha;
		private long		lastUpdateTime;
		private boolean		isIncr;

		public RecordDot(Context context)
		{
			super(context);

			dotDrawable = getResources().getDrawable(R.drawable.voice_rec);
		}

		public void resetAlpha()
		{
			alpha = 1.0f;
			lastUpdateTime = System.currentTimeMillis();
			isIncr = false;
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			dotDrawable.setBounds(0, 0, AndroidUtilities.dp(11), AndroidUtilities.dp(11));
			dotDrawable.setAlpha(185 + (int) (70 * alpha));
			long dt = (System.currentTimeMillis() - lastUpdateTime);
			if (!isIncr)
			{
				alpha -= dt / 200.0f;
				if (alpha <= 0)
				{
					alpha = 0;
					isIncr = true;
				}
			}
			else
			{
				alpha += dt / 200.0f;
				if (alpha >= 1)
				{
					alpha = 1;
					isIncr = false;
				}
			}
			lastUpdateTime = System.currentTimeMillis();
			dotDrawable.draw(canvas);
			invalidate();
		}
	}

	private String					lastTimeString;
	private float					startedDraggingX	= -1;
	private boolean					recordingAudio		= false;
	private boolean					recordBtnUp			= true;
	private float					distCanMove			= AndroidUtilities.dp(80);
	private int						audioInterfaceState;
	private PowerManager.WakeLock	mWakeLock;

	private void updateAudioRecordIntefrace()
	{
		if (recordingAudio)
		{
			if (audioInterfaceState == 1)
			{
				return;
			}
			audioInterfaceState = 1;
			try
			{
				if (mWakeLock == null)
				{
					PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
					mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "audio record lock");
					mWakeLock.acquire();
				}
			}
			catch (Exception e)
			{
				// FileLog.e("tmessages", e);
			}
			AndroidUtilities.lockOrientation(activity);

			onRecordPanelShown(true);
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
			if (runningAnimationAudio != null)
			{
				runningAnimationAudio.cancel();
			}
			runningAnimationAudio = new AnimatorSetProxy();
			runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", 0),
					ObjectAnimatorProxy.ofFloat(recordCircle, "scale", (float) getAudioUIScale()),
					ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 0));
			runningAnimationAudio.setDuration(300);
			runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy()
			{
				@Override
				public void onAnimationEnd(Object animator)
				{
					if (runningAnimationAudio != null && runningAnimationAudio.equals(animator))
					{
						ViewProxy.setX(recordPanel, 0);
						runningAnimationAudio = null;
					}
				}
			});
			runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
			runningAnimationAudio.start();
		}
		else
		{
			if (mWakeLock != null)
			{
				try
				{
					mWakeLock.release();
					mWakeLock = null;
				}
				catch (Exception e)
				{
					// FileLog.e("tmessages", e);
				}
			}
			AndroidUtilities.unlockOrientation(activity);
			if (audioInterfaceState == 0)
			{
				return;
			}
			audioInterfaceState = 0;

			if (runningAnimationAudio != null)
			{
				runningAnimationAudio.cancel();
			}
			runningAnimationAudio = new AnimatorSetProxy();
			runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", AndroidUtilities.displaySize.x),
					ObjectAnimatorProxy.ofFloat(recordCircle, "scale", 0.0f),
					ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 1.0f));
			runningAnimationAudio.setDuration(300);
			runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy()
			{
				@Override
				public void onAnimationEnd(Object animator)
				{
					if (runningAnimationAudio != null && runningAnimationAudio.equals(animator))
					{
						FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
						params.leftMargin = AndroidUtilities.dp(30);
						slideLayout.setLayoutParams(params);
						ViewProxy.setAlpha(slideLayout, 1);
						onRecordPanelShown(false);
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

	private Timer		recordTimer;
	private TimerTask	recordTimerTask;
	private long		recordStartTime;

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
				if (lastTimeString == null || !lastTimeString.equals(str))
				{
					lastTimeString = str;
					if (time % 5 == 0)
					{
						// MessagesController.getInstance().sendTyping(dialog_id,
						// 1, 0);
					}
					if (recordTimeText != null && activity != null)
					{
						activity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									recordTimeText.setText(str);

									// if (recordCircle != null) {
									// recordCircle.setAmplitude((Double)
									// 100.0);
									// }
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
		if (recordTimer != null)
		{
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

	public void didReceivedNotification(String id, Object... args)
	{
		if (id.equals("NotificationCenter.recordProgressChanged"))
		{
			Long time = (Long) args[0] / 1000;
			String str = String.format("%02d:%02d", time / 60, time % 60);
			if (lastTimeString == null || !lastTimeString.equals(str))
			{
				if (time % 5 == 0)
				{
					// MessagesController.getInstance().sendTyping(dialog_id, 1,
					// 0);
				}
				if (recordTimeText != null)
				{
					recordTimeText.setText(str);
				}
			}
			if (recordCircle != null)
			{
				recordCircle.setAmplitude((Double) args[1]);
			}
		}
		else if (id.equals("NotificationCenter.recordStartError") || id.equals("NotificationCenter.recordStopped"))
		{
			if (recordingAudio)
			{
				exitRecordingUI();
			}
		}
		else if (id.equals("NotificationCenter.recordStarted"))
		{
			if (!recordingAudio)
			{
				recordingAudio = true;
				updateAudioRecordIntefrace();
			}
		}
		else if (id.equals("NotificationCenter.audioDidSent"))
		{
			// if (delegate != null) {
			// delegate.onMessageSend(null);
			// }
		}
	}

	public void exitRecordingUI()
	{
		recordingAudio = false;
		updateAudioRecordIntefrace();
	}

	public void refreshCustomChannelImage(ChatChannel channel)
	{
		showingCustomChannel = channel;
		if (activity != null && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT
				&& isSettingCustomChannel)
		{
			ImageUtil.setChannelImage(activity, channel, customChannelHeadImage);
			refreshSettingCustomChannelName(channel);
		}
		if (showingCustomChannel != null)
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP2));
		else
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP));
	}

	private synchronized void prepareCustomChannelData()
	{
		if (activity != null && ChatServiceController.isInChat())
		{
			SparseArray<List<ChatChannel>> channelMap = new SparseArray<List<ChatChannel>>();
			friendList = new ArrayList<ChatChannel>();
			chatroomChannelList = new ArrayList<ChatChannel>();
			List<String> friendChannelIdList = new ArrayList<String>();
			List<String> chatRoomChannelIdList = new ArrayList<String>();

			List<ChatChannel> msgChannelList = ChannelManager.getInstance().getAllMessageChannel();
			List<ChatChannel> modChannelList = ChannelManager.getInstance().getAllModChannel();

			boolean customChannelExist = false;
			if (customChannelType == -1 || StringUtils.isEmpty(customChannelId)
					|| customChannelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				customChannelExist = true;

			if (modChannelList != null)
			{
				Iterator<ChatChannel> it = modChannelList.iterator();
				while (it.hasNext())
				{
					ChatChannel channel = it.next();
					if (channel != null)
					{
						if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && !chatRoomChannelIdList.contains(channel.channelID))
						{
							chatRoomChannelIdList.add(channel.channelID);
							chatroomChannelList.add(channel);
							if (!customChannelExist && customChannelType == channel.channelType
									&& customChannelId.equals(channel.channelID))
								customChannelExist = true;
						}
						else if ((channel.isMessageChannel() || channel.isModChannel())
								&& !friendChannelIdList.contains(channel.channelID))
						{
							friendChannelIdList.add(channel.channelID);
							friendList.add(channel);
							if (!customChannelExist && customChannelType == channel.channelType
									&& customChannelId.equals(channel.channelID))
								customChannelExist = true;
						}
					}
				}
			}

			if (msgChannelList != null)
			{
				Iterator<ChatChannel> it = msgChannelList.iterator();
				while (it.hasNext())
				{
					ChatChannel channel = it.next();
					if (channel != null)
					{
						if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && !chatRoomChannelIdList.contains(channel.channelID))
						{
							chatRoomChannelIdList.add(channel.channelID);
							chatroomChannelList.add(channel);
							if (!customChannelExist && customChannelType == channel.channelType
									&& customChannelId.equals(channel.channelID))
								customChannelExist = true;
						}
						else if ((channel.isMessageChannel() || channel.isModChannel())
								&& !friendChannelIdList.contains(channel.channelID))
						{
							friendChannelIdList.add(channel.channelID);
							friendList.add(channel);
							if (!customChannelExist && customChannelType == channel.channelType
									&& customChannelId.equals(channel.channelID))
								customChannelExist = true;
						}
					}
				}
			}

			if (!customChannelExist)
			{
				customChannelType = -1;
				customChannelId = "";

				LocalConfig config = ConfigManager.getInstance().getLocalConfig();
				if (config == null)
					config = new LocalConfig();
				config.setCustomChannelType(-1);
				config.setCustomChannelId("");
				ConfigManager.getInstance().setLocalConfig(config);
			}

			Collections.sort(friendList);
			Collections.sort(chatroomChannelList);
			channelMap.put(0, friendList);
			channelMap.put(1, chatroomChannelList);
			customChannelListAdapter = new CustomExpandableListAdapter(activity, channelMap);
			custom_expand_listview.setAdapter(customChannelListAdapter);
			custom_expand_listview.expandGroup(0);
			custom_expand_listview.expandGroup(1);
			if (ChatServiceController.randomChatEnable)
				custom_expand_listview.expandGroup(2);
		}
	}

	private void onRecordPanelShown(boolean b)
	{
		if (isHornUI)
		{
			// horn_tip_layout.setVisibility(b ? View.GONE : View.VISIBLE);
		}
		else
		{
			// imageView1.setVisibility(b ? View.GONE : View.VISIBLE);
		}
	}

	private LinearLayout			sendMessageLayout;
	private LinearLayout			voice_rec_button_layout;
	private Button					voice_rec_button;

	private RecordCircle			recordCircle;
	private RecordDot				recordDot;
	private LinearLayout			slideLayout;
	private TextView				recordTimeText;
	private FrameLayout				recordPanel;
	private FrameLayout				inputFrameLayout;
	private FrameLayout				popFrameLayout;
	private SizeNotifierFrameLayout	sizeNotifierFrameLayout;
	private boolean					hornTextHidden			= false;
	private boolean					battleHornTextHidden	= false;

	private void setSlideLayoutCollapsed()
	{
		ChatChannel channel = ChannelManager.getInstance().getAllianceSysChannel();
		MsgItem item = null;
		if (channel != null)
		{
			if (channel.msgList != null && channel.msgList.size() > 0)
				item = channel.msgList.get(channel.msgList.size() - 1);
		}
		if (item != null)
		{
			new_alliance_sys_message.setVisibility(View.VISIBLE);
			ChatServiceController.getInstance().setText(activity, new_alliance_sys_message, item.msg, item);
		}
		else
		{
			new_alliance_sys_message.setVisibility(View.GONE);
		}

		allianceSlideTimer = new Timer();
		TimerTask task = new TimerTask()
		{

			@Override
			public void run()
			{
				if (activity != null)
				{
					activity.runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							if (alliance_sliding_layout != null && alliance_sliding_layout.getPanelState() != PanelState.HIDDEN)
							{
								alliance_sliding_layout.setPanelState(PanelState.HIDDEN);
							}
						}
					});
				}
				allianceSlideTimer.cancel();
				allianceSlideTimer.purge();
			}
		};
		allianceSlideTimer.schedule(task, 5000);
	}

	public void setAllianceSlideLayoutExpandState()
	{
		new_alliance_sys_sliding_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.sliding_up));
		new_alliance_sys_message.setVisibility(View.GONE);
		setChannelViewIndex(TAB_ALLIANCE_SYS - 1);
		if (allianceSlideTimer != null)
		{
			allianceSlideTimer.cancel();
			allianceSlideTimer.purge();
		}
		updateAllianceSlideDragLayoutHeight(PanelState.COLLAPSED, PanelState.EXPANDED, false);
	}

	private void setCountrySlideLayoutCollapsed()
	{
		ChatChannel channel = ChannelManager.getInstance().getCountrySysChannel();
		MsgItem item = null;
		if (channel != null)
		{
			if (channel.msgList != null && channel.msgList.size() > 0)
				item = channel.msgList.get(channel.msgList.size() - 1);
		}
		if (item != null)
		{
			new_country_sys_message.setVisibility(View.VISIBLE);
			ChatServiceController.getInstance().setText(activity, new_country_sys_message, item.msg, item);
		}
		else
		{
			new_country_sys_message.setVisibility(View.GONE);
		}

		countrySlideTimer = new Timer();
		TimerTask task = new TimerTask()
		{

			@Override
			public void run()
			{
				if (activity != null)
				{
					activity.runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							if (country_sliding_layout != null && country_sliding_layout.getPanelState() != PanelState.HIDDEN)
							{
								country_sliding_layout.setPanelState(PanelState.HIDDEN);
							}
						}
					});
				}
				countrySlideTimer.cancel();
				countrySlideTimer.purge();
			}
		};
		countrySlideTimer.schedule(task, 5000);
	}

	public void hideEmojPanel()
	{
		if (emoj_panel != null && emoj_panel.getVisibility() != View.GONE)
			emoj_panel.setVisibility(View.GONE);
		if (emoj_checkbox != null && emoj_checkbox.isChecked())
			emoj_checkbox.setChecked(false);
	}

	public void hideEmojBtn()
	{
		emoj_checkbox.setVisibility(View.GONE);
		if (emoj_panel != null && emoj_panel.getVisibility() != View.GONE)
			emoj_panel.setVisibility(View.GONE);
		if (emoj_checkbox != null && emoj_checkbox.isChecked())
			emoj_checkbox.setChecked(false);
	}

	public void setCountrySlideLayoutExpandState()
	{
		new_country_sys_sliding_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.sliding_up));
		new_country_sys_message.setVisibility(View.GONE);
		setChannelViewIndex(TAB_COUNTRY_SYS - 1);
		if (countrySlideTimer != null)
		{
			countrySlideTimer.cancel();
			countrySlideTimer.purge();
		}
		updateCountrySlideDragLayoutHeight(PanelState.COLLAPSED, PanelState.EXPANDED, false);
	}

	private void refreshAtListHighLight(String actualText)
	{
		if (!currentAtText.equals(actualText))
		{
			currentAtText = actualText;
			System.out.println("currentAtText:" + currentAtText);
			if (activity != null)
			{
				activity.runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						if (ChatServiceController.isInAllianceOrSysAllianceChannel() && allianceAutoCompleteAdapter != null)
							allianceAutoCompleteAdapter.notifyDataSetChanged();
						else if (ChatServiceController.isInCountryOrSysCountryChannel() && countryAutoCompleteAdapter != null)
							countryAutoCompleteAdapter.notifyDataSetChanged();
					}
				});
			}
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		System.out.println("chatfragmentnew onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		if (!ChatServiceController.isInChat())
			refreshMemberSelectBtn();
		this.noAllianceFrameLayout = (FrameLayout) view.findViewById(R.id.hs__noAllianceLayout);
		this.relativeLayout1 = (LinearLayout) view.findViewById(R.id.relativeLayout1);
		this.buttonsLinearLayout = (LinearLayout) view.findViewById(R.id.buttonsLinearLayout);
		this.messagesListFrameLayout = (RelativeLayout) view.findViewById(R.id.hs__messagesListLayout);
		messagesListLayout = (RelativeLayout) view.findViewById(R.id.messagesListLayout);
		ImageUtil.setYRepeatingBG(activity, messagesListLayout, R.drawable.mail_list_bg);

		emoj_checkbox = (CheckBox) view.findViewById(R.id.emoj_checkbox);
		if (ChatServiceController.canUserEmojPanel())
			emoj_checkbox.setVisibility(View.VISIBLE);
		else
			emoj_checkbox.setVisibility(View.GONE);
		emoj_panel = (EmojPanel) view.findViewById(R.id.emoj_panel);
		emoj_panel.setVisibility(View.GONE);

		emoj_panel.initEmoj(activity);

		emoj_panel.setEmojMenuListener(new EmojMenuListener()
		{

			@Override
			public void onExpressionClicked(EmojIcon emojicon)
			{
				if (emojicon.getType() != EmojIcon.Type.BIG_EMOJ)
				{
					ChatServiceController.sendMsg(emojicon.getEmojId(), MsgItem.MSG_TYPE_EMOJ);
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "getEmojiText", emojicon.getEmojiText(), "getIcon", emojicon.getIcon());
				}
				else
				{
					ChatServiceController.sendMsg(emojicon.getEmojId(), MsgItem.MSG_TYPE_EMOJ);
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "getIdentityCode", emojicon.getId(), "name", emojicon.getName());
				}
			}

			@Override
			public void onDeleteImageClicked()
			{
				// TODO Auto-generated method stub

			}
		});

		emoj_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					activity.hideSoftKeyBoard();
					emoj_panel.setVisibility(View.VISIBLE);
				}
				else
					emoj_panel.setVisibility(View.GONE);
			}
		});

		imageView1 = (ImageView) view.findViewById(R.id.imageView1);
		imageView2 = (ImageView) view.findViewById(R.id.imageView2);
		horn_checkbox = (CheckBox) view.findViewById(R.id.horn_checkbox);
		// horn_tip_layout = (LinearLayout)
		// view.findViewById(R.id.horn_tip_layout);
		// horn_text_tip = (TextView) view.findViewById(R.id.horn_text_tip);
		// horn_text_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_HORN_TEXT));
		horn_scroll_text = (ScrollText) view.findViewById(R.id.horn_scroll_text);
		horn_scroll_text.setChannelType(DBDefinition.CHANNEL_TYPE_COUNTRY);
		horn_name = (TextView) view.findViewById(R.id.horn_name);
		horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.horn_scroll_layout);
		horn_scroll_layout.setVisibility(View.GONE);
		horn_close_btn = (ImageView) view.findViewById(R.id.horn_close_btn);
		battle_horn_image = (ImageView) view.findViewById(R.id.battle_horn_image);
		horn_image = (ImageView) view.findViewById(R.id.horn_image);

		battle_horn_scroll_text = (ScrollText) view.findViewById(R.id.battle_horn_scroll_text);
		battle_horn_scroll_text.setChannelType(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
		battle_horn_name = (TextView) view.findViewById(R.id.battle_horn_name);
		battle_horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.battle_horn_scroll_layout);
		battle_horn_scroll_layout.setVisibility(View.GONE);
		battle_horn_close_btn = (ImageView) view.findViewById(R.id.battle_horn_close_btn);

		hs__dragon_chat_tip_layout = (LinearLayout) view.findViewById(R.id.hs__dragon_chat_tip_layout);
		hs__dragon_chat_tip_layout.setVisibility(View.GONE);
		dragon_chat_tip_text = (TextView) view.findViewById(R.id.dragon_chat_tip_text);
		dragon_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_DRAGON_CHAT));

		custom_chat_tip_layout = (RelativeLayout) view.findViewById(R.id.custom_chat_tip_layout);
		addCustomChatBtn = (TextView) view.findViewById(R.id.addCustomChatBtn);
		custom_chat_tip_text = (TextView) view.findViewById(R.id.custom_chat_tip_text);
		custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP));

		random_chat_destory_tip_layout = (RelativeLayout) view.findViewById(R.id.random_chat_destory_tip_layout);
		joinCustomChatBtn = (TextView) view.findViewById(R.id.joinCustomChatBtn);
		random_chat_destory_tip_text = (TextView) view.findViewById(R.id.random_chat_destory_tip_text);
		random_chat_destory_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RANDOM_DESTORY));

		joinCustomChatBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_GO_TO_SEE));
		joinCustomChatBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
				LogUtil.trackAction("click_add_custom_btn");
			}
		});

		random_chat_tip_layout = (LinearLayout) view.findViewById(R.id.random_chat_tip_layout);
		addLocalRandomChatBtn = (TextView) view.findViewById(R.id.addLocalRandomChatBtn);
		local_random_chat_tip_text = (TextView) view.findViewById(R.id.local_random_chat_tip_text);

		random_room_join_tip_layout = (RelativeLayout) view.findViewById(R.id.random_room_join_tip_layout);
		random_room_join_tip = (TextView) view.findViewById(R.id.random_room_join_tip);
		random_room_join_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RANDOM_ROOM_JOINING));
		retry_btn = (TextView) view.findViewById(R.id.retry_btn);
		// retry_btn.setText(LanguageManager.getLangByKey(LanguageKeys));

		retry_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
			}
		});

		addLocalRandomChat_layout = (FrameLayout) view.findViewById(R.id.addLocalRandomChat_layout);
		addLocalRandomChat_Container = (FixedAspectRatioFrameLayout) view.findViewById(R.id.addLocalRandomChat_Container);
		addLocalRandomChat_langImage = (RoundImageView) view.findViewById(R.id.addLocalRandomChat_langImage);
		GradientDrawable bgShape = (GradientDrawable) addLocalRandomChat_langImage.getBackground();
		if (bgShape != null)
			bgShape.setColor(0xDD000000);

		String lang = ConfigManager.getInstance().gameLang;
		if (StringUtils.isEmpty(lang))
			lang = "en";
		String langImage = LanguageManager.getLangImage(lang);
		int resId = ImageUtil.getHeadResId(activity, langImage);
		Drawable drawable = null;
		if (resId > 0)
			drawable = activity.getResources().getDrawable(resId);
		if (drawable != null)
		{
			addLocalRandomChat_layout.setVisibility(View.VISIBLE);
			addLocalRandomChat_langImage.setImageDrawable(drawable);
		}
		else
		{
			addLocalRandomChat_layout.setVisibility(View.GONE);
		}

		addLocalRandomChat_langImage.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
				else
					MqttManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
				LogUtil.trackAction("enter_local_random_chat");
			}
		});

		addGlobalRandomChat_layout = (FrameLayout) view.findViewById(R.id.addGlobalRandomChat_layout);
		addGlobalRandomChat_Container = (FixedAspectRatioFrameLayout) view.findViewById(R.id.addGlobalRandomChat_Container);
		addGlobalRandomChat_langImage = (RoundImageView) view.findViewById(R.id.addGlobalRandomChat_langImage);
		GradientDrawable bgShape2 = (GradientDrawable) addGlobalRandomChat_langImage.getBackground();
		if (bgShape2 != null)
			bgShape2.setColor(0xDD000000);

		addGlobalRandomChat_langImage.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().getRandomChatRoomId("");
				else
					MqttManager.getInstance().getRandomChatRoomId("");
				LogUtil.trackAction("enter_global_random_chat");
			}
		});

		global_random_chat_tip_text = (TextView) view.findViewById(R.id.global_random_chat_tip_text);

		battle_field_tip_layout = (RelativeLayout) view.findViewById(R.id.battle_field_tip_layout);
		battle_field_btn = (TextView) view.findViewById(R.id.battle_field_btn);
		battle_field_tip_text = (TextView) view.findViewById(R.id.battle_field_tip_text);
		if (ChatServiceController.getInstance().needShowBattleTipLayout())
		{
			battle_field_btn.setVisibility(View.VISIBLE);
			battle_field_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_GO_TO_SEE));
			battle_field_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_BATTLE_FIELD_ADD_TIP));
			battle_field_btn.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					LogUtil.trackAction("click_to_battle_from_chat");
					ChatServiceController.doHostAction("showBattleActivity", "", "", "", false);
				}
			});
		}
		else if (ChatServiceController.getInstance().needShowDragonObserverTipLayout())
		{
			refreshDragonJoinStatus();
		}

		addCustomChatBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD));

		addCustomChatBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
				LogUtil.trackAction("click_add_custom_btn");
			}
		});

		String originalLang = LanguageManager.getOriginalLangByKey(lang);
		addLocalRandomChatBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SET_LOCAL_CHATROOM, originalLang));
		addLocalRandomChatBtn.setVisibility(addLocalRandomChat_layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
		local_random_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SET_LOCAL_CHATROOM, originalLang));

		addLocalRandomChatBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// showCustomChannelSetting();
				if (SwitchUtils.mqttEnable)
					MqttManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
				else
					WebSocketManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
				LogUtil.trackAction("enter_local_random_chat");
			}
		});

		global_random_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SET_GLOBAL_CHATROOM));

		custom_setting_layout = (LinearLayout) view.findViewById(R.id.custom_setting_layout);
		add_title = (TextView) view.findViewById(R.id.add_title);
		add_title.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD));
		add_tip = (TextView) view.findViewById(R.id.add_tip);
		if (StringUtils.isNotEmpty(customChannelId))
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
				if (customChannelListAdapter != null)
					customChannelListAdapter.notifyDataSetChanged();
			}
		});

		custom_setting_layout.setVisibility(View.GONE);
		customChannelName = (TextView) view.findViewById(R.id.name);
		custom_mod_image = (ImageView) view.findViewById(R.id.custom_mod_image);

		custom_head_layout = (FrameLayout) view.findViewById(R.id.member_head_layout);
		custom_expand_listview = (ExpandableListView) view.findViewById(R.id.custom_expand_listview);
		custom_expand_listview.setGroupIndicator(null);

		headImageContainer = (FixedAspectRatioFrameLayout) view.findViewById(R.id.headImageContainer);

		inputFrameLayout = (FrameLayout) view.findViewById(R.id.inputFrameLayout);
		recordPanel = new FrameLayoutFixed(activity);
		recordPanel.setVisibility(View.GONE);
		recordPanel.setBackgroundColor(COLOR_RECORD_BACK);
		inputFrameLayout.addView(recordPanel,
				LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM));

		slideLayout = new LinearLayout(activity);
		slideLayout.setOrientation(LinearLayout.HORIZONTAL);
		recordPanel.addView(slideLayout,
				LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 30, 0, 0, 0));

		ImageView slideArrowImageView = new ImageView(activity);
		slideArrowImageView.setImageResource(R.drawable.voice_slidearrow);
		slideLayout.addView(slideArrowImageView,
				LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

		TextView slideTextView = new TextView(activity);
		slideTextView.setText(LanguageManager.getLangByKey(LanguageKeys.AUDIO_SLIDE_TO_CANCEL));
		slideTextView.setTextColor(COLOR_RECORD_SLIDE_TEXT);
		slideTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		ScaleUtil.adjustTextSize(slideTextView, getAudioUIScale());
		slideLayout.addView(slideTextView,
				LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

		LinearLayout linearLayout = new LinearLayout(activity);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setPadding(AndroidUtilities.dp(13), 0, 0, 0);
		linearLayout.setBackgroundColor(COLOR_RECORD_DOT_BACK);
		recordPanel.addView(linearLayout,
				LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

		recordDot = new RecordDot(activity);
		linearLayout.addView(recordDot, LayoutHelper.createLinear(11, 11, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

		recordTimeText = new TextView(activity);
		recordTimeText.setText("00:00");
		recordTimeText.setTextColor(COLOR_RECORD_TIME_TEXT);
		recordTimeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		ScaleUtil.adjustTextSize(recordTimeText, getAudioUIScale());

		linearLayout.addView(recordTimeText,
				LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

		sendMessageLayout = (LinearLayout) view.findViewById(R.id.hs__sendMessageLayout);
		voice_rec_button_layout = (LinearLayout) view.findViewById(R.id.voice_rec_button_layout);
		voice_rec_button = (Button) view.findViewById(R.id.voice_rec_button);

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

		alliance_drag_layout = (RelativeLayout) view.findViewById(R.id.alliance_drag_layout);

		alliance_sliding_layout = (SlidingUpPanelLayout) view.findViewById(R.id.alliance_sliding_layout);
		alliance_sliding_layout.setDragViewId(R.id.alliance_dragView);
		alliance_sliding_layout.setScrollableViewResId(R.id.alliance_sys_msg_listview);
		new_alliance_sys_message = (TextView) view.findViewById(R.id.new_alliance_sys_message);
		alliance_null_sys_tip = (TextView) view.findViewById(R.id.alliance_null_sys_tip);
		alliance_null_sys_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SYS_MSG_NULL));
		new_alliance_sys_sliding_btn = (ImageView) view.findViewById(R.id.new_alliance_sys_sliding_btn);

		alliance_sliding_layout.addPanelSlideListener(new PanelSlideListener()
		{
			@Override
			public void onPanelSlide(View panel, float slideOffset)
			{
				// Log.i("sliding_layout", "onPanelSlide, offset " +
				// slideOffset);
			}

			@Override
			public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState)
			{

				System.out.println("alliance onPanelStateChanged previousState:" + previousState + "  newState:" + newState);
				if (newState != PanelState.HIDDEN)
					hasNewSystemAllianceMsg = false;

				if (previousState == newState)
					return;
				if (newState == PanelState.EXPANDED)
				{
					new_alliance_sys_sliding_btn.setImageResource(R.drawable.sliding_up);
					new_alliance_sys_message.setVisibility(View.GONE);
					setChannelViewIndex(TAB_ALLIANCE_SYS - 1);
				}
				else
				{
					new_alliance_sys_sliding_btn.setImageResource(R.drawable.sliding_down);
					setChannelViewIndex(TAB_ALLIANCE);
				}

				if (newState == PanelState.COLLAPSED)
					setSlideLayoutCollapsed();
				else
				{
					if (allianceSlideTimer != null)
					{
						allianceSlideTimer.cancel();
						allianceSlideTimer.purge();
					}
				}

				updateAllianceSlideDragLayoutHeight(previousState, newState, false);
			}
		});
		alliance_sliding_layout.setFadeOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				alliance_sliding_layout.setPanelState(PanelState.HIDDEN);
			}
		});

		alliance_sys_top_layout = (RelativeLayout) view.findViewById(R.id.alliance_sys_top_layout);
		alliance_name = (TextView) view.findViewById(R.id.alliance_name);
		if (UserManager.getInstance().getCurrentUser() != null)
			alliance_name.setText(UserManager.getInstance().getCurrentUser().asn);
		alliance_sys_top_icon = (ImageView) view.findViewById(R.id.alliance_sys_top_icon);
		alliance_sys_top_icon.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				onContentAreaTouched();
				if (alliance_sliding_layout.getPanelState() != PanelState.HIDDEN)
				{
					alliance_sliding_layout.setPanelState(PanelState.HIDDEN);
					LogUtil.trackAction("click_alliance_sys_btn_hidden");
				}
				else
				{
					alliance_sliding_layout.setPanelState(PanelState.EXPANDED);
					LogUtil.trackAction("click_alliance_sys_btn_expand");
				}
			}
		});

		alliance_msg_layout = (RelativeLayout) view.findViewById(R.id.alliance_msg_layout);
		alliance_sys_msg_listview = (ListView) view.findViewById(R.id.alliance_sys_msg_listview);
		alliance_sys_msg_listview.setVerticalFadingEdgeEnabled(false);
		alliance_sys_msg_listview.setCacheColorHint(Color.TRANSPARENT);
		alliance_sys_msg_listview.setDivider(null);
		alliance_sys_msg_listview.setOnScrollListener(mOnScrollListener);
		alliance_sys_msg_listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
		alliance_sys_msg_listview.setKeepScreenOn(true);

		alliance_sliding_layout.setScrollableView(alliance_sys_msg_listview);
		alliance_sys_list = (PullDownToLoadMoreView) view.findViewById(R.id.alliance_sys_list);
		alliance_sliding_layout.setScrollableViewParent(alliance_sys_list);
		alliance_sys_list.setTopViewInitialize(true);
		alliance_sys_list.setAllowPullDownRefersh(false);
		alliance_sys_list.setBottomViewWithoutScroll(false);
		alliance_sys_list.setListViewLoadListener(mListViewLoadListener);
		alliance_sys_list.setListViewTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				onContentAreaTouched();
				System.out.println("event:" + event.getAction());
				// if(sliding_layout!=null)
				// {
				// System.out.println("setPanelState HIDDEN 4");
				// sliding_layout.setPanelState(PanelState.HIDDEN);
				// }
				return false;
			}
		});

		country_drag_layout = (RelativeLayout) view.findViewById(R.id.country_drag_layout);

		country_sliding_layout = (SlidingUpPanelLayout) view.findViewById(R.id.country_sliding_layout);
		country_sliding_layout.setDragViewId(R.id.country_dragView);
		country_sliding_layout.setScrollableViewResId(R.id.country_sys_msg_listview);
		new_country_sys_message = (TextView) view.findViewById(R.id.new_country_sys_message);
		country_null_sys_tip = (TextView) view.findViewById(R.id.country_null_sys_tip);
		country_null_sys_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SYS_MSG_NULL));
		new_country_sys_sliding_btn = (ImageView) view.findViewById(R.id.new_country_sys_sliding_btn);

		country_sliding_layout.addPanelSlideListener(new PanelSlideListener()
		{
			@Override
			public void onPanelSlide(View panel, float slideOffset)
			{
				// Log.i("sliding_layout", "onPanelSlide, offset " +
				// slideOffset);
			}

			@Override
			public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState)
			{

				System.out.println("country onPanelStateChanged previousState:" + previousState + "  newState:" + newState);
				if (newState != PanelState.HIDDEN)
					hasNewSystemCountryMsg = false;

				if (previousState == newState)
					return;
				if (newState == PanelState.EXPANDED)
				{
					new_country_sys_sliding_btn.setImageResource(R.drawable.sliding_up);
					new_country_sys_message.setVisibility(View.GONE);
					setChannelViewIndex(TAB_COUNTRY_SYS - 1);
				}
				else
				{
					new_country_sys_sliding_btn.setImageResource(R.drawable.sliding_down);
					setChannelViewIndex(TAB_COUNTRY);
				}

				if (newState == PanelState.COLLAPSED)
					setCountrySlideLayoutCollapsed();
				else
				{
					if (countrySlideTimer != null)
					{
						countrySlideTimer.cancel();
						countrySlideTimer.purge();
					}
				}

				updateCountrySlideDragLayoutHeight(previousState, newState, false);
			}
		});
		country_sliding_layout.setFadeOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				country_sliding_layout.setPanelState(PanelState.HIDDEN);
			}
		});

		country_sys_top_icon = (ImageView) view.findViewById(R.id.country_sys_top_icon);
		country_sys_top_icon.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				onContentAreaTouched();
				if (country_sliding_layout.getPanelState() != PanelState.HIDDEN)
				{
					country_sliding_layout.setPanelState(PanelState.HIDDEN);
					LogUtil.trackAction("click_country_sys_btn_hidden");
				}
				else
				{
					country_sliding_layout.setPanelState(PanelState.EXPANDED);
					LogUtil.trackAction("click_country_sys_btn_expand");
				}
			}
		});

		country_msg_layout = (RelativeLayout) view.findViewById(R.id.country_msg_layout);
		country_sys_msg_listview = (ListView) view.findViewById(R.id.country_sys_msg_listview);
		country_sys_msg_listview.setVerticalFadingEdgeEnabled(false);
		country_sys_msg_listview.setCacheColorHint(Color.TRANSPARENT);
		country_sys_msg_listview.setDivider(null);
		country_sys_msg_listview.setOnScrollListener(mOnScrollListener);
		country_sys_msg_listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
		country_sys_msg_listview.setKeepScreenOn(true);

		country_sliding_layout.setScrollableView(country_sys_msg_listview);
		country_sys_list = (PullDownToLoadMoreView) view.findViewById(R.id.country_sys_list);
		country_sliding_layout.setScrollableViewParent(country_sys_list);
		country_sys_list.setTopViewInitialize(true);
		country_sys_list.setAllowPullDownRefersh(false);
		country_sys_list.setBottomViewWithoutScroll(false);
		country_sys_list.setListViewLoadListener(mListViewLoadListener);
		country_sys_list.setListViewTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				onContentAreaTouched();
				System.out.println("event:" + event.getAction());
				// if(sliding_layout!=null)
				// {
				// System.out.println("setPanelState HIDDEN 4");
				// sliding_layout.setPanelState(PanelState.HIDDEN);
				// }
				return false;
			}
		});

		country_exchange_layout = (RelativeLayout) view.findViewById(R.id.country_exchange_layout);
		country_channel_name = (TextView) view.findViewById(R.id.country_channel_name);
		country_exchange_btn = (ImageView) view.findViewById(R.id.country_exchange_btn);

		country_exchange_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (currentTab == TAB_COUNTRY)
				{
					showTab(TAB_BATTLE_FIELD);
					countryExchangeFlag = 1;
				}
				else if (currentTab == TAB_BATTLE_FIELD)
				{
					showTab(TAB_COUNTRY);
					countryExchangeFlag = 0;
				}
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
				if (config == null)
				{
					config = new LocalConfig();
					ConfigManager.getInstance().setLocalConfig(config);
				}
				if (showingCustomChannel == null
						&& (config.getCustomChannelType() != -1 || StringUtils.isNotEmpty(config.getCustomChannelId()) || StringUtils
								.isNotEmpty(config.randomChannelId)))
				{
					boolean oldChannelIsRandom = false;
					if (StringUtils.isNotEmpty(config.randomChannelId))
					{
						oldChannelIsRandom = true;
						if (!SwitchUtils.mqttEnable)
							WebSocketManager.getInstance().leaveCurrentRandomRoom();
						else
							MqttManager.getInstance().leaveCurrentRandomRoom();
					}
					config.setCustomChannelType(-1);
					config.setCustomChannelId("");
					config.randomChannelId = "";
					config.randomChatMode = 0;
					if (customChannelType != -1)
					{
						LogUtil.trackAction("custom_channel_removed");
						ChatServiceController.getInstance().postLatestCustomChatMessage(null);
						customChannelChange = true;
					}
					refreshCustomChatChannel();
					boolean hasCustomData = (friendList != null && friendList.size() > 0)
							|| (chatroomChannelList != null && chatroomChannelList.size() > 0);
					custom_chat_tip_layout.setVisibility(!oldChannelIsRandom
							&& getCurrentChannel().tab == TAB_CUSTOM
							&& (StringUtils.isEmpty(customChannelId) && (hasCustomData || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
					hideCustomChannelSetting(!oldChannelIsRandom);
				}
				else if (showingCustomChannel != null
						&& (config.getCustomChannelType() != showingCustomChannel.channelType
								|| (!showingCustomChannel.isRandomChatRoomChannel() && (StringUtils.isNotEmpty(config.randomChannelId) || !config
										.getCustomChannelId().equals(showingCustomChannel.channelID)))
								|| (showingCustomChannel
										.isRandomChatRoomChannel() && config.randomChatMode != showingCustomChannel.randomChatMode)))
				{
					if (!showingCustomChannel.isRandomChatRoomChannel())
					{
						WebSocketManager.getInstance().leaveCurrentRandomRoom();
						config.setCustomChannelType(showingCustomChannel.channelType);
						config.setCustomChannelId(showingCustomChannel.channelID);
						config.randomChannelId = "";
						config.randomChatMode = 0;
						ChatChannel channel = ChannelManager.getInstance().getChannel(showingCustomChannel.channelType,
								showingCustomChannel.channelID);
						if (channel != null)
							ServiceInterface.sendChatLatestMessage(channel);
						refreshCustomChatChannel();
						custom_chat_tip_layout.setVisibility(StringUtils.isEmpty(customChannelId) ? View.VISIBLE : View.GONE);
						hideCustomChannelSetting(true);
					}
					else
					{
						if (showingCustomChannel.randomChatMode == 1)
						{
							if (!SwitchUtils.mqttEnable)
								WebSocketManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
							else
								MqttManager.getInstance().getRandomChatRoomId(ConfigManager.getInstance().gameLang);
						}
						else if (showingCustomChannel.randomChatMode == 2)
						{
							if (!SwitchUtils.mqttEnable)
								WebSocketManager.getInstance().getRandomChatRoomId("");
							else
								MqttManager.getInstance().getRandomChatRoomId("");
						}
						hideCustomChannelSetting(false);
					}

					if (customChannelType == -1 && showingCustomChannel.channelType != -1)
					{
						LogUtil.trackAction("custom_channel_added");
						customChannelChange = true;
					}
					else if (customChannelType != -1
							&& showingCustomChannel.channelType != -1
							&& (customChannelType != showingCustomChannel.channelType || (!customChannelId
									.equals(showingCustomChannel.channelID))))
					{
						LogUtil.trackAction("custom_channel_changed");
						customChannelChange = true;
					}
				}

			}
		});

		recordCircle = new RecordCircle(activity);
		recordCircle.setVisibility(View.GONE);
		popFrameLayout = (FrameLayout) view.findViewById(R.id.popFrameLayout);
		// sizeNotifierFrameLayout = new SizeNotifierFrameLayout(activity);
		// ColorDrawable back = new ColorDrawable(0x000000);
		// back.setAlpha(0);
		// sizeNotifierFrameLayout.setBackgroundImage(back);
		// popFrameLayout.addView(sizeNotifierFrameLayout,
		// LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,
		// LayoutHelper.MATCH_PARENT));
		// sizeNotifierFrameLayout.addView(recordCircle,
		// LayoutHelper.createFrame(124, 124, Gravity.BOTTOM | Gravity.RIGHT, 0,
		// 0, -36, -38));
		popFrameLayout.addView(recordCircle, LayoutHelper.createFrame((int) (124 * getAudioUIScale()), (int) (124 * getAudioUIScale()),
				Gravity.BOTTOM | Gravity.RIGHT, 0, 0, (int) (-36 * getAudioUIScale()), (int) (-38 * getAudioUIScale())));

		// fragmentLayout.addView(recordCircle, LayoutHelper.createFrame(124,
		// 124, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -36, -38));

		prepareCustomChannelData();

		if ((friendList != null && friendList.size() > 0) || (chatroomChannelList != null && chatroomChannelList.size() > 0))
		{
			addCustomChatBtn.setVisibility(View.VISIBLE);
			custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP));
		}
		else
		{
			addCustomChatBtn.setVisibility(View.GONE);
			if (!ChatServiceController.randomChatEnable)
				custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_CHAT_ADD_TIP));
			else
				custom_chat_tip_text.setVisibility(View.GONE);
		}

		if (channelViews != null && channelViews.size() >= TAB_CUSTOM + 1)
		{
			ChannelView channelView = channelViews.get(TAB_CUSTOM);
			if (channelView != null)
			{
				refreshCustomChannelName(channelView.chatChannel);
			}
		}

		voice_rec_button_layout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
				{
					hideEmojPanel();

					if (!PermissionManager.getInstance().checkXMRecordPermission())
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
							if (activity != null)
							{
								activity.runOnUiThread(new Runnable()
								{

									@Override
									public void run()
									{
										if (!recordBtnUp)
										{
											startedDraggingX = -1;
											gotoLastLine();
											XiaoMiToolManager.getInstance().startRecord();
											startRecordTimer();

											// 录音开始的回调中的处理
											if (!recordingAudio)
											{
												recordingAudio = true;
												updateAudioRecordIntefrace();
											}

											updateAudioRecordIntefrace();
											voice_rec_button_layout.getParent().requestDisallowInterceptTouchEvent(true);
										}
									}
								});
							}

						}
					};
					timer.schedule(timerTask, 300);

				}
				else if ((motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL))
				{
					recordBtnUp = true;
					startedDraggingX = -1;
					if (recordingAudio)
					{
						XiaoMiToolManager.getInstance().stopRecord(recordingAudio);
						stopRecordTimer();
					}
					exitRecordingUI();
				}
				else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && recordingAudio)
				{
					float x = motionEvent.getX();
					if (x < -distCanMove)
					{
						if (recordingAudio)
						{
							LogUtil.trackPageView("Audio-cancelRecord");
							XiaoMiToolManager.getInstance().stopRecord(false);
							stopRecordTimer();
						}

						exitRecordingUI();
					}

					x = x + ViewProxy.getX(voice_rec_button_layout);
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
					if (startedDraggingX != -1)
					{
						float dist = (x - startedDraggingX);
						ViewProxy.setTranslationX(recordCircle, dist);
						params.leftMargin = AndroidUtilities.dp(30) + (int) dist;
						slideLayout.setLayoutParams(params);
						float alpha = 1.0f + dist / distCanMove;
						if (alpha > 1)
						{
							alpha = 1;
						}
						else if (alpha < 0)
						{
							alpha = 0;
						}
						ViewProxy.setAlpha(slideLayout, alpha);
					}
					if (x <= ViewProxy.getX(slideLayout) + slideLayout.getWidth() + AndroidUtilities.dp(30))
					{
						if (startedDraggingX == -1)
						{
							startedDraggingX = x;
							distCanMove = (recordPanel.getMeasuredWidth() - slideLayout.getMeasuredWidth() - AndroidUtilities.dp(48)) / 2.0f;
							if (distCanMove <= 0)
							{
								distCanMove = AndroidUtilities.dp(80);
							}
							else if (distCanMove > AndroidUtilities.dp(80))
							{
								distCanMove = AndroidUtilities.dp(80);
							}
						}
					}
					if (params.leftMargin > AndroidUtilities.dp(30))
					{
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
		// initInputLayout();

		if (!lazyLoading)
			renderList();

		this.replyField = ((AutoCompleteTextView) view.findViewById(ResUtil.getId(this.activity, "id", "hs__messageText")));

		List<UserInfo> countryAtUserList = UserManager.getInstance().getCurrentCountryLoadUser();
		initAtAdapter(countryAtUserList, DBDefinition.CHANNEL_TYPE_COUNTRY);
		List<UserInfo> allianceAtUserList = UserManager.getInstance().getAllianceAtMemberList();
		initAtAdapter(allianceAtUserList, DBDefinition.CHANNEL_TYPE_ALLIANCE);
		replyField.setAdapter(allianceAutoCompleteAdapter);

		replyField.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				UserInfo user = (UserInfo) parent.getAdapter().getItem(position);
				if (user != null)
				{

					String currentText = currentInputText;
					currentText = currentText.substring(0, currentText.lastIndexOf("@") + 1);
					String userName = "";
					if (user.isNpc() && ChatServiceController.isInCountryOrSysCountryChannel())
						userName = "Agatha";
					else
						userName = user.userName;
					if (StringUtils.isNotEmpty(userName))
					{
						String atName = "@" + userName + " ";

						CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();

						boolean isExist = false;
						boolean existNpcAt = false;
						if (inputAtList != null)
						{
							for (InputAtContent at : inputAtList)
							{
								if (!isExist && at != null && StringUtils.isNotEmpty(at.getAtNameText()) && at.getAtNameText().equals(atName))
								{
									isExist = true;
									if ((existNpcAt && ChatServiceController.isInCountryOrSysCountryChannel())
											|| !ChatServiceController.isInCountryOrSysCountryChannel())
										break;
								}
								if (!existNpcAt && at.isNpcAt() && ChatServiceController.isInCountryOrSysCountryChannel())
								{
									existNpcAt = true;
									if (isExist)
										break;
								}
							}

							if (!isExist)
							{
								InputAtContent contentAt = new InputAtContent();
								contentAt.setUid(user.uid);
								contentAt.setAtNameText(atName);
								if (user.isNpc())
									contentAt.setLang(ConfigManager.getInstance().gameLang);
								int startPos = currentText.length() - 1;
								if (existNpcAt && ChatServiceController.isInCountryOrSysCountryChannel())
								{
									String npcName = LanguageManager.getNPCName();
									String wrapNpcName = "Agatha";
									startPos -= (npcName.length() - wrapNpcName.length());
								}
								if (startPos < 0)
									startPos = 0;
								contentAt.setStartPos(startPos);
								inputAtList.add(contentAt);
							}
						}

						if (user.isNpc() && ChatServiceController.isInCountryOrSysCountryChannel())
							currentText += LanguageManager.getNPCName();
						else
							currentText += user.userName;
						currentText += " ";
					}
					currentAtText = "";
					replyField.setText(currentText);
					replyField.setSelection(currentText.length());
				}
			}

		});

		replyField.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();
				if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputAtList != null && inputAtList.size() > 0)
				{
					AutoCompleteTextView editText = (AutoCompleteTextView) v;
					if (editText != null)
					{
						int start = editText.getSelectionStart();
						int end = editText.getSelectionEnd();
						String currentText = currentInputText;

						List<InputAtContent> deleteAt = new ArrayList<InputAtContent>();

						int lastDeleteIndex = -1;
						if (inputAtList != null)
						{
							if (start == end)
							{
								InputAtContent removeAt = null;
								for (int i = 0; i < inputAtList.size(); i++)
								{
									InputAtContent at = inputAtList.get(i);
									if (at != null && StringUtils.isNotEmpty(at.getAtNameText()))
									{
										String atNameText = at.getAtNameText();
										if (at.isNpcAt())
											atNameText = "@" + LanguageManager.getNPCName() + " ";
										int atEnd = at.getStartPos() + atNameText.length();
										if (atEnd == end)
										{
											int startPos = at.getStartPos();
											if (startPos < 0) 
											{
												startPos = 0; //MM: substring的负数坑了
											}
											String front = currentText.substring(0, startPos);
											String behind = currentText.substring(atEnd);
											currentText = front + behind;
											replyField.setText(currentText);
											replyField.setSelection(startPos);
											lastDeleteIndex = i;
											removeAt = at;
											break;
										}
									}
								}

								if (removeAt != null && StringUtils.isNotEmpty(removeAt.getAtNameText()))
								{
									for (int i = lastDeleteIndex + 1; i < inputAtList.size(); i++)
									{
										InputAtContent at = inputAtList.get(i);
										if (at != null)
											at.setStartPos(at.getStartPos() - removeAt.getAtNameText().length());
									}
								}
								else
								{
									for (int i = 0; i < inputAtList.size(); i++)
									{
										InputAtContent at = inputAtList.get(i);
										if (at != null && end < at.getStartPos())
											at.setStartPos(at.getStartPos() - 1);
									}
								}

								inputAtList.remove(removeAt);
							}
							else if (start < end)
							{
								boolean existNpcAt = false;
								for (int i = 0; i < inputAtList.size(); i++)
								{
									InputAtContent at = inputAtList.get(i);
									if (at != null && StringUtils.isNotEmpty(at.getAtNameText()))
									{
										String atNameText = at.getAtNameText();
										int startPos = at.getStartPos();
										if (existNpcAt)
										{
											String npcName = LanguageManager.getNPCName();
											String wrapNpcName = "Agatha";
											startPos += (npcName.length() - wrapNpcName.length());
										}
										if (at.isNpcAt())
											atNameText = "@" + LanguageManager.getNPCName() + " ";
										int atEnd = startPos + atNameText.length();

										if (startPos >= end)
											break;
										else if ((startPos <= start && start < atEnd) ||
												(startPos <= end && end < atEnd) ||
												(startPos >= start && atEnd < end))
										{
											deleteAt.add(at);
											lastDeleteIndex = i;
										}

										if (!existNpcAt && at.isNpcAt())
											existNpcAt = true;
									}
								}

								if (lastDeleteIndex > -1)
								{
									for (int i = lastDeleteIndex + 1; i < inputAtList.size(); i++)
									{
										InputAtContent at = inputAtList.get(i);
										if (at != null)
											at.setStartPos(at.getStartPos() - (end - start));
									}
								}

								inputAtList.removeAll(deleteAt);
							}
						}
					}

				}

				return false;
			}
		});

		this.wordCount = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		if (!ChatServiceController.isInMailDialog())
		{
			if (StringUtils.isNotEmpty(chatStatus))
			{
				getTitleLabel().setText(chatStatus);
			}
			else
			{
				getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
			}
		}
		else
		{
			getTitleLabel().setText(ChatServiceController.getInstance().getUserMailTitle());
		}

		if (ChatServiceController.isChatRestrictForLevel())
		{
			replyField.setEnabled(false);
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
					"" + ChatServiceController.getChatRestrictLevel()));
		}
		else
		{
			replyField.setEnabled(true);
			replyField.setHint("");
			if (ChatServiceController.needShowAllianceDialog)
				replyField.setText(LanguageManager.getLangByKey(LanguageKeys.INPUT_ALLIANCE_DIALOG));
			else
			{
				initSavedInputAtList();
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
				if (channel != null && StringUtils.isNotEmpty(channel.draft))
				{
					replyField.setText(channel.draft);
					replyField.setSelection(channel.draft.length());

					if (StringUtils.isNotEmpty(channel.draft))
					{
						replyField.setText(channel.draft);
						replyField.setSelection(channel.draft.length());
					}
				}
				else
				{
					replyField.setText("");
				}
			}
		}

		addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));

		getMemberSelectButton()
				.setVisibility(
						isSelectMemberBtnEnable
								&& (ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_COUNTRY
										&& ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && ChatServiceController
												.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_ALLIANCE) ? View.VISIBLE : View.GONE);

		buttonCountry = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonCountry"));
		buttonAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonAllie"));
		buttonCustom = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonFriend"));

		// if (ChatServiceController.isInCrossFightServer())
		// {
		// buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD));
		// }
		// else
		// {
		buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY));
		// }
		buttonAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE));
		buttonCustom.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT));
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, true);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, false);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCustom, false);

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

		channelButton = new ArrayList<Button>();
		channelButton.add(buttonCountry);
		channelButton.add(buttonAlliance);
		channelButton.add(buttonCustom);

		buttonJoinAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceBtn"));
		buttonJoinAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));

		buttonJoinAlliance.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ChatServiceController.doHostAction("joinAllianceBtnClick", "", "", "", true);
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

		battle_horn_close_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				hideHornScrollText();
				battleHornTextHidden = true;
			}
		});

		noAllianceTipText = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceTipText")));
		noAllianceTipText.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE));

		refreshSendButton();

		for (int i = 0; i < channelButton.size(); i++)
		{
			channelButton.get(i).setTag("" + i);
			channelButton.get(i).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					try
					{
						if (view.getTag() == null || channelViews == null || channelViews.size() <= 0)
							return;
						int index = Integer.parseInt(view.getTag().toString());

						int tab = index;
						if (index == TAB_COUNTRY && countryExchangeFlag == 1)
						{
							index = TAB_BATTLE_FIELD - 1;
							tab = TAB_BATTLE_FIELD;
						}

						showTab(tab);
						if (index >= channelViews.size())
							return;
						ChannelView channelView = channelViews.get(index);
						if (channelView != null)
						{
							channelView.setLoadingStart(false);

							if (channelView.tab == TAB_CUSTOM)
							{
								if (StringUtils.isNotEmpty(customChannelId))
								{
									LogUtil.trackAction("click_custom_channel_exist_true");
								}
								else
								{
									if ((friendList != null && friendList.size() > 0)
											|| (chatroomChannelList != null && chatroomChannelList.size() > 0))
										LogUtil.trackAction("click_custom_channel_exist_false");
									else
										LogUtil.trackAction("click_custom_channe_empty");
								}
							}
							else
							{
								LogUtil.trackAction("click_chat_tab" + channelView.tab);
							}

							if (channelView.tab == TAB_COUNTRY || channelView.tab == TAB_COUNTRY_SYS)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY) });
							}
							else if (channelView.tab == TAB_ALLIANCE || channelView.tab == TAB_ALLIANCE_SYS)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE) });
							}
							else if (channelView.tab == TAB_CUSTOM)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT) });
							}
							else if (channelView.tab == TAB_BATTLE_FIELD)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD) });
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		getMemberSelectButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				hideSoftKeyBoard();
				hideEmojPanel();
				ServiceInterface.showChatRoomSettingActivity(activity);
				// ServiceInterface.showMemberSelectorActivity(activity, true);
			}
		});

		addReply.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String inputText = replyField.getText().toString();
				int frontSpaceNum = 0;
				int endSpaceNum = 0;
				CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();
				if (inputAtList != null && inputAtList.size() > 0)
				{
					if (inputText.startsWith(" "))
					{
						for (int i = 0; i < inputText.length(); i++)
						{
							if (inputText.charAt(i) <= '\u0020')
								frontSpaceNum++;
							else
								break;
						}
					}

					if (inputText.endsWith(" "))
					{
						for (int i = inputText.length() - 1; i >= 0; i--)
						{
							if (inputText.charAt(i) <= '\u0020')
								endSpaceNum++;
							else
								break;
						}
					}
				}

				String replyText = replyField.getText().toString().trim();
				if (endSpaceNum > 0)
					replyText += " ";

				if (frontSpaceNum > 0)
				{
					for (int i = 0; i < inputAtList.size(); i++)
					{
						InputAtContent at = inputAtList.get(i);
						if (at != null)
						{
							int startPos = at.getStartPos() - frontSpaceNum;
							if (startPos < 0)
								startPos = 0;
							at.setStartPos(startPos);
						}
					}
				}

				if (!TextUtils.isEmpty(replyText))
				{
					if (horn_checkbox.isChecked()
							&& (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY || ChatServiceController
									.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
					{
						try
						{
							int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
							if (hornBanedTime == 0)
							{
								int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
								String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
								if (price == 0)
								{
									if (ConfigManager.isFirstUserHorn)
										MenuController.showSendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn),
												replyText);
									else
									{
										ChatServiceController.sendMsg(replyText, true, false, null);
									}
								}
								else if (price > 0)
								{
									if (ConfigManager.isFirstUserCornForHorn)
										MenuController.showSendHornWithCornConfirm(
												LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn), replyText, price);
									else
									{
										boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough",
												new Object[] { Integer.valueOf(price) });
										if (isCornEnough)
										{
											ChatServiceController.sendMsg(replyText, true, true, null);
										}
										else
										{
											MenuController.showCornNotEnoughConfirm(LanguageManager
													.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
										}
									}
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

					}
					else
					{
						if (ChatServiceController.isInCountryOrAllianceChannel())
							ChatServiceController.sendMsgWithAt(replyText, false, false, null, inputAtList);
						else
							ChatServiceController.sendMsg(replyText, false, false, null);
					}

				}
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
				hideEmojPanel();
				EditText editText = (EditText) v;
				if (editText != null)
				{
					String hint;
					if (hasFocus)
					{
						hint = editText.getHint().toString();
						editText.setTag(hint);
						editText.setHint("");
					}
					else
					{
						hint = editText.getTag().toString();
						editText.setHint(hint);
					}
				}
				if (getCurrentChannel() != null)
				{
					if (getCurrentChannel().tab == TAB_ALLIANCE_SYS && ChatServiceController.allianceSysChannelEnable)
					{
						if (alliance_sliding_layout != null && alliance_sliding_layout.getPanelState() != PanelState.HIDDEN)
							alliance_sliding_layout.setPanelState(PanelState.HIDDEN);
					}
					else if (getCurrentChannel().tab == TAB_COUNTRY_SYS && ChatServiceController.countrySysChannelEnable)
					{
						if (country_sliding_layout != null && country_sliding_layout.getPanelState() != PanelState.HIDDEN)
							country_sliding_layout.setPanelState(PanelState.HIDDEN);
					}
				}
			}
		});

		replyField.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				AutoCompleteTextView editText = (AutoCompleteTextView) v;
				if (editText != null)
				{
					CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();
					int start = editText.getSelectionStart();
					if (inputAtList != null)
					{
						for (int i = 0; i < inputAtList.size(); i++)
						{
							InputAtContent at = inputAtList.get(i);
							if (at != null && StringUtils.isNotEmpty(at.getAtNameText()))
							{
								int index = at.getStartPos();
								if (index < 0)
								{
									index = 0; //MM: setSelection可能越界。
								}
								String atNameText = at.getAtNameText();
								if (at.isNpcAt())
									atNameText = "@" + LanguageManager.getNPCName() + " ";
								if (index < start && start < index + atNameText.length())
								{
									replyField.setSelection(index);
									break;
								}
							}
						}
					}
				}
			}
		});

		textChangedListener = new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				if (replyField != null)
				{
					replyField.post(new Runnable()
					{
						@Override
						public void run()
						{
							refreshWordCount();
							if (ChatServiceController.isInCountryOrAllianceChannel())
							{
								if (replyField != null)
								{
									currentInputText = replyField.getText().toString();
									currentCursorPos = replyField.getSelectionEnd();
									CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();
									if (currentCursorPos > beforeCursorPos && currentInputText.length() > currentCursorPos && inputAtList != null)
									{
										for (int i = 0; i < inputAtList.size(); i++)
										{
											InputAtContent at = inputAtList.get(i);
											if (at != null && at.getStartPos() > beforeCursorPos)
												at.setStartPos(at.getStartPos() + currentCursorPos - beforeCursorPos);
										}
									}
								}
							}
						}
					});
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				beforeCursorPos = replyField.getSelectionEnd();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		};
		this.replyField.addTextChangedListener(textChangedListener);

		refreshRealtimeBtnVisible();
		if (!WebRtcPeerManager.published)
			getRealtimeRightBtnText().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_REALTIME_VOICE));
		else
			getRealtimeRightBtnText().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_VOICE_SETTING));

		getRealtimeRightBtn().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				// ChatServiceController.doHostAction("showFriend", "", "", "", false);
				if (!PermissionManager.getInstance().checkRealtimeVoicePermission())
					return;
				ServiceInterface.showRealtimeVoiceRoomActivity();
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
				checkFirstGlobalLayout();
				adjustHeight();
			}
		};
		messagesListFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		((ChatActivity) getActivity()).fragment = this;

		if (!lazyLoading)
		{
			refreshToolTip();
			refreshHasMoreData(getCurrentChannel());
		}

		MsgItem msgItem = ScrollTextManager.getInstance().getNextText(ChatServiceController.getCurrentChannelType());
		if (msgItem != null)
			showHornScrollText(msgItem);
		else
		{
			horn_scroll_layout.setVisibility(View.GONE);
			battle_horn_scroll_layout.setVisibility(View.GONE);
		}

		initSensorListener();
	}

	public void notifyCustomChannelDataSetChanged()
	{
		if (customChannelListAdapter != null)
			customChannelListAdapter.notifyDataSetChanged();
	}

	public void notifyAllianceMemberChanged()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					List<UserInfo> userList = UserManager.getInstance().getAllianceAtMemberList();
					initAtAdapter(userList, DBDefinition.CHANNEL_TYPE_ALLIANCE);
					if (replyField != null && ChatServiceController.isInAllianceOrSysAllianceChannel())
						replyField.setAdapter(allianceAutoCompleteAdapter);
				}
			});
		}
	}

	private void refreshDragonJoinStatus()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (WebSocketManager.isInDragonObserverRoom())
		{
			battle_field_tip_layout.setVisibility(View.GONE);
			if (currentTab == TAB_BATTLE_FIELD)
				relativeLayout1.setVisibility(View.VISIBLE);
		}
		else if (WebSocketManager.joinDragonObserverRooming())
		{
			battle_field_btn.setVisibility(View.GONE);
			battle_field_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.DRAGON_OBSERVER_JOIN_TIP));
		}
		else if (WebSocketManager.joinDragonObserverRoomFailed())
		{
			battle_field_btn.setVisibility(View.VISIBLE);
			battle_field_btn.setText(LanguageManager.getLangByKey(LanguageKeys.DRAGON_OBSERVER_JOIN_TIP_BTN));
			battle_field_btn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if (!SwitchUtils.mqttEnable)
						WebSocketManager.getInstance().joinDragonObserverRoom();
					else
						MqttManager.getInstance().joinDragonObserverRoom();
					refreshDragonJoinStatus();
				}
			});
			battle_field_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.DRAGON_OBSERVER_JOIN_FAILED_TIP));
		}
	}

	public void refreshDrgonObserverChannelView()
	{
		if (activity == null || !ChatServiceController.isInChat() || channelViews == null || channelViews.size() != TAB_BATTLE_FIELD)
			return;

		activity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				final ChannelView channelView = channelViews.get(TAB_BATTLE_FIELD - 1);
				if (channelView == null)
					return;

				channelView.tab = TAB_BATTLE_FIELD;
				ChatChannel chatChannel = null;
				if (ChatServiceController.getInstance().needShowBattleFieldChannel())
					chatChannel = ChannelManager.getInstance().getBattleFieldChannel();

				if (chatChannel != null)
				{
					if (!chatChannel.hasInitLoaded())
						chatChannel.loadMoreMsg();
					chatChannel.clearFirstNewMsg();
					chatChannel.setChannelView(channelView);
					channelView.setVisibility(View.VISIBLE);
				}

				channelView.chatChannel = chatChannel;
				channelView.channelType = DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
				channelView.tab = TAB_BATTLE_FIELD;

				List<MsgItem> msgList = null;

				if (channelView.chatChannel != null)
				{
					msgList = channelView.chatChannel.msgList;
					channelView.setVisibility(View.VISIBLE);
				}
				else
				{
					channelView.setVisibility(View.GONE);
				}

				if (msgList == null)
					msgList = new ArrayList<MsgItem>();

				if (msgList != null)
				{
					MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
					channelView.setMessagesAdapter(adapter);
					XiaoMiToolManager.getInstance().addAudioListener(channelView.getMessagesAdapter());
				}
				if (channelView.messagesListView != null)
					channelView.messagesListView.setAdapter(channelView.getMessagesAdapter());
				if (channelView.chatChannel != null && channelView.chatChannel.lastPosition.x != -1 && rememberPosition)
				{
					channelView.messagesListView.setSelectionFromTop(channelView.chatChannel.lastPosition.x,
							channelView.chatChannel.lastPosition.y);
				}
				else
				{
					if (channelView.getMessagesAdapter() != null)
						channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
			}
		});
	}

	public void refreshDragonObserverJoinStatus()
	{
		if (!ChatServiceController.dragonObserverEnable)
			return;

		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					refreshDragonJoinStatus();
				}
			});
		}
	}

	public void showHornScrollText(MsgItem msgItem)
	{
		if (!(msgItem.isHornMessage() || msgItem.isStealFailedMessage()))
			return;

		if (!(ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY || ChatServiceController
				.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
		{
			showHornScrollLayout(msgItem, false, false);
		}
		else
		{
			if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
			{
				if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					hornTextHidden = false;
					showHornScrollLayout(msgItem, true, false);
				}
				else
				{
					if (msgItem.canEnterScrollTextQueue())
					{
						ScrollTextManager.getInstance().clear(msgItem.channelType);
						ScrollTextManager.getInstance().push(msgItem, msgItem.channelType);
					}
				}
			}
			else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			{
				if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				{
					battleHornTextHidden = false;
					showHornScrollLayout(msgItem, false, true);
				}
				else
				{
					if (msgItem.canEnterScrollTextQueue())
					{
						ScrollTextManager.getInstance().clear(msgItem.channelType);
						ScrollTextManager.getInstance().push(msgItem, msgItem.channelType);
					}
				}
			}
		}
	}

	private void showHornScrollLayout(MsgItem msgItem, boolean country, boolean battle)
	{
		if (horn_scroll_layout != null)
		{
			if (country)
			{
				if (horn_scroll_layout.getVisibility() != View.VISIBLE)
					horn_scroll_layout.setVisibility(View.VISIBLE);
				ScrollTextManager.getInstance().showScrollText(msgItem, horn_scroll_text, horn_name, horn_scroll_layout,
						DBDefinition.CHANNEL_TYPE_COUNTRY);
			}
			else if (!country && horn_scroll_layout.getVisibility() != View.GONE)
				horn_scroll_layout.setVisibility(View.GONE);
		}
		if (battle_horn_scroll_layout != null)
		{
			if (battle)
			{
				if (battle_horn_scroll_layout.getVisibility() != View.VISIBLE)
					battle_horn_scroll_layout.setVisibility(View.VISIBLE);
				ScrollTextManager.getInstance().showScrollText(msgItem, battle_horn_scroll_text, battle_horn_name,
						battle_horn_scroll_layout, DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
			}
			else if (!battle && battle_horn_scroll_layout.getVisibility() != View.GONE)
				battle_horn_scroll_layout.setVisibility(View.GONE);
		}
	}

	private void refreshCustomChannelName(ChatChannel channel)
	{
		if (channel == null)
		{
			if (custom_channel_setting_layout.getVisibility() != View.GONE)
				custom_channel_setting_layout.setVisibility(View.GONE);
		}
		else
		{
			if (custom_channel_setting_layout.getVisibility() != View.VISIBLE)
				custom_channel_setting_layout.setVisibility(View.VISIBLE);

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				if (StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					UserInfo userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;

					if (userInfo != null && StringUtils.isNotEmpty(userInfo.userName))
					{
						nameText = userInfo.userName;
					}
					else
					{
						if (StringUtils.isNotEmpty(channel.customName))
							nameText = channel.customName;
						else
							nameText = fromUid;
					}

					if (StringUtils.isNotEmpty(nameText))
					{
						if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
							nameText += "(MOD)";
						else if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
							nameText += "(" + LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_DRIFTING_BOTTLE) + ")";
					}
					custom_channel_name.setText(nameText);
				}
				else
				{
					if (StringUtils.isNotEmpty(channel.customName))
						custom_channel_name.setText(channel.customName);
					else
						custom_channel_name.setText(channel.channelID);
				}

			}
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM
					|| channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (StringUtils.isNotEmpty(channel.customName))
					custom_channel_name.setText(channel.customName);
				else
					custom_channel_name.setText(channel.channelID);
			}
		}
	}

	private void refreshSettingCustomChannelName(ChatChannel channel)
	{
		custom_mod_image.setVisibility(View.GONE);
		if (channel == null)
		{
			if (customChannelName.getVisibility() != View.INVISIBLE)
				customChannelName.setVisibility(View.INVISIBLE);
		}
		else
		{
			if (customChannelName.getVisibility() != View.VISIBLE)
				customChannelName.setVisibility(View.VISIBLE);

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				if (StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					UserInfo userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null && StringUtils.isNotEmpty(userInfo.userName))
					{
						nameText = userInfo.userName;
						if (StringUtils.isNotEmpty(userInfo.asn))
							nameText = "(" + userInfo.asn + ")" + nameText;
					}
					else
					{
						if (StringUtils.isNotEmpty(channel.customName))
							nameText = channel.customName;
						else
							nameText = fromUid;
					}
					if (StringUtils.isNotEmpty(nameText) && channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
					{
						custom_mod_image.setVisibility(View.VISIBLE);
					}
					customChannelName.setText(nameText);
				}
				else
				{
					if (StringUtils.isNotEmpty(channel.customName))
						customChannelName.setText(channel.customName);
					else
						customChannelName.setText(fromUid);
				}
			}
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM
					|| channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (StringUtils.isNotEmpty(channel.customName))
					customChannelName.setText(channel.customName);
				else
					customChannelName.setText(channel.channelID);
			}
		}
	}

	public void hideHornScrollText()
	{
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
			ScrollTextManager.getInstance().shutDownScrollText(horn_scroll_text, ChatServiceController.getCurrentChannelType());
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			ScrollTextManager.getInstance().shutDownScrollText(battle_horn_scroll_text, ChatServiceController.getCurrentChannelType());
	}

	private boolean lazyLoading = true;

	protected void onBecomeVisible()
	{
		if (inited)
			return;
		timerDelay = 500;
		startTimer();
	}

	private int currentChannelViewIndex;

	private void initChannelViews()
	{
		channelViews = new ArrayList<ChannelView>();
		if (ChatServiceController.isInChat())
		{
			LocalConfig config = ConfigManager.getInstance().getLocalConfig();
			if (config != null)
			{
				if (StringUtils.isNotEmpty(config.randomChannelId))
				{
					customChannelType = DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
					customChannelId = config.randomChannelId;
				}
				else
				{
					customChannelType = config.getCustomChannelType();
					customChannelId = config.getCustomChannelId();
				}
			}

			int channelViewCount = 5;
			if (ChatServiceController.getInstance().needCrossServerBattleChat())
				channelViewCount = 6;

			for (int i = 0; i < channelViewCount; i++)
			{
				ChannelView channelView = new ChannelView();
				channelView.tab = i;
				ChatChannel chatChannel = null;
				if (i == TAB_CUSTOM)
				{
					System.out.println("customChannelType:" + customChannelType + "  customChannelId:" + customChannelId);
					if (ChannelManager.isUserChannelType(customChannelType) && StringUtils.isNotEmpty(customChannelId))
						chatChannel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);
					channelView.channelType = DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
				}
				else if (i == 5)
				{
					if (ChatServiceController.getInstance().needShowBattleFieldChannel())
						chatChannel = ChannelManager.getInstance().getBattleFieldChannel();
					channelView.channelType = DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
					channelView.tab = TAB_BATTLE_FIELD;
				}
				else if (i == 3)
				{
					chatChannel = ChannelManager.getInstance().getAllianceSysChannel();
					channelView.channelType = DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS;
					channelView.tab = TAB_ALLIANCE_SYS;
				}
				else if (i == 4)
				{
					chatChannel = ChannelManager.getInstance().getCountrySysChannel();
					channelView.channelType = DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
					channelView.tab = TAB_COUNTRY_SYS;
				}
				else
				{
					chatChannel = ChannelManager.getInstance().getChannel(i);
					channelView.channelType = i;
				}
				if (chatChannel != null)
				{
					if (!chatChannel.hasInitLoaded())
						chatChannel.loadMoreMsg();
					System.out.println("i:" + i + "  chatChannel.channelId:" + chatChannel.channelID + "  chatChannel.msgList:"
							+ chatChannel.msgList.size());
					chatChannel.clearFirstNewMsg();
					chatChannel.setChannelView(channelView);
					if (i == TAB_CUSTOM)
					{
						int mailType = MailManager.MAIL_USER;
						if (chatChannel.isModChannel())
							mailType = MailManager.MAIL_MOD_PERSONAL;
						else if (chatChannel.isDriftingBottleChannel())
							mailType = MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND;
						ServiceInterface.setMailInfo(chatChannel.channelID, chatChannel.latestId, chatChannel.getCustomName(), mailType);
					}
				}
				channelView.chatChannel = chatChannel;
				channelViews.add(channelView);
			}
		}
		else if (ChatServiceController.isInMailDialog())
		{
			ChannelView channelView = new ChannelView();
			channelView.tab = TAB_MAIL;

			int currentChannelType = ChatServiceController.getCurrentChannelType();
			if (currentChannelType == DBDefinition.CHANNEL_TYPE_USER || currentChannelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			{
				ChatChannel chatChannel = ChannelManager.getInstance().getChannel(currentChannelType);
				if (chatChannel != null)
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "chatChannel.channelID", chatChannel.channelID);
					chatChannel.clearFirstNewMsg();
					chatChannel.setChannelView(channelView);
				}
				channelView.channelType = currentChannelType;
				channelView.chatChannel = chatChannel;
				if (channelView.chatChannel != null && channelView.chatChannel.msgList != null)
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "channelView.chatChannel.msgList size", channelView.chatChannel.msgList.size());
			}
			channelViews.add(channelView);
		}
	}

	public ChannelView getChannelView(int index)
	{
		if (channelViews == null || index < 0 || index >= channelViews.size())
			return null;

		return channelViews.get(index);
	}

	public void setChannelViewIndex(int i)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "set current channelView index", i);
		if (i >= 0 && i < channelViews.size())
		{
			currentChannelViewIndex = i;
			ChannelView channelView = getCurrentChannelView();
			if (channelView != null)
				refreshHasMoreData(channelView);
		}
	}

	public ChannelView getCurrentChannelView()
	{
		try
		{
			if (channelViews == null || currentChannelViewIndex < 0 || currentChannelViewIndex >= channelViews.size())
				return null;
			return channelViews.get(currentChannelViewIndex);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public ChannelView getCountryChannelView()
	{
		try
		{
			if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
				return channelViews.get(0);
			else
				return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	// public void resetChannelView()
	// {
	// for (int i = 0; i < channelViews.size(); i++)
	// {
	// ChannelView channelView = channelViews.get(i);
	// if(channelView!=null)
	// {
	// if (channelView.chatChannel != null)
	// channelView.chatChannel.setChannelView(null);
	// channelView.init();
	// }
	// }
	// }

	private void showCustomChannelSetting()
	{
		custom_setting_layout.setVisibility(View.VISIBLE);
		activity.hideSoftKeyBoard();
		hideEmojPanel();
		replyField.clearFocus();
		isSettingCustomChannel = true;
		relativeLayout1.setVisibility(View.GONE);
		random_chat_destory_tip_layout.setVisibility(View.GONE);
		if (!SwitchUtils.mqttEnable)
			WebSocketManager.getInstance().setRandomChatRoomDestoryed(false);
		else
			MqttManager.getInstance().setRandomChatRoomDestoryed(false);
		custom_settting_btn_layout.setVisibility(View.VISIBLE);
		if (channelViews != null && channelViews.size() >= TAB_CUSTOM + 1)
		{
			ChannelView channelView = channelViews.get(TAB_CUSTOM);
			if (channelView != null)
			{
				refreshCustomChannelImage(channelView.chatChannel);
				if (customChannelListAdapter != null)
					customChannelListAdapter.notifyDataSetWithSort();
			}
		}
	}

	public void hideCustomChannelSetting(boolean showCustomTop)
	{
		custom_setting_layout.setVisibility(View.GONE);
		boolean hasCustomData = (friendList != null && friendList.size() > 0)
				|| (chatroomChannelList != null && chatroomChannelList.size() > 0);
		boolean isJoiningRandomRoom = SwitchUtils.mqttEnable ? MqttManager.getInstance().isJoiningRandomRoom() : WebSocketManager.getInstance().isJoiningRandomRoom();
		custom_chat_tip_layout
				.setVisibility(getCurrentChannel().tab == TAB_CUSTOM
						&& !isJoiningRandomRoom
						&& (StringUtils.isEmpty(customChannelId) && (hasCustomData || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
		random_chat_tip_layout.setVisibility(getCurrentChannel().tab == TAB_CUSTOM && !WebSocketManager.getInstance().isJoiningRandomRoom()
				&& StringUtils.isEmpty(customChannelId) && !hasCustomData && ChatServiceController.randomChatEnable ? View.VISIBLE : View.GONE);
		isSettingCustomChannel = false;
		if (StringUtils.isEmpty(customChannelId))
			relativeLayout1.setVisibility(View.GONE);
		else
			relativeLayout1.setVisibility(View.VISIBLE);
		custom_settting_btn_layout.setVisibility(View.GONE);
		if (showCustomTop)
			custom_channel_setting_layout.setVisibility(StringUtils.isNotEmpty(customChannelId) ? View.VISIBLE : View.GONE);

		horn_scroll_layout.setVisibility(View.GONE);
		battle_horn_scroll_layout.setVisibility(View.GONE);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void renderList()
	{
		if (channelViews == null)
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		for (int i = 0; i < channelViews.size(); i++)
		{
			final ChannelView channelView = channelViews.get(i);
			if (channelView == null)
				continue;

			List<MsgItem> msgList = null;
			if (channelView.chatChannel != null)
				msgList = channelView.chatChannel.msgList;
			if (msgList == null)
				msgList = new ArrayList<MsgItem>();

			if (msgList != null)
			{
				if (channelView.chatChannel != null)
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelView.chatChannel channelID:", channelView.chatChannel.channelID);
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "msgList size", msgList.size());
				MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
				channelView.setMessagesAdapter(adapter);
			}
			if ((ChatServiceController.isInChat() && i != 0) || !ChatServiceController.isInChat())
				XiaoMiToolManager.getInstance().addAudioListener(channelView.getMessagesAdapter());

			if (i == TAB_ALLIANCE_SYS - 1)
			{
				alliance_sys_msg_listview.setAdapter(channelView.getMessagesAdapter());
				channelView.pullDownToLoadListView = alliance_sys_list;
				channelView.messagesListView = alliance_sys_msg_listview;
			}
			else if (i == TAB_COUNTRY_SYS - 1)
			{
				country_sys_msg_listview.setAdapter(channelView.getMessagesAdapter());
				channelView.pullDownToLoadListView = country_sys_list;
				channelView.messagesListView = country_sys_msg_listview;
			}
			else
			{
				PullDownToLoadMoreView pullDownToLoadListView = new PullDownToLoadMoreView(activity);
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
				messagesListView.setAdapter(channelView.getMessagesAdapter());
				messagesListView.setOnScrollListener(mOnScrollListener);
				messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
				messagesListView.setKeepScreenOn(true);

				pullDownToLoadListView.addView(messagesListView);
				channelView.pullDownToLoadListView = pullDownToLoadListView;
				channelView.messagesListView = messagesListView;

				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "i", i);

				if (i == TAB_ALLIANCE)
				{
					if (ChatServiceController.allianceSysChannelEnable)
					{
						alliance_msg_layout.removeAllViews();
						alliance_msg_layout.addView(pullDownToLoadListView);
					}
					else
						messagesListFrameLayout.addView(pullDownToLoadListView);
				}
				else if (i == TAB_COUNTRY && ChatServiceController.isInChat())
				{
					if (ChatServiceController.countrySysChannelEnable)
					{
						country_msg_layout.removeAllViews();
						country_msg_layout.addView(pullDownToLoadListView);
					}
					else
						messagesListFrameLayout.addView(pullDownToLoadListView);
				}
				else
				{
					System.out.println("messagesListFrameLayout.addView(pullDownToLoadListView)");
					messagesListFrameLayout.addView(pullDownToLoadListView);
				}
			}

			if (channelView.chatChannel != null && channelView.chatChannel.lastPosition.x != -1 && rememberPosition)
			{
				channelView.messagesListView.setSelectionFromTop(channelView.chatChannel.lastPosition.x,
						channelView.chatChannel.lastPosition.y);
			}
			else
			{
				if (channelView.getMessagesAdapter() != null)
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
			}

		}
		if (lazyLoading)
		{
			System.out.println("lazyLoading refreshTab");
			refreshTab();
			lazyLoading = false;
		}
		activity.hideProgressBar();
	}

	protected void refreshTab()
	{
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			showTab(TAB_ALLIANCE);
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			if (ChatServiceController.getInstance().needCrossServerBattleChat())
			{
				showTab(TAB_BATTLE_FIELD);
				countryExchangeFlag = 1;
			}
			else
			{
				showTab(TAB_COUNTRY);
				countryExchangeFlag = 0;
			}
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
		{
			showTab(TAB_CUSTOM);
		}
		else if (ChatServiceController.isInMailDialog())
		{
			showTab(TAB_MAIL);
		}
		refreshWordCount();
	}

	public void showNewSystemAnimation()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("showNewSystemAnimation");
					country_sys_top_icon.setImageResource(R.drawable.new_system_tip_anim);
					startNewSystemAnimation();
				}
			});
		}
	}

	public void hideNewSystemAnimation()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("hideNewSystemAnimation");
					stopNewSystemAnimation();
					country_sys_top_icon.setImageResource(R.drawable.alliance_sys_top_btn);
				}
			});
		}
	}

	public void startNewSystemAnimation()
	{
		System.out.println("startNewSystemAnimation 0");
		Drawable drawable = country_sys_top_icon.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			System.out.println("startNewSystemAnimation 2");
			sys_new_tip_animation = (AnimationDrawable) country_sys_top_icon.getDrawable();
			if (sys_new_tip_animation != null && !sys_new_tip_animation.isRunning())
				sys_new_tip_animation.start();
		}
	}

	private CopyOnWriteArrayList<InputAtContent> getCurInputAtContent()
	{
		CopyOnWriteArrayList<InputAtContent> inputAtList = null;
		if (ChatServiceController.isInCountryOrSysCountryChannel())
		{
			if (countryInputAtList == null)
				countryInputAtList = new CopyOnWriteArrayList<InputAtContent>();
			inputAtList = countryInputAtList;
		}
		else if (ChatServiceController.isInAllianceOrSysAllianceChannel())
		{
			if (allianceInputAtList == null)
				allianceInputAtList = new CopyOnWriteArrayList<InputAtContent>();
			inputAtList = allianceInputAtList;
		}
		return inputAtList;
	}

	public void stopNewSystemAnimation()
	{
		System.out.println("stopNewSystemAnimation 0");
		Drawable drawable = country_sys_top_icon.getDrawable();
		if (drawable != null && drawable instanceof AnimationDrawable)
		{
			System.out.println("stopNewSystemAnimation 2");
			sys_new_tip_animation = (AnimationDrawable) country_sys_top_icon.getDrawable();
			if (sys_new_tip_animation != null && sys_new_tip_animation.isRunning())
				sys_new_tip_animation.stop();
		}
	}

	private void initAtAdapter(List<UserInfo> userList, int channelType)
	{

		FilteredArrayAdapter<UserInfo> adapter = new FilteredArrayAdapter<UserInfo>(activity, R.layout.cs__autocomplete_item, userList)
		{

			@Override
			protected boolean isFilterCondition(UserInfo item, String constraint)
			{

				if (ChatServiceController.isInCountryOrAllianceChannel() && item != null
						&& !item.uid.equals(UserManager.getInstance().getCurrentUserId())
						&& ((StringUtils.isNotEmpty(item.userName) || item.isNpc())))
				{
					CopyOnWriteArrayList<InputAtContent> inputAtList = getCurInputAtContent();
					if (inputAtList != null)
					{
						for (InputAtContent at : inputAtList)
						{
							if (at != null && StringUtils.isNotEmpty(at.getAtNameText()))
							{
								if (at.isNpcAt() && ChatServiceController.isInCountryOrSysCountryChannel())
								{
									if (item.isNpc())
										return false;
								}
								else
								{
									if (at.getAtNameText().equals("@" + item.userName + " "))
										return false;
								}
							}

						}
					}

					String prefixString = constraint.toLowerCase();
					if (StringUtils.isNotEmpty(prefixString))
					{
						if (inputAtList != null && inputAtList.size() > 0)
						{
							for (InputAtContent at : inputAtList)
							{
								if (at != null)
								{
									if (StringUtils.isNotEmpty(at.getAtNameText()))
									{
										if (at.isNpcAt())
										{
											String atName = "@" + LanguageManager.getNPCName() + " ";
											prefixString = prefixString.replaceFirst(atName, "");
										}
										else
											prefixString = prefixString.replaceFirst(at.getAtNameText(), "");
									}
								}
							}
						}

						if (!prefixString.contains("@"))
							return false;
						else
						{
							prefixString = prefixString.substring(prefixString.lastIndexOf("@"));
							if (prefixString.length() >= 1)
							{
								if (ChatServiceController.isInCountryOrSysCountryChannel() && item.isNpc())
									return true;
								String actualText = prefixString.substring(1);

								List<String> validLoadUidList = new ArrayList<String>();

								if (ChatServiceController.isInAllianceOrSysAllianceChannel())
								{
									List<String> currentLoadedUid = UserManager.getInstance().getCurrentAllianceLoadUid();
									Set<String> keySet = UserManager.getInstance().getAllianceMemberUids();
									if (keySet != null)
									{
										for (String uid : currentLoadedUid)
										{
											if (StringUtils.isNotEmpty(uid) && keySet.contains(uid) && !uid.equals(UserManager.getInstance().getCurrentUserId()))
												validLoadUidList.add(uid);
										}
									}
								}

								if (prefixString.equals("@") && StringUtils.isNotEmpty(item.userName) && !item.userName.equals(item.uid))
								{
									if (ChatServiceController.isInAllianceOrSysAllianceChannel())
									{
										if (validLoadUidList != null && validLoadUidList.size() > 0)
										{
											refreshAtListHighLight("");
											if (validLoadUidList.contains(item.uid))
												return true;
										}
									}
									else if (ChatServiceController.isInCountryOrSysCountryChannel())
									{
										refreshAtListHighLight("");
										return true;
									}
								}
								else
								{
									if (prefixString.startsWith("@") && StringUtils.isNotEmpty(item.userName) && !item.userName.equals(item.uid)
											&& (item.userName.contains(actualText)
													|| (StringUtils.isNotEmpty(item.getLowcaseName()) && item.getLowcaseName().contains(actualText))
													|| (StringUtils.isNotEmpty(item.getUppercaseName()) && item.getUppercaseName().contains(actualText))))
									{
										refreshAtListHighLight(actualText);
										return true;
									}
								}
							}
						}
					}
				}
				return false;
			}
		};
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			countryAutoCompleteAdapter = adapter;
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			allianceAutoCompleteAdapter = adapter;
	}

	private boolean showed = false;

	private void showAutoTranslateSettingDialog()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if (ConfigManager.isAutoTranslateEnable() && !showed
				&& (config == null || (config != null && !config.isAutoTranslateSettingShowed())))
		{
			MenuController.showAutoTranslateSettingConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATE_SETTING));
			if (config == null)
				config = new LocalConfig();
			config.setAutoTranslateSettingShowed(true);
			ConfigManager.getInstance().setLocalConfig(config);
			ConfigManager.getInstance().saveLocalConfig();
		}
	}

	private boolean randomTipShowed = false;

	private void showRandomSettingTipDialog()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if (ChatServiceController.randomChatEnable && !randomTipShowed
				&& (config == null || (config != null && !config.isRandomChatTipShowed())))
		{
			MenuController.showOldContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_RANDOM_CHAT_SETTING_TIP));
			if (config == null)
				config = new LocalConfig();
			config.setRandomChatTipShowed(true);
			ConfigManager.getInstance().setLocalConfig(config);
			ConfigManager.getInstance().saveLocalConfig();
		}
	}

	protected void onNetworkConnectionChanged()
	{
		if (voice_rec_button_layout.getVisibility() == View.VISIBLE)
		{
			// 在切换tab后，可能会被重刷成alpha为1；在连接恢复后，可能因为不可见而没有设为enable；还需要增加调用
			// refreshAudioButton();
		}
		else
		{
			refreshSendButton();
		}
	}

	private boolean isWSConnectionAvailable()
	{
		boolean result = true;
		ChatChannel channel = null;
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
			channel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);
		else
			channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel == null)
		{
			result = false;
		}
		else if ((WebSocketManager.isRecieveFromWebSocket(channel.channelType) || WebSocketManager.isSendFromWebSocket(channel.channelType)) || channel.isNearbyChannel())
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
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel == null)
			return;

		if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType) && !channel.isNearbyChannel())
		{
			if (channel.canLoadAllNew())
			{
				getCurrentChannel().setLoadingStart(true);
				loadMoreCount = 0;
				channel.isLoadingAllNew = true;
				channel.hasLoadingAllNew = true;
				ChannelManager.getInstance().loadAllNew(channel);

				refreshToolTip();
			}
		}
		else
		{
			if (channel.hasAtMeMsg())
			{
				getCurrentChannel().setLoadingStart(true);
				loadMoreCount = 0;
				// channel.hasLoadedEarliestAtMeMsg = true;
				channel.isLoadingEarliestAtMeMsg = true;
				int selectionTarget = ChannelManager.getInstance().loadLatestAtMsg(channel);
				if (selectionTarget != -1)
				{
					ListView listView = getCurrentChannel().messagesListView;
					if (listView != null)
					{
						listView.setSelectionFromTop(selectionTarget, 0);
					}
				}
				channel.isLoadingEarliestAtMeMsg = false;
				refreshToolTip();
				LogUtil.trackATplayers("at_receive_click");
			}
			else if (channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
			{
				channel.wsNewMsgCount = 0;
				updateListPositionForOldMsg(channel.channelType, channel.channelID, 0, false);
			}
		}
	}

	public void refreshToolTip()
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		// 未加入联盟时，channel不存在
		if (channel == null || isInMail())
		{
			showToolTip(false);
			return;
		}

		if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType) && !channel.isNearbyChannel())
		{
			refreshToolTipInGameServer(channel);
		}
		else
		{
			refreshToolTipInWSServer(channel);
		}
	}

	private void refreshToolTipInGameServer(ChatChannel channel)
	{
		if (channel != null && channel.canLoadAllNew())
		{
			String newMsgCount = channel.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.getNewMsgCount() + "" : ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
			tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
			showToolTip(true);
		}
		else
		{
			showToolTip(false);
		}
	}

	private void refreshToolTipInWSServer(ChatChannel channel)
	{
		// 第一次加载历史消息后，重置channel.wsNewMsgCount
		// TODO 应该改为显示到第一条消息后重置
		// if(channel.wsNewMsgCount > 0 && channel.msgList.size() !=
		// channel.wsNewMsgCount)
		// {
		// channel.wsNewMsgCount = 0;
		// }

		boolean hasAtMeMsg = channel.hasAtMeMsg();
		if (channel != null && (hasAtMeMsg || channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT))
		{
			if (hasAtMeMsg)
			{
				tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_AT_ME));
			}
			else
			{
				String newMsgCount = channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.wsNewMsgCount + "" : ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
				tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
			}
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
		if (ChatServiceController.isInAllianceOrSysAllianceChannel() && allianceInputAtList != null)
			allianceInputAtList.clear();
		if (ChatServiceController.isInCountryOrSysCountryChannel() && countryInputAtList != null)
			countryInputAtList.clear();
	}

	private boolean isHornUI;

	private void refreshBottomUI(boolean isChecked)
	{
		if (!isChecked)
			ChatServiceController.isHornItemUsed = false;
		isHornUI = isChecked
				&& (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY || ChatServiceController
						.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				&& ConfigManager.enableChatHorn;
		String background = "btn_chat_send";
		String bottomBg = "chuzheng_frame02";
		String lineBg = "line_grey02";
		if (isHornUI)
		{
			background = "btn_chat_send_horn";
			bottomBg = "bottom_bg";
			lineBg = "line_brown";
			hideEmojBtn();
		}
		else
		{
			if (ChatServiceController.canUserEmojPanel())
				emoj_checkbox.setVisibility(View.VISIBLE);
			if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			{
				background = "btn_battle_chat_send";
				bottomBg = "battle_bottom_bg";
				lineBg = "line_battle_bottom";
			}
		}

		String inputBg = isHornUI ? "text_field_horn" : "text_field_bg2";
		addReply.setBackgroundResource(ResUtil.getId(activity, "drawable", background));
		if (isHornUI)
		{
			relativeLayout1.setBackgroundColor(0xffffcb64);
		}
		else
		{
			relativeLayout1.setBackgroundResource(ResUtil.getId(activity, "drawable", bottomBg));
		}
		replyField.setBackgroundResource(ResUtil.getId(activity, "drawable", inputBg));
		// horn_tip_layout.setVisibility(isHornUI ? View.VISIBLE : View.GONE);
		// imageView1.setVisibility(isHornUI ? View.GONE : View.VISIBLE);
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

				ChatServiceController.doHostAction("joinAllianceBtnClick", "", "", "", true);
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
	private LoadMoreMsgParam getLoadMoreMsgParam(int channelType)
	{
		if (!(channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS))
		{
			return null;
		}
		ChatChannel channel = ChannelManager.getInstance().getChannel(channelType);
		if (channel == null || channel.msgList == null || channel.getChannelView() == null)
		{
			return null;
		}

		if (!WebSocketManager.isRecieveFromWebSocket(channelType) && !channel.isNearbyChannel())
		{
			return getLoadMoreMsgParamByTime(channel);
		}
		else
		{
			return getLoadMoreMsgParamByTime(channel);
		}
	}

	private LoadMoreMsgParam getLoadMoreMsgParamByTime(ChatChannel channel)
	{
		int minTime = channel.getMinCreateTime();
		// 如果用时间，则肯定是webSocket服务，由于时间不连续，没法判断再前面的消息是在db还是server，所以初始化时将新消息全部加载到本地
		Pair<Long, Long> range = DBManager.getInstance().getHistoryTimeRange(channel.getChatTable(), minTime,
				ChannelManager.LOAD_MORE_COUNT);
		if (range != null)
		{
			return new LoadMoreMsgParam(range.first, range.second, false);
		}

		return null;
	}

	private LoadMoreMsgParam getLoadMoreMsgParamBySeqId(ChatChannel channel)
	{
		int viewMinSeqId = channel.getMinSeqId();

		// 不能加载: 没有消息时viewMinSeqId为0，有消息时seqId最小为1
		if (viewMinSeqId <= 1)
		{
			return null;
		}

		// desireMaxSeqId可能等于desireMinSeqId，仅当二者都为1时
		int desireMaxSeqId = DBManager.getInstance().getLoadMoreMaxSeqId(channel.getChatTable(), viewMinSeqId);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "desireMaxSeqId", desireMaxSeqId, "viewMinSeqId", viewMinSeqId);
		int desireMinSeqId = (desireMaxSeqId - 19) > 1 ? (desireMaxSeqId - 19) : 1;

		// 如果desireMaxSeqId在本地db中有，就从db加载（不一定能满20条）
		if (DBManager.getInstance().isMsgExists(channel.getChatTable(), desireMaxSeqId, -1))
		{
			return new LoadMoreMsgParam(desireMinSeqId, desireMaxSeqId, false);
		}

		// 否则，如果在server范围内，从server加载
		// server中seqId连续，可以用交集判断
		Point inter = getIntersection(new Point(channel.serverMinSeqId, channel.serverMaxSeqId), new Point(desireMinSeqId, desireMaxSeqId));
		if (inter != null)
		{
			return new LoadMoreMsgParam(inter.x, inter.y, true);
		}

		// 既不在db，又不在server（再往前的也肯定不在server），则找到db中最早的，加载之
		Point range = DBManager.getInstance().getHistorySeqIdRange(channel.getChatTable(), desireMaxSeqId, ChannelManager.LOAD_MORE_COUNT);
		if (range != null)
		{
			return new LoadMoreMsgParam(range.x, range.y, false);
		}

		return null;
	}

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
		if (getCurrentChannel() == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			// 初始化时showTab肯定会发生，此时getCurrentChannel().getMessagesAdapter()为null
			// LogUtil.trackMessage("checkMessagesAdapter() fail: currentChannel = "
			// + getCurrentChannel() + " messagesAdapter = "
			// + (getCurrentChannel() == null ? "null" :
			// getCurrentChannel().getMessagesAdapter()) + " currentChatType = "
			// + ChatServiceController.getCurrentChannelType() +
			// " chatActivity = " + ChatServiceController.getChatActivity()
			// + " chatFragment = " + ChatServiceController.getChatFragment());
			return false;
		}
		return true;
	}

	private void loadMoreMsg()
	{
		createTimerTask();

		if (!checkMessagesAdapter() || getCurrentChannel() == null)
			return;

		ChatChannel channel = ChannelManager.getInstance().getChannel(getCurrentChannel().channelType);
		// 极少情况下会发生
		if (channel == null)
			return;
		LoadMoreMsgParam loadMoreMsgParam = getLoadMoreMsgParam(channel.channelType);

		if (!getCurrentChannel().getLoadingStart() && loadMoreMsgParam != null)
		{
			LogUtil.trackPageView("LoadMoreMsg");
			getCurrentChannel().setLoadingStart(true);
			// 可能有异常 getCount() on a null object reference
			loadMoreCount = 0;
			System.out.println("");
			channel.isLoadingAllNew = false;
			channel.isLoadingEarliestAtMeMsg = false;
			if (loadMoreMsgParam.fetchFromServer)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从server加载消息");
				JniController.getInstance().excuteJNIVoidMethod(
						"getMsgBySeqId",
						new Object[] {
								Integer.valueOf(loadMoreMsgParam.requestMinSeqId),
								Integer.valueOf(loadMoreMsgParam.requestMaxSeqId),
								Integer.valueOf(channel.channelType),
								channel.channelID });
			}
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从db加载消息");
				if (!loadMoreMsgParam.useTime)
				{
					ChannelManager.getInstance().loadMoreMsgFromDB(channel, loadMoreMsgParam.requestMinSeqId,
							loadMoreMsgParam.requestMaxSeqId, -1, false);
				}
				else
				{
					ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
				}
			}
		}

	}

	private void loadMoreMail()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		if (!getCurrentChannel().getLoadingStart() && getCurrentChannel().hasMoreData)
		{
			LogUtil.trackPageView("LoadMoreMail");
			if (ChatServiceController.isNewMailListEnable)
			{
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
				if (channel != null)
					ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
			}
			else
			{
				getCurrentChannel().setLoadingStart(true);
				loadMoreCount = 0;

				JniController.getInstance().excuteJNIVoidMethod(
						"requestMoreMail",
						new Object[] {
								UserManager.getInstance().getCurrentMail().opponentUid,
								UserManager.getInstance().getCurrentMail().mailUid,
								Integer.valueOf(getCurrentChannel().getMessageCount()) });
			}
		}
	}

	/**
	 * 时机：各个参数变化时、初始化时 server数据变化时：GetNewMsg返回时 view数据变化时：获取到新消息时
	 */
	public void refreshHasMoreData(ChannelView channelView)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (channelView == null)
			return;
		if (!isInMail())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "非邮件");
			if (ChannelManager.getInstance().isGetingNewMsg)
			{
				channelView.hasMoreData = false;
			}
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelView.channelType", channelView.channelType);
				if (channelView.channelType == -1 || channelView.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
					channelView.hasMoreData = false;
				else
					channelView.hasMoreData = getLoadMoreMsgParam(channelView.channelType) != null;
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelView.hasMoreData", channelView.hasMoreData);
			}
		}
		else if (ChatServiceController.isNewMailListEnable)
		{
			if (channelView.chatChannel == null)
			{
				channelView.hasMoreData = false;
			}
			else
			{
				List<MsgItem> dbUserMails = DBManager.getInstance().getMsgsByTime(channelView.chatChannel.getChatTable(), channelView.chatChannel.getMinCreateTime(), 1);
				channelView.hasMoreData = dbUserMails.size() > 0;
			}
		}
	}

	private boolean isInMail()
	{
		return (getCurrentChannel().tab == TAB_CUSTOM && customChannelType == DBDefinition.CHANNEL_TYPE_USER)
				|| getCurrentChannel().tab == TAB_MAIL;
	}

	public static String chatStatus = "";

	public static void setConnectionStatus(final String title)
	{
		chatStatus = title;
		if (!ChatServiceController.isInMailDialog() && ChatServiceController.getCurrentActivity() != null)
		{
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{
				public void run()
				{
					if (ChatServiceController.getChatFragment() != null)
					{
						if (StringUtils.isNotEmpty(title))
						{
							ChatServiceController.getChatFragment().getTitleLabel().setText(title);
						}
						else
						{
							ChatServiceController.getChatFragment().getTitleLabel()
									.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
						}
						ChatServiceController.getChatFragment().onNetworkConnectionChanged();
					}
				}
			});
		}
	}

	private ListViewLoadListener mListViewLoadListener = new ListViewLoadListener()
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
			if (getCurrentChannel() == null
					|| getCurrentChannel().messagesListView == null)
				return false;
			ListView listView = getCurrentChannel().messagesListView;

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
			if (getCurrentChannel() == null
					|| getCurrentChannel().messagesListView == null)
				return false;
			ListView listView = getCurrentChannel().messagesListView;
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
		final ChannelView channelView = getCurrentChannelView();
		if (channelView == null)
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
							if (channelView.pullDownToLoadListView != null && channelView.chatChannel != null)
							{
								channelView.pullDownToLoadListView.hideProgressBar();
								resetMoreDataStart(channelView.channelType, channelView.chatChannel.channelID);
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

	private OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener()
	{

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
			{
				if (getCurrentChannel() != null
						&& getCurrentChannel().messagesListView != null)
				{
					View topView = getCurrentChannel().messagesListView
							.getChildAt(getCurrentChannel().messagesListView
									.getFirstVisiblePosition());
					if ((topView != null) && (topView.getTop() == 0)
							&& !getCurrentChannel().getLoadingStart())
					{
						getCurrentChannel().pullDownToLoadListView.startTopScroll();
					}
				}

			}

			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
				ChatServiceController.isListViewFling = true;
			else
				ChatServiceController.isListViewFling = false;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount)
		{
			if (getCurrentChannel() != null
					&& getCurrentChannel().pullDownToLoadListView != null
					&& getCurrentChannel().pullDownToLoadListView.getVisibility() == View.VISIBLE)
			{
				if (getCurrentChannel().hasMoreData)
				{
					if (!getCurrentChannel().getLoadingStart())
					{
						getCurrentChannel().pullDownToLoadListView
								.setAllowPullDownRefersh(false);
					}
					else
					{
						getCurrentChannel().pullDownToLoadListView
								.setAllowPullDownRefersh(true);
					}
				}
				else
				{
					getCurrentChannel().pullDownToLoadListView
							.setAllowPullDownRefersh(true);
				}
			}
		}
	};

	public void onJoinAnnounceInvitationSuccess()
	{
		if (getCountryChannelView() != null)
		{
			// 隐藏noAllianceFrameLayout，点联盟自然会调用
			getCountryChannelView().getMessagesAdapter().onJoinAnnounceInvitationSuccess();
		}
	}

	public int getToastPosY()
	{
		int[] location = { 0, 0 };
		messagesListFrameLayout.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(activity, 5);
	}

	private final int	TAB_COUNTRY				= 0;
	private final int	TAB_ALLIANCE			= 1;
	private final int	TAB_CUSTOM				= 2;
	private final int	TAB_MAIL				= 3;
	private final int	TAB_ALLIANCE_SYS		= 4;
	private final int	TAB_COUNTRY_SYS			= 5;
	private final int	TAB_BATTLE_FIELD		= 6;

	private int			repeatCount				= 0;

	private int			currentTab				= -1;

	private Timer		mNewSysAllianceTimer	= null;

	private void stopNewSysAllianceTimer()
	{
		if (mNewSysAllianceTimer != null)
		{
			mNewSysAllianceTimer.cancel();
			mNewSysAllianceTimer.purge();
		}
	}

	private void startNewSysAllianceTimer()
	{
		if (ChatServiceController.allianceSysChannelEnable && hasNewSystemAllianceMsg)
		{
			stopNewSysAllianceTimer();
			mNewSysAllianceTimer = new Timer();

			TimerTask task = new TimerTask()
			{

				@Override
				public void run()
				{
					if (alliance_sliding_layout != null)
					{
						if (activity != null)
						{
							activity.runOnUiThread(new Runnable()
							{

								@Override
								public void run()
								{
									if (getCurrentChannel() != null && getCurrentChannel().tab == TAB_ALLIANCE)
									{
										alliance_sliding_layout.setPanelState(PanelState.COLLAPSED);
										if (!alliance_sliding_layout.isFirstLayout())
										{
											hasNewSystemAllianceMsg = false;
											setSlideLayoutCollapsed();
											updateAllianceSlideDragLayoutHeight(PanelState.HIDDEN, PanelState.COLLAPSED, true);
										}
									}
								}
							});
						}
					}
				}
			};

			mNewSysAllianceTimer.schedule(task, 2000);
		}
	}

	private Timer mNewSysCountryTimer = null;

	private void stopNewSysCountryTimer()
	{
		if (mNewSysCountryTimer != null)
		{
			mNewSysCountryTimer.cancel();
			mNewSysCountryTimer.purge();
		}
	}

	private void startNewSysCountryTimer()
	{
		if (ChatServiceController.countrySysChannelEnable && hasNewSystemCountryMsg)
		{
			stopNewSysCountryTimer();
			mNewSysCountryTimer = new Timer();

			TimerTask task = new TimerTask()
			{

				@Override
				public void run()
				{
					if (country_sliding_layout != null)
					{
						if (activity != null)
						{
							activity.runOnUiThread(new Runnable()
							{

								@Override
								public void run()
								{
									if (getCurrentChannel() != null && getCurrentChannel().tab == TAB_COUNTRY)
									{
										country_sliding_layout.setPanelState(PanelState.COLLAPSED);
										if (!country_sliding_layout.isFirstLayout())
										{
											hasNewSystemCountryMsg = false;
											setCountrySlideLayoutCollapsed();
											updateCountrySlideDragLayoutHeight(PanelState.HIDDEN, PanelState.COLLAPSED, true);
										}
									}
								}
							});
						}
					}
				}
			};

			mNewSysCountryTimer.schedule(task, 2000);
		}
	}

	private void showTab(int tab)
	{
		System.out.println("showTab tab:" + tab);
		if (currentTab < 3 || currentTab == TAB_BATTLE_FIELD)
		{
			saveDraft();
		}

		if (currentTab == TAB_COUNTRY && tab == TAB_BATTLE_FIELD)
			LogUtil.trackAction("click_from_country_to_battle");
		else if (currentTab == TAB_BATTLE_FIELD && tab == TAB_COUNTRY)
			LogUtil.trackAction("click_from_battle_to_country");

		if (currentTab != -1 && currentTab != tab)
		{
			activity.hideSoftKeyBoard();
			hideEmojPanel();
			replyField.clearFocus();
		}

		boolean hasCustomData = (friendList != null && friendList.size() > 0)
				|| (chatroomChannelList != null && chatroomChannelList.size() > 0);

		currentTab = tab;
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, tab == TAB_ALLIANCE);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCustom, tab == TAB_CUSTOM);

		if (currentTab == TAB_MAIL || (currentTab == TAB_BATTLE_FIELD && ChatServiceController.canEnterDragonObserverRoom()))
		{
			buttonsLinearLayout.setVisibility(View.GONE);
		}
		else
		{
			buttonsLinearLayout.setVisibility(View.VISIBLE);
			imageView2.setVisibility(View.VISIBLE);
		}

		boolean isInAlliance = false;
		// 少量异常 Attempt to read from field 'java.lang.String
		// com.elex.chatservice.model.UserInfo.allianceId' on a null object
		// reference
		if (UserManager.getInstance().getCurrentUser() != null)
		{
			isInAlliance = StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId) ? false : true;
		}

		if (channelViews != null)
		{
			for (int i = 0; i < channelViews.size(); i++)
			{
				ChannelView channelView = channelViews.get(i);
				if (channelView != null)
				{
					if (tab == channelView.tab)
					{
						if ((tab == TAB_ALLIANCE && !isInAlliance)
								|| (tab == TAB_CUSTOM && (StringUtils.isEmpty(customChannelId) || (!hasCustomData && !ChatServiceController.randomChatEnable)))
								|| (tab == TAB_COUNTRY && !ChatServiceController.isBattleChatEnable && ChatServiceController
										.isInDragonSencen())
								|| (tab == TAB_BATTLE_FIELD && ChatServiceController.canEnterDragonObserverRoom()
										&& !(SwitchUtils.mqttEnable ? MqttManager.isInDragonObserverRoom() : WebSocketManager.isInDragonObserverRoom())))
							channelView.setVisibility(View.GONE);
						else
							channelView.setVisibility(tab == channelView.tab ? View.VISIBLE : View.GONE);
					}
					else
					{
						channelView.setVisibility(View.GONE);
					}
					// if((tab == TAB_ALLIANCE && !isInAlliance)
					// || (tab == TAB_CUSTOM &&
					// (StringUtils.isEmpty(customChannelId)
					// || (!hasCustomData &&
					// ChatServiceController.randomChatEnable)))
					// || (tab == TAB_COUNTRY &&
					// !ChatServiceController.isBattleChatEnable &&
					// ChatServiceController.isInDragonSencen()))
					// channelView.setVisibility(View.GONE);
					// else
					// channelView.setVisibility(tab == channelView.tab ?
					// View.VISIBLE : View.GONE);
				}
			}
			if (channelViews.size() > TAB_ALLIANCE_SYS - 1 && ChatServiceController.allianceSysChannelEnable)
			{
				ChannelView allianceSysChannelView = channelViews.get(TAB_ALLIANCE_SYS - 1);
				if (allianceSysChannelView != null && allianceSysChannelView.getVisibility() != View.VISIBLE)
					allianceSysChannelView.setVisibility(View.VISIBLE);
				if (allianceSysChannelView != null && allianceSysChannelView.chatChannel != null
						&& allianceSysChannelView.chatChannel.msgList != null && allianceSysChannelView.chatChannel.msgList.size() > 0)
					alliance_null_sys_tip.setVisibility(View.GONE);
				else
					alliance_null_sys_tip.setVisibility(View.VISIBLE);
			}

			if (channelViews.size() > TAB_COUNTRY_SYS - 1 && ChatServiceController.countrySysChannelEnable)
			{
				ChannelView countrySysChannelView = channelViews.get(TAB_COUNTRY_SYS - 1);
				if (countrySysChannelView != null && countrySysChannelView.getVisibility() != View.VISIBLE)
					countrySysChannelView.setVisibility(View.VISIBLE);
				if (countrySysChannelView != null && countrySysChannelView.chatChannel != null
						&& countrySysChannelView.chatChannel.msgList != null && countrySysChannelView.chatChannel.msgList.size() > 0)
					country_null_sys_tip.setVisibility(View.GONE);
				else
					country_null_sys_tip.setVisibility(View.VISIBLE);
			}
		}

		if (tab == TAB_COUNTRY)
		{
			country_channel_name.setText(ChatServiceController.originalServerName);
		}
		else if (tab == TAB_BATTLE_FIELD)
		{
			if (ChatServiceController.isInAncientSencen())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_ANCIENT));
			else if (ChatServiceController.isInDragonSencen())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_DRAGON));
			else if (ChatServiceController.getInstance().isInKingdomBattleField())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_KINGDOM));
			else if (ChatServiceController.getInstance().needShowBattleTipLayout())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD));
			else if (ChatServiceController.canEnterDragonObserverRoom())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.DRAGON_OBSERVER_CHAT_ROOM));
		}

		alliance_sliding_layout.setVisibility((tab == TAB_ALLIANCE && isInAlliance) ? View.VISIBLE : View.GONE);
		country_sliding_layout.setVisibility(tab == TAB_COUNTRY ? View.VISIBLE : View.GONE);
		country_sys_top_icon.setVisibility(tab == TAB_COUNTRY && ChatServiceController.countrySysChannelEnable ? View.VISIBLE : View.GONE);
		horn_checkbox.setVisibility(((tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) && ConfigManager.enableChatHorn) ? View.VISIBLE : View.GONE);
		custom_channel_setting_layout
				.setVisibility((tab == TAB_CUSTOM && !isSettingCustomChannel && StringUtils.isNotEmpty(customChannelId)
						&& (hasCustomData || (!hasCustomData && ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
		country_exchange_layout.setVisibility((ChatServiceController.getInstance().needShowBattleFieldChannel() || ChatServiceController.countrySysChannelEnable)
				&& (tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) ? View.VISIBLE : View.GONE);
		country_exchange_btn.setVisibility(ChatServiceController.getInstance().needShowBattleFieldChannel() && !ChatServiceController.canEnterDragonObserverRoom()
				&& (tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) ? View.VISIBLE : View.GONE);
		alliance_sys_top_layout
				.setVisibility((tab == TAB_ALLIANCE && isInAlliance && ChatServiceController.allianceSysChannelEnable) ? View.VISIBLE : View.GONE);
		custom_setting_layout.setVisibility(tab == TAB_CUSTOM && isSettingCustomChannel ? View.VISIBLE : View.GONE);
		noAllianceFrameLayout.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.VISIBLE : View.GONE);
		custom_chat_tip_layout
				.setVisibility(tab == TAB_CUSTOM
						&& (StringUtils.isEmpty(customChannelId) && !WebSocketManager.getInstance().isRandomChatRoomDestoryed()
								&& (hasCustomData || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
		random_chat_destory_tip_layout.setVisibility(tab == TAB_CUSTOM && StringUtils.isEmpty(customChannelId)
				&& WebSocketManager.getInstance().isRandomChatRoomDestoryed() && ChatServiceController.randomChatEnable ? View.VISIBLE : View.GONE);
		random_chat_tip_layout
				.setVisibility(tab == TAB_CUSTOM && StringUtils.isEmpty(customChannelId)
						&& !WebSocketManager.getInstance().isRandomChatRoomDestoryed() && !hasCustomData
						&& ChatServiceController.randomChatEnable ? View.VISIBLE : View.GONE);
		random_room_join_tip_layout.setVisibility(View.GONE);
		battle_field_tip_layout
				.setVisibility(tab == TAB_BATTLE_FIELD && (ChatServiceController.getInstance().needShowBattleTipLayout()
						|| ChatServiceController.getInstance().needShowDragonObserverTipLayout()) ? View.VISIBLE : View.GONE);
		if (tab == TAB_BATTLE_FIELD)
			refreshDragonJoinStatus();
		custom_settting_btn_layout.setVisibility(tab == TAB_CUSTOM && isSettingCustomChannel ? View.VISIBLE : View.GONE);
		hs__dragon_chat_tip_layout.setVisibility((tab == TAB_COUNTRY && !ChatServiceController.isBattleChatEnable && ChatServiceController
				.isInDragonSencen()) ? View.VISIBLE : View.GONE);
		relativeLayout1
				.setVisibility(hs__dragon_chat_tip_layout.getVisibility() == View.VISIBLE
						|| (tab == TAB_ALLIANCE && !isInAlliance)
						|| (tab == TAB_BATTLE_FIELD && (ChatServiceController.getInstance().needShowBattleTipLayout()
								|| ChatServiceController.getInstance().needShowDragonObserverTipLayout()))
						|| (tab == TAB_CUSTOM
								&& (StringUtils.isEmpty(customChannelId) || isSettingCustomChannel || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.GONE : View.VISIBLE);

		LocalConfig config = ConfigManager.getInstance().getLocalConfig();

		if (country_sys_top_icon.getVisibility() == View.VISIBLE)
		{
			ChatChannel countrySysChannel = ChannelManager.getInstance().getCountrySysChannel();
			if (countrySysChannel != null && countrySysChannel.hasUnreadCountrySysMsg())
				showNewSystemAnimation();
			else
				hideNewSystemAnimation();
		}

		if (country_exchange_layout.getVisibility() == View.VISIBLE && country_exchange_btn.getVisibility() == View.VISIBLE
				&& (config == null || (config != null && !config.isBattleChannelShowed())))
		{
			if (config != null && !config.isBattleChannelShowed())
			{
				config.setBattleChannelShowed(true);
				ConfigManager.getInstance().saveLocalConfig();
			}
			else if (config == null)
			{
				config = new LocalConfig();
				config.setBattleChannelShowed(true);
				ConfigManager.getInstance().setLocalConfig(config);
				ConfigManager.getInstance().saveLocalConfig();
			}

			ScaleAnimation animation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(500);
			animation.setRepeatCount(5);
			animation.setRepeatMode(Animation.REVERSE);
			country_exchange_btn.startAnimation(animation);
		}

		if (tab == TAB_ALLIANCE && !isInAlliance && ConfigManager.getInstance().isFirstJoinAlliance && !isJoinAlliancePopupShowing)
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

		if (tab == TAB_COUNTRY || tab == TAB_COUNTRY_SYS)
		{
			ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_COUNTRY);
			replyField.setAdapter(countryAutoCompleteAdapter);
			if (ChatServiceController.isHornItemUsed && ConfigManager.enableChatHorn)
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

			if (ChatServiceController.isChatRestrictForLevel())
			{
				replyField.setEnabled(false);
				replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
						"" + ChatServiceController.getChatRestrictLevel()));
			}
			else
			{
				replyField.setHint("");
				replyField.setEnabled(true);
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
				if (channel != null && StringUtils.isNotEmpty(channel.draft))
				{
					replyField.setText(channel.draft);
					try
					{
						replyField.setSelection(channel.draft.length());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					replyField.setText("");
				}
			}
		}
		else
		{
			if (tab != TAB_BATTLE_FIELD && (config == null || (config != null && !config.isAudioUsed())))
				replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_USE));
			else
				replyField.setHint("");
			replyField.setEnabled(true);

			if (tab == TAB_ALLIANCE || tab == TAB_ALLIANCE_SYS)
			{
				ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_ALLIANCE);
				replyField.setAdapter(allianceAutoCompleteAdapter);
			}
			else if (tab == TAB_CUSTOM)
				ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT);
			else if (tab == TAB_BATTLE_FIELD)
			{
				ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
				if (ChatServiceController.isHornItemUsed && ConfigManager.enableChatHorn)
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

			refreshBottomUI(false);

			ChatChannel channel = null;
			if (tab == TAB_CUSTOM)
			{
				if (ChannelManager.isUserChannelType(customChannelType) && StringUtils.isNotEmpty(customChannelId))
					channel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);
				if (isSettingCustomChannel)
					refreshCustomChannelImage(channel);
			}
			else
				channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());

			if (channel != null && StringUtils.isNotEmpty(channel.draft))
			{
				replyField.setText(channel.draft);
				replyField.setSelection(channel.draft.length());
			}
			else
				replyField.setText("");
		}

		refreshInputButton();
		// getShowFriendButton().setVisibility(ChatServiceController.isInMailDialog()
		// || (ChatServiceController.getCurrentChannelType() ==
		// DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT) ? View.GONE : View.VISIBLE);
		// setSelectMemberBtnState();
		if (channelViews == null || channelViews.size() <= 0)
			return;
		ChannelView channelView = null;
		if (tab == TAB_MAIL)
		{
			channelView = channelViews.get(0);
			setChannelViewIndex(0);
		}
		else if (tab == TAB_BATTLE_FIELD)
		{
			if (channelViews.size() > TAB_BATTLE_FIELD - 1)
			{
				channelView = channelViews.get(TAB_BATTLE_FIELD - 1);
				setChannelViewIndex(TAB_BATTLE_FIELD - 1);
			}
		}
		else if (tab == TAB_ALLIANCE_SYS)
		{
			if (channelViews.size() > TAB_ALLIANCE_SYS - 1)
			{
				channelView = channelViews.get(TAB_ALLIANCE_SYS - 1);
				setChannelViewIndex(TAB_ALLIANCE_SYS - 1);
			}
		}
		else if (tab == TAB_COUNTRY_SYS)
		{
			if (channelViews.size() > TAB_COUNTRY_SYS - 1)
			{
				channelView = channelViews.get(TAB_COUNTRY_SYS - 1);
				setChannelViewIndex(TAB_COUNTRY_SYS - 1);
			}
		}
		else if (tab < channelViews.size())
		{
			channelView = channelViews.get(tab);
			setChannelViewIndex(tab);
		}

		if (channelView != null && channelView.getVisibility() == View.VISIBLE)
		{
			refreshToolTip();
			// showAutoSwitchLayout(true);
			if ((tab == TAB_COUNTRY && !hornTextHidden) || (tab == TAB_BATTLE_FIELD && !battleHornTextHidden))
			{
				MsgItem msgItem = ScrollTextManager.getInstance().getNextText(ChatServiceController.getCurrentChannelType());
				if (msgItem != null)
					showHornScrollText(msgItem);
				else
				{
					horn_scroll_layout.setVisibility(View.GONE);
					battle_horn_scroll_layout.setVisibility(View.GONE);
				}
			}
			else
			{
				horn_scroll_layout.setVisibility(View.GONE);
				battle_horn_scroll_layout.setVisibility(View.GONE);
			}

			if (tab == TAB_CUSTOM
					&& (customChannelType == DBDefinition.CHANNEL_TYPE_USER || customChannelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
					&& !isSettingCustomChannel)
			{
				showRandomSettingTipDialog();
			}

			if (tab == TAB_ALLIANCE)
				startNewSysAllianceTimer();
			else if (tab == TAB_COUNTRY)
				startNewSysCountryTimer();
			else
			{
				stopNewSysAllianceTimer();
				stopNewSysCountryTimer();
			}

			if (channelView.chatChannel != null)
			{
				channelView.chatChannel.getTimeNeedShowMsgIndex();
				channelView.chatChannel.markAsRead();
			}
			if (tab != TAB_CUSTOM)
				showAutoTranslateSettingDialog();
		}
		else
		{
			showToolTip(false);
			horn_scroll_layout.setVisibility(View.GONE);
			battle_horn_scroll_layout.setVisibility(View.GONE);
		}
	}

	public void updateAudioHint()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if (ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_COUNTRY
				&& ChatServiceController.getCurrentChannelType() != TAB_BATTLE_FIELD
				&& (config == null || (config != null && !config.isAudioUsed())))
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_USE));
		else
			replyField.setHint("");
	}

	private void updateHornLayoutHeight(RelativeLayout horn_scroll_layout)
	{
		if (horn_scroll_layout != null)
		{
			int length1 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor() + ScaleUtil
					.dip2px(6));
			int length2 = ScaleUtil.dip2px(10);
			FrameLayout.LayoutParams horn_scroll_layoutLayoutParams = (FrameLayout.LayoutParams) horn_scroll_layout.getLayoutParams();

			if (ChatServiceController.getInstance().needShowBattleFieldChannel())
			{
				if (horn_scroll_layoutLayoutParams.topMargin != length1)
				{
					horn_scroll_layoutLayoutParams.topMargin = length1;
					horn_scroll_layout.setLayoutParams(horn_scroll_layoutLayoutParams);
				}
			}
			else
			{
				if (horn_scroll_layoutLayoutParams.topMargin != length2)
				{
					horn_scroll_layoutLayoutParams.topMargin = length2;
					horn_scroll_layout.setLayoutParams(horn_scroll_layoutLayoutParams);
				}
			}
		}
	}

	private void updateAllianceSlideDragLayoutHeight(PanelState previousState, PanelState panelState, boolean isForcus)
	{
		if (alliance_sliding_layout == null || activity == null)
			return;
		if (isForcus
				|| ((previousState == PanelState.COLLAPSED || previousState == PanelState.HIDDEN) && (panelState != PanelState.COLLAPSED
						&& panelState != PanelState.HIDDEN && panelState != panelState.DRAGGING))
				|| ((previousState != PanelState.COLLAPSED && previousState != PanelState.HIDDEN) && (panelState == PanelState.COLLAPSED || panelState == PanelState.HIDDEN))
				|| (previousState == PanelState.DRAGGING && panelState == PanelState.EXPANDED))
		{
			int length = (int) (ScaleUtil.dip2px(activity, 36) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			System.out.println("length:" + length);
			if (panelState == PanelState.COLLAPSED)
			{
				new_alliance_sys_message.setVisibility(View.VISIBLE);
				int msgHeight = new_alliance_sys_message.getHeight();
				int btnHeight = new_alliance_sys_sliding_btn.getHeight();
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) alliance_drag_layout.getLayoutParams();
				int height = length + msgHeight + ScaleUtil.dip2px(23) + btnHeight;
				drag_layoutLayoutParams.height = height;
				alliance_drag_layout.setLayoutParams(drag_layoutLayoutParams);
				alliance_sliding_layout.setPanelHeight(height);
			}
			else
			{
				new_alliance_sys_message.setVisibility(View.GONE);
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) alliance_drag_layout.getLayoutParams();
				drag_layoutLayoutParams.height = (int) (ScaleUtil.dip2px(20) * ConfigManager.scaleRatio * activity
						.getScreenCorrectionFactor()) + new_alliance_sys_sliding_btn.getHeight();
				alliance_drag_layout.setLayoutParams(drag_layoutLayoutParams);

			}
		}
	}

	private void updateCountrySlideDragLayoutHeight(PanelState previousState, PanelState panelState, boolean isForcus)
	{
		if (country_sliding_layout == null)
			return;

        // BUG FIXED:
        //Fatal Exception: java.lang.NullPointerException
        //Attempt to invoke virtual method 'double com.elex.chatservice.view.actionbar.MyActionBarActivity.getScreenCorrectionFactor()' on a null object reference
        if (activity == null)
            return;

		if (isForcus
				|| ((previousState == PanelState.COLLAPSED || previousState == PanelState.HIDDEN) && (panelState != PanelState.COLLAPSED
						&& panelState != PanelState.HIDDEN && panelState != panelState.DRAGGING))
				|| ((previousState != PanelState.COLLAPSED && previousState != PanelState.HIDDEN) && (panelState == PanelState.COLLAPSED || panelState == PanelState.HIDDEN))
				|| (previousState == PanelState.DRAGGING && panelState == PanelState.EXPANDED))
		{
			int length = (int) (ScaleUtil.dip2px(activity, 36) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (panelState == PanelState.COLLAPSED)
			{
				new_country_sys_message.setVisibility(View.VISIBLE);
				int msgHeight = new_country_sys_message.getHeight();
				int btnHeight = new_country_sys_sliding_btn.getHeight();
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) country_drag_layout.getLayoutParams();
				int height = length + msgHeight + ScaleUtil.dip2px(23) + btnHeight;
				drag_layoutLayoutParams.height = height;
				country_drag_layout.setLayoutParams(drag_layoutLayoutParams);
				country_sliding_layout.setPanelHeight(height);
			}
			else
			{
				new_country_sys_message.setVisibility(View.GONE);
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) country_drag_layout.getLayoutParams();
				drag_layoutLayoutParams.height = (int) (ScaleUtil.dip2px(20) * ConfigManager.scaleRatio * activity
						.getScreenCorrectionFactor()) + new_country_sys_sliding_btn.getHeight();
				country_drag_layout.setLayoutParams(drag_layoutLayoutParams);

			}
		}
	}

	private int previousTextCount = 0;

	/**
	 * 没有文字时，设置初始状态
	 */
	private void resetInputButton(boolean resetVisibility)
	{
		if (resetVisibility)
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
		if (replyField == null)
			return;
		String replyFieldText = replyField.getText().toString();
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel != null && replyFieldText != null)
		{
			if (channel.inputDraft == null)
				channel.inputDraft = new InputDraft();
			channel.draft = replyFieldText;

			if (StringUtils.isNotEmpty(channel.draft))
			{
				if (channel.draftAt == null)
					channel.draftAt = new ArrayList<InputAtContent>();
				else
					channel.draftAt.clear();
				if (channel.isAllianceOrAllianceSysChannel() && allianceInputAtList != null && allianceInputAtList.size() > 0)
				{
					for (InputAtContent at : allianceInputAtList)
						channel.draftAt.add(at);
				}
				else if (channel.isCountryOrCountrySysChannel() && countryInputAtList != null && countryInputAtList.size() > 0)
				{
					for (InputAtContent at : countryInputAtList)
						channel.draftAt.add(at);
				}
			}

			channel.inputDraft.setDraft(channel.draft);
			channel.inputDraft.setInputAt(channel.draftAt);
			channel.draftTime = TimeManager.getInstance().getCurrentTimeMS();
			DBManager.getInstance().updateChannel(channel);
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
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN && ConfigManager.isXMEnabled
				&& ConfigManager.isXMAudioEnabled && currentTab != TAB_COUNTRY && currentTab != TAB_BATTLE_FIELD && !isHornUI;
	}

	private void refreshInputButton()
	{
		if (canShowRecordButton())
		{
			checkSendButton(true);
		}
		else
		{
			resetInputButton(true);
		}
	}

	private void refreshWordCount()
	{
		if (replyField == null || wordCount == null)
			return;

		// 有文字与没文字之间发生切换时
		if ((previousTextCount == 0 && replyField.getText().length() > 0) || (previousTextCount > 0 && replyField.getText().length() == 0))
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

	private AnimatorSetProxy	runningAnimation;
	private AnimatorSetProxy	runningAnimation2;
	private AnimatorSetProxy	runningAnimationAudio;
	private int					runningAnimationType;

	/*
	 * 语音和发送按钮的动画切换
	 */
	private void checkSendButton(final boolean animated)
	{
		if (replyField.getText().length() > 0)
		{
			if (voice_rec_button_layout.getVisibility() == View.VISIBLE)
			{
				if (animated)
				{
					if (runningAnimationType == 1)
					{
						return;
					}
					if (runningAnimation != null)
					{
						runningAnimation.cancel();
						runningAnimation = null;
					}
					if (runningAnimation2 != null)
					{
						runningAnimation2.cancel();
						runningAnimation2 = null;
					}

					sendMessageLayout.setVisibility(View.VISIBLE);
					runningAnimation = new AnimatorSetProxy();
					runningAnimationType = 1;

					runningAnimation.playTogether(ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleX", 0.1f),
							ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleY", 0.1f),
							ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 0.0f),
							ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleX", 1.0f),
							ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleY", 1.0f),
							ObjectAnimatorProxy.ofFloat(sendMessageLayout, "alpha", 1.0f));

					runningAnimation.setDuration(150);
					runningAnimation.addListener(new AnimatorListenerAdapterProxy()
					{
						@Override
						public void onAnimationEnd(Object animation)
						{
							if (runningAnimation != null && runningAnimation.equals(animation))
							{
								sendMessageLayout.setVisibility(View.VISIBLE);
								voice_rec_button_layout.setVisibility(View.GONE);
								voice_rec_button_layout.clearAnimation();
								runningAnimation = null;
								runningAnimationType = 0;
							}
						}
					});
					runningAnimation.start();
				}
				else
				{
					ViewProxy.setScaleX(voice_rec_button_layout, 0.1f);
					ViewProxy.setScaleY(voice_rec_button_layout, 0.1f);
					ViewProxy.setAlpha(voice_rec_button_layout, 0.0f);
					ViewProxy.setScaleX(sendMessageLayout, 1.0f);
					ViewProxy.setScaleY(sendMessageLayout, 1.0f);
					ViewProxy.setAlpha(sendMessageLayout, 1.0f);
					sendMessageLayout.setVisibility(View.VISIBLE);
					voice_rec_button_layout.setVisibility(View.GONE);
					voice_rec_button_layout.clearAnimation();
				}
			}
		}
		else
		{
			if (animated)
			{
				if (runningAnimationType == 2)
				{
					return;
				}

				if (runningAnimation != null)
				{
					runningAnimation.cancel();
					runningAnimation = null;
				}
				if (runningAnimation2 != null)
				{
					runningAnimation2.cancel();
					runningAnimation2 = null;
				}

				voice_rec_button_layout.setVisibility(View.VISIBLE);
				runningAnimation = new AnimatorSetProxy();
				runningAnimationType = 2;

				runningAnimation.playTogether(ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleX", 0.1f),
						ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleY", 0.1f),
						ObjectAnimatorProxy.ofFloat(sendMessageLayout, "alpha", 0.0f),
						ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleX", 1.0f),
						ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleY", 1.0f),
						ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 1.0f));

				runningAnimation.setDuration(150);
				runningAnimation.addListener(new AnimatorListenerAdapterProxy()
				{
					@Override
					public void onAnimationEnd(Object animation)
					{
						if (runningAnimation != null && runningAnimation.equals(animation))
						{
							sendMessageLayout.setVisibility(View.GONE);
							sendMessageLayout.clearAnimation();
							voice_rec_button_layout.setVisibility(View.VISIBLE);
							runningAnimation = null;
							runningAnimationType = 0;
						}
					}
				});
				runningAnimation.start();
			}
			else
			{
				ViewProxy.setScaleX(sendMessageLayout, 0.1f);
				ViewProxy.setScaleY(sendMessageLayout, 0.1f);
				ViewProxy.setAlpha(sendMessageLayout, 0.0f);
				ViewProxy.setScaleX(voice_rec_button_layout, 1.0f);
				ViewProxy.setScaleY(voice_rec_button_layout, 1.0f);
				ViewProxy.setAlpha(voice_rec_button_layout, 1.0f);
				sendMessageLayout.setVisibility(View.GONE);
				sendMessageLayout.clearAnimation();
				voice_rec_button_layout.setVisibility(View.VISIBLE);
			}
		}
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
	int							targetButtonWidth;
	int							targetButtonHeight;

	private double getAudioUIScale()
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
			if (resourceId > 0)
			{
				AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
			}

			int originalWidth = addReply.getWidth();
			int scaleWidth = (int) Math.floor(ScaleUtil.getScreenWidth() / 14);
			if (ConfigManager.getInstance().needScaleInputPanel() && scaleWidth > originalWidth)
			{
				targetButtonWidth = scaleWidth;
			}
			else
			{
				targetButtonWidth = originalWidth;
			}

			double editTextRatio = (double) targetButtonWidth / (double) originalWidth;

			// S3手机上的尺寸(目标效果是在S3手机上调的好，界面、文字都相对于它进行缩放)
			// addReply宽度是宽度的1/4，让其高度保持长宽比，然后再计算出缩放的倍率（textRatio）
			double sendButtonRatio = (double) sendButtonBaseHeight / (double) sendButtonBaseWidth;
			targetButtonHeight = (int) (targetButtonWidth * sendButtonRatio);

			// updateHornLayoutHeight(horn_scroll_layout);
			// updateHornLayoutHeight(battle_horn_scroll_layout);

			LinearLayout.LayoutParams relativeLayoutLayoutParams = (LinearLayout.LayoutParams) relativeLayout1.getLayoutParams();
			relativeLayoutLayoutParams.height = targetButtonHeight + ScaleUtil.dip2px(5);
			relativeLayout1.setLayoutParams(relativeLayoutLayoutParams);

			LinearLayout.LayoutParams checkboxParams = (LinearLayout.LayoutParams) horn_checkbox.getLayoutParams();
			checkboxParams.width = targetButtonHeight;
			checkboxParams.height = targetButtonHeight;
			horn_checkbox.setLayoutParams(checkboxParams);

			LinearLayout.LayoutParams emoj_btn_Layout = (LinearLayout.LayoutParams) emoj_checkbox.getLayoutParams();
			emoj_btn_Layout.width = targetButtonHeight;
			emoj_btn_Layout.height = targetButtonHeight;
			emoj_checkbox.setLayoutParams(emoj_btn_Layout);

			LinearLayout.LayoutParams addReplyParams = (LinearLayout.LayoutParams) addReply.getLayoutParams();
			addReplyParams.width = targetButtonWidth;
			addReplyParams.height = targetButtonHeight;
			addReply.setLayoutParams(addReplyParams);

			// LinearLayout.LayoutParams replyFieldParams = (LinearLayout.LayoutParams) replyField.getLayoutParams();
			// replyFieldParams.height = targetButtonHeight;
			// replyField.setLayoutParams(replyFieldParams);
			// int dropDownHeight = (int) (ScaleUtil.dip2px(activity, 160) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			// replyField.setDropDownHeight(dropDownHeight);

			LinearLayout.LayoutParams buttonCountryParams = (LinearLayout.LayoutParams) buttonCountry.getLayoutParams();
			buttonCountryParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonCountry.setLayoutParams(buttonCountryParams);

			LinearLayout.LayoutParams recordButtonParams = (LinearLayout.LayoutParams) voice_rec_button.getLayoutParams();
			recordButtonParams.width = targetButtonWidth;
			recordButtonParams.height = targetButtonHeight;
			voice_rec_button.setLayoutParams(recordButtonParams);

			LinearLayout.LayoutParams buttonAllianceParams = (LinearLayout.LayoutParams) buttonAlliance.getLayoutParams();
			buttonAllianceParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonAlliance.setLayoutParams(buttonAllianceParams);

			LinearLayout.LayoutParams buttonFriendParams = (LinearLayout.LayoutParams) buttonCustom.getLayoutParams();
			buttonFriendParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonCustom.setLayoutParams(buttonFriendParams);

			LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams((int) (13 * ConfigManager.scaleRatio),
					(int) (17 * ConfigManager.scaleRatio), 1);
			param3.gravity = Gravity.CENTER_VERTICAL;
			tooltipArrow.setLayoutParams(param3);

			ScaleUtil.adjustTextSize(addReply, ConfigManager.scaleRatio);
			if (ConfigManager.getInstance().needScaleInputPanel())
			{
				ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio * editTextRatio * 0.9);
			}
			else
			{
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

			if (addLocalRandomChat_layout != null)
			{
				LinearLayout.LayoutParams addLocalRandomChat_layout_LayoutParams = (LinearLayout.LayoutParams) addLocalRandomChat_layout
						.getLayoutParams();
				addLocalRandomChat_layout_LayoutParams.width = length;
				addLocalRandomChat_layout_LayoutParams.height = length;
				addLocalRandomChat_layout.setLayoutParams(addLocalRandomChat_layout_LayoutParams);
			}

			if (addLocalRandomChat_Container != null)
			{
				FrameLayout.LayoutParams addLocalRandomChat_Container_LayoutParams = (FrameLayout.LayoutParams) addLocalRandomChat_Container
						.getLayoutParams();
				addLocalRandomChat_Container_LayoutParams.width = length2;
				addLocalRandomChat_Container_LayoutParams.height = length2;
				addLocalRandomChat_Container.setLayoutParams(addLocalRandomChat_Container_LayoutParams);
			}

			if (addGlobalRandomChat_layout != null)
			{
				LinearLayout.LayoutParams addGlobalRandomChat_layout_LayoutParams = (LinearLayout.LayoutParams) addGlobalRandomChat_layout
						.getLayoutParams();
				addGlobalRandomChat_layout_LayoutParams.width = length;
				addGlobalRandomChat_layout_LayoutParams.height = length;
				addGlobalRandomChat_layout.setLayoutParams(addGlobalRandomChat_layout_LayoutParams);
			}

			if (addGlobalRandomChat_Container != null)
			{
				FrameLayout.LayoutParams addGlobalRandomChat_Container_LayoutParams = (FrameLayout.LayoutParams) addGlobalRandomChat_Container
						.getLayoutParams();
				addGlobalRandomChat_Container_LayoutParams.width = length2;
				addGlobalRandomChat_Container_LayoutParams.height = length2;
				addGlobalRandomChat_Container.setLayoutParams(addGlobalRandomChat_Container_LayoutParams);
			}

			int length3 = (int) (ScaleUtil.dip2px(activity, 36) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_channel_setting_layout != null)
			{
				FrameLayout.LayoutParams custom_channel_setting_layoutLayoutParams = (FrameLayout.LayoutParams) custom_channel_setting_layout
						.getLayoutParams();
				custom_channel_setting_layoutLayoutParams.height = length3;
				custom_channel_setting_layout.setLayoutParams(custom_channel_setting_layoutLayoutParams);
			}

			if (country_exchange_layout != null)
			{
				FrameLayout.LayoutParams country_exchange_layoutLayoutParams = (FrameLayout.LayoutParams) country_exchange_layout
						.getLayoutParams();
				country_exchange_layoutLayoutParams.height = length3;
				country_exchange_layout.setLayoutParams(country_exchange_layoutLayoutParams);
			}

			if (alliance_sys_top_layout != null)
			{
				FrameLayout.LayoutParams alliance_sys_top_layoutLayoutParams = (FrameLayout.LayoutParams) alliance_sys_top_layout
						.getLayoutParams();
				alliance_sys_top_layoutLayoutParams.height = length3;
				alliance_sys_top_layout.setLayoutParams(alliance_sys_top_layoutLayoutParams);
			}

			if (alliance_drag_layout != null)
			{
				new_alliance_sys_message.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) alliance_drag_layout.getLayoutParams();
				int height = length + new_alliance_sys_message.getHeight()
						+ (int) (ScaleUtil.dip2px(23) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())
						+ new_alliance_sys_sliding_btn.getHeight();
				drag_layoutLayoutParams.height = height;
				alliance_drag_layout.setLayoutParams(drag_layoutLayoutParams);
				alliance_sliding_layout.setPanelHeight(height);
			}

			if (country_drag_layout != null)
			{
				new_country_sys_message.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams drag_layoutLayoutParams = (LinearLayout.LayoutParams) country_drag_layout.getLayoutParams();
				int height = length + new_country_sys_message.getHeight()
						+ (int) (ScaleUtil.dip2px(23) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())
						+ new_country_sys_sliding_btn.getHeight();
				drag_layoutLayoutParams.height = height;
				country_drag_layout.setLayoutParams(drag_layoutLayoutParams);
				country_sliding_layout.setPanelHeight(height);
			}

			int length4 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (add_title != null)
			{
				LinearLayout.LayoutParams add_titleLayoutParams = (LinearLayout.LayoutParams) add_title.getLayoutParams();
				add_titleLayoutParams.height = length4;
				add_title.setLayoutParams(add_titleLayoutParams);
			}

			int length4_1 = (int) (ScaleUtil.dip2px(activity, 35) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_channel_settting_btn != null)
			{
				RelativeLayout.LayoutParams custom_channel_settting_btn_Layout = (RelativeLayout.LayoutParams) custom_channel_settting_btn
						.getLayoutParams();
				custom_channel_settting_btn_Layout.width = length4_1;
				custom_channel_settting_btn_Layout.height = length4_1;
				if (ConfigManager.getInstance().needRTL())
				{
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
						custom_channel_settting_btn_Layout.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					else
						custom_channel_settting_btn_Layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
					custom_channel_settting_btn_Layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				custom_channel_settting_btn.setLayoutParams(custom_channel_settting_btn_Layout);
			}

			if (alliance_sys_top_icon != null)
			{
				RelativeLayout.LayoutParams alliance_sys_top_icon_Layout = (RelativeLayout.LayoutParams) alliance_sys_top_icon
						.getLayoutParams();
				alliance_sys_top_icon_Layout.width = length4_1;
				alliance_sys_top_icon_Layout.height = length4_1;
				if (ConfigManager.getInstance().needRTL())
				{
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
						alliance_sys_top_icon_Layout.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					else
						alliance_sys_top_icon_Layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
					alliance_sys_top_icon_Layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				alliance_sys_top_icon.setLayoutParams(alliance_sys_top_icon_Layout);
			}

			if (country_sys_top_icon != null)
			{
				RelativeLayout.LayoutParams country_sys_top_icon_Layout = (RelativeLayout.LayoutParams) country_sys_top_icon
						.getLayoutParams();
				country_sys_top_icon_Layout.width = length4_1;
				country_sys_top_icon_Layout.height = length4_1;
				if (ConfigManager.getInstance().needRTL())
				{
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
						country_sys_top_icon_Layout.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					else
						country_sys_top_icon_Layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
					country_sys_top_icon_Layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				country_sys_top_icon.setLayoutParams(country_sys_top_icon_Layout);
			}

			int length5 = (int) (ScaleUtil.dip2px(activity, 25) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (country_exchange_btn != null)
			{
				LinearLayout.LayoutParams country_exchange_btn_Layout = (LinearLayout.LayoutParams) country_exchange_btn.getLayoutParams();
				country_exchange_btn_Layout.width = length5;
				country_exchange_btn_Layout.height = length5;
				country_exchange_btn.setLayoutParams(country_exchange_btn_Layout);
			}

			int length6 = (int) (ScaleUtil.dip2px(activity, 24) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (battle_horn_close_btn != null)
			{
				RelativeLayout.LayoutParams battle_horn_close_btn_Layout = (RelativeLayout.LayoutParams) battle_horn_close_btn
						.getLayoutParams();
				battle_horn_close_btn_Layout.width = length6;
				battle_horn_close_btn_Layout.height = length6;
				battle_horn_close_btn.setLayoutParams(battle_horn_close_btn_Layout);
			}

			if (horn_close_btn != null)
			{
				RelativeLayout.LayoutParams horn_close_btn_Layout = (RelativeLayout.LayoutParams) horn_close_btn.getLayoutParams();
				horn_close_btn_Layout.width = length6;
				horn_close_btn_Layout.height = length6;
				horn_close_btn.setLayoutParams(horn_close_btn_Layout);
			}

			if (battle_horn_image != null)
			{
				LinearLayout.LayoutParams battle_horn_image_Layout = (LinearLayout.LayoutParams) battle_horn_image.getLayoutParams();
				battle_horn_image_Layout.width = length6;
				battle_horn_image_Layout.height = length6;
				battle_horn_image.setLayoutParams(battle_horn_image_Layout);
			}

			if (horn_image != null)
			{
				LinearLayout.LayoutParams horn_image_Layout = (LinearLayout.LayoutParams) horn_image.getLayoutParams();
				horn_image_Layout.width = length6;
				horn_image_Layout.height = length6;
				horn_image.setLayoutParams(horn_image_Layout);
			}

			int length7 = (int) (ScaleUtil.dip2px(activity, 75) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (tooltipLayout != null)
			{
				RelativeLayout.LayoutParams tooltipLayout_Layout = (RelativeLayout.LayoutParams) tooltipLayout.getLayoutParams();
				tooltipLayout_Layout.topMargin = length7;
				tooltipLayout.setLayoutParams(tooltipLayout_Layout);
			}

			ScaleUtil.adjustTextSize(wordCount, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonCountry, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonAlliance, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonCustom, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(new_alliance_sys_message, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(new_country_sys_message, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(alliance_null_sys_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(country_null_sys_tip, ConfigManager.scaleRatio);

			ScaleUtil.adjustTextSize(buttonJoinAlliance, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noAllianceTipText, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(dragon_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(addCustomChatBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tooltipLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_scroll_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_horn_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_horn_scroll_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(customChannelName, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_channel_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_setting_confim, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(country_channel_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(alliance_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_field_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_field_btn, ConfigManager.scaleRatio);

			ScaleUtil.adjustTextSize(addLocalRandomChatBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(local_random_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(global_random_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(random_room_join_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(retry_btn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(joinCustomChatBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(random_chat_destory_tip_text, ConfigManager.scaleRatio);

			adjustSizeCompleted = true;

			this.resetInputButton(true);

			if (lazyLoading)
			{
				activity.showProgressBar();
				onBecomeVisible();
			}
		}
	}

	private void refreshCustomChatChannel(ChatChannel chatChannel)
	{
		if (activity == null || !ChatServiceController.isInChat() || channelViews == null || channelViews.size() < (TAB_CUSTOM + 1))
			return;

		final ChannelView channelView = channelViews.get(TAB_CUSTOM);
		if (channelView == null)
			return;

		channelView.tab = TAB_CUSTOM;
		if (chatChannel != null)
		{
			if (!chatChannel.hasInitLoaded())
				chatChannel.loadMoreMsg();
			chatChannel.clearFirstNewMsg();
			chatChannel.setChannelView(channelView);
			int mailType = MailManager.MAIL_USER;
			if (chatChannel.isModChannel())
				mailType = MailManager.MAIL_MOD_PERSONAL;
			else if (chatChannel.isDriftingBottleChannel())
				mailType = MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND;
			else if (chatChannel.isRandomChatRoomChannel())
			{
				if (random_chat_tip_layout.getVisibility() != View.GONE)
					random_chat_tip_layout.setVisibility(View.GONE);
				if (custom_chat_tip_layout.getVisibility() != View.GONE)
					custom_chat_tip_layout.setVisibility(View.GONE);
				if (random_chat_destory_tip_layout.getVisibility() != View.GONE)
					random_chat_destory_tip_layout.setVisibility(View.GONE);
				if (relativeLayout1.getVisibility() != View.VISIBLE)
					relativeLayout1.setVisibility(View.VISIBLE);
			}
			if (!chatChannel.isRandomChatRoomChannel())
				ServiceInterface.setMailInfo(chatChannel.channelID, chatChannel.latestId, chatChannel.getCustomName(), mailType);
		}
		else
		{
			ServiceInterface.setMailInfo("", "", "", -1);
		}
		// refreshMemberSelectBtn();
		channelView.channelType = DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
		channelView.chatChannel = chatChannel;

		List<MsgItem> msgList = null;

		if (channelView.chatChannel != null)
		{
			msgList = channelView.chatChannel.msgList;
			channelView.setVisibility(View.VISIBLE);
		}
		else
		{
			channelView.setVisibility(View.GONE);
		}

		if (msgList == null)
			msgList = new ArrayList<MsgItem>();

		if (msgList != null)
		{
			MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
			channelView.setMessagesAdapter(adapter);
			XiaoMiToolManager.getInstance().addAudioListener(channelView.getMessagesAdapter());
		}

		if (channelView.messagesListView != null)
			channelView.messagesListView.setAdapter(channelView.getMessagesAdapter());
		if (channelView.chatChannel != null && channelView.chatChannel.lastPosition.x != -1 && rememberPosition)
		{
			channelView.messagesListView.setSelectionFromTop(channelView.chatChannel.lastPosition.x,
					channelView.chatChannel.lastPosition.y);
		}
		else
		{
			if (channelView.getMessagesAdapter() != null)
				channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
		}
		refreshCustomChannelName(chatChannel);
	}

	private void refreshCustomChatChannel(int channelType, String channelId)
	{
		ChatChannel chatChannel = ChannelManager.getInstance().getChannel(channelType, channelId);
		refreshCustomChatChannel(chatChannel);
		if (chatChannel != null && StringUtils.isNotEmpty(chatChannel.draft))
		{
			replyField.setText(chatChannel.draft);
			replyField.setSelection(chatChannel.draft.length());
		}
		else
			replyField.setText("");
	}

	public void refreshCustomChatChannel()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					LocalConfig config = ConfigManager.getInstance().getLocalConfig();
					if (config != null)
					{
						if (StringUtils.isNotEmpty(config.randomChannelId))
						{
							customChannelType = DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
							customChannelId = config.randomChannelId;
						}
						else
						{
							customChannelType = config.getCustomChannelType();
							customChannelId = config.getCustomChannelId();
						}
					}
					refreshCustomChatChannel(customChannelType, customChannelId);
				}
			});
		}
	}

	public void showRedPackageConfirm(final MsgItem msgItem)
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

	public void refreshRandomDestoryTip()
	{
		if (getCurrentChannel() == null)
			return;
		if (activity == null)
			return;
		refreshCustomChatChannel();

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					hideSoftKeyBoard();
					hideEmojPanel();
					relativeLayout1.setVisibility(View.GONE);
					boolean hasCustomData = (friendList != null && friendList.size() > 0)
							|| (chatroomChannelList != null && chatroomChannelList.size() > 0);
					boolean isRandomChatRoomDestoryed = SwitchUtils.mqttEnable ? MqttManager.getInstance().isRandomChatRoomDestoryed() : WebSocketManager.getInstance().isRandomChatRoomDestoryed();
					custom_chat_tip_layout.setVisibility(!isRandomChatRoomDestoryed
							&& getCurrentChannel().tab == TAB_CUSTOM
							&& (StringUtils.isEmpty(customChannelId) && (hasCustomData || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
					random_chat_tip_layout.setVisibility(!isRandomChatRoomDestoryed
							&& getCurrentChannel().tab == TAB_CUSTOM && StringUtils.isEmpty(customChannelId) && !hasCustomData
							&& ChatServiceController.randomChatEnable ? View.VISIBLE : View.GONE);
					random_chat_destory_tip_layout.setVisibility(getCurrentChannel().tab == TAB_CUSTOM
							&& StringUtils.isEmpty(customChannelId) && isRandomChatRoomDestoryed ? View.VISIBLE : View.GONE);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void refreshRandomTip()
	{
		if (getCurrentChannel() == null)
			return;
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					boolean hasCustomData = (friendList != null && friendList.size() > 0)
							|| (chatroomChannelList != null && chatroomChannelList.size() > 0);
					custom_channel_setting_layout
							.setVisibility((getCurrentChannel().tab == TAB_CUSTOM && !isSettingCustomChannel
									&& StringUtils.isNotEmpty(customChannelId) && (hasCustomData || (!hasCustomData && ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
					custom_chat_tip_layout.setVisibility(getCurrentChannel().tab == TAB_CUSTOM
							&& (StringUtils.isEmpty(customChannelId) && (hasCustomData || (!hasCustomData && !ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
					random_chat_tip_layout.setVisibility(getCurrentChannel().tab == TAB_CUSTOM && StringUtils.isEmpty(customChannelId)
							&& !hasCustomData && ChatServiceController.randomChatEnable ? View.VISIBLE : View.GONE);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static boolean emojPanelChanged = false;

	public void onEmojPanelChanged()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		emojPanelChanged = true;
		refreshEmojPanel();
	}

	private void refreshEmojPanel()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					if (emoj_panel != null)
						emoj_panel.initEmoj(activity);
					emojPanelChanged = false;
				}
			});
		}
	}

	public void refreshRandomJoinTip(final boolean visible, final boolean sucess)
	{
		if (getCurrentChannel() == null)
			return;
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					custom_channel_setting_layout.setVisibility(View.GONE);
					if (visible)
					{
						random_chat_tip_layout.setVisibility(View.GONE);
						custom_chat_tip_layout.setVisibility(View.GONE);
						random_chat_destory_tip_layout.setVisibility(View.GONE);
						if (getCurrentChannel() != null)
							getCurrentChannel().setVisibility(View.GONE);
					}
					else
					{
						if (getCurrentChannel() != null)
							getCurrentChannel().setVisibility(View.VISIBLE);
						boolean hasCustomData = (friendList != null && friendList.size() > 0)
								|| (chatroomChannelList != null && chatroomChannelList.size() > 0);
						custom_channel_setting_layout
								.setVisibility((getCurrentChannel().tab == TAB_CUSTOM && !isSettingCustomChannel
										&& StringUtils.isNotEmpty(customChannelId) && (hasCustomData || (!hasCustomData && ChatServiceController.randomChatEnable))) ? View.VISIBLE : View.GONE);
					}

					random_room_join_tip_layout.setVisibility(visible ? View.VISIBLE : View.GONE);
					if (sucess)
					{
						retry_btn.setVisibility(View.GONE);
						random_room_join_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RANDOM_ROOM_JOINING));
					}
					else
					{
						retry_btn.setVisibility(View.VISIBLE);
						random_room_join_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RANDOM_ROOM_JOIN_FAILED));
					}
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

	public MsgItem getCurrentRedPackageItem()
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
		hideEmojPanel();
		replyField.clearFocus();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (customChannelChange)
		{
			ConfigManager.getInstance().saveLocalConfig();
			customChannelChange = false;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);

		if (customChannelChange)
		{
			ConfigManager.getInstance().saveLocalConfig();
			customChannelChange = false;
		}
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel != null)
			channel.hideSpecialMsg();
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			ChatServiceController.getInstance().setGameMusiceEnable(true);
		ChatServiceController.needShowAllianceDialog = false;
		if (tooltipLayout != null)
			tooltipLayout.setOnClickListener(null);
		if (buttonJoinAlliance != null)
			buttonJoinAlliance.setOnClickListener(null);
		if (channelButton != null)
		{
			for (int i = 0; i < channelButton.size(); i++)
			{
				channelButton.get(i).setTag(null);
				channelButton.get(i).setOnClickListener(null);
			}
			channelButton.clear();
			channelButton = null;
		}

		try
		{
			getMemberSelectButton().setOnClickListener(null);
			if (getRealtimeRightBtn() != null)
			{
				getRealtimeRightBtn().setOnClickListener(null);
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

		noAllianceFrameLayout = null;
		relativeLayout1 = null;
		buttonsLinearLayout = null;
		imageView1 = null;
		imageView2 = null;
		wordCount = null;
		buttonCountry = null;
		buttonAlliance = null;
		buttonCustom = null;
		tooltipLayout = null;
		tooltipLabel = null;
		tooltipArrow = null;
		buttonJoinAlliance = null;
		noAllianceTipText = null;
		hs__dragon_chat_tip_layout = null;
		dragon_chat_tip_text = null;

		if (mManager != null)
		{
			mManager.unregisterListener(this);// 注销传感器监听
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
		mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), // 距离感应器
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

	protected static boolean dataChanged = false;

	@Override
	public void onResume()
	{
		System.out.println("onResume");
		super.onResume();
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			String name = UserManager.getInstance().getCurrentMail().opponentName;
			if (StringUtils.isNotEmpty(name) && name.length() > 30)
			{
				name = name.substring(0, 30);
				name += "...";
			}
			getTitleLabel().setText(name);
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
		{
			if (customChannelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT && StringUtils.isNotEmpty(customChannelId))
			{
				ChatChannel channel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);
				if (channel != null)
					custom_channel_name.setText(channel.customName);
			}
			else
				custom_channel_name.setText(UserManager.getInstance().getCurrentMail().opponentName);
		}
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel != null && StringUtils.isNotEmpty(channel.draft))
		{
			activity.showSoftKeyBoard(replyField);
			replyField.requestFocus();
		}
		if (dataChanged && getCurrentChannel() != null && getCurrentChannel().chatChannel != null)
		{
			System.out.println("onResume chatfragment onMsgAdd");
			notifyDataSetChanged(ChatServiceController.getCurrentChannelType(), getCurrentChannel().chatChannel.channelID, true);
		}

		if (emojPanelChanged)
		{
			refreshEmojPanel();
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}
}