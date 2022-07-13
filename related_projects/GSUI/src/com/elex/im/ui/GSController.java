package com.elex.im.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.elex.im.CokChannelDef;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.DBStatusEvent;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.controller.MenuController;
import com.elex.im.ui.host.DummyHost;
import com.elex.im.ui.host.GameHost;

public class GSController
{
	private static GSController			instance;

	private ScheduledExecutorService	service							= null;
	private Timer						audioTimer						= null;
	private TimerTask					audioTimerTask					= null;
	private int							currentAoduiSendLocalTime		= 0;
	private static boolean				gameMusicEnable					= true;
	private static int					currentChatType					= -1;											// 刚进入时由C++设置，在java中可修改，退出后会再给C++
	public static int					currentLevel					= 1;

	public static long					oldSendTime						= 0;											// 上一次发送时间

	// C++传入的参数
	public static boolean				isContactMod					= false;
	public static boolean				isHornItemUsed					= false;										// 是否使用喇叭道具
	public static boolean				isCreateChatRoom				= false;

	public static boolean				isNewMailListEnable				= false;										// 是否使用新的邮件列表
	public static int					serverId;
	public static boolean				isReturningToGame				= false;										// 仅在打开原生activity时重置为false，在IF.onResume中重置false的话，会导致无法记忆二级邮件列表
	public static boolean				isFriendEnable					= true;										// 好友功能开关
	public static boolean				isDetectInfoEnable				= false;										// 侦察战报更新开关
	public static long					oldReportContentTime			= 0;
	public static long					REPORT_CONTENT_TIME_INTERVAL	= 30000;
	public static String				banTime							= "1|2|3|4";									// 国王的UID
	public static boolean				isListViewFling					= false;
	public static boolean				needShowAllianceDialog			= false;										// 需要在联盟聊天输入框显示特定的dialog，仅在发现联盟宝藏后首次进入聊天时显示
	public static int					sendTimeTextHeight				= 0;
	public static boolean				isNewYearStyleMsg				= false;
	public static String				originalServerName				= "";

	public static int					sortType						= -1;

	public static boolean				isDefaultTranslateEnable		= true;										// 默认翻译开关

	public static String				kingUid							= "";											// 国王的UID
	public static int					red_package_during_time			= 24;											// 红包到期时间
	
	public static GSController getInstance()
	{
		if (instance == null)
		{
			instance = new GSController();
		}
		return instance;
	}

	private GSController()
	{
		service = Executors.newSingleThreadScheduledExecutor();
		
		IMCore.getInstance().addEventListener(DBStatusEvent.INIT_COMPLETE, this, new EventCallBack(){
			public void onCallback(Event event){
				JniController.getInstance().excuteJNIVoidMethod("completeInitDatabase", null);
//				ChatServiceController.getInstance().host.completeInitDatabase();
			};
		});
	}

	public static void setCurrentChannelType(int type)
	{
		currentChatType = type;
	}

	public static int getCurrentChannelType()
	{
		return currentChatType;
	}
	
	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup)
	{
		doHostAction(action, uid, name, attachmentId, returnToChatAfterPopup, false);
	}

	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup,
			boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "action", action, "returnToChat", returnToChatAfterPopup);

		JniController.getInstance().excuteJNIVoidMethod("setActionAfterResume",
				new Object[] { action, uid, name, attachmentId, Boolean.valueOf(returnToChatAfterPopup) });

		try
		{
			UIManager.showGameActivity(UIManager.getCurrentActivity(), reverseAnimation);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void setMailInfo(String mailFromUid, String mailUid, String mailName, int mailType)
	{
		UserManager.getInstance().getCurrentMail().opponentUid = mailFromUid;
		UserManager.getInstance().getCurrentMail().mailUid = mailUid;
		UserManager.getInstance().getCurrentMail().opponentName = mailName;
		UserManager.getInstance().getCurrentMail().type = mailType;
		if (mailType == MailManager.MAIL_MOD_PERSONAL || mailType == MailManager.MAIL_MOD_SEND)
			isContactMod = true;
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

	public static void sendDummyAudioMsg(long length, int sendLocalTime)
	{
		if (GSController.getCurrentChannelType() < 0 || !GSController.isSendIntervalValid() || UIManager.getChatFragment() == null)
			return;
		if (CokChannelDef.isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}

		UIManager.getChatFragment().clearInput();

		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel == null)
			return;

		int post = Msg.MSG_TYPE_AUDIO;
		Msg msgItem = new Msg(UserManager.getInstance().getCurrentUser().uid, true, true, channel.getChannelType(), post, "" + length,
				sendLocalTime);
		msgItem.sendState = Msg.SENDING;
		msgItem.createTime = sendLocalTime;

		msgItem.initUserForSendedMsg();
		
		channel.addDummySequenceId(msgItem);

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);

		channel.addDummyMsg(msgItem);
		channel.getTimeNeedShowMsgIndex();
		// getChatFragment().notifyDataSetChanged(GSController.getCurrentChannelType());
		// 发送后的行为（跳到最后一行）
		UIManager.getChatFragment().afterSendMsgShowed();
		GSController.oldSendTime = System.currentTimeMillis();
	}

	// 仅一处引用
//	public boolean getNoMoreDataFlag(int index)
//	{
//		if (getChannelByViewIndex(index) == null)
//			return false;
//
//		return getChannelByViewIndex(index).noMoreDataFlag;
//	}
	/**
	 * 仅当视图存在时才返回，找不到不会创建
	 */
//	private Channel getChannelByViewIndex(int index)
//	{
//		Channel result = null;
//		if (UIManager.getChatFragment() != null)
//		{
//			result = UIManager.getChatFragment().getChannelView(index).chatChannel;
//		}
//		return result;
//	}
	
	public static ArrayList<String> getSelectMemberUidArr()
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();
		
		if (UserManager.getInstance().getCurrentUser()==null )
			return memberUidArray;
		
		if (isCreateChatRoom)
		{
			if (!UserManager.getInstance().getCurrentUser().uid.equals(""))
				memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
		}
		else
		{
			if (!(CokChannelDef.isInMailDialog() || CokChannelDef.isInChat()))
				return memberUidArray;
			if (!CokChannelDef.isInChatRoom())
			{
				if (!UserManager.getInstance().getCurrentUser().uid.equals(""))
					memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
			}
			else
			{
//				memberUidArray = ChannelManager.getInstance().getChatRoomMemberArrayByKey(getCurrentMail().opponentUid);
			}
		}
		return memberUidArray;
	}

	public static ArrayList<String> getSelctedMemberArr(boolean isFromAlliance)
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();
		if (UserManager.getInstance().getCurrentUser() == null)
			return memberUidArray;
		
		boolean isInAlliance = !UserManager.getInstance().getCurrentUser().allianceId.equals("");

		if (isCreateChatRoom || (CokChannelDef.isInMailDialog() && !CokChannelDef.isInChatRoom()))
		{
			if (((isInAlliance && isFromAlliance) || (!isInAlliance && !isFromAlliance))
					&& !UserManager.getInstance().getCurrentUser().uid.equals(""))
				memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
		}
		else if (CokChannelDef.isInChatRoom())
		{
			memberUidArray = new ArrayList<String>();
			HashMap<String, User> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			Set<String> uidKeySet = memberInfoMap.keySet();
//			List<String> userArray = ChannelManager.getInstance().getChatRoomMemberArrayByKey(getCurrentMail().opponentUid);
//			for (int i = 0; i < userArray.size(); i++)
//			{
//				String uid = userArray.get(i);
//				if (!uid.equals("") && (isFromAlliance && uidKeySet.contains(uid)) || (!isFromAlliance && !uidKeySet.contains(uid)))
//					memberUidArray.add(uid);
//			}
		}
		return memberUidArray;
	}

	public static boolean isSendIntervalValid()
	{
		boolean isValid = true;
		long sendTime = System.currentTimeMillis();
		if ((sendTime - oldSendTime) < ConfigManager.sendInterval)
		{
			Toast.makeText(UIManager.getCurrentActivity(), LanguageManager.getLangByKey(LanguageKeys.TIP_SENDMSG_WARN), Toast.LENGTH_SHORT).show();
			isValid = false;
		}
		return isValid;
	}
	
	public void notifyUserDataChanged()
	{
//		if (getChatFragment() != null && getChatFragment().getCurrentChannelView()!=null && getChatFragment().getCurrentChannelView().chatChannel!=null)
//		{
//			getChatFragment().notifyDataSetChanged(getChatFragment().getCurrentChannelView().channelType, getChatFragment().getCurrentChannelView().chatChannel.channelID,false);
//		}
//		else if (getMemberSelectorFragment() != null)
//		{
//			getMemberSelectorFragment().notifyDataSetChanged();
//		}
//		else if (getChannelListFragment() != null)
//		{
//			getChannelListFragment().notifyDataSetChanged();
//		}
	}

	public static void init(Activity a, boolean isDummyHost)
	{
		IMCore.hostActivity = a;
		IMCore.hostClass = a.getClass();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "hostClass", IMCore.hostClass.getName());

		if (!isDummyHost)
		{
			IMCore.getInstance().host = new GameHost();
		}
		else
		{
			IMCore.getInstance().host = new DummyHost();
		}
	}

	// TODO 删除此函数
	public boolean isInDummyHost()
	{
		return IMCore.getInstance().host instanceof DummyHost;
	}

	public boolean isUsingDummyHost()
	{
		return IMCore.getInstance().host instanceof DummyHost && ((DummyHost) IMCore.getInstance().host).treatAsDummyHost;
	}

	public boolean isActuallyUsingDummyHost()
	{
		return IMCore.getInstance().host instanceof DummyHost;
	}
}