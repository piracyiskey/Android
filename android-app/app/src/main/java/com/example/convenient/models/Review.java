package com.example.convenient.models;

public class Review {
    private String profileURL;
    private String userName;
    private double star;
    private String body;
    private String createdDate;

    public Review() {}

    public Review(String profileURL, String userName, double star, String body, String createdDate) {
        this.profileURL = profileURL;
        this.userName = userName;
        this.star = star;
        this.body = body;
        this.createdDate = createdDate;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getStar() {
        return star;
    }

    public void setStar(double star) {
        this.star = star;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
