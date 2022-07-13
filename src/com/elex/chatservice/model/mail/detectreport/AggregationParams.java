package com.elex.chatservice.model.mail.detectreport;

import java.util.List;

public class AggregationParams
{
	private int total;
	private AggregationUserParams originator;
	private List<AggregationUserParams> members;
	private int bLevel;
	
	public int getbLevel() {
		return bLevel;
	}
	
	public void setbLevel(int bLevel) {
		this.bLevel = bLevel;
	}
	
	public int getTotal()
	{
		return total;
	}
	public void setTotal(int total)
	{
		this.total = total;
	}
	public AggregationUserParams getOriginator()
	{
		return originator;
	}
	public void setOriginator(AggregationUserParams originator)
	{
		this.originator = originator;
	}
	public List<AggregationUserParams> getMembers()
	{
		return members;
	}
	public void setMembers(List<AggregationUserParams> members)
	{
		this.members = members;
	}
	
	
}
