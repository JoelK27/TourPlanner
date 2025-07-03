package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.apiclient.TourApiService;
import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.viewmodel.TourDetailsViewModel;
import at.fhtw.tourplanner.viewmodel.TourOverviewViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

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
    @FXML public ImageView tourImageView;
    @FXML public Label imageStatusLabel;

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

        Platform.runLater(() -> {
            if (mapWebView != null) {
                // Detailliertes Error-Handling für WebView
                mapWebView.getEngine().setOnError(event -> {
                    System.err.println("WebView Error: " + event.getMessage());
                    System.err.println("Error source: " + event.getSource());
                });

                // JavaScript Console Output anzeigen
                mapWebView.getEngine().setOnAlert(event -> {
                    System.out.println("JavaScript Alert: " + event.getData());
                });

                // Load Worker Status überwachen
                mapWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    System.out.println("WebView Load State: " + oldValue + " -> " + newValue);

                    switch (newValue) {
                        case SUCCEEDED:
                            System.out.println("WebView content loaded successfully!");
                            break;
                        case FAILED:
                            System.err.println("WebView failed to load content");
                            Throwable exception = mapWebView.getEngine().getLoadWorker().getException();
                            if (exception != null) {
                                System.err.println("Exception: " + exception.getMessage());
                            }
                            break;
                        case CANCELLED:
                            System.err.println("WebView loading was cancelled");
                            break;
                    }
                });
            } else {
                System.err.println("mapWebView is null!");
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
            loadAndDisplayTourImage();

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
                        System.err.println("Error loading map for tour: " + e.getMessage());
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
            showAlert("Error", "Error creating Log: " + e.getMessage());
        }
    }

    @FXML
    void onDeleteLogButtonPressed() {
        Log selectedLog = logListView.getSelectionModel().getSelectedItem();
        if (selectedLog != null) {
            tourDetailsViewModel.deleteSelectedLog();

            // Wenn nach dem Löschen noch Logs vorhanden sind, wähle das erste aus
            if (!logListView.getItems().isEmpty()) {
                logListView.getSelectionModel().select(0);
            }
        } else {
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

                    tourDetailsViewModel.updateSelectedLog(selectedLog, comment, difficulty, totalDistance, totalTime, rating);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Entry", "Bitte geben Sie gültige Werte für die Log-Attribute ein.");
                } catch (IllegalArgumentException e) {
                    showAlert("Invalid Timeformat", "Please provide a time in following format: HH:MM:SS.");
                }
            } else {
                showAlert("Invalidation Error", "Please fill in all required fields with valid values.");
            }
        } else {
            showAlert("No log selected", "Please select a log-entry from the list.");
        }
    }

    @FXML
    void onUploadImageClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            TourApiService.getInstance().uploadTourImage(currentTour.getId(), file);
            showAlert("Uploaded Image", "The image has been uploaded successfully.");
            loadAndDisplayTourImage();
        }
    }

    @FXML
    void onDownloadImageClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            byte[] imageBytes = TourApiService.getInstance().downloadTourImage(currentTour.getId());
            if (imageBytes != null) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(imageBytes);
                    showAlert("Image saved", "The image has been saved successfully.");
                } catch (Exception e) {
                    showAlert("Error", "Error saving image: " + e.getMessage());
                }
            } else {
                showAlert("Error", "No image available or error while saving.");
            }
        }
    }

    @FXML
    void onRefreshImageClicked() {
        loadAndDisplayTourImage();
    }

    @FXML
    void onSaveTourReportClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            TourApiService.getInstance().saveTourReport(currentTour.getId(), file);
            showAlert("Saved Report", "The Tour-Report has been saved successfully.");
        }
    }

    @FXML
    void onSaveSummaryReportClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            TourApiService.getInstance().saveSummaryReport(file);
            showAlert("Saved Report", "The Summary-Report has been saved successfully.");
        }
    }

    @FXML
    void onShowTourStatsClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        var stats = TourApiService.getInstance().getTourStats(currentTour.getId());
        showAlert("Tour-Statistics",
                "Popularity: " + stats.get("popularity") +
                "\nChild-Friendliness: " + stats.get("childFriendliness") +
                "\nØ Difficulty: " + stats.get("averageDifficulty") +
                "\nØ Time (sec.): " + stats.get("averageTotalTimeSeconds") +
                "\nØ Distance: " + stats.get("averageTotalDistance"));
    }

    @FXML
    void onShowWeatherClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        var weather = TourApiService.getInstance().getTourWeather(currentTour.getId());
        showAlert("Weather Information",
                "Start: " + weather.get("fromWeather") +
                "\nGoal: " + weather.get("toWeather"));
    }

    @FXML
    void onCalculateRoutePressed() {
        String fromLocation = fromField.getText().trim();
        String toLocation = toField.getText().trim();
        String transportType = transportTypeChoice.getValue();

        System.out.println("=== Calculate Route Button Pressed ===");
        System.out.println("From: '" + fromLocation + "'");
        System.out.println("To: '" + toLocation + "'");
        System.out.println("Transport: '" + transportType + "'");

        if (fromLocation.isEmpty() || toLocation.isEmpty()) {
            showAlert("Missing Information", "Please enter both from and to locations.");
            return;
        }

        if (transportType == null) {
            transportType = "Car"; // Default fallback
        }

        try {
            System.out.println("Calling TourApiService.calculateRoute...");
            Map<String, Object> routeInfo = TourApiService.getInstance()
                    .calculateRoute(fromLocation, toLocation, transportType);

            System.out.println("Route calculation completed. Result: " + routeInfo);

            if (!routeInfo.isEmpty()) {
                // Update distance and time fields
                double distance = (Double) routeInfo.get("distance");
                double estimatedTime = (Double) routeInfo.get("estimatedTime");

                System.out.println("Distance: " + distance + " km");
                System.out.println("Estimated Time: " + estimatedTime + " hours");

                distanceField.setText(String.format("%.2f", distance));
                estimatedTimeField.setText(String.format("%.2f", estimatedTime));

                // Update map with route
                System.out.println("Calling updateMap...");
                updateMap(routeInfo);

                showAlert("Route Calculated",
                        String.format("Distance: %.2f km\nEstimated Time: %.2f hours",
                                distance, estimatedTime));
            } else {
                System.err.println("ERROR: Route info is empty!");
                showAlert("Error", "No route data received from server");
            }
        } catch (Exception e) {
            System.err.println("=== EXCEPTION in onCalculateRoutePressed ===");
            System.err.println("Error calculating route: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==========================================");
            showAlert("Error", "Failed to calculate route: " + e.getMessage());

            // Fallback: Zeige Test-Karte
            showTestMap();
        }
    }

    private void updateMap(Map<String, Object> routeInfo) {
        try {
            System.out.println("=== UpdateMap Debug ===");
            System.out.println("RouteInfo keys: " + routeInfo.keySet());
            System.out.println("RouteInfo: " + routeInfo);

            String routeGeometry = (String) routeInfo.get("routeGeometry");
            System.out.println("Route Geometry type: " + (routeGeometry != null ? routeGeometry.getClass().getName() : "null"));
            System.out.println("Route Geometry: " + routeGeometry);

            // Prüfe ob es valides JSON ist
            if (routeGeometry != null && !routeGeometry.equals("{}") && !routeGeometry.equals("null")) {
                System.out.println("✓ Valid route geometry detected");
            } else {
                System.out.println("⚠ No valid route geometry - showing simple map");
                showTestMap();
                return;
            }

            // Koordinaten verarbeiten
            Object startCoordsObj = routeInfo.get("startCoords");
            Object endCoordsObj = routeInfo.get("endCoords");

            System.out.println("Start Coords Object: " + startCoordsObj + " (Type: " + (startCoordsObj != null ? startCoordsObj.getClass().getName() : "null") + ")");
            System.out.println("End Coords Object: " + endCoordsObj + " (Type: " + (endCoordsObj != null ? endCoordsObj.getClass().getName() : "null") + ")");

            if (startCoordsObj == null || endCoordsObj == null) {
                System.err.println("ERROR: Coordinates are null!");
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
                System.err.println("ERROR: Unexpected startCoords type: " + startCoordsObj.getClass());
                showTestMap();
                return;
            }

            if (endCoordsObj instanceof List) {
                List<Double> endCoordsList = (List<Double>) endCoordsObj;
                endCoords = endCoordsList.stream().mapToDouble(Double::doubleValue).toArray();
            } else if (endCoordsObj instanceof double[]) {
                endCoords = (double[]) endCoordsObj;
            } else {
                System.err.println("ERROR: Unexpected endCoords type: " + endCoordsObj.getClass());
                showTestMap();
                return;
            }

            System.out.println("Start Coords: [" + startCoords[0] + ", " + startCoords[1] + "]");
            System.out.println("End Coords: [" + endCoords[0] + ", " + endCoords[1] + "]");

            String mapHtml = generateMapHtml(routeGeometry, startCoords, endCoords);
            System.out.println("Generated HTML length: " + mapHtml.length());

            Platform.runLater(() -> {
                System.out.println("Loading map HTML into WebView...");
                mapWebView.getEngine().loadContent(mapHtml);
            });

            System.out.println("========================");
        } catch (Exception e) {
            System.err.println("=== ERROR in updateMap ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================");
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
            System.out.println("Loading test map...");
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
    }

    // Methode zum Laden und Anzeigen des Tour-Bildes:
    private void loadAndDisplayTourImage() {
        if (currentTour == null) {
            tourImageView.setImage(null);
            imageStatusLabel.setText("No image available");
            return;
        }
        
        try {
            byte[] imageBytes = TourApiService.getInstance().downloadTourImage(currentTour.getId());
            if (imageBytes != null && imageBytes.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                tourImageView.setImage(image);
                imageStatusLabel.setText("Image loaded successfully");
            } else {
                tourImageView.setImage(null);
                imageStatusLabel.setText("No image available");
            }
        } catch (Exception e) {
            tourImageView.setImage(null);
            imageStatusLabel.setText("Error loading image");
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    @FXML
    void onSaveTourDetailsClicked() {
        if (currentTour == null) {
            showAlert("No Tour Selected", "Please select a tour-entry from the list.");
            return;
        }
        try {
            // Tour im ViewModel/Backend aktualisieren
            tourDetailsViewModel.updateTourModel();
            showAlert("Saved", "Tour details have been saved successfully.");
            updateTourTitle();

        } catch (NumberFormatException e) {
            showAlert("Invalid Entry", "Please enter valid numbers for distance and estimated time.");
        }
    }
}