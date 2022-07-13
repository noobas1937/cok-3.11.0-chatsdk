package com.elex.im.ui.view;

import java.util.Iterator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.elex.im.CokChannelDef;
import com.elex.im.CokConfig;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.ui.model.ChannelView;

public class ChatPagerAdapter extends FragmentPagerAdapter
{
	SparseArray<ChatFragment>	registeredFragments	= new SparseArray<ChatFragment>();
	ChannelView[]			channelViews;
    
	public ChatPagerAdapter(FragmentManager fm)
	{
		super(fm);
		initChannelViews();
	}
	
	private ChannelView[] getChannelViews()
	{
		if (CokChannelDef.isInMailDialog())
		{
			return new ChannelView[] { CokConfig.getInstance().getMailChannelView() };
		}
		else if (CokChannelDef.isInChat())
		{
			if (!CokConfig.needCrossServerBattleChat())
			{
				return new ChannelView[] {
						CokConfig.getInstance().getCountryChannelView(),
						CokConfig.getInstance().getAllianceChannelView(),
						CokConfig.getInstance().getCustomChannelView() };
			}
			else
			{
				return new ChannelView[] {
						CokConfig.getInstance().getCountryChannelView(),
						CokConfig.getInstance().getAllianceChannelView(),
						CokConfig.getInstance().getCustomChannelView(),
						CokConfig.getInstance().getBattleFieldChannelView() };
			}
		}
		return new ChannelView[] {};
	}
	
	private void initChannelViews()
	{
		channelViews = getChannelViews();
		for (int i = 0; i < channelViews.length; i++)
		{
			ChannelView channelView = channelViews[i];
			channelView.tab = i;

			if (channelView.channel != null)
			{
				if (!channelView.channel.hasInitLoaded()) //  && CokChannelDef.isInChat()
					channelView.channel.loadMoreMsg();
				
				channelView.channel.clearFirstNewMsg();
				channelView.channel.setChannelView(channelView);
			}
		}
	}
	
	// 被ServiceInterface中的onJoinAnnounceInvitationSuccess回调调用
	public void onJoinAnnounceInvitationSuccess()
	{
		for (int i = 0; i < channelViews.length; i++)
		{
			ChannelView channelView = channelViews[i];
			if (channelView != null && CokChannelDef.canHandleJoinAnnounceInvitation(channelView.channelType))
			{
				// 隐藏noAllianceFrameLayout，点联盟自然会调用
				channelView.getMessagesAdapter().onJoinAnnounceInvitationSuccess();
			}
		}
	}
	
	public ChannelView getChannelView(int i)
	{
		return channelViews[i];
	}

	@Override
	public Fragment getItem(int i)
	{
        try
		{
    		ChatFragment fragment = new ChatFragment(channelViews[i]);
    		Bundle args = new Bundle();
    		// Our object is just an integer :-P
    		args.putInt(ChatFragment.ARG_OBJECT, i + 1);
    		fragment.setArguments(args);
    		return fragment;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
        return null;
	}

	@Override
	public int getCount()
	{
		return channelViews.length;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		if(position == 0)
		{
			if (CokConfig.isInCrossFightServer())
			{
				return LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD);
			}
			else
			{
				return LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY);
			}
		}else if(position == 1){
			return LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE);
		}else if(position == 2){
			return LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT);
		}
		
		return "国家";
	}

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
    	ChatFragment fragment = (ChatFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public ChatFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
    
    public ChatFragment getFragment(Channel channel) {
    	for (int i = 0; i < registeredFragments.size(); i++)
		{
    		if(registeredFragments.get(i).getCurrentChannel() == channel)
    		{
    			return registeredFragments.get(i);
    		}
		}
        return null;
    }

	public void saveStates()
	{
		if(channelViews == null)
			return;
		for (int i = 0; i < channelViews.length; i++)
		{
			ChannelView channelView = channelViews[i];
			ChatFragment.saveState(channelView);
		}
	}
	
	// 用代码选择的话，还得手动调onShow
//	private void selectFirstTab()
//	{
//		if (CokChannelDef.isInAlliance())
//		{
//			showTab(TAB_ALLIANCE);
//		}
//		else if (CokChannelDef.isInCountryTab())
//		{
//			if(channelViews.size() == 4 && CokConfig.needCrossServerBattleChat())
//				showTab(TAB_BATTLE_FIELD);
//			else
//				showTab(TAB_COUNTRY);
//		}
//		else if (CokChannelDef.isInCustomChat())
//		{
//			showTab(TAB_CUSTOM);
//		}
//		else if (CokChannelDef.isInMailDialog())
//		{
//			showTab(TAB_MAIL);
//		}
//	}
}
