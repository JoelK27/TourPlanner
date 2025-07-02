package at.fhtw.tourplanner.repo;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Integer> {
    List<Log> findByTour(Tour tour);

    @Query("SELECT l FROM Log l WHERE " +
       "LOWER(l.comment) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
       "CAST(l.difficulty AS string) LIKE CONCAT('%', :query, '%') OR " +
       "CAST(l.totalDistance AS string) LIKE CONCAT('%', :query, '%') OR " +
       "CAST(l.totalTime AS string) LIKE CONCAT('%', :query, '%') OR " +
       "CAST(l.rating AS string) LIKE CONCAT('%', :query, '%')")
    List<Log> searchLogs(@Param("query") String query);
}