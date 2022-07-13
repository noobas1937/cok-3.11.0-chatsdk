package com.elex.chatservice.view.allianceshare;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.adapter.ImageGridAdapter;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 图片选择
 * 
 */
public class ImageChooseActivity extends MyActionBarActivity
{
	
	private List<ImageItem>		imageList	= new ArrayList<ImageItem>();
	private String				bucketName;
	private GridView			gridView;
	private ImageGridAdapter	imageGridAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(false, true, this);

		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__image_choose_fragment, fragmentLayout, true);
		
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(imageChooseComfirmButton);
		
		imageList = (List<ImageItem>) getIntent().getSerializableExtra(IntentConstants.EXTRA_IMAGE_LIST);
		if (imageList == null)
			imageList = new ArrayList<ImageItem>();
		bucketName = getIntent().getStringExtra(IntentConstants.EXTRA_BUCKET_NAME);

		if (StringUtils.isEmpty(bucketName))
		{
			bucketName = "请选择";
		}
		titleLabel.setText(bucketName);
		initView();
		initListener();
	}
	
	private void initView()
	{
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		imageGridAdapter = new ImageGridAdapter(this, imageList);
		gridView.setAdapter(imageGridAdapter);
		imageChooseComfirmButton.setText("完成" + "(" + AllianceShareManager.getInstance().getSelectedImages().size() + "/" + AllianceShareManager.SHARE_NUM_LIMIT + ")");
		imageGridAdapter.notifyDataSetChanged();
	}

	private void initListener()
	{
		imageChooseComfirmButton.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				ServiceInterface.showPublishAllianceShareActivity(ImageChooseActivity.this, true);
			}

		});

		gridView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				ImageItem item = imageList.get(position);
				if (item.isSelected)
				{
					item.isSelected = false;
					AllianceShareManager.getInstance().removeSelectedImage(item.sourcePath);;
				}
				else
				{
					if (AllianceShareManager.getInstance().getSelectedImageSize() >= AllianceShareManager.SHARE_NUM_LIMIT)
					{
						Toast.makeText(ImageChooseActivity.this, "最多选择" + AllianceShareManager.SHARE_NUM_LIMIT + "张图片", Toast.LENGTH_SHORT).show();
						return;
					}
					item.isSelected = true;
					AllianceShareManager.getInstance().putSelectedImage(item.sourcePath, item);
				}

				imageChooseComfirmButton.setText("完成" + "(" + AllianceShareManager.getInstance().getSelectedImageSize() + "/" + AllianceShareManager.SHARE_NUM_LIMIT + ")");
				imageGridAdapter.notifyDataSetChanged();
			}

		});

	}
}