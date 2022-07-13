package com.elex.chatservice.view.recyclerrefreshview;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenu;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuCreator;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuItem;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuHandle;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuRecyclerListView.OnMenuItemClickListener;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;

public class RecyclerSysMailActivity extends AbstractRecyclerActivity
{

	public static final int	MENU_TYPE_READ		= 0;
	public static final int	MENU_TYPE_DELETE	= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		showEditButton(true);
	}

	public void openItem(ChannelListItem item)
	{
		if (item instanceof MailData)
		{
			MailManager.getInstance().openMail((MailData) item);
		}
	}

	@Override
	protected void adjustSizeExtend()
	{
		if (actionbar_editButton != null)
		{
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) actionbar_editButton.getLayoutParams();
			param.width = (int) (124 * ConfigManager.scaleRatioButton);
			param.height = (int) (48 * ConfigManager.scaleRatioButton);
			actionbar_editButton.setLayoutParams(param);
		}
	}

	@Override
	public void createAdapter()
	{
		adapter = new RecyclerSysMailAdapter(this, channelType, channelId);
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
				readItem.setMenuType(MENU_TYPE_READ);
				menu.addMenuItem(readItem);

				RecyclerSwipeMenuItem deleteItem = new RecyclerSwipeMenuItem(getBaseContext());
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
				deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
				deleteItem.setTitleSize(menuTextSize);
				deleteItem.setTitleColor(Color.WHITE);
				deleteItem.setWidth(menuWidth);
				deleteItem.setMenuType(MENU_TYPE_DELETE);
				menu.addMenuItem(deleteItem);
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
					if (menuType == MENU_TYPE_READ)
					{
						onReadMenuClick(position);
					}
					else if (menuType == MENU_TYPE_DELETE)
					{
						onDeleteMenuClick(position);
					}
				}
				return true;
			}
		};
	}

	@Override
	protected void onReadMenuClick(int position)
	{
		if (ChatServiceController.getInstance().isInDummyHost())
		{
			readDummyItem(position);
		}
		else
		{
			readSysMail(position);
		}
	}

	private void readSysMail(int position)
	{
		if (adapter.getItemCount() <= 0)
			return;

		MailData mailData = (MailData) adapter.getItem(position);
		if (mailData != null)
			actualReadSingleSysMail(mailData);
	}

	public void actualReadSingleSysMail(MailData mailData)
	{
		readSystemMail(mailData);
		if (adapter != null)
			adapter.notifyDataSetChangedOnUI();
	}

	public void setTitleLabel()
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
			if (channel != null)
				titleLabel.setText(channel.nameText);
		}
	}

	public void updateMailDataList(MailData mailData)
	{
		if (mailData != null && adapter != null && StringUtils.isNotEmpty(channelId) && mailData.getChannelId().equals(channelId))
		{
			MailData mail = null;
			boolean isInMailList = false;
			for (int i = 0; i < adapter.list.size(); i++)
			{
				mail = (MailData) adapter.list.get(i);
				if (mail != null && mail.getUid().equals(mailData.getUid()))
				{
					isInMailList = true;
					break;
				}
			}
			if (mail != null && isInMailList)
			{
				adapter.list.remove(mail);
				adapter.list.add(mailData);
				adapter.refreshOrder();
			}
		}
	}

	public void refreshMailDataList(MailData mailData)
	{
		if (adapter != null && StringUtils.isNotEmpty(channelId) && mailData != null && StringUtils.isNotEmpty(mailData.getChannelId())
				&& mailData.getChannelId().equals(channelId))
		{
			adapter.list.add(mailData);
			adapter.refreshOrder();
		}
	}

	public void refreshScrollLoadEnabled()
	{
		if (channelListPullView.isPullLoadEnabled())
			channelListPullView.setPullLoadEnabled(false);
		if (channelListPullView.isPullRefreshEnabled())
			channelListPullView.setPullRefreshEnabled(false);

		if (adapter != null && ((mailReadStateChecked && adapter.hasMoreUnreadData()) || (!mailReadStateChecked && adapter.hasMoreData())))
		{
			if (!channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(true);
		}
		else
		{
			if (channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(false);
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

			ChatChannel parentChannel = ChannelManager.getInstance().getChannel(channelType, channelId);

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

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
		if (type == ChannelManager.OPERATION_DELETE_MUTI)
			actualDeleteSysMails(checkedItems);
		else if (type == ChannelManager.OPERATION_REWARD_MUTI)
			actualRewardSysMails(checkedItems);
	}

	private void actualDeleteSysMails(List<ChannelListItem> sysMails)
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

	private void actualRewardSysMails(List<ChannelListItem> sysMails)
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

	public static void onMailDataAdded(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getRecyclerSysMailActivity() != null)
		{
			ChatServiceController.getRecyclerSysMailActivity().refreshMailDataList(mailData);
		}
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
						if (item != null)
						{
							if (item.isLock() || item.hasReward())
							{
								if (item.isUnread())
									return new int[] { MENU_TYPE_READ };
								else
									return new int[] {};
							}
							else
							{
								if (item.isUnread())
									return new int[] { MENU_TYPE_READ, MENU_TYPE_DELETE };
								else
									return new int[] { MENU_TYPE_DELETE };
							}
						}
					}
					return null;
				}
			};
		}
		return null;
	}

}
