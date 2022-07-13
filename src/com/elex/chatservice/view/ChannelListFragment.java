package com.elex.chatservice.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuLayout;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ApplicationItem;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.FlyMutiRewardInfo;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.ActionBarFragment;
import com.elex.chatservice.view.adapter.AbstractMailListAdapter;
import com.elex.chatservice.view.adapter.MainChannelAdapter;
import com.elex.chatservice.view.adapter.SysMailAdapter;
import com.elex.chatservice.view.banner.BannerAdapter;
import com.elex.chatservice.view.banner.BannerInfo;
import com.elex.chatservice.view.banner.ViewFlow;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshSwipeListView;
import com.nineoldandroids.view.ViewHelper;

public class ChannelListFragment extends ActionBarFragment
{
	protected AbstractMailListAdapter		adapter					= null;

	protected SwipeMenuListView				mListView;
	private FrameLayout						channel_list_layout;
	protected PullToRefreshSwipeListView	channelListPullView;
	protected TextView						tip_no_mail_textView;
	private RelativeLayout					channelListFragmentLayout;
	private ImageView						introducer_hand;
	private LinearLayout					mailButtonBarLayout;
	private ImageView						mailButtonBarWrite;
	private ImageView						mailButtonBarReward;
	private RelativeLayout					mail_reward_layout;
	private View							mailButtonBarAll;
	private TextView						checkboxLabel;
	private ImageView						mailButtonBarDelete;
	private CheckBox						mailButtonBarUnread;
	private ViewFlow						mViewFlow;
	private BannerAdapter					mBannerAdapter;

	public String							channelId				= "";
	private boolean							allSelectedValue		= false;
	public boolean							mailReadStateChecked	= false;

	protected ChannelListActivity			channelListActivity;

	public boolean isInEditMode()
	{
		return isInEditMode;
	}

	public SwipeMenuListView getListView()
	{
		return mListView;
	}

	private void initBanner()
	{
		if (!ChannelManager.bannerEnable || (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_NEAR_BY)))
			return;
		try
		{
			List<BannerInfo> list = ChannelManager.getInstance().getBannerInfoList();
			if (list != null && list.size() > 0)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "list size:", list.size());
				mBannerAdapter = new BannerAdapter(activity, list);
				if (mBannerAdapter != null)
				{
					mViewFlow.setVisibility(View.VISIBLE);
					mViewFlow.setAdapter(mBannerAdapter);
					mViewFlow.setmSideBuffer(list.size());
					mViewFlow.setCurrentAdapterIndex(list.size() * 1000);
					// if(activity!=null)
					// mViewFlow.setSelection(list.size()*1000);
					// mViewFlow.setFlowIndicator(mFlowIndicator);
					mViewFlow.setTimeSpan(8000);
					mViewFlow.startAutoFlowTimer();
				}
				else
				{
					mViewFlow.setVisibility(View.GONE);
				}
			}
			else
				mViewFlow.setVisibility(View.GONE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MAIL, "ChannelListFragment");
		this.activity = (ChannelListActivity) getActivity();
		channelListActivity = (ChannelListActivity) activity;

		Bundle extras = this.activity.getIntent().getExtras();
		if (extras != null)
		{
			if (extras.containsKey("channelId"))
				channelId = extras.getString("channelId");
		}
		else
		{
			channelId = "";
		}

		int layoutId = ChatServiceController.isNewMailUIEnable ? R.layout.cs__channel_list_new : R.layout.cs__channel_list;
		if (ConfigManager.getInstance().needRTL())
			layoutId = R.layout.cs__channel_list_new_ar;
		return inflater.inflate(layoutId, container, false);
	}

	protected static boolean dataChanged = false;

	public static void onChannelAdd()
	{
		dataChanged = true;
		if (ChatServiceController.getChannelListFragment() != null)
		{
			try
			{
				ChatServiceController.getChannelListFragment().reload();
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
		// TODO 判断是否已经在一级列表中
		onChannelAdd();
	}

	public static void onMsgAdded(ChatChannel channel)
	{
		channel.refreshRenderData();
		ChannelManager.getInstance().addToLoadedChannel(channel);

		ChannelListFragment fragment = ChatServiceController.isNewMailUIEnable ? ChatServiceController.getMsgListFragment() : ChatServiceController.getMainListFragment();
		if (fragment != null && fragment.adapter != null && fragment.adapter.list != null)
		{
			for (int i = 0; i < fragment.adapter.list.size(); i++)
			{
				ChannelListItem item = fragment.adapter.list.get(i);
				if (item instanceof ChatChannel && channel.channelID.equals(((ChatChannel) item).channelID))
				{
					dataChanged = false;
					fragment.adapter.refreshOrder();
					return;
				}
			}

			// 重新加载
			onChannelAdd();
		}

		// 如果处于主界面，未读数变了，需要刷新界面
		if (ChatServiceController.getMainListFragment() != null)
		{
			ChatServiceController.getMainListFragment().notifyDataSetChanged();
		}
	}

	private static void refreshModChannel(ChatChannel chatChannel)
	{
		if (chatChannel != null && chatChannel.isModChannel())
			dataChanged = true;
	}

	public static void onChannelRefresh()
	{
		dataChanged = true;
		if (ChatServiceController.getMainListFragment() != null && ChatServiceController.getMainListFragment().adapter != null)
		{
			ChatServiceController.getMainListFragment().refreshChannel();
		}
	}

	public static void onMailDataAdded(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getSysMailListFragment() != null)
		{
			ChatServiceController.getSysMailListFragment().refreshMailDataList(mailData);
		}
	}

	public static void onMailDataRefresh(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getSysMailListFragment() != null)
		{
			ChatServiceController.getSysMailListFragment().updateMailDataList(mailData);
		}
	}

	public void reload()
	{
		if (adapter != null && activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						adapter.reloadData();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public void onResume()
	{
		super.onResume();

		LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "onResume+ChannelListFragment");
		if (channelId.equals(MailManager.CHANNELID_MOD))
		{
			ChatServiceController.contactMode = 1;
		}
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
		{
			ChatServiceController.contactMode = 2;
		}
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
		{
			ChatServiceController.contactMode = 3;
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "onResume+ChannelListFragment+赋值=0");
			ChatServiceController.contactMode = 0;
		}

		if (introducer_hand != null && introducer_hand.getVisibility() != View.GONE)
			introducer_hand.setVisibility(View.GONE);
		refreshTitleLabel();

		if (!this.inited)
		{
			activity.showProgressBar();
			onBecomeVisible();
			return;
		}

		if (dataChanged || ChannelManager.getInstance().isInRootChannelList)
		{
			reload();
			dataChanged = false;
		}
		else
		{
			if (adapter != null)
				adapter.refreshOrder();
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
		if (tip_no_mail_textView != null && ChatServiceController.isNewMailUIEnable)
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

	private boolean isInEditMode = false;

	public void onViewCreated(final View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		channelListFragmentLayout = (RelativeLayout) view.findViewById(R.id.channelListFragmentLayout);

		channelListPullView = (PullToRefreshSwipeListView) view.findViewById(R.id.channelListPullView);
		channelListPullView.setLanguage(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		// 极少量RuntimeException com.baoyz.swipemenulistview.SwipeMenuListView
		// cannot be cast to com.baoyz.swipemenulistview.SwipeMenuListView
		mListView = channelListPullView.getRefreshableView();
		mListView.setCacheColorHint(0x00000000);
		if (ChatServiceController.isNewMailUIEnable)
		{
			mListView.setDivider(null);

			ImageUtil.setYRepeatingBG(activity, channelListPullView, R.drawable.mail_list_bg);
		}
		else
		{
			mListView.setDivider(activity.getResources().getDrawable(R.drawable.mail_separate3));
		}

		channelListPullView.setOnRefreshListener(new OnRefreshListener<SwipeMenuListView>()
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
		});

		channel_list_layout = (FrameLayout) view.findViewById(R.id.channel_list_layout);
		introducer_hand = (ImageView) view.findViewById(R.id.introducer_hand);
		introducer_hand.setVisibility(View.GONE);
		tip_no_mail_textView = (TextView) view.findViewById(R.id.tip_no_mail);
		if (ChatServiceController.isNewMailUIEnable && tip_no_mail_textView != null)
		{
			tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_MAIL));
			tip_no_mail_textView.setVisibility(View.GONE);
		}

		mailButtonBarLayout = (LinearLayout) view.findViewById(R.id.mailButtonBarLayout);
		mailButtonBarWrite = (ImageView) view.findViewById(R.id.mailButtonBarWrite);
		mailButtonBarReward = (ImageView) view.findViewById(R.id.mailButtonBarReward);
		mail_reward_layout = (RelativeLayout) view.findViewById(R.id.mail_reward_layout);
		if (StringUtils.isNotEmpty(channelId) && (channelId.equals(MailManager.CHANNELID_RECYCLE_BIN) || ChannelManager.isMainMsgChannel(channelId)))
			mail_reward_layout.setVisibility(View.GONE);
		else
			mail_reward_layout.setVisibility(View.VISIBLE);
		mailButtonBarAll = view.findViewById(R.id.mailButtonBarAll);
		checkboxLabel = (TextView) view.findViewById(R.id.checkboxLabel);
		mailButtonBarDelete = (ImageView) view.findViewById(R.id.mailButtonBarDelete);
		mailButtonBarUnread = (CheckBox) view.findViewById(R.id.mailButtonBarUnread);
		mailButtonBarUnread.setChecked(false);
		mailReadStateChecked = false;
		if (ChatServiceController.isNewMailUIEnable)
			showBottomBar(false);
		refreshTitleLabel();

		getTitleLabel().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				// ChatServiceController.getInstance().host.testMailCommand();
				// String test =
				// "{\"flyToolReward\":[{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item4002.png\",\"itemNum\":2},"
				// + "{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item400.png\",\"itemNum\":2},"
				// + "{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item408.png\",\"itemNum\":8},"
				// + "{\"itemPic\":\"item011.png\",\"itemNum\":9},"
				// + "{\"itemPic\":\"item201.png\",\"itemNum\":10},"
				// + "{\"itemPic\":\"item403.png\",\"itemNum\":11},"
				// + "{\"itemPic\":\"item403.png\",\"itemNum\":11},"
				// +
				// "{\"itemPic\":\"item402.png\",\"itemNum\":15}],\"flyReward\":[{\"itemPic\":\"ui_gold.png\",\"itemNum\":1},{\"itemPic\":\"item402.png\",\"itemNum\":2},{\"itemPic\":\"item400.png\",\"itemNum\":3}]}";
				// ServiceInterface.postMutiRewardItem(test);
				// ChatServiceController.getInstance().host.changeMailListSwitch(!ChatServiceController.isNewMailListEnable);
				// ChatServiceController.isNewMailListEnable =
				// !ChatServiceController.isNewMailListEnable;
			}
		});

		showEditButton(true);
		getEditButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				enterEditMode();
			}
		});
		getReturnButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				exitEditMode();
			}
		});

		mailButtonBarAll.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				selectAll();
			}
		});
		if (checkboxLabel != null)
		{
			checkboxLabel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					((CheckBox) mailButtonBarAll).setChecked(!((CheckBox) mailButtonBarAll).isChecked());
					selectAll();
				}
			});
		}

		mailButtonBarDelete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				operateMultiple(ChannelManager.OPERATION_DELETE_MUTI);
				LogUtil.trackAction("click_" + channelId + "_deletebar");
			}
		});

		mailButtonBarWrite.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				exitEditMode();
				writeNewMail();
				LogUtil.trackAction("click_" + channelId + "_writebar");
			}
		});

		mailButtonBarReward.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				operateMultiple(ChannelManager.OPERATION_REWARD_MUTI);
				LogUtil.trackAction("click_" + channelId + "_rewardbar");
			}
		});

		mailButtonBarUnread.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
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
		});

		SwipeMenuCreator creator = new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				switch (menu.getViewType())
				{
					case AbstractMailListAdapter.VIEW_TYPE_DELETE:
						SwipeMenuItem deleteItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						deleteItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
						deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
						deleteItem.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						deleteItem.setTitleColor(Color.WHITE);
						deleteItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));
						menu.addMenuItem(deleteItem);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_READ_AND_DELETE:
						SwipeMenuItem readItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						readItem.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
						readItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
						readItem.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						readItem.setTitleColor(Color.WHITE);
						readItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));

						SwipeMenuItem deleteItem2 = new SwipeMenuItem(channelListActivity.getApplicationContext());
						deleteItem2.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
						deleteItem2.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
						deleteItem2.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						deleteItem2.setTitleColor(Color.WHITE);
						deleteItem2.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));
						if (ConfigManager.getInstance().needRTL())
						{
							menu.addMenuItem(deleteItem2);
							menu.addMenuItem(readItem);
						}
						else
						{
							menu.addMenuItem(readItem);
							menu.addMenuItem(deleteItem2);
						}

						break;
					case AbstractMailListAdapter.VIEW_TYPE_READ_AND_REWARD:
						SwipeMenuItem readItem3 = new SwipeMenuItem(channelListActivity.getApplicationContext());
						readItem3.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
						readItem3.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
						readItem3.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						readItem3.setTitleColor(Color.WHITE);
						readItem3.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));

						SwipeMenuItem rewardItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						rewardItem.setBackground(new ColorDrawable(Color.rgb(0x84, 0x62, 0x2c)));
						rewardItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_REWARD_ALL));
						rewardItem.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						rewardItem.setTitleColor(Color.WHITE);
						rewardItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));

						if (ConfigManager.getInstance().needRTL())
						{
							menu.addMenuItem(rewardItem);
							menu.addMenuItem(readItem3);
						}
						else
						{
							menu.addMenuItem(readItem3);
							menu.addMenuItem(rewardItem);
						}
						break;
					case AbstractMailListAdapter.VIEW_TYPE_READ:
						SwipeMenuItem readItem2 = new SwipeMenuItem(channelListActivity.getApplicationContext());
						readItem2.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
						readItem2.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
						readItem2.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						readItem2.setTitleColor(Color.WHITE);
						readItem2.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));
						menu.addMenuItem(readItem2);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_CLEAR:
						SwipeMenuItem clearItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						clearItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
						clearItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_CLEAR));
						clearItem.setTitleSize(ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						clearItem.setTitleColor(Color.WHITE);
						clearItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor())));
						menu.addMenuItem(clearItem);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_NONE:
						break;
				}
			}
		};
		mListView.setMenuCreator(creator);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			// 在本例中 arg2==arg3
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3)
			{
				onListItemClick(adapterView, view, arg2);
			}
		});

		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				int menuType = menu.getViewType();
				if (adapter instanceof MainChannelAdapter)
				{
					if (index == 0)
					{
						if (menuType == AbstractMailListAdapter.VIEW_TYPE_READ_AND_REWARD)
						{
							if (ConfigManager.getInstance().needRTL())
								onRewardAllMenuClick(position);
							else
								onReadMenuClick(position);
						}
						else if (menuType == AbstractMailListAdapter.VIEW_TYPE_CLEAR)
							onClearMenuClick(position);
						else
							onReadMenuClick(position);
					}
					else if (index == 1)
					{
						if (menuType == AbstractMailListAdapter.VIEW_TYPE_READ_AND_REWARD)
						{
							if (ConfigManager.getInstance().needRTL())
								onReadMenuClick(position);
							else
								onRewardAllMenuClick(position);
						}
					}
					return true;
				}
				else
				{
					ChannelListItem item = adapter.getItem(position);
					if (item != null)
					{
						if (item.isUnread())
						{
							switch (index)
							{
								case 0:
									if (menuType == AbstractMailListAdapter.VIEW_TYPE_READ_AND_DELETE)
									{
										if (ConfigManager.getInstance().needRTL())
											onDeleteMenuClick(position);
										else
											onReadMenuClick(position);
									}
									else
									{
										onDeleteMenuClick(position);
									}
									break;
								case 1:
									if (menuType == AbstractMailListAdapter.VIEW_TYPE_READ_AND_DELETE)
									{
										if (ConfigManager.getInstance().needRTL())
											onReadMenuClick(position);
										else
											onDeleteMenuClick(position);
									}
									break;
							}
							return true;
						}
						else
						{
							if (index == 0)
							{
								onDeleteMenuClick(position);
								return true;
							}
							else
							{
								return false;
							}
						}
					}
					else
					{
						return false;
					}
				}

			}
		});

		mViewFlow = (ViewFlow) view.findViewById(R.id.viewflow);
		if (mViewFlow != null)
			mViewFlow.setVisibility(View.GONE);

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		channelListFragmentLayout.getChildAt(0).getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		((ChannelListActivity) getActivity()).fragment = this;
	}

	private void selectAll()
	{
		if (adapter != null && adapter.list != null)
		{
			allSelectedValue = !allSelectedValue;
			for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
			{
				ChannelListItem item = (ChannelListItem) iterator.next();
				if (item != null)
					item.checked = allSelectedValue;
			}
			notifyDataSetChanged();
		}
	}

	public void refreshTitle()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
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
			getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		}
		else
		{
			setTitleLabel();
		}
	}

	protected void setTitleLabel()
	{
		getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MAIL));
	}

	protected void writeNewMail()
	{
		ServiceInterface.showWriteMailActivity(channelListActivity, false, null, null, null);
	}

	public void onLoadMoreComplete()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					try
					{
						notifyDataSetChanged();
						channelListPullView.onPullDownRefreshComplete();
						channelListPullView.onPullUpRefreshComplete();
						refreshScrollLoadEnabled();

						if (adapter != null)
						{
							adapter.isLoadingMore = false;
							adapter.refreshAdapterList();
						}
						activity.hideProgressBar();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	protected void onBecomeVisible()
	{
		if (inited)
		{
			activity.hideProgressBar();
			return;
		}

		jumpToSecondaryList();

		timerDelay = 0;
		startTimer();
	}

	protected void jumpToSecondaryList()
	{

	}

	protected void createList()
	{
		adapter.fragment = this;
		refreshScrollLoadEnabled();
	}

	protected void renderList()
	{
		if (adapter != null)
			mListView.setAdapter(adapter);
		restorePosition();
		activity.hideProgressBar();
		initBanner();
	}

	protected void restorePosition()
	{
	}

	protected void enterEditMode()
	{
		isInEditMode = true;
		showEditButton(false);
		showBottomBar(true);
	}

	protected void exitEditMode()
	{
		isInEditMode = false;
		showEditButton(true);
		showBottomBar(false);
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

	public void showMutiRewardFlyAnimation(FlyMutiRewardInfo rewardInfo)
	{
		if (activity != null)
			activity.showFlyMutiReward(rewardInfo);
	}

	private void openDummyChannel(ChannelListItem item, int index)
	{
		if (index % 2 == 0)
		{
			ServiceInterface.showChatActivity(channelListActivity, DBDefinition.CHANNEL_TYPE_COUNTRY, false);
		}
		else
		{
			ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, "dummyList", false);
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
					ServiceInterface.showChannelListActivity(channelListActivity, false, DBDefinition.CHANNEL_TYPE_USER, channel.channelID,
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
					ServiceInterface.showChatActivity(channelListActivity, channel.channelType, false);
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
				ServiceInterface.showChatActivity(channelListActivity, channel.channelType, false);
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
				stopRewardMenuAnimationTimer();
				ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, channel.channelID,
						false);
				LogUtil.trackPageView("openChannel_" + channel.channelID);
			}

		}
	}

	public void showRewardMenuIntroduceAniamtion()
	{
		if (adapter == null || adapter.list == null || adapter.list.size() <= 0)
			return;
		System.out.println("showRewardMenuIntroduceAniamtion 1");
		List<ChannelListItem> channelList = adapter.list;
		for (int position = 0; position < channelList.size(); position++)
		{
			ChannelListItem item = channelList.get(position);
			if (item != null && item instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) item;
				if (channel != null && channel.isRewardAllChannel() && channel.hasMailDataInDBByType(DBManager.CONFIG_TYPE_REWARD)
						&& position >= mListView.getFirstVisiblePosition() && position <= mListView.getLastVisiblePosition())
				{
					System.out.println("showRewardMenuIntroduceAniamtion 2");
					int listViewLocation[] = { 0, 0 };
					channel_list_layout.getLocationInWindow(listViewLocation);
					View view = mListView.getChildAt(position - mListView.getFirstVisiblePosition());
					if (view != null && view instanceof SwipeMenuLayout)
					{
						final SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) view;
						int location[] = { 0, 0 };
						view.getLocationInWindow(location);
						int posY = location[1] - listViewLocation[1];
						int posX = -ScaleUtil.dip2px(10);
						mListView.smoothCloseMenu();
						if (!ConfigManager.getInstance().needRTL())
						{
							posX = ScaleUtil.getScreenWidth() - (int) (ScaleUtil.dip2px(30) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
						}

						introducer_hand.setVisibility(View.VISIBLE);
						FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) introducer_hand.getLayoutParams();
						layoutParams.topMargin = posY;
						layoutParams.leftMargin = posX;
						introducer_hand.setLayoutParams(layoutParams);
						ViewHelper.setTranslationX(introducer_hand, 0);

						offsetX = 0;
						toXDelta = 2 * dp2px(ScaleUtil.getAdjustTextSize(80, ConfigManager.scaleRatio * activity.getScreenCorrectionFactor()));
						count = 0;
						swipeMenuLayout.setIsOpen(true);
						timer = new Timer();
						TimerTask timerTask = new TimerTask()
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
											if (!saveLocalConfig)
											{
												LocalConfig config = ConfigManager.getInstance().getLocalConfig();
												boolean changed = false;
												if (config != null && !config.isRewardMenuAnimationShowed())
												{
													config.setRewardMenuAnimationShowed(true);
													changed = true;
												}
												else if (config == null)
												{
													config = new LocalConfig();
													config.setRewardMenuAnimationShowed(true);
													ConfigManager.getInstance().setLocalConfig(config);
													changed = true;
												}

												if (changed)
													ConfigManager.getInstance().saveLocalConfig();
												saveLocalConfig = true;
											}

											if (!ConfigManager.getInstance().needRTL())
												offsetX -= 1;
											else
												offsetX += 1;
											if (Math.abs(offsetX) <= toXDelta)
											{
												ViewHelper.setTranslationX(introducer_hand, offsetX);
												swipeMenuLayout.setIsOpen(true);
												swipeMenuLayout.swipe((int) Math.abs(offsetX));
											}
											else if (count < 300)
											{
												count++;
											}
											else if (count >= 300)
											{
												introducer_hand.setVisibility(View.GONE);
												swipeMenuLayout.smoothCloseMenu();
												stopRewardMenuAnimationTimer();
											}
										}
									});
								}
								else
								{
									stopRewardMenuAnimationTimer();
								}

							}
						};

						timer.scheduleAtFixedRate(timerTask, 0, 2);
					}
					break;
				}
			}

		}
	}

	public void stopRewardMenuAnimationTimer()
	{
		if (timer != null)
		{
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	private boolean	saveLocalConfig	= false;
	private Timer	timer;
	private float	offsetX			= 0;
	private int		count			= 0;
	private int		toXDelta		= 0;

	private void transportAndShowMailData(MailData mailData)
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
				activity.showProgressBar();
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

	protected void openMail(MailData mailData)
	{
		if (mailData != null)
		{
			LogUtil.trackPageView("ShowSysMail-" + mailData.channelId);
			transportAndShowMailData(mailData);
			readSystemMail(mailData);
		}
	}

	private void readSystemMail(MailData mailData)
	{
		if (mailData.isUnread())
		{
			// 更新mail
			mailData.setStatus(1);
			JniController.getInstance().excuteJNIVoidMethod("readMail",
					new Object[] { mailData.getUid(), Integer.valueOf(mailData.getType()) });

			// 更新channel
			ChatChannel parentChannel = ((SysMailAdapter) adapter).parentChannel;

			if (parentChannel != null && StringUtils.isNotEmpty(mailData.getRewardId()) && mailData.getRewardStatus() == 1)
				refreshRewardMailChannel(mailData, parentChannel);
			else
				DBManager.getInstance().updateMail(mailData);

			if (parentChannel != null)
			{
				if (parentChannel.unreadCount > 0)
				{
					parentChannel.unreadCount--;
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}
				parentChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
				DBManager.getInstance().updateChannel(parentChannel);
			}
		}
	}

	private void refreshRewardMailChannel(MailData deleteMail, ChatChannel channel)
	{
		if (deleteMail != null)
		{
			ChatChannel recycleBinChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
					MailManager.CHANNELID_RECYCLE_BIN);
			if (recycleBinChannel != null)
			{
				deleteMail.setRecycleTime(TimeManager.getInstance().getCurrentTimeMS());
				recycleBinChannel.addNewMailData(deleteMail);
			}
			DBManager.getInstance().updateMail(deleteMail);
			if (channel != null)
			{
				if (channel.mailDataList != null && channel.mailDataList.size() > 0)
					channel.mailDataList.remove(deleteMail);
				if (channel.mailUidList != null && channel.mailUidList.size() > 0)
					channel.mailUidList.remove(deleteMail.getUid());
			}
			onMailAdded();
		}
	}

	private void open(ApplicationInfo item)
	{
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(item.packageName);
		List<ResolveInfo> resolveInfoList = channelListActivity.getPackageManager().queryIntentActivities(resolveIntent, 0);
		if (resolveInfoList != null && resolveInfoList.size() > 0)
		{
			ResolveInfo resolveInfo = resolveInfoList.get(0);
			String activityPackageName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName componentName = new ComponentName(activityPackageName, className);

			intent.setComponent(componentName);
			startActivity(intent);
			channelListActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
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
		getEditButton().setVisibility(show ? View.VISIBLE : View.GONE);
		getReturnButton().setVisibility(!show ? View.VISIBLE : View.GONE);
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
			if (ChatServiceController.isNewMailUIEnable && mailButtonBarLayout.getWidth() != 0)
			{
				RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mailButtonBarLayout.getLayoutParams();
				param.height = (int) (70 * ConfigManager.scaleRatioButton);
				mailButtonBarLayout.setLayoutParams(param);
			}

			int height = (int) (ScaleUtil.dip2px(65) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (mViewFlow != null)
			{
				RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mViewFlow.getLayoutParams();
				param.height = height;
				mViewFlow.setLayoutParams(param);
			}

			if (introducer_hand != null)
			{
				System.out.println("introducer_hand!=null");
				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) introducer_hand.getLayoutParams();
				layoutParams.width = (int) (ScaleUtil.dip2px(40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				layoutParams.height = (int) (ScaleUtil.dip2px(36) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				introducer_hand.setLayoutParams(layoutParams);
			}

			adjustSizeCompleted = true;
			showBottomBar(false);
		}
	}

	private int dp2px(int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	protected static int	lastScrollX				= -1;
	protected static int	lastScrollY				= -1;
	protected static int	secondLastScrollX		= -1;
	protected static int	secondLastScrollY		= -1;
	public static boolean	rememberSecondChannelId;
	public static boolean	preventSecondChannelId	= false;
	protected static String	lastSecondChannelId		= "";

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
		adapter.list.remove(position);

		notifyDataSetChanged();
	}

	protected void readDummyItem(int position)
	{
	}

	protected void readChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			return;
		}

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleChannel(channel);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleChannel(channel);
		}
	}

	protected void deleteChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (ChatServiceController.getChannelListFragment() == null || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
		{
			return;
		}

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			boolean hasCannotDeleteMail = channel.cannotOperatedForMuti(ChannelManager.OPERATION_DELETE_MUTI);
			String content = "";
			if (hasCannotDeleteMail)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK);
			}
			else
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
			MenuController.showDeleteChannelConfirm(content, channel);
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
			if (ChatServiceController.getChannelListFragment() != null)
				ChatServiceController.getChannelListFragment().actualDeleteSingleSysMail(mailData);
		}
	}

	protected void readSysMail(int position)
	{
		if (adapter.getCount() <= 0)
			return;

		MailData mailData = (MailData) adapter.getItem(position);
		if (mailData != null)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleSysMail(mailData);
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
			{
				content = LanguageManager.getLangByKey(LanguageKeys.TIP_MAIL_NOREWARD);
			}
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK) + "\n"
						+ LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
		}
		else
		{
			if (type == ChannelManager.OPERATION_REWARD_MUTI)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.TIP_REWARD_THESE_MAIL);
			}
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
		}

		MenuController.showOperateMutiMail(content, checkedItems, type);
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

	public void actualReadSingleSysMail(MailData mailData)
	{
		readSystemMail(mailData);
		if (adapter != null)
			adapter.notifyDataSetChangedOnUI();
	}

	public void actualDeleteSingleChannel(ChatChannel channel)
	{
		List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
		channels.add(channel);
		actualDeleteChannels(channels);
	}

	public void actualReadSingleChannel(ChatChannel channel)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
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
			channel.markAsRead();
			if (ChatServiceController.getChannelListFragment() != null)
				ChatServiceController.getChannelListFragment().notifyDataSetChanged();
		}
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

		// afterDeleteMsgChannel();

		adapter.notifyDataSetChangedOnUI();
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
		boolean hasMoreData = false;
		System.out.println("afterDeleteMsgChannel contactMode：" + ChatServiceController.contactMode);
		if (ChatServiceController.isModContactMode())
		{
			ChatChannel modChannel = ChannelManager.getInstance().getModChannel();
			List<ChatChannel> modChannelList = ChannelManager.getInstance().getLoadedModChannel();
			if (modChannel != null && modChannelList != null && modChannelList.size() == 0 && DBManager.getInstance().isModChannelExist())
			{
				hasMoreData = true;
			}
		}
		else if (ChatServiceController.isDriftingBottleContactMode())
		{
			ChatChannel channel = ChannelManager.getInstance().getDriftingBottleChannel();
			List<ChatChannel> channelList = ChannelManager.getInstance().getLoadedDriftringBottleChannel();
			if (channel != null && channelList != null && channelList.size() == 0 && DBManager.getInstance().isDriftingBottleChannelExist())
			{
				hasMoreData = true;
			}
		}
		else if (ChatServiceController.isNearbyContactMode())
		{
			ChatChannel channel = ChannelManager.getInstance().getNearbyChannel();
			List<ChatChannel> channelList = ChannelManager.getInstance().getLoadedNearbyChannel();
			if (channel != null && channelList != null && channelList.size() == 0 && DBManager.getInstance().isNearbyChannelExist())
			{
				hasMoreData = true;
			}
		}
		else
		{
			ChatChannel messageChannel = ChannelManager.getInstance().getMessageChannel();
			List<ChatChannel> messageChannelList = ChannelManager.getInstance().getLoadedMessageChannel();
			if (messageChannel != null && messageChannelList != null && messageChannelList.size() == 0
					&& DBManager.getInstance().isMessageChannelExist())
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
					String type = channel.getChannelRewardTypes();

					uids = ChannelManager.appendStr(uids, mailUids);
				}
			}
		}

		if (!(uids.equals("")))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" });
			activity.showRewardLoadingPopup();
		}
	}

	protected void actualDeleteSysMails(List<ChannelListItem> sysMails)
	{
		String uids = "";

		ChatChannel channel = null;
		int canDeleteStatus = 0;
		boolean hasDetectMail = false;
		for (int i = 0; i < sysMails.size(); i++)
		{
			MailData mailData = (MailData) sysMails.get(i);
			if (mailData != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				if (mailData.canDelete())
				{
					channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
					uids = ChannelManager.appendStr(uids, mailData.getUid());
					if (channel != null && StringUtils.isNotEmpty(mailData.getUid()))
					{
						if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
							hasDetectMail = true;
						ChannelManager.getInstance().deleteSysMailFromChannel(channel, mailData, true);
					}
					adapter.list.remove(mailData);
					if (canDeleteStatus == 0)
						canDeleteStatus = 1;
				}
				else
				{
					if (canDeleteStatus == 1)
						canDeleteStatus = 2;
				}
			}
		}

		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();

		ChannelManager.getInstance().calulateAllChannelUnreadNum();

		if (canDeleteStatus == 1 || canDeleteStatus == 2) // 只能删一部分
		{
			adapter.notifyDataSetChangedOnUI();
		}

		if (channel != null && channel.mailDataList.size() == 0)
		{
			ChannelManager.getInstance().deleteChannel(channel);
		}

		if (StringUtils.isNotEmpty(uids))
		{
			JniController.getInstance().excuteJNIVoidMethod("deleteMutiMail", new Object[] { uids, "" });
		}
	}

	protected void actualRewardSysMails(List<ChannelListItem> sysMails)
	{
		String uids = "";

		for (int i = 0; i < sysMails.size(); i++)
		{
			MailData mailData = (MailData) sysMails.get(i);
			if (mailData != null && mailData.channel != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				if (mailData.hasReward())
				{
					uids = ChannelManager.appendStr(uids, mailData.getUid());
				}
			}
		}

		if (!(uids.equals("")))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" });
		}
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

	protected void onListItemClick(AdapterView<?> adapterView, View view, int position)
	{
		ChannelListItem item = (ChannelListItem) adapterView.getItemAtPosition(position);
		if (item == null)
		{
			return;
		}
		if (isInEditMode)
		{
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.channel_checkBox);
			item.checked = !item.checked;
			checkbox.setChecked(item.checked);
			return;
		}

		if (ChatServiceController.getInstance().isInDummyHost())
		{
			if (ChatServiceController.isNewMailUIEnable)
			{
				openItem((ChannelListItem) adapterView.getItemAtPosition(position));
			}
			else
			{
				ApplicationItem item2 = (ApplicationItem) adapterView.getItemAtPosition(position);
				openDummyChannel(item2, position);
			}
		}
		else
		{
			openItem(item);
		}
	}

	protected void openItem(ChannelListItem item)
	{
	}

	private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		System.out.println("channelListfragment onDestory");

		stopRewardMenuAnimationTimer();

		if (adapter != null)
		{
			adapter.destroy();
			adapter = null;
		}

		mailReadStateChecked = false;

		if (mListView != null)
		{
			mListView.setMenuCreator(null);
			mListView.setOnItemClickListener(null);
			mListView.setOnMenuItemClickListener(null);
			mListView = null;
		}

		if (channelListPullView != null)
		{
			channelListPullView.setOnRefreshListener(null);
			channelListPullView = null;
		}

		if (mailButtonBarAll != null)
		{
			mailButtonBarAll.setOnClickListener(null);
			mailButtonBarAll = null;
		}
		if (mailButtonBarDelete != null)
		{
			mailButtonBarDelete.setOnClickListener(null);
			mailButtonBarDelete = null;
		}
		if (mailButtonBarWrite != null)
		{
			mailButtonBarWrite.setOnClickListener(null);
			mailButtonBarWrite = null;
		}
		if (checkboxLabel != null)
		{
			checkboxLabel.setOnClickListener(null);
			checkboxLabel = null;
		}

		mailButtonBarLayout = null;

		if (getTitleLabel() != null)
		{
			getTitleLabel().setOnClickListener(null);
		}
		if (getEditButton() != null)
		{
			getEditButton().setOnClickListener(null);
		}
		if (getReturnButton() != null)
		{
			getReturnButton().setOnClickListener(null);
		}

		if (activity != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
			if (content != null && content.getChildAt(0) != null && content.getChildAt(0).getViewTreeObserver() != null)
			{
				content.getChildAt(0).getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
			}
		}
		onGlobalLayoutListener = null;

		if (getActivity() != null)
		{
			((ChannelListActivity) getActivity()).fragment = null;
		}

		super.onDestroy();
	}
}
