package control;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import structures.FeatureVector;
import structures.Frame;
import structures.Region;

public class Analysis {
	static Frame frame;
	static FeatureVector featureVector;
	static int numAnimals;
	static int index;
	
	public static Stage startAnalysis(){
		featureVector = new FeatureVector(false);
		final ArrayList<BufferedImage> animalBImg = doAnalysis();
		
		BorderPane border = new BorderPane();
		
		HBox topBox = new HBox();
        topBox.setPadding(new Insets(15, 12, 15, 12));
        topBox.setSpacing(10);   // Gap between nodes
        
        final Label selectedLbl = new Label("Animals found: " + numAnimals);
        
        final ImageView iView = new ImageView();
        BufferedImage bfI = animalBImg.get(index);
        Image im = SwingFXUtils.toFXImage(bfI, null);
        iView.setImage(im);
        
        Button nextAnimalBtn = new Button("Next Animal");
        nextAnimalBtn.setPrefSize(150, 20);
        nextAnimalBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                   		index++;
                   		if(index < animalBImg.size()) {
                   			iView.setImage(SwingFXUtils.toFXImage(animalBImg.get(index),  null));
                   		}
                    }
        });
        
        topBox.getChildren().addAll(selectedLbl, nextAnimalBtn);
        
        HBox midBox = new HBox();
        midBox.setPadding(new Insets(15, 12, 15, 12));
        midBox.setSpacing(10);   // Gap between nodes
        
        midBox.getChildren().addAll(iView);
        
        border.setTop(topBox);
        border.setCenter(midBox);
		
        Scene scene = new Scene(border);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("T1000");
        stage.show();
		return stage;
	}
	
	public static ArrayList<BufferedImage> doAnalysis() {
		ArrayList<BufferedImage> animalsBImg = new ArrayList<BufferedImage>();
		frame = Launcher.video.nextFrame();
		while(frame != null) {
			ArrayList<Region> regions = frame.classifyRegions(featureVector);
			for(int i = 0; i < regions.size(); i++) {
				Region region = regions.get(i);
				if(region.isAnimal()) {
					animalsBImg.add(region.getIRBox().getBufferedImage());
					numAnimals++;
				}
			}
			
			frame = Launcher.video.nextFrame();
		}
		return animalsBImg;
	}
}
