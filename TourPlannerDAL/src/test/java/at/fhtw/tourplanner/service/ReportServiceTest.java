package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.LogRepository;
import at.fhtw.tourplanner.repo.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ReportService reportService;

    private Tour testTour;
    private Log testLog;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setId(1);
        testTour.setName("Test Tour");
        testTour.setTourDescription("Test Description");
        testTour.setFromLocation("Vienna");
        testTour.setToLocation("Salzburg");
        testTour.setTransportType("Car");
        testTour.setTourDistance(300.0);
        testTour.setEstimatedTime(3.5);
        testTour.setEncodedRouteGeometry("{\"type\":\"LineString\"}");
        testTour.setStartCoords("[16.3738, 48.2082]");
        testTour.setEndCoords("[13.0550, 47.8095]");

        testLog = new Log();
        testLog.setId(1);
        testLog.setDate(Date.valueOf("2024-01-01"));
        testLog.setTime(Time.valueOf("10:00:00"));
        testLog.setComment("Test comment");
        testLog.setDifficulty(3);
        testLog.setTotalDistance(295.0);
        testLog.setTotalTime(Time.valueOf("03:30:00"));
        testLog.setRating(4);
        testLog.setTour(testTour);
    }

    @Test
    void testGenerateTourReportSuccess() {
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.findByTour(testTour)).thenReturn(Arrays.asList(testLog));
        when(imageService.fetchTourMapImage(anyString(), any(double[].class), any(double[].class)))
                .thenReturn("mock-image".getBytes());

        byte[] result = reportService.generateTourReport(1);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(tourRepository).findById(1);
        verify(logRepository).findByTour(testTour);
        verify(imageService).fetchTourMapImage(anyString(), any(double[].class), any(double[].class));
    }

    @Test
    void testGenerateTourReportTourNotFound() {
        when(tourRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            reportService.generateTourReport(999);
        });
    }

    @Test
    void testGenerateTourReportWithoutImage() {
        testTour.setEncodedRouteGeometry(null);
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.findByTour(testTour)).thenReturn(Arrays.asList(testLog));

        byte[] result = reportService.generateTourReport(1);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(imageService, never()).fetchTourMapImage(anyString(), any(double[].class), any(double[].class));
    }

    @Test
    void testGenerateSummaryReport() {
        when(tourRepository.findAll()).thenReturn(Arrays.asList(testTour));
        when(logRepository.findByTour(testTour)).thenReturn(Arrays.asList(testLog));

        byte[] result = reportService.generateSummaryReport();

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(tourRepository).findAll();
        verify(logRepository).findByTour(testTour);
    }

    @Test
    void testGenerateSummaryReportNoTours() {
        when(tourRepository.findAll()).thenReturn(Arrays.asList());

        byte[] result = reportService.generateSummaryReport();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGenerateTourReportMultipleLogs() {
        Log secondLog = new Log();
        secondLog.setId(2);
        secondLog.setDate(Date.valueOf("2024-01-02"));
        secondLog.setTime(Time.valueOf("14:00:00"));
        secondLog.setComment("Second test comment");
        secondLog.setDifficulty(4);
        secondLog.setTotalDistance(300.0);
        secondLog.setTotalTime(Time.valueOf("04:00:00"));
        secondLog.setRating(5);
        secondLog.setTour(testTour);

        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.findByTour(testTour)).thenReturn(Arrays.asList(testLog, secondLog));

        byte[] result = reportService.generateTourReport(1);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}