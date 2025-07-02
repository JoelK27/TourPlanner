package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.repo.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final TourRepository tourRepository;
    private final LogRepository logRepository;

    public Map<String, Object> getTourStats(int tourId) {
        Map<String, Object> stats = new HashMap<>();
        Tour tour = tourRepository.findById(tourId).orElseThrow();
        List<Log> logs = logRepository.findByTour(tour);

        // Popularity: Anzahl der Logs
        int popularity = logs.size();
        stats.put("popularity", popularity);

        // Durchschnittswerte berechnen
        double avgDifficulty = logs.stream().mapToInt(Log::getDifficulty).average().orElse(0);
        double avgTotalTime = logs.stream()
                .mapToDouble(l -> l.getTotalTime() != null ? l.getTotalTime().toLocalTime().toSecondOfDay() : 0)
                .average().orElse(0);
        double avgTotalDistance = logs.stream().mapToDouble(Log::getTotalDistance).average().orElse(0);

        stats.put("averageDifficulty", avgDifficulty);
        stats.put("averageTotalTimeSeconds", avgTotalTime);
        stats.put("averageTotalDistance", avgTotalDistance);

        // Child-Friendliness: Beispiel-Formel (je niedriger, desto besser)
        // < 3 Difficulty, < 2h Zeit, < 10km = sehr kinderfreundlich
        boolean childFriendly = avgDifficulty <= 2.5 && avgTotalTime <= 2 * 3600 && avgTotalDistance <= 10;
        stats.put("childFriendliness", childFriendly);

        return stats;
    }
}