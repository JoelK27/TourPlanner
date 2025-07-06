package at.fhtw.tourplanner.viewmodel;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.apiclient.TourApiService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.sql.Date;
import java.sql.Time;

public class TourDetailsViewModel {
    private final TourApiService apiService = TourApiService.getInstance();
    private Tour tourModel;
    private volatile boolean isInitValue = false;

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty from = new SimpleStringProperty();
    private final StringProperty to = new SimpleStringProperty();
    private final StringProperty transportType = new SimpleStringProperty();
    private final DoubleProperty distance = new SimpleDoubleProperty();
    private final DoubleProperty estimatedTime = new SimpleDoubleProperty();

    private final ObservableList<Log> logs = FXCollections.observableArrayList();
    private final ObjectProperty<Log> selectedLog = new SimpleObjectProperty<>();

    // List of possible transport types
    @Getter
    private final ObservableList<String> transportTypes =
            FXCollections.observableArrayList("Car", "Bicycle", "Walking", "Hiking", "Bus", "Train");

    public TourDetailsViewModel() {
        // Update model when properties change

    }

    // Add property accessor methods
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty fromProperty() {
        return from;
    }

    public StringProperty toProperty() {
        return to;
    }

    public StringProperty transportTypeProperty() {
        return transportType;
    }

    public DoubleProperty distanceProperty() {
        return distance;
    }

    public DoubleProperty estimatedTimeProperty() {
        return estimatedTime;
    }

    public ObservableList<Log> getLogs() {
        return logs;
    }

    public ObjectProperty<Log> selectedLogProperty() {
        return selectedLog;
    }

    public void setTourModel(Tour tourModel) {
        isInitValue = true;
        if (tourModel == null) {
            // Clear fields if no tour is selected
            name.set("");
            description.set("");
            from.set("");
            to.set("");
            transportType.set("Car");
            distance.set(0.0);
            estimatedTime.set(0.0);
            logs.clear();
            return;
        }

        System.out.println("Set tour model: " + tourModel.getName());
        this.tourModel = tourModel;
        name.set(tourModel.getName());
        description.set(tourModel.getTourDescription());
        from.set(tourModel.getFrom());
        to.set(tourModel.getTo());
        transportType.set(tourModel.getTransportType());
        distance.set(tourModel.getTourDistance());
        estimatedTime.set(tourModel.getEstimatedTime());
        loadLogsForTour(tourModel);
        isInitValue = false;
    }

    public void updateTourModel() {
        if (!isInitValue && tourModel != null) {
            apiService.updateTour(
                    tourModel,
                    name.get(),
                    description.get(),
                    from.get(),
                    to.get(),
                    transportType.get(),
                    distance.get(),
                    estimatedTime.get()
            );
        }
    }

    private void loadLogsForTour(Tour tour) {
        logs.clear();
        logs.addAll(apiService.getLogsForTour(tour.getId()));
    }

    public Log createNewLog(Date date, Time time, String comment, int difficulty, double totalDistance, Time totalTime, int rating) {
        if (tourModel != null) {
            Log newLog = new Log();
            newLog.setTourId(tourModel.getId());
            newLog.setDate(date);
            newLog.setTime(time);
            newLog.setComment(comment);
            newLog.setDifficulty(difficulty);
            newLog.setTotalDistance(totalDistance);
            newLog.setTotalTime(totalTime);
            newLog.setRating(rating);
            Log savedLog = apiService.addLog(tourModel.getId(), newLog);

            if (savedLog != null) {
                logs.add(savedLog);
                selectedLog.set(savedLog);
                return savedLog;
            }
            return null;
        }
        return null;
    }

    public void updateSelectedLog(Log log, String comment, int difficulty, double totalDistance, Time totalTime, int rating) {
        if (log != null && log.getId() > 0) {
            log.setComment(comment);
            log.setDifficulty(difficulty);
            log.setTotalDistance(totalDistance);
            log.setTotalTime(totalTime);
            log.setRating(rating);

            Log updatedLog = apiService.updateLog(log);

            // Prüfe, ob das Update erfolgreich war
            if (updatedLog != null) {
                log.setDate(updatedLog.getDate());
                log.setTime(updatedLog.getTime());
            }

            refreshLogs();
        } else {
            System.err.println("Attempt to update invalid log (id=" + (log != null ? log.getId() : "null") + ")");
        }
    }

    public void deleteSelectedLog() {
        if (selectedLog.get() != null) {
            apiService.deleteLog(selectedLog.get());
            logs.remove(selectedLog.get());
            selectedLog.set(null);
            refreshLogs();
        }
    }

    private void refreshLogs() {
        if (tourModel != null) {
            logs.setAll(apiService.getLogsForTour(tourModel.getId()));
        }
    }

    public boolean validateLogFields(String comment, String difficulty, String totalDistance, String totalTime, String rating) {
        try {
            Integer.parseInt(difficulty);
            Double.parseDouble(totalDistance);
            Time.valueOf(totalTime);
            Integer.parseInt(rating);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return !comment.isEmpty();
    }
}