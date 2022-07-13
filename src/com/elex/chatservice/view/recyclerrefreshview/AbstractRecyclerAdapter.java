package com.elex.chatservice.view.recyclerrefreshview;

import java.util.List;

import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.SortUtil;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class AbstractRecyclerAdapter extends RecyclerView.Adapter
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
	
	public static final int			ITEM_VIEW_TYPE_SWIPE_MENU		= 0;
	public static final int			ITEM_VIEW_TYPE_LOADING_MORE		= 1;
	public static final int			ITEM_VIEW_TYPE_TIP				= 2;
	

	protected static final int		VIEW_TYPE_ENABLE			= 0;
	protected static final int		VIEW_TYPE_DISABLE			= 1;

	public List<ChannelListItem>	list						= null;
	public AbstractRecyclerActivity	context;
	public int						channelType;
	public String					channelId;
	public boolean					isLoadingMore;
	public LayoutInflater			inflater;

	public AbstractRecyclerAdapter(AbstractRecyclerActivity context, int channelType, String channelId)
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

	public ChannelListItem getItem(int position)
	{
		if (list == null || list.size() == 0 || position < 0 || position >= list.size())
			return null;
		else
			return list.get(position);
	}

	@Override
	public int getItemCount()
	{
		if (list != null)
			return list.size() + 1;
		else
			return 0;
	}

	@Override
	public void onBindViewHolder(ViewHolder arg0, int arg1)
	{
		refreshMenu();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int i)
	{
		return null;
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

}
