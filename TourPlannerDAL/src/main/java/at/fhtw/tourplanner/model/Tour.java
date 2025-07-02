package at.fhtw.tourplanner.model;

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
}
