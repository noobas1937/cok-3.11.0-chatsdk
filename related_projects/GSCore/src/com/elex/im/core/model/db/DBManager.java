package com.elex.im.core.model.db;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.elex.im.core.IMCore;
import com.elex.im.core.event.DBStatusEvent;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.StringUtils;

public class DBManager
{
	private static DBManager	instance;
	private DBHelper			helper;
	private SQLiteDatabase		db;
	/** 手写DAO */
	private MsgDAO				msgDAOCustom;
	/** 封装ormLiteDAO */
	private ChannelDAO			channelDAO;
	/** 封装ormLiteDAO */
	private UserDao				userDAO;

	private DBManager()
	{
	}
	
	public void initInWrapper()
	{
		isIniting = true;
		initDB(IMCore.hostActivity);
		isIniting = false;
	}

	public static DBManager getInstance()
	{
		if (instance == null)
		{
			instance = new DBManager();
		}
		return instance;
	}
	

	/**
	 * 若isAccountChanged，则强制重新初始化db
	 */
	public static void initDatabase(boolean isAccountChanged, boolean isNewUser)
	{
		isIniting = true;
		
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isAccountChanged", isAccountChanged, "isNewUser", isNewUser,
				"DBManager.getInstance().isDBAvailable()", getInstance().isDBAvailable(),
				"isExternalStoragePermissionsAvaiable()",
				PermissionManager.isExternalStoragePermissionsAvaiable(IMCore.hostActivity), "attemptedGetStoragePermissionsBefore",
				attemptedGetStoragePermissionsBefore);

		if (!isNewUser && needGetStoragePermissions())
		{
			getStoragePermissions(isAccountChanged);
			isIniting = false;
			return;
		}
		if (isAccountChanged && getInstance() != null && getInstance().isDBAvailable())
		{
			getInstance().closeDB();
		}
		if (getInstance() != null && !getInstance().isDBAvailable())
		{
			getInstance().initDB(IMCore.hostActivity);
		}
		if (isAccountChanged)
		{
			IMCore.getInstance().reset();
			WebSocketManager.getInstance().forceClose();
		}
		IMCore.getInstance().dispatch(new DBStatusEvent(DBStatusEvent.INIT_COMPLETE));
		
		isIniting = false;
	}

	public static boolean needGetStoragePermissions()
	{
		return !PermissionManager.isExternalStoragePermissionsAvaiable(IMCore.hostActivity) && !attemptedGetStoragePermissionsBefore;
	}

	private static boolean		initDatabaseParam						= false;
	private static boolean		attemptedGetStoragePermissionsBefore	= false;

	private static void getStoragePermissions(boolean isAccountChanged)
	{
		attemptedGetStoragePermissionsBefore = true;
		initDatabaseParam = isAccountChanged;
		
		PermissionManager.getExternalStoragePermission();
	}

	public void onRequestPermissionsResult()
	{
		initDatabase(initDatabaseParam, false);
	}

	public void initDB(Context context)
	{
		helper = DBHelper.getInstance(context);

		db = helper.getWritableDatabase();

		msgDAOCustom = new MsgDAO(context, db);

		channelDAO = new ChannelDAO(context);

		userDAO = new UserDao(context);
		
		isInited = true;
	}

	public void rmDatabaseFile()
	{
		if (helper != null)
		{
			LogUtil.trackMessage("delete database file by user");
			helper.rmDirectory();
			isInited = false;
			ChannelManager.getInstance().reset();
		}
	}
	
	public UserDao getUserDao()
	{
		return userDAO;
	}
	
	public ChannelDAO getChannelDAO()
	{
		return channelDAO;
	}
	
	public MsgDAO getMsgDAO()
	{
		return msgDAOCustom;
	}

	public Channel getChannel(MsgTable msgTable)
	{
		if (StringUtils.isEmpty(msgTable.channelID))
		{
			return null;
		}
		Channel channel = channelDAO.getChannel(msgTable);
		return channel;
	}

	public ArrayList<Channel> getAllChannels()
	{
		ArrayList<Channel> findAll = channelDAO.getAll();

		return findAll;
	}

	public ArrayList<Msg> getMsgByTime(MsgTable msgTable, int createTime, int size)
	{
		if (StringUtils.isEmpty(msgTable.channelID))
		{
			return null;
		}
		ArrayList<Msg> msgs = msgDAOCustom.findMsgByTime(msgTable, createTime, size);
		return msgs;
	}

	public void insertChannel(Channel channel)
	{
		if (channel == null)
		{
			return;
		}
		channelDAO.add(channel);
	}

	public void updateChannel(Channel channel)
	{
		if (channel == null)
		{
			return;
		}
		channelDAO.update(channel);
	}

	public void updateUser(User user)
	{
		if (user == null)
		{
			return;
		}
		userDAO.update(user);
	}

	public void updateMsg(Msg msg)
	{
		if (msg == null)
		{
			return;
		}
		msgDAOCustom.update(msg);
	}
	
	public void updateMsg(Msg msg, MsgTable msgTable)
	{
		if (StringUtils.isEmpty(msgTable.channelID))
		{
			return;
		}
		msgDAOCustom.update(msg, msgTable);
	}

	public void closeDB()
	{
		try
		{
			if (helper != null)
			{
				helper.close();
				helper = null;
			}
			if (db != null)
			{
				if (db.isOpen())
					db.close();
				db = null;
			}
			isInited = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void prepareMsgTable(MsgTable msgTable)
	{
		// TODO msgTable建表
	}

	public static boolean	isInited		= false;
	public static boolean	isIniting		= false;
	/** 为了打点只记录一次 */
	private static boolean	nullTracked		= false;
	private static boolean	notopenTracked	= false;
	
	public boolean isDBAvailable()
	{
		if (!isInited)
			return false;

		boolean result = db != null && db.isOpen();
		if (!result)
		{
			if (db == null)
			{
				if (isInited && !isIniting && !nullTracked)
				{
					LogUtil.trackMessage("database is unavailable (db is null)");
					nullTracked = true;
				}
			}
			else
			{
				if (!isIniting && !notopenTracked)
				{
					// 什么情况会出现这个，删除database吗？
					LogUtil.trackMessage("database is unavailable (db is not open)");
					notopenTracked = true;
				}
			}
		}
		return result;
	}

	public void prepareChatTable(Channel channel)
	{
		if (StringUtils.isEmpty(channel.getChannelID()) || !isDBAvailable())
			return;

		if (!isTableExists(channel.getTableName()))
		{
//			createChatTable(channel.getTableName());
//
//			if (channel == null)
//			{
//				ChatChannel channel = ChannelManager.getInstance().getChannel(chatTable);
//				insertChannel(channel);
//			}
		}
	}

//	public void insertChannel(Channel channel)
//	{
//		if (channel == null || !isDBAvailable())
//			return;
//		try
//		{
//			db.insert(DBDefinition.TABEL_CHANNEL, null, channel.getContentValues());
//		}
//		catch (Exception e)
//		{
//			LogUtil.printException(e);
//		}
//	}
	
	public Pair<Long, Long> getHistoryTimeRange(Channel channel, int upperTime, int count)
	{
		Pair<Long, Long> result = null;
		if (StringUtils.isEmpty(channel.getChannelID()) || !isDBAvailable())
			return result;

		Cursor c = null;
		try
		{

			String sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s = %d AND %s = '%s' AND %s < %d ORDER BY %s DESC LIMIT %d",
					DBDefinition.MSG_CREATE_TIME, "Msg", 
					DBDefinition.MSG_CHANNEL_TYPE, channel.getChannelType(),
					DBDefinition.MSG_CHANNEL_ID, channel.getChannelID(),
					DBDefinition.MSG_CREATE_TIME, upperTime, DBDefinition.MSG_CREATE_TIME, count);
			c = db.rawQuery(sql, null);
			while (c.moveToNext())
			{
				long createTime = c.getLong(c.getColumnIndex(DBDefinition.MSG_CREATE_TIME));
				if (result == null)
				{
					result = new Pair<Long, Long>(createTime, createTime);
				}
				else
				{
					result = new Pair<Long, Long>(createTime, result.second);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			closeCursor(c);
		}

		return result;
	}
	
	public boolean isTableExists(String tableName)
	{
		if (StringUtils.isEmpty(tableName) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT COUNT(*) FROM %s WHERE type = '%s' AND name = '%s'",
					DBDefinition.TABLE_SQLITE_MASTER, "table", tableName);
			c = db.rawQuery(sql, null);
			if (c.moveToFirst())
			{
				count = c.getInt(0);
			}
		}
		catch (Exception e)
		{
			// LogUtil.printException(e);
		}
		finally
		{
			closeCursor(c);
		}
		return count > 0;
	}
	
	private void closeCursor(Cursor c)
	{
		if (c != null)
		{
			try
			{
				c.close();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}
}
