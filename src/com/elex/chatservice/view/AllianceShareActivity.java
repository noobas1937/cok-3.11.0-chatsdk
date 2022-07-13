package com.elex.chatservice.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.ImagePagerActivity;
import com.elex.chatservice.view.allianceshare.PopupWindows;
import com.elex.chatservice.view.allianceshare.adapter.PublicGridAdapter;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

public class AllianceShareActivity extends MyActionBarActivity
{

	private EditText			share_edittext;
	private GridView			share_image_gridview;
	private PublicGridAdapter	mAdapter;
	private PopupWindows		popupMenu;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__alliance_share_fragment, fragmentLayout, true);

		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(null);

		share_edittext = (EditText) findViewById(R.id.share_edittext);
		share_edittext.setHint(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE_HINT) + "...");

		share_edittext.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendState();
				hidePopupMenu();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});

		share_image_gridview = (GridView) findViewById(R.id.share_image_gridview);

		fragmentLayout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				System.out.println("fragmentLayout.setOnTouchListener");
				hideSoftKeyBoard();
				hidePopupMenu();
				return false;
			}
		});
		initView();

		allianceShareSend.setVisibility(View.VISIBLE);
		setSendBtnEnable(allianceShareSend, false);

		allianceShareSend.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				hideSoftKeyBoard();
				if (canPublic())
				{
					String imageDataJson = uploadAllImage();
					System.out.println("id:" + "" + "  type:" + Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_MSG) + "    msg:"
							+ share_edittext.getText().toString() + "   auth:alliance");
					int type = StringUtils.isNotEmpty(imageDataJson) ? AllianceShareManager.ALLIANCE_SHARE_IMAGE
							: AllianceShareManager.ALLIANCE_SHARE_MSG;
					JniController.getInstance().excuteJNIVoidMethod(
							"sendAllianceCircleCommand",
							new Object[] {
									"",
									Integer.valueOf(type),
									share_edittext.getText().toString(),
									imageDataJson,
									"",
									"",
									"",
									"alliance" });
					ServiceInterface.showAllianceShareListActivity(AllianceShareActivity.this);
				}
			}
		});
		
		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		
	}

	private boolean adjustSizeCompleted = false;
	
	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (allianceShareSend.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (!adjustSizeCompleted && allianceShareSend.getWidth() != 0)
		{

			float newTextSize = (int) (share_edittext.getTextSize() * ConfigManager.scaleRatio);
			share_edittext.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
			
			if(!ScaleUtil.isPad())
			{
				share_image_gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			}
			else
			{
				int screenWidth = ScaleUtil.getScreenWidth();
				int gridWidth = screenWidth > 1080 ? 1080 : screenWidth;
				int columWidth = (gridWidth - ScaleUtil.dip2px(10) - ScaleUtil.dip2px(15) - 3* ScaleUtil.dip2px(10))/4;
				share_image_gridview.setStretchMode(GridView.NO_STRETCH);
				share_image_gridview.setColumnWidth(columWidth);
			}
			
			adjustSizeCompleted = true;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		System.out.println("AllianceShareActivity onDestroy");
		AllianceShareManager.getInstance().clearSelectedImages();
	}

	public static String	currentPhotoPath	= "";

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("onActivityResult");
		switch (requestCode)
		{
			case AllianceShareManager.TAKE_PICTURE:
				if (AllianceShareManager.getInstance().getSelectedImageSize() < AllianceShareManager.SHARE_NUM_LIMIT
						&& resultCode == RESULT_OK && StringUtils.isNotEmpty(currentPhotoPath))
				{
					ImageItem item = new ImageItem();
					item.sourcePath = currentPhotoPath;
					AllianceShareManager.getInstance().putSelectedImage(item.sourcePath, item);
				}
				break;
		}
	}

	@Override
	public void onResume()
	{
		System.out.println("AllianceShareFragment onResume");
		super.onResume();
		notifyDataChanged(); // 当在ImageZoomActivity中删除图片时，返回这里需要刷新
		refreshSendState();
	}

	private void notifyDataChanged()
	{
		mAdapter.refreshAdapter();
	}

	private String uploadAllImage()
	{
		Map<String, ImageItem> itemMap = AllianceShareManager.getInstance().getSelectedImages();
		if (itemMap != null)
		{
			Set<String> urlSet = itemMap.keySet();
			List<AllianceShareImageData> imageDataList = new ArrayList<AllianceShareImageData>();
			for (String url : urlSet)
			{
				System.out.println("uploadAllImage url:" + url);
				AllianceShareImageData imageData = uploadImage(url);
				if (imageData != null)
					imageDataList.add(imageData);
			}

			try
			{
				String imageDataJson = JSON.toJSONString(imageDataList);
				return imageDataJson;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}

	public AllianceShareImageData uploadImage(String filePath)
	{
		if (StringUtils.isEmpty(filePath))
		{
			Toast.makeText(AllianceShareActivity.this, "Could not find the filepath of the selected file", Toast.LENGTH_LONG).show();
			return null;
		}

		AllianceShareImageData imageData = new AllianceShareImageData();
		String fileName = UserManager.getInstance().getCurrentUserId() + System.currentTimeMillis() + ".jpeg";

		String targetFileName = AllianceShareManager.getInstance().getLocalAllianceShareImagePath(fileName);
		File file = ImageUtil.compressImage(filePath, targetFileName);
		if(file!=null)
			AllianceShareManager.getInstance().uploadImage(file, fileName);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(targetFileName, options);
		int height = options.outHeight;
		int width = options.outWidth;

		imageData.setUrl(fileName);
		imageData.setW("" + width);
		imageData.setH("" + height);

		return imageData;

	}

	private void refreshSendState()
	{
		if (canPublic())
			setSendBtnEnable(allianceShareSend, true);
		else
			setSendBtnEnable(allianceShareSend, false);
	}

	private boolean canPublic()
	{
		return share_edittext.getText().length() > 0 || AllianceShareManager.getInstance().getSelectedImages().size() > 0;
	}

	private void initView()
	{

		mAdapter = new PublicGridAdapter(this);
		share_image_gridview.setAdapter(mAdapter);

		share_image_gridview.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				hideSoftKeyBoard();
				if (position == AllianceShareManager.getInstance().getSelectedImageSize())
				{
					// showPhotoPopup();
					if (popupMenu == null)
						popupMenu = new PopupWindows(AllianceShareActivity.this);
					if (!popupMenu.isShowing())
						popupMenu.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
				}
				else
				{
					Intent intent = new Intent(AllianceShareActivity.this, ImagePagerActivity.class);
					intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
					ServiceInterface.showImagePagerActivity(AllianceShareActivity.this, intent);
					// ServiceInterface.showImageZoomActivity(activity, intent);
				}

			}

		});
	}
	
	@Override
	public void onBackButtonClick()
	{
		if(canPublic())
			MenuController.showExitAllianceShareConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ABORT_EDIT),this);
		else
			super.onBackButtonClick();
	}

	public void hidePopupMenu()
	{
		if (popupMenu != null)
			popupMenu.dismiss();
	}
}