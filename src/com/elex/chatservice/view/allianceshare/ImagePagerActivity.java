package com.elex.chatservice.view.allianceshare;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 图片查看器
 */
public class ImagePagerActivity extends MyActionBarActivity
{
	private static final String				STATE_POSITION		= "STATE_POSITION";
	public static final String				EXTRA_IMAGE_INDEX	= "image_index";
	public static final String				EXTRA_IMAGE_URLS	= "image_urls";

	private HackyViewPager					mPager;
	private int								pagerPosition;
	private TextView						indicator;
	private List<ImageItem>					imageList			= null;
	private List<AllianceShareImageData>	imageListFromServer	= null;
	private ImagePagerAdapter				adapter;
	public ImageDetailFragment				detailFragment;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.image_detail_pager, fragmentLayout, true);
		showRightBtn(null);
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));

		initImageZoomData();

		mPager = (HackyViewPager) findViewById(R.id.pager);

		imageDelButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (imageList.size() == 1)
				{
					removeImage();
					exitActivity();
				}
				else
				{
					removeImage(pagerPosition);
//					mPager.removeViewAt(pagerPosition);
//					mPager.removeAllViews();
					adapter.notifyDataSetChanged();
				}
			}
		});

		if (imageListFromServer != null)
		{
			imageDelButton.setVisibility(View.GONE);
			adapter = new ImagePagerAdapter(getSupportFragmentManager(), imageListFromServer);
			// photo_bt_del.setVisibility(View.GONE);
		}
		else
		{
			imageDelButton.setVisibility(View.VISIBLE);
			adapter = new ImagePagerAdapter(getSupportFragmentManager(), imageList, false);
			// photo_bt_del.setVisibility(View.VISIBLE);
		}

		mPager.setAdapter(adapter);
		indicator = (TextView) findViewById(R.id.indicator);

		String text = "1/" + mPager.getAdapter().getCount();
		indicator.setText(text);
		// 更新下标
		mPager.addOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageSelected(int arg0)
			{
				String text = (arg0 + 1) + "/" + mPager.getAdapter().getCount();
				indicator.setText(text);
			}

		});
		if (savedInstanceState != null)
		{
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mPager.setCurrentItem(pagerPosition);
	}

	private void removeImage()
	{
		imageList.clear();
		AllianceShareManager.getInstance().clearSelectedImages();
	}

	private void removeImage(int location)
	{
		if (location + 1 <= imageList.size())
		{
			ImageItem item = imageList.get(location);
			if (item != null && StringUtils.isNotEmpty(item.sourcePath))
				AllianceShareManager.getInstance().removeSelectedImage(item.sourcePath);
			imageList.remove(location);
		}
	}

	private void initImageZoomData()
	{
		pagerPosition = getIntent().getIntExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, 0);

		if (getIntent().hasExtra(IntentConstants.EXTRA_DETAIL_IMAGE_LIST))
		{
			imageListFromServer = (List<AllianceShareImageData>) getIntent().getSerializableExtra(IntentConstants.EXTRA_DETAIL_IMAGE_LIST);
		}
		else
		{
			imageList = AllianceShareManager.getInstance().getSelctedImageArray();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter
	{

		public List<AllianceShareImageData>	imageDataList	= new ArrayList<AllianceShareImageData>();
		public List<ImageItem>				dataList		= new ArrayList<ImageItem>();
		private boolean						isFromServer	= true;

		public ImagePagerAdapter(FragmentManager fm, List<AllianceShareImageData> imageDataList)
		{
			super(fm);
			this.imageDataList = imageDataList;
			isFromServer = true;
		}

		public ImagePagerAdapter(FragmentManager fm, List<ImageItem> imageDataList, boolean isFromServer)
		{
			super(fm);
			this.dataList = imageDataList;
			this.isFromServer = false;
		}

		@Override
		public int getCount()
		{
			if (isFromServer)
				return imageDataList == null ? 0 : imageDataList.size();
			else
				return dataList == null ? 0 : dataList.size();
		}
		
		@Override
	    public int getItemPosition(Object object) {
	        if (object.getClass().getName().equals(ImageDetailFragment.class.getName())) {
	            return POSITION_NONE;
	        }
	        return super.getItemPosition(object);
	    }

		@Override
		public Fragment getItem(int position)
		{
			if (position < 0)
				return null;
			if (isFromServer)
			{
				if (imageDataList == null || position > imageDataList.size())
					return null;
				AllianceShareImageData imageData = imageDataList.get(position);
				int columWidth = AllianceShareManager.getImageWidthByImageNum(imageDataList.size());
				return ImageDetailFragment.newInstance(imageData,columWidth);
			}
			else
			{
				if (imageList == null || position > imageList.size())
					return null;
				ImageItem item = imageList.get(position);
				if (item != null && StringUtils.isNotEmpty(item.sourcePath))
					return ImageDetailFragment.newInstance(item.sourcePath);
				else
					return null;
			}
		}

	}

	@Override
	protected void onDestroy()
	{
		imageListFromServer = null;
		imageList = null;
		super.onDestroy();
	}
}
