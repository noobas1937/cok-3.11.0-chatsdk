package org;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import android.util.Log;

import com.elex.im.core.event.EventCenter;
import com.elex.im.core.event.LogEvent;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;

public class WsClient extends WebSocketClient {
	public boolean						isOpen	= false;
	public boolean						isClose	= false;
	private WebSocketManager			webSocketManager;
	private ScheduledExecutorService	heartbeatService;
	private String						clientID;
	private int							pingCnt	= 0;
	private int							pongCnt	= 0;

    public WsClient(String serverURI, Map<String, String> header, WebSocketManager webSocketManager) throws URISyntaxException {
        super(new URI(serverURI), new Draft_10(), header, 1000);
        
        this.webSocketManager = webSocketManager;
    }
    
    private void startKeepAlive()
    {
    	if(heartbeatService != null) return;
    	
        heartbeatService = Executors.newSingleThreadScheduledExecutor();
        TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					sendKeepAlive();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};
		
		heartbeatService.scheduleWithFixedDelay(timerTask, 10000, 15 * 1000, TimeUnit.MILLISECONDS);
    }
	
	private void sendKeepAlive()
	{
		if(isOpen()){
			pingCnt++;
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_SEND, "ping " + pingCnt);
			FramedataImpl1 framedata = new FramedataImpl1(Framedata.Opcode.PING);
			framedata.setFin(true);
			sendFrame(framedata);
		}
	}

	@Override
	public void onWebsocketPong( WebSocket conn, Framedata f ) {
		super.onWebsocketPong(conn, f);
		pongCnt++;
		LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_RECIEVE, "pong " + pongCnt);
	}

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        isOpen = true;
        
    	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "Connected");
    	try
		{
    		EventCenter.getInstance().dispatchEvent(new LogEvent("Connected"));
    		webSocketManager.resetReconnectInterval();
    		webSocketManager.onOpen();
    		startKeepAlive();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
    }

    @Override
    public void onMessage(String s) {
    	try
		{
			webSocketManager.handleMessage(s);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        isClose = true;
        isOpen = false;
        
        String errorInfo = String.format(Locale.US, "WSClient.onClose Code:%d Reason:%s Remote:%b", code, reason, b);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, errorInfo);
		EventCenter.getInstance().dispatchEvent(new LogEvent(errorInfo));
		LogUtil.trackMessage(errorInfo);
		
		webSocketManager.handleDisconnect();
		webSocketManager.onConnectError();
    }

    @Override
    public void onError(Exception e) {
        isClose = true;
        isOpen = false;
    	LogUtil.printException(e);
		LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_WS_STATUS, "error", e.getMessage());
		EventCenter.getInstance().dispatchEvent(new LogEvent("Error:" + e.getMessage()));
		LogUtil.trackMessage("WSClient.onError msg:" + e.getMessage());
		
		webSocketManager.onConnectError();
//		ServiceInterface.flyHint(null, "", "连接错误", 0, 0, false);
    }

//	@Override
//	public void onStreamInput()
//	{
//		statusListener.onStremInput();
//	}
//
//	@Override
//	public void onStatus(String status)
//	{
//		statusListener.onStatus(status);
//	}
	
	private String getClientID(JSONObject json)
	{
		try
		{
			return json.getString("server") + json.getString("clientid");
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		
		return null;
	}
    
    public boolean isMyMessage(JSONObject json)
    {
    	return StringUtils.isNotEmpty(clientID) && !clientID.equals(getClientID(json));
    }
	
	public void setClientID(JSONObject json)
	{
		clientID = getClientID(json);
	}
	
	private void resetClientID()
	{
		clientID = null;
	}
    
    public void destroy()
    {
    	heartbeatService.shutdown();
    	heartbeatService = null;
    	
//    	reconnectService.shutdown();
//    	reconnectService = null;
    }
}
