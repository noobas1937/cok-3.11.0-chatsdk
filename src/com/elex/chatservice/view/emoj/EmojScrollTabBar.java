package com.elex.chatservice.view.emoj;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.elex.chatservice.R;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class EmojScrollTabBar extends RelativeLayout{

    private Context context;
    private HorizontalScrollView scrollView;
    private LinearLayout tabContainer;
    
    private List<ImageView> tabList = new ArrayList<ImageView>();
    private EmojScrollTabBarItemClickListener itemClickListener;
    
    private int tabWidth = 60;

    public EmojScrollTabBar(Context context) {
        this(context, null);
    }

    public EmojScrollTabBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EmojScrollTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.emoj_tab_bar, this);
        
        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
        tabContainer = (LinearLayout) findViewById(R.id.tab_container);
    }
    
    /**
     * add tab
     * @param icon
     */
    public void addTab(int icon){
        View tabView = View.inflate(context, R.layout.emoj_scroll_tab_item, null);
        ImageView imageView = (ImageView) tabView.findViewById(R.id.iv_icon);
        imageView.setImageResource(icon);
        initTab(tabView, imageView);
    }
    
    public void clearAllTab()
    {
    	if(tabContainer!=null)
    		tabContainer.removeAllViews();
    	if(tabList!=null)
    		tabList.clear();
    }
    
    private void initTab(View tabView,ImageView imageView)
    {
    	LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(ScaleUtil.dip2px(context, tabWidth), LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imgParams);
        tabContainer.addView(tabView);
        tabList.add(imageView);
        final int position = tabList.size() -1;
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(itemClickListener != null){
                    itemClickListener.onItemClick(position);
                }
            }
        });
    }
    
    public void addTab(String groupId){
        View tabView = View.inflate(context, R.layout.emoj_scroll_tab_item, null);
        ImageView imageView = (ImageView) tabView.findViewById(R.id.iv_icon);
        String url = EmojSubscribeManager.getEmojTabLocalPath(groupId);
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "url",url);
        Glide.with(context).load(url).dontAnimate().into(imageView);
        initTab(tabView,imageView);
    }
    
    /**
     * remove tab
     * @param position
     */
    public void removeTab(int position){
        tabContainer.removeViewAt(position);
        tabList.remove(position);
    }
    
    public void selectedTo(int position){
        scrollTo(position);
        for (int i = 0; i < tabList.size(); i++) {
            if (position == i) {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emoj_tab_selected));
            } else {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emoj_tab_nomal));
            }
        }
    }
    
    private void scrollTo(final int position){
        int childCount = tabContainer.getChildCount();
        if(position < childCount){
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    int mScrollX = tabContainer.getScrollX();
                    int childX = (int)ViewCompat.getX(tabContainer.getChildAt(position));

                    if(childX < mScrollX){
                        scrollView.scrollTo(childX,0);
                        return;
                    }

                    int childWidth = (int)tabContainer.getChildAt(position).getWidth();
                    int hsvWidth = scrollView.getWidth();
                    int childRight = childX + childWidth;
                    int scrollRight = mScrollX + hsvWidth;

                    if(childRight > scrollRight){
                        scrollView.scrollTo(childRight - scrollRight,0);
                        return;
                    }
                }
            });
        }
    }
    
    
    public void setTabBarItemClickListener(EmojScrollTabBarItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    
    
    public interface EmojScrollTabBarItemClickListener{
        void onItemClick(int position);
    }

}
