package com.elex.chatservice.view.kurento;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class VoiceFloatBall extends LinearLayout
{
	private WindowManager				mWindowManager;
	private WindowManager.LayoutParams	mLayoutParams;
	private float						mLastX;
	private float						mLastY;
	private int							mLayoutGravity	= Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	private Scroller					mScroller, mClipScroller;
	private int							mScreenWidth, mScreenHeight;
	private int							mRightEdge;
	private boolean						isAdded;
	private int							mTop;
	private Rect						windowRect		= new Rect();

	private View						voiceBall;
	private ImageView					floatBall;
	private int							leftMenuWidth;
	private int							mTouchSlop;
	private boolean						isIntercepted	= false;
	private boolean						layoutfromTouch	= false;
	private boolean						isMenuShowing	= false;
	private int							menuWidth;
	private int							menuHeight;
	private ExpanableLayout				menu;
	private int							floatBallWidth, floatBallHeight;
	private int							SCROLL_DURATION	= 300;
	private SingleIcon					singleIcon;
	private DoubleIcon					doubleIcon;

	// 小悬浮窗的宽
	public int							viewWidth;
	// 小悬浮窗的高
	public int							viewHeight;

	private IMenu						menuOperator;

	// public void setIsHiddenWhenExit(boolean isHiddenWhenExit) {
	// this.isHiddenWhenExit = isHiddenWhenExit;
	// }
	public static class Builder
	{
		private Context			context;
		private int				width, height;
		private IMenu			iMenu;
		private SingleIcon		singleIcon;
		private DoubleIcon		doubleIcon;
		private WindowManager	windowManager;

		public Builder(Context context)
		{
			this.context = context;
		}

		/**
		 * 设置悬浮球的菜单
		 *
		 * @param menu
		 * @return
		 */
		public Builder menu(IMenu menu)
		{
			this.iMenu = menu;
			return this;
		}

		public Builder windowManager(WindowManager windowManager)
		{
			this.windowManager = windowManager;
			return this;
		}

		/**
		 * 设置悬浮球的宽度
		 *
		 * @param width
		 * @return
		 */
		public Builder width(int width)
		{
			this.width = width;
			return this;
		}

		/**
		 * 设置悬浮球的高度
		 *
		 * @param height
		 * @return
		 */
		public Builder height(int height)
		{
			this.height = height;
			return this;
		}

		/**
		 * 设置悬浮球显示的图片和在点击以后和正常两种状态下图片显示的透明度
		 *
		 * @param singleIcon
		 * @return
		 */
		public Builder icon(SingleIcon singleIcon)
		{
			this.singleIcon = singleIcon;
			this.doubleIcon = null;
			return this;
		}

		/**
		 * 设置悬浮球在点击以后和正常两种状态下显示的两张图片
		 *
		 * @param doubleIcon
		 * @return
		 */
		public Builder doubleIcon(DoubleIcon doubleIcon)
		{
			this.doubleIcon = doubleIcon;
			this.singleIcon = null;
			return this;
		}

		public VoiceFloatBall build()
		{
			VoiceFloatBall floatBall = new VoiceFloatBall(context, iMenu, singleIcon, doubleIcon, width, height);
			return floatBall;
		}
	}

	private OnClickListener mClickListener;

	public void setOnClickListener(OnClickListener listener)
	{
		mClickListener = listener;
	}

	public VoiceFloatBall(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, null, null, null, 0, 0);
	}

	private VoiceFloatBall(Context context, IMenu menu, SingleIcon singleIcon, DoubleIcon doubleIcon, int floatBallWidth,
			int floatBallHeight)
	{
		super(context);
		init(context, menu, singleIcon, doubleIcon, floatBallWidth, floatBallHeight);
	}

	private void init(Context context, IMenu menu, SingleIcon singleIcon, DoubleIcon doubleIcon, int fbWidth, int fbHeight)
	{
		if (menu != null)
		{
			menu.onAttach(this, context.getApplicationContext());
		}
		this.singleIcon = singleIcon;
		this.doubleIcon = doubleIcon;
		menuOperator = menu;
		mScroller = new Scroller(getContext());
		mClipScroller = new Scroller(getContext(), new LinearInterpolator());
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		addMenu(context);
		LayoutInflater.from(context).inflate(R.layout.voice_float_ball, this);
		voiceBall = findViewById(R.id.voice_ball);
		floatBall = (ImageView) findViewById(R.id.floatBall);
		setFloatImage(true);
		voiceBall.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mClickListener != null)
				{
					mClickListener.onClick(v);
				}
				showMenu(!isMenuShowing);
				invalidate();
			}
		});
		viewWidth = voiceBall.getLayoutParams().width;
		viewHeight = voiceBall.getLayoutParams().height;
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "viewWidth", viewWidth, "viewHeight", viewHeight, "menuWidth",
//				menuWidth);
		leftMenuWidth = menuWidth - viewWidth / 2;
	}

	private void addMenu(Context context)
	{
		if (menuOperator != null)
		{
			menuWidth = menuOperator.getMenuWidth();
			menuHeight = menuOperator.getMenuHeight();
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "addMenu menuWidth  ", menuWidth, "menuHeight",
//					menuHeight, "ScaleUtil.getScreenWidth() ", ScaleUtil.getScreenWidth());
		}
		else
		{
			menuWidth = ScaleUtil.dip2px(getContext(), 135);
			menuHeight = ScaleUtil.dip2px(getContext(), 30);
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "addMenu2 menuWidth  ", menuWidth, "menuHeight",
//					menuHeight, "ScaleUtil.getScreenWidth() ", ScaleUtil.getScreenWidth());
		}
		menu = new ExpanableLayout(context);
		menu.setId(getId());
		addMenuContent(menu);
		LayoutParams layoutParams = new LayoutParams(menuWidth, LayoutParams.WRAP_CONTENT);
		if (menuOperator != null)
		{
			addView(menu, layoutParams);
			// menu.setVisibility(GONE);
		}
		// 如果没有背景，则设置透明的背景，不然不会出现动画展开和缩放的动画
		if (menu.getBackground() == null)
		{
			menu.setBackgroundColor(Color.TRANSPARENT);
		}
		setMenuOffset(0);
	}

	public int getId()
	{
		return IDFactory.getId();
	}

	private void setFloatImage(boolean enable)
	{
		// if (singleIcon != null) {
		// ivFloatBall.setImageResource(singleIcon.bitmap);
		// ViewHelper.setAlpha(this, enable ? singleIcon.enable :
		// singleIcon.normal);
		// } else if (doubleIcon != null) {
		// ivFloatBall.setImageResource(enable ? doubleIcon.enable :
		// doubleIcon.normal);
		// }
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "enable", enable);
		CompatibleApiUtil.getInstance().setButtonAlpha(floatBall, enable);
	}

	public void updateFloatBallImage()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (floatBall != null)
		{
			if (WebRtcPeerManager.getInstance().canSpeak())
				floatBall.setImageResource(R.drawable.realtime_voice_btn);
			else
				floatBall.setImageResource(R.drawable.realtime_voice_btn_disable);
		}
	}

	private void addMenuContent(RelativeLayout parent)
	{
		if (menuOperator != null)
		{
			menuOperator.addMenu(parent);
		}
	}

	public void hideFloatBall()
	{
		isMenuShowing = false;
		// stopClipRunner();
		menu.setOffset(0);
		setVisibility(GONE);
		removeCallbacks(mclipRunnable);
		mClipScroller.setFinalX(0);
	}

	public void hideMenu()
	{
		removeCallbacks(mHideMenuRunnable);
		showMenu(false);
	}

	public void hideMenuImmediately()
	{
		removeCallbacks(mHideMenuRunnable);
		isMenuShowing = false;
		setMenuOffset(0);
	}

	public void showMenu(String tip)
	{
		if (menuOperator != null)
			menuOperator.setMenuTip(tip);
		if (floatBallLayouted)
		{
			forceLayout = true;
			requestLayout();
			setFloatImage(true);
			showMenu(true);
			fadeOutFloatBall();
		}
	}

	private static final int	Left	= 0;
	private static final int	Right	= 1;

	private void showMenuSide(int side)
	{
		if (menu == null)
		{
			return;
		}
		switch (side)
		{
			case Left:
				menu.setOritation(ExpanableLayout.LEFT);
				if (menuOperator != null)
				{
					menuOperator.showingLeftMenu();
				}
				break;
			case Right:
				menu.setOritation(ExpanableLayout.RIGHT);
				if (menuOperator != null)
				{
					menuOperator.showingRightMenu();
				}
				break;
		}
	}

	private void showMenu(boolean show)
	{
		stopClipRunner();
		if (show)
		{
			if (isOnLeft())
			{
				if (menuOperator != null && menuOperator.isRightMenuEnable())
				{
					isMenuShowing = true;
					showMenuSide(Right);
				}
				else
				{
					isMenuShowing = false;
				}
			}
			else
			{
				if (menuOperator != null && menuOperator.isLeftMenuEnable())
				{
					isMenuShowing = true;
					showMenuSide(Left);
				}
				else
				{
					isMenuShowing = false;
				}
			}
			if (isMenuShowing)
			{
				menu.setVisibility(VISIBLE);
				mClipScroller.startScroll(0, 0, menuWidth, 0, SCROLL_DURATION);
				post(mclipRunnable);
				autoHideMenu();
			}
		}
		else
		{
			if (isMenuShowing)
			{
				isMenuShowing = false;
				mClipScroller.startScroll(menuWidth, 0, -menuWidth, 0, SCROLL_DURATION);
				post(mclipRunnable);
			}
		}
	}

	public void show()
	{
		if (mWindowManager == null)
		{
			createWindowManager();
		}
		if (mWindowManager == null)
		{
			return;
		}
		// ViewParent parent = this.getParent();
		// if (parent != null && parent instanceof ViewGroup) {
		// ((ViewGroup) parent).removeView(this);
		// }
		if (isAdded && mWindowManager != null)
		{
			mWindowManager.removeView(this);
			isAdded = false;
		}
		setVisibility(VISIBLE);
		if (!isAdded)
		{
			mWindowManager.addView(this, mLayoutParams);
			fadeOutFloatBall();
			isAdded = true;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
		}
		setMeasuredDimension(menuWidth * 2, voiceBall.getMeasuredHeight());
	}

	private boolean	forceLayout			= false;
	private boolean	floatBallLayouted	= false;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "forceLayout", forceLayout);
		if (!forceLayout && (layoutfromTouch || mScroller.computeScrollOffset() || mClipScroller.computeScrollOffset()))
		{
			return;
		}
		initScreenParams();
		int[] finalLocation = correctLocation();
		if (!forceLayout || !floatBallLayouted)
			doMove(finalLocation[0], finalLocation[1]);
		int[] floatLocation = new int[2];
		getLocationOnScreen(floatLocation);
		int left = floatLocation[0];
		int menuTop = (getMeasuredHeight() - menu.getMeasuredHeight()) / 2;
		int menuLeft = 0;
		if (isOnLeft())
		{
			menuLeft = menu.getMeasuredWidth();
		}
		else
		{
			menuLeft = 0;
		}
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "menu.getMeasuredWidth()", menu.getMeasuredWidth());
		menu.layout(menuLeft, menuTop, menuLeft + menu.getMeasuredWidth(), menuTop + menu.getMeasuredHeight());
		int floatLeft = l + leftMenuWidth;
		if (!forceLayout || !floatBallLayouted)
		{
			voiceBall.layout(floatLeft, t, floatLeft + voiceBall.getMeasuredWidth(), t + voiceBall.getMeasuredHeight());
			floatBallLayouted = true;
		}
		forceLayout = false;
	}

	private void Log(String msg)
	{
		Log.i("Tag", msg);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		int action = event.getActionMasked();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				layoutfromTouch = true;
				mLastX = event.getRawX();
				mLastY = event.getRawY();
				if (isTouchFloatBall(event))
				{
					removeCallbacks(mFadeOutRunnable);
					removeCallbacks(mScrollRunnable);
					mScroller.forceFinished(true);
					setFloatImage(true);
				}
				if (isTouchFloatBall(event))
				{
					hasTouchFloatBall = true;
				}
				if (hasTouchFloatBall && WebRtcPeerManager.getInstance().canSpeak() && ChatServiceController.isPressToSpeakVoiceMode)
					ServiceInterface.enableAudio(true);

				break;
			case MotionEvent.ACTION_MOVE:
				float x = event.getRawX();
				float y = event.getRawY();
				int deltaX = (int) (x - mLastX);
				int deltaY = (int) (y - mLastY);
				if (!isIntercepted)
				{
					if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop)
					{
						sendCancelEvent(event);
						isIntercepted = true;
						if (isMenuShowing && menu != null)
						{
							isMenuShowing = false;
							setMenuOffset(0);
						}
					}
					else
					{
						return super.dispatchTouchEvent(event);
					}
				}
				if (hasTouchFloatBall || isTouchFloatBall(event))
				{
					hasTouchFloatBall = true;
				}
				if (hasTouchFloatBall && !isMenuShowing || isMenuShowing)
				{
					move(deltaX, deltaY);
					mLastX = x;
					mLastY = y;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				hasTouchFloatBall = false;
				layoutfromTouch = false;
				isIntercepted = false;
				onFingerReleased();
				if (WebRtcPeerManager.getInstance().canSpeak() && ChatServiceController.isPressToSpeakVoiceMode)
					ServiceInterface.enableAudio(false);
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(event);
	}

	private void setMenuOffset(int offset)
	{
		menu.setOffset(offset);
		menu.setVisibility(offset == 0 ? GONE : VISIBLE);
		removeCallbacks(mclipRunnable);
		mClipScroller.setFinalX(0);
	}

	private boolean hasTouchFloatBall = false;

	/**
	 * 是否摸到了某个view
	 *
	 * @param ev
	 * @return
	 */
	private boolean isTouchFloatBall(MotionEvent ev)
	{
		if (voiceBall != null && voiceBall.getVisibility() == VISIBLE)
		{
			Rect bounds = new Rect();
			voiceBall.getGlobalVisibleRect(bounds);
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			if (bounds.contains(x, y))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}

	private void sendCancelEvent(MotionEvent lastEvent)
	{
		MotionEvent last = lastEvent;
		MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(),
				MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
		super.dispatchTouchEvent(e);
	}

	private boolean isOnLeft()
	{
		int[] floatLocation = new int[2];
		voiceBall.getLocationOnScreen(floatLocation);
		if (floatLocation[0] + viewWidth / 2 > mScreenWidth / 2)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private int[] correctLocation()
	{
		int[] correctLocation = new int[2];
		if (mLayoutParams == null || mWindowManager == null)
		{
			return correctLocation;
		}
		int finalX = mLayoutParams.x;
		int finalY = mLayoutParams.y;
		finalX = finalX + leftMenuWidth;
		mRightEdge = mScreenWidth - viewWidth;
		if (finalX + viewWidth / 2 > mScreenWidth / 2)
		{
			finalX = mRightEdge - leftMenuWidth;
		}
		else
		{
			finalX = 0 - leftMenuWidth;
		}
		int[] floatLocation = new int[2];
		voiceBall.getLocationOnScreen(floatLocation);
		int offsetY = 0;
		if (floatLocation[1] < mTop)
		{
			offsetY = mTop - floatLocation[1];
		}
		else if (floatLocation[1] + viewHeight > mScreenHeight)
		{
			offsetY = -(floatLocation[1] + viewHeight - mScreenHeight);
		}
		int gravity = mLayoutParams.gravity;
		if ((Gravity.BOTTOM & gravity) == Gravity.BOTTOM)
		{
			offsetY = -offsetY;
		}
		finalY = finalY + offsetY;
		correctLocation[0] = finalX;
		correctLocation[1] = finalY;
		return correctLocation;
	}

	private void initScreenParams()
	{
		mTop = ScaleUtil.getStatusHeight();
		mScreenWidth = ScaleUtil.getScreenWidth();
		mScreenHeight = ScaleUtil.getScreenHeight();
	}

	public void startScroll(int finalLeft, int finalTop, int duration)
	{
		final int startLeft = mLayoutParams.x;
		final int startTop = mLayoutParams.y;
		final int dx = finalLeft - startLeft;
		final int dy = finalTop - startTop;
		mScroller.startScroll(startLeft, startTop, dx, dy, duration);
		post(mScrollRunnable);
	}

	private void onFingerReleased()
	{
		forceLayout = true;
		requestLayout();

		int[] finalLocation = correctLocation();
		startScroll(finalLocation[0], finalLocation[1], SCROLL_DURATION);
	}

	private void doMove(int x, int y)
	{
		if (mWindowManager == null || mLayoutParams == null)
		{
			return;
		}
		mLayoutParams.x = x;
		mLayoutParams.y = y;
		mWindowManager.updateViewLayout(this, mLayoutParams);
	}

	private void move(int deltaX, int deltaY)
	{
		int gravity = mLayoutParams.gravity;
		if ((Gravity.RIGHT & gravity) == Gravity.RIGHT)
		{
			deltaX = -deltaX;
		}
		else if ((Gravity.LEFT & gravity) == Gravity.LEFT)
		{
		}
		if ((Gravity.BOTTOM & gravity) == Gravity.BOTTOM)
		{
			deltaY = -deltaY;
		}
		else if ((Gravity.TOP & gravity) == Gravity.TOP)
		{
		}
		mLayoutParams.x += deltaX;
		mLayoutParams.y += deltaY;
		doMove(mLayoutParams.x, mLayoutParams.y);
	}

	private ClipRunnable mclipRunnable = new ClipRunnable();

	private class ClipRunnable implements Runnable
	{
		@Override
		public void run()
		{
			final int currentX = mClipScroller.getCurrX();
			if (menu != null && menu.getVisibility() == VISIBLE)
			{
				if (mClipScroller.computeScrollOffset())
				{
					menu.setOffset(currentX);
					if (currentX == mClipScroller.getFinalX() && currentX == 0)
					{
						menu.setVisibility(GONE);
					}
					post(this);
				}
				else
				{
					menu.setOffset(currentX);
					if (currentX == mClipScroller.getFinalX() && currentX == 0)
					{
						menu.setVisibility(GONE);
					}
					removeCallbacks(this);
				}
			}
		}
	}

	private void fadeOutFloatBall()
	{
		removeCallbacks(mFadeOutRunnable);
		postDelayed(mFadeOutRunnable, 2000);
	}

	private void autoHideMenu()
	{
		removeCallbacks(mHideMenuRunnable);
		postDelayed(mHideMenuRunnable, 4000);
	}

	private FadeOutRunnable mFadeOutRunnable = new FadeOutRunnable();

	private class FadeOutRunnable implements Runnable
	{

		@Override
		public void run()
		{
			if (!isMenuShowing && !hasTouchFloatBall)
			{
				setFloatImage(false);
			}
		}
	}

	private Runnable			mScrollRunnable		= new Runnable()
													{

														@Override
														public void run()
														{
															if (mScroller.computeScrollOffset())
															{
																final int currentX = mScroller.getCurrX();
																final int currentY = mScroller.getCurrY();
																doMove(currentX, currentY);
																post(this);
															}
															else
															{
																removeCallbacks(this);
																fadeOutFloatBall();
															}
														}
													};

	private HideMenuRunnable	mHideMenuRunnable	= new HideMenuRunnable();

	private class HideMenuRunnable implements Runnable
	{

		@Override
		public void run()
		{
			if (isMenuShowing)
				hideMenuImmediately();
		}
	}

	public boolean isFloatBallShowed()
	{
		return isAdded;
	}

	public void dismiss()
	{
		if (isAdded && mWindowManager != null)
		{
			mWindowManager.removeView(this);
		}
		removeCallbacks(mFadeOutRunnable);
		stopClipRunner();
		stopScrollRunner();
		isAdded = false;
	}

	private void stopScrollRunner()
	{
		mScroller.abortAnimation();
	}

	private void stopClipRunner()
	{
		mClipScroller.abortAnimation();
	}

	/**
	 * 设置WindowManager
	 */
	private void createWindowManager()
	{

		Context context = getContext().getApplicationContext();
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mLayoutParams = new WindowManager.LayoutParams();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		}
		else
		{
			mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		}

		// mLayoutParams.type =
		// WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		mLayoutParams.format = PixelFormat.RGBA_8888;
		mLayoutParams.gravity = mLayoutGravity;
		mLayoutParams.width = viewWidth;
		mLayoutParams.height = viewHeight;
	}

	/**
	 * 设置悬浮球的位置
	 */
	public void setLayoutGravity(int layoutgravity)
	{
		mLayoutGravity = layoutgravity;
	}

	public static class DoubleIcon
	{
		public int normal, enable;

		/**
		 * @param enable
		 *            点击悬浮球以后显示的图片
		 * @param normal
		 *            普通状态下悬浮球上显示的图片
		 */
		public DoubleIcon(int enable, int normal)
		{
			this.normal = normal;
			this.enable = enable;
		}
	}

	public static class SingleIcon
	{
		public float	normal, enable;
		public int		bitmap;

		/**
		 * @param icon
		 * @param enable
		 *            图标点击以后的透明度,范围是0~1,正常是1
		 * @param normal
		 *            图标普通状态的下的透明度,范围是0~1
		 */
		public SingleIcon(int icon, float enable, float normal)
		{
			this.bitmap = icon;
			this.normal = normal;
			this.enable = enable;
		}
	}

	/**
	 * 判断悬浮球是否在屏幕以内
	 *
	 * @return
	 */
	private boolean hasBallInside()
	{
		Rect bounds = new Rect();
		voiceBall.getGlobalVisibleRect(bounds);
		int[] location = new int[2];
		voiceBall.getLocationOnScreen(location);
		bounds.set(location[0], location[1], location[0] + bounds.right, location[1] + bounds.bottom);
		if (windowRect.contains(bounds))
		{
			return true;
		}
		return false;
	}
}
