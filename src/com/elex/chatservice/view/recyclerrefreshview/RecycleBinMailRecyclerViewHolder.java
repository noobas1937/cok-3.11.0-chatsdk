package com.elex.chatservice.view.recyclerrefreshview;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.util.ScaleUtil;

public class RecycleBinMailRecyclerViewHolder extends RecyclerView.ViewHolder
{
	public TextView		recycle_mail_tip;

	public RecycleBinMailRecyclerViewHolder(View view)
	{
		super(view);
		recycle_mail_tip = (TextView) view.findViewById(R.id.recycle_mail_tip);
	}

	protected void adjustSizeExtend(Context context)
	{
		ScaleUtil.adjustTextSize(recycle_mail_tip, ConfigManager.scaleRatio);
	}
}
