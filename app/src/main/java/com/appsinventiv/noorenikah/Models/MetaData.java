
package com.appsinventiv.noorenikah.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaData {
    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    @SerializedName("error")
    @Expose
    private Error error;


    @SerializedName("user")
    @Expose
    private UserModel user;
//    private Ustad ustad;

//    public Ustad getUstad() {
//        return ustad;
//    }
//
//    public void setUstad(Ustad ustad) {
//        this.ustad = ustad;
//    }


    public MetaData(Error error, UserModel user) {
        this.error = error;
        this.user = user;
    }

    public MetaData(UserModel user) {
        this.user = user;
    }



    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
