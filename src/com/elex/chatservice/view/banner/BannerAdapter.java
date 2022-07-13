package com.elex.chatservice.view.banner;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.MathUtil;
import com.elex.chatservice.util.ScaleUtil;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BannerAdapter extends BaseAdapter
{

	private List<BannerInfo>	items;
	private Activity		mActivity;
	private LayoutInflater inflator;
	private int listSize = 0;
	private double screenCorrectFactor = 1;
	
	private static final String BANNER_ICON_PREFIX = "banner_icon_";

	public BannerAdapter(Activity activity,List<BannerInfo> list)
	{
		mActivity = activity;
		items = list;
		if(items!=null)
			listSize = items.size();
		if(mActivity!=null)
		{
			screenCorrectFactor = ScaleUtil.getScreenCorrectionFactor();
			inflator = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}

	@Override
	public int getCount()
	{
		return Integer.MAX_VALUE;
	}
	
	private int getPosition(int position) {
		return position % listSize;
	}

	@Override
	public Object getItem(int position)
	{
		if (items != null && position >= 0)
			return items.get(getPosition(position));
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return getPosition(position);
	}
	
	private void adjustTextSize(View convertView)
	{
		TextView buy_text = ViewHolderHelper.get(convertView, R.id.buy_text);
		if(buy_text!=null)
			ScaleUtil.adjustTextSize(buy_text, ConfigManager.scaleRatio);
		
		TextView banner_text = ViewHolderHelper.get(convertView, R.id.banner_text);
		if(banner_text!=null)
			ScaleUtil.adjustTextSize(banner_text, ConfigManager.scaleRatio);
		
		TextView name_text = ViewHolderHelper.get(convertView, R.id.name_text);
		if(name_text!=null)
			ScaleUtil.adjustTextSize(name_text, ConfigManager.scaleRatio);
		
		TextView gold_text = ViewHolderHelper.get(convertView, R.id.gold_text);
		if(gold_text!=null)
			ScaleUtil.adjustTextSize(gold_text, ConfigManager.scaleRatio);
		
		TextView end_time_text = ViewHolderHelper.get(convertView, R.id.end_time_text);
		if(end_time_text!=null)
			ScaleUtil.adjustTextSize(end_time_text, ConfigManager.scaleRatio);
		
		TextView price_text = ViewHolderHelper.get(convertView, R.id.price_text);
		if(price_text!=null)
			ScaleUtil.adjustTextSize(price_text, ConfigManager.scaleRatio);
	}

	private void adjustSize(View convertView)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);
			int iconWidth = (int) (ScaleUtil.dip2px(65) * ConfigManager.scaleRatio * screenCorrectFactor);
			ImageView banner_icon = ViewHolderHelper.get(convertView, R.id.banner_icon);
			if(banner_icon!=null)
			{
				FrameLayout.LayoutParams banner_iconParams = (FrameLayout.LayoutParams) banner_icon.getLayoutParams();
				banner_iconParams.width = iconWidth;
				banner_iconParams.height = iconWidth;
				banner_icon.setLayoutParams(banner_iconParams);
			}
			
			LinearLayout gold_name_layout = ViewHolderHelper.get(convertView, R.id.gold_name_layout);
			if(gold_name_layout!=null)
			{
				LinearLayout.LayoutParams gold_name_layoutParams = (LinearLayout.LayoutParams) gold_name_layout.getLayoutParams();
				gold_name_layoutParams.leftMargin = iconWidth;
				gold_name_layout.setLayoutParams(gold_name_layoutParams);
			}
			
			LinearLayout banner_layout = ViewHolderHelper.get(convertView, R.id.banner_layout);
			if(banner_layout!=null)
			{
				FrameLayout.LayoutParams banner_layoutParams = (FrameLayout.LayoutParams) banner_layout.getLayoutParams();
				banner_layoutParams.height = iconWidth;
				banner_layout.setLayoutParams(banner_layoutParams);
			}
			
			int buy_btnHeight = (int) (ScaleUtil.dip2px(mActivity, 50) * ConfigManager.scaleRatio * screenCorrectFactor);
			LinearLayout buy_btn = ViewHolderHelper.get(convertView, R.id.buy_btn);
			if(buy_btn!=null)
			{
				LinearLayout.LayoutParams buy_btnParams = (LinearLayout.LayoutParams) buy_btn.getLayoutParams();
				buy_btnParams.height = buy_btnHeight;
				buy_btn.setLayoutParams(buy_btnParams);
			}
			
			int height = (int) (ScaleUtil.dip2px(mActivity, 20) * ConfigManager.scaleRatio * screenCorrectFactor);
			ImageView endtime_icon = ViewHolderHelper.get(convertView, R.id.endtime_icon);
			if(endtime_icon!=null)
			{
				RelativeLayout.LayoutParams endtime_iconParams = (RelativeLayout.LayoutParams) endtime_icon.getLayoutParams();
				endtime_iconParams.height = height;
				endtime_iconParams.width = height;
				endtime_icon.setLayoutParams(endtime_iconParams);
			}
			
			
			RelativeLayout banner_title_layout = ViewHolderHelper.get(convertView, R.id.banner_title_layout);
			if(banner_title_layout!=null)
			{
				LinearLayout.LayoutParams banner_title_layoutParams = (LinearLayout.LayoutParams) banner_title_layout.getLayoutParams();
				banner_title_layoutParams.height = height;
				banner_title_layout.setLayoutParams(banner_title_layoutParams);
			}
			
//			ImageView title_bg = ViewHolderHelper.get(convertView, R.id.title_bg);
//			if(title_bg!=null)
//			{
//				int height = (int) (ScaleUtil.dip2px(mActivity, 17) * ConfigManager.scaleRatio * mActivity.getScreenCorrectionFactor()+ScaleUtil.dip2px(2));
//				FrameLayout.LayoutParams title_bgParams = (FrameLayout.LayoutParams) title_bg.getLayoutParams();
//				title_bgParams.height = height;
//				title_bg.setLayoutParams(title_bgParams);
//			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			if(inflator!=null)
				convertView = inflator.inflate(R.layout.banner_item, null);
			adjustSize(convertView);
		}
		
		TextView buy_text = ViewHolderHelper.get(convertView, R.id.buy_text);
		if(buy_text!=null)
			buy_text.setText(LanguageManager.getLangByKey(LanguageKeys.BANNER_BUY));
		
		LinearLayout banner_layout = ViewHolderHelper.get(convertView, R.id.banner_layout);
		if(banner_layout!=null)
		{
			if(position % 3 == 0)
				ImageUtil.setBackground(banner_layout, mActivity.getResources().getDrawable(R.drawable.banner_bg1));
			else if(position % 3 == 1)
				ImageUtil.setBackground(banner_layout, mActivity.getResources().getDrawable(R.drawable.banner_bg2));
			else
				ImageUtil.setBackground(banner_layout, mActivity.getResources().getDrawable(R.drawable.banner_bg3));
		}
		
		final BannerInfo bannerInfo = (BannerInfo)getItem(position);
		if(bannerInfo!=null)
		{
			TextView banner_text = ViewHolderHelper.get(convertView, R.id.banner_text);
			if(banner_text!=null)
			{
				if(StringUtils.isNotEmpty(bannerInfo.getTitle()))
				{
					banner_text.setVisibility(View.VISIBLE);
					banner_text.setText(LanguageManager.getLangByKey(bannerInfo.getTitle()));
				}
				else
					banner_text.setVisibility(View.GONE);
			}
			
			TextView name_text = ViewHolderHelper.get(convertView, R.id.name_text);
			if(name_text!=null)
				name_text.setText(LanguageManager.getLangByKey(bannerInfo.getName()));
			
			TextView gold_text = ViewHolderHelper.get(convertView, R.id.gold_text);
			if(gold_text!=null)
				gold_text.setText("Gold+"+MathUtil.getFormatNumber(bannerInfo.getGold()));
			
			TimeTextView end_time_text = ViewHolderHelper.get(convertView, R.id.end_time_text);
			if(end_time_text!=null)
			{
				int[] timeArr = new int[4];
				int time = bannerInfo.getTime();
				int endTime = bannerInfo.getEndTime();
				int curTime = TimeManager.getInstance().getCurrentLocalTime();
				
//				int curTime2 = TimeManager.getInstance().getCurrentTime();
				
				int lastTime = 0;
				if (time > 0)
				{
					int gapTime = (endTime - curTime);
					int count = gapTime / (time * 3600);
					lastTime = endTime - (time * 3600) * count - curTime;
				}
				else
				{
					lastTime = endTime - curTime;
				}
//				System.out.println("bannerInfo.getName():"+bannerInfo.getName()+"  curTime:"+curTime+"  endTime:"+endTime+"  lastTime:"+lastTime+"   curTime2:"+curTime2);
				int day = 0;
				int hour = 0;
				int min = 0;
				int second = 0;
				
				if(lastTime>24*60*60)
					day = lastTime/(24*60*60);
				lastTime = lastTime%(24*60*60);
				if(lastTime>60*60)
					hour = lastTime/(60*60);
				lastTime = lastTime%(60*60);
				if(lastTime>60)
					min = lastTime/60;
				second = lastTime%60;
				timeArr[0] = day;
				timeArr[1] = hour;
				timeArr[2] = min;
				timeArr[3] = second;
				
				end_time_text.setTimes(timeArr);
				if (!end_time_text.isRun()) {
					end_time_text.run();
				}
			}
			
			TextView price_text = ViewHolderHelper.get(convertView, R.id.price_text);
			if(price_text!=null)
				price_text.setText(bannerInfo.getPrice());
			
			ImageView banner_icon = ViewHolderHelper.get(convertView, R.id.banner_icon);
			if(banner_icon!=null)
			{
				String icon = bannerInfo.getIcon();
				String[] iconArr = icon.split("\\|");
				String icon1 = "";
				String icon2 = "";
				if(iconArr.length == 1)
					icon1 = iconArr[0];
				else if(iconArr.length == 2)
				{
					icon1 = iconArr[0];
					icon2 = iconArr[1];
				}
				
				String iconName = "";
				if (StringUtils.isNotEmpty(icon1) && !icon1.equals("0")) {
					String pic = icon1.toLowerCase();
					iconName = BANNER_ICON_PREFIX + pic +".png";
					ImageUtil.setDynamicImage(mActivity, iconName, banner_icon);
			    }else if (StringUtils.isNotEmpty(icon2) && !icon2.equals("0")){
			    	String pic = icon2.toLowerCase();
			    	iconName = BANNER_ICON_PREFIX + pic +".png";
			    	ImageUtil.setDynamicImage(mActivity, iconName, banner_icon);
			    }else{
			    	iconName = "banner_defalut_icon.png";
			    	banner_icon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.banner_icon_default));
			    }
//				System.out.println("iconName:"+iconName);
				
			}
			
			LinearLayout buy_btn = ViewHolderHelper.get(convertView, R.id.buy_btn);
			if(buy_btn!=null)
				buy_btn.setOnClickListener(new OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						ChatServiceController.doHostAction("showGiftView", "", "", bannerInfo.getId(), false);
					}
				});
			 
		}
		return convertView;
	}

}
