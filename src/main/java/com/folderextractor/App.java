package com.folderextractor;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;


public class App extends Application{

    private static Scene scene;

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
      }

      public static void main(String[] args) throws IOException{
        try {
            launch();
        }
        catch(Throwable t) {
            for(; t != null; t = t.getCause()) {
                System.err.println(t);
                for(StackTraceElement e: t.getStackTrace())
                    System.err.println("\tat "+e);
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
      // load fxml file
      scene = new Scene(loadFXML("extractionwindow"));
      stage.setMinWidth(800);
      stage.setMinHeight(600);
      stage.setTitle("Folder Extractor");
      stage.setScene(scene);
      stage.show();
    }
}
