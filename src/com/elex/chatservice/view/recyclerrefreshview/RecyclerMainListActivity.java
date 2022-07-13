package com.elex.chatservice.view.recyclerrefreshview;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenu;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuCreator;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuItem;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuHandle;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuRecyclerListView.OnMenuItemClickListener;

public class RecyclerMainListActivity extends AbstractRecyclerActivity
{

	public static final int	MENU_TYPE_READ_ALL		= 0;
	public static final int	MENU_TYPE_REWARD_ALL	= 1;
	public static final int	MENU_TYPE_CLEAR_ALL		= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MAIL, "show RecyclerMainListActivity");
		actionbar_writeButton = (Button) findViewById(R.id.actionbar_writeButton);
		actionbar_writeButton.setVisibility(View.VISIBLE);
		actionbar_writeButton.setOnClickListener(this);
	}

	@Override
	protected void adjustSizeExtend()
	{
		if (actionbar_writeButton != null)
		{
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) actionbar_writeButton.getLayoutParams();
			param.width = (int) (124 * ConfigManager.scaleRatioButton);
			param.height = (int) (48 * ConfigManager.scaleRatioButton);
			actionbar_writeButton.setLayoutParams(param);
		}
	}

	@Override
	public RecyclerSwipeMenuCreator initSwipeMenuCreator()
	{
		return new RecyclerSwipeMenuCreator()
		{
			@Override
			public void create(RecyclerSwipeMenu menu)
			{
				int menuWidth = (int) (ScaleUtil.dip2px(80) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int menuTextSize = ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * getScreenCorrectionFactor());

				RecyclerSwipeMenuItem readItem = new RecyclerSwipeMenuItem(getBaseContext());
				readItem.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
				readItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
				readItem.setTitleSize(menuTextSize);
				readItem.setTitleColor(Color.WHITE);
				readItem.setWidth(menuWidth);
				readItem.setMenuType(MENU_TYPE_READ_ALL);
				menu.addMenuItem(readItem);

				RecyclerSwipeMenuItem rewardItem = new RecyclerSwipeMenuItem(getBaseContext());
				rewardItem.setBackground(new ColorDrawable(Color.rgb(0x84, 0x62, 0x2c)));
				rewardItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_REWARD_ALL));
				rewardItem.setTitleSize(menuTextSize);
				rewardItem.setTitleColor(Color.WHITE);
				rewardItem.setWidth(menuWidth);
				rewardItem.setMenuType(MENU_TYPE_REWARD_ALL);
				menu.addMenuItem(rewardItem);

				RecyclerSwipeMenuItem clearItem = new RecyclerSwipeMenuItem(getBaseContext());
				clearItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
				clearItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_CLEAR));
				clearItem.setTitleSize(menuTextSize);
				clearItem.setTitleColor(Color.WHITE);
				clearItem.setWidth(menuWidth);
				clearItem.setMenuType(MENU_TYPE_CLEAR_ALL);
				menu.addMenuItem(clearItem);
			}
		};
	}

	@Override
	public OnMenuItemClickListener initSwipeMenuItemClickListener()
	{
		return new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(int position, RecyclerSwipeMenu menu, int index)
			{
				RecyclerSwipeMenuItem item = menu.getMenuItem(index);
				if (item != null)
				{
					int menuType = item.getMenuType();
					if (menuType == MENU_TYPE_READ_ALL)
					{
						onReadMenuClick(position);
					}
					else if (menuType == MENU_TYPE_REWARD_ALL)
					{
						onRewardAllMenuClick(position);
					}
					else if (menuType == MENU_TYPE_CLEAR_ALL)
					{
						onClearMenuClick(position);
					}
				}
				return true;
			}
		};
	}

	protected void jumpToSecondaryList()
	{
		if (canJumpToSecondaryList())
		{
			ServiceInterface.showChannelListActivity(this, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, lastSecondChannelId,
					true);
			rememberSecondChannelId = false;
			lastSecondChannelId = "";
			return;
		}
	}

	protected void refreshChannel()
	{
		notifyDataSetChanged();
		dataChanged = false;
	}

	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			mRecyclerView.setSelectionFromTop(lastX, lastY);
		}
		lastScrollX = lastScrollY = -1;
	}

	protected void onDeleteMenuClick(int position)
	{
		if (ChatServiceController.getInstance().isInDummyHost() && !ChatServiceController.isNearbyContactMode())
			deleteDummyItem(position);
		else
			deleteChannel(position);
	}

	protected void onReadMenuClick(int position)
	{
		System.out.println("onReadMenuClick");
		if (ChatServiceController.getInstance().isInDummyHost())
			readDummyItem(position);
		else
			readMainChannel(position);
	}

	protected void onRewardAllMenuClick(int position)
	{
		System.out.println("onRewardAllMenuClick");
		rewardMainChannel(position);

	}

	protected void rewardMainChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel != null && channel.isRewardAllChannel())
		{
			LogUtil.trackAction(channel.channelID + "_all_reward");
			String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_REWARD);
			if (StringUtils.isNotEmpty(uids))
			{
				System.out.println("rewardMainChannel uids:" + uids);
				JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" });
				showRewardLoadingPopup();
			}
			else
			{
				MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_MAIL_NOREWARD));
			}
		}
	}

	@Override
	protected void onClearMenuClick(int position)
	{
		System.out.println("onClearMenuClick");
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			List<MailData> mailList = DBManager.getInstance().getSysMailFromDB(channel.channelID, -1);
			if (mailList != null)
			{
				String uids = "";

				int canDeleteStatus = 0;
				boolean hasDetectMail = false;
				for (int i = 0; i < mailList.size(); i++)
				{
					MailData mailData = (MailData) mailList.get(i);
					if (mailData != null)
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

				if (channel != null && !channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
				{
					if (hasDetectMail)
						DBManager.getInstance().getDetectMailInfo();

					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}

				if (canDeleteStatus == 1 || canDeleteStatus == 2) // 只能删一部分
				{
					adapter.notifyDataSetChangedOnUI();
				}

				if (channel != null && channel.mailDataList.size() == 0 && !channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
				{
					ChannelManager.getInstance().deleteChannel(channel);
				}

				if (StringUtils.isNotEmpty(uids))
				{
					JniController.getInstance().excuteJNIVoidMethod("deleteMutiMail", new Object[] { uids, "" });
				}
			}
		}

	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (ConfigManager.mail_pull_state != ConfigManager.MAIL_PULLING && RecyclerMainListActivity.this != null)
		{
			LocalConfig config = ConfigManager.getInstance().getLocalConfig();
			if (config == null || (config != null && !config.isRewardMenuAnimationShowed()))
			{
				Timer timer = new Timer();
				TimerTask timerTask = new TimerTask()
				{

					@Override
					public void run()
					{
						if (RecyclerMainListActivity.this != null)
						{
							RecyclerMainListActivity.this.runOnUiThread(new Runnable()
							{

								@Override
								public void run()
								{
								}
							});
						}

					}
				};

				timer.schedule(timerTask, 2000);
			}
		}
	}

	protected void readMainChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		ChannelManager.getInstance().readAllMainMsgChannel(channel);
	}

	public void openItem(ChannelListItem item)
	{
		if (item != null && item instanceof ChatChannel)
		{
			openChannel((ChatChannel) item);
		}
	}

	public void saveState()
	{
		if (getCurrentPos() != null)
		{
			lastScrollX = getCurrentPos().x;
			lastScrollY = getCurrentPos().y;
		}
	}

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
		if (type == ChannelManager.OPERATION_DELETE_MUTI)
			actualDeleteChannels(checkedItems);
		else if (type == ChannelManager.OPERATION_REWARD_MUTI)
			actualRewardChannels(checkedItems);
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void createAdapter()
	{
		adapter = new RecyclerMainChannelAdapter(this, channelType, channelId);
	}

	@Override
	public SwipeMenuHandle createSwipeMenuHandle()
	{
		if (adapter != null)
		{
			return new SwipeMenuHandle()
			{

				@Override
				public int[] getCurrentItemMenuTypeList(int position)
				{
					if (adapter != null)
					{
						ChannelListItem item = adapter.getItem(position);
						if (item != null && item instanceof ChatChannel)
						{
							ChatChannel channel = (ChatChannel) item;
							if (!channel.channelID.equals(MailManager.CHANNELID_MONSTER)
									&& !channel.channelID.equals(MailManager.CHANNELID_MOD)
									&& !channel.channelID.equals(MailManager.CHANNELID_RESOURCE)
									&& !channel.channelID.equals(MailManager.CHANNELID_KNIGHT)
									&& !channel.channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)
									&& !channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP)
									&& !channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
							{
								if (channel != null && channel.isRewardAllChannel() && channel.hasMailDataInDBByType(DBManager.CONFIG_TYPE_REWARD))
								{
									if (channel.isUnread())
										return new int[] { MENU_TYPE_READ_ALL, MENU_TYPE_REWARD_ALL };
									else
										return new int[] { MENU_TYPE_REWARD_ALL };
								}
								else
								{
									if (channel.isUnread())
										return new int[] { MENU_TYPE_READ_ALL };
									else
										return new int[] {};
								}
							}
							else if (channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN) && (channel.hasSysMailInList() || channel.hasMailDataInDB()))
								return new int[] { MENU_TYPE_CLEAR_ALL };
							else
								return new int[] {};
						}
					}
					return null;
				}
			};
		}
		return null;
	}
}
