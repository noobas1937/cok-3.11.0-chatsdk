package com.elex.im.core.util;

import java.io.File;

import android.content.Context;

import com.elex.im.core.IMCore;
import com.elex.im.core.model.db.DBHelper;
import com.elex.im.core.util.HeadPicUtil.MD5;

public class FileUtil
{
	public static boolean isUpdateImageExist(String imageName)
	{
		return isFileExist(getCommonPicLocalPath(imageName));
	}

	public static String getCommonPicLocalPath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(IMCore.hostActivity, "common_pic");
		path += "cache_" + MD5.getMD5Str(fileName);
		return path;
	}
	
	public static String getChatroomHeadPicPath(Context context)
	{
		if (context == null)
			return null;
		
		return DBHelper.getHeadDirectoryPath(context) + "chatroom/";
	}
	
	public static boolean isFileExist(String path)
	{
		if (StringUtils.isEmpty(path))
			return false;
		try
		{
			File file = new File(path);
			if (file.exists())
			{
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isPicExist(String path)
	{
		String fileName = path;
		if (StringUtils.isEmpty(fileName))
			return false;

		try
		{
			File file = new File(fileName);
			if (file.exists())
			{
				return true;
			}
			if (!path.endsWith(".png") && !path.endsWith(".jpg"))
			{
				fileName = path + ".png";
				file = new File(fileName);
				if (file.exists())
				{
					return true;
				}
				else
				{
					fileName = path + ".jpg";
					file = new File(fileName);
					if (file.exists())
					{
						return true;
					}
				}
			}
			else
			{
				file = new File(path);
				if (file.exists())
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
