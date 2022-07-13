package com.elex.im.core.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.elex.im.core.model.Channel;
import com.elex.im.core.model.Msg;
import com.elex.im.core.util.StringUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

/**
 * Msg_XXX表 使用ORM工具管理效果不明显，推荐使用SQLite，进行手动DAO封装
 */
public class MsgDAO
{
	private DBHelper			helper;
	private SQLiteDatabase		db;
	/** ormLite的DAO，对于不好管理的Msg表不太适用，可选择性遗弃 */
	private Dao<Msg, Integer>	msgDao;

	public MsgDAO(Context context, SQLiteDatabase dataBase)
	{
		helper = DBHelper.getInstance(context);
		this.db = dataBase;
		try
		{
			msgDao = helper.getDao(Msg.class);
			// db = helper.getWritableDatabase();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	// public long insertTestMsg(){
	// return msgDAOCustom.addTest();
	// }
	// 此方法尝试使用ormLite 的raw查询，但实际与SQLite的原始查询没有太大区别
	// public ArrayList<Msg> getMsgRaw(){
	// ArrayList<Msg> fingMsg = msgDAOCustom.fingMsg();
	// return fingMsg;
	// }

	public void insertMsgs(ArrayList<Msg> msgs)
	{
		insertMsgs(msgs.toArray(new Msg[0]));
	}

	/**
	 * 需要保证msgs是从旧到新排列的，这样万一中间出错（如sd卡移除、sd卡写满），后面还可以重新获取
	 */
	public void insertMsgs(final Msg[] msgs)
	{
		try
		{
			msgDao.callBatchTasks(new Callable<Void>()
			{
				public Void call() throws Exception
				{
					for (Msg msg : msgs)
					{
						insertMsg(msg);
					}
					return null;
				}
			});
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void insertMsg(Msg msg)
	{
		try
		{
			if (getBySeqId(msg.sequenceId) == null)
			{
				msgDao.create(msg);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList<Msg> getAll()
	{
		ArrayList<Msg> queryForAll = null;
		try
		{
			queryForAll = (ArrayList<Msg>) msgDao.queryForAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return queryForAll;
	}

	public Msg getById(String id)
	{
		Msg msg = null;
		try
		{
			List<Msg> queryForEq = msgDao.queryForEq("_id", id);
			msg = queryForEq.get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return msg;
	}

	public Msg getBySeqId(int seqId)
	{
		Msg msg = null;
		try
		{
			List<Msg> queryForEq = msgDao.queryForEq(DBDefinition.MSG_SEQUENCE_ID, seqId);
			if(queryForEq.size() > 0)
				msg = queryForEq.get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return msg;
	}

	public void update(Msg msg)
	{
		try
		{
			msgDao.update(msg);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void insertMsgs(ArrayList<Msg> msgs, MsgTable msgTable)
	{
		insertMsgs(msgs.toArray(new Msg[0]), msgTable);
	}

	public void insertMsgs(Msg[] msgs, MsgTable msgTable)
	{
		if (StringUtils.isEmpty(msgTable.channelID))
		{
			return;
		}
		addMsgs(msgs, msgTable);
	}
	
	public void addMsgs(Msg[] msgs, MsgTable msgTable)
	{

	}

	// 使用SQLite语句
	public long addTest()
	{

		ContentValues values = new ContentValues();

		// SQLiteDatabase database = helper.getWritableDatabase();

		values.put(DBDefinition.MSG_CHANNEL_TYPE, 3);
		values.put(DBDefinition.MSG_UID, "abc");
		values.put(DBDefinition.MSG_MSG, "asdtgdsda");

		long insert = db.insert("Msg_123", "", values);

		return insert;
	}

	public ArrayList<Msg> findMsgBySize(MsgTable msgTable, long size)
	{
		ArrayList<Msg> result = new ArrayList<Msg>();
		if (StringUtils.isEmpty(msgTable.channelID))
		{
			return result;
		}
//		Cursor cursor = db.query(msgTable.getTableName(), null, null, null, null, null, "DESC", size + " , " + size * index);
//		if (cursor == null || cursor.getCount() == 0)
//		{
//			return result;
//		}
		
		List<Msg> queryForEq;
		try
		{
			queryForEq = msgDao.queryBuilder().orderBy(DBDefinition.MSG_CREATE_TIME, false).limit(size).where()
					.eq(DBDefinition.MSG_CHANNEL_ID, msgTable.channelID).and()
					.eq(DBDefinition.MSG_CHANNEL_TYPE, msgTable.channelType).query();
			for (Msg msg : queryForEq)
			{
				result.add(msg);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public ArrayList<Msg> findMsgByTime(MsgTable msgTable, int createTime, int size)
	{
		return null;
	}
	

	public ArrayList<Msg> getMsgsByTime(Channel channel, int minCreateTime, long countLimit)
	{
		ArrayList<Msg> result = new ArrayList<Msg>();
		if (StringUtils.isEmpty(channel.getChannelID()))
		{
			return result;
		}

		List<Msg> queryForEq;
		try
		{
			queryForEq = msgDao.queryBuilder().orderBy(DBDefinition.MSG_CREATE_TIME, false).limit(countLimit).where()
					.eq(DBDefinition.MSG_CHANNEL_ID, channel.getChannelID()).and()
					.eq(DBDefinition.MSG_CHANNEL_TYPE, channel.getChannelType()).and()
					.lt(DBDefinition.MSG_CREATE_TIME, minCreateTime).query();
			for (Msg msg : queryForEq)
			{
				result.add(msg);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public void update(Msg msg, MsgTable msgTable)
	{
	}

	/**
	 * 尝试使用ormLite的rawQuery，回调格式还行，但没有减少太多工作量，可以直接使用SQLite查询
	 */
	public ArrayList<Msg> fingMsg()
	{
		String sql = "SELECT * FROM Msg_123";
		ArrayList<Msg> msgList = new ArrayList<Msg>();
		// 使用ormLite 的rawSql语句
		try
		{
			GenericRawResults<Msg> queryRaw = msgDao.queryRaw(sql, new RawRowMapper<Msg>()
			{
				@Override
				public Msg mapRow(String[] columnNames, String[] resultColumns) throws SQLException
				{
					Msg msg = new Msg();
					msg.uid = resultColumns[2];
					msg.channelType = Integer.valueOf(resultColumns[3]);
					return msg;
				}
			});
			Iterator<Msg> iterator = queryRaw.iterator();
			while (iterator.hasNext())
			{
				Msg msg = iterator.next();
				msgList.add(msg);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return msgList;
	}

}
