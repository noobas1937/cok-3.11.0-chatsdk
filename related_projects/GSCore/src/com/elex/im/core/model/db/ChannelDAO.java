package com.elex.im.core.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.elex.im.core.model.Channel;
import com.j256.ormlite.dao.Dao;

public class ChannelDAO
{
	private Dao<Channel, Integer>	channelDAO;
	private DBHelper				helper;

	public ChannelDAO(Context context)
	{
		try
		{
			helper = DBHelper.getInstance(context);
			channelDAO = helper.getDao(Channel.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void add(Channel channel)
	{
		try
		{
			channelDAO.create(channel);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList<Channel> getAll()
	{
		List<Channel> queryForAll = null;
		try
		{
			queryForAll = channelDAO.queryForAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return (ArrayList<Channel>) queryForAll;
	}

	public Channel getChannel(String channelId, int channelType)
	{
		Channel channel = null;

		try
		{
			List<Channel> queryForEq = channelDAO.queryBuilder().where()
					.eq(DBDefinition.CHANNEL_CHANNEL_ID, channelId).and()
					.eq(DBDefinition.CHANNEL_TYPE, channelType).query();
			if(queryForEq.size() > 0)
				channel = queryForEq.get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return channel;
	}

	public Channel getChannel(MsgTable msgTable)
	{
		Channel channel = null;

		try
		{
			List<Channel> queryForEq = channelDAO.queryForEq(DBDefinition.CHANNEL_CHANNEL_ID, msgTable.channelID);
			channel = queryForEq.get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return channel;
	}

	public void update(Channel channel)
	{
		try
		{
			channelDAO.update(channel);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
