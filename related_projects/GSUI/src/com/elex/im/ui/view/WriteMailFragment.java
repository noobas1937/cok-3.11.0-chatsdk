package com.elex.im.ui.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elex.im.CokConfig;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.util.CompatibleApiUtil;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.view.actionbar.ActionBarFragment;

public class WriteMailFragment extends ActionBarFragment
{
	private ImageButton		sendMailButton;
	private ImageView		addPeopleButton;
	private EditText		recieverEditText;
	private EditText		contentEditText;
	private LinearLayout	fragmentContentLayout;

	private String			roomName	= "";
	private String			memberUids	= "";
	private String			memberNames	= "";

	public WriteMailFragment()
	{
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		((WriteMailActivity) getActivity()).fragment = this;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((WriteMailActivity) getActivity());

		if(ConfigManager.getInstance().needRTL())
			return inflater.inflate(R.layout.cs__write_mail_fragment_ar, container, false);
		else
			return inflater.inflate(R.layout.cs__write_mail_fragment, container, false);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (StringUtils.isNotEmpty(getWriteMailActivity().roomName))
			roomName = getWriteMailActivity().roomName;
		if (StringUtils.isNotEmpty(getWriteMailActivity().memberUids))
			memberUids = getWriteMailActivity().memberUids;
		if (StringUtils.isNotEmpty(getWriteMailActivity().memberNames))
		{
			memberNames = getWriteMailActivity().memberNames;
			recieverEditText.setText(memberNames);
		}
	}

	public WriteMailActivity getWriteMailActivity()
	{
		return (WriteMailActivity) activity;
	}

	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MAIL));

		fragmentContentLayout = (LinearLayout) view.findViewById(R.id.fragmentContentLayout);
		ImageUtil.setYRepeatingBG(activity, fragmentContentLayout, R.drawable.mail_list_bg);

		sendMailButton = (ImageButton) view.findViewById(R.id.sendMailButton);
		addPeopleButton = (ImageView) view.findViewById(R.id.addPeopleButton);
		recieverEditText = (EditText) view.findViewById(R.id.recieverEditText);
		contentEditText = (EditText) view.findViewById(R.id.contentEditText);

		refreshSendButton();

		recieverEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		});

		contentEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				contentEditText.post(new Runnable()
				{
					@Override
					public void run()
					{
					}
				});
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		});

		addPeopleButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				GSController.isCreateChatRoom = true;
				UIManager.showMemberSelectorActivity(activity, true);
			}
		});

		sendMailButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendMail();
			}
		});

		fragmentLayout.setOnTouchListener(new OnTouchListener()
		{
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hideSoftKeyBoard();
				return false;
			}
		});
	}

	private void refreshSendButton()
	{
		sendMailButton.setEnabled(canSend());
		CompatibleApiUtil.getInstance().setButtonAlpha(sendMailButton, canSend());
	}
	
	private boolean canSend()
	{
		return recieverEditText.getText().length() != 0 && (isMultiReceiver() || contentEditText.getText().length() != 0);
	}
	
	private boolean isMultiReceiver()
	{
		if (StringUtils.isNotEmpty(memberUids))
		{
			String[] uidArr = memberUids.split("\\|");
			if (uidArr.length > 1)
				return true;
		}
		return false;
	}

	private void sendMail()
	{
		boolean isOnlyOneReceiver = !isMultiReceiver();
		
		if (memberUids.equals("") || isOnlyOneReceiver)
		{
			String content = contentEditText.getText().toString();
			String title = content;
			if (content.length() > 30)
				title = content.substring(0, 29);

			String allianceMailId = "";
			// 如果填自己的名字则发联盟邮件
			
			if (UserManager.getInstance().getCurrentUser()!=null )
			{
				if (recieverEditText.getText().toString().equals(UserManager.getInstance().getCurrentUser().userName)
						&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().allianceId))
				{
					allianceMailId = UserManager.getInstance().getCurrentUser().allianceId;
				}
			}
			JniController.getInstance().excuteJNIVoidMethod(
					"sendMailMsg",
					new Object[] {
							recieverEditText.getText().toString(),
							title,
							content,
							allianceMailId,
							"",
							Boolean.valueOf(false),
							Integer.valueOf(0),
							"",
							"" });
			if (isOnlyOneReceiver && StringUtils.isNotEmpty(memberUids))
			{
				GSController.setMailInfo(memberUids, "", memberNames, MailManager.MAIL_USER);
				UIManager.showChatActivity(UIManager.getCurrentActivity(), CokConfig.getUserChannelType(), false);
			}
		}
		else
		{
			LogUtil.trackPageView("CreateChatRoom");
			String tempRoomName = roomName;
			if(StringUtils.isEmpty(tempRoomName))
			{
				if(StringUtils.isNotEmpty(memberNames) && !memberNames.contains(UserManager.getInstance().getCurrentUser().userName))
					tempRoomName = UserManager.getInstance().getCurrentUser().userName + "," + memberNames;
				else
					tempRoomName = memberNames;
			}
			JniController.getInstance().excuteJNIVoidMethod("createChatRoom",
					new Object[] { memberNames, memberUids, tempRoomName, contentEditText.getText().toString() });
		}
		activity.exitActivity();
		GSController.isCreateChatRoom = false;
	}
}
