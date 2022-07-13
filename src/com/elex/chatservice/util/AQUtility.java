package com.elex.chatservice.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by wangyan on 2017/10/27.
 */

public class AQUtility {

    private static Handler handler;
    public static Handler getHandler(){
        if(handler == null){
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static void post(Runnable run){
        getHandler().post(run);
    }

    public static void postDelayed(Runnable run, long delay){
        getHandler().postDelayed(run, delay);
    }
    public static void removePost(Runnable run){
        getHandler().removeCallbacks(run);
    }

}
