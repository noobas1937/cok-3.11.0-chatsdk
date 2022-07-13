package com.elex.im.ui.view.listview;

public abstract interface ListViewLoadListener
{
	public abstract boolean getIsListViewToTop();

	public abstract boolean getIsListViewToBottom();

	public abstract void refreshData();
}
