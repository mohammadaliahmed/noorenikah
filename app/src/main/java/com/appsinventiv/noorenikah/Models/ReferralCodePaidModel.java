package com.appsinventiv.noorenikah.Models;

public class ReferralCodePaidModel {
    String phone,code;
    boolean paid;

    public ReferralCodePaidModel(String phone, String code, boolean paid) {
        this.phone = phone;
        this.code = code;
        this.paid = paid;
    }

    public ReferralCodePaidModel() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
