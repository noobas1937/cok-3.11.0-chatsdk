package com.elex.chatservice.model.mail.detectreport;

import java.util.List;

import com.elex.chatservice.model.mail.battle.DragonFightParams;
import com.elex.chatservice.model.mail.battle.NewGeneralInfo;

public class DefenceParams
{
	private int					total;
	private boolean				about;
	private List<ArmsParams>	arms;
	private List<DragonFightParams>	defDrag;
	private List<NewGeneralInfo>				generalInfo;

	public List<NewGeneralInfo> getGeneralInfo()
	{
		return generalInfo;
	}

	public void setGeneralInfo(List<NewGeneralInfo> generalInfo)
	{
		this.generalInfo = generalInfo;
	}

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public boolean isAbout()
	{
		return about;
	}

	public void setAbout(boolean about)
	{
		this.about = about;
	}

	public List<ArmsParams> getArms()
	{
		return arms;
	}

	public void setArms(List<ArmsParams> arms)
	{
		this.arms = arms;
	}

	public List<DragonFightParams> getDefDrag() {
		return defDrag;
	}

	public void setDefDrag(List<DragonFightParams> defDrag) {
		this.defDrag = defDrag;
	}

}
