package com.elex.chatservice.model.kurento;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.HeadPicUtil.MD5;
import com.elex.chatservice.view.kurento.VoiceFloatBall;
import com.elex.chatservice.view.kurento.VoiceFloatBallMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by xupengzhan on 2016/12/2.
 */

public class WebRtcPeerManager {
	
	private static final String WEB_RTC_ROOM_JOIN_URL = "http://api.cok.chat/voip/join";
	private static final String WEB_RTC_ROOM_LEAVE_URL = "http://api.cok.chat/voip/leave";

    private static WebRtcPeerManager mInstance;
    private Map<String,PeerUserInfo> peerMap;
    private Map<String, Boolean> peerPublishStatusMap = new HashMap<String, Boolean>();
    private List<String> speakerList;
    private List<String> allList;
    
    public static boolean published = false;
    private WindowManager windowManager ;
    private VoiceFloatBall mFloatBall;
    private boolean isGettingWebRtcUrl = false;
    public static String webRtcUrl = "";
    
    public boolean isFloatBallShowed()
    {
    	return mFloatBall!=null && mFloatBall.isFloatBallShowed();
    }
    
    public boolean canControllerRole()
    {
    	return UserManager.getInstance().getCurrentUser()!=null && UserManager.getInstance().getCurrentUser().allianceRank >=4;
    }
    
    
    
    public void destoryVoiceFloatWindow()
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
    	if(!ChatServiceController.realtime_voice_enable)
    		return;
    	if(mFloatBall!=null)
    	{
    		mFloatBall.dismiss();
    		mFloatBall = null;
    	}
    }
    
    public void hideVoiceFloatWindow()
    {
    	
    	if(!ChatServiceController.realtime_voice_enable)
    		return;
    	if(ChatServiceController.hostActivity!=null)
    	{
    		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(mFloatBall!=null && mFloatBall.getVisibility()!=View.GONE)
					{
						LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
						mFloatBall.setVisibility(View.GONE);
					}
				}
			});
    	}
    }
    
    public void showRealtimeTip(final String tip)
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"tip",tip);
    	if(!ChatServiceController.realtime_voice_enable)
    		return;
    	if(ChatServiceController.hostActivity!=null)
    	{
    		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(mFloatBall!=null && mFloatBall.getVisibility()==View.VISIBLE)
			    		mFloatBall.showMenu(tip);
				}
			});
    	}
    }
    
    public void showVoiceFloatWindow()
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
    	if(!ChatServiceController.realtime_voice_enable)
    		return;
    	
    	if(ChatServiceController.hostActivity!=null)
    	{
    		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(mFloatBall!=null && mFloatBall.getVisibility()!=View.VISIBLE)
			    		mFloatBall.setVisibility(View.VISIBLE);
				}
			});
    	}
    }
    
    public void updateFloatBallImage()
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
    	if(!ChatServiceController.realtime_voice_enable)
    		return;
    	
    	if(ChatServiceController.hostActivity!=null)
    	{
    		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(mFloatBall!=null)
			    		mFloatBall.updateFloatBallImage();
				}
			});
    	}
    }
    
    
    public void createVoiceFloatBall() {
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if(ChatServiceController.getCurrentActivity()!=null)
		{
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"createWindow");
					if(mFloatBall == null)
					{
						VoiceFloatBallMenu menu = new VoiceFloatBallMenu();
				        mFloatBall = new VoiceFloatBall.Builder(ChatServiceController.getCurrentActivity().getApplicationContext()).menu(menu).build();
				        mFloatBall.setOnClickListener(new View.OnClickListener() {
				            @Override
				            public void onClick(View v) {
				                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "mFloatBall click");
				            }
				        });
				        mFloatBall.setLayoutGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
					}
					mFloatBall.show();
					if(ChatServiceController.getRealtimeVoiceRoomSettingActivity()!=null)
						ChatServiceController.getRealtimeVoiceRoomSettingActivity().exitActivity();
				}
			});
			
		}
	}
    
    public boolean isWindowShowing() {
		return mFloatBall != null;
	}

    private WebRtcPeerManager()
    {
        peerMap = new HashMap<String, PeerUserInfo>();
        peerPublishStatusMap = new HashMap<String, Boolean>();
        speakerList = new ArrayList<String>();
        allList = new ArrayList<String>();
    }
    
    public void reset()
    {
    	if(peerMap != null)
    		peerMap.clear();
    	if(peerPublishStatusMap != null)
    		peerPublishStatusMap.clear();
    	if(speakerList != null)
    		speakerList.clear();
    	if(allList != null)
    		allList.clear();
    }
    
    public void initSpeak(List<String> speakList)
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
    	boolean canSpeakBefore = false;
    	boolean canSpeakAfter = false;
    	if(speakerList!=null)
    	{
    		canSpeakBefore = speakerList.contains(UserManager.getInstance().getCurrentUserId());
    		speakerList.clear();
    	}
    	if(speakList == null || speakList.size() <= 0)
    		return;
    	for(String speaker : speakList)
    	{
    		if(!speakerList.contains(speaker))
    			speakerList.add(speaker);
    	}
    	
    	if(speakerList.contains(UserManager.getInstance().getCurrentUserId()))
    	{
    		canSpeakAfter = true;
    		ServiceInterface.enableAudio(true);
    	}
    	else
    	{
    		ServiceInterface.enableAudio(false);
    	}
    	if(canSpeakBefore!=canSpeakAfter)
    	{
    		updateFloatBallImage();
    	}
    	notifyRoomSetttingActivity();
    }
    
    public void initAll(List<String> allArr)
    {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
    	if(allList!=null)
    		allList.clear();
    	if(allArr == null || allArr.size() <= 0)
    		return;
    	for(String user : allArr)
    	{
    		if(!allList.contains(user))
    			allList.add(user);
    	}
    	notifyRoomSetttingActivity();
    }
    
    public void addSpeak(List<String> speakList)
    {
    	if(speakList == null || speakList.size() <= 0)
    		return;
    	for(String speaker : speakList)
    	{
    		if(!speakerList.contains(speaker))
    			speakerList.add(speaker);
    	}
    	notifyRoomSetttingActivity();
    }

    public void removeSpeak(List<String> speakList)
    {
    	if(speakList == null || speakList.size() <= 0)
    		return;
    	for(String speaker : speakList)
    	{
    		if(speakerList.contains(speaker))
    			speakerList.remove(speaker);
    	}
    	notifyRoomSetttingActivity();
    }
    
    public void removeSpeak(String speaker)
    {
    	if(StringUtils.isEmpty(speaker))
    		return;
    	if(speakerList.contains(speaker))
    	{
    		speakerList.remove(speaker);
    		notifyRoomSetttingActivity();
    	}
    }

    public List<String> getSpeakerList()
	{
		return speakerList;
	}
    
    public List<String> getAllList()
	{
		return allList;
	}
    
    public int getSpeakerCount()
	{
    	if(speakerList!=null)
    		return speakerList.size();
    	else
    		return 0;
	}

	public static WebRtcPeerManager getInstance()
    {
        if (mInstance == null)
        {
            synchronized (WebRtcPeerManager.class)
            {
                if (mInstance == null)
                    mInstance = new WebRtcPeerManager();
            }
        }
        return mInstance;
    }

    public Map<String, PeerUserInfo> getPeerMap() {
        return peerMap;
    }

    public List<PeerUserInfo> getPeerList()
    {
        List<PeerUserInfo> list = new ArrayList<PeerUserInfo>();
        if (peerMap != null)
        {
            Set<String> keySet = peerMap.keySet();
            for (String key:keySet)
            {
                list.add(peerMap.get(key));
            }
        }
        return list;
    }

    public void addPeer(PeerUserInfo peerInfo)
    {
        if (peerMap == null || peerInfo == null || StringUtils.isEmpty(peerInfo.getName()))
            return;
        peerMap.put(peerInfo.getName(),peerInfo);
        peerPublishStatusMap.put(peerInfo.getName(),Boolean.TRUE);
    }

    public Map<String, Boolean> getPeerPublishStatusMap() {
        return peerPublishStatusMap;
    }

    public void addPeer(String userName)
    {
        if (peerMap == null || StringUtils.isEmpty(userName))
            return;
        PeerUserInfo peer = new PeerUserInfo(userName);
        addPeer(peer);
    }
    
    public boolean canSpeak()
    {
    	return published && speakerList!=null && speakerList.contains(UserManager.getInstance().getCurrentUserId());
    }
    
    public boolean audioEnable()
    {
    	return !ChatServiceController.isPressToSpeakVoiceMode && speakerList!=null && speakerList.contains(UserManager.getInstance().getCurrentUserId());
    }

    public void changePeerPublishStatus(String userName,boolean pulished)
    {
        if (peerMap == null || peerPublishStatusMap == null || StringUtils.isEmpty(userName))
            return;
        if(!peerMap.containsKey(userName))
            addPeer(userName,Boolean.valueOf(pulished));
        else
            peerPublishStatusMap.put(userName,pulished);
    }
    
    public void removePeer(String userName)
    {
    	if(StringUtils.isEmpty(userName))
    		return;
        if (peerMap != null &&  peerMap.containsKey(userName))
        	peerMap.remove(userName);
        if (peerPublishStatusMap != null &&  peerPublishStatusMap.containsKey(userName))
        	peerPublishStatusMap.remove(userName);
        notifyRoomSetttingActivity();
        UserManager.checkUser(userName, "", 0);
		UserInfo user = UserManager.getInstance().getUser(userName);
		String name = userName;
		if (user != null && StringUtils.isNotEmpty(user.userName))
			name = user.userName;
        showRealtimeTip(LanguageManager.getLangByKey(LanguageKeys.TIP_VOICE_ROOM_LEAVE,name));
    }
    
    private void notifyRoomSetttingActivity()
    {
    	if(ChatServiceController.getRealtimeVoiceRoomSettingActivity()!=null)
        {
        	ChatServiceController.getRealtimeVoiceRoomSettingActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(ChatServiceController.getRealtimeVoiceRoomSettingActivity()!=null)
						ChatServiceController.getRealtimeVoiceRoomSettingActivity().notifyDataChanged();
				}
			});
        }

    }

    public void addPeer(String userName,Boolean pulished)
    {
        if (peerMap == null || StringUtils.isEmpty(userName))
            return;
        PeerUserInfo peerInfo = new PeerUserInfo(userName);
        peerMap.put(peerInfo.getName(),peerInfo);
        peerPublishStatusMap.put(peerInfo.getName(),pulished);
        notifyRoomSetttingActivity();
    	UserManager.checkUser(peerInfo.getName(), "", 0);
		UserInfo user = UserManager.getInstance().getUser(peerInfo.getName());
		String name = peerInfo.getName();
		if (user != null && StringUtils.isNotEmpty(user.userName))
			name = user.userName;
        showRealtimeTip(LanguageManager.getLangByKey(LanguageKeys.TIP_VOICE_ROOM_ENTER,name));
     }

    public void addPeer(Map<String,Boolean> pulishPeerMap)
    {
        if (peerMap == null || pulishPeerMap == null || pulishPeerMap.size() == 0)
            return;
        Set<String> keySet = pulishPeerMap.keySet();
        if (keySet!=null)
        {
            for (String key:keySet)
            {
                addPeer(key,pulishPeerMap.get(key));
            }
        }
    }
    
    public void removeSmallWindow() {
		if (mFloatBall != null && windowManager!=null) {
			windowManager.removeView(mFloatBall);
			mFloatBall = null;
		}
	}

	public void removeAll() {
		removeSmallWindow();
	}
	
	public String getWebRtcUrl()
	{
		return webRtcUrl;
	}

	private void getWebRtcUrlByHttp(int role)
	{
		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(WEB_RTC_ROOM_JOIN_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String uid = UserManager.getInstance().getCurrentUserId();
			String appId = "100001";
			String roomId = UserManager.getInstance().getCurrentUser().allianceId;
			String roleStr = ""+role;
			BasicNameValuePair uidPair = new BasicNameValuePair("uid", uid);
			BasicNameValuePair roomPair = new BasicNameValuePair("roomid", roomId);
			BasicNameValuePair rolePair = new BasicNameValuePair("role", roleStr);
			BasicNameValuePair appidPair = new BasicNameValuePair("appid", appId);
			long time = System.currentTimeMillis();
			String timeStr = Long.toString(time);
			BasicNameValuePair tPair = new BasicNameValuePair("t", timeStr);
			String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3))
					+ MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
			String sign = MD5.stringMD5(appId + roomId+uid +roleStr+ secret);
			BasicNameValuePair signPair = new BasicNameValuePair("s", sign);
			params.add(uidPair);
			params.add(roomPair);
			params.add(rolePair);
			params.add(appidPair);
			params.add(tPair);
			params.add(signPair);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);
			if (httpResponse.getStatusLine() != null)
			{
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != 200)
					return;
			}
			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "responseStr", responseStr);
			JSONObject json = JSON.parseObject(responseStr);
			if (json != null && json.containsKey("code"))
			{
				int code = json.getIntValue("code");
				if (code == 1)
				{
					if (json.containsKey("data"))
					{
						JSONObject data = json.getJSONObject("data");
						if(data.containsKey("server"))
						{
							JSONObject server = data.getJSONObject("server");
							String protocol = "ws";
							String ip = "";
							String port = "";
							if(server.containsKey("protocol"))
								protocol = server.getString("protocol");
							if(server.containsKey("ip"))
								ip = server.getString("ip");
							if(server.containsKey("port"))
								port = server.getString("port");
							if(StringUtils.isNotEmpty(ip) && StringUtils.isNotEmpty(port))
							{
								webRtcUrl = protocol + "://" + ip + ":" + port;
								LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "webRtcUrl",webRtcUrl);
							}
						}
						
						if(data.containsKey("speaks"))
						{
							JSONArray speaks = data.getJSONArray("speaks");
						}
					}
				}
			}
			
		}
		catch (Exception e)
		{
			isGettingWebRtcUrl = false;
			e.printStackTrace();
		}
	}
	
	public void getRealtimeVoiceRoomUrl(final int roleType)
	{
		if(isGettingWebRtcUrl || !ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null ||
				StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		isGettingWebRtcUrl = true;
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					getWebRtcUrlByHttp(roleType);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		new Thread(runnable).start();
	}
	
	public void leaveRealtimeRoom()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if(!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null ||
				StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"1");
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"2");
					leaveRealtimeRoomByHttp();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		new Thread(runnable).start();
	}
	
	private void leaveRealtimeRoomByHttp()
	{
		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(WEB_RTC_ROOM_LEAVE_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String uid = UserManager.getInstance().getCurrentUserId();
			String appId = "100001";
			String roomId = UserManager.getInstance().getCurrentUser().allianceId;
			BasicNameValuePair uidPair = new BasicNameValuePair("uid", uid);
			BasicNameValuePair roomPair = new BasicNameValuePair("roomid", roomId);
			BasicNameValuePair appidPair = new BasicNameValuePair("appid", appId);
			long time = System.currentTimeMillis();
			String timeStr = Long.toString(time);
			BasicNameValuePair tPair = new BasicNameValuePair("t", timeStr);
			String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3))
					+ MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
			String sign = MD5.stringMD5(appId + roomId+uid+ secret);
			BasicNameValuePair signPair = new BasicNameValuePair("s", sign);
			params.add(uidPair);
			params.add(roomPair);
			params.add(appidPair);
			params.add(tPair);
			params.add(signPair);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);
			if (httpResponse.getStatusLine() != null)
			{
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != 200)
					return;
			}
			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "responseStr", responseStr);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
