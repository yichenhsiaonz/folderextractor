module com.folderextractor {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.folderextractor to javafx.fxml;
    opens com.folderextractor.controllers to javafx.fxml;
    exports com.folderextractor.controllers;
    exports com.folderextractor;
}
