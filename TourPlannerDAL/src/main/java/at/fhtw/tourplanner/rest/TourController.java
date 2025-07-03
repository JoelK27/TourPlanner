package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.service.ImageService;
import at.fhtw.tourplanner.service.OpenRouteService;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourRepository tourRepository;
    private final ReportService reportService;
    private final ImageService imageService;
    private final StatsService statsService;
    private final OpenRouteService openRouteService;

    @GetMapping
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Tour> getTourById(@PathVariable int id) {
        return tourRepository.findById(id);
    }

    @PostMapping
    public Tour createTour(@RequestBody Tour tour) {
        return tourRepository.save(tour);
    }

    @PutMapping("/{id}")
    public Tour updateTour(@PathVariable int id, @RequestBody Tour tour) {
        tour.setId(id);
        return tourRepository.save(tour);
    }

    @DeleteMapping("/{id}")
    public void deleteTour(@PathVariable int id) {
        tourRepository.deleteById(id);
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

    @PostMapping("/{id}/image")
    public ResponseEntity<Void> uploadTourImage(@PathVariable int id, @RequestBody byte[] imageBytes) {
        imageService.saveTourImage(id, imageBytes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getTourImage(@PathVariable int id) {
        Resource image = imageService.loadTourImage(id);
        if (image == null || !image.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(image);
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
}
