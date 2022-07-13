package com.elex.chatservice.view;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.adapter.AppAdapter;
import com.elex.chatservice.view.adapter.MainChannelAdapter;
import com.elex.chatservice.view.adapter.MsgChannelAdapter;

public class MainListFragment extends ChannelListFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MAIL, "show MainListFragment");
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

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * 不override的话，父类的onViewCreated会调两次
	 */
	public void onViewCreated(final View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		showWriteButton();
	}

	protected void showWriteButton()
	{
		if (ChatServiceController.isNewMailUIEnable)
		{
			getEditButton().setVisibility(View.GONE);
			getReturnButton().setVisibility(View.GONE);

			getWriteButton().setVisibility(View.VISIBLE);
			getWriteButton().setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					writeNewMail();
					LogUtil.trackAction("click_write_mail");
				}
			});
		}
		else
		{
			getWriteButton().setVisibility(View.GONE);
		}
	}
	
	public static boolean canJumpToSecondaryList()
	{
		return rememberSecondChannelId && StringUtils.isNotEmpty(lastSecondChannelId);
	}

	protected void jumpToSecondaryList()
	{
		if (canJumpToSecondaryList())
		{
			ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, lastSecondChannelId,
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

	protected void createList()
	{
		if (ChannelManager.isMainMsgChannel(channelId))
		{
			adapter = new MsgChannelAdapter(channelListActivity, this, channelId);
		}
		else
		{
			if (ChatServiceController.getInstance().isInDummyHost() && !ChatServiceController.isNewMailUIEnable)
			{
				ChatChannel parentChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
				adapter = new AppAdapter(channelListActivity, this, parentChannel);
			}
			else
			{
				adapter = new MainChannelAdapter(channelListActivity, this);
			}
		}
		super.createList();
	}

	public MainChannelAdapter getMainChannelAdapter()
	{
		if (adapter != null && adapter instanceof MainChannelAdapter)
		{
			return (MainChannelAdapter) adapter;
		}
		return null;
	}

	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			mListView.setSelectionFromTop(lastX, lastY);
		}
		lastScrollX = lastScrollY = -1;
	}

	protected void onDeleteMenuClick(int position)
	{
		if (ChatServiceController.getInstance().isInDummyHost() && !ChatServiceController.isNearbyContactMode())
		{
			deleteDummyItem(position);
		}
		else
		{
			deleteChannel(position);
		}
		
	}

	protected void onReadMenuClick(int position)
	{
		System.out.println("onReadMenuClick");
		stopRewardMenuAnimationTimer();
		if (ChatServiceController.getInstance().isInDummyHost())
		{
			readDummyItem(position);
		}
		else
		{
			if (adapter instanceof MainChannelAdapter)
			{
				readMainChannel(position);
			}
			else if (adapter instanceof MsgChannelAdapter)
			{
				readChannel(position);
			}
		}
	}
	
	protected void onRewardAllMenuClick(int position)
	{
		System.out.println("onRewardAllMenuClick");
		stopRewardMenuAnimationTimer();
		if (adapter instanceof MainChannelAdapter)
		{
			rewardMainChannel(position);
		}
		
	}
	
	protected void rewardMainChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel != null && channel.isRewardAllChannel())
		{
			LogUtil.trackAction(channel.channelID+"_all_reward");
			String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_REWARD);
			if (StringUtils.isNotEmpty(uids))
			{
				System.out.println("rewardMainChannel uids:"+uids);
				JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" });
				activity.showRewardLoadingPopup();
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
			if(mailList!=null)
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

				if(!channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
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
		
		if(ConfigManager.mail_pull_state != ConfigManager.MAIL_PULLING && ChatServiceController.getChannelListFragment()!=null && !(ChatServiceController.getChannelListFragment() instanceof MsgMailListFragment))
		{
			LocalConfig config = ConfigManager.getInstance().getLocalConfig();
			if(config == null || (config!=null && !config.isRewardMenuAnimationShowed()))
			{
				Timer timer =new Timer();
				TimerTask timerTask = new TimerTask()
				{
					
					@Override
					public void run()
					{
						if(activity!=null)
						{
							activity.runOnUiThread(new Runnable()
							{
								
								@Override
								public void run()
								{
									showRewardMenuIntroduceAniamtion();
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

	protected void openItem(ChannelListItem item)
	{
		if (item != null && item instanceof ChatChannel)
		{
			openChannel((ChatChannel) item);
		}
	}

	public void saveState()
	{
		if (inited && getCurrentPos() != null)
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

	public void onDestroy()
	{
		if (getWriteButton() != null)
		{
			getWriteButton().setOnClickListener(null);
		}
		super.onDestroy();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		stopRewardMenuAnimationTimer();
	}
}
