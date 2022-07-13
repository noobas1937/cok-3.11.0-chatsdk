package com.elex.im.ui.view;

import android.os.Bundle;

import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;

public class MemberSelectorActivity extends MyActionBarActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		fragmentClass = MemberSelectorFragment.class;

		UIManager.toggleFullScreen(true, true, this);

		super.onCreate(savedInstanceState);
	}
	
	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	public void onBackButtonClick()
	{
		((MemberSelectorFragment) fragment).onBackButtonClick();
		super.onBackButtonClick();
	}
}
