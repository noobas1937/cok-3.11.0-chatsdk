package com.elex.chatservice.model;

import android.graphics.Color;


public class DanmuInfo
{
	private boolean isSelf;
	private String text;
	private int colorIndex;
	private boolean isStroke;
	
	private static final int DANMU_COLOR_1 = 0xffc7beb3;
	private static final int DANMU_COLOR_2 = 0xff56e578;
	private static final int DANMU_COLOR_3 = 0xff4599f8;
	private static final int DANMU_COLOR_4 = 0xffaf49ea;
	private static final int DANMU_COLOR_5 = 0xffe8771f;
	private static final int DANMU_COLOR_6 = 0xffedd538;
	private static final int DANMU_COLOR_7 = 0xffff0000;
	private static final int DANMU_COLOR_8 = 0xffc7beb3;
	
	
	
	public int getColor()
	{
		int color = Color.WHITE;
		switch (colorIndex)
		{
			case 1:
				color = DANMU_COLOR_1;
				break;
			case 2:
				color = DANMU_COLOR_2;
				break;
			case 3:
				color = DANMU_COLOR_3;
				break;
			case 4:
				color = DANMU_COLOR_4;
				break;
			case 5:
				color = DANMU_COLOR_5;
				break;
			case 6:
				color = DANMU_COLOR_6;
				break;
			case 7:
				color = DANMU_COLOR_7;
				break;
			case 8:
				color = DANMU_COLOR_8;
				break;
			default:
				color = Color.WHITE;
				break;
		}
		return color;
	}
	
	
	public boolean isSelf()
	{
		return isSelf;
	}


	public void setSelf(boolean isSelf)
	{
		this.isSelf = isSelf;
	}



	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	public int getColorIndex()
	{
		return colorIndex;
	}
	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}
	public boolean isStroke()
	{
		return isStroke;
	}
	public void setStroke(boolean isStroke)
	{
		this.isStroke = isStroke;
	}
	
	private DanmuInfo(DanmuInfoBuilder builder)
	{
		this.text = builder.text;
		this.colorIndex = builder.colorIndex;
		this.isStroke = builder.isStroke;
		this.isSelf = builder.isSelf;
	}
	
	public static class DanmuInfoBuilder
	{
		private String text = "";
		private int colorIndex = 0;
		private boolean isStroke = false;
		private boolean isSelf = false;
		
		public DanmuInfoBuilder(String text)
		{
			this.text = text;
		}
		
		public DanmuInfoBuilder colorIndex(int colorIndex)
		{
			this.colorIndex = colorIndex;
			return this;
		}
		
		public DanmuInfoBuilder stokeColor(int colorIndex)
		{
			this.colorIndex = colorIndex;
			return this;
		}
		
		public DanmuInfoBuilder isSelf()
		{
			this.isSelf = true;
			return this;
		}
		
		public DanmuInfo build()
		{
			return new DanmuInfo(this);
		}
	}
}
