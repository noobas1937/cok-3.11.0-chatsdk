package com.elex.im.core.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.MathUtil;
import com.elex.im.core.util.NetworkUtil;
import com.elex.im.core.util.StringUtils;

public class WSServerInfoManager
{
	private static WSServerInfoManager	instance;
	private ConcurrentHashMap<String, ArrayList<WSServerInfo>>	serverInfoMap;

	public static WSServerInfoManager getInstance()
	{
		if (instance == null)
		{
			synchronized (WSServerInfoManager.class)
			{
				if (instance == null)
				{
					instance = new WSServerInfoManager();
				}
			}
		}
		return instance;
	}

	private WSServerInfoManager()
	{
		serverInfoMap = new ConcurrentHashMap<String, ArrayList<WSServerInfo>>();
		load();
	}

	private String serialize(ConcurrentHashMap<String, ArrayList<WSServerInfo>>	map) throws IOException {
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

	private ConcurrentHashMap<String, ArrayList<WSServerInfo>> deSerialization(String str) throws IOException,
			ClassNotFoundException {
		long startTime = System.currentTimeMillis();
		String redStr = java.net.URLDecoder.decode(str, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		ConcurrentHashMap<String, ArrayList<WSServerInfo>> map = (ConcurrentHashMap<String, ArrayList<WSServerInfo>>) objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		long endTime = System.currentTimeMillis();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "反序列化耗时", endTime - startTime);
		return map;
	}

	void saveObject(String strObject) {
		SharedPreferences sp = IMCore.hostActivity.getSharedPreferences("wsServerInfo", Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString("wsServerInfo", strObject);
		edit.commit();
	}

	String getObject() {
		SharedPreferences sp = IMCore.hostActivity.getSharedPreferences("wsServerInfo", Context.MODE_PRIVATE);
		return sp.getString("wsServerInfo", null);
	}
	
	/**
	 * @return 未联网时，返回null
	 */
	private String getNetworkKey()
	{
		return NetworkUtil.getNetworkSummary();
	}

	public void save(String key, ArrayList<WSServerInfo> infos) {
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
	
	private void updateMap(String key, ArrayList<WSServerInfo> newInfos)
	{
		ArrayList<WSServerInfo> oldInfos = serverInfoMap.get(key);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "oldTestResults", servers2str(oldInfos), "newTestResults", servers2str(newInfos));
		for (WSServerInfo newInfo : newInfos)
		{
			boolean hasTestedBefore = false;
			for (WSServerInfo oldInfo : oldInfos)
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
	
	public void updateLastErrorTime(WSServerInfo newInfo)
	{
		String key = getNetworkKey();
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<WSServerInfo> savedInfos = serverInfoMap.get(key);

			for (int i = 0; i < savedInfos.size(); i++)
			{
				WSServerInfo testedServer = savedInfos.get(i);
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
					ArrayList<WSServerInfo> savedInfos = serverInfoMap.get(key);
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
	
	public static String servers2str(ArrayList<WSServerInfo> loadedInfos)
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
	
	public static void sortServers(ArrayList<WSServerInfo> servers)
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

	private static class SortByLossAndLatency implements Comparator<WSServerInfo>
	{
		@Override
		public int compare(WSServerInfo o1, WSServerInfo o2)
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

	public WSServerInfo selectPrimaryServer(ArrayList<WSServerInfo> servers)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "allowedServers", servers2str(servers));
		String key = getNetworkKey();
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<WSServerInfo> testedServers = serverInfoMap.get(key);
			ArrayList<WSServerInfo> testedServersCopy = new ArrayList<WSServerInfo>();
			for (int i = 0; i < testedServers.size(); i++)
			{
				testedServersCopy.add(testedServers.get(i));
			}
			sortServers(testedServersCopy);
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "testedServers", servers2str(testedServers));
			for (int i = 0; i < testedServersCopy.size(); i++)
			{
				WSServerInfo testedServer = testedServersCopy.get(i);
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
			WSServerInfo randomServer = servers.get(MathUtil.random(0, servers.size() - 1));
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "result random server", randomServer);
			return randomServer;
		}
		
		return null;
	}
	
	private WSServerInfo findInServers(WSServerInfo server, ArrayList<WSServerInfo> servers)
	{
		for (WSServerInfo s : servers)
		{
			if (s.equalTo(server))
			{
				return s;
			}
		}
		return null;
	}
}
