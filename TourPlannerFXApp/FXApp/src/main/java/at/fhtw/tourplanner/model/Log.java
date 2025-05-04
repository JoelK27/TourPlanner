package at.fhtw.tourplanner.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
public class Log implements Serializable {
    private int id;
    private int tourId;
    private Date date;
    private Time time;
    private String comment;
    private int difficulty;
    private double totalDistance;
    private Time totalTime;
    private int rating;

    public Log() {
    }

    public Log(int id, int tourId, Date date, Time time, String comment,
               int difficulty, double totalDistance, Time totalTime, int rating) {
        this.id = id;
        this.tourId = tourId;
        this.date = date;
        this.time = time;
        this.comment = comment;
        this.difficulty = difficulty;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return date + " - " + comment;
    }
}