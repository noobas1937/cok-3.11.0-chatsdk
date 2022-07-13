package com.elex.chatservice.model;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.elex.chatservice.controller.DanmuManager;
import com.elex.chatservice.danmu.controller.IDanmakuView;
import com.elex.chatservice.danmu.model.BaseDanmaku;
import com.elex.chatservice.danmu.model.android.Danmakus;
import com.elex.chatservice.danmu.parser.BaseDanmakuParser;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;

public class DragonDanmuParser extends BaseDanmakuParser {

	private IDanmakuView mDanmakuView;
	private Context context;
	
	public DragonDanmuParser(IDanmakuView mDanmakuView,Context context)
	{
		this.mDanmakuView = mDanmakuView;
		this.context = context;
	}
	
    @Override
    public Danmakus parse() {

    	List<DanmuInfo> danmuData = DanmuManager.getInstance().getDanmuData();
    	if(danmuData == null)
    		return null;
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE	, LogUtil.TAG_DEBUG, "size",danmuData.size());
    	Danmakus result = new Danmakus();
    	for(DanmuInfo danmuInfo : danmuData)
    	{
    		if(danmuInfo!=null)
    		{
    			BaseDanmaku danmu = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, mContext);
    			if(mDanmakuView!=null)
    				danmu.setTime(mDanmakuView.getCurrentTime() + 1000);
    			if(context!=null)
    			{
    				danmu.textSize = ScaleUtil.sp2px(context,18);
        			danmu.padding = ScaleUtil.dip2px(context,5);
    			}
    			else
    			{
    				danmu.textSize =(int)(18f * getDisplayer().getDensity()+0.5f);
    				danmu.padding = (int)(5f * getDisplayer().getDensity()+0.5f);
    			}
    			
    	    	danmu.textColor = danmuInfo.getColor();
    	    	danmu.text = danmuInfo.getText();
    	    	danmu.priority = 1;
    	    	danmu.isLive = true;
    	    	if(danmuInfo.isSelf())
    	    		danmu.borderColor = Color.GREEN;
    			result.addItem(danmu);
    		}
    	}
        return result;
    }
    
}
