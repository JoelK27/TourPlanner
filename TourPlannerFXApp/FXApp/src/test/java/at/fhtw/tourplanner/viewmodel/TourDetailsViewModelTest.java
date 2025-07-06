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
}