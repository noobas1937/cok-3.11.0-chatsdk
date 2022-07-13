package com.elex.chatservice.model;

import java.util.List;

public class ParseResult {
	private String text;
	private List<ColorFragment> colorExtraList;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<ColorFragment> getColorExtraList() {
		return colorExtraList;
	}
	public void setColorExtraList(List<ColorFragment> colorExtraList) {
		this.colorExtraList = colorExtraList;
	}
	
	
}
