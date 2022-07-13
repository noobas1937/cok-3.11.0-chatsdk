package com.elex.chatservice.view.kurento;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.kurento.PeerUserInfo;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.NewGridView;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class RealtimeVoiceRoomSettingActivity extends MyActionBarActivity implements OnItemClickListener
{
	private TextView			speaker_title;
	private TextView			listener_title;
	private NewGridView			speaker_gridview;
	private MemberGridAdapter	speaker_adapter;
	private NewGridView			listener_gridview;
	private MemberGridAdapter	listener_adapter;
	private CheckBox			press_to_speak_checkbox;
	private TextView			joinBtn;
	private TextView			settingConfirmBtn;
	private TextView			quitBtn;
	private TextView			no_speaker_tip;
	private TextView			no_listener_tip;
	private TextView			mysetting_title;

	private boolean				adjustSizeCompleted	= false;
	private boolean				isCreate			= false;

	private List<UserInfo>		mSpeakerList		= null;
	private List<UserInfo>		mListenerList		= null;
	private boolean				isMove				= false;

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

		List<String> allList = null;
		if(!WebRtcPeerManager.published)
		{
			allList = WebRtcPeerManager.getInstance().getAllList();
		}
		else
		{
			List<PeerUserInfo> peerList = WebRtcPeerManager.getInstance().getPeerList();
			if(peerList!=null)
			{
				allList = new ArrayList<String>();
				for (PeerUserInfo peer : peerList)
				{
					if (StringUtils.isNotEmpty(peer.getName()))
						allList.add(peer.getName());
				}
			}
		}
		
		List<String> speakerList = WebRtcPeerManager.getInstance().getSpeakerList();
		if (allList != null)
		{
			for (String uid : allList)
			{
				if (StringUtils.isNotEmpty(uid))
				{
					UserManager.checkUser(uid, "", 0);
					UserInfo user = UserManager.getInstance().getUser(uid);
					if (user != null)
					{
						if (speakerList != null && speakerList.contains(uid))
							mSpeakerList.add(user);
						else
							mListenerList.add(user);
					}
				}
			}
		}

	}

	private int getSpeakerCount()
	{
		if (mSpeakerList != null)
			return mSpeakerList.size();
		else
			return 0;
	}

	private void refreshSpeakerTitle()
	{
		speaker_title.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_SPEAKER) + "(" + getSpeakerCount() + "/5)");
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
		refreshData();

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.realtime_voice_room_setting, fragmentLayout, true);
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_VOICE_CHAT_ROOM));
		showRightBtn(null);

		speaker_title = (TextView) findViewById(R.id.speaker_title);
		String num = "(" + getSpeakerCount() + "/5)";
		String text = LanguageManager.getLangByKey(LanguageKeys.TITLE_SPEAKER) + num;
		if (getSpeakerCount() >= 5)
		{
			try
			{
				int start = text.indexOf(num);
				int end = start + num.length();
				if (end > text.length())
					end = text.length();
				SpannableStringBuilder style = new SpannableStringBuilder(text);
				style.setSpan(new ForegroundColorSpan(0xFF38693F), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				speaker_title.setText(style);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
			speaker_title.setText(text);

		listener_title = (TextView) findViewById(R.id.listener_title);
		listener_title.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_LISTENER));

		speaker_gridview = (NewGridView) findViewById(R.id.speaker_gridview);
		speaker_adapter = new MemberGridAdapter(this, inflater, mSpeakerList, 2);
		speaker_gridview.setAdapter(speaker_adapter);
		speaker_gridview.setOnItemClickListener(this);
		listener_gridview = (NewGridView) findViewById(R.id.listener_gridview);
		listener_adapter = new MemberGridAdapter(this, inflater, mListenerList, 1);
		listener_gridview.setAdapter(listener_adapter);
		listener_gridview.setOnItemClickListener(this);

		press_to_speak_checkbox = (CheckBox) findViewById(R.id.press_to_speak_checkbox);
		press_to_speak_checkbox.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_PRESS_TO_SPEAK));
		press_to_speak_checkbox.setChecked(ChatServiceController.isPressToSpeakVoiceMode);

		joinBtn = (TextView) findViewById(R.id.joinBtn);
		joinBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_JOIN_ROOM));
		joinBtn.setVisibility(!WebRtcPeerManager.published ? View.VISIBLE : View.GONE);
		joinBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showProgressBar();
				ServiceInterface.showRealtimeVoice();
			}
		});

		settingConfirmBtn = (TextView) findViewById(R.id.settingConfirmBtn);
		settingConfirmBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SAVE_SETTING));
		settingConfirmBtn.setVisibility(WebRtcPeerManager.published ? View.VISIBLE : View.GONE);
		settingConfirmBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mSpeakerList != null)
				{
					List<String> originalSpeakerList = WebRtcPeerManager.getInstance().getSpeakerList();
					boolean changed = false;
					if (originalSpeakerList.size() != mSpeakerList.size())
						changed = true;
					String uidStr = "";
					for (UserInfo speaker : mSpeakerList)
					{
						if (speaker != null && StringUtils.isNotEmpty(speaker.uid))
						{
							if (!changed && !originalSpeakerList.contains(speaker.uid))
								changed = true;
							if (StringUtils.isNotEmpty(uidStr))
								uidStr += ",";
							uidStr += speaker.uid;
						}
					}
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "uidStr", uidStr);
					if (changed)
					{
						if (!SwitchUtils.mqttEnable)
							WebSocketManager.getInstance().changeRealtimeVoiceRoomRole(uidStr);
						else
							MqttManager.getInstance().changeRealtimeVoiceRoomRole(uidStr);
					}

				}

				if (ChatServiceController.isPressToSpeakVoiceMode != press_to_speak_checkbox.isChecked())
				{
					ChatServiceController.isPressToSpeakVoiceMode = press_to_speak_checkbox.isChecked();
					if (WebRtcPeerManager.getInstance().canSpeak())
					{
						if (ChatServiceController.isPressToSpeakVoiceMode)
							ServiceInterface.enableAudio(false);
						else
							ServiceInterface.enableAudio(true);
					}
				}

				exitActivity();
			}
		});

		quitBtn = (TextView) findViewById(R.id.quitBtn);
		quitBtn.setVisibility(WebRtcPeerManager.published ? View.VISIBLE : View.GONE);
		quitBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_QUIT_REALTIME_VOICE));
		quitBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (RealtimeVoiceRoomSettingActivity.this != null)
					MenuController.showQuitRealtimeVoiceRoomConfirm(RealtimeVoiceRoomSettingActivity.this,
							LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_QUIT));
			}
		});

		no_speaker_tip = (TextView) findViewById(R.id.no_speaker_tip);
		no_speaker_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_SPEAKER));

		no_listener_tip = (TextView) findViewById(R.id.no_listener_tip);
		no_listener_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_LISTENER));
		mysetting_title = (TextView) findViewById(R.id.mysetting_title);
		mysetting_title.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MY_SETTING));

		refreshNoTip();

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

	public void hideProgress()
	{
		if (ChatServiceController.getCurrentActivity() != null)
		{
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					if (ChatServiceController.getCurrentActivity() != null)
						ChatServiceController.getInstance().hideProgressBar();
				}
			});
		}
	}

	public void refreshNoTip()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "WebRtcPeerManager.published", WebRtcPeerManager.published);
		no_speaker_tip.setVisibility(mSpeakerList.size() > 0 ? View.GONE : View.VISIBLE);
		no_listener_tip.setVisibility(mListenerList.size() > 0 ? View.GONE : View.VISIBLE);
	}

	public void refreshNoTipWithMove()
	{
		no_speaker_tip.setVisibility(speaker_adapter.getCount() > 0  ? View.GONE : View.VISIBLE);
		no_listener_tip.setVisibility(listener_adapter.getCount() > 0  ? View.GONE : View.VISIBLE);
	}

	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (!adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		int width = (int) (ScaleUtil.dip2px(100) * ConfigManager.scaleRatioButton * getScreenCorrectionFactor());
		int height = (int) (ScaleUtil.dip2px(30) * ConfigManager.scaleRatioButton * getScreenCorrectionFactor());
		LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) settingConfirmBtn.getLayoutParams();
		param.width = width;
		param.height = height;
		settingConfirmBtn.setLayoutParams(param);

		LinearLayout.LayoutParams param2 = (LinearLayout.LayoutParams) quitBtn.getLayoutParams();
		param2.width = width;
		param2.height = height;
		quitBtn.setLayoutParams(param2);

		LinearLayout.LayoutParams param3 = (LinearLayout.LayoutParams) joinBtn.getLayoutParams();
		param3.width = width;
		param3.height = height;
		joinBtn.setLayoutParams(param3);

		int length = (int) (ScaleUtil.dip2px(this, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
		if (speaker_gridview != null)
			speaker_gridview.setColumnWidth(length);
		if (listener_gridview != null)
			listener_gridview.setColumnWidth(length);

		if (!adjustSizeCompleted)
		{
			ScaleUtil.adjustTextSize(settingConfirmBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(quitBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(press_to_speak_checkbox, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(speaker_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(listener_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(no_listener_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(no_speaker_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(joinBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(mysetting_title, ConfigManager.scaleRatio);

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

	public void notifyDataChanged()
	{
		refreshData();
		refreshNoTip();
		if (speaker_adapter != null)
			speaker_adapter.notifyDataSetChanged();
		if (listener_adapter != null)
			listener_adapter.notifyDataSetChanged();
	}

	class MemberGridAdapter extends BaseAdapter
	{
		private MyActionBarActivity	activity;
		private LayoutInflater		inflater;
		private List<UserInfo>		userList;
		private int					role;
		private int					remove_position	= -1;
		private boolean				isVisible		= true;

		public MemberGridAdapter(MyActionBarActivity activity, LayoutInflater inflater, List<UserInfo> userList, int role)
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

			RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
			if (headImage != null)
			{
				GradientDrawable bgShape = (GradientDrawable) headImage.getBackground();
				if (role == 1)
					bgShape.setColor(0xFF852828);
				else if (role == 2)
					bgShape.setColor(0xFF38693F);
			}
			TextView name = ViewHolderHelper.get(convertView, R.id.name);

			if (!isVisible && (position == -1 + userList.size()))
			{
				if (headImage != null)
					headImage.setImageDrawable(null);
				if (name != null)
					name.setText("");
			}
			else if (remove_position == position)
			{
				if (headImage != null)
					headImage.setImageDrawable(null);
				if (name != null)
					name.setText("");
			}
			else
			{
				UserInfo userInfo = (UserInfo) getItem(position);

				if (userInfo != null)
				{
					if (headImage != null)
						ImageUtil.setHeadImage(activity, userInfo.headPic, headImage, userInfo);
					if (name != null)
						name.setText(userInfo.userName);
				}

			}

			return convertView;
		}

		public void addItem(UserInfo user)
		{
			userList.add(user);
			notifyDataSetChanged();
		}

		public void setRemove(int position)
		{
			remove_position = position;
			notifyDataSetChanged();
		}

		public void remove()
		{
			if (remove_position >= 0)
				userList.remove(remove_position);
			remove_position = -1;
			notifyDataSetChanged();
		}

		public boolean isVisible()
		{
			return isVisible;
		}

		public void setVisible(boolean visible)
		{
			isVisible = visible;
		}
	}

	private void moveWithAnim(View moveView, int[] startLocation, int[] endLocation, final UserInfo userInfo, final GridView clickGridView,
			final int role)
	{
		int[] initLocation = new int[2];
		moveView.getLocationInWindow(initLocation);
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
		TranslateAnimation moveAnimation = new TranslateAnimation(startLocation[0], endLocation[0], startLocation[1], endLocation[1]);
		moveAnimation.setDuration(300L);
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
				isMove = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				moveViewGroup.removeView(mMoveView);
				if (role == 2)
				{
					listener_adapter.setVisible(true);
					listener_adapter.notifyDataSetChanged();
					speaker_adapter.remove();
				}
				else
				{
					speaker_adapter.setVisible(true);
					speaker_adapter.notifyDataSetChanged();
					listener_adapter.remove();
				}
				refreshNoTipWithMove();
				isMove = false;
			}
		});
	}

	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation)
	{
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}

	private ViewGroup getMoveViewGroup()
	{
		ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(this);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}

	private ImageView getView(View view)
	{
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(cache);
		return iv;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
	{
		if (isMove || !WebRtcPeerManager.getInstance().canControllerRole())
			return;
		if (parent.getId() == R.id.speaker_gridview)
		{
			final ImageView moveImageView = getView(view);
			if (moveImageView != null)
			{
				RoundImageView headImage = (RoundImageView) view.findViewById(R.id.headImage);
				final int[] startLocation = new int[2];
				headImage.getLocationInWindow(startLocation);
				final UserInfo user = (UserInfo) ((MemberGridAdapter) parent.getAdapter()).getItem(position);
				listener_adapter.setVisible(false);
				listener_adapter.addItem(user);
				refreshNoTipWithMove();
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						try
						{
							int[] endLocation = new int[2];
							int lastPos = listener_gridview.getLastVisiblePosition();
							listener_gridview.getChildAt(lastPos).getLocationInWindow(endLocation);
							moveWithAnim(moveImageView, startLocation, endLocation, user, speaker_gridview, 2);
							speaker_adapter.setRemove(position);
							refreshSpeakerTitle();
						}
						catch (Exception localException)
						{
							localException.printStackTrace();
						}
					}
				}, 50L);
			}
		}
		else if (parent.getId() == R.id.listener_gridview)
		{
			if (getSpeakerCount() >= 5)
				return;
			final ImageView moveImageView = getView(view);
			if (moveImageView != null)
			{
				RoundImageView headImage = (RoundImageView) view.findViewById(R.id.headImage);
				final int[] startLocation = new int[2];
				headImage.getLocationInWindow(startLocation);
				final UserInfo user = (UserInfo) ((MemberGridAdapter) parent.getAdapter()).getItem(position);
				speaker_adapter.setVisible(false);
				speaker_adapter.addItem(user);
				refreshNoTipWithMove();
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						try
						{
							int[] endLocation = new int[2];
							int lastPos = speaker_gridview.getLastVisiblePosition();
							speaker_gridview.getChildAt(lastPos).getLocationInWindow(endLocation);
							moveWithAnim(moveImageView, startLocation, endLocation, user, listener_gridview, 1);
							listener_adapter.setRemove(position);
							refreshSpeakerTitle();
						}
						catch (Exception localException)
						{
							localException.printStackTrace();
						}
					}
				}, 50L);
			}
		}
	}
}