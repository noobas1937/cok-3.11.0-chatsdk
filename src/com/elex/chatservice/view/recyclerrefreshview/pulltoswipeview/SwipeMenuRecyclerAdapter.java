package com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuRecyclerView.OnSwipeItemClickListener;

public class SwipeMenuRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
		OnSwipeItemClickListener {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    private Context mContext;
    private SwipeMenuRecyclerListView.OnMenuItemClickListener onMenuItemClickListener;
    private SwipeMenuHandle menuHandle;

    public SwipeMenuRecyclerAdapter(Context context, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        mAdapter = adapter;
        mContext = context;
    }
    
    public void createMenu(RecyclerSwipeMenu menu) {
        // Test Code
        RecyclerSwipeMenuItem item = new RecyclerSwipeMenuItem(mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(Color.GRAY));
        item.setWidth(300);
        menu.addMenuItem(item);

        item = new RecyclerSwipeMenuItem(mContext);
        item.setTitle("Item 2");
        item.setBackground(new ColorDrawable(Color.RED));
        item.setWidth(300);
        menu.addMenuItem(item);
    }

    @Override
    public void onItemClick(SwipeMenuRecyclerView view, RecyclerSwipeMenu menu, int index) {
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getPosition(), menu,
                    index);
        }
    }

    public void setOnSwipeItemClickListener(
    		SwipeMenuRecyclerListView.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }
    
    public void setSwipMenuHandle(SwipeMenuHandle handle)
    {
    	menuHandle = handle;
    }


    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

	@Override
	public int getItemCount()
	{
		return mAdapter.getItemCount();
	}
	

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		mAdapter.onBindViewHolder(holder, position);
        SwipeMenuRecyclerLayout layout = (SwipeMenuRecyclerLayout) holder.itemView;
        if(menuHandle!=null)
        	layout.setVisibleMenu(menuHandle.getCurrentItemMenuTypeList(position));
        layout.setPosition(position);
        layout.closeMenu();
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int viewType)
	{
		ViewHolder holder  = mAdapter.onCreateViewHolder(viewgroup, viewType);
        RecyclerSwipeMenu menu = new RecyclerSwipeMenu(mContext);
        menu.setViewType(viewType);
        createMenu(menu);
        SwipeMenuRecyclerView menuView = new SwipeMenuRecyclerView(menu,
                (SwipeMenuRecyclerListView) viewgroup);
        menuView.setOnSwipeItemClickListener(this);
        SwipeMenuRecyclerListView listView = (SwipeMenuRecyclerListView) viewgroup;
        SwipeMenuRecyclerLayout layout = new SwipeMenuRecyclerLayout(holder.itemView, menuView,
                listView.getCloseInterpolator(),
                listView.getOpenInterpolator());
        return new SwipeMenuLayoutHolder(layout,holder);
	}
	
	public void notifyDataSetChangedUI()
	{
		notifyDataSetChanged();
		if(mAdapter!=null)
			mAdapter.notifyDataSetChanged();
	}
	
	public void notifyItemChangedUI(int position)
	{
		notifyItemChanged(position);
	}
	
	public void notifyItemRemovedUI(int position)
	{
		notifyItemRemoved(position);
		if(position!=getItemCount())
			notifyItemRangeChanged(position, getItemCount() - position);
	}

}
