package at.fhtw.tourplanner.bl;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.store.TourStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}