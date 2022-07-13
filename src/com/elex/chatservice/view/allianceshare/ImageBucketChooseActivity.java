package com.elex.chatservice.view.allianceshare;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.adapter.ImageBucketAdapter;
import com.elex.chatservice.view.allianceshare.model.ImageBucket;
import com.elex.chatservice.view.allianceshare.util.ImageFetcher;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 选择相册
 * 
 */

public class ImageBucketChooseActivity extends MyActionBarActivity
{
	private ImageFetcher		mHelper;
	private List<ImageBucket>	mDataList	= new ArrayList<ImageBucket>();
	private ListView			mListView;
	private ImageBucketAdapter	mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(false, true, this);

		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__image_bucket_choose_fragment, fragmentLayout, true);
		
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(null);
		
		mHelper = ImageFetcher.getInstance(this);
		initBucketData();

		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new ImageBucketAdapter(this, mDataList);
		mListView.setAdapter(mAdapter);
		initView();
	}
	
	private void initBucketData()
	{
		mDataList = mHelper.getImagesBucketList(false);
	}

	private void initView()
	{

		mListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				selectOne(position);

				Intent intent = new Intent(ImageBucketChooseActivity.this, ImageChooseActivity.class);
				intent.putExtra(IntentConstants.EXTRA_IMAGE_LIST, (Serializable) mDataList.get(position).imageList);
				intent.putExtra(IntentConstants.EXTRA_BUCKET_NAME, mDataList.get(position).bucketName);
				ServiceInterface.showImageChooseActivity(ImageBucketChooseActivity.this, intent);
			}
		});
	}
	
	private void selectOne(int position)
	{
		int size = mDataList.size();
		for (int i = 0; i != size; i++)
		{
			if (i == position)
				mDataList.get(i).selected = true;
			else
			{
				mDataList.get(i).selected = false;
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	

}
