package com.elex.chatservice.view.adapter;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;

public class MsgChannelAdapter extends ChannelAdapter
{
	protected String	mChannelId	= "";
	private boolean		initLoaded	= false;

	public MsgChannelAdapter(ChannelListActivity context, ChannelListFragment fragment, String channelId)
	{
		super(context, fragment);
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "channelId+c————+MsgChannelAdapter",channelId);
		mChannelId = channelId;
		if (channelId.equals(MailManager.CHANNELID_MOD))
			ChatServiceController.contactMode = 1;
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			ChatServiceController.contactMode = 2;
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			ChatServiceController.contactMode = 3;
		reloadData();
	}

	public boolean hasMoreData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			int count = getAllMsgChannels().size();

			return count > list.size();
		}
	}
	
	public boolean hasMoreUnreadData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			List<ChatChannel> msgChannel = getAllMsgChannels();
			int unreadCount = 0;
			for(ChatChannel channel : msgChannel)
			{
				if(channel!=null && channel.isUnread())
					unreadCount++;
			}
			return unreadCount > list.size();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChatChannel channel = (ChatChannel) getItem(position);
		if (channel != null)
			channel.refreshRenderData();
		return super.getView(position, convertView, parent);
	}

	public synchronized void loadMoreData()
	{
		initLoaded = true;
		List<ChatChannel> allMsgChannels = getAllMsgChannels();
		List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();

		int moreCount = loadedMsgChannels.size() + 10;
		int actualCount = allMsgChannels.size() > moreCount ? moreCount : allMsgChannels.size();

		List<ChatChannel> subMsgChannels = allMsgChannels.subList(0, actualCount);
		
		int addCnt = 0;

		List<ChatChannel> tempList = new ArrayList<ChatChannel>();
		for (int i = 0; i < subMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = subMsgChannels.get(i);
			if (chatChannel != null && !ChannelManager.isChannelInList(chatChannel, loadedMsgChannels))
			{
				addCnt++;
				tempList.add(chatChannel);
			}
		}
		
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "allMsgChannels.size()", allMsgChannels.size(), "moreCount",
				moreCount, "actualCount", actualCount, "subMsgChannels.size()", subMsgChannels.size(), "addCnt", addCnt);

		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null && !chatChannel.hasInitLoaded())
			{
				chatChannel.loadMoreMsg();
			}
			loadedMsgChannels.add(chatChannel);
		}

		tempList.clear();

		for (int i = 0; i < loadedMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = loadedMsgChannels.get(i);
			if (chatChannel != null && !ChannelManager.isChannelInList(chatChannel, list))
			{
				tempList.add(chatChannel);
			}
		}

		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null)
			{
				list.add(chatChannel);
			}
		}

		refreshOrder();

		fragment.onLoadMoreComplete();
	}
	
	public synchronized void loadMoreUnreadData()
	{
		initLoaded = true;
		List<ChatChannel> allUnreadMsgChannels = getAllUnreadMsgChannels();
		List<ChatChannel> unreadloadedMsgChannels = getLoadedUnreadMsgChannels();

		int moreCount = unreadloadedMsgChannels.size() + 10;
		int actualCount = allUnreadMsgChannels.size() > moreCount ? moreCount : allUnreadMsgChannels.size();

		List<ChatChannel> subMsgChannels = allUnreadMsgChannels.subList(0, actualCount);
		
		int addCnt = 0;

		List<ChatChannel> tempList = new ArrayList<ChatChannel>();
		for (int i = 0; i < subMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = subMsgChannels.get(i);
			if (chatChannel != null && !ChannelManager.isChannelInList(chatChannel, unreadloadedMsgChannels))
			{
				addCnt++;
				tempList.add(chatChannel);
			}
		}
		
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "allUnreadMsgChannels.size()", allUnreadMsgChannels.size(), "moreCount",
				moreCount, "actualCount", actualCount, "subMsgChannels.size()", subMsgChannels.size(), "addCnt", addCnt);

		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null && !chatChannel.hasInitLoaded())
			{
				chatChannel.loadMoreMsg();
			}
			unreadloadedMsgChannels.add(chatChannel);
		}

		tempList.clear();

		for (int i = 0; i < unreadloadedMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = unreadloadedMsgChannels.get(i);
			if (chatChannel != null && !ChannelManager.isChannelInList(chatChannel, list))
			{
				tempList.add(chatChannel);
			}
		}

		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null)
			{
				list.add(chatChannel);
			}
		}

		refreshOrder();

		fragment.onLoadMoreComplete();
	}
	

	public void reloadData()
	{
		if(!fragment.mailReadStateChecked)
		{
			List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "loadedMsgChannels.size()", loadedMsgChannels.size());

			list.clear();

			// 第一次进入列表时，可能loadedMsgChannels已经有内容（刚收到的消息），此时需要loadMoreData一次，否则会只显示刚收到的channel
			if (loadedMsgChannels.size() == 0 || !initLoaded)
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    初次加载");
				loadMoreData();
			}
			else
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    重新加载");
				list.addAll(loadedMsgChannels);
			}
		}
		else
		{
			List<ChatChannel> loadedUnreadMsgChannels = getLoadedUnreadMsgChannels();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "loadedUnreadMsgChannels.size()", loadedUnreadMsgChannels.size());

			list.clear();

			// 第一次进入列表时，可能loadedMsgChannels已经有内容（刚收到的消息），此时需要loadMoreData一次，否则会只显示刚收到的channel
			if (loadedUnreadMsgChannels.size() == 0 || !initLoaded)
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    初次加载");
				loadMoreUnreadData();
			}
			else
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    重新加载");
				list.addAll(loadedUnreadMsgChannels);
			}
		}

		refreshOrder();

		fragment.setNoMailTipVisible(list.size() <= 0);
	}
	
	public void refreshAdapterList()
	{
		list.clear();
		
		if(fragment.mailReadStateChecked)
		{
			List<ChatChannel> loadedUnreadMsgChannels = getLoadedUnreadMsgChannels();
			
			if(loadedUnreadMsgChannels != null)
			{
				list.addAll(loadedUnreadMsgChannels);
			}
		}
		else
		{
			List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();
			
			if(loadedMsgChannels != null)
			{
				list.addAll(loadedMsgChannels);
			}
		}
		
		refreshOrder();
		try
		{
			if(ChatServiceController.getCurrentActivity()!=null)
			{
				ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(fragment!=null)
						fragment.setNoMailTipVisible(list.size() <= 0);
					}
				});
			
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		
		
	}

	private List<ChatChannel> getAllMsgChannels()
	{
		return ChannelManager.getInstance().getAllMsgChannelById(mChannelId);
	}
	
	private List<ChatChannel> getAllUnreadMsgChannels()
	{
		List<ChatChannel> allUnreadMsgChannel = new ArrayList<ChatChannel>();
		for(ChatChannel channel: getAllMsgChannels())
		{
			if(channel!=null && channel.isUnread())
				allUnreadMsgChannel.add(channel);
		}
		return allUnreadMsgChannel;
	}

	private List<ChatChannel> getLoadedMsgChannels()
	{
		return ChannelManager.getInstance().getLoadedChannelListById(mChannelId);
	}
	
	private List<ChatChannel> getLoadedUnreadMsgChannels()
	{
		List<ChatChannel> loadedUnreadMsgChannel = new ArrayList<ChatChannel>();
		for(ChatChannel channel: getLoadedMsgChannels())
		{
			if(channel!=null && channel.isUnread())
				loadedUnreadMsgChannel.add(channel);
		}
		return loadedUnreadMsgChannel;
	}

	@Override
	public void destroy()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "destroy");
		super.destroy();
		ChatServiceController.contactMode = 0;
	}
}
