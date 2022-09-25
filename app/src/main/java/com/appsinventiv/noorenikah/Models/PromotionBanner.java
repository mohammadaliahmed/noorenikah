package com.appsinventiv.noorenikah.Models;

public class PromotionBanner {
    String url,imgUrl;

    public PromotionBanner(String url, String imgUrl) {
        this.url = url;
        this.imgUrl = imgUrl;
    }

    public PromotionBanner() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
