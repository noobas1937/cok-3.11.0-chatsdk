package com.elex.chatservice.model;

import java.util.List;

public class InputDraft
{
	private String draft;
	private List<InputAtContent> inputAt;
	public String getDraft()
	{
		return draft;
	}
	public void setDraft(String draft)
	{
		this.draft = draft;
	}
	public List<InputAtContent> getInputAt()
	{
		return inputAt;
	}
	public void setInputAt(List<InputAtContent> inputAt)
	{
		this.inputAt = inputAt;
	}
	
}
