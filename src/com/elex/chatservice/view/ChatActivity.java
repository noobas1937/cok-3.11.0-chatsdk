package com.elex.chatservice.view;

import java.util.HashMap;
import java.util.Map;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.LogController;
import com.elex.chatservice.host.DummyHost;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

import android.os.Bundle;

public final class ChatActivity extends MyActionBarActivity
{
	public int	channelType;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			this.bundle = new Bundle(extras);
			if (extras.getInt("channelType") >= 0)
			{
				channelType = extras.getInt("channelType");
				ChatServiceController.setCurrentChannelType(channelType);
			}
		}

		fragmentClass = ChatFragmentNew.class;

		ChatServiceController.toggleFullScreen(false, true, this);

		super.onCreate(savedInstanceState);
		long time = System.currentTimeMillis() - LogController.getInstance().startChatTime;
		Map<String,String> map = new HashMap<String, String>();
		map.put("time",String.valueOf(time));
		LogController.getInstance().event("chatStart",map);
	}
	
	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		ChatServiceController.setCurrentChannelType(ChatServiceController.getCurrentChannelType());
	}
	
	@Override
	public void onBackButtonClick()
	{
		if(ChatServiceController.getChatFragment()!=null && ChatServiceController.getChatFragment().isSettingCustomChannel && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
			ChatServiceController.getChatFragment().hideCustomChannelSetting(true);
		else
			super.onBackButtonClick();
	}
	
	@Override
	public void onBackPressed()
	{
		if(ChatServiceController.getChatFragment()!=null && ChatServiceController.getChatFragment().isSettingCustomChannel && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
			ChatServiceController.getChatFragment().hideCustomChannelSetting(true);
		else
			super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void exitActivity()
	{
		if (ChatServiceController.getInstance().isInDummyHost()
				&& ((DummyHost) (ChatServiceController.getInstance().host)).actionAfterResume != null)
		{
			((DummyHost) (ChatServiceController.getInstance().host)).actionAfterResume = null;
			return;
		}

		super.exitActivity();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			// 这里调onBecomeVisible()与adjustHeight中调差不多
			// showProgressBar();
			// ((ChatFragment) fragment).onBecomeVisible();
		}
		else
		{
		}
	}
}