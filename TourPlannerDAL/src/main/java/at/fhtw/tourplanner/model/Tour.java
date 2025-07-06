package at.fhtw.tourplanner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
@Setter
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Internal.class) // Nur für interne Verwendung
    private int id;

    @NotNull(message = "Tour name cannot be null")
    @NotEmpty(message = "Tour name cannot be empty")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String name;

    @JsonView({Views.Internal.class, Views.Export.class})
    private String tourDescription;

    @NotNull(message = "From location cannot be null")
    @NotEmpty(message = "From location cannot be empty")
    @Column(name = "from_location")
    @JsonProperty("from")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String fromLocation;

    @NotNull(message = "To location cannot be null")
    @NotEmpty(message = "To location cannot be empty")
    @Column(name = "to_location")
    @JsonProperty("to")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String toLocation;

    @JsonView({Views.Internal.class, Views.Export.class})
    private String transportType;

    @NotNull(message = "Tour distance cannot be null")
    @Min(value = 0, message = "Tour distance must be positive")
    @JsonView({Views.Internal.class, Views.Export.class})
    private double tourDistance;

    @NotNull(message = "Estimated time cannot be null")
    @Min(value = 0, message = "Estimated time must be positive")
    @JsonView({Views.Internal.class, Views.Export.class})
    private double estimatedTime;

    @JsonView({Views.Internal.class, Views.Export.class})
    @Column(name = "quick_notes", length = 1000)
    private String quickNotes;


    // Routingdaten für statische Karte
    @Column(columnDefinition = "TEXT")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String encodedRouteGeometry; // GeoJSON-String

    @Column(columnDefinition = "TEXT")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String startCoords; // z.B. "[16.3725,48.2082]"

    @Column(columnDefinition = "TEXT")
    @JsonView({Views.Internal.class, Views.Export.class})
    private String endCoords;   // z.B. "[13.0433,47.8222]"

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView({Views.Internal.class, Views.Export.class})
    private List<Log> logs = new ArrayList<>();

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
