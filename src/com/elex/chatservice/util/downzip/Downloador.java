package com.elex.chatservice.util.downzip;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.emoj.EmojGroupEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Downloador
{
	public static final String		TAG						= "Downloador";
	private static final int		THREAD_POOL_SIZE		= 9;
	private static final int		THREAD_NUM				= 3;
	private static final int		GET_LENGTH_SUCCESS		= 1;
	public static final Executor	THREAD_POOL_EXECUTOR	= Executors.newFixedThreadPool(THREAD_POOL_SIZE);

	private List<DownloadTask>		tasks;
	private InnerHandler			handler					= new InnerHandler();

	private DownLoadEntity			downLoadEntity;
	private long					downloadLength;
	private long					fileLength;
	private Context					context;
	private String					downloadPath;

	public Downloador(Context context, DownLoadEntity appContent)
	{
		this.context = context;
		this.downLoadEntity = appContent;
		this.downloadPath = DBHelper.getLocalDirectoryPath(context, "common_pic");
	}

	public void unzipFile()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (downLoadEntity == null)
			return;
		String groupId = downLoadEntity.getGroupId();
		File zipDir = DBHelper.getLocalDirectory(ChatServiceController.hostActivity, "common_pic");
		String groupName = EmojSubscribeManager.GROUP_PREFIX + groupId;
		String zipName = zipDir.getPath() + "/" + groupName + ".zip";
		File zipFile = new File(zipName);

		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"zipName",zipName);
			unzipFile(zipFile, zipDir);
			Map<String,EmojGroupEntity> subaviliableMap = EmojSubscribeManager.getInstance().getSubableEmojMap();
			if(subaviliableMap !=null && subaviliableMap.containsKey(groupId))
			{
				EmojGroupEntity entity = subaviliableMap.get(groupId);
				if(entity!=null)
				{
					entity.setDownLoadEntity(true);
					if(ChatServiceController.getChatFragment()!=null)
						ChatServiceController.getChatFragment().onEmojPanelChanged();
					else
						ChatFragmentNew.emojPanelChanged = true;
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		finally
		{
			zipFile.delete();
		}
	}

	private void unzipFile(File zipFile, File destination)
	{
		DecompressZip decomp = new DecompressZip(zipFile.getPath(), destination.getPath() + File.separator);
		decomp.unzip();
	}

	public void download()
	{
		if (TextUtils.isEmpty(downloadPath))
		{
			Toast.makeText(context, "SD card no found", Toast.LENGTH_SHORT).show();
			return;
		}
		if (downLoadEntity == null)
		{
			throw new IllegalArgumentException("download content can not be null");
		}
		new Thread()
		{
			@Override
			public void run()
			{
				// 获取文件大小
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(downLoadEntity.getUrl());
				HttpResponse response = null;
				try
				{
					response = client.execute(request);
					fileLength = response.getEntity().getContentLength();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (request != null)
					{
						request.abort();
					}
				}
				Message.obtain(handler, GET_LENGTH_SUCCESS).sendToTarget();
			}
		}.start();
	}

	private void beginDownload()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "beginDownload" + downLoadEntity.getUrl());
		downLoadEntity.setStatus(DownLoadEntity.Status.DOWNLOADING);
		long blockLength = fileLength / THREAD_NUM;
		for (int i = 0; i < THREAD_NUM; i++)
		{
			long beginPosition = i * blockLength;// 每条线程下载的开始位置
			long endPosition = (i + 1) * blockLength;// 每条线程下载的结束位置
			if (i == (THREAD_NUM - 1))
			{
				endPosition = fileLength;// 如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
			}
			DownloadTask task = new DownloadTask(i, beginPosition, endPosition, this, context);
			task.executeOnExecutor(THREAD_POOL_EXECUTOR, downLoadEntity.getUrl());
			if (tasks == null)
			{
				tasks = new ArrayList<DownloadTask>();
			}
			tasks.add(task);
		}
	}

	public void pause()
	{
		for (DownloadTask task : tasks)
		{
			if (task != null && (task.getStatus() == AsyncTask.Status.RUNNING || !task.isCancelled()))
			{
				task.cancel(true);
			}
		}
		tasks.clear();
		downLoadEntity.setStatus(DownLoadEntity.Status.PAUSED);
	}

	protected synchronized void resetDownloadLength()
	{
		this.downloadLength = 0;
	}

	protected synchronized void updateDownloadLength(long size)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "downloadLength" + downloadLength);
		this.downloadLength += size;
		// 通知更新界面
		int percent = (int) ((float) downloadLength * 100 / (float) fileLength);
		downLoadEntity.setDownloadPercent(percent);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "percent" + percent);
		
		if (percent == 100 || downloadLength == fileLength)
		{
			downLoadEntity.setDownloadPercent(100); // 上面计算有时候会有点误差，算到percent=99
			downLoadEntity.setStatus(DownLoadEntity.Status.FINISHED);
		}
		if(downLoadEntity.getDownloadPercent() > 0 && downLoadEntity.getDownloadPercent() == 100)
			unzipFile();
		if(downLoadEntity.getDownloadPercent() > 0 && downLoadEntity.getDownloadPercent() % 25 == 0)
		{
			if(ChatServiceController.getEmojSubscribActivity()!=null)
			{
				ChatServiceController.getEmojSubscribActivity().runOnUiThread(new Runnable()
				{
					
					@Override
					public void run()
					{
						if(ChatServiceController.getEmojSubscribActivity()!=null)
							ChatServiceController.getEmojSubscribActivity().notifyDataSetChanged();
					}
				});
			}
		}
	}

	protected String getDownloadPath()
	{
		return downloadPath;
	}

	private class InnerHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case GET_LENGTH_SUCCESS:
					beginDownload();
					break;
			}
			super.handleMessage(msg);
		}
	}
}
