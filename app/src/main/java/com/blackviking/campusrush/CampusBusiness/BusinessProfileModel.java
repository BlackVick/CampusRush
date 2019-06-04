package com.blackviking.campusrush.CampusBusiness;

public class BusinessProfileModel {

    private String businessName;
    private String businessAddress;
    private String businessCategory;
    private String businessDescription;
    private String businessPhone;
    private String businessFacebook;
    private String businessInstagram;
    private String businessTwitter;

    public BusinessProfileModel() {
    }

    public BusinessProfileModel(String businessName, String businessAddress, String businessCategory, String businessDescription, String businessPhone, String businessFacebook, String businessInstagram, String businessTwitter) {
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.businessCategory = businessCategory;
        this.businessDescription = businessDescription;
        this.businessPhone = businessPhone;
        this.businessFacebook = businessFacebook;
        this.businessInstagram = businessInstagram;
        this.businessTwitter = businessTwitter;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getBusinessFacebook() {
        return businessFacebook;
    }

    public void setBusinessFacebook(String businessFacebook) {
        this.businessFacebook = businessFacebook;
    }

    public String getBusinessInstagram() {
        return businessInstagram;
    }

    public void setBusinessInstagram(String businessInstagram) {
        this.businessInstagram = businessInstagram;
    }

    public String getBusinessTwitter() {
        return businessTwitter;
    }

    public void setBusinessTwitter(String businessTwitter) {
        this.businessTwitter = businessTwitter;
    }
}
