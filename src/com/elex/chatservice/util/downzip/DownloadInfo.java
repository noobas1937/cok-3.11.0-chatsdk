package com.elex.chatservice.util.downzip;

public class DownloadInfo {

    private String url; 
    private int taskId;
    private long downloadLength;
    private int downloadSuccess;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public int isDownloadSuccess() {
        return downloadSuccess;
    }

    public void setDownloadSuccess(int downloadSuccess) {
        this.downloadSuccess = downloadSuccess;
    }
}
