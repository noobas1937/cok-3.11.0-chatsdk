package com.elex.im.ui.adaptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.im.CokChannelDef;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.FileUtil;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.image.AsyncImageLoader;
import com.elex.im.core.util.image.ImageLoaderListener;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.util.BitmapUtil;
import com.elex.im.ui.util.CombineBitmapManager;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.util.RoundImageView;
import com.elex.im.ui.view.ChatFragment;
import com.elex.im.ui.view.NewGridView;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;
import com.elex.im.ui.viewholder.ViewHolderHelper;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter
{

	private MyActionBarActivity				activity;
	private SparseArray<List<Channel>>	userMap;
	private LayoutInflater					inflater;

	public CustomExpandableListAdapter(MyActionBarActivity activity, SparseArray<List<Channel>> userMap)
	{
		this.activity = activity;
		this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.userMap = userMap;
	}

	@Override
	public int getGroupCount()
	{
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
				List<Channel> listChannel = userMap.valueAt(i);
				if(listChannel!=null)
					Collections.sort(listChannel);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		if (groupPosition == 0)
			return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_FRIEND);
		else if (groupPosition == 1)
			return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_CHATROOM);
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		if (userMap != null && groupPosition >= 0 && groupPosition < userMap.size())
			return userMap.get(groupPosition);
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
		if(getChild(groupPosition, childPosition) != null)
		{
			List<Channel> channelList = (List<Channel>) getChild(groupPosition, childPosition);
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
			
			final List<Channel> channelList = (List<Channel>) getChild(groupPosition, childPosition);
			final MemberGridAdapter adapter = new MemberGridAdapter(activity, inflater, channelList);
			member_grid_view.setAdapter(adapter);
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
				null_child_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NULL_FRIEND));
			else if(groupPosition == 1)
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
		private List<Channel>					mDataList;
		private ConcurrentHashMap<String, Bitmap>	chatroomHeadImages;
		private int									customPicLoadingCnt;
		private boolean								chatroomHeadImagesLoading	= false;

		public MemberGridAdapter(MyActionBarActivity activity, LayoutInflater inflater, List<Channel> mDataList)
		{
			this.activity = activity;
			this.mDataList = mDataList;
			this.inflater = inflater;
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

			if (getItem(position) == null || !(getItem(position) instanceof Channel))
				return null;
			final Channel channel = (Channel) getItem(position);
			
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
			headImage.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (UIManager.getChatFragment() != null)
						UIManager.getChatFragment().refreshCustomChannelImage(channel);
					if(member_single_del_btn!=null)
					{
						if (channel != null && ChatFragment.showingCustomChannel != null
								&& channel.getChannelType() == ChatFragment.showingCustomChannel.getChannelType()
								&& channel.getChannelID().equals(ChatFragment.showingCustomChannel.getChannelID()))
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
					if(UIManager.getChatFragment()!=null)
						UIManager.getChatFragment().notifyCustomChannelDataSetChanged();
					
					
					if (UIManager.getChatFragment() != null)
						UIManager.getChatFragment().refreshCustomChannelImage(channel);
				}
			});
			if(member_single_del_btn!=null)
			{
				if (ChatFragment.showingCustomChannel != null
						&& channel.getChannelType() == ChatFragment.showingCustomChannel.getChannelType()
						&& channel.getChannelID().equals(ChatFragment.showingCustomChannel.getChannelID()))
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
			if (CokChannelDef.isInUserMail(channel.getChannelType()))
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
				if (StringUtils.isNotEmpty(fromUid)  && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					User userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null)
					{
						nameText = userInfo.userName;
						if(StringUtils.isNotEmpty(userInfo.asn))
							nameText = "("+userInfo.asn+")"+nameText;
					}
					else{
						nameText = channel.getCustomName();
					}
					if(StringUtils.isNotEmpty(nameText))
						nameText += CokChannelDef.getChannelNamePostfix(channel.getChannelID());
					name.setText(nameText);
				}
				else
				{
					if(StringUtils.isNotEmpty(channel.getCustomName()))
						name.setText(channel.getCustomName());
					else
						name.setText(channel.getChannelID());
				}
			}
			else if (CokChannelDef.isInChatRoom(channel.getChannelType()))
			{
				if(StringUtils.isNotEmpty(channel.getCustomName()))
					name.setText(channel.getCustomName());
				else
					name.setText(channel.getChannelID());
			}
			return convertView;
		}

		private void setChatRoomIcon(final Channel channel, final ImageView imageView)
		{
			if (channel.memberUidArray == null || channel.memberUidArray.size() == 0)
			{
				imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.mail_pic_flag_31));
				return;
			}

			String fileName = FileUtil.getChatroomHeadPicPath(activity) + getChatroomHeadPicFile(channel.getChannelID());
			if (!channel.isMemberUidChanged)
			{
				if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
				{
					Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
					imageView.setImageBitmap(bitmap);
					return;
				}
				else if (isChatroomHeadPicExist(channel.getChannelID()))
				{
					imageView.setTag(channel.getChannelID());
					AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							String groupId = (String) imageView.getTag();
							if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.getChannelID())) || bitmap == null)
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

			ArrayList<User> users = new ArrayList<User>();
			for (int i = 0; i < channel.memberUidArray.size(); i++)
			{
				User user = UserManager.getInstance().getUser(channel.memberUidArray.get(i));
				if (user != null)
				{
					users.add(user);
				}
				if (users.size() >= 9)
					break;
			}

			for (int i = 0; i < users.size(); i++)
			{
				final User user = users.get(i);
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

		private void generateCombinePic(Channel channel, ImageView imageView)
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
				if (bitmap != null && StringUtils.isNotEmpty(channel.getChannelID()) && FileUtil.getChatroomHeadPicPath(activity) != null)
				{
					BitmapUtil.saveMyBitmap(bitmap, FileUtil.getChatroomHeadPicPath(activity), getChatroomHeadPicFile(channel.getChannelID()));

					if (channel.isMemberUidChanged)
					{
						channel.isMemberUidChanged = false;
						String fileName = FileUtil.getChatroomHeadPicPath(activity) + getChatroomHeadPicFile(channel.getChannelID());
						AsyncImageLoader.removeMemoryCache(fileName);
					}
				}
			}
			catch (IOException e)
			{
				LogUtil.printException(e);
			}

			String groupId = (String) imageView.getTag();
			if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.getChannelID())) || bitmap == null)
				return;
			ImageUtil.setImageOnUiThread(activity, imageView, bitmap);
		}

		private synchronized void onCustomImageLoaded(Channel channel, String uid, final Bitmap bitmap, ImageView imageView)
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
				String fileName = FileUtil.getChatroomHeadPicPath(activity) + getOldChatroomHeadPicFile(channelId);
				File oldfile = new File(fileName);
				if (oldfile.exists())
				{
					oldfile.delete();
				}

			}
			catch (Exception e)
			{
			}

			String fileName = FileUtil.getChatroomHeadPicPath(activity) + getChatroomHeadPicFile(channelId);
			File file = new File(fileName);
			if (file.exists())
			{
				return true;
			}
			return false;
		}

	}

}
