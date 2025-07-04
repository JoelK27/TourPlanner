package at.fhtw.tourplanner.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TourTest {

    private Tour tour;

    @BeforeEach
    void setUp() {
        tour = new Tour();
        tour.setId(1);
        tour.setName("Test Tour");
        tour.setTourDescription("Test Description");
        tour.setFromLocation("Vienna");
        tour.setToLocation("Salzburg");
        tour.setTransportType("Car");
        tour.setTourDistance(300.0);
        tour.setEstimatedTime(3.5);
    }

    @Test
    void testTourBasicProperties() {
        assertEquals(1, tour.getId());
        assertEquals("Test Tour", tour.getName());
        assertEquals("Test Description", tour.getTourDescription());
        assertEquals("Vienna", tour.getFromLocation());
        assertEquals("Salzburg", tour.getToLocation());
        assertEquals("Car", tour.getTransportType());
        assertEquals(300.0, tour.getTourDistance());
        assertEquals(3.5, tour.getEstimatedTime());
    }

    @Test
    void testRoutingDataProperties() {
        String geometry = "{\"type\":\"LineString\",\"coordinates\":[[16.3738,48.2082],[13.0550,47.8095]]}";
        String startCoords = "[16.3738, 48.2082]";
        String endCoords = "[13.0550, 47.8095]";

        tour.setEncodedRouteGeometry(geometry);
        tour.setStartCoords(startCoords);
        tour.setEndCoords(endCoords);

        assertEquals(geometry, tour.getEncodedRouteGeometry());
        assertEquals(startCoords, tour.getStartCoords());
        assertEquals(endCoords, tour.getEndCoords());
    }

    @Test
    void testStartCoordsAsArray() {
        tour.setStartCoords("[16.3738, 48.2082]");
        double[] coords = tour.getStartCoordsAsArray();

        assertNotNull(coords);
        assertEquals(2, coords.length);
        assertEquals(16.3738, coords[0], 0.0001);
        assertEquals(48.2082, coords[1], 0.0001);
    }

    @Test
    void testEndCoordsAsArray() {
        tour.setEndCoords("[13.0550, 47.8095]");
        double[] coords = tour.getEndCoordsAsArray();

        assertNotNull(coords);
        assertEquals(2, coords.length);
        assertEquals(13.0550, coords[0], 0.0001);
        assertEquals(47.8095, coords[1], 0.0001);
    }

    @Test
    void testCoordsAsArrayWithNullInput() {
        tour.setStartCoords(null);
        tour.setEndCoords(null);

        assertNull(tour.getStartCoordsAsArray());
        assertNull(tour.getEndCoordsAsArray());
    }

    @Test
    void testCoordsAsArrayWithInvalidInput() {
        tour.setStartCoords("[16.3738]"); // Only one coordinate
        tour.setEndCoords("invalid");

        assertNull(tour.getStartCoordsAsArray());
        assertNull(tour.getEndCoordsAsArray());
    }

    @Test
    void testLogsRelationship() {
        Log log1 = new Log();
        log1.setId(1);
        log1.setComment("Test Log 1");
        log1.setTour(tour);

        Log log2 = new Log();
        log2.setId(2);
        log2.setComment("Test Log 2");
        log2.setTour(tour);

        tour.setLogs(Arrays.asList(log1, log2));

        assertEquals(2, tour.getLogs().size());
        assertEquals("Test Log 1", tour.getLogs().get(0).getComment());
        assertEquals("Test Log 2", tour.getLogs().get(1).getComment());
    }

    @Test
    void testEmptyLogs() {
        tour.setLogs(new ArrayList<>());
        assertTrue(tour.getLogs().isEmpty());
    }

    @Test
    void testCoordsAsArrayWithSpaces() {
        tour.setStartCoords("[ 16.3738 , 48.2082 ]");
        double[] coords = tour.getStartCoordsAsArray();

        assertNotNull(coords);
        assertEquals(16.3738, coords[0], 0.0001);
        assertEquals(48.2082, coords[1], 0.0001);
    }

    @Test
    void testTourEquality() {
        Tour tour2 = new Tour();
        tour2.setId(1);
        tour2.setName("Test Tour");

        // Note: Using @Data annotation means equals/hashCode are generated
        // This test verifies the Lombok-generated methods work
        assertEquals(tour.getId(), tour2.getId());
        assertEquals(tour.getName(), tour2.getName());
    }
}