package com.mydrakortv.beta.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Configuration {
    @SerializedName("app_config")
    @Expose
    private AppConfig appConfig;
    @SerializedName("ads_config")
    @Expose
    private AdsConfig adsConfig;
    @SerializedName("payment_config")
    @Expose
    private PaymentConfig paymentConfig;

    @SerializedName("apk_version_info")
    @Expose
    private ApkUpdateInfo apkUpdateInfo;

    public Configuration() {
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public AdsConfig getAdsConfig() {
        return adsConfig;
    }

    public void setAdsConfig(AdsConfig adsConfig) {
        this.adsConfig = adsConfig;
    }

    public PaymentConfig getPaymentConfig() {
        return paymentConfig;
    }

    public void setPaymentConfig(PaymentConfig paymentConfig) {
        this.paymentConfig = paymentConfig;
    }


    public ApkUpdateInfo getApkUpdateInfo() {
        return apkUpdateInfo;
    }

    public void setApkUpdateInfo(ApkUpdateInfo apkUpdateInfo) {
        this.apkUpdateInfo = apkUpdateInfo;
    }
}
