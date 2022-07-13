package com.elex.chatservice.view;

import org.apache.commons.lang.StringUtils;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class ChatRoomNameModifyActivity extends MyActionBarActivity
{
	private EditText	name_edit;
	private TextView	btn_change_name;
	private boolean		adjustSizeCompleted	= false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		if(ConfigManager.getInstance().needRTL())
			inflater.inflate(R.layout.chat_room_change_name_activity_ar, fragmentLayout, true);
		else
			inflater.inflate(R.layout.chat_room_change_name_activity, fragmentLayout, true);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_MODIFY_CHATROOM_NAME));
		
		showRightBtn(null);

		name_edit = (EditText) findViewById(R.id.name_edit);
		name_edit.setText("");
		name_edit.requestFocus();
		name_edit.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				refreshBtnEnable();
			}
		});

		btn_change_name = (TextView) findViewById(R.id.btn_change_name);
		btn_change_name.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_MODIFY_CHATROOM_NAME));
		btn_change_name.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if(SwitchUtils.customWebsocketEnable){
					WebSocketManager.getInstance().roomGroupModifyName(UserManager.getInstance().getCurrentMail().opponentUid,name_edit.getText().toString());
				}else {
					JniController.getInstance().excuteJNIVoidMethod("modifyChatRoomName",
							new Object[] { UserManager.getInstance().getCurrentMail().opponentUid, name_edit.getText().toString() });
				}
				exitActivity();
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
	}

	private void refreshBtnEnable()
	{
		if (StringUtils.isNotEmpty(name_edit.getText().toString()))
		{
			setSendBtnEnable(btn_change_name, true);
		}
		else
		{
			setSendBtnEnable(btn_change_name, false);
		}
	}

	private void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			return;
		}

		if (!adjustSizeCompleted)
		{
			int length = (int) (ScaleUtil.dip2px(this, 40) * ConfigManager.scaleRatio * getScreenCorrectionFactor());

			RelativeLayout.LayoutParams btn_change_name_LayoutParams = (RelativeLayout.LayoutParams) btn_change_name.getLayoutParams();
			btn_change_name_LayoutParams.height = length;
			btn_change_name.setLayoutParams(btn_change_name_LayoutParams);

			RelativeLayout.LayoutParams name_edit_LayoutParams = (RelativeLayout.LayoutParams) name_edit.getLayoutParams();
			name_edit_LayoutParams.height = length;
			name_edit.setLayoutParams(name_edit_LayoutParams);

			ScaleUtil.adjustTextSize(btn_change_name, ConfigManager.scaleRatio);
			float newTextSize = (int) (name_edit.getTextSize() * ConfigManager.scaleRatio);
			name_edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
			adjustSizeCompleted = true;
		}
	}

}