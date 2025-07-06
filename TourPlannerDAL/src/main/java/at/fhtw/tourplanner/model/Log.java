package at.fhtw.tourplanner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.sql.Date;
import java.sql.Time;

@Entity
@Data
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonView({Views.Internal.class, Views.Export.class})
    private Date date;

    @JsonView({Views.Internal.class, Views.Export.class})
    private Time time;

    @JsonView({Views.Internal.class, Views.Export.class})
    private String comment;

    @Range(min = 1, max = 5, message = "Difficulty must be between 1 and 5")
    @JsonView({Views.Internal.class, Views.Export.class})
    private int difficulty;

    @JsonView({Views.Internal.class, Views.Export.class})
    private double totalDistance;

    @NotNull
    @JsonView({Views.Internal.class, Views.Export.class})
    private Time totalTime;

    @Range(min = 1, max = 5, message = "Rating must be between 1 and 5")
    @JsonView({Views.Internal.class, Views.Export.class})
    private int rating;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonIgnore
    private Tour tour;

    // Helper method to get tourId for JSON serialization
    public int getTourId() {
        return tour != null ? tour.getId() : 0;
    }

    // Helper method to set tourId (used by JSON deserialization)
    public void setTourId(int tourId) {
        // This will be handled by the controller
    }
}
