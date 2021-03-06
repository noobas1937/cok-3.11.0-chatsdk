package com.elex.chatservice.view.autoscroll;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.db.DBDefinition;

public class ScrollText extends TextView implements Runnable
{
	private int					currentScrollX	= 0;
	private boolean				isWorking		= false;
	private boolean				isMeasured		= false;
	private int					textWidth;
	private int					scrollCount		= 0;
	private static final int	MAX_SCROLL_NUM	= 2;
	private static final int	SCROLL_SPEED	= 2;
	private static final int	SCROLL_INTERVAL	= 5;
	private String				oldText			= "";
	private boolean				isOldLongText	= false;
	private String				currentText		= "";
	private int 				channelType		= DBDefinition.CHANNEL_TYPE_COUNTRY;

	public ScrollText(Context context)
	{
		super(context);
	}
	
	public void setChannelType(int channelType)
	{
		this.channelType = channelType;
	}

	public ScrollText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ScrollText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (!isMeasured)
		{
			setText(currentText);
			oldText = currentText;
			getTextWidth();
			isMeasured = true;
			int textViewWidth = getWidth();
			if (textWidth > getWidth())
			{
				currentScrollX = -textViewWidth;
				scrollTo(-textViewWidth, 0);
				isOldLongText = true;
			}
			else
			{
				isOldLongText = false;
			}
			setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 获取文字宽度
	 */
	private void getTextWidth()
	{
		Paint paint = this.getPaint();
		String str = this.getText().toString();
		textWidth = (int) paint.measureText(str);
	}

	@Override
	public void run()
	{
		int textViewWidth = getWidth();
		currentScrollX += SCROLL_SPEED;// 滚动速度
		if (textWidth <= textViewWidth && currentScrollX < textViewWidth * MAX_SCROLL_NUM)
		{
			postDelayed(this, SCROLL_INTERVAL);
			return;
		}
		if ((scrollCount >= MAX_SCROLL_NUM && ScrollTextManager.getInstance().getScrollQueueLength(channelType) > 1)
				|| (textWidth <= textViewWidth && currentScrollX >= textViewWidth * MAX_SCROLL_NUM))
		{
			isWorking = false;
			scrollNext();
			return;
		}

		if (!isWorking)
		{
			return;
		}
		if (currentScrollX >= textWidth)
		{
			scrollTo(-textViewWidth, 0);
			currentScrollX = -textViewWidth;
			scrollCount++;
		}

		scrollTo(currentScrollX, 0);
		postInvalidate();
		postDelayed(this, SCROLL_INTERVAL);
	}

	public void startScroll()
	{
		isWorking = true;
		isMeasured = false;
		scrollCount = 0;
		currentScrollX = 0;
		this.removeCallbacks(this);
		post(this);
	}

	public void stopScroll()
	{
		isWorking = false;
		ScrollTextManager.getInstance().hideScrollLayout(channelType);
	}

	// 从头开始滚动
	public void reStart()
	{
		setVisibility(View.VISIBLE);
		if (textWidth > getWidth())
		{
			currentScrollX = -this.getWidth();
			startScroll();
		}
	}

	public void scrollNext()
	{
		ScrollTextManager.getInstance().pop(channelType);
		if (ScrollTextManager.getInstance().getScrollQueueLength(channelType) <= 0)
			return;
		
		MsgItem msgItem = ScrollTextManager.getInstance().getNextText(channelType);

		if (msgItem != null && StringUtils.isNotEmpty(msgItem.msg))
		{
			String name = "";
			if (StringUtils.isNotEmpty(msgItem.getASN()))
				name += "(" + msgItem.getASN() + ")";
			if (StringUtils.isNotEmpty(msgItem.getName()))
				name += msgItem.getName();
			else
				name += msgItem.uid;
			name += ":";
			if (StringUtils.isNotEmpty(name) && !name.equals(":"))
			{
				ScrollTextManager.getInstance().setHornName(name,channelType);
			}

			if (msgItem.isSelfMsg())
			{
				currentText = msgItem.msg;
			}
			else
			{
				if(msgItem.isStealFailedMessage() && StringUtils.isNotEmpty(msgItem.attachmentId))
				{
					String msgStr = msgItem.parseExtraDialogMsg();
					currentText = StringUtils.isNotEmpty(msgStr) ? msgStr : msgItem.msg;
				}
				else
				{
					if (msgItem.canShowTranslateMsg())
					{
						if (StringUtils.isNotEmpty(msgItem.translateMsg))
							currentText = msgItem.translateMsg;
						else
							currentText = msgItem.msg;
					}
					else
					{
						currentText = msgItem.msg;
					}
				}
			}
			if (StringUtils.isEmpty(oldText))
				setText("");
			else
			{
				if (!isOldLongText)
					setText(oldText + " ");
				else
					setText("");
			}
			requestFocus();
			startScroll();
		}
		else
		{
			if(ScrollTextManager.getInstance().getScrollQueueLength(channelType) == 1)
			{
				ScrollTextManager.getInstance().clear(channelType);
				return;
			}
			else
				scrollNext();
		}
	}
}