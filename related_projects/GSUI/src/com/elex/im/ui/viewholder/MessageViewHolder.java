package com.elex.im.ui.viewholder;

import java.util.Timer;
import java.util.TimerTask;

import android.view.View;

import com.elex.im.core.model.Msg;

public class MessageViewHolder
{
	public Timer sendTimer;
	public TimerTask sendTimerTask;
	public Msg currentMsgItem;
	
	public View findViewById(View convertView,int id)
	{
		return null;
	}
	
	public void stopSendTimer()
	{
		if (sendTimer != null)
		{
			if (sendTimerTask != null)
			{
				sendTimerTask.cancel();
			}
			sendTimer.cancel();
			sendTimer.purge();
		}
	}

	public void removeSendTimer()
	{
		stopSendTimer();
		sendTimer = null;
		sendTimerTask = null;
	}
}
