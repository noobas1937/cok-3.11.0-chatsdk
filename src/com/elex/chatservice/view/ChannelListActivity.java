package com.elex.chatservice.view;

import android.os.Bundle;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class ChannelListActivity extends MyActionBarActivity
{
	public int	channelType;

	public ChannelListFragment getFragment()
	{
		return (ChannelListFragment) fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Bundle extras = getIntent().getExtras();

		boolean isSecondLvList = false;
		boolean isGoBack = false;
		String channelId = "";
		if (extras != null)
		{
			this.bundle = new Bundle(extras);
			if (extras.containsKey("channelType"))
			{
				channelType = extras.getInt("channelType");
				ChatServiceController.setCurrentChannelType(channelType);
			}
			
			if (extras.containsKey("isSecondLvList"))
			{
				isSecondLvList = extras.getBoolean("isSecondLvList");
			}
			if (extras.containsKey("isGoBack"))
			{
				isGoBack = extras.getBoolean("isGoBack");
			}

			if (extras.containsKey("channelId"))
			{
				channelId = extras.getString("channelId");
			}
		}

		if (!isSecondLvList)
		{
			if (ChannelManager.isMainMsgChannel(channelId))
			{
				if(!isGoBack) LogUtil.trackPageView("ShowChannelList-" + channelId);
				fragmentClass = MsgMailListFragment.class;
			}
			else
			{
				if(!isGoBack && !MainListFragment.canJumpToSecondaryList())
				{
					LogUtil.trackPageView("ShowChannelList");
				}
				fragmentClass = MainListFragment.class;
			}
		}
		else
		{
			if(!isGoBack) LogUtil.trackPageView("ShowChannelList-" + channelId);
			fragmentClass = SysMailListFragment.class;
		}

		ChatServiceController.toggleFullScreen(false, true, this);

		super.onCreate(savedInstanceState);
	}
	
	protected void showBackground()
	{
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		ChatServiceController.setCurrentChannelType(channelType);
	}

	@Override
	public void onDestroy()
	{
		if (ChatServiceController.isReturningToGame && !ChannelListFragment.preventSecondChannelId)
		{
			ChannelListFragment.rememberSecondChannelId = true;
		}
		else
		{
			ChannelListFragment.rememberSecondChannelId = false;
		}
		super.onDestroy();
	}

	public void onBackButtonClick()
	{
		if (fragment != null && fragment instanceof ChannelListFragment && ((ChannelListFragment) fragment).handleBackPressed())
		{
			return;
		}
		super.onBackButtonClick();
	}

	@Override
	public void onBackPressed()
	{
		if (fragment != null && fragment instanceof ChannelListFragment && ((ChannelListFragment) fragment).handleBackPressed())
		{
			return;
		}
		super.onBackPressed();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			// ????????????????????????????????????????????????????????????????????????activity????????????????????????????????????onResume???????????????????????????
			// getFragment().onBecomeVisible();
		}
		else
		{

		}
	}

	public void hideProgressBar()
	{
		// ??????????????????????????????????????????????????????????????????
		if (!(getFragment() != null && getFragment().adapter != null && getFragment().adapter.isLoadingMore))
		{
			super.hideProgressBar();
		}
	}
}
