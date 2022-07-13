package com.elex.chatservice.view.danmu;

import com.elex.chatservice.controller.DanmuManager;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DanmuService extends Service {  
	  
    Context context;  
    
    private DanmuBinder danmuBinder = new DanmuBinder();
    
    public class DanmuBinder extends Binder
    {
    	public DanmuService getService()
    	{
    		return DanmuService.this;
    	}
    }
    
    public void showDanmu(Activity activity)
    {
    	DanmuManager.getInstance().createDanmuWindow(activity);
    }
    
  
    @Override  
    public IBinder onBind(Intent intent) {  
    	System.out.println("onBind");
        return danmuBinder;  
    }  
    
    @Override
    public boolean onUnbind(Intent intent)
    {
    	System.out.println("onUnbind");
    	return super.onUnbind(intent);
    }
  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        this.context = this;  
        System.out.println("onCreate");
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
  
        flags = START_STICKY;  
  
        return super.onStartCommand(intent, flags, startId);  
    }  
  
    @Override  
    public void onStart(Intent intent, int startId) {  
        super.onStart(intent, startId);  
    }  
  
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
    }  
  
}
