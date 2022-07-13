package com.elex.chatservice.view.danmu;

import java.util.ArrayList;
import java.util.List;
import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.DanmuMenuInfo;
import com.elex.chatservice.util.ScaleUtil;

import android.R.color;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DanmuScrollMenuBar extends RelativeLayout
{

	private Context							context;
	private HorizontalScrollView			scrollView;
	private LinearLayout					tabContainer;

	private List<TextView>					tabList		= new ArrayList<TextView>();
	private DanmuMenuBarItemClickListener	itemClickListener;

	private int								tabWidth	= 33;
	private int								tabHeight	= 20;

	public DanmuScrollMenuBar(Context context)
	{
		this(context, null);
	}

	public DanmuScrollMenuBar(Context context, AttributeSet attrs, int defStyle)
	{
		this(context, attrs);
	}

	public DanmuScrollMenuBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.danmu_menu_bar, this);

		scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
		tabContainer = (LinearLayout) findViewById(R.id.tab_container);
	}

	public void clearAllTab()
	{
		if (tabContainer != null)
			tabContainer.removeAllViews();
		if (tabList != null)
			tabList.clear();
	}

	@SuppressWarnings("deprecation")
	public void refreshMenuSelectedStatus(int position)
	{
		if (tabList != null)
		{
			for (int i = 0; i < tabList.size(); i++)
			{
				TextView textView = tabList.get(i);
				if (textView != null)
				{
					int width = (int) (ScaleUtil.dip2px(context, 2) * ConfigManager.scaleRatioButton);
					if (i == position)
					{
						GradientDrawable drawable = (GradientDrawable) textView.getBackground();
						drawable.setStroke(width, Color.BLACK);
						textView.setBackgroundDrawable(drawable);
					}
					else
					{
						GradientDrawable drawable = (GradientDrawable) textView.getBackground();
						drawable.setStroke(width, Color.TRANSPARENT);
						textView.setBackgroundDrawable(drawable);
					}
				}
			}
		}
	}

	private void initTab(View tabView, TextView textView)
	{
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
				(int) (ScaleUtil.dip2px(context, tabWidth) * ConfigManager.scaleRatioButton),
				(int) (ScaleUtil.dip2px(context, tabHeight) * ConfigManager.scaleRatioButton));
		textView.setLayoutParams(textParams);
		tabContainer.addView(tabView);
		tabList.add(textView);
		final int position = tabList.size() - 1;
		textView.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (itemClickListener != null)
				{
					itemClickListener.onItemClick(position);
					refreshMenuSelectedStatus(position);
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	public void addMenu(DanmuMenuInfo info)
	{
		if (info == null)
			return;
		View tabView = View.inflate(context, R.layout.danmu_scroll_menu_item, null);
		TextView menu_text = (TextView) tabView.findViewById(R.id.menu_text);
		int color = info.getBackgroundColor();
		String text = info.getText();
		menu_text.setText(text);
		GradientDrawable drawable = new GradientDrawable();
		drawable.setColor(color);
		drawable.setStroke(0, Color.TRANSPARENT);
		menu_text.setBackgroundDrawable(drawable);
		initTab(tabView, menu_text);
	}

	public void removeTab(int position)
	{
		tabContainer.removeViewAt(position);
		tabList.remove(position);
	}

	public void selectedTo(int position)
	{
		scrollTo(position);
		for (int i = 0; i < tabList.size(); i++)
		{
			if (position == i)
			{
				tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emoj_tab_selected));
			}
			else
			{
				tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emoj_tab_nomal));
			}
		}
	}

	private void scrollTo(final int position)
	{
		int childCount = tabContainer.getChildCount();
		if (position < childCount)
		{
			scrollView.post(new Runnable()
			{
				@Override
				public void run()
				{
					int mScrollX = tabContainer.getScrollX();
					int childX = (int) ViewCompat.getX(tabContainer.getChildAt(position));

					if (childX < mScrollX)
					{
						scrollView.scrollTo(childX, 0);
						return;
					}

					int childWidth = (int) tabContainer.getChildAt(position).getWidth();
					int hsvWidth = scrollView.getWidth();
					int childRight = childX + childWidth;
					int scrollRight = mScrollX + hsvWidth;

					if (childRight > scrollRight)
					{
						scrollView.scrollTo(childRight - scrollRight, 0);
						return;
					}
				}
			});
		}
	}

	public void setMenuBarItemClickListener(DanmuMenuBarItemClickListener itemClickListener)
	{
		this.itemClickListener = itemClickListener;
	}

	public interface DanmuMenuBarItemClickListener
	{
		void onItemClick(int position);
	}

}
