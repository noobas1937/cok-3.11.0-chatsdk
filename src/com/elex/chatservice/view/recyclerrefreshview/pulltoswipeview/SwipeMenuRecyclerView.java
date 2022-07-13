package com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview;

import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SwipeMenuRecyclerView extends LinearLayout implements OnClickListener
{

	private SwipeMenuRecyclerListView	mListView;
	private SwipeMenuRecyclerLayout		mLayout;
	private RecyclerSwipeMenu			mMenu;
	private OnSwipeItemClickListener	onItemClickListener;
	private int							position;
	private List<Integer>				visibleMenuTypeList;
	private SparseArray<View>			menuViewMap;

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public void setVisibleMenu(int[] visibleType)
	{
		if(visibleType == null)
			return;
		if (visibleMenuTypeList != null)
			visibleMenuTypeList.clear();
		else
			visibleMenuTypeList = new ArrayList<Integer>();
		for (int i = 0; i < visibleType.length; i++)
		{
			visibleMenuTypeList.add(visibleType[i]);
		}

		for (int i = 0; i < menuViewMap.size(); i++)
		{
			View menu = menuViewMap.valueAt(i);
			if (menu != null)
			{
				int menuType = menuViewMap.keyAt(i);
				if (visibleMenuTypeList.contains(menuType))
				{
					if (menu.getVisibility() != View.VISIBLE)
						menu.setVisibility(View.VISIBLE);
				}
				else
				{
					if (menu.getVisibility() != View.GONE)
						menu.setVisibility(View.GONE);
				}
			}
		}

	}

	public SwipeMenuRecyclerView(RecyclerSwipeMenu menu, SwipeMenuRecyclerListView listView)
	{
		super(menu.getContext());
		mListView = listView;
		mMenu = menu;
		visibleMenuTypeList = new ArrayList<Integer>();
		menuViewMap = new SparseArray<View>();
		List<RecyclerSwipeMenuItem> items = menu.getMenuItems();
		int id = 0;
		for (RecyclerSwipeMenuItem item : items)
		{
			addItem(item, id++);
			if (!visibleMenuTypeList.contains(item.getMenuType()))
				visibleMenuTypeList.add(item.getMenuType());
		}
	}

	private void addItem(RecyclerSwipeMenuItem item, int id)
	{
		LayoutParams params = new LayoutParams(item.getWidth(),
				LayoutParams.MATCH_PARENT);
		LinearLayout parent = new LinearLayout(getContext());
		parent.setId(id);
		parent.setGravity(Gravity.CENTER);
		parent.setOrientation(LinearLayout.VERTICAL);
		parent.setLayoutParams(params);
		parent.setBackgroundDrawable(item.getBackground());
		parent.setOnClickListener(this);
		addView(parent);
		menuViewMap.put(item.getMenuType(), parent);

		if (item.getIcon() != null)
		{
			parent.addView(createIcon(item));
		}
		if (!TextUtils.isEmpty(item.getTitle()))
		{
			parent.addView(createTitle(item));
		}

	}

	private ImageView createIcon(RecyclerSwipeMenuItem item)
	{
		ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(item.getIcon());
		return iv;
	}

	private TextView createTitle(RecyclerSwipeMenuItem item)
	{
		TextView tv = new TextView(getContext());
		tv.setText(item.getTitle());
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(item.getTitleSize());
		tv.setTextColor(item.getTitleColor());
		return tv;
	}

	@Override
	public void onClick(View v)
	{
		if (onItemClickListener != null && mLayout.isOpen())
		{
			onItemClickListener.onItemClick(this, mMenu, v.getId());
		}
	}

	public OnSwipeItemClickListener getOnSwipeItemClickListener()
	{
		return onItemClickListener;
	}

	public void setOnSwipeItemClickListener(OnSwipeItemClickListener onItemClickListener)
	{
		this.onItemClickListener = onItemClickListener;
	}

	public void setLayout(SwipeMenuRecyclerLayout mLayout)
	{
		this.mLayout = mLayout;
	}

	public static interface OnSwipeItemClickListener
	{
		void onItemClick(SwipeMenuRecyclerView view, RecyclerSwipeMenu menu, int index);

		void createMenu(RecyclerSwipeMenu menu);
	}
}
