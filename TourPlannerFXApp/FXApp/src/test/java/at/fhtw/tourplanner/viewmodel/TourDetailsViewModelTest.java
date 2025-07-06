package at.fhtw.tourplanner.viewmodel;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

public class TourDetailsViewModelTest {

    private TourDetailsViewModel viewModel;
    private Tour testTour;

    @BeforeEach
    void setUp() {
        viewModel = new TourDetailsViewModel();

        testTour = new Tour();
        testTour.setId(1);
        testTour.setName("Test Tour");
        testTour.setTourDescription("Test Description");
        testTour.setFrom("Vienna");
        testTour.setTo("Salzburg");
        testTour.setTransportType("Car");
        testTour.setTourDistance(300.0);
        testTour.setEstimatedTime(3.5);
    }

    @Test
    void testSetTourModel() {
        viewModel.setTourModel(testTour);

        assertEquals("Test Tour", viewModel.nameProperty().get());
        assertEquals("Test Description", viewModel.descriptionProperty().get());
        assertEquals("Vienna", viewModel.fromProperty().get());
        assertEquals("Salzburg", viewModel.toProperty().get());
        assertEquals("Car", viewModel.transportTypeProperty().get());
        assertEquals(300.0, viewModel.distanceProperty().get());
        assertEquals(3.5, viewModel.estimatedTimeProperty().get());
    }

    @Test
    void testSetTourModelWithNull() {
        viewModel.setTourModel(null);

        assertEquals("", viewModel.nameProperty().get());
        assertEquals("", viewModel.descriptionProperty().get());
        assertEquals("", viewModel.fromProperty().get());
        assertEquals("", viewModel.toProperty().get());
        assertEquals("Car", viewModel.transportTypeProperty().get());
        assertEquals(0.0, viewModel.distanceProperty().get());
        assertEquals(0.0, viewModel.estimatedTimeProperty().get());
    }

    @Test
    void testUpdateTourModel() {
        viewModel.setTourModel(testTour);

        // Change properties
        viewModel.nameProperty().set("Updated Tour");
        viewModel.descriptionProperty().set("Updated Description");
        viewModel.fromProperty().set("Graz");
        viewModel.toProperty().set("Linz");
        viewModel.transportTypeProperty().set("Bicycle");
        viewModel.distanceProperty().set(150.0);
        viewModel.estimatedTimeProperty().set(2.0);

        viewModel.updateTourModel();

        // Verify tour was updated
        assertEquals("Updated Tour", testTour.getName());
        assertEquals("Updated Description", testTour.getTourDescription());
        assertEquals("Graz", testTour.getFrom());
        assertEquals("Linz", testTour.getTo());
        assertEquals("Bicycle", testTour.getTransportType());
        assertEquals(150.0, testTour.getTourDistance());
        assertEquals(2.0, testTour.getEstimatedTime());
    }

    @Test
    void testCreateNewLog() {
        viewModel.setTourModel(testTour);

        Date date = Date.valueOf("2024-01-01");
        Time time = Time.valueOf("10:00:00");

        Log newLog = viewModel.createNewLog(date, time, "Test Comment", 4, 100.0,
                Time.valueOf("02:00:00"), 5);

        assertNotNull(newLog);
        assertEquals(testTour.getId(), newLog.getTourId());
        assertEquals(date, newLog.getDate());
        assertEquals(time, newLog.getTime());
        assertEquals("Test Comment", newLog.getComment());
        assertEquals(4, newLog.getDifficulty());
        assertEquals(100.0, newLog.getTotalDistance());
        assertEquals(Time.valueOf("02:00:00"), newLog.getTotalTime());
        assertEquals(5, newLog.getRating());

        // Verify log was added to the observable list
        ObservableList<Log> logs = viewModel.getLogs();
        assertTrue(logs.contains(newLog));
    }

    @Test
    void testUpdateSelectedLog() {
        viewModel.setTourModel(testTour);

        // Create and select a log
        Log log = viewModel.createNewLog(Date.valueOf("2024-01-01"), Time.valueOf("10:00:00"),
                "Original Comment", 3, 50.0, Time.valueOf("01:00:00"), 3);
        viewModel.selectedLogProperty().set(log);

        // Update the log
        viewModel.updateSelectedLog(log, "Updated Comment", 5, 100.0,
                Time.valueOf("02:30:00"), 4);

        assertEquals("Updated Comment", log.getComment());
        assertEquals(5, log.getDifficulty());
        assertEquals(100.0, log.getTotalDistance());
        assertEquals(Time.valueOf("02:30:00"), log.getTotalTime());
        assertEquals(4, log.getRating());
    }

    @Test
    void testDeleteSelectedLog() {
        viewModel.setTourModel(testTour);

        // Create a log
        Log log = viewModel.createNewLog(Date.valueOf("2024-01-01"), Time.valueOf("10:00:00"),
                "Test Comment", 3, 50.0, Time.valueOf("01:00:00"), 3);
        viewModel.selectedLogProperty().set(log);

        int initialSize = viewModel.getLogs().size();

        viewModel.deleteSelectedLog();

        assertEquals(initialSize - 1, viewModel.getLogs().size());
        assertFalse(viewModel.getLogs().contains(log));
        assertNull(viewModel.selectedLogProperty().get());
    }

    @Test
    void testValidateLogFields() {
        // Valid input
        assertTrue(viewModel.validateLogFields("Test comment", "3", "100.5", "02:30:00", "4"));

        // Invalid difficulty
        assertFalse(viewModel.validateLogFields("Test comment", "invalid", "100.5", "02:30:00", "4"));

        // Invalid distance
        assertFalse(viewModel.validateLogFields("Test comment", "3", "invalid", "02:30:00", "4"));

        // Invalid time
        assertFalse(viewModel.validateLogFields("Test comment", "3", "100.5", "invalid", "4"));

        // Invalid rating
        assertFalse(viewModel.validateLogFields("Test comment", "3", "100.5", "02:30:00", "invalid"));

        // Empty comment
        assertFalse(viewModel.validateLogFields("", "3", "100.5", "02:30:00", "4"));
    }

    @Test
    void testTransportTypes() {
        ObservableList<String> transportTypes = viewModel.getTransportTypes();

        assertNotNull(transportTypes);
        assertFalse(transportTypes.isEmpty());
        assertTrue(transportTypes.contains("Car"));
        assertTrue(transportTypes.contains("Bicycle"));
        assertTrue(transportTypes.contains("Hiking"));
    }

    @Test
    void testSelectedLogProperty() {
        viewModel.setTourModel(testTour);

        Log log = viewModel.createNewLog(Date.valueOf("2024-01-01"), Time.valueOf("10:00:00"),
                "Test Comment", 3, 50.0, Time.valueOf("01:00:00"), 3);

        assertNull(viewModel.selectedLogProperty().get());

        viewModel.selectedLogProperty().set(log);
        assertEquals(log, viewModel.selectedLogProperty().get());

        viewModel.selectedLogProperty().set(null);
        assertNull(viewModel.selectedLogProperty().get());
    }

    @Test
    void testLogsObservableList() {
        viewModel.setTourModel(testTour);

        ObservableList<Log> logs = viewModel.getLogs();
        assertNotNull(logs);
        assertTrue(logs.isEmpty());

        // Add logs and verify they appear in the observable list
        Log log1 = viewModel.createNewLog(Date.valueOf("2024-01-01"), Time.valueOf("10:00:00"),
                "Log 1", 3, 50.0, Time.valueOf("01:00:00"), 3);
        Log log2 = viewModel.createNewLog(Date.valueOf("2024-01-02"), Time.valueOf("11:00:00"),
                "Log 2", 4, 75.0, Time.valueOf("01:30:00"), 4);

        assertEquals(2, logs.size());
        assertTrue(logs.contains(log1));
        assertTrue(logs.contains(log2));
    }
}