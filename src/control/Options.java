package control;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Options {
	public static int local = 20;
	public static double brightMult = 1.4;
	public static int method = 0;
	public static double fps = 5;
	
	public static Stage startOptions() {
		BorderPane border = new BorderPane();
		Scene scene = new Scene(border);
        final Stage stage = new Stage();
		
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(15, 12, 15, 12));
		vBox.setSpacing(10);   // Gap between nodes
		
		final Slider localSlider = new Slider();
		localSlider.setMin(10);
		localSlider.setMax(100);
		localSlider.setValue(23);
		localSlider.setShowTickLabels(true);
		localSlider.setShowTickMarks(true);
		localSlider.setMajorTickUnit(5);
		localSlider.setMinorTickCount(0);
		localSlider.setBlockIncrement(5);
		localSlider.setSnapToTicks(true);
		
		Label localLbl = new Label("Local (higher more accurate, slower)");
        
		final Slider bMultSlider = new Slider();
		bMultSlider.setMin(0);
		bMultSlider.setMax(100);
		bMultSlider.setValue(40);
		bMultSlider.setShowTickLabels(true);
		bMultSlider.setShowTickMarks(true);
		bMultSlider.setMajorTickUnit(10);
		bMultSlider.setMinorTickCount(10);
		bMultSlider.setBlockIncrement(10);
		bMultSlider.setSnapToTicks(true);
		
		Label bMultLbl = new Label("Brightness");
		
		final Slider methodSlider = new Slider();
		methodSlider.setMin(1);
		methodSlider.setMax(3);
		methodSlider.setValue(1);
		methodSlider.setShowTickLabels(true);
		methodSlider.setShowTickMarks(true);
		methodSlider.setMajorTickUnit(1);
		methodSlider.setMinorTickCount(0);
		methodSlider.setBlockIncrement(1);
		methodSlider.setSnapToTicks(true);
		
		Label methodLbl = new Label("Method (Mean, Median (no), MinMax)");
		
		final Slider fpsSlider = new Slider();
		fpsSlider.setMin(1);
		fpsSlider.setMax(10);
		fpsSlider.setValue(5);
		fpsSlider.setShowTickLabels(true);
		fpsSlider.setShowTickMarks(true);
		fpsSlider.setMajorTickUnit(1);
		fpsSlider.setMinorTickCount(0);
		fpsSlider.setBlockIncrement(1);
		fpsSlider.setSnapToTicks(true);
		
		Label fpsLabel = new Label("Frames per Second");
		
		Button okBtn = new Button("Ok");
		okBtn.setPrefSize(100, 20);
		okBtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	local = (int)localSlider.getValue();
                    	brightMult = (int) 1+(bMultSlider.getValue() / 100);
                    	method = (int) methodSlider.getValue()-1;
                    	fps = fpsSlider.getValue();
                    	stage.hide();
                    }
        });
		
		vBox.getChildren().addAll(localLbl, localSlider, bMultLbl, bMultSlider, methodLbl, methodSlider, fpsLabel, fpsSlider, okBtn);
		
		border.setTop(vBox);
        
        stage.setScene(scene);
        stage.setTitle("Options");
        stage.show();
		return stage;
	}

}
