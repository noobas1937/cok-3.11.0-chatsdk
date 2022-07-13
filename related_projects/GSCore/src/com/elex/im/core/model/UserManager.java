package com.elex.im.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;

public class UserManager
{
	private static UserManager					instance;
	private User								currentUser;
	private String								currentUserId;
	private ArrayList<User>						userList;
	private MailInfo							currentMail;

	private ArrayList<String>					banUidList;
	private ArrayList<String>					banNoticeUidList;
	private ArrayList<String>					blockUidList;
	private ArrayList<String>					reportUidList;
	private ArrayList<Msg>						reportContentList;
	private ArrayList<Msg>						reportContentTranslationList;

	private HashMap<String, ArrayList<User>>	allianceMemberMap;
	/** 好友列表 */
	private HashMap<String, User>				friendMemberMap;
	/** 用于联盟成员等级排序 */
	private HashMap<String, Integer>			rankMap;
	public HashMap<String, User>				allianceMemberInfoMap;
	/** 非联盟成员信息map */
	public HashMap<String, User>				nonAllianceMemberInfoMap;
	private ScheduledExecutorService			service;
	private TimerTask							timerTask;
	private long								lastAddUidTime					= 0;
	private long								lastCallSuccessTime				= 0;
	private long								lastCallTime					= 0;
	private long								CALL_TIME_OUT					= 8000;
	private static final int					GET_USER_INFO_UID_COUNT			= 20;
	/** 实际向后台发送了请求的uid列表 */
	private ArrayList<String>					fechingUids						= new ArrayList<String>();
	/** 请求的uid队列 */
	private ArrayList<String>					queueUids						= new ArrayList<String>();
	/** 获取不到信息的uid列表 */
	private ArrayList<String>					unknownUids						= new ArrayList<String>();

	public static final int						BLOCK_LIST						= 1;
	public static final int						BAN_LIST						= 2;
	public static final int						REPORT_LIST						= 3;
	public static final int						REPORT_CONTETN_LIST				= 4;
	public static final int						REPORT_TRANSLATION_LIST			= 5;
	public static final int						BAN_NOTICE_LIST					= 6;

	public static final int						NOTIFY_USERINFO_TYPE_ALLIANCE	= 0;
	public static final int						NOTIFY_USERINFO_TYPE_FRIEND		= 1;

	private UserManager()
	{
		reset();
	}

	public void reset()
	{
		userList = new ArrayList<User>();
		banUidList = new ArrayList<String>();
		banNoticeUidList = new ArrayList<String>();
		blockUidList = new ArrayList<String>();
		reportUidList = new ArrayList<String>();
		reportContentList = new ArrayList<Msg>();
		reportContentTranslationList = new ArrayList<Msg>();
		currentMail = new MailInfo();
		allianceMemberMap = new HashMap<String, ArrayList<User>>();
		rankMap = new HashMap<String, Integer>();
		allianceMemberInfoMap = new HashMap<String, User>();
		nonAllianceMemberInfoMap = new HashMap<String, User>();
		friendMemberMap = new HashMap<String, User>();
	}

	public static UserManager getInstance()
	{
		if (instance == null)
		{
			instance = new UserManager();
		}
		return instance;
	}

	public void setCurrentUserId(String id)
	{
		if (!StringUtils.isEmpty(id))
			currentUserId = id;
	}

	public String getCurrentUserId()
	{
		return currentUserId;
	}

	public void setCurrentUser(User user)
	{
		currentUser = user;
	}

	public User getCurrentUser()
	{
		if(currentUser != null)
			return currentUser;
		if (!StringUtils.isEmpty(currentUserId))
		{
			User user = getUser(currentUserId);
			if (user == null)
			{
				user = new User();
				user.uid = currentUserId;
				addUser(user);
			}
			return user;
		}
		else
		{
			LogUtil.trackMessage("UserManager.getCurrentUser() currentUserId is empty");
			return null;
		}
	}

	/**
	 * 如果UserManager获取不到，就从DB获取
	 */
	public User getUser(String userID)
	{
		for (int i = 0; i < userList.size(); i++)
		{
			if (userID.equals(userList.get(i).uid))
				return userList.get(i);
		}

		User result = null;
		result = DBManager.getInstance().getUserDao().getById(userID);
		if (result != null)
		{
			_addUser(result);
		}

		return result;
	}
	/**
	 * 仅在get不到的时候才调用
	 */
	public void addUser(User user)
	{
		if (!isUserExists(user.uid))
		{
			_addUser(user);

			if (!user.isDummy)
			{
				DBManager.getInstance().getUserDao().add(user);
			}
		}
	}

	/**
	 * 实际添加，不触发数据库刷新
	 */
	private void _addUser(User user)
	{
		userList.add(user);
	}
	public boolean isUserExists(String userID)
	{
		for (int i = 0; i < userList.size(); i++)
		{
			if (userID.equals(userList.get(i).uid))
				return true;
		}
		return false;
	}

	/**
	 * 检查uid指向的用户在db中是否存在且是最新的，如果不是则从后台获取用户信息
	 * <p>
	 * 如果用户不存在，会创建一个dummy user
	 * <p>
	 * 
	 * @param name
	 *            可为""，如果指定的话，创建dummy user时，设置其name
	 * @param updateTime
	 *            为0时只检查存在性(认为是新的，可能是db中以前没存)，大于0时检查新旧性
	 */
	public static void checkUser(String uid, String name, int updateTime)
	{
		User user = UserManager.getInstance().getUser(uid);

		boolean isOld = false;
		if (user != null)
		{
			isOld = updateTime > 0 ? updateTime > user.lastUpdateTime : false;
		}

		// 以前有!user.isValid()条件，是多余的。
		// dummy user只有本函数创建，如果是dummy的，说明已经获取过了，不需要再次获取
		if (user == null || (isOld && !user.uid.equals(UserManager.getInstance().getCurrentUser().uid)) || user.lang == null)
		{
			if (user != null && !IMCore.getInstance().host.isUsingDummyHost())
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid", uid, "user", user, "updateTime", updateTime,
						"user.lastUpdateTime", user.lastUpdateTime, "isOld", isOld);
			}
			else if(!IMCore.getInstance().host.isUsingDummyHost())
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid", uid, "user", user, "updateTime", updateTime,
						"isOld", isOld);
			}

			if (user == null)
			{
				user = new User(uid);
				if (StringUtils.isNotEmpty(name))
					user.userName = name;
				UserManager.getInstance().addUser(user);
			}

			ArrayList<String> uids = new ArrayList<String>();
			uids.add(uid);
			UserManager.getInstance().getMultiUserInfo(uids);
		}
	}

	private static String array2Str(ArrayList<String> arr)
	{
		String result = "";
		for (int i = 0; i < arr.size(); i++)
		{
			if (i > 0)
			{
				result += ",";
			}
			result += arr.get(i);
		}
		return result;
	}

	private synchronized void getMultiUserInfo(ArrayList<String> uids)
	{
		if (IMCore.getInstance().getAppConfig().canGetMultiUserInfo())
		{
			return;
		}
		
		synchronized (this)
		{
			boolean hasNewUid = false;

			for (int i = 0; i < uids.size(); i++)
			{
				String uid = uids.get(i);
				if (!fechingUids.contains(uid) && !queueUids.contains(uid) && !unknownUids.contains(uid))
				{
					// LogUtil.printVariablesWithFuctionName(Log.INFO,
					// LogUtil.TAG_MSG, "uid", uid, "fechingUids",
					// array2Str(fechingUids),
					// "queueUids", array2Str(queueUids), "user",
					// UserManager.getInstance().getUser(uid));

					queueUids.add(uid);
					hasNewUid = true;
					lastAddUidTime = System.currentTimeMillis();
				}
			}

			if (hasNewUid && service == null)
			{
				startTimer();
			}
		}
	}

	private synchronized void startTimer()
	{
		if (service != null)
		{
			return;
		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);

		service = Executors.newSingleThreadScheduledExecutor();
		timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					checkUidQueue();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		service.scheduleWithFixedDelay(timerTask, 100, 2500, TimeUnit.MILLISECONDS);
	}

	private boolean isQueueClear()
	{
		return queueUids.size() == 0 && fechingUids.size() == 0;
	}

	private synchronized void checkUidQueue()
	{
		if (isQueueClear())
		{
			return;
		}

		synchronized (this)
		{
			long now = System.currentTimeMillis();

			if ((now - lastAddUidTime) > 500 && (!isCalling() || isLastCallTimeOut()))
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "now", now, "lastAddUidTime", lastAddUidTime
						, "lastCallSuccessTime", lastCallSuccessTime, "lastCallTime", lastCallTime);
				callJNI();
			}
		}
	}
	
	private boolean isCalling()
	{
		// 实际调后台后，lastCallSuccessTime会被置为大数
		return (lastCallSuccessTime - System.currentTimeMillis()) > 0;
	}

	private boolean isLastCallTimeOut()
	{
		return lastCallTime > 0 && (System.currentTimeMillis() - lastCallTime) > CALL_TIME_OUT;
	}
	
	/**
	 * 距最后添加uid有一定时间，且两个队列不都为空，且没有正在调用后台（没有调过，没有成功触发，或者已经调用成功），或者调后台后已经超时
	 */
	private synchronized void callJNI()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fechingUids", array2Str(fechingUids), "queueUids", array2Str(queueUids));
		if (fechingUids.size() > 0 && isCalling() && isLastCallTimeOut())
		{
			// 后台接口未返回，导致超时
			LogUtil.trackMessage("超时：fechingUids is not empty");
			LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "超时：fechingUids is not empty");
		}
		int count = queueUids.size() > (GET_USER_INFO_UID_COUNT - fechingUids.size()) ? (GET_USER_INFO_UID_COUNT - fechingUids.size())
				: queueUids.size();
		for (int i = 0; i < count; i++)
		{
			fechingUids.add(queueUids.remove(0));
		}
		String uidsStr = Channel.getMembersString(fechingUids);
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_MSG, "fechingUidsNew", array2Str(fechingUids), "queueUidsNew", array2Str(queueUids));
		IMCore.getInstance().getAppConfig().excuteJNIVoidMethod("getMultiUserInfo", new Object[] { uidsStr });
	}
	
	public synchronized void onServerActualCalled()
	{
		lastCallTime = System.currentTimeMillis();
		lastCallSuccessTime = System.currentTimeMillis() * 2;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fechingUids", array2Str(fechingUids), "queueUids",
				array2Str(queueUids));
	}

	public synchronized void onReceiveUserInfo(Object[] userInfoArray)
	{
		if (userInfoArray == null)
			return;

		synchronized (this)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);
			for (int i = 0; i < userInfoArray.length; i++)
			{
				User user = (User) userInfoArray[i];
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_MSG, "uid", user.uid);
				if (friendMemberMap.containsKey(user.uid))
				{
					putFriendMemberInMap(user);
				}
				if (allianceMemberInfoMap.containsKey(user.uid))
					putChatRoomMemberInMap(user);

				user.initNullField();
				User oldUser = getUser(user.uid);

				if (fechingUids.contains(user.uid))
				{
					fechingUids.remove(user.uid);
				}

				if (oldUser == null)
				{
					LogUtil.trackMessage("onReceiveUserInfo(): oldUser is null (impossible): " + user.uid);
					LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "oldUser is null (impossible):" + user.uid);
					addUser(user);
				}
				else if (oldUser.isDummy || user.lastUpdateTime > oldUser.lastUpdateTime || (oldUser.lang == null && user.lang != null))
				{
					updateUser(user);
				}
				else
				{
					LogUtil.trackMessage("onReceiveUserInfo(): user is not newer: " + user.uid);
					LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "user is not newer:" + user.uid);
					LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG,
							"compare user:\n" + LogUtil.compareObjects(new Object[] { oldUser, user }));
				}
			}
			
			if (fechingUids.size() > 0)
			{
				// 成功返回，但有些uid取不到
				LogUtil.trackMessage("取不到：fechingUids is not empty");
				LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "取不到：", array2Str(fechingUids));
				for (int i = 0; i < fechingUids.size(); i++)
				{
					unknownUids.add(fechingUids.get(i));
					LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG, fechingUids.get(i));
				}
				fechingUids.clear();
			}

			lastCallSuccessTime = System.currentTimeMillis();
			lastAddUidTime = System.currentTimeMillis();

//			GSController.getInstance().notifyUserDataChanged();
		}
	}

	public void onReceiveSearchUserInfo(Object[] userInfoArray)
	{
		if (userInfoArray == null)
			return;

		final ArrayList<User> userArr = new ArrayList<User>();
		ArrayList<String> nonAllianceMemberArr = getSelctedMemberArr(false);
		for (int i = 0; i < userInfoArray.length; i++)
		{
			User user = (User) userInfoArray[i];
			if (nonAllianceMemberArr.contains(user.uid))
				continue;
			userArr.add(user);
			putChatRoomMemberInMap(user);
			user.initNullField();
			User oldUser = getUser(user.uid);

			if (oldUser == null)
			{
				addUser(user);
			}
			else if (oldUser.isDummy || user.lastUpdateTime > oldUser.lastUpdateTime || (oldUser.lang == null && user.lang != null))
			{
				updateUser(user);
//				GSController.getInstance().notifyUserDataChanged();
			}
		}
		IMCore.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
//					if (ChatServiceController.getMemberSelectorFragment() != null)
//					{
//						ChatServiceController.getMemberSelectorFragment().refreshSearchListData(userArr);
//					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public ArrayList<String> getSelctedMemberArr(boolean isFromAlliance)
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();
		if (UserManager.getInstance().getCurrentUser() == null)
			return memberUidArray;
		
		boolean isInAlliance = !UserManager.getInstance().getCurrentUser().allianceId.equals("");

//		if (ChatServiceController.isCreateChatRoom || (ChatServiceController.isInMailDialog() && !ChatServiceController.isInChatRoom()))
//		{
//			if (((isInAlliance && isFromAlliance) || (!isInAlliance && !isFromAlliance))
//					&& !UserManager.getInstance().getCurrentUser().uid.equals(""))
//				memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
//		}
//		else if (ChatServiceController.isInChatRoom())
//		{
//			memberUidArray = new ArrayList<String>();
//			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
//			Set<String> uidKeySet = memberInfoMap.keySet();
//			List<String> userArray = ChannelManager.getInstance().getChatRoomMemberArrayByKey(getCurrentMail().opponentUid);
//			for (int i = 0; i < userArray.size(); i++)
//			{
//				String uid = userArray.get(i);
//				if (!uid.equals("") && (isFromAlliance && uidKeySet.contains(uid)) || (!isFromAlliance && !uidKeySet.contains(uid)))
//					memberUidArray.add(uid);
//			}
//		}
		return memberUidArray;
	}

	private void resetAllianceRank(String key)
	{
		if (allianceMemberMap.containsKey(key))
			return;
		ArrayList<User> userInfoArray = new ArrayList<User>();
		allianceMemberMap.put(key, userInfoArray);
	}
	
	public void updateUser(User user)
	{
		if(user == null) return;
		for (int i = 0; i < userList.size(); i++)
		{
			if (user.uid.equals(userList.get(i).uid))
			{
				userList.set(i, user);
			}
		}
		DBManager.getInstance().updateUser(user);
	}

	/**
	 * 初始登录时会调，此时数据库还未初始化
	 */
	public void updateCurrentUser()
	{
		DBManager.getInstance().updateUser(getCurrentUser());
	}

	public void putNonAllianceInMap(User user)
	{
		if (user == null)
			return;
		String uid = user.uid;
		nonAllianceMemberInfoMap.put(uid, user);
	}

	public void putChatRoomMemberInMap(User user)
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return  ;
		int rank = user.allianceRank;
		String uid = user.uid;
		String allianceId = UserManager.getInstance().getCurrentUser().allianceId;
		if (allianceId != null && allianceId.equals(user.allianceId))
		{
			allianceMemberInfoMap.put(uid, user);

			if (rank > 0)
			{
				String rankKey = getRankLang(rank);
				rankMap.put(rankKey, Integer.valueOf(rank));
				resetAllianceRank(rankKey);
				ArrayList<User> userArr = allianceMemberMap.get(rankKey);
				boolean isInRank = false;
				for (int i = 0; i < userArr.size(); i++)
				{
					User info = userArr.get(i);
					if (info.uid.equals(user.uid))
					{
						allianceMemberMap.get(rankKey).remove(info);
						allianceMemberMap.get(rankKey).add(user);
						isInRank = true;
						return;
					}
				}
				if (!isInRank)
					allianceMemberMap.get(rankKey).add(user);
			}
		}
		else
		{
			putNonAllianceInMap(user);
		}
	}
	
	public void putFriendMemberInMap(User user)
	{
		if (user == null)
			return;
		String uid = user.uid;
		friendMemberMap.put(uid, user);
	}

	public boolean isCurrentUserInAlliance()
	{
		if (getCurrentUser() != null && StringUtils.isNotEmpty(getCurrentUser().allianceId))
			return true;
		return false;
	}
	
	public boolean isInBattleField()
	{
		return getCurrentUser()!=null && getCurrentUser().crossFightSrcServerId > 0;
	}

	public MailInfo getCurrentMail()
	{
		return currentMail;
	}

	public HashMap<String, ArrayList<User>> getChatRoomMemberMap()
	{
		return allianceMemberMap;
	}

	public HashMap<String, User> getChatRoomMemberInfoMap()
	{
		return allianceMemberInfoMap;
	}

	public HashMap<String, User> getNonAllianceMemberInfoMap()
	{
		return nonAllianceMemberInfoMap;
	}

	public String getRankLang(int rank)
	{
		String rankStr = "";
		switch (rank)
		{
			case 1:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK1);
				break;
			case 2:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK2);
				break;
			case 3:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK3);
				break;
			case 4:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK4);
				break;
			case 5:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK5);
				break;
		}
		return rankStr;
	}

	public HashMap<String, Integer> getRankMap()
	{
		return rankMap;
	}

	public HashMap<String, ArrayList<User>> getJoinedMemberMap(String key, List<String> uidArr)
	{
		HashMap<String, ArrayList<User>> map = new HashMap<String, ArrayList<User>>();

		if (uidArr != null && uidArr.size() > 0)
		{
			ArrayList<User> userArr = new ArrayList<User>();
			HashMap<String, User> memberInfoMap = UserManager.getInstance().getNonAllianceMemberInfoMap();
			for (int i = 0; i < uidArr.size(); i++)
			{
				String uid = uidArr.get(i);
				if (!uid.equals(""))
				{
					if (memberInfoMap.containsKey(uid))
						userArr.add(memberInfoMap.get(uid));
					else
					{
						checkUser(uid, "", 0);
						User user = getUser(uid);
						if (user != null)
						{
							userArr.add(user);
						}
					}
				}
			}

			if (userArr.size() > 0)
				map.put(key, userArr);
		}
		return map;
	}

	public ArrayList<String> getFriendMemberArr()
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();

		if (friendMemberMap != null && friendMemberMap.size() > 0)
		{
			Set<String> uidKeySet = friendMemberMap.keySet();
			for (String uid : uidKeySet)
			{
				if (StringUtils.isNotEmpty(uid))
					memberUidArray.add(uid);
			}
		}
		return memberUidArray;
	}

	public HashMap<String, ArrayList<User>> getFriendMemberMap(String key, List<String> uidArr)
	{
		HashMap<String, ArrayList<User>> map = new HashMap<String, ArrayList<User>>();
		if (uidArr != null && uidArr.size() > 0)
		{
			ArrayList<User> userArr = new ArrayList<User>();
			for (int i = 0; i < uidArr.size(); i++)
			{
				String uid = uidArr.get(i);
				if (!uid.equals(""))
				{
					if (friendMemberMap.containsKey(uid) && friendMemberMap.get(uid) != null && !friendMemberMap.get(uid).isDummy)
						userArr.add(friendMemberMap.get(uid));
					else
					{
						checkUser(uid, "", 0);
						User user = getUser(uid);
						if (user != null)
						{
							userArr.add(user);
						}
					}
				}
			}

			if (userArr.size() > 0)
				map.put(key, userArr);
		}
		return map;
	}
	
	public String createUidStr(ArrayList<String> uidArr)
	{
		String uidStr = "";
		for (int i = 0; i < uidArr.size(); i++)
		{
			if (!uidArr.get(i).equals(""))
			{
				if (!uidStr.equals(""))
					uidStr = uidStr + "|" + uidArr.get(i);
				else
					uidStr = uidArr.get(i);
			}
		}
		return uidStr;

	}

	public String createNameStr(ArrayList<String> uidArr)
	{
		String nameStr = "";
		for (int i = 0; i < uidArr.size(); i++)
		{
			if (!uidArr.get(i).equals(""))
			{
				String uid = uidArr.get(i);

				User user = null;
				if (allianceMemberInfoMap.containsKey(uid))
				{
					user = allianceMemberInfoMap.get(uid);
				}
				else if (nonAllianceMemberInfoMap.containsKey(uid))
				{
					user = nonAllianceMemberInfoMap.get(uid);
				}
				else if (friendMemberMap.containsKey(uid))
				{
					user = friendMemberMap.get(uid);
				}

				if (user == null)
					user = getUser(uid);

				if (user != null)
				{
					if (!nameStr.equals(""))
						nameStr = nameStr + "," + user.userName;
					else
						nameStr = user.userName;
				}
			}
		}
		return nameStr;
	}

	public void addRestrictUser(String uid, int type)
	{
		if (!isInRestrictList(uid, type))
		{
			if (type == BLOCK_LIST)
				blockUidList.add(uid);
			else if (type == BAN_LIST)
				banUidList.add(uid);
			else if (type == REPORT_LIST)
				reportUidList.add(uid);
			else if (type == BAN_NOTICE_LIST)
				banNoticeUidList.add(uid);
		}
	}
	
	public String getShieldSql()
	{
		if(blockUidList == null || blockUidList.size() <=0)
			return "";
		String result = "";
		for (int i = 0; i < blockUidList.size(); i++)
		{
			String uid = blockUidList.get(i);
			if(StringUtils.isNotEmpty(uid))
				result += " AND UserID <> " + uid;
		}
		return result;
	}

	public void addReportContent(Msg item, int type)
	{
		if (!isInReportContentList(item, type))
		{
			if (type == REPORT_CONTETN_LIST)
				reportContentList.add(item);
			else
				reportContentTranslationList.add(item);
		}
	}

	public void removeRestrictUser(String uid, int type)
	{
		if (type == BLOCK_LIST)
		{
			for (int i = 0; i < blockUidList.size(); i++)
			{
				String n = blockUidList.get(i);
				if (n.equals(uid))
				{
					blockUidList.remove(i);
				}
			}
		}
		else if (type == BAN_LIST)
		{
			for (int i = 0; i < banUidList.size(); i++)
			{
				String n = banUidList.get(i);
				if (n.equals(uid))
				{
					banUidList.remove(i);
				}
			}
		}
		else if (type == BAN_NOTICE_LIST)
		{
			for (int i = 0; i < banNoticeUidList.size(); i++)
			{
				String n = banNoticeUidList.get(i);
				if (n.equals(uid))
				{
					banNoticeUidList.remove(i);
				}
			}
		}
	}

	public boolean isInReportContentList(Msg msgItem, int type)
	{
		if (type == REPORT_CONTETN_LIST && reportContentList != null && reportContentList.contains(msgItem))
			return true;
		else if (type == REPORT_TRANSLATION_LIST && reportContentTranslationList != null && reportContentTranslationList.contains(msgItem))
			return true;
		return false;
	}

	public boolean isInRestrictList(String uid, int type)
	{
		if (type == BLOCK_LIST)
		{
			for (int i = 0; i < blockUidList.size(); i++)
			{
				String n = blockUidList.get(i);
				if (n.equals(uid))
					return true;
			}
		}
		else if (type == BAN_LIST)
		{
			for (int i = 0; i < banUidList.size(); i++)
			{
				String n = banUidList.get(i);
				if (n.equals(uid))
					return true;
			}
		}
		else if (type == BAN_NOTICE_LIST)
		{
			for (int i = 0; i < banNoticeUidList.size(); i++)
			{
				String n = banNoticeUidList.get(i);
				if (n.equals(uid))
					return true;
			}
		}
		else if (type == REPORT_LIST)
		{
			for (int i = 0; i < reportUidList.size(); i++)
			{
				String n = reportUidList.get(i);
				if (n.equals(uid))
					return true;
			}
		}

		return false;
	}

	public void clearAllianceMember()
	{
		if (allianceMemberMap != null)
			allianceMemberMap.clear();
		if (allianceMemberInfoMap != null)
			allianceMemberInfoMap.clear();
		if (rankMap != null)
			rankMap.clear();
	}

	public void clearFriendMember()
	{
		if (friendMemberMap != null)
			friendMemberMap.clear();
	}

	public void clearNonAllianceMember()
	{
		if (nonAllianceMemberInfoMap != null)
			nonAllianceMemberInfoMap.clear();
	}
}
