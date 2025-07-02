package at.fhtw.tourplanner.repo;

import at.fhtw.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    @Query("""
        SELECT t FROM Tour t
        LEFT JOIN t.logs l
        GROUP BY t
        HAVING
            LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(t.tourDescription) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR
            CAST(COUNT(l) AS string) LIKE CONCAT('%', :query, '%') OR
            CAST(COALESCE(AVG(l.difficulty), 0) AS string) LIKE CONCAT('%', :query, '%')
    """)
    List<Tour> searchTours(@Param("query") String query);
}