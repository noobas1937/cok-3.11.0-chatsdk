package com.elex.im.core.net;

import java.util.ArrayList;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.ChannelChangeEvent;
import com.elex.im.core.event.EventCenter;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.MailAudioContent;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.core.util.TranslateManager;

public class SyncController
{
	private static SyncController	instance;

	public static SyncController getInstance()
	{
		if (instance == null)
		{
			instance = new SyncController();
		}
		return instance;
	}

	protected SyncController()
	{
	}

	/**
	 * 对消息的预处理，主要是为了渲染而做的准备。
	 * 收到新消息和加载历史消息都会先走这里，应该只做预处理，不触发后续操作
	 */
	public static void handleMessage(final Msg[] chatInfoArr, final String channelId, final String customName,
			final boolean calulateUnread, final boolean isFromServer)
	{
		if(chatInfoArr == null || chatInfoArr.length == 0)
			return;

		int channelType = chatInfoArr[0].channelType;
		Channel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
		if (channel == null)
			return;
		
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			Msg msg = chatInfoArr[i];
			msg.setChannel(channel);
			
			// 检查用户信息，放到ui中
			msg.initUserForReceivedMsg(channelId, customName);

			// 翻译
			if (isFromServer && msg.hasTranslation())
				msg.translatedLang = ConfigManager.getInstance().gameLang;
			if (TranslateManager.getInstance().hasTranslated(msg))
				msg.hasTranslated = true;
			else
				msg.hasTranslated = false;
			if (ConfigManager.autoTranlateMode > 0)
			{
				TranslateManager.getInstance().loadTranslation(msg, null);
			}
			
			// 语音消息文字内容生成
			if(msg.isAudioMessage() && IMCore.getInstance().getAppConfig().isExtendedAudioMsg(msg.getChannel())
					&& StringUtils.isNotEmpty(msg.msg) && StringUtils.isEmpty(msg.media))
			{
				try
				{
					MailAudioContent content = JSON.parseObject(msg.msg, MailAudioContent.class);
					if (content != null)
					{
						msg.msg = content.getAudio_time();
						msg.media = content.getMedia();
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		boolean isNewMessage = chatInfoArr[0].isNewMsg;

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "channelId", channelId,
				"isFromServer", isFromServer);
		if (isFromServer)
		{
			save2DB(chatInfoArr, channel, customName);
		}
		
		// 插入数据，修改状态
		insertMessage(channel, isNewMessage, chatInfoArr, channelId, customName, isFromServer);
	}
	
	private static void insertMessage(final Channel channel, final boolean isNewMessage, final Msg[] chatInfoArr,
			final String channelId, String customName, boolean isFromServer)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "size", chatInfoArr.length, "isNewMessage", isNewMessage,
				"isFromServer", isFromServer);
		if (channel == null || chatInfoArr.length <= 0)
			return;

		// 应该已经没用了，ws不会传，个人邮件和聊天室用
//		channel.setCustomName(customName);

		if (isNewMessage)
		{
			onReceiveNewMsg(chatInfoArr, channel);
			
			// 存在chatfragment时的处理
			EventCenter.getInstance().dispatchEvent(new ChannelChangeEvent(ChannelChangeEvent.ON_RECIEVE_NEW_MSG, channel, chatInfoArr, hasSelfMsg(chatInfoArr)));
			
			// 不存在chatfragment时的处理
//			ChatFragmentNew.onMsgAdd(channelType, channel.channelID, true);
//			if (recievedMsg!=null && recievedMsg.canEnterScrollTextQueue())
//			{
//				ScrollTextManager.getInstance().clear(channelType);
//				ScrollTextManager.getInstance().push(recievedMsg,channelType);
//			}
		}
		else
		{
			Msg oldFirstItem = null;
			if (channel.msgList != null && channel.msgList.size() > 0)
				oldFirstItem = channel.msgList.get(0);
			
			int loadCount = onLoadLocalMsg(chatInfoArr, channel);
			EventCenter.getInstance().dispatchEvent(new ChannelChangeEvent(ChannelChangeEvent.ON_RECIEVE_OLD_MSG, channel, chatInfoArr, oldFirstItem, loadCount));
		}

//		if (isFromServer)
//		{
//			// 会触发reload，仅在服务器端来了新消息才调用
//			ChannelListFragment.onMsgAdded(channel);
//		}
//
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelType", channelType, "channel.channelType",
//				channel.channelType, "isWebSocketEnabled()", WebSocketManager.isWebSocketEnabled(), "isRecieveFromWebSocket",
//				ConfigManager.isRecieveFromWebSocket);
//
//		if (channel!=null && isNewMessage && (WebSocketManager.isRecieveFromWebSocket(channelType) && (channel.isCountryChannel() 
//				|| channel.isAllianceChannel()
//				|| channel.isBattleChannel()
//				|| channel.isCustomChannelNeedSendLatestMsg())))
//		{
//			sendChatLatestMessage(channel);
//		}
	}
	
	private static boolean hasSelfMsg(final Msg[] chatInfoArr)
	{
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			if(chatInfoArr[i].isSelfMsg)
				return true;
		}
		return false;
	}

	private static void onReceiveNewMsg(final Msg[] chatInfoArr, Channel channel)
	{
		ArrayList<Msg> msgList = channel.msgList;
		ArrayList<Msg> sendingMsgList = channel.sendingMsgList;
		
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			Msg sendingMsg = null;
			Msg recievedMsg = chatInfoArr[i];

			if (msgList != null && msgList.size() > 0)
			{
				for (int j = 0; j < sendingMsgList.size(); j++)
				{
					Msg sendMsg = sendingMsgList.get(j);
					if (sendMsg == null)
						continue;
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "sendMsg.msg", sendMsg.msg,
							"sendMsg.sendLocalTime", sendMsg.sendLocalTime);
					if (sendMsg.sendLocalTime != 0 && sendMsg.sendLocalTime == recievedMsg.sendLocalTime)
					{
						sendingMsg = sendMsg;
					}
				}
			}

			// TODO 判断方法修改，检查发送者的uid
			//  && (!recievedMsg.isSystemMessage() || recievedMsg.isHornMessage())
			if (sendingMsg != null && recievedMsg.isSelfMsg())
			{
				// 我发的消息
				sendingMsg.sendState = Msg.SEND_SUCCESS;
				sendingMsgList.remove(sendingMsg);
				channel.replaceDummyMsg(recievedMsg, msgList.indexOf(sendingMsg));
			}
			else
			{
				// 别人发的消息
				channel.addNewMsg(recievedMsg);
			}
			if(channel.getLatestTime() < TimeManager.getTimeInMS(recievedMsg.createTime))
			{
				channel.setLatestTime(recievedMsg.createTime);
			}
		}
		
		DBManager.getInstance().getMsgDAO().insertMsgs(chatInfoArr);
		DBManager.getInstance().getChannelDAO().update(channel);
	}

	private static int onLoadLocalMsg(final Msg[] chatInfoArr, Channel channel)
	{
		// 用于界面显示时，时间显示的分组
		int loadCount = 0;
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			boolean isAddSuccess = channel.addHistoryMsg(chatInfoArr[i]);
			if (isAddSuccess) loadCount++;
		}
		return loadCount;
	}
	
	private static void save2DB(Msg[] infoArr, final Channel channel, String customName)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "size",
				infoArr.length);

		if (infoArr.length == 0 || channel == null)
			return;

		// 应该已经没用了，ws不会传，个人邮件和聊天室用
//		channel.setCustomName(customName);
		
//		DBManager.getInstance().getMsgDAO().insertMsgs(infoArr);
	}
}
