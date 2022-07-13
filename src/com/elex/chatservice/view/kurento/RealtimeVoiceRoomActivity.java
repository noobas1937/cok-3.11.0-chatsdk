package com.elex.chatservice.view.kurento;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class RealtimeVoiceRoomActivity extends MyActionBarActivity
{
	private ExpandableListView					voice_expand_listview;
	private VoiceSettingExpandableListAdapter	voiceExpandableApdater;
	private TextView							settingConfirmBtn;
	private TextView							quitBtn;
	private boolean								adjustSizeCompleted	= false;
	private boolean								isCreate			= false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.realtime_voice_room, fragmentLayout, true);
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_VOICE_CHAT_ROOM));
		showRightBtn(null);

		voice_expand_listview = (ExpandableListView) findViewById(R.id.voice_expand_listview);
		voice_expand_listview.setGroupIndicator(null);
		voiceExpandableApdater = new VoiceSettingExpandableListAdapter(this);
		voice_expand_listview.setAdapter(voiceExpandableApdater);
		voice_expand_listview.expandGroup(0);
		voice_expand_listview.expandGroup(1);
		voice_expand_listview.expandGroup(2);

		settingConfirmBtn = (TextView) findViewById(R.id.settingConfirmBtn);
		settingConfirmBtn.setVisibility(WebRtcPeerManager.published && UserManager.getInstance().getCurrentUser() != null
				&& UserManager.getInstance().getCurrentUser().allianceRank >= 4 ? View.VISIBLE : View.GONE);
		settingConfirmBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (voiceExpandableApdater != null)
				{
					List<String> speakerList = voiceExpandableApdater.getUnConfirmedSpeaker();
					if (speakerList != null)
					{
						List<String> originalSpeakerList = WebRtcPeerManager.getInstance().getSpeakerList();
						boolean changed = false;
						if (originalSpeakerList.size() != speakerList.size())
							changed = true;
						String uidStr = "";
						for (String speaker : speakerList)
						{
							if (StringUtils.isNotEmpty(speaker))
							{
								if (!changed && !originalSpeakerList.contains(speaker))
									changed = true;
								if (StringUtils.isNotEmpty(uidStr))
									uidStr += ",";
								uidStr += speaker;
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
						exitActivity();
					}
				}
			}
		});

		quitBtn = (TextView) findViewById(R.id.quitBtn);
		quitBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (RealtimeVoiceRoomActivity.this != null)
					MenuController.showQuitRealtimeVoiceRoomConfirm(RealtimeVoiceRoomActivity.this, LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_QUIT));
			}
		});

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

		if (!adjustSizeCompleted)
		{
			ScaleUtil.adjustTextSize(settingConfirmBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(quitBtn, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
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
		if (voiceExpandableApdater != null)
		{
			voiceExpandableApdater.refreshData();
			voiceExpandableApdater.notifyDataSetChanged();
		}
	}
}