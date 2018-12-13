package com;

import java.util.List;

public class CustomerProfile {

    String customerName;
    String customerAccountNumber;
    String customerGeo;
    List<Event> events;
    List<TrainingModel> models;
    List<eventAnalysis> eventAnalysisModels;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public String getCustomerGeo() {
        return customerGeo;
    }

    public void setCustomerGeo(String customerGeo) {
        this.customerGeo = customerGeo;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<TrainingModel> getModels() {
        return models;
    }

    public void setModels(List<TrainingModel> models) {
        this.models = models;
    }

    public List<eventAnalysis> getEventAnalysisModels() {
        return eventAnalysisModels;
    }

    public void setEventAnalysisModels(List<eventAnalysis> eventAnalysisModels) {
        this.eventAnalysisModels = eventAnalysisModels;
    }

    @Override
    public String toString() {
        return "CustomerProfile{" +
                "customerName='" + customerName + '\'' +
                ", customerAccountNumber='" + customerAccountNumber + '\'' +
                ", customerGeo='" + customerGeo + '\'' +
                ", Event Analysis" + eventAnalysisModels +
                '}';
    }
}
