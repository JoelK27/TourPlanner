package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.apiclient.TourApiService;
import at.fhtw.tourplanner.viewmodel.MainWindowViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

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
                        if (tourDetailsController != null) {
                            System.out.println("Setting tour: " + (selectedTour != null ? selectedTour.getName() : "null")); // Debug
                            tourDetailsController.setTour(selectedTour); // <-- Wichtig!
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

    @FXML
    void onMenuFileImportClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            TourApiService.getInstance().importToursFromFile(file);
            showAlert("Import successful", "The tours have been imported successfully.");
        }
    }

    @FXML
    void onMenuFileExportClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            TourApiService.getInstance().exportToursToFile(file);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}