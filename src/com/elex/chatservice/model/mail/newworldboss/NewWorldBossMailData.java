package com.elex.chatservice.model.mail.newworldboss;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.mail.monster.DefParams;
import com.elex.chatservice.util.LogUtil;

public class NewWorldBossMailData extends MailData
{
	private int							unread;
	private int							totalNum;
	private List<NewWorldBossMailContents>	worldbosslist;

	public int getUnread()
	{
		return unread;
	}

	public void setUnread(int unread)
	{
		this.unread = unread;
	}

	public int getTotalNum()
	{
		return totalNum;
	}

	public void setTotalNum(int totalNum)
	{
		this.totalNum = totalNum;
	}

	public List<NewWorldBossMailContents> getWorldbosslist()
	{
		return worldbosslist;
	}

	public void setWorldbosslist(List<NewWorldBossMailContents> worldbosslist)
	{
		this.worldbosslist = worldbosslist;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			try
			{
				if (getStatus() == 0)
					setUnread(1);
				else
					setUnread(0);
				setTotalNum(1);
				worldbosslist = new ArrayList<NewWorldBossMailContents>();
				NewWorldBossMailContents detail = JSON.parseObject(getContents(), NewWorldBossMailContents.class);
				if (detail == null)
					return;
				detail.setUid(getUid());
				detail.setCreateTime(String.valueOf(getCreateTime()));
				detail.setType(getType());
				worldbosslist.add(detail);
				hasMailOpend = true;
				if (detail == null || needParseByForce)
					return;

				DefParams def = detail.getDef();
				if (def == null)
					return;
				String name = "";
				String level = "";
				if (StringUtils.isNotEmpty(def.getId()))
				{
					name = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { def.getId() });
					level = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { def.getId(), "level" });
				}
				name += " Lv.";
				name += level;
				contentText = name;
				contentText += "  ";

				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[MonsterMailData parseContents error]: contents:" + getContents());
			}
		}
	}
}
