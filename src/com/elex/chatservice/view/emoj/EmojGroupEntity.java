package com.elex.chatservice.view.emoj;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.downzip.DownLoadEntity;
import com.elex.chatservice.util.downzip.DownLoadEntity.Status;
import com.elex.chatservice.view.emoj.EmojIcon.Type;

import android.util.Log;

public class EmojGroupEntity
{
	private List<EmojIcon>			details;
	private String					name;
	private String					groupId;
	private String					price;
	private String					description;
	private long					endTime;

	private transient DownLoadEntity downLoadEntity;
	private transient int			progress;
	private transient int			icon;
	private transient EmojIcon.Type	type;
	private transient String priceText = "";
	
	
	@JSONField(serialize = false)
	public DownLoadEntity getDownLoadEntity()
	{
		return downLoadEntity;
	}

	@JSONField(serialize = false)
	public void setDownLoadEntity(DownLoadEntity downLoadEntity)
	{
		this.downLoadEntity = downLoadEntity;
	}
	
	@JSONField(serialize = false)
	public void setDownLoadEntity(boolean downLoaded)
	{
		if(this.downLoadEntity == null )
			this.downLoadEntity = new DownLoadEntity(groupId, EmojSubscribeManager.getEmojZipCDNPath(groupId));
		boolean isExist = EmojSubscribeManager.isEmojGroupExist(groupId);
		downLoadEntity.setStatus(downLoaded && isExist ? Status.FINISHED : Status.WAITING);
		downLoadEntity.setDownloadPercent(downLoaded  && isExist ? 100 : 0);
	}

	@JSONField(serialize = false)
	public int getProgress()
	{
		return progress;
	}

	@JSONField(serialize = false)
	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	@JSONField(serialize = false)
	public void setGroupId()
	{
		this.type = Type.BIG_EMOJ;
		if (StringUtils.isEmpty(groupId) || details == null || details.size() <= 0)
			return;
		getPriceText();
		for (EmojIcon emoj : details)
		{
			if (emoj != null)
			{
				emoj.setGroupId(groupId);
				emoj.setType(Type.BIG_EMOJ);
			}
		}
	}

	public List<EmojIcon> getDetails()
	{
		return details;
	}

	public void setDetails(List<EmojIcon> details)
	{
		this.details = details;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public String getPrice()
	{
		return price;
	}

	public void setPrice(String price)
	{
		this.price = price;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public String getTabIconLocalPath()
	{
		return EmojSubscribeManager.getEmojTabLocalPath(groupId);
	}

	public String getTabIconCDNPath()
	{
		return EmojSubscribeManager.getEmojTabCDNPath(groupId);
	}

	public EmojGroupEntity()
	{
	}

	public EmojGroupEntity(int icon, List<EmojIcon> emojiconList)
	{
		this.icon = icon;
		this.details = emojiconList;
		type = EmojIcon.Type.NORMAL;
	}

	public EmojGroupEntity(int icon, List<EmojIcon> emojiconList, EmojIcon.Type type)
	{
		this.icon = icon;
		this.details = emojiconList;
		this.type = type;
	}

	public EmojGroupEntity(String groupId, String name, String price, String decription, List<EmojIcon> emojiconList)
	{
		this.groupId = groupId;
		this.details = emojiconList;
		this.name = name;
		this.price = price;
		this.description = decription;
		this.type = EmojIcon.Type.BIG_EMOJ;
	}
	
	@JSONField(serialize = false)
	public String getPriceText()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "price",price,"priceText",priceText);
		if(StringUtils.isEmpty(priceText))
		{
			if(StringUtils.isNotEmpty(price))
				priceText = EmojSubscribeManager.getInstance().getExpressionPrice(price);
		}
		return priceText;
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@JSONField(serialize = false)
	public EmojIcon.Type getType()
	{
		return type;
	}

	@JSONField(serialize = false)
	public void setType(EmojIcon.Type type)
	{
		this.type = type;
	}

}
