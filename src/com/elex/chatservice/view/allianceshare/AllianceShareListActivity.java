package com.elex.chatservice.view.allianceshare;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.adapter.AllianceShareListAdapter;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshListView;

public class AllianceShareListActivity extends MyActionBarActivity
{
	public AllianceShareListAdapter	adapter							= null;
	private ListView				mListView;
	private PullToRefreshListView	allianceShareListView;
	private TextView				nullAllianceShareTip;
	private RelativeLayout			comment_input_layout;
	private EditText				comment_edit;
	private Button					sendCommentBtn;
	private static int				lastScrollX						= -1;
	private static int				lastScrollY						= -1;
	private static boolean			dataChanged						= false;
	private String					currentNoticeUid				= "";
	private String					currentAtUid					= "";
	private String					currentFid						= "";
	private PopupWindows			deleltePopupMenu				= null;
	private boolean					adjustSizeCompleted				= false;
	private Timer					mTimer;
	private TimerTask				mTimerTask;
	private int						inputEditTopPos					= 0;
	private int						currentAllianceSharePosition	= -1;
	private int						currentListItemHeight			= 0;
	private int						currentCommentOffset			= 0;
	public boolean 					onEnter = false;
	private Timer					timer;
	private int						initTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__alliance_share_list_fragment, fragment_holder, true);
		
		ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_ALLIANCE_SHARE);
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(allianceShareBtn);
		
		allianceShareListView = (PullToRefreshListView) findViewById(R.id.allianceShareListView);
		comment_input_layout = (RelativeLayout) findViewById(R.id.comment_input_layout);
		comment_edit = (EditText) findViewById(R.id.comment_edit);
		sendCommentBtn = (Button) findViewById(R.id.sendCommentBtn);
		nullAllianceShareTip = (TextView) findViewById(R.id.nullAllianceShareTip);
		nullAllianceShareTip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_ALLIANCE_SHARE));
		nullAllianceShareTip.setVisibility(View.GONE);
		
		allianceShareListView.setLanguage(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		allianceShareListView.setLoadTextCorlor(0xffe0c6aa);
		allianceShareListView.setPullLoadEnabled(false);
		allianceShareListView.setPullRefreshEnabled(false);
		refreshScrollLoadEnabled();
		mListView = allianceShareListView.getRefreshableView();
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mListView.setDivider(getResources().getDrawable(R.drawable.share_list_divier));
		
		allianceShareListView.setOnScrollListener(new OnScrollListener()
		{
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				if(scrollState == SCROLL_STATE_FLING)
					Glide.with(AllianceShareListActivity.this).pauseRequests();
				else
					Glide.with(AllianceShareListActivity.this).resumeRequests();
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
			}
		});

		
		comment_input_layout.setVisibility(View.INVISIBLE);

		comment_input_layout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{

			@Override
			public void onGlobalLayout()
			{
				int location[] = { 0, 0 };
				comment_input_layout.getLocationInWindow(location);
				if (inputEditTopPos == 0 && location[1] < ScaleUtil.getScreenHeight() - 200)
				{
					inputEditTopPos = ScaleUtil.getScreenHeight() - location[1];
					if (currentListItemHeight != 0 && currentAllianceSharePosition != -1)
					{
						int height = currentListItemHeight + inputEditTopPos;
						final int offsetY = mListView.getHeight() - height + currentCommentOffset;
						runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								mListView.setSelectionFromTop(currentAllianceSharePosition, offsetY);
								currentListItemHeight = 0;
								currentAllianceSharePosition = -1;
							}
						});
					}
				}

			}
		});

		

		comment_edit.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hidePopupMenu();
				return false;
			}
		});

		
		sendCommentBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (comment_edit.getText().length() > 0)
				{
					System.out.println("评论了: " + comment_edit.getText().toString());
					JniController.getInstance().excuteJNIVoidMethod(
							"sendAllianceCircleCommand",
							new Object[] {
									"",
									Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_COMMENT),
									comment_edit.getText().toString(),
									"",
									currentFid,
									currentAtUid,
									currentNoticeUid,
									"alliance" });
				}
				resetState();
			}
		});


		allianceShareListView.setOnRefreshListener(new OnRefreshListener<ListView>()
		{
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
			{

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
			{
				if (adapter != null)
					adapter.isLoadingMore = true;
				AllianceShareManager.getInstance().loadMoreHistoryAllianceShare();
			}
		});

		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		
		createList();
		renderList();
		
		initTime = TimeManager.getInstance().getCurrentTime();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "initTime", initTime);
	}
    
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case AllianceShareManager.TAKE_PICTURE:
				if (AllianceShareManager.getInstance().getSelectedImageSize() < AllianceShareManager.SHARE_NUM_LIMIT && resultCode == RESULT_OK
						&& StringUtils.isNotEmpty(AllianceShareActivity.currentPhotoPath))
				{
					ImageItem item = new ImageItem();
					item.sourcePath = AllianceShareActivity.currentPhotoPath;
					AllianceShareManager.getInstance().putSelectedImage(item.sourcePath, item);
					ServiceInterface.showPublishAllianceShareActivity(this, true);
				}
				break;
		}
	}
	
	@Override
	protected void onDestroy()
	{
        
		int stayTime = TimeManager.getInstance().getCurrentTime() - initTime;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "stayTime", stayTime);
		LogUtil.trackAllianceShareStayTime(stayTime);
		AllianceShareManager.getInstance().goOffline();
		resetCurrentCommentInfo();
		stopLoadTimer();
		nullAllianceShareTip.setVisibility(View.GONE);
		onEnter = false;
		if(AllianceShareManager.localNewestAllianceShareTime >= AllianceShareManager.remoteNewestAllianceShareTime)
			JniController.getInstance().excuteJNIVoidMethod("postNewAllianceShare", null);
		super.onDestroy();
	}
	
	public void refreshAllianceShareNotice()
	{
		if (adapter != null)
			adapter.notifyDataSetChangedOnUI();
	}
	
	public void onLoadMoreComplete()
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{
					if (allianceShareListView != null)
					{
						allianceShareListView.onPullDownRefreshComplete();
						allianceShareListView.onPullUpRefreshComplete();
					}

					if (adapter != null)
					{
						adapter.isLoadingMore = false;
						adapter.notifyDataSetChangedOnUI();
					}
					hideProgressBar();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	protected void createList()
	{
		adapter = new AllianceShareListAdapter(this);
	}

	public static void onAllianceShareDataChanged()
	{
		dataChanged = true;
		if (ChatServiceController.getAllianceShareListActivity() != null)
		{
			try
			{
//				System.out.println("onAllianceShareDataChanged");
				ChatServiceController.getAllianceShareListActivity().refreshLoadState();
				ChatServiceController.getAllianceShareListActivity().notifyDataSetChanged();
				dataChanged = false;
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}
	
	private void refreshLoadState()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				nullAllianceShareTip.setVisibility(View.GONE);
				stopLoadTimer();
				refreshScrollLoadEnabled();
			}
		});
	}
	
	private void stopLoadTimer()
	{
		hideProgressBar();
		if(timer!=null)
		{
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		AllianceShareManager.getInstance().goOnline();
		refreshScrollLoadEnabled();
		if(!AllianceShareManager.getInstance().isAllianceShareDataExist() && !onEnter)
		{
			showProgressBar();
			timer = new Timer();
			TimerTask task = new TimerTask()
			{
				
				@Override
				public void run()
				{
					runOnUiThread(new Runnable()
					{
						
						@Override
						public void run()
						{
							nullAllianceShareTip.setVisibility(View.VISIBLE);
							stopLoadTimer();
						}
					});
					
				}
			};
			timer.schedule(task, 10000);
		}

		if (dataChanged)
		{
			nullAllianceShareTip.setVisibility(View.GONE);
			System.out.println("alliancesharelistfragment onResume 2");
			notifyDataSetChanged();
			dataChanged = false;
		}
		onEnter = true;
	}

	protected void renderList()
	{
		System.out.println("alliance adapter renderList");
		if (adapter != null)
			mListView.setAdapter(adapter);
		restorePosition();
		hideProgressBar();
	}

	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			mListView.setSelectionFromTop(lastX, lastY);
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
		if (mListView == null)
		{
			return null;
		}
		int x = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		System.out.println("v.height");
		int y = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());

		return new Point(x, y);
	}

	public void notifyDataSetChanged()
	{
		try
		{
			if (adapter != null)
				adapter.notifyDataSetChangedOnUI();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private void resetCurrentCommentInfo()
	{
		currentNoticeUid = "";
		currentAtUid = "";
		currentFid = "";
	}

	public void createTimerTask()
	{
		stopTimerTask();
		mTimer = new Timer();
		mTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				onLoadMoreComplete();
			}
		};
		if (mTimer != null)
			mTimer.schedule(mTimerTask, 5000);
	}

	private void stopTimerTask()
	{
		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}
	
	private final static int	sendButtonBaseWidth		= 51;
	private final static int	sendButtonBaseHeight	= 50;

	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (sendCommentBtn.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (!adjustSizeCompleted && sendCommentBtn.getWidth() != 0)
		{
			RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) sendCommentBtn.getLayoutParams();
			layoutParams1.width = (int) (sendButtonBaseWidth * ConfigManager.scaleRatioButton);
			layoutParams1.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			sendCommentBtn.setLayoutParams(layoutParams1);

			RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) comment_edit.getLayoutParams();
			layoutParams2.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			comment_edit.setLayoutParams(layoutParams2);

			ScaleUtil.adjustTextSize(comment_edit, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(nullAllianceShareTip, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
	}

	public void resetState()
	{
		hideSoftKeyBoard();
		hidePopupMenu();
		hideCommentInputLayout();
		if (adapter != null)
			adapter.hideCommentLikeMenu();
	}

	public void showCommentInputLayout(int position, int commentOffset, String currentFid, String currentAtUid, String currentNoticeUid,
			String replayName)
	{
		if (isSoftKeyBoardVisibile)
		{
			comment_input_layout.setVisibility(View.INVISIBLE);
			hideSoftKeyBoard();
			return;
		}
		comment_input_layout.setVisibility(View.VISIBLE);
		comment_edit.setText("");
		comment_edit.requestFocus();
		this.currentAllianceSharePosition = position;
		this.currentCommentOffset = commentOffset;
		showSoftKeyBoard(comment_edit);
		
		int visiblePos = mListView.getFirstVisiblePosition();
		int index = position - visiblePos;
		if (index < adapter.getCount() && index >= 0)
		{
			View view = mListView.getChildAt(index);
			if (view != null)
			{
				if (inputEditTopPos != 0)
				{
					currentListItemHeight = 0;
					int height = view.getHeight() + inputEditTopPos;
					int offsetY = mListView.getHeight() - height + commentOffset;
					mListView.setSelectionFromTop(position, offsetY);
				}
				else
				{
					currentListItemHeight = view.getHeight();
				}
			}
		}

		this.currentNoticeUid = currentNoticeUid;
		this.currentAtUid = currentAtUid;
		this.currentFid = currentFid;
		if (StringUtils.isEmpty(replayName))
		{
			comment_edit.setHint(LanguageManager.getLangByKey(LanguageKeys.BTN_COMMENT));
		}
		else
		{
			comment_edit.setHint(LanguageManager.getLangByKey(LanguageKeys.BTN_REPLY) + replayName + ":");
		}
	}

	public void hideCommentInputLayout()
	{
		if (comment_input_layout != null && comment_input_layout.getVisibility() != View.INVISIBLE)
			comment_input_layout.setVisibility(View.INVISIBLE);
		resetCurrentCommentInfo();
	}

	public void showAllianceShareCommentDeletePopupWindow(View parent, AllianceShareComment comment, String allianceShareSender)
	{
		deleltePopupMenu = new PopupWindows(AllianceShareListActivity.this, comment, allianceShareSender);
		deleltePopupMenu.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}

	public void hideAllianceShareCommentDeletePopupWindow()
	{
		if (deleltePopupMenu != null && deleltePopupMenu.isShowing())
			deleltePopupMenu.dismiss();
	}
	
	public void refreshScrollLoadEnabled()
	{
		if(AllianceShareManager.getInstance().isAllianceShareDataExist() && !allianceShareListView.isScrollLoadEnabled())
			allianceShareListView.setScrollLoadEnabled(true);
	}

	public void showLoadFooter(boolean isVisible)
	{
		allianceShareListView.showFooterLayout(isVisible);
	}
}
