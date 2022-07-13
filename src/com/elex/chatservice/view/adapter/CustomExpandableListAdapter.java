package com.elex.chatservice.view.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.host.GameHost;
import com.elex.chatservice.image.AsyncImageLoader;
import com.elex.chatservice.image.ImageLoaderListener;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.BitmapUtil;
import com.elex.chatservice.util.CombineBitmapManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.util.SharePreferenceUtil;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.NewGridView;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter
{

	private MyActionBarActivity				activity;
	private SparseArray<List<ChatChannel>>	userMap;
	private LayoutInflater					inflater;

	public CustomExpandableListAdapter(MyActionBarActivity activity, SparseArray<List<ChatChannel>> userMap)
	{
		this.activity = activity;
		this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.userMap = userMap;
	}

	@Override
	public int getGroupCount()
	{
		if(ChatServiceController.randomChatEnable)
			return userMap.size()+1;
		else
			return userMap.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return 1;
	}
	
	public void notifyDataSetWithSort()
	{
		if(userMap != null)
		{
			for(int i = 0 ;i<userMap.size() ;i++)
			{
				List<ChatChannel> listChannel = userMap.valueAt(i);
				if(listChannel!=null)
					Collections.sort(listChannel);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		if(ChatServiceController.randomChatEnable)
		{
			if (groupPosition == 0)
				return LanguageManager.getLangByKey(LanguageKeys.TITLE_RANDOM_CAHTROOM);
			else if (groupPosition == 1)
				return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_FRIEND);
			else if (groupPosition == 2)
				return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_CHATROOM);
		}
		else
		{
			if (groupPosition == 0)
				return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_FRIEND);
			else if (groupPosition == 1)
				return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_CHATROOM);
		}
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		if (userMap != null && groupPosition >= 0 && groupPosition < getGroupCount())
		{
			if(ChatServiceController.randomChatEnable)
			{
				if(groupPosition == 0)
					return null;
				else if(groupPosition > 0)
					return userMap.get(groupPosition-1);
			}
			else
				return userMap.get(groupPosition);
		}
		return null;
	}
	
	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		if (getGroup(groupPosition) == null)
			return null;
		if (convertView == null)
		{
			if(ConfigManager.getInstance().needRTL())
				convertView = inflater.inflate(R.layout.custom_channel_list_group_ar, null);
			else
				convertView = inflater.inflate(R.layout.custom_channel_list_group, null);
			TextView listHeader = ViewHolderHelper.get(convertView, R.id.listHeader);
			if(listHeader!=null)
				ScaleUtil.adjustTextSize(listHeader, ConfigManager.scaleRatio);
			RelativeLayout group_layout = ViewHolderHelper.get(convertView, R.id.group_layout);
			if(group_layout!=null)
			{
				int length = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)group_layout.getLayoutParams();
				if(layoutParams!=null)
				{
					layoutParams.height = length;
					group_layout.setLayoutParams(layoutParams);
				}
			}
		}
		
		TextView listHeader = ViewHolderHelper.get(convertView, R.id.listHeader);
		if(listHeader!=null)
			listHeader.setText(getGroup(groupPosition).toString());

		ImageView group_arrow = ViewHolderHelper.get(convertView, R.id.group_arrow);
		if (group_arrow != null)
		{
			if (isExpanded)
				group_arrow.setImageDrawable(activity.getResources().getDrawable(R.drawable.arrow_down));
			else
				group_arrow.setImageDrawable(activity.getResources().getDrawable(R.drawable.group_btn_right));
		}
		
		return convertView;
	}
	
	@Override
	public int getChildType(int groupPosition, int childPosition)
	{
		if(groupPosition == 0)
			return 0;
		else if(getChild(groupPosition, childPosition) != null)
		{
			List<ChatChannel> channelList = (List<ChatChannel>) getChild(groupPosition, childPosition);
			if(channelList!=null && channelList.size()>0)
				return 0;
		}
		return 1;
	}
	
	@Override
	public int getChildTypeCount()
	{
		return 2;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		int childType = getChildType(groupPosition, childPosition);
		if(childType == 0)
		{
			NewGridView member_grid_view = null;
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.custom_channel_list_item, null);
				member_grid_view = (NewGridView) convertView.findViewById(R.id.member_grid_view);
				convertView.setTag(member_grid_view);
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
				{
					int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
					member_grid_view.setColumnWidth(length);
				}
			}
			else
			{
				member_grid_view = (NewGridView) convertView.getTag();
			}
			
			if(ChatServiceController.randomChatEnable && groupPosition == 0)
			{
				RandomMemberGridAdapter adapter = new RandomMemberGridAdapter(activity, inflater);
				member_grid_view.setAdapter(adapter);
			}
			else
			{
				final List<ChatChannel> channelList = (List<ChatChannel>) getChild(groupPosition, childPosition);
				int channelType = DBDefinition.CHANNEL_TYPE_USER;
				if (groupPosition == 2)
					channelType = DBDefinition.CHANNEL_TYPE_CHATROOM;
				final MemberGridAdapter adapter = new MemberGridAdapter(activity, inflater, channelList, channelType);
				member_grid_view.setAdapter(adapter);
			}
			member_grid_view.setSelector(new ColorDrawable(0x00000000));
		}
		else
		{
			TextView null_child_text = null;
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.custom_channel_list_tip_item, null);
				null_child_text = (TextView) convertView.findViewById(R.id.null_child_text);
				convertView.setTag(null_child_text);
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
				{
					ScaleUtil.adjustTextSize(null_child_text, ConfigManager.scaleRatio);
				}
			}
			else
			{
				null_child_text = (TextView) convertView.getTag();
			}
			if(groupPosition == 0)
				null_child_text.setVisibility(View.GONE);
			else if(groupPosition == 1)
				null_child_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NULL_FRIEND));
			else if(groupPosition == 2)
				null_child_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NULL_CHATROOM));
		}
		

		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
	}

	class MemberGridAdapter extends BaseAdapter
	{
		private MyActionBarActivity					activity;
		private LayoutInflater						inflater;
		private List<ChatChannel>					mDataList;
		private int									channelType;
		private ConcurrentHashMap<String, Bitmap>	chatroomHeadImages;
		private int									customPicLoadingCnt;
		private boolean								chatroomHeadImagesLoading	= false;

		public MemberGridAdapter(MyActionBarActivity activity, LayoutInflater inflater, List<ChatChannel> mDataList, int channelType)
		{
			this.activity = activity;
			this.mDataList = mDataList;
			this.inflater = inflater;
			this.channelType = channelType;
		}

		@Override
		public int getCount()
		{
			if (mDataList != null)
				return mDataList.size();
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			if (mDataList != null && position >= 0 && position < mDataList.size())
				return mDataList.get(position);
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		private void adjustTextSize(View convertView)
		{
			TextView name = ViewHolderHelper.get(convertView, R.id.name);
			if (name != null)
				ScaleUtil.adjustTextSize(name, ConfigManager.scaleRatio);
		}

		private void adjustSize(View convertView)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				adjustTextSize(convertView);
				int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				FrameLayout member_head_layout = ViewHolderHelper.get(convertView, R.id.member_head_layout);
				if (member_head_layout != null)
				{
					LinearLayout.LayoutParams headImageLayoutParams = (LinearLayout.LayoutParams) member_head_layout.getLayoutParams();
					headImageLayoutParams.width = length;
					headImageLayoutParams.height = length;
					member_head_layout.setLayoutParams(headImageLayoutParams);
				}

				int width = (int) (ScaleUtil.dip2px(activity, 20) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn);
				if (member_single_del_btn != null)
				{
					FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) member_single_del_btn.getLayoutParams();
					headImageLayoutParams.width = width;
					headImageLayoutParams.height = width;
					member_single_del_btn.setLayoutParams(headImageLayoutParams);
				}

			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.item_chat_room_member, null);
				adjustSize(convertView);
				ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn); 
				if(member_single_del_btn!=null)
					member_single_del_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.mail_list_edit_check_box_checked));
			}
			
			if (getItem(position) == null || !(getItem(position) instanceof ChatChannel))
				return convertView;
			final ChatChannel channel = (ChatChannel) getItem(position);

			RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
			final ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn); 
			headImage.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (ChatServiceController.getChatFragment() != null)
						ChatServiceController.getChatFragment().refreshCustomChannelImage(channel);
					if(member_single_del_btn!=null)
					{
						if(channel!=null && ChatFragmentNew.showingCustomChannel!=null && channel.channelType == ChatFragmentNew.showingCustomChannel.channelType && channel.channelID.equals(ChatFragmentNew.showingCustomChannel.channelID))
						{
							if(member_single_del_btn.getVisibility()!=View.VISIBLE)
								member_single_del_btn.setVisibility(View.VISIBLE);
						}
						else
						{
							if(member_single_del_btn.getVisibility()!=View.GONE)
								member_single_del_btn.setVisibility(View.GONE);
						}
					}
					if(ChatServiceController.getChatFragment()!=null)
						ChatServiceController.getChatFragment().notifyCustomChannelDataSetChanged();
				}
			});
			if(member_single_del_btn!=null)
			{
				if(ChatFragmentNew.showingCustomChannel!=null && channel.channelType == ChatFragmentNew.showingCustomChannel.channelType && channel.channelID.equals(ChatFragmentNew.showingCustomChannel.channelID))
				{
					if(member_single_del_btn.getVisibility()!=View.VISIBLE)
						member_single_del_btn.setVisibility(View.VISIBLE);
				}
				else
				{
					if(member_single_del_btn.getVisibility()!=View.GONE)
						member_single_del_btn.setVisibility(View.GONE);
				}
			}

			TextView name = ViewHolderHelper.get(convertView, R.id.name);

			ImageUtil.setChannelImage(activity, channel, headImage);
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
				if (StringUtils.isNotEmpty(fromUid)  && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					UserInfo userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null && StringUtils.isNotEmpty(userInfo.userName))
					{
						nameText = userInfo.userName;
						if(StringUtils.isNotEmpty(userInfo.asn))
							nameText = "("+userInfo.asn+")"+nameText;
					}
					else
					{
						if(StringUtils.isNotEmpty(channel.customName))
							nameText = channel.customName;
						else
							nameText = fromUid;
					}
					if(StringUtils.isNotEmpty(nameText) && channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
						nameText+="(MOD)";
					name.setText(nameText);
				}
				else
				{
					if(StringUtils.isNotEmpty(channel.customName))
						name.setText(channel.customName);
					else
						name.setText(fromUid);
				}
			}
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			{
				if(StringUtils.isNotEmpty(channel.customName))
					name.setText(channel.customName);
				else
					name.setText(channel.channelID);
			}
			return convertView;
		}

		private void setChatRoomIcon(final ChatChannel channel, final ImageView imageView)
		{
			if (channel.memberUidArray == null || channel.memberUidArray.size() == 0)
			{
				imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.mail_pic_flag_31));
				return;
			}

			String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channel.channelID);
			if (!channel.isMemberUidChanged)
			{
				if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
				{
					Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
					imageView.setImageBitmap(bitmap);
					return;
				}
				else if (isChatroomHeadPicExist(channel.channelID))
				{
					imageView.setTag(channel.channelID);
					AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							String groupId = (String) imageView.getTag();
							if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
								return;
							ImageUtil.setImageOnUiThread(activity, imageView, bitmap);
						}
					});
					return;
				}
			}

			chatroomHeadImages = new ConcurrentHashMap<String, Bitmap>();
			customPicLoadingCnt = 0;
			chatroomHeadImagesLoading = true;

			ArrayList<UserInfo> users = new ArrayList<UserInfo>();
			for (int i = 0; i < channel.memberUidArray.size(); i++)
			{
				UserInfo user = UserManager.getInstance().getUser(channel.memberUidArray.get(i));
				if (user != null)
				{
					users.add(user);
				}
				if (users.size() >= 9)
					break;
			}

			for (int i = 0; i < users.size(); i++)
			{
				final UserInfo user = users.get(i);
				Bitmap predefinedHeadImage = BitmapFactory.decodeResource(activity.getResources(),
						ImageUtil.getHeadResId(activity, user.headPic));
				if (predefinedHeadImage != null)
					chatroomHeadImages.put(user.uid, predefinedHeadImage);

				if (user.isCustomHeadImage())
				{
					customPicLoadingCnt++;
					ImageUtil.getDynamicPic(user.getCustomHeadPicUrl(), user.getCustomHeadPic(), new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(final Bitmap bitmap)
						{
							onCustomImageLoaded(channel, user.uid, bitmap, imageView);
						}
					});
				}
			}
			if (customPicLoadingCnt == 0)
			{
				generateCombinePic(channel, imageView);
			}
		}

		private void generateCombinePic(ChatChannel channel, ImageView imageView)
		{
			chatroomHeadImagesLoading = false;

			ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
			Set<String> keySet = chatroomHeadImages.keySet();
			for (String key : keySet)
			{
				if (StringUtils.isNotEmpty(key) && chatroomHeadImages.get(key) != null)
				{
					bitmaps.add(chatroomHeadImages.get(key));
				}
			}

			Bitmap bitmap = CombineBitmapManager.getInstance().getCombinedBitmap(bitmaps);
			try
			{
				if (bitmap != null && StringUtils.isNotEmpty(channel.channelID) && getChatroomHeadPicPath() != null)
				{
					BitmapUtil.saveMyBitmap(bitmap, getChatroomHeadPicPath(), getChatroomHeadPicFile(channel.channelID));

					if (channel.isMemberUidChanged)
					{
						channel.isMemberUidChanged = false;
						String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channel.channelID);
						AsyncImageLoader.removeMemoryCache(fileName);
					}
				}
			}
			catch (IOException e)
			{
				LogUtil.printException(e);
			}

			String groupId = (String) imageView.getTag();
			if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
				return;
			ImageUtil.setImageOnUiThread(activity, imageView, bitmap);
		}

		private synchronized void onCustomImageLoaded(ChatChannel channel, String uid, final Bitmap bitmap, ImageView imageView)
		{
			if (bitmap != null)
			{
				chatroomHeadImages.put(uid, bitmap);
			}
			customPicLoadingCnt--;
			if (customPicLoadingCnt == 0)
			{
				generateCombinePic(channel, imageView);
			}
		}

		public String getChatroomHeadPicPath()
		{
			if (activity == null)
				return null;

			return DBHelper.getHeadDirectoryPath(activity) + "chatroom/";
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
	
	class RandomMemberGridAdapter extends BaseAdapter
	{
		private MyActionBarActivity					activity;
		private LayoutInflater						inflater;

		public RandomMemberGridAdapter(MyActionBarActivity activity, LayoutInflater inflater)
		{
			this.activity = activity;
			this.inflater = inflater;
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		private void adjustTextSize(View convertView)
		{
			TextView name = ViewHolderHelper.get(convertView, R.id.name);
			if (name != null)
				ScaleUtil.adjustTextSize(name, ConfigManager.scaleRatio);
		}

		private void adjustSize(View convertView)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				adjustTextSize(convertView);
				int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				FrameLayout member_head_layout = ViewHolderHelper.get(convertView, R.id.member_head_layout);
				if (member_head_layout != null)
				{
					LinearLayout.LayoutParams headImageLayoutParams = (LinearLayout.LayoutParams) member_head_layout.getLayoutParams();
					headImageLayoutParams.width = length;
					headImageLayoutParams.height = length;
					member_head_layout.setLayoutParams(headImageLayoutParams);
				}

				int width = (int) (ScaleUtil.dip2px(activity, 20) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn);
				if (member_single_del_btn != null)
				{
					FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) member_single_del_btn.getLayoutParams();
					headImageLayoutParams.width = width;
					headImageLayoutParams.height = width;
					member_single_del_btn.setLayoutParams(headImageLayoutParams);
				}

			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.item_chat_room_member, null);
				adjustSize(convertView);
				ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn); 
				if(member_single_del_btn!=null)
					member_single_del_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.mail_list_edit_check_box_checked));
			}

			RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
			final ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn); 
			final ChatChannel channel = new ChatChannel();
			channel.channelType = DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
			final int pos = position+1;
			
			TextView name = ViewHolderHelper.get(convertView, R.id.name);

			if(position == 0)
			{
				String lang = ConfigManager.getInstance().gameLang;
				if(StringUtils.isEmpty(lang))
					lang = "en";
				String langImage = LanguageManager.getLangImage(lang);
				int resId = ImageUtil.getHeadResId(activity, langImage);
				Drawable drawable = null;
				if(resId>0)
					drawable = activity.getResources().getDrawable(resId);
				if(drawable!=null)
					headImage.setImageDrawable(drawable);
				else
					headImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.mail_pic_flag_31));
				String nameStr = LanguageManager.getLangByKey(LanguageKeys.BTN_LOCAL_CHATROOM,LanguageManager.getOriginalLangByKey(lang));
				name.setText(nameStr);
				channel.randomChatMode = 1;
				channel.customName = nameStr;
			}
			else if(position == 1)
			{
				headImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.random_global));
				String nameStr = LanguageManager.getLangByKey(LanguageKeys.BTN_GLOBAL_CHATROOM);
				name.setText(nameStr);
				channel.randomChatMode = 2;
				channel.customName = nameStr;
			}
			
			headImage.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (ChatServiceController.getChatFragment() != null)
						ChatServiceController.getChatFragment().refreshCustomChannelImage(channel);
					if(member_single_del_btn!=null)
					{
						if(ChatFragmentNew.showingCustomChannel!=null && ChatFragmentNew.showingCustomChannel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT && ChatFragmentNew.showingCustomChannel.randomChatMode == pos)
						{
							if(member_single_del_btn.getVisibility()!=View.VISIBLE)
								member_single_del_btn.setVisibility(View.VISIBLE);
						}
						else
						{
							if(member_single_del_btn.getVisibility()!=View.GONE)
								member_single_del_btn.setVisibility(View.GONE);
						}
					}
					if(ChatServiceController.getChatFragment()!=null)
						ChatServiceController.getChatFragment().notifyCustomChannelDataSetChanged();
				}
			});
			if(member_single_del_btn!=null)
			{
				if(ChatFragmentNew.showingCustomChannel!=null && ChatFragmentNew.showingCustomChannel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT && ChatFragmentNew.showingCustomChannel.randomChatMode == pos)
				{
					if(member_single_del_btn.getVisibility()!=View.VISIBLE)
						member_single_del_btn.setVisibility(View.VISIBLE);
				}
				else
				{
					if(member_single_del_btn.getVisibility()!=View.GONE)
						member_single_del_btn.setVisibility(View.GONE);
				}
			}

			return convertView;
		}
	}

}
