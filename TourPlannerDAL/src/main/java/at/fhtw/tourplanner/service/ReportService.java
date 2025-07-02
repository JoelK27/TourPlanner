package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.repo.TourRepository;
import at.fhtw.tourplanner.repo.LogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final TourRepository tourRepository;
    private final LogRepository logRepository;

    public byte[] generateTourReport(int tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow();
        List<Log> logs = logRepository.findByTour(tour);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - 50;
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Tour Report: " + tour.getName());
            content.endText();

            y -= 30;
            content.setFont(PDType1Font.HELVETICA, 12);

            // Tour-Details
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Description: " + tour.getTourDescription());
            content.endText();

            y -= 20;
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("From: " + tour.getFromLocation() + "   To: " + tour.getToLocation());
            content.endText();

            y -= 20;
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Transport: " + tour.getTransportType() + "   Distance: " + tour.getTourDistance() + " km   Time: " + tour.getEstimatedTime() + " h");
            content.endText();

            y -= 30;
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Tour Logs:");
            content.endText();

            y -= 20;
            content.setFont(PDType1Font.HELVETICA, 11);

            for (Log log : logs) {
                if (y < 100) { // Neue Seite, falls zu wenig Platz
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - 50;
                }
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(String.format("Date: %s | Comment: %s | Difficulty: %d | Distance: %.2f km | Time: %s | Rating: %d",
                        log.getDate(), log.getComment(), log.getDifficulty(), log.getTotalDistance(),
                        log.getTotalTime(), log.getRating()));
                content.endText();
                y -= 18;
            }

            content.close();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generateSummaryReport() {
        List<Tour> tours = tourRepository.findAll();

        try (PDDocument doc = new PDDocument();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - 50;
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Tour Summary Report");
            content.endText();

            y -= 30;
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Tour Name | Avg. Time (h) | Avg. Distance (km) | Avg. Rating");
            content.endText();

            y -= 20;
            content.setFont(PDType1Font.HELVETICA, 11);

            for (Tour tour : tours) {
                List<Log> logs = logRepository.findByTour(tour);

                double avgTime = logs.stream()
                        .mapToDouble(l -> l.getTotalTime() != null ? l.getTotalTime().toLocalTime().toSecondOfDay() / 3600.0 : 0)
                        .average().orElse(0);
                double avgDistance = logs.stream()
                        .mapToDouble(Log::getTotalDistance)
                        .average().orElse(0);
                double avgRating = logs.stream()
                        .mapToInt(Log::getRating)
                        .average().orElse(0);

                if (y < 100) { // Neue Seite, falls zu wenig Platz
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - 50;
                }

                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(String.format("%s | %.2f | %.2f | %.2f",
                        tour.getName(), avgTime, avgDistance, avgRating));
                content.endText();
                y -= 18;
            }

            content.close();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}