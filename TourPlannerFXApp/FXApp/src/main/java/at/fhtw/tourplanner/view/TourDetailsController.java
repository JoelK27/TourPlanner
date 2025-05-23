package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.store.TourStore;
import at.fhtw.tourplanner.viewmodel.TourDetailsViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.NumberStringConverter;

import java.sql.Date;
import java.sql.Time;

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

    // Tour Logs Tab - neue UI-Komponenten
    @FXML public ListView<Log> logListView;
    @FXML public Label logDateLabel;
    @FXML public Label logTimeLabel;
    @FXML public TextArea logCommentArea;
    @FXML public TextField logDifficultyField;
    @FXML public TextField logTotalDistanceField;
    @FXML public TextField logTotalTimeField;
    @FXML public TextField logRatingField;

    private final TourDetailsViewModel tourDetailsViewModel;
    private final TourStore store = TourStore.getInstance();
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

        // Setup für die Log-ListView
        logListView.setItems(tourDetailsViewModel.getLogs());

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
        updateTourTitle();
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
}