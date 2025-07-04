module tourplanner {
    requires javafx.fxml;
    requires static lombok;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires javafx.web;
    requires java.sql;
    requires org.apache.logging.log4j;

    opens at.fhtw.tourplanner.view to javafx.graphics, javafx.fxml;
    exports at.fhtw.tourplanner;
    exports at.fhtw.tourplanner.viewmodel;
    exports at.fhtw.tourplanner.view;
    exports at.fhtw.tourplanner.model;
}