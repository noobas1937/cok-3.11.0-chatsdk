package com.elex.chatservice.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;

public class PermissionManager
{
	private static PermissionManager	instance;

	public PermissionManager()
	{
		
	}

	public static PermissionManager getInstance()
	{
		if (instance == null)
		{
			instance = new PermissionManager();
		}
		return instance;
	}
	
	private static final int	REQUEST_CODE_EXTERNAL_STORAGE		= 1;
	private static final int	REQUEST_CODE_XM_RECORD				= 2;
	private static final int	REQUEST_CODE_XM_VIDEO				= 3;
	private static final int	REQUEST_CODE_ALLIANCE_SHARE			= 4;
	private static final int	REQUEST_CODE_NEARBY					= 5;
	private static final int	REQUEST_CODE_REALTIME_VOICE		= 6;

	private static String[]		PERMISSIONS_EXTERNAL_STORAGE		= { Manifest.permission.WRITE_EXTERNAL_STORAGE };
	private static String[]		PERMISSIONS_XM_RECORD				= {
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE				};
	private static String[]		PERMISSIONS_XM_VIDEO				= {
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE				};
	private static String[]		PERMISSIONS_ALLIANCE_SHARE			= {
			Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE				};

	private static String[]		PERMISSIONS_NEARBY					= {
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION				};
	
	private static String[]		PERMISSIONS_REALTIME_VOICE		= {
			Manifest.permission.RECORD_AUDIO				};

	/**
	 * 用于SharePreference，标识是否请求过存储权限，用于将第一次请求和不再询问区分开，以免不再询问时继续请求，再次弹提示框。
	 * 卸载程序后会重置
	 */
	private static final String	REQUESTED_SP_KEY_EXTERNAL_STORAGE	= "PERMISSIONS_STORAGE_REQUESTED";
	private static final String	REQUESTED_SP_KEY_XM_RECORD			= "PERMISSIONS_XM_RECORD_REQUESTED";
	private static final String	REQUESTED_SP_KEY_XM_VIDEO			= "PERMISSIONS_XM_VIDEO_REQUESTED";
	private static final String	REQUESTED_SP_KEY_ALLIANCE_SHARE		= "PERMISSIONS_ALLIANCE_SHARE_REQUESTED";
	private static final String	REQUESTED_SP_KEY_NEARBY				= "PERMISSIONS_NEARBY_REQUESTED";
	private static final String	REQUESTED_SP_KEY_REALTIME_VOICE		= "PERMISSIONS_REALTIME_VOICE_REQUESTED";
	
	public abstract class PermissionGroup
	{
		protected int		requestCode;
		protected String[]	permissions;
		protected String	requestedSharePreferenceKey;
		protected String	langInfo;
		protected String	langExplain;
		protected String	langManual;
		protected Activity	dialogParent;
		
		protected boolean shouldShowRequestPermissionRationale()
		{
			for (String permission : permissions)
			{
				if (ActivityCompat.shouldShowRequestPermissionRationale(ChatServiceController.hostActivity, permission))
				{
					return true;
				}
			}
			return false;
		}

		protected void showInfoDialog()
		{
			if(langInfo.equals(LanguageKeys.PERMISSION_INFO_NEARBY))
			{
				PermissionManager.getInstance().onInfoDialogConfirm(langInfo);
			}
			else
			{
				String notify = LanguageManager.getLangByKey(langInfo);
				MenuController.showPermissionDialog(dialogParent, notify, langInfo, true, null);
			}
			
		}

		protected void showExplainDialog()
		{
			String notify = LanguageManager.getLangByKey(langExplain);
			OnClickListener okCancellickListener = new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onRequestOver();
				}
			};
			MenuController.showPermissionDialog(dialogParent, notify, langExplain, true,
					okCancellickListener);
		}

		protected void showManualDialog()
		{
			String notify = LanguageManager.getLangByKey(langManual);
			MenuController.showPermissionDialog(dialogParent, notify, langManual, false, null);
		}
		
		protected void actualGetPermissions()
		{
			SharePreferenceUtil.setPreferenceBoolean(ChatServiceController.hostActivity.getApplicationContext(),
					requestedSharePreferenceKey, true);
			// 这个activity必须实现onRequestPermissionsResult，而且必须是当前显示的activity，否则不会回调
			ActivityCompat.requestPermissions(dialogParent, permissions, requestCode);
		}
		
		protected void onRequestOver()
		{
			permissionGroup = null;
		}
		
		protected void onRequestSucess()
		{
			permissionGroup = null;
		}
	}
	
	public class DatabasePermission extends PermissionGroup
	{
		public DatabasePermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_EXTERNAL_STORAGE;
			permissions					= PERMISSIONS_EXTERNAL_STORAGE;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_EXTERNAL_STORAGE;
			langInfo					= LanguageKeys.PERMISSION_INFO_WRITE_SD_CARD;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_WRITE_SD_CARD;
			langManual					= LanguageKeys.PERMISSION_MANUAL_WRITE_SD_CARD;
			dialogParent				= parent;
		}
		
		protected void onRequestOver()
		{
			super.onRequestOver();
			DBManager.getInstance().onRequestPermissionsResult();
		}
		
		protected void onRequestSucess()
		{
			super.onRequestOver();
			DBManager.getInstance().onRequestPermissionsResult();
		}
	}
	
	public class XMRecordPermission extends PermissionGroup
	{
		public XMRecordPermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_XM_RECORD;
			permissions					= PERMISSIONS_XM_RECORD;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_XM_RECORD;
			langInfo					= LanguageKeys.PERMISSION_INFO_RECORD_VOICE;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_RECORD_VOICE;
			langManual					= LanguageKeys.PERMISSION_MANUAL_RECORD_VOICE;
			dialogParent				= parent;
		}
	}
	
	public class RealtimeVoicePermission extends PermissionGroup
	{
		public RealtimeVoicePermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_REALTIME_VOICE;
			permissions					= PERMISSIONS_REALTIME_VOICE;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_REALTIME_VOICE;
			langInfo					= LanguageKeys.PERMISSION_INFO_RECORD_VOICE;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_RECORD_VOICE;
			langManual					= LanguageKeys.PERMISSION_MANUAL_RECORD_VOICE;
			dialogParent				= parent;
		}
	}

	/**
	 * 已不再使用，所以没有多语言
	 */
	public class XMVideoPermission extends PermissionGroup
	{
		public XMVideoPermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_XM_VIDEO;
			permissions					= PERMISSIONS_XM_VIDEO;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_XM_VIDEO;
			langInfo					= LanguageKeys.PERMISSION_INFO_RECORD_VOICE;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_RECORD_VOICE;
			langManual					= LanguageKeys.PERMISSION_MANUAL_RECORD_VOICE;
			dialogParent				= parent;
		}
	}
	
	public class AllianceSharePermission extends PermissionGroup
	{
		public AllianceSharePermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_ALLIANCE_SHARE;
			permissions					= PERMISSIONS_ALLIANCE_SHARE;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_ALLIANCE_SHARE;
			langInfo					= LanguageKeys.PERMISSION_INFO_ALLIANCE_SHARE;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_ALLIANCE_SHARE;
			langManual					= LanguageKeys.PERMISSION_MANUAL_ALLIANCE_SHARE;
			dialogParent				= parent;
		}
		
		@Override
		public void onRequestSucess()
		{
			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			String fileName = UserManager.getInstance().getCurrentUserId() + System.currentTimeMillis() + ".jpeg";
			String targetFileName = AllianceShareManager.getInstance().getLocalAllianceShareCaptureImagePath(fileName);
			File vFile = new File(targetFileName);
			AllianceShareActivity.currentPhotoPath = targetFileName;

			System.out.println("takePhoto path:" + AllianceShareActivity.currentPhotoPath);
			Uri cameraUri = Uri.fromFile(vFile);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
			dialogParent.startActivityForResult(openCameraIntent, AllianceShareManager.TAKE_PICTURE);
		}
	}

	public class NearbyPermission extends PermissionGroup
	{
		public NearbyPermission(Activity parent)
		{
			requestCode					= REQUEST_CODE_NEARBY;
			permissions					= PERMISSIONS_NEARBY;
			requestedSharePreferenceKey	= REQUESTED_SP_KEY_NEARBY;
			langInfo					= LanguageKeys.PERMISSION_INFO_NEARBY;
			langExplain					= LanguageKeys.PERMISSION_EXPLAIN_NEARBY;
			langManual					= LanguageKeys.PERMISSION_MANUAL_NEARBY;
			dialogParent				= parent;
		}

		protected void onRequestSucess()
		{
			super.onRequestOver();
			NearByManager.getInstance().onPermissionGot();
		}
	}
	
	private PermissionGroup permissionGroup;
	
	public void requestPermission(PermissionGroup permissionObject, boolean showDialogWhenNeverAsk)
	{
		if(permissionGroup != null)
		{
			LogUtil.trackMessage("permissionGroup is not null in requestPermission()");
//			return;
		}
		permissionGroup = permissionObject;
		
		if(permissionGroup.dialogParent == null)
		{
			LogUtil.trackMessage("dialogParent is null in requestPermission()");
			return;
		}
		
		if (permissionGroup.shouldShowRequestPermissionRationale())
		{
			// 拒绝过权限，弹提示窗口，再请求权限
			permissionGroup.showInfoDialog();
		}
		else
		{
			if (!SharePreferenceUtil.getSharePreferenceBoolean(
					ChatServiceController.hostActivity.getApplicationContext(),
					permissionGroup.requestedSharePreferenceKey, false))
			{
				// 尚未请求过权限
				permissionGroup.showInfoDialog();
			}
			else
			{
				// 不再询问
				
				if(showDialogWhenNeverAsk)
					permissionGroup.showManualDialog();

				// 要把初始化继续进行下去	
				permissionGroup.onRequestOver();
			}
		}
	}

	public void onInfoDialogConfirm(String permissionKey)
	{
		if(StringUtils.isEmpty(permissionKey) || permissionGroup == null)
			return;
		if (permissionKey.equals(permissionGroup.langInfo) || permissionKey.equals(permissionGroup.langExplain))
		{
			permissionGroup.actualGetPermissions();
		}
	}

	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		if(permissionGroup == null)
		{
			LogUtil.trackMessage("permissionGroup is null in onRequestPermissionsResult()");
			return;
		}
		
		if(requestCode == permissionGroup.requestCode)
		{
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				// If request is cancelled, the result arrays are empty.
				permissionGroup.onRequestSucess();
			}
			else
			{
				if (!permissionGroup.shouldShowRequestPermissionRationale())
				{
					// 不再询问
					permissionGroup.showManualDialog();
					
					permissionGroup.onRequestOver();
				}
				else
				{
					// 拒绝
					permissionGroup.showExplainDialog();
				}
			}
		}
	}
	
	public static boolean isPermissionsAvaiable(String[] permissions)
	{
		if (ChatServiceController.hostActivity == null)
			return true;
		
		for (String permission : permissions)
		{
			if (ActivityCompat.checkSelfPermission(ChatServiceController.hostActivity, permission) != PackageManager.PERMISSION_GRANTED)
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isExternalStoragePermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_EXTERNAL_STORAGE);
	}

	public void getDBStoragePermission()
	{
		requestPermission(new DatabasePermission(ChatServiceController.hostActivity), false);
	}

	public boolean checkXMRecordPermission()
	{
		if (!isXMRecordPermissionsAvaiable())
		{
			requestPermission(new XMRecordPermission(ChatServiceController.getCurrentActivity()), true);

			return false;
		}

		return true;
	}

	private static boolean isXMRecordPermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_XM_RECORD);
	}
	
	public boolean checkRealtimeVoicePermission()
	{
		if (!isRealtimeVoicePermissionsAvaiable())
		{
			if(ChatServiceController.getCurrentActivity()!=null)
				requestPermission(new RealtimeVoicePermission(ChatServiceController.getCurrentActivity()), true);
			else
				requestPermission(new RealtimeVoicePermission(ChatServiceController.hostActivity), true);
			return false;
		}

		return true;
	}

	private static boolean isRealtimeVoicePermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_REALTIME_VOICE);
	}

	public boolean checkXMVideoPermission()
	{
		if (XiaoMiToolManager.getInstance().getCurrentRecordActivity() == null)
		{
			return false;
		}
		if (!isXMVideoPermissionsAvaiable())
		{
			requestPermission(new XMVideoPermission(XiaoMiToolManager.getInstance().getCurrentRecordActivity()), true);

			return false;
		}

		return true;
	}

	public static boolean isXMVideoPermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_XM_VIDEO);
	}

	public boolean checkAllianceSharePermissions(Activity activity)
	{
		if (!isAllianceSharePermissionsAvaiable())
		{
			requestPermission(new AllianceSharePermission(activity), true);

			return false;
		}
		new AllianceSharePermission(activity).onRequestSucess();

		return true;
	}
	
	private static boolean isAllianceSharePermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_ALLIANCE_SHARE);
	}
	
	public boolean checkLocationPermissions()
	{
		if (!isLocationPermissionsAvaiable())
		{
			requestPermission(new NearbyPermission(ChatServiceController.getCurrentActivity()), true);
			
			return false;
		}
		return true;
	}
	
	public static boolean isLocationPermissionsAvaiable()
	{
		return isPermissionsAvaiable(PERMISSIONS_NEARBY);
	}
	
	/**
	 * normal权限，无需请求
	 */
	public static boolean isWifiStatePermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}

	/**
	 * normal权限，无需请求
	 */
	public static boolean isNetworkStatePermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}
}
