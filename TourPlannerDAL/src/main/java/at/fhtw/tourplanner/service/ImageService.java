package at.fhtw.tourplanner.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    private final String imageDir = "tour-images";

    public void saveTourImage(int tourId, byte[] imageBytes) {
        try {
            Files.createDirectories(Paths.get(imageDir));
            Path filePath = Paths.get(imageDir, "tour_" + tourId + ".jpg");
            Files.write(filePath, imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Could not save image", e);
        }
    }

    public Resource loadTourImage(int tourId) {
        Path filePath = Paths.get(imageDir, "tour_" + tourId + ".jpg");
        return new FileSystemResource(filePath);
    }
}