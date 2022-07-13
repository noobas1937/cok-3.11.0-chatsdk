package com.elex.im.core.model.db;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.StringUtils;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBHelper extends OrmLiteSqliteOpenHelper
{
	private static final String	DATABASE_NAME							= "gsim.sqlite";

	private static final int	VERSION_BASIS							= 1;
	private static final int	VERSION_ADD_CROSS_FIGHT_SRC_SERVER_ID	= 2;	// 6.11提交内网，外网尚未发布过
	private static final int	VERSION_ADD_MAIL_TABLE					= 3;
	private static final int	VERSION_ADD_TITLE_AND_SUMMARY			= 4;
	private static final int	VERSION_ADD_PARSE_VERSION				= 5;
	private static final int	VERSION_ADD_REWARD_LEVEL				= 6;
	private static final int	VERSION_ADD_USER_LANG					= 7;
	private static final int	VERSION_ADD_USER_SVIP					= 8;
	private static final int	VERSION_ADD_DRAFT						= 9;
	private static final int	VERSION_ADD_MAIL_RECYCLE_TIME			= 10;
	private Context				mContext;

	public static final int		CURRENT_DATABASE_VERSION				= VERSION_BASIS;

	private Map<String, Dao>	daos									= new HashMap<String, Dao>();

	private static DBHelper	instance;

	public static DBHelper getInstance(Context context)
	{

		context = context.getApplicationContext();
		if (instance == null)
		{
			synchronized (DBHelper.class)
			{
				if (instance == null)
					instance = new DBHelper(context);
			}
		}

		return instance;
	}

	public DBHelper(Context context)
	{
		// CursorFactory设置为null,使用默认值
		super(context, getDBFilePath(context), null, CURRENT_DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
	{
		try
		{
			/** TableUtils直接创建ormLite可以管理的表 */
			TableUtils.createTable(connectionSource, Channel.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Msg.class);
			// 创建一张测试Msg表
			// database.execSQL(DBDefinition.CREATE_TABEL_MSG);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		// TODO 升级逻辑

	}

	/**
	 * 获得Dao，使用map进行缓存处理
	 */
	public synchronized Dao getDao(Class clazz) throws SQLException
	{
		Dao dao = null;
		String className = clazz.getSimpleName();

		if (daos.containsKey(className))
		{
			dao = daos.get(className);
		}
		if (dao == null)
		{
			dao = super.getDao(clazz);
			daos.put(className, dao);
		}
		return dao;
	}

	/**
	 * 释放资源
	 */
	@Override
	public void close()
	{
		super.close();

		for (String key : daos.keySet())
		{
			Dao dao = daos.get(key);
			dao = null;
		}
	}

	private static String getDBFilePath(Context context)
	{
		String result = getDBDirectoryPath(context, false) + getDBFileName(context);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "directory", result);
		return result;
	}

	public static String getDBFileAbsolutePath(Context context)
	{
		String result = getDBDirectoryPath(context, true) + getDBFileName(context);

		return result;
	}

	private static String getDBFileName(Context context)
	{
		if (isSDCardWritable(context))
		{
			return DATABASE_NAME;
		}
		else
		{
			String user = StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().uid) ? "unknownUser" : UserManager.getInstance()
					.getCurrentUser().uid;
			return user + ".db";
		}
	}

	public static String getDBDirectoryPath(Context context, boolean returnAbsolutePath)
	{
		if (isSDCardWritable(context))
		{
			String user = StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().uid) ? "unknownUser" : UserManager.getInstance()
					.getCurrentUser().uid;
			// SD卡应用目录（卸载会删除）：context.getExternalFilesDir(null) + File.separator
			// + user + "/database/"
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + File.separator + user
					+ "/database/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}

		if (returnAbsolutePath)
		{
			// 用于复制预设db到内存、删除db（不通过DBHelper，无法使用预设目录）
			File files = context.getCacheDir();
			String directory = files.getParentFile().getAbsolutePath() + "/databases/";
			prepareDirectory(directory);
			return directory;
		}
		else
		{
			return "";
		}
	}

	public static boolean isSDCardWritable(Context context)
	{
		return isSDcardAvaiable() && PermissionManager.isExternalStoragePermissionsAvaiable(context);
	}

	public static boolean isSDcardAvaiable()
	{
		String sdCardState = Environment.getExternalStorageState();
		return sdCardState.equals(Environment.MEDIA_MOUNTED);
	}

	public static String getLocalDirectoryPath(Context context, String directoryName)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + "/" + directoryName
					+ "/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		File files = context.getCacheDir();
		String directory = files.getAbsolutePath() + "/" + directoryName + "/";
		prepareDirectory(directory);
		return directory;
	}

	public static File getLocalDirectoryFile(Context context, String dirName)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + "/" + dirName + "/";
			File file = new File(directory);
			if (!(file.exists() && file.isDirectory()))
				file.mkdirs();
			return file;
		}
		File cacheFile = context.getCacheDir();
		String directory = cacheFile.getAbsolutePath() + "/" + dirName + "/";
		File file = new File(directory);
		if (!(file.exists() && file.isDirectory()))
			file.mkdirs();
		return file;
	}

	public static String getHeadDirectoryPath(Context context)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + "/head/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		File files = context.getCacheDir();
		String directory = files.getAbsolutePath() + "/head/";
		prepareDirectory(directory);
		return directory;
	}

	public static String getLocalXiaoMiDirectoryPath(Context context)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/mivtalk/images/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		File files = context.getCacheDir();
		String directory = files.getAbsolutePath() + "/";
		prepareDirectory(directory);
		return directory;
	}

	public static String getLocalDirectoryPathWithOutSDCard(Context context, String directoryName)
	{
		File files = context.getFilesDir();
		String directory = files.getAbsolutePath() + "/" + directoryName + "/";
		prepareDirectory(directory);
		return directory;
	}

	/**
	 * S4上，有user时，要手动创建目录才行
	 */
	private static boolean prepareDirectory(String path)
	{
		File file = new File(path);
		if (file.exists() && file.isDirectory())
		{
			return true;
		}
		else
		{
			boolean result = file.mkdirs();
			return result;
		}
	}

	/**
	 * db在内存中时，getDBFilePath()和getDatabaseName()都只得到"chat_service.db"，这个文件找不到，
	 * 需要加上包内的路径 SQLiteDatabaseConfiguration.MEMORY_DB_PATH（":memory:"）不行
	 */
	public boolean rmDirectory()
	{
		String dataBaseName = getDBFileAbsolutePath(IMCore.hostActivity);
		if (StringUtils.isNotEmpty(dataBaseName))
		{
			File file = new File(dataBaseName);
			if (file.exists())
			{
				return file.delete();
			}
			else
			{
				return false;
			}
		}
		return false;
	}
}
