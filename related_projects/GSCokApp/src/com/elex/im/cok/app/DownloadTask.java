package com.elex.im.cok.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

// usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
public class DownloadTask extends AsyncTask<String, Integer, String>
{
	private GSCokAppActivity		context;
	private PowerManager.WakeLock	mWakeLock;
    // declare the dialog as a member field of your activity
//    ProgressDialog mProgressDialog;
	public String filePath;
    
	public DownloadTask(GSCokAppActivity context, String filePath)
	{
		this.context = context;
		this.filePath = filePath;
		
	    // instantiate it within the onCreate method
//	    mProgressDialog = new ProgressDialog(context);
//	    mProgressDialog.setMessage("A message");
//	    mProgressDialog.setIndeterminate(true);
//	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//	    mProgressDialog.setCancelable(true);

//	    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//	        @Override
//	        public void onCancel(DialogInterface dialog) {
//	            downloadTask.cancel(true);
//	        }
//	    });
	}

	@Override
	protected String doInBackground(String... sUrl)
	{
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(sUrl[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
			}
			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();
			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(filePath);
			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1)
			{
				// allow canceling with back button
				if (isCancelled())
				{
					input.close();
					return null;
				}
				total += count;
				// publishing the progress....
				if (fileLength > 0) // only if total length is known
					publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}
		}
		catch (Exception e)
		{
			return e.toString();
		}
		finally
		{
			try
			{
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			}
			catch (IOException ignored)
			{
			}
			if (connection != null)
				connection.disconnect();
		}
		return null;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		mWakeLock.acquire();
		context.setProgressText("Downloading language file");
//		mProgressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		super.onProgressUpdate(progress);
		context.setProgressText("Downloading language file " + progress + "%");
		// if we get here, length is known, now set indeterminate to false
//		mProgressDialog.setIndeterminate(false);
//		mProgressDialog.setMax(100);
//		mProgressDialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(String result)
	{
		mWakeLock.release();
//		mProgressDialog.dismiss();
		if (result != null){
			Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
			context.setProgressText("Download error: " + result);
		}else{
//			Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
			context.setProgressText("Download complete");

			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					LanguageConfiger.onDownLoaedComplete();
				}
			};
			Thread thread = new Thread(run);
			thread.start();
		}
	}
}