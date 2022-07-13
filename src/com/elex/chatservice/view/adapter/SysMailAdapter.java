package com.elex.chatservice.view.adapter;

import org.apache.commons.lang.StringUtils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;
import com.elex.chatservice.view.MainListFragment;
import com.elex.chatservice.view.MsgMailListFragment;

public class SysMailAdapter extends AbstractMailListAdapter
{
	public static final int	VIEW_TYPE_NONE				= 0;
	public static final int	VIEW_TYPE_DELETE			= 1;
	public static final int	VIEW_TYPE_READ_AND_DELETE	= 2;
	public static final int	VIEW_TYPE_READ				= 3;
	public static final int	VIEW_TYPE_RECYCLE_TIP		= 4;
	
	public ChatChannel	parentChannel;

	public SysMailAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		super(context, fragment);

		reloadData();
	}

	public boolean hasMoreData()
	{
		if (MailManager.hasMoreNewMailToGet)
		{
			return false;
		}
		else
		{
			int dbCount = parentChannel.getSysMailCountInDB();
			return dbCount > parentChannel.mailDataList.size();
		}
		
	}
	
	public boolean hasMoreUnreadData()
	{
		if (MailManager.hasMoreNewMailToGet)
		{
			return false;
		}
		else
		{
			int dbUnreadCount = parentChannel.getUnreadSysMailCountInDB();
			return dbUnreadCount > parentChannel.getUnreadCountInMailList();
		}
		
	}

	public synchronized void loadMoreData()
	{
//		MailData lastItem = null;
//		if (parentChannel.mailDataList.size() > 0)
//		{
//			lastItem = parentChannel.mailDataList.get(parentChannel.mailDataList.size() - 1);
//		}
		if(StringUtils.isNotEmpty(parentChannel.channelID) && parentChannel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
			ChannelManager.getInstance().loadMoreRecycleBinMailFromDB(parentChannel, parentChannel.latestLoadedMailRecycleTime);
		else
			ChannelManager.getInstance().loadMoreSysMailFromDB(parentChannel, parentChannel.latestLoadedMailCreateTime);
	}
	
	public synchronized void loadMoreUnreadData()
	{
		MailData lastItem = null;
		if (parentChannel.mailDataList.size() > 0)
		{
			lastItem = parentChannel.mailDataList.get(parentChannel.mailDataList.size() - 1);
		}
		ChannelManager.getInstance().loadMoreUnreadSysMailFromDB(parentChannel, lastItem != null ? lastItem.getCreateTime() : -1);
	}

	public void reloadData()
	{
		parentChannel = ChannelManager.getInstance().getChannel(context.channelType, fragment.channelId);
		if (list.size() < ChannelManager.LOAD_MORE_COUNT && 
				((!fragment.mailReadStateChecked && parentChannel.mailDataList.size() < ChannelManager.LOAD_MORE_COUNT && hasMoreData()))
				|| (fragment.mailReadStateChecked && parentChannel.getUnreadCountInMailList() < ChannelManager.LOAD_MORE_COUNT && hasMoreUnreadData()))
		{
			context.showProgressBar();
			isLoadingMore = true;
			if(fragment.mailReadStateChecked)
				loadMoreUnreadData();
			else
				loadMoreData();
		}
		else
		{
			refreshAdapterList();
		}
	}
	
	public void refreshAdapterList()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		list.clear();
		if (context.channelType != -1 && !fragment.channelId.equals(""))
		{
			if (parentChannel != null && parentChannel.mailDataList!=null)
			{
				if(fragment.mailReadStateChecked)
				{
					for(int i = 0;i<parentChannel.mailDataList.size();i++)
					{
						MailData mail = parentChannel.mailDataList.get(i);
						if(mail!=null && mail.isUnread())
							list.add(mail);
					}
				}
				else
				{
					list.addAll(parentChannel.mailDataList);
				}
			}
		}
		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}
	
	public void refreshOrder()
	{
		if(StringUtils.isNotEmpty(fragment.channelId) && fragment.channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
		{
			SortUtil.getInstance().sortByRecycleTime(list, ChannelListItem.class);
			notifyDataSetChangedOnUI();
		}
		else
		{
			super.refreshOrder();
		}
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		int viewType = getItemViewType(position);
		if(convertView == null)
		{
			if(viewType == VIEW_TYPE_RECYCLE_TIP)
			{
				convertView = View.inflate(context, R.layout.cs__list_text_item, null);
				TextView tipTextView = (TextView) convertView.findViewById(R.id.recycle_mail_tip);
				convertView.setTag(tipTextView);
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
		
		if(viewType == VIEW_TYPE_RECYCLE_TIP)
		{
			TextView tipTextView = (TextView) convertView.getTag();
			if(tipTextView!=null)
				tipTextView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RECYCLE_CHANNEL));
		}
		else
		{
			MailData mailData = (MailData) getItem(position);
			if (mailData != null)
			{
				MailViewHolder holder = (MailViewHolder) convertView.getTag();
				int bgColor = 0;
				if (ChatServiceController.isNewMailUIEnable)
				{
					bgColor = MailManager.getColorByChannelId(parentChannel.channelID);
				}
				if(holder.top_item_divider!=null)
				{
					if(parentChannel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
					{
						if(position == 1)
							holder.top_item_divider.setVisibility(View.VISIBLE);
						else
							holder.top_item_divider.setVisibility(View.INVISIBLE);
					}
					else
						holder.top_item_divider.setVisibility(View.INVISIBLE);
				}
				holder.setContent(context, mailData, false, null, mailData.nameText, mailData.contentText, mailData.timeText,
						fragment.isInEditMode(), position, bgColor);
				setIcon(mailData, holder.item_icon);
				refreshMenu();
			}
		}

		return convertView;
	}
	
	private int getItemType(ChannelListItem item)
	{
		if (item != null && item instanceof MailData)
		{
			MailData mail = (MailData) item;
			if(mail == null)
			{
				return VIEW_TYPE_NONE;
			}
			else
			{
				if(mail.isLock())
				{
					return VIEW_TYPE_NONE;
				}
				else
				{
					if (mail.isUnread())
						return VIEW_TYPE_READ_AND_DELETE;
					else
						return VIEW_TYPE_DELETE;
				}
			}
		}
		else
		{
			return VIEW_TYPE_NONE;
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		if(parentChannel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
		{
			if(position == 0)
				return VIEW_TYPE_RECYCLE_TIP; 
			else
			{
				ChannelListItem item = getItem(position);
				return getItemType(item);
			}
		}
		else
		{
			ChannelListItem item = getItem(position);
			return getItemType(item);
		}
	}
	
	@Override
	public int getViewTypeCount()
	{
		return 5;
	}

	private void setIcon(MailData mailData, ImageView iconView)
	{
		String mailIcon = mailData.mailIcon;
		if (mailIcon.equals(""))
		{
			int defaultId = ResUtil.getId(context, "drawable", "g044");
			try
			{
				if (defaultId != 0)
					iconView.setImageDrawable(context.getResources().getDrawable(defaultId));
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		else
		{
			int idFlag = ResUtil.getId(context, "drawable", mailIcon);
			try
			{
				if (idFlag != 0)
				{
					// 极少情况可能发生 Fatal Exception: java.lang.OutOfMemoryError
					// ，且没有被try捕获
					iconView.setImageDrawable(context.getResources().getDrawable(idFlag));
				}
				else
				{
					int defaultId = ResUtil.getId(context, "drawable", "g044");
					if (defaultId != 0)
						iconView.setImageDrawable(context.getResources().getDrawable(defaultId));
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void destroy()
	{
		parentChannel = null;
		super.destroy();
	}
}
