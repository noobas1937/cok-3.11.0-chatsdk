package com.elex.im.core.util;

public interface IAnalyticTracker
{
	public void trackException(String exceptionType, String funcInfo, String cause, String message);

//	public void transportMail(String jsonStr, boolean isShowDetectMail);
	public String getPublishChannelName();

	/**
	 * 值为String类型
	 */
	public void trackMessage(String messageType, String... args);
	/**
	 * 值可以是int、long、String三种类型
	 */
	public void trackValue(String messageType, Object... args);
}
