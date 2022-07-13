package com.elex.chatservice.net;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.util.FileVideoUtils;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.PlayVideoActivity;
import com.mi.milink.sdk.util.FileUtils;
import com.mi.mimsgsdk.AudioRecordListener;
import com.mi.mimsgsdk.IMXMsgCallback;
import com.mi.mimsgsdk.MsgSdkManager;
import com.mi.mimsgsdk.controller.MessageController;
import com.mi.mimsgsdk.message.AudioBody;
import com.mi.mimsgsdk.message.CustomBody;
import com.mi.mimsgsdk.message.MiMsgBody;
import com.mi.mimsgsdk.message.TextBody;
import com.mi.mimsgsdk.service.aidl.MiMessage;
import com.mi.mimsgsdk.service.aidl.RetValue;
import com.mi.mimsgsdk.video.VideoBody;
import com.xiaomi.channel.common.audio.MessageType;

public class XiaoMiToolManager {
	public static final String XIAO_MI_LOG_TAG = "xiaomi";
	public static final int TIME_OUT = 3000;
	private static XiaoMiToolManager  	instance;
	public static Activity currentActivity;
	private static PlayVideoActivity mPlayVideoActivity = null;
	private  Activity	currentRecordActivity = null;
	
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

	public static  String appId = "100000006";
	public static  String appKey = "p0LHhSJkHrIuHPtZ";
	public static   String pSkey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD+qNU/W2iWBi5APoJ9nOSgD1IFCI18OQ6ksWDqjWK0GIpOU0wapEa9cVKbbhDkGwpX5I5JEuHygPsAEMWLRF6zr9h5RqdOjISlaeAU4nwsd4dJRNHeHON5COkGo38Eu9PJSGzOed7sjCC7XxCI+E2N7hiaFRQlF2obHQch6Cnb9wIDAQAB";
	public static  String pId = "3";

	public static String gUid = "1387904183000001";//"1383239301000001";
	public static String b2Token = "0aNNwIy/TNO0MBw71xyd0Ow7Bw5qevpjXVyBNafrgbQ=";//"oMVc06fjGdBL1St4dngqsx4IgafDEtm0Cxt53VI+5j4=";

	public static final String gUid2 = "1393641546000001";
	public static final String b2Token2 = "wl5ESDLq/mtv6EL7FjoHAYCKQvJiwbpK2Sxe4GIWbEU=";
	
//	public static final String allianceGuid = "MI_193ed2dd61934512b3e4ad1e369d51d4";
	
	/**
	 * 语音保存的本地路径
	 */
	public String urlSaved = null;
	//保存到小米服务器这边的url
	public String urlServerVoice = null;
	public long mLength;
	Random rand = new Random();
	int randNum = rand.nextInt(3);
	private int msgId = randNum * 10000;

	private MsgSdkManager msgSdkManager = null;
	
    // mUrl前缀的只负责播放或者下载
	public String mUrlPath;
	public int mUrlLength;
	public String mUrlThumbPath;
    
    // 接收到上传成功的回调
	public String uploadFileUrl;
	public String uploadFileThumbUrl;
	public String uploadFileClocalVideoPath;//上传本地视频转化为3gp后的路径
	public String uploadFileClocalThumPath;//上传本地视频缩略图路径
	
	public String playVideoUrl;//从播放器来的视频路径
	public String playThumUrl;//从播放器来的缩略图路径
	
	public static final int SEND_TYPE_LOCAL = 0;
	public static final int SEND_TYPE_RECORD = 1;
	public int sendVideoType = 0;
	
	public int videoW = 480;
	public int videoH = 640;

	public VideoBody recordVideo = null;
	public boolean isRecordVoice = false;
	public boolean exitChat = false;
	public boolean sdkInitFail = false;
	
	public static void setPlayVideoActivity(PlayVideoActivity mPlayVideoActivity) {
		XiaoMiToolManager.mPlayVideoActivity = mPlayVideoActivity;
	}

	public static XiaoMiToolManager getInstance()
	{
		if (instance == null)
		{
			instance = new XiaoMiToolManager();
		}
		return instance;
	}

	private XiaoMiToolManager()
	{
		audioRecordListeners = new ArrayList<AudioRecordListener>();
	}
	
	public static void testActivity(Activity activity){
		getInstance().initSDK(activity,appId,appKey,pId,pSkey,gUid,b2Token);
	}
	
//	public static void initActivity(Activity activity,String appId, String appKey,String pid, String pkey,String guid, String b2token){
//		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "appId", appId, "appKey", appKey, "pid", pid, "pkey", pkey, "guid", guid, "b2token", b2token);
//		getInstance().initSDK(activity,appId,appKey,pid,pkey,guid,b2token);
//	}
	
	public void initSDK(Activity activity,String aId, String aKey,String publicId, String pkey,String guid, String b2token){
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "aId", aId, "aKey", aKey, "publicId", publicId, "pkey", pkey, "guid", guid, "b2token", b2token);
		pSkey = pkey;
		appId = aId;
		appKey = aKey;
		pId = publicId;
	
		gUid = guid;
		b2Token = b2token;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "appId", appId, "appKey", appKey, "pId", pId, "pSkey", pSkey, "gUid", gUid, "b2Token", b2Token);
		initSDK(activity);
	}
	
	public void initSDK(Activity activity)
	{
    	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN && activity!=null){
			Log.d(XIAO_MI_LOG_TAG, "xiaomi sdkManager init");
			if(msgSdkManager==null){
				currentActivity = activity;
				msgSdkManager = new MsgSdkManager(activity);
				msgSdkManager.audioMsgSdkInit(activity, audioManagerListener);
				initMiMsgSDK();
			}else if(sdkInitFail && msgSdkManager!=null){
				msgSdkManager.destroy();
				sdkInitFail = false;
				currentActivity = activity;
				msgSdkManager = new MsgSdkManager(activity);
				msgSdkManager.audioMsgSdkInit(activity, audioManagerListener);
				initMiMsgSDK();
			}
			Log.d(XIAO_MI_LOG_TAG, "xiaomi sdkManager init finish");
		}
	}
	
	/**
	 * 消息回调函数
	 */

	private IMXMsgCallback mMessageListener = new IMXMsgCallback() {

		@Override
		public boolean onReceiveOldUserMessage(List<MiMessage> arg0) {
			return false;
		}
		
		@Override
		public boolean onReceiveOldGroupMessage(String arg0, List<MiMessage> arg1) {
			return false;
		}
		
		@Override
		public boolean onReceiveMessage(final int arg0, final MiMessage arg1) {
			Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "tuning test parseMessage ,MainActivity callback");
			updateGetMessage(arg0, arg1);
			return true;
		}
		
		@Override
		public void onInitResult(RetValue arg0) {
			String str = "retcode: " + arg0.retCode + " retmsg: " + arg0.retMsg;
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "currentActivity", currentActivity, "xiao mi sdk init ok " + str);
			updateUI(str);
			String vedioLog = "xiaoMi_onInitResult_"+ arg0.toString();
			//postVideoLogToServer(vedioLog);
			final int code = arg0.retCode;
			if(code==0){
				sdkInitFail = false;
				if(currentActivity!=null){
					currentActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
						        JniController.getInstance().excuteJNIVoidMethod("xiaomiSDKInit",
						                new Object[] {code});
							}
					});
				}
			}else{
				sdkInitFail = true;
				//postVideoLogToServer("xiaoMi_sdkInitFail");
			}
		}
		
		@Override
		public void onDataSendResponse(int arg0, RetValue arg1, MiMessage arg2) {
			if(arg0==MessageController.BODY_TYPE_VIDEO){
				//String vedioLog = "xiaoMi_onDataSendResponse_"+ arg1.toString();
				//postVideoLogToServer(vedioLog);
			}
		}
		
		@Override
		public void onConnectionStateChanged(int arg0) {
			String vedioLog = "xiaoMi_onConnectionStateChanged_"+ arg0;
			//postVideoLogToServer(vedioLog);
		}

		@SuppressLint("NewApi")
		@Override
		public void onDownloadMediaFileResponse(final int messageType, RetValue arg0) {
			updateUI("downloadVideo result retCode=" + arg0.retCode + " retMsg=" + arg0.retMsg);
			final String mLocalPath = arg0.retMsg;
			String vedioLog = "xiaoMi_onDownloadMediaFileResponse_"+ arg0.toString();
			//postVideoLogToServer(vedioLog);
			if (arg0.retCode == 0) {
				if (messageType == MessageType.VIDEO) {
					if(mPlayVideoActivity!=null){
						mPlayVideoActivity.playVideo(mLocalPath);
						saveKeyAndValueToGame(playVideoUrl,mLocalPath);
						return;
					}
				} else if (messageType == MessageType.IMAGE) {
					if(mPlayVideoActivity!=null){
						mPlayVideoActivity.showThumb(mLocalPath);
						saveKeyAndValueToGame(playThumUrl,mLocalPath);
						return;
					}
				}
				if(currentActivity!=null){
					currentActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "xiaomi DownloadMediaFile success");
							JniController.getInstance().excuteJNIVoidMethod("saveMediaFileLocalURL",
									new Object[] { messageType,mLocalPath});
						}
					});
				}
			}
		}

		public void saveKeyAndValueToGame(final String keyStr,final String valueStr){
			if(currentActivity!=null){
				currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "xiaomi saveKeyAndValueToGame success");
						JniController.getInstance().excuteJNIVoidMethod("saveKeyAndValueToGame",
								new Object[] { keyStr,valueStr});
					}
				});
			}
		}
		
		@Override
		public void onUploadVideoResponse(RetValue arg0, final VideoBody arg1) {
			updateUI("uploadVideo Response result retCode=" + arg0.retCode + " retMsg=" + arg0.retMsg + " url=" + arg1.getUrl());
			String vedioLog = "xiaoMi_onUploadVideoResponse_"+ arg0.toString();
			//postVideoLogToServer(vedioLog);
			if (arg0.retCode == 0) {
				uploadFileUrl = arg1.getUrl();
				uploadFileThumbUrl = arg1.getThumbnailUrl();
				callGameSaveVideoInfo(arg1,sendVideoType);
			}
		}

		@Override
		public void onUploadLogsResponse(RetValue arg0)
		{
		}
	};

    private void updateGetMessage(final int channelId, final MiMessage message ) {
    	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "channelId", channelId, "message", message);
    	final MiMsgBody receivedMsg = message.body;
		String channelInfomation = "";
		switch (channelId) {
		case MessageController.CHANNEL_USER:
			channelInfomation = "user";
			break;
		case MessageController.CHANNEL_ROOM:
			channelInfomation = "room";
			break;
		case MessageController.CHANNEL_GROUP:
			channelInfomation = "group";
			break;
		default:
			Log.w(XiaoMiToolManager.XIAO_MI_LOG_TAG, "your sdk verson is not new ,please download the new one");
			break;
		}
		if (receivedMsg == null) {
			Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "receivedMsg is null");
			return;
		}
		Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "onReceiveGameMessage,channel =  " + channelInfomation +  ",value : " + receivedMsg.toString());

		String prex = "receive message from:" + message.from + " to:" + message.to;
		switch (message.bodyType) {
		case MessageController.BODY_TYPE_CUSTOM:// 默认,传输的普通的二进制， 传入是怎么数据，接收到就怎么解析
			updateUI(prex + "  custom body size "	+ ((CustomBody) receivedMsg).getData().length);
			break;
		case MessageController.BODY_TYPE_TEXT:// 普通文本
			TextBody mTextBody = (TextBody) receivedMsg;
			try {
				updateUI(prex + " text body " + mTextBody.getText()
						+ ";extra data = "
						+ new String(mTextBody.getContent(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;
		case MessageController.BODY_TYPE_AUDIO:// 语音消息
			updateUIByAudioMessage(prex, receivedMsg, message.from);
			break;
		case MessageController.BODY_TYPE_VIDEO: // 视频消息
			String vedioLog = "xiaoMi_onReceiveMessage_"+ message.toString();
			//postVideoLogToServer(vedioLog);
			VideoBody mVideoBody;
			if (null != receivedMsg && receivedMsg instanceof VideoBody) {
				mVideoBody = (VideoBody) receivedMsg;
			} else {
				Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "receive a old message ,please update your sdk version");
				return;
			}
			mUrlPath = mVideoBody.getUrl();
			mUrlLength = mVideoBody.getLength();
			uploadFileThumbUrl = mVideoBody.getThumbnailUrl();
//			if(sendVideoType==SEND_TYPE_RECORD){
//				callGameSaveVideoInfo(mVideoBody,sendVideoType);
//				sendVideoType = SEND_TYPE_LOCAL;
//			}
			byte[] videoExtraData = mVideoBody.getContent();
			try {
				updateUI(prex + " video body url=" + mUrlPath + " length="
						+ mVideoBody.getLength() + " size=" + mVideoBody.getSize() + " thumbPath " + mVideoBody.getThumbnailUrl() + " video extra "
						+ new String(videoExtraData, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "receive a video message, url= " + mUrlPath);
			break;
		default:
			Log.w(XiaoMiToolManager.XIAO_MI_LOG_TAG, "your sdk verson is not new ,please download the new one");
			break;
		}
    }
    
    private void updateUI(final String str) {
    	Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, str);
    }
	/**
	 * 语音录制回调
	 */
	AudioRecordListener audioManagerListener = new AudioRecordListener() {
		@Override
		public void onRecordStart() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onRecordStart");
			for (Iterator<AudioRecordListener> iterator = audioRecordListeners.iterator(); iterator.hasNext();)
			{
				AudioRecordListener audioRecordListener = (AudioRecordListener) iterator.next();
				if (audioRecordListener != null)
				{
					audioRecordListener.onRecordStart();
				}
			}
		}

		@Override
		public void onRecordInitializationSucceed() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onRecordInitializationSucceed");
		}

		@Override
		public void onRecordInitializationFailed() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onRecordInitializationFailed");
		}

		@Override
		public void onRecordInitializationCancelled() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onRecordInitializationCancelled");
		}

		@Override
		public void onRecordFinished(String localPath, long length) {
			String tips = "record finish, local path is " + localPath
					+ " length is " + length + "(" + TimeManager.getAudioLength(length) + ")";
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "sendCurrentRecord", sendCurrentRecord, tips);
			isRecordVoice = false;
			updateTvShow(tips);
			urlSaved = localPath;
			mLength = length;
			if(sendCurrentRecord)
			{
				if(mLength>=1800)
				{
					int sendLocalTime = TimeManager.getInstance().getCurrentTime();
					ChatServiceController.sendDummyAudioMsg(length,sendLocalTime);
					sendAudio(sendLocalTime);
				}
				else
				{
					MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_TOO_SHORT));
				}
			}
			sendCurrentRecord = false;
			//mSendAudio.setClickable(true);
		}

		@Override
		public void onRecordFailed() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onRecordFailed");
		}

		@Override
		public void onEndingRecord() {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onEndingRecord");
			if(!isStopRecordByUser){
				sendCurrentRecord = true;
				// 识别由sdk触发的终止，如录音超出60s
				for (Iterator<AudioRecordListener> iterator = audioRecordListeners.iterator(); iterator.hasNext();)
				{
					AudioRecordListener audioRecordListener = (AudioRecordListener) iterator.next();
					if (audioRecordListener != null)
					{
						audioRecordListener.onEndingRecord();
					}
				}
			}
		}

		@Override
		public void onPlayEnd(String arg0, boolean arg1) {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onPlayEnd", arg0, arg1);
			for (Iterator<AudioRecordListener> iterator = audioRecordListeners.iterator(); iterator.hasNext();)
			{
				AudioRecordListener audioRecordListener = (AudioRecordListener) iterator.next();
				if (audioRecordListener != null)
				{
					audioRecordListener.onPlayEnd(arg0, arg1);
				}
			}
		}

		@Override
		public void onPlayBegin(String arg0) {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "onPlayBegin", arg0);
			for (Iterator<AudioRecordListener> iterator = audioRecordListeners.iterator(); iterator.hasNext();)
			{
				AudioRecordListener audioRecordListener = (AudioRecordListener) iterator.next();
				if (audioRecordListener != null)
				{
					audioRecordListener.onPlayBegin(arg0);
				}
			}
		}

		@Override
		public void onRmsChanged(int arg0)
		{
		}

		@Override
		public void onAudioCoderInitializationFailed()
		{
		}
	};

	/**
	 * 初始化SDK回调控件,此事件为耗时操作，请用异步线程处理，或者如果需要同步则需要等待 界面
	 */
	public void initMiMsgSDK() {
    	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM);
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected Void doInBackground(Void... params) {
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "appId", appId, "appKey", appKey, "pId", pId, "pSkey", pSkey, "gUid", gUid, "b2Token", b2Token);
				msgSdkManager.init(gUid, pSkey, appId, b2Token, pId,
						mMessageListener);
				refreshSpeakerphoneState();
				return null;
			}
		};
		task.execute(null, null, null);
	}

	/**
	 * 更新收到的语音信息到界面
	 * 
	 * @param receivedMsg
	 */
	public void updateUIByAudioMessage(final String prex,
			final MiMsgBody receivedMsg, final String fromGuid) {
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);
//		Log.d(XIAO_MI_LOG_TAG, "xiaomi updateUIByAudioMessage");
		AudioBody mAudioBody = null;
		if (null != receivedMsg && receivedMsg instanceof AudioBody) {
			mAudioBody = (AudioBody) receivedMsg;
		} else {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "receive a old message ,please update your sdk version");
//			Log.d(XIAO_MI_LOG_TAG,"receive a old message ,please update your sdk version");
		}
		if(mAudioBody!=null){
			final String url = mAudioBody.getUrl();
			byte[] extraData = mAudioBody.getContent();
			String extraStr = "";
			try {
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG,
						prex + " audio body url " + url + " length " + mAudioBody.getLength() + ",audio extra =:"
								+ new String(extraData, "UTF-8"));
				extraStr = new String(extraData, "UTF-8");
				updateTvShow(prex + " audio body url " + url + " length " + mAudioBody.getLength() + ",audio extra :"
						+ extraStr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "xiaomi receive an audio message, url= " + url);
//			Log.d(XIAO_MI_LOG_TAG, "xiaomi receive an audio message, url= " + url);
			
			urlSaved = url;
			urlServerVoice = url;
			mLength = mAudioBody.getLength();
			
			final String sendLocalTime = extraStr;
			
			if(fromGuid.equals(gUid))
			{
				if (ChatServiceController.hostActivity != null) {
					ChatServiceController.hostActivity.runOnUiThread(new Runnable()
    				{
    					@Override
    					public void run()
    					{
    						ChatServiceController.sendAudioMsgToServer(url,sendLocalTime);
//							ChatServiceController.sendMsg(mLength + "", false, false, url);
    					}
    				});
                }
			}
		}
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
	//开始录语音
	public void startRecord() {
		if(msgSdkManager!=null){
			isStopRecordByUser = false;
			isRecordVoice = true;
			stopPlayVoice();
			ChatServiceController.getInstance().setGameMusiceEnable(false);
			msgSdkManager.startRecord();
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	private boolean sendCurrentRecord = false;
	private boolean isStopRecordByUser = false;
	//停止录语音
	public void stopRecord(boolean send) {
		isStopRecordByUser = true;
		sendCurrentRecord = send;
		if(msgSdkManager!=null){
			isRecordVoice = false;
			ChatServiceController.getInstance().setGameMusiceEnable(true);
			msgSdkManager.stopRecord();
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	//播放语音
	public void playVoice() {
		if(msgSdkManager!=null){
			msgSdkManager.playVoiceWithUrl(urlSaved);
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	public void playVoice(String url) {
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "url", url);
		if(msgSdkManager!=null && StringUtils.isNotEmpty(url)){
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "url2", url);
			msgSdkManager.playVoiceWithUrl(url);
		}
	}
	//停止播放语音
	public void stopPlayVoice() {
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);
		if(msgSdkManager!=null){
			msgSdkManager.stopPlayVoice();
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	//发送语音
	public void sendAudio(int sendLocalTime) {
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);
		if(msgSdkManager!=null){
			if (!TextUtils.isEmpty(urlSaved) && mLength != 0) {
				try
				{
					AudioBody mAudioBody = new AudioBody();
					mAudioBody.setLength(mLength);
					mAudioBody.setUrl(urlSaved);
					mAudioBody.setContent(Integer.toString(sendLocalTime).getBytes());
					MiMessage message = getMessage(gUid, "MI_" + UserManager.getInstance().getCurrentUser().allianceId, mAudioBody,
							msgId++, BODY_TYPE_AUDIO);
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "send message from: " + message.from + " to: "
							+ message.to + " message type audio, url " + urlSaved + " length " + mLength);
					LogUtil.trackPageView("Audio-send2xm");
					msgSdkManager.sendMessage(CHANNEL_USER, message, TIME_OUT);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			} else {
				Log.d(XIAO_MI_LOG_TAG,"no record audio");
			}
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	public void downloadMediaFile(int type,String url) {
		if(msgSdkManager!=null){
			if(StringUtils.isEmpty(url)){
				Log.d(XIAO_MI_LOG_TAG,"xiaomi downloadMediaFile is null");
				return ;
			}
			msgSdkManager.downloadMediaFile(type, url);
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	public void sendMessage(int type,MiMessage message,int timeout) {
		if(msgSdkManager!=null){
			msgSdkManager.sendMessage(type, message, timeout);
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	public void uploadVideo(VideoBody vBody) {
		if(msgSdkManager!=null){
			uploadFileClocalVideoPath = vBody.getUrl();
			uploadFileClocalThumPath = vBody.getThumbnailUrl();
			msgSdkManager.uploadVideo(vBody);
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	public void sendRecordVideo() {
		if(recordVideo==null){
			return;
		}
		int videoSize = recordVideo.getSize();
        if(videoSize > 50*1024){
            if(currentActivity!=null){
                currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(XIAO_MI_LOG_TAG, "upload video alert");
                            JniController.getInstance().excuteJNIVoidMethod("uploadVideoTooLarge",
                                    new Object[] {});
                            
                        }
                });
            }
            return ;
        }
		sendVideoType = SEND_TYPE_RECORD;
		uploadVideo(recordVideo);
		if(currentActivity!=null){
			currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JniController.getInstance().excuteJNIVoidMethod("loadingRecordVideo",
				                new Object[] {});
					}
			});
		}
	}
	
	public void uploadLocalVideoToXiaoMi(String localUrl) {
		if(currentActivity==null){
			return ;
		}
		currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				JniController.getInstance().excuteJNIVoidMethod("addEventToFaceBook",
		                new Object[] {"sendUploadVideo"});
			}
		});
		if(msgSdkManager!=null){
	  		String fileName = System.currentTimeMillis() + "";
	  		String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	  		uploadFileClocalVideoPath = storageDir + "/mivtalk/video/v_" + fileName + ".3gp";
	  		String storageImgDir = storageDir + "/mivtalk/images/";
	  		Log.d(XIAO_MI_LOG_TAG, "storageVideo="+uploadFileClocalVideoPath+"  storageImgDir="+storageImgDir);
	  		sendVideoType = SEND_TYPE_LOCAL;
			File oldfile = new File(localUrl);
			int videolength = (int) (oldfile.length()/1024 +10);
			if(videolength > 10*1024){
				if(currentActivity!=null){
					currentActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Log.d(XIAO_MI_LOG_TAG, "upload video alert");
								JniController.getInstance().excuteJNIVoidMethod("uploadVideoTooLarge",
						                new Object[] {});
								
							}
					});
				}
				return ;
			}
			File newfile = new File(uploadFileClocalVideoPath);  
			FileUtils.copyFile(oldfile, newfile);
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uploadFileClocalVideoPath, Images.Thumbnails.MINI_KIND);
			if(FileVideoUtils.checkSDCardAvailable()){
		        videoW = bitmap.getWidth();
		        videoH = bitmap.getHeight();
				uploadFileClocalThumPath = FileVideoUtils.saveJpegToSDCard(bitmap,storageImgDir,"img_"+fileName);
			}else{
				currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JniController.getInstance().excuteJNIVoidMethod("addEventToFaceBook",
				                new Object[] {"uploadVideoNeedSDCardRight"});
					}
				});
				return ;
			}
			Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "iconPath="+uploadFileClocalThumPath+"  videolength="+videolength);
			VideoBody tempVideo = new VideoBody();
	  		tempVideo.setLength(1000*60*15);
	  		tempVideo.setUrl(uploadFileClocalVideoPath);
	  		tempVideo.setSize(videolength);
	  		tempVideo.setThumbnailUrl(uploadFileClocalThumPath);
	  		uploadVideo(tempVideo);
			if(currentActivity!=null){
				currentActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							JniController.getInstance().excuteJNIVoidMethod("loadingRecordVideo",
					                new Object[] {});
						}
				});
			}
		}else{
			Log.d(XIAO_MI_LOG_TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	public void callGameSaveVideoInfo(final VideoBody video,final int sVideoType){
		if(currentActivity!=null){
			currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(video!=null){
							String uploadUrl = video.getUrl();
							String uploadThumbUrl = video.getThumbnailUrl();
					        String tempVideoURL = uploadUrl + ";" + uploadFileClocalVideoPath;
					        String tempImgURL = uploadThumbUrl +";" + uploadFileClocalThumPath;
					        if(sVideoType==SEND_TYPE_RECORD){
					        	videoW = 480;
					        	videoH = 640;
					        }else{
					        	if(!StringUtils.isEmpty(uploadFileClocalVideoPath)){
							        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uploadFileClocalVideoPath, Images.Thumbnails.MINI_KIND);
							        videoW = bitmap.getWidth();
							        videoH = bitmap.getHeight();
							        if(videoW>480){
							        	float tempScale = (float) (480.0/videoW);
							        	if(videoH<=0){
							        		videoH = 640;
							        	}
							        	videoH = (int) (tempScale*videoH);
							        	videoW = 480;
							        }
							        if(videoW<240){
							        	if(videoW<=0){
							        		videoW =240;
							        	}
							        	if(videoH<=0){
							        		videoH =320;
							        	}
							        	float tempScale = (float) (240.0/videoW);
							        	videoH = (int) (tempScale*videoH);
							        	videoW = 240;
							        }	
					        	}else{
						        	videoW = 480;
						        	videoH = 640;
					        	}
					        }

					        int videoSize = video.getSize();
					        int videoLength = video.getLength();
					        String tipInfo = "tempVideoURL="+tempVideoURL+" tempImgURL="+tempImgURL + "videoW="+videoW + " videoH="+videoH+" videoSize="+videoSize+"videoLength="+videoLength;
					        Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "call to C++ save upload info="+tipInfo);
					        JniController.getInstance().excuteJNIVoidMethod("saveVideoAndThumbnailURL",
					                new Object[] { tempVideoURL,tempImgURL,sVideoType,videoW,videoH,videoSize,videoLength});
						}else{
							Log.d(XIAO_MI_LOG_TAG, "call video is null");
						}
					}
			});
		}
	}
	
	private void updateTvShow(final String tips) {
		Log.d(XIAO_MI_LOG_TAG, "updateTvShow"+tips);
//		runOnUiThread(new Runnable() {
//			public void run() {
//				//mPushTextView.append(tips + "\n");
//			}
//		});
	}

	private ArrayList<AudioRecordListener> audioRecordListeners;
	public void addAudioListener(AudioRecordListener audioRecordListener)
	{
		if (audioRecordListener != null && !audioRecordListeners.contains(audioRecordListener))
		{
			audioRecordListeners.add(audioRecordListener);
		}
	}
	public void removeAudioListener(AudioRecordListener audioRecordListener)
	{
		if (audioRecordListener != null && audioRecordListeners.contains(audioRecordListener))
		{
			audioRecordListeners.remove(audioRecordListener);
		}
	}
	
	public Activity getCurrentRecordActivity() {
		return currentRecordActivity;
	}

	public void setCurrentRecordActivity(Activity currentRecordActivity) {
		this.currentRecordActivity = currentRecordActivity;
	}
	
	/**
	 * 设置播放方式
	 * 
	 * @param isOn true用扬声器，false用听筒
	 */
	public void refreshSpeakerphoneState()
	{
		if(msgSdkManager!=null)
			msgSdkManager.setSpeakerphoneOn(ConfigManager.playAudioBySpeaker);
	}
	
	public void postVideoLogToServer(final String VideoLog){
		if(currentActivity!=null){
			currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JniController.getInstance().excuteJNIVoidMethod("postVideoLogToServer",
				                new Object[] {VideoLog});
					}
			});
		}
	}
}
