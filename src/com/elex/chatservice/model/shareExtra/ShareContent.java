package com.elex.chatservice.model.shareExtra;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareContent
{
	private boolean useDialog;
	private String text;
	private List<DialogExtra> dialogExtra;
	private int time;
	
	public int getTime()
	{
		return time;
	}
	public void setTime(int time)
	{
		this.time = time;
	}
	public boolean isUseDialog()
	{
		return useDialog;
	}
	public void setUseDialog(boolean useDialog)
	{
		this.useDialog = useDialog;
	}
	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	public List<DialogExtra> getDialogExtra()
	{
		return dialogExtra;
	}
	public void setDialogExtra(List<DialogExtra> dialogExtra)
	{
		this.dialogExtra = dialogExtra;
	}
	
	@JSONField(serialize = false)
	public void addDialogExtraInfo(int type,String info)
	{
		addDialogExtraInfo(type, info, true);
	}
	
	@JSONField(serialize = false)
	public void addDialogExtraInfo(int type,String info,boolean needCreateExtra)
	{
		if(type<0 || info == null || type == 1)
			return;
		if(needCreateExtra || dialogExtra == null)
			dialogExtra = new ArrayList<DialogExtra>();
		DialogExtra extra = new DialogExtra();
		extra.setType(type);
		if(type == 0)
			extra.setText(info);
		if(type == 2)
			extra.setDialog(info);
		dialogExtra.add(extra);
	}
}
