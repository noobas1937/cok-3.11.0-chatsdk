package com.elex.chatservice.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
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
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChannelView;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.ActionBarFragment;
import com.elex.chatservice.view.autoscroll.ScrollText;
import com.elex.chatservice.view.autoscroll.ScrollTextManager;
import com.elex.chatservice.view.listview.ListViewLoadListener;
import com.elex.chatservice.view.listview.PullDownToLoadMoreView;
import com.nineoldandroids.view.ViewHelper;

public class ChatFragment extends ActionBarFragment
{
	protected RelativeLayout	messagesListFrameLayout;
	private FrameLayout			noAllianceFrameLayout;
	private RelativeLayout		relativeLayout1;
	protected LinearLayout		buttonsLinearLayout;
	protected EditText			replyField;
	private RelativeLayout		messageBox;
	// protected LinearLayout header;
	private MenuItem			attachScreenshotMenu;
	private TextView			wordCount;
	protected Button			addReply;
	private Button				buttonCountry;
	private Button				buttonAlliance;
	private ArrayList<Button>	channelButton;
	private ImageView			imageView1;
	protected ImageView			imageView2;
	private Button				buttonJoinAlliance;
	private TextView			noAllianceTipText;
	private Timer				mTimer;
	private TimerTask			mTimerTask;
	private CheckBox			horn_checkbox;
	// private LinearLayout horn_tip_layout;
	private RelativeLayout		horn_scroll_layout;
	// private TextView horn_text_tip;
	private TextView			horn_name;
	private ScrollText			horn_scroll_text;
	private LinearLayout		tooltipLayout;
	private TextView			tooltipLabel;
	private ImageView			tooltipArrow;
	private ImageView			horn_close_btn;
	private LinearLayout		hs__dragon_chat_tip_layout;
	private TextView			dragon_chat_tip_text;

	private int					oldAdapterCount				= 0;
	private int					loadMoreCount				= 0;

	protected int				loadingHeaderHeight;
	protected boolean			isKeyBoardFirstShowed		= false;
	private int					curMaxInputLength			= 500;

	public static boolean		rememberPosition			= false;

	private static String		savedText					= "";
	private boolean				isJoinAlliancePopupShowing	= false;
	public static String		gmailAccount				= "";

	public boolean				isKeyBoradShowing			= false;
	public boolean				isKeyBoradChange			= false;

	private boolean				isSelectMemberBtnEnable		= false;

	public boolean isSelectMemberBtnEnable()
	{
		return isSelectMemberBtnEnable;
	}

	public ChannelView getCurrentChannel()
	{
		return getCurrentChannelView();
	}

	public ChatFragment()
	{
		isKeyBoardFirstShowed = false;
		initChannelViews();
	}

	public void refreshMemberSelectBtn()
	{
		if (!(ChatServiceController.isInMailDialog() && !ChatServiceController.isCreateChatRoom))
			return;

		try
		{
			if (ChatServiceController.isInChatRoom()
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
			getMemberSelectButton().setVisibility(isSelectMemberBtnEnable ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	private void showMessageBox()
	{
		this.messageBox.setVisibility(View.VISIBLE);
		this.buttonsLinearLayout.setVisibility(View.VISIBLE);
		refreshWordCount();

		if (this.attachScreenshotMenu != null)
		{
			this.attachScreenshotMenu.setVisible(true);
		}
	}

	public void saveState()
	{
		for (int i = 0; i < getChannelViewCount(); i++)
		{
			ChannelView channelView = getChannelView(i);
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
			for (int i = 0; i < getChannelViewCount(); i++)
			{
				if (getChannelView(i).chatChannel != null && getChannelView(i).chatChannel.lastPosition.x == -1
						&& getChannelView(i).messagesListView != null && getChannelView(i).getMessagesAdapter() != null)
				{
					getChannelView(i).messagesListView.setSelection(getChannelView(i).getMessagesAdapter().getCount() - 1);
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

		return inflater.inflate(ResUtil.getId(this, "layout", "cs__messages_fragment"), container, false);
	}

	private FrameLayout.LayoutParams getLayoutParams()
	{
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER;
		return param;
	}

	public void refreshIsInLastScreen(final int channelType)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (isSameChannel(channelType))
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

	private boolean isInLastScreen()
	{
		// messagesListView?????????messagesListView.getChildAt(0)????????????0
		if (getCurrentChannel() == null || getCurrentChannel().getMessagesAdapter() == null
				|| getCurrentChannel().getMessagesAdapter().getCount() == 0 || getCurrentChannel().messagesListView == null)
		{
			return true;
		}
		// ?????????view.getFirstVisiblePosition()????????????????????????????????????item?????????????????????????????????????????????????????????1.6??????
		View v = getCurrentChannel().messagesListView.getChildAt(0);
		if (v == null)
		{
			return true;
		}

		// ?????????item????????????????????????
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

	boolean	inLastScreen	= false;

	public void updateListPositionForNewMsg(int channelType, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType) || getCurrentChannel().messagesListView == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
		{
			gotoLastLine();
		}
		inLastScreen = false;
	}

	public void smoothUpdateListPositionForNewMsg(int channelType, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType) || getCurrentChannel().messagesListView == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
		{
			scrollToLastLine();
		}
		inLastScreen = false;
	}

	public View getItemView(int position)
	{
		if (getCurrentChannel() == null || position < 0)
			return null;
		ListView listView = getCurrentChannel().messagesListView;
		if (listView == null || position > listView.getChildCount())
			return null;
		System.out.println("getItemView count:" + listView.getChildCount());
		return listView.getChildAt(position);
	}

	public void updateListPositionForOldMsg(int channelType, int loadCount, final boolean needMergeSendTime)
	{
		final ListView listView = getCurrentChannel().messagesListView;
		if (!isSameChannel(channelType) || listView == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}
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
					if (!getCurrentChannel().chatChannel.isLoadingAllNew)
					{
						int heightOffest = getCurrentChannel().pullDownToLoadListView.getPullDownHeight();
						if (needMergeSendTime)
						{
							if (ChatServiceController.sendTimeTextHeight != 0)
								heightOffest += ChatServiceController.sendTimeTextHeight + ScaleUtil.dip2px(activity, 15);
							else
								heightOffest += ScaleUtil.dip2px(activity, 44);
						}
						listView.setSelectionFromTop(loadMoreCount, heightOffest);
					}
					else
					{
						listView.setSelectionFromTop(0, 0);
					}
					refreshToolTip();
					getCurrentChannel().pullDownToLoadListView.hideProgressBar();
					stopTimerTask();
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
		System.out.println("chatfragment onMsgAdd");
		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().notifyDataSetChanged(channelType,channelId, needCalculateShowTimeIndex);
			dataChanged = false;
		}
	}

	public void notifyDataSetChanged(final int channelType,final String channelId, final boolean needCalculateShowTimeIndex)
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
					if(!isSameChannel(channelType))
						return;
					ChannelView curChannelView = getCurrentChannelView();
					if(curChannelView!=null)
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
	

	// ?????????notifyDataSetChanged???????????????
	public void refreshListItem(MsgItem msgItem)
	{
		try
		{
			MessagesAdapter adapter = getChannelView(ChannelManager.channelType2tab(msgItem.channelType)).getMessagesAdapter();
			final ListView list = getChannelView(ChannelManager.channelType2tab(msgItem.channelType)).messagesListView;
			if (adapter != null && list != null)
			{
				int start = list.getFirstVisiblePosition();
				for (int i = start, j = list.getLastVisiblePosition(); i <= j; i++)
					if (msgItem == list.getItemAtPosition(i))
					{
						View view = list.getChildAt(i - start);
						list.getAdapter().getView(i, view, list);
						activity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								// ??????????????????item??????????????????setConvertView
								list.invalidateViews();
							}
						});
						break;
					}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void afterSendMsgShowed(int channelType)
	{
		if(getCurrentChannel()!=null && getCurrentChannel().chatChannel!=null)
		{
			notifyDataSetChanged(ChatServiceController.getCurrentChannelType(),getCurrentChannel().chatChannel.channelID,true);
			scrollToLastLine();
		}
	}

	public void resetMoreDataStart(int channelType)
	{
		if (isSameChannel(channelType))
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
	
	protected boolean isSameChannel(int channelType)
	{
		if (getCurrentChannel() == null)
			return false;
		if (getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| getCurrentChannel().channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			return getCurrentChannel().channelType == channelType;
		else
		{
			if (getCurrentChannel().chatChannel != null)
				return (getCurrentChannel().channelType == channelType || getCurrentChannel().chatChannel.channelType == channelType)
						&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid)
						&& StringUtils.isNotEmpty(getCurrentChannel().chatChannel.channelID)
						&& getCurrentChannel().chatChannel.channelID.equals(UserManager.getInstance().getCurrentMail().opponentUid);
			else
				return false;
		}
	}

	protected boolean	isOverrided	= false;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		XiaoMiToolManager.getInstance().exitChat = false;
		super.onViewCreated(view, savedInstanceState);
		if (isOverrided)
			return;

		refreshMemberSelectBtn();
		this.noAllianceFrameLayout = (FrameLayout) view.findViewById(R.id.hs__noAllianceLayout);
		this.relativeLayout1 = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
		this.buttonsLinearLayout = (LinearLayout) view.findViewById(R.id.buttonsLinearLayout);
		this.messagesListFrameLayout = (RelativeLayout) view.findViewById(R.id.hs__messagesListLayout);
		imageView1 = (ImageView) view.findViewById(R.id.imageView1);
		imageView2 = (ImageView) view.findViewById(R.id.imageView2);
		horn_checkbox = (CheckBox) view.findViewById(R.id.horn_checkbox);
		// horn_tip_layout = (LinearLayout)
		// view.findViewById(R.id.horn_tip_layout);
		// horn_text_tip = (TextView) view.findViewById(R.id.horn_text_tip);
		// horn_text_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_HORN_TEXT));
		horn_scroll_text = (ScrollText) view.findViewById(R.id.horn_scroll_text);
		horn_name = (TextView) view.findViewById(R.id.horn_name);
		horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.horn_scroll_layout);
		horn_scroll_layout.setVisibility(View.GONE);
		horn_close_btn = (ImageView) view.findViewById(R.id.horn_close_btn);
		hs__dragon_chat_tip_layout = (LinearLayout) view.findViewById(R.id.hs__dragon_chat_tip_layout);
		dragon_chat_tip_text = (TextView) view.findViewById(R.id.dragon_chat_tip_text);
		dragon_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_DRAGON_CHAT));

		if (!lazyLoading)
			renderList();

		this.replyField = ((EditText) view.findViewById(ResUtil.getId(this.activity, "id", "hs__messageText")));
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
			String title = UserManager.getInstance().getCurrentMail().opponentName;
			if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER)
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(UserManager.getInstance().getCurrentMail().opponentUid);
				if (StringUtils.isNotEmpty(fromUid))
				{
					if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
					{
						title = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
					}
					else
					{
						UserInfo fromUser = UserManager.getInstance().getUser(fromUid);
						if (fromUser != null && StringUtils.isNotEmpty(fromUser.userName))
						{
							title = fromUser.userName;
						}
					}
				}

			}

			if (ChatServiceController.isInChatRoom() && title.length() > 16)
			{
				title = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHATROOM);
				if (title.equals(""))
					title = "Group";
			}
			if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				title += "(MOD)";
			getTitleLabel().setText(title);
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
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
				if (channel != null && StringUtils.isNotEmpty(channel.draft))
					replyField.setText(channel.draft);
				else
					replyField.setText("");
			}
		}

		addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));
		addReply.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SEND));

		this.messageBox = ((RelativeLayout) view.findViewById(ResUtil.getId(this.activity, "id", "relativeLayout1")));
		// header = ((LinearLayout)
		// view.findViewById(ResUtil.getId(this.activity, "id",
		// "headerRelativeLayout")));

		getMemberSelectButton().setVisibility(isSelectMemberBtnEnable ? View.VISIBLE : View.GONE);

		buttonCountry = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonCountry"));
		buttonAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonAllie"));
		if (ChatServiceController.isInCrossFightServer())
		{
			buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD));
		}
		else
		{
			buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY));
		}
		buttonAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE));
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, true);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, false);

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
			}
		});

		noAllianceTipText = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceTipText")));
		noAllianceTipText.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE));

		refreshSendButton();

		for (int i = 0; i < channelButton.size(); i++)
		{
			channelButton.get(i).setTag(getChannelView(i));
			channelButton.get(i).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					ChannelView channel = ((ChannelView) view.getTag());

					channel.setLoadingStart(false);

					showTab(channel.tab);

					if (channel.tab == TAB_COUNTRY)
					{
						JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
								new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY) });
					}
					else if (channel.tab == TAB_ALLIANCE)
					{
						JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
								new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE) });
					}
					if (channel.tab == TAB_ALLIANCE && UserManager.getInstance().getCurrentUser().allianceId.equals(""))
					{
						return;
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
				ServiceInterface.showChatRoomSettingActivity(activity);
				// ServiceInterface.showMemberSelectorActivity(activity, true);
			}
		});

		addReply.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String replyText = replyField.getText().toString().trim();

				if (!TextUtils.isEmpty(replyText))
				{
					// if(replyText.endsWith("png"))
					// {
					// System.out.println("setCommonImage");
					// ImageUtil.setCommonImage(activity, replyText,
					// imageView2);
					// }

					if (horn_checkbox.isChecked() && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
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
									ChatServiceController.sendMsg(replyText, true, false, null);
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
					else
					{
						ChatServiceController.sendMsg(replyText, false, false, null);
					}

				}
			}
		});

		this.replyField.setOnEditorActionListener(new TextView.OnEditorActionListener()
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

		replyField.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (ChatServiceController.isChatRestrict())
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

		getShowFriendButton().setVisibility(ChatServiceController.isInMailDialog() ? View.GONE : View.VISIBLE);
		getShowFriendButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ChatServiceController.doHostAction("showFriend", "", "", "", false);
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
			refreshHasMoreData();
		}

		if (ScrollTextManager.getInstance().getScrollQueueLength(DBDefinition.CHANNEL_TYPE_COUNTRY) > 0)
		{
			MsgItem msgItem = ScrollTextManager.getInstance().getNextText(DBDefinition.CHANNEL_TYPE_COUNTRY);
			if (msgItem != null)
				showHornScrollText(msgItem);
		}
	}

	public void showHornScrollText(MsgItem msgItem)
	{
		if (!msgItem.isHornMessage())
			return;
		if (horn_scroll_layout != null)
		{
			horn_scroll_layout.setVisibility(View.VISIBLE);
			ScrollTextManager.getInstance().showScrollText(msgItem, horn_scroll_text, horn_name, horn_scroll_layout,DBDefinition.CHANNEL_TYPE_COUNTRY);
		}

	}

	public void hideHornScrollText()
	{
		ScrollTextManager.getInstance().shutDownScrollText(horn_scroll_text,DBDefinition.CHANNEL_TYPE_COUNTRY);
	}

	private boolean	lazyLoading	= true;

	protected void onBecomeVisible()
	{
		if (inited)
			return;

		timerDelay = 500;
		startTimer();
	}

	public static final int			CHANNEL_COUNT	= 3;
	private int						currentChannelViewIndex;
	private ArrayList<ChannelView>	channelViews;

	private void initChannelViews()
	{
		channelViews = new ArrayList<ChannelView>();
		for (int i = 0; i < CHANNEL_COUNT; i++)
		{
			ChannelView channelView = new ChannelView();

			channelView.tab = i;

			channelViews.add(channelView);
		}
	}

	public int getChannelViewCount()
	{
		return CHANNEL_COUNT;
	}

	public ChannelView getChannelView(int index)
	{
		return channelViews.get(index);
	}

	public void setChannelViewIndex(int i)
	{
		if (i >= 0 && i < channelViews.size())
		{
			currentChannelViewIndex = i;
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
			return getChannelView(0);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void resetChannelView()
	{
		for (int i = 0; i < CHANNEL_COUNT; i++)
		{
			if (getChannelView(i) != null)
			{
				if (getChannelView(i).chatChannel != null)
				{
					getChannelView(i).chatChannel.setChannelView(null);
				}
				getChannelView(i).init();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void renderList()
	{
		for (int i = 0; i < getChannelViewCount(); i++)
		{
			final ChannelView channelView = getChannelView(i);
			ChatChannel chatChannel = null;
			if (i < 2)
			{
				chatChannel = ChannelManager.getInstance().getChannel(i, "");
				channelView.channelType = i;
			}
			else if (ChatServiceController.isInMailDialog())
			{
				String channelId = UserManager.getInstance().getCurrentMail().opponentUid;
				if (ChatServiceController.isModContactMode() && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
					channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
				chatChannel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType(), channelId);
				channelView.channelType = ChatServiceController.getCurrentChannelType();
			}
			else
			{
				continue;
			}
			if (chatChannel != null)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "chatChannel", chatChannel, "msgList.size()",
						chatChannel.msgList.size());
				chatChannel.clearFirstNewMsg();
				chatChannel.setChannelView(channelView);
			}
			channelView.chatChannel = chatChannel;

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

			// TODO ?????????????????????????????????
			// scroll speed decreases as friction increases. a value of 2 worked
			// well in an emulator; i need to test it on a real device
			// messagesListView.setFriction(ViewConfiguration.getScrollFriction()
			// * 2);

			if (chatChannel != null && chatChannel.msgList != null)
			{
				// this.getActivity() ?????????null
				MessagesAdapter adapter = new MessagesAdapter(activity, chatChannel.msgList);
				if (i == 1)
					XiaoMiToolManager.getInstance().addAudioListener(adapter);
				channelView.setMessagesAdapter(adapter);
			}
			else
			{
				MessagesAdapter adapter = new MessagesAdapter(activity, new ArrayList<MsgItem>());
				if (i == 1)
					XiaoMiToolManager.getInstance().addAudioListener(adapter);
				channelView.setMessagesAdapter(adapter);
			}

			messagesListView.setAdapter(channelView.getMessagesAdapter());

			messagesListView.setOnScrollListener(mOnScrollListener);
			messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
			messagesListView.setKeepScreenOn(true);

			pullDownToLoadListView.addView(messagesListView);

			channelView.pullDownToLoadListView = pullDownToLoadListView;
			channelView.messagesListView = messagesListView;

			// TODO ??????????????????
			messagesListView.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (channelView != null && channelView.messagesListView != null)
					{
						// ?????????NullPointerException??????
						// channelView.messagesListView.setSelection(channelView.messagesListView.getCount());
					}
				}
			});

			if (chatChannel != null)
			{
				if (chatChannel.lastPosition.x != -1 && rememberPosition)
				{
					channelView.messagesListView.setSelectionFromTop(chatChannel.lastPosition.x, chatChannel.lastPosition.y);
				}
				else
				{
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
			}

			messagesListFrameLayout.addView(pullDownToLoadListView);
		}
		if (lazyLoading)
		{
			refreshTab();
		}
		activity.hideProgressBar();
	}

	protected void refreshTab()
	{
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			showTab(TAB_ALLIANCE);
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			showTab(TAB_COUNTRY);
		}
		else if (ChatServiceController.isInMailDialog())
		{
			showTab(TAB_MAIL);
		}
		refreshWordCount();
	}

	protected void onNetworkConnectionChanged()
	{
	}

	protected void refreshSendButton()
	{
		if (this.replyField.getText().length() == 0)
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

		if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType))
		{
			if (channel.canLoadAllNew())
			{
				getCurrentChannel().setLoadingStart(true);
				oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
				loadMoreCount = 0;
				channel.isLoadingAllNew = true;
				channel.hasLoadingAllNew = true;
				ChannelManager.getInstance().loadAllNew(channel);

				refreshToolTip();
			}
		}
		else
		{
			if (channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
			{
				channel.wsNewMsgCount = 0;
				updateListPositionForOldMsg(channel.channelType, 0, false);
			}
		}
	}

	public void refreshToolTip()
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		// ?????????????????????channel?????????
		if (channel == null || isInMail())
		{
			return;
		}

		if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType))
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
			String newMsgCount = channel.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.getNewMsgCount() + ""
					: ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
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
		// ???????????????????????????????????????channel.wsNewMsgCount
		// TODO ?????????????????????????????????????????????
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

	private void refreshBottomUI(boolean isChecked)
	{
		if (!isChecked)
			ChatServiceController.isHornItemUsed = false;
		boolean isHornUI = isChecked && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY
				&& ConfigManager.enableChatHorn;
		String background = isHornUI ? "btn_green3" : "btn_yellow";
		String bottomBg = isHornUI ? "bottom_bg" : "chuzheng_frame02";
		String inputBg = isHornUI ? "input" : "text_field_bg2";
		addReply.setBackgroundResource(ResUtil.getId(activity, "drawable", background));
		relativeLayout1.setBackgroundResource(ResUtil.getId(activity, "drawable", bottomBg));
		replyField.setBackgroundResource(ResUtil.getId(activity, "drawable", inputBg));
		// horn_tip_layout.setVisibility(isHornUI ? View.VISIBLE : View.GONE);
		imageView1.setVisibility(isHornUI ? View.GONE : View.VISIBLE);
		setMaxInputLength(isHornUI);
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
	 * ???????????????????????????????????????????????????????????????
	 */
	private LoadMoreMsgParam getLoadMoreMsgParam(int channelType)
	{
		if (!(channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM))
		{
			return null;
		}
		ChatChannel channel = ChannelManager.getInstance().getChannel(channelType);
		if (channel == null || channel.msgList == null || channel.getChannelView() == null)
		{
			return null;
		}

		if (!WebSocketManager.isRecieveFromWebSocket(channelType))
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
		// ??????????????????????????????webSocket?????????????????????????????????????????????????????????????????????db??????server??????????????????????????????????????????????????????
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

		// ????????????: ???????????????viewMinSeqId???0???????????????seqId?????????1
		if (viewMinSeqId <= 1)
		{
			return null;
		}

		// desireMaxSeqId????????????desireMinSeqId?????????????????????1???
		int desireMaxSeqId = DBManager.getInstance().getLoadMoreMaxSeqId(channel.getChatTable(), viewMinSeqId);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "desireMaxSeqId", desireMaxSeqId, "viewMinSeqId", viewMinSeqId);
		int desireMinSeqId = (desireMaxSeqId - 19) > 1 ? (desireMaxSeqId - 19) : 1;

		// ??????desireMaxSeqId?????????db???????????????db????????????????????????20??????
		if (DBManager.getInstance().isMsgExists(channel.getChatTable(), desireMaxSeqId, -1))
		{
			return new LoadMoreMsgParam(desireMinSeqId, desireMaxSeqId, false);
		}

		// ??????????????????server???????????????server??????
		// server???seqId??????????????????????????????
		Point inter = getIntersection(new Point(channel.serverMinSeqId, channel.serverMaxSeqId), new Point(desireMinSeqId, desireMaxSeqId));
		if (inter != null)
		{
			return new LoadMoreMsgParam(inter.x, inter.y, true);
		}

		// ?????????db????????????server??????????????????????????????server???????????????db????????????????????????
		Point range = DBManager.getInstance().getHistorySeqIdRange(channel.getChatTable(), desireMaxSeqId, ChannelManager.LOAD_MORE_COUNT);
		if (range != null)
		{
			return new LoadMoreMsgParam(range.x, range.y, false);
		}

		return null;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param sec1
	 *            [sec1.x, sec1.y]???????????????
	 * @param sec2
	 *            [sec2.x, sec2.y]???????????????
	 * @return null??????????????????
	 */
	public static Point getIntersection(Point sec1, Point sec2)
	{
		int[] fourValue = { sec1.x, sec1.y, sec2.x, sec2.y };
		Arrays.sort(fourValue); // ????????????
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
			LogUtil.trackMessage("checkMessagesAdapter() fail: currentChannel = " + getCurrentChannel() + " messagesAdapter = "
					+ (getCurrentChannel() == null ? "null" : getCurrentChannel().getMessagesAdapter()) + " currentChatType = "
					+ ChatServiceController.getCurrentChannelType() + " chatActivity = " + ChatServiceController.getChatActivity()
					+ " chatFragment = " + ChatServiceController.getChatFragment());
			return false;
		}
		return true;
	}

	private void loadMoreMsg()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		// ????????????????????????
		if (channel == null)
			return;
		LoadMoreMsgParam loadMoreMsgParam = getLoadMoreMsgParam(channel.channelType);

		if (!getCurrentChannel().getLoadingStart() && loadMoreMsgParam != null)
		{
			LogUtil.trackPageView("LoadMoreMsg");
			getCurrentChannel().setLoadingStart(true);
			// ??????????????? getCount() on a null object reference
			oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
			loadMoreCount = 0;
			channel.isLoadingAllNew = false;
			if (loadMoreMsgParam.fetchFromServer)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "???server????????????");
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
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "???db????????????");
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

		if (!getCurrentChannel().getLoadingStart() && hasMoreData())
		{
			LogUtil.trackPageView("LoadMoreMail");
			if (ChatServiceController.isNewMailListEnable)
			{
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
				ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
			}
			else
			{
				getCurrentChannel().setLoadingStart(true);
				oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
				loadMoreCount = 0;

				JniController.getInstance().excuteJNIVoidMethod(
						"requestMoreMail",
						new Object[] {
								UserManager.getInstance().getCurrentMail().opponentUid,
								UserManager.getInstance().getCurrentMail().mailUid,
								Integer.valueOf(oldAdapterCount) });
			}
		}
	}

	/**
	 * ????????????????????????????????????????????? server??????????????????GetNewMsg????????? view???????????????????????????????????????
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
				hasMoreData = getLoadMoreMsgParam(ChatServiceController.getCurrentChannelType()) != null;
			}
		}
		else if (ChatServiceController.isNewMailListEnable)
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
			if (channel == null)
			{
				hasMoreData = false;
				return;
			}
			List<MsgItem> dbUserMails = DBManager.getInstance().getMsgsByTime(channel.getChatTable(), channel.getMinCreateTime(), 1);
			hasMoreData = dbUserMails.size() > 0;
		}
	}

	private boolean isInMail()
	{
		return getCurrentChannel().tab == TAB_MAIL && !ChatServiceController.isInChatRoom();
	}

	public static String	chatStatus	= "";

	public static void setConnectionStatus(final String title)
	{
		chatStatus = title;
		if (!ChatServiceController.isInMailDialog() && ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
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

	public boolean hasMoreData()
	{
		if (isInMail())
		{
			if (ChatServiceController.isNewMailListEnable)
			{
				return hasMoreData;
			}
			else
			{
				return !ChannelManager.getInstance().getNoMoreDataFlag(getCurrentChannel().tab);
			}
		}
		else
		{
			return hasMoreData;
		}
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
		stopTimerTask();
		mTimer = new Timer();
		mTimerTask = new TimerTask()
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
							if (getCurrentChannel().pullDownToLoadListView != null)
							{
								getCurrentChannel().pullDownToLoadListView.hideProgressBar();
								resetMoreDataStart(getCurrentChannel().channelType);
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
		if (mTimer != null)
			mTimer.schedule(mTimerTask, 5000);
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

	private boolean				hasMoreData			= true;

	private OnScrollListener	mOnScrollListener	= new AbsListView.OnScrollListener()
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
																if (hasMoreData())
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
		if (getCountryChannelView() != null && getCountryChannelView().getMessagesAdapter() != null)
		{
			// ??????noAllianceFrameLayout???????????????????????????
			getCountryChannelView().getMessagesAdapter().onJoinAnnounceInvitationSuccess();
		}
	}

	public int getToastPosY()
	{
		int[] location = { 0, 0 };
		messagesListFrameLayout.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(activity, 5);
	}

	private final int	TAB_COUNTRY		= 0;
	private final int	TAB_ALLIANCE	= 1;
	private final int	TAB_MAIL		= 2;

	private void showTab(int tab)
	{
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, tab == TAB_COUNTRY);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, tab == TAB_ALLIANCE);

		if (tab == TAB_MAIL)
		{
			buttonsLinearLayout.setVisibility(View.GONE);
		}
		else
		{
			buttonsLinearLayout.setVisibility(View.VISIBLE);
			imageView2.setVisibility(View.VISIBLE);
		}

		boolean isInAlliance = false;
		// ???????????? Attempt to read from field 'java.lang.String
		// com.elex.chatservice.model.UserInfo.allianceId' on a null object
		// reference
		if (UserManager.getInstance().getCurrentUser() != null)
		{
			isInAlliance = UserManager.getInstance().getCurrentUser().allianceId.equals("") ? false : true;
		}

		for (int i = 0; i < getChannelViewCount(); i++)
		{
			if (getChannelView(i).pullDownToLoadListView != null)
			{
				getChannelView(i).pullDownToLoadListView.setVisibility(tab == i ? View.VISIBLE : View.GONE);
				if ((i == TAB_ALLIANCE && !isInAlliance) || (i == TAB_COUNTRY && ChatServiceController.isInDragonSencen()))
				{
					getChannelView(i).pullDownToLoadListView.setVisibility(View.GONE);
				}
			}
		}

		horn_checkbox.setVisibility((tab == 0 && ConfigManager.enableChatHorn) ? View.VISIBLE : View.GONE);

		noAllianceFrameLayout.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.VISIBLE : View.GONE);
		hs__dragon_chat_tip_layout.setVisibility((tab == TAB_COUNTRY && ChatServiceController.isInDragonSencen()) ? View.VISIBLE
				: View.GONE);
		relativeLayout1.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.GONE : View.VISIBLE);

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

		if (tab == TAB_COUNTRY)
		{
			ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_COUNTRY);
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
		}
		else
		{
			replyField.setEnabled(true);
			replyField.setHint("");
			ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
			if (channel != null && StringUtils.isNotEmpty(channel.draft))
				replyField.setText(channel.draft);
			else
				replyField.setText("");
			if (tab == TAB_ALLIANCE)
			{
				ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_ALLIANCE);
				refreshBottomUI(false);
			}
			else
			{
				refreshBottomUI(false);
			}
		}

		setChannelViewIndex(tab);

		if (checkMessagesAdapter())
		{
			oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
			refreshToolTip();
			this.refreshHasMoreData();

			if (getCurrentChannel().chatChannel != null)
			{
				getCurrentChannel().chatChannel.getTimeNeedShowMsgIndex();
				getCurrentChannel().chatChannel.markAsRead();
			}
		}
	}

	int	mIndex	= 0;

	private void refreshWordCount()
	{
		if (replyField == null || wordCount == null)
			return;

		if (replyField.getLineCount() > 2)
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

	private final static int	sendButtonBaseWidth		= 173;
	private final static int	sendButtonBaseHeight	= 84;
	private final static int	hornCheckBoxWidth		= 70;

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
			// S3??????????????????(??????????????????S3???????????????????????????????????????????????????????????????)
			// addReply??????????????????1/4?????????????????????????????????????????????????????????????????????textRatio???
			double sendButtonRatio = (double) sendButtonBaseHeight / (double) sendButtonBaseWidth;
			float hornRatio = (float) (addReply.getWidth() * sendButtonRatio / hornCheckBoxWidth);
			ViewHelper.setScaleX(horn_checkbox, hornRatio > 1 ? 1 : hornRatio);
			ViewHelper.setScaleY(horn_checkbox, hornRatio > 1 ? 1 : hornRatio);

			addReply.setLayoutParams(new LinearLayout.LayoutParams(addReply.getWidth(), (int) (addReply.getWidth() * sendButtonRatio)));
			LinearLayout.LayoutParams buttonCountryParams = (LinearLayout.LayoutParams) buttonCountry.getLayoutParams();
			buttonCountryParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonCountry.setLayoutParams(buttonCountryParams);

			LinearLayout.LayoutParams buttonAllianceParams = (LinearLayout.LayoutParams) buttonAlliance.getLayoutParams();
			buttonAllianceParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonAlliance.setLayoutParams(buttonAllianceParams);

			LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams((int) (13 * ConfigManager.scaleRatio),
					(int) (17 * ConfigManager.scaleRatio), 1);
			param3.gravity = Gravity.CENTER_VERTICAL;
			tooltipArrow.setLayoutParams(param3);

			// TODO ???????????????
			// 9.png??????????????????????????????????????????????????????????????????
			// RelativeLayout.LayoutParams param2 = new
			// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
			// LayoutParams.WRAP_CONTENT);
			// param2.setMargins(dip2px(activity, -4), dip2px(activity, -2),
			// dip2px(activity, -1), 0);
			// param2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			// imageView2.setLayoutParams(param2);
			// imageView2.setScaleType(ScaleType.FIT_XY);
			// ViewHelper.setScaleY(imageView2, (float) scaleRatioButton);

			ScaleUtil.adjustTextSize(addReply, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(wordCount, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonCountry, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonAlliance, ConfigManager.scaleRatio);

			ScaleUtil.adjustTextSize(buttonJoinAlliance, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noAllianceTipText, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(dragon_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tooltipLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_scroll_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_name, ConfigManager.scaleRatio);

			adjustSizeCompleted = true;

			if (lazyLoading)
			{
				activity.showProgressBar();
				onBecomeVisible();
			}
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

	protected static boolean	dataChanged	= false;

	@Override
	public void onResume()
	{
		super.onResume();
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM)
			getTitleLabel().setText(UserManager.getInstance().getCurrentMail().opponentName);
		if (dataChanged && getCurrentChannel()!=null && getCurrentChannel().chatChannel!=null)
		{
			System.out.println("chatfragment onMsgAdd");
			notifyDataSetChanged(ChatServiceController.getCurrentChannelType(),getCurrentChannel().chatChannel.channelID,true);
		}

	}

	protected void onContentAreaTouched()
	{
		hideSoftKeyBoard();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "onDestroy");
		dataChanged = false;
		XiaoMiToolManager.getInstance().isRecordVoice = false;
		XiaoMiToolManager.getInstance().exitChat = false;
		XiaoMiToolManager.getInstance().stopPlayVoice();
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			ChatServiceController.getInstance().setGameMusiceEnable(true);
		ChatServiceController.contactMode = 0;
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

		noAllianceFrameLayout = null;
		relativeLayout1 = null;
		buttonsLinearLayout = null;
		imageView1 = null;
		imageView2 = null;
		// horn_tip_layout = null;
		// horn_text_tip = null;
		wordCount = null;
		messageBox = null;
		// header = null;
		buttonCountry = null;
		buttonAlliance = null;
		tooltipLayout = null;
		tooltipLabel = null;
		tooltipArrow = null;
		buttonJoinAlliance = null;
		noAllianceTipText = null;
		hs__dragon_chat_tip_layout = null;
		dragon_chat_tip_text = null;

		((ChatActivity) getActivity()).fragment = null;

		super.onDestroy();
	}

	protected ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;
	private TextWatcher									textChangedListener;
}