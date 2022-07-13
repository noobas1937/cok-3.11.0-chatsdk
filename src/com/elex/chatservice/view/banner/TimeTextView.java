package com.elex.chatservice.view.banner;


import com.elex.chatservice.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义倒计时文本控件
 * 
 * @author Administrator
 * 
 */
public class TimeTextView extends TextView implements Runnable {
	Paint mPaint; 
	private int[] times;
	private long mday, mhour, mmin, msecond;
	private boolean run = false;

	public TimeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
	}

	public TimeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	public TimeTextView(Context context) {
		super(context);
	}

	public int[] getTimes() {
		return times;
	}

	public void setTimes(int[] times) {
		this.times = times;
		mday = times[0];
		mhour = times[1];
		mmin = times[2];
		msecond = times[3];
	}

	/**
	 * 倒计时计算
	 */
	private void ComputeTime() {
		msecond--;
		if (msecond < 0) {
			mmin--;
			msecond = 59;
			if (mmin < 0) {
				mmin = 59;
				mhour--;
				if (mhour < 0) {
					mhour = 59;
					mday--;
				}
			}
		}
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
	private static String getTimeStr(long time)
	{
		if(time<10 && time>=0)
			return "0"+time;
		else
			return ""+time;
	}

	@Override
	public void run() {
		run = true;

		ComputeTime();
		String strTime = "";
		if(mday>0)
			strTime += mday + "d ";
		strTime += getTimeStr(mhour) + ":" + getTimeStr(mmin) + ":"
				+ getTimeStr(msecond);
		this.setText(strTime);
		if (mday <= 0 && mhour == 0 && mmin == 0 && msecond == 0) {
			return;
		}
		postDelayed(this, 1000);
	}
}
