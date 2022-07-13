package com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview;

import com.elex.chatservice.util.LogUtil;
import com.lee.pullrefresh.ui.FooterLoadingLayout;
import com.lee.pullrefresh.ui.HeaderLoadingLayout2;
import com.lee.pullrefresh.ui.ILoadingLayout.State;
import com.lee.pullrefresh.ui.LoadingLayout;
import com.lee.pullrefresh.ui.PullToRefreshBase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PullToRefreshSwipeRecyclerView extends PullToRefreshBase<SwipeMenuRecyclerListView> {
    
    /**SwipeMenuRecyclerListView*/
    private SwipeMenuRecyclerListView mListView;
    /**用于滑到底部自动加载的Footer*/
    private LoadingLayout mLoadMoreFooterLayout;
    /**滚动的监听器*/
    private OnScrollListener mScrollListener;
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public PullToRefreshSwipeRecyclerView(Context context) {
        this(context, null);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public PullToRefreshSwipeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public PullToRefreshSwipeRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setPullLoadEnabled(false);
    }

    @Override
    protected SwipeMenuRecyclerListView createRefreshableView(Context context, AttributeSet attrs) {
        SwipeMenuRecyclerListView listView = new SwipeMenuRecyclerListView(context);
        mListView = listView;
        
        mScrollListener = new OnScrollListener()
		{
        	
        	@Override
        	public void onScrollStateChanged(RecyclerView recyclerview, int scrollState)
        	{
        		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "scrollState",scrollState,"isScrollLoadEnabled()",isScrollLoadEnabled(),"hasMoreData()",hasMoreData());
        		if (isScrollLoadEnabled() && hasMoreData()) {
                    if (scrollState == 0 || scrollState == 2) {
                        if (isReadyForPullUp()) {
                            startLoading();
                        }
                    }
                }
        		super.onScrollStateChanged(recyclerview, scrollState);
        	}
        	
        	@Override
        	public void onScrolled(RecyclerView recyclerview, int i, int j)
        	{
        		super.onScrolled(recyclerview, i, j);
        	}
		};
        listView.addOnScrollListener(mScrollListener);
        
        return listView;
    }
    
    /**
     * 设置是否有更多数据的标志
     * 
     * @param hasMoreData true表示还有更多的数据，false表示没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {
            if (null != mFooterLayout) {
            	mFooterLayout.setState(State.NO_MORE_DATA);
            }
        }
    }

    /**
     * 设置滑动的监听器
     * 
     * @param l 监听器
     */
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }
    
    @Override
    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected void startLoading() {
        super.startLoading();
    }
    
    @Override
    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();
    }
    
    @Override
    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
    	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "scrollLoadEnabled",scrollLoadEnabled);
        super.setScrollLoadEnabled(scrollLoadEnabled);
        
        if (scrollLoadEnabled) {
            mFooterLayout.show(true);
        } else {
        	mFooterLayout.show(false);
        }
    }
    
    @Override
    public LoadingLayout getFooterLoadingLayout() {
        return super.getFooterLoadingLayout();
    }

    
    @Override
    protected LoadingLayout createHeaderLoadingLayout(Context context, AttributeSet attrs) {
        return new HeaderLoadingLayout2(context);
    }
    
    public void setLoadingText(String text)
    {
    	mHeaderLayout.setLoadingText(text);
    }

    public void setLoadingTextSizeRatio(double textRatio)
    {
    	mHeaderLayout.setLoadingTextSizeRatio(textRatio);
    }
    
    /**
     * 表示是否还有更多数据
     * 
     * @return true表示还有更多数据
     */
    private boolean hasMoreData() {
        if ((null != mFooterLayout) && (mFooterLayout.getState() == State.NO_MORE_DATA)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 判断第一个child是否完全显示出来
     * 
     * @return true完全显示出来，否则false
     */
    private boolean isFirstItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.getItemCount() == 0) {
            return true;
        }

        int mostTop = (mListView.getChildCount() > 0) ? mListView.getChildAt(0).getTop() : 0;
        if (mostTop >= 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断最后一个child是否完全显示出来
     * 
     * @return true完全显示出来，否则false
     */
    private boolean isLastItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.getItemCount() == 0) {
            return true;
        }

        final int lastItemPosition = adapter.getItemCount() - 1;
        final int lastVisiblePosition = mListView.getLastVisiblePosition();

        /**
         * This check should really just be: lastVisiblePosition == lastItemPosition, but SwipeMenuRecyclerListView
         * internally uses a FooterView which messes the positions up. For me we'll just subtract
         * one to account for it and rely on the inner condition which checks getBottom().
         */
        if (lastVisiblePosition >= lastItemPosition - 1) {
            final int childIndex = lastVisiblePosition - mListView.getFirstVisiblePosition();
            final int childCount = mListView.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = mListView.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= mListView.getBottom();
            }
        }

        return false;
    }
    
    public void setLanguage(String loading)
    {
    	FooterLoadingLayout.setLanguage(loading);
    }
}
