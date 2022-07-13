package com.elex.chatservice.view.emoj;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bumptech.glide.Glide;
import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.SubedEmojEntity;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.CompatibleApiUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.util.downzip.DownLoadEntity;
import com.elex.chatservice.util.downzip.DownLoadEntity.Status;
import com.elex.chatservice.util.downzip.Downloador;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EmojSubscribAdapter extends BaseAdapter
{

	private MyActionBarActivity				activity;
	private List<EmojGroupEntity>			subableEmojList;
	private Map<String, SubedEmojEntity>	subedEmojMap;
	private LayoutInflater					inflater;

	public EmojSubscribAdapter(MyActionBarActivity context)
	{
		subableEmojList = EmojSubscribeManager.getInstance().getSubableEmojList();
		subedEmojMap = EmojSubscribeManager.getInstance().getSubedEmojMap();
		activity = context;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void refreshEmojListData()
	{
		subableEmojList = EmojSubscribeManager.getInstance().getSubableEmojList();
		subedEmojMap = EmojSubscribeManager.getInstance().getSubedEmojMap();
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


	private void downEmojGroupResource(DownLoadEntity downLoadEntity)
	{
		Downloador downloador = new Downloador(activity, downLoadEntity);
        downloador.download();
	}

	@Override
	public int getCount()
	{
		if (subableEmojList != null)
			return subableEmojList.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (position < 0 || position > subableEmojList.size())
			return null;
		return subableEmojList.get(position);
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
			convertView = inflater.inflate(R.layout.emoj_subscrib_item, null);
			adjustSize(convertView);
		}
		
		final EmojGroupEntity emojGroupEntity = (EmojGroupEntity) getItem(position);
		if (emojGroupEntity == null)
			return convertView;

		ImageView emoj_group_icon = ViewHolderHelper.get(convertView, R.id.emoj_group_icon);
		if (emoj_group_icon != null)
		{
			String url = EmojSubscribeManager.getEmojTabCDNPath(emojGroupEntity.getGroupId());
			Glide.with(activity).load(url).placeholder(R.drawable.emoj_default).into(emoj_group_icon);
		}

		TextView name = ViewHolderHelper.get(convertView, R.id.name);
		if (name != null)
		{
			if (StringUtils.isNotEmpty(emojGroupEntity.getName()))
				name.setText(LanguageManager.getLangByKey(emojGroupEntity.getName()));
			else
				name.setText("");
		}

		TextView description = ViewHolderHelper.get(convertView, R.id.description);
		if (description != null)
		{
			if (StringUtils.isNotEmpty(emojGroupEntity.getDescription()))
				description.setText(LanguageManager.getLangByKey(emojGroupEntity.getDescription()));
			else
				description.setText("");
		}

		LinearLayout right_layout = ViewHolderHelper.get(convertView, R.id.right_layout);
		TextView buy_btn = ViewHolderHelper.get(convertView, R.id.buy_btn);
		ProgressBar down_progress = ViewHolderHelper.get(convertView, R.id.down_progress);

		if (right_layout != null)
		{
			right_layout.setTag(emojGroupEntity.getGroupId());
			DownLoadEntity downLoadEntity = emojGroupEntity.getDownLoadEntity();
			
			if (buy_btn != null)
			{
				buy_btn.setEnabled(true);
				CompatibleApiUtil.getInstance().setButtonAlpha(buy_btn, true);
			}
			
			
			if(downLoadEntity!=null && downLoadEntity.getStatus() == Status.DOWNLOADING)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "getDownloadPercent",downLoadEntity.getDownloadPercent());
				if (buy_btn != null)
					buy_btn.setVisibility(View.GONE);
				if (down_progress != null)
				{
					down_progress.setVisibility(View.VISIBLE);
					down_progress.setProgress(downLoadEntity.getDownloadPercent());
				}
			}
			else
			{
				if (down_progress != null)
					down_progress.setVisibility(View.GONE);
				if (buy_btn != null)
				{
					buy_btn.setVisibility(View.VISIBLE);
					if (StringUtils.isEmpty(emojGroupEntity.getPrice()))
					{
						if(downLoadEntity!=null && downLoadEntity.getStatus() == Status.FINISHED)
						{
							buy_btn.setVisibility(View.GONE);
						}
						else
						{
							buy_btn.setVisibility(View.VISIBLE);
							buy_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_DOWNLOAD));
						}
					}
					else
					{
						if (subedEmojMap.containsKey(emojGroupEntity.getGroupId()))
						{
							SubedEmojEntity subedEmoj = subedEmojMap.get(emojGroupEntity.getGroupId());
							long endTime = subedEmoj.getEndTime();
							if (endTime > System.currentTimeMillis())
							{
								if(downLoadEntity!=null && downLoadEntity.getStatus() == Status.FINISHED)
								{
									buy_btn.setEnabled(false);
									CompatibleApiUtil.getInstance().setButtonAlpha(buy_btn, false);
									buy_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_BUYED));
								}
								else
									buy_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_DOWNLOAD));
							}
							else
							{
//								buy_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SUBSCRIBE));
								buy_btn.setText(emojGroupEntity.getPriceText());
							}
						}
						else
						{
//							buy_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SUBSCRIBE));
							buy_btn.setText(emojGroupEntity.getPriceText());
						}
					}
				}
			}

			right_layout.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if (v != null && v.getTag() != null)
					{
						String groupId = v.getTag().toString();
						Map<String, EmojGroupEntity> avaliableEmojMap = EmojSubscribeManager.getInstance().getSubableEmojMap();
						if (avaliableEmojMap != null)
						{
							EmojGroupEntity emojGroupEntity = avaliableEmojMap.get(groupId);
							DownLoadEntity downLoadEntity = emojGroupEntity.getDownLoadEntity();
							if (emojGroupEntity != null && !(downLoadEntity!=null && downLoadEntity.getStatus() == Status.DOWNLOADING))
							{
								if (StringUtils.isEmpty(emojGroupEntity.getPrice()))
								{
									if(downLoadEntity!=null && downLoadEntity.getStatus() != Status.FINISHED)
									{
										downEmojGroupResource(emojGroupEntity.getDownLoadEntity());
					                    downLoadEntity.setStatus(Status.DOWNLOADING);
					                    notifyDataSetChanged();
									}
								}
								else
								{
									if (subedEmojMap.containsKey(emojGroupEntity.getGroupId()))
									{
										SubedEmojEntity subedEmoj = subedEmojMap.get(emojGroupEntity.getGroupId());
										long endTime = subedEmoj.getEndTime();
										if (endTime > System.currentTimeMillis())
										{
											if(downLoadEntity!=null && downLoadEntity.getStatus() == Status.FINISHED)
											{
//												EmojSubscribeManager.getInstance().unsubscribeGroupEmoj(groupId);
											}
											else
											{
												downEmojGroupResource(emojGroupEntity.getDownLoadEntity());
							                    downLoadEntity.setStatus(Status.DOWNLOADING);
							                    notifyDataSetChanged();
											}
										}
										else
										{
											EmojSubscribeManager.getInstance().subscribeGroupEmoj(emojGroupEntity.getPrice());
										}
									}
									else
									{
										EmojSubscribeManager.getInstance().subscribeGroupEmoj(emojGroupEntity.getPrice());
									}
								}
							}
						}
					}
				}
			});
		}

		return convertView;
	}

	private void adjustTextSize(View convertView)
	{
		TextView name = ViewHolderHelper.get(convertView, R.id.name);
		if (name != null)
			ScaleUtil.adjustTextSize(name, ConfigManager.scaleRatio);

		TextView description = ViewHolderHelper.get(convertView, R.id.description);
		if (description != null)
			ScaleUtil.adjustTextSize(description, ConfigManager.scaleRatio);

		TextView buy_btn = ViewHolderHelper.get(convertView, R.id.buy_btn);
		if (buy_btn != null)
			ScaleUtil.adjustTextSize(buy_btn, ConfigManager.scaleRatio);
	}

	private void adjustSize(View convertView)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);

			int length = (int) (ScaleUtil.dip2px(activity, 45) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			ImageView emoj_group_icon = ViewHolderHelper.get(convertView, R.id.emoj_group_icon);
			if (emoj_group_icon != null)
			{
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) emoj_group_icon.getLayoutParams();
				layoutParams.width = length;
				layoutParams.height = length;
				emoj_group_icon.setLayoutParams(layoutParams);
			}

			TextView buy_btn = ViewHolderHelper.get(convertView, R.id.buy_btn);
			if (buy_btn != null)
			{
				int height = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buy_btn.getLayoutParams();
				layoutParams.height = height;
				buy_btn.setLayoutParams(layoutParams);
			}
			
			ProgressBar down_progress = ViewHolderHelper.get(convertView, R.id.down_progress);
			if (down_progress != null)
			{
				int height = (int) (ScaleUtil.dip2px(activity, 150) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) down_progress.getLayoutParams();
				layoutParams.width = height;
				down_progress.setLayoutParams(layoutParams);
			}
		}
	}

}
