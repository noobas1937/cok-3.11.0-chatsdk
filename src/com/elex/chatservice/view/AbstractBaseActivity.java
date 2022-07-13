package com.elex.chatservice.view;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.actionbar.RecyclerActionBarActivity;
import com.elex.chatservice.view.adapter.AbstractBaseAdapter;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshSwipeListView;

public abstract class AbstractBaseActivity extends RecyclerActionBarActivity implements OnClickListener
{
	protected AbstractBaseAdapter			adapter					= null;
	protected SwipeMenuListView				mListView;
	private FrameLayout						channel_list_layout;
	protected PullToRefreshSwipeListView	channelListPullView;
	protected TextView						tip_no_mail_textView;
	private LinearLayout					mailButtonBarLayout;
	private ImageView						mailButtonBarWrite;
	private ImageView						mailButtonBarReward;
	private LinearLayout					mail_reward_layout;
	private CheckBox						mailButtonBarAll;
	private ImageView						mailButtonBarDelete;
	private CheckBox						mailButtonBarUnread;
	public int								channelType;
	public String							channelId				= "";
	public boolean							mailReadStateChecked	= false;
	private boolean							isInEditMode			= false;
	protected boolean						adjustSizeCompleted		= false;
	public Button							actionbar_writeButton;
	public Button							actionbar_editButton;
	public Button							actionbar_returnButton;

	public static final int					LOAD_MORE_COMPLELETD	= 0x1;

	public Handler							mHandler				= new Handler()
																	{
																		@Override
																		public void handleMessage(Message msg)
																		{
																			super.handleMessage(msg);
																			switch (msg.what)
																			{
																				case LOAD_MORE_COMPLELETD:
																					onLoadMoreComplete();
																					break;
																				default:
																					break;
																			}
																		};
																	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate START");
		Bundle extras = getIntent().getExtras();
		boolean isSecondLvList = false;
		boolean isGoBack = false;
		if (extras != null)
		{
			if (extras.containsKey("channelType"))
			{
				channelType = extras.getInt("channelType");
				ChatServiceController.setCurrentChannelType(channelType);
			}

			if (extras.containsKey("isSecondLvList"))
				isSecondLvList = extras.getBoolean("isSecondLvList");
			if (extras.containsKey("isGoBack"))
				isGoBack = extras.getBoolean("isGoBack");
			if (extras.containsKey("channelId"))
				channelId = extras.getString("channelId");
		}

		if (!isSecondLvList)
		{
			if (ChannelManager.isMainMsgChannel(channelId))
			{
				if (!isGoBack)
					LogUtil.trackPageView("ShowChannelList-" + channelId);
			}
			else
			{
				if (!isGoBack && !canJumpToSecondaryList())
					LogUtil.trackPageView("ShowChannelList");
			}
		}
		else
		{
			if (!isGoBack)
				LogUtil.trackPageView("ShowChannelList-" + channelId);
		}

		super.onCreate(savedInstanceState);
		initView();
		createAdapter();
		mListView.setAdapter(adapter);
		refreshScrollLoadEnabled();
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "onCreate END");
	}

	public void notifyLoadMoreCompleted()
	{
		if (mHandler != null)
		{
			mHandler.sendEmptyMessage(LOAD_MORE_COMPLELETD);
		}
	}

	public boolean isInEditMode()
	{
		return isInEditMode;
	}

	public SwipeMenuListView getListView()
	{
		return mListView;
	}

	public SwipeMenuCreator initSwipeMenuCreator()
	{
		return null;
	}

	public OnMenuItemClickListener initSwipeMenuItemClickListener()
	{
		return null;
	}

	private OnGlobalLayoutListener					onGlobalLayoutListener	= new ViewTreeObserver.OnGlobalLayoutListener()
																			{
																				public void onGlobalLayout()
																				{
																					adjustHeight();
																				}
																			};

	private OnRefreshListener<SwipeMenuListView>	refreshlistener			= new OnRefreshListener<SwipeMenuListView>()
																			{
																				@Override
																				public void onPullDownToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
																				{
																				}

																				@Override
																				public void onPullUpToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
																				{
																					if (adapter != null)
																					{
																						if (mailReadStateChecked && adapter.hasMoreUnreadData())
																						{
																							adapter.loadMoreUnreadData();
																						}
																						else if (!mailReadStateChecked && adapter.hasMoreData())
																						{
																							adapter.loadMoreData();
																						}
																						LogUtil.trackPageView("LoadMoreList-" + channelId);
																					}
																				}
																			};

	@Override
	public void onClick(View v)
	{
		int viewId = v.getId();
		if (viewId == R.id.actionbar_editButton)
		{
			enterEditMode();
		}
		else if (viewId == R.id.actionbar_returnButton)
		{
			exitEditMode();
		}
		else if (viewId == R.id.mailButtonBarDelete)
		{
			operateMultiple(ChannelManager.OPERATION_DELETE_MUTI);
			LogUtil.trackAction("click_" + channelId + "_deletebar");
		}
		else if (viewId == R.id.mailButtonBarWrite)
		{
			writeNewMail();
			exitEditMode();
			LogUtil.trackAction("click_" + channelId + "_writebar");
		}
		else if (viewId == R.id.mailButtonBarReward)
		{
			operateMultiple(ChannelManager.OPERATION_REWARD_MUTI);
			LogUtil.trackAction("click_" + channelId + "_rewardbar");
		}

	}

	public void selectedAll(boolean checked)
	{
		mailButtonBarAll.setChecked(checked);
		if (adapter != null && adapter.list != null)
		{
			for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
			{
				ChannelListItem item = (ChannelListItem) iterator.next();
				if (item != null)
					item.checked = checked;
			}
			notifyDataSetChanged();
		}
	}

	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener()
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			int viewId = buttonView.getId();
			if (viewId == R.id.mailButtonBarAll)
			{
				selectedAll(isChecked);
			}
			else if (viewId == R.id.mailButtonBarUnread)
			{
				if (isChecked)
				{
					mailReadStateChecked = true;
					adapter.reloadData();
					LogUtil.trackAction("check" + channelId + "_unreadbar");
				}
				else
				{
					mailReadStateChecked = false;
					adapter.reloadData();
					LogUtil.trackAction("uncheck" + channelId + "_unreadbar");
				}
				refreshScrollLoadEnabled();
			}
		}
	};

	private void initView()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView START");

		channelListPullView = (PullToRefreshSwipeListView) findViewById(R.id.channelListPullView);
		channelListPullView.setLanguage(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		mListView = channelListPullView.getRefreshableView();
		mListView.setCacheColorHint(0x00000000);
		mListView.setDivider(null);
		ImageUtil.setYRepeatingBG(this, channelListPullView, R.drawable.mail_list_bg);

		channelListPullView.setOnRefreshListener(refreshlistener);
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView 3");

		actionbar_editButton = (Button) findViewById(R.id.actionbar_editButton);
		actionbar_editButton.setOnClickListener(this);
		actionbar_returnButton = (Button) findViewById(R.id.actionbar_returnButton);
		actionbar_returnButton.setOnClickListener(this);

		channel_list_layout = (FrameLayout) findViewById(R.id.channel_list_layout);
		tip_no_mail_textView = (TextView) findViewById(R.id.tip_no_mail);
		tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_MAIL));

		mailButtonBarLayout = (LinearLayout) findViewById(R.id.mailButtonBarLayout);
		mailButtonBarWrite = (ImageView) findViewById(R.id.mailButtonBarWrite);
		mailButtonBarReward = (ImageView) findViewById(R.id.mailButtonBarReward);
		mail_reward_layout = (LinearLayout) findViewById(R.id.mail_reward_layout);
		if (StringUtils.isNotEmpty(channelId) && (channelId.equals(MailManager.CHANNELID_RECYCLE_BIN) || ChannelManager.isMainMsgChannel(channelId)))
			mail_reward_layout.setVisibility(View.GONE);
		else
			mail_reward_layout.setVisibility(View.VISIBLE);
		mailButtonBarAll = (CheckBox) findViewById(R.id.mailButtonBarAll);
		mailButtonBarAll.setChecked(false);
		mailButtonBarDelete = (ImageView) findViewById(R.id.mailButtonBarDelete);
		mailButtonBarUnread = (CheckBox) findViewById(R.id.mailButtonBarUnread);
		mailButtonBarUnread.setChecked(false);
		mailReadStateChecked = false;
		showBottomBar(false);
		refreshTitleLabel();
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView 5");

		mailButtonBarAll.setOnCheckedChangeListener(checkedChangeListener);
		mailButtonBarUnread.setOnCheckedChangeListener(checkedChangeListener);
		mailButtonBarDelete.setOnClickListener(this);
		mailButtonBarWrite.setOnClickListener(this);
		mailButtonBarReward.setOnClickListener(this);

		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView 6");
		mListView.setMenuCreator(initSwipeMenuCreator());
		mListView.setOnMenuItemClickListener(initSwipeMenuItemClickListener());

		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView 7");
		channel_list_layout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "initView END");
	}

	protected static boolean dataChanged = false;

	public static void onChannelAdd()
	{
		dataChanged = true;
		if (ChatServiceController.getBaseListActivity() != null)
		{
			try
			{
				ChatServiceController.getBaseListActivity().reload();
				dataChanged = false;
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public static void onMailAdded()
	{
		onChannelAdd();
	}

	public static void onMsgAdded(ChatChannel channel)
	{
		if (channel == null)
			return;
		ChannelManager.getInstance().addToLoadedChannel(channel);

		if (ChatServiceController.getBaseListActivity() != null)
		{
			AbstractBaseAdapter adapter = ChatServiceController.getBaseListActivity().adapter;
			if (adapter != null && adapter.list != null)
			{
				for (int i = 0; i < adapter.list.size(); i++)
				{
					ChannelListItem item = adapter.list.get(i);
					if (item instanceof ChatChannel && channel.channelID.equals(((ChatChannel) item).channelID))
					{
						dataChanged = false;
						adapter.refreshOrder();
						return;
					}
				}
			}
		}
		onChannelAdd();
	}

	public void reload()
	{
		if (adapter != null)
			adapter.reloadData();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (StringUtils.isNotEmpty(channelId))
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "channelId+AbstractBaseActivity",channelId);
			if (channelId.equals(MailManager.CHANNELID_MOD))
				ChatServiceController.contactMode = 1;
			else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
				ChatServiceController.contactMode = 2;
			else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
				ChatServiceController.contactMode = 3;
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "channelId+AbstractBaseActivity+赋值=0",channelId);

				ChatServiceController.contactMode = 0;
			}
				
		}

		refreshTitleLabel();

		if (dataChanged || ChannelManager.getInstance().isInRootChannelList)
		{
			reload();
			dataChanged = false;
		}
		if (ChannelManager.getInstance().isInRootChannelList)
			ChannelManager.getInstance().isInRootChannelList = false;
	}

	public void refreshScrollLoadEnabled()
	{
		if (channelListPullView.isPullLoadEnabled())
			channelListPullView.setPullLoadEnabled(false);
		if (channelListPullView.isPullRefreshEnabled())
			channelListPullView.setPullRefreshEnabled(false);
		if (channelListPullView.isScrollLoadEnabled())
			channelListPullView.setScrollLoadEnabled(false);
	}

	public void setNoMailTipVisible(boolean isVisble)
	{
		if (tip_no_mail_textView != null)
		{
			if (isVisble)
			{
				if (MailManager.hasMoreNewMailToGet)
					tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
				else
				{
					if (mailReadStateChecked)
						tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_UNREAD_MAIL));
					else
						tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_MAIL));
				}
				tip_no_mail_textView.setVisibility(View.VISIBLE);
			}
			else
			{
				tip_no_mail_textView.setVisibility(View.GONE);
			}
		}
	}

	public void refreshTitle()
	{
		if (AbstractBaseActivity.this != null)
		{
			AbstractBaseActivity.this.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					refreshTitleLabel();
				}
			});
		}
	}

	public void refreshTitleLabel()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg || MailManager.hasMoreNewMailToGet)
		{
			titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		}
		else
		{
			setTitleLabel();
		}
	}

	public void setTitleLabel()
	{
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MAIL));
	}

	protected void writeNewMail()
	{
		ServiceInterface.showWriteMailActivity(this, false, null, null, null);
	}

	public void onLoadMoreComplete()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		try
		{
			channelListPullView.onPullDownRefreshComplete();
			channelListPullView.onPullUpRefreshComplete();
			

			if (adapter != null)
			{
				adapter.isLoadingMore = false;
				adapter.refreshAdapterList();
			}
			refreshScrollLoadEnabled();
			hideProgressBar();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	protected void restorePosition()
	{
	}

	protected void enterEditMode()
	{
		isInEditMode = true;
		showEditButton(false);
		showBottomBar(true);
		notifyDataSetChanged();
	}

	protected void exitEditMode()
	{
		isInEditMode = false;
		showEditButton(true);
		showBottomBar(false);
		notifyDataSetChanged();
	}

	public void notifyDataSetChanged()
	{
		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			if (adapter != null)
				adapter.notifyDataSetChangedOnUI();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

	}

	protected void openChannel(ChatChannel channel)
	{
		ChatServiceController.isCreateChatRoom = false;
		// 打开具体聊天
		if (channel.channelType < DBDefinition.CHANNEL_TYPE_OFFICIAL || channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			{
				if (channel.isMainMsgChannel())
				{
					ServiceInterface.showMaillListActivity(this, false, DBDefinition.CHANNEL_TYPE_USER, channel.channelID,
							false);
					LogUtil.trackPageView("openChannel_" + channel.channelID);
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "fromUid", channel.channelID,
							"channel.customName:", channel.getCustomName());
					int mailType = MailManager.MAIL_USER;
					if (channel.isModChannel())
						mailType = MailManager.MAIL_MOD_PERSONAL;
					else if (channel.isDriftingBottleChannel())
						mailType = MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND;

					if (channel.isNearbyChannel())
					{
						ServiceInterface.setMailInfo(3, channel.channelID, channel.getCustomName());
					}
					else
					{
						ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), mailType);
					}
					if (channel.isNearbyChannel() && NearByManager.getInstance().getEnter_list_type() == 2)
						LogUtil.trackNearby("nearbychat_from_newTip");
					ServiceInterface.showChatActivity(this, channel.channelType, false);
					if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && channel.isNotMainMsgChannel() && StringUtils.isNotEmpty(channel.latestId))
					{
						if (ChatServiceController.isModContactMode())
						{
							String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
							JniController.getInstance()
									.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(1) });
						}
						else if (ChatServiceController.isDriftingBottleContactMode())
						{
							String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
							JniController.getInstance()
									.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(2) });
						}
						else if (ChatServiceController.isNearbyContactMode())
						{
							String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
							if (!SwitchUtils.mqttEnable)
								WebSocketManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
							else
								MqttManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
						}
						else
						{
							JniController.getInstance().excuteJNIVoidMethod("readChatMail",
									new Object[] { channel.channelID, Integer.valueOf(0) });
						}
					}
				}
			}
			else
			{
				ServiceInterface.showChatActivity(this, channel.channelType, false);
			}
		}
		// 打开二级列表
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "channelID", channel.channelID);
			if (channel.channelID.equals(MailManager.CHANNELID_MONSTER) || channel.channelID.equals(MailManager.CHANNELID_RESOURCE)
					|| channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP)
					|| channel.channelID.equals(MailManager.CHANNELID_KNIGHT)
					|| channel.channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			{
				MailData mail = null;
				if (channel.channelID.equals(MailManager.CHANNELID_MONSTER))
					mail = channel.getMonsterMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE))
					mail = channel.getResourceMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT))
					mail = channel.getKnightMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
					mail = channel.getResourceHelpMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
					mail = channel.getNewWorldBossMailData();

				if (mail != null)
				{
					transportAndShowMailData(mail);
					if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT))
					{
						String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
						if (StringUtils.isNotEmpty(uids))
						{
							JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { uids });
						}
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
								new Object[] { Integer.valueOf(mail.getType()), Boolean.valueOf(false), "" });
					}
					LogUtil.trackPageView("openChannel_" + channel.channelID);
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_ALL, "resource or monster mail is null！");
				}
			}
			else
			{
				ServiceInterface.showRecyclerMaillListActivity(this, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, channel.channelID,
						false);
				LogUtil.trackPageView("openChannel_" + channel.channelID);
			}

		}
	}

	public void transportAndShowMailData(MailData mailData)
	{
		if (ChatServiceController.getInstance().isUsingDummyHost())
			return;
		System.out.println("transportAndShowMailData");
		if (mailData != null)
		{
			if (MailManager.getInstance().isInTransportedMailList(mailData.getUid()))
			{
				System.out.println("transportAndShowMailData isInTransportedMailList");
				MailManager.getInstance().setShowingMailUid("");

				if (mailData.hasReward())
				{
					LocalConfig config = ConfigManager.getInstance().getLocalConfig();
					boolean hasShowed = (config != null && config.isFirstRewardTipShowed());
					JniController.getInstance().excuteJNIVoidMethod("postFirstRewardAnimationShowed", new Object[] { Boolean.valueOf(hasShowed) });
				}
				ChatServiceController.doHostAction("showMailPopup", mailData.getUid(), "", "", true, true);
			}
			else
			{
				System.out.println("transportAndShowMailData not isInTransportedMailList:" + mailData.getUid());
				MailManager.getInstance().setShowingMailUid(mailData.getUid());
				MailManager.getInstance().transportMailData(mailData);
				showProgressBar();
			}

			if (!mailData.getChannelId().equals(MailManager.CHANNELID_RESOURCE)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_KNIGHT)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_MONSTER)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			{
				MailManager.getInstance().transportNeiberMailData(mailData, true, true);
			}

		}
	}

	public boolean handleBackPressed()
	{
		if (isInEditMode)
		{
			exitEditMode();
			return true;
		}
		else
		{
			return false;
		}
	}

	protected void showEditButton(boolean show)
	{
		actionbar_editButton.setVisibility(show ? View.VISIBLE : View.GONE);
		actionbar_returnButton.setVisibility(!show ? View.VISIBLE : View.GONE);
	}

	private void showBottomBar(boolean show)
	{
		mailButtonBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			adjustSizeCompleted = true;
			return;
		}

		if (!adjustSizeCompleted)
		{
			adjustSizeExtend();
			adjustSizeCompleted = true;
		}
	}

	protected void adjustSizeExtend()
	{

	}

	protected static int	lastScrollX				= -1;
	protected static int	lastScrollY				= -1;
	protected static int	secondLastScrollX		= -1;
	protected static int	secondLastScrollY		= -1;
	public static boolean	rememberSecondChannelId;
	public static boolean	preventSecondChannelId	= false;
	protected static String	lastSecondChannelId		= "";

	public static boolean canJumpToSecondaryList()
	{
		return rememberSecondChannelId && StringUtils.isNotEmpty(lastSecondChannelId);
	}

	protected Point getCurrentPos()
	{
		if (mListView == null)
		{
			return null;
		}
		int x = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		int y = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());

		return new Point(x, y);
	}

	protected void onDeleteMenuClick(int position)
	{
	}

	protected void onReadMenuClick(int position)
	{
	}

	protected void onClearMenuClick(int position)
	{
	}

	protected void onRewardAllMenuClick(int position)
	{
	}

	protected void deleteDummyItem(int position)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		adapter.list.remove(position);
		notifyDataSetChanged();
	}

	protected void readDummyItem(int position)
	{
	}

	protected void readChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			actualReadSingleChannel(channel);
			notifyDataSetChanged();
		}
	}

	protected void deleteChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel == null)
			return;

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			actualDeleteSingleChannel(channel, position);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			boolean hasCannotDeleteMail = channel.cannotOperatedForMuti(ChannelManager.OPERATION_DELETE_MUTI);
			String content = "";
			if (hasCannotDeleteMail)
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK);
			else
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			MenuController.showDeleteRecyclerChannelConfirm(content, channel, position);
		}
	}

	protected void deleteSysMail(int position)
	{
		if (adapter.getCount() <= 0)
			return;

		MailData mailData = (MailData) adapter.getItem(position);
		if (mailData == null)
			return;
		if (!mailData.canDelete())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "rewardid", mailData.getRewardId(), "rewardStatus",
					mailData.getRewardStatus());
			if (mailData.hasReward())
			{
				MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD));
			}
			else if (mailData.getSave() == 1)
			{
				MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_LOCK));
			}
		}
		else
		{
			if (ChatServiceController.getBaseListActivity() != null)
				ChatServiceController.getBaseListActivity().actualDeleteSingleSysMail(mailData);
		}
	}

	private void operateMultiple(int type)
	{
		ArrayList<ChannelListItem> checkedItems = new ArrayList<ChannelListItem>();
		for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
		{
			ChannelListItem item = (ChannelListItem) iterator.next();
			if (item.checked)
			{
				checkedItems.add(item);
			}
		}

		String content = "";
		boolean hasCannotOperateMutiMail = false;

		boolean hasMailData = false;
		for (int i = 0; i < checkedItems.size(); i++)
		{
			ChannelListItem item = checkedItems.get(i);
			if (item == null)
				continue;

			if (item instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) item;
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && channel.cannotOperatedForMuti(type))
				{
					hasCannotOperateMutiMail = true;
					break;
				}
			}
			else if (item instanceof MailData)
			{
				MailData mailData = (MailData) item;
				if (mailData != null
						&& mailData.channel != null
						&& mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL
						&& ((type == ChannelManager.OPERATION_DELETE_MUTI && !mailData.canDelete()) || (type == ChannelManager.OPERATION_REWARD_MUTI && mailData
								.hasReward())))
				{
					hasCannotOperateMutiMail = true;
					hasMailData = true;
					break;
				}
			}
		}

		if (type == ChannelManager.OPERATION_REWARD_MUTI)
		{
			if (hasMailData)
				hasCannotOperateMutiMail = false;
		}

		if (hasCannotOperateMutiMail || (type == ChannelManager.OPERATION_REWARD_MUTI && !hasCannotOperateMutiMail && !hasMailData))
		{
			if (type == ChannelManager.OPERATION_REWARD_MUTI)
				content = LanguageManager.getLangByKey(LanguageKeys.TIP_MAIL_NOREWARD);
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK) + "\n"
						+ LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
		}
		else
		{
			if (type == ChannelManager.OPERATION_REWARD_MUTI)
				content = LanguageManager.getLangByKey(LanguageKeys.TIP_REWARD_THESE_MAIL);
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
		}

		MenuController.showRecyclerOperateMutiMail(content, checkedItems, type);
	}

	public void actualDeleteSingleSysMail(MailData mailData)
	{
		if (mailData == null)
			return;
		JniController.getInstance().excuteJNIVoidMethod("deleteSingleMail",
				new Object[] { Integer.valueOf(mailData.tabType), Integer.valueOf(mailData.getType()), mailData.getUid(), "" });
		ChannelManager.getInstance().deleteSysMailFromChannel(mailData.channel, mailData, false);
		adapter.list.remove(mailData);
		adapter.notifyDataSetChangedOnUI();
	}

	public void actualDeleteSingleChannel(ChatChannel channel, int position)
	{
		List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
		channels.add(channel);
		actualDeleteChannels(channels);
	}

	public void actualReadSingleChannel(ChatChannel channel)
	{
		if (channel == null)
			return;
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				JniController.getInstance().excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(1) });
			}
			else if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				JniController.getInstance()
						.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(2) });
			}
			else if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
				else
					MqttManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
			}
			else
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				JniController.getInstance().excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(0) });
			}
		}
		channel.markAsRead();
	}

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
	}

	protected void actualDeleteChannels(List<ChannelListItem> channels)
	{
		String uids = "";
		String userUid = "";
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) channels.get(i);
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
						|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
						|| channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
					continue;
				
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "delete channelType "+ channel.channelType + " channelID:"+channel.channelID);

				if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				{
					String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
					if (StringUtils.isNotEmpty(fromUid))
						userUid = ChannelManager.appendStr(userUid, fromUid);
					// ChannelManager.getInstance().deleteChannel(channel);
					adapter.list.remove(channel);
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				{
					ChannelManager.getInstance().deleteChannel(channel);
					adapter.list.remove(channel);
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					List<String> uidArray = channel.getChannelDeleteUidArray();
					String mailUids = getUidsByArray(uidArray);
					uids = ChannelManager.appendStr(uids, mailUids);

					boolean hasCannotDeleteMail = channel.cannotOperatedForMuti(ChannelManager.OPERATION_DELETE_MUTI);
					if (hasCannotDeleteMail)
					{
						if (channel.mailDataList != null)
						{
							for (int j = 0; j < channel.mailDataList.size(); j++)
							{
								MailData mailData = channel.mailDataList.get(j);
								if (mailData != null && mailData.canDelete())
								{
									ChannelManager.getInstance().deleteSysMailFromChannel(channel, mailData, true);
								}
							}
						}

						DBManager.getInstance().deleteSysMailChannel(channel.getChatTable());
						channel.querySysMailCountFromDB();
						channel.queryUnreadSysMailCountFromDB();
						ChannelManager.getInstance().calulateAllChannelUnreadNum();

					}
					else
					{
						ChannelManager.getInstance().deleteChannel(channel);
						adapter.list.remove(channel);
					}

				}
			}
		}

		notifyDataSetChanged();
		if (StringUtils.isNotEmpty(userUid))
		{
			if (ChatServiceController.isNearbyContactMode())
			{
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().deleteUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, userUid);
				else
					MqttManager.getInstance().deleteUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, userUid);
			}
			else
				JniController.getInstance().excuteJNIVoidMethod("deleteMutiChatMail", new Object[] { userUid, Integer.valueOf(ChatServiceController.contactMode) });
		}
		if (StringUtils.isNotEmpty(uids))
			JniController.getInstance().excuteJNIVoidMethod("deleteMutiMail", new Object[] { uids, "" });
	}

	public void afterDeleteMsgChannel()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		if (AbstractBaseActivity.this != null)
		{
			AbstractBaseActivity.this.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						boolean hasMoreData = false;
						if (ChatServiceController.isModContactMode())
						{
							List<ChatChannel> loadedModChannelList = ChannelManager.getInstance().getLoadedModChannel();
							List<ChatChannel> allModChannelList = ChannelManager.getInstance().getAllModChannel();
							if (loadedModChannelList != null && loadedModChannelList.size() == 0
									&& allModChannelList != null && loadedModChannelList.size() < allModChannelList.size())
							{
								hasMoreData = true;
							}
						}
						else
						{
							List<ChatChannel> loadedMessageChannelList = ChannelManager.getInstance().getLoadedMessageChannel();
							List<ChatChannel> allMessageChannelList = ChannelManager.getInstance().getAllMessageChannel();
							if (loadedMessageChannelList != null && loadedMessageChannelList.size() == 0
									&& allMessageChannelList != null && loadedMessageChannelList.size() < allMessageChannelList.size())
							{
								hasMoreData = true;
							}
						}

						if (adapter != null)
						{
							if (hasMoreData)
								adapter.loadMoreData();
							else
								adapter.refreshAdapterList();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	protected void actualRewardChannels(List<ChannelListItem> channels)
	{
		String uids = "";
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) channels.get(i);
				if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					List<String> uidArray = channel.getChannelRewardUidArray();
					String mailUids = getUidsByArray(uidArray);
					uids = ChannelManager.appendStr(uids, mailUids);
				}
			}
		}

		if (StringUtils.isNotEmpty(uids))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" });
			showRewardLoadingPopup();
		}
	}

	public void showRewardLoadingPopup()
	{

	}

	public static String getUidsByArray(List<String> uidArray)
	{
		String uids = "";
		for (int i = 0; i < uidArray.size(); i++)
		{
			String uid = uidArray.get(i);
			if (!uid.equals("") && !uids.contains(uid))
			{
				if (uids.equals(""))
					uids = uid;
				else
					uids += "," + uid;
			}
		}
		return uids;
	}

	public void openItem(ChannelListItem item)
	{
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		super.onDestroy();

		mailReadStateChecked = false;

		if (mListView != null)
		{
			mListView.setMenuCreator(null);
			mListView.setOnMenuItemClickListener(null);
		}
		if (channelListPullView != null)
			channelListPullView.setOnRefreshListener(null);
		if (mailButtonBarAll != null)
			mailButtonBarAll.setOnClickListener(null);
		if (mailButtonBarDelete != null)
			mailButtonBarDelete.setOnClickListener(null);
		if (mailButtonBarWrite != null)
			mailButtonBarWrite.setOnClickListener(null);
		if (actionbar_editButton != null)
			actionbar_editButton.setOnClickListener(null);
		if (actionbar_returnButton != null)
			actionbar_returnButton.setOnClickListener(null);
	}

	public abstract void createAdapter();
}
