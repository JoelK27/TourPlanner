package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.model.Tour;
import at.fhtw.tourplanner.viewmodel.TourOverviewViewModel;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class TourOverviewController {
    @FXML
    private ListView<Tour> tourListView;
    @FXML
    private Button newTourButton;
    @FXML
    private Button deleteTourButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;

    private final TourOverviewViewModel tourOverviewViewModel;

    public TourOverviewController(TourOverviewViewModel tourOverviewViewModel) {
        this.tourOverviewViewModel = tourOverviewViewModel;
    }

    public TourOverviewController() {
        this.tourOverviewViewModel = new TourOverviewViewModel();
    }

    @FXML
    void initialize() {
        tourListView.setItems(tourOverviewViewModel.getObservableTours());
        tourListView.getSelectionModel().selectedItemProperty().addListener(tourOverviewViewModel.getChangeListener());
        searchTextField.textProperty().bindBidirectional(tourOverviewViewModel.searchTextProperty());
    }

    @FXML
    void onSearchButtonClicked(ActionEvent event) {
        tourOverviewViewModel.search();
    }

    @FXML
    void onNewTourClicked(ActionEvent event) {
        tourOverviewViewModel.addNewTour();

        // Select the newly created tour (will be the last item)
        ObservableList<Tour> items = tourOverviewViewModel.getObservableTours();
        if (!items.isEmpty()) {
            Tour newTour = items.get(items.size() - 1);
            tourListView.getSelectionModel().select(newTour);
        }
    }

    @FXML
    void onDeleteTourClicked(ActionEvent event) {
        Tour selectedTour = tourListView.getSelectionModel().getSelectedItem();
        if (selectedTour != null) {
            tourOverviewViewModel.deleteTour(selectedTour);
        }
    }

    public TourOverviewViewModel getTourOverviewViewModel() {
        return tourOverviewViewModel;
    }

    public void setItems(ObservableList<Tour> tours) {
        tourListView.setItems(tours);
    }

    public void setOnSelectionChanged(TourOverviewViewModel.SelectionChangedListener listener) {
        tourOverviewViewModel.addSelectionChangedListener(listener);
    }

    public void setOnNewItem(Runnable action) {
        newTourButton.setOnAction(event -> action.run());
    }

    public void setOnDeleteItem(Runnable action) {
        deleteTourButton.setOnAction(event -> action.run());
    }
}