package com.elex.chatservice.view.allianceshare.adapter;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.allianceshare.AllianceShareListActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AllianceShareCommentAdapter extends BaseAdapter
{

	private AllianceShareListActivity	activity;
	private List<AllianceShareComment>	mCommentList;
	private LayoutInflater				inflater;
	private AllianceShareInfo			allianceShareInfo;
	private int 						mPosition;

	public AllianceShareCommentAdapter(AllianceShareListActivity context, AllianceShareInfo info,int position)
	{
		allianceShareInfo = info;
		activity = context;
		if(info!=null)
			mCommentList = allianceShareInfo.getComment();
		mPosition = position;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		if (mCommentList != null)
			return mCommentList.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (position < 0 || position > mCommentList.size())
			return null;
		return mCommentList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final AllianceShareComment comment = (AllianceShareComment)getItem(position);
		TextView comment_text = null;
		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.item_allianceshare_comment, null);
			comment_text = (TextView) convertView.findViewById(R.id.comment_text);
			convertView.setTag(comment_text);
			ScaleUtil.adjustTextSize(comment_text, ConfigManager.scaleRatio);
		}
		else
		{
			comment_text = (TextView) convertView.getTag();
		}
		
		if(comment == null)
			return convertView;
		
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
			comment_text.setText(span);
		}
		else
		{
			String text = comment.getName() + "：" + comment.getMsg();
			SpannableString span = new SpannableString(text);
			span.setSpan(new ForegroundColorSpan(0xffe1994b), 0, comment.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			comment_text.setText(span);
		}
		
		final View mParent = parent;

		comment_text.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (comment.isSelfAllianceShareComment())
				{
					activity.showAllianceShareCommentDeletePopupWindow(v, comment, allianceShareInfo.getSender());
				}
				else
				{
					String noticeUid = comment.getSender();
					if (!allianceShareInfo.isSelfAllianceShare() && StringUtils.isNotEmpty(allianceShareInfo.getSender()) && !allianceShareInfo.getSender().equals(noticeUid))
						noticeUid += ("," + allianceShareInfo.getSender());

					int commentLayoutHeight = mParent.getHeight();
					final int commentBottom = v.getBottom();
					int offsetY = commentLayoutHeight - commentBottom + ScaleUtil.dip2px(10);

					activity.showCommentInputLayout(mPosition, offsetY, comment.getFid(), comment.getSender(), noticeUid, comment.getName());
				}
			}
		});
		
		
		return convertView;
	}

}
