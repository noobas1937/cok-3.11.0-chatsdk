package com.elex.chatservice.view.lbs;

import java.util.List;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.NearByUserInfo;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class NearByActivity extends MyActionBarActivity
{
	public NearByAdapter	adapter				= null;
	private ListView		near_by_listView;
	private TextView		noBodyTip;
	private TextView		permission_request_tip;
	private LinearLayout	permission_layout;
	private LinearLayout	open_location_layout;
	private TextView		open_location_tip;
	private Button			open_locationBtn;
	private Button			confirmBtn;
	private RelativeLayout	near_by_list_layout;
	private Button			leave_nearby_btn;
	private TextView		nearby_today_like_num;
	private Button			noBtn;
	private static int		lastScrollX			= -1;
	private static int		lastScrollY			= -1;
	private static boolean	dataChanged			= false;
	private boolean			adjustSizeCompleted	= false;
	private LocationManager	locationManager		= null;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__nearby_list_activity, fragment_holder, true);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY));
		showRightBtn(nearby_btn_layout);

		showNearbyMsgBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				NearByManager.getInstance().setEnter_list_type(1);
				LogUtil.trackNearby("nearby_msg_btn_click");
				ServiceInterface.showSecondChannelList(DBDefinition.CHANNEL_TYPE_USER, MailManager.CHANNELID_NEAR_BY, true);
			}
		});

		near_by_listView = (ListView) findViewById(R.id.near_by_listView);
		noBodyTip = (TextView) findViewById(R.id.noBodyTip);
		nearby_today_like_num = (TextView) findViewById(R.id.nearby_today_like_num);
		if (MailManager.nearbyLikeEnable)
		{
			nearby_today_like_num.setVisibility(View.VISIBLE);
			nearby_today_like_num.setText(LanguageManager.getLangByKey(LanguageKeys.NEARBY_LIKE_NUM_TEXT, "" + NearByManager.getInstance().getTodayLikeNum()));
		}
		else
		{
			nearby_today_like_num.setVisibility(View.GONE);
		}
		noBodyTip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SEARCH_NEARBY_USER));
		permission_request_tip = (TextView) findViewById(R.id.permission_request_tip);
		permission_request_tip.setText(LanguageManager.getLangByKey(LanguageKeys.PERMISSION_EXPLAIN_NEARBY));
		permission_layout = (LinearLayout) findViewById(R.id.permission_layout);
		open_location_layout = (LinearLayout) findViewById(R.id.open_location_layout);
		open_locationBtn = (Button) findViewById(R.id.open_locationBtn);
		open_location_tip = (TextView) findViewById(R.id.open_location_tip);
		open_location_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_OPEN_LOCATION));
		open_locationBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_OPEN_LOCATION));
		confirmBtn = (Button) findViewById(R.id.confirmBtn);
		noBtn = (Button) findViewById(R.id.noBtn);
		confirmBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_YES));
		noBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_NO));

		near_by_list_layout = (RelativeLayout) findViewById(R.id.near_by_list_layout);
		leave_nearby_btn = (Button) findViewById(R.id.leave_nearby_btn);
		leave_nearby_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CLEAR_LOCATION));

		near_by_listView.setDivider(null);
		adapter = new NearByAdapter(this);
		near_by_listView.setAdapter(adapter);

		Drawable d = ImageUtil.getRepeatingBG(this, R.drawable.near_by_bg);
		if (d != null)
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				fragmentLayout.setBackgroundDrawable(d);
			else
				fragmentLayout.setBackground(d);
		}

		restorePosition();
		refreshView(true);

		open_locationBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (NearByManager.getInstance().isClearLocation())
					uploadLocation();
				else
				{
					LogUtil.trackNearby("nearby_settingLocation");
					NearByManager.getInstance().gotoLocServiceSettings(NearByActivity.this);
				}
			}
		});

		leave_nearby_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				MenuController.showClearLocationConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CLEAR_LOCATION));
			}
		});

		confirmBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				LogUtil.trackNearby("nearby_permission_agree");
				if (PermissionManager.getInstance().checkLocationPermissions())
					NearByManager.getInstance().onPermissionGot();
			}
		});

		noBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				LogUtil.trackNearby("nearby_permission_reject");
			}
		});

		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
	}

	private void refreshView(boolean isCreate)
	{
		if (!PermissionManager.isLocationPermissionsAvaiable() && !NearByManager.getInstance().isPermissionGot())
		{
			open_location_layout.setVisibility(View.GONE);
			noBodyTip.setVisibility(View.GONE);
			permission_layout.setVisibility(View.VISIBLE);
			near_by_list_layout.setVisibility(View.GONE);
		}
		else
		{
			permission_layout.setVisibility(View.GONE);

			if (!(isNetworkOpen() || isGpsOpen()))
			{
				open_location_layout.setVisibility(View.VISIBLE);
				noBodyTip.setVisibility(View.GONE);
				near_by_list_layout.setVisibility(View.GONE);
				LogUtil.trackNearby("nearby_system_location_closed");
			}
			else if (!NearByManager.getInstance().isHasUploadLocation() || !NearByManager.getInstance().isHasSearchNearByUser())
			{
				if (!NearByManager.getInstance().isHasUploadLocation())
					uploadLocation();
				if (!NearByManager.getInstance().isHasSearchNearByUser())
				{
					if (!SwitchUtils.mqttEnable)
						WebSocketManager.getInstance().getNearByUserList();
					else
						MqttManager.getInstance().getNearByUserList();
					open_location_layout.setVisibility(View.GONE);
					noBodyTip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SEARCH_NEARBY_USER));
					noBodyTip.setVisibility(View.VISIBLE);
					near_by_list_layout.setVisibility(View.GONE);
				}
				LogUtil.trackNearby("nearby_system_location_opened");
			}
			else
			{
				open_location_layout.setVisibility(View.GONE);
				LogUtil.trackNearby("nearby_system_location_opened");
				List<NearByUserInfo> list = NearByManager.getInstance().getNearByUserArray();
				if (list == null || list.size() <= 0)
				{
					noBodyTip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NEARBY_NO_BODY));
					noBodyTip.setVisibility(View.VISIBLE);
					near_by_list_layout.setVisibility(View.GONE);
				}
				else
				{
					noBodyTip.setVisibility(View.GONE);
					near_by_list_layout.setVisibility(View.VISIBLE);
				}
				if (isCreate)
					uploadLocation();
			}
		}
	}

	public boolean isGpsOpen()
	{
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean isNetworkOpen()
	{
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public void uploadLocation()
	{
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		try
		{
			List<String> providers = locationManager.getProviders(true);
			if (providers != null)
			{
				String locationProvider = "";
				if (providers.contains(LocationManager.NETWORK_PROVIDER))
				{
					locationProvider = LocationManager.NETWORK_PROVIDER;
				}
				else if (providers.contains(LocationManager.GPS_PROVIDER))
				{
					locationProvider = LocationManager.GPS_PROVIDER;
				}
				else
				{
					if (providers.size() > 0)
						locationProvider = providers.get(0);
					else
					{
						Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				Location location = locationManager.getLastKnownLocation(locationProvider);
				if (location != null)
				{
					if (!SwitchUtils.mqttEnable)
						WebSocketManager.getInstance().uploadLocation(Double.toString(location.getLongitude()),
								Double.toString(location.getLatitude()));
					else
						MqttManager.getInstance().uploadLocation(Double.toString(location.getLongitude()),
								Double.toString(location.getLatitude()));
				}
				else
				{
					if (locationProvider.equals(LocationManager.NETWORK_PROVIDER))
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
					else if (locationProvider.equals(LocationManager.GPS_PROVIDER))
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
				}
			}
			else
				Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	private LocationListener locationListener = new LocationListener()
	{

		@Override
		public void onStatusChanged(String provider, int status, Bundle arg2)
		{

		}

		@Override
		public void onProviderEnabled(String provider)
		{

		}

		@Override
		public void onProviderDisabled(String provider)
		{

		}

		@Override
		public void onLocationChanged(Location location)
		{
			if (location != null)
			{
				if (SwitchUtils.mqttEnable)
					MqttManager.getInstance().uploadLocation(
							Double.toString(location.getLongitude()),
							Double.toString(location.getLatitude()));
				else
					WebSocketManager.getInstance().uploadLocation(
							Double.toString(location.getLongitude()),
							Double.toString(location.getLatitude()));
			}
		}
	};

	@Override
	public void onResume()
	{
		super.onResume();
		refreshView(false);
		refreshNearbyUnreadCount();
		if (adapter != null)
			adapter.refreshNearByData();
	}

	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			near_by_listView.setSelectionFromTop(lastX, lastY);
		}
		lastScrollX = lastScrollY = -1;
	}

	public void saveState()
	{
		if (getCurrentPos() != null)
		{
			lastScrollX = getCurrentPos().x;
			lastScrollY = getCurrentPos().y;
		}
	}

	protected Point getCurrentPos()
	{
		if (near_by_listView == null)
		{
			return null;
		}
		int x = near_by_listView.getFirstVisiblePosition();
		View v = near_by_listView.getChildAt(0);
		System.out.println("v.height");
		int y = (v == null) ? 0 : (v.getTop() - near_by_listView.getPaddingTop());

		return new Point(x, y);
	}

	public void notifyDataSetChanged()
	{
		if (ChatServiceController.getNearByListActivity() != null)
		{
			ChatServiceController.getNearByListActivity().runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					refreshView(false);
					refreshNearbyData();
				}
			});
		}
	}

	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (!adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (!adjustSizeCompleted)
		{
			System.out.println("adjustHeight");
			ScaleUtil.adjustTextSize(confirmBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(open_locationBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noBodyTip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(open_location_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(permission_request_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(nearby_today_like_num, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
	}

	@Override
	protected void onDestroy()
	{
		if (locationListener != null && locationManager != null)
			locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}

	public void refreshNearbyData()
	{
		if (MailManager.nearbyLikeEnable)
		{
			nearby_today_like_num.setVisibility(View.VISIBLE);
			nearby_today_like_num.setText(LanguageManager.getLangByKey(LanguageKeys.NEARBY_LIKE_NUM_TEXT, "" + NearByManager.getInstance().getTodayLikeNum()));
		}
		else
		{
			nearby_today_like_num.setVisibility(View.GONE);
		}
		if (adapter != null)
			adapter.refreshNearByData();
	}

}