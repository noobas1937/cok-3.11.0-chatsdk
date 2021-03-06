package com.elex.chatservice.view.adapter;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.HtmlTextUtil;
import com.elex.chatservice.util.ScaleUtil;

public class MailViewHolder extends CategoryViewHolder
{
	public TextView		item_latest_msg;
	public TextView		item_time;
	public LinearLayout	item_icon_layout;
	public ImageView	item_lock_icon;
	public ImageView	item_reward_icon;
	public TextView 	item_recycle_time;
	public LinearLayout	contentLinearLayout;
	public RelativeLayout top_item_divider;

	public MailViewHolder(View view)
	{
		super(view);

		item_latest_msg = (TextView) view.findViewById(R.id.channel_latest_msg);
		item_time = (TextView) view.findViewById(R.id.channel_item_time);
		item_icon_layout = (LinearLayout) view.findViewById(R.id.channel_icon_layout);
		item_lock_icon = (ImageView) view.findViewById(R.id.channel_lock_icon);
		item_recycle_time = (TextView) view.findViewById(R.id.recycle_time);
		item_reward_icon = (ImageView) view.findViewById(R.id.channel_reward_icon);
		contentLinearLayout = (LinearLayout) view.findViewById(R.id.content_linear_layout);
		top_item_divider = (RelativeLayout) view.findViewById(R.id.top_item_divider);
	}

	public void setContent(Context context, ChannelListItem item, boolean showUreadAsText, Drawable drawable, String title, String summary,
			String time, boolean isInEditMode, int position, int bgColor)
	{
		super.setContent(context, item, showUreadAsText, drawable, title, summary, time, isInEditMode, position, bgColor);

		if (StringUtils.isNotEmpty(summary))
		{
			if(summary.contains(".png"))
				HtmlTextUtil.setResourceHtmlText(item_latest_msg, summary);
			else
			{
				item_latest_msg.setText(summary);
			}
		}
		
		item_time.setText(time);

		if (bgColor != 0)
		{
			GradientDrawable bgShape = (GradientDrawable) item_icon.getBackground();
			bgShape.setColor(bgColor);
		}
	}

	protected void showUnreadCountText(ChannelListItem item ,Context context)
	{
		unread_text.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);

		if (unreadCount <= 0)
			return;

		if (ChatServiceController.isNewMailUIEnable || !showUreadAsText)
		{
			unread_text.setText("");
			unread_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, 1);
			unread_text.setBackgroundResource(R.drawable.channel_red_dot);
		}
		else
		{
			unread_text.setText(Integer.toString(unreadCount));
			Resources r = context.getResources();
			unread_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, r.getDimension(R.dimen.cs__textSummary));
			unread_text.setBackgroundDrawable(getDefaultBackground(context));
		}
	}

	protected void showIcon(boolean isLock, boolean reward, ChannelListItem item, boolean isInEditMode)
	{
		
		item_lock_icon.setVisibility(isLock ? View.VISIBLE : View.GONE);
		item_reward_icon.setVisibility(reward ? View.VISIBLE : View.GONE);
		if(item instanceof MailData)
		{
			MailData mail = (MailData)item;
			if(mail!=null && mail.getChannelId().equals(MailManager.CHANNELID_RECYCLE_BIN) && mail.getRecycleTime()!=-1)
			{
				item_recycle_time.setVisibility(View.VISIBLE);
				item_recycle_time.setText(mail.getReableRecycleTime());
			}
			else
				item_recycle_time.setVisibility(View.GONE);
		}
		else
		{
			item_recycle_time.setVisibility(View.GONE);
		}

		if (isInEditMode)
		{
			adjustContentLinearLayout(false);
		}
		else if (!enableAnimation)
		{
			adjustContentLinearLayout(true);
		}
		super.showIcon(isLock, reward, item, isInEditMode);
	}

	private void adjustContentLinearLayout(boolean isExpand)
	{
		// ??????UI??????contentLinearLayout
		if (contentLinearLayout == null)
			return;

		if (isExpand)
		{
			LinearLayout.LayoutParams lay = (LinearLayout.LayoutParams) contentLinearLayout.getLayoutParams();
			lay.weight = 504;
		}
		else
		{
			LinearLayout.LayoutParams lay = (LinearLayout.LayoutParams) contentLinearLayout.getLayoutParams();
			// ??????item_checkbox_layout?????????item_leading_space
			lay.weight = 504 - 64 + 16;
		}
	}

	protected void onHideAnimationEnd()
	{
		adjustContentLinearLayout(true);

		super.onHideAnimationEnd();
	}

	protected void adjustSizeExtend(Context context)
	{
		int muteIconWidthDP = 15;
		
		 ScaleUtil.adjustTextSize(item_latest_msg, ConfigManager.scaleRatio *getFontScreenCorrectionFactor(ChatServiceController.getCurrentActivity()));
		 ScaleUtil.adjustTextSize(item_time, ConfigManager.scaleRatio *getFontScreenCorrectionFactor(ChatServiceController.getCurrentActivity()));
		
		LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams((int) (ScaleUtil.dip2px(context, muteIconWidthDP)
				* ConfigManager.scaleRatio * getScreenCorrectionFactor(context)), (int) (ScaleUtil.dip2px(context, muteIconWidthDP)
				* ConfigManager.scaleRatio * getScreenCorrectionFactor(context)));
		param1.setMargins(0, 0, 3, 0);
		item_reward_icon.setLayoutParams(param1);

		item_lock_icon.setLayoutParams(param1);
	}
}
