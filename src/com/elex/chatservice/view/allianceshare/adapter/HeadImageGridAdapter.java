package com.elex.chatservice.view.allianceshare.adapter;

import java.util.List;
import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HeadImageGridAdapter extends BaseAdapter
{

	private MyActionBarActivity	activity;
	private LayoutInflater		inflater;
	private List<String>		userIdList;

	public HeadImageGridAdapter(MyActionBarActivity activity, List<String> userIdList)
	{
		this.activity = activity;
		this.userIdList = userIdList;
		this.inflater = ((LayoutInflater) this.activity.getSystemService("layout_inflater"));
	}

	@Override
	public int getCount()
	{
		if (userIdList != null)
			return userIdList.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (userIdList != null && userIdList.size() > 0 && position >= 0 && position < userIdList.size())
			return userIdList.get(position);
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
			convertView = inflater.inflate(R.layout.item_head_grid, parent, false);
			adjustSize(convertView);
		}
		if(getItem(position)!=null)
		{
			String userId = getItem(position).toString();
			if(StringUtils.isNotEmpty(userId))
			{
				UserInfo user = UserManager.getInstance().getUser(userId);
				if(user!=null)
				{
					ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
					if (headImage != null)
					{
						ImageUtil.setHeadImage(activity, user.headPic, headImage, user);
					}
				}
			}
		}
		return convertView;
	}

	private void adjustSize(View convertView)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			int width = (int) (ScaleUtil.dip2px(activity, 60) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
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

}
