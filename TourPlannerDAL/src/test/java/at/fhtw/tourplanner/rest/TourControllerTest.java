package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.service.OpenRouteService;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.StatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourController.class)
public class TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TourRepository tourRepository;

    @MockBean
    private OpenRouteService openRouteService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Tour testTour;
    private OpenRouteService.RouteInfo mockRouteInfo;

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

        mockRouteInfo = new OpenRouteService.RouteInfo(
                300.0, 3.5, "{\"type\":\"LineString\"}",
                new double[]{16.3738, 48.2082},
                new double[]{13.0550, 47.8095}
        );
    }

    @Test
    void testGetAllTours() throws Exception {
        when(tourRepository.findAll()).thenReturn(Arrays.asList(testTour));

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Tour"))
                .andExpect(jsonPath("$[0].from").value("Vienna"));
    }

    @Test
    void testGetTourById() throws Exception {
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));

        mockMvc.perform(get("/api/tours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tour"))
                .andExpect(jsonPath("$.from").value("Vienna"))
                .andExpect(jsonPath("$.to").value("Salzburg"));
    }

    @Test
    void testGetTourByIdNotFound() throws Exception {
        when(tourRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tours/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTour() throws Exception {
        when(tourRepository.save(any(Tour.class))).thenReturn(testTour);
        when(openRouteService.getRouteInfo(anyString(), anyString(), anyString()))
                .thenReturn(mockRouteInfo);

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tour"));

        verify(openRouteService).getRouteInfo("Vienna", "Salzburg", "Car");
        verify(tourRepository, times(2)).save(any(Tour.class));
    }

    @Test
    void testUpdateTour() throws Exception {
        when(tourRepository.save(any(Tour.class))).thenReturn(testTour);
        when(openRouteService.getRouteInfo(anyString(), anyString(), anyString()))
                .thenReturn(mockRouteInfo);

        mockMvc.perform(put("/api/tours/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tour"));
    }

    @Test
    void testDeleteTour() throws Exception {
        when(tourRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/tours/1"))
                .andExpect(status().isOk());

        verify(tourRepository).deleteById(1);
    }

    @Test
    void testDeleteTourNotFound() throws Exception {
        when(tourRepository.existsById(999)).thenReturn(false);

        mockMvc.perform(delete("/api/tours/999"))
                .andExpect(status().isNotFound());

        // Verify dass deleteById NICHT aufgerufen wurde
        verify(tourRepository, never()).deleteById(999);
    }

    @Test
    void testGetTourReport() throws Exception {
        byte[] pdfData = "PDF content".getBytes();
        when(reportService.generateTourReport(1)).thenReturn(pdfData);

        mockMvc.perform(get("/api/tours/1/report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=tour-report-1.pdf"))
                .andExpect(content().bytes(pdfData));
    }

    @Test
    void testGetSummaryReport() throws Exception {
        byte[] pdfData = "Summary PDF content".getBytes();
        when(reportService.generateSummaryReport()).thenReturn(pdfData);

        mockMvc.perform(get("/api/tours/summary-report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=summary-report.pdf"));
    }

    @Test
    void testGetTourStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("popularity", 5);
        stats.put("childFriendliness", true);
        stats.put("averageDifficulty", 3.5);

        when(statsService.getTourStats(1)).thenReturn(stats);

        mockMvc.perform(get("/api/tours/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popularity").value(5))
                .andExpect(jsonPath("$.childFriendliness").value(true));
    }

    @Test
    void testCalculateRoute() throws Exception {
        TourController.RouteRequest request = new TourController.RouteRequest();
        request.setFromLocation("Vienna");
        request.setToLocation("Salzburg");
        request.setTransportType("Car");

        when(openRouteService.getRouteInfo("Vienna", "Salzburg", "Car"))
                .thenReturn(mockRouteInfo);

        mockMvc.perform(post("/api/tours/calculate-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value(300.0))
                .andExpect(jsonPath("$.estimatedTime").value(3.5));
    }

    @Test
    void testImportTours() throws Exception {
        when(tourRepository.save(any(Tour.class))).thenReturn(testTour);

        mockMvc.perform(post("/api/tours/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(testTour))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Tour"));
    }

    @Test
    void testExportTours() throws Exception {
        when(tourRepository.findAll()).thenReturn(Arrays.asList(testTour));

        mockMvc.perform(get("/api/tours/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Tour"));
    }
}