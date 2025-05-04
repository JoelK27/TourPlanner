package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.viewmodel.MainWindowViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MainWindowController {
    private final MainWindowViewModel viewModel;

    // These reference the controllers of included FXML files
    @FXML private SearchBarController searchBarController;
    @FXML private TourOverviewController tourOverviewController;
    @FXML private TourDetailsController tourDetailsController;

    public MainWindowController(MainWindowViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public MainWindowController() {
        this.viewModel = new MainWindowViewModel();
    }

    @FXML
    public void initialize() {
        // The sub-controllers are already initialized with their own ViewModels
        // No need to directly access their UI components from here

        // If you need coordination between controllers, you can set up listeners here
        if (searchBarController != null && searchBarController.getSearchBarViewModel() != null) {
            searchBarController.getSearchBarViewModel().addSearchListener(searchString -> {
                viewModel.searchFieldProperty().set(searchString);
                viewModel.updateTourList();
            });
        }

        if (tourOverviewController != null && tourOverviewController.getTourOverviewViewModel() != null) {
            tourOverviewController.getTourOverviewViewModel().addSelectionChangedListener(
                    selectedTour -> {
                        viewModel.selectedTourProperty().set(selectedTour);
                        if (tourDetailsController != null && selectedTour != null) {
                            tourDetailsController.getTourDetailsViewModel().setTourModel(selectedTour);
                        }
                    }
            );
        }
    }

    @FXML
    void onMenuFileQuitClicked(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void onMenuHelpAboutClicked(ActionEvent event) {
        // Show about dialog
    }
}