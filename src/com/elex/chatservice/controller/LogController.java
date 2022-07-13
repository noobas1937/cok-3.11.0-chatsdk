package com.elex.chatservice.controller;

import android.content.Context;

import com.elex.optc.log.OPTCLogInstance;
import com.elex.optc.log.ReportLogLevel;

import java.util.Map;

/**
 * Created by wangyan on 17/9/12.
 */

public class LogController {

    public long startChatTime;
    public long startMailTime;

    private static volatile LogController instance;

    private LogController() {

    }

    public static LogController getInstance() {
        if (null == instance) {
            synchronized (LogController.class) {
                if (null == instance) {
                    instance = new LogController();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context, String appId, String userId, String serverId) {
        if (SwitchUtils.optcLogEnable) {
            OPTCLogInstance.getInstance().init(context, appId, userId, serverId);
            OPTCLogInstance.getInstance().setDebugEnable(true);
        }
    }

    /**
     * 销毁
     */
    public void destory() {
        if (SwitchUtils.optcLogEnable) {
            OPTCLogInstance.getInstance().destory();
        }
    }

    /**
     * 事件统计
     *
     * @param event
     */
    public void event(String event) {
        event(event, null, ReportLogLevel.INFO);
    }

    /**
     * 事件统计
     *
     * @param event
     * @param detail
     */
    public void event(String event, Map<String, String> detail) {
        event(event, detail, ReportLogLevel.INFO);
    }

    /**
     * 事件统计
     *
     * @param event
     * @param detail
     * @param level
     */
    public void event(final String event, final Map<String, String> detail, final ReportLogLevel level) {
        if (SwitchUtils.optcLogEnable) {
            OPTCLogInstance.getInstance().event(event, detail, level);
        }
    }

}
