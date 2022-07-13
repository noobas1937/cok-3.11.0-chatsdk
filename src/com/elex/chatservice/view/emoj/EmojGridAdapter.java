package com.elex.chatservice.view.emoj;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elex.chatservice.R;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.view.emoj.EmojIcon.Type;

public class EmojGridAdapter extends ArrayAdapter<EmojIcon>
{

	private EmojIcon.Type	emojIconType;

	public EmojGridAdapter(Context context, int textViewResourceId, List<EmojIcon> objects, EmojIcon.Type emojiconType)
	{
		super(context, textViewResourceId, objects);
		this.emojIconType = emojiconType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			if (emojIconType == EmojIcon.Type.BIG_EMOJ)
			{
				convertView = View.inflate(getContext(), R.layout.emoj_grid_big_item, null);
			}
			else
			{
				convertView = View.inflate(getContext(), R.layout.emoj_grid_item, null);
			}
		}

		EmojIcon emojicon = getItem(position);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_emoj);
		if (imageView != null)
		{
			if (EmojUtils.DELETE_KEY.equals(emojicon.getEmojiText()))
			{
				imageView.setImageResource(R.drawable.emoj_delete);
			}
			else
			{
				if (emojicon.getIcon() != 0)
				{
					imageView.setImageResource(emojicon.getIcon());
				}
				else
				{
					if(StringUtils.isNotEmpty(emojicon.getIconPath()))
						Glide.with(getContext()).load(emojicon.getIconPath()).placeholder(R.drawable.emoj_default).into(imageView);
					else if(StringUtils.isNotEmpty(emojicon.getIconCNDPath()))
						Glide.with(getContext()).load(emojicon.getIconCNDPath()).placeholder(R.drawable.emoj_default).into(imageView);
				}
			}
		}

		TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
		if (tv_name != null)
		{
			if (EmojUtils.DELETE_KEY.equals(emojicon.getEmojiText()))
			{
				tv_name.setText(LanguageManager.getLangByKey(LanguageKeys.DELETE));
			}
			else
			{
				if (StringUtils.isNotEmpty(emojicon.getName()))
					tv_name.setText(LanguageManager.getLangByKey(emojicon.getName()));
				else
					tv_name.setText("");
			}
		}

		return convertView;
	}

}
