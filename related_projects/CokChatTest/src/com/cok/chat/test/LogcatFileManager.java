package com.cok.chat.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.elex.chatservice.net.IWebSocketStatusListener;
import com.elex.chatservice.util.LogUtil;

public class LogcatFileManager
{
	private static LogcatFileManager	INSTANCE			= null;
	private static String				PATH_LOGCAT;
	private LogDumper					mLogDumper			= null;
	private int							mPId;
	private SimpleDateFormat			filenameDateFormat	= new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	private SimpleDateFormat			logDateFormat		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public IWebSocketStatusListener		statusListener;
	private static String 				FOLDER				= "CokChatTest";
	
	public static LogcatFileManager getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new LogcatFileManager();
		}
		return INSTANCE;
	}

	private LogcatFileManager()
	{
		mPId = android.os.Process.myPid();
	}

	public void startLogcatManager(Context context)
	{
		String folderPath = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER;
		}
		else
		{
			folderPath = context.getFilesDir().getAbsolutePath() + File.separator + FOLDER;
		}
		LogcatFileManager.getInstance().start(folderPath);
	}

	public void stopLogcatManager()
	{
		LogcatFileManager.getInstance().stop();
	}

	private void setFolderPath(String folderPath)
	{
		File folder = new File(folderPath);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		if (!folder.isDirectory())
		{
			throw new IllegalArgumentException("The logcat folder path is not a directory: " + folderPath);
		}

		PATH_LOGCAT = folderPath.endsWith("/") ? folderPath : folderPath + "/";
//		LogUtils.d(PATH_LOGCAT);
	}

	public void start(String saveDirectoy)
	{
		setFolderPath(saveDirectoy);
		if (mLogDumper == null)
		{
			mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
		}
		mLogDumper.start();
	}

	public void stop()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		if (mLogDumper != null)
		{
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread
	{
		private Process				logcatProc;
		private BufferedReader		mReader		= null;
		private boolean				mRunning	= true;
		String						cmds		= null;
		private String				mPID;
		private FileOutputStream	out			= null;

		public LogDumper(String pid, String dir)
		{
			mPID = pid;
			try
			{
				String fileName = "CokChatTest " + filenameDateFormat.format(new Date()) + ".log";
				out = new FileOutputStream(new File(dir, fileName), true);

				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					statusListener.onConsoleOutput("Log is saved at /SD Card/CokChatTest/" + fileName);
				}
				else
				{
					statusListener.onConsoleOutput("Log is saved at " + dir + fileName);
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}

			/**
			 * * * log level：*:v , *:d , *:w , *:e , *:f , *:s * * Show the
			 * current mPID process level of E and W log. * *
			 */
			// cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			cmds = "logcat *:v | grep \"(" + mPID + ")\"";
		}

		public void stopLogs()
		{
			mRunning = false;
		}

		@Override
		public void run()
		{
			try
			{
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null)
				{
					if (!mRunning)
					{
						break;
					}
					if (line.length() == 0)
					{
						continue;
					}
					if (out != null && line.contains(mPID))
					{
						out.write((logDateFormat.format(new Date()) + "  " + line + "\n").getBytes());
					}
				}
			}
			catch (IOException e)
			{
				LogUtil.printException(e);
			}
			finally
			{
				if (logcatProc != null)
				{
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null)
				{
					try
					{
						mReader.close();
						mReader = null;
					}
					catch (IOException e)
					{
						LogUtil.printException(e);
					}
				}
				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (IOException e)
					{
						LogUtil.printException(e);
					}
					out = null;
				}
			}
		}

	}
}
