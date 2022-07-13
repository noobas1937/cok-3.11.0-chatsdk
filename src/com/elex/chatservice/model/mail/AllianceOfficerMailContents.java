package com.elex.chatservice.model.mail;

public class AllianceOfficerMailContents
{
	private int deal;
	private String operator;
	private String officer;
	private String dialogId;
	
	public String getDialogId()
	{
		return dialogId;
	}
	public void setDialogId(String dialogId)
	{
		this.dialogId = dialogId;
	}
	public int getDeal()
	{
		return deal;
	}
	public void setDeal(int deal)
	{
		this.deal = deal;
	}
	public String getOperator()
	{
		return operator;
	}
	public void setOperator(String operator)
	{
		this.operator = operator;
	}
	public String getOfficer()
	{
		return officer;
	}
	public void setOfficer(String officer)
	{
		this.officer = officer;
	}
	
	
}
