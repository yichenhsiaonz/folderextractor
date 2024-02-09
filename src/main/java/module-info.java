module com.folderextractor {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    
    opens com.folderextractor to javafx.fxml;
    opens com.folderextractor.controllers to javafx.fxml;

    exports com.folderextractor.controllers;
    exports com.folderextractor;
}
