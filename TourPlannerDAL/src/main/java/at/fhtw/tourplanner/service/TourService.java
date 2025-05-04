package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.model.Tour;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class TourService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8080/api/tours";

    public List<Tour> getAllTours() {
        return Arrays.asList(restTemplate.getForObject(baseUrl, Tour[].class));
    }

    public void addTour(Tour tour) {
        restTemplate.postForObject(baseUrl, tour, Tour.class);
    }
}