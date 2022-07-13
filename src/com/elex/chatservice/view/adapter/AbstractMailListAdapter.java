package com.elex.chatservice.view.adapter;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;
import com.elex.chatservice.view.MainListFragment;
import com.elex.chatservice.view.MsgMailListFragment;

public class AbstractMailListAdapter extends BaseAdapter
{
	public static final int	VIEW_TYPE_NONE				= 0;
	public static final int	VIEW_TYPE_DELETE			= 1;
	public static final int	VIEW_TYPE_READ_AND_DELETE	= 2;
	public static final int	VIEW_TYPE_READ				= 3;
	public static final int	VIEW_TYPE_READ_AND_REWARD	= 4;
	public static final int	VIEW_TYPE_CLEAR				= 5;
	
	public ChannelListFragment			fragment;

	protected ChannelListActivity		context;
	public ArrayList<ChannelListItem>	list	= new ArrayList<ChannelListItem>();
	public boolean						isLoadingMore;

	public AbstractMailListAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		this.context = context;
		this.fragment = fragment;
	}

	public void reloadData()
	{
	}

	public void refreshOrder()
	{
		SortUtil.getInstance().refreshListOrder(list, ChannelListItem.class);

		notifyDataSetChangedOnUI();
	}
	
	public void refreshAdapterList()
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
	

	public synchronized void loadMoreData()
	{
	}
	
	public synchronized void loadMoreUnreadData()
	{
	}

	public int getCount()
	{
		if(fragment!=null && StringUtils.isNotEmpty(fragment.channelId) && fragment.channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
		{
			if(list!=null)
				return list.size()+1;
			else 
				return 1;
		}
		else
		{
			if(list!=null)
				return list.size();
			else 
				return 0;
		}
	}

	@Override
	public int getViewTypeCount()
	{
		return 6;
	}

	@Override
	public int getItemViewType(int position)
	{
		return VIEW_TYPE_READ;
	}

	@Override
	public ChannelListItem getItem(int position)
	{
		if(fragment!=null && StringUtils.isNotEmpty(fragment.channelId) && fragment.channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
		{
			if (position-1 >= 0 && position-1 < list.size())
				return list.get(position-1);
		}
		else
		{
			if (position >= 0 && position < list.size())
				return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			if (ChatServiceController.isNewMailUIEnable)
			{
				if (fragment instanceof MainListFragment && !(fragment instanceof MsgMailListFragment))
				{
					if(ConfigManager.getInstance().needRTL())
						convertView = View.inflate(context, R.layout.cs__channel_list_item_category_ar, null);
					else
						convertView = View.inflate(context, R.layout.cs__channel_list_item_category, null);
					convertView.setTag(new CategoryViewHolder(convertView));
				}
				else
				{
					if(ConfigManager.getInstance().needRTL())
						convertView = View.inflate(context, R.layout.cs__channel_list_item_mail_ar, null);
					else
						convertView = View.inflate(context, R.layout.cs__channel_list_item_mail, null);
					convertView.setTag(new MailViewHolder(convertView));
				}
			}
			else
			{
				convertView = View.inflate(context, R.layout.cs__channel_list_item, null);
				convertView.setTag(new MailViewHolder(convertView));
			}
		}
		return convertView;
	}

	protected void refreshMenu()
	{
		if (fragment.isInEditMode())
		{
			fragment.getListView().smoothCloseMenu();
			fragment.getListView().setSwipEnable(false);
		}
		else
		{
			fragment.getListView().setSwipEnable(true);
		}
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
				try
				{
					notifyDataSetChanged();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void destroy()
	{
		list.clear();
		notifyDataSetChanged();
		fragment = null;
		context = null;
		list = null;
	}
}
