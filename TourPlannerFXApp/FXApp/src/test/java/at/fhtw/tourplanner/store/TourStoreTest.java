package at.fhtw.tourplanner.store;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.apiclient.TourApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TourStoreTest {
    private TourApiService apiService;

    @BeforeEach
    public void setUp() {
        apiService = TourApiService.getInstance();
    }

    private void clearAllTours() {
        List<Tour> allTours = apiService.getAllTours();
        for (Tour tour : allTours) {
            apiService.deleteTour(tour);
        }
    }

    @Test
    public void testCreateNewTour() {
        Tour newTour = apiService.createNewTour();
        assertNotNull(newTour);
        assertEquals("New Tour", newTour.getName());
    }

    @Test
    public void testFindMatchingTours() {
        List<Tour> tours = apiService.searchTours("Vienna");
        assertFalse(tours.isEmpty());
        assertTrue(tours.stream().anyMatch(tour -> tour.getName().contains("Vienna")));
    }

    @Test
    public void testUpdateTour() {
        Tour tour = apiService.createNewTour();
        apiService.updateTour(tour, "Updated Tour", "Updated Description", "From", "To", "Car", 100.0, 2.0);
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
        Tour tour = apiService.createNewTour();
        apiService.deleteTour(tour);
        List<Tour> tours = apiService.searchTours("New Tour");
        assertTrue(tours.isEmpty());
    }

    @Test
    public void testCreateNewLog() {
        Tour tour = apiService.createNewTour();
        Log newLog = apiService.createNewLog(tour.getId());
        assertNotNull(newLog);
        assertEquals(tour.getId(), newLog.getTourId());
    }

    @Test
    public void testGetLogsForTour() {
        Tour tour = apiService.createNewTour();
        Log newLog = apiService.createNewLog(tour.getId());
        List<Log> logs = apiService.getLogsForTour(tour.getId());
        assertFalse(logs.isEmpty());
        assertTrue(logs.contains(newLog));
    }

    @Test
    public void testUpdateLog() {
        Tour tour = apiService.createNewTour();
        Log log = apiService.createNewLog(tour.getId());
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

        apiService.updateLog(log);

        List<Log> logs = apiService.getLogsForTour(tour.getId());
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
        Tour tour = apiService.createNewTour();
        Log log = apiService.createNewLog(tour.getId());
        log.setComment("UniqueSearchTerm");

        apiService.updateLog(log);
        List<Tour> results = apiService.searchTours("UniqueSearchTerm");

        assertFalse(results.isEmpty());
        assertTrue(results.contains(tour));
    }

    @Test
    public void testDeleteLog() {
        Tour tour = apiService.createNewTour();
        Log log = apiService.createNewLog(tour.getId());

        apiService.deleteLog(log);
        List<Log> logs = apiService.getLogsForTour(tour.getId());

        assertTrue(logs.isEmpty());
    }

    @Test
    public void testSearchToursWithNoMatch() {
        List<Tour> results = apiService.searchTours("NonExistentSearchTerm");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testAddMultipleLogsToTour() {
        Tour tour = apiService.createNewTour();

        Log log1 = apiService.createNewLog(tour.getId());
        Log log2 = apiService.createNewLog(tour.getId());

        List<Log> logs = apiService.getLogsForTour(tour.getId());
        assertEquals(2, logs.size());
        assertTrue(logs.contains(log1));
        assertTrue(logs.contains(log2));
    }

    @Test
    public void testUpdateTourWithEmptyFields() {
        Tour tour = apiService.createNewTour();

        apiService.updateTour(tour, "", "", "", "", "", 0.0, 0.0);

        assertEquals("", tour.getName());
        assertEquals("", tour.getTourDescription());
        assertEquals("", tour.getFrom());
        assertEquals("", tour.getTo());
        assertEquals("", tour.getTransportType());
        assertEquals(0.0, tour.getTourDistance());
        assertEquals(0.0, tour.getEstimatedTime());
    }
}