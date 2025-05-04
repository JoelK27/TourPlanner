package at.fhtw.tourplanner.rest;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.repo.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourRepository tourRepository;

    @GetMapping
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @PostMapping
    public Tour createTour(@RequestBody Tour tour) {
        return tourRepository.save(tour);
    }

    @DeleteMapping("/{id}")
    public void deleteTour(@PathVariable int id) {
        tourRepository.deleteById(id);
    }
}
