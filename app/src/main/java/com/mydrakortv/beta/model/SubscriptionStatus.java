package com.mydrakortv.beta.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SubscriptionStatus implements Serializable {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("package_title")
    @Expose
    private String packageTitle;
    @SerializedName("expire_date")
    @Expose
    private String expireDate;
    @SerializedName("event_list")
    @Expose
    private List<String> eventList = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPackageTitle() {
        return packageTitle;
    }

    public void setPackageTitle(String packageTitle) {
        this.packageTitle = packageTitle;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public List<String> getEventList() {
        return eventList;
    }

    public void setEventList(List<String> eventList) {
        this.eventList = eventList;
    }
}
