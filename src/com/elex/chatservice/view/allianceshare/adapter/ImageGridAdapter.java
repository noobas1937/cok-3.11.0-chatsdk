package com.elex.chatservice.view.allianceshare.adapter;

import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.view.allianceshare.controller.ImageDispalyManager;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageGridAdapter extends BaseAdapter
{
	private Context			mContext;
	private List<ImageItem>	mDataList;

	public ImageGridAdapter(Context context, List<ImageItem> dataList)
	{
		this.mContext = context;
		this.mDataList = dataList;
	}

	@Override
	public int getCount()
	{
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position)
	{
		if(mDataList!=null && mDataList.size() >0 && position>=0 && position<mDataList.size())
			return mDataList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder mHolder;

		if (convertView == null)
		{
			convertView = View.inflate(mContext, R.layout.item_image_list, null);
			mHolder = new ViewHolder();
			mHolder.imageIv = (ImageView) convertView.findViewById(R.id.image);
			mHolder.selectedImageView = (ImageView) convertView.findViewById(R.id.selected_tag);
			mHolder.selectedBgTextView = (TextView) convertView.findViewById(R.id.image_selected_bg);
			convertView.setTag(mHolder);
		}
		else
		{
			mHolder = (ViewHolder) convertView.getTag();
		}

		final ImageItem item = (ImageItem)getItem(position);

		if(item!=null)
		{
			ImageDispalyManager.getInstance(mContext).displayBmp(mHolder.imageIv, item.thumbnailPath, item.sourcePath);

			if (AllianceShareManager.getInstance().containsSelelctedImageKey(item.sourcePath))
			{
				mHolder.selectedImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.mail_checked));
				mHolder.selectedImageView.setVisibility(View.VISIBLE);
			}
			else
			{
				mHolder.selectedImageView.setImageDrawable(null);
				mHolder.selectedImageView.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	static class ViewHolder
	{
		private ImageView	imageIv;
		private ImageView	selectedImageView;
		private TextView	selectedBgTextView;
	}

}
