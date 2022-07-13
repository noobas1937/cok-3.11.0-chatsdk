package com.elex.chatservice.view;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

import com.elex.chatservice.R;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.mi.mimsgsdk.AudioRecordListener;
import com.mi.mimsgsdk.ConnectionStatus;
import com.mi.mimsgsdk.IMXMsgCallback;
import com.mi.mimsgsdk.MsgSdkManager;
import com.mi.mimsgsdk.message.AudioBody;
import com.mi.mimsgsdk.message.CustomBody;
import com.mi.mimsgsdk.message.MiMsgBody;
import com.mi.mimsgsdk.message.TextBody;
import com.mi.mimsgsdk.service.aidl.IMessageListener;
import com.mi.mimsgsdk.service.aidl.MiMessage;
import com.mi.mimsgsdk.service.aidl.RetValue;
import com.mi.mimsgsdk.video.VideoBody;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MsgActivity extends Activity {
	private String TAG = MsgActivity.class.getSimpleName();
	// 用户7测试账号
	public static final String gUid7 = "android_test_guid7";
	public static final String b2Token7 = "9Yig8vishKukzmals9QCqy7VCMJw3xMbRvmWGB5KmVU=";
		
	// 用户8测试账号
	public static final String gUid8 = "android_test_guid8";
	public static final String b2Token8 = "ZmvAzDjNsF+OfPlAMdzBmydfLiFg/lO0hvGrtWQgLFs=";
	public static final String CORE_PROCESS_NAME = "com.mi.msg";
	
	public static final int appId = 100000000;
	public static final  String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDH6Tvnsh5NM6WBzRqkHS5pyijmQ/W5LaL41CS7UGFNZlsl7/dke9Rt8tErcjzydbQ+fbXMD8dw36yIV64Q7CSkWr/qmy69/wBuijWWX4evFe557y5xm8GjhAPu4Yjz8TidqbI2H2EzSEjFltmSx2gpxEts//ifjLcMKhR43HSIKwIDAQAB";
	public static final String pId = "1";
	public static final String gRid = "androidTestRoom";
	public static final String gGid = "androidTestGroup";
	/**
	 * 文本发送按钮
	 */
	private EditText mEditText;
	private Button mSenderButton;
	private Button mSendRoomButton;
	private Button mSendGroupButton;
	private TextView mPushTextView;

	/**
	 * 语音录制按钮
	 */
	private Button mSendAudio;
	private Button mRecord;
	private Button mStopRecord;
	private Button mPlay;

	/**
	 * 点击事件定义
	 */
	private static final int ACTION_CLICK_SENDER = 100;
	private static final int ACTION_CLICK_SENDER_ROOM = 101;
	private static final int ACTION_CLICk_SENDER_GROUP = 102;

	/**
	 * 消息来源渠道，即单聊，群组聊天，或房间
	 */
	public static final int CHANNEL_USER = 1; // 单聊
	public static final int CHANNEL_ROOM = 2; // 房间
	public static final int CHANNEL_GROUP = 3; // 群组

	/**
	 * bodyType
	 */
	public static final int BODY_TYPE_CUSTOM = 0;
	public static final int BODY_TYPE_TEXT = 1;
	public static final int BODY_TYPE_AUDIO = 2;

	/**
	 * 临时变量定义
	 */
	private String mLocalPath;
	private long mLocalLength;
	private String mUrlPath;
	private long mUrlLength;
	
	Random rand = new Random();
	int randNum = rand.nextInt(3);
	private int msgId = randNum * 10000; // 生成migId
	private int TIME_OUT = 3000;

	private String currentUser;
	private String targetUser;

	/**
	 * 消息回调函数
	 */

	private IMXMsgCallback mMessageListener = new IMXMsgCallback() {

		@Override
		public boolean onReceiveOldUserMessage(List<MiMessage> arg0) {
			Log.d(TAG, "onReceiveOldUserGameMessage size = " + arg0.size());
			return false;
		}

		@Override
		public boolean onReceiveOldGroupMessage(String arg0,
				List<MiMessage> arg1) {
			Log.d(TAG, "onReceiveOldGroupGameMessage size = " + arg1.size());
			return false;
		}

		@Override
		public boolean onReceiveMessage(int channel, MiMessage arg0) {
//			ClientLog.d(TAG, "tuning test parseMessage ,MainActivity callback");
			final MiMsgBody receivedMsg = arg0.body;
			String channelInfomation = "";
			switch (channel) {
			case CHANNEL_USER:
				channelInfomation = "user";
				break;
			case CHANNEL_ROOM:
				channelInfomation = "room";
				break;
			case CHANNEL_GROUP:
				channelInfomation = "group";
				break;
			default:
				Log.w(TAG,
						"your sdk verson is not new ,please download the new one");
				break;
			}
			Log.d(TAG, "onReceiveGameMessage,channel =  " + channelInfomation
					+ ",value : " + receivedMsg.toString());

			if (null != mPushTextView) {
				String prex = "receive message from:" + arg0.from + " to:"
						+ arg0.to;
				switch (arg0.bodyType) {
				case 0:// 默认,传输的普通的二进制， 传入是怎么数据，接收到就怎么解析
					updateTvShow(prex + "  custom body size "
							+ ((CustomBody) receivedMsg).getData().length);
					break;
				case 1:// 普通文本
					TextBody mTextBody = (TextBody) receivedMsg;
					try {
						updateTvShow(prex + " text body " + mTextBody.getText()
								+ ";extra data = "
								+ new String(mTextBody.getContent(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case 2:// 语音消息
					updateUIByAudioMessage(prex, receivedMsg);
					break;
				default:
					Log.w(TAG, "your sdk verson is not new ,please download the new one");
					break;
				}
			}
			return false;
		}

		@Override
		public void onInitResult(RetValue arg0) {
			Log.d(TAG, "onInitResult value : " + arg0.retMsg);
			final String str = arg0.retMsg;
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mPushTextView.append("onInitResult value: " + str + "\n");
				}
			});
			
		}

		@Override
		public void onDataSendResponse(int channel, RetValue arg0,
				MiMessage arg1) {
			Log.d(TAG, "onDataSendResponse  message send result  : "
					+ arg0.retCode + ",from channel = " + channel);
		}

		@Override
		public void onConnectionStateChanged(int arg0) {
			Log.d(TAG, "onConnectionStateChanged  value  : " + arg0);
			final String str = String.valueOf(arg0);
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					mPushTextView.append("onConnectionStateChanged value " + str + "\n");
				}
			});
			
		}

		@Override
		public void onUploadVideoResponse(RetValue arg0, VideoBody arg1) {
		}

		@Override
		public void onDownloadMediaFileResponse(int arg0, RetValue arg1) {
		}

		@Override
		public void onUploadLogsResponse(RetValue arg0)
		{
		}
	};

	/**
	 * 界面录制按钮点击事件响应
	 */
	OnClickListener mAudioListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.record_audio){
				XiaoMiToolManager.getInstance().startRecord();
			}else if(v.getId()==R.id.stop_record){
				XiaoMiToolManager.getInstance().stopRecord(false);
			}else if(v.getId()==R.id.play){
				XiaoMiToolManager.getInstance().playVoice();
			}else if(v.getId()==R.id.send_audio){
				XiaoMiToolManager.getInstance().sendAudio(TimeManager.getInstance().getCurrentTime());
			}
		}
	};

	/**
	 * 界面发送文本消息点击事件
	 */
	private View.OnClickListener mOnclickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (null != v) {
				int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
				switch (viewAction) {
				case ACTION_CLICK_SENDER: {
					if (null != mEditText) {

					}
				}
					break;

				case ACTION_CLICK_SENDER_ROOM: {
					if (null != mEditText) {

					}
				}
					break;

				case ACTION_CLICk_SENDER_GROUP: {
					if (null != mEditText) {

					}
				}
					break;

				default:
					break;
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg);
		XiaoMiToolManager.testActivity(this);
		mEditText = (EditText) findViewById(R.id.enter_noti);
		mSenderButton = (Button) findViewById(R.id.send);
		mPushTextView = (TextView) findViewById(R.id.pushTextView);
		mPushTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		mSenderButton.setOnClickListener(mOnclickListener);
		mSenderButton.setTag(ACTION_CLICK_SENDER);
		mSendRoomButton = (Button) findViewById(R.id.send_room);
		mSendGroupButton = (Button) findViewById(R.id.send_group);
		mSendRoomButton.setOnClickListener(mOnclickListener);
		mSendGroupButton.setOnClickListener(mOnclickListener);
		mSendRoomButton.setTag(ACTION_CLICK_SENDER_ROOM);
		mSendGroupButton.setTag(ACTION_CLICk_SENDER_GROUP);
		
		currentUser = gUid7;
		
		TextView msgCurrentUser = (TextView) findViewById(R.id.msg_current_user);
		msgCurrentUser.setText("当前用户: " + currentUser);
		
		//msgSdkManager = new MsgSdkManager(getApplicationContext());
		initAudioComp();
		//initMiMsgSDK();
		// 隐藏键盘
		mEditText.clearFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(),
				0);
		
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.choose_user_group);
		targetUser = getTargetUser(radioGroup.getCheckedRadioButtonId());
		Log.d(TAG, "targetUser is " + targetUser);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				targetUser = getTargetUser(checkedId);
				Log.d(TAG, "onCheckedChanged targetUser is " + targetUser);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initAudioComp() {

		mSendAudio = (Button) findViewById(R.id.send_audio);
		mSendAudio.setOnClickListener(mAudioListener);
		mRecord = (Button) findViewById(R.id.record_audio);
		mRecord.setOnClickListener(mAudioListener);
		mStopRecord = (Button) findViewById(R.id.stop_record);
		mStopRecord.setOnClickListener(mAudioListener);
		mPlay = (Button) findViewById(R.id.play);
		mPlay.setOnClickListener(mAudioListener);
	}

	/**
	 * 更新收到的语音信息到界面
	 * 
	 * @param receivedMsg
	 */
	public void updateUIByAudioMessage(final String prex,
			final MiMsgBody receivedMsg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AudioBody mAudioBody = null;
				if (null != receivedMsg && receivedMsg instanceof AudioBody) {
					mAudioBody = (AudioBody) receivedMsg;
				} else {
					Log.d(TAG, "receive a old message ,please update your sdk version");
				}

				if(mAudioBody!=null)
				{
					final String url = mAudioBody.getUrl();
					byte[] extraData = mAudioBody.getContent();
					try {
						updateTvShow(prex + " audio body url " + url + " length "
								+ mAudioBody.getLength() + ",audio extra =:"
								+ new String(extraData, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d(TAG, "receive an audio message, url= " + url);
					mUrlPath = url;
					mUrlLength = mAudioBody.getLength();
				}
			}
		});
	}

	public MiMessage getMessage(String fromId, String toId, MiMsgBody body,
			long msgId, int bodyType) {
		MiMessage mMiMessage = new MiMessage();
		mMiMessage.from = fromId;
		mMiMessage.to = toId;
		mMiMessage.body = body;
		mMiMessage.msgId = msgId;
		mMiMessage.bodyType = bodyType;
		mMiMessage.sendTime = (int) (System.currentTimeMillis() / 1000);
		return mMiMessage;
	}

	private void updateTvShow(final String tips) {
		runOnUiThread(new Runnable() {
			public void run() {
				mPushTextView.append(tips + "\n");
			}
		});
	}
	
	private String getTargetUser(int targetId) {
		return gUid7;
	}
}
