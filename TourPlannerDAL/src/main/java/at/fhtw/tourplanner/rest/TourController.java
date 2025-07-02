package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.service.ImageService;
import at.fhtw.tourplanner.service.ReportService;
import at.fhtw.tourplanner.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourRepository tourRepository;
    private final ReportService reportService;
    private final ImageService imageService;
    private final StatsService statsService;

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
            Tour newTour = new Tour();
            newTour.setName(tour.getName());
            newTour.setTourDescription(tour.getTourDescription());
            newTour.setFromLocation(tour.getFromLocation());
            newTour.setToLocation(tour.getToLocation());
            newTour.setTransportType(tour.getTransportType());
            newTour.setTourDistance(tour.getTourDistance());
            newTour.setEstimatedTime(tour.getEstimatedTime());
            savedTours.add(tourRepository.save(newTour));
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
        return ResponseEntity.ok().body(image);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> getTourStats(@PathVariable int id) {
        return statsService.getTourStats(id);
    }
}
