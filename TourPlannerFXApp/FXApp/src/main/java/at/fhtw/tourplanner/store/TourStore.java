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

        // Füge einige Beispiel-Logs hinzu
        Log log1 = new Log(nextLogId++, 1, new Date(System.currentTimeMillis()),
                new Time(System.currentTimeMillis()), "Das ist eine tolle Tour!", 3, 295.0,
                Time.valueOf("03:30:00"), 5);
        Log log2 = new Log(nextLogId++, 2, new Date(System.currentTimeMillis()),
                new Time(System.currentTimeMillis()), "Bergwanderung war anstrengend aber schön", 4, 12.0,
                Time.valueOf("04:15:00"), 4);

        addLog(1, log1);
        addLog(2, log2);
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
        newTour.setQuickNotes(""); // Neues Feld initialisieren
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

        String lowerSearchText = searchText.toLowerCase();

        // Neue Liste für die Ergebnisse
        List<Tour> results = new ArrayList<>();

        // Füge zuerst alle Tours hinzu, die direkt im Namen, Beschreibung, etc. übereinstimmen
        results.addAll(tours.stream()
                .filter(tour ->
                        tour.getName().toLowerCase().contains(lowerSearchText) ||
                                tour.getTourDescription().toLowerCase().contains(lowerSearchText) ||
                                tour.getFrom().toLowerCase().contains(lowerSearchText) ||
                                tour.getTo().toLowerCase().contains(lowerSearchText) ||
                                tour.getTransportType().toLowerCase().contains(lowerSearchText))
                .collect(Collectors.toList()));

        // Nun suche in den TourLogs und füge die entsprechenden Tours hinzu
        tours.forEach(tour -> {
            List<Log> logs = tourLogs.getOrDefault(tour.getId(), new ArrayList<>());
            boolean hasMatchingLog = logs.stream()
                    .anyMatch(log ->
                            (log.getComment() != null && log.getComment().toLowerCase().contains(lowerSearchText)));

            if (hasMatchingLog && !results.contains(tour)) {
                results.add(tour);
            }
        });

        return results;
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

    public void updateTourNotes(int tourId, String notes) {
        Optional<Tour> tourOpt = getTourById(tourId);
        if (tourOpt.isPresent()) {
            tourOpt.get().setQuickNotes(notes);
        }
    }
}