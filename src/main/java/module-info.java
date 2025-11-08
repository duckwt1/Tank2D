module com.tank2d.tank2d {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    requires org.controlsfx.controls;

    opens com.tank2d.client.ui to javafx.fxml;
    opens com.tank2d.masterserver.ui to javafx.fxml;

    exports com.tank2d.client;
    exports com.tank2d.client.core;
    exports com.tank2d.client.ui;
    exports com.tank2d.shared;
    exports com.tank2d.masterserver;
    exports com.tank2d.masterserver.core;
    exports com.tank2d.masterserver.db;
    exports com.tank2d.masterserver.ui;
    exports com.tank2d.masterserver.core.room;
}