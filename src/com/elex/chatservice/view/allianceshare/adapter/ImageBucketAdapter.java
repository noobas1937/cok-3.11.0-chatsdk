package com.elex.chatservice.view.allianceshare.adapter;

import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.view.allianceshare.controller.ImageDispalyManager;
import com.elex.chatservice.view.allianceshare.model.ImageBucket;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageBucketAdapter extends BaseAdapter
{
	private List<ImageBucket>	mDataList;
	private Context				mContext;

	public ImageBucketAdapter(Context context, List<ImageBucket> dataList)
	{
		this.mContext = context;
		this.mDataList = dataList;
	}

	@Override
	public int getCount()
	{
		return mDataList.size();
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
			convertView = View.inflate(mContext, R.layout.item_bucket_list, null);
			mHolder = new ViewHolder();
			mHolder.coverImageView = (ImageView) convertView.findViewById(R.id.cover);
			mHolder.titleTextView = (TextView) convertView.findViewById(R.id.title);
			mHolder.countTextView = (TextView) convertView.findViewById(R.id.count);
			convertView.setTag(mHolder);
		}
		else
		{
			mHolder = (ViewHolder) convertView.getTag();
		}

		ImageBucket item = (ImageBucket)getItem(position);
		if(item!=null)
		{
			if (item.imageList != null && item.imageList.size() > 0)
			{
				String thumbPath = item.imageList.get(0).thumbnailPath;
				String sourcePath = item.imageList.get(0).sourcePath;
				ImageDispalyManager.getInstance(mContext).displayBmp(mHolder.coverImageView, thumbPath, sourcePath);
			}
			else
			{
				mHolder.coverImageView.setImageBitmap(null);
			}

			mHolder.titleTextView.setText(item.bucketName);
			mHolder.countTextView.setText(""+item.count);
		}

		return convertView;
	}

	static class ViewHolder
	{
		private ImageView	coverImageView;
		private TextView	titleTextView;
		private TextView	countTextView;
	}

}
