package com.elex.chatservice.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.TranslateListener;
import com.elex.chatservice.util.TranslateNewParams;
import com.elex.chatservice.util.TranslateParam;
import com.elex.chatservice.util.TranslateUtil;
import com.elex.chatservice.util.TranslatedByLuaResult;
import com.elex.chatservice.util.HeadPicUtil.MD5;

public class TranslateManager
{

	private static TranslateManager		_instance				= null;
	private List<MsgItem>				translateQueue			= null;
	private Map<String, List<MsgItem>>	translateQueueLua		= null;
	private Timer						timer					= null;
	private TimerTask					timerTask				= null;
	private long						tranlateStartTime		= 0;
	private boolean						isTranlating			= false;
	public String						disableLang				= "";
	public static boolean				isTranslatedByLuaStart	= false;
	public static boolean				isUIShow				= false;
	private  ExecutorService				executorService			= null;
	public static boolean				google_server_available	 = false;
	public static boolean 				client_translate_company_new = false;
	public static boolean				translateByAgentEnable	= false;
	
	private static final String			GOOGLE_SERVER_TEST_URL	= "http://www.google.com/generate_204";
	private static final String			SERVER_TRANSLATE_URL	= "http://translate.elexapp.com/translate2.php";
	private static final String			NEW_SERVER_TRANSLATE_URL	= "http://translate.elexapp.com/newTranslate1.php";
	private static final String			NEW_SERVER_TRANSLATE_AGENT_URL	= "http://lx3.cok.elexapp.ccgslb.net/newTranslate1.php";
	private static final String			TRANSLATE_DEVELOP_URL	= "http://translate.elexapp.com/feedback";
	
	private boolean						canTranslateByAgent		= true;

	public static TranslateManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (TranslateManager.class)
			{
				if (_instance == null)
					_instance = new TranslateManager();
			}
		}
		return _instance;
	}

	private TranslateManager()
	{
		executorService = Executors.newFixedThreadPool(5);
		reset();
//		testGoogleAvailable();
	}

	public void reset()
	{
		stopTimer();
		translateQueue = new ArrayList<MsgItem>();
		translateQueueLua = new HashMap<String, List<MsgItem>>();
	}

	public static boolean isNeedTranslateChar(String str)
	{
		if (StringUtils.isNotEmpty(str) && StringUtils.isNumeric(str))
			return false;
		char[] chars = str.toCharArray();
		if (chars.length == 1)
		{
			int code = (int) chars[0];
			if (code >= 0 && code <= 255)
				return false;
		}
		return true;
	}

	public static boolean isLangSameAsTargetLang(String lang)
	{
		boolean isSame = false;
		if (StringUtils.isNotEmpty(lang) && StringUtils.isNotEmpty(ConfigManager.getInstance().gameLang)
				&& (ConfigManager.getInstance().gameLang.equals(lang) || isSameZhLang(lang, ConfigManager.getInstance().gameLang)))
			isSame = true;
		return isSame;
	}

	public static boolean isSameZhLang(String originalLang, String targetLang)
	{
		if (StringUtils.isNotEmpty(originalLang) && StringUtils.isNotEmpty(targetLang)
				&& ((isZh_CN(originalLang) && isZh_CN(targetLang)) || (isZh_TW(originalLang) && isZh_TW(targetLang))))
			return true;
		return false;
	}

	public static boolean isZh_CN(String lang)
	{
		if (StringUtils.isNotEmpty(lang) && lang.equals("zh-CN") || lang.equals("zh_CN") || lang.equals("zh-Hans") || lang.equals("zh-CHS"))
			return true;
		return false;
	}

	public static boolean isZh_TW(String lang)
	{
		if (StringUtils.isNotEmpty(lang) && lang.equals("zh-TW") || lang.equals("zh_TW") || lang.equals("zh-Hant") || lang.equals("zh-CHT"))
			return true;
		return false;
	}


	public void enterTranlateQueue(MsgItem msgItem)
	{
		if (!ConfigManager.isAutoTranslateEnable())
			return;
		try
		{
			if (msgItem != null && !msgItem.isNewMsg && !(StringUtils.isEmpty(msgItem.msg) || msgItem.canNotShowTranslateQuickActionMenu() || msgItem.isTranslateMsgValid()))
			{
				if (translateQueue != null && !translateQueue.contains(msgItem))
				{
					translateQueue.add(msgItem);
				}
				if (translateQueueLua != null)
				{
					List<MsgItem> list = null;
					if (translateQueueLua.containsKey(msgItem.msg))
					{
						list = translateQueueLua.get(msgItem.msg);
						if (list != null && !list.contains(msgItem))
						{
							list.add(msgItem);
						}
						else if (list == null)
						{
							list = new ArrayList<MsgItem>();
							list.add(msgItem);
							translateQueueLua.put(msgItem.msg, list);
						}
					}
					else
					{
						list = new ArrayList<MsgItem>();
						list.add(msgItem);
						translateQueueLua.put(msgItem.msg, list);
					}
				}

				if (timer == null)
					createTimer();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private boolean canTranslateByLua()
	{
		if (isTranslatedByLuaStart && isUIShow)
			return true;
		isTranslatedByLuaStart = JniController.getInstance().excuteJNIMethod("canTransalteByLua", null);
		return isTranslatedByLuaStart;
	}

	public void handleTranslateResult(TranslatedByLuaResult result)
	{
		List<MsgItem> list = translateQueueLua.get(result.getOriginalMsg());
		if (list != null && list.size() > 0)
		{
			for (int i = 0; i < list.size(); i++)
			{
				MsgItem msgItem = list.get(i);
				if (msgItem != null)
				{
					msgItem.translateMsg = result.getTranslatedMsg();
					msgItem.originalLang = result.getOriginalLang();
					msgItem.translatedLang = ConfigManager.getInstance().gameLang;
					
					if (msgItem.hasTranslated())
						msgItem.hasTranslated = true;
					else
						msgItem.hasTranslated = false;
					
					ChatChannel channel = null;
					if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
							&& msgItem.chatChannel != null)
					{
						channel = ChannelManager.getInstance().getChannel(msgItem.channelType, msgItem.chatChannel.channelID);
					}
					else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
							|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
							|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
					{
						channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
					}
					if (channel != null)
					{
						DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
					}
				}
			}
			translateQueueLua.remove(result.getOriginalMsg());
			isTranlating = false;
		}
	}

	public boolean isInTranslateQueue(String msg)
	{
		if (translateQueueLua != null && translateQueueLua.containsKey(msg))
			return true;
		return false;
	}

	private void createTimer()
	{
		if (ConfigManager.autoTranlateMode <= 0)
			return;
		timer = new Timer();
		timerTask = new TimerTask()
		{

			@Override
			public void run()
			{
				if (System.currentTimeMillis() - tranlateStartTime >= 5000 || !isTranlating)
				{
//					if (!ConfigManager.autoTranlateEnable && ConfigManager.autoTranlateMode == 2 && canTranslateByLua() && !translateQueueLua.isEmpty())
//					{
//
//						Set<String> msgKeySet = translateQueueLua.keySet();
//						if (msgKeySet.size() > 0)
//						{
//							String msg = msgKeySet.toArray()[0].toString();
//							if (StringUtils.isNotEmpty(msg))
//							{
//								if (System.currentTimeMillis() - tranlateStartTime >= 5000 && isTranlating)
//								{
//									translateQueueLua.remove(msg);
//									isTranlating = false;
//								}
//								else
//								{
//									tranlateStartTime = System.currentTimeMillis();
//									isTranlating = true;
//									JniController.getInstance().excuteJNIVoidMethod("translateMsgByLua",
//											new Object[] { msg, ConfigManager.getInstance().gameLang });
//								}
//							}
//						}
//						if (translateQueueLua.isEmpty())
//							stopTimer();
//					}
//					else 
					if (ConfigManager.isAutoTranslateEnable() && !translateQueue.isEmpty())
					{
						final MsgItem msgItem = translateQueue.get(0);
						if (msgItem != null)
						{
							tranlateStartTime = System.currentTimeMillis();
							isTranlating = true;
							try
							{
								String ret = msgItem.msg;
								String translateMsg = "";
								String originalLang = "";
								String msg = msgItem.msg;
								boolean needPreProcess = msgItem.needTranslatePreProcess();
								if(needPreProcess)
									msg = msgItem.getProcessedMsg();
//								if(ConfigManager.autoTranlateByGoogle && google_server_available)
//								{
//									String originalLanguage = msgItem.getLang();
//									if(msgItem.isOriginalLangInValid() || !msgItem.isTranslateMsgValid())
//										originalLanguage = "";
//									ret = translateByGoogle(msg, originalLanguage);
//									if(StringUtils.isNotEmpty(ret) && !ret.contains("<!DOCTYPE html>"))
//									{
//										if((!(ret.contains("[") && ret.contains("]")) && (ret.startsWith("\"") && ret.endsWith("\""))) || ret.equals("\""+msgItem.msg+"\""))
//										{
//											if(ret.length()>=2 && !(msgItem.msg.startsWith("\"") && msgItem.msg.endsWith("\"")))
//												translateMsg = ret.substring(1, ret.length()-1);
//											else
//												translateMsg = ret;
//											if(StringUtils.isNotEmpty(translateMsg))
//												translateMsg = translateMsg.replaceAll("\\\\n", "\n");
//										}
//										else
//										{
//											JSONArray jsonArray = JSON.parseArray(ret);
//											if(jsonArray.size() >= 2)
//											{
//												translateMsg = jsonArray.getString(0);
//												if(StringUtils.isNotEmpty(translateMsg))
//													translateMsg = translateMsg.replaceAll("\\\\n", "\n");
//												originalLang = jsonArray.getString(1);
//											}
//										}
//									}
//									
//								}
//								else
//								{
									if(client_translate_company_new)
										ret = translateByServer(msgItem.msg, msgItem.getLang());
									else
										ret = translateNew(msgItem.msg, msgItem.getLang());
									
									TranslateNewParams params = JSON.parseObject(ret, TranslateNewParams.class);
									translateMsg = params.getTranslateMsg();
									originalLang = params.getOriginalLang();
//								}
								
								if (StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{"))
								{
									msgItem.translateMsg = translateMsg;
									if(needPreProcess)
										msgItem.postProcessTranslateMsg();
									msgItem.originalLang = originalLang;
									msgItem.translatedLang = ConfigManager.getInstance().gameLang;
									
									if (msgItem.hasTranslated())
										msgItem.hasTranslated = true;
									else
										msgItem.hasTranslated = false;
									
									ChatChannel channel = null;
									if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
											&& msgItem.chatChannel != null)
									{
										channel = ChannelManager.getInstance().getChannel(msgItem.channelType,
												msgItem.chatChannel.channelID);
									}
									else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
											|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
											|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
									{
										channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
									}
									if (channel != null)
									{
										DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
									}
									translateQueue.remove(msgItem);
									isTranlating = false;
								}
							}
							catch (Exception e)
							{
								translateQueue.remove(msgItem);
								// LogUtil.trackMessage("JSON.parseObject exception on server"
								// +
								// UserManager.getInstance().getCurrentUser().serverId);
							}
						}
						if (translateQueue.isEmpty())
							stopTimer();
					}
				}

			}
		};
		timer.schedule(timerTask, 0, 100);
	}

	private void stopTimer()
	{
		try
		{
			if (timerTask != null)
			{
				timerTask.cancel();
				timerTask = null;
			}

			if (timer != null)
			{
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	class TransalteDevelopRunnable implements Runnable
	{

		private MsgItem	msgItem;
		
		public TransalteDevelopRunnable(MsgItem item)
		{
			this.msgItem = item;
		}
		
		@Override
		public void run()
		{
			if(msgItem!=null && StringUtils.isNotEmpty(msgItem.msg) && StringUtils.isNotEmpty(msgItem.translateMsg) && StringUtils.isNotEmpty(msgItem.originalLang))
				translateDevelop(msgItem.msg, msgItem.translateMsg, msgItem.originalLang);
		}
		
	}

	class TranslateRunnable implements Runnable
	{
		private MsgItem	msgItem;
		private Handler	handler;

		public TranslateRunnable(MsgItem item, Handler handler)
		{
			this.msgItem = item;
			this.handler = handler;
		}

		@Override
		public void run()
		{
			try
			{
				String ret = msgItem.msg;
				String translateMsg = "";
				String originalLang = "";
				String msgStr = msgItem.msg;
				boolean needPreProcess = msgItem.needTranslatePreProcess();
				if(needPreProcess)
					msgStr = msgItem.getProcessedMsg();
//				if(false)
//				{
//					if(!msgItem.hasTranslated)
//					{
//						String originalLanguage = msgItem.getLang();
//						if(msgItem.isOriginalLangInValid() || !msgItem.isTranslateMsgValid())
//							originalLanguage = "";
//						ret = translateByGoogle(msgStr, originalLanguage);
//						if(StringUtils.isNotEmpty(ret) && !ret.contains("<!DOCTYPE html>"))
//						{
//							if((!(ret.contains("[") && ret.contains("]")) && (ret.startsWith("\"") && ret.endsWith("\""))) || ret.equals("\""+msgItem.msg+"\""))
//							{
//								if(ret.length()>=2 && !(msgItem.msg.startsWith("\"") && msgItem.msg.endsWith("\"")))
//									translateMsg = ret.substring(1, ret.length()-1);
//								else
//									translateMsg = ret;
//								if(StringUtils.isNotEmpty(translateMsg))
//									translateMsg = translateMsg.replaceAll("\\\\n", "\n");
//							}
//							else
//							{
//								JSONArray jsonArray = JSON.parseArray(ret);
//								if(jsonArray.size() >= 2)
//								{
//									translateMsg = jsonArray.getString(0);
//									if(StringUtils.isNotEmpty(translateMsg))
//										translateMsg = translateMsg.replaceAll("\\\\n", "\n");
//									originalLang = jsonArray.getString(1);
//								}
//							}
//						}
//					}
//					else
//					{
//						translateMsg = msgItem.translateMsg;
//						originalLang = msgItem.getLang();
//					}
//					
//				}
//				else
//				{
//					
					if(client_translate_company_new)
						ret = translateByServer(msgItem.msg, msgItem.getLang());
					else
						ret = translateNew(msgItem.msg, msgItem.getLang());
					TranslateNewParams params = JSON.parseObject(ret, TranslateNewParams.class);
					translateMsg = params.getTranslateMsg();
					originalLang = params.getOriginalLang();
//				}
				

				if (StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
				{
					if (handler != null)
					{
						Message msg = new Message();
						Bundle data = new Bundle();
						data.putString("translateMsg", "");
						msg.setData(data);
						handler.sendMessage(msg);
					}
					return;
				}

				if (!msgItem.isTranlateDisable() && !msgItem.isOriginalSameAsTargetLang())
					msgItem.hasTranslated = true;
				else
					msgItem.hasTranslated = false;

				msgItem.translateMsg = translateMsg;
				if(needPreProcess)
					msgItem.postProcessTranslateMsg();
				msgItem.originalLang = originalLang;
				msgItem.translatedLang = ConfigManager.getInstance().gameLang;

				ChatChannel channel = null;
				if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
						&& msgItem.chatChannel != null)
				{
					channel = ChannelManager.getInstance().getChannel(msgItem.channelType, msgItem.chatChannel.channelID);
				}
				else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
						|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
						|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				{
					channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
				}
				if (channel != null)
				{
					DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
				}

				if (handler != null)
				{
					msgItem.hasTranslated = true;
					msgItem.isTranslatedByForce = true;
					msgItem.hasTranslatedByForce = true;

					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("translateMsg", msgItem.translateMsg);
					msg.setData(data);
					handler.sendMessage(msg);
				}

			}
			catch (Exception e)
			{
				// LogUtil.trackMessage("JSON.parseObject exception on server" +
				// UserManager.getInstance().getCurrentUser().serverId);
			}
			finally
			{
				if(msgItem!=null)
					msgItem.isTranslating = false;
			}
		}

	}
	
	public String translateDevelop(final String srcMsg, final String translateMsg,final String orginalLang)
	{
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(TRANSLATE_DEVELOP_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			BasicNameValuePair sc = new BasicNameValuePair("sc", srcMsg);
			String originalLangStr = TranslateManager.getInstance().getTranslateLang(orginalLang);
			BasicNameValuePair sf = new BasicNameValuePair("sf", originalLangStr);
			String translateLang = TranslateManager.getInstance().getTranslateLang(ConfigManager.getInstance().gameLang);
			BasicNameValuePair tf = new BasicNameValuePair("tf", translateLang);
			BasicNameValuePair ch = new BasicNameValuePair("tc", translateMsg);
			String currentTime = Long.toString(System.currentTimeMillis());
			BasicNameValuePair t = new BasicNameValuePair("t", currentTime);
			String md5 = MD5.getMD5Str(originalLangStr + translateLang + currentTime + "feedback@cok");
			BasicNameValuePair sig = new BasicNameValuePair("sig", md5);

			params.add(sc);
			params.add(sf);
			params.add(tf);
			params.add(ch);
			params.add(t);
			params.add(sig);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);
			if(httpResponse.getStatusLine()!=null)
			{
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "statusCode",statusCode);
				if(statusCode != 200 )
					return "";
			}
			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			return responseStr;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return srcMsg;
	}
	
	public String translateByServer(final String srcMsg, final String orginalLang)
	{
		String transalteUrl = "";
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			if(translateByAgentEnable && canTranslateByAgent && !google_server_available)
				transalteUrl = NEW_SERVER_TRANSLATE_AGENT_URL;
			else
				transalteUrl = NEW_SERVER_TRANSLATE_URL;
			HttpPost post = new HttpPost(transalteUrl);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			BasicNameValuePair sc = new BasicNameValuePair("sc", srcMsg);
			String originalLangStr = TranslateManager.getInstance().getTranslateLang(orginalLang);
			BasicNameValuePair sf = new BasicNameValuePair("sf", originalLangStr);
			String translateLang = TranslateManager.getInstance().getTranslateLang(ConfigManager.getInstance().gameLang);
			BasicNameValuePair tf = new BasicNameValuePair("tf", translateLang);
			BasicNameValuePair ch = new BasicNameValuePair("ch", "cokweb");
			String currentTime = Long.toString(System.currentTimeMillis());
			BasicNameValuePair t = new BasicNameValuePair("t", currentTime);
			String md5 = MD5.getMD5Str(originalLangStr + translateLang + "cokweb" + currentTime + "jv89#klnme_*@cokweb");
			BasicNameValuePair sig = new BasicNameValuePair("sig", md5);

			params.add(sc);
			params.add(sf);
			params.add(tf);
			params.add(ch);
			params.add(t);
			params.add(sig);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);
			if(httpResponse.getStatusLine()!=null)
			{
				if(httpResponse.getStatusLine().getStatusCode() != 200 && StringUtils.isNotEmpty(transalteUrl) && transalteUrl.equals(NEW_SERVER_TRANSLATE_AGENT_URL))
				{
					canTranslateByAgent = false;
					return "";
				}
			}
			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			return responseStr;
		}
		catch (Exception e)
		{
			if(StringUtils.isNotEmpty(transalteUrl) && transalteUrl.equals(NEW_SERVER_TRANSLATE_AGENT_URL))
				canTranslateByAgent = false;
			e.printStackTrace();
		}
		return srcMsg;
	}

	public String translateNew(final String srcMsg, final String orginalLang)
	{
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(SERVER_TRANSLATE_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			BasicNameValuePair sc = new BasicNameValuePair("sc", srcMsg);
			String originalLangStr = TranslateManager.getInstance().getTranslateLang(orginalLang);
			BasicNameValuePair sf = new BasicNameValuePair("sf", originalLangStr);
			String key = TranslateManager.getInstance().getTranslateLang(ConfigManager.getInstance().gameLang);
			String translateLang = "[\"" + key + "\"]";
			BasicNameValuePair tf = new BasicNameValuePair("tf", translateLang);
			BasicNameValuePair ch = new BasicNameValuePair("ch", "cok");
			String currentTime = Long.toString(System.currentTimeMillis());
			BasicNameValuePair t = new BasicNameValuePair("t", currentTime);
			String md5 = MD5.getMD5Str(srcMsg + originalLangStr + translateLang + "cok" + currentTime + "jv89#klnme_*@");
			BasicNameValuePair sig = new BasicNameValuePair("sig", md5);

			params.add(sc);
			params.add(sf);
			params.add(tf);
			params.add(ch);
			params.add(t);
			params.add(sig);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);

			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			return responseStr;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return srcMsg;
	}
	
	public String translateGoogle(final String srcMsg, final String orginalLang)
	{
		InputStream inputStream = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BufferedInputStream bufferInputStream = null;
		String engineUrl = "http://translate.google.com/translate_a/t?";

		String originalLangStr = getTranslateLangForGoogle(orginalLang);
		String translateLang = getTranslateLangForGoogle(ConfigManager.getInstance().gameLang);
		String token = generateToken(originalLangStr, translateLang, srcMsg);
		String params = "client=webapp&hl=en";
    	params = appendParams(params, "sl", originalLangStr);
    	params = appendParams(params, "tl", translateLang);
    	params += "&ie=UTF-8&oe=UTF-8&multires=1&otf=0&pc=1&trs=1&ssel=0&tsel=0&kc=1";
    	params = appendParams(params, "tk", token);
    	params = appendParams(params, "q", TranslateUtil.encodeText(srcMsg));
    	params = params.replaceAll("/%5B(?:[0-9]|[1-9][0-9]+)%5D=/", "=");
		String urlstr = engineUrl + params;
		URL url = null;
		String res = "";
		HttpURLConnection connection = null;
		try
		{
			url = new URL(urlstr);
			connection = (HttpURLConnection) url.openConnection();
			String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
			long randomType = Math.round(Math.random() * 3 + 1);
			switch ((int) randomType)
			{
				case 1:
					userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
					break;
				case 2:
					userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
					break;
				case 3:
					userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; fr-fr; MB525 Build/JRO03H; CyanogenMod-10) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
					break;
				case 4:
					userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_4) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30";
					break;
				default:
					userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
					break;

			}
			connection.setRequestProperty("User-Agent", userAgent);
			connection.connect();
			inputStream = connection.getInputStream();
			bufferInputStream = new BufferedInputStream(inputStream, 4096);
			int i = -1;
			byte buf[] = new byte[4 * 1024];
			while ((i = bufferInputStream.read(buf)) != -1)
			{
				output.write(buf, 0, i);
			}
			res = new String(output.toByteArray(), "UTF-8");
			inputStream.close();
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return res;
	}
	
	public String translateByGoogle(final String srcMsg, final String orginalLang)
	{
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost("http://translate.google.com/translate_a/t");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String originalLangStr = getTranslateLangForGoogle(orginalLang);
			String translateLang = getTranslateLangForGoogle(ConfigManager.getInstance().gameLang);
			if(StringUtils.isEmpty(originalLangStr))
				originalLangStr = "auto";
			String token = generateToken(originalLangStr, translateLang, srcMsg);
		//	System.out.println("srcMsg:"+srcMsg+"  originalLangStr:"+originalLangStr+"  translateLang:"+translateLang+"  token:"+token);
			
			BasicNameValuePair client = new BasicNameValuePair("client", "webapp");
			BasicNameValuePair hl = new BasicNameValuePair("hl", "en");
			BasicNameValuePair sl = new BasicNameValuePair("sl", originalLangStr);
			BasicNameValuePair tl = new BasicNameValuePair("tl", translateLang);
			BasicNameValuePair ie = new BasicNameValuePair("ie", "UTF-8");
			BasicNameValuePair oe = new BasicNameValuePair("oe", "UTF-8");
			BasicNameValuePair multires = new BasicNameValuePair("multires", "1");
			BasicNameValuePair otf = new BasicNameValuePair("otf", "0");
			BasicNameValuePair pc = new BasicNameValuePair("pc", "1");
			BasicNameValuePair trs = new BasicNameValuePair("trs", "1");
			BasicNameValuePair ssel = new BasicNameValuePair("ssel", "0");
			BasicNameValuePair tsel = new BasicNameValuePair("tsel", "0");
			BasicNameValuePair kc = new BasicNameValuePair("kc", "1");
			BasicNameValuePair tk = new BasicNameValuePair("tk", token);
			BasicNameValuePair q = new BasicNameValuePair("q", srcMsg);
			
			params.add(client);
			params.add(hl);
			params.add(sl);
			params.add(tl);
			params.add(oe);
			params.add(ie);
			params.add(multires);
			params.add(otf);
			params.add(pc);
			params.add(trs);
			params.add(ssel);
			params.add(tsel);
			params.add(kc);
			params.add(tk);
			params.add(q);
			
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			post.setHeader("User-Agent", getUserAgent());
			HttpResponse httpResponse = httpClient.execute(post);
			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			return responseStr;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return srcMsg;
	}
	

	public void loadTranslation(final MsgItem msgItem, final TranslateListener translateListener)
	{
		if (msgItem == null|| StringUtils.isEmpty(msgItem.msg) || msgItem.canNotShowTranslateQuickActionMenu() || msgItem.isTranslateMsgValid())
		{
			if(msgItem!=null)
				msgItem.isTranslating = false;
			if (translateListener != null)
				translateListener.onTranslateFinish("");
			return;
		}

		Handler handler = null;
		if (translateListener != null)
		{
			handler = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{
					try
					{
						super.handleMessage(msg);
						Bundle data = msg.getData();
						String translateMsg = data.getString("translateMsg");
						if (translateListener != null)
						{
							translateListener.onTranslateFinish(translateMsg);
						} 
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}

				}
			};
		}

		if (executorService != null)
			executorService.execute(new TranslateRunnable(msgItem, handler));
	}
	
	public void submitTranslateDevelop(final MsgItem msgItem)
	{
		if(!ChatServiceController.translateDevelopEnable)
			return;
		if (executorService != null)
			executorService.execute(new TransalteDevelopRunnable(msgItem));
	}

	public String getTranslateLang(String originalLang)
	{
		if (isZh_CN(originalLang))
			return "zh-Hans";
		else if (isZh_TW(originalLang))
			return "zh-Hant";
		return originalLang;
	}
	
	public String getTranslateLangForGoogle(String originalLang)
	{
		if (isZh_CN(originalLang))
			return "zh-CN";
		else if (isZh_TW(originalLang))
			return "zh-TW";
		return originalLang;
	}
	
	public String generateToken(String sourceLang,String targetLang,String text)
	{
		long base1 = 406398;
		long base2 = 561666268 + 1526272306;
		int len = text.length();
		long[] codeArray = new long[len*3];
		int count = 0;
		for (int i = 0; i < len; i++) {
			int code = text.codePointAt(i);
            if (128 > code) {
            	codeArray[count++] = code;
            } else {
                if (2048 > code) {
                	codeArray[count++] = code >> 6 | 192;
                } else {
                    if (55296 == (code & 64512) && i + 1 < len && 56320 == (text.codePointAt(i+1) & 64512)) {
                    	code = 65536 + ((code & 1023) << 10) + (text.codePointAt(++i) & 1023);
                    	codeArray[count++] = code >> 18 | 240;
                    	codeArray[count++] = code >> 12 & 63 | 128;
                    } else {
                    	codeArray[count++] = code >> 12 | 224;
                    	codeArray[count++] = code >> 6 & 63 | 128;
                    }
                }
                codeArray[count++] = code & 63 | 128;
            }
        }
		long tempBase = base1;
        for (int i = 0; i < count; i++) {
        	tempBase += codeArray[i];
        	tempBase = processToken(tempBase, "+-a^+6");
        }
        tempBase = processToken(tempBase, "+-3^+b+-f");
        tempBase ^= base2;
        if (0 > tempBase) {
        	tempBase = (int)(((long)tempBase & 2147483647) + 2147483648L);
        }
        tempBase = tempBase % (int)(Math.pow(10, 6));
        return tempBase+"."+(tempBase ^ base1);
	}
    
    private long processToken(long a, String b)
    {
    	long result = a;
        for (int i = 0; i < b.length() - 2; i += 3) {
            String d = b.substring(i+2, i+3);
            long intValue = StringUtils.isNumeric(d) ? Integer.parseInt(d) : 0;
            long res = (d.compareTo("a") >= 0) ? d.codePointAt(0) - 87 : intValue;
            long shrd = shr32(result, res);
            String temp = b.substring(i+1, i+2);
            res = temp.equals("+") ? shrd : result << res;
            String temp2 = b.substring(i, i+1);
            result = temp2.equals("+") ? (result + res & 4294967295L) : result ^ res;
        }
        return result;
    }
    
    private long shr32(long x, long bits)
    {
        if (bits <= 0) {
            return x;
        }
        if (bits >= 32) {
            return 0;
        }
        String bin = Long.toBinaryString(x);
        int len = bin.length();
        if (len > 32) {
        	bin = bin.substring(len-32, 32);
        } 
        else if (len < 32) {
        	bin = formatStr(32-len,bin);
        }
        String bin2 = bin.substring(0, 32-(int)bits);
        return Long.valueOf(bin2, 2).longValue();
    }
    
    private String formatStr(int len,String str)
    {
    	StringBuilder builder = new StringBuilder();
    	for(int i = 0;i<len;i++)
    		builder.append("0");
    	builder.append(str);
    	return builder.toString();
    }
    
    private String getUserAgent()
    {
    	String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
		long randomType = Math.round(Math.random() * 3 + 1);
		switch ((int) randomType)
		{
			case 1:
				userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
				break;
			case 2:
				userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
				break;
			case 3:
				userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; fr-fr; MB525 Build/JRO03H; CyanogenMod-10) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
				break;
			case 4:
				userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_4) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30";
				break;
			default:
				userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
				break;

		}
		return userAgent;
    }
    
    private String appendParams(String params,String key,String value)
    {
    	if(StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value))
    	{
    		if(StringUtils.isNotEmpty(params))
    			params+="&";
    		return params+=(key+"="+value);
    	}
    	return params;
    }
    
    private void testGoogleAvailable()
    {
    	executorService.execute(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
					HttpConnectionParams.setSoTimeout(httpParams, 20000);
					HttpClient httpClient = new DefaultHttpClient(httpParams);
					HttpPost post = new HttpPost(GOOGLE_SERVER_TEST_URL);
					post.setHeader("User-Agent", getUserAgent());
					HttpResponse httpResponse = httpClient.execute(post);
					if(httpResponse!=null && httpResponse.getStatusLine()!=null && httpResponse.getStatusLine().getStatusCode() == 204)
						google_server_available = true;
					else
						google_server_available = false;
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "google_server_available", google_server_available);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
    }
}
