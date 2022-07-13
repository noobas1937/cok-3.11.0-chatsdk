package com.elex.im.core.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.elex.im.core.model.User;
import com.elex.im.core.util.StringUtils;
import com.j256.ormlite.dao.Dao;


/**
 * ORM工具的DAO 包装类
 */
public class UserDao
{
	private DBHelper			helper;
	private Dao<User, Integer>	userDAO;

	public UserDao(Context context)
	{
		try
		{
			helper = DBHelper.getInstance(context);
			userDAO = helper.getDao(User.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void add(User user)
	{
		if (user == null)
		{
			return;
		}
		
		try
		{
			userDAO.create(user);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList<User> getAll()
	{
		ArrayList<User> queryForAll = null;
		try
		{
			queryForAll = (ArrayList<User>) userDAO.queryForAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return queryForAll;
	}

	public User getById(String uid)
	{
		if (StringUtils.isEmpty(uid))
		{
			return null;
		}
		User user = null;
		try
		{
			List<User> queryForEq = userDAO.queryForEq(DBDefinition.USER_COLUMN_USER_ID, uid);
			if(queryForEq.size() > 0)
				user = queryForEq.get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return user;
	}

	public void update(User user)
	{
		try
		{
			userDAO.update(user);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
