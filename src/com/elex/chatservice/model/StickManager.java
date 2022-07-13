package com.elex.chatservice.model;

import org.apache.commons.lang.StringUtils;

public class StickManager
{
	private static final String[]	emojsOriginal	= { "✌", "✌️", "💪", "😡", "😒", "😄", "👍", "😓" };	// "️😀","😁","👍","👎","😤","😳"
	private static final String[]	emojsCustom		= { "13", "13", "14", "15", "16", "17", "18", "19" }; // "14","13","57","58","11","6"
	/** 相对于头像的大小，大于0时有效 */
	private static final double[]	emojsScale		= { -1, -1, -1, 1.5, 1.5, 1.5, 1.5, 1.5 };
	
	public static String getPredefinedEmoj(String content)
	{
		String result = null;

		if (StringUtils.isEmpty(content) || content.length() > 2)
			return result;

		for (int i = 0; i < emojsOriginal.length; i++)
		{
			if (content.equals(emojsOriginal[i]))
			{
				result = emojsCustom[i];
				break;
			}
		}
		if (result != null)
		{
			result = "emoj" + result;
		}
		return result;
	}

	public static double getPredefinedEmojScale(String content)
	{
		double result = -1;

		if (StringUtils.isEmpty(content) || content.length() > 2)
			return result;

		for (int i = 0; i < emojsOriginal.length; i++)
		{
			if (content.equals(emojsOriginal[i]))
			{
				result = emojsScale[i];
				break;
			}
		}
		
		return result;
	}

	public static String getAliEmoj(String content)
	{
		Integer emo = null;
		try
		{
			emo = Integer.decode(content);
		}
		catch (Exception e)
		{
		}
		if (emo != null && emo.intValue() >= 1 && emo.intValue() <= 50)
		{
			return "ali" + Integer.toString(emo.intValue());
		}
		return null;
	}
}
