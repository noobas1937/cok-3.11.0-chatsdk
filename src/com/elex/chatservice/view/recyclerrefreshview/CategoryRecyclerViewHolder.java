package com.elex.chatservice.view.recyclerrefreshview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;

public class CategoryRecyclerViewHolder extends RecyclerView.ViewHolder
{
	public LinearLayout		channel_content_layout;
	public LinearLayout		item_checkbox_layout;
	public LinearLayout		item_divider_title_layout;
	public View				item_leading_space;
	public ImageView		item_icon;
	public TextView			item_title;
	public TextView			divideText;
	public ImageView		list_item_arrow;
	public TextView			unread_text;
	public int				unreadCount;
	public TextView			unread_dot;

	public CheckBox			item_checkBox;
	public boolean			adjustSizeCompleted	= false;
	public boolean			showUreadAsText;

	public FrameLayout		base;

	public CategoryRecyclerViewHolder(View view)
	{
		super(view);
		channel_content_layout = (LinearLayout) view.findViewById(R.id.channel_content_layout);
		item_leading_space = view.findViewById(R.id.channel_leading_space);
		item_checkbox_layout = (LinearLayout) view.findViewById(R.id.channel_item_checkbox_layout);
		item_divider_title_layout = (LinearLayout) view.findViewById(R.id.divider_title_layout);
		unread_text = (TextView) view.findViewById(R.id.channel_unread_count);
		unread_dot = (TextView) view.findViewById(R.id.unread_dot);
		item_icon = (ImageView) view.findViewById(R.id.channel_icon);
		item_title = (TextView) view.findViewById(R.id.channel_name);
		item_checkBox = (CheckBox) view.findViewById(R.id.channel_checkBox);
		list_item_arrow = (ImageView) view.findViewById(R.id.list_item_arrow);
		divideText = (TextView) view.findViewById(R.id.divideText);
		if (divideText != null)
			divideText.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_REPORT));
		item_checkBox.setClickable(false);
		item_checkBox.setFocusable(false);
	}

	public void setContent(Context context, ChannelListItem item, boolean showUreadAsText, Drawable drawable, String title, String summary,
			String time, boolean isInEditMode, int bgColor)
	{
		adjustSize(context);

		if (item instanceof MailData)
		{
			this.unreadCount = item.isUnread() ? 1 : 0;
		}
		else
		{
			this.unreadCount = item.unreadCount;
		}
		this.showUreadAsText = showUreadAsText;

		showUnreadCountText(item, context);

		if (drawable != null)
		{
			item_icon.setImageDrawable(drawable);
		}

		if (item_divider_title_layout != null && item instanceof ChatChannel && (((ChatChannel) item).isDialogChannel()
				|| ((ChatChannel) item).channelID.equals(MailManager.CHANNELID_MOD))
				&& ((ChatChannel) item).channelID.equals(ChannelManager.getInstance().getFirstChannelID()))
		{
			item_divider_title_layout.setVisibility(View.VISIBLE);
		}
		else if (item_divider_title_layout != null)
		{
			item_divider_title_layout.setVisibility(View.GONE);
		}

		item_title.setText(title);
	}

	protected void showUnreadCountText(ChannelListItem item, Context context)
	{
		unread_text.setVisibility(View.VISIBLE);
		String unread = Integer.toString(unreadCount);
		if (unreadCount > 99)
		{
			unread = "99+";
		}
		else if (unreadCount <= 0)
		{
			unread = "";
		}
		if (item instanceof ChatChannel && (((ChatChannel) item).channelID.equals(MailManager.CHANNELID_RESOURCE) || ((ChatChannel) item).channelID.equals(MailManager.CHANNELID_MONSTER))
				&& unreadCount > 0)
		{
			unread_text.setText("");
			if (unread_dot != null)
				unread_dot.setVisibility(View.VISIBLE);
		}
		else
		{
			if (unread_dot != null)
				unread_dot.setVisibility(View.GONE);
			unread_text.setText(unread);
		}
	}

	private static final int	DEFAULT_CORNER_RADIUS_DIP	= 8;
	private final int			DEFAULT_BADGE_COLOR			= Color.parseColor("#CCFF0000");

	protected ShapeDrawable getDefaultBackground(Context context)
	{

		int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP, context);
		float[] outerR = new float[] { r, r, r, r, r, r, r, r };

		RoundRectShape rr = new RoundRectShape(outerR, null, null);
		ShapeDrawable drawable = new ShapeDrawable(rr);
		drawable.getPaint().setColor(DEFAULT_BADGE_COLOR);

		return drawable;
	}

	private int dipToPixels(int dip, Context context)
	{
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}

	protected void adjustSize(Context context)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0 && !adjustSizeCompleted)
		{
			double factor = ScaleUtil.getFontScreenCorrectionFactor();
			float textSize = item_title.getTextSize();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "textSize",textSize);
			ScaleUtil.adjustTextSize(item_title, ConfigManager.scaleRatio * factor);
			ScaleUtil.adjustTextSize(unread_text, ConfigManager.scaleRatio * factor);
			adjustSizeExtend(context);
			adjustSizeCompleted = true;
		}
	}

	protected void adjustSizeExtend(Context context)
	{
	}

}
