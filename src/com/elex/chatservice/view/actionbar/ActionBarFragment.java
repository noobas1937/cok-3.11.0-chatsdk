package com.elex.chatservice.view.actionbar;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.view.allianceshare.PopupWindows;

public class ActionBarFragment extends Fragment
{
	protected MyActionBarActivity	activity;
	protected View					fragmentLayout;
	protected boolean				adjustSizeCompleted	= false;
	private PopupWindows			popupMenu			= null;

	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		fragmentLayout = view.findViewById(ResUtil.getId(this.activity, "id", "fragmentLayout"));

		getMemberSelectButton().setVisibility(View.GONE);
		getEditButton().setVisibility(View.GONE);
		getReturnButton().setVisibility(View.GONE);
		getWriteButton().setVisibility(View.GONE);
		getShowFriendButton().setVisibility(View.GONE);
		getShowNearbyMsgBtn().setVisibility(View.GONE);
		getShowNearbyPeopleBtn().setVisibility(View.GONE);
		getAllianceShareButton().setVisibility(View.GONE);
		getAllianceShareSendButton().setVisibility(View.GONE);
		getAllianceImageDelButton().setVisibility(View.GONE);
		getImageChooseConfirmButton().setVisibility(View.GONE);
		getRealtimeRightBtn().setVisibility(View.GONE);
	}

	public TextView getTitleLabel()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.titleLabel;
	}

	protected Button getMemberSelectButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.optionButton;
	}

	protected Button getEditButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.editButton;
	}

	protected Button getWriteButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.writeButton;
	}

	protected Button getShowFriendButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.showFriend;
	}
	
	protected View getRealtimeRightBtn()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.realtime_btn;
	}
	
	protected TextView getRealtimeRightBtnText()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.realtime_btn_text;
	}
	
	protected Button getShowNearbyPeopleBtn()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.showNearbyPeopleBtn;
	}
	
	protected View getShowNearbyMsgBtn()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.showNearbyMsgBtn;
	}

	protected Button getAllianceShareButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.allianceShareBtn;
	}

	protected Button getAllianceShareSendButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.allianceShareSend;
	}

	protected Button getAllianceImageDelButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.imageDelButton;
	}

	protected Button getImageChooseConfirmButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.imageChooseComfirmButton;
	}

	protected Button getReturnButton()
	{
		if (activity == null)
		{
			return null;
		}
		return activity.returnButton;
	}

	protected int computeUsableHeight()
	{
		Rect r = new Rect();
		// In effect, this tells you the available area where content can be
		// placed and remain visible to users.
		fragmentLayout.getWindowVisibleDisplayFrame(r);
		return (r.bottom - r.top);
	}

	protected int	usableHeight	= -1;
	public boolean isSoftKeyBoardVisibile = false;

	public void hideSoftKeyBoard()
	{
		InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager != null && fragmentLayout != null && fragmentLayout.getWindowToken() != null)
		{
			inputManager.hideSoftInputFromWindow(fragmentLayout.getWindowToken(), 0);
			isSoftKeyBoardVisibile = false;
		}
	}

	public void showSoftKeyBoard(View view)
	{
		InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, 0);
		isSoftKeyBoardVisibile = true;
	}

	public void saveState()
	{
	}

	protected boolean	inited	= false;

	protected void stopTimer()
	{
		if (service != null)
		{
			service.shutdown();
		}
	}

	protected void onBecomeVisible()
	{

	}

	// 200ms是动画的时间
	protected int						timerDelay	= 200;
	private ScheduledExecutorService	service;

	protected void startTimer()
	{
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					if (activity == null)
					{
						stopTimer();
						activity.hideProgressBar();
						return;
					}
					// 有时activity为null
					activity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							stopTimer();
							initData();
						}
					});
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(timerTask, timerDelay, TimeUnit.MILLISECONDS);
	}

	public void hidePopupMenu()
	{
		if (popupMenu != null && popupMenu.isShowing())
			popupMenu.dismiss();
	}

	protected void initData()
	{
		if (inited)
		{
			if(activity!=null)
				activity.hideProgressBar();
			return;
		}

		if (activity == null)
			return;

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// 可能在加载过程中关掉activity，导致出异常
				try
				{
					createList();

					renderList();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

		inited = true;
	}

	protected void createList()
	{
	}

	protected void renderList()
	{
	}

	public void onDestroy()
	{
		activity = null;
		fragmentLayout = null;
		popupMenu = null;
		isSoftKeyBoardVisibile = false;
		super.onDestroy();
	}
}
