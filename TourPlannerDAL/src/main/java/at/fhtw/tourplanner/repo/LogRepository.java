package at.fhtw.tourplanner.repo;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Integer> {
    List<Log> findByTour(Tour tour);
}

