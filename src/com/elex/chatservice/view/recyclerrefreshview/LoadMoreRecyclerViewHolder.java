package com.elex.chatservice.view.recyclerrefreshview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import com.elex.chatservice.R;

public class LoadMoreRecyclerViewHolder extends RecyclerView.ViewHolder
{
	public LinearLayout		loading_layout;

	public LoadMoreRecyclerViewHolder(View view)
	{
		super(view);
		loading_layout = (LinearLayout) view.findViewById(R.id.loading_layout);
	}
}
