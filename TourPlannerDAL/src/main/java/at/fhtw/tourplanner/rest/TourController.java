package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.service.OpenRouteService;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourRepository tourRepository;
    private final ReportService reportService;
    private final StatsService statsService;
    private final OpenRouteService openRouteService;
    private static final Logger logger = LogManager.getLogger(TourController.class);

    @GetMapping
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @GetMapping("/{id}")
    public Tour getTourById(@PathVariable int id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));
    }

    @PostMapping
    public Tour createTour(@RequestBody Tour tour) {
        logger.info("Creating new tour: {}", tour.getName());

        // Tour zuerst speichern
        Tour savedTour = tourRepository.save(tour);
        logger.debug("Tour saved with ID: {}", savedTour.getId());

        // Routingdaten berechnen und setzen
        try {
            OpenRouteService.RouteInfo routeInfo = openRouteService.getRouteInfo(
                    savedTour.getFromLocation(),
                    savedTour.getToLocation(),
                    savedTour.getTransportType()
            );
            savedTour.setEncodedRouteGeometry(routeInfo.routeGeometry);
            savedTour.setStartCoords(Arrays.toString(routeInfo.startCoords));
            savedTour.setEndCoords(Arrays.toString(routeInfo.endCoords));
            savedTour.setTourDistance(routeInfo.distance);
            savedTour.setEstimatedTime(routeInfo.duration);
            savedTour = tourRepository.save(savedTour);
            logger.info("Tour created successfully with routing data: {}", savedTour.getId());
        } catch (Exception e) {
            logger.error("Route calculation failed for new tour {}: {}", savedTour.getId(), e.getMessage(), e);
        }

        return savedTour;
    }

    @PutMapping("/{id}")
    public Tour updateTour(@PathVariable int id, @RequestBody Tour tour) {
        logger.info("Updating tour with ID: {}", id);

        tour.setId(id);
        Tour savedTour = tourRepository.save(tour);

        // Routingdaten neu berechnen
        try {
            OpenRouteService.RouteInfo routeInfo = openRouteService.getRouteInfo(
                    savedTour.getFromLocation(),
                    savedTour.getToLocation(),
                    savedTour.getTransportType()
            );
            savedTour.setEncodedRouteGeometry(routeInfo.routeGeometry);
            savedTour.setStartCoords(Arrays.toString(routeInfo.startCoords));
            savedTour.setEndCoords(Arrays.toString(routeInfo.endCoords));
            savedTour.setTourDistance(routeInfo.distance);
            savedTour.setEstimatedTime(routeInfo.duration);
            savedTour = tourRepository.save(savedTour);
            logger.info("Tour updated successfully with routing data: {}", savedTour.getId());
        } catch (Exception e) {
            logger.error("Route calculation failed for updated tour {}: {}", savedTour.getId(), e.getMessage(), e);
        }

        return savedTour;
    }

    @DeleteMapping("/{id}")
    public void deleteTour(@PathVariable int id) {
        logger.info("Deleting tour with ID: {}", id);

        if (!tourRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent tour with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found");
        }

        tourRepository.deleteById(id);
        logger.info("Tour with ID {} deleted successfully", id);
    }

    @GetMapping("/search")
    public List<Tour> searchTours(@RequestParam String query) {
        return tourRepository.searchTours(query);
    }

    @PostMapping("/import")
    @Transactional
    public List<Tour> importTours(@RequestBody List<Tour> tours) {
        List<Tour> savedTours = new ArrayList<>();
        for (Tour tour : tours) {
            // Setze Tour-ID auf 0, damit immer eine neue Tour angelegt wird
            tour.setId(0);

            // Logs neu aufbauen und IDs auf 0 setzen
            List<Log> newLogs = new ArrayList<>();
            if (tour.getLogs() != null) {
                for (Log log : tour.getLogs()) {
                    Log newLog = new Log();
                    newLog.setId(0);
                    newLog.setDate(log.getDate());
                    newLog.setTime(log.getTime());
                    newLog.setComment(log.getComment());
                    newLog.setDifficulty(log.getDifficulty());
                    newLog.setTotalDistance(log.getTotalDistance());
                    newLog.setTotalTime(log.getTotalTime());
                    newLog.setRating(log.getRating());
                    newLog.setTour(tour); // Beziehung setzen
                    newLogs.add(newLog);
                }
            }
            tour.setLogs(newLogs);

            savedTours.add(tourRepository.save(tour));
        }
        return savedTours;
    }

    @GetMapping("/export")
    public List<Tour> exportTours() {
        return tourRepository.findAll();
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getTourReport(@PathVariable int id) {
        // Report-Generierung als PDF/Byte-Array
        byte[] pdf = reportService.generateTourReport(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=tour-report-" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/summary-report")
    public ResponseEntity<byte[]> getSummaryReport() {
        byte[] pdf = reportService.generateSummaryReport();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=summary-report.pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> getTourStats(@PathVariable int id) {
        return statsService.getTourStats(id);
    }

    @PostMapping("/calculate-route")
    public Map<String, Object> calculateRoute(@RequestBody RouteRequest request) {
        try {
            OpenRouteService.RouteInfo routeInfo = openRouteService.getRouteInfo(
                    request.getFromLocation(),
                    request.getToLocation(),
                    request.getTransportType()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("distance", routeInfo.distance);
            result.put("estimatedTime", routeInfo.duration);
            result.put("routeGeometry", routeInfo.routeGeometry);
            result.put("startCoords", routeInfo.startCoords);
            result.put("endCoords", routeInfo.endCoords);

            return result;
        } catch (Exception e) {
            System.err.println("Route calculation failed: " + e.getMessage());

            // Fallback: Schätze Distanz und Zeit
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("distance", 100.0); // Default distance
            fallback.put("estimatedTime", 2.0); // Default time
            fallback.put("routeGeometry", "{}"); // Empty geometry
            fallback.put("startCoords", new double[]{-87.6298, 41.8781}); // Chicago coords
            fallback.put("endCoords", new double[]{-118.2437, 34.0522}); // LA coords

            return fallback;
        }
    }

    public static class RouteRequest {
        private String fromLocation;
        private String toLocation;
        private String transportType;

        // Getters and setters
        public String getFromLocation() { return fromLocation; }
        public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
        public String getToLocation() { return toLocation; }
        public void setToLocation(String toLocation) { this.toLocation = toLocation; }
        public String getTransportType() { return transportType; }
        public void setTransportType(String transportType) { this.transportType = transportType; }
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<Map<String, String>> updateTourNotes(@PathVariable int id, @RequestBody Map<String, String> notesData) {
        logger.info("Updating notes for tour ID: {}", id);
        
        Optional<Tour> tourOpt = tourRepository.findById(id);
        if (tourOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Tour tour = tourOpt.get();
        String notes = notesData.get("notes");
        tour.setQuickNotes(notes);
        tourRepository.save(tour);
        
        Map<String, String> response = new HashMap<>();
        response.put("notes", notes);
        response.put("message", "Notes updated successfully");
        
        logger.info("Notes updated for tour: {}", tour.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<Map<String, String>> getTourNotes(@PathVariable int id) {
        logger.info("Fetching notes for tour ID: {}", id);
        
        Optional<Tour> tourOpt = tourRepository.findById(id);
        if (tourOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Tour tour = tourOpt.get();
        Map<String, String> response = new HashMap<>();
        response.put("notes", tour.getQuickNotes() != null ? tour.getQuickNotes() : "");
        response.put("tourName", tour.getName());
        
        return ResponseEntity.ok(response);
    }
}
