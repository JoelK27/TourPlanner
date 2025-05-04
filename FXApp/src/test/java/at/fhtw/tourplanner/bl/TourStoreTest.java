package at.fhtw.tourplanner.bl;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.store.TourStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TourStoreTest {
    private TourStore tourStore;

    @BeforeEach
    public void setUp() {
        tourStore = TourStore.getInstance();
    }

    private void clearAllTours() {
        List<Tour> allTours = tourStore.getAllTours();
        for (Tour tour : allTours) {
            tourStore.deleteTour(tour);
        }
    }

    @Test
    public void testCreateNewTour() {
        Tour newTour = tourStore.createNewTour();
        assertNotNull(newTour);
        assertEquals("New Tour", newTour.getName());
    }

    @Test
    public void testFindMatchingTours() {
        List<Tour> tours = tourStore.searchTours("Vienna");
        assertFalse(tours.isEmpty());
        assertTrue(tours.stream().anyMatch(tour -> tour.getName().contains("Vienna")));
    }

    @Test
    public void testUpdateTour() {
        Tour tour = tourStore.createNewTour();
        tourStore.updateTour(tour, "Updated Tour", "Updated Description", "From", "To", "Car", 100.0, 2.0);
        assertEquals("Updated Tour", tour.getName());
        assertEquals("Updated Description", tour.getTourDescription());
        assertEquals("From", tour.getFrom());
        assertEquals("To", tour.getTo());
        assertEquals("Car", tour.getTransportType());
        assertEquals(100.0, tour.getTourDistance());
        assertEquals(2.0, tour.getEstimatedTime());
    }

    @Test
    public void testDeleteTour() {
        clearAllTours();
        Tour tour = tourStore.createNewTour();
        tourStore.deleteTour(tour);
        List<Tour> tours = tourStore.searchTours("New Tour");
        assertTrue(tours.isEmpty());
    }

    @Test
    public void testCreateNewLog() {
        Tour tour = tourStore.createNewTour();
        Log newLog = tourStore.createNewLog(tour.getId());
        assertNotNull(newLog);
        assertEquals(tour.getId(), newLog.getTourId());
    }

    @Test
    public void testGetLogsForTour() {
        Tour tour = tourStore.createNewTour();
        Log newLog = tourStore.createNewLog(tour.getId());
        List<Log> logs = tourStore.getLogsForTour(tour.getId());
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains(newLog));
    }

    @Test
    public void testUpdateLog() {
        Tour tour = tourStore.createNewTour();
        Log log = tourStore.createNewLog(tour.getId());
        String updatedComment = "Updated Comment";
        int updatedDifficulty = 5;
        double updatedDistance = 150.0;
        Time updatedTime = Time.valueOf("02:30:00");
        int updatedRating = 4;

        log.setComment(updatedComment);
        log.setDifficulty(updatedDifficulty);
        log.setTotalDistance(updatedDistance);
        log.setTotalTime(updatedTime);
        log.setRating(updatedRating);

        tourStore.updateLog(log);

        List<Log> logs = tourStore.getLogsForTour(tour.getId());
        assertTrue(logs.contains(log));
        Log updatedLog = logs.get(0);
        assertEquals(updatedComment, updatedLog.getComment());
        assertEquals(updatedDifficulty, updatedLog.getDifficulty());
        assertEquals(updatedDistance, updatedLog.getTotalDistance());
        assertEquals(updatedTime, updatedLog.getTotalTime());
        assertEquals(updatedRating, updatedLog.getRating());
    }

    @Test
    public void testSearchToursWithMatchingLogs() {
        Tour tour = tourStore.createNewTour();
        Log log = tourStore.createNewLog(tour.getId());
        log.setComment("UniqueSearchTerm");

        tourStore.updateLog(log);
        List<Tour> results = tourStore.searchTours("UniqueSearchTerm");

        assertFalse(results.isEmpty());
        assertTrue(results.contains(tour));
    }

    @Test
    public void testDeleteLog() {
        Tour tour = tourStore.createNewTour();
        Log log = tourStore.createNewLog(tour.getId());

        tourStore.deleteLog(log);
        List<Log> logs = tourStore.getLogsForTour(tour.getId());

        assertTrue(logs.isEmpty());
    }

    @Test
    public void testSearchToursWithNoMatch() {
        List<Tour> results = tourStore.searchTours("NonExistentSearchTerm");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testAddMultipleLogsToTour() {
        Tour tour = tourStore.createNewTour();

        Log log1 = tourStore.createNewLog(tour.getId());
        Log log2 = tourStore.createNewLog(tour.getId());

        List<Log> logs = tourStore.getLogsForTour(tour.getId());
        assertEquals(2, logs.size());
        assertTrue(logs.contains(log1));
        assertTrue(logs.contains(log2));
    }

    @Test
    public void testUpdateTourWithEmptyFields() {
        Tour tour = tourStore.createNewTour();

        tourStore.updateTour(tour, "", "", "", "", "", 0.0, 0.0);

        assertEquals("", tour.getName());
        assertEquals("", tour.getTourDescription());
        assertEquals("", tour.getFrom());
        assertEquals("", tour.getTo());
        assertEquals("", tour.getTransportType());
        assertEquals(0.0, tour.getTourDistance());
        assertEquals(0.0, tour.getEstimatedTime());
    }
}