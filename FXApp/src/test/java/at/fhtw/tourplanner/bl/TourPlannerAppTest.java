package at.fhtw.tourplanner.bl;

import at.fhtw.tourplanner.TourPlannerApplication;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
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
        // Create a new tour before each test
        clickOn("#newTourButton");
    }

    @Test
    public void testCreateNewTour() {
        // Verify that the new tour is created and selected
        verifyThat("#nameField", TextInputControlMatchers.hasText("New Tour"));
        verifyThat("#tourTitleLabel", LabeledMatchers.hasText("Tour: New Tour"));
    }

    @Test
    public void testDeleteTour() {
        // Count the number of tours before deletion
        int initialTourCount = lookup("#tourListView").queryListView().getItems().size();

        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Click on the "Delete" button
        clickOn("#deleteTourButton");

        // Count the number of tours after deletion
        int finalTourCount = lookup("#tourListView").queryListView().getItems().size();

        // Verify that the tour count has decreased by one
        assertEquals(initialTourCount - 1, finalTourCount);
    }

    @Test
    public void testUpdateTour() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Update the tour name
        clickOn("#nameField").write(" (updated)");

        // Verify that the tour name is updated
        verifyThat("#nameField", TextInputControlMatchers.hasText("New Tour (updated)"));
        verifyThat("#tourTitleLabel", LabeledMatchers.hasText("Tour: New Tour (updated)"));
    }

    @Test
    public void testCreateNewLog() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Navigate to Tour logs tab
        clickOn("Tour logs");

        // Click on the "+" button to add a new log
        clickOn("+");

        // Verify that the new log is created
        ListView<?> logListView = lookup("#logListView").queryListView();
        assertTrue(logListView.getItems().size() > 0, "A log entry should be created");
    }

    @Test
    public void testDeleteLog() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Navigate to Tour logs tab
        clickOn("Tour logs");

        // Click on the "+" button to add a new log
        clickOn("+");

        // Count logs before deletion
        ListView<?> logListView = lookup("#logListView").queryListView();
        int initialCount = logListView.getItems().size();

        // Select the log in the list
        clickOn("#logListView");

        // Click on the "-" button to delete the log
        clickOn("-");

        // Verify that the log is deleted
        assertEquals(initialCount - 1, logListView.getItems().size(), "Log should be deleted");
    }

    @Test
    public void testUpdateLog() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Navigate to Tour logs tab
        clickOn("Tour logs");

        // Click on the "+" button to add a new log
        clickOn("+");

        // Update the log comment
        clickOn("#logCommentArea").eraseText(9).write("Updated test comment");

        // Click the save button
        clickOn("Save Changes");

        // Verify the update persisted
        verifyThat("#logCommentArea", TextInputControlMatchers.hasText("Updated test comment"));
    }

    @Test
    public void testSearchFunction() {
        // Create a tour with a specific name
        clickOn("#nameField").eraseText(9).write("SearchTest");

        // Create a log with specific content
        clickOn("Tour logs");
        clickOn("+");
        clickOn("#logCommentArea").write("UniqueSearchTerm");
        clickOn("Save Changes");

        // Go back to overview and search
        clickOn("Tour details");
        clickOn("#searchTextField").write("UniqueSearchTerm");
        clickOn("#searchButton");

        // Verify the tour with the matching log is found
        ListView<?> tourListView = lookup("#tourListView").queryListView();
        assertEquals(1, tourListView.getItems().size(), "One tour should be found");
    }
}