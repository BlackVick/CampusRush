package com.blackviking.campusrush.Model;

public class MaterialModel {

    private String materialName;
    private String courseName;
    private String downloadLink;
    private String materialInfo;
    private String uploader;

    public MaterialModel() {
    }

    public MaterialModel(String materialName, String courseName, String downloadLink, String materialInfo, String uploader) {
        this.materialName = materialName;
        this.courseName = courseName;
        this.downloadLink = downloadLink;
        this.materialInfo = materialInfo;
        this.uploader = uploader;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getMaterialInfo() {
        return materialInfo;
    }

    public void setMaterialInfo(String materialInfo) {
        this.materialInfo = materialInfo;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
}
