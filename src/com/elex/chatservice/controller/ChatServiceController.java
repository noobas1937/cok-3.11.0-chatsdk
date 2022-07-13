package com.elex.chatservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.chatservice.R;
import com.elex.chatservice.host.DummyHost;
import com.elex.chatservice.host.GameHost;
import com.elex.chatservice.host.IHost;
import com.elex.chatservice.model.AllianceOfficerAttachment;
import com.elex.chatservice.model.AllianceSkillInfo;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.DriftingBottleContent;
import com.elex.chatservice.model.InputAtContent;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LatestChannelChatInfo;
import com.elex.chatservice.model.LatestChatInfo;
import com.elex.chatservice.model.MailAudioContent;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.TranslateManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.AllianceTaskInfo;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.AbstractBaseActivity;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;
import com.elex.chatservice.view.ChatActivity;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.ChatRoomSettingActivity;
import com.elex.chatservice.view.MainListFragment;
import com.elex.chatservice.view.MemberSelectorFragment;
import com.elex.chatservice.view.MsgMailListFragment;
import com.elex.chatservice.view.SysMailListFragment;
import com.elex.chatservice.view.actionbar.ActionBarFragment;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.actionbar.RecyclerActionBarActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareCommentListActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareDetailActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareListActivity;
import com.elex.chatservice.view.allianceshare.ImageDetailFragment;
import com.elex.chatservice.view.allianceshare.ImagePagerActivity;
import com.elex.chatservice.view.emoj.EmojSubscribActivity;
import com.elex.chatservice.view.kurento.RealtimeVoiceRoomSettingActivity;
import com.elex.chatservice.view.lbs.NearByActivity;
import com.elex.chatservice.view.recyclerrefreshview.AbstractRecyclerActivity;
import com.elex.chatservice.view.recyclerrefreshview.RecyclerMainListActivity;
import com.elex.chatservice.view.recyclerrefreshview.RecyclerSysMailActivity;

public class ChatServiceController
{
	public static Activity					hostActivity;																	// IF或wrapper的activity，原生未打开时依然存在
	private static Class<?>					hostClass;
	private static Activity					currentActivity;
	public IHost							host;

	public static boolean					isNativeShowing					= false;										// 仅在IF.onResume中重置为false，主要被IF使用
	/** 正在打开native界面，可能是从游戏打开，或从native打开另一个新的，或退出当前的native显示堆栈中的另一个 */
	public static boolean					isNativeOpenning				= false;										// 主要被原生activity使用

	// C++传入的参数
	// public static boolean isContactMod = false;
	public static int						contactMode						= 0;
	public static boolean					isHornItemUsed					= false;										// 是否使用喇叭道具
	public static boolean					isCreateChatRoom				= false;
	private static int						currentChatType					= -1;											// 刚进入时由C++设置，在java中可修改，退出后会再给C++

	public static boolean					isNewMailListEnable				= false;										// 是否使用新的邮件列表
	public static boolean					isNewMailUIEnable				= true;
	public static int						serverId;
	/** crossFightSrcServerId = -1 表示没有跨服， >=0表示现在处于跨服状态 */
	public static int						crossFightSrcServerId;
	public static boolean					isReturningToGame				= false;										// 仅在打开原生activity时重置为false，在IF.onResume中重置false的话，会导致无法记忆二级邮件列表
	public static int						sortType						= -1;
	public static boolean					isDefaultTranslateEnable		= true;											// 默认翻译开关
	public static boolean					isFriendEnable					= true;											// 好友功能开关
	public static boolean					isDetectInfoEnable				= false;										// 侦察战报更新开关

	public static long						oldReportContentTime			= 0;
	public static long						REPORT_CONTENT_TIME_INTERVAL	= 30000;
	public static int						red_package_during_time			= 24;											// 红包到期时间
	public static String					kingUid							= "";											// 国王的UID
	public static String					banTime							= "1|2|3|4";									// 国王的UID
	public static boolean					isListViewFling					= false;
	public static int						serverType						= -1;
	public static String					originalServerName				= "";
	public static boolean					needShowAllianceDialog			= false;										// 需要在联盟聊天输入框显示特定的dialog
	public static String					switch_chat_k10					= "cn_uc,cn1,cn_mihy,cn_wdj,cn_ewan,cn_anzhi";
	public static String					switch_chat_k11					= "5|6";
	public static int						currentLevel					= 1;
	public static int						currentMainCityLevel			= 1;
	public static boolean					nameNicked						= false;
	public static int						sendTimeTextHeight				= 0;
	public static boolean					isNewYearStyleMsg				= false;
	private Timer							audioTimer						= null;
	private TimerTask						audioTimerTask					= null;
	private int								currentAoduiSendLocalTime		= 0;
	private static boolean					gameMusicEnable					= true;
	public static boolean					isKingdomBattleStart			= false;
	public static boolean					isAnicientBattleStart			= false;
	public static boolean					isDragonBattleStart				= false;
	public static boolean					isDragonPlayOffStart			= false;
	public static String					dragonRoomId					= "";
	public static boolean					isBattleChatEnable				= false;
	public static boolean					isAllianceTreasureHide			= false;
	public static int						newLastUpdateTime				= 0;
	public static int						kingdomBattleEnemyServerId		= -1;
	public static boolean					allianceSysChannelEnable		= false;
	public static boolean					countrySysChannelEnable			= false;
	public static boolean					randomChatEnable				= false;
	public static boolean					name_check_enable				= false;
	public static boolean					isValidateChat					= false;
	public static boolean					dragonObserverEnable			= false;
	public static boolean					dragonObserverDanmuEnable		= false;
	public static String					dragonObserverRoomId			= "";
	public static String					dragonObserverDanmuRoomId		= "";
	public static boolean					isInDragonObserverRoom			= false;
	public static boolean					translateDevelopEnable			= false;
	public static boolean					expressionPanelEnable			= false;
	public static boolean					expressionSubEnable				= false;
	public static boolean					isPressToSpeakVoiceMode			= false;
	public static boolean					realtime_voice_enable			= false;
	public static boolean          redPakcageOpenEnable      = true;
	public static int            redEnvelopesCastleLevel      = 0;

	public static final int					NORMAL							= 0;											// 普通服
	public static final int					TEST							= 1;											// 外网测试服
	public static final int					ANCIENT_BATTLE_FIELD			= 2;											// 远古战场服务器
	public static final int					DRAGON_BATTLE					= 3;											// 巨龙战役服务器
	public static final int					ANCIENT_BATTLE_FIELD_TEST		= 4;											// 远古战场服务器测试
	public static final int					DRAGON_BATTLE_TEST				= 5;											// 巨龙战役服务器测试
	public static final int					INNER_TEST						= 6;											// 内网测试服
	public static final int					DRAGON_PLAYOFF					= 7;											// 巨龙季后赛服务器
	public static final int					DRAGON_PLAYOFF_TEST				= 8;											// 巨龙季后赛测试服

	private static ChatServiceController	instance;

	public static boolean isAllianceTreasureHiden()
	{
		return isAllianceTreasureHide && !allianceSysChannelEnable;
	}

	public static boolean canUserEmojPanel()
	{
		return expressionPanelEnable && !isModContactMode() && !isDriftingBottleContactMode();
	}

	public static ChatServiceController getInstance()
	{
		if (instance == null)
		{
			synchronized (ChatServiceController.class)
			{
				if (instance == null)
					instance = new ChatServiceController();
			}
		}
		return instance;
	}

	private ChatServiceController()
	{
	}

	public static boolean isModContactMode()

	{
		
		 Log.d("ChatServiceController", "onCreate: 判断CHANNEL_ID_POSTFIX_MOD"+contactMode);
		return isModContactMod(contactMode);
	}

	public static boolean isDriftingBottleContactMode()
	{
		return isDriftingBottleContactMod(contactMode);
	}

	public static boolean isNearbyContactMode()
	{
		return isNearbyContactMod(contactMode);
	}

	public static boolean isModContactMod(int contactMode)
	{
		return contactMode == 1;
	}

	public static boolean isDriftingBottleContactMod(int contactMode)
	{
		return contactMode == 2;
	}

	public static boolean isNearbyContactMod(int contactMode)
	{
		return contactMode == 3;
	}

	public static boolean isNotTestServer()
	{
		return serverType != NORMAL && serverType != ANCIENT_BATTLE_FIELD && serverType != DRAGON_BATTLE && serverType != DRAGON_PLAYOFF;
	}

	public boolean needShowBattleFieldChannel()
	{
		return (ChatServiceController.isBattleChatEnable && (isKingdomBattleStart || (isAnicientBattleStart && canEnterAnicient())
				|| isDragonBattleStart || (UserManager.getInstance().isInBattleField() && !isInAncientSencen())
				|| (isInAncientSencen() && canEnterAnicient()) || canJoinDragonRoom()))
				|| canEnterDragonObserverRoom();
	}

	public boolean canEnterAnicient()
	{
		return currentMainCityLevel >= 15;
	}

	public boolean isInKingdomBattleField()
	{
		return ChatServiceController.isBattleChatEnable && (isKingdomBattleStart || UserManager.getInstance().isInBattleField())
				&& !isInAncientSencen() && !canJoinDragonRoom() && !canEnterDragonObserverRoom();
	}

	public boolean needCrossServerBattleChat()
	{
		return ChatServiceController.isBattleChatEnable
				&& (isKingdomBattleStart || (UserManager.getInstance().isInBattleField() && !isInAncientSencen())
						|| (isInAncientSencen() && canEnterAnicient()) || canJoinDragonRoom() || canEnterDragonObserverRoom());
	}

	public boolean needShowBattleTipLayout()
	{
		return ChatServiceController.isBattleChatEnable
				&& (!isKingdomBattleStart && !UserManager.getInstance().isInBattleField()
						&& (isAnicientBattleStart || isDragonBattleStart || isDragonPlayOffStart) && !isInAncientSencen() && !canJoinDragonRoom())
				&& !canEnterDragonObserverRoom();
	}

	public boolean needShowDragonObserverTipLayout()
	{
		return ChatServiceController.dragonObserverEnable && canEnterDragonObserverRoom()
				&& !(SwitchUtils.mqttEnable ? MqttManager.isInDragonObserverRoom() : WebSocketManager.isInDragonObserverRoom());
	}

	public static boolean isInDragonSencen()
	{
		return serverType == 3 && UserManager.getInstance().isInBattleField() && !isInDragonObserverRoom;
	}

	public static boolean canJoinDragonRoom()
	{
		return isInDragonSencen() && StringUtils.isNotEmpty(dragonRoomId) && !dragonRoomId.equals("dragon_") && !isInDragonObserverRoom;
	}

	public static boolean canEnterDragonObserverRoom()
	{
		return dragonObserverEnable && isInDragonObserverRoom && StringUtils.isNotEmpty(dragonObserverRoomId);
	}

	public static boolean canEnterDragonObserverDanmuRoom()
	{
		return dragonObserverEnable && isInDragonObserverRoom && StringUtils.isNotEmpty(dragonObserverDanmuRoomId);
	}

	public static boolean isInAncientSencen()
	{
		return serverType == 2 && UserManager.getInstance().isInBattleField();
	}

	public static void init(Activity a, boolean isDummyHost)
	{
		hostActivity = a;
		hostClass = a.getClass();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "hostClass", hostClass.getName());

		if (!isDummyHost)
		{
			getInstance().host = new GameHost();
		}
		else
		{
			getInstance().host = new DummyHost();
		}
	}

	public void reset()
	{
		UserManager.getInstance().reset();
		TranslateManager.getInstance().reset();
		ChannelManager.getInstance().reset();
	}

	public static void setCurrentActivity(Activity a)
	{
		currentActivity = a;
	}
	
	public static Activity getCurrentActivity()
	{
		return currentActivity;
	}
	
	public void refreshRealtimeBtnText()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				((MyActionBarActivity)currentActivity).refreshRealtimeBtnText();
		}
	}
	
	
	public void hideProgressBar()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				((MyActionBarActivity)currentActivity).hideProgressBar();
			else if(currentActivity instanceof RecyclerActionBarActivity)
				((RecyclerActionBarActivity)currentActivity).hideProgressBar();
		}
	}
	
	
	public int getToastPosY()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				return ((MyActionBarActivity)currentActivity).getToastPosY();
			else if(currentActivity instanceof RecyclerActionBarActivity)
				return ((RecyclerActionBarActivity)currentActivity).getToastPosY();
		}
		return 0;
	}
	
	public void refreshNetWorkState()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				((MyActionBarActivity)currentActivity).refreshNetWorkState();
			else if(currentActivity instanceof RecyclerActionBarActivity)
				((RecyclerActionBarActivity)currentActivity).refreshNetWorkState();
		}
	}
	
	
	
	public boolean isSoftKeyBoardVisibile()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				return ((MyActionBarActivity)currentActivity).isSoftKeyBoardVisibile;
			else if(currentActivity instanceof RecyclerActionBarActivity)
				return ((RecyclerActionBarActivity)currentActivity).isSoftKeyBoardVisibile;
		}
		return false;
	}
	
	public static ActionBarFragment getCurrentFragment()
	{
		if(currentActivity!=null)
		{
			if(currentActivity instanceof MyActionBarActivity)
				return ((MyActionBarActivity)currentActivity).fragment;
		}
		return null;
	}

	// TODO 删除此函数
	public boolean isInDummyHost()
	{
		return false;// host instanceof DummyHost;
	}

	public boolean isUsingDummyHost()
	{
		return host instanceof DummyHost && ((DummyHost) host).treatAsDummyHost;
	}

	public boolean isActuallyUsingDummyHost()
	{
		return host instanceof DummyHost;
	}

	public static void setCurrentChannelType(int type)
	{
		currentChatType = type;
	}

	public static int getCurrentChannelType()
	{
		return currentChatType;
	}

	public static boolean isInNormalOrSysCountryChannel()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY || currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}

	public static boolean isInCountryOrSysCountryChannel()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}

	public static boolean isInAllianceOrSysAllianceChannel()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS;
	}

	public static boolean isInCountryOrAllianceChannel()
	{
		return isInCountryOrSysCountryChannel() || isInAllianceOrSysAllianceChannel();
	}

	public static boolean canShowSysIconInAdapter()
	{
		return (!allianceSysChannelEnable && !countrySysChannelEnable && (isInCountryOrAllianceChannel() || currentChatType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
				|| (allianceSysChannelEnable && !countrySysChannelEnable && isInCountryChannel())
				|| (!allianceSysChannelEnable && countrySysChannelEnable && isInAllianceChannel());
	}

	public static boolean isInCountryChannel()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY || currentChatType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
	}

	public static boolean isInAllianceChannel()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}

	private static long oldSendTime = 0; // 上一次发送时间

	private static boolean isSendIntervalValid()
	{
		boolean isValid = true;
		long sendTime = System.currentTimeMillis();
		if ((sendTime - oldSendTime) < ConfigManager.sendInterval)
		{
			Toast.makeText(currentActivity, LanguageManager.getLangByKey(LanguageKeys.TIP_SENDMSG_WARN), Toast.LENGTH_SHORT).show();
			isValid = false;
		}
		return isValid;
	}

	public static boolean isChatRestrict()
	{
		if (!name_check_enable)
			return false;
		boolean result = false;
		if (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY || currentChatType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			// UserInfo user = UserManager.getInstance().getCurrentUser();
			// String uid = UserManager.getInstance().getCurrentUserId();
			// if (StringUtils.isNotEmpty(uid) && uid.length() >= 3)
			// {
			// String uidPostfix = uid.substring(uid.length() - 3,
			// uid.length());
			// if (StringUtils.isNumeric(uidPostfix))
			// {
			// int serverId = Integer.parseInt(uidPostfix);
			// uidPostfix = "" + serverId;
			// if (user != null && StringUtils.isNotEmpty(user.userName))
			// {
			// if (user.userName.startsWith("Empire") &&
			// user.userName.endsWith(uidPostfix))
			// return true;
			// else
			// return false;
			// }
			// }
			//
			// }
			result = !nameNicked;
		}
		return result;
	}

	public static void sendDummyAudioMsg(long length, int sendLocalTime)
	{
		if (currentChatType < 0 || !isSendIntervalValid() || getChatFragment() == null)
			return;
		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}

		getChatFragment().clearInput();

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel == null)
			return;

		int post = MsgItem.MSG_TYPE_AUDIO;
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUserId(), true, true, channel.channelType, post, "" + length,
				sendLocalTime);
		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;

		msgItem.initUserForSendedMsg();
		if (channel.msgList != null && channel.msgList.size() > 0 && currentChatType != DBDefinition.CHANNEL_TYPE_USER)
		{
			MsgItem lastItem = channel.msgList.get(channel.msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}

		if (channel.isNearbyChannel() || contactMode == 3)
		{
			LogUtil.trackNearby("nearby_send");
			if (channel.msgList == null || channel.msgList.size() == 0)
				LogUtil.trackNearby("nearby_group");
		}

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);

		channel.addDummyMsg(msgItem);
		channel.getTimeNeedShowMsgIndex();
		// 发送后的行为（跳到最后一行）
		getChatFragment().afterSendMsgShowed();
		oldSendTime = System.currentTimeMillis();
	}

	public static void sendAudioMsgToServer(String media, String sendLocalTime)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel == null)
			return;
		// 实际发给后台

		int sendTime = 0;
		if (StringUtils.isNumeric(sendLocalTime))
			sendTime = Integer.parseInt(sendLocalTime);
		if (channel.sendingMsgList != null && channel.sendingMsgList.size() > 0)
		{
			MsgItem sendingItem = null;
			if (sendTime > 0)
			{
				for (MsgItem msgItem : channel.sendingMsgList)
				{
					if (msgItem != null && msgItem.sendLocalTime == sendTime)
					{
						sendingItem = msgItem;
						break;
					}
				}
			}
			else
			{
				sendingItem = channel.sendingMsgList.get(channel.sendingMsgList.size() - 1);
			}

			if (sendingItem != null)
			{
				sendingItem.media = media;
				if (channel.isUserChannelType())
				{
					if (channel.isDriftingBottleChannel())
					{
						try
						{
							DriftingBottleContent content = new DriftingBottleContent();
							content.setMedia_time(sendingItem.msg);
							content.setText(sendingItem.msg);
							content.setMedia(media);
							content.setType(1);
							sendingItem.msg = JSON.toJSONString(content);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							MailAudioContent content = new MailAudioContent();
							content.setAudio_time(sendingItem.msg);
							content.setMedia(media);
							sendingItem.msg = JSON.toJSONString(content);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}

				}
				System.out.println("sendAudioMsgToServer sendingItem.media:" + sendingItem.media + "  sendingItem.msg:" + sendingItem.msg);
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				{
					if (ChatServiceController.randomChatEnable)
						sendMsgToStandaloneServer(channel, sendingItem.msg, false, false, sendingItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, media);
				}
				else
				{
					if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
						sendMsg2Server(channel, sendingItem.msg, false, false, sendingItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, media);
					else
						sendMsgToStandaloneServer(channel, sendingItem.msg, false, false, sendingItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, media);
				}
			}
		}
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			LogUtil.trackAction("send_user_mail_audio");
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			LogUtil.trackAction("send_chatroom_audio");
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			LogUtil.trackAction("send_random_audio");

	}

	// 发送系统消息
	public static void sendMsg(String messageText, int post)
	{
		if (currentChatType < 0 || !isSendIntervalValid() || getChatFragment() == null)
			return;

		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}

		getChatFragment().clearInput();

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel == null)
		{
			LogUtil.trackMessage("sendMsg() channel is null: currentChatType=" + currentChatType + " fromUid="
					+ UserManager.getInstance().getCurrentMail().opponentUid);
			return;
		}

		int sendLocalTime = TimeManager.getInstance().getCurrentTime();

		int channelType = channel.channelType;
		// 创建消息对象，加入正在发送列表
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUserId(), true, true, channelType, post, messageText,
				sendLocalTime);
		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;
		msgItem.initUserForSendedMsg();
		if (channel.msgList != null && channel.msgList.size() > 0 && currentChatType != DBDefinition.CHANNEL_TYPE_USER)
		{
			MsgItem lastItem = channel.msgList.get(channel.msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}

		channel.sendingMsgList.add(msgItem);
		try
		{
			if (channel.isNearbyChannel() || contactMode == 3)
			{
				LogUtil.trackNearby("nearby_send");
				if (channel.msgList == null || channel.msgList.size() == 0)
					LogUtil.trackNearby("nearby_group");
			}
			channel.addDummyMsg(msgItem);
			channel.getTimeNeedShowMsgIndex();
			// 发送后的行为（跳到最后一行）
			getChatFragment().afterSendMsgShowed();
			trackSendAction(channel.channelType, false, false, msgItem);
			// 实际发给后台

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && channel.isDriftingBottleChannel())
			{
				try
				{
					DriftingBottleContent content = new DriftingBottleContent();
					content.setText(messageText);
					content.setType(0);
					messageText = JSON.toJSONString(content);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}

			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (ChatServiceController.randomChatEnable)
				{
					sendMsgToStandaloneServer(channel, messageText, false, false, sendLocalTime, msgItem.post, msgItem.media);
				}
			}
			else
			{
				if (ChatServiceController.isBattleChatEnable && channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					sendMsgToStandaloneServer(channel, messageText, false, false, sendLocalTime, msgItem.post, msgItem.media);
				}
				else
				{
					if (!WebSocketManager.isSendFromWebSocket(channel.channelType) && !channel.isNearbyChannel())
						sendMsg2Server(channel, messageText, false, false, sendLocalTime, msgItem.post, msgItem.media);
					else
						sendMsgToStandaloneServer(channel, messageText, false, false, sendLocalTime, msgItem.post, msgItem.media);
				}
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				LogUtil.trackAction("send_user_mail_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				LogUtil.trackAction("send_chatroom_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				LogUtil.trackAction("send_random_text");
			oldSendTime = System.currentTimeMillis();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	// 发送消息
	public static void sendMsg(String messageText, final boolean isHornMsg, boolean usePoint, String audioUrl)
	{
		// 极少情况会出现 chatActivity == null 或 chatActivity.chatFragment == null
		if (currentChatType < 0 || !isSendIntervalValid() || getChatFragment() == null)
			return;

		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}

		messageText = messageText.replaceAll("(\\n){2,}", "\\\n");

		getChatFragment().clearInput();

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel == null)
		{
			LogUtil.trackMessage("sendMsg() channel is null: currentChatType=" + currentChatType + " fromUid="
					+ UserManager.getInstance().getCurrentMail().opponentUid);
			return;
		}

		int sendLocalTime = TimeManager.getInstance().getCurrentTime();
		int post = isHornMsg ? 6 : MsgItem.MSGITEM_TYPE_MESSAGE;

		if (StringUtils.isNotEmpty(audioUrl))
		{
			post = MsgItem.MSG_TYPE_AUDIO;
		}

		int channelType = channel.channelType;
		// 创建消息对象，加入正在发送列表
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUserId(), true, true, channelType, post, messageText,
				sendLocalTime);
		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;
		if (StringUtils.isNotEmpty(audioUrl))
		{
			msgItem.media = audioUrl;
		}
		msgItem.initUserForSendedMsg();
		if (channel.msgList != null && channel.msgList.size() > 0 && currentChatType != DBDefinition.CHANNEL_TYPE_USER)
		{
			MsgItem lastItem = channel.msgList.get(channel.msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);

		// 加入model，更新视图
		try
		{
			if (channel.isNearbyChannel() || contactMode == 3)
			{
				LogUtil.trackNearby("nearby_send");
				if (channel.msgList == null || channel.msgList.size() == 0)
					LogUtil.trackNearby("nearby_group");
			}
			channel.addDummyMsg(msgItem);
			channel.getTimeNeedShowMsgIndex();
			// 发送后的行为（跳到最后一行）
			getChatFragment().afterSendMsgShowed();
			trackSendAction(channel.channelType, isHornMsg, false, msgItem);
			// 实际发给后台

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && channel.isDriftingBottleChannel())
			{
				try
				{
					DriftingBottleContent content = new DriftingBottleContent();
					content.setText(messageText);
					content.setType(0);
					messageText = JSON.toJSONString(content);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}

				// MsgItem[] chatInfoArr = new MsgItem[1];
				// MsgItem newMsgItem = new MsgItem(msgItem.uid, true, true,
				// channel.channelType, MsgItem.MSG_TYPE_DRIFTING_BOTTLE,
				// messageText, sendLocalTime);
				// newMsgItem.mailId = "fasf4234fsd2342342jkj23424";
				// newMsgItem.createTime = msgItem.createTime;
				// chatInfoArr[0] = newMsgItem;
				// ServiceInterface.handleMessage(chatInfoArr,
				// channel.channelID, channel.customName, false, true);
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (ChatServiceController.randomChatEnable)
					sendMsgToStandaloneServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
			}
			else
			{
				if (ChatServiceController.isBattleChatEnable && channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					if (isHornMsg)
						sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
					else
						sendMsgToStandaloneServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
				}
				else
				{
					if (!WebSocketManager.isSendFromWebSocket(channel.channelType) && !channel.isNearbyChannel())
					{
						sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
					}
					else
					{
						if (isHornMsg)
							sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
						else
							sendMsgToStandaloneServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
					}
				}
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				LogUtil.trackAction("send_user_mail_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				LogUtil.trackAction("send_chatroom_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				LogUtil.trackAction("send_random_text");
			oldSendTime = System.currentTimeMillis();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void sendMsgWithAt(String messageText, final boolean isHornMsg, boolean usePoint, String audioUrl,
			CopyOnWriteArrayList<InputAtContent> inputAtList)
	{
		if (currentChatType < 0 || !isSendIntervalValid() || getChatFragment() == null)
			return;

		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}

		// messageText = messageText.replaceAll("(\\n){2,}", "\\\n");
		Pattern pattern = Pattern.compile("(\\n){2,}");
		Matcher matcher = pattern.matcher(messageText);
		while (matcher.find())
		{
			String matchStr = matcher.group();
			int matchLen = matchStr.length();
			int start = messageText.indexOf(matchStr);

			if (inputAtList != null)
			{
				for (InputAtContent at : inputAtList)
				{
					if (at != null && at.getStartPos() > start)
					{
						at.setStartPos(at.getStartPos() - matchLen + 1);
					}
				}
			}

			matchStr.replaceAll("(\\n){2,}", "\\\n");
		}

		List<InputAtContent> atList = new ArrayList<InputAtContent>();
		if (inputAtList != null)
		{
			for (int i = 0; i < inputAtList.size(); i++)
			{
				InputAtContent at = inputAtList.get(i);
				if (at != null)
					atList.add(at);
			}
		}

		getChatFragment().clearInput();

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel == null)
		{
			LogUtil.trackMessage("sendMsg() channel is null: currentChatType=" + currentChatType + " fromUid="
					+ UserManager.getInstance().getCurrentMail().opponentUid);
			return;
		}

		int sendLocalTime = TimeManager.getInstance().getCurrentTime();
		int post = isHornMsg ? 6 : MsgItem.MSGITEM_TYPE_MESSAGE;

		if (StringUtils.isNotEmpty(audioUrl))
		{
			post = MsgItem.MSG_TYPE_AUDIO;
		}

		int channelType = channel.channelType;
		// 创建消息对象，加入正在发送列表
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUserId(), true, true, channelType, post, messageText,
				sendLocalTime);
		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;
		if (channel.isCountryOrAllianceChannel())
			msgItem.inputAtList = atList;
		if (StringUtils.isNotEmpty(audioUrl))
		{
			msgItem.media = audioUrl;
		}
		msgItem.initUserForSendedMsg();
		if (channel.msgList != null && channel.msgList.size() > 0 && currentChatType != DBDefinition.CHANNEL_TYPE_USER)
		{
			MsgItem lastItem = channel.msgList.get(channel.msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);

		// 加入model，更新视图
		try
		{
			if (channel.isNearbyChannel() || contactMode == 3)
			{
				LogUtil.trackNearby("nearby_send");
				if (channel.msgList == null || channel.msgList.size() == 0)
					LogUtil.trackNearby("nearby_group");
			}
			channel.addDummyMsg(msgItem);
			channel.getTimeNeedShowMsgIndex();
			// 发送后的行为（跳到最后一行）
			getChatFragment().afterSendMsgShowed();
			trackSendAction(channel.channelType, isHornMsg, false, msgItem);
			// 实际发给后台

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && channel.isDriftingBottleChannel())
			{
				try
				{
					DriftingBottleContent content = new DriftingBottleContent();
					content.setText(messageText);
					content.setType(0);
					messageText = JSON.toJSONString(content);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}

				// MsgItem[] chatInfoArr = new MsgItem[1];
				// MsgItem newMsgItem = new MsgItem(msgItem.uid, true, true,
				// channel.channelType, MsgItem.MSG_TYPE_DRIFTING_BOTTLE,
				// messageText, sendLocalTime);
				// newMsgItem.mailId = "fasf4234fsd2342342jkj23424";
				// newMsgItem.createTime = msgItem.createTime;
				// chatInfoArr[0] = newMsgItem;
				// ServiceInterface.handleMessage(chatInfoArr,
				// channel.channelID, channel.customName, false, true);
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (ChatServiceController.randomChatEnable)
					sendMsgToStandaloneServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
			}
			else
			{
				if (ChatServiceController.isBattleChatEnable && channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					if (isHornMsg)
						sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
					else
						sendMsgToStandaloneServerWithAt(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post,
								msgItem.media, msgItem.inputAtList);
					// sendMsg2WSServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
				}
				else
				{
					if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
					{
						sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
					}
					else
					{
						if (isHornMsg)
							sendMsg2Server(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
						else
						{
							if (channel.isCountryOrAllianceChannel())
								sendMsgToStandaloneServerWithAt(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post,
										msgItem.media, msgItem.inputAtList);
							else
								sendMsgToStandaloneServer(channel, messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
						}
					}
				}
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				LogUtil.trackAction("send_user_mail_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				LogUtil.trackAction("send_chatroom_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				LogUtil.trackAction("send_random_text");
			oldSendTime = System.currentTimeMillis();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private static void trackSendAction(int channelType, boolean isHornMsg, boolean resend, MsgItem msgItem)
	{
		if (msgItem.inputAtList != null && msgItem.inputAtList.size() > 0)
		{
			LogUtil.trackATplayers("at_send");
		}
		else
		{
			boolean isWriteByUser = msgItem.post == 0 || msgItem.post == MsgItem.MSG_TYPE_AUDIO || msgItem.post == MsgItem.MSG_TYPE_HORN;
			boolean isWS = WebSocketManager.isSendFromWebSocket(channelType);
			LogUtil.trackPageView("SendMessage-" + channelType + (isWriteByUser ? "-by_user" : "") + (isHornMsg ? "-horn" : "")
					+ (isWS ? (SwitchUtils.mqttEnable ? "-mqtt" : "-ws") : "") + (resend ? "-resend" : "") + (msgItem.isAudioMessage() ? "-audio" : ""));
		}
	}

	private static void sendMsgToStandaloneServerWithAt(ChatChannel channel, String messageText, boolean isHornMessage, boolean usePoint,
			int sendLocalTime, int post, String media, List<InputAtContent> inputAtList)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel);
			else
				MqttManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel);
		}
		else if (channel.isCountryOrAllianceChannel() || channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
		{
			if (channel.isCountryOrCountrySysChannel() && inputAtList != null && inputAtList.size() > 0)
			{
				int pos = -1;
				for (InputAtContent at : inputAtList)
				{
					if (at != null && at.isNpcAt())
					{
						pos = at.getStartPos();
						break;
					}
				}
				if (pos >= 0)
				{
					int npcNameEndIndex = messageText.indexOf(" ", pos);
					if (npcNameEndIndex > pos)
					{
						String fronPart = messageText.substring(0, npcNameEndIndex);
						String behindePart = messageText.substring(npcNameEndIndex);
						String repleacePart = messageText.substring(pos, npcNameEndIndex);
						messageText = fronPart.replace(repleacePart, "@Agatha") + behindePart;
					}
				}
			}

			String atUids = "";
			if (inputAtList != null && inputAtList.size() > 0)
			{
				atUids = JSON.toJSONString(inputAtList);
			}

			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendRoomMsgWithAt(messageText, sendLocalTime, channel, post, media, atUids);
			else
				MqttManager.getInstance().sendRoomMsgWithAt(messageText, sendLocalTime, channel, post, media, atUids);

		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
			String targetUid = fromUid;

			int contactMode = 0;
			if (UserManager.getInstance().getCurrentMail() != null
					&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				 Log.d("ChatServiceController", "onCreate: +sendMsgToStandaloneServerWithAt"+contactMode);
				if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				{
					contactMode = 1;
					 Log.d("ChatServiceController", "onCreate: +sendMsgToStandaloneServerWithAt+++CHANNEL_ID_POSTFIX_MOD"+contactMode);
				}
					
				else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
				{
					contactMode = 2;
				}
					
				else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
				{
					contactMode = 3;
				}
					
			}
			else
			{
				if (ChatServiceController.isModContactMode())
				{
					 Log.d("ChatServiceController", "onCreate: ++else+++++sendMsgToStandaloneServerWithAt+++CHANNEL_ID_POSTFIX_MOD"+contactMode);
					contactMode = 1;
				}
				else if (ChatServiceController.isDriftingBottleContactMode())
					contactMode = 2;
				else if (ChatServiceController.isNearbyContactMode())
					contactMode = 3;
			}

			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendUserMsg(targetUid, sendLocalTime, messageText, contactMode, post);
			else
				MqttManager.getInstance().sendUserMsg(targetUid, sendLocalTime, messageText, contactMode, post);
		}
	}

	private static void sendMsgToStandaloneServer(ChatChannel channel, String messageText, boolean isHornMessage, boolean usePoint,
			int sendLocalTime, int post, String media)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
			else
				MqttManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
			else
				MqttManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
		{
			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
			else
				MqttManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
			String targetUid = fromUid;

			LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "UserManager.getInstance().getCurrentMail().opponentUid",UserManager.getInstance().getCurrentMail().opponentUid,
"UserManager.getInstance().getCurrentMail() != null",UserManager.getInstance().getCurrentMail() != null);
			int contactMode = 0;
			if (UserManager.getInstance().getCurrentMail() != null
					&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				
				  Log.d("ChatServiceController", "onCreate: "+contactMode);
				  LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "opponentUid",UserManager.getInstance().getCurrentMail().opponentUid);
				if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				{
					contactMode = 1;
					 Log.d("ChatServiceController", "onCreate: +CHANNEL_ID_POSTFIX_MOD"+contactMode);
				}
			
				else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
				{
					contactMode = 2;
				}
					
				else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
				{
					contactMode = 3;
				}
					
			}
			else
			{
				 Log.d("ChatServiceController", "onCreate: ++else+++++CHANNEL_ID_POSTFIX_MOD"+contactMode);
				if (ChatServiceController.isModContactMode())
				{
					contactMode = 1;
					 Log.d("ChatServiceController", "onCreate: ++else+++++CHANNEL_ID_POSTFIX_MOD"+contactMode);
				}
					
					
				else if (ChatServiceController.isDriftingBottleContactMode())
				{
					contactMode = 2;
				}
					
				else if (ChatServiceController.isNearbyContactMode())
				{
					contactMode = 3;
				}
					
			}

			if (!SwitchUtils.mqttEnable)
			{
				 LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "sendmsg",contactMode);
				WebSocketManager.getInstance().sendUserMsg(targetUid, sendLocalTime, messageText, contactMode, post);
			}
				
			else
				MqttManager.getInstance().sendUserMsg(targetUid, sendLocalTime, messageText, contactMode, post);
		}
	}
	private static void sendMsg2Server(ChatChannel channel, String messageText, boolean isHornMessage, boolean usePoint, int sendLocalTime,

			int post, String media)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "channelType", currentChatType, "messageText", messageText,
				"isHornMessage", isHornMessage, "usePoint", usePoint, "sendLocalTime", sendLocalTime, "post", post, "media", media);

		if (currentChatType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
		{
			if (channel == null)
				return;
			if (StringUtils.isNotEmpty(channel.channelID))
			{
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				{
					String toName = "";
					String allianceUid = "";
					String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
					if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
					{
						toName = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
						allianceUid = UserManager.getInstance().getCurrentUser().allianceId;
					}
					else
					{
						UserInfo user = UserManager.getInstance().getUser(fromUid);
						if (user != null)
							toName = user.userName;
						else
							toName = "";
					}
					String targetUid = fromUid;

					boolean isModChannel = false;
					boolean isDriftingBottleChannel = false;
					if (UserManager.getInstance().getCurrentMail() != null
							&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid))
					{
						if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
							isModChannel = true;
						else if (UserManager.getInstance().getCurrentMail().opponentUid
								.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
							isDriftingBottleChannel = true;
					}

					int type = MailManager.MAIL_SELF_SEND;
					if (ChatServiceController.isModContactMode() || isModChannel)
					{
						if (post == MsgItem.MSG_TYPE_AUDIO)
							type = MailManager.MAIL_MOD_AUDIO_SELF_SEND;
						else
							type = MailManager.MAIL_MOD_PERSONAL;
					}
					else if (ChatServiceController.isDriftingBottleContactMode() || isDriftingBottleChannel)
					{
						type = MailManager.MAIL_DRIFTING_BOTTLE_SELF_SEND;
					}
					else
					{
						if (post == MsgItem.MSG_TYPE_AUDIO)
							type = MailManager.MAIL_AUDIO_SELF_SEND;
						else if (post == MsgItem.MSG_TYPE_EMOJ)
							type = MailManager.MAIL_EXPRESSION_USER_SELF_SEND;
					}

					JniController.getInstance().excuteJNIVoidMethod(
							"sendMailMsg",
							new Object[] {
									toName,
									"",
									messageText,
									allianceUid,
									channel.latestId,
									Boolean.valueOf(false),
									Integer.valueOf(type),
									Integer.toString(sendLocalTime),
									targetUid });
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				{
					boolean isMedia = (post == MsgItem.MSG_TYPE_AUDIO);
					boolean isEmoj = (post == MsgItem.MSG_TYPE_EMOJ);
					if(SwitchUtils.customWebsocketEnable){
						WebSocketManager.getInstance().roomGroupSendMsg(channel.channelID, messageText, post, media, sendLocalTime);
					}else {
						JniController.getInstance().excuteJNIVoidMethod("sendChatRoomMsg",
								new Object[] { channel.channelID, messageText, Integer.toString(sendLocalTime), Boolean.valueOf(isMedia), Boolean.valueOf(isEmoj) });
					}
				}
			}

		}
		else if (currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			boolean isMedia = (post == MsgItem.MSG_TYPE_AUDIO);
			boolean isEmoj = (post == MsgItem.MSG_TYPE_EMOJ);
			if(SwitchUtils.customWebsocketEnable){
				WebSocketManager.getInstance().roomGroupSendMsg(UserManager.getInstance().getCurrentMail().opponentUid, messageText, post,media, sendLocalTime);
			}else {
				JniController.getInstance().excuteJNIVoidMethod(
						"sendChatRoomMsg",
						new Object[] {
								UserManager.getInstance().getCurrentMail().opponentUid,
								messageText,
								Integer.toString(sendLocalTime),
								Boolean.valueOf(isMedia),
								Boolean.valueOf(isEmoj) });
			}
		}
		else if (currentChatType == DBDefinition.CHANNEL_TYPE_USER)
		{
			String toName = "";
			String allianceUid = "";
			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(UserManager.getInstance().getCurrentMail().opponentUid);
			if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
			{
				toName = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
				allianceUid = UserManager.getInstance().getCurrentUser().allianceId;
			}
			else
			{
				toName = UserManager.getInstance().getCurrentMail().opponentName;
			}
			String targetUid = fromUid;

			boolean isModChannel = false;
			boolean isDriftingBottleChannel = false;
			if (UserManager.getInstance().getCurrentMail() != null
					&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
					isModChannel = true;
				else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
					isDriftingBottleChannel = true;
			}
			int type = MailManager.MAIL_SELF_SEND;
			if (ChatServiceController.isModContactMode() || isModChannel)
			{
				if (post == MsgItem.MSG_TYPE_AUDIO)
					type = MailManager.MAIL_MOD_AUDIO_SELF_SEND;
				else
					type = MailManager.MAIL_MOD_PERSONAL;
			}
			else if (ChatServiceController.isDriftingBottleContactMode() || isDriftingBottleChannel)
			{
				type = MailManager.MAIL_DRIFTING_BOTTLE_SELF_SEND;
			}
			else
			{
				if (post == MsgItem.MSG_TYPE_AUDIO)
					type = MailManager.MAIL_AUDIO_SELF_SEND;
				else if (post == MsgItem.MSG_TYPE_EMOJ)
					type = MailManager.MAIL_EXPRESSION_USER_SELF_SEND;
			}

			JniController.getInstance().excuteJNIVoidMethod(
					"sendMailMsg",
					new Object[] {
							toName,
							"",
							messageText,
							allianceUid,
							UserManager.getInstance().getCurrentMail().mailUid,
							Boolean.valueOf(UserManager.getInstance().getCurrentMail().isCurChannelFirstVisit),
							Integer.valueOf(type),
							Integer.toString(sendLocalTime),
							targetUid });
		}
		else if (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY || currentChatType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			if (!isHornMessage)
			{
				JniController.getInstance().excuteJNIVoidMethod(
						"sendChatMessage",
						new Object[] {
								messageText,
								Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY),
								Integer.toString(sendLocalTime),
								Integer.valueOf(post),
								media,
								getRoomId(),
								getExtraRoomId() });
			}
			else
			{
				if (isBattleChatEnable)
				{
					JniController.getInstance().excuteJNIVoidMethod(
							"sendOriginalServerHornMessage",
							new Object[] {
									messageText,
									Boolean.valueOf(usePoint),
									getRoomId(),
									getExtraRoomId(),
									Integer.toString(sendLocalTime) });
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("sendHornMessage",
							new Object[] { messageText, Boolean.valueOf(usePoint), Integer.toString(sendLocalTime) });
				}

				if (!usePoint)
				{
					ConfigManager.isFirstUserHorn = false;
				}
				else
				{
					ConfigManager.isFirstUserCornForHorn = false;
				}
			}
		}
		else if (currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			JniController.getInstance().excuteJNIVoidMethod(
					"sendChatMessage",
					new Object[] {
							messageText,
							Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE),
							Integer.toString(sendLocalTime),
							Integer.valueOf(post),
							media,
							getRoomId(),
							getExtraRoomId() });
		}
	}

	private static String getRoomId()
	{
		return SwitchUtils.mqttEnable ? MqttManager.getInstance().getRoomId(currentChatType) : WebSocketManager.getInstance().getRoomId(currentChatType);
	}

	private static String getExtraRoomId()
	{
		return SwitchUtils.mqttEnable ? MqttManager.getInstance().getExtraRoomId(currentChatType) : WebSocketManager.getInstance().getExtraRoomId(currentChatType);
	}

	// 重发消息
	public static void resendAudioMsg(MsgItem msgItem)
	{
		if (!isSendIntervalValid())
			return;
		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}
		// 显示转圈
		msgItem.sendState = MsgItem.SENDING;

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel != null)
		{
			if (getChatFragment() != null)
				getChatFragment().notifyDataSetChanged(currentChatType, channel.channelID, false);
			if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
				sendMsg2Server(channel, msgItem.msg, false, false, msgItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, msgItem.media);
			else
				sendMsgToStandaloneServer(channel, msgItem.msg, false, false, msgItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, msgItem.media);
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				LogUtil.trackAction("resend_user_mail_audio");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				LogUtil.trackAction("resend_chatroom_audio");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				LogUtil.trackAction("resend_random_audio");
		}
	}

	// 重发消息
	public static void resendMsg(MsgItem msgItem, boolean isHornMsg, boolean usePoint)
	{
		if (!isSendIntervalValid())
			return;
		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}
		// 显示转圈
		msgItem.sendState = MsgItem.SENDING;
		final MsgItem item = msgItem;

		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel != null)
		{
			if (getChatFragment() != null)
				getChatFragment().notifyDataSetChanged(item.channelType, channel.channelID, false);

			trackSendAction(channel.channelType, isHornMsg, true, msgItem);

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				if (ChatServiceController.randomChatEnable)
					sendMsgToStandaloneServer(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
			}
			else
			{
				if (ChatServiceController.isBattleChatEnable && channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					if (isHornMsg)
						sendMsg2Server(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
					else
						sendMsgToStandaloneServerWithAt(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post,
								msgItem.media, msgItem.inputAtList);
					// sendMsg2WSServer(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
				}
				else
				{
					if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
					{
						sendMsg2Server(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
					}
					else
					{
						if (isHornMsg)
							sendMsg2Server(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
						else
						{
							if (channel.isCountryOrAllianceChannel())
								sendMsgToStandaloneServerWithAt(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post,
										msgItem.media, msgItem.inputAtList);
							else
								sendMsgToStandaloneServer(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post,
										msgItem.media);
						}
					}
				}
			}

			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				LogUtil.trackAction("resend_user_mail_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				LogUtil.trackAction("resend_chatroom_text");
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
				LogUtil.trackAction("resend_random_text");
		}
	}

	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup)
	{
		doHostAction(action, uid, name, attachmentId, returnToChatAfterPopup, false);
	}

	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup,
			boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "action", action, "attachmentId", attachmentId, "returnToChat", returnToChatAfterPopup);

		JniController.getInstance().excuteJNIVoidMethod("setActionAfterResume",
				new Object[] { action, uid, name, attachmentId, Boolean.valueOf(returnToChatAfterPopup) });

		try
		{
			ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity(), reverseAnimation);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void toggleFullScreen(final boolean fullscreen, final boolean noTitle, final Activity activity)
	{
		activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{
					// TODO 删除noTitle参数
					if (noTitle)
					{
						activity.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
					}
					else
					{
						// activity.requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
						// activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);//
						// 去掉标题栏
					}
					if (fullscreen)
					{
						activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
					else
					{
						activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				}
				catch (Exception e)
				{
				}
			}
		});
	}

	// 重发消息
	public void notifyUserDataChanged()
	{
		if (getChatFragment() != null && getChatFragment().getCurrentChannelView() != null
				&& getChatFragment().getCurrentChannelView().chatChannel != null)
		{
			getChatFragment().notifyDataSetChanged(getChatFragment().getCurrentChannelView().channelType,
					getChatFragment().getCurrentChannelView().chatChannel.channelID, false);
			getChatFragment().notifyAllianceMemberChanged();
		}
		else if (getMemberSelectorFragment() != null)
		{
			if (ChatServiceController.getCurrentActivity() != null)
			{
				ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (getMemberSelectorFragment() != null)
							{
								getMemberSelectorFragment().notifyDataSetChanged();
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}

		}
		else if (getChannelListFragment() != null)
		{
			getChannelListFragment().notifyDataSetChanged();
		}
		else if (getBaseListActivity() != null)
		{
			getBaseListActivity().notifyDataSetChanged();
		}
		else if (getNearByListActivity() != null)
		{
			getNearByListActivity().notifyDataSetChanged();
		}
	}

	public static void showGameActivity(Activity a)
	{
		showGameActivity(a, false);
	}

	public static void showGameActivity(Activity a, boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW);
		isReturningToGame = true;
		ServiceInterface.clearActivityStack();

		Intent intent = new Intent(a, hostClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		a.startActivity(intent);
		if (!reverseAnimation)
		{
			a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
		else
		{
			a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left_fast);
		}
	}

	public static boolean isInChatRoom()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	public static boolean isInUserMail()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_USER);
	}

	public static boolean isInChat()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY || currentChatType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE || currentChatType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS || currentChatType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT);
	}

	public static boolean isInMailDialog()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_USER || currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM);
	}

	public static MemberSelectorFragment getMemberSelectorFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof MemberSelectorFragment)
		{
			return (MemberSelectorFragment) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static ChatActivity getChatActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ChatActivity)
		{
			return (ChatActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static RealtimeVoiceRoomSettingActivity getRealtimeVoiceRoomSettingActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof RealtimeVoiceRoomSettingActivity)
		{
			return (RealtimeVoiceRoomSettingActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChatFragmentNew getChatFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof ChatFragmentNew)
		{
			return (ChatFragmentNew) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static ImageDetailFragment getImageDetailFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ImagePagerActivity
				&& ((ImagePagerActivity) ChatServiceController.getCurrentActivity()) != null)
		{
			return ((ImagePagerActivity) ChatServiceController.getCurrentActivity()).detailFragment;
		}
		return null;
	}

	public static AllianceShareDetailActivity getAllianceShareDetailActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareDetailActivity)
		{
			return (AllianceShareDetailActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChatRoomSettingActivity getChatRoomSettingActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof ChatRoomSettingActivity)
		{
			return (ChatRoomSettingActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static AllianceShareListActivity getAllianceShareListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareListActivity)
		{
			return (AllianceShareListActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static NearByActivity getNearByListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof NearByActivity)
		{
			return (NearByActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static EmojSubscribActivity getEmojSubscribActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof EmojSubscribActivity)
		{
			return (EmojSubscribActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static AllianceShareCommentListActivity getAllianceShareCommentListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareCommentListActivity)
		{
			return (AllianceShareCommentListActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}
	
	public static AbstractRecyclerActivity getRecyclerListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof AbstractRecyclerActivity)
		{
			return (AbstractRecyclerActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}
	
	public static AbstractBaseActivity getBaseListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof AbstractBaseActivity)
		{
			return (AbstractBaseActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}
	
	public static RecyclerMainListActivity getRecyclerMainListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof RecyclerMainListActivity)
		{
			return (RecyclerMainListActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}
	
	public static RecyclerSysMailActivity getRecyclerSysMailActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof RecyclerSysMailActivity)
		{
			return (RecyclerSysMailActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChannelListActivity getChannelListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ChannelListActivity)
		{
			return (ChannelListActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChannelListFragment getChannelListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof ChannelListFragment)
		{
			return (ChannelListFragment) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static SysMailListFragment getSysMailListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof SysMailListFragment)
		{
			return (SysMailListFragment) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static MainListFragment getMainListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof MainListFragment)
		{
			return (MainListFragment) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static MsgMailListFragment getMsgListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentFragment() != null
				&& ChatServiceController.getCurrentFragment() instanceof MsgMailListFragment)
		{
			return (MsgMailListFragment) ChatServiceController.getCurrentFragment();
		}
		return null;
	}

	public static boolean isInTheSameChannel(String channelId)
	{
		if (getChatFragment() != null && getChatFragment().getCurrentChannel() != null
				&& getChatFragment().getCurrentChannel().chatChannel != null
				&& StringUtils.isNotEmpty(getChatFragment().getCurrentChannel().chatChannel.channelID))
		{
			return getChatFragment().getCurrentChannel().chatChannel.channelID.equals(channelId);
		}
		else if (getChatRoomSettingActivity() != null && UserManager.getInstance().getCurrentMail() != null
				&& UserManager.getInstance().getCurrentMail().opponentUid.equals(channelId)
				&& currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			return true;
		return false;
	}

	public static boolean isInCrossFightServer()
	{
		return crossFightSrcServerId > 0;
	}

	public static boolean isParseEnable()
	{
		if (getChannelListFragment() != null || getChatFragment() != null || getMemberSelectorFragment() != null)
			return true;
		return false;
	}

	public static boolean isInnerVersion()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.clash.of.kings.inner");
	}

	public static boolean isBetaVersion()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.hcg.cok.beta");
	}

	public static int getChatRestrictLevel()
	{
		int level = 1;
		String channelName = MailManager.getInstance().getPublishChannelName();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelName", channelName);
		if (StringUtils.isEmpty(channelName) || StringUtils.isEmpty(ChatServiceController.switch_chat_k11))
			return level;
		String[] switchArr = ChatServiceController.switch_chat_k11.split("\\|");
		if (switchArr.length != 2)
			return level;
		if (ChatServiceController.switch_chat_k10.contains(channelName))
		{
			if (StringUtils.isNumeric(switchArr[1]))
				level = Integer.parseInt(switchArr[1]);
		}
		else
		{
			if (StringUtils.isNumeric(switchArr[0]))
				level = Integer.parseInt(switchArr[0]);
		}
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "level", level);
		return level;
	}

	public static boolean isChatRestrictForLevel()
	{
		if (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			return currentLevel < getChatRestrictLevel();
		return false;
	}

	public void postLatestChatMessage(MsgItem msgItem)
	{
		if (msgItem == null || ((SwitchUtils.mqttEnable ? MqttManager.isInDragonObserverRoom() : WebSocketManager.isInDragonObserverRoom())
				&& msgItem.channelType != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
			return;
		LatestChannelChatInfo chatInfo = new LatestChannelChatInfo();
		LatestChatInfo latestChatInfo = new LatestChatInfo();
		latestChatInfo.setMsgInfo(msgItem);
		if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			chatInfo.setLatestCountryChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			chatInfo.setLatestAllianceChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			chatInfo.setLatestBattleChatInfo(latestChatInfo);
		else if (msgItem.isUserChatChannelMsg())
			chatInfo.setLatestCustomChatInfo(latestChatInfo);

		try
		{
			String lateChatMessage = JSON.toJSONString(chatInfo);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "lateChatMessage", lateChatMessage);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] { lateChatMessage });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void postLatestCustomChatMessage(MsgItem msgItem)
	{
		LatestChannelChatInfo chatInfo = new LatestChannelChatInfo();
		LatestChatInfo latestChatInfo = new LatestChatInfo();
		if (msgItem != null)
			latestChatInfo.setMsgInfo(msgItem);
		chatInfo.setLatestCustomChatInfo(latestChatInfo);

		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "chatInfo", chatInfo);
			String lateChatMessage = JSON.toJSONString(chatInfo);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "lateChatMessage", lateChatMessage);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] { lateChatMessage });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isDifferentDate(MsgItem item, List<MsgItem> items)
	{
		if (item == null || items == null)
			return true;
		int index = items.indexOf(item);
		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChatType);
		if (channel != null && channel.getMsgIndexArrayForTimeShow() != null && channel.getMsgIndexArrayForTimeShow().size() > 0)
		{
			if (channel.getMsgIndexArrayForTimeShow().contains(Integer.valueOf(index)))
				return true;
		}
		else
		{
			if (index == 0)
			{
				return true;
			}
			else if (index > 0 && items.get(index - 1) != null)
			{
				return !item.getSendTimeYMD().equals(items.get(index - 1).getSendTimeYMD());
			}
		}

		return false;
	}

	public synchronized void setGameMusiceEnable(boolean enable)
	{
		if (!enable)
		{
			gameMusicEnable = false;
			JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable", new Object[] { Boolean.valueOf(false) });
			System.out.println("setGameMusiceEnable false");
		}
		else
		{
			gameMusicEnable = true;
			if (audioTimer != null)
				return;

			audioTimer = new Timer();
			audioTimerTask = new TimerTask()
			{

				@Override
				public void run()
				{
					System.out.println("setGameMusiceEnable gameMusicEnable:" + gameMusicEnable);
					if (gameMusicEnable)
					{
						JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable", new Object[] { Boolean.valueOf(true) });
						stopAudioTimer();
					}
				}
			};
			audioTimer.schedule(audioTimerTask, 1000);
		}
	}

	private synchronized void stopAudioTimer()
	{
		try
		{
			if (audioTimer != null)
			{
				// 可能会有NullPointerException，应该是重入问题
				audioTimer.cancel(); // NullPointerException发生数量少
				audioTimer.purge(); // NullPointerException发生数量多
				audioTimer = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getPackageName()
	{
		return hostActivity.getPackageName();
	}

	public static String getApplicationName()
	{
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try
		{
			packageManager = hostActivity.getApplicationContext().getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			applicationInfo = null;
		}

		return (String) packageManager.getApplicationLabel(applicationInfo);
	}

	public static String getApplicationVersionName()
	{
		try
		{
			return hostActivity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
		}
		return null;
	}

	public static int getApplicationVersionCode()
	{
		try
		{
			return hostActivity.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{
		}
		return 0;
	}

	public static String getApplicationVersionInfo()
	{
		return getApplicationVersionName() + " (" + getApplicationVersionCode() + ")";
	}

	public boolean isCoordinateValid(String coord)
	{
		return Integer.parseInt(coord) >= 0 && Integer.parseInt(coord) <= 1200;
	}

	private void onURLClick(View widget, String url)
	{
		final String[] coords = url.split(",");

		if (coords[0].equals("ShowEquip"))
		{
			try
			{
				if (coords.length == 2 && StringUtils.isNotEmpty(coords[1]))
				{
					ChatServiceController.doHostAction("showEquipment", "", "", coords[1], true);
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			return;
		}
		else
		{
			if (!isCoordinateValid(coords[0]) || !isCoordinateValid(coords[1]))
			{
				Toast.makeText(getCurrentActivity(), "coordinate (" + coords[0] + "," + coords[1] + ") is invalid!", Toast.LENGTH_LONG)
						.show();
				return;
			}
			ChatServiceController.doHostAction("gotoCoordinate", coords[0], coords[1], "", false);
		}
	}

	private class MyURLSpan extends ClickableSpan
	{
		private String mUrl;

		MyURLSpan(String url)
		{
			mUrl = url;
		}

		@Override
		public void onClick(View widget)
		{
			onURLClick(widget, mUrl);
		}
	}

	public void setText(MyActionBarActivity c, TextView textView, String str, MsgItem item)
	{
		String equipName = "";
		String taskName = "";
		String allianceTreasureName = "";
		int colorIndex = -1;
		if (item.isEquipMessage())
		{
			String msgStr = item.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] equipInfo = msgStr.split("\\|");
				if (equipInfo.length == 2)
				{
					equipName = LanguageManager.getLangByKey(equipInfo[1]);
					if (StringUtils.isNumeric(equipInfo[0]))
						colorIndex = Integer.parseInt(equipInfo[0]);
				}
			}
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_EQUIP_SHARE, equipName);
		}
		else if (item.isAllianceTaskMessage())
		{
			String msgStr = item.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] taskInfo = msgStr.split("\\|");
				if (taskInfo.length >= 4)
				{
					taskName = LanguageManager.getLangByKey(taskInfo[2]);
					if (StringUtils.isNumeric(taskInfo[0]))
						colorIndex = Integer.parseInt(taskInfo[0]);
					String taskPlayerName = taskInfo[3];
					if (taskInfo.length > 4)
					{
						for (int i = 4; i < taskInfo.length; i++)
						{
							taskPlayerName += "|" + taskInfo[i];
						}
					}
					if (StringUtils.isNotEmpty(taskPlayerName))
					{
						try
						{
							List<AllianceTaskInfo> taskInfoArr = JSON.parseArray(taskPlayerName, AllianceTaskInfo.class);
							if (taskInfoArr != null && taskInfoArr.size() >= 1 && taskInfoArr.get(0) != null)
							{
								String publisher = taskInfoArr.get(0).getName();
								if (taskInfoArr.size() == 1 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1))
								{
									str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1, publisher, taskName);
								}
								else if (taskInfoArr.size() == 2 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2))
								{
									AllianceTaskInfo taskInfo2 = taskInfoArr.get(1);
									if (taskInfo2 != null)
									{
										str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2, publisher, taskName,
												taskInfo2.getName());
									}
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}

		}
		else if (item.isAllianceTreasureMessage())
		{
			String name = item.getAllianceTreasureInfo(1);
			if (StringUtils.isNotEmpty(name) && StringUtils.isNumeric(name))
				allianceTreasureName = LanguageManager.getLangByKey(name);
			if (StringUtils.isNotEmpty(allianceTreasureName))
				str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_SHARE, allianceTreasureName);
			String colorStr = item.getAllianceTreasureInfo(0);
			if (StringUtils.isNotEmpty(colorStr) && StringUtils.isNumeric(colorStr))
				colorIndex = Integer.parseInt(colorStr);
		}
		else if (item.isAllianceHelpMessage())
		{
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_HELP);
		}
		else if (item.isAllianceJoinMessage())
		{
			str = LanguageManager.getLangByKey(LanguageKeys.MSG_ALLIANCE_JOIN) + "("
					+ LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM) + ")";
		}
		else if (item.isLotteryMessage())
		{
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_LUCK_WHEEL);
		}
		else if (item.isNeedParseDialog())
		{
			String msgStr = item.parseExtraDialogMsg();
			if (StringUtils.isNotEmpty(msgStr))
				str = msgStr;
		}
		else if (item.isShotMessage())
		{
			String msgStr = item.parseShotMsg();
			if (StringUtils.isNotEmpty(msgStr))
				str = msgStr;
		}
		else if (item.isCordinateShareMessage())
		{
			if (StringUtils.isNotEmpty(item.attachmentId))
				str = LanguageManager.getLangByKey(LanguageKeys.TIP_SHARE_CORDINATE, item.attachmentId);
		}
		else if (item.isAllianceSkillMessage())
		{
			if (StringUtils.isNotEmpty(item.attachmentId) && (item.attachmentId.contains("{") || item.attachmentId.contains("[")))
			{
				try
				{
					AllianceSkillInfo skillInfo = JSON.parseObject(item.attachmentId, AllianceSkillInfo.class);
					if (skillInfo != null)
					{
						String skillId = skillInfo.getSkillId();
						if (StringUtils.isNotEmpty(skillId))
						{
							String skillName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { skillId });
							String skillDes = JniController.getInstance().excuteJNIMethod("getPropById",
									new Object[] { skillId, "description" });
							String skillBase = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { skillId, "base" });
							String des = "";
							if (StringUtils.isEmpty(skillBase))
							{
								des = skillDes + skillBase;
							}
							else
							{
								String[] baseArr = skillBase.split("\\|");
								if (baseArr.length == 1)
								{
									des = LanguageManager.getLangByKey(skillDes, skillBase);
								}
								else if (baseArr.length == 2)
								{
									des = LanguageManager.getLangByKey(skillDes, baseArr[0], baseArr[1]);
								}
							}
							if (StringUtils.isEmpty(skillInfo.getPointId()))
								str = LanguageManager.getLangByKey(skillInfo.getDialog(), skillName, des);
							else
								str = LanguageManager.getLangByKey(skillInfo.getDialog(), skillInfo.getPointId(), skillName, des);
						}

					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (item.isAllianceOfficerMessage())
		{
			if (StringUtils.isNotEmpty(item.attachmentId))
			{
				try
				{
					AllianceOfficerAttachment officer = JSON.parseObject(item.attachmentId, AllianceOfficerAttachment.class);
					if (officer != null)
					{
						str = LanguageManager.getLangByKey(officer.getDialog(), officer.getName(),
								LanguageManager.getLangByKey(officer.getOfficer()));
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (item.isVersionInvalid())
			str = LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);

		if (StringUtils.isNotEmpty(str))
			str = item.getName() + ":" + str;
		textView.setText(str);
	}

	private int getColorByIndex(int index)
	{
		int color = 0;
		switch (index)
		{
			case 0:
				color = 0xffc7beb3;
				break;
			case 1:
				color = 0xff56e578;
				break;
			case 2:
				color = 0xff4599f8;
				break;
			case 3:
				color = 0xffaf49ea;
				break;
			case 4:
				color = 0xffe8771f;
				break;
			case 5:
				color = 0xffedd538;
				break;
			case 6:
				color = 0xffff0000;
				break;
			default:
				color = 0xffc7beb3;
				break;
		}
		return color;
	}

	public String getUserMailTitle()
	{
		String title = UserManager.getInstance().getCurrentMail().opponentName;
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER)
		{
			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(UserManager.getInstance().getCurrentMail().opponentUid);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "fromUid",fromUid);
			if (StringUtils.isNotEmpty(fromUid))
			{
				if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
				{
					title = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
				}
				else
				{
					UserInfo fromUser = UserManager.getInstance().getUser(fromUid);
					if (fromUser != null && StringUtils.isNotEmpty(fromUser.userName))
						title = fromUser.userName;
					else
						title = fromUid;
				}
			}

		}

		if (StringUtils.isNotEmpty(title) && title.length() > 30)
		{
			title = title.substring(0, 30);
			title += "...";
		}

		if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
			title += "(MOD)";
		else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
			title += "(" + LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_DRIFTING_BOTTLE) + ")";
		else if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
			title += "(" + LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY) + ")";
		return title;
	}

	public static int parseColor(String colorText)
	{
		String[] rgb = colorText.split(":");
		if (rgb.length != 3)
			return 0;
		if (StringUtils.isNumeric(rgb[0]) && StringUtils.isNumeric(rgb[1]) && StringUtils.isNumeric(rgb[2]))
		{
			int r = Integer.parseInt(rgb[0]);
			int g = Integer.parseInt(rgb[1]);
			int b = Integer.parseInt(rgb[2]);
			if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255)
				return Color.rgb(r, g, b);
			else
				return 0;
		}
		else
			return 0;
	}
}
