package at.fhtw.tourplanner.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
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

    @Override
    public String toString() {
        return name;
    }
}