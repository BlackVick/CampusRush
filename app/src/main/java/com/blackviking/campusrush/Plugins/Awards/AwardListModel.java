package com.blackviking.campusrush.Plugins.Awards;

public class AwardListModel {

    private String awardName;
    private String awardFaculty;
    private String awardDepartment;

    public AwardListModel() {
    }

    public AwardListModel(String awardName, String awardFaculty, String awardDepartment) {
        this.awardName = awardName;
        this.awardFaculty = awardFaculty;
        this.awardDepartment = awardDepartment;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    public String getAwardFaculty() {
        return awardFaculty;
    }

    public void setAwardFaculty(String awardFaculty) {
        this.awardFaculty = awardFaculty;
    }

    public String getAwardDepartment() {
        return awardDepartment;
    }

    public void setAwardDepartment(String awardDepartment) {
        this.awardDepartment = awardDepartment;
    }
}
