package com.elex.im.ui.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;

import com.elex.im.CokChannelDef;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.ChannelChangeEvent;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.model.ChannelView;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;

public final class ChatActivity extends MyActionBarActivity
{
	public int					channelType;
	private ChatPagerAdapter	chatPagerAdapter;
	private ViewPager			mViewPager;
	private TabLayout			tabLayout;
	private int					previousTab	= -1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		hasTab = true;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			this.bundle = new Bundle(extras);
			if (extras.getInt("channelType") >= 0)
			{
				channelType = extras.getInt("channelType");
				GSController.setCurrentChannelType(channelType);
			}
		}

//		if(!hasTab){
//			fragmentClass = ChatFragmentNew.class;
//		}
		UIManager.toggleFullScreen(false, true, this);

		super.onCreate(savedInstanceState);

		if(hasTab){
			initEvent();
			initViewPager();
		}
	}
	
	private void initViewPager()
	{
		chatPagerAdapter = new ChatPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		
		mViewPager.setAdapter(chatPagerAdapter);
		tabLayout.setupWithViewPager(mViewPager);
//        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
//        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
//        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
		
		// 会导致点击tab失效，据说是因为setupWithViewPager失效
		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
		    @Override
		    public void onTabSelected(TabLayout.Tab tab){
		        try
				{
					mViewPager.setCurrentItem(tab.getPosition());
					onShowFragment(tab.getPosition());
					getCurrentFragment().onSelected(previousTab != -1 && previousTab != tab.getPosition());
					previousTab = tab.getPosition();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		    }

			@Override
			public void onTabReselected(Tab arg0)
			{
			}

			@Override
			public void onTabUnselected(Tab arg0)
			{
			}
		});
		
		tabLayout.setVisibility(chatPagerAdapter.getCount() > 1 ? View.VISIBLE : View.GONE);
	}
	
	public ChatFragment getCurrentFragment()
	{
		int index = mViewPager.getCurrentItem();
		return chatPagerAdapter.getRegisteredFragment(index);
	}

	private void onShowFragment(int position)
	{
		try
		{
			ChannelView channelView = chatPagerAdapter.getChannelView(position);
			if (channelView != null)
			{
				channelView.setLoadingStart(false);
				
				LogUtil.trackAction(CokChannelDef.getInstance().getChannelDef(channelView.channelType).getPageViewTrackName());

				JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
						new Object[] { Integer.valueOf(channelView.channelType) });
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onBackButtonClick()
	{
		if(UIManager.getChatFragment()!=null && UIManager.getChatFragment().isSettingCustomChannel && CokChannelDef.isInCustomChat())
			UIManager.getChatFragment().hideCustomChannelSetting();
		else
			super.onBackButtonClick();
	}
	
	@Override
	public void onBackPressed()
	{
		if(UIManager.getChatFragment()!=null && UIManager.getChatFragment().isSettingCustomChannel && CokChannelDef.isInCustomChat())
			UIManager.getChatFragment().hideCustomChannelSetting();
		else
			super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void exitActivity()
	{
		// 貌似没用
//		if (ChatServiceController.getInstance().isInDummyHost()
//				&& ((DummyHost) (IMCore.getInstance().host)).actionAfterResume != null)
//		{
//			((DummyHost) (IMCore.getInstance().host)).actionAfterResume = null;
//			return;
//		}

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
	
	protected void actualAdjustSize()
	{
		super.actualAdjustSize();

		if (hasTab)
		{
			LinearLayout.LayoutParams tabLayoutParams = (LinearLayout.LayoutParams) tabLayout.getLayoutParams();
			tabLayoutParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			tabLayout.setLayoutParams(tabLayoutParams);
		}
		
//		ScaleUtil.adjustTextSize(buttonCountry, ConfigManager.scaleRatio);
	}

	protected void saveState()
	{
		chatPagerAdapter.saveStates();
	}

	private void initEvent()
	{
		IMCore.getInstance().addEventListener(ChannelChangeEvent.AFTER_ADD_DUMMY_MSG, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof ChannelChangeEvent)
				{
					afterAddDummyMsg((ChannelChangeEvent) event);
				}
			};
		});
		IMCore.getInstance().addEventListener(ChannelChangeEvent.DATASET_CHANGED, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof ChannelChangeEvent)
				{
					onDataSetChanged((ChannelChangeEvent) event);
				}
			};
		});
		IMCore.getInstance().addEventListener(ChannelChangeEvent.ON_RECIEVE_NEW_MSG, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof ChannelChangeEvent)
				{
					onRecieveNewMsg((ChannelChangeEvent) event);
				}
			};
		});
		IMCore.getInstance().addEventListener(ChannelChangeEvent.ON_RECIEVE_OLD_MSG, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof ChannelChangeEvent)
				{
					onRecieveOldMsg((ChannelChangeEvent) event);
				}
			};
		});
	}
	
	private ChatFragment getFragment(ChannelChangeEvent event)
	{
		return chatPagerAdapter.getFragment(event.channel);
	}
	
	public ChatFragment getFragment(Channel channel)
	{
		return chatPagerAdapter.getFragment(channel);
	}
	
	private void afterAddDummyMsg(ChannelChangeEvent event)
	{
		// 发送后的行为（跳到最后一行），包含了notifyDataSetChanged
		getFragment(event).afterSendMsgShowed();
	}

	private void onDataSetChanged(ChannelChangeEvent event)
	{
		getFragment(event).onDataSetChanged(event);
	}
	
	private void onRecieveNewMsg(ChannelChangeEvent event)
	{
		getFragment(event).onRecieveNewMsg(event);
	}

	private void onRecieveOldMsg(ChannelChangeEvent event)
	{
		getFragment(event).onRecieveOldMsg(event);
	}
}