package com.folderextractor.controllers;

import java.io.File;
import java.nio.file.Files;

import javafx.stage.DirectoryChooser;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ExtractWindowController {

    private static HashMap<Integer, String> fileNames = new HashMap<>();
    private static List<String> extensions = new ArrayList<>();

    @FXML private Button scanButton;
    @FXML private Button targetFolderButton;
    @FXML private Button destinationFolderButton;
    @FXML private TextArea console;
    @FXML private TextArea list;
    @FXML private TextArea extensionlist;
    @FXML private TextField targetPathTextField;
    @FXML private TextField destinationPathTextField;
    @FXML private CheckBox copyCheckBox;

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
        String targetPathString = targetPathTextField.getText();
        String destinationPathString = destinationPathTextField.getText();
        disableAllButtons();
        console.appendText("Extracting: "+ targetPathString + "\n");
        fileNames.clear();
        list.clear();
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
                File targetPath = new File(targetPathString);
                if(!targetPath.exists()){
                    Platform.runLater(() -> {
                        console.appendText("Path does not exist\n\n");
                        enableAllButtons();
                    });
                    return null;
                } else if(!targetPath.isDirectory()){
                    Platform.runLater(() -> {
                        console.appendText("Path is not a directory\n\n");
                        enableAllButtons();
                    });
                    return null;
                } else {
                    Platform.runLater(() -> {
                        console.appendText("Extracting files...\n");
                    });
                File destinationPath = new File(destinationPathString);
                if(!destinationPath.exists()){

                    boolean success = destinationPath.mkdirs();
                    if(!success){
                        Platform.runLater(() -> {
                            console.appendText("Failed to create destination directory\n\n");
                            enableAllButtons();
                        });
                        return null;
                    }
                } else if(destinationPath.isFile()){
                    Platform.runLater(() -> {
                        console.appendText("Destination is a file\n\n");
                        enableAllButtons();
                    });
                    return null;
                } else {
                    scanDestination(destinationPath);
                    for(String i: fileNames.values()){
                        System.out.println(i);
                    }
                } 
                // get all files in the directory
                if(copyCheckBox.isSelected()){
                    recursiveFileCopier(targetPath, destinationPath);
                } else {
                    recursiveFileExtractor(targetPath, destinationPath);
                }     
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
    private void onDestinationFolderButtonClicked(){
        pathPicker(destinationPathTextField);    
    }

    @FXML
    private void onTargetFolderButtonClicked(){
        pathPicker(targetPathTextField);
    }

    private void pathPicker(TextField textField){
        //open folder dialogue 
        DirectoryChooser directoryChooser = new DirectoryChooser();
        // set initial directory to the path in the text field if it exists, otherwise set it to the user's desktop
        File selectedDirectory = new File(textField.getText());
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
            textField.setText(selectedDirectory.getAbsolutePath());
            console.appendText("Selected: " + selectedDirectory.getAbsolutePath() + "\n\n");
        }
    }

    private void disableAllButtons(){
        scanButton.setDisable(true);
        targetFolderButton.setDisable(true);
        destinationFolderButton.setDisable(true);
    }

    private void enableAllButtons(){
        scanButton.setDisable(false);
        targetFolderButton.setDisable(false);
        destinationFolderButton.setDisable(false);
    }

    private void scanDestination(File file){
        for(File f: file.listFiles()){
            String fileName = f.getName().toLowerCase();
            fileNames.put(fileName.hashCode(), fileName);
        }
    }

    private String[] getFilenameAndExtension(File file){
        String originalFileName = file.getName().toLowerCase();
        int index = originalFileName.lastIndexOf(".");
        String fileName;
        String extension;
        String separator;
        if(index == -1) {
            System.out.println("No extension");
            fileName = originalFileName.toLowerCase();
            extension = "";
            separator = "";
        } else {
            fileName = originalFileName.substring(0, index).toLowerCase();
            extension = originalFileName.substring(index+1).toLowerCase();
            separator = ".";
        }
        String[] result = {fileName, extension, separator};
        return result;
    }

    private String getUniqueName(String[] name){
        String fileName = name[0];
        String extension = name[1];
        String separator = name[2];
        String newFileName = fileName + separator + extension;
        int hash = newFileName.hashCode();

        int counter = 0;
        while(fileNames.containsKey(hash)) {
            newFileName = fileName + " (" + counter + ")" + separator + extension;
            hash = newFileName.hashCode();
            counter++;
        }
        fileNames.put(hash, newFileName);

        return newFileName;
    } 

    private boolean isExtensionValid(String extension){
        if(useExtensionfilter){
            return extensions.contains(extension);
        }
        return true;
    }
    
    private void recursiveFileCopier (File path, File destination){
        for(File f: path.listFiles()){
            if(f.isDirectory()){
                recursiveFileCopier(f, destination);
            } else {

                String[] originalFileName = getFilenameAndExtension(f);
                if(isExtensionValid(originalFileName[1])){
                    String newFileName = getUniqueName(originalFileName);
                    try {
                        Files.copy(f.toPath(), new File(destination.getAbsolutePath() + File.separator + newFileName).toPath(), 
                        java.nio.file.StandardCopyOption.COPY_ATTRIBUTES, 
                        java.nio.file.LinkOption.NOFOLLOW_LINKS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String finalFileName = newFileName;
                    Platform.runLater(() -> {
                        console.appendText("Renamed: " + originalFileName[0] + originalFileName[2] + originalFileName[1] + " to " + finalFileName + "\n");
                        list.appendText(finalFileName + "\n");
                    });
                } else {
                    Platform.runLater(() -> {
                        console.appendText("Skipped: " + originalFileName[0] + originalFileName[2] + originalFileName[1] + "\n");
                    });
                }
            }
        }
    }

    private void recursiveFileExtractor(File path, File destination){
        for(File f: path.listFiles()){
            if(f.isDirectory()){
                recursiveFileExtractor(f, destination);
            } else {
                String[] originalFileName = getFilenameAndExtension(f);
                if(isExtensionValid(originalFileName[1])){
                    String newFileName = getUniqueName(originalFileName);
                    f.renameTo(new File(destination.getAbsolutePath() + File.separator + newFileName));
                    String finalFileName = newFileName;
                    Platform.runLater(() -> {
                        console.appendText("Renamed: " + originalFileName[0] + originalFileName[2] + originalFileName[1] + " to " + finalFileName + "\n");
                        list.appendText(finalFileName + "\n");
                    });
                } else {
                    Platform.runLater(() -> {
                        console.appendText("Skipped: " + originalFileName[0] + originalFileName[2] + originalFileName[1] + "\n");
                    });
                }
            }
        }
    }
}
