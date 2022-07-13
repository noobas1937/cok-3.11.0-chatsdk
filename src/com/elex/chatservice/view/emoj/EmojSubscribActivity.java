package com.elex.chatservice.view.emoj;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

public class EmojSubscribActivity extends MyActionBarActivity
{
	public EmojSubscribAdapter	adapter		= null;
	private ListView			emoj_listView;
	private static int			lastScrollX	= -1;
	private static int			lastScrollY	= -1;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.emoj_subscrib_list_activity, fragment_holder, true);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_EXEPRESSION));
		showRightBtn(null);

		emoj_listView = (ListView) findViewById(R.id.emoj_listView);
		emoj_listView.setDivider(null);
		adapter = new EmojSubscribAdapter(this);
		emoj_listView.setAdapter(adapter);
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);
		restorePosition();
	}
	
	public void notifyDataSetChanged()
	{
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (adapter != null)
			adapter.refreshEmojListData();
	}

	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			emoj_listView.setSelectionFromTop(lastX, lastY);
		}
		lastScrollX = lastScrollY = -1;
	}

	public void saveState()
	{
		if (getCurrentPos() != null)
		{
			lastScrollX = getCurrentPos().x;
			lastScrollY = getCurrentPos().y;
		}
	}

	protected Point getCurrentPos()
	{
		if (emoj_listView == null)
		{
			return null;
		}
		int x = emoj_listView.getFirstVisiblePosition();
		View v = emoj_listView.getChildAt(0);
		System.out.println("v.height");
		int y = (v == null) ? 0 : (v.getTop() - emoj_listView.getPaddingTop());

		return new Point(x, y);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

}