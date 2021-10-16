package com.example.meetup.model;

public class Users {
    String userName,city,country,profession,status, profileImage,interest;

    public Users() {
    }

    public Users(String userName, String city, String counrty, String profession,  String profileImage,String status, String interest) {
        this.userName = userName;
        this.city = city;
        this.country = counrty;
        this.profession = profession;
        this.status = status;
        this.profileImage = profileImage;
        this.interest = interest;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }
}
