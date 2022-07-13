package com.elex.chatservice.view.emoj;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.elex.chatservice.R;
import com.elex.chatservice.util.LogUtil;

public class EmojPagerView extends ViewPager{

    private Context context;
    private List<EmojGroupEntity> groupEntities;
    private List<EmojIcon> totalEmojiconList = new ArrayList<EmojIcon>();
    
    private PagerAdapter pagerAdapter;
    
    private int emojiconRows = 3;
    private int emojiconColumns = 7;
    
    private int bigEmojiconRows = 2;
    private int bigEmojiconColumns = 4;
    
    private int firstGroupPageSize;
    
    private int maxPageCount;
    private int previousPagerPosition;
	private EmojPagerViewListener pagerViewListener;
    private List<View> viewpages; 

    public EmojPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public EmojPagerView(Context context) {
        this(context, null);
    }
    
    
    public void init(List<EmojGroupEntity> emojiconGroupList, int emijiconColumns, int bigEmojiconColumns){
        if(emojiconGroupList == null){
            throw new RuntimeException("emojiconGroupList is null");
        }
        
        this.groupEntities = emojiconGroupList;
        this.emojiconColumns = emijiconColumns;
        this.bigEmojiconColumns = bigEmojiconColumns;
        
        maxPageCount = 0;
        viewpages = new ArrayList<View>();
        for(int i = 0; i < groupEntities.size(); i++){
            EmojGroupEntity group = groupEntities.get(i);
            List<EmojIcon> groupEmojicons = group.getDetails();
            totalEmojiconList.addAll(groupEmojicons);
            List<View> gridViews = getGroupGridViews(group);
            if(i == 0){
                firstGroupPageSize = gridViews.size();
            }
            maxPageCount = Math.max(gridViews.size(), maxPageCount);
            viewpages.addAll(gridViews);
        }
        
        pagerAdapter = new EmojPagerAdapter(viewpages);
        setAdapter(pagerAdapter);
        addOnPageChangeListener(new EmojPagerChangeListener());
        
        if(pagerViewListener != null){
            pagerViewListener.onPagerViewInited(maxPageCount, firstGroupPageSize);
        }
    }
    
    public void setPagerViewListener(EmojPagerViewListener pagerViewListener){
    	this.pagerViewListener = pagerViewListener;
    }
    
    
    /**
     * set emojicon group position
     * @param position
     */
    public void setGroupPostion(int position){
    	if (getAdapter() != null && position >= 0 && position < groupEntities.size()) {
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += getPageSize(groupEntities.get(i));
            }
            setCurrentItem(count);
        }
    }
    
    /**
     * get emojicon group gridview list
     * @param groupEntity
     * @return
     */
    public List<View> getGroupGridViews(EmojGroupEntity groupEntity){
        List<EmojIcon> emojiconList = groupEntity.getDetails();
        int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
        EmojIcon.Type emojiType = groupEntity.getType();
        if(emojiType == EmojIcon.Type.BIG_EMOJ){
            itemSize = bigEmojiconColumns * bigEmojiconRows;
        }
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;   
        List<View> views = new ArrayList<View>();
        if(pageSize == 0)
        {
        	View view = View.inflate(context, android.R.layout.simple_list_item_1, null);
        	views.add(view);
        }
        else
        {
        	for(int i = 0; i < pageSize; i++){
                View view = View.inflate(context, R.layout.emoj_icon_gridview, null);
                GridView gv = (GridView) view.findViewById(R.id.gridview);
                if(emojiType == EmojIcon.Type.BIG_EMOJ){
                    gv.setNumColumns(bigEmojiconColumns);
                }else{
                    gv.setNumColumns(emojiconColumns);
                }
                List<EmojIcon> list = new ArrayList<EmojIcon>();
                if(i != pageSize -1){
                    list.addAll(emojiconList.subList(i * itemSize, (i+1) * itemSize));
                }else{
                    list.addAll(emojiconList.subList(i * itemSize, totalSize));
                }
                if(emojiType != EmojIcon.Type.BIG_EMOJ){
                    EmojIcon deleteIcon = new EmojIcon();
                    deleteIcon.setEmojiText(EmojUtils.DELETE_KEY);
                    list.add(deleteIcon);
                }
                final EmojGridAdapter gridAdapter = new EmojGridAdapter(context, 1, list, emojiType);
                gv.setAdapter(gridAdapter);
                gv.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        EmojIcon emojicon = gridAdapter.getItem(position);
                        if(pagerViewListener != null){
                            String emojiText = emojicon.getEmojiText();
                            if(emojiText != null && emojiText.equals(EmojUtils.DELETE_KEY)){
                                pagerViewListener.onDeleteImageClicked();
                            }else{
                                pagerViewListener.onExpressionClicked(emojicon);
                            }
                            
                        }
                        
                    }
                });
                
                views.add(view);
            }
        }
        
        return views;
    }
    

    /**
     * add emojicon group
     * @param groupEntity
     */
    public void addEmojiconGroup(EmojGroupEntity groupEntity, boolean notifyDataChange) {
        int pageSize = getPageSize(groupEntity);
        if(pageSize > maxPageCount){
            maxPageCount = pageSize;
            if(pagerViewListener != null && pagerAdapter != null){
                pagerViewListener.onGroupMaxPageSizeChanged(maxPageCount);
            }
        }
        viewpages.addAll(getGroupGridViews(groupEntity));
        if(pagerAdapter != null && notifyDataChange){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * remove emojicon group
     * @param position
     */
    public void removeEmojiconGroup(int position){
        if(position > groupEntities.size() - 1){
            return;
        }
        if(pagerAdapter != null){
            pagerAdapter.notifyDataSetChanged();
        }
    }
    
    public void clearAllEmojGroup()
    {
    	maxPageCount = 0;
    	if(viewpages!=null)
    		viewpages.clear();
    }
    
    /**
     * get size of pages
     * @param emojiconList
     * @return
     */
    private int getPageSize(EmojGroupEntity groupEntity) {
        List<EmojIcon> emojiconList = groupEntity.getDetails();
        int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "totalSize",totalSize);
        EmojIcon.Type emojiType = groupEntity.getType();
        if(emojiType == EmojIcon.Type.BIG_EMOJ){
            itemSize = bigEmojiconColumns * bigEmojiconRows;
        }
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;   
        return pageSize;
    }
    
    private class EmojPagerChangeListener implements OnPageChangeListener{
        @Override
        public void onPageSelected(int position) {
        	int endSize = 0;
        	int groupPosition = 0;
            for(EmojGroupEntity groupEntity : groupEntities){
            	int groupPageSize = getPageSize(groupEntity);
            	//if the position is in current group
            	if(endSize + groupPageSize > position){
            		//this is means user swipe to here from previous page
            		if(previousPagerPosition - endSize < 0){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPageSize);
            				pagerViewListener.onGroupPagePostionChangedTo(0);
            			}
            			break;
            		}
            		//this is means user swipe to here from back page
            		if(previousPagerPosition - endSize >= groupPageSize){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPageSize);
            				pagerViewListener.onGroupPagePostionChangedTo(position - endSize);
            			}
            			break;
            		}
            		
            		//page changed
            		if(pagerViewListener != null){
            			pagerViewListener.onGroupInnerPagePostionChanged(previousPagerPosition-endSize, position-endSize);
            		}
            		break;
            		
            	}
            	groupPosition++;
            	endSize += groupPageSize;
            }
            
            previousPagerPosition = position;
        }
        
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }
    
    
    
    public interface EmojPagerViewListener{
        /**
         * pagerview initialized
         * @param groupMaxPageSize --max pages size
         * @param firstGroupPageSize-- size of first group pages
         */
        void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize);
        
    	/**
    	 * group position changed
    	 * @param groupPosition--group position
    	 * @param pagerSizeOfGroup--page size of group
    	 */
    	void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup);
    	/**
    	 * page position changed
    	 * @param oldPosition
    	 * @param newPosition
    	 */
    	void onGroupInnerPagePostionChanged(int oldPosition, int newPosition);
    	
    	/**
    	 * group page position changed
    	 * @param position
    	 */
    	void onGroupPagePostionChangedTo(int position);
    	
    	/**
    	 * max page size changed
    	 * @param maxCount
    	 */
    	void onGroupMaxPageSizeChanged(int maxCount);
    	
    	void onDeleteImageClicked();
    	void onExpressionClicked(EmojIcon emojicon);
    	
    }

}
