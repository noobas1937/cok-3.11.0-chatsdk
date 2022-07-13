package com.elex.chatservice.view.recyclerrefreshview;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.image.AsyncImageLoader;
import com.elex.chatservice.image.ImageLoaderListener;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuLayoutHolder;

public class RecyclerMsgChannelAdapter extends AbstractRecyclerAdapter
{

	public RecyclerMsgChannelAdapter(AbstractRecyclerActivity context, int channelType, String channelId)
	{
		super(context, channelType, channelId);
		if (StringUtils.isNotEmpty(channelId))
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "channelId+RecyclerMsgChannelAdapter",channelId);
			if (channelId.equals(MailManager.CHANNELID_MOD))
				ChatServiceController.contactMode = 1;
			else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
				ChatServiceController.contactMode = 2;
			else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
				ChatServiceController.contactMode = 3;
		}
		reloadData();
	}
	
	public void loadMoreAfterDelete()
	{
		
	}

	public boolean hasMoreData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			int count = getAllMsgChannels().size();

			return count > list.size();
		}
	}

	public boolean hasMoreUnreadData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			List<ChatChannel> msgChannel = getAllMsgChannels();
			int unreadCount = 0;
			for (ChatChannel channel : msgChannel)
			{
				if (channel != null && channel.isUnread())
					unreadCount++;
			}
			return unreadCount > list.size();
		}
	}

	public void loadMoreData()
	{
		if (context != null)
			context.selectedAll(false);
		List<ChatChannel> allMsgChannels = getAllMsgChannels();
		Collections.sort(allMsgChannels);
		List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();

		int moreCount = loadedMsgChannels.size() + 10;
		int actualCount = allMsgChannels.size() > moreCount ? moreCount : allMsgChannels.size();

		List<ChatChannel> subMsgChannels = allMsgChannels.subList(0, actualCount);

		for (int i = 0; i < subMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = subMsgChannels.get(i);
			if (chatChannel != null)
			{
				if (!ChannelManager.isChannelInList(chatChannel, loadedMsgChannels))
				{
					if (!chatChannel.hasInitLoaded())
						chatChannel.loadMoreMsg();
					loadedMsgChannels.add(chatChannel);
				}
			}
		}

		if (context != null)
			context.notifyLoadMoreCompleted();
	}

	public synchronized void loadMoreUnreadData()
	{
		List<ChatChannel> allUnreadMsgChannels = getAllUnreadMsgChannels();
		List<ChatChannel> unreadloadedMsgChannels = getLoadedUnreadMsgChannels();

		int moreCount = unreadloadedMsgChannels.size() + 10;
		int actualCount = allUnreadMsgChannels.size() > moreCount ? moreCount : allUnreadMsgChannels.size();

		List<ChatChannel> subMsgChannels = allUnreadMsgChannels.subList(0, actualCount);

		for (int i = 0; i < subMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = subMsgChannels.get(i);
			if (chatChannel != null)
			{
				if (!ChannelManager.isChannelInList(chatChannel, unreadloadedMsgChannels))
				{
					if (!chatChannel.hasInitLoaded())
						chatChannel.loadMoreMsg();
					unreadloadedMsgChannels.add(chatChannel);
				}
			}
		}

		if (context != null)
			context.onLoadMoreComplete();
	}

	public void reloadData()
	{
		if (list == null)
			list = new ArrayList<ChannelListItem>();
		else
			list.clear();
		if (!context.mailReadStateChecked)
		{
			List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "loadedMsgChannels.size()", loadedMsgChannels.size());

			// 第一次进入列表时，可能loadedMsgChannels已经有内容（刚收到的消息），此时需要loadMoreData一次，否则会只显示刚收到的channel
			if (loadedMsgChannels.size() == 0)
			{
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "    初次加载");
				loadMoreData();
			}
			else
			{
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "    重新加载");
				list.addAll(loadedMsgChannels);
				List<ChatChannel> allMsgChannels = getAllMsgChannels();
				if(loadedMsgChannels.size() < 10 && loadedMsgChannels.size() < allMsgChannels.size())
					loadMoreData();
			}
		}
		else
		{
			List<ChatChannel> loadedUnreadMsgChannels = getLoadedUnreadMsgChannels();
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "loadedUnreadMsgChannels.size()", loadedUnreadMsgChannels.size());

			// 第一次进入列表时，可能loadedMsgChannels已经有内容（刚收到的消息），此时需要loadMoreData一次，否则会只显示刚收到的channel
			if (loadedUnreadMsgChannels.size() == 0)
			{
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "    初次加载");
				loadMoreUnreadData();
			}
			else
			{
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "    重新加载");
				list.addAll(loadedUnreadMsgChannels);
			}
		}

		refreshOrder();

		context.setNoMailTipVisible(list.size() <= 0);
	}

	public void refreshAdapterList()
	{
		list.clear();

		if (context.mailReadStateChecked)
		{
			List<ChatChannel> loadedUnreadMsgChannels = getLoadedUnreadMsgChannels();

			if (loadedUnreadMsgChannels != null)
				list.addAll(loadedUnreadMsgChannels);
		}
		else
		{
			List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();

			if (loadedMsgChannels != null)
				list.addAll(loadedMsgChannels);
		}

		refreshOrder();
		if (context != null)
			context.setNoMailTipVisible(list.size() <= 0);
	}

	private List<ChatChannel> getAllMsgChannels()
	{
		return ChannelManager.getInstance().getAllMsgChannelById(channelId);
	}

	private List<ChatChannel> getAllUnreadMsgChannels()
	{
		List<ChatChannel> allUnreadMsgChannel = new ArrayList<ChatChannel>();
		List<ChatChannel> allMsgChannels = getAllMsgChannels();
		Collections.sort(allMsgChannels);
		for (ChatChannel channel : allMsgChannels)
		{
			if (channel != null && channel.isUnread())
				allUnreadMsgChannel.add(channel);
		}
		return allUnreadMsgChannel;
	}

	private List<ChatChannel> getLoadedMsgChannels()
	{
		return ChannelManager.getInstance().getLoadedChannelListById(channelId);
	}

	private List<ChatChannel> getLoadedUnreadMsgChannels()
	{
		List<ChatChannel> loadedUnreadMsgChannel = new ArrayList<ChatChannel>();
		for (ChatChannel channel : getLoadedMsgChannels())
		{
			if (channel != null && channel.isUnread())
				loadedUnreadMsgChannel.add(channel);
		}
		return loadedUnreadMsgChannel;
	}

	@Override
	public int getItemViewType(int position)
	{
		int count = getItemCount();
		if (count > 0 && position == count - 1)
			return ITEM_VIEW_TYPE_LOADING_MORE;
		else
			return ITEM_VIEW_TYPE_SWIPE_MENU;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int itemType)
	{
//		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "itemType", itemType);
		if (itemType == ITEM_VIEW_TYPE_LOADING_MORE)
		{
			View convertView = inflater.inflate(R.layout.mail_loading_view, viewgroup, false);
			return new LoadMoreRecyclerViewHolder(convertView);
		}
		else
		{
			View convertView = inflater.inflate(R.layout.recycler_mail_list_item, viewgroup, false);
			return new MailRecyclerViewHolder(convertView);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position)
	{
//		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "position", position);
		super.onBindViewHolder(viewHolder, position);
		SwipeMenuLayoutHolder holder = (SwipeMenuLayoutHolder) viewHolder;
		if (holder != null && holder.viewHolder != null)
		{
			if (holder.viewHolder instanceof LoadMoreRecyclerViewHolder)
			{
				LoadMoreRecyclerViewHolder loadHolder = (LoadMoreRecyclerViewHolder) holder.viewHolder;
				if (loadHolder != null)
				{
					if (hasMoreData() && position > 0 && position == getItemCount()-1)
						loadHolder.loading_layout.setVisibility(View.VISIBLE);
					else
						loadHolder.loading_layout.setVisibility(View.GONE);
				}
			}
			else if (holder.viewHolder instanceof MailRecyclerViewHolder)
			{
				final MailRecyclerViewHolder mailHolder = (MailRecyclerViewHolder) holder.viewHolder;
				if (mailHolder != null)
				{
					final ChatChannel channel = (ChatChannel) getItem(position);

					if (channel != null)
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "content", channel.contentText, "channelId", channel.channelID, "tableName",
								channel.getChatTable().getTableName());
						int bgColor = MailManager.getColorByChannelId(channelId);
						mailHolder.setContent(channel, channel.nameText, channel.contentText, channel.timeText,
								context.isInEditMode(), bgColor);

						setIcon(channel, mailHolder.mail_icon);

						if (mailHolder.mail_checkbox != null)
							mailHolder.mail_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{

								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
								{
									channel.checked = isChecked;
								}
							});

						if (mailHolder.mail_content_layout != null)
							mailHolder.mail_content_layout.setOnClickListener(new OnClickListener()
							{

								@Override
								public void onClick(View v)
								{
									if (context.isInEditMode())
									{
										channel.checked = !channel.checked;
										if (mailHolder.mail_checkbox != null)
											mailHolder.mail_checkbox.setChecked(channel.checked);
									}
									else
										context.openItem(channel);
								}
							});
					}
				}
			}

//			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "position END", position);
		}
	}
	
	public void setIcon(final ChatChannel channel, final ImageView imageView)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			if (channel.memberUidArray == null || channel.memberUidArray.size() == 0)
			{
				imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
				return;
			}
			else if(channel.chatRoomBitmap!=null)
			{
				imageView.setImageBitmap(channel.chatRoomBitmap);
			}
			else
			{
				String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channel.channelID);
				if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
				{
					Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
					imageView.setImageBitmap(bitmap);
				}
				else if (isChatroomHeadPicExist(channel.channelID))
				{
					imageView.setTag(channel.channelID);
					AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							try
							{
								if (imageView.getTag() != null && imageView.getTag() instanceof String)
								{
									String groupId = (String) imageView.getTag();
									if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
										return;
									ImageUtil.setImageOnUiThread(context, imageView, bitmap);
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

						}
					});
				}
				else
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
			}
		}
		else
		{
			UserInfo user = null;
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				user = channel.channelShowUserInfo;
			}
			else if (channel.showItem != null)
			{
				user = channel.showItem.getUser();
			}
			ImageUtil.setHeadImage(context, channel.channelIcon, imageView, user);
		}
	}

	public String getChatroomHeadPicPath()
	{
		if (context == null)
			return null;

		return DBHelper.getHeadDirectoryPath(context) + "chatroom/";
	}

	public String getChatroomHeadPicFile(String channelId)
	{
		return channelId;
	}

	public String getOldChatroomHeadPicFile(String channelId)
	{
		return channelId + ".png";
	}

	public boolean isChatroomHeadPicExist(String channelId)
	{
		try
		{
			String fileName = getChatroomHeadPicPath() + getOldChatroomHeadPicFile(channelId);
			File oldfile = new File(fileName);
			if (oldfile.exists())
			{
				oldfile.delete();
			}

		}
		catch (Exception e)
		{
		}

		String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channelId);
		File file = new File(fileName);
		if (file.exists())
		{
			return true;
		}
		return false;
	}
}
