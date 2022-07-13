package com.elex.im.core.net;

import java.io.Serializable;

import com.elex.im.core.util.NetworkUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;

public class WSServerInfo implements Serializable
{
	private static final long	serialVersionUID	= -8642074625225319216L;
	
	public String				protocol;
	public String				address;
	public String				port;
	public double				loss;
	public double				delay;
	/** GMT格式 */
	public long					firstTestTime;
	public long					lastTestTime;
	public int					testCount;
	public long					lastErrorTime;

	public WSServerInfo(String protocol, String address, String port)
	{
		this.protocol = protocol;
		this.address = address;
		this.port = port;
	}

	/**
	 * 如果以前没有测试过，就直接插入测试结果集
	 * 如果测试过，这里的信息就无用，转而更新旧的测试记录
	 */
	public void initTestInfo()
	{
		firstTestTime = TimeManager.getInstance().getCurrentTimeMS();
		lastTestTime = TimeManager.getInstance().getCurrentTimeMS();
		testCount = 1;
	}

	public boolean equalTo(WSServerInfo info)
	{
		return info.address.equals(address) && info.protocol.equals(protocol) && info.port.equals(port);
	}

	public void updateTestResult(WSServerInfo newInfo)
	{
		lastTestTime = TimeManager.getInstance().getCurrentTimeMS();
		if(!isTestTooOld())
		{
			// 求均值
			loss = (loss * testCount + newInfo.loss) / (testCount + 1);
			
			if(delay == NetworkUtil.DELAY_OF_ALL_LOST && newInfo.delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				delay = NetworkUtil.DELAY_OF_ALL_LOST;
			}else if(delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				delay = newInfo.delay;
			}else if(newInfo.delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				// delay = delay;
			}else{
				delay = (delay * testCount + newInfo.delay) / (testCount + 1);
			}
			
			testCount++;
		}
		else
		{
			loss = newInfo.loss;
			delay = newInfo.delay;
			testCount = 1;
			firstTestTime = TimeManager.getInstance().getCurrentTimeMS();
		}
	}
	
	public boolean isTestTooOld()
	{
		long timeoutMS = WebSocketManager.getInstance().networkOptimizationTimeout * 1000;
		return (TimeManager.getInstance().getCurrentTimeMS() - firstTestTime) > 
			(timeoutMS == 0 ? WebSocketManager.DEFAULT_TEST_RESULT_VALID_TIME : timeoutMS);
	}
	
	/**
	 * 判断上次断线时间，小于一定时间则不再连（这个时间取决于重连间隔时间、有多少个服可选，大致定为2分钟）
	 * 更精确的，时间应该是等差数列Sn=a1*n+[n*(n-1)*d]/2=5*n+[n*(n-1)*5]/2 （重连的a1和d都是5，WebSocketManager.RECONNECT_INTERVAL）
	 * 如果断线时间大于测试时间，也不再连
	 */
	public boolean isConnectionErrorRecently()
	{
		long timePassed = TimeManager.getInstance().getCurrentTimeMS() - lastErrorTime;
		return (timePassed > 0 && timePassed <= (2 * 60 * 1000)) || lastErrorTime > lastTestTime;
	}

	public String toString()
	{
		if (delay == 0 && firstTestTime == 0 && lastTestTime == 0)
		{
			return address + " " + protocol + ":" + port + (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
		else if (testCount == 0 && firstTestTime == 0 && lastTestTime == 0)
		{
			return address + " " + protocol + ":" + port + " loss=" + Math.round(loss) + "% avgDelay=" + Math.round(delay) + "ms"
					+ (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
		else
		{
			return address + " " + protocol + ":" + port + " loss=" + Math.round(loss) + "% avgDelay=" + Math.round(delay) + "ms"
					+ " testCnt=" + testCount + " firstTestTime=" + firstTestTime + " lastTestTime=" + lastTestTime
					+ (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
	}

	public boolean isValid()
	{
		return StringUtils.isNotEmpty(protocol) && StringUtils.isNotEmpty(address) && StringUtils.isNotEmpty(port);
	}
}