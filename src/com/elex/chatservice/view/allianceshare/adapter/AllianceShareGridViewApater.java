package com.elex.chatservice.view.allianceshare.adapter;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.ImagePagerActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AllianceShareGridViewApater extends BaseAdapter
{
	private MyActionBarActivity				activity;
	private LayoutInflater					inflater;
	private List<AllianceShareImageData>	allianceShareImageList;
	private AllianceShareInfo				allianceShareInfo;

	public AllianceShareGridViewApater(MyActionBarActivity activity, AllianceShareInfo allianceShareInfo)
	{
		this.activity = activity;
		this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
		this.allianceShareInfo = allianceShareInfo;
		if (allianceShareInfo != null)
			allianceShareImageList = allianceShareInfo.getData();
	}

	@Override
	public int getCount()
	{
		if (allianceShareImageList != null)
			return allianceShareImageList.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (allianceShareImageList != null && allianceShareImageList.size() > 0 && position >= 0
				&& position < allianceShareImageList.size())
			return allianceShareImageList.get(position);
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
		// System.out.println("grid getView position:" + position);
		final AllianceShareImageData item = (AllianceShareImageData) getItem(position);
		
		ImageView gridImage = null;
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.item_allianceshare_grid, parent, false);
			gridImage = (ImageView) convertView.findViewById(R.id.item_grid_image);
			convertView.setTag(gridImage);
		}
		else
		{
			gridImage = (ImageView) convertView.getTag();
		}

		adjustSize(gridImage);
		final int currentPos = position;
		
		if (item == null)
			return convertView;

		if (StringUtils.isNotEmpty(item.getUrl()))
		{
			if(!allianceShareInfo.isSelfAllianceShare() && TimeManager.getInstance().getCurrentTimeMS() - allianceShareInfo.getTime() > 60 *60 *1000)
				ImageUtil.loadAllianeShareThumbImage(activity, gridImage, item, true);
			else
				ImageUtil.loadAllianeShareThumbImage2(activity, gridImage, item, true);
			
			gridImage.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(activity, ImagePagerActivity.class);
					intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, currentPos);
					intent.putExtra(IntentConstants.EXTRA_DETAIL_IMAGE_LIST, (Serializable) allianceShareImageList);
					ServiceInterface.showImagePagerActivity(activity, intent);
				}
			});
		}

		return convertView;
	}

	private void adjustSize(ImageView gridImage)
	{
		int columWidth = AllianceShareManager.getImageWidthByImageNum(allianceShareImageList.size());
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gridImage.getLayoutParams();
		layoutParams.width = columWidth;
		layoutParams.height = columWidth;
		gridImage.setLayoutParams(layoutParams);
	}

}
