package com.elex.im.ui.view;

import android.content.Intent;
import android.os.Bundle;

import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;

public class WriteMailActivity extends MyActionBarActivity
{
	public String	roomName;
	public String	memberUids;
	public String	memberNames;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		fragmentClass = WriteMailFragment.class;

		UIManager.toggleFullScreen(true, true, this);

		super.onCreate(savedInstanceState);
	}
	
	protected void showBackground()
	{
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			roomName = data.getStringExtra("roomName");
			memberUids = data.getStringExtra("uidStr");
			memberNames = data.getStringExtra("nameStr");
		}
	}
}