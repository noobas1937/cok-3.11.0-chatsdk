package com.elex.im.cok.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elex.im.AppConfig;
import com.elex.im.cok.ServiceInterface;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.event.LogEvent;
import com.elex.im.core.event.PermissionStatusEvent;
import com.elex.im.core.event.ValidateChatEvent;
import com.elex.im.core.event.WSStatusEvent;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBHelper;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.SharePreferenceUtil;
import com.elex.im.ui.GSController;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.config.AllianceChannelDef;
import com.elex.im.ui.config.BattleFieldChannelDef;
import com.elex.im.ui.config.ChannelDef;
import com.elex.im.ui.config.ChannelDefManager;
import com.elex.im.ui.config.ChatroomChannelDef;
import com.elex.im.ui.config.CountryChannelDef;
import com.elex.im.ui.config.CustomChannelDef;
import com.elex.im.ui.config.UserMailChannelDef;
import com.elex.im.ui.controller.MenuController;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.util.LoginShareDataUtil;
import com.elex.im.ui.util.LoginShareDataUtil.LoginShareData;
import com.mi.mimsgsdk.utils.GlobalData;

public class GSCokAppActivity extends Activity {
	private static final String	ACTIVITY_TAG	= "GSCokAppActivity";
	private static final String	KEY_ACTIVE	= "active";
	private static GSCokAppActivity activity;
	private EditText statusLabel;
	private LinearLayout progressLayout;
	private TextView progressTextView;
	private ProgressBar	progressBar;
	
	public GSCokAppActivity()
	{
	}
	
	public static GSCokAppActivity getInstance()
	{
		return activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gs_cok_app_loading);
		
		activity = this;

		GlobalData.initialize(this, 100000006);
		
		progressLayout = (LinearLayout) findViewById(ResUtil.getId(this, "id", "progressLayout"));
		progressBar = (ProgressBar) findViewById(ResUtil.getId(this, "id", "progressBar"));
		progressTextView = (TextView) findViewById(ResUtil.getId(this, "id", "progressTextView"));
		progressTextView.setText("");
		
		ArrayList<ChannelDef> channelDefs = new ArrayList<ChannelDef>();
		channelDefs.add(new CountryChannelDef());
		channelDefs.add(new AllianceChannelDef());
		channelDefs.add(new UserMailChannelDef());
		channelDefs.add(new ChatroomChannelDef());
		channelDefs.add(new BattleFieldChannelDef());
		channelDefs.add(new CustomChannelDef());
		ChannelDefManager.getInstance().init(channelDefs);
		
		GSController.getInstance().init(this, true);
		UIManager.setCurrentActivity(this);

		IMCore.getInstance().setAppConfig(AppConfig.getInstance());
		
		IMCore.getInstance().addEventListener(ValidateChatEvent.GOTO_GAME, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof ValidateChatEvent)
				{
					openGame();
				}
			};
		});

		IMCore.getInstance().addEventListener(PermissionStatusEvent.PERMISION_STATUS, this, new EventCallBack(){
			public void onCallback(Event event){
				if(event instanceof PermissionStatusEvent)
				{
					checkSDCardPermission();
				}
			};
		});
		
		LanguageConfiger.initMinimamLang();

		checkSDCardPermission();
	}
	
	private void checkSDCardPermission()
	{
		if(!DBHelper.isSDcardAvaiable())
		{
			MenuController.showYesConfirm("SD Card is unavailable, please make sure your SD Card is mounted and try again.", null, false, "", false);
			return;
		}

		if(!PermissionManager.isExternalStoragePermissionsAvaiable())
		{
			PermissionManager.getInstance().getDBStoragePermission();
			return;
		}
		
		checkLoginShareData();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		PermissionManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	private void checkLoginShareData()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL);
		if (isSessionTimeValid())
		{
			try
			{
				init();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		else
		{
			// TODO 弹窗说明；如果没有选择游戏，之后还要再提醒
			openGame();
		}
	}
	
	private boolean isSessionTimeValid()
	{
		LoginShareData loginData = LoginShareDataUtil.getSavedLoginShareData();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "loginData", loginData);
		if (loginData == null)
			return false;

		// 注意此时currentUserId为null，存sharePreference时，key里uid为null，不过只作为临时存取没有问题
		WebSocketManager.chatSessionEnable = loginData.chatSessionEnable;
		SharePreferenceUtil.setChatSessionForApp(loginData.session, loginData.sessionExpire);
		return SharePreferenceUtil.isSessionTimeValid();
	}
	
	private boolean inited = false;
	/**
	 * 如果后台发现session过期，会调第二次
	 */
	private void init()
	{
		LoginShareData loginData = LoginShareDataUtil.getSavedLoginShareData();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "loginData", LoginShareDataUtil.getSavedLoginDataJson());

		if(!inited){
			UserManager.getInstance().setCurrentUserId(loginData.uid);
			User currentUser = new User();
			currentUser.setFromJson(loginData.currentUser);
			UserManager.getInstance().setCurrentUser(currentUser);
			
//			DBHelper.targetPackageName = loginData.packageName;
			DBManager.getInstance().initInWrapper();
			DBManager.getInstance().getUserDao().upsert(currentUser);
			
			User me = UserManager.getInstance().getCurrentUser();
			ServiceInterface.setPlayerAllianceInfo(me.asn, me.allianceId, me.allianceRank, true);
			
			IMCore.getInstance().addEventListener(WSStatusEvent.STATUS_CHANGE, this, new EventCallBack(){
				public void onCallback(Event event){
					if(event instanceof WSStatusEvent)
					{
						onWSStatusChanged((WSStatusEvent) event);
					}
				};
			});
			IMCore.getInstance().addEventListener(LogEvent.LOG, this, new EventCallBack(){
				public void onCallback(Event event){
					if(event instanceof LogEvent)
					{
						refreshStatus(((LogEvent) event).log);
					}
				};
			});
			IMCore.getInstance().start();
			LanguageConfiger.initFromWebINI(loginData);
		}
		
		WebSocketManager.chatSessionEnable = loginData.chatSessionEnable;
		SharePreferenceUtil.setChatSession(loginData.session, loginData.sessionExpire);
		
		if(SharePreferenceUtil.checkSession())
		{
			WebSocketManager.getInstance().connect();
			
			try
			{
				XiaoMiToolManager.getInstance().initSDK(this, loginData.xiaomi);	
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			
			if(languageLoadComplete && !enterChatFragment && isActive())
			{
				showChatFragment();
			}
		}
		
		inited = true;
	}
	
	private boolean languageLoadComplete = false;
	private boolean enterChatFragment = false;
	public void onLanguageLoadComplete()
	{
		languageLoadComplete = true;

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				progressLayout.setVisibility(View.GONE);
			}
		});
		
		// 如果后台验证不通过，显示弹窗后，语言刚好加载完，要通过isOpenGameConfirmShowing来禁止进入聊天界面
		// 但这个变量只覆盖了自己的确认弹窗，系统选择游戏弹窗没有覆盖
		if (inited && !enterChatFragment && isActive() && !isOpenGameConfirmShowing)
		{
			showChatFragment();
		}
	}

    protected void onWSStatusChanged(WSStatusEvent event)
	{
		String output = "";

		switch (event.subType)
		{
			case WSStatusEvent.RECONNECTING:
				break;
			case WSStatusEvent.CONNECTING:
				output = "Connecting";
				refreshStatus(output);
				break;
			case WSStatusEvent.CONNECTED:
				break;
			case WSStatusEvent.DISCONNECTED:
				break;
			case WSStatusEvent.CONNECTION_FAILED:
				break;
			default:
				return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
//		ChatServiceController.setCurrentActivity(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "requestCode", requestCode, "data", data);
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0)
		{
			if(data == null)
			{
				// 关掉了系统选择游戏弹窗
				if(!isSessionTimeValid())
				{
					checkLoginShareData();
				}
			}
			else if (data.getStringExtra("return").equals("validateChat"))
			{
				checkLoginShareData();
			}
			
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		IMCore.getInstance().removeAllEventListener(this);
	}

	private int statusCount = 0;
	private void refreshStatus(final String message)
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				statusCount++;
				if(statusLabel == null) return;
				statusLabel.setText(statusLabel.getText() + (statusLabel.getText().length() > 0 ? "\n" : "") + "[" + statusCount + "] "
						+ message);
				statusLabel.setSelection(statusLabel.getText().length(), statusLabel.getText().length());
			}
		});
	}

	protected void showChatFragment() {
		UIManager.showChatActivity(GSCokAppActivity.this, DBDefinition.CHANNEL_TYPE_COUNTRY, false);
		enterChatFragment = true;
	}

	protected void showMailFragment() {
		UIManager.showChatActivity(GSCokAppActivity.this, DBDefinition.CHANNEL_TYPE_USER, false);
	}

	protected void showMemberSelectorFragment() {
		GSController.isCreateChatRoom = true;
		UIManager.showMemberSelectorActivity(GSCokAppActivity.this, true);
	}

	protected void showChatRoomSetting() {
    	GSController.setMailInfo("146673000001", "", "123", 1);
    	UIManager.showChatRoomSettingActivity(GSCokAppActivity.this);
	}

	private boolean isOpenGameConfirmShowing = false;
	public void openGame()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL);
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					OnClickListener okOnlickListener = new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							isOpenGameConfirmShowing = false;
							activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("cok://validateChat")), 0);
						}
					};
					// "尚未登陆或登陆已过期，请启动游戏验证登陆"
					MenuController.showYesConfirm("Session is missing or expired, please launch game and login", okOnlickListener, true, "Launch game", false);
					isOpenGameConfirmShowing = true;
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}
	
    @Override
    public void onBackPressed()
    {
    }

	@Override
	protected void onStart()
	{
		super.onStart();

		SharedPreferences sp = getSharedPreferences(ACTIVITY_TAG, MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putBoolean(KEY_ACTIVE, true);
		ed.commit();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		SharedPreferences sp = getSharedPreferences(ACTIVITY_TAG, MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putBoolean(KEY_ACTIVE, false);
		ed.commit();
	}
	
	private boolean isActive()
	{
		SharedPreferences sp = getSharedPreferences(ACTIVITY_TAG, MODE_PRIVATE);
		return sp.getBoolean(KEY_ACTIVE, false);
	}
	
	protected void setProgressText(final String text)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				progressTextView.setText(text);
			}
		});
	}
}
