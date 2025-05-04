package at.fhtw.tourplanner.repo;

import at.fhtw.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Integer> {}
