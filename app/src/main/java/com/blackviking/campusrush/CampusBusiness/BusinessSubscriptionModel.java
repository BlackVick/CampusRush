package com.blackviking.campusrush.CampusBusiness;

public class BusinessSubscriptionModel {

    private String subscriptionStatus;
    private String paymentStatus;
    private String transactionRef;
    private String transactionDate;

    public BusinessSubscriptionModel() {
    }

    public BusinessSubscriptionModel(String subscriptionStatus, String paymentStatus, String transactionRef, String transactionDate) {
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionRef = transactionRef;
        this.transactionDate = transactionDate;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
}
