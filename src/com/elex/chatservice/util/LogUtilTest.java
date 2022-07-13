package com.elex.chatservice.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtilTest {
	 private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
	 private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
	            "yyyy-MM-dd HH:mm:ss");// 日志的输出格式
	 private static String GLASS_LOG_PATH_SDCARD_DIR = "/sdcard/";
			 private static String GLASSLOGFILEName = "glass_log.txt";
	public static void writeLogToFile(String logType, String tag, String text) {// 新建或打开日志文件
        
            Date time = new Date();
            String needWriteFile = logfile.format(time);
            String needWriteMessage = myLogSdf.format(time) + "    " + logType
                    + "    " + tag + "    " + text;
            File file = new File(GLASS_LOG_PATH_SDCARD_DIR, GLASSLOGFILEName);
            try {
                FileWriter filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                bufWriter.write(needWriteMessage);
                bufWriter.newLine();
                bufWriter.close();
                filerWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
    }
}
