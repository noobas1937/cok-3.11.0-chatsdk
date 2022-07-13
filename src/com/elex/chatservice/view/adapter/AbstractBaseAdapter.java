package com.elex.chatservice.view.adapter;

import java.util.List;

import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.AbstractBaseActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AbstractBaseAdapter extends BaseAdapter
{

	public static final String[]	MAIN_CHANNEL_ORDERS			= {
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

	public static final int			ITEM_VIEW_TYPE_SWIPE_MENU	= 0;
	public static final int			ITEM_VIEW_TYPE_LOADING_MORE	= 1;
	public static final int			ITEM_VIEW_TYPE_TIP			= 2;

	protected static final int		VIEW_TYPE_ENABLE			= 0;
	protected static final int		VIEW_TYPE_DISABLE			= 1;

	public List<ChannelListItem>	list						= null;
	public AbstractBaseActivity		context;
	public int						channelType;
	public String					channelId;
	public boolean					isLoadingMore;
	public LayoutInflater			inflater;

	public AbstractBaseAdapter(AbstractBaseActivity context, int channelType, String channelId)
	{
		this.channelType = channelType;
		this.channelId = channelId;
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	protected boolean swipeEnableByViewType(int viewType)
	{
		if (viewType == VIEW_TYPE_ENABLE)
			return true;
		else
			return viewType != VIEW_TYPE_DISABLE;
	}

	public void refreshAdapterList()
	{

	}

	public void reloadData()
	{
	}

	public boolean hasMoreData()
	{
		return false;
	}

	public boolean hasMoreUnreadData()
	{
		return false;
	}

	public void loadMoreData()
	{
	}

	public void loadMoreUnreadData()
	{
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getCount()
	{
		if (list != null)
			return list.size();
		else
			return 0;
	}

	public void notifyDataSetChangedOnUI()
	{
		if (context == null)
			return;

		context.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
				context.notifyDataSetChanged();
			}
		});
	}

	public void refreshMenu()
	{
		if (context.isInEditMode())
		{
			context.getListView().smoothCloseMenu();
			context.getListView().setSwipEnable(false);
		}
		else
		{
			context.getListView().setSwipEnable(true);
		}
	}

	public void refreshOrder()
	{
		SortUtil.getInstance().refreshListOrder(list, ChannelListItem.class);
		notifyDataSetChangedOnUI();
	}

	@Override
	public Object getItem(int position)
	{
		if (position >= 0 && position < list.size())
			return list.get(position);
		else
			return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return null;
	}

}
