package com.elex.im.ui.controller;

public interface IJniCallHelper
{
	public <T> T excuteJNIMethod(final String methodName, final Object[] params);

	public void excuteJNIVoidMethod(final String methodName, final Object[] params);
}
