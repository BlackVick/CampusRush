package com.blackviking.campusrush.Model;

public class UserModel {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String status;
    private String profilePicture;
    private String profilePictureThumb;

    public UserModel() {
    }

    public UserModel(String username, String firstName, String lastName, String email, String gender, String status, String profilePicture, String profilePictureThumb) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.status = status;
        this.profilePicture = profilePicture;
        this.profilePictureThumb = profilePictureThumb;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePictureThumb() {
        return profilePictureThumb;
    }

    public void setProfilePictureThumb(String profilePictureThumb) {
        this.profilePictureThumb = profilePictureThumb;
    }
}
