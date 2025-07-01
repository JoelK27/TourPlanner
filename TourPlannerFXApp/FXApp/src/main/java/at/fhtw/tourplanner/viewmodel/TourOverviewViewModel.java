package at.fhtw.tourplanner.viewmodel;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.apiclient.TourApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class TourOverviewViewModel {
    private final TourApiService apiService = TourApiService.getInstance();

    public interface SelectionChangedListener {
        void changeSelection(Tour tour);
    }

    private final List<SelectionChangedListener> listeners = new ArrayList<>();
    private final ObservableList<Tour> observableTours = FXCollections.observableArrayList();
    private final StringProperty searchText = new SimpleStringProperty("");

    public TourOverviewViewModel() {
        setTours(apiService.getAllTours());
    }

    public ObservableList<Tour> getObservableTours() {
        return observableTours;
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public void search() {
        List<Tour> results = apiService.searchTours(searchText.get());
        observableTours.clear();
        observableTours.addAll(results);
    }

    public ChangeListener<Tour> getChangeListener() {
        return (observableValue, oldValue, newValue) -> notifyListeners(newValue);
    }

    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionChangedListener(SelectionChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Tour newValue) {
        for (var listener : listeners) {
            listener.changeSelection(newValue);
        }
    }

    public void setTours(List<Tour> tours) {
        observableTours.clear();
        observableTours.addAll(tours);
    }

    public void addNewTour() {
        var tour = apiService.createNewTour();
        observableTours.add(tour);
    }

    public void deleteTour(Tour tour) {
        apiService.deleteTour(tour);
        observableTours.remove(tour);
    }
}