package at.fhtw.tourplanner.viewmodel;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.store.TourStore;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainWindowViewModel {
    private final TourStore store = TourStore.getInstance();
    private final StringProperty searchField = new SimpleStringProperty("");
    private final ObservableList<Tour> tourList = FXCollections.observableArrayList();
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    // Tour properties
    private final StringProperty tourName = new SimpleStringProperty("");
    private final StringProperty tourDescription = new SimpleStringProperty("");
    private final StringProperty from = new SimpleStringProperty("");
    private final StringProperty to = new SimpleStringProperty("");
    private final StringProperty transportType = new SimpleStringProperty("Car");
    private final DoubleProperty tourDistance = new SimpleDoubleProperty(0);
    private final DoubleProperty estimatedTime = new SimpleDoubleProperty(0);

    private final SearchBarViewModel searchBarViewModel;
    private final TourOverviewViewModel tourOverviewViewModel;
    private final TourDetailsViewModel tourDetailsViewModel;

    public MainWindowViewModel(SearchBarViewModel searchBarViewModel,
                               TourOverviewViewModel tourOverviewViewModel,
                               TourDetailsViewModel tourDetailsViewModel) {
        this.searchBarViewModel = searchBarViewModel;
        this.tourOverviewViewModel = tourOverviewViewModel;
        this.tourDetailsViewModel = tourDetailsViewModel;

        setupBindings();
        updateTourList(); // Initial load
    }

    // Default constructor
    public MainWindowViewModel() {
        this.searchBarViewModel = null;
        this.tourOverviewViewModel = null;
        this.tourDetailsViewModel = null;

        setupBindings();
        updateTourList(); // Initial load
    }

    private void setupBindings() {
        selectedTour.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tourName.set(newValue.getName());
                tourDescription.set(newValue.getTourDescription());
                from.set(newValue.getFrom());
                to.set(newValue.getTo());
                transportType.set(newValue.getTransportType());
                tourDistance.set(newValue.getTourDistance());
                estimatedTime.set(newValue.getEstimatedTime());
            } else {
                tourName.set("");
                tourDescription.set("");
                from.set("");
                to.set("");
                transportType.set("Car");
                tourDistance.set(0);
                estimatedTime.set(0);
            }
        });
    }

    public void updateTourList() {
        tourList.clear();
        tourList.addAll(store.searchTours(searchField.get()));

        if (tourOverviewViewModel != null) {
            tourOverviewViewModel.setTours(tourList);
        }
    }

    public void createNewTour() {
        var newTour = store.createNewTour();
        updateTourList();
        selectedTour.set(newTour);
    }

    public void updateSelectedTour() {
        if (selectedTour.get() != null) {
            store.updateTour(
                    selectedTour.get(),
                    tourName.get(),
                    tourDescription.get(),
                    from.get(),
                    to.get(),
                    transportType.get(),
                    tourDistance.get(),
                    estimatedTime.get()
            );
            updateTourList();
        }
    }

    public void deleteSelectedTour() {
        if (selectedTour.get() != null) {
            store.deleteTour(selectedTour.get());
            updateTourList();
            selectedTour.set(null);
        }
    }

    // Getters for properties
    public StringProperty searchFieldProperty() { return searchField; }
    public ObservableList<Tour> tourListProperty() { return tourList; }
    public ObjectProperty<Tour> selectedTourProperty() { return selectedTour; }
    public StringProperty tourNameProperty() { return tourName; }
    public StringProperty tourDescriptionProperty() { return tourDescription; }
    public StringProperty fromProperty() { return from; }
    public StringProperty toProperty() { return to; }
    public StringProperty transportTypeProperty() { return transportType; }
    public DoubleProperty tourDistanceProperty() { return tourDistance; }
    public DoubleProperty estimatedTimeProperty() { return estimatedTime; }
}