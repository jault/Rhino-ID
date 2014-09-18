package structures;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import control.Trainer;
import control.RegionLabeler;

public class Frame {
	private ImageProcessor imgPro;
	private ImageProcessor drawPro;
	private ArrayList<Region> regions;
	private int currentRegion;
	
	public Frame(BufferedImage frame) {
		imgPro = new ImagePlus("frameType", frame).getProcessor();
		drawPro = imgPro.duplicate();
		regions = RegionLabeler.getRegions(imgPro);
		currentRegion = 0;
	}
	
	public BufferedImage drawInitialRegion() {
		ImageProcessor tempPro = drawPro.duplicate();
		if(regions.size() == 0) {
			System.out.println("regions is 0");
			return null;
		}
		regions.get(currentRegion).drawRegionBox(tempPro, Integer.MAX_VALUE/200);
		return tempPro.getBufferedImage();
	}
	
	public BufferedImage selectNextRegion() {
		if(currentRegion+1 < regions.size()) {
			currentRegion++;
			ImageProcessor tempPro = drawPro.duplicate();
			regions.get(currentRegion).drawRegionBox(tempPro, Integer.MAX_VALUE/200);
			//Interface.imgLbl.setIcon(new ImageIcon(tempPro.getBufferedImage()));
			return tempPro.getBufferedImage();
		}
		return null;
	}
	
	public BufferedImage drawRegionPermanent() {
		Region region = regions.get(currentRegion);
		region.markAnimal();
		region.drawRegionBox(drawPro, 6100000);
		//Interface.imgLbl.setIcon(new ImageIcon(drawPro.getBufferedImage()));
		return drawPro.getBufferedImage();
	}
	
	public BufferedImage selectLastRegion() {
		if(currentRegion-1 >= 0) {
			currentRegion--;
			Region region = regions.get(currentRegion);
			if(region.isAnimal()) {
				region.unmarkAnimal();
				Trainer.animalCount--;
			}
			redrawRegions();
			currentRegion--;
			
			return selectNextRegion();
		}
		return null;
	}
	
	private void redrawRegions() {
		drawPro = imgPro.duplicate();
		for(int i = 0; i < currentRegion; i++) {
			Region region = regions.get(i);
			if(region.isAnimal()) {
				region.drawRegionBox(drawPro, 6100000);
			}
		}
	}
	
	public void generateFeatures(FeatureVector featureVector) {
		for(int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);
			Features regionFeatures = new Features(region, imgPro);
			if(region.isAnimal()) {
				featureVector.addFeature("animal", regionFeatures);
			} else {
				featureVector.addFeature("nonanimal", regionFeatures);
			}
		}
	}
	
	public ArrayList<Region> classifyRegions(FeatureVector featureVector) {
		for(int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);
			Features regionFeatures = new Features(region, imgPro);
			String type = featureVector.classifyKNN(regionFeatures, 5);
			if(type.equalsIgnoreCase("animal")) {
				region.markAnimal();
			}
		}
		return regions;
	}
}
