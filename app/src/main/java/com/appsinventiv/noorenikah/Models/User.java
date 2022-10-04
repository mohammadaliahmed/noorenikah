package com.appsinventiv.noorenikah.Models;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {
    String name,referralCode, phone, password;
    String fcmKey;
    String livePicPath,  belonging, houseSize, city, houseAddress,
            nationality, fatherName, motherName, gender, jobOrBusiness,
            maritalStatus, education, religion, cast, homeType,sect,companyName,fatherOccupation,
            motherOccupation,about;
    int age,income,brothers,sisters;
    float height;
    String myReferralCode;
    HashMap<String,String> friends,blockedMe,iBlocked;
    boolean paid=false;
    boolean rejected;
    boolean liked=false;
    boolean phoneVerified;
    boolean matchMakerProfile;
    String matchMakerId;

    public User() {
    }

    public User(String name, String phone, String password, String referralCode, String myReferralCode) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.myReferralCode = myReferralCode;
        this.referralCode = referralCode;
    }

    public boolean isMatchMakerProfile() {
        return matchMakerProfile;
    }

    public void setMatchMakerProfile(boolean matchMakerProfile) {
        this.matchMakerProfile = matchMakerProfile;
    }

    public String getMatchMakerId() {
        return matchMakerId;
    }

    public void setMatchMakerId(String matchMakerId) {
        this.matchMakerId = matchMakerId;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isRejected() {
        return rejected;
    }

    public HashMap<String, String> getBlockedMe() {
        if(blockedMe==null) return new HashMap<>();

        return blockedMe;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setBlockedMe(HashMap<String, String> blockedMe) {
        this.blockedMe = blockedMe;
    }

    public HashMap<String, String> getiBlocked() {
        if(iBlocked==null) return new HashMap<>();
        return iBlocked;
    }

    public void setiBlocked(HashMap<String, String> iBlocked) {
        this.iBlocked = iBlocked;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFatherOccupation() {
        return fatherOccupation;
    }

    public void setFatherOccupation(String fatherOccupation) {
        this.fatherOccupation = fatherOccupation;
    }

    public String getMotherOccupation() {
        return motherOccupation;
    }

    public void setMotherOccupation(String motherOccupation) {
        this.motherOccupation = motherOccupation;
    }

    public String getMyReferralCode() {
        return myReferralCode;
    }

    public void setMyReferralCode(String myReferralCode) {
        this.myReferralCode = myReferralCode;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public HashMap<String, String> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, String> friends) {
        this.friends = friends;
    }

    public String getName() {
        return name;
    }

    public String getSect() {
        return sect;
    }

    public void setSect(String sect) {
        this.sect = sect;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFcmKey() {
        return fcmKey;
    }

    public void setFcmKey(String fcmKey) {
        this.fcmKey = fcmKey;
    }

    public String getLivePicPath() {
        return livePicPath;
    }

    public void setLivePicPath(String livePicPath) {
        this.livePicPath = livePicPath;
    }

    public String getBelonging() {
        return belonging;
    }

    public void setBelonging(String belonging) {
        this.belonging = belonging;
    }

    public String getHouseSize() {
        return houseSize;
    }

    public void setHouseSize(String houseSize) {
        this.houseSize = houseSize;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHouseAddress() {
        return houseAddress;
    }

    public void setHouseAddress(String houseAddress) {
        this.houseAddress = houseAddress;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJobOrBusiness() {
        return jobOrBusiness;
    }

    public void setJobOrBusiness(String jobOrBusiness) {
        this.jobOrBusiness = jobOrBusiness;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getHomeType() {
        return homeType;
    }

    public void setHomeType(String homeType) {
        this.homeType = homeType;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public int getBrothers() {
        return brothers;
    }

    public void setBrothers(int brothers) {
        this.brothers = brothers;
    }

    public int getSisters() {
        return sisters;
    }

    public void setSisters(int sisters) {
        this.sisters = sisters;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
