package com.elex.chatservice.model.mail.battle;

import java.util.List;

public class DragonFightParams {
	private int dragonId;
	private int kill;
	private int level;
	private int friendly;
	private int	decFriendly;
	private List<DragonReportParams> dragonReport;
	
	public List<DragonReportParams> getDragonReport()
	{
		return dragonReport;
	}
	public void setDragonReport(List<DragonReportParams> dragonReport)
	{
		this.dragonReport = dragonReport;
	}
	public int getDragonId() {
		return dragonId;
	}
	public void setDragonId(int dragonId) {
		this.dragonId = dragonId;
	}
	public int getKill() {
		return kill;
	}
	public void setKill(int kill) {
		this.kill = kill;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFriendly() {
		return friendly;
	}
	public void setFriendly(int friendly) {
		this.friendly = friendly;
	}
	public int getDecFriendly() {
		return decFriendly;
	}
	public void setDecFriendly(int decFriendly) {
		this.decFriendly = decFriendly;
	}
}
