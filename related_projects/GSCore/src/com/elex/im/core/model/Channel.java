package com.elex.im.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Pair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.ChannelChangeEvent;
import com.elex.im.core.event.EventCenter;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.model.db.MsgTable;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.MathUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Channel")
public class Channel implements Comparable<Channel>
{
	@DatabaseField(generatedId = true)
	public int					_id;
	@DatabaseField
	private int					channelType			= -1;
	@DatabaseField
	private String				channelID;
	@DatabaseField
	private String				memberUidStr;

	/** 聊天室房主 */
	@DatabaseField
	private String				roomOwner;
	/** 聊天室自定义名称 */
	@DatabaseField
	private String				customName			= "";
	/** 最近消息时间 */
	@DatabaseField
	private long				latestTime			= -1;
	/** 聊天室设置 */
	@DatabaseField
	private String				settings;
	/** 聊天草稿 */
	@DatabaseField
	private String				draft				= "";
	/** 聊天草稿时间 */
	@DatabaseField
	private long				draftTime			= -1;
	/** 最近修改时间，仅针对系统邮件 */
	@DatabaseField
	private long				latestModifyTime	= -1;

	// 运行时属性
	/** 消息对象List，保存所有消息 */
	public ArrayList<Msg>		msgList				= new ArrayList<Msg>();
	/** 正在发送的消息 */
	public ArrayList<Msg>		sendingMsgList		= new ArrayList<Msg>();
	/** 聊天室成员uid列表 */
	public ArrayList<String>	memberUidArray		= new ArrayList<String>();
	public int					firstNewMsgSeqId;
	public long					serverMaxTime;
	public long					serverMinTime;
	/** 连ws后台时，登陆后从history.roomsv2接口加载到的新消息数量 **/
	public int					wsNewMsgCount;
	/** 是否是聊天室成员 */
	public boolean				isMember			= false;

	private List<Integer>			msgTimeIndexArray	= null;
	public boolean					isMemberUidChanged;
	public HashMap<String, Bitmap>	chatroomHeadImages;
	public int						customPicLoadingCnt;
	public Point					lastPosition		= new Point(-1, -1);
	/** 是否正在批量加载新消息 */
	public boolean				isLoadingAllNew					= false;
	/** 是否已经批量加载过新消息 */
	public boolean				hasLoadingAllNew				= false;

	private boolean		initLoaded	= false;
	/** 最近消息的id（邮件专用） */
	public String				latestId						= "0";
	private Object			channelView						= null;
	private int	unreadCount;
	public boolean	noMoreDataFlag;
	
	private String wsRoomId;
	private String wsGroupId;
	private int tabIndex;
	
	public Channel()
	{
	}
	
	/**
	 * 从db加载更多消息
	 */
	public void loadMoreMsg()
	{
		
	}
	
	/**
	 * 去重添加
	 * 修改自己发送消息的状态
	 * 存储db
	 */
	public void onReceiveMsg()
	{
		
	}
	
	/**
	 * 预处理要插入list的消息
	 */
	public void preProcessMsg(Msg msg)
	{
//		initMsg(msg);
	}
	
	private void initMsg(Msg msg)
	{
//		refreshRenderData();
	}
	
	public MsgTable getMsgTable(){
		return MsgTable.createMsgTable(getChannelID(),getChannelType());
	}

	public void sendDummyAudioMsg(long length, int sendLocalTime)
	{
		int post = Msg.MSG_TYPE_AUDIO;
		Msg msgItem = new Msg(UserManager.getInstance().getCurrentUser().uid, true, true, channelType, post,
				"" + length, sendLocalTime);
		msgItem.sendState = Msg.SENDING;
		msgItem.createTime = sendLocalTime;

		msgItem.initUserForSendedMsg();
		
		addDummySequenceId(msgItem);

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		sendingMsgList.add(msgItem);

		addDummyMsg(msgItem);
		oldSendTime = System.currentTimeMillis();
	}

	private static long	oldSendTime	= 0;	// 上一次发送时间
	// 发送消息
	public void sendMsg(final String messageText, final boolean isHornMsg, boolean usePoint, String audioUrl)
	{
		int sendLocalTime = TimeManager.getInstance().getCurrentTime();
		int post = isHornMsg ? 6 : Msg.MSGITEM_TYPE_MESSAGE;

		if (StringUtils.isNotEmpty(audioUrl))
		{
			post = Msg.MSG_TYPE_AUDIO;
		}

		int channelType = getChannelType();
		// 创建消息对象，加入正在发送列表
		Msg msgItem = new Msg(UserManager.getInstance().getCurrentUser().uid, true, true, channelType, post, messageText,
				sendLocalTime);
		msgItem.sendState = Msg.SENDING;
		msgItem.createTime = sendLocalTime;
		if (StringUtils.isNotEmpty(audioUrl))
		{
			msgItem.media = audioUrl;
		}
		msgItem.initUserForSendedMsg();
		
		addDummySequenceId(msgItem);

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		sendingMsgList.add(msgItem);

		// 加入model，更新视图
		try
		{
			trackSendAction(channelType, isHornMsg, true, false,
					msgItem.isAudioMessage());
			addDummyMsg(msgItem);
			
			// 实际发给后台
			sendMsg2WSServer(messageText, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);

			oldSendTime = System.currentTimeMillis();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void addDummySequenceId(Msg msgItem)
	{
		// 去掉了需要不是个人邮件的判断
		if (msgList != null && msgList.size() > 0)
		{
			Msg lastItem = msgList.get(msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}
	}
	
	private void sendMsg2WSServer(String messageText, boolean isHornMessage, boolean usePoint,
			int sendLocalTime, int post, String media)
	{
		WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, this, post, media);
	}
	
	// 重发消息
	public void resendMsg(Msg msgItem, boolean isHornMsg, boolean usePoint)
	{
		// 显示转圈
		msgItem.sendState = Msg.SENDING;
		final Msg item = msgItem;

		EventCenter.getInstance().dispatchEvent(new ChannelChangeEvent(ChannelChangeEvent.DATASET_CHANGED, this));

		trackSendAction(channelType, isHornMsg, true, true, msgItem.isAudioMessage());
		
		sendMsg2WSServer(msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
	}

	// 重发消息
	public void resendAudioMsg(Msg msgItem)
	{
		// 显示转圈
		msgItem.sendState = Msg.SENDING;

		EventCenter.getInstance().dispatchEvent(new ChannelChangeEvent(ChannelChangeEvent.DATASET_CHANGED, this));

		sendMsg2WSServer(msgItem.msg, false, false, msgItem.sendLocalTime, Msg.MSG_TYPE_AUDIO, msgItem.media);
	}

	public void sendAudioMsgToServer(String media,String sendLocalTime)
	{
		int sendTime = 0;
		if(StringUtils.isNumeric(sendLocalTime))
			sendTime = Integer.parseInt(sendLocalTime);
		if (sendingMsgList != null && sendingMsgList.size() > 0)
		{
			Msg sendingItem = null;
			if(sendTime>0)
			{
				for(Msg msgItem: sendingMsgList)
				{
					if(msgItem!=null && msgItem.sendLocalTime == sendTime)
					{
						sendingItem = msgItem;
						break;
					}
				}
			}
			else
			{
				sendingItem = sendingMsgList.get(sendingMsgList.size() - 1);
			}
			
			if (sendingItem != null)
			{
				sendingItem.media = media;
				// 游戏中，个人聊天和聊天室与联盟聊天不一样，没有media这个字段(个人邮件存db，不好改)
				// 所以把元数据存在了msg中，在这里做解析
				// TODO 改成都从独立后台收发的话，如果把格式改为统一，还会有麻烦，旧客户端需要能识别这种格式
				if(IMCore.getInstance().getAppConfig().isExtendedAudioMsg(this))
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
				System.out.println("sendAudioMsgToServer sendingItem.media:"+sendingItem.media + "  sendingItem.msg:"+sendingItem.msg);
				sendMsg2WSServer(sendingItem.msg, false, false, sendingItem.sendLocalTime, Msg.MSG_TYPE_AUDIO, media);
			}
		}
	}

	private static void trackSendAction(int channelType, boolean isHornMsg, boolean isWS, boolean resend, boolean isAudio)
	{
		LogUtil.trackPageView("SendMessage-" + channelType + (isHornMsg ? "-horn" : "") + (isWS ? "-ws" : "") + (resend ? "-resend" : "")
				+ (isAudio ? "-audio" : ""));
	}
	

	public boolean addHistoryMsg(Msg msg)
	{
		if (!isMsgExist(msg) && !isMsgIgnored(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			// TODO 需要改变方法，直接从新消息数组长度获取，websocket需要控制读到所有消息之后一次性插入
			// 计算firstNewMsgSeqId
//			if (firstNewMsgSeqId > 0 && firstNewMsgSeqId == msg.sequenceId)
//			{
//				if (this.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT)
//				{
//					msg.firstNewMsgState = 1;
//				}
//				else
//				{
//					msg.firstNewMsgState = 2;
//				}
//			}

			addMsg(msg);
			return true;
		}
		return false;
	}

	private boolean isMsgExist(Msg msg)
	{
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).msg.equals(msg.msg) && msgList.get(i).createTime == msg.createTime)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isMsgIgnored(Msg msg)
	{
		// TODO 添加过滤逻辑，如国家里不应该有语音聊天，可使用枚举定义
		return false;
	}

	public void addMsg(Msg msg)
	{
		addMsgAndSort(msg);
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则可能新发的消息会插到前面
	 */
	public void addDummyMsg(Msg msg)
	{
		msgList.add(msg);
		// 全部遍历了，有点冗余
		getTimeNeedShowMsgIndex();
		
		EventCenter.getInstance().dispatchEvent(new ChannelChangeEvent(ChannelChangeEvent.AFTER_ADD_DUMMY_MSG, this));
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则会错乱
	 */
	public void replaceDummyMsg(Msg msg, int index)
	{
		msgList.set(index, msg);
	}

	private void addMsgAndSort(Msg msg)
	{
		int pos = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msg.createTime > msgList.get(i).createTime)
			{
				pos = i + 1;
			}
			else
			{
				break;
			}
		}
		msgList.add(pos, msg);
	}

	public void addNewMsg(Msg msg)
	{
		if (!isMsgExist(msg) && !isMsgIgnored(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			addMsg(msg);
			// 未读数修改
//			if (isModChannel())
//			{
//				ChannelManager.getInstance().latestModChannelMsg = msg.msg;
//				Channel modChannel = ChannelManager.getInstance().getModChannel();
//				if (modChannel != null)
//					modChannel.unreadCount++;
//			}
//			else if (isMessageChannel())
//			{
//				ChannelManager.getInstance().latestMessageChannelMsg = msg.msg;
//				Channel messageChannel = ChannelManager.getInstance().getMessageChannel();
//				if (messageChannel != null)
//					messageChannel.unreadCount++;
//			}
		}
	}
	
	public String getChannelName()
	{
		return getChannelName(channelType, channelID);
	}
	public static String getChannelName(int channelType, String channelID)
	{
		return channelID + DBDefinition.getPostfixForType(channelType);
	}

	public String getTableName()
	{
		String channelName = getChannelName();
		String md5TableId = MathUtil.md5(channelName);
		String tableName = "";
		tableName = DBDefinition.chatTableId2Name(md5TableId);

		return tableName;
	}

	public int getChannelType()
	{
		return channelType;
	}

	public void setChannelType(int channelType)
	{
		this.channelType = channelType;
	}

	public String getChannelID()
	{
		return channelID;
	}

	public void setChannelID(String channelID)
	{
		this.channelID = channelID;
	}

	public String getMemberUidStr()
	{
		return memberUidStr;
	}

	public void setMemberUidStr(String memberUidStr)
	{
		this.memberUidStr = memberUidStr;
	}

	public String getRoomOwner()
	{
		return roomOwner;
	}

	public void setRoomOwner(String roomOwner)
	{
		this.roomOwner = roomOwner;
	}

	public String getCustomName()
	{
		return customName;
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	public long getLatestTime()
	{
		return TimeManager.getTimeInMS(latestTime);
	}

	public void setLatestTime(long latestTime)
	{
		this.latestTime = latestTime;
	}

	public String getSettings()
	{
		return settings;
	}

	public void setSettings(String settings)
	{
		this.settings = settings;
	}

	public String getDraft()
	{
		return draft;
	}

	public void setDraft(String draft)
	{
		this.draft = draft;
	}

	public long getDraftTime()
	{
		return draftTime;
	}

	public void setDraftTime(long draftTime)
	{
		this.draftTime = draftTime;
	}

	public long getLatestModifyTime()
	{
		return latestModifyTime;
	}

	public void setLatestModifyTime(long latestModifyTime)
	{
		this.latestModifyTime = latestModifyTime;
	}

	public void getTimeNeedShowMsgIndex()
	{
		if (msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
				msgTimeIndexArray = new ArrayList<Integer>();
			else
				msgTimeIndexArray.clear();
			int tempCreateTime = 0;
			for (int i = 0; i < msgList.size(); i++)
			{
				Msg msgItem = msgList.get(i);
				if (msgItem.createTime - tempCreateTime > 5 * 60)
				{
					tempCreateTime = msgItem.createTime;
					msgTimeIndexArray.add(Integer.valueOf(i));
				}
			}
		}
	}

	public void getLoadedTimeNeedShowMsgIndex(int loadCount)
	{
		if (msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
			{
				getTimeNeedShowMsgIndex();
			}
			else
			{
				if (msgTimeIndexArray.size() > 0)
					msgTimeIndexArray.remove(Integer.valueOf(0));
				for (int i = 0; i < msgTimeIndexArray.size(); i++)
				{
					Integer indexInt = msgTimeIndexArray.get(i);
					if (indexInt != null)
					{
						msgTimeIndexArray.set(i, Integer.valueOf(indexInt.intValue() + loadCount));
					}
				}

				int tempCreateTime = 0;
				for (int i = 0; i < msgList.size() && i < loadCount + 1; i++)
				{
					Msg msgItem = msgList.get(i);
					if (msgItem.createTime - tempCreateTime > 5 * 60)
					{
						tempCreateTime = msgItem.createTime;
						msgTimeIndexArray.add(Integer.valueOf(i));
					}
				}
			}
		}
	}

	public List<Integer> getMsgIndexArrayForTimeShow()
	{
		return msgTimeIndexArray;
	}

	@Override
	public int compareTo(Channel another)
	{
		if (another == null)
			return -1;
		else
		{
			if (IMCore.getInstance().getAppConfig().isMessageChannel(this) && IMCore.getInstance().getAppConfig().isMessageChannel(another))
			{
				long timeOffset = another.latestTime - latestTime;
				if (timeOffset > 0)
					return 1;
				if (timeOffset < 0)
					return -1;
				else
					return 0;
			}
			else
				return 0;
		}
	}

	public void setChannelView(Object v)
	{
		channelView = v;
	}

	public Object getChannelView()
	{
		return channelView;
	}
	
	/**
	 * 聊天型channel已经完成初次加载（从网络或db加载历史消息，收到push消息）
	 */
	public boolean hasInitLoaded()
	{
		return initLoaded == true || msgList.size() > 0;
	}

	public void clearFirstNewMsg()
	{
		if (wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
		{
			return;
		}

		firstNewMsgSeqId = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			msgList.get(i).firstNewMsgState = 0;
		}
	}

	public void markAsRead()
	{
		if (unreadCount > 0)
		{
			unreadCount = 0;
			latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
//			ChannelManager.getInstance().calulateAllChannelUnreadNum();
			updateDB();
		}
	}

	public int getMinCreateTime()
	{
		if (msgList == null || msgList.size() == 0)
			return 0;

		int result = msgList.get(0).createTime;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).createTime < result)
			{
				result = msgList.get(i).createTime;
			}
		}
		return result;
	}
	
	public Pair<Long, Long> getLoadMoreTimeRange()
	{
		int minTime = getMinCreateTime();
		// 如果用时间，则肯定是webSocket服务，由于时间不连续，没法判断再前面的消息是在db还是server，所以初始化时将新消息全部加载到本地
		Pair<Long, Long> range = null;
		range = DBManager.getInstance().getHistoryTimeRange(this, minTime, ChannelManager.LOAD_MORE_COUNT);
		return range;
	}
	
	public void updateDB()
	{
		DBManager.getInstance().updateChannel(this);
	}
	
	public Msg getLatestMsgInDB()
	{
		//TODO 实现
		return null;
	}

	public void resetMsgChannel()
	{
		initLoaded = false;
		if (msgList != null && msgList.size() > 0)
			msgList.clear();
	}

	public static String getMembersString(ArrayList<String> members)
	{
		String uidsStr = "";
		if (members == null)
			return uidsStr;

		for (int i = 0; i < members.size(); i++)
		{
			try
			{
				uidsStr += (i > 0 ? "|" : "") + members.get(i);
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		return uidsStr;
	}
}
