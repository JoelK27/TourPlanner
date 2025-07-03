package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.LogRepository;
import at.fhtw.tourplanner.repo.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PutMapping("/logs/{id}")
    public Log updateLog(@PathVariable int id, @RequestBody Log log) {
        Log existingLog = logRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log not found"));
        // Felder aktualisieren
        existingLog.setDate(log.getDate());
        existingLog.setTime(log.getTime());
        existingLog.setComment(log.getComment());
        existingLog.setDifficulty(log.getDifficulty());
        existingLog.setTotalDistance(log.getTotalDistance());
        existingLog.setTotalTime(log.getTotalTime());
        existingLog.setRating(log.getRating());
        // Tour-Zuordnung ggf. aktualisieren
        if (log.getTourId() != 0) {
            Tour tour = tourRepository.findById(log.getTourId()).orElseThrow();
            existingLog.setTour(tour);
        }
        return logRepository.save(existingLog);
    }

    @GetMapping("/api/logs/search")
    public List<Log> searchLogs(@RequestParam String query) {
        return logRepository.searchLogs(query);
    }
}
