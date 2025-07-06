package at.fhtw.tourplanner;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class TourPlannerApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLDependencyInjection.load("MainWindow.fxml", Locale.ENGLISH);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/at/fhtw/tourplanner/view/styles/application.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/at/fhtw/tourplanner/view/icons/ziel1.png")));

        primaryStage.setScene(scene);
        primaryStage.setTitle("Tour Planner");
        primaryStage.show();
    }
}
