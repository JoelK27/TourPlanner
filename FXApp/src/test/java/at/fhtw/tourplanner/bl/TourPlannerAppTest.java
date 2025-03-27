package at.fhtw.tourplanner.bl;

import at.fhtw.tourplanner.TourPlannerApplication;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

public class TourPlannerAppTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new TourPlannerApplication().start(stage);
    }

    @BeforeEach
    public void setUp() {
        // Bereinige die Testumgebung
        while (!lookup("#tourListView").queryListView().getItems().isEmpty()) {
            clickOn("#tourListView").type(KeyCode.END);
            clickOn("#deleteTourButton");
        }
        // Erstelle eine neue Tour
        clickOn("#newTourButton");
    }

    @Test
    public void testCreateNewTour() {
        clickOn("#newTourButton");
        ListView<?> tourListView = lookup("#tourListView").queryListView();
        assertTrue(tourListView.getItems().size() > 0, "The tour list should not be empty after creating a new tour");
    }

    @Test
    public void testDeleteTour() {
        int initialTourCount = lookup("#tourListView").queryListView().getItems().size();
        clickOn("#tourListView").type(KeyCode.END);
        clickOn("#deleteTourButton");
        int finalTourCount = lookup("#tourListView").queryListView().getItems().size();
        assertEquals(initialTourCount - 1, finalTourCount);
    }

    @Test
    public void testUpdateTour() {
        clickOn("#tourListView").type(KeyCode.END);
        clickOn("#nameField").write(" (updated)");
        verifyThat("#nameField", TextInputControlMatchers.hasText("New Tour (updated)"));
        verifyThat("#tourTitleLabel", LabeledMatchers.hasText("Tour: New Tour (updated)"));
    }

    @Test
    public void testCreateNewLog() {
        clickOn("#tourListView").type(KeyCode.END);
        clickOn("Tour logs");
        clickOn("+");
        ListView<?> logListView = lookup("#logListView").queryListView();
        assertTrue(logListView.getItems().size() > 0, "A log entry should be created");
    }

    @Test
    public void testDeleteLog() {
        clickOn("#tourListView").type(KeyCode.END);
        clickOn("Tour logs");
        clickOn("+");
        ListView<?> logListView = lookup("#logListView").queryListView();
        int initialCount = logListView.getItems().size();
        clickOn("#logListView");
        clickOn("-");
        assertEquals(initialCount - 1, logListView.getItems().size(), "Log should be deleted");
    }

    @Test
    public void testUpdateLog() {
        clickOn("#tourListView").type(KeyCode.END);
        clickOn("Tour logs");
        clickOn("+");

        ListView<?> logListView = lookup("#logListView").queryListView();
        assertTrue(logListView.getItems().size() > 0, "A log entry should be created");
        clickOn("#logListView").type(KeyCode.END);

        clickOn("#logCommentArea").eraseText(9).write("Updated test comment");
        clickOn("Save Changes");

        logListView = lookup("#logListView").queryListView();
        clickOn("#logListView").type(KeyCode.END);
        verifyThat("#logCommentArea", TextInputControlMatchers.hasText("Updated test comment"));
    }

    @Test
    public void testSearchFunction() {
        clickOn("#nameField").eraseText(9).write("SearchTest");
        clickOn("Tour logs");
        clickOn("+");
        clickOn("#logCommentArea").eraseText(9).write("UniqueSearchTerm");
        clickOn("Save Changes");
        clickOn("Tour details");
        clickOn("#searchTextField").write("UniqueSearchTerm");
        clickOn("#searchButton");
        ListView<?> tourListView = lookup("#tourListView").queryListView();
        assertEquals(1, tourListView.getItems().size(), "One tour should be found");
    }

}