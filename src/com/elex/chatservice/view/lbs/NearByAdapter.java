package com.elex.chatservice.view.lbs;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ColorFragment;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.NearByUserInfo;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.shareExtra.DialogExtra;
import com.elex.chatservice.model.shareExtra.ShareContent;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.FixedAspectRatioFrameLayout;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.lbs.NearByActivity;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NearByAdapter extends BaseAdapter
{

	private NearByActivity			activity;
	private List<NearByUserInfo>	nearByUserList;
	private LayoutInflater			inflater;

	public NearByAdapter(NearByActivity context)
	{
		nearByUserList = NearByManager.getInstance().getNearByUserArray();
		activity = context;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void refreshNearByData()
	{
		nearByUserList = NearByManager.getInstance().getNearByUserArray();
		if (activity != null)
		{
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
	}

	@Override
	public int getCount()
	{
		if (nearByUserList != null)
			return nearByUserList.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (position < 0 || position > nearByUserList.size())
			return null;
		return nearByUserList.get(position);
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
			convertView = inflater.inflate(R.layout.cs__nearby_item, null);
			adjustSize(convertView);
		}
		
		final NearByUserInfo userInfo = (NearByUserInfo) getItem(position);
		if (userInfo == null)
			return convertView;
		
		RoundImageView user_icon = ViewHolderHelper.get(convertView, R.id.user_icon);

		if (StringUtils.isNotEmpty(userInfo.getUid()))
		{
			UserManager.checkUser(userInfo.getUid(), "", 0);
			UserInfo user = UserManager.getInstance().getUser(userInfo.getUid());

			if (user != null)
			{
				TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
				if (allianceLabel != null)
				{
					if (StringUtils.isNotEmpty(user.asn))
						allianceLabel.setText("(" + user.asn + ")");
					else
						allianceLabel.setText("");
				}

				TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
				if (nameLabel != null)
				{
					String name = "";
					if (StringUtils.isNotEmpty(user.userName))
						name = user.userName;
					else
						name = userInfo.getUid();
					String serverText = user.serverId > 0 ? "#" + user.serverId : "";
					nameLabel.setText(name+serverText);
				}
				
				if(user_icon!=null)
					ImageUtil.setHeadImage(activity, "", user_icon, user);
			}
		}
		
		if(user_icon!=null)
		{
			GradientDrawable bgShape = (GradientDrawable) user_icon.getBackground();
			if (bgShape != null)
				bgShape.setColor(0xFF2E3D59);
		}
		

		TextView distanceText = ViewHolderHelper.get(convertView, R.id.distanceText);
		if (distanceText != null)
		{
			double distance = userInfo.getDistance();
			if(distance<0)
				distance = 0;
			if(distance>1)
			{
				String dis = String.format("%.2f", distance);
				distanceText.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY_DISTANCE, dis));
			}
			else
			{
				String dis = String.format("%.2f", distance*1000);
				distanceText.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY_DISTANCE_M, dis));
			}
		}

		TextView lastLoginTimeText = ViewHolderHelper.get(convertView, R.id.lastLoginTimeText);
		if (lastLoginTimeText != null)
		{
			String freshNewsText = userInfo.getFreshNewsText();
			
			if(StringUtils.isNotEmpty(freshNewsText))
			{
				SpannableStringBuilder style = new SpannableStringBuilder(freshNewsText);
				style.clearSpans();
				List<ColorFragment> colorFragmentList = userInfo.getFreshNewsColorFragmentList();
				if(colorFragmentList!=null && colorFragmentList.size()>0)
				{
					String txt = style.toString();
					for(int i=0;i<colorFragmentList.size();i++)
					{
						ColorFragment colorFragment = colorFragmentList.get(i);
						if(colorFragment!=null && colorFragment.getColor()!=0 && StringUtils.isNotEmpty(colorFragment.getDialogExtra()))
						{
							try
							{
								String dialogExtra = colorFragment.getDialogExtra();
								int start = txt.indexOf(colorFragment.getDialogExtra());
								style.setSpan(new ForegroundColorSpan(colorFragment.getColor()), start, start
										+ colorFragment.getDialogExtra().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				lastLoginTimeText.setText(style);
			}
			else
			{
				long time = userInfo.getLastLoginTime();
				if (time < 0)
					time = 0;
				lastLoginTimeText.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY_LAST_LOGIN_TIME,
						TimeManager.getNearByReadableTime(time)));
			}
		}

		RelativeLayout nearby_item_layout = ViewHolderHelper.get(convertView, R.id.nearby_item_layout);
		if (nearby_item_layout != null)
		{
			nearby_item_layout.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					
					String channelId = userInfo.getUid();
					if(!channelId.equals(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
						channelId += DBDefinition.CHANNEL_ID_POSTFIX_NEARBY;
					ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER, channelId);
					if (channel != null)
					{
						if (!channel.hasInitLoaded())
							channel.loadMoreMsg();
						ServiceInterface.setMailInfo(3,channel.channelID, "");
						ServiceInterface.showChatActivity(ChatServiceController.hostActivity, channel.channelType, false);
					}
				}
			});
		}
		
		TextView nearby_like_num = ViewHolderHelper.get(convertView, R.id.nearby_like_num);
		if (nearby_like_num != null)
		{
			int likeNum = userInfo.getLikeNum();
			if(likeNum<0)
				likeNum = 0;
			nearby_like_num.setText(""+likeNum);
		}
		
		ImageView nearby_like_btn = ViewHolderHelper.get(convertView, R.id.nearby_like_btn);
		LinearLayout right_layout = ViewHolderHelper.get(convertView, R.id.right_layout);
		
		
		if(right_layout!=null)
		{
			if(MailManager.nearbyLikeEnable)
				right_layout.setVisibility(View.VISIBLE);
			else
				right_layout.setVisibility(View.GONE);
			
			if (nearby_like_btn != null)
			{
				String uid = userInfo.getUid();
				right_layout.setTag(uid);
				List<String> uidList = NearByManager.getInstance().getTodayLikeUidList();
				if(StringUtils.isNotEmpty(uid) && uidList!=null)
				{
					if(uidList.contains(uid))
					{
						right_layout.setEnabled(true);
						nearby_like_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.nearby_like_cancel_btn));
					}
					else if(uidList.size()<NearByManager.todayLikeNumLimit)
					{
						right_layout.setEnabled(true);
						nearby_like_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.nearby_like_btn));
					}
					else
					{
						right_layout.setEnabled(false);
						nearby_like_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.nearby_like_disable));
					}
				}
				else
				{
					right_layout.setEnabled(false);
					nearby_like_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.nearby_like_disable));
				}
				
				right_layout.setOnClickListener(new OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						if(v!=null && v.getTag()!=null)
						{
							String uid = v.getTag().toString();
							List<String> uidList = NearByManager.getInstance().getTodayLikeUidList();
							
							if(StringUtils.isNotEmpty(uid) && uidList!=null)
							{
								if(uidList.contains(uid))
									NearByManager.getInstance().cancelLikeNearbyUser(uid);
								else if(uidList.size()<NearByManager.todayLikeNumLimit)
									NearByManager.getInstance().likeNearbyUser(uid);
							}
						}
					}
				});
			}
		}

		return convertView;
	}

	private void adjustTextSize(View convertView)
	{
		TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
		if (allianceLabel != null)
			ScaleUtil.adjustTextSize(allianceLabel, ConfigManager.scaleRatio);

		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
			ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);

		TextView distanceText = ViewHolderHelper.get(convertView, R.id.distanceText);
		if (distanceText != null)
			ScaleUtil.adjustTextSize(distanceText, ConfigManager.scaleRatio);

		TextView lastLoginTimeText = ViewHolderHelper.get(convertView, R.id.lastLoginTimeText);
		if (lastLoginTimeText != null)
			ScaleUtil.adjustTextSize(lastLoginTimeText, ConfigManager.scaleRatio);
		
		TextView nearby_like_num = ViewHolderHelper.get(convertView, R.id.nearby_like_num);
		if (nearby_like_num != null)
			ScaleUtil.adjustTextSize(nearby_like_num, ConfigManager.scaleRatio);
	}

	private void adjustSize(View convertView)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);

			int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			FixedAspectRatioFrameLayout user_pic_layout = ViewHolderHelper.get(convertView, R.id.user_pic_layout);
			if (user_pic_layout != null)
			{
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) user_pic_layout.getLayoutParams();
				layoutParams.width = length;
				layoutParams.height = length;
				user_pic_layout.setLayoutParams(layoutParams);
			}
			
			int length2 = (int) (ScaleUtil.dip2px(activity, 20) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			ImageView nearby_like_btn = ViewHolderHelper.get(convertView, R.id.nearby_like_btn);
			if (nearby_like_btn != null)
			{
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nearby_like_btn.getLayoutParams();
				layoutParams.width = length2;
				layoutParams.height = length2;
				nearby_like_btn.setLayoutParams(layoutParams);
			}
		}
	}

}
