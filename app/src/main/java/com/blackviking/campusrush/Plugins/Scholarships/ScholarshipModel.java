package com.blackviking.campusrush.Plugins.Scholarships;

public class ScholarshipModel {

    private String title;
    private String location;
    private String program;
    private String deadline;
    private String commencement;
    private String eligibility;
    private String info;

    public ScholarshipModel() {
    }

    public ScholarshipModel(String title, String location, String program, String deadline, String commencement, String eligibility, String info) {
        this.title = title;
        this.location = location;
        this.program = program;
        this.deadline = deadline;
        this.commencement = commencement;
        this.eligibility = eligibility;
        this.info = info;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCommencement() {
        return commencement;
    }

    public void setCommencement(String commencement) {
        this.commencement = commencement;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
