package com.elex.im.ui.view.autoscroll;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.im.CokChannelDef;
import com.elex.im.core.model.Msg;

public class ScrollTextManager
{
	private static ScrollTextManager		_instance	= null;
	private ArrayList<ScrollTextObject>	handlers	= null;

	public static ScrollTextManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (ScrollTextManager.class)
			{
				if (_instance == null)
				{
					_instance = new ScrollTextManager();
				}
			}
		}
		return _instance;
	}
	
	private ScrollTextManager()
	{
		handlers = new ArrayList<ScrollTextObject>();
		ArrayList<Integer> hornChannelTypes = CokChannelDef.getHornChannelTypes();
		for (Integer channelType : hornChannelTypes)
		{
			handlers.add(new ScrollTextObject(channelType));
		}
	}

	public ScrollTextObject getHandler(int channelType)
	{
		for (ScrollTextObject scrollTextHandler : handlers)
		{
			if(scrollTextHandler.channelType == channelType)
				return scrollTextHandler;
		}
		return null;
	}

	public void setHornLayout(RelativeLayout layout, TextView view, int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).setHornLayout(layout, view);
	}

	public void showScrollText(Msg msgItem, ScrollText scrollTextView, TextView hornNameTextView, RelativeLayout layout, int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).showScrollText(msgItem, scrollTextView, hornNameTextView, layout);
	}

	public int getScrollQueueLength(int channelType)
	{
		if(getHandler(channelType) == null)
			return 0;
		return getHandler(channelType).getScrollQueueLength();
	}

	public Msg getNextText(int channelType)
	{
		if(getHandler(channelType) == null)
			return null;
		return getHandler(channelType).getNextText();
	}

	public void pop(int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).pop();
	}

	public void clear(int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).clear();
	}

	public void push(Msg msgItem, int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).push(msgItem);
	}

	public void shutDownScrollText(ScrollText scrollTextView, int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).shutDownScrollText(scrollTextView);
	}

	public void hideScrollLayout(int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).hideScrollLayout();
	}

	public void setHornName(String name, int channelType)
	{
		if(getHandler(channelType) != null)
			getHandler(channelType).setHornName(name);
	}
	
	public class ScrollTextObject
	{
		public int				channelType;
		private List<Msg>		mScrollQueue		= null;
		private TextView		mHornNameTextView	= null;
		private RelativeLayout	horn_layout			= null;
		/** 为节省计算而设置的，本次登录期间，遍历到了就不用再遍历了 */
		public boolean			oldHornMsgPushed	= false;

		private ScrollTextObject(int channelType)
		{
			this.channelType = channelType;
			mScrollQueue = new ArrayList<Msg>();
		}
		
		public void setHornLayout(RelativeLayout layout, TextView view)
		{
			horn_layout = layout;
			mHornNameTextView = view;
		}

		public void showScrollText(Msg msgItem, ScrollText scrollTextView, TextView hornNameTextView, RelativeLayout layout)
		{
			if (layout != null && layout.getVisibility() != View.VISIBLE)
				layout.setVisibility(View.VISIBLE);
			
			setHornLayout(layout, hornNameTextView);
			
			if (mScrollQueue != null)
			{
				mScrollQueue.add(msgItem);
				scrollTextView.scrollNext();
			}
		}

		public int getScrollQueueLength()
		{
			if (mScrollQueue != null)
				return mScrollQueue.size();
			return 0;
		}
		
		public Msg getNextText()
		{
			if (mScrollQueue != null && !mScrollQueue.isEmpty())
			{
				return mScrollQueue.get(0);
			}
			return null;
		}
		
		public void pop()
		{
			if (mScrollQueue != null && !mScrollQueue.isEmpty() && mScrollQueue.size() > 1)
				mScrollQueue.remove(0);
		}

		public void clear()
		{
			if (mScrollQueue != null && !mScrollQueue.isEmpty())
				mScrollQueue.clear();
		}

		public void push(Msg msgItem)
		{
			if (mScrollQueue != null && !mScrollQueue.contains(msgItem))
				mScrollQueue.add(msgItem);
		}

		public void shutDownScrollText(ScrollText scrollTextView)
		{
			if (mScrollQueue != null && !mScrollQueue.isEmpty())
				mScrollQueue.clear();
			scrollTextView.stopScroll();
		}

		public void hideScrollLayout()
		{
			if (horn_layout != null)
				horn_layout.setVisibility(View.GONE);
		}

		public void setHornName(String name)
		{
			if (mHornNameTextView != null)
				mHornNameTextView.setText(name);
		}
	}
}
