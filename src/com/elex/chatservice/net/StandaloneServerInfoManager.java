package com.elex.chatservice.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.MathUtil;
import com.elex.chatservice.util.NetworkUtil;

public class StandaloneServerInfoManager
{
	private static StandaloneServerInfoManager	instance;
	private ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>>	serverInfoMap;

	public static StandaloneServerInfoManager getInstance()
	{
		if (instance == null)
		{
			synchronized (StandaloneServerInfoManager.class)
			{
				if (instance == null)
				{
					instance = new StandaloneServerInfoManager();
				}
			}
		}
		return instance;
	}

	private StandaloneServerInfoManager()
	{
		serverInfoMap = new ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>>();
		load();
	}

	private String serialize(ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>>	map) throws IOException {
		long startTime = System.currentTimeMillis();
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(map);
		String serStr = byteArrayOutputStream.toString("ISO-8859-1");
		serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
		objectOutputStream.close();
		byteArrayOutputStream.close();
		
		long endTime = System.currentTimeMillis();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "序列化耗时", endTime - startTime);

		return serStr;
	}

	private ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>> deSerialization(String str) throws IOException,
			ClassNotFoundException {
		long startTime = System.currentTimeMillis();
		String redStr = java.net.URLDecoder.decode(str, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>> map = (ConcurrentHashMap<String, ArrayList<StandaloneServerInfo>>) objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		long endTime = System.currentTimeMillis();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "反序列化耗时", endTime - startTime);
		return map;
	}

	void saveObject(String strObject) {
		String key = !SwitchUtils.mqttEnable ? "wsServerInfo" : "mqttServerInfo";
		SharedPreferences sp = ChatServiceController.hostActivity.getSharedPreferences(key, Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString("wsServerInfo", strObject);
		edit.commit();
	}

	String getObject() {
		String key = !SwitchUtils.mqttEnable ? "wsServerInfo" : "mqttServerInfo";
		SharedPreferences sp = ChatServiceController.hostActivity.getSharedPreferences(key, Context.MODE_PRIVATE);
		return sp.getString("wsServerInfo", null);
	}
	
	/**
	 * @return 未联网时，返回null
	 */
	private String getNetworkKey()
	{
		return NetworkUtil.getNetworkSummary();
	}

	public void save(String key, ArrayList<StandaloneServerInfo> infos) {
		if (StringUtils.isEmpty(key))
			return;
		
		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "key", key);
			if(serverInfoMap.containsKey(key))
			{
				updateMap(key, infos);
			}
			else
			{
				serverInfoMap.put(key, infos);
			}
			saveObject(serialize(serverInfoMap));
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
	}
	
	private void updateMap(String key, ArrayList<StandaloneServerInfo> newInfos)
	{
		ArrayList<StandaloneServerInfo> oldInfos = serverInfoMap.get(key);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "oldTestResults", servers2str(oldInfos), "newTestResults", servers2str(newInfos));
		for (StandaloneServerInfo newInfo : newInfos)
		{
			boolean hasTestedBefore = false;
			for (StandaloneServerInfo oldInfo : oldInfos)
			{
				if(oldInfo.equalTo(newInfo))
				{
					hasTestedBefore = true;
					if(oldInfo.isTestTooOld())
					{
						LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "result is out of time");
					}
					oldInfo.updateTestResult(newInfo);
				}
			}
			if(!hasTestedBefore)
			{
				oldInfos.add(newInfo);
			}
		}
		LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "updatedTestResults", servers2str(oldInfos));
	}
	
	public void updateLastErrorTime(StandaloneServerInfo newInfo)
	{
		String key = getNetworkKey();
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<StandaloneServerInfo> savedInfos = serverInfoMap.get(key);

			for (int i = 0; i < savedInfos.size(); i++)
			{
				StandaloneServerInfo testedServer = savedInfos.get(i);
				if(newInfo.equalTo(testedServer))
				{
					testedServer.lastErrorTime = newInfo.lastErrorTime;
					
					try
					{
						saveObject(serialize(serverInfoMap));
					}
					catch (IOException e)
					{
						LogUtil.printException(e);
					}
					
					return;
				}
			}
		}
	}

	public void load()
	{
		try
		{
			String savedValue = getObject();
			if (StringUtils.isNotEmpty(savedValue))
			{
				serverInfoMap = deSerialization(savedValue);

				String key = getNetworkKey();
				if(key != null && serverInfoMap.containsKey(key))
				{
					ArrayList<StandaloneServerInfo> savedInfos = serverInfoMap.get(key);
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "network", key, "savedInfos", servers2str(savedInfos));
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			LogUtil.printException(e);
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
	}
	
	public static String servers2str(ArrayList<StandaloneServerInfo> loadedInfos)
	{
		String result = "";
		if (loadedInfos != null)
		{
			for (int i = 0; i < loadedInfos.size(); i++)
			{
				result += "\n[" + i + "] " + loadedInfos.get(i);
			}
		}
		return result;
	}
	
	public static void sortServers(ArrayList<StandaloneServerInfo> servers)
	{
		if (servers != null && servers.size() > 0)
		{
			try
			{
				Collections.sort(servers, new SortByLossAndLatency());
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	private static class SortByLossAndLatency implements Comparator<StandaloneServerInfo>
	{
		@Override
		public int compare(StandaloneServerInfo o1, StandaloneServerInfo o2)
		{
			if (o1.loss > o2.loss
					|| (o1.loss == o2.loss && o1.delay > o2.delay))
			{
				return 1;
			}
			else if (o1.loss == o2.loss && o1.delay == o2.delay)
			{
				return 0;
			}

			return -1;
		}
	}
	
	public StandaloneServerInfo selectPrimaryServer(ArrayList<StandaloneServerInfo> servers)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "allowedServers", servers2str(servers));
		String key = getNetworkKey();
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<StandaloneServerInfo> testedServers = serverInfoMap.get(key);
			ArrayList<StandaloneServerInfo> testedServersCopy = new ArrayList<StandaloneServerInfo>();
			for (int i = 0; i < testedServers.size(); i++)
			{
				testedServersCopy.add(testedServers.get(i));
			}
			sortServers(testedServersCopy);
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "testedServers", servers2str(testedServers));
			for (int i = 0; i < testedServersCopy.size(); i++)
			{
				StandaloneServerInfo testedServer = testedServersCopy.get(i);
				if(testedServer.isTestTooOld() || testedServer.isConnectionErrorRecently())
				{
					continue;
				}
				if (servers != null && servers.size() > 0 && findInServers(testedServer, servers) == null)
				{
					continue;
				}
				
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "result selected server", testedServer);
				return testedServer;
			}
		}

		if (servers != null && servers.size() > 0)
		{
			StandaloneServerInfo randomServer = servers.get(MathUtil.random(0, servers.size() - 1));
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "result random server", randomServer);
			return randomServer;
		}
		
		return null;
	}
	
	private StandaloneServerInfo findInServers(StandaloneServerInfo server, ArrayList<StandaloneServerInfo> servers)
	{
		for (StandaloneServerInfo s : servers)
		{
			if (s.equalTo(server))
			{
				return s;
			}
		}
		return null;
	}
}
