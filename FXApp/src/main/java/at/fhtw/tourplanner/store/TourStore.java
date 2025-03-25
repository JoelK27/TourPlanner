package at.fhtw.tourplanner.store;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TourStore {
    private static final TourStore instance = new TourStore();
    private final List<Tour> tours = new ArrayList<>();
    private final Map<Integer, List<Log>> tourLogs = new HashMap<>();
    private int nextTourId = 1;
    private int nextLogId = 1;

    private TourStore() {
        // Initialize with sample data
        addTour(new Tour(nextTourId++, "Vienna to Salzburg", "Scenic route through the Austrian countryside",
                "Vienna", "Salzburg", "Car", 295.0, 3.5));
        addTour(new Tour(nextTourId++, "Innsbruck Mountain Tour", "Alpine tour with beautiful mountain views",
                "Innsbruck", "Nordkette", "Hiking", 12.0, 4.0));
        addTour(new Tour(nextTourId++, "Danube Bike Path", "Cycling along the Danube River",
                "Vienna", "Krems", "Bicycle", 80.0, 5.0));
    }

    public static TourStore getInstance() {
        return instance;
    }

    public List<Tour> getAllTours() {
        return new ArrayList<>(tours);
    }

    public Optional<Tour> getTourById(int id) {
        return tours.stream()
                .filter(tour -> tour.getId() == id)
                .findFirst();
    }

    public Tour addTour(Tour tour) {
        if (tour.getId() <= 0) {
            tour.setId(nextTourId++);
        }
        tours.add(tour);
        return tour;
    }

    public Tour createNewTour() {
        Tour newTour = new Tour(nextTourId++, "New Tour", "",
                "", "", "Car", 0.0, 0.0);
        tours.add(newTour);
        return newTour;
    }

    public void updateTour(Tour tour, String name, String description, String from,
                           String to, String transportType, double distance, double time) {
        tour.setName(name);
        tour.setTourDescription(description);
        tour.setFrom(from);
        tour.setTo(to);
        tour.setTransportType(transportType);
        tour.setTourDistance(distance);
        tour.setEstimatedTime(time);
    }

    public void deleteTour(Tour tour) {
        tours.remove(tour);
        tourLogs.remove(tour.getId());
    }

    public List<Tour> searchTours(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return new ArrayList<>(tours);
        }

        return tours.stream()
                .filter(tour -> tour.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        tour.getTourDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                        tour.getFrom().toLowerCase().contains(searchText.toLowerCase()) ||
                        tour.getTo().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Log management methods

    public List<Log> getLogsForTour(int tourId) {
        return tourLogs.getOrDefault(tourId, new ArrayList<>());
    }

    public Log addLog(int tourId, Log log) {
        if (log.getId() <= 0) {
            log.setId(nextLogId++);
        }
        log.setTourId(tourId);

        List<Log> logs = tourLogs.computeIfAbsent(tourId, k -> new ArrayList<>());
        logs.add(log);
        return log;
    }

    public Log createNewLog(int tourId) {
        Log log = new Log();
        log.setId(nextLogId++);
        log.setTourId(tourId);
        log.setDate(new Date(System.currentTimeMillis()));
        log.setTime(new Time(System.currentTimeMillis()));

        List<Log> logs = tourLogs.computeIfAbsent(tourId, k -> new ArrayList<>());
        logs.add(log);
        return log;
    }

    public void updateLog(Log log) {
        // The log is already in the collection, no need to update the collection itself
        // Just update the object properties directly
    }

    public void deleteLog(Log log) {
        List<Log> logs = tourLogs.get(log.getTourId());
        if (logs != null) {
            logs.remove(log);
        }
    }
}