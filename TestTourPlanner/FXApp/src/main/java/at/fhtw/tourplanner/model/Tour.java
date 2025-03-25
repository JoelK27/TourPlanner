package at.fhtw.tourplanner.model;

import java.io.Serializable;

public class Tour implements Serializable {
    private int id;
    private String name;
    private String tourDescription;
    private String from;
    private String to;
    private String transportType;
    private double tourDistance;
    private double estimatedTime;

    public Tour() {
    }

    public Tour(int id, String name, String tourDescription, String from, String to,
                String transportType, double tourDistance, double estimatedTime) {
        this.id = id;
        this.name = name;
        this.tourDescription = tourDescription;
        this.from = from;
        this.to = to;
        this.transportType = transportType;
        this.tourDistance = tourDistance;
        this.estimatedTime = estimatedTime;
    }

    // Explicit getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTourDescription() {
        return tourDescription;
    }

    public void setTourDescription(String tourDescription) {
        this.tourDescription = tourDescription;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public double getTourDistance() {
        return tourDistance;
    }

    public void setTourDistance(double tourDistance) {
        this.tourDistance = tourDistance;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    @Override
    public String toString() {
        return name;
    }
}