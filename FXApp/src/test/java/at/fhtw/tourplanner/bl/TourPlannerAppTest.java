package at.fhtw.tourplanner.bl;

import at.fhtw.tourplanner.TourPlannerApplication;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }


    @Test
    public void testCreateNewLog() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Click on the "+" button to add a new log
        clickOn("#logTableView").type(KeyCode.PLUS);

        // Verify that the new log is created
        verifyThat("#logTableView", TableViewMatchers.hasNumRows(1));
    }


    @Test
    public void testDeleteLog() {
        // Select the newly created tour (will be the last item)
        clickOn("#tourListView").type(KeyCode.END);

        // Click on the "+" button to add a new log
        clickOn("#logTableView").type(KeyCode.PLUS);

        // Select the first log in the table
        clickOn("#logTableView").type(KeyCode.DOWN);

        // Click on the "-" button to delete the log
        clickOn("#logTableView").type(KeyCode.MINUS);

        // Verify that the log is deleted
        verifyThat("#logTableView", TableViewMatchers.hasNumRows(0));
    }
}