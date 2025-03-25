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
    @FXML public TextField nameTextField;
    @FXML public TextField nameField;
    @FXML public TextArea descriptionArea;
    @FXML public TextField fromField;
    @FXML public TextField toField;
    @FXML public ChoiceBox<String> transportTypeChoice;
    @FXML public TextField distanceField;
    @FXML public TextField estimatedTimeField;

    @FXML public TableView<Log> logTableView;
    @FXML public TableColumn<Log, Date> dateColumn;
    @FXML public TableColumn<Log, Time> timeColumn;
    @FXML public TableColumn<Log, String> commentColumn;
    @FXML public TableColumn<Log, Integer> difficultyColumn;
    @FXML public TableColumn<Log, Double> totalDistanceColumn;
    @FXML public TableColumn<Log, Time> totalTimeColumn;
    @FXML public TableColumn<Log, Integer> ratingColumn;

    @FXML public TextField logCommentField;
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
        // Bind text fields to view model properties
        nameTextField.textProperty().bindBidirectional(tourDetailsViewModel.nameProperty());
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

        // Setup log table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        totalDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalDistance"));
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Set logs as the table's data source
        logTableView.setItems(tourDetailsViewModel.getLogs());

        // Add edit capability to log table
        logTableView.setEditable(true);
        setupEditableColumns();

        // Bind selected log to input fields
        tourDetailsViewModel.selectedLogProperty().addListener((obs, oldLog, newLog) -> {
            if (newLog != null) {
                logCommentField.setText(newLog.getComment());
                logDifficultyField.setText(String.valueOf(newLog.getDifficulty()));
                logTotalDistanceField.setText(String.valueOf(newLog.getTotalDistance()));
                logTotalTimeField.setText(newLog.getTotalTime().toString());
                logRatingField.setText(String.valueOf(newLog.getRating()));
            } else {
                logCommentField.clear();
                logDifficultyField.clear();
                logTotalDistanceField.clear();
                logTotalTimeField.clear();
                logRatingField.clear();
            }
        });

        logTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            tourDetailsViewModel.selectedLogProperty().set(newSelection);
        });
    }

    private void setupEditableColumns() {
        // Implement editable columns if needed
    }

    public void setTour(Tour tour) {
        this.currentTour = tour;
        tourDetailsViewModel.setTourModel(tour);
    }

    public void clearFields() {
        tourDetailsViewModel.setTourModel(null);
        observableLogs.clear();
    }

    @FXML
    void onAddLogButtonPressed() {
        if (validateLogFields()) {
            try {
                Date date = new Date(System.currentTimeMillis());
                Time time = new Time(System.currentTimeMillis());
                String comment = logCommentField.getText();
                int difficulty = Integer.parseInt(logDifficultyField.getText());
                double totalDistance = Double.parseDouble(logTotalDistanceField.getText());
                Time totalTime = Time.valueOf(logTotalTimeField.getText());
                int rating = Integer.parseInt(logRatingField.getText());

                tourDetailsViewModel.createNewLog(date, time, comment, difficulty, totalDistance, totalTime, rating);
            } catch (NumberFormatException e) {
                // Handle invalid input
                showAlert("Invalid input", "Please enter valid values for the log attributes.");
            }
        } else {
            showAlert("Validation Error", "Please fill in all required fields with valid values.");
        }
    }

    @FXML
    void onDeleteLogButtonPressed() {
        tourDetailsViewModel.deleteSelectedLog();
    }

    @FXML
    void onUpdateLogButtonPressed() {
        Log selectedLog = tourDetailsViewModel.selectedLogProperty().get();
        if (selectedLog != null) {
            if (validateLogFields()) {
                try {
                    String comment = logCommentField.getText();
                    int difficulty = Integer.parseInt(logDifficultyField.getText());
                    double totalDistance = Double.parseDouble(logTotalDistanceField.getText());
                    Time totalTime = Time.valueOf(logTotalTimeField.getText());
                    int rating = Integer.parseInt(logRatingField.getText());

                    tourDetailsViewModel.updateSelectedLog(selectedLog, comment, difficulty, totalDistance, totalTime, rating);
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    showAlert("Invalid input", "Please enter valid values for the log attributes.");
                }
            } else {
                showAlert("Validation Error", "Please fill in all required fields with valid values.");
            }
        }
    }

    private boolean validateLogFields() {
        return tourDetailsViewModel.validateLogFields(
                logCommentField.getText(),
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
}