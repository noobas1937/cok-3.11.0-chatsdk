package com.elex.im.cok.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.elex.im.CokConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.event.LogEvent;
import com.elex.im.core.event.WSStatusEvent;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBHelper;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.ui.GSController;
import com.elex.im.ui.UIManager;

public class GSCokActivity extends Activity {
	private static final String	ACTIVITY_TAG	= "GSCokActivity";
	
	private Button button1;
	private Button button0;
	private Button button2;
	private Button button3;
	private EditText statusLabel;
	
	public GSCokActivity()
	{
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wrapper_chat_service_main);
		
//		GlobalData.initialize(getApplicationContext(), 100000006);
//		Firebase.setAndroidContext(getApplicationContext());
		
		final GSCokActivity context = this;
		button0 = (Button)findViewById(ResUtil.getId(this, "id", "button0"));
		button0.setText("国家联盟聊天");
		button0.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	showChatFragment();
            }
        });
//		buttonConnect.setVisibility(View.GONE);
		
		button1 = (Button)findViewById(ResUtil.getId(this, "id", "button1"));
		button1.setText("邮件");
		button1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	showMailFragment();
            }
        });
		
		button2 = (Button)findViewById(ResUtil.getId(this, "id", "button2"));
		button2.setText("聊天室成员选择");
		button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	showMemberSelectorFragment();
//            	WebSocketManager.getInstance().sendUserMsg();
            }
        });

		button3 = (Button)findViewById(ResUtil.getId(this, "id", "button3"));
		button3.setText("聊天室选项");
		button3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	showChatRoomSetting();
            }
        });
		
		statusLabel = (EditText)findViewById(ResUtil.getId(this, "id", "wsStatusLabel"));
		statusLabel.setKeyListener(null);
		
		GSController.init(this, true);
		
		User me = new User();
		UserManager.getInstance().setCurrentUser(me);
		me.uid = "1380131787000001";
		me.userName = "haofanlu";
		me.headPic = "g026";
		me.serverId = 1;
		me.lang = "zh_CN";
		me.lastUpdateTime = 1452513691;
		me.allianceId = "193ed2dd61934512b3e4ad1e369d51d4";
		me.allianceRank = 1;
		me.asn = "SSD";
		me.crossFightSrcServerId = -1;
//		me.headPicVer = 0;
//		me.lastChatTime = 0;
//		me.mGmod = 1;
//		me.svipLevel = -1;
//		me.tableVer = 8;
//		me.type = 0;
//		me.vipEndTime = 1447259828;
//		me.vipLevel = 4;
		
//		ChatRoomId original = new ChatRoomId("test_original_1", "original", TimeManager.getTimeInMS(0));
//		ChatRoomId alliance = new ChatRoomId("alliance_1_193ed2dd61934512b3e4ad1e369d51d4", "alliance", TimeManager.getTimeInMS(0));

		IMCore.getInstance();
		DBManager.getInstance().initInWrapper();
		
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
		IMCore.getInstance().init(CokConfig.getInstance());
		
//		initDummy();
//		initRealDB();
//		XiaoMiToolManager.testActivity(this);
		
		LanguageConfiger.initFromINIInBackground(this);

//		XiaoMiToolManager.getInstance().initSDK(this);
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
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		IMCore.getInstance().removeAllEventListener(this);
		
		// 只能关闭其它task的一个activity
//		android.os.Process.killProcess(android.os.Process.myPid());

//		ServiceInterface.clearStack();
//		int currentVersion = android.os.Build.VERSION.SDK_INT;
//		if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1)
//		{
//			Intent startMain = new Intent(Intent.ACTION_MAIN);
//			startMain.addCategory(Intent.CATEGORY_HOME);
//			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(startMain);
//			System.exit(0);
//		}
//		else
//		{
//			// android2.1
//			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//			am.restartPackage(getPackageName());
//		}
	}

	private void initRealDB()
	{
//		ChatServiceController.init(this, true);
//		initBaseInfo();
//		copyDBFile();
//
//		DBManager.getInstance().initDB(this);
//
//		User me = UserManager.getInstance().getCurrentUser();
//		ServiceInterface.setPlayerInfo(me.serverId, TimeManager.getInstance().getCurrentTime(), me.mGmod, me.headPicVer, me.userName, me.uid, me.headPic, me.vipLevel, me.svipLevel, me.vipEndTime, me.lastUpdateTime, me.crossFightSrcServerId);
//		ServiceInterface.setPlayerAllianceInfo(me.asn, me.allianceId, me.allianceRank, true);
//		
//		LanguageConfiger.initLanguage(this);
//		initAllianceMembers(UserManager.getInstance().getCurrentUser().allianceId); //需要语言包才能正确解析
//		
//		ServiceInterface.handleGetNewMailMsg(ChannelManager.getInstance().getSimulateReturnChannelInfo());
	}

	private void initAllianceMembers(String allianceId)
	{
		if(StringUtils.isNotEmpty(allianceId)){
			ArrayList<User> members = new ArrayList<User>();//DBManager.getInstance().getAllianceMembers(allianceId);
			String uidStr = "";
			String lastUpdateTimeStr = "";
			for (int i = 0; i < members.size(); i++)
			{
				if(i > 0){
					uidStr += "_";
					lastUpdateTimeStr += "_";
				}
				uidStr += members.get(i).uid;
				lastUpdateTimeStr += members.get(i).lastUpdateTime;
			}
//			ServiceInterface.notifyUserUids(uidStr, lastUpdateTimeStr, UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE);
		}
	}
	
	public final String dummyDBUser = USER_ZY_INNER;
	public static final String USER_ZY_INNER = "1380131787000001"; //内网包，个人邮件适量，系统邮件900
	public static final String USER_XU_INNER = "1380625871000001";
	public static final String USER_HUCHAO_532 = "909504798000489";
	public static final String USER_MAIL_TEMP = "51568477000000";
	public static final String USER_XUTEST_BETA = "1385363567000001";

	public static final String USER_XIAOHUA_BETA = "139192923000001";
	
	public static final String USER_ZY532_Major = "913608715000047";
	public static final String USER_ZY532_Minor = "1480348070000532";
	
	public static final String USER_MOD = "424596561000011"; //mod，个人邮件很多，系统邮件5
	public static final String USER_MAIL_OVER_500 = "195242040000007"; //个人邮件超过500，系统邮件230
	
	public static final String USER_HUCHAO_BETA = "51568477000000";
	
	private void copyDBFile() {
	    AssetManager assetManager = getAssets();
	    String sourceFile = "database/" + dummyDBUser + ".db";
        InputStream in = null;
        OutputStream out = null;
        try {
          in = assetManager.open(sourceFile);
          File outFile = new File(DBHelper.getDBFileAbsolutePath(this));
          out = new FileOutputStream(outFile);
          copyFile(in, out);
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + sourceFile, e);
        }     
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	private void initBaseInfo()
	{
//		ServiceInterface.setIsNewMailListEnable(true);
//		ConfigManager.enableCustomHeadImg = true;
//		UserManager.getInstance().setCurrentUserId(dummyDBUser); // 1380131787000001 379997000002
//		ServiceInterface.postPlayerLevel(6);
	}
	
	private void initDummy() {
//		ChatServiceController.init(this, true);
//		initBaseInfo();
//		DBManager.getInstance().initDB(this);
//		
//		ServiceInterface.setPlayerInfo(	1,
//										TimeManager.getInstance().getCurrentTime(),
//										2,
//										0,
//										"zhou",
//										"379997000002",
//										"g044",
//										4,
//										0,
//										TimeManager.getInstance().getCurrentLocalTime() + 60,
//										TimeManager.getInstance().getCurrentTime(), 
//										-1);
////		ServiceInterface.setPlayerAllianceInfo("zhe", "allianceIdX", 2, true);
//		LanguageConfiger.initLanguage(this);
//		
//		initDummyUserAndMsg();
	}

	private void initDummyUserAndMsg()
	{
//		User[] userInfos = getDummyUsers();
//		for (int i = 0; i < userInfos.length; i++)
//		{
//			UserManager.getInstance().addUser(userInfos[i]);
//		}
//		User cuser = UserManager.getInstance().getCurrentUser();
//
//		Msg[] msgs = getDummyMsgs(userInfos, cuser);
//		for (int i = 0; i < msgs.length; i++)
//		{
//			if(!msgs[i].isRedPackageMessage())
//				msgs[i].sendState = Msg.SEND_SUCCESS;
//			ChannelManager.getInstance().countryChannel.msgList.add(msgs[i]);
//		}
//		DBManager.getInstance().insertMessages(msgs, ChannelManager.getInstance().countryChannel.getChatTable());
//		
//		ChatChannel chatRoom = ChannelManager.getInstance().addDummyChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, "dummyChatRoom");
//		for (int i = 0; i < userInfos.length; i++)
//		{
//			chatRoom.memberUidArray.add(userInfos[i].uid);
//		}
//		chatRoom.latestTime = TimeManager.getInstance().getCurrentTime();
//		Msg[] msgs2 = getDummyMsgs(userInfos, cuser);
//		for (int i = 0; i < msgs2.length; i++)
//		{
//			msgs2[i].sendState = Msg.SEND_SUCCESS;
//			chatRoom.msgList.add(msgs2[i]);
//		}
		
//		ChannelManager.getInstance().countryChannel.msgList.get(0).user = UserManager.getInstance().getCurrentUser();
//		ChannelManager.getInstance().countryChannel.msgList.get(4).user = UserManager.getInstance().getCurrentUser();
//		System.out.println(LogUtil.typeToString(ChannelManager.getInstance().countryChannel.msgList.get(0)));
//		System.out.println(LogUtil.compareObjects(new Object[]{ChannelManager.getInstance().countryChannel.msgList.get(0), ChannelManager.getInstance().countryChannel.msgList.get(3), ChannelManager.getInstance().countryChannel.msgList.get(4)}));
//		System.out.println(LogUtil.typeToString(UserManager.getInstance().getCurrentUser()));
//		System.out.println(LogUtil.compareObjects(new Object[]{UserManager.getInstance().getCurrentUser(), UserManager.getInstance().getCurrentUser()}));
		
//		ChatServiceController.currentChatType=0;
//		ChatServiceController.isInMailDialog=true;

//		TimeManager.getInstance().setServerBaseTime(Math.round(System.currentTimeMillis() / 1000));
	}

	private User[] getDummyUsers()
	{
		User[] userInfos = {
			new User(5, 0, 0, 7, "131762465000002", "Ned", "Winterfell", "g045", TimeManager.getInstance().getCurrentTime()),
			new User(1, 0, 0, 0, "131762465000003", "Jemmy", "King`s Landing", "g008", TimeManager.getInstance().getCurrentTime()) ,
			new User(5, 0, 0, 1, "131762465000004", "Imp", "Casterly Rock", "g044", TimeManager.getInstance().getCurrentTime()) ,
			new User(11, 0, 0, 10, "131762465000005", "John Snow", "Winterfell", "g043", TimeManager.getInstance().getCurrentTime()) };
		return userInfos;
	}
	private Msg[] getDummyMsgs(User userInfos[], User cuser)
	{
		Msg[] msgs = {
				new Msg(1, true, false, 0, 100, userInfos[0].uid, "我要退出联盟", "","中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(2, true, true, 0, 3, cuser.uid, "In order to use the Software and related services on www.cok.com, or call the number 13825462145. You must first agree to this License Agreement. android@cok.com.", "", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(3, true, false, 0, 12, userInfos[1].uid, "快来拆红包1", "快来拆红包1", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(4, false, true, 0, 12, cuser.uid, "快来拆红包2", "快来拆红包2", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(5, true, false, 0, 12, userInfos[2].uid, "快来拆红包3", "快来拆红包3", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(6, false, true, 0, 7, cuser.uid, "3|134054|105392|[{\"name\":\"sfds|f|t\"}]", "3|王者之剑", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(7, true, true, 0, 12, cuser.uid, "快来拆红包4", "快来拆红包4", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(8, false, false, 0, 6, cuser.uid, "集合攻打此反倒是坐标200:341", "集合攻打此坐标0:341", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(9, true, false, 0, 13, userInfos[3].uid, "5", "8", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(10, false, true, 0, 0, cuser.uid, "6", "9", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(11, true, false, 0, 0, userInfos[3].uid, "dsfdsfewrwerfds", "发生的范德萨范德萨", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(12, false, true, 0, 0, cuser.uid, "玩儿玩儿玩儿", "发生的福尔沃特V大是", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(13, true, false, 0, 0, userInfos[3].uid, "威尔额外热温热温热污染️", "发生的服务而额外任务而", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(14, false, true, 0, 0, cuser.uid, "💪", "9", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(15, true, false, 0, 0, userInfos[3].uid, "😤", "8", "中文",TimeManager.getInstance().getCurrentTime()),
				new Msg(16, false, true, 0, 0, cuser.uid, "😳", "9", "中文",TimeManager.getInstance().getCurrentTime()) 
				
//				new Msg(1, true, false, 0, 100, userInfos[0].uid, "1", "","中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(2, true, true, 0, 6, cuser.uid, "2", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(3, true, false, 0, 12, userInfos[1].uid, "3", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(4, false, true, 0, 12, cuser.uid, "4", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(5, true, false, 0, 12, userInfos[2].uid, "5", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(6, false, true, 0, 13, cuser.uid, "6", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(7, true, true, 0, 12, cuser.uid, "7", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(8, false, false, 0, 6, cuser.uid, "8", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(9, true, false, 0, 13, userInfos[3].uid, "9", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(10, false, true, 0, 0, cuser.uid, "10", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(11, true, false, 0, 0, userInfos[3].uid, "11", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(12, false, true, 0, 0, cuser.uid, "12", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(13, true, false, 0, 0, userInfos[3].uid, "13️", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(14, false, true, 0, 0, cuser.uid, "14", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(15, true, false, 0, 0, userInfos[3].uid, "15", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new Msg(16, false, true, 0, 0, cuser.uid, "16", "", "中文",TimeManager.getInstance().getCurrentTime())
				};
		msgs[2].sendState = 1;
//		msgs[2].createTime = TimeManager.getInstance().getCurrentTime() - 24*60*60 + 60;
		msgs[3].sendState = 0;
		msgs[4].sendState = 2;
		msgs[6].sendState = 3;
		msgs[2].attachmentId = "fsdfwerwwr_1";
		msgs[3].attachmentId = "32423dsfsrwr_1";
		msgs[4].attachmentId = "34235dsas_1";
		msgs[6].attachmentId = "fsdf324235werwwr_1";
		return msgs;
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
				statusLabel.setText(statusLabel.getText() + (statusLabel.getText().length() > 0 ? "\n" : "") + "[" + statusCount + "] "
						+ message);
				statusLabel.setSelection(statusLabel.getText().length(), statusLabel.getText().length());
			}
		});
	}

	protected void showChatFragment() {
		UIManager.showChatActivity(GSCokActivity.this, DBDefinition.CHANNEL_TYPE_COUNTRY, false);
	}

	protected void onSendMsg() {
//    	Channel channel = ChannelManager.getInstance().getAllianceChannel();
//    	channel.sendMsg("test message", false, false, null);
	}

	protected void showMailFragment() {
		UIManager.showChatActivity(GSCokActivity.this, DBDefinition.CHANNEL_TYPE_USER, false);
	}

	protected void showMemberSelectorFragment() {
		GSController.isCreateChatRoom = true;
		UIManager.showMemberSelectorActivity(GSCokActivity.this, true);
	}

	protected void showChatRoomSetting() {
    	GSController.setMailInfo("146673000001", "", "123", 1);
    	UIManager.showChatRoomSettingActivity(GSCokActivity.this);
	}
}
