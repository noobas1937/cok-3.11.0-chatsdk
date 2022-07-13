package com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

public class SwipeMenuRecyclerListView extends RecyclerView {

    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_X = 1;
    private static final int TOUCH_STATE_Y = 2;

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = -1;
    private int mDirection = 1;//swipe from right to left by default

    private int MAX_Y = 5;
    private int MAX_X = 3;
    private float mDownX;
    private float mDownY;
    private int mTouchState;
    private int mTouchPosition;
    private SwipeMenuRecyclerLayout mTouchView;
    private OnSwipeListener mOnSwipeListener;

    private RecyclerSwipeMenuCreator mMenuCreator;
    private OnItemClickListener	mOnItemClickListener;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private OnMenuStateChangeListener mOnMenuStateChangeListener;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;
    private LinearLayoutManager layoutManager;
    private SwipeMenuRecyclerAdapter mAdapter;
    private List<View>	mFootViews;

    public SwipeMenuRecyclerListView(Context context) {
        super(context);
        init(context);
    }

    public SwipeMenuRecyclerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SwipeMenuRecyclerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        MAX_X = dp2px(MAX_X);
        MAX_Y = dp2px(MAX_Y);
        mTouchState = TOUCH_STATE_NONE;
        layoutManager = new LinearLayoutManager(context);
        setLayoutManager(layoutManager);
        mFootViews = new ArrayList<View>();
    }

    public int getLastVisiblePosition()
    {
    	if(layoutManager!=null)
    		return layoutManager.findLastVisibleItemPosition();
    	else 
    		return -1;
    }
    
    public int getFirstVisiblePosition()
    {
    	if(layoutManager!=null)
    		return layoutManager.findFirstVisibleItemPosition();
    	else 
    		return -1;
    }
    
    public void notifyDataSetChanged()
    {
    	if(mAdapter!=null)
    		mAdapter.notifyDataSetChangedUI();
    	mTouchView = null;
    }
    
    public void notifyItemChanged(int position)
    {
    	if(mAdapter!=null)
    		mAdapter.notifyItemChangedUI(position);
    	mTouchView = null;
    }
    
    public void notifyItemRemoved(int position)
    {
    	if(mAdapter!=null)
    		mAdapter.notifyItemRemovedUI(position);
    	mTouchView = null;
    }
    
    @Override
    public void setAdapter(Adapter adapter) {
    	mAdapter = new SwipeMenuRecyclerAdapter(getContext(), adapter) {
            @Override
            public void createMenu(RecyclerSwipeMenu menu) {
                if (mMenuCreator != null) {
                    mMenuCreator.create(menu);
                }
            }

            @Override
            public void onItemClick(SwipeMenuRecyclerView view, RecyclerSwipeMenu menu,
                                    int index) {
                boolean flag = false;
                if (mOnMenuItemClickListener != null) {
                    flag = mOnMenuItemClickListener.onMenuItemClick(
                            view.getPosition(), menu, index);
                }
                if (mTouchView != null && !flag) {
                    mTouchView.smoothCloseMenu();
                }
            }
        };
        super.setAdapter(mAdapter);
    }
    
    public void setSwipMenuHandle(SwipeMenuHandle handle)
    {
    	if(mAdapter!=null)
    		mAdapter.setSwipMenuHandle(handle);
    }

    public void setCloseInterpolator(Interpolator interpolator) {
        mCloseInterpolator = interpolator;
    }

    public void setOpenInterpolator(Interpolator interpolator) {
        mOpenInterpolator = interpolator;
    }

    public Interpolator getOpenInterpolator() {
        return mOpenInterpolator;
    }

    public Interpolator getCloseInterpolator() {
        return mCloseInterpolator;
    }
    
    public int pointToPosition(int x, int y,int firstPosition) {
        Rect frame = new Rect();

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return firstPosition + i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //在拦截处处理，在滑动设置了点击事件的地方也能swip，点击时又不能影响原来的点击事件
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                boolean handled = super.onInterceptTouchEvent(ev);
                mTouchState = TOUCH_STATE_NONE;
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY(),firstVisiblePosition);
                View view = getChildAt(mTouchPosition - firstVisiblePosition);

                //只在空的时候赋值 以免每次触摸都赋值，会有多个open状态
                if (view instanceof SwipeMenuRecyclerLayout) {
                    //如果有打开了 就拦截.
                    if (mTouchView != null && mTouchView.isOpen() && !inRangeOfView(mTouchView.getMenuView(), ev)) {
                        return true;
                    }
                    mTouchView = (SwipeMenuRecyclerLayout) view;
                    mTouchView.setSwipeDirection(mDirection);
                }
                //如果摸在另外个view
                if (mTouchView != null && mTouchView.isOpen() && view != mTouchView) {
                    handled = true;
                }

                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }
                return handled;
            case MotionEvent.ACTION_MOVE:
                float dy = Math.abs((ev.getY() - mDownY));
                float dx = Math.abs((ev.getX() - mDownX));
                if (Math.abs(dy) > MAX_Y || Math.abs(dx) > MAX_X) {
                    //每次拦截的down都把触摸状态设置成了TOUCH_STATE_NONE 只有返回true才会走onTouchEvent 所以写在这里就够了
                    if (mTouchState == TOUCH_STATE_NONE) {
                        if (Math.abs(dy) > MAX_Y) {
                            mTouchState = TOUCH_STATE_Y;
                        } else if (dx > MAX_X) {
                            mTouchState = TOUCH_STATE_X;
                            if (mOnSwipeListener != null) {
                                mOnSwipeListener.onSwipeStart(mTouchPosition);
                            }
                        }
                    }
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN && mTouchView == null)
            return super.onTouchEvent(ev);
        int action = ev.getAction();
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int oldPos = mTouchPosition;
                mDownX = ev.getX();
                mDownY = ev.getY();
                mTouchState = TOUCH_STATE_NONE;

                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY(),firstVisiblePosition);

                if (mTouchPosition == oldPos && mTouchView != null
                        && mTouchView.isOpen()) {
                    mTouchState = TOUCH_STATE_X;
                    mTouchView.onSwipe(ev);
                    return true;
                }

                View view = getChildAt(mTouchPosition - firstVisiblePosition);

                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                    mTouchView = null;
                    // return super.onTouchEvent(ev);
                    // try to cancel the touch event
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    onTouchEvent(cancelEvent);
                    if (mOnMenuStateChangeListener != null) {
                        mOnMenuStateChangeListener.onMenuClose(oldPos);
                    }
                    return true;
                }
                if (view instanceof SwipeMenuRecyclerLayout) {
                    mTouchView = (SwipeMenuRecyclerLayout) view;
                    mTouchView.setSwipeDirection(mDirection);
                }
                if (mTouchView != null) {
                    mTouchView.onSwipe(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //有些可能有header,要减去header再判断
                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY(),firstVisiblePosition);
                //如果滑动了一下没完全展现，就收回去，这时候mTouchView已经赋值，再滑动另外一个不可以swip的view
                //会导致mTouchView swip 。 所以要用位置判断是否滑动的是一个view
                if (!mTouchView.getSwipEnable() || mTouchPosition != mTouchView.getPosition()) {
                    break;
                }
                float dy = Math.abs((ev.getY() - mDownY));
                float dx = Math.abs((ev.getX() - mDownX));
                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                    }
//                    getSelector().setState(new int[]{0});
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                } else if (mTouchState == TOUCH_STATE_NONE) {
                    if (Math.abs(dy) > MAX_Y) {
                        mTouchState = TOUCH_STATE_Y;
                    } else if (dx > MAX_X) {
                        mTouchState = TOUCH_STATE_X;
                        if (mOnSwipeListener != null) {
                            mOnSwipeListener.onSwipeStart(mTouchPosition);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_X) {
                	
                    if (mTouchView != null) {
                        boolean isBeforeOpen = mTouchView.isOpen();
                        mTouchView.onSwipe(ev);
                        boolean isAfterOpen = mTouchView.isOpen();
                        
                        if (isBeforeOpen != isAfterOpen && mOnMenuStateChangeListener != null) {
                            if (isAfterOpen) {
                                mOnMenuStateChangeListener.onMenuOpen(mTouchPosition);
                            } else {
                                mOnMenuStateChangeListener.onMenuClose(mTouchPosition);
                            }
                        }
                        if (!isAfterOpen) {
                            mTouchPosition = -1;
                            mTouchView = null;
                        }
                    }
                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipeEnd(mTouchPosition);
                    }
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void smoothOpenMenu(int position) {
    	int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
    	int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        if (position >= firstVisiblePosition
                && position <= lastVisiblePosition) {
            View view = getChildAt(position - firstVisiblePosition);
            if (view instanceof SwipeMenuRecyclerLayout) {
                mTouchPosition = position;
                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                }
                mTouchView = (SwipeMenuRecyclerLayout) view;
                mTouchView.setSwipeDirection(mDirection);
                mTouchView.smoothOpenMenu();
            }
        }
    }

    public void smoothCloseMenu(){
        if (mTouchView != null && mTouchView.isOpen()) {
            mTouchView.smoothCloseMenu();
        }
    }
    
    public void setSwipEnable(boolean enable){
        if (mTouchView != null) {
            mTouchView.setSwipEnable(enable);
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    public void setMenuCreator(RecyclerSwipeMenuCreator menuCreator) {
        this.mMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(
            OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public void setOnMenuStateChangeListener(OnMenuStateChangeListener onMenuStateChangeListener) {
        mOnMenuStateChangeListener = onMenuStateChangeListener;
    }

    public static interface OnMenuItemClickListener {
        boolean onMenuItemClick(int position, RecyclerSwipeMenu menu, int index);
    }
    
    public static interface OnItemClickListener{
    	
    }

    public static interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }

    public static interface OnMenuStateChangeListener {
        void onMenuOpen(int position);

        void onMenuClose(int position);
    }

    public void setSwipeDirection(int direction) {
        mDirection = direction;
    }

    /**
     * 判断点击事件是否在某个view内
     *
     * @param view
     * @param ev
     * @return
     */
    public static boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getRawX() < x || ev.getRawX() > (x + view.getWidth()) || ev.getRawY() < y || ev.getRawY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }
    
    public void addFooterView(View footerView)
    {
    }
    
    public void setSelectionFromTop(int position, int offset)
    {
    	if(layoutManager!=null)
    		layoutManager.scrollToPositionWithOffset(position, offset);
    }
}
