package at.fhtw.tourplanner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String tourDescription;
    
    @Column(name = "from_location")
    @JsonProperty("from")
    private String fromLocation;
    
    @Column(name = "to_location")
    @JsonProperty("to")
    private String toLocation;
    
    private String transportType;
    private double tourDistance;
    private double estimatedTime;

    @Column(name = "quick_notes", length = 1000)
    private String quickNotes;


    // Routingdaten für statische Karte
    private String encodedRouteGeometry; // GeoJSON-String
    private String startCoords; // z.B. "[16.3725,48.2082]"
    private String endCoords;   // z.B. "[13.0433,47.8222]"

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Log> logs = new ArrayList<>();

    // Getter und Setter für logs
    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    // Getter/Setter
    public String getEncodedRouteGeometry() { return encodedRouteGeometry; }
    public void setEncodedRouteGeometry(String encodedRouteGeometry) { this.encodedRouteGeometry = encodedRouteGeometry; }

    public String getStartCoords() { return startCoords; }
    public void setStartCoords(String startCoords) { this.startCoords = startCoords; }

    public String getEndCoords() { return endCoords; }
    public void setEndCoords(String endCoords) { this.endCoords = endCoords; }

    // Hilfsmethoden für double[] (für ImageService)
    public double[] getStartCoordsAsArray() {
        if (startCoords == null) return null;
        String s = startCoords.replaceAll("[\\[\\]\\s]", "");
        String[] parts = s.split(",");
        if (parts.length != 2) return null;
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    public double[] getEndCoordsAsArray() {
        if (endCoords == null) return null;
        String s = endCoords.replaceAll("[\\[\\]\\s]", "");
        String[] parts = s.split(",");
        if (parts.length != 2) return null;
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    public String getQuickNotes() { return quickNotes; }
    public void setQuickNotes(String quickNotes) { this.quickNotes = quickNotes; }
}
