package com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class RecyclerSwipeMenu {

	private Context mContext;
	private List<RecyclerSwipeMenuItem> mItems;
	private int mViewType;

	public RecyclerSwipeMenu(Context context) {
		mContext = context;
		mItems = new ArrayList<RecyclerSwipeMenuItem>();
	}

	public Context getContext() {
		return mContext;
	}

	public void addMenuItem(RecyclerSwipeMenuItem item) {
		mItems.add(item);
	}

	public void removeMenuItem(RecyclerSwipeMenuItem item) {
		mItems.remove(item);
	}

	public List<RecyclerSwipeMenuItem> getMenuItems() {
		return mItems;
	}

	public RecyclerSwipeMenuItem getMenuItem(int index) {
		return mItems.get(index);
	}

	public int getViewType() {
		return mViewType;
	}

	public void setViewType(int viewType) {
		this.mViewType = viewType;
	}

}
