package at.fhtw.tourplanner.apiclient;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TourApiService {
    @Getter
    private static final TourApiService instance = new TourApiService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BASE_URL = "http://localhost:8080";

    private TourApiService() {
    }

    // ====== TOUR OPERATIONS ======

    public List<Tour> getAllTours() {
        try {
            String response = executeGet(BASE_URL + "/tours");
            return objectMapper.readValue(response, new TypeReference<List<Tour>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching tours: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<Tour> getTourById(int id) {
        try {
            String response = executeGet(BASE_URL + "/tours/" + id);
            Tour tour = objectMapper.readValue(response, Tour.class);
            return Optional.of(tour);
        } catch (Exception e) {
            System.err.println("Error fetching tour with ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public Tour addTour(Tour tour) {
        try {
            String jsonTour = objectMapper.writeValueAsString(tour);
            String response = executePost(BASE_URL + "/tours", jsonTour);
            return objectMapper.readValue(response, Tour.class);
        } catch (Exception e) {
            System.err.println("Error adding tour: " + e.getMessage());
            return null;
        }
    }

    public Tour createNewTour() {
        Tour newTour = new Tour(0, "New Tour", "", "", "", "Car", 0.0, 0.0);
        return addTour(newTour);
    }

    public void updateTour(Tour tour, String name, String description, String from,
                           String to, String transportType, double distance, double time) {
        try {
            // Update local object first
            tour.setName(name);
            tour.setTourDescription(description);
            tour.setFrom(from);
            tour.setTo(to);
            tour.setTransportType(transportType);
            tour.setTourDistance(distance);
            tour.setEstimatedTime(time);

            // Then send to API
            String jsonTour = objectMapper.writeValueAsString(tour);
            executePut(BASE_URL + "/tours/" + tour.getId(), jsonTour);
        } catch (Exception e) {
            System.err.println("Error updating tour: " + e.getMessage());
        }
    }

    public void deleteTour(Tour tour) {
        try {
            executeDelete(BASE_URL + "/tours/" + tour.getId());
        } catch (Exception e) {
            System.err.println("Error deleting tour: " + e.getMessage());
        }
    }

    public List<Tour> searchTours(String searchText) {
        try {
            String response = executeGet(BASE_URL + "/tours/search?query=" + searchText);
            return objectMapper.readValue(response, new TypeReference<List<Tour>>() {});
        } catch (Exception e) {
            System.err.println("Error searching tours: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ====== LOG OPERATIONS ======

    public List<Log> getLogsForTour(int tourId) {
        try {
            String response = executeGet(BASE_URL + "/tours/" + tourId + "/logs");
            return objectMapper.readValue(response, new TypeReference<List<Log>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching logs for tour " + tourId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Log addLog(int tourId, Log log) {
        try {
            log.setTourId(tourId);
            String jsonLog = objectMapper.writeValueAsString(log);
            String response = executePost(BASE_URL + "/tours/" + tourId + "/logs", jsonLog);
            return objectMapper.readValue(response, Log.class);
        } catch (Exception e) {
            System.err.println("Error adding log: " + e.getMessage());
            return null;
        }
    }

    public Log createNewLog(int tourId) {
        Log log = new Log();
        log.setTourId(tourId);
        log.setDate(new Date(System.currentTimeMillis()));
        log.setTime(new Time(System.currentTimeMillis()));
        return addLog(tourId, log);
    }

    public void updateLog(Log log) {
        try {
            String jsonLog = objectMapper.writeValueAsString(log);
            executePut(BASE_URL + "/logs/" + log.getId(), jsonLog);
        } catch (Exception e) {
            System.err.println("Error updating log: " + e.getMessage());
        }
    }

    public void deleteLog(Log log) {
        try {
            executeDelete(BASE_URL + "/logs/" + log.getId());
        } catch (Exception e) {
            System.err.println("Error deleting log: " + e.getMessage());
        }
    }

    // ====== HTTP UTILITY METHODS ======

    private String executeGet(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                handleHttpError(response);
                return getResponseContent(response);
            }
        }
    }

    private String executePost(String url, String jsonBody) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setEntity(new StringEntity(jsonBody));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                handleHttpError(response);
                return getResponseContent(response);
            }
        }
    }

    private String executePut(String url, String jsonBody) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setEntity(new StringEntity(jsonBody));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                handleHttpError(response);
                return getResponseContent(response);
            }
        }
    }

    private String executeDelete(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                handleHttpError(response);
                return getResponseContent(response);
            }
        }
    }

    private String getResponseContent(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return EntityUtils.toString(entity);
        }
        return null;
    }

    private void handleHttpError(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400) {
            String errorBody = EntityUtils.toString(response.getEntity());
            throw new IOException("API error: " + statusCode + " - " + errorBody);
        }
    }
}
