package com.elex.chatservice.view;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.MailManager;

public class MsgMailListFragment extends MainListFragment
{
	protected void setTitleLabel()
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			ChatChannel channel = ChannelManager.getInstance().getMainMsgChannelById(channelId);
			if (channel != null)
				this.getTitleLabel().setText(channel.nameText);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		dataChanged = true;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		ChannelManager.getInstance().isInRootChannelList = true;
	}

	@Override
	protected void showWriteButton()
	{
	}

	public void refreshScrollLoadEnabled()
	{
		if(channelListPullView.isPullLoadEnabled())
			channelListPullView.setPullLoadEnabled(false);
		if(channelListPullView.isPullRefreshEnabled())
			channelListPullView.setPullRefreshEnabled(false);

		if (adapter != null && adapter.hasMoreData())
		{
			if(!channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(true);
		}
		else
		{
			if(channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(false);
		}
	}
}
