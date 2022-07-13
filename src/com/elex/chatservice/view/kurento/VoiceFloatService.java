package com.elex.chatservice.view.kurento;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class VoiceFloatService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
