package com.elex.chatservice.view.kurento;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.kurento.PeerUserInfo;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.NewGridView;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VoiceSettingExpandableListAdapter extends BaseExpandableListAdapter
{

	private MyActionBarActivity	activity;
	private LayoutInflater		inflater;
	private List<UserInfo>		mSpeakerList	= null;
	private List<UserInfo>		mListenerList	= null;

	public VoiceSettingExpandableListAdapter(MyActionBarActivity activity)
	{
		this.activity = activity;
		this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		refreshData();
	}

	public void refreshData()
	{
		if (mSpeakerList == null)
			mSpeakerList = new ArrayList<UserInfo>();
		else
			mSpeakerList.clear();
		
		if (mListenerList == null)
			mListenerList = new ArrayList<UserInfo>();
		else
			mListenerList.clear();
		
		List<PeerUserInfo> peerList = WebRtcPeerManager.getInstance().getPeerList();
		List<String> speakerList = WebRtcPeerManager.getInstance().getSpeakerList();
		if (peerList != null)
		{
			for (PeerUserInfo peer : peerList)
			{
				if (StringUtils.isNotEmpty(peer.getName()))
				{
					UserManager.checkUser(peer.getName(), "", 0);
					UserInfo user = UserManager.getInstance().getUser(peer.getName());
					if (user != null)
					{
						if(speakerList!=null && speakerList.contains(peer.getName()))
							mSpeakerList.add(user);
						else
							mListenerList.add(user);
					}
				}
			}
		}
		
	}

	@Override
	public int getGroupCount()
	{
		return 3;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return 1;
	}

	public void notifyDataSetWithSort()
	{
		notifyDataSetChanged();
	}
	
	public List<String> getUnConfirmedSpeaker()
	{
		List<String> speakerList = new ArrayList<String>();
		if(mSpeakerList!=null)
		{
			for(UserInfo user : mSpeakerList)
			{
				if(user!=null)
					speakerList.add(user.uid);
			}
		}
		return speakerList;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		if (groupPosition == 0)
			return LanguageManager.getLangByKey(LanguageKeys.TITLE_SPEAKER);
		else if (groupPosition == 1)
			return LanguageManager.getLangByKey(LanguageKeys.TITLE_LISTENER);
		else if (groupPosition == 2)
			return LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_SETTING);
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		if (groupPosition == 0)
				return mSpeakerList;
		else if (groupPosition == 1)
			return mListenerList;
		else
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
			convertView = inflater.inflate(R.layout.custom_channel_list_group, null);
			TextView listHeader = ViewHolderHelper.get(convertView, R.id.listHeader);
			if (listHeader != null)
				ScaleUtil.adjustTextSize(listHeader, ConfigManager.scaleRatio);
			RelativeLayout group_layout = ViewHolderHelper.get(convertView, R.id.group_layout);
			if (group_layout != null)
			{
				int length = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) group_layout.getLayoutParams();
				if (layoutParams != null)
				{
					layoutParams.height = length;
					group_layout.setLayoutParams(layoutParams);
				}
			}
		}

		TextView listHeader = ViewHolderHelper.get(convertView, R.id.listHeader);
		if (listHeader != null)
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
		if (groupPosition == 0 || groupPosition == 1)
			return 0;
		else
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
		if (childType == 0)
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

			final List<UserInfo> userList = (List<UserInfo>) getChild(groupPosition, childPosition);
			int role = groupPosition == 0 ? 2 : 1;
			final MemberGridAdapter adapter = new MemberGridAdapter(activity, inflater, userList,role);
			member_grid_view.setAdapter(adapter);
			member_grid_view.setSelector(new ColorDrawable(0x00000000));
		}
		else
		{
			CheckBox press_to_speak_checkbox = null;
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.item_realtime_voice_my_setting, null);
				press_to_speak_checkbox = (CheckBox) convertView.findViewById(R.id.press_to_speak_checkbox);
				convertView.setTag(press_to_speak_checkbox);
			}
			else
			{
				press_to_speak_checkbox = (CheckBox) convertView.getTag();
			}

			if (press_to_speak_checkbox != null)
			{
				press_to_speak_checkbox.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_PRESS_TO_SPEAK));
				press_to_speak_checkbox.setChecked(ChatServiceController.isPressToSpeakVoiceMode);
				press_to_speak_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						ChatServiceController.isPressToSpeakVoiceMode = isChecked;
					}
				});
			}
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
		private MyActionBarActivity	activity;
		private LayoutInflater		inflater;
		private List<UserInfo>		userList;
		private int role;

		public MemberGridAdapter(MyActionBarActivity activity, LayoutInflater inflater, List<UserInfo> userList,int role)
		{
			this.activity = activity;
			this.userList = userList;
			this.inflater = inflater;
			this.role = role;
		}

		@Override
		public int getCount()
		{
			if (userList != null)
				return userList.size();
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			if (userList != null && position >= 0 && position < userList.size())
				return userList.get(position);
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
			}

			if (getItem(position) == null || !(getItem(position) instanceof UserInfo))
				return convertView;
			final UserInfo userInfo = (UserInfo) getItem(position);
			
			RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
			ImageUtil.setHeadImage(activity, userInfo.headPic, headImage, userInfo);
			
			if(UserManager.getInstance().getCurrentUser()!=null && UserManager.getInstance().getCurrentUser().allianceRank>=4
					&& !userInfo.uid.equals(UserManager.getInstance().getCurrentUserId()))
			{
				headImage.setOnClickListener(new OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						if(role == 1)
						{
							if(mListenerList.contains(userInfo))
								mListenerList.remove(userInfo);
							if(!mSpeakerList.contains(userInfo))
								mSpeakerList.add(userInfo);
						}
						else if(role == 2)
						{
							if(!mListenerList.contains(userInfo))
								mListenerList.add(userInfo);
							if(mSpeakerList.contains(userInfo))
								mSpeakerList.remove(userInfo);
						}
						notifyDataSetWithSort();
					}
				});
			}

			TextView name = ViewHolderHelper.get(convertView, R.id.name);
			name.setText(userInfo.userName);

			return convertView;
		}
	}

}
