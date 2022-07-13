package com.elex.im.ui.model;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.util.AllianceTaskInfo;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.controller.JniController;

public class LatestChatInfo
{
	private String	name;
	private String	asn;
	private String	msg;
	private int		vip;
	private int		svip;
	private int		isVersionValid;
	private int		sequenceId;
	private int		createTime;
	private int		post;
	private int 	colorIndex;
	private String  attachment;
	private String  dialog;
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAsn()
	{
		return asn;
	}

	public void setAsn(String asn)
	{
		this.asn = asn;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public int getVip()
	{
		return vip;
	}

	public void setVip(int vip)
	{
		this.vip = vip;
	}

	public int getSvip()
	{
		return svip;
	}

	public void setSvip(int svip)
	{
		this.svip = svip;
	}

	public int getIsVersionValid()
	{
		return isVersionValid;
	}

	public void setIsVersionValid(int isVersionValid)
	{
		this.isVersionValid = isVersionValid;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime = createTime;
	}

	public int getSequenceId()
	{
		return sequenceId;
	}

	public void setSequenceId(int sequenceId)
	{
		this.sequenceId = sequenceId;
	}

	public int getPost()
	{
		return post;
	}

	public void setPost(int post)
	{
		this.post = post;
	}
	
	public int getColorIndex()
	{
		return colorIndex;
	}

	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}

	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment(String attachment)
	{
		this.attachment = attachment;
	}
	
	public String getDialog()
	{
		return dialog;
	}

	public void setDialog(String dialog)
	{
		this.dialog = dialog;
	}

	@JSONField(serialize = false)
	public void setMsgInfo(Msg msgItem)
	{
		this.name = msgItem.getName();
		this.asn = msgItem.getASN();
		this.msg = StringUtils.isNotEmpty(msgItem.translateMsg) ? msgItem.translateMsg : msgItem.msg;
		this.vip = msgItem.getVipLevel();
		this.svip = msgItem.getSVipLevel();
		this.isVersionValid = msgItem.isVersionInvalid() ? 0 : 1;
		this.sequenceId = msgItem.sequenceId;
		this.createTime = msgItem.createTime;
		this.post = msgItem.post;
		
		if (msgItem.isEquipMessage())
		{
			String msgStr = msgItem.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] equipInfo = msgStr.split("\\|");
				if (equipInfo.length == 2)
				{
					attachment = equipInfo[1];
					if (StringUtils.isNumeric(equipInfo[0]))
						colorIndex = Integer.parseInt(equipInfo[0]);
				}
			}
			this.dialog = LanguageKeys.TIP_EQUIP_SHARE;
		}
		else if (msgItem.isAllianceTaskMessage())
		{
			String msgStr = msgItem.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] taskInfo = msgStr.split("\\|");
				if (taskInfo.length >= 4)
				{
					AllianceTaskJson jsonObj = new AllianceTaskJson();
					jsonObj.setTaskName(taskInfo[2]);
					if (StringUtils.isNumeric(taskInfo[0]))
						colorIndex = Integer.parseInt(taskInfo[0]);
					String taskPlayerName = taskInfo[3];
					if (taskInfo.length > 4)
					{
						for (int i = 4; i < taskInfo.length; i++)
						{
							taskPlayerName += "|" + taskInfo[i];
						}
					}
					if (StringUtils.isNotEmpty(taskPlayerName))
					{
						try
						{
							List<AllianceTaskInfo> taskInfoArr = JSON.parseArray(taskPlayerName, AllianceTaskInfo.class);
							if (taskInfoArr != null && taskInfoArr.size() >= 1 && taskInfoArr.get(0) != null)
							{
								String publisher = taskInfoArr.get(0).getName();
								jsonObj.setPublisher(publisher);
								if (taskInfoArr.size() == 1 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1))
								{
									this.dialog = LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1;
									jsonObj.setMsgDialog(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1);
								}
								else if (taskInfoArr.size() == 2 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2))
								{
									this.dialog = LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2;
									jsonObj.setMsgDialog(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2);
									AllianceTaskInfo taskInfo2 = taskInfoArr.get(1);
									if (taskInfo2 != null)
									{
										jsonObj.setReceiver(taskInfo2.getName());
									}
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					
					try
					{
						attachment = JSON.toJSONString(jsonObj);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					
				}
			}

		}
		else if (msgItem.isAllianceTreasureMessage())
		{
			this.dialog = LanguageKeys.TIP_ALLIANCE_TREASURE_SHARE;
			attachment = msgItem.getAllianceTreasureInfo(1);
			String colorStr = msgItem.getAllianceTreasureInfo(0);
			if (StringUtils.isNotEmpty(colorStr) && StringUtils.isNumeric(colorStr))
				colorIndex = Integer.parseInt(colorStr);
		}
		else if(msgItem.isAllianceHelpMessage())
		{
			if(!msgItem.isVersionInvalid())
			{
				msg = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_HELP);
			}
		}
		else if(msgItem.isStealFailedMessage())
		{
			if(!msgItem.isVersionInvalid())
			{
				if(StringUtils.isNotEmpty(msgItem.attachmentId))
				{
					try
					{
						JSONObject extraJson = JSON.parseObject(msgItem.attachmentId);
						if (extraJson != null && extraJson.containsKey("dialog") && extraJson.containsKey("msgArr"))
						{
							String dialog = extraJson.getString("dialog");
							JSONArray dialogMsgArr = extraJson.getJSONArray("msgArr");
							if (dialogMsgArr != null && dialogMsgArr.size() == 2)
							{
								msg = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), dialogMsgArr.getString(1));
							}
						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if( msgItem.isAllianceSkillMessage())
		{
			if(StringUtils.isNotEmpty(msgItem.attachmentId))
			{
				try
				{
					AllianceSkillInfo skillInfo = JSON.parseObject(msgItem.attachmentId, AllianceSkillInfo.class);
					if (skillInfo != null)
					{
						String skillId = skillInfo.getSkillId();
						if (StringUtils.isNotEmpty(skillId))
						{
							String skillName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { skillId });
							String skillDes = JniController.getInstance().excuteJNIMethod("getPropById",
									new Object[] { skillId, "description" });
							String skillBase = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { skillId, "base" });
							String des = "";
							if (StringUtils.isEmpty(skillBase))
							{
								des = skillDes + skillBase;
							}
							else
							{
								String[] baseArr = skillBase.split("\\|");
								if (baseArr.length == 1)
								{
									des = LanguageManager.getLangByKey(skillDes, skillBase);
								}
								else if (baseArr.length == 2)
								{
									des = LanguageManager.getLangByKey(skillDes, baseArr[0], baseArr[1]);
								}
							}
							msg = LanguageManager.getLangByKey(skillInfo.getDialog(), skillName, des);
						}

					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(msgItem.isLotteryMessage())
		{
			if(!msgItem.isVersionInvalid())
			{
				msg = LanguageManager.getLangByKey(LanguageKeys.TIP_LUCK_WHEEL);
			}
		}
		else if(msgItem.isStealFailedMessage())
		{
			if(!msgItem.isVersionInvalid())
			{
				String msgStr = Msg.parseStealFailedMsg(msgItem);
				msg = StringUtils.isNotEmpty(msgStr) ? msgStr :LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);
			}
		}
		else if(msgItem.isAllianceOfficerMessage())
		{
			if(StringUtils.isNotEmpty(msgItem.attachmentId))
			{
				try
				{
					AllianceOfficerAttachment officer = JSON.parseObject(msgItem.attachmentId, AllianceOfficerAttachment.class);
					if (officer != null)
					{
						msg = LanguageManager.getLangByKey(officer.getDialog(), officer.getName(),
								LanguageManager.getLangByKey(officer.getOfficer()));
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (msgItem.isShotMessage())
		{
			String msgStr = Msg.parseShotMsg(msgItem);
			if(StringUtils.isNotEmpty(msgStr))
				msg = msgStr;
		}
		
		if (msgItem.isVersionInvalid())
		{
			msg = LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);
		}
	}
	
	public class AllianceTaskJson
	{
		private String taskName;
		private String publisher;
		private String receiver;
		private String msgDialog;
		public String getTaskName()
		{
			return taskName;
		}
		public void setTaskName(String taskName)
		{
			this.taskName = taskName;
		}
		public String getPublisher()
		{
			return publisher;
		}
		public void setPublisher(String publisher)
		{
			this.publisher = publisher;
		}
		public String getReceiver()
		{
			return receiver;
		}
		public void setReceiver(String receiver)
		{
			this.receiver = receiver;
		}
		public String getMsgDialog()
		{
			return msgDialog;
		}
		public void setMsgDialog(String msgDialog)
		{
			this.msgDialog = msgDialog;
		}
		
	}
}
