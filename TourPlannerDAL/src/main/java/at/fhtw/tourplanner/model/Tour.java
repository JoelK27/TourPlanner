package at.fhtw.tourplanner.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String tourDescription;
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private double tourDistance;
    private double estimatedTime;
}
