package com.appsinventiv.noorenikah.Models;

public class RequestPayoutModel {
    String id,phone,payoutOption,name;
    int amount;
    long time,payoutTime;
    boolean paid;


    public RequestPayoutModel(String id, String phone, String payoutOption, String name,int amount, long time) {
        this.id = id;
        this.phone = phone;
        this.payoutOption = payoutOption;
        this.name = name;
        this.amount = amount;
        this.time = time;
    }

    public long getPayoutTime() {
        return payoutTime;
    }

    public void setPayoutTime(long payoutTime) {
        this.payoutTime = payoutTime;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public RequestPayoutModel() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPayoutOption() {
        return payoutOption;
    }

    public void setPayoutOption(String payoutOption) {
        this.payoutOption = payoutOption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
