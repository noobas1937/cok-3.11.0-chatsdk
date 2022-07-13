package com.elex.chatservice.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.ChatTable;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.nineoldandroids.view.ViewHelper;

public class ChatRoomSettingActivity extends MyActionBarActivity
{
	private NewGridView			member_grid_view;
	private MemberGridAdapter	mAdapter;
	private TextView			name_text;
	private TextView			chat_room_name;
	private TextView			tip_text;
	private TextView			level_text;
	private RelativeLayout		change_name_layout;
	private RelativeLayout		leave_layout;
	private ImageView			name_btn_arrow;
	private ImageView			leave_btn_arrow;
	private boolean				adjustSizeCompleted	= false;
	private boolean				isCreate			= false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		if(ConfigManager.getInstance().needRTL())
			inflater.inflate(R.layout.chat_room_set_fragment_ar, fragmentLayout, true);
		else
			inflater.inflate(R.layout.chat_room_set_fragment, fragmentLayout, true);
		String title = UserManager.getInstance().getCurrentMail().opponentName;
		if(StringUtils.isNotEmpty(title) && title.length()>30)
		{
			title = title.substring(0, 30);
			title+= "...";
		}
		
		if(ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER)
			title = ChatServiceController.getInstance().getUserMailTitle();
		
		titleLabel.setText(title);
		showRightBtn(null);
		
		member_grid_view = (NewGridView) findViewById(R.id.member_grid_view);
		mAdapter = new MemberGridAdapter(this);
		member_grid_view.setAdapter(mAdapter);

		name_btn_arrow = (ImageView) findViewById(R.id.name_btn_arrow);
		leave_btn_arrow = (ImageView) findViewById(R.id.leave_btn_arrow);
		if(ConfigManager.getInstance().needRTL())
		{
			if(name_btn_arrow!=null)
				ViewHelper.setRotation(name_btn_arrow, 180.0f);
			if(leave_btn_arrow!=null)
				ViewHelper.setRotation(leave_btn_arrow, 180.0f);
		}
		
		name_text = (TextView) findViewById(R.id.name_text);
		name_text.setText(LanguageManager.getLangByKey(LanguageKeys.TEXT_NAME));
		chat_room_name = (TextView) findViewById(R.id.chat_room_name);
		chat_room_name.setText(title);
		change_name_layout = (RelativeLayout) findViewById(R.id.change_name_layout);
		change_name_layout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ServiceInterface.showChatRoomNameModifyActivity(ChatRoomSettingActivity.this);
			}
		});

		leave_layout = (RelativeLayout) findViewById(R.id.leave_layout);
		leave_layout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String content = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_QUIT);
				MenuController.quitChatRoomConfirm(ChatRoomSettingActivity.this, content);
			}
		});

		level_text = (TextView) findViewById(R.id.level_text);
		level_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_LEVEL_CHAT_ROOM));

		tip_text = (TextView) findViewById(R.id.tip_text);
		tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LEVEL_CHAT_ROOM));

		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		isCreate = true;
	}
	
	public void refreshData()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				notifyDataChanged();
			}
		});
	}
	
	public void refreshChatRoomName()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				String title = UserManager.getInstance().getCurrentMail().opponentName;
				if(StringUtils.isNotEmpty(title) && title.length()>30)
				{
					title = title.substring(0, 30);
					title+= "...";
				}
				chat_room_name.setText(title);
			}
		});
	}
	
	public void refreshTitle()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				String title = UserManager.getInstance().getCurrentMail().opponentName;
				if(StringUtils.isNotEmpty(title) && title.length()>30)
				{
					title = title.substring(0, 30);
					title+= "...";
				}
				titleLabel.setText(title);
			}
		});
	}

	private void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			return;
		}

		if (!adjustSizeCompleted)
		{
			int length = (int) (ScaleUtil.dip2px(this, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
			if (member_grid_view != null)
				member_grid_view.setColumnWidth(length);

			ScaleUtil.adjustTextSize(chat_room_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(name_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(level_text, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		isCreate = false;
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!isCreate)
			notifyDataChanged();
	}

	private void notifyDataChanged()
	{
		if (mAdapter != null)
		{
			mAdapter.refreshData();
			mAdapter.notifyDataSetChanged();
		}
	}

	public void showMemberSelectActivity()
	{
		ServiceInterface.showMemberSelectorActivity(this, false);
	}

	class MemberGridAdapter extends BaseAdapter
	{
		private ChatRoomSettingActivity	activity;
		private LayoutInflater			inflater;
		private List<UserInfo>			mDataList			= null;
		private static final int		ITEM_TYPE_NORMAL	= 0;
		private static final int		ITEM_TYPE_ADD		= 1;
		private static final int		ITEM_TYPE_DEL		= 2;
		private boolean					isDelState			= false;

		public MemberGridAdapter(ChatRoomSettingActivity activity)
		{
			this.activity = activity;
			this.inflater = (LayoutInflater) (this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
			refreshData();
		}

		public void refreshData()
		{
			isDelState = false;
			if (mDataList == null)
				mDataList = new ArrayList<UserInfo>();
			else
				mDataList.clear();
			ChatChannel channel = ChannelManager.getInstance().getChannel(
					ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
			if (channel != null && channel.memberUidArray != null && channel.memberUidArray.size() > 0)
			{
				for (String uid : channel.memberUidArray)
				{
					if (StringUtils.isNotEmpty(uid))
					{
						UserManager.checkUser(uid, "", 0);
						UserInfo user = UserManager.getInstance().getUser(uid);
						if (user != null)
							mDataList.add(user);
					}
				}
			}
			
			if(mDataList.size() == 0 && UserManager.getInstance().getCurrentUser()!=null)
				mDataList.add(UserManager.getInstance().getCurrentUser());

			UserInfo addUser = new UserInfo();
			addUser.btnType = ITEM_TYPE_ADD;
			mDataList.add(addUser);

			String founderUid = ChannelManager.getInstance()
					.getChatRoomFounderByKey(UserManager.getInstance().getCurrentMail().opponentUid);

			if (mDataList.size() > 2 && StringUtils.isNotEmpty(founderUid)
					&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
					&& UserManager.getInstance().getCurrentUserId().equals(founderUid))
			{
				UserInfo delUser = new UserInfo();
				delUser.btnType = ITEM_TYPE_DEL;
				mDataList.add(delUser);
			}
		}

		@Override
		public int getCount()
		{
			if (mDataList != null)
			{
				return mDataList.size();
			}
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
		public int getItemViewType(int position)
		{
			if (position < 0 || position >= mDataList.size())
				return -1;
			UserInfo item = mDataList.get(position);
			if (item != null)
			{
				return item.btnType;
			}
			return -1;
		}

		@Override
		public int getViewTypeCount()
		{
			return 3;
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

		private void adjustSize(View convertView, int type)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				adjustTextSize(convertView);

				int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				if (type == ITEM_TYPE_NORMAL)
				{
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
				else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
				{
					ImageView member_btn = ViewHolderHelper.get(convertView, R.id.member_btn);
					if (member_btn != null)
					{
						LinearLayout.LayoutParams headImageLayoutParams = (LinearLayout.LayoutParams) member_btn.getLayoutParams();
						headImageLayoutParams.width = length;
						headImageLayoutParams.height = length;
						member_btn.setLayoutParams(headImageLayoutParams);
					}
				}

			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			int type = getItemViewType(position);
			if (convertView == null)
			{
				if (type == ITEM_TYPE_NORMAL)
					convertView = inflater.inflate(R.layout.item_chat_room_member, parent, false);
				else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
					convertView = inflater.inflate(R.layout.item_chat_room_member_btn, parent, false);
				adjustSize(convertView, type);
			}
			
			final UserInfo userInfo = (UserInfo) getItem(position);
			if (userInfo == null)
				return convertView;

			if (type == ITEM_TYPE_NORMAL)
			{
				RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
				ImageUtil.setHeadImage(activity, userInfo.headPic, headImage, userInfo);

				ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn);
				if (isDelState && !userInfo.uid.equals(UserManager.getInstance().getCurrentUserId()))
					member_single_del_btn.setVisibility(View.VISIBLE);
				else
					member_single_del_btn.setVisibility(View.GONE);
				member_single_del_btn.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						if(SwitchUtils.customWebsocketEnable){
							WebSocketManager.getInstance().roomGroupKick(UserManager.getInstance().getCurrentMail().opponentUid,userInfo.uid);
						}else {
							JniController.getInstance().excuteJNIVoidMethod("kickChatRoomMember",
									new Object[] { UserManager.getInstance().getCurrentMail().opponentUid, userInfo.userName, userInfo.uid });
						}
					}
				});

				TextView name = ViewHolderHelper.get(convertView, R.id.name);
				name.setText(userInfo.userName);
			}
			else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
			{
				ImageView member_btn = ViewHolderHelper.get(convertView, R.id.member_btn);
				if (member_btn != null)
				{
					if (type == ITEM_TYPE_ADD)
					{
						member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.member_add));
						member_btn.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View v)
							{
								isDelState = false;
								activity.showMemberSelectActivity();
							}
						});
					}
					else if (type == ITEM_TYPE_DEL)
					{
						if(!isDelState)
							member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.member_del));
						else
							member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_comfirm));
						member_btn.setOnClickListener(new OnClickListener()
						{

							@Override
							public void onClick(View v)
							{
								isDelState = !isDelState;
								notifyDataSetChanged();
							}
						});
					}
				}
			}

			return convertView;
		}

	}
}