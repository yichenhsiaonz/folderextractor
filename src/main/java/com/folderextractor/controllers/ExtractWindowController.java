package com.folderextractor.controllers;

import java.io.File;

import javafx.stage.DirectoryChooser;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ExtractWindowController {

    private static HashMap<Integer, String> fileNames = new HashMap<>();
    private static List<String> extensions = new ArrayList<>();

    @FXML private Button scanButton;
    @FXML private Button folderButton;
    @FXML private TextArea console;
    @FXML private TextArea list;
    @FXML private TextArea extensionlist;
    @FXML private TextField pathTextField;

    private String userDesktopPath;
    private boolean useExtensionfilter;

    @FXML
    private void initialize(){
        // set text areas to read only
        console.setEditable(false);
        list.setEditable(false);

        // set default path
        userDesktopPath = System.getProperty("user.home") + File.separator +"Desktop";
    }

    @FXML
    private void onScanButton(){
        String pathString = pathTextField.getText();
        disableAllButtons();
        console.appendText("Extracting: "+ pathString + "\n");
        fileNames.clear();
        extensions.clear();
        useExtensionfilter = false;
        
        if(extensionlist.getText().length() > 0){
            useExtensionfilter = true;
            extensions = Arrays.asList(extensionlist.getText().split("\n"));
            for(String i: extensions){
                i = i.toLowerCase();
                System.out.println(i);
            }
        }
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File path = new File(pathString);
                if(!path.exists()){
                    Platform.runLater(() -> {
                        console.appendText("Path does not exist\n\n");
                        enableAllButtons();
                    });
                    return null;
                } else if(!path.isDirectory()){
                    Platform.runLater(() -> {
                        console.appendText("Path is not a directory\n\n");
                        enableAllButtons();
                    });
                    return null;
                } else {
                    Platform.runLater(() -> {
                        console.appendText("Extracting files...\n");
                    });
                // get all files in the directory
                recursiveFileFinder(path, path);
                return null;
                }
            }
        };
        task.setOnSucceeded(e -> {
            console.appendText("\nExtraction complete\n\n");
            enableAllButtons();
        });
        new Thread(task).start();
    }

    @FXML
    private void onFolderButtonClicked(){
        //open folder dialogue 
        DirectoryChooser directoryChooser = new DirectoryChooser();
        // set initial directory to the path in the text field if it exists, otherwise set it to the user's desktop
        File selectedDirectory = new File(pathTextField.getText());
        if(selectedDirectory.exists()){
            directoryChooser.setInitialDirectory(selectedDirectory);
        } else {
            directoryChooser.setInitialDirectory(new File(userDesktopPath));
        }
        directoryChooser.setTitle("Select Folder");
        selectedDirectory = directoryChooser.showDialog(null);
        // if no directory is selected, do nothing
        if(selectedDirectory == null){
            console.appendText("No Directory selected\n\n");
        // if a directory is selected, set the text field to the path of the selected directory
        } else {
            pathTextField.setText(selectedDirectory.getAbsolutePath());
            console.appendText("Selected: " + selectedDirectory.getAbsolutePath() + "\n\n");
        }
        
    }

    private void disableAllButtons(){
        scanButton.setDisable(true);
        folderButton.setDisable(true);
    }

    private void enableAllButtons(){
        scanButton.setDisable(false);
        folderButton.setDisable(false);
    }

    private void recursiveFileFinder(File path, File root){
        for(File f: path.listFiles()){
            if(f.isDirectory()){
                recursiveFileFinder(f, root);
            } else {
                String originalFileName = f.getName();
                int index = originalFileName.lastIndexOf(".");

                String fileName;
                String extension;
                if(index == -1) {
                    fileName = originalFileName;
                    extension = "";
                } else {
                    fileName = originalFileName.substring(0, index).toLowerCase();
                    extension = originalFileName.substring(index+1).toLowerCase();
                }
                System.out.println(extension);
                if((useExtensionfilter && extensions.contains(extension)) || !useExtensionfilter){
                    Platform.runLater(() -> {
                        console.appendText("Extracting: " + f.getAbsolutePath() + "\n");
                    });
                    String newFileName = originalFileName;
                System.out.println(newFileName);
                int hash = newFileName.hashCode();

                int counter = 0;
                while(fileNames.containsKey(hash)) {
                    newFileName = fileName + " (" + counter + ")." + extension;
                    System.out.println(newFileName);
                    hash = newFileName.hashCode();
                    counter++;
                }
                fileNames.put(hash, newFileName);
                f.renameTo(new File(root.getAbsolutePath() + File.separator + newFileName));
                String finalFileName = newFileName;
                Platform.runLater(() -> {
                    console.appendText("Renamed: " + originalFileName + " to " + finalFileName + "\n");
                    list.appendText(finalFileName + "\n");
                });
                }
            }
        } 
    }
}
