package com.elex.chatservice.view.allianceshare;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AllianceShareCommentListActivity extends MyActionBarActivity
{
	
	protected CommentListAdapter	adapter				= null;
	protected boolean				adjustSizeCompleted	= false;
	private ListView				commentListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(false, true, this);
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__alliance_share_comment_list_fragment, fragmentLayout, true);
		
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(allianceShareBtn);
		commentListView = (ListView) findViewById(R.id.commentListView);
		adapter = new CommentListAdapter(this);
		commentListView.setAdapter(adapter);
		commentListView.setVerticalFadingEdgeEnabled(false);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case AllianceShareManager.TAKE_PICTURE:
				if (AllianceShareManager.getInstance().getSelectedImageSize() < AllianceShareManager.SHARE_NUM_LIMIT && resultCode == RESULT_OK
						&& StringUtils.isNotEmpty(AllianceShareActivity.currentPhotoPath))
				{
					ImageItem item = new ImageItem();
					item.sourcePath = AllianceShareActivity.currentPhotoPath;
					AllianceShareManager.getInstance().putSelectedImage(item.sourcePath, item);
					ServiceInterface.showPublishAllianceShareActivity(this, true);
				}
				break;
		}
	}
	
	public void notifyDataChanged()
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onDestroy()
	{
		AllianceShareManager.getInstance().clearNoticeData();
		super.onDestroy();
	}

	public class CommentListAdapter extends BaseAdapter
	{
		private MyActionBarActivity			activity;
		private LayoutInflater				inflater;
		private List<AllianceShareComment>	commentList;

		public CommentListAdapter(MyActionBarActivity context)
		{
			activity = context;
			this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
			commentList = AllianceShareManager.getInstance().getAllianceShareNoticeData();
		}

		@Override
		public int getCount()
		{
			if (commentList != null)
				return commentList.size();
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			if (commentList != null && commentList.size() > 0 && position >= 0 && position < commentList.size())
				return commentList.get(position);
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

			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.cs__alliance_share_comment_list_item, null);
				adjustSize(convertView);
			}
			
			final AllianceShareComment item = (AllianceShareComment) getItem(position);
			if (item == null)
				return convertView;

			RelativeLayout comment_item_layout = ViewHolderHelper.get(convertView, R.id.comment_item_layout);
			if (comment_item_layout != null)
			{
				comment_item_layout.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						if(AllianceShareManager.getInstance().isAllianceShareInfoExist(item.getFid()))
						{
							Intent intent = new Intent(activity, AllianceShareDetailActivity.class);
							intent.putExtra(AllianceShareManager.ALLIANCE_DETAIL_FID, item.getFid());
							ServiceInterface.showAllianceShareDetailActivity(activity, intent);
						}
						else
						{
							Toast.makeText(activity, "The alliance share has been deleted !", Toast.LENGTH_SHORT);
						}
					}
				});
			}

			setPlayerData(convertView, item);

			TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
			ImageView likeImage = ViewHolderHelper.get(convertView, R.id.likeImage);
			if (item.isComment())
			{
				if (commentText != null)
				{
					commentText.setVisibility(View.VISIBLE);
					commentText.setText(item.getMsg());
				}
				if (likeImage != null)
				{
					likeImage.setVisibility(View.GONE);
				}
			}
			else if (item.isLike())
			{
				if (commentText != null)
				{
					commentText.setVisibility(View.GONE);
				}
				if (likeImage != null)
				{
					likeImage.setVisibility(View.VISIBLE);
				}
			}

			TextView timeLabel = ViewHolderHelper.get(convertView, R.id.timeLabel);
			if (timeLabel != null)
				timeLabel.setText(TimeManager.getReadableTime(item.getTime()));

			TextView shareMsg = ViewHolderHelper.get(convertView, R.id.shareMsg);
			ImageView shareImage = ViewHolderHelper.get(convertView, R.id.shareImage);
			AllianceShareInfo info = AllianceShareManager.getInstance().getAllianceShareInfoById(item.getFid());
			if (info != null)
			{
				if (info.getData() != null && info.getData().size() > 0)
				{
					if (shareMsg != null)
					{
						shareMsg.setVisibility(View.GONE);
					}
					if (shareImage != null)
					{
						final AllianceShareImageData imageData = info.getData().get(0);
						if (imageData != null && StringUtils.isNotEmpty(imageData.getUrl()))
						{
							shareImage.setVisibility(View.VISIBLE);
							// shareImage.setTag(imageData.getUrl());
							ImageUtil.loadAllianeShareThumbImage(activity, shareImage, imageData, true);
//							ImageUtil.loadAllianeShareImage(activity, shareImage, imageData, true);
							// ImageUtil.setAllianeShareImage(activity,
							// shareImage, imageData,true);
						}
						else
						{
							shareImage.setVisibility(View.GONE);
						}
					}
				}
				else
				{
					if (shareMsg != null)
					{
						shareMsg.setVisibility(View.VISIBLE);
						shareMsg.setText(info.getMsg());
					}
					if (shareImage != null)
					{
						shareImage.setVisibility(View.GONE);
					}
				}
			}

			return convertView;
		}

		private void setPlayerData(View convertView, AllianceShareComment comment)
		{
			TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
			if (vipLabel != null)
				vipLabel.setText(comment.getVipLabel());

			TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
			if (allianceLabel != null)
			{
				if (ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_ALLIANCE)
					allianceLabel.setText(comment.getAllianceLabel());
				else
					allianceLabel.setText("");
			}

			TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
			if (nameLabel != null)
			{
				nameLabel.setText(comment.getName());
			}

			ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
			if (headImage != null)
			{
				ImageUtil.setHeadImage(activity, comment.getHeadPic(), headImage, comment.getUser());
			}

		}

		private void adjustSize(View convertView)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				adjustTextSize(convertView);
				adjustHeadImageContainerSize(convertView);
				adjustRightLayoutSize(convertView);
			}
		}
		
		private void adjustRightLayoutSize(View convertView)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				LinearLayout rightlayout = ViewHolderHelper.get(convertView, R.id.rightlayout);
				int width = (int) (ScaleUtil.dip2px(activity, 70) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				if (rightlayout != null)
				{
					RelativeLayout.LayoutParams rightlayoutLayoutParams = (RelativeLayout.LayoutParams) rightlayout
							.getLayoutParams();
					rightlayoutLayoutParams.width = width;
					rightlayoutLayoutParams.height = width;
					rightlayout.setLayoutParams(rightlayoutLayoutParams);
				}
			}
		}

		private void adjustHeadImageContainerSize(View convertView)
		{
			if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
				int width = (int) (ScaleUtil.dip2px(activity, 60) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				if (headImageContainer != null)
				{
					RelativeLayout.LayoutParams headImageContainerLayoutParams = (RelativeLayout.LayoutParams) headImageContainer
							.getLayoutParams();
					headImageContainerLayoutParams.width = width;
					headImageContainerLayoutParams.height = width;
					headImageContainer.setLayoutParams(headImageContainerLayoutParams);
				}

				ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
				if (headImage != null)
				{
					FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) headImage.getLayoutParams();
					headImageLayoutParams.width = width;
					headImageLayoutParams.height = width;
					headImage.setLayoutParams(headImageLayoutParams);
				}
			}
		}

		private void adjustTextSize(View convertView)
		{
			TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
			if (vipLabel != null)
				ScaleUtil.adjustTextSize(vipLabel, ConfigManager.scaleRatio);

			TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
			if (allianceLabel != null)
				ScaleUtil.adjustTextSize(allianceLabel, ConfigManager.scaleRatio);

			TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
			if (nameLabel != null)
				ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);

			TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
			if (commentText != null)
				ScaleUtil.adjustTextSize(commentText, ConfigManager.scaleRatio);

			TextView timeLabel = ViewHolderHelper.get(convertView, R.id.timeLabel);
			if (timeLabel != null)
				ScaleUtil.adjustTextSize(timeLabel, ConfigManager.scaleRatio);
		}

	}
}