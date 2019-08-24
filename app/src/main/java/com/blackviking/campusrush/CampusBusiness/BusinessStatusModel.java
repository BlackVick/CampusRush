package com.blackviking.campusrush.CampusBusiness;

public class BusinessStatusModel {

    private String status;
    private String info;

    public BusinessStatusModel() {
    }

    public BusinessStatusModel(String status, String info) {
        this.status = status;
        this.info = info;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
