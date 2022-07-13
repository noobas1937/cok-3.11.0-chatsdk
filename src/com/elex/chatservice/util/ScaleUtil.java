package com.elex.chatservice.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.TextView;

public class ScaleUtil
{
	private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static float screenDensity = 0;
    private static float scaleDensity = 0;
    private static int densityDpi = 0;
    private static float xdpi = 0;
    private static float ydpi = 0;
    private static boolean isPad = false;
    private static int statusHeight = 0;
    
    public static int getScreenDensity()
    {
    	return densityDpi;
    }
    

    public static void initialize(Context context){
        if (context == null || screenWidth > 0)
            return;
        
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;     // 屏幕宽度
        screenHeight = metrics.heightPixels;   // 屏幕高度
        screenDensity = metrics.density;      // 0.75 / 1.0 / 1.5 / 2.0 / 3.0
        scaleDensity = metrics.scaledDensity;
        densityDpi = metrics.densityDpi;  //120 160 240 320 480
        xdpi = metrics.xdpi;
        ydpi = metrics.ydpi;
        
        double x = Math.pow(screenWidth / xdpi, 2);  
	    double y = Math.pow(screenHeight / ydpi, 2);  
	    double screenInches = Math.sqrt(x + y);  
	    if (screenInches >= 6.0)  
	    	isPad = true;
	    
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "screenDensity", screenDensity, "densityDpi", densityDpi,"statusHeight",statusHeight,"screenWidth",screenWidth,"screenHeight",screenHeight);
    }
    
    /**
	 * 高ppi手机的缩放修正因子
	 */
	public static double getScreenCorrectionFactor()
	{
		return densityDpi == DisplayMetrics.DENSITY_XXHIGH ? 0.8 : 1.0;
	}

	public static double getFontScreenCorrectionFactor()
	{
		return densityDpi >= DisplayMetrics.DENSITY_XHIGH ? 0.8 : 1.0;
	}

    public static int dip2px(float dipValue){
        return (int)(dipValue * screenDensity + 0.5f);
    }

    public static int px2dip(float pxValue){

        return (int)(pxValue / screenDensity + 0.5f);
    }
    
    public static int sp2px(float spValue) {  
        return (int) (spValue * scaleDensity + 0.5f);  
    }  
    
    public static int px2sp(float pxValue) {  
        return (int) (pxValue / scaleDensity + 0.5f);  
    }  

    public static int getScreenWidth() {
        return screenWidth;
    }
    
    public static int getStatusHeight() {
        return statusHeight;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }
	
	public static int getAdjustTextSize(float size, double textRatio)
	{
		int newTextSize = (int) (size * textRatio);
		return newTextSize;
	}

	public static void adjustTextSize(TextView textView, double textRatio)
	{
		float newTextSize = (int) (textView.getTextSize() * textRatio);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
	}

	public static int dip2px(Context context, float dipValue)
	{
		if(context == null || context.getResources() == null || context.getResources().getDisplayMetrics() == null)
			return (int)dipValue;
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public static int sp2px(Context context, float spValue)
	{
		if(context == null || context.getResources() == null || context.getResources().getDisplayMetrics() == null)
			return (int)spValue;
		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue)
	{
		if(context == null || context.getResources() == null || context.getResources().getDisplayMetrics() == null)
			return (int)pxValue;
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static float dipToPixels(Context context, float dipValue)
	{
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
	}
	
	public static boolean isPad() {  
		return isPad;
	}  
}
