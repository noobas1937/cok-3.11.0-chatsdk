package com.elex.chatservice.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.HeadPicUtil.MD5;
import com.elex.chatservice.util.downzip.DecompressZip;
import com.elex.chatservice.util.downzip.DownLoadEntity;
import com.elex.chatservice.util.downzip.Downloador;
import com.elex.chatservice.util.downzip.DownLoadEntity.Status;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.emoj.DefaultEmojDatas;
import com.elex.chatservice.view.emoj.EmojGroupEntity;
import com.elex.chatservice.view.emoj.EmojIcon;
import com.elex.chatservice.view.emoj.GifEmojDatas;

public class EmojSubscribeManager
{
	private static EmojSubscribeManager		mInstance			= null;

	private static final String				GROUP_TAB_PREFIX	= "group_tab_";
	public static final String				GROUP_PREFIX		= "group_";
	private static final String				EMOJ_PREFIX			= "sub_em_";

	private static final String				EMOJ_SUB_URL		= "http://api.cok.chat/player/expressionlist";

	private Map<String, EmojGroupEntity>	subableEmojMap		= null;
	private Map<String, SubedEmojEntity>	subedEmojMap		= null;

	public static EmojSubscribeManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (EmojSubscribeManager.class)
			{
				if (mInstance == null)
					mInstance = new EmojSubscribeManager();
			}
		}
		return mInstance;
	}

	private EmojSubscribeManager()
	{
		subableEmojMap = new HashMap<String, EmojGroupEntity>();
		subedEmojMap = new HashMap<String, SubedEmojEntity>();
	}

	public List<EmojGroupEntity> getSubableEmojList()
	{
		List<EmojGroupEntity> list = new ArrayList<EmojGroupEntity>();
		if (subableEmojMap != null)
		{
			Set<String> keySet = subableEmojMap.keySet();
			for (String key : keySet)
			{
				list.add(subableEmojMap.get(key));
			}
		}
		return list;
	}

	public List<EmojGroupEntity> getAviliableSubedEmojList()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		List<EmojGroupEntity> list = new ArrayList<EmojGroupEntity>();
		if (subedEmojMap != null && subableEmojMap != null)
		{
			Set<String> keySet = subedEmojMap.keySet();
			for (String key : keySet)
			{
				if (StringUtils.isNotEmpty(key) && subableEmojMap.containsKey(key))
				{
					EmojGroupEntity entity = subableEmojMap.get(key);
					if(entity!=null)
					{
						DownLoadEntity downloadEntity = entity.getDownLoadEntity();
						if(downloadEntity.getStatus() == Status.FINISHED)
							list.add(entity);
					}
					
				}
			}
		}
		return list;
	}

	public Map<String, EmojGroupEntity> getSubableEmojMap()
	{
		return subableEmojMap;
	}

	public void addSubableEmoj(String groupId, String name, String description, String price, List<EmojIcon> emojiconList)
	{
		if (subableEmojMap == null || StringUtils.isEmpty(groupId) || subableEmojMap.containsKey(groupId))
			return;
		EmojGroupEntity emojGroup = new EmojGroupEntity(groupId, name, price, description, emojiconList);
		subableEmojMap.put(groupId, emojGroup);
	}

	public void addSubableEmojGroup(EmojGroupEntity emojGroup)
	{
		if (emojGroup == null || StringUtils.isEmpty(emojGroup.getGroupId()) || subableEmojMap.containsKey(emojGroup.getGroupId()))
			return;
		subableEmojMap.put(emojGroup.getGroupId(), emojGroup);
	}

	public List<SubedEmojEntity> getSubedEmojList()
	{
		List<SubedEmojEntity> list = new ArrayList<SubedEmojEntity>();
		if (subedEmojMap != null)
		{
			Set<String> keySet = subedEmojMap.keySet();
			for (String key : keySet)
			{
				list.add(subedEmojMap.get(key));
			}
		}
		return list;
	}
	
	private void beginDownload(final DownLoadEntity downLoadEntity, final String downloadPath, final long fileLength)
	{
		if (downLoadEntity == null || StringUtils.isEmpty(downloadPath) || fileLength <= 0)
			return;
		String url = downLoadEntity.getUrl();
		long downloadLength = 0;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response;
		InputStream is;
		RandomAccessFile fos = null;
		OutputStream output = null;

		try
		{
			String fileName = downloadPath + url.substring(url.lastIndexOf("/") + 1) ;
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"fileName",fileName);
			File file = new File(fileName);
			if (!file.exists())
			{
				downloadLength = 0;
			}
			// 执行请求获取下载输入流
			response = client.execute(request);
			is = response.getEntity().getContent();

			fos = new RandomAccessFile(file, "rw");
			// 从文件的size以后的位置开始写入
			fos.seek(0);

			byte buffer[] = new byte[1024];
			int inputSize = -1;
			while ((inputSize = is.read(buffer)) != -1)
			{
				fos.write(buffer, 0, inputSize);
				downloadLength += inputSize;
			}
			if (downloadLength == fileLength)
			{
				downLoadEntity.setDownloadPercent(100);
				downLoadEntity.setStatus(DownLoadEntity.Status.FINISHED);

				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"download complete!");
				String groupId = downLoadEntity.getGroupId();
				File zipDir = DBHelper.getLocalDirectory(ChatServiceController.hostActivity, "common_pic");
				String groupName = EmojSubscribeManager.GROUP_PREFIX + groupId;
				String zipName = zipDir.getPath() + "/" + groupName + ".zip";
				File zipFile = new File(zipName);

				try
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "zipName", zipName);
					DecompressZip decomp = new DecompressZip(zipFile.getPath(), zipDir.getPath() + File.separator);
					decomp.unzip();
					Map<String, EmojGroupEntity> subaviliableMap = EmojSubscribeManager.getInstance().getSubableEmojMap();
					if (subaviliableMap != null && subaviliableMap.containsKey(groupId))
					{
						EmojGroupEntity entity = subaviliableMap.get(groupId);
						if (entity != null)
						{
							entity.setDownLoadEntity(true);
							if (ChatServiceController.getChatFragment() != null)
								ChatServiceController.getChatFragment().onEmojPanelChanged();
							else
								ChatFragmentNew.emojPanelChanged = true;
						}
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				catch (OutOfMemoryError e)
				{
					e.printStackTrace();
				}
				finally
				{
					zipFile.delete();
				}
			}

		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (request != null)
				{
					request.abort();
				}
				if (output != null)
				{
					output.close();
				}
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void downEmojGroupResource(final DownLoadEntity downLoadEntity)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if(downLoadEntity == null)
			return;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"url",downLoadEntity.getUrl());
		new Thread()
		{
			@Override
			public void run()
			{
				HttpGet request = new HttpGet(downLoadEntity.getUrl());
				try
				{
					long fileLength = 0;
					// 获取文件大小
					HttpClient client = new DefaultHttpClient();
					HttpResponse response = null;
					response = client.execute(request);
					fileLength = response.getEntity().getContentLength();
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"fileLength",fileLength);
					downLoadEntity.setStatus(DownLoadEntity.Status.DOWNLOADING);
					if(ChatServiceController.hostActivity!=null)
					{
						String downloadPath = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "common_pic");
						LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"downloadPath",downloadPath);
						beginDownload(downLoadEntity, downloadPath, fileLength);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (request != null)
					{
						request.abort();
					}
				}
			}
		}.start();
	}
	
	public EmojGroupEntity getEmojGroupEntity(String groupId)
	{
		if(subableEmojMap!=null && subableEmojMap.containsKey(groupId))
		{
			return subableEmojMap.get(groupId);
		}
		return null;
	}

	public Map<String, SubedEmojEntity> getSubedEmojMap()
	{
		return subedEmojMap;
	}

	public void addSubedEmojEntity(String groupId, long endTime)
	{
		if (subedEmojMap == null || StringUtils.isEmpty(groupId) || subedEmojMap.containsKey(groupId))
			return;
		SubedEmojEntity emoj = new SubedEmojEntity(groupId, endTime);
		subedEmojMap.put(groupId, emoj);
	}

	public void addSubedEmojEntity(SubedEmojEntity emojGroup)
	{
		if (subedEmojMap == null || StringUtils.isEmpty(emojGroup.getGroupId()) || subedEmojMap.containsKey(emojGroup.getGroupId()))
			return;
		subedEmojMap.put(emojGroup.getGroupId(), emojGroup);
	}
	
	public void setEmojGroupDownLoadEntity()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if(subableEmojMap == null || subableEmojMap.size() <= 0)
			return;
		if(subedEmojMap !=null && subedEmojMap.size()>0)
		{
			Set<String> keySet = subedEmojMap.keySet();
			for(String groupId : keySet)
			{
				if(StringUtils.isNotEmpty(groupId) && subableEmojMap.containsKey(groupId))
				{
					EmojGroupEntity entity = subableEmojMap.get(groupId);
					if(entity!=null)
						entity.setDownLoadEntity(true);
				}
			}
		}
		
		if(subableEmojMap !=null && subableEmojMap.size()>0 && subableEmojMap.values()!=null)
		{
			for(EmojGroupEntity entity : subableEmojMap.values())
			{
				if(entity!=null && (subedEmojMap == null || !subedEmojMap.containsKey(entity.getGroupId())))
					entity.setDownLoadEntity(false);
			}
		}
	}
	
	public static String getSubedEmojPicLocalPath(String groupName, String fileName, boolean isGif)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "common_pic/" + groupName);
		path += fileName;
		if (isGif)
		{
			if (!fileName.endsWith(".gif"))
				path += ".gif";
		}
		else
		{
			if (!fileName.endsWith(".png"))
				path += ".png";
		}

		return path;
	}

	public static String getEmojCommonPicLocalPath(String groupName, String fileName, boolean isGif)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "common_pic/" + groupName);
		path += "cache_" + MD5.getMD5Str(fileName);
		if (isGif)
		{
			if (!fileName.endsWith(".gif"))
				path += ".gif";
		}
		else
		{
			if (!fileName.endsWith(".png"))
				path += ".png";
		}

		return path;
	}
	
	public static File getEmojGroupLocalDirectory(String groupId)
	{
		String groupName = GROUP_PREFIX + groupId;
		return DBHelper.getLocalDirectory(ChatServiceController.hostActivity, "common_pic/" + groupName);
	}

	public static String getEmojLocalFullName(String groupId, String id)
	{
		return EMOJ_PREFIX + groupId + "_" + id;
	}

	public static String getEmojLocalPath(String groupId, String id, boolean isGif)
	{
		return getEmojCommonPicLocalPath(GROUP_PREFIX + groupId, EMOJ_PREFIX + groupId + "_" + id, isGif);
	}
	
	public static String getSubedEmojLocalPath(String groupId, String id, boolean isGif)
	{
		return getSubedEmojPicLocalPath(GROUP_PREFIX + groupId, EMOJ_PREFIX + groupId + "_" + id, isGif);
	}

	public static String getEmojTabLocalPath(String groupId)
	{
		return getSubedEmojPicLocalPath(GROUP_PREFIX + groupId, GROUP_TAB_PREFIX + groupId, false);
	}
	
	public static boolean isEmojGroupExist(String groupId)
	{
		String tabLocalPath = getSubedEmojPicLocalPath(GROUP_PREFIX + groupId, GROUP_TAB_PREFIX + groupId, false);
		return ImageUtil.isFileExist(tabLocalPath);
	}

	public static String getEmojCDNPath(String groupId, String id, boolean isGif)
	{
		String fileName = isGif ? EMOJ_PREFIX + groupId + "_" + id + ".gif" : EMOJ_PREFIX + groupId + "_" + id + ".png";
		return ConfigManager.getCDNUrl(GROUP_PREFIX + groupId, fileName);
	}
	
	public static String getEmojZipCDNPath(String groupId)
	{
		return ConfigManager.getCDNUrl(GROUP_PREFIX + groupId, GROUP_PREFIX + groupId+".zip");
	}

	public static String getEmojTabCDNPath(String groupId)
	{
		String fileName = GROUP_TAB_PREFIX + groupId + ".png";
		return ConfigManager.getCDNUrl(GROUP_PREFIX + groupId, fileName);
	}

	public void downGroupEmoj(String groupId)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "groupId", groupId);
		
	}

	public void subscribeGroupEmoj(final String price)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "price", price);
		boolean isSucess = JniController.getInstance().excuteJNIMethod("subscribExpression", new Object[]{price});
		if(isSucess)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "isSucess", isSucess);
			ServiceInterface.exitChatActivityFrom2dx(true);
		}
	}
	
	public  String getExpressionPrice(final String packageId)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "packageId", packageId);
		String priceText =  JniController.getInstance().excuteJNIMethod("getExpressionPrice", new Object[]{packageId});
		return priceText;
	}

	public void unsubscribeGroupEmoj(String groupId)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "groupId", groupId);
	}

	public void getEmojSubData()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		Runnable runnable = new Runnable()
		{

			@Override
			public void run()
			{
				getEmojSubDataByHttp();
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();
	}

	private void getEmojSubDataByHttp()
	{
		try
		{
			if(subableEmojMap!=null)
				subableEmojMap.clear();
			if(subedEmojMap!=null)
				subedEmojMap.clear();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(EMOJ_SUB_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String uid = UserManager.getInstance().getCurrentUserId();
			String appId = "100001";
			BasicNameValuePair uidPair = new BasicNameValuePair("uid", uid);
			BasicNameValuePair appidPair = new BasicNameValuePair("appid", appId);
			long time = System.currentTimeMillis();
			String timeStr = Long.toString(time);
			BasicNameValuePair tPair = new BasicNameValuePair("t", timeStr);
			String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3))
					+ MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
			String sign = MD5.stringMD5(appId + uid + secret);
			BasicNameValuePair signPair = new BasicNameValuePair("s", sign);
			params.add(uidPair);
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
//			responseStr = "{\"code\":1,\"message\":\"success\",\"data\":{\"list\":[{\"groupId\":\"1001\",\"name\":\"132496\",\"description\":\"132496\",\"price\":\"\",\"details\":[{\"id\":\"1\",\"name\":\"\"},{\"id\":\"2\",\"name\":\"\"},{\"id\":\"3\",\"name\":\"\"},{\"id\":\"4\",\"name\":\"\"},{\"id\":\"5\",\"name\":\"\"},{\"id\":\"6\",\"name\":\"\"},{\"id\":\"7\",\"name\":\"\"},{\"id\":\"8\",\"name\":\"\"},{\"id\":\"9\",\"name\":\"\"},{\"id\":\"10\",\"name\":\"\"}]}],\"mine\":[{\"groupId\":\"1001\",\"endTime\":1479685870365}]}}";
			JSONObject json = JSON.parseObject(responseStr);
			if (json != null && json.containsKey("code"))
			{
				int code = json.getIntValue("code");
				if (code == 1)
				{
					if (json.containsKey("data"))
					{

						JSONObject data = json.getJSONObject("data");
						if (data.containsKey("list"))
						{
							JSONArray groupArr = data.getJSONArray("list");
							for (int i = 0; i < groupArr.size(); i++)
							{
								EmojGroupEntity group = groupArr.getObject(i, EmojGroupEntity.class);
								if (group != null)
								{
									group.setGroupId();
									addSubableEmojGroup(group);
								}
							}
						}

						if (data.containsKey("mine"))
						{
							JSONArray mineArr = data.getJSONArray("mine");
							for (int i = 0; i < mineArr.size(); i++)
							{
								SubedEmojEntity subedEntity = mineArr.getObject(i, SubedEmojEntity.class);
								if (subedEntity != null)
									addSubedEmojEntity(subedEntity);
							}
						}
						
						EmojSubscribeManager.getInstance().setEmojGroupDownLoadEntity();

					}
				}
			}
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "responseStr", responseStr);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public EmojIcon getEmojIcon(String groupId, String id)
	{
		if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(id))
			return null;
		if (groupId.equals(DefaultEmojDatas.DEFAULT_EMOJ_GROUP_ID))
		{
			return DefaultEmojDatas.getEmoj(groupId, id);
		}
		else if (groupId.equals(GifEmojDatas.GIF_EMOJ_GROUP_ID))
		{
			return GifEmojDatas.getEmoj(groupId, id);
		}
		else
		{
			if (subableEmojMap != null && subableEmojMap.containsKey(groupId))
			{
				EmojGroupEntity entity = subableEmojMap.get(groupId);
				if (entity != null && entity.getDetails() != null)
				{
					for (EmojIcon emoj : entity.getDetails())
					{
						if (emoj != null && emoj.getId().equals(id))
							return emoj;
					}
				}
			}
			return null;
		}

	}
}
