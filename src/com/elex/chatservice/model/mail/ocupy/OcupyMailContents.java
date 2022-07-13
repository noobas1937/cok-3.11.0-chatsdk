package com.elex.chatservice.model.mail.ocupy;

import java.util.List;

import com.elex.chatservice.model.mail.battle.DragonFightParams;
import com.elex.chatservice.model.mail.battle.NewGeneralInfo;

public class OcupyMailContents
{

	private int					pointId;
	private List<ArmysParams>	arms;
	private List<DragonFightParams>	defDrag;
	private UserInfoParams		user;
	private int					pointType;
	private boolean				isTreasureMap;
	private int					ckf;
	private int					serverType;
	private List<NewGeneralInfo>	battleGenerals;

	public List<NewGeneralInfo> getBattleGenerals()
	{
		return battleGenerals;
	}

	public void setBattleGenerals(List<NewGeneralInfo> battleGenerals)
	{
		this.battleGenerals = battleGenerals;
	}

	public int getPointId()
	{
		return pointId;
	}

	public void setPointId(int pointId)
	{
		this.pointId = pointId;
	}

	public List<ArmysParams> getArms()
	{
		return arms;
	}

	public void setArms(List<ArmysParams> arms)
	{
		this.arms = arms;
	}

	public UserInfoParams getUser()
	{
		return user;
	}

	public void setUser(UserInfoParams user)
	{
		this.user = user;
	}

	public int getPointType()
	{
		return pointType;
	}

	public void setPointType(int pointType)
	{
		this.pointType = pointType;
	}

	public boolean isTreasureMap()
	{
		return isTreasureMap;
	}

	public void setTreasureMap(boolean isTreasureMap)
	{
		this.isTreasureMap = isTreasureMap;
	}

	public int getCkf()
	{
		return ckf;
	}

	public void setCkf(int ckf)
	{
		this.ckf = ckf;
	}

	public int getServerType()
	{
		return serverType;
	}

	public void setServerType(int serverType)
	{
		this.serverType = serverType;
	}

	public List<DragonFightParams> getDefDrag() {
		return defDrag;
	}

	public void setDefDrag(List<DragonFightParams> defDrag) {
		this.defDrag = defDrag;
	}

}
