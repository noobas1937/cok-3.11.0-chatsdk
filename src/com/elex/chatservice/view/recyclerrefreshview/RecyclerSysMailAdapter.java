package com.elex.chatservice.view.recyclerrefreshview;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuLayoutHolder;

public class RecyclerSysMailAdapter extends AbstractRecyclerAdapter
{
	public static final int	VIEW_TYPE_NONE				= 0;
	public static final int	VIEW_TYPE_DELETE			= 1;
	public static final int	VIEW_TYPE_READ_AND_DELETE	= 2;
	public static final int	VIEW_TYPE_READ				= 3;
	public static final int	VIEW_TYPE_RECYCLE_TIP		= 4;

	public ChatChannel		parentChannel;

	public RecyclerSysMailAdapter(AbstractRecyclerActivity context, int channelType, String channelId)
	{
		super(context, channelType, channelId);
		parentChannel = ChannelManager.getInstance().getChannel(channelType, channelId);
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
			if (parentChannel != null)
			{
				int dbCount = parentChannel.getSysMailCountInDB();
				return dbCount > parentChannel.mailDataList.size();
			}
			else
			{
				return false;
			}
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
			if (parentChannel != null)
			{
				int dbUnreadCount = parentChannel.getUnreadSysMailCountInDB();
				return dbUnreadCount > parentChannel.getUnreadCountInMailList();
			}
			else
			{
				return false;
			}
		}

	}

	public void loadMoreData()
	{
		if (parentChannel != null)
		{
			if (StringUtils.isNotEmpty(parentChannel.channelID) && parentChannel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
				ChannelManager.getInstance().loadMoreRecycleBinMailFromDB(parentChannel, parentChannel.latestLoadedMailRecycleTime);
			else
				ChannelManager.getInstance().loadMoreSysMailFromDB(parentChannel, parentChannel.latestLoadedMailCreateTime);
		}
	}

	public void loadMoreUnreadData()
	{
		if (parentChannel != null)
		{
			MailData lastItem = null;
			if (parentChannel.mailDataList.size() > 0)
				lastItem = parentChannel.mailDataList.get(parentChannel.mailDataList.size() - 1);
			ChannelManager.getInstance().loadMoreUnreadSysMailFromDB(parentChannel, lastItem != null ? lastItem.getCreateTime() : -1);

		}
	}

	public void reloadData()
	{
		if (list == null)
			list = new ArrayList<ChannelListItem>();
		else
			list.clear();
		if (list.size() < ChannelManager.LOAD_MORE_COUNT &&
				((!context.mailReadStateChecked && parentChannel != null && parentChannel.mailDataList != null && parentChannel.mailDataList.size() < ChannelManager.LOAD_MORE_COUNT && hasMoreData()))
				|| (context.mailReadStateChecked && parentChannel.getUnreadCountInMailList() < ChannelManager.LOAD_MORE_COUNT && hasMoreUnreadData()))
		{
			context.showProgressBar();
			isLoadingMore = true;
			if (context.mailReadStateChecked)
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
		if (context.channelType != -1 && StringUtils.isNotEmpty(channelId))
		{
			if (parentChannel != null && parentChannel.mailDataList != null)
			{
				if (context.mailReadStateChecked)
				{
					for (int i = 0; i < parentChannel.mailDataList.size(); i++)
					{
						MailData mail = parentChannel.mailDataList.get(i);
						if (mail != null && mail.isUnread())
							list.add(mail);
					}
				}
				else
				{
					list.addAll(parentChannel.mailDataList);
				}
			}
		}
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "list size", list.size());
		if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
			SortUtil.getInstance().sortByRecycleTime(list, ChannelListItem.class);
		context.notifyDataSetChanged();
		context.setNoMailTipVisible(list.size() <= 0);
	}

	public void refreshOrder()
	{
		if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
			SortUtil.getInstance().sortByRecycleTime(list, ChannelListItem.class);
		notifyDataSetChangedOnUI();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int viewType)
	{
		if (viewType == ITEM_VIEW_TYPE_TIP)
		{
			View convertView = View.inflate(context, R.layout.cs__list_text_item, null);
			return new RecycleBinMailRecyclerViewHolder(convertView);
		}
		else if (viewType == ITEM_VIEW_TYPE_LOADING_MORE)
		{
			View convertView = View.inflate(context, R.layout.mail_loading_view, null);
			return new LoadMoreRecyclerViewHolder(convertView);
		}
		else
		{
			View convertView = View.inflate(context, R.layout.recycler_mail_list_item, null);
			return new MailRecyclerViewHolder(convertView);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position)
	{
		SwipeMenuLayoutHolder holder = (SwipeMenuLayoutHolder) viewHolder;
		if (holder != null && holder.viewHolder != null)
		{
			if (holder.viewHolder instanceof RecycleBinMailRecyclerViewHolder)
			{
				RecycleBinMailRecyclerViewHolder mailHolder = (RecycleBinMailRecyclerViewHolder) holder.viewHolder;
				mailHolder.recycle_mail_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RECYCLE_CHANNEL));
			}
			else if (holder.viewHolder instanceof LoadMoreRecyclerViewHolder)
			{
				LoadMoreRecyclerViewHolder loadHolder = (LoadMoreRecyclerViewHolder) holder.viewHolder;
				if (loadHolder != null)
				{
					if (hasMoreData() && position!=0)
						loadHolder.loading_layout.setVisibility(View.VISIBLE);
					else
						loadHolder.loading_layout.setVisibility(View.GONE);
				}
			}
			else
			{
				final MailRecyclerViewHolder mailHolder = (MailRecyclerViewHolder) holder.viewHolder;
				if (mailHolder != null)
				{
					final MailData mailData = (MailData) getItem(position);
					if (mailData != null)
					{
						int bgColor = MailManager.getColorByChannelId(channelId);
						mailHolder.setContent(mailData, mailData.nameText, mailData.contentText, mailData.timeText,
								context.isInEditMode(), bgColor);
						setIcon(mailData, mailHolder.mail_icon);
						refreshMenu();
						
						if (mailHolder.mail_checkbox != null)
							mailHolder.mail_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{

								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
								{
									mailData.checked = isChecked;
								}
							});
						
						mailHolder.mail_content_layout.setOnClickListener(new OnClickListener()
						{
							
							@Override
							public void onClick(View v)
							{
								if(context.isInEditMode())
								{
									mailData.checked = !mailData.checked;
									if(mailHolder.mail_checkbox!=null)
										mailHolder.mail_checkbox.setChecked(mailData.checked);
									return;
								}
								else
									context.openItem(mailData);
							}
						});
					}
				}
			}
		}
	}

	private int getItemType(ChannelListItem item)
	{
		if (item != null && item instanceof MailData)
		{
			MailData mail = (MailData) item;
			if (mail.isLock())
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
		else
		{
			return VIEW_TYPE_NONE;
		}
	}

	@Override
	public int getItemCount()
	{
		if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
			return super.getItemCount() + 1;
		else
			return super.getItemCount();
	}

	@Override
	public ChannelListItem getItem(int position)
	{
		if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
		{
			if (position == 0)
				return null;
			else
				return super.getItem(position - 1);
		}
		else
			return super.getItem(position);
	}

	@Override
	public int getItemViewType(int position)
	{
		int count = getItemCount();
		if (count > 0 && position == count - 1)
		{
			if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
			{
				if (count == 1)
					return ITEM_VIEW_TYPE_TIP;
				else
					return ITEM_VIEW_TYPE_LOADING_MORE;
			}
			else
			{
				return ITEM_VIEW_TYPE_LOADING_MORE;
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_RECYCLE_BIN) && position == 0)
			{
				return ITEM_VIEW_TYPE_TIP;
			}
			else
			{
				return ITEM_VIEW_TYPE_SWIPE_MENU;
			}
		}
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
}
