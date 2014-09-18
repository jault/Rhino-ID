package control;

import java.io.File;

import structures.Video;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Launcher extends Application {
	private String filePath;
	public static Video video;
	
	public static void main(String[] args) {
		launch(Launcher.class, args);
	}

	
	 
    @Override
    public void start(final Stage stage) {
        final FileChooser fileChooser = new FileChooser();
 
		BorderPane border = new BorderPane();
		
        HBox topBox = new HBox();
        topBox.setPadding(new Insets(15, 12, 15, 12));
        topBox.setSpacing(10);   // Gap between nodes
        
        final Label selectedLbl = new Label("No video selected");
        selectedLbl.setPrefSize(300, 20);

        Button openButton = new Button("Select Video");
        openButton.setPrefSize(100, 20);
        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(stage);
                        if (file != null) {
                            filePath = file.toString();
                            selectedLbl.setText(filePath);
                        }
                    }
        });
        
        topBox.getChildren().addAll(openButton, selectedLbl);
        
        HBox midBox = new HBox();
        midBox.setPadding(new Insets(15, 12, 15, 12));
        midBox.setSpacing(10);   // Gap between nodes
        
        final Label processLbl = new Label("Press process after selecting a video, this will take some time.");
        Button processBtn = new Button("Process Video");
        processBtn.setPrefSize(150, 20);
        processBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	if(filePath == null) {
                    		// Do nothing
                    	} else if (supportsFormat()) {
                        	processLbl.setText("Processing video...");
                        	video = new Video(filePath);
                        	processLbl.setText("Processing complete, please select a mode.");
                        } else {
                        	processLbl.setText("The video format is not supported.");
                        }
                    }
        });
        
        midBox.getChildren().addAll(processBtn, processLbl);
        
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(15, 12, 15, 12));
        bottomBox.setSpacing(10);   // Gap between nodes
        
        Button trainBtn = new Button("Training Mode");
        trainBtn.setPrefSize(150, 20);
        trainBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	if(video != null) {
                    		Trainer.startTraining().show();
                    		stage.hide();
                    	}
                    }
        });
        
        Button analysisBtn = new Button("Auto-Analysis Mode");
        analysisBtn.setPrefSize(150, 20);
        analysisBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	if(video != null) {
                    		Analysis.startAnalysis().show();
                    		stage.hide();
                    	}
                    }
        });
        
        Button optionsBtn = new Button("Options");
        optionsBtn.setPrefSize(150, 20);
        optionsBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                   		Options.startOptions().show();
                    }
        });
        
        bottomBox.getChildren().addAll(trainBtn, analysisBtn, optionsBtn);
        
        border.setTop(topBox);
        border.setLeft(midBox);
        border.setBottom(bottomBox);
		
        Scene scene = new Scene(border);
        stage.setScene(scene);
        stage.setTitle("Rhino Finda");
        stage.setResizable(false);
        stage.show();
    }
    
    private boolean supportsFormat() {
    	if(filePath.toLowerCase().contains(".3gp")) return true;
    	if(filePath.toLowerCase().contains(".asf")) return true;
    	if(filePath.toLowerCase().contains(".avi")) return true;
    	if(filePath.toLowerCase().contains(".mov")) return true;
    	if(filePath.toLowerCase().contains(".mp4")) return true;
    	if(filePath.toLowerCase().contains(".m4v")) return true;
    	if(filePath.toLowerCase().contains(".rm")) return true;
    	if(filePath.toLowerCase().contains(".swf")) return true;
    	if(filePath.toLowerCase().contains(".flv")) return true;
    	return false;
    }
    
}
