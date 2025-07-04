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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final TourRepository tourRepository;
    private final LogRepository logRepository;
    private final ImageService imageService;
    private static final Logger logger = LogManager.getLogger(ReportService.class);


    public byte[] generateTourReport(int tourId) {
        logger.info("Starting tour report generation for tour ID: {}", tourId);
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
            float lineHeight = 14f;
            // Beschreibung mehrzeilig ausgeben
            String[] descLines = tour.getTourDescription() != null ? tour.getTourDescription().split("\\r?\\n") : new String[]{""};
            for (String line : descLines) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Description: ".equals(line) ? line : line);
                content.endText();
                y -= lineHeight;
            }

            // From/To
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("From: " + tour.getFromLocation() + "   To: " + tour.getToLocation());
            content.endText();
            y -= lineHeight;

            // Transport/Distanz/Zeit
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Transport: " + tour.getTransportType() + "   Distance: " + tour.getTourDistance() + " km   Time: " + tour.getEstimatedTime() + " h");
            content.endText();
            y -= lineHeight;

            // === MAP IMAGE EINBINDEN ===
            y -= 20;
            try {
                String encodedRouteGeometry = tour.getEncodedRouteGeometry();
                double[] startCoords = tour.getStartCoordsAsArray();
                double[] endCoords = tour.getEndCoordsAsArray();

                if (encodedRouteGeometry != null && startCoords != null && endCoords != null) {
                    logger.debug("Generating map image for tour {}", tour.getId());

                    byte[] imageBytes = imageService.fetchTourMapImage(encodedRouteGeometry, startCoords, endCoords);

                    if (imageBytes != null) {
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, "map.jpg");
                        float imageWidth = 350;
                        float imageHeight = 200;
                        float x = 50;
                        y -= imageHeight;
                        content.drawImage(pdImage, x, y, imageWidth, imageHeight);
                        y -= 20;
                        logger.debug("Map image successfully added to PDF for tour {}", tour.getId());
                    } else {
                        logger.warn("Failed to load map image for tour {}", tour.getId());
                        y -= 20;
                        content.beginText();
                        content.newLineAtOffset(50, y);
                        content.showText("[Karte konnte nicht geladen werden]");
                        content.endText();
                        y -= 10;
                    }
                } else {
                    logger.warn("No routing data available for map generation for tour {}", tour.getId());
                    y -= 20;
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    content.showText("[Keine Routingdaten für Karte vorhanden]");
                    content.endText();
                    y -= 10;
                }
            } catch (Exception e) {
                logger.error("Error inserting map into PDF for tour {}: {}", tour.getId(), e.getMessage(), e);
                y -= 20;
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("[Fehler beim Einfügen der Karte]");
                content.endText();
                y -= 10;
            }

            y -= 10;
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Tour Logs:");
            content.endText();
            y -= lineHeight;
            content.setFont(PDType1Font.HELVETICA, 11);

            for (Log log : logs) {
                if (y < 100) { // Neue Seite, falls zu wenig Platz
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - 50;
                }
                // Kommentar mehrzeilig ausgeben
                String[] commentLines = log.getComment() != null ? log.getComment().split("\\r?\\n") : new String[]{""};
                for (int i = 0; i < commentLines.length; i++) {
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    if (i == 0) {
                        content.showText(String.format("Date: %s | Comment: %s | Difficulty: %d | Distance: %.2f km | Time: %s | Rating: %d",
                                log.getDate(), commentLines[i], log.getDifficulty(), log.getTotalDistance(),
                                log.getTotalTime(), log.getRating()));
                    } else {
                        content.showText("                " + commentLines[i]);
                    }
                    content.endText();
                    y -= lineHeight;
                }
            }

            content.close();
            doc.save(out);
            logger.info("Tour report generated successfully for tour ID: {}", tourId);
            return out.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating tour report for tour ID {}: {}", tourId, e.getMessage(), e);
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