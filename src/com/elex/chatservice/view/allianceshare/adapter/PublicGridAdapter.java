package com.elex.chatservice.view.allianceshare.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.controller.ImageDispalyManager;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PublicGridAdapter extends BaseAdapter
{
	private MyActionBarActivity		activity;
	private LayoutInflater	inflater;
	private Drawable		addImageIcon;
	private List<ImageItem>	mDataList	= new ArrayList<ImageItem>();
	private static final int ITEM_TYPE_NORMAL = 0;
	private static final int ITEM_TYPE_ADD	=1;

	public PublicGridAdapter(MyActionBarActivity activity)
	{
		this.activity = activity;
		this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
		refreshAdapter();
	}
	
	public void refreshAdapter()
	{
		mDataList = AllianceShareManager.getInstance().getSelctedImageArray();
		if(mDataList.size() < AllianceShareManager.SHARE_NUM_LIMIT)
		{
			ImageItem item = new ImageItem();
			item.isAddBtn = true;
			mDataList.add(item);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		if (mDataList != null)
		{
			return mDataList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (mDataList != null && position >= 0 && position < mDataList.size())
			return mDataList.get(position);
		return null;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		if(position <0 || position>=mDataList.size())
			return -1;
		ImageItem item = mDataList.get(position);
		if(item!=null)
		{
			if(item.isAddBtn)
				return ITEM_TYPE_ADD;
			else
				return ITEM_TYPE_NORMAL;
		}
		return -1;
	}
	
	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView gridImage = null;
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.item_share_grid, parent, false);
			gridImage = (ImageView) convertView.findViewById(R.id.item_grid_image);
			adjustImageSize(gridImage);
			convertView.setTag(gridImage);
		}
		else
		{
			gridImage = (ImageView) convertView.getTag();
		}
		
		
		int type = getItemViewType(position);

		if(type == ITEM_TYPE_ADD)
		{
			if (addImageIcon == null)
				addImageIcon = activity.getResources().getDrawable(R.drawable.share_grid_add_pic);
			gridImage.setImageDrawable(addImageIcon);
		}
		else if(type == ITEM_TYPE_NORMAL)
		{
			ImageItem item = (ImageItem) getItem(position);
			if(item !=null)
				ImageUtil.loadAllianeShareImage(activity, gridImage, item.sourcePath, true);
//			ImageDispalyManager.getInstance(activity).displayBmp(gridImage, item.thumbnailPath, item.sourcePath);
		}
		else
		{
			gridImage.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	private void adjustImageSize(ImageView imageView)
	{
		if(imageView!=null)
		{
			int screenWidth = ScaleUtil.getScreenWidth();
			System.out.println("screenWidth:"+screenWidth);
			int columWidth = ScaleUtil.dip2px(70);
			int gridWidth = screenWidth > 1080 ? 1080 : screenWidth;
			if(!ScaleUtil.isPad())
				columWidth = (screenWidth - ScaleUtil.dip2px(10) - ScaleUtil.dip2px(15) - 3* ScaleUtil.dip2px(10))/4;
			else
				columWidth = (gridWidth - ScaleUtil.dip2px(10) - ScaleUtil.dip2px(15) - 3* ScaleUtil.dip2px(10))/4;
			LinearLayout.LayoutParams imageViewLayoutParams = (LinearLayout.LayoutParams) imageView
					.getLayoutParams();
			imageViewLayoutParams.width = columWidth;
			imageViewLayoutParams.height = columWidth;
			imageView.setLayoutParams(imageViewLayoutParams);
		}
	}

}
