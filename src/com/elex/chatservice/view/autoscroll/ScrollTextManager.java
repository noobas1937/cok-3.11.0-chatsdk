package com.elex.chatservice.view.autoscroll;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.db.DBDefinition;

public class ScrollTextManager
{
	private static ScrollTextManager	_instance			= null;
	private List<MsgItem>				mScrollQueue		= null;
	private List<MsgItem>				mBattleScrollQueue	= null;
	private TextView					mHornNameTextView	= null;
	private RelativeLayout				horn_layout			= null;
	private TextView					mBattleHornNameTextView	= null;
	private RelativeLayout				battle_horn_layout			= null;

	private ScrollTextManager()
	{
		mScrollQueue = new ArrayList<MsgItem>();
		mBattleScrollQueue = new ArrayList<MsgItem>();
	}

	public void setHornLayout(RelativeLayout layout, TextView view,int channelType)
	{
		if(channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			horn_layout = layout;
			mHornNameTextView = view;
		}
		else if(channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			battle_horn_layout = layout;
			mBattleHornNameTextView = view;
		}
		
	}

	public static ScrollTextManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (ScrollTextManager.class)
			{
				if (_instance == null)
				{
					_instance = new ScrollTextManager();
				}
			}
		}
		return _instance;
	}

	public void showScrollText(MsgItem msgItem, ScrollText scrollTextView, TextView hornNameTextView, RelativeLayout layout,int channelType)
	{
		if(layout!=null && layout.getVisibility()!=View.VISIBLE)
			layout.setVisibility(View.VISIBLE);
		setHornLayout(layout, hornNameTextView,channelType);
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null)
		{
			mScrollQueue.add(msgItem);
			scrollTextView.scrollNext();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue != null)
		{
			mBattleScrollQueue.add(msgItem);
			scrollTextView.scrollNext();
		}
		
	}

	public int getScrollQueueLength(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null)
			return mScrollQueue.size();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue != null)
			return mBattleScrollQueue.size();
		return 0;
	}

	public MsgItem getNextText(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null && !mScrollQueue.isEmpty())
		{
			return mScrollQueue.get(0);
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue != null && !mBattleScrollQueue.isEmpty())
		{
			return mBattleScrollQueue.get(0);
		}
		return null;
	}

	public void pop(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null && !mScrollQueue.isEmpty() && mScrollQueue.size() > 1)
			mScrollQueue.remove(0);
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue != null && !mBattleScrollQueue.isEmpty() && mBattleScrollQueue.size() > 1)
			mBattleScrollQueue.remove(0);
	}

	public void clear(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null && !mScrollQueue.isEmpty())
			mScrollQueue.clear();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue !=null && !mBattleScrollQueue.isEmpty())
			mBattleScrollQueue.clear();
	}

	public void push(MsgItem msgItem,int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null && !mScrollQueue.contains(msgItem))
			mScrollQueue.add(msgItem);
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue!=null && !mBattleScrollQueue.contains(msgItem))
			mBattleScrollQueue.add(msgItem);
	}

	public void shutDownScrollText(ScrollText scrollTextView,int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mScrollQueue != null && !mScrollQueue.isEmpty())
			mScrollQueue.clear();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleScrollQueue != null && !mBattleScrollQueue.isEmpty())
			mBattleScrollQueue.clear();
		scrollTextView.stopScroll();
	}

	public void hideScrollLayout(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && horn_layout != null)
			horn_layout.setVisibility(View.GONE);
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && battle_horn_layout != null)
			battle_horn_layout.setVisibility(View.GONE);
	}

	public void setHornName(String name,int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && mHornNameTextView != null)
			mHornNameTextView.setText(name);
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && mBattleHornNameTextView != null)
			mBattleHornNameTextView.setText(name);
	}
}
