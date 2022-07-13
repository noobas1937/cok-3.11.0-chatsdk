package com.elex.chatservice.view.recyclerrefreshview;

import java.util.ArrayList;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuLayoutHolder;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class RecyclerMainChannelAdapter extends AbstractRecyclerAdapter
{

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position)
	{
		super.onBindViewHolder(viewHolder, position);
		SwipeMenuLayoutHolder holder = (SwipeMenuLayoutHolder) viewHolder;
		if(holder!=null && holder.viewHolder!=null)
		{
			if(holder.viewHolder instanceof CategoryRecyclerViewHolder)
			{
				final CategoryRecyclerViewHolder categoryHolder = (CategoryRecyclerViewHolder)holder.viewHolder;
				if(categoryHolder!=null)
				{
					int bgColor = 0;
					
					final ChatChannel channel = (ChatChannel) getItem(position);

					if(channel!=null)
					{
						categoryHolder.setContent(context, channel, true, null, channel.nameText, channel.contentText, channel.timeText,
								context.isInEditMode(), bgColor);

						setIcon(channel, categoryHolder.item_icon);
					}
				}
			}
		}
	}
	
	public void setIcon(final ChatChannel channel, final ImageView imageView)
	{
		ImageUtil.setHeadImage(context, channel.channelIcon, imageView, null);
	}
	
	@Override
	public int getItemCount()
	{
		if(list == null)
			return 0;
		else
			return list.size();
	}
	
	@Override
	public ChannelListItem getItem(int position)
	{
		int itemCount = getItemCount();
		if (itemCount == 0 || position < 0 || position >= itemCount)
			return null;
		else
			return list.get(position);
	}
	
	public RecyclerMainChannelAdapter(AbstractRecyclerActivity context,int channelType,String channelId)
	{
		super(context,channelType,channelId);
		reloadData();
	}
	
	public void reloadData()
	{
		if(list == null)
			list = new ArrayList<ChannelListItem>();
		else
			list.clear();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		list.addAll(ChannelManager.getInstance().getAllMailChannel());
		refreshOrder();
		context.setNoMailTipVisible(list.size() <= 0);
	}
	
	public void refreshOrder()
	{
		if(list == null)
			return;
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
					list.add(0, list.remove(j));
					break;
				}
			}
		}

		notifyDataSetChangedOnUI();
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int itemType)
	{
		View convertView = inflater.inflate(R.layout.recycler_category_list_item, viewgroup,false);
		return new CategoryRecyclerViewHolder(convertView);
	}
	
}
