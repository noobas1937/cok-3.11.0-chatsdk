package com.elex.im.core.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.elex.im.core.IMCore;

public class AppInfoUtil
{
	public static String getPackageName()
	{
		return IMCore.hostActivity.getPackageName();
	}
	
	public static String getApplicationName()
	{
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try
		{
			packageManager = IMCore.hostActivity.getApplicationContext().getPackageManager();
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
			return IMCore.hostActivity.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
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
			return IMCore.hostActivity.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
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
}
