package com.elex.chatservice.util.downzip;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DownLoadEntity implements Serializable, Parcelable {
    private String groupId;
    private String url;
    private int downloadPercent = 0;

    private Status status = Status.PENDING;

    public DownLoadEntity(String name, String url) {
        this.groupId = name;
        this.url = url;
    }

    public String getGroupId()
	{
		return groupId;
	}



	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}



	public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return groupId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupId);
        dest.writeString(url);
        dest.writeInt(downloadPercent);
        dest.writeValue(status);
    }

    public static final Parcelable.Creator<DownLoadEntity> CREATOR =
            new Parcelable.Creator<DownLoadEntity>() {

                @Override
                public DownLoadEntity createFromParcel(Parcel source) {
                    String name = source.readString();
                    String url = source.readString();
                    int downloadPercent = source.readInt();
                    Status status = (Status)source.readValue(new ClassLoader(){});
                    DownLoadEntity appContent = new DownLoadEntity(name, url);
                    appContent.setDownloadPercent(downloadPercent);
                    appContent.setStatus(status);
                    return appContent;
                }

                @Override
                public DownLoadEntity[] newArray(int size)
                {
                    return new DownLoadEntity[size];
                }
            };

    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING(1),
        /**
         * Indicates that the task is wating.
         */
        WAITING(2),
        /**
         * Indicates that the task is downloading.
         */
        DOWNLOADING(3),

        /**
         * Indicates that the task was paused.
         */
        PAUSED(4),

        /**
         * Indicates that the task has finished.
         */
        FINISHED(5);

        private int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status getByValue(int value) {
            switch (value) {
                case 1:
                    return Status.PENDING;
                case 2:
                    return Status.WAITING;
                case 3:
                    return Status.DOWNLOADING;
                case 4:
                    return Status.PAUSED;
                case 5:
                    return Status.FINISHED;
            }
            return Status.PENDING;
        }
    }
}
