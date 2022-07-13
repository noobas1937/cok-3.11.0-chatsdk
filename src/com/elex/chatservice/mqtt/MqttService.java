package com.elex.chatservice.mqtt;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.elex.chatservice.net.StandaloneServerInfo;
import com.elex.chatservice.util.LogUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

public class MqttService extends Service implements MqttCallback, MqttConstants
{

	private static final String			TAG			= "MqttService";
	private boolean						mStarted	= false;		// Is the Client started?
	private String						mDeviceId;					// Device ID,Secure.ANDROID_ID
	private Handler						mConnHandler;				// Seperate Handler thread for networking
	private MqttDefaultFilePersistence	mDataStore;					// Defaults to FileStore
	private MemoryPersistence			mMemStore;					// On Fail reverts to MemoryStore
	private MqttConnectOptions			mOpts;						// Connection Options
	private MqttTopic					mKeepAliveTopic;			// Instance Variable for Keepalive topic
	private MqttClient					mClient;					// Mqtt Client
	private AlarmManager				mAlarmManager;				// Alarm manager to perform repeating tasks
	private ConnectivityManager			mConnectivityManager;		// To check for connectivity changes

	/**
	 * Start MQTT Client
	 * 
	 * @param Context
	 *            context to start the service with
	 * @return void
	 */
	public static void actionStart(Context ctx)
	{
		Intent i = new Intent(ctx, MqttService.class);
		i.setAction(ACTION_START);
		ctx.startService(i);
	}

	/**
	 * Stop MQTT Client
	 * 
	 * @param Context
	 *            context to start the service with
	 * @return void
	 */
	public static void actionStop(Context ctx)
	{
		Intent i = new Intent(ctx, MqttService.class);
		i.setAction(ACTION_STOP);
		ctx.startService(i);
	}

	/**
	 * Send a KeepAlive Message
	 * 
	 * @param Context
	 *            context to start the service with
	 * @return void
	 */
	public static void actionKeepalive(Context ctx)
	{
		Intent i = new Intent(ctx, MqttService.class);
		i.setAction(ACTION_KEEPALIVE);
		ctx.startService(i);
	}

	/**
	 * Initalizes the DeviceId and most instance variables Including the
	 * Connection Handler, Datastore, Alarm Manager and ConnectivityManager.
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();

		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		// mDeviceId = String.format(DEVICE_ID_FORMAT,
		// Secure.getString(getContentResolver(), Secure.ANDROID_ID));
		mDeviceId = MqttManager.getInstance().getSession();

		HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
		thread.start();

		mConnHandler = new Handler(thread.getLooper());
		mDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());
		mOpts = new MqttConnectOptions();
		mOpts.setCleanSession(MQTT_CLEAN_SESSION);
		// Do not set keep alive interval on mOpts we keep track of it with
		// alarm's

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	}

	/**
	 * Service onStartCommand Handles the action passed via the Intent
	 *
	 * @return START_REDELIVER_INTENT
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		// String action = intent.getAction();
		//
		// LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Received action of " + action);
		//
		// if (action == null)
		// {
		// LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Starting service with no action\n Probably from a crash");
		// }
		// else
		// {
		// if (action.equals(ACTION_START))
		// {
		// LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Received ACTION_START");
		// start();
		// }
		// else if (action.equals(ACTION_STOP))
		// {
		// stop();
		// }
		// else if (action.equals(ACTION_KEEPALIVE))
		// {
		// keepAlive();
		// }
		// else if (action.equals(ACTION_RECONNECT))
		// {
		// if (isNetworkAvailable())
		// {
		// reconnectIfNecessary();
		// }
		// }
		// }

		return START_REDELIVER_INTENT;
	}

	public synchronized void start()
	{
		if (mStarted)
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
					"Attempt to start while already started");
			return;
		}

		if (hasScheduledKeepAlives())
		{
			stopKeepAlives();
		}

		connect();

		registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	/**
	 * Attempts to stop the Mqtt client as well as halting all keep alive
	 * messages queued in the alarm manager
	 */
	public synchronized void stop()
	{
		if (!mStarted)
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
					"Attemtpign to stop connection that isn't running");
			return;
		}

		if (mClient != null)
		{
			mConnHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						mClient.disconnect();
					}
					catch (MqttException ex)
					{
						ex.printStackTrace();
					}
					mClient = null;
					mStarted = false;

					stopKeepAlives();
				}
			});
		}

		unregisterReceiver(mConnectivityReceiver);
	}
	
	@Override
	public void onDestroy()
	{
		stop();
		super.onDestroy();
	}

	/**
	 * Connects to the broker with the appropriate datastore
	 */
	private synchronized void connect()
	{
		StandaloneServerInfo serverInfo = MqttManager.getInstance().getCurrentServer();
		if (serverInfo != null)
		{
			String mqttServerIP = serverInfo.address;
			String port = serverInfo.port;
			if (StringUtils.isEmpty(port))
				port = MQTT_PORT;
			String protocol = serverInfo.protocol;
			if (StringUtils.isEmpty(protocol))
				protocol = MQTT_PROTOCOL;
			String url = String.format(Locale.US, MQTT_URL_FORMAT, protocol, mqttServerIP, port);
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Connecting with URL: " + url);
			try
			{
				String clientId = MqttManager.getInstance().getSession();
				if (mDataStore != null)
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
							"Connecting with DataStore , clientId",clientId);
					
					mClient = new MqttClient(url, clientId, mDataStore);
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
							"Connecting with MemStore , clientId",clientId);
					mClient = new MqttClient(url, clientId, mMemStore);
				}
			}
			catch (MqttException e)
			{
				e.printStackTrace();
			}

			mConnHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String username = MqttManager.getInstance().getConnectUserName();
						String password = MqttManager.getInstance().getConnectPassword();
						LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "username", username, "password", password);
						mOpts.setUserName(username);
						mOpts.setPassword(password.toCharArray());
						mClient.connect(mOpts);

						mClient.setCallback(MqttService.this);

						mStarted = true; // Service is now connected

						List<String> topicList = MqttManager.getInstance().getSubList();
						String[] strArr = new String[topicList.size()];
						// int[] qosArr = new int[topicList.size()];
						// for (int i=0; i<qosArr.length; i++) {
						// qosArr[i] = 0;
						// }

						mClient.subscribe(topicList.toArray(strArr));

						LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
								"Successfully connected and subscribed starting keep alives");

						startKeepAlives();
					}
					catch (MqttException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void subscribe(final String subTopic)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,"subTopic",subTopic);
		if (mConnHandler == null)
			return;
		mConnHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (mClient != null)
					{
						if(StringUtils.isEmpty(subTopic))
						{
							List<String> topicList = MqttManager.getInstance().getSubList();
							String[] strArr = new String[topicList.size()];
							// int[] qosArr = new int[topicList.size()];
							// for (int i=0; i<qosArr.length; i++) {
							// qosArr[i] = 0;
							// }

							mClient.subscribe(topicList.toArray(strArr));
						}
						else
						{
							mClient.subscribe(subTopic);
						}
					}
				}
				catch (MqttException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	public void unsubscribe(final String subTopic)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,"subTopic",subTopic);
		if (mConnHandler == null)
			return;
		mConnHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (mClient != null)
					{
						mClient.unsubscribe(subTopic);
					}
				}
				catch (MqttException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Schedules keep alives via a PendingIntent in the Alarm Manager
	 */
	private void startKeepAlives()
	{
		Intent i = new Intent();
		i.setClass(this, MqttService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + MQTT_KEEP_ALIVE,
				MQTT_KEEP_ALIVE, pi);
	}

	/**
	 * Cancels the Pending Intent in the alarm manager
	 */
	private void stopKeepAlives()
	{
		Intent i = new Intent();
		i.setClass(this, MqttService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		mAlarmManager.cancel(pi);
	}

	/**
	 * Publishes a KeepALive to the topic in the broker
	 */
	private synchronized void keepAlive()
	{
		if (isConnected())
		{
			try
			{
				sendKeepAlive();
				return;
			}
			catch (MqttConnectivityException ex)
			{
				ex.printStackTrace();
				reconnectIfNecessary();
			}
			catch (MqttPersistenceException ex)
			{
				ex.printStackTrace();
				stop();
			}
			catch (MqttException ex)
			{
				ex.printStackTrace();
				stop();
			}
		}
	}

	/**
	 * Checkes the current connectivity and reconnects if it is required.
	 */
	private synchronized void reconnectIfNecessary()
	{
		if (mStarted && mClient == null)
		{
			connect();
		}
	}

	/**
	 * Query's the NetworkInfo via ConnectivityManager to return the current
	 * connected state
	 * 
	 * @return boolean true if we are connected false otherwise
	 */
	private boolean isNetworkAvailable()
	{
		NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

		return (info == null) ? false : info.isConnected();
	}

	/**
	 * Verifies the client State with our local connected state
	 * 
	 * @return true if its a match we are connected false if we aren't connected
	 */
	public boolean isConnected()
	{
		if (mStarted && mClient != null && !mClient.isConnected())
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG,
					"Mismatch between what we think is connected and what is connected");
		}

		if (mClient != null)
		{
			return (mStarted && mClient.isConnected()) ? true : false;
		}

		return false;
	}

	/**
	 * Receiver that listens for connectivity chanes via ConnectivityManager
	 */
	private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Connectivity Changed...");
		}
	};

	/**
	 * Sends a Keep Alive message to the specified topic
	 * 
	 * @see MQTT_KEEP_ALIVE_MESSAGE
	 * @see MQTT_KEEP_ALIVE_TOPIC_FORMAT
	 * @return MqttDeliveryToken specified token you can choose to wait for
	 *         completion
	 */
	private synchronized MqttDeliveryToken sendKeepAlive()
			throws MqttConnectivityException, MqttPersistenceException, MqttException
	{
		if (!isConnected())
			throw new MqttConnectivityException();

		if (mKeepAliveTopic == null)
		{
			String clientId = MqttManager.getInstance().getSession();
			mKeepAliveTopic = mClient
					.getTopic(String.format(Locale.US, MQTT_KEEP_ALIVE_TOPIC_FORAMT, clientId));
		}

		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "Sending Keepalive to " + MQTT_BROKER);

		MqttMessage message = new MqttMessage(MQTT_KEEP_ALIVE_MESSAGE);
		message.setQos(MQTT_KEEP_ALIVE_QOS);

		return mKeepAliveTopic.publish(message);
	}

	/**
	 * Query's the AlarmManager to check if there is a keep alive currently
	 * scheduled
	 * 
	 * @return true if there is currently one scheduled false otherwise
	 */
	private synchronized boolean hasScheduledKeepAlives()
	{
		Intent i = new Intent();
		i.setClass(this, MqttService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

		return (pi != null) ? true : false;
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return mqttBinder;
	}

	private MqttBinder mqttBinder = new MqttBinder();

	public class MqttBinder extends Binder
	{
		public MqttService getService()
		{
			return MqttService.this;
		}
	}

	/**
	 * Connectivity Lost from broker
	 */
	@Override
	public void connectionLost(Throwable arg0)
	{
		stopKeepAlives();

		mClient = null;

		if (isNetworkAvailable())
		{
			reconnectIfNecessary();
		}
	}

	/**
	 * MqttConnectivityException Exception class
	 */
	private class MqttConnectivityException extends Exception
	{
		private static final long serialVersionUID = -7385866796799469420L;
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, MQTT_PUSH, "topic", topic);
		if (StringUtils.isNotEmpty(topic) && message != null)
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, MQTT_PUSH, "Message:", new String(message.getPayload())
					+ "  QoS:\t"
					+ message.getQos());
			String messageStr = new String(message.getPayload());
			if (StringUtils.isNotEmpty(messageStr))
				MqttManager.getInstance().onRecieveMessage(messageStr);

		}
	}
}
