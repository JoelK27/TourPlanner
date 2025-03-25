package at.fhtw.tourplanner;

import at.fhtw.tourplanner.view.ControllerFactory;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * FXMLLoader with Dependency-Injection
 * based on https://edencoding.com/dependency-injection/
 */
public class FXMLDependencyInjection {
    public static Parent load(String location, Locale locale) throws IOException {
        FXMLLoader loader = getLoader(location, locale);
        return loader.load();
    }

    public static FXMLLoader getLoader(String location, Locale locale) {
        return new FXMLLoader(
                FXMLDependencyInjection.class.getResource("/at/fhtw/tourplanner/view/" + location),
                ResourceBundle.getBundle("at/fhtw/tourplanner/view/gui_strings", locale),
                new JavaFXBuilderFactory(),
                controllerClass-> ControllerFactory.getInstance().create(controllerClass)
        );
    }
}