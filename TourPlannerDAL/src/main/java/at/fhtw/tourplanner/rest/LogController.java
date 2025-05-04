package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.LogRepository;
import at.fhtw.tourplanner.repo.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final LogRepository logRepository;
    private final TourRepository tourRepository;

    @GetMapping("/api/tours/{tourId}/logs")
    public List<Log> getLogsForTour(@PathVariable int tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow();
        return logRepository.findByTour(tour);
    }

    @PostMapping("/api/tours/{tourId}/logs")
    public Log addLog(@PathVariable int tourId, @RequestBody Log log) {
        Tour tour = tourRepository.findById(tourId).orElseThrow();
        log.setTour(tour);
        return logRepository.save(log);
    }

    @DeleteMapping("/api/logs/{id}")
    public void deleteLog(@PathVariable int id) {
        logRepository.deleteById(id);
    }
}
