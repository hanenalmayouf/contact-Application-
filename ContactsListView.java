package com.app.contacts;

public class ContactsListView {
    public String username;
    public String fullName;
    public String callNumber;
    public String imagePath;

    public ContactsListView(String username, String fullName, String callNumber, String imagePath) {
        this.username = username;
        this.fullName = fullName;
        this.callNumber = callNumber;
        this.imagePath = imagePath;
    }

    public ContactsListView() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
