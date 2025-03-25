module tourplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires static lombok;
    requires java.sql;

    opens at.fhtw.tourplanner.view to javafx.graphics, javafx.fxml;
    exports at.fhtw.tourplanner;
    exports at.fhtw.tourplanner.viewmodel;
    exports at.fhtw.tourplanner.view;
    exports at.fhtw.tourplanner.model;
}