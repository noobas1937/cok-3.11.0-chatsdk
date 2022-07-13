package com.elex.chatservice.view.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;

public class MainChannelAdapter extends ChannelAdapter
{
	public MainChannelAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		super(context, fragment);

		reloadData();
	}

	public void reloadData()
	{
		System.out.println("MainChannelAdapter reloadData");
		list.clear();
		list.addAll(ChannelManager.getInstance().getAllMailChannel());
		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChatChannel channel = (ChatChannel) getItem(position);
		if (channel != null)
			channel.refreshRenderData();

		return super.getView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position)
	{
		if (ChatServiceController.isNewMailUIEnable)
		{
			ChannelListItem item = getItem(position);
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
					if (channel!=null && channel.isRewardAllChannel() && channel.hasMailDataInDBByType(DBManager.CONFIG_TYPE_REWARD))
						return VIEW_TYPE_READ_AND_REWARD;
					else
						return VIEW_TYPE_READ;
				}
				else if(channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN) && (channel.hasSysMailInList() || channel.hasMailDataInDB()))
					return VIEW_TYPE_CLEAR;
				else
					return VIEW_TYPE_NONE;
			}
			return VIEW_TYPE_NONE;
		}
		else
		{
			ChannelListItem item = getItem(position);
			if (item != null)
			{
				if (item.isUnread())
					return VIEW_TYPE_READ_AND_DELETE;
				else
					return VIEW_TYPE_DELETE;
			}
			return VIEW_TYPE_READ_AND_DELETE;
		}
	}

	public static final String[]	MAIN_CHANNEL_ORDERS	= {
			MailManager.CHANNELID_MESSAGE,
			MailManager.CHANNELID_DRIFTING_BOTTLE,
			MailManager.CHANNELID_NEAR_BY,
			MailManager.CHANNELID_ALLIANCE,
			MailManager.CHANNELID_FIGHT,
			MailManager.CHANNELID_EVENT,
			MailManager.CHANNELID_NOTICE,
			MailManager.CHANNELID_STUDIO,
			MailManager.CHANNELID_SYSTEM,
			MailManager.CHANNELID_DRAGON_TOWER,
			MailManager.CHANNELID_RECYCLE_BIN,
			MailManager.CHANNELID_MOD,
			MailManager.CHANNELID_RESOURCE,
			MailManager.CHANNELID_MONSTER,
			MailManager.CHANNELID_NEW_WORLD_BOSS,
			MailManager.CHANNELID_KNIGHT
							};

	public void refreshOrder()
	{
		if (ChatServiceController.isNewMailUIEnable)
		{
			for (int i = MAIN_CHANNEL_ORDERS.length - 1; i >= 0; i--)
			{
				String type = MAIN_CHANNEL_ORDERS[i];
				for (int j = 0; j < list.size(); j++)
				{
					ChatChannel channel = (ChatChannel) list.get(j);
					if (channel == null)
						continue;

					if (channel.channelID.equals(type))
					{
						moveToHead(j);
						break;
					}
				}
			}

			notifyDataSetChangedOnUI();
		}
		else
		{
			super.refreshOrder();
		}
	}

	private void moveToHead(int i)
	{
		list.add(0, list.remove(i));
	}
}
