package at.fhtw.tourplanner.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

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

    // Explicit getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Time getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Time totalTime) {
        this.totalTime = totalTime;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return date + " - " + comment;
    }
}