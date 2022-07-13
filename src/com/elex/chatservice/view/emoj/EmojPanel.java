package com.elex.chatservice.view.emoj;

import java.util.ArrayList;
import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.emoj.EmojPagerView.EmojPagerViewListener;
import com.elex.chatservice.view.emoj.EmojScrollTabBar.EmojScrollTabBarItemClickListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EmojPanel extends LinearLayout{
	
	public EmojMenuListener listener;
	
	private int emojiconColumns;
	private int bigEmojiconColumns;
	private final int defaultBigColumns = 4;
	private final int defaultColumns = 7;
    private EmojScrollTabBar tabBar;
    private EmojViewPagerIndicator indicatorView;
    private EmojPagerView pagerView;
    private ImageView emoj_subscrib_btn;
    private MyActionBarActivity activity;
    
    private List<EmojGroupEntity> emojGroupList = new ArrayList<EmojGroupEntity>();
    
    private boolean inited = false;
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public EmojPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public EmojPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public EmojPanel(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs){
		LayoutInflater.from(context).inflate(R.layout.emoj_panel, this);
		emojiconColumns = defaultColumns;
		bigEmojiconColumns = defaultBigColumns;
		
		pagerView = (EmojPagerView) findViewById(R.id.pager_view);
		indicatorView = (EmojViewPagerIndicator) findViewById(R.id.indicator_view);
		tabBar = (EmojScrollTabBar) findViewById(R.id.tab_bar);
		emoj_subscrib_btn = (ImageView) findViewById(R.id.emoj_subscrib_btn);
		if(ChatServiceController.expressionSubEnable)
		{
			emoj_subscrib_btn.setVisibility(View.VISIBLE);
		}
		else
		{
			emoj_subscrib_btn.setVisibility(View.GONE);
		}
		
		emoj_subscrib_btn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if(activity!=null)
				{
					ServiceInterface.showEmojSubscribActivity(activity);
					activity.hideSoftKeyBoard();
				}
			}
		});
		
	}
	
	public void init(List<EmojGroupEntity> groupEntities){
	    if(groupEntities == null || groupEntities.size() == 0){
	        return;
	    }
	    for(EmojGroupEntity groupEntity : groupEntities){
	        emojGroupList.add(groupEntity);
	        if(groupEntity.getIcon()>0)
	        	tabBar.addTab(groupEntity.getIcon());
	        else
	        	tabBar.addTab(groupEntity.getGroupId());
	    }
	    
	    pagerView.setPagerViewListener(new EmojPagerViewImpListener());
        pagerView.init(emojGroupList, emojiconColumns,bigEmojiconColumns);
        
        tabBar.setTabBarItemClickListener(new EmojScrollTabBarItemClickListener() {
            
            @Override
            public void onItemClick(int position) {
                pagerView.setGroupPostion(position);
            }
        });
	    
	}
	
	private void addEmojGroupEntity(EmojGroupEntity entity)
	{
		if(emojGroupList == null || entity == null)
			return;
		emojGroupList.add(entity);
        if(entity.getIcon()>0)
        	tabBar.addTab(entity.getIcon());
        else
        	tabBar.addTab(entity.getGroupId());
	}
	
	public void initEmoj(MyActionBarActivity activity)
	{
		this.activity = activity;
		if(emojGroupList!=null)
			emojGroupList.clear();
		if(tabBar!=null)
			tabBar.clearAllTab();
		if(indicatorView!=null)
			indicatorView.clearAllDotViews();
		List<EmojGroupEntity> entityList = new ArrayList<EmojGroupEntity>();
		EmojGroupEntity emojEntity = DefaultEmojDatas.getData();
		entityList.add(emojEntity);
		init(entityList);
		
//		EmojGroupEntity gifEntity = GifEmojDatas.getData();
//		addEmojGroup(gifEntity);
		
		List<EmojGroupEntity> emojGroupArr = EmojSubscribeManager.getInstance().getAviliableSubedEmojList();
		
		addEmojGroup(emojGroupArr);
	}
	
	
	/**
     * add emojicon group
     * @param groupEntity
     */
    public void addEmojGroup(EmojGroupEntity groupEntity){
        emojGroupList.add(groupEntity);
        pagerView.addEmojiconGroup(groupEntity, true);
        if(groupEntity.getIcon()>0)
        	tabBar.addTab(groupEntity.getIcon());
        else
        	tabBar.addTab(groupEntity.getGroupId());
    }
    
    /**
     * add emojicon group list
     * @param groupEntitieList
     */
    public void addEmojGroup(List<EmojGroupEntity> groupEntitieList){
        for(int i= 0; i < groupEntitieList.size(); i++){
        	EmojGroupEntity groupEntity = groupEntitieList.get(i);
            emojGroupList.add(groupEntity);
            pagerView.addEmojiconGroup(groupEntity, i == groupEntitieList.size()-1 ? true : false);
            if(groupEntity.getIcon()>0)
            	tabBar.addTab(groupEntity.getIcon());
            else
            	tabBar.addTab(groupEntity.getGroupId());
        }
        
    }
    
    /**
     * remove emojicon group
     * @param position
     */
    public void removeEmojGroup(int position){
        emojGroupList.remove(position);
        pagerView.removeEmojiconGroup(position);
        tabBar.removeTab(position);
    }
    
    public void setTabBarVisibility(boolean isVisible){
        if(!isVisible){
            tabBar.setVisibility(GONE);
        }else{
            tabBar.setVisibility(VISIBLE);
        }
    }
    
    public void setEmojMenuListener(EmojMenuListener listener){
        this.listener = listener;
    }
    
    public interface EmojMenuListener{
        /**
         * on emojicon clicked
         * @param emojicon
         */
        void onExpressionClicked(EmojIcon emojicon);
        /**
         * on delete image clicked
         */
        void onDeleteImageClicked();
    }
	
	
	private class EmojPagerViewImpListener implements EmojPagerView.EmojPagerViewListener {

        @Override
        public void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize) {
            indicatorView.init(groupMaxPageSize);
            indicatorView.updateIndicator(firstGroupPageSize);
            tabBar.selectedTo(0);
        }

        @Override
        public void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup) {
            indicatorView.updateIndicator(pagerSizeOfGroup);
            tabBar.selectedTo(groupPosition);
        }

        @Override
        public void onGroupInnerPagePostionChanged(int oldPosition, int newPosition) {
            indicatorView.selectTo(oldPosition, newPosition);
        }

        @Override
        public void onGroupPagePostionChangedTo(int position) {
            indicatorView.selectTo(position);
        }

        @Override
        public void onGroupMaxPageSizeChanged(int maxCount) {
            indicatorView.updateIndicator(maxCount);
        }

        @Override
        public void onDeleteImageClicked() {
            if(listener != null){
                listener.onDeleteImageClicked();
            }
        }

        @Override
        public void onExpressionClicked(EmojIcon emojicon) {
            if(listener != null){
                listener.onExpressionClicked(emojicon);
            }
        }
	    
	}
	
}
