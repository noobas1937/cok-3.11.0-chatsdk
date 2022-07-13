package com.elex.chatservice.view.adapter;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.FixedAspectRatioFrameLayout;
import com.elex.chatservice.util.HtmlTextUtil;
import com.elex.chatservice.util.RoundImageView;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MailContentViewHolder
{
	public CheckBox						mail_checkbox;
	public RelativeLayout				mail_content_layout;
	public FixedAspectRatioFrameLayout	mail_icon_layout;
	public RoundImageView				mail_icon;
	public TextView						unread_count;
	public TextView						mail_title;
	public TextView						mail_time;
	public TextView						mail_summary;
	public TextView						recycle_time;
	public ImageView					lock_icon;
	public ImageView					reward_icon;

	public MailContentViewHolder(View view)
	{
		mail_checkbox = (CheckBox) view.findViewById(R.id.mail_checkbox);
		mail_content_layout = (RelativeLayout) view.findViewById(R.id.mail_content_layout);
		mail_icon_layout = (FixedAspectRatioFrameLayout) view.findViewById(R.id.mail_icon_layout);
		mail_icon = (RoundImageView) view.findViewById(R.id.mail_icon);
		unread_count = (TextView) view.findViewById(R.id.unread_count);
		mail_title = (TextView) view.findViewById(R.id.mail_title);
		mail_time = (TextView) view.findViewById(R.id.mail_time);
		mail_summary = (TextView) view.findViewById(R.id.mail_summary);
		recycle_time = (TextView) view.findViewById(R.id.recycle_time);
		lock_icon = (ImageView) view.findViewById(R.id.lock_icon);
		reward_icon = (ImageView) view.findViewById(R.id.reward_icon);
	}

	public void setContent(ChannelListItem item, String title, String summary,
			String time, boolean isInEditMode, int bgColor)
	{
		mail_title.setText(title);
		mail_time.setText(time);
		if (StringUtils.isNotEmpty(summary))
		{
			if (summary.contains(".png"))
				HtmlTextUtil.setResourceHtmlText(mail_summary, summary);
			else
				mail_summary.setText(summary);
		}

		if (bgColor != 0)
		{
			GradientDrawable bgShape = (GradientDrawable) mail_icon.getBackground();
			bgShape.setColor(bgColor);
		}

		int unreadCount = 0;
		if (item instanceof MailData)
			unreadCount = item.isUnread() ? 1 : 0;
		else
			unreadCount = item.unreadCount;
		unread_count.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
		showIcon(item.isLock(), item.hasReward(), item, isInEditMode);
	}

	protected void showIcon(boolean isLock, boolean reward, ChannelListItem item, boolean isInEditMode)
	{
		lock_icon.setVisibility(isLock ? View.VISIBLE : View.GONE);
		reward_icon.setVisibility(reward ? View.VISIBLE : View.GONE);
		if (item instanceof MailData)
		{
			MailData mail = (MailData) item;
			if (mail != null && mail.getChannelId().equals(MailManager.CHANNELID_RECYCLE_BIN) && mail.getRecycleTime() != -1)
			{
				recycle_time.setVisibility(View.VISIBLE);
				recycle_time.setText(mail.getReableRecycleTime());
			}
			else
				recycle_time.setVisibility(View.GONE);
		}
		else
		{
			recycle_time.setVisibility(View.GONE);
		}

		if (isInEditMode)
		{
			mail_checkbox.setVisibility(View.VISIBLE);
			mail_checkbox.setChecked(item.checked);
		}
		else
		{
			mail_checkbox.setVisibility(View.GONE);
		}
	}
}
