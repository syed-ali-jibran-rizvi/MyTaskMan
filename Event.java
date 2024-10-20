package com.example.mytaskman;

public class Event {
    private String id;
    private String name;
    private long date;
    private String status;
    private String details;
    private long expiryTime;
    private int rank; // New field for event rank

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Event.class)
    }

    public Event(String id, String name, long date, String status, String details, long expiryTime) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.status = status;
        this.details = details;
        this.expiryTime = expiryTime;
    }

    // Getters and setters for Firebase

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
