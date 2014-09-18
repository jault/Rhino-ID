package control;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import structures.FeatureVector;
import structures.Frame;

public class Trainer {
	public static Frame frame;
	public static ImageView iView;
	public static int animalCount;
	public static FeatureVector featureVector;

	public static Stage startTraining(){
		frame = Launcher.video.nextFrame();
		featureVector = new FeatureVector(true);
		
		BorderPane border = new BorderPane();
		
		HBox hbox = addHBox();
        border.setTop(hbox);
        border.setLeft(addVBox());
		
        Scene scene = new Scene(border);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Eye of the Tiger");
        stage.show();
		return stage;
	}
	
    private static HBox addHBox() {

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);   // Gap between nodes
        //hbox.setStyle("-fx-background-color: #336699;");
        
        Label infoLbl = new Label("Is the indicated area an animal?");
        infoLbl.setPrefSize(200, 20);
        final Label animalCountLbl = new Label("0");
        animalCountLbl.setPrefSize(100, 20);

        Button yesBtn = new Button("Yes");
        yesBtn.setPrefSize(100, 20);
        yesBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Update animal counter, permanently draw region, and go to next region
				animalCount++;
				animalCountLbl.setText(""+animalCount);
				
				iView.setImage(SwingFXUtils.toFXImage(frame.drawRegionPermanent(), null));
				BufferedImage tmp = frame.selectNextRegion();
				if(tmp != null) {
					 iView.setImage(SwingFXUtils.toFXImage(tmp, null));
				}
			}
        });

        Button noBtn = new Button("No");
        noBtn.setPrefSize(100, 20);
        noBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Go to next region
				BufferedImage tmp = frame.selectNextRegion();
				if(tmp != null) {
					 iView.setImage(SwingFXUtils.toFXImage(tmp, null));
				}
			}
        });
        
        Button backBtn = new Button("Back");
        backBtn.setPrefSize(100, 20);
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Update animal counter, redraw previous regions
				BufferedImage tmp = frame.selectLastRegion();
				if(tmp != null) {
					 iView.setImage(SwingFXUtils.toFXImage(tmp, null));
				}
				animalCountLbl.setText(""+animalCount);
			}
        });
        
        final Button nextBtn = new Button("Next Frame");
        nextBtn.setPrefSize(100, 20);
        nextBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if(nextBtn.getText().equals("Close")) {
					featureVector.writeFVToFile();
					System.exit(0);
				}
				if(frame != null) {
					frame.generateFeatures(featureVector);
				}
				
				Frame tmpFrame = Launcher.video.nextFrame();
				if(tmpFrame != null) {
					frame = tmpFrame;
					BufferedImage tmp = tmpFrame.drawInitialRegion();
					if(tmp != null) {
						 iView.setImage(SwingFXUtils.toFXImage(tmp, null));
					}
				} else {
					nextBtn.setText("Close");
				}
			}
        });
        
        hbox.getChildren().addAll(infoLbl, yesBtn, noBtn, backBtn, nextBtn, animalCountLbl);
        
        return hbox;
    }
    
    private static VBox addVBox() {
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10)); // Set all sides to 10
        vbox.setSpacing(8);              // Gap between nodes

//        Image image = null;
//        try {
//			image = new Image(new File("herd.png").toURI().toURL().toString(), 640, 480, false, false);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        iView = new ImageView(SwingFXUtils.toFXImage(frame.drawInitialRegion(), null));
        
        vbox.getChildren().addAll(iView);
        
        return vbox;
    }
}
