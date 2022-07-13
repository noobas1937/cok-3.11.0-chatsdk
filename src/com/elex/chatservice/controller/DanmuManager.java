package com.elex.chatservice.controller;

import java.util.ArrayList;
import java.util.List;

import com.elex.chatservice.model.DanmuInfo;
import com.elex.chatservice.model.DanmuMenuInfo;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.danmu.DanmuService;
import com.elex.chatservice.view.danmu.GameDamuView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

public class DanmuManager {

	private GameDamuView danmuWindow;
	private static DanmuManager danmuWindowManager;
	private Activity activity;
	private List<DanmuInfo> danmuData;
	private SparseArray<List<DanmuMenuInfo>> danmuMenuSparseArray;
	
	public static int danmuFgColorIndex = 0;
	public static int danmuStrokeColor  = Color.TRANSPARENT;
	
	public static final int				DANMU_MENU_TYPE_BGCOLOR = 1;
	public static final int				DANMU_MENU_TYPE_STROKE_COLOR = 2;
	
	private DanmuManager() {
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		danmuData = new ArrayList<DanmuInfo>();
		danmuMenuSparseArray = new SparseArray<List<DanmuMenuInfo>>();
		addDammuyMenuData();
	}
	
	public void clear()
	{
		if(danmuData!=null)
			danmuData.clear();
	}
	
	private void addDammuyMenuData()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		List<DanmuMenuInfo> menuArr = new ArrayList<DanmuMenuInfo>();
		
		DanmuMenuInfo info0 = new DanmuMenuInfo();
		info0.setBackgroundColor(0xffeeeeee);
		info0.setIndex(0);
		menuArr.add(info0);
		
		DanmuMenuInfo info1 = new DanmuMenuInfo();
		info1.setBackgroundColor(0xffc7beb3);
		info1.setIndex(1);
		menuArr.add(info1);
		
		DanmuMenuInfo info2 = new DanmuMenuInfo();
		info2.setBackgroundColor(0xff56e578);
		info2.setIndex(2);
		menuArr.add(info2);
		
		DanmuMenuInfo info3 = new DanmuMenuInfo();
		info3.setBackgroundColor(0xff4599f8);
		info3.setIndex(3);
		menuArr.add(info3);
		
		DanmuMenuInfo info4 = new DanmuMenuInfo();
		info4.setBackgroundColor(0xffaf49ea);
		info4.setIndex(4);
		menuArr.add(info4);
		
		DanmuMenuInfo info5 = new DanmuMenuInfo();
		info5.setBackgroundColor(0xffe8771f);
		info5.setIndex(5);
		menuArr.add(info5);
		
		danmuMenuSparseArray.put(DANMU_MENU_TYPE_BGCOLOR, menuArr);
		
	}
	
	public List<DanmuInfo> getDanmuData()
	{
		return danmuData;
	}

	public SparseArray<List<DanmuMenuInfo>> getDanmuMenuSparseArray()
	{
		return danmuMenuSparseArray;
	}
	
	public void setDanmuTextStyle(int danmuMenuType,int position)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "danmuMenuType",danmuMenuType);
		List<DanmuMenuInfo> menuInfoArr = danmuMenuSparseArray.get(danmuMenuType);
		if(menuInfoArr == null )
			return;
		if(position < 0 || position>=menuInfoArr.size())
			return;
		DanmuMenuInfo danmuMenuInfo = menuInfoArr.get(position);
		if(danmuMenuInfo!=null)
		{
			if(danmuMenuType == DANMU_MENU_TYPE_BGCOLOR)
				danmuFgColorIndex = danmuMenuInfo.getIndex();
//			else if(danmuMenuType == DANMU_MENU_TYPE_STROKE_COLOR)
//				danmuFgColor = danmuMenuInfo.get();
		}
			
	}

	public static DanmuManager getInstance()
	{
		if (danmuWindowManager == null)
		{
			synchronized (DanmuManager.class)
			{
				if (danmuWindowManager == null)
					danmuWindowManager = new DanmuManager();
			}
		}
		return danmuWindowManager;
	}

	public void createDanmuWindow(Activity activity) {
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		this.activity = activity;
		if(this.activity!=null)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"createWindow");
			WindowManager windowManager = this.activity.getWindowManager();
			if (danmuWindow == null) {
				danmuWindow = new GameDamuView(this.activity);
				windowManager.addView(danmuWindow, danmuWindow.danmuWindowParams);
			}
		}
	}
	
	public void addDanmuMenuInfo(int menuType,DanmuMenuInfo menuInfo)
	{
		List<DanmuMenuInfo> danmuMenuArray = danmuMenuSparseArray.get(menuType);
		if(danmuMenuArray == null)
			danmuMenuArray = new ArrayList<DanmuMenuInfo>();
		danmuMenuArray.add(menuInfo);
	}
	
	public void addDanmu(DanmuInfo danmuInfo)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if(danmuWindow!=null)
			danmuWindow.addDanmu(danmuInfo);
	}
	
	public void hideDanmu()
	{
		if(danmuWindow!=null)
			danmuWindow.hideDanmu();
	}
	
	public void showDanmu()
	{
		if(danmuWindow!=null)
			danmuWindow.showDanmu();
	}
	
	public void showDanmuInput()
	{
		if(danmuWindow!=null)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			danmuWindow.showSoftKeyBoard();
			WindowManager windowManager = activity.getWindowManager();
			windowManager.updateViewLayout(danmuWindow, danmuWindow.danmuWindowParams);
		}
	}
	
	public void hideDanmuInput()
	{
		if(danmuWindow!=null)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			danmuWindow.hideSoftKeyboard();
			WindowManager windowManager = activity.getWindowManager();
			windowManager.updateViewLayout(danmuWindow, danmuWindow.danmuWindowParams);
		}
	}

	public void removeDanmuWindow() {
		System.out.println("removeDanmuWindow");
		if (danmuWindow != null) {
			if(activity!=null)
			{
				WindowManager windowManager = activity.getWindowManager();
				windowManager.removeView(danmuWindow);
			}
			danmuWindow = null;
		}
	}

	public void removeAll() {
		activity.stopService(new Intent(activity, DanmuService.class));
		removeDanmuWindow();

	}

	public boolean isWindowShowing() {
		return danmuWindow != null;
	}

}
