package com.elex.chatservice.model.mail.battle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailIconName;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.shareExtra.DialogExtra;
import com.elex.chatservice.model.shareExtra.ShareContent;
import com.elex.chatservice.model.shareExtra.ShareMsgExtra;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.MathUtil;

public class BattleMailData extends MailData
{
	private BattleMailContents			detail;
	private Content						attualContent;
	private List<BattleMailContents>	knight;
	private int							unread;
	private int							totalNum;
	private String						contentsJson;
	private ShareMsgExtra 				shareExtra;

	private static final int			WIN		= 0;
	private static final int			DRAW	= 1;
	private static final int			LOOSE	= 2;

	public BattleMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(BattleMailContents detail)
	{
		this.detail = detail;
	}

	public Content getAttualContent()
	{
		return attualContent;
	}

	public void setAttualContent(Content attualContent)
	{
		this.attualContent = attualContent;
	}

	public List<BattleMailContents> getKnight()
	{
		return knight;
	}

	public void setKnight(List<BattleMailContents> knight)
	{
		this.knight = knight;
	}

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
	
	public String getContentsJson()
	{
		return contentsJson;
	}

	public void setContentsJson(String contentsJson)
	{
		this.contentsJson = contentsJson;
	}

	public void parseContents()
	{
		super.parseContents();
		isKnightMail = false;
		isDragonTowerMail = false;
		isKnightActivityFinishMail = false;
		contentsJson = getContents();
		if (!getContents().equals(""))
		{
			try
			{
				detail = JSON.parseObject(getContents(), BattleMailContents.class);

				if (detail == null)
					return;

				 
				attualContent = new Content();
				if (detail.getWarPoint() != null)
					attualContent.setWarPoint(detail.getWarPoint());
				else
					attualContent.setWarPoint("");
				if (detail.getDefUser() != null && detail.getDefUser().getNpcId() != null && !detail.getDefUser().getNpcId().equals(""))
				{
					attualContent.setNpcId(detail.getDefUser().getNpcId());
					attualContent.setDefName("");
				}
				else
				{
					if (detail.getDefUser() != null && detail.getDefUser().getName() != null)
						attualContent.setDefName(detail.getDefUser().getName());
					else
						attualContent.setDefName("");
					attualContent.setNpcId("");
				}
				if (detail.getAtkUser() != null && detail.getAtkUser().getName() != null)
					attualContent.setAtkName(detail.getAtkUser().getName());
				else
					attualContent.setAtkName("");

				if (detail.getWinner() == null || detail.getWinner().equals(""))
				{
					attualContent.setWin(2);
				}
				else if (detail.getWinner().equals(UserManager.getInstance().getCurrentUserId()))
				{
					attualContent.setWin(0);
				}
				else
				{
					attualContent.setWin(1);
				}

				int battleType = detail.getBattleType();
				hasMailOpend = true;
				hasParseForKnight = true;

				if (detail.getMsReport() == 1)
					isKnightActivityFinishMail = true;
				if (battleType == 6)
					isKnightMail = true;
				else if (battleType == 10 || battleType == 11)
					isDragonTowerMail = true;

				if (battleType != 6 && needParseByForce)
					return;
				
				shareExtra = new ShareMsgExtra();
				ShareContent shareTitle = new ShareContent(); 
				ShareContent shareContent = new ShareContent();
				shareTitle.setUseDialog(true);
				shareContent.setUseDialog(true);

				String defUserName = "";
				if (detail.getAtkUser() != null && detail.getAtkUser().getNpcId() != null && !detail.getAtkUser().getNpcId().equals(""))
				{
					defUserName = detail.getAtkUser().getNpcId();
				}
				else if (detail.getDefUser() != null)
				{
					if (detail.getDefUser().getName() != null)
						defUserName = detail.getDefUser().getName();
				}

				if (detail.getAtkUser() != null && detail.getAtkUser().getName() != null
						&& UserManager.getInstance().getCurrentUser() != null)
				{
					shareContent.setText(LanguageKeys.MAIL_TITLE_ATTACK);
					
					if (UserManager.getInstance().getCurrentUser().userName.equals(detail.getAtkUser().getName()))
					{
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK,
								LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY), defUserName);
						
						shareContent.addDialogExtraInfo(2, LanguageKeys.MAIL_TITLE_MYARMY);
						shareContent.addDialogExtraInfo(0, defUserName,false);
					}
					else
					{
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, detail.getAtkUser().getName(),
								LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYCASTLE));
						
						shareContent.addDialogExtraInfo(0, detail.getAtkUser().getName());
						shareContent.addDialogExtraInfo(2, LanguageKeys.MAIL_TITLE_MYCASTLE,false);
					}
				}

				String kill = "";
				String loss = "";

				boolean isbigLoss = false;
				if (detail.getAtkGen() == null || detail.getAtkGen().size() <= 0)
				{
					isbigLoss = true;
				}
				else
				{
					String killAndLoss = calculateKillandLoss(detail);
					if (StringUtils.isNotEmpty(killAndLoss))
					{
						String[] strArr = killAndLoss.split("_");
						if (strArr.length == 2)
						{
							kill = strArr[0];
							loss = strArr[1];
							if (StringUtils.isNumeric(kill))
								kill = MathUtil.getFormatNumber(Integer.parseInt(kill));
							if (StringUtils.isNumeric(loss))
								loss = MathUtil.getFormatNumber(Integer.parseInt(loss));
						}
					}
				}

				String playerUid = UserManager.getInstance().getCurrentUserId();

				boolean isAtt = false;
				String attUid = "";
				if (detail.getAtkUser() != null && StringUtils.isNotEmpty(detail.getAtkUser().getUid()))
					attUid = detail.getAtkUser().getUid();
				String attName = "";
				String defDialog = "";
				if (detail.getAtkUser() != null && StringUtils.isNotEmpty(detail.getAtkUser().getName()))
					attName = detail.getAtkUser().getName();
				String defName = "";
				if (detail.getDefUser() != null && StringUtils.isNotEmpty(detail.getDefUser().getName()))
					defName = detail.getDefUser().getName();
				else if(StringUtils.isNotEmpty(detail.getBarbarionConfigId()))
				{
					defDialog = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { detail.getBarbarionConfigId(),"name"});
					defName =LanguageManager.getLangByKey(defDialog);
				}

				if (detail.getAtkHelper() != null)
				{
					for (int i = 0; i < detail.getAtkHelper().size(); i++)
					{
						String helpUid = detail.getAtkHelper().get(i);
						if (StringUtils.isNotEmpty(helpUid) && helpUid.equals(playerUid))
						{
							isAtt = true;
							break;
						}
					}
				}

				if (StringUtils.isNotEmpty(attUid) && playerUid.equals(attUid))
				{
					isAtt = true;
				}

				
				
				int reportState;
				if (StringUtils.isEmpty(detail.getWinner()))
				{
					reportState = DRAW;
					if (isAtt)
					{
						shareTitle.setText(LanguageKeys.TIP_ATT_DRAW);
						shareTitle.addDialogExtraInfo(0, defName);
					}
					else
					{
						shareTitle.setText(LanguageKeys.TIP_ATTED_DRAW);
						shareTitle.addDialogExtraInfo(0, attName);
					}
				}
				else if (detail.getWinner().equals(attUid))
				{
					if (isAtt)
					{
						reportState = WIN;
						if(battleType == 10 || battleType == 11)
						{
							shareTitle.setText(LanguageKeys.TITLE_CHALLENGE_SUCESS);
						}
						else
						{
							shareTitle.setText(LanguageKeys.TIP_ATT_SUCSESS);
							shareTitle.addDialogExtraInfo(0, defName);
						}
					}
					else
					{
						reportState = LOOSE;
						if(battleType == 10 || battleType == 11)
						{
							shareTitle.setText(LanguageKeys.TITLE_CHALLENGE_FAILED);
						}
						else
						{
							shareTitle.setText(LanguageKeys.TIP_ATTED_FAILED);
							shareTitle.addDialogExtraInfo(0, attName);
						}
						
					}
				}
				else
				{
					if (isAtt)
					{
						reportState = LOOSE;
						if(battleType == 10 || battleType == 11)
						{
							shareTitle.setText(LanguageKeys.TITLE_CHALLENGE_FAILED);
						}
						else
						{
							shareTitle.setText(LanguageKeys.TIP_ATT_FAILED);
							shareTitle.addDialogExtraInfo(0, defName);
						}
					}
					else
					{
						reportState = WIN;
						if(battleType == 10 || battleType == 11)
						{
							shareTitle.setText(LanguageKeys.TITLE_CHALLENGE_SUCESS);
						}
						else
						{
							shareTitle.setText(LanguageKeys.TIP_ATTED_SUCCESS);
							shareTitle.addDialogExtraInfo(0, attName);
						}
					}
				}
				
				shareExtra.setTitle(shareTitle);
				
				if(reportState == WIN || reportState == DRAW)
				{
					shareExtra.setIcon("sucess");
				}
				else if(reportState == LOOSE)
				{
					if(isbigLoss)
						shareExtra.setIcon("large_fail");
					else
						shareExtra.setIcon("fail");
				}

				
				shareContent.setText(LanguageKeys.MAIL_TITLE_ATTACK);
				if (isAtt)
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK,
							LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY), defName);
					
					shareContent.addDialogExtraInfo(2, LanguageKeys.MAIL_TITLE_MYARMY);
					if(StringUtils.isNotEmpty(defDialog))
						shareContent.addDialogExtraInfo(2, defDialog,false);
					else
						shareContent.addDialogExtraInfo(0, defName,false);
				}
				else
				{
					if (detail.getBattleType() != 3)
					{
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY));
						
						shareContent.addDialogExtraInfo(0, attName);
						shareContent.addDialogExtraInfo(2, LanguageKeys.MAIL_TITLE_MYARMY,false);
					}
					else
					{
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ATTACK, attName,
								LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYCASTLE));
						shareContent.addDialogExtraInfo(0, attName);
						shareContent.addDialogExtraInfo(2, LanguageKeys.MAIL_TITLE_MYCASTLE,false);
					}
				}

				int pointType = detail.getPointType();
				if (reportState == WIN)
				{
					
					if (battleType == 3 || battleType == 7)
					{
						String lang = "";
						if (isAtt)
						{
							mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_VICTORY);
							lang = LanguageKeys.TIP_ATK_SUCCESS;
						}
						else
						{
							mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_VICTORY);
							lang = LanguageKeys.TIP_DEF_SUCCESS;
						}
						
						

						if (isAtt)
						{ // 攻城胜利
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105578);
						}
						else
						{ // 守城胜利
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105579);
						}
						
						switch (pointType)
						{
							case MailManager.Throne:
								if (isAtt)
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105700);
								}
								else
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105702);
								}
								break;
							case MailManager.Trebuchet:
								if (isAtt)
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105704);
								}
								else
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105706);
								}
								break;
							case MailManager.Crystal:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Crystal));
								break;
							case MailManager.Armory:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Armory));
								break;
							case MailManager.TrainingField:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_TrainingField));
								break;
							case MailManager.SupplyPoint:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_SupplyPoint));
								break;
							case MailManager.BessingTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_BessingTower));
								break;
							case MailManager.MedicalTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_MedicalTower));
								break;
							case MailManager.DragonTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_DragonTower));
								break;
							case MailManager.Barracks:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Barracks));
								break;
							case MailManager.TransferPoint:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_TransferPoint));
								break;
							case MailManager.Tile_allianceArea:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_allianceArea));
								break;
							case MailManager.tile_banner:
							case MailManager.tile_flagsbuilding:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_tile_banner));
								break;
							default:
								break;
						}

						
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115341, kill, loss);
						
						shareContent.setText(LanguageKeys.MAIL_TITLE_115341);
						shareContent.addDialogExtraInfo(0, kill);
						shareContent.addDialogExtraInfo(0, loss,false);
						
					}
					else if (battleType == 4 || battleType == 2)
					{
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105117);
					}
					else if (battleType == 6)
					{
						isKnightMail = true;
						knight = new ArrayList<BattleMailContents>();
						detail.setUid(getUid());
						detail.setType(getType());
						detail.setCreateTime(String.valueOf(getCreateTime()));
						knight.add(detail);
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105579);
						shareContent.setText(LanguageKeys.MAIL_TITLE_105579);
						shareContent.setDialogExtra(null);
					}
					else if(battleType == 10 || battleType == 11)
					{
						isDragonTowerMail = true;
						nameText = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHALLENGE_SUCESS);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115341, kill, loss);
						shareContent.setText(LanguageKeys.MAIL_TITLE_115341);
						shareContent.addDialogExtraInfo(0, kill);
						shareContent.addDialogExtraInfo(0, loss,false);
						
					}
					else if (detail.getMsReport() == 1)
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_133083);
						shareContent.setText(LanguageKeys.MAIL_TITLE_133083);
						shareContent.setDialogExtra(null);
					}
				}
				else
				{
					
					if (battleType == 3 || battleType == 7)
					{
						String lang = "";
						if (isAtt)
						{
							mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_FAILURE);
							lang = LanguageKeys.TIP_ATK_FAILED;
						}
						else
						{
							mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_FAILURE);
							lang = LanguageKeys.TIP_DEF_FAILED;
						}
						
						if (isbigLoss)
						{
							if (isAtt)
							{ // 攻城大败
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105583);
							}
							else
							{ // 守城大败
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105581);
							}
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105535);
							shareContent.setText(LanguageKeys.MAIL_TITLE_105535);
							shareContent.setDialogExtra(null);
						}
						else
						{
							if (isAtt)
							{ // 攻城失败
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105582);
							}
							else
							{ // 守城失败
								nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105580);
							}
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115341, kill, loss);
							
							shareContent.setText(LanguageKeys.MAIL_TITLE_115341);
							shareContent.addDialogExtraInfo(0, kill);
							shareContent.addDialogExtraInfo(0, loss,false);
						}

						switch (pointType)
						{
							case MailManager.Throne:
								if (isAtt)
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105701);
								}
								else
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105703);
								}
								break;
							case MailManager.Trebuchet:
								if (isAtt)
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105705);
								}
								else
								{
									nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105707);
								}
								break;
							case MailManager.Crystal:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Crystal));
								break;
							case MailManager.Armory:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Armory));
								break;
							case MailManager.TrainingField:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_TrainingField));
								break;
							case MailManager.SupplyPoint:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_SupplyPoint));
								break;
							case MailManager.BessingTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_BessingTower));
								break;
							case MailManager.MedicalTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_MedicalTower));
								break;
							case MailManager.DragonTower:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_DragonTower));
								break;
							case MailManager.Barracks:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_Barracks));
								break;
							case MailManager.TransferPoint:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_TransferPoint));
								break;
							case MailManager.Tile_allianceArea:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_allianceArea));
								break;
							case MailManager.tile_banner:
							case MailManager.tile_flagsbuilding:
								nameText = LanguageManager.getLangByKey(lang,
										LanguageManager.getLangByKey(LanguageKeys.POINT_TYPE_NAME_tile_banner));
								break;
							default:
								break;
						}
					}
					else if (battleType == 4 || battleType == 2)
					{
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105118);
					}
					else if (battleType == 6)
					{
						isKnightMail = true;
						knight = new ArrayList<BattleMailContents>();
						detail.setUid(getUid());
						detail.setType(getType());
						detail.setCreateTime(String.valueOf(getCreateTime()));
						knight.add(detail);
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105580);
						
						shareContent.setText(LanguageKeys.MAIL_TITLE_105580);
						shareContent.setDialogExtra(null);
					}
					else if(battleType == 10 || battleType == 11)
					{
						isDragonTowerMail = true;
						nameText = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHALLENGE_FAILED);
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115341, kill, loss);
						shareContent.setText(LanguageKeys.MAIL_TITLE_115341);
						shareContent.addDialogExtraInfo(0, kill);
						shareContent.addDialogExtraInfo(0, loss,false);
					}
					else if (detail.getMsReport() == 1)
					{
						mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_KNIGHT);
						nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
						
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_133083);
						shareContent.setText(LanguageKeys.MAIL_TITLE_133083);
						shareContent.setDialogExtra(null);
					}
				}
				
				shareExtra.setDescription(shareContent);

				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@JSONField(deserialize = false)
	public ShareMsgExtra getShareExtra()
	{
		return shareExtra;
	}

	@JSONField(deserialize = false)
	public void setShareExtra(ShareMsgExtra shareExtra)
	{
		this.shareExtra = shareExtra;
	}

	private String calculateKillandLoss(BattleMailContents detail)
	{
		boolean isAtt = false;
		String attUid = "";
		if (detail.getAtkUser() != null && detail.getAtkUser().getUid() != null)
			attUid = detail.getAtkUser().getUid();
		String playerUid = UserManager.getInstance().getCurrentUserId();
		if (StringUtils.isNotEmpty(playerUid) && playerUid.equals(attUid))
		{
			isAtt = true;
		}
		else
		{
			if (detail.getAtkHelper() != null && detail.getAtkHelper().size() > 0)
			{
				for (int i = 0; i < detail.getAtkHelper().size(); i++)
				{
					String helpUid = detail.getAtkHelper().get(i);
					if (StringUtils.isNotEmpty(helpUid) && helpUid.equals(playerUid))
					{
						isAtt = true;
						break;
					}
				}
			}
		}

		UserParams attUserInfo = null;
		if (isAtt)
		{
			attUserInfo = detail.getAtkUser();
		}
		else
		{
			attUserInfo = detail.getDefUser();
		}

		String npc = "";
		if (attUserInfo != null && StringUtils.isNotEmpty(attUserInfo.getNpcId()))
			npc = attUserInfo.getNpcId();
		int dead = 0;
		int num = 0;
		int hurt = 0;
		int kill = 0;
		int total = 0;

		if (StringUtils.isNotEmpty(npc))
		{
			if (detail.getDefReport() != null)
			{
				int count = detail.getDefReport().size();
				for (int i = 0; i < count; i++)
				{
					ArmyParams army = detail.getDefReport().get(i);
					if (army != null)
					{
						dead += army.getDead();
						num += army.getNum();
						hurt += army.getHurt();
						kill += army.getKill();
					}
				}
			}
			total = dead + num + hurt;
			if (total <= 0)
				total = 1;
		}
		else
		{
			if (isAtt)
			{
				if (detail.getAtkArmyTotal() != null)
				{
					kill += detail.getAtkArmyTotal().getKill();
					dead += detail.getAtkArmyTotal().getDead();
					hurt += detail.getAtkArmyTotal().getHurt();
					num += detail.getAtkArmyTotal().getNum();
				}

				List<Integer> genKillArr = detail.getAtkGenKill();
				if (genKillArr != null)
				{
					for (int i = 0; i < genKillArr.size(); i++)
					{
						kill += genKillArr.get(i).intValue();
					}
				}
			}
			else
			{
				if (detail.getDefArmyTotal() != null)
				{
					kill += detail.getDefArmyTotal().getKill();
					dead += detail.getDefArmyTotal().getDead();
					hurt += detail.getDefArmyTotal().getHurt();
					num += detail.getDefArmyTotal().getNum();
				}

				List<Integer> genKillArr = detail.getDefGenKill();
				if (genKillArr != null)
				{
					for (int i = 0; i < genKillArr.size(); i++)
					{
						kill += genKillArr.get(i).intValue();
					}
				}
				if (detail.getDefTowerKill() != null)
				{

					for (int i = 0; i < detail.getDefTowerKill().size(); i++)
					{
						TowerKillParams toweKill = detail.getDefTowerKill().get(i);
						if (toweKill != null)
							kill += toweKill.getKill();
					}
				}
				if (detail.getDefFortLost() != null)
				{
					for (int i = 0; i < detail.getDefFortLost().size(); i++)
					{
						ArmyParams armyParm = detail.getDefFortLost().get(i);
						if (armyParm != null)
						{
							kill += armyParm.getKill();
						}
					}
				}
			}
		}
		String ret = kill + "_" + dead;
		return ret;
	}
}
