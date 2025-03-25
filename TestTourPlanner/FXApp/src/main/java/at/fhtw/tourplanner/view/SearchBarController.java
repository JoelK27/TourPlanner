package at.fhtw.tourplanner.view;

import at.fhtw.tourplanner.viewmodel.SearchBarViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class SearchBarController {
    @FXML
    private Button searchButton;
    @FXML
    private TextField searchTextField;

    private SearchBarViewModel searchBarViewModel;

    // Empty constructor for FXML loader
    public SearchBarController() {
        this.searchBarViewModel = new SearchBarViewModel();
    }

    // Constructor with viewModel for ControllerFactory
    public SearchBarController(SearchBarViewModel searchBarViewModel) {
        this.searchBarViewModel = searchBarViewModel;
    }

    public SearchBarViewModel getSearchBarViewModel() {
        return searchBarViewModel;
    }

    @FXML
    void initialize() {
        searchTextField.textProperty().bindBidirectional(searchBarViewModel.searchStringProperty());
        searchButton.disableProperty().bind(searchBarViewModel.searchDisabledBinding());
    }

    @FXML
    public void onSearchButton(ActionEvent actionEvent) {
        System.out.println("Search button pressed with text: " + searchTextField.getText());
        searchBarViewModel.doSearch();
    }
}