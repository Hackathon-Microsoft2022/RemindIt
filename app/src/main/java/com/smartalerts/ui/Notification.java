package com.smartalerts.ui;

public class Notification {

    private  int id;
    private String title;
    private String description;
    private String eventType;
    private String locationDetail ;

    public int getId() {
        return id;
    }

    public Notification(int id, String title, String description, String eventType, String locationDetail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.locationDetail = locationDetail;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocationDetail() {
        return locationDetail;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }
}
