package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.LogRepository;
import at.fhtw.tourplanner.repo.TourRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogRepository logRepository;

    @MockBean
    private TourRepository tourRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Tour testTour;
    private Log testLog;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setId(1);
        testTour.setName("Test Tour");

        testLog = new Log();
        testLog.setId(10);
        testLog.setDate(Date.valueOf("2024-01-01"));
        testLog.setTime(Time.valueOf("10:00:00"));
        testLog.setComment("Test Log");
        testLog.setDifficulty(3);
        testLog.setTotalDistance(100.0);
        testLog.setTotalTime(Time.valueOf("01:00:00"));
        testLog.setRating(4);
        testLog.setTour(testTour);
    }

    @Test
    void testGetLogsForTour() throws Exception {
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.findByTour(testTour)).thenReturn(Arrays.asList(testLog));

        mockMvc.perform(get("/api/tours/1/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testLog.getId()))
                .andExpect(jsonPath("$[0].comment").value("Test Log"));
    }

    @Test
    void testGetLogsForTour_NotFound() throws Exception {
        when(tourRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tours/99/logs"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLog() throws Exception {
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        Log newLog = new Log();
        newLog.setComment("New Log");
        newLog.setDifficulty(2);
        newLog.setTotalDistance(50.0);
        newLog.setTotalTime(Time.valueOf("00:30:00"));
        newLog.setRating(3);

        mockMvc.perform(post("/api/tours/1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Test Log"));
    }

    @Test
    void testDeleteLog() throws Exception {
        doNothing().when(logRepository).deleteById(10);

        mockMvc.perform(delete("/api/logs/10"))
                .andExpect(status().isOk());

        verify(logRepository).deleteById(10);
    }

    @Test
    void testUpdateLog() throws Exception {
        when(logRepository.findById(10)).thenReturn(Optional.of(testLog));
        when(tourRepository.findById(1)).thenReturn(Optional.of(testTour));
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        Log updatedLog = new Log();
        updatedLog.setDate(Date.valueOf("2024-02-02"));
        updatedLog.setTime(Time.valueOf("12:00:00"));
        updatedLog.setComment("Updated");
        updatedLog.setDifficulty(4);
        updatedLog.setTotalDistance(200.0);
        updatedLog.setTotalTime(Time.valueOf("02:00:00"));
        updatedLog.setRating(5);
        updatedLog.setTourId(1);

        mockMvc.perform(put("/api/logs/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Updated"));
    }

    @Test
    void testUpdateLog_NotFound() throws Exception {
        when(logRepository.findById(99)).thenReturn(Optional.empty());

        Log updatedLog = new Log();
        updatedLog.setComment("Updated");

        mockMvc.perform(put("/api/logs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLog)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchLogs() throws Exception {
        when(logRepository.searchLogs("Test")).thenReturn(Collections.singletonList(testLog));

        mockMvc.perform(get("/api/logs/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Test Log"));
    }
}