package at.fhtw.tourplanner.model;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {

    @Test
    void testDefaultValues() {
        Log log = new Log();
        assertEquals(0, log.getId());
        assertNull(log.getDate());
        assertNull(log.getTime());
        assertNull(log.getComment());
        assertEquals(0, log.getDifficulty());
        assertEquals(0.0, log.getTotalDistance());
        assertNull(log.getTotalTime());
        assertEquals(0, log.getRating());
        assertNull(log.getTour());
    }

    @Test
    void testSettersAndGetters() {
        Log log = new Log();
        Date date = Date.valueOf("2024-01-01");
        Time time = Time.valueOf("10:00:00");
        Time totalTime = Time.valueOf("01:30:00");

        log.setId(5);
        log.setDate(date);
        log.setTime(time);
        log.setComment("Test");
        log.setDifficulty(3);
        log.setTotalDistance(42.5);
        log.setTotalTime(totalTime);
        log.setRating(4);

        assertEquals(5, log.getId());
        assertEquals(date, log.getDate());
        assertEquals(time, log.getTime());
        assertEquals("Test", log.getComment());
        assertEquals(3, log.getDifficulty());
        assertEquals(42.5, log.getTotalDistance());
        assertEquals(totalTime, log.getTotalTime());
        assertEquals(4, log.getRating());
    }

    @Test
    void testTourReferenceAndTourId() {
        Log log = new Log();
        Tour tour = new Tour();
        tour.setId(99);
        log.setTour(tour);

        assertEquals(tour, log.getTour());
        assertEquals(99, log.getTourId());
    }

    @Test
    void testTourIdWhenTourIsNull() {
        Log log = new Log();
        log.setTour(null);
        assertEquals(0, log.getTourId());
    }

    @Test
    void testSetTourIdDoesNothing() {
        Log log = new Log();
        log.setTourId(123); // Should not throw or change anything
        assertEquals(0, log.getTourId());
    }
}