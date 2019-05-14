package com.blackviking.campusrush.Plugins.Vacancies;

public class VacancyModel {

    private String title;
    private String company;
    private String location;
    private String info;
    private String reference;
    private String type;

    public VacancyModel() {
    }

    public VacancyModel(String title, String company, String location, String info, String reference, String type) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.info = info;
        this.reference = reference;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
