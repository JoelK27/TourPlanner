package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.apiclient.TourApiService;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.viewmodel.TourDetailsViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TourDetailsController {
    // Tour Details Tab
    @FXML public Label tourTitleLabel;
    @FXML public TextField nameField;
    @FXML public TextArea descriptionArea;
    @FXML public TextField fromField;
    @FXML public TextField toField;
    @FXML public ChoiceBox<String> transportTypeChoice;
    @FXML public TextField distanceField;
    @FXML public TextField estimatedTimeField;
    @FXML public TextArea quickNotesArea;
    @FXML public Button saveNotesButton;

    // Tour Logs Tab - neue UI-Komponenten
    @FXML public ListView<Log> logListView;
    @FXML public Label logDateLabel;
    @FXML public Label logTimeLabel;
    @FXML public TextArea logCommentArea;
    @FXML public TextField logDifficultyField;
    @FXML public TextField logTotalDistanceField;
    @FXML public TextField logTotalTimeField;
    @FXML public TextField logRatingField;
    @FXML public Button calculateRouteButton;
    @FXML public WebView mapWebView;

    private final TourDetailsViewModel tourDetailsViewModel;
    private final ObservableList<Log> observableLogs = FXCollections.observableArrayList();
    private Tour currentTour;
    private static final Logger logger = LogManager.getLogger(TourDetailsController.class);

    public TourDetailsController(TourDetailsViewModel tourDetailsViewModel) {
        this.tourDetailsViewModel = tourDetailsViewModel;
    }

    public TourDetailsController() {
        this.tourDetailsViewModel = new TourDetailsViewModel();
    }

    public TourDetailsViewModel getTourDetailsViewModel() {
        return tourDetailsViewModel;
    }

    @FXML
    void initialize() {
        // Bind text fields to view model properties for tour details
        tourDetailsViewModel.nameProperty().addListener((obs, oldName, newName) -> {
            updateTourTitle();
        });

        nameField.textProperty().bindBidirectional(tourDetailsViewModel.nameProperty());
        descriptionArea.textProperty().bindBidirectional(tourDetailsViewModel.descriptionProperty());
        fromField.textProperty().bindBidirectional(tourDetailsViewModel.fromProperty());
        toField.textProperty().bindBidirectional(tourDetailsViewModel.toProperty());

        // Setup transport type choice box
        transportTypeChoice.setItems(tourDetailsViewModel.getTransportTypes());
        transportTypeChoice.valueProperty().bindBidirectional(tourDetailsViewModel.transportTypeProperty());

        // Bind numeric fields with converter
        distanceField.textProperty().bindBidirectional(
                tourDetailsViewModel.distanceProperty(),
                new NumberStringConverter()
        );
        estimatedTimeField.textProperty().bindBidirectional(
                tourDetailsViewModel.estimatedTimeProperty(),
                new NumberStringConverter()
        );

        // Quick Notes TextArea Setup
        if (quickNotesArea != null) {
            quickNotesArea.setPromptText("Add quick notes about this tour...");
            quickNotesArea.setWrapText(true);
            quickNotesArea.setPrefRowCount(3);
        }

        Platform.runLater(() -> {
            if (mapWebView != null) {
                mapWebView.getEngine().setOnError(event -> {
                    logger.error("WebView Error: {}", event.getMessage());
                    logger.error("Error source: {}", event.getSource());
                });

                mapWebView.getEngine().setOnAlert(event -> {
                    logger.info("JavaScript Alert: {}", event.getData());
                });

                mapWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    logger.debug("WebView Load State: {} -> {}", oldValue, newValue);

                    switch (newValue) {
                        case SUCCEEDED:
                            logger.debug("WebView content loaded successfully!");
                            break;
                        case FAILED:
                            logger.error("WebView failed to load content");
                            Throwable exception = mapWebView.getEngine().getLoadWorker().getException();
                            if (exception != null) {
                                logger.error("Exception: {}", exception.getMessage(), exception);
                            }
                            break;
                        case CANCELLED:
                            logger.warn("WebView loading was cancelled");
                            break;
                    }
                });
            } else {
                logger.error("mapWebView is null!");
            }
        });

        // Setup für die Log-ListView
        logListView.setItems(tourDetailsViewModel.getLogs());

        if (tourDetailsViewModel.getLogs().isEmpty()) {
            logListView.getSelectionModel().clearSelection();
            tourDetailsViewModel.selectedLogProperty().set(null);
        }

        // Anzeigen der Details beim Auswählen eines Logs
        logListView.getSelectionModel().selectedItemProperty().addListener((obs, oldLog, newLog) -> {
            tourDetailsViewModel.selectedLogProperty().set(newLog);
            updateLogDetailsView(newLog);
        });

        // Zellenfactory für bessere Darstellung in der Liste
        logListView.setCellFactory(lv -> new ListCell<Log>() {
            @Override
            protected void updateItem(Log log, boolean empty) {
                super.updateItem(log, empty);
                if (empty || log == null) {
                    setText(null);
                } else {
                    // Format: "YYYY-MM-DD - Anfang des Kommentars..."
                    String shortComment = log.getComment();
                    if (shortComment != null && shortComment.length() > 30) {
                        shortComment = shortComment.substring(0, 27) + "...";
                    }
                    setText(log.getDate() + " - " + shortComment);
                }
            }
        });

        // Listener für die Auswahl eines Logs
        tourDetailsViewModel.selectedLogProperty().addListener((obs, oldLog, newLog) -> {
            updateLogDetailsView(newLog);
        });
    }

    private void updateLogDetailsView(Log log) {
        if (log != null) {
            logDateLabel.setText(log.getDate().toString());
            logTimeLabel.setText(log.getTime().toString());
            logCommentArea.setText(log.getComment());
            logDifficultyField.setText(String.valueOf(log.getDifficulty()));
            logTotalDistanceField.setText(String.valueOf(log.getTotalDistance()));
            logTotalTimeField.setText(log.getTotalTime().toString());
            logRatingField.setText(String.valueOf(log.getRating()));
        } else {
            logDateLabel.setText("");
            logTimeLabel.setText("");
            logCommentArea.setText("");
            logDifficultyField.clear();
            logTotalDistanceField.clear();
            logTotalTimeField.clear();
            logRatingField.clear();
        }
    }

    public void setTour(Tour tour) {
        this.currentTour = tour;
        tourDetailsViewModel.setTourModel(tour);

        if (tour != null) {
            updateTourTitle();
            loadTourNotes(); // Neue Methode

            // Automatisch Route berechnen und Karte anzeigen, falls From/To vorhanden
            if (tour.getFrom() != null && tour.getTo() != null &&
                    !tour.getFrom().isEmpty() && !tour.getTo().isEmpty()) {

                // Kleine Verzögerung, damit die UI-Elemente geladen sind
                Platform.runLater(() -> {
                    try {
                        Map<String, Object> routeInfo = TourApiService.getInstance()
                                .calculateRoute(tour.getFrom(), tour.getTo(),
                                        tour.getTransportType());
                        updateMap(routeInfo);
                    } catch (Exception e) {
                        logger.error("Error loading map for tour: {}", e.getMessage(), e);
                        // Zeige eine einfache Karte ohne Route
                        showSimpleMap();
                    }
                });
            } else {
                // Zeige eine leere Karte
                showSimpleMap();
            }
        } else {
            clearFields();
        }
    }

    private void loadTourNotes() {
        if (currentTour != null && quickNotesArea != null) {
            try {
                Map<String, String> notesResponse = TourApiService.getInstance().getTourNotes(currentTour.getId());
                String notes = notesResponse.get("notes");
                quickNotesArea.setText(notes != null ? notes : "");
                currentTour.setQuickNotes(notes);
            } catch (Exception e) {
                logger.error("Error loading notes: {}", e.getMessage(), e);
                quickNotesArea.setText("");
            }
        }
    }

    private void showSimpleMap() {
        // Zeige eine einfache Karte ohne Route
        String simpleMapHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <title>Tour Map</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
            <style>body { margin: 0; padding: 0; } #map { height: 100vh; width: 100%; }</style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
            <script>
                var map = L.map('map').setView([48.2082, 16.3738], 6); // Zentriert auf Österreich
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);
            </script>
        </body>
        </html>
        """;

        mapWebView.getEngine().loadContent(simpleMapHtml);
    }

    public void clearFields() {
        tourDetailsViewModel.setTourModel(null);
        observableLogs.clear();
        if (quickNotesArea != null) {
            quickNotesArea.clear();
        }
    }

    @FXML
    void onAddLogButtonPressed() {
        try {
            Date date = new Date(System.currentTimeMillis());
            Time time = new Time(System.currentTimeMillis());

            // Erstelle ein neues Log mit Standardwerten
            Log newLog = tourDetailsViewModel.createNewLog(date, time, "New Entry",
                    3, 0.0, Time.valueOf("00:00:00"), 3);

            // Wähle das neue Log in der Liste aus
            logListView.getSelectionModel().select(newLog);
            logListView.scrollTo(newLog);

            // Setze den Fokus auf das Kommentarfeld für sofortige Bearbeitung
            logCommentArea.requestFocus();
        } catch (Exception e) {
            logger.error("Error creating log: {}", e.getMessage(), e);
            showAlert("Error", "Error creating Log: " + e.getMessage());
        }
    }

    @FXML
    void onDeleteLogButtonPressed() {
        Log selectedLog = logListView.getSelectionModel().getSelectedItem();
        if (selectedLog != null) {
            logger.info("Deleting log with ID: {}", selectedLog.getId());
            tourDetailsViewModel.deleteSelectedLog();

            // Wenn nach dem Löschen noch Logs vorhanden sind, wähle das erste aus
            if (!logListView.getItems().isEmpty()) {
                logListView.getSelectionModel().select(0);
            }
        } else {
            logger.warn("Attempt to delete log without selection");
            showAlert("No log selected", "Please select a log-entry from the list.");
        }
    }

    @FXML
    void onUpdateLogButtonPressed() {
        Log selectedLog = logListView.getSelectionModel().getSelectedItem();
        if (selectedLog != null) {
            if (validateLogFields()) {
                try {
                    String comment = logCommentArea.getText();
                    int difficulty = Integer.parseInt(logDifficultyField.getText());
                    double totalDistance = Double.parseDouble(logTotalDistanceField.getText());
                    Time totalTime = Time.valueOf(logTotalTimeField.getText());
                    int rating = Integer.parseInt(logRatingField.getText());

                    logger.info("Updating log with ID: {} - Comment: {}, Difficulty: {}, Distance: {}, Time: {}, Rating: {}", 
                              selectedLog.getId(), comment, difficulty, totalDistance, totalTime, rating);
                    
                    tourDetailsViewModel.updateSelectedLog(selectedLog, comment, difficulty, totalDistance, totalTime, rating);
                } catch (NumberFormatException e) {
                    logger.error("Invalid number format in log fields: {}", e.getMessage());
                    showAlert("Invalid Entry", "Bitte geben Sie gültige Werte für die Log-Attribute ein.");
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid time format in log fields: {}", e.getMessage());
                    showAlert("Invalid Timeformat", "Please provide a time in following format: HH:MM:SS.");
                }
            } else {
                logger.warn("Log field validation failed");
                showAlert("Invalidation Error", "Please fill in all required fields with valid values.");
            }
        } else {
            logger.warn("Attempt to update log without selection");
            showAlert("No log selected", "Please select a log-entry from the list.");
        }
    }

    @FXML
    void onSaveTourReportClicked() {
        if (currentTour == null) {
            logger.warn("Attempt to save tour report without tour selection");
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            logger.info("Saving tour report for tour ID {} to file: {}", currentTour.getId(), file.getAbsolutePath());
            TourApiService.getInstance().saveTourReport(currentTour.getId(), file);
            showInformation("Saved Report", "The Tour-Report has been saved successfully.");
        }
    }

    @FXML
    void onSaveSummaryReportClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            logger.info("Saving summary report to file: {}", file.getAbsolutePath());
            TourApiService.getInstance().saveSummaryReport(file);
            showInformation("Saved Report", "The Summary-Report has been saved successfully.");
        }
    }

    @FXML
    void onShowTourStatsClicked() {
        if (currentTour == null) {
            logger.warn("Attempt to show tour stats without tour selection");
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        logger.info("Loading tour statistics for tour ID: {}", currentTour.getId());
        var stats = TourApiService.getInstance().getTourStats(currentTour.getId());
        showInformation("Tour-Statistics",
                "Popularity: " + stats.get("popularity") +
                "\nChild-Friendliness: " + stats.get("childFriendliness") +
                "\nØ Difficulty: " + stats.get("averageDifficulty") +
                "\nØ Time (sec.): " + stats.get("averageTotalTimeSeconds") +
                "\nØ Distance: " + stats.get("averageTotalDistance"));
    }

    @FXML
    void onCalculateRoutePressed() {
        String fromLocation = fromField.getText().trim();
        String toLocation = toField.getText().trim();
        String transportType = transportTypeChoice.getValue();

        logger.info("=== Calculate Route Button Pressed ===");
        logger.info("From: '{}'", fromLocation);
        logger.info("To: '{}'", toLocation);
        logger.info("Transport: '{}'", transportType);

        if (fromLocation.isEmpty() || toLocation.isEmpty()) {
            logger.warn("Missing from or to location for route calculation");
            showAlert("Missing Information", "Please enter both from and to locations.");
            return;
        }

        if (transportType == null) {
            transportType = "Car"; // Default fallback
            logger.debug("Using default transport type: {}", transportType);
        }

        try {
            logger.debug("Calling TourApiService.calculateRoute...");
            Map<String, Object> routeInfo = TourApiService.getInstance()
                    .calculateRoute(fromLocation, toLocation, transportType);

            logger.debug("Route calculation completed. Result size: {}", routeInfo.size());

            if (!routeInfo.isEmpty()) {
                // Update distance and time fields
                double distance = (Double) routeInfo.get("distance");
                double estimatedTime = (Double) routeInfo.get("estimatedTime");

                logger.info("Route calculated successfully - Distance: {} km, Estimated Time: {} hours", distance, estimatedTime);

                distanceField.setText(String.format("%.2f", distance));
                estimatedTimeField.setText(String.format("%.2f", estimatedTime));

                // Update map with route
                logger.debug("Calling updateMap...");
                updateMap(routeInfo);

                showInformation("Route Calculated",
                        String.format("Distance: %.2f km\nEstimated Time: %.2f hours",
                                distance, estimatedTime));
            } else {
                logger.error("Route info is empty!");
                showAlert("Error", "No route data received from server");
            }
        } catch (Exception e) {
            logger.error("=== EXCEPTION in onCalculateRoutePressed ===");
            logger.error("Error calculating route: {}", e.getMessage(), e);
            showAlert("Error", "Failed to calculate route: " + e.getMessage());

            // Fallback: Zeige Test-Karte
            showTestMap();
        }
    }

    private void updateMap(Map<String, Object> routeInfo) {
        try {
            logger.debug("=== UpdateMap Debug ===");
            logger.debug("RouteInfo keys: {}", routeInfo.keySet());
            logger.debug("RouteInfo: {}", routeInfo);

            String routeGeometry = (String) routeInfo.get("routeGeometry");
            logger.debug("Route Geometry type: {}", (routeGeometry != null ? routeGeometry.getClass().getName() : "null"));

            if (routeGeometry != null && !routeGeometry.equals("{}") && !routeGeometry.equals("null")) {
                logger.debug("✓ Valid route geometry detected");
            } else {
                logger.warn("⚠ No valid route geometry - showing test map");
                showTestMap();
                return;
            }

            // Koordinaten verarbeiten
            Object startCoordsObj = routeInfo.get("startCoords");
            Object endCoordsObj = routeInfo.get("endCoords");

            logger.debug("Start Coords Object: {} (Type: {})", startCoordsObj, 
                        (startCoordsObj != null ? startCoordsObj.getClass().getName() : "null"));
            logger.debug("End Coords Object: {} (Type: {})", endCoordsObj, 
                        (endCoordsObj != null ? endCoordsObj.getClass().getName() : "null"));

            if (startCoordsObj == null || endCoordsObj == null) {
                logger.error("Coordinates are null!");
                showTestMap();
                return;
            }

            // Konvertiere zu double arrays
            double[] startCoords, endCoords;

            if (startCoordsObj instanceof List) {
                List<Double> startCoordsList = (List<Double>) startCoordsObj;
                startCoords = startCoordsList.stream().mapToDouble(Double::doubleValue).toArray();
            } else if (startCoordsObj instanceof double[]) {
                startCoords = (double[]) startCoordsObj;
            } else {
                logger.error("Unexpected startCoords type: {}", startCoordsObj.getClass());
                showTestMap();
                return;
            }

            if (endCoordsObj instanceof List) {
                List<Double> endCoordsList = (List<Double>) endCoordsObj;
                endCoords = endCoordsList.stream().mapToDouble(Double::doubleValue).toArray();
            } else if (endCoordsObj instanceof double[]) {
                endCoords = (double[]) endCoordsObj;
            } else {
                logger.error("Unexpected endCoords type: {}", endCoordsObj.getClass());
                showTestMap();
                return;
            }

            logger.debug("Start Coords: [{}, {}]", startCoords[0], startCoords[1]);
            logger.debug("End Coords: [{}, {}]", endCoords[0], endCoords[1]);

            String mapHtml = generateMapHtml(routeGeometry, startCoords, endCoords);
            logger.debug("Generated HTML length: {}", mapHtml.length());

            Platform.runLater(() -> {
                logger.debug("Loading map HTML into WebView...");
                mapWebView.getEngine().loadContent(mapHtml);
            });

            logger.debug("Map update completed successfully");
        } catch (Exception e) {
            logger.error("Error updating map: {}", e.getMessage(), e);
            showTestMap();
        }
    }

    private void showTestMap() {
        String testHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <title>Test Map</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
            <style>
                body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
                #map { height: 100vh; width: 100%; }
                .test-info {
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    z-index: 1000;
                    background: white;
                    padding: 10px;
                    border-radius: 5px;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
                }
            </style>
        </head>
        <body>
            <div class="test-info">
                <h3>🧪 TEST MAP</h3>
                <p>If you see this, WebView works!</p>
            </div>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
            <script>
                console.log('Initializing test map...');
                
                var map = L.map('map').setView([48.2082, 16.3738], 8);
                
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);
                
                // Test-Marker
                L.marker([48.2082, 16.3738]).addTo(map)
                    .bindPopup('<b>Vienna</b><br/>Test marker')
                    .openPopup();
                    
                L.marker([47.8095, 13.0550]).addTo(map)
                    .bindPopup('<b>Salzburg</b><br/>Test destination');
                
                // Test-Route (gerade Linie)
                var testRoute = [
                    [48.2082, 16.3738],
                    [47.8095, 13.0550]
                ];
                
                L.polyline(testRoute, {
                    color: 'red',
                    weight: 4,
                    opacity: 0.8
                }).addTo(map);
                
                console.log('Test map loaded successfully!');
            </script>
        </body>
        </html>
        """;

        Platform.runLater(() -> {
            logger.debug("Loading test map...");
            mapWebView.getEngine().loadContent(testHtml);
        });
    }

    private String generateMapHtml(String routeGeometry, double[] startCoords, double[] endCoords) {
        // KEIN Escaping, sondern direkt als JS-Objekt einfügen!
        String routeGeoJsonJs = (routeGeometry != null && !routeGeometry.equals("{}") && !routeGeometry.equals("null"))
                ? routeGeometry
                : "null";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"utf-8\" />" +
                "<title>Tour Map</title>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />" +
                "<style>body { margin: 0; padding: 0; } #map { height: 100vh; width: 100%; }</style>" +
                "</head>" +
                "<body>" +
                "<div id=\"map\"></div>" +
                "<script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>" +
                "<script>" +
                "var map = L.map('map');" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "attribution: '© OpenStreetMap contributors'" +
                "}).addTo(map);" +
                "var routeGeoJSON = " + routeGeoJsonJs + ";" +
                "if (routeGeoJSON && routeGeoJSON !== null) {" +
                "var routeLayer = L.geoJSON(routeGeoJSON, {" +
                "style: { color: '#3388ff', weight: 5, opacity: 0.8 }" +
                "}).addTo(map);" +
                "map.fitBounds(routeLayer.getBounds());" +
                "} else {" +
                "map.setView([" + startCoords[1] + ", " + startCoords[0] + "], 10);" +
                "}" +
                "L.marker([" + startCoords[1] + ", " + startCoords[0] + "]).addTo(map).bindPopup('Start');" +
                "L.marker([" + endCoords[1] + ", " + endCoords[0] + "]).addTo(map).bindPopup('End');" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    private boolean validateLogFields() {
        return tourDetailsViewModel.validateLogFields(
                logCommentArea.getText(),
                logDifficultyField.getText(),
                logTotalDistanceField.getText(),
                logTotalTimeField.getText(),
                logRatingField.getText()
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTourTitle() {
        String name = tourDetailsViewModel.nameProperty().get();
        String from = tourDetailsViewModel.fromProperty().get();
        String to = tourDetailsViewModel.toProperty().get();

        if (name != null && !name.isEmpty()) {
            if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
                tourTitleLabel.setText("Tour: " + name + " (" + from + " to " + to + ")");
            } else {
                tourTitleLabel.setText("Tour: " + name);
            }
        } else {
            tourTitleLabel.setText("New Tour");
        }

        if (currentTour != null && tourTitleLabel != null) {
            String title = currentTour.getName();
            
            // Add note indicator if notes exist
            if (currentTour.getQuickNotes() != null && !currentTour.getQuickNotes().trim().isEmpty()) {
                title += " 📝";
            }
            
            tourTitleLabel.setText(title);
        }
    }

    @FXML
    void onSaveTourDetailsClicked() {
        if (currentTour == null) {
            logger.warn("Attempt to save tour details without tour selection");
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        try {
            logger.info("Saving tour details for tour ID: {}", currentTour.getId());
            // Tour im ViewModel/Backend aktualisieren
            tourDetailsViewModel.updateTourModel();
            showInformation("Saved", "Tour details have been saved successfully.");
            updateTourTitle();
            logger.info("Tour details saved successfully for tour ID: {}", currentTour.getId());

        } catch (NumberFormatException e) {
            logger.error("Invalid number format in tour details: {}", e.getMessage());
            showAlert("Invalid Entry", "Please enter valid numbers for distance and estimated time.");
        }
    }

    @FXML
    void onSaveNotesClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour from the list.");
            return;
        }
        
        String notes = quickNotesArea.getText();
        logger.info("Saving notes for tour ID: {}", currentTour.getId());
        
        try {
            Map<String, String> response = TourApiService.getInstance().updateTourNotes(currentTour.getId(), notes);
            
            if (response.containsKey("error")) {
                showAlert("Error", "Failed to save notes: " + response.get("error"));
                return;
            }
            
            currentTour.setQuickNotes(notes);
            showInformation("Success", "Notes saved successfully!");
            
            // Update tour title to show note indicator
            updateTourTitle();
            
        } catch (Exception e) {
            logger.error("Error saving notes: {}", e.getMessage(), e);
            showAlert("Error", "Failed to save notes: " + e.getMessage());
        }
    }

    @FXML
    void onShowAllNotesClicked() {
        logger.info("Showing all tour notes");
        
        try {
            List<Tour> allTours = TourApiService.getInstance().getAllTours();
            showAllNotesDialog(allTours);
            
        } catch (Exception e) {
            logger.error("Error loading all notes: {}", e.getMessage(), e);
            showAlert("Error", "Failed to load notes: " + e.getMessage());
        }
    }

    private void showAllNotesDialog(List<Tour> tours) {
        StringBuilder allNotes = new StringBuilder();
        allNotes.append("📝 **ALL TOUR NOTES**\n\n");
        
        int notesCount = 0;
        for (Tour tour : tours) {
            if (tour.getQuickNotes() != null && !tour.getQuickNotes().trim().isEmpty()) {
                allNotes.append("🚗 **").append(tour.getName()).append("**\n");
                allNotes.append(tour.getQuickNotes()).append("\n\n");
                notesCount++;
            }
        }
        
        if (notesCount == 0) {
            allNotes.append("No notes found. Start adding some quick notes to your tours!");
        } else {
            allNotes.insert(0, "Found " + notesCount + " tours with notes\n\n");
        }
        
        showInformation("📝 All Tour Notes", allNotes.toString());
    }
}