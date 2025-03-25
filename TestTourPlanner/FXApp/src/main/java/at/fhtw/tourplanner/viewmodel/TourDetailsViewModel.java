package at.fhtw.tourplanner.viewmodel;

import at.fhtw.tourplanner.model.Log;
import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.store.TourStore;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.sql.Date;
import java.sql.Time;

public class TourDetailsViewModel {
    private final TourStore store = TourStore.getInstance();
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
        name.addListener((arg, oldVal, newVal) -> updateTourModel());
        description.addListener((arg, oldVal, newVal) -> updateTourModel());
        from.addListener((arg, oldVal, newVal) -> updateTourModel());
        to.addListener((arg, oldVal, newVal) -> updateTourModel());
        transportType.addListener((arg, oldVal, newVal) -> updateTourModel());
        distance.addListener((arg, oldVal, newVal) -> updateTourModel());
        estimatedTime.addListener((arg, oldVal, newVal) -> updateTourModel());
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

    private void updateTourModel() {
        if (!isInitValue && tourModel != null) {
            store.updateTour(
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
        logs.addAll(store.getLogsForTour(tour.getId()));
    }

    public void createNewLog(Date date, Time time, String comment, int difficulty, double totalDistance, Time totalTime, int rating) {
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
            store.addLog(tourModel.getId(), newLog);
            logs.add(newLog);
            selectedLog.set(newLog);
        }
    }

    public void updateSelectedLog(Log log, String comment, int difficulty, double totalDistance, Time totalTime, int rating) {
        if (log != null) {
            log.setComment(comment);
            log.setDifficulty(difficulty);
            log.setTotalDistance(totalDistance);
            log.setTotalTime(totalTime);
            log.setRating(rating);
            store.updateLog(log);
            refreshLogs();
        }
    }

    public void deleteSelectedLog() {
        if (selectedLog.get() != null) {
            store.deleteLog(selectedLog.get());
            logs.remove(selectedLog.get());
            selectedLog.set(null);
            refreshLogs();
        }
    }

    private void refreshLogs() {
        if (tourModel != null) {
            logs.setAll(store.getLogsForTour(tourModel.getId()));
        }
    }

    public boolean validateLogFields(String comment, String difficulty, String totalDistance, String totalTime, String rating) {
        try {
            Integer.parseInt(difficulty);
            Double.parseDouble(totalDistance);
            Time.valueOf(totalTime);
            Integer.parseInt(rating);
        } catch (NumberFormatException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return !comment.isEmpty();
    }
}