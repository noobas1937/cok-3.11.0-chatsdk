package com.elex.chatservice.view.allianceshare.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.allianceshare.AllianceShareListActivity;
import com.elex.chatservice.view.allianceshare.ImagePagerActivity;
import com.elex.chatservice.view.allianceshare.NoScrollGridView;
import com.elex.chatservice.view.allianceshare.NoScrollListView;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

public class AllianceShareListAdapter extends BaseAdapter
{
	private static final int			VIEW_ITEM_NORMAL	= 0;
//	private static final int			VIEW_ITEM_NORMAL_1	= 1;
//	private static final int			VIEW_ITEM_NORMAL_2	= 2;
//	private static final int			VIEW_ITEM_NORMAL_3	= 3;
//	private static final int			VIEW_ITEM_NORMAL_4	= 4;
//	private static final int			VIEW_ITEM_NORMAL_5	= 5;
//	private static final int			VIEW_ITEM_NORMAL_6	= 6;
//	private static final int			VIEW_ITEM_NORMAL_7	= 7;
//	private static final int			VIEW_ITEM_NORMAL_8	= 8;
//	private static final int			VIEW_ITEM_NORMAL_9	= 9;
	private static final int			VIEW_ITEM_TIP		= 1;

	protected AllianceShareListActivity	activity;
	public List<AllianceShareInfo>		list				= new ArrayList<AllianceShareInfo>();
	private LayoutInflater				inflater;
	public boolean						isLoadingMore;
	private PopupWindow					mMorePopupWindow	= null;
	private View						mPopupMenuView		= null;
	private int							mShowMorePopupWindowWidth;
	private int							mShowMorePopupWindowHeight;

	public AllianceShareListAdapter(AllianceShareListActivity context)
	{
		this.activity = context;
		this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
		this.list = AllianceShareManager.getInstance().getAllianceShareData();
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position == 0)
			return VIEW_ITEM_TIP;
		else
			return VIEW_ITEM_NORMAL;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	private View createConvertViewByType(int type)
	{
		View view = null;
		if (type == VIEW_ITEM_TIP)
			view = inflater.inflate(R.layout.cs__alliance_share_list_item_tip, null);
		else if (type == VIEW_ITEM_NORMAL)
			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal, null);
//		else if (type == VIEW_ITEM_NORMAL_1)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_1, null);
//		else if (type == VIEW_ITEM_NORMAL_2)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_2, null);
//		else if (type == VIEW_ITEM_NORMAL_3)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_3, null);
//		else if (type == VIEW_ITEM_NORMAL_4)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_4, null);
//		else if (type == VIEW_ITEM_NORMAL_5)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_5, null);
//		else if (type == VIEW_ITEM_NORMAL_6)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_6, null);
//		else if (type == VIEW_ITEM_NORMAL_7)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_7, null);
//		else if (type == VIEW_ITEM_NORMAL_8)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_8, null);
//		else if (type == VIEW_ITEM_NORMAL_9)
//			view = inflater.inflate(R.layout.cs__alliance_share_list_item_normal_9, null);
		return view;
	}

	public void refreshAllianceShareNotice(View convertView)
	{
		List<AllianceShareComment> list = AllianceShareManager.getInstance().getAllianceShareNoticeData();
		RelativeLayout share_item_layout = ViewHolderHelper.get(convertView, R.id.share_item_layout);
		LinearLayout refresh_layout = ViewHolderHelper.get(convertView, R.id.refresh_layout);
		TextView refreshtip = ViewHolderHelper.get(convertView, R.id.refreshtip);

		if (list != null && list.size() > 0)
		{
			AllianceShareComment comment = list.get(0);
			if (comment != null)
			{
				ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
				if (headImage != null)
				{
					ImageUtil.setHeadImage(activity, comment.getHeadPic(), headImage, comment.getUser());
				}

			}

			if (share_item_layout != null)
				share_item_layout.setVisibility(View.VISIBLE);
			if (refresh_layout != null)
			{
				refresh_layout.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						ServiceInterface.showAllianceShareCommentListActivity(activity);
					}
				});

			}
			if (refreshtip != null)
				refreshtip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NEW_ALLIANCE_SHARE, "" + list.size()));
		}
		else
		{
			if (share_item_layout != null)
				share_item_layout.setVisibility(View.GONE);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		int itemType = getItemViewType(position);
		if (convertView == null)
		{
			convertView = createConvertViewByType(itemType);
			adjustSize(convertView, itemType);
			if (itemType == VIEW_ITEM_TIP)
			{
				LinearLayout toplayout = ViewHolderHelper.get(convertView, R.id.toplayout);
				toplayout.setOnTouchListener(new OnTouchListener()
				{

					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							activity.resetState();
						return false;
					}
				});
			}
			else
			{
//				adjustImageGridSize(convertView, itemType);
				RelativeLayout share_item_layout = ViewHolderHelper.get(convertView, R.id.share_item_layout);
				if(share_item_layout!=null)
				share_item_layout.setOnTouchListener(new OnTouchListener()
				{

					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN)
							activity.resetState();
						return false;
					}
				});
				adjustCommentBtnSize(convertView);
			}
		}
		
		if (itemType == VIEW_ITEM_TIP)
		{
			refreshAllianceShareNotice(convertView);
		}
		else
		{
			
			AllianceShareInfo item = (AllianceShareInfo) getItem(position);
			if (item == null)
				return convertView;
			
			setPlayerData(convertView, item);
			
			TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
			if (messageText != null)
			{
				String msg = item.getMsg();
				if(StringUtils.isNotEmpty(msg) && (msg.contains("http://") || msg.contains("https://")))
					messageText.setAutoLinkMask(Linkify.WEB_URLS);
				else
					messageText.setAutoLinkMask(0);
				messageText.setText(msg);
			}
			
			
			TextView timeLabel = ViewHolderHelper.get(convertView, R.id.timeLabel);
			if (timeLabel != null)
				timeLabel.setText(item.getReadableTime());

			setImageGridData(convertView, item);
			setLikeData(convertView, item);
			setCommentData(convertView, item, position);
			addOnDeleteClickListener(convertView, item);
			addOnCommentClickListener(convertView, item, position);
		}
		return convertView;
	}

	private String getNames(List<AllianceShareComment> commentArray)
	{
		String ret = "";
		for (int i = 0; i < commentArray.size(); i++)
		{
			AllianceShareComment comment = commentArray.get(i);
			if (comment != null && StringUtils.isNotEmpty(comment.getSender()))
			{
				if (StringUtils.isNotEmpty(ret))
					ret += ",";
				ret += comment.getName();
			}
		}
		return ret;
	}

	private void addOnDeleteClickListener(View convertView, final AllianceShareInfo info)
	{
		TextView deleteButton = ViewHolderHelper.get(convertView, R.id.deleteButton);
		if (deleteButton != null)
		{
			if (info.isSelfAllianceShare())
			{
				deleteButton.setVisibility(View.VISIBLE);
			}
			else
			{
				deleteButton.setVisibility(View.GONE);
			}
			deleteButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					MenuController.showAllianceShareDeleteComfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_DELETE), info);
				}
			});
		}
	}

	private void addOnCommentClickListener(final View convertView, final AllianceShareInfo info, final int position)
	{
		final Button commentButton = ViewHolderHelper.get(convertView, R.id.commentButton);
		if (commentButton != null)
		{
			commentButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					showCommentLikeMenu(v, convertView, info, position);
				}
			});
		}
	}

	private void setLikeData(View convertView, AllianceShareInfo info)
	{
		LinearLayout like_layout = ViewHolderHelper.get(convertView, R.id.like_layout);
		if (like_layout != null)
		{
			if (info.getLike() != null)
			{
				TextView loveNames = ViewHolderHelper.get(convertView, R.id.loveNames);
				String likeName = getNames(info.getLike());
				if (loveNames != null && StringUtils.isNotEmpty(likeName))
				{
					like_layout.setVisibility(View.VISIBLE);
					loveNames.setText(likeName);
				}
				else
					like_layout.setVisibility(View.GONE);
			}
			else
			{
				like_layout.setVisibility(View.GONE);
			}
		}
	}

	private void setCommentData(View convertView, AllianceShareInfo info, int position)
	{
		NoScrollListView comment_text_listview = ViewHolderHelper.get(convertView, R.id.comment_text_listview);
		if(comment_text_listview == null || info == null || info.getComment() == null || info.getComment().size() <= 0)
		{
			if(comment_text_listview!=null && comment_text_listview.getVisibility()!=View.GONE)
				comment_text_listview.setVisibility(View.GONE);
			return;
		}
		if(comment_text_listview.getVisibility()!=View.VISIBLE)
			comment_text_listview.setVisibility(View.VISIBLE);
		comment_text_listview.setDivider(null);
		comment_text_listview.setAdapter(new AllianceShareCommentAdapter(activity,  info, position));
//		LinearLayout comment_text_layout = ViewHolderHelper.get(convertView, R.id.comment_text_layout);
//		if (comment_text_layout != null)
//		{
//			if (info == null || info.getComment() == null || info.getComment().size() <= 0)
//			{
//				comment_text_layout.setVisibility(View.GONE);
//				return;
//			}
//			comment_text_layout.setVisibility(View.VISIBLE);
//			boolean hasComment = false;
//			
//			if(comment_text_layout.getChildCount()<=info.getComment().size())
//			{
//				
//				for (int i = 0; i < comment_text_layout.getChildCount(); i++)
//				{
//					View view = comment_text_layout.getChildAt(i);
//					if(view!=null)
//					{
//						if(view.getVisibility()!=View.VISIBLE)
//							view.setVisibility(View.VISIBLE);
//						if(view instanceof TextView)
//						{
//							AllianceShareComment comment = info.getComment().get(i);
//							boolean addCommentSuccess = addCommentText(comment_text_layout, comment, info, position,(TextView)view);
//							hasComment = hasComment || addCommentSuccess;
//						}
//					}
//				}
//				
//				for (int i = comment_text_layout.getChildCount(); i < info.getComment().size(); i++)
//				{
//					AllianceShareComment comment = info.getComment().get(i);
//					boolean addCommentSuccess = addCommentText(comment_text_layout, comment, info, position,null);
//					hasComment = hasComment || addCommentSuccess;
//				}
//			}
//			else
//			{
//				for (int i = 0; i < info.getComment().size(); i++)
//				{
//					View view = comment_text_layout.getChildAt(i);
//					if(view!=null)
//					{
//						if(view.getVisibility()!=View.VISIBLE)
//							view.setVisibility(View.VISIBLE);
//						if(view instanceof TextView)
//						{
//							AllianceShareComment comment = info.getComment().get(i);
//							boolean addCommentSuccess = addCommentText(comment_text_layout, comment, info, position,(TextView)view);
//							hasComment = hasComment || addCommentSuccess;
//						}
//					}
//				}
//				
//				for (int i = info.getComment().size(); i < comment_text_layout.getChildCount(); i++)
//				{
//					View view = comment_text_layout.getChildAt(i);
//					if(view!=null && view.getVisibility()!=View.GONE)
//						view.setVisibility(View.GONE);
//				}
//			}
//			
//			if (!hasComment)
//				comment_text_layout.setVisibility(View.GONE);
//		}
	}

	private boolean addCommentText(final LinearLayout parent, final AllianceShareComment comment, final AllianceShareInfo info,
			final int position,TextView textView)
	{
		if (parent == null || comment == null || StringUtils.isEmpty(comment.getMsg()))
			return false;
		TextView commentText;
		if(textView!=null)
			commentText = textView;
		else
		{	
			commentText = new TextView(activity);
			commentText.setTextColor(0xffe0c6aa);
			commentText.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT)));
			commentText.setTextSize(10);
			ScaleUtil.adjustTextSize(commentText, ConfigManager.scaleRatio);
			parent.addView(commentText);
		}
		final TextView commentTextView = commentText;

		if (StringUtils.isNotEmpty(comment.getAt()))
		{
			String replyName = comment.getAtName();
			String text = comment.getName() + LanguageManager.getLangByKey(LanguageKeys.BTN_REPLY) + replyName + "：" + comment.getMsg();
			SpannableString span = new SpannableString(text);

			span.setSpan(new ForegroundColorSpan(0xffe1994b), 0, comment.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (StringUtils.isNotEmpty(replyName))
			{
				int index = text.indexOf(replyName);
				span.setSpan(new ForegroundColorSpan(0xffe1994b), index, index + replyName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			commentText.setText(span);
		}
		else
		{
			String text = comment.getName() + "：" + comment.getMsg();
			SpannableString span = new SpannableString(text);
			span.setSpan(new ForegroundColorSpan(0xffe1994b), 0, comment.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			commentText.setText(span);
		}
		

		commentText.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (comment.isSelfAllianceShareComment())
				{
					activity.showAllianceShareCommentDeletePopupWindow(v, comment, info.getSender());
				}
				else
				{
					String noticeUid = comment.getSender();
					if (!info.isSelfAllianceShare() && StringUtils.isNotEmpty(info.getSender()) && !info.getSender().equals(noticeUid))
						noticeUid += ("," + info.getSender());

					int commentLayoutHeight = parent.getHeight();
					//System.out.println("commentLayoutHeight:" + commentLayoutHeight);
					final int commentBottom = commentTextView.getBottom();
					//System.out.println("bottom:" + commentBottom);
					int offsetY = commentLayoutHeight - commentBottom + ScaleUtil.dip2px(10);

					activity.showCommentInputLayout(position, offsetY, comment.getFid(), comment.getSender(), noticeUid, comment.getName());
				}
			}
		});
		return true;
	}

	private void setPlayerData(View convertView, AllianceShareInfo info)
	{
		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
		{
			nameLabel.setText(info.getName());
		}

		ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
		if (headImage != null)
		{
			ImageUtil.setHeadImage(activity, info.getHeadPic(), headImage, info.getUser());
		}
	}

	public double getScreenCorrectionFactor()
	{
		int density = activity.getResources().getDisplayMetrics().densityDpi;

		if (density >= DisplayMetrics.DENSITY_XXHIGH)
		{
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	}

	private void adjustSize(View convertView, int type)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);
			adjustHeadImageContainerSize(convertView);
		}
	}


	private void setImageGridData(View convertView, final AllianceShareInfo info)
	{
		NoScrollGridView imageGridView = ViewHolderHelper.get(convertView, R.id.imageGridView);
		if(imageGridView == null)
			return;
		if (info.getData() == null || info.getData().size() <= 0)
		{
			imageGridView.setVisibility(View.GONE);
			return;
		}
		
		if(imageGridView.getVisibility() != View.VISIBLE)
			imageGridView.setVisibility(View.VISIBLE);
		
		int size = info.getData().size();
		AllianceShareManager.adjustGridViewSize(imageGridView, size);
		imageGridView.setAdapter(new AllianceShareGridViewApater(activity, info));
	}

	private void adjustCommentBtnSize(View convertView)
	{
		final Button commentButton = ViewHolderHelper.get(convertView, R.id.commentButton);
		if (commentButton != null)
		{
			TextView deleteButton = ViewHolderHelper.get(convertView, R.id.deleteButton);
			if (deleteButton != null)
			{
				int textSize = (int) (deleteButton.getTextSize());
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) commentButton.getLayoutParams();
				params.height = textSize + ScaleUtil.sp2px(10);
				params.width = (int) (params.height * 38 / 26.0f);
				commentButton.setLayoutParams(params);
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
				LinearLayout.LayoutParams headImageContainerLayoutParams = (LinearLayout.LayoutParams) headImageContainer.getLayoutParams();
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
		TextView refreshtip = ViewHolderHelper.get(convertView, R.id.refreshtip);
		if (refreshtip != null)
			ScaleUtil.adjustTextSize(refreshtip, ConfigManager.scaleRatio);

		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
			ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);

		TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		if (messageText != null)
			ScaleUtil.adjustTextSize(messageText, ConfigManager.scaleRatio);

		TextView timeLabel = ViewHolderHelper.get(convertView, R.id.timeLabel);
		if (timeLabel != null)
			ScaleUtil.adjustTextSize(timeLabel, ConfigManager.scaleRatio);

		TextView deleteButton = ViewHolderHelper.get(convertView, R.id.deleteButton);
		if (deleteButton != null)
		{
			ScaleUtil.adjustTextSize(deleteButton, ConfigManager.scaleRatio);
		}

		TextView loveNames = ViewHolderHelper.get(convertView, R.id.loveNames);
		if (loveNames != null)
			ScaleUtil.adjustTextSize(loveNames, ConfigManager.scaleRatio);
	}

	public void notifyDataSetChangedOnUI()
	{
		if (activity == null)
			return;

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					notifyDataSetChanged();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void destroy()
	{
		list.clear();
		notifyDataSetChanged();
		activity = null;
		list = null;
	}

	@Override
	public int getCount()
	{
		if (list != null)
			return list.size() + 1;
		return 1;
	}

	@Override
	public Object getItem(int position)
	{
		if (list == null || position == 0)
			return null;
		else if (position >= 1 && position <= list.size())
			return list.get(position - 1);
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public void hideCommentLikeMenu()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						if (mMorePopupWindow != null && mMorePopupWindow.isShowing())
							mMorePopupWindow.dismiss();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			});
		}

	}

	private void showCommentLikeMenu(final View commentBtnView, View convertView, final AllianceShareInfo info, final int position)
	{
		if (mMorePopupWindow == null)
		{
			mPopupMenuView = inflater.inflate(R.layout.layout_comment, null, false);

			mMorePopupWindow = new PopupWindow(mPopupMenuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mMorePopupWindow.setOutsideTouchable(true);
			mMorePopupWindow.setTouchable(true);
		}

		View parent = mMorePopupWindow.getContentView();

		TextView like = (TextView) parent.findViewById(R.id.menu_like);
		if (!info.hasLiked())
			like.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_LOVE));
		else
			like.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ABORT_LOVE));

		mPopupMenuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		mShowMorePopupWindowWidth = mPopupMenuView.getMeasuredWidth();
		mShowMorePopupWindowHeight = mPopupMenuView.getMeasuredHeight();

		final TextView comment = (TextView) parent.findViewById(R.id.menu_comment);
		comment.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COMMENT));

		like.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				hideCommentLikeMenu();
				String selfLikeId = info.getSelfLikeId();
				if (StringUtils.isEmpty(selfLikeId))
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
									Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_LIKE) ,
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
				String noticeId = info.getSender().equals(UserManager.getInstance().getCurrentUserId()) ? "" : info.getSender();
				activity.showCommentInputLayout(position, 0, info.getId(), "", noticeId, "");
			}
		});

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
}
