package com.elex.chatservice.view.allianceshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.adapter.AllianceShareGridViewApater;
import com.elex.chatservice.view.allianceshare.adapter.HeadImageGridAdapter;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AllianceShareDetailActivity extends MyActionBarActivity
{
	
	protected CommentAdapter			adapter					= null;
	private RelativeLayout				comment_input_layout;
	private EditText					comment_edit;
	private Button						sendCommentBtn;
	private PopupWindows				deleltePopupMenu		= null;
	protected boolean					adjustSizeCompleted		= false;
	private RelativeLayout				allianceshare_layout;

	private GridView					imageGridView;
	private AllianceShareGridViewApater	imageGridAdapter;
	private GridView					likeGridView;
	private HeadImageGridAdapter		likeAdapter;
	private RelativeLayout				comment_layout;
	private RelativeLayout				like_layout;
	private ImageView					comment_like_divider;

	private PopupWindow					mMorePopupWindow		= null;
	private int							mShowMorePopupWindowWidth;
	private int							mShowMorePopupWindowHeight;
	private TextView					nameLabel;
	private TextView					messageText;
	private TextView					timeLabel;
	private ImageView					headImage;
	private FrameLayout					headImageContainer;
	private TextView					deleteButton;
	private Button						commentButton;
	private AllianceShareInfo			allianceShareInfo;
	private LayoutInflater				inflater;
	private ListView					comment_listView;
	private String						currentAtUid			= "";
	private String						currentFid				= "";

	private String						currentAllianceShareId	= "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(false, true, this);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		super.onCreate(savedInstanceState);
		
		inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.cs__alliance_share_detail_fragment, fragmentLayout, true);
		
		titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_SHARE));
		showRightBtn(allianceShareBtn);
		
		currentAllianceShareId = getIntent().getStringExtra(AllianceShareManager.ALLIANCE_DETAIL_FID);
		System.out.println("currentAllianceShareId:" + currentAllianceShareId);
		allianceShareInfo = AllianceShareManager.getInstance().getAllianceShareInfoById(currentAllianceShareId);


		nameLabel = (TextView) findViewById(R.id.nameLabel);
		headImage = (ImageView) findViewById(R.id.headImage);
		comment_like_divider = (ImageView) findViewById(R.id.comment_like_divider);
		comment_listView = (ListView) findViewById(R.id.comment_listView);
		comment_layout = (RelativeLayout) findViewById(R.id.comment_layout);
		
		comment_listView.setVerticalFadingEdgeEnabled(false);
		messageText = (TextView) findViewById(R.id.messageText);
		headImageContainer = (FrameLayout) findViewById(R.id.headImageContainer);
		timeLabel = (TextView) findViewById(R.id.timeLabel);
		deleteButton = (TextView) findViewById(R.id.deleteButton);
		deleteButton.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_DELETE));
		commentButton = (Button) findViewById(R.id.commentButton);
		commentButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showCommentLikeMenu(v, allianceShareInfo);
			}
		});

		allianceshare_layout = (RelativeLayout) findViewById(R.id.allianceshare_layout);
		comment_input_layout = (RelativeLayout) findViewById(R.id.comment_input_layout);
		comment_input_layout.setVisibility(View.INVISIBLE);
		allianceshare_layout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				resetState();
			}
		});
		
		comment_listView.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					resetState();
				return false;
			}
		});
		
		comment_layout = (RelativeLayout) findViewById(R.id.comment_layout);
		like_layout = (RelativeLayout) findViewById(R.id.like_layout);

		comment_edit = (EditText) findViewById(R.id.comment_edit);
		sendCommentBtn = (Button) findViewById(R.id.sendCommentBtn);
		sendCommentBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (comment_edit.getText().length() > 0)
				{
					System.out.println("评论了: " + comment_edit.getText().toString());
					JniController.getInstance().excuteJNIVoidMethod(
							"sendAllianceCircleCommand",
							new Object[] {
									"",
									Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_COMMENT),
									comment_edit.getText().toString(),
									"",
									currentFid,
									currentAtUid,
									currentAtUid,
									"alliance" });
				}
				resetState();
			}
		});

		imageGridView = (GridView) findViewById(R.id.imageGridView);

		likeGridView = (GridView) findViewById(R.id.likeGridView);
		
		likeGridView.setOnTouchListener(new OnTouchListener()
		{
			
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					resetState();
				return false;
			}
		});

		comment_edit.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hidePopupMenu();
				return false;
			}
		});

		deleteButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				MenuController.showAllianceShareDeleteComfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_DELETE), allianceShareInfo);
			}
		});

		commentButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showCommentLikeMenu(v, allianceShareInfo);
			}
		});

		if (allianceShareInfo != null)
		{
			if (nameLabel != null)
			{
				nameLabel.setText(allianceShareInfo.getName());
			}

			if (headImage != null)
			{
				ImageUtil.setHeadImage(this, allianceShareInfo.getHeadPic(), headImage, allianceShareInfo.getUser());
			}
			messageText.setText(allianceShareInfo.getMsg());
			timeLabel.setText(TimeManager.getReadableTime(allianceShareInfo.getTime()));
			if (deleteButton != null)
			{
				if (allianceShareInfo.isSelfAllianceShare())
				{
					deleteButton.setVisibility(View.VISIBLE);
				}
				else
				{
					deleteButton.setVisibility(View.GONE);
				}

			}

			if (commentButton != null)
			{
				if (deleteButton != null)
				{
					int textSize = (int) (deleteButton.getTextSize());
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) commentButton.getLayoutParams();
					params.height = textSize + 5;
					params.width = (int) (params.height * 38 / 26.0f);
					commentButton.setLayoutParams(params);
				}
			}
			
			if (allianceShareInfo.getData() != null && allianceShareInfo.getData().size() > 0)
			{
				imageGridView.setVisibility(View.VISIBLE);
				imageGridAdapter = new AllianceShareGridViewApater(this, allianceShareInfo);
				AllianceShareManager.adjustGridViewSize(imageGridView, allianceShareInfo.getData().size());
				imageGridView.setAdapter(imageGridAdapter);
				imageGridView.setOnItemClickListener(new OnItemClickListener()
				{

					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						Intent intent = new Intent(AllianceShareDetailActivity.this, ImagePagerActivity.class);
						intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
						intent.putExtra(IntentConstants.EXTRA_DETAIL_IMAGE_LIST, (Serializable) allianceShareInfo.getData());
						ServiceInterface.showImagePagerActivity(AllianceShareDetailActivity.this, intent);
					}

				});
			}
			else
			{
				imageGridView.setVisibility(View.GONE);
			}

			refreshCommentData();

		}
		
		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				System.out.println("OnGlobalLayoutListener");
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
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
	
	public static void onAllianceShareDataChanged()
	{
		if (ChatServiceController.getAllianceShareDetailActivity() != null)
		{
			try
			{
				ChatServiceController.getAllianceShareDetailActivity().runOnUiThread(new Runnable()
				{
					
					@Override
					public void run()
					{
						ChatServiceController.getAllianceShareDetailActivity().refreshCommentData();
					}
				}); 
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}
	
	private void refreshCommentData()
	{
		allianceShareInfo = AllianceShareManager.getInstance().getAllianceShareInfoById(currentAllianceShareId);
		if (allianceShareInfo!=null && allianceShareInfo.getLike() != null && allianceShareInfo.getLike().size() > 0)
		{
			like_layout.setVisibility(View.VISIBLE);
			likeGridView.setVisibility(View.VISIBLE);
			List<String> uidList = new ArrayList<String>();
			for (AllianceShareComment like : allianceShareInfo.getLike())
			{
				if (like != null && StringUtils.isNotEmpty(like.getSender()))
					uidList.add(like.getSender());
			}
			likeAdapter = new HeadImageGridAdapter(this, uidList);
			adjustLikeGridViewSize(likeGridView);
			likeGridView.setAdapter(likeAdapter);
		}
		else
		{
			like_layout.setVisibility(View.GONE);
			likeGridView.setVisibility(View.GONE);
		}
		
		if (allianceShareInfo != null && allianceShareInfo.getComment() != null && allianceShareInfo.getComment().size() > 0)
		{
			comment_layout.setVisibility(View.VISIBLE);
			adapter = new CommentAdapter(this);
			comment_listView.setAdapter(adapter);
			comment_like_divider.setVisibility(View.VISIBLE);
		}
		else
		{
			comment_layout.setVisibility(View.GONE);
			comment_like_divider.setVisibility(View.GONE);
		}
	}
	
	private void adjustCommentBtnSize(TextView deleteButton, Button commentButton)
	{
		if (commentButton != null && deleteButton != null)
		{
			int textSize = (int) (deleteButton.getTextSize());
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) commentButton.getLayoutParams();
			params.height = textSize + ScaleUtil.sp2px(10);
			params.width = (int) (params.height * 38 / 26.0f);
			commentButton.setLayoutParams(params);
		}
	}
	
	private void adjustLikeGridViewSize(GridView gridView)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			int width = (int) (ScaleUtil.dip2px(this, 60) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
			gridView.setColumnWidth(width);
		}
	}

	private final static int	sendButtonBaseWidth		= 51;
	private final static int	sendButtonBaseHeight	= 50;

	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (sendCommentBtn.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (!adjustSizeCompleted && sendCommentBtn.getWidth() != 0)
		{

			int width = (int) (ScaleUtil.dip2px(this, 60) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
			if (headImageContainer != null)
			{
				RelativeLayout.LayoutParams headImageContainerLayoutParams = (RelativeLayout.LayoutParams) headImageContainer
						.getLayoutParams();
				headImageContainerLayoutParams.width = width;
				headImageContainerLayoutParams.height = width;
				headImageContainer.setLayoutParams(headImageContainerLayoutParams);
			}

			if (headImage != null)
			{
				FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) headImage.getLayoutParams();
				headImageLayoutParams.width = width;
				headImageLayoutParams.height = width;
				headImage.setLayoutParams(headImageLayoutParams);
			}

			ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(deleteButton, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(messageText, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(timeLabel, ConfigManager.scaleRatio);

			adjustCommentBtnSize(deleteButton, commentButton);

			RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) sendCommentBtn.getLayoutParams();
			layoutParams1.width = (int) (sendButtonBaseWidth * ConfigManager.scaleRatioButton);
			layoutParams1.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			sendCommentBtn.setLayoutParams(layoutParams1);

			RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) comment_edit.getLayoutParams();
			layoutParams2.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			comment_edit.setLayoutParams(layoutParams2);

			ScaleUtil.adjustTextSize(comment_edit, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
	}

	public void resetState()
	{
		hideSoftKeyBoard();
		hideCommentLikeMenu();
		hidePopupMenu();
		hideCommentInputLayout();
	}

	public void notifyDataChanged()
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if (adapter != null)
					adapter.notifyDataSetChanged();
			}
		});
	}

	public void showCommentInputLayout(String currentFid, String currentAtUid, String replayName)
	{
		System.out.println("showCommentInputLayout currentFid:" + currentFid + "  currentAtUid:" + currentAtUid);
		comment_input_layout.setVisibility(View.VISIBLE);
		comment_edit.setText("");
		comment_edit.requestFocus();
		showSoftKeyBoard(comment_edit);
		this.currentAtUid = currentAtUid;
		this.currentFid = currentFid;
		if (StringUtils.isEmpty(replayName))
		{
			comment_edit.setHint(LanguageManager.getLangByKey(LanguageKeys.BTN_COMMENT));
		}
		else
		{
			comment_edit.setHint(LanguageManager.getLangByKey(LanguageKeys.BTN_REPLY) + replayName + ":");
		}
	}

	public void hideCommentInputLayout()
	{
		if (comment_input_layout != null && comment_input_layout.getVisibility() != View.INVISIBLE)
			comment_input_layout.setVisibility(View.INVISIBLE);
		resetCurrentCommentInfo();
	}

	private void resetCurrentCommentInfo()
	{
		currentAtUid = "";
		currentFid = "";
	}

	public void hideAllianceShareCommentDeletePopupWindow()
	{
		if (deleltePopupMenu != null && deleltePopupMenu.isShowing())
			deleltePopupMenu.dismiss();
	}

	private void showCommentLikeMenu(View commentBtnView, final AllianceShareInfo info)
	{
		if (mMorePopupWindow == null)
		{
			View content = inflater.inflate(R.layout.layout_comment, null, false);

			mMorePopupWindow = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mMorePopupWindow.setOutsideTouchable(true);
			mMorePopupWindow.setTouchable(true);

			content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			mShowMorePopupWindowWidth = content.getMeasuredWidth();
			mShowMorePopupWindowHeight = content.getMeasuredHeight();

			View parent = mMorePopupWindow.getContentView();

			TextView like = (TextView) parent.findViewById(R.id.menu_like);
			if(!info.hasLiked())
				like.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_LOVE));
			else
				like.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ABORT_LOVE));
			TextView comment = (TextView) parent.findViewById(R.id.menu_comment);
			comment.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COMMENT));

			like.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					hideCommentLikeMenu();
					String selfLikeId = info.getSelfLikeId();
					if(StringUtils.isEmpty(selfLikeId))
					{
						String noticeId = info.getSender().equals(UserManager.getInstance().getCurrentUserId()) ? "" : info.getSender();
						JniController.getInstance().excuteJNIVoidMethod(
								"sendAllianceCircleCommand",
								new Object[] {
										"",
										Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_LIKE),
										"",
										"",
										info.getId(),
										"",
										noticeId,
										"alliance" });
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod(
								"deleteAllianceShare",
								new Object[] {
										selfLikeId,
										UserManager.getInstance().getCurrentUser().allianceId,
										info.getId(),
										info.getSender(),
										Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_LIKE),
										info.getSender()});
					}
				}
			});

			comment.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					hideCommentLikeMenu();
					showCommentInputLayout(info.getId(), info.getSender(), "");
				}
			});
		}

		if (mMorePopupWindow.isShowing())
		{
			mMorePopupWindow.dismiss();
		}
		else
		{
			int heightMoreBtnView = commentBtnView.getHeight();
			mMorePopupWindow.showAsDropDown(commentBtnView, -mShowMorePopupWindowWidth,
					-(mShowMorePopupWindowHeight + heightMoreBtnView) / 2);
		}
	}

	private void hideCommentLikeMenu()
	{
		if (mMorePopupWindow != null && mMorePopupWindow.isShowing())
			mMorePopupWindow.dismiss();
	}

	@Override
	public void onDestroy()
	{
		resetCurrentCommentInfo();
		super.onDestroy();
	}

	public void showAllianceShareCommentDeletePopupWindow(View parent, AllianceShareComment comment, String allianceShareSender)
	{
		deleltePopupMenu = new PopupWindows(this, comment, allianceShareSender);
		deleltePopupMenu.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}

	public class CommentAdapter extends BaseAdapter
	{
		private MyActionBarActivity	activity;
		private LayoutInflater		inflater;

		public CommentAdapter(MyActionBarActivity context)
		{
			activity = context;
			this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
		}

		@Override
		public int getCount()
		{
			if (allianceShareInfo != null && allianceShareInfo.getComment() != null)
				return allianceShareInfo.getComment().size();
			return 0;
		}

		@Override
		public Object getItem(int position)
		{
			if (allianceShareInfo != null && allianceShareInfo.getComment() != null && allianceShareInfo.getComment().size() > 0
					&& position >= 0 && position < allianceShareInfo.getComment().size())
				return allianceShareInfo.getComment().get(position);
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
				convertView = inflater.inflate(R.layout.cs__alliance_share_comment_item, null);
				adjustSize(convertView);
			}
			
			final AllianceShareComment item = (AllianceShareComment) getItem(position);
			if (item == null)
				return convertView;

			setPlayerData(convertView, item);
			
			RelativeLayout comment_item_layout = ViewHolderHelper.get(convertView, R.id.comment_item_layout);
			comment_item_layout.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					resetState();
				}
			});

			TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
			if (commentText != null)
			{
				commentText.setText(item.getMsg());
				commentText.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						if (item.isSelfAllianceShareComment())
						{
							showAllianceShareCommentDeletePopupWindow(v, item, allianceShareInfo.getSender());
						}
						else
						{
							showCommentInputLayout(item.getFid(), item.getSender(), item.getName());
						}
					}
				});
			}

			TextView timeLabel = ViewHolderHelper.get(convertView, R.id.timeLabel);
			if (timeLabel != null)
				timeLabel.setText(TimeManager.getReadableTime(item.getTime()));

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