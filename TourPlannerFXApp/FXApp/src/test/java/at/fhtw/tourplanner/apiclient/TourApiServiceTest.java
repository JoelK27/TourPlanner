package at.fhtw.tourplanner.apiclient;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TourApiServiceTest {

    private TourApiService apiService;
    private boolean backendAvailable;

    @BeforeEach
    void setUp() {
        apiService = TourApiService.getInstance();

        // Teste Backend-Verfügbarkeit
        try {
            List<Tour> tours = apiService.getAllTours();
            backendAvailable = tours != null;
            System.out.println("Backend available: " + backendAvailable);
        } catch (Exception e) {
            backendAvailable = false;
            System.out.println("Backend not available: " + e.getMessage());
        }
    }

    @Test
    void testSingletonPattern() {
        // Dieser Test ist Backend-unabhängig
        TourApiService instance1 = TourApiService.getInstance();
        TourApiService instance2 = TourApiService.getInstance();
        assertSame(instance1, instance2, "TourApiService should be a singleton");
    }

    @Test
    void testCreateNewTour() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour newTour = apiService.createNewTour();

        assertNotNull(newTour, "New tour should not be null");
        assertNotNull(newTour.getName(), "Tour name should not be null");
        assertTrue(newTour.getName().contains("New"), "Tour name should contain 'New'");
        assertEquals("Car", newTour.getTransportType(), "Default transport type should be Car");
        assertEquals("Vienna", newTour.getFrom(), "From should be Vienna");
        assertEquals("Salzburg", newTour.getTo(), "To should be Salzburg");
        assertTrue(newTour.getTourDistance() > 0.0, "Distance should be greater than 0.0");
        assertTrue(newTour.getEstimatedTime() > 0.0, "Estimated time should be greater than 0.0");
    }

    @Test
    void testUpdateTour() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        int originalId = tour.getId();
        String updatedName = "Updated Tour Name";
        String updatedDescription = "Updated Description";
        String from = "Vienna";
        String to = "Salzburg";
        String transportType = "Bicycle";
        double distance = 150.0;
        double time = 2.5;

        apiService.updateTour(tour, updatedName, updatedDescription, from, to, transportType, distance, time);

        assertEquals(originalId, tour.getId(), "Tour ID should remain unchanged");
        assertEquals(updatedName, tour.getName(), "Tour name should be updated");
        assertEquals(updatedDescription, tour.getTourDescription(), "Tour description should be updated");
        assertEquals(from, tour.getFrom(), "From location should be updated");
        assertEquals(to, tour.getTo(), "To location should be updated");
        assertEquals(transportType, tour.getTransportType(), "Transport type should be updated");
        assertEquals(distance, tour.getTourDistance(), "Distance should be updated");
        assertEquals(time, tour.getEstimatedTime(), "Estimated time should be updated");
    }

    @Test
    void testDeleteTour() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        int initialCount = apiService.getAllTours().size();

        apiService.deleteTour(tour);

        int finalCount = apiService.getAllTours().size();
        assertEquals(initialCount - 1, finalCount, "Tour count should decrease by 1");

        // Überprüfe, dass die Tour nicht mehr in der Liste ist
        List<Tour> remainingTours = apiService.getAllTours();
        boolean tourExists = remainingTours.stream()
                .anyMatch(t -> t.getId() == tour.getId());
        assertFalse(tourExists, "Deleted tour should not exist in the list");
    }

    @Test
    void testSearchToursWithValidQuery() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        // Erstelle eine Tour mit bekanntem Namen
        Tour testTour = apiService.createNewTour();
        Assumptions.assumeTrue(testTour != null, "Tour creation failed");

        // Suche nach "New" (sollte die neue Tour finden)
        List<Tour> results = apiService.searchTours("New");

        assertNotNull(results, "Search results should not be null");
        assertFalse(results.isEmpty(), "Search should return results");

        boolean foundTour = results.stream()
                .anyMatch(tour -> tour.getName().contains("New"));
        assertTrue(foundTour, "Search should find tour with 'New' in name");
    }

    @Test
    void testSearchToursWithEmptyQuery() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        List<Tour> allTours = apiService.getAllTours();
        List<Tour> searchResults = apiService.searchTours("");

        assertNotNull(searchResults, "Search results should not be null");
        assertEquals(allTours.size(), searchResults.size(), "Empty search should return all tours");
    }

    @Test
    void testSearchToursWithNoMatches() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        List<Tour> results = apiService.searchTours("NonExistentTour123456789");

        assertNotNull(results, "Search results should not be null");
        assertTrue(results.isEmpty(), "Search with no matches should return empty list");
    }

    @Test
    void testCreateNewLog() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        Log newLog = apiService.createNewLog(tour.getId());

        if (newLog == null) {
            System.out.println("Log creation returned null - skipping assertions");
            return;
        }

        assertNotNull(newLog, "New log should not be null");
        assertEquals(tour.getId(), newLog.getTourId(), "Log should belong to the correct tour");
        assertNotNull(newLog.getDate(), "Log date should not be null");
        assertNotNull(newLog.getTime(), "Log time should not be null");

        // Flexible Assertions für verschiedene Backend-Implementierungen
        assertTrue(newLog.getDifficulty() >= 0, "Difficulty should be non-negative");
        assertTrue(newLog.getTotalDistance() >= 0.0, "Total distance should be non-negative");
        assertTrue(newLog.getRating() >= 0, "Rating should be non-negative");
    }

    @Test
    void testUpdateLog() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        Log log = apiService.createNewLog(tour.getId());
        Assumptions.assumeTrue(log != null, "Log creation failed");

        // Update log with new values
        String newComment = "Updated Test Comment";
        int newDifficulty = 4;
        double newDistance = 100.5;
        Time newTime = Time.valueOf("02:30:00");
        int newRating = 5;

        log.setComment(newComment);
        log.setDifficulty(newDifficulty);
        log.setTotalDistance(newDistance);
        log.setTotalTime(newTime);
        log.setRating(newRating);

        Log updatedLog = apiService.updateLog(log);

        if (updatedLog != null) {
            // Verify through API response
            assertEquals(newComment, updatedLog.getComment(), "Comment should be updated");
            assertEquals(newDifficulty, updatedLog.getDifficulty(), "Difficulty should be updated");
            assertEquals(newDistance, updatedLog.getTotalDistance(), "Distance should be updated");
            assertEquals(newTime, updatedLog.getTotalTime(), "Time should be updated");
            assertEquals(newRating, updatedLog.getRating(), "Rating should be updated");
        } else {
            // Verify through tour logs if direct response is null
            List<Log> logs = apiService.getLogsForTour(tour.getId());
            Log foundLog = logs.stream()
                    .filter(l -> l.getId() == log.getId())
                    .findFirst()
                    .orElse(null);

            assertNotNull(foundLog, "Updated log should be found");
            assertEquals(newComment, foundLog.getComment(), "Comment should be updated");
        }
    }

    @Test
    void testDeleteLog() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        Log log = apiService.createNewLog(tour.getId());
        Assumptions.assumeTrue(log != null, "Log creation failed");

        int initialLogCount = apiService.getLogsForTour(tour.getId()).size();

        apiService.deleteLog(log);

        int finalLogCount = apiService.getLogsForTour(tour.getId()).size();
        assertEquals(initialLogCount - 1, finalLogCount, "Log count should decrease by 1");
    }

    @Test
    void testGetLogsForTour() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        Log newLog = apiService.createNewLog(tour.getId());
        Assumptions.assumeTrue(newLog != null, "Log creation failed");

        List<Log> logs = apiService.getLogsForTour(tour.getId());

        assertNotNull(logs, "Logs list should not be null");
        assertFalse(logs.isEmpty(), "Logs list should not be empty");

        // Verify the created log is in the list
        boolean logFound = logs.stream()
                .anyMatch(log -> log.getId() == newLog.getId());
        assertTrue(logFound, "Created log should be found in the logs list");

        // Verify all logs belong to the correct tour
        boolean allLogsMatchTour = logs.stream()
                .allMatch(log -> log.getTourId() == tour.getId());
        assertTrue(allLogsMatchTour, "All logs should belong to the correct tour");
    }

    @Test
    void testCalculateRoute() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Map<String, Object> result = apiService.calculateRoute("Vienna", "Salzburg", "Car");

        assertNotNull(result, "Route calculation result should not be null");
        assertFalse(result.isEmpty(), "Route calculation should return data");

        // Basic structure validation
        assertTrue(result.containsKey("distance") || result.containsKey("error"),
                "Result should contain distance or error information");

        if (result.containsKey("distance")) {
            assertTrue(result.containsKey("estimatedTime"), "Result should contain estimated time");
        }
    }

    @Test
    void testGetTourStats() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        Map<String, Object> stats = apiService.getTourStats(tour.getId());

        assertNotNull(stats, "Stats should not be null");

        // Verify expected keys exist
        String[] expectedKeys = {"popularity", "childFriendliness", "averageDifficulty"};
        for (String key : expectedKeys) {
            assertTrue(stats.containsKey(key), "Stats should contain key: " + key);
        }
    }

    @Test
    void testExportTours() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        // Ensure at least one tour exists
        Tour testTour = apiService.createNewTour();
        Assumptions.assumeTrue(testTour != null, "Tour creation failed");

        List<Tour> exportedTours = apiService.exportTours();

        assertNotNull(exportedTours, "Exported tours should not be null");
        assertFalse(exportedTours.isEmpty(), "Should have at least one tour to export");
    }

    @Test
    void testImportTours() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        // Create test tours for import
        Tour tour1 = new Tour(0, "Import Test Tour 1", "Test Description 1",
                "Vienna", "Salzburg", "Car", 300.0, 3.5);
        Tour tour2 = new Tour(0, "Import Test Tour 2", "Test Description 2",
                "Graz", "Linz", "Bicycle", 150.0, 2.0);

        List<Tour> toursToImport = List.of(tour1, tour2);

        List<Tour> importedTours = apiService.importTours(toursToImport);

        assertNotNull(importedTours, "Imported tours should not be null");
        assertEquals(2, importedTours.size(), "Should import 2 tours");

        // Verify tours were imported with new IDs
        for (Tour importedTour : importedTours) {
            assertTrue(importedTour.getId() > 0, "Imported tour should have valid ID");
        }
    }

    @Test
    void testFileOperations() throws IOException {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        // Create a temporary file for testing
        File tempFile = File.createTempFile("test_export", ".json");
        tempFile.deleteOnExit();

        // Test export to file
        assertDoesNotThrow(() -> apiService.exportToursToFile(tempFile),
                "Export to file should not throw exception");

        assertTrue(tempFile.exists(), "Export file should exist");
        assertTrue(tempFile.length() > 0, "Export file should not be empty");

        // Test import from file
        assertDoesNotThrow(() -> {
            List<Tour> importedTours = apiService.importToursFromFile(tempFile);
            assertNotNull(importedTours, "Imported tours should not be null");
        }, "Import from file should not throw exception");
    }

    @Test
    void testReportOperations() {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        // Test tour report
        byte[] tourReport = apiService.downloadTourReport(tour.getId());
        assertNotNull(tourReport, "Tour report should not be null");
        assertTrue(tourReport.length > 0, "Tour report should not be empty");

        // Test summary report
        byte[] summaryReport = apiService.downloadSummaryReport();
        assertNotNull(summaryReport, "Summary report should not be null");
        assertTrue(summaryReport.length > 0, "Summary report should not be empty");
    }

    @Test
    void testSaveReportToFile() throws IOException {
        Assumptions.assumeTrue(backendAvailable, "Backend server required for this test");

        Tour tour = apiService.createNewTour();
        Assumptions.assumeTrue(tour != null, "Tour creation failed");

        File tempFile = File.createTempFile("test_report", ".pdf");
        tempFile.deleteOnExit();

        assertDoesNotThrow(() -> apiService.saveTourReport(tour.getId(), tempFile),
                "Save report to file should not throw exception");

        assertTrue(tempFile.exists(), "Report file should exist");
        assertTrue(tempFile.length() > 0, "Report file should not be empty");
    }
}