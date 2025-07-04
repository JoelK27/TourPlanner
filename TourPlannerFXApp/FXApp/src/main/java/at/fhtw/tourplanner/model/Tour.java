package at.fhtw.tourplanner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

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

    // Routingdaten für statische Karte
    private String encodedRouteGeometry; // GeoJSON-String
    private String startCoords; // z.B. "[16.3725,48.2082]"
    private String endCoords;   // z.B. "[13.0433,47.8222]"
    private String quickNotes;

    private List<Log> logs;

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
        this.quickNotes = "";
    }

    // Hilfsmethoden für double[] - mit @JsonIgnore markiert
    @JsonIgnore
    public double[] getStartCoordsAsArray() {
        if (startCoords == null) return null;
        String s = startCoords.replaceAll("[\\[\\]\\s]", "");
        String[] parts = s.split(",");
        if (parts.length != 2) return null;
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    @JsonIgnore
    public double[] getEndCoordsAsArray() {
        if (endCoords == null) return null;
        String s = endCoords.replaceAll("[\\[\\]\\s]", "");
        String[] parts = s.split(",");
        if (parts.length != 2) return null;
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    @Override
    public String toString() {
        return name;
    }
}