package com.elex.chatservice.view.emoj;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.util.LogUtil;

public class EmojIcon
{

	private String				groupId;

	private String				id;

	private String				name;

	private int					icon;

	private int					dt	= 1;

	private transient int		bigIcon;

	private transient String	emojiText;

	private transient Type		type;

	private transient String	iconPath;

	private transient String	bigIconPath;

	private transient String	iconCNDPath;

	private transient String	bigCNDPath;

	public EmojIcon()
	{
	}

	public EmojIcon(int icon, String emojiText)
	{
		this.icon = icon;
		this.emojiText = emojiText;
		this.type = Type.NORMAL;
	}

	public EmojIcon(int icon, String emojiText, Type type)
	{
		this.icon = icon;
		this.emojiText = emojiText;
		this.type = type;
	}

	public int getDt()
	{
		return dt;
	}

	public void setDt(int dt)
	{
		this.dt = dt;
	}

	public EmojIcon(String groupId, String id, String name)
	{
		this.groupId = groupId;
		this.id = id;
		this.type = Type.BIG_EMOJ;
		setEmojFilePath();
	}

	public void setEmojFilePath()
	{
		this.iconPath = EmojSubscribeManager.getSubedEmojLocalPath(groupId, id, false);
		this.iconCNDPath = EmojSubscribeManager.getEmojCDNPath(groupId, id, false);
		this.bigIconPath = EmojSubscribeManager.getSubedEmojLocalPath(groupId, id, true);
		this.bigCNDPath = EmojSubscribeManager.getEmojCDNPath(groupId, id, true);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "iconPath", iconPath, "bigIconPath", bigIconPath);
	}

	@JSONField(serialize = false)
	public String getIconCNDPath()
	{
		return iconCNDPath;
	}

	@JSONField(serialize = false)
	public void setIconCNDPath(String iconCNDPath)
	{
		this.iconCNDPath = iconCNDPath;
	}

	@JSONField(serialize = false)
	public String getBigCNDPath()
	{
		return bigCNDPath;
	}

	@JSONField(serialize = false)
	public void setBigCNDPath(String bigCNDPath)
	{
		this.bigCNDPath = bigCNDPath;
	}

	@JSONField(serialize = false)
	public int getIcon()
	{
		return icon;
	}

	@JSONField(serialize = false)
	public void setIcon(int icon)
	{
		this.icon = icon;
	}

	@JSONField(serialize = false)
	public int getBigIcon()
	{
		return bigIcon;
	}

	@JSONField(serialize = false)
	public void setBigIcon(int dynamicIcon)
	{
		this.bigIcon = dynamicIcon;
	}

	@JSONField(serialize = false)
	public String getEmojiText()
	{
		return emojiText;
	}

	@JSONField(serialize = false)
	public void setEmojiText(String emojiText)
	{
		this.emojiText = emojiText;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@JSONField(serialize = false)
	public Type getType()
	{
		return type;
	}

	@JSONField(serialize = false)
	public void setType(Type type)
	{
		this.type = type;
	}

	@JSONField(serialize = false)
	public String getIconPath()
	{
		return iconPath;
	}

	@JSONField(serialize = false)
	public void setIconPath(String iconPath)
	{
		this.iconPath = iconPath;
	}

	@JSONField(serialize = false)
	public String getBigIconPath()
	{
		return bigIconPath;
	}

	@JSONField(serialize = false)
	public void setBigIconPath(String bigIconPath)
	{
		this.bigIconPath = bigIconPath;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
		setEmojFilePath();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@JSONField(serialize = false)
	public String getEmojId()
	{
		String nameStr = "";
		if(StringUtils.isNotEmpty(name))
			nameStr = name;
				
		return groupId + "|" + id + "|" + dt+ "|" + nameStr;
	}

	public static final String newEmojiText(int codePoint)
	{
		if (Character.charCount(codePoint) == 1)
		{
			return String.valueOf(codePoint);
		}
		else
		{
			return new String(Character.toChars(codePoint));
		}
	}

	public enum Type
	{
		/**
		 * normal icon, can be input one or more in edit view
		 */
		NORMAL,
		/**
		 * big icon, send out directly when your press it
		 */
		BIG_EMOJ
	}
}
