package com.cok.chat.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.host.DummyHost;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.net.IWebSocketStatusListener;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.view.ChatFragment;
import com.firebase.client.Firebase;

public class CokChatTestActivity extends Activity implements IWebSocketStatusListener {
	private static final String	ACTIVITY_TAG	= "CokChatTestActivity";
	private Button				buttonConnect;
	private EditText			statusLabel;
	private Button				buttonTest;
	private Button				buttonSave;
	private TextView			textViewTitle;
	private TextView			textViewVersion;
	private int streamCount = 0;
	private int statusCount = 0;
	private boolean	isTesting = false;
	private boolean isFirstTest = true;
	
	public CokChatTestActivity()
	{
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wrapper_chat_service_main);
		
		statusLabel = (EditText)findViewById(ResUtil.getId(this, "id", "wsStatusLabel"));
		statusLabel.setKeyListener(null);

		textViewTitle = (TextView)findViewById(ResUtil.getId(this, "id", "textViewTitle"));
		textViewTitle.setText("COK Chat Test");
		textViewVersion = (TextView)findViewById(ResUtil.getId(this, "id", "textViewVersion"));
		textViewVersion.setText("V " + getVersionName() + " (" + getVersionCode() + ")");
		
		buttonTest = (Button)findViewById(ResUtil.getId(this, "id", "buttonTest"));
		buttonTest.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	doTest();
            }
        });
		
		buttonSave = (Button)findViewById(ResUtil.getId(this, "id", "buttonSave"));
		buttonSave.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
            	doSave();
            }
        });
		buttonSave.setVisibility(View.GONE);
		buttonSave.setEnabled(false);
		
		WebSocketManager.getInstance().setStatusListener(this);
		WebSocketManager.getInstance().connectAsSupervisor = true;
		Firebase.setAndroidContext(getApplicationContext());
		
		openSwitch();
		
		initDummy();
//		initRealDB();
		
		LogcatFileManager.getInstance().statusListener = this;
		LogcatFileManager.getInstance().startLogcatManager(this);
		

        spinner = (Spinner) findViewById(R.id.spinner);
    
        //数据
        data_list = new ArrayList<String>();
        data_list.add("Draft 10");
        data_list.add("Draft 17");
        data_list.add("Draft 75");
        data_list.add("Draft 76");
        
        //适配器
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {    
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "select draft："+ index);
                WebSocketManager.draftVersion = index;
            }
            public void onNothingSelected(AdapterView<?> arg0) {    
            }    
        });    
	}
	private Spinner spinner;
	private List<String> data_list;
	private ArrayAdapter<String> arr_adapter;

	private void doTest()
	{
		if(isFirstTest) isFirstTest = false;
		isTesting = true;
		enableButton(false);
		
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (WebSocketManager.getInstance().isConnected())
				{
					WebSocketManager.getInstance().forceReconnect();
				}
				else
				{
					ServiceInterface.connect2WS();
				}
			}
		});
	}
	
	protected void doSave()
	{
		String fileName = "CokChatTest " + TimeManager.getTimeYMDHMS(TimeManager.getInstance().getCurrentTime()) + ".txt";
		String filePath = Environment.getExternalStorageDirectory() + "/CokChatTest/";
		File logDirectory = new File(filePath);
		if ( !logDirectory.exists() ) {
			logDirectory.mkdir();
		}

		FileOutputStream out = null;
        try
		{
			out = new FileOutputStream(new File(filePath, fileName), true);
		}
		catch (FileNotFoundException e)
		{
			LogUtil.printException(e);
		}
        
		try
		{
			Process logcat;
            
//			final StringBuilder log = new StringBuilder();
			logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
			BufferedReader br = new BufferedReader(new InputStreamReader(logcat.getInputStream()),4*1024);
			String line;
			String separator = System.getProperty("line.separator"); 
		    while ((line = br.readLine()) != null) {
//		        log.append(line);
//		        log.append(separator);

                if (out != null)
                {
                    out.write((line + separator).getBytes());
                }
		    }

		    if (br != null) br.close();
		    br = null;
		    if (out != null) out.close();
		    out = null;

            if (logcat != null)
            {
            	logcat.destroy();
            	logcat = null;
            }
			
			// /storage/emulated/0/CokChatTest 2016-04-28 17-00-56.txt
//			Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath + fileName}); // , "chatservice:V", "*:S"
			this.onConsoleOutput("Log is saved at /SD Card/CokChatTest/" + fileName);
//			clearLog();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	private void clearLog()
	{
		try
		{
			Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
	}

	@Override
	public void onTestComplete()
	{
		isTesting = false;
		enableButton(true);
	}

	@Override
	public void onConnectError()
	{
		ServiceInterface.connect2WS();
	}
	
	protected void enableButton(final boolean enable)
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (!enable)
				{
					buttonTest.setText("Testing");
					buttonTest.setEnabled(false);
					buttonSave.setEnabled(false);
//					CompatibleApiUtil.getInstance().setButtonAlpha(buttonTest, false);
				}
				else
				{
					buttonTest.setText(isFirstTest ? "Test" : "Test Again");
					buttonTest.setEnabled(true);
					buttonSave.setEnabled(true);
//					CompatibleApiUtil.getInstance().setButtonAlpha(buttonTest, true);
				}
			}
		});
	}

	@Override
	public void onStremInput()
	{
		streamCount++;
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				buttonConnect.setText("连接websocket" + " [" + streamCount + "]");
			}
		});
	}

	private SimpleDateFormat dateFormat	= new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private void refreshStatus(final String message)
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				statusCount++;
				statusLabel.setText(statusLabel.getText() + (statusLabel.getText().length() > 0 ? "\n" : "") + "[" + statusCount + "] "
						+ dateFormat.format(new Date()) + " " + message);
				statusLabel.setSelection(statusLabel.getText().length(), statusLabel.getText().length());
			}
		});
	}

	@Override
	public void onStatus(String message)
	{
		ChatFragment.setConnectionStatus(message);
	}

	private void openSwitch()
	{
		ConfigManager.useWebSocketServer = true;
		ConfigManager.isRecieveFromWebSocket = true;
		ConfigManager.isSendFromWebSocket = true;
		ConfigManager.isXMEnabled = true;
		ConfigManager.isXMAudioEnabled = true;
		ConfigManager.isXMVedioEnabled = true;
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		ChatServiceController.setCurrentActivity(null);
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
		LogcatFileManager.getInstance().stopLogcatManager();
	}
	
	private void initBaseInfo()
	{
		ServiceInterface.setIsNewMailListEnable(true);
		ConfigManager.enableCustomHeadImg = true;
		UserManager.getInstance().setCurrentUserId(dummyDBUser); // 1380131787000001 379997000002
		ServiceInterface.postPlayerLevel(6,15);
	}
	
	private void initDummy() {
		ChatServiceController.init(this, true);
		((DummyHost) ChatServiceController.getInstance().host).treatAsDummyHost = false;
		initBaseInfo();
		UserInfo user0 = UserManager.getInstance().getCurrentUser();
		DBManager.getInstance().initDB(this);
		
		UserInfo user = UserManager.getInstance().getCurrentUser();
		ServiceInterface.setPlayerInfo(	1,
				TimeManager.getInstance().getCurrentTime(),
				8,
				2,
				0,
				"zhou",
				"379997000002",
				"g044",
				4,
				1,
				TimeManager.getInstance().getCurrentLocalTime() + 60,
				TimeManager.getInstance().getCurrentTime(), 
				-1,
				1,
				TimeManager.getInstance().getCurrentTime()+24*60*60);
//		ServiceInterface.setPlayerAllianceInfo("zhe", "allianceIdX", 2, true);
//		LanguageConfiger.initLanguage(this);
		UserInfo user2 = UserManager.getInstance().getCurrentUser();

//		ServiceInterface.handleGetNewMailMsg(ChannelManager.getInstance().getSimulateReturnChannelInfo());
		
//		initDummyUserAndMsg();
	}

	private void initDummyUserAndMsg()
	{
		UserInfo[] userInfos = getDummyUsers();
		for (int i = 0; i < userInfos.length; i++)
		{
			UserManager.getInstance().addUser(userInfos[i]);
		}
		UserInfo cuser = UserManager.getInstance().getCurrentUser();

		MsgItem[] msgs = getDummyMsgs(userInfos, cuser);
		for (int i = 0; i < msgs.length; i++)
		{
			if(!msgs[i].isRedPackageMessage())
				msgs[i].sendState = MsgItem.SEND_SUCCESS;
			ChannelManager.getInstance().countryChannel.msgList.add(msgs[i]);
		}
		DBManager.getInstance().insertMessages(msgs, ChannelManager.getInstance().countryChannel.getChatTable());
		
		ChatChannel chatRoom = ChannelManager.getInstance().addDummyChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, "dummyChatRoom");
		for (int i = 0; i < userInfos.length; i++)
		{
			chatRoom.memberUidArray.add(userInfos[i].uid);
		}
		chatRoom.latestTime = TimeManager.getInstance().getCurrentTime();
		MsgItem[] msgs2 = getDummyMsgs(userInfos, cuser);
		for (int i = 0; i < msgs2.length; i++)
		{
			msgs2[i].sendState = MsgItem.SEND_SUCCESS;
			chatRoom.msgList.add(msgs2[i]);
		}
		
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

	private UserInfo[] getDummyUsers()
	{
		UserInfo[] userInfos = {
			new UserInfo(5, 0, 0, 7, "131762465000002", "Ned", "Winterfell", "g045", TimeManager.getInstance().getCurrentTime(),1,TimeManager.getInstance().getCurrentTime()+24*60*60),
			new UserInfo(1, 0, 0, 0, "131762465000003", "Jemmy", "King`s Landing", "g008", TimeManager.getInstance().getCurrentTime(),1,TimeManager.getInstance().getCurrentTime()+24*60*60) ,
			new UserInfo(5, 0, 0, 1, "131762465000004", "Imp", "Casterly Rock", "g044", TimeManager.getInstance().getCurrentTime(),1,TimeManager.getInstance().getCurrentTime()+24*60*60) ,
			new UserInfo(11, 0, 0, 10, "131762465000005", "John Snow", "Winterfell", "g043", TimeManager.getInstance().getCurrentTime(),1,TimeManager.getInstance().getCurrentTime()+24*60*60) };
		return userInfos;
	}
	private MsgItem[] getDummyMsgs(UserInfo userInfos[], UserInfo cuser)
	{
		MsgItem[] msgs = {
				new MsgItem(1, true, false, 0, 100, userInfos[0].uid, "我要退出联盟", "","中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(2, true, true, 0, 3, cuser.uid, "In order to use the Software and related services on www.cok.com, or call the number 13825462145. You must first agree to this License Agreement. android@cok.com.", "", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(3, true, false, 0, 12, userInfos[1].uid, "快来拆红包1", "快来拆红包1", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(4, false, true, 0, 12, cuser.uid, "快来拆红包2", "快来拆红包2", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(5, true, false, 0, 12, userInfos[2].uid, "快来拆红包3", "快来拆红包3", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(6, false, true, 0, 7, cuser.uid, "3|134054|105392|[{\"name\":\"sfds|f|t\"}]", "3|王者之剑", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(7, true, true, 0, 12, cuser.uid, "快来拆红包4", "快来拆红包4", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(8, false, false, 0, 6, cuser.uid, "集合攻打此反倒是坐标200:341", "集合攻打此坐标0:341", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(9, true, false, 0, 13, userInfos[3].uid, "5", "8", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(10, false, true, 0, 0, cuser.uid, "6", "9", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(11, true, false, 0, 0, userInfos[3].uid, "dsfdsfewrwerfds", "发生的范德萨范德萨", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(12, false, true, 0, 0, cuser.uid, "玩儿玩儿玩儿", "发生的福尔沃特V大是", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(13, true, false, 0, 0, userInfos[3].uid, "威尔额外热温热温热污染️", "发生的服务而额外任务而", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(14, false, true, 0, 0, cuser.uid, "💪", "9", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(15, true, false, 0, 0, userInfos[3].uid, "😤", "8", "中文",TimeManager.getInstance().getCurrentTime()),
				new MsgItem(16, false, true, 0, 0, cuser.uid, "😳", "9", "中文",TimeManager.getInstance().getCurrentTime()) 
				
//				new MsgItem(1, true, false, 0, 100, userInfos[0].uid, "1", "","中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(2, true, true, 0, 6, cuser.uid, "2", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(3, true, false, 0, 12, userInfos[1].uid, "3", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(4, false, true, 0, 12, cuser.uid, "4", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(5, true, false, 0, 12, userInfos[2].uid, "5", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(6, false, true, 0, 13, cuser.uid, "6", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(7, true, true, 0, 12, cuser.uid, "7", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(8, false, false, 0, 6, cuser.uid, "8", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(9, true, false, 0, 13, userInfos[3].uid, "9", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(10, false, true, 0, 0, cuser.uid, "10", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(11, true, false, 0, 0, userInfos[3].uid, "11", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(12, false, true, 0, 0, cuser.uid, "12", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(13, true, false, 0, 0, userInfos[3].uid, "13️", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(14, false, true, 0, 0, cuser.uid, "14", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(15, true, false, 0, 0, userInfos[3].uid, "15", "", "中文",TimeManager.getInstance().getCurrentTime()),
//				new MsgItem(16, false, true, 0, 0, cuser.uid, "16", "", "中文",TimeManager.getInstance().getCurrentTime())
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

	@Override
	public void onConsoleOutput(String message)
	{
		refreshStatus(message);
	}

	public String getVersionName()
	{
		return getPackageInfo().versionName;
	}

	public int getVersionCode()
	{
		return getPackageInfo().versionCode;
	}
	
	public String getAppName()
	{
		PackageManager packageManager = getApplicationContext().getPackageManager();
		ApplicationInfo applicationInfo = null;
		try
		{
			applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
		}
		catch (NameNotFoundException e)
		{
			applicationInfo = null;
		}
		return (String) packageManager.getApplicationLabel(applicationInfo);
	}
	
	private PackageInfo getPackageInfo()
	{
		Context context = this;
		PackageInfo pi = null;
		try
		{
			PackageManager pm = context.getPackageManager();
			pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);

			return pi;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		return pi;
	}

	private void initRealDB()
	{
		ChatServiceController.init(this, true);
		initBaseInfo();
		copyDBFile();

//		DBManager.initDatabase(false, false);
		DBManager.getInstance().initDB(this);

		UserInfo me = UserManager.getInstance().getCurrentUser();
//		ServiceInterface.setPlayerInfo(me.serverId, TimeManager.getInstance().getCurrentTime(), me.mGmod, me.headPicVer, me.userName, me.uid, me.headPic, me.vipLevel, me.svipLevel, me.vipEndTime, me.lastUpdateTime, me.crossFightSrcServerId);
		ServiceInterface.setPlayerAllianceInfo(me.asn, me.allianceId, me.allianceRank, true);
		
//		LanguageConfiger.initLanguage(this);
		initAllianceMembers(UserManager.getInstance().getCurrentUser().allianceId); //需要语言包才能正确解析
		
		ServiceInterface.handleGetNewMailMsg(ChannelManager.getInstance().getSimulateReturnChannelInfo());
//		ChannelManager.deserialize();
	}

	private void initAllianceMembers(String allianceId)
	{
		if(StringUtils.isNotEmpty(allianceId)){
			ArrayList<UserInfo> members = DBManager.getInstance().getAllianceMembers(allianceId);
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
			ServiceInterface.notifyUserUids(uidStr, lastUpdateTimeStr, UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE);
		}
	}
	
	public final String dummyDBUser = USER_ZY532_Major;
	public static final String USER_ZY_INNER = "1380131787000001"; //内网包，个人邮件适量，系统邮件900
	public static final String USER_XU_INNER = "1380625871000001";
	public static final String USER_HUCHAO_532 = "909504798000489";
	public static final String USER_MAIL_TEMP = "51568477000000";
	public static final String USER_XUTEST_BETA = "1385363567000001";

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
}
