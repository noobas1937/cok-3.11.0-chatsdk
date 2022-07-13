package com.elex.chatservice.view.danmu;

import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.DanmuManager;
import com.elex.chatservice.model.DanmuMenuInfo;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.danmu.DanmuScrollMenuBar.DanmuMenuBarItemClickListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class DanmuMenuPanel extends LinearLayout{
    private DanmuScrollMenuBar danmu_bgcolor_menu;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public DanmuMenuPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public DanmuMenuPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public DanmuMenuPanel(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs){
		LayoutInflater.from(context).inflate(R.layout.danmu_menu_panel, this);
		danmu_bgcolor_menu = (DanmuScrollMenuBar) findViewById(R.id.danmu_bgcolor_menu);
	}
	
	public void init(final int menuType,DanmuScrollMenuBar menuBar,List<DanmuMenuInfo> danmuMenuArray){
	    if(danmuMenuArray == null || danmuMenuArray.size() == 0){
	        return;
	    }
	    for(DanmuMenuInfo danmuMenuInfo : danmuMenuArray){
	    	menuBar.addMenu(danmuMenuInfo);
	    }
	    if(menuType == DanmuManager.DANMU_MENU_TYPE_BGCOLOR)
	    	menuBar.refreshMenuSelectedStatus(DanmuManager.danmuFgColorIndex);
        
	    menuBar.setMenuBarItemClickListener(new DanmuMenuBarItemClickListener() {
            
            @Override
            public void onItemClick(int position) {
            	DanmuManager.getInstance().setDanmuTextStyle(menuType, position);
            }
        });
	}
	
	public void initDanmuMenu(Activity activity)
	{
		SparseArray<List<DanmuMenuInfo>> danmuMenuSparseArray = DanmuManager.getInstance().getDanmuMenuSparseArray();
		if(danmuMenuSparseArray == null || danmuMenuSparseArray.size()<=0)
			return;
		List<DanmuMenuInfo> bgColorMenuArray = danmuMenuSparseArray.get(DanmuManager.DANMU_MENU_TYPE_BGCOLOR);
		if(bgColorMenuArray == null || bgColorMenuArray.size() <= 0)
			return;
		init(DanmuManager.DANMU_MENU_TYPE_BGCOLOR,danmu_bgcolor_menu,bgColorMenuArray);
	}
    
    public void setTabBarVisibility(DanmuScrollMenuBar danmuMenu,boolean isVisible){
        if(!isVisible){
        	danmuMenu.setVisibility(GONE);
        }else{
        	danmuMenu.setVisibility(VISIBLE);
        }
    }
	
}
