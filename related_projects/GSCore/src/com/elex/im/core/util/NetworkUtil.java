package com.elex.im.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.elex.im.core.IMCore;
import com.elex.im.core.event.EventCenter;
import com.elex.im.core.event.LogEvent;
import com.elex.im.core.net.WSServerInfo;
import com.elex.im.core.net.WSServerInfoManager;

public class NetworkUtil
{
	private static final int	NUMBER_OF_PACKTETS	= 5;
	private static final String	WIFI				= "WIFI";
	/** 包全丢了的情况下，没有delay数据，所使用的delay值 */
	public static final double	DELAY_OF_ALL_LOST	= -1;

	public static Pair<Double, Double> getPingResult(String address)
	{
		return getPingResult(address, NUMBER_OF_PACKTETS);
	}

	/*
	 * Returns the package loss rate and average latency to a given server in
	 * mili-seconds by issuing a ping command. system will issue
	 * NUMBER_OF_PACKTETS ICMP Echo Request packet each having size of 56 bytes
	 * every second, and returns the avg latency of them. Returns 0 when there
	 * is no connection
	 */
	public static Pair<Double, Double> getPingResult(String address, int packets)
	{
		String pingCommand = "/system/bin/ping -c " + packets + " " + address;
		String inputLine = "";
		double avgRtt = DELAY_OF_ALL_LOST;
		String percentStr = null;
		double lossPercent = 100;

		try
		{
			// execute the command on the environment interface
			Process process = Runtime.getRuntime().exec(pingCommand);
			// gets the input stream to get the output of the executed command
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			inputLine = bufferedReader.readLine();
			while (inputLine != null)
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, inputLine);
				if (inputLine.length() > 0 && inputLine.contains("loss"))
				{
					percentStr = inputLine.substring(inputLine.indexOf("received, ") + 10, inputLine.indexOf("%")).trim();
				}
				// rtt min/avg/max/mdev = 112.732/143.162/154.774/17.601 ms
				// 如果包全部丢了，则不会有这行
				if (inputLine.length() > 0 && inputLine.contains("avg"))
				{
					// when we get to the last line of executed ping command
					break;
				}
				inputLine = bufferedReader.readLine();
			}
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}

		if (inputLine != null && inputLine.length() > 0 && inputLine.contains("avg"))
		{
			try
			{
				// Extracting the average round trip time from the inputLine string
				String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
				String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
				String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
				avgRtt = Double.valueOf(strAvgRtt);
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}

		if (StringUtils.isNotEmpty(percentStr))
		{
			try
			{
				double loss = Double.valueOf(percentStr);
				if (loss >= 0 && loss <= 100)
				{
					lossPercent = loss;
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		else
		{
			LogUtil.trackMessage("no package loss info");
		}

//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "avgRtt", avgRtt, "lossPercent", lossPercent);
		return new Pair<Double, Double>(lossPercent, avgRtt);
	}

	/**
	 * 在本地先试试，看看结果是否稳定
	 */
	public static void testServerAndSaveResult(ArrayList<WSServerInfo> servers)
	{
		if(!NetworkUtil.isConnected(IMCore.hostActivity))
		{
			return;
		}
		
		String network = getNetworkSummary();
		if(StringUtils.isEmpty(network))
			return;
		
		int i = 0;
		for (WSServerInfo server : servers)
		{
			Pair<Double, Double> lossAndLatency = getPingResult(server.address);
			server.loss = lossAndLatency.first;
			server.delay = lossAndLatency.second;
			String health = getWebsocketHealthStatus(server);
			i++;
			EventCenter.getInstance().dispatchEvent(new LogEvent("Ping server " + i + ": <" + health + "> " + server));
			server.initTestInfo();
		}
		
		// 如果测试前后网络不一样，则不存储结果
		if(!network.equals(getNetworkSummary()))
		{
			return;
		}

		if(servers.size() > 0){
			WSServerInfoManager.sortServers(servers);
			WSServerInfoManager.getInstance().save(network, servers);
		}
	}

	private static String getWebsocketHealthStatus(WSServerInfo server)
	{
		String url = "http://" + server.address +":" + server.port + "/system/health";
		return HttpRequestUtil.sendGet(url, "");
	}

	/**
	 * Check device's network connectivity and speed
	 * @author emil http://stackoverflow.com/users/220710/emil
	 *
	 */
	public static void testNetworkInfo()
	{
		Context context = getContext();

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isConnected", isConnected(context));
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isConnectedWifi", isConnectedWifi(context));
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isConnectedMobile", isConnectedMobile(context));
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isConnectedFast", isConnectedFast(context));
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isConnectedFastNew", isConnectedFastNew(context));
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "getNetworkClass", getNetworkClass());
	}

	/**
	 * Get the network info
	 * 
	 * @param context
	 * @return
	 */
	public static NetworkInfo getNetworkInfo(Context context)
	{
		if(PermissionManager.isNetworkStatePermissionsAvaiable(getContext()))
		{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo();
		}
		return null;
	}

	/**
	 * Check if there is any connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context)
	{
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected());
	}

	/**
	 * Check if there is any connectivity to a Wifi network
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedWifi(Context context)
	{
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * Check if there is any connectivity to a mobile network
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedMobile(Context context)
	{
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	/**
	 * Check if there is fast connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnectedFast(Context context)
	{
		NetworkInfo info = getNetworkInfo(context);
		return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
	}

	/**
	 * Check if the connection is fast
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionFast(int type, int subType){
		if(type==ConnectivityManager.TYPE_WIFI){
			return true;
		}else if(type==ConnectivityManager.TYPE_MOBILE){
			switch(subType){
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return false; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return true; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return true; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return false; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return true; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return true; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return true; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return true; // ~ 400-7000 kbps
			/*
			 * Above API level 7, make sure to set android:targetSdkVersion 
			 * to appropriate level to use these
			 */
			case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11 
				return true; // ~ 1-2 Mbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
				return true; // ~ 5 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
				return true; // ~ 10-20 Mbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				return false; // ~25 kbps 
			case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
				return true; // ~ 10+ Mbps
			// Unknown
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default:
				return false;
			}
		}else{
			return false;
		}
	}

	/**
	 * Small addition: We should ideally use TelephonyManager to detect network
	 * types. So the above should instead read:
	 */
	public static boolean isConnectedFastNew(Context context)
	{
		NetworkInfo info = getNetworkInfo(getContext());
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return (info != null && info.isConnected() && isConnectionFast(info.getType(), tm.getNetworkType()));
	}

	public static int getNetworkSubtype()
	{
		if(!isConnected(getContext()))
			return -1;
		
		NetworkInfo info = getNetworkInfo(getContext());
		if(info != null)
		{
			return info.getSubtype();
		}
		else
		{
			return -1;
		}
	}

	public static String getNetworkClass() {
	    NetworkInfo info = getNetworkInfo(getContext());
	    if(info == null || !isConnected(getContext()))
	        return "-"; //not connected
	    
	    if(info.getType() == ConnectivityManager.TYPE_WIFI)
	        return WIFI;
	    if(info.getType() == ConnectivityManager.TYPE_MOBILE){
	        int networkType = info.getSubtype();
	        switch (networkType) {
	            case TelephonyManager.NETWORK_TYPE_GPRS:
	            case TelephonyManager.NETWORK_TYPE_EDGE:
	            case TelephonyManager.NETWORK_TYPE_CDMA:
	            case TelephonyManager.NETWORK_TYPE_1xRTT:
	            case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
	                return "2G";
	            case TelephonyManager.NETWORK_TYPE_UMTS:
	            case TelephonyManager.NETWORK_TYPE_EVDO_0:
	            case TelephonyManager.NETWORK_TYPE_EVDO_A:
	            case TelephonyManager.NETWORK_TYPE_HSDPA:
	            case TelephonyManager.NETWORK_TYPE_HSUPA:
	            case TelephonyManager.NETWORK_TYPE_HSPA:
	            case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
	            case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
	            case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
	                return "3G";
	            case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
	                return "4G";
	            default:
	                return "?";
	         }
	    }
	    return "?";
	}

	public static boolean isOnline()
	{
		return isNetworkAvailable(getContext()) && isInternetAvailable();
	}

	private static boolean isNetworkAvailable(Context context)
	{
		NetworkInfo netInfo = getNetworkInfo(getContext());
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	/**
	 * TODO 测试网址需要全球都能访问。8.8.8.8在国内丢包较多，114.114.114.114国外不知访问是否顺畅
	 */
	private static boolean isInternetAvailable()
	{
		Runtime runtime = Runtime.getRuntime();
		try
		{
			long startTime = System.currentTimeMillis();

			Process ipProcess = runtime.exec("/system/bin/ping -c 1 114.114.114.114"); // 8.8.8.8
			int exitValue = ipProcess.waitFor();

			long endTime = System.currentTimeMillis();
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "responseTime", endTime - startTime, "exitValue", exitValue,
					"available", exitValue == 0);

			return (exitValue == 0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, false);

		return false;
	}
	
	public static String getNetworkSPN()
	{
		Context context = getContext();
		// Get System TELEPHONY service reference
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// Get carrier name (Network Operator Name)
		// 有sim卡（可能还得连上运营商网络）才有，否则为空
		String carrierName = tManager.getNetworkOperatorName();
		// It returns the same result in normal situation. but it may (and
		// may not, i never tested it) return different result on roaming.
		String simOperatorName = tManager.getSimOperatorName();
		
		return carrierName;
	}
	
	private static Context getContext()
	{
		return IMCore.hostActivity;
	}
	
	public static String getSSID()
	{
		String ssid = "";
		
		if(PermissionManager.isWifiStatePermissionsAvaiable(getContext()))
		{
			WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			ssid = wifiInfo.getSSID();
			if(ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\""))
			{
				ssid = ssid.substring(1, ssid.length() - 1);
			}
		}
		
		return ssid;
	}

	/**
	 * @return 未联网时，返回null
	 */
	public static String getNetworkSummary()
	{
		if(!NetworkUtil.isConnected(getContext()))
		{
			return null;
		}
		
		if (NetworkUtil.getNetworkClass().equals(WIFI))
		{
			String ssid = getSSID();
			if(StringUtils.isNotEmpty(ssid))
			{
				ssid = "-" + ssid;
			}
			return NetworkUtil.getNetworkClass() + ssid;
		}
		else
		{
			return NetworkUtil.getNetworkClass() + "-" + NetworkUtil.getNetworkSubtype() + "-" + NetworkUtil.getNetworkSPN();
		}
	}
}
