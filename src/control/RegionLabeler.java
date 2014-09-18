package control;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import structures.Region;

public class RegionLabeler {
	public static final int BLACK = 0; // Value of black
	public static final int WHITE = 65535; // Value of white
	private static final int BACK = WHITE; // Color of background
		
	/**
	 * Method to draw regions on an image.
	 * @param imgPro Image to draw regions on.
	 * @param regions List of regions to draw.
	 * @return Image with regions drawn on.
	 */
	public ImageProcessor drawRegions(ImageProcessor imgPro, ArrayList<Region> regions) {
		imgPro = imgPro.convertToRGB();
		for(int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);
			LinkedList<Point> pts = region.getRegionPoints();
			int color = 0;
			if(region.getLabel() % 2 == 0) {
				color = Integer.MIN_VALUE+9999999/regions.size()/2*(region.getLabel()+1);
			} else {
				color = Integer.MAX_VALUE-9999999/regions.size()/2*(region.getLabel()+1);
			}
			for(int j = 0; j < pts.size(); j++) {
				Point pt = pts.get(j);
				imgPro.putPixel(pt.x, pt.y, color);
			}
		}
		return imgPro;
	}
	

	
	/**
	 * @param pixel Pixel to be checked.
	 * @param height Height of image.
	 * @param width Width of image.
	 * @param x Horizontal position of pixel.
	 * @param y Vertical position of pixel.
	 * @return True if pixel is out of bounds or background value.
	 */
	private static boolean isBackground(int pixel, int height, int width, int x, int y) {
		if(x < 0 || x > width) return true;
		if(y < 0 || y > height) return true;
		if(pixel == BACK) return true;
		return false;
	}
	
	/**
	 * @param pixel Pixel to be checked.
	 * @return True if pixel is labeled already.
	 */
	private static boolean isLabeled(int pixel) {
		if(pixel != BLACK && pixel != WHITE) return true;
		return false;
	}
	
	/**
	 * @param imgPro Image to be labeled, must be binary.
	 * @param lowerBound Regions with an area smaller than this are omitted.
	 * @param upperBound Regions with an area larger than this are omitted.
	 * @return Returns a list of Region with area between lowerBound and upperBound.
	 */
	private static ArrayList<Region> sequentialLabeling(ImageProcessor imgPro, int lowerBound, int upperBound) {
		int label = 1;
		ArrayList<Region> regions = new ArrayList<Region>();
		Map<String, Point> collisions = new HashMap<String, Point>();
		int height = imgPro.getHeight();
		int width = imgPro.getWidth();
		
		// Skip first index because 0 is reserved for image values
		regions.add(null);
		
		// Initial labeling
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				if(!isBackground(imgPro.getPixel(x, y), height, width, x, y)) {
					// Using 4-connected neighbors
					int leftNeighbor = imgPro.getPixel(x-1, y);
					int topNeighbor = imgPro.getPixel(x, y-1);
					if(isBackground(leftNeighbor, height, width, x-1, y) && isBackground(topNeighbor, height, width, x, y-1)) { // Neighbors are background
						imgPro.putPixel(x, y, label);
						Region newRegion = new Region(label);
						newRegion.addPoint(x, y);
						regions.add(label, newRegion);
						label++;
					} else {
						boolean leftLabeled = isLabeled(leftNeighbor);
						boolean topLabeled = isLabeled(topNeighbor);
					
						if(leftLabeled && !topLabeled) { // Only left neighbor is labeled
							imgPro.putPixel(x, y, leftNeighbor);
							regions.get(leftNeighbor).addPoint(x, y);
						} else if(!leftLabeled && topLabeled) { // Only top neighbor is labeled
							imgPro.putPixel(x, y, topNeighbor);
							regions.get(topNeighbor).addPoint(x, y);
						} else if(leftLabeled && topLabeled && leftNeighbor != topNeighbor) { // Both neighbors are labeled
							int lessLbl, greatLbl;
							if(topNeighbor < leftNeighbor) {
								lessLbl = topNeighbor;
								greatLbl = leftNeighbor;
							} else {
								lessLbl = leftNeighbor;
								greatLbl = topNeighbor;
							}
							imgPro.putPixel(x, y, lessLbl);
							regions.get(lessLbl).addPoint(x, y);
							
							// Collision detected
							Point collision = new Point(lessLbl, greatLbl);
							
							collisions.put(""+lessLbl+""+greatLbl, collision);
						} else {
								imgPro.putPixel(x, y, topNeighbor);
								regions.get(topNeighbor).addPoint(x, y);
						}
					}
				}
				
			}
		}
		
		
		// Resolve collision labels
		// labelTable is used to lookup where the list for each label is held
		Map<Integer, LinkedList<Integer>> labelTable = new HashMap<Integer, LinkedList<Integer>>();
		@SuppressWarnings("unchecked")
		LinkedList<Integer>[] labelList = new LinkedList[label];
		
		// Initialize tables for collision resolution
		for(int i = 1; i < label; i++) { // Start at 1, no region 0
			LinkedList<Integer> myList = new LinkedList<Integer>();
			myList.add(i);
			labelList[i] = myList;
			labelTable.put(i, myList);
		}
		
		// Find equivalent labels
		Iterator<String> keys = collisions.keySet().iterator();
		for(int i = 1; i < collisions.keySet().size(); i++) { // Start at 1, no region 0
			Point collision = collisions.get(keys.next());
			LinkedList<Integer> aLabel = labelTable.get(collision.x);
			LinkedList<Integer> bLabel = labelTable.get(collision.y);
			if(!aLabel.equals(bLabel)) {
				aLabel.addAll(bLabel);
				Iterator<Integer> it = aLabel.iterator();
				while(it.hasNext()){
					labelTable.put(it.next(), aLabel);
				}
				bLabel.clear();
			}
		}
		
		// Combine equivalent regions
		for(int i = 1; i < labelList.length; i++) { // Start at 1, no region 0
			if(labelList[i].size() > 0) {
				LinkedList<Integer> myLabels = labelList[i];
				Iterator<Integer> it = myLabels.iterator();
				Region region = regions.get(it.next());
				while(it.hasNext()) {
					Region subRegion = regions.get(it.next());
					region.addRegion(subRegion);
					subRegion.destroyRegion();
				}
			}
		}
		
		// Reduce to minimal regions
		ArrayList<Region> minimalRegions = new ArrayList<Region>();
		for(int i = 1, j = 0; i < regions.size(); i++) {
			Region region = regions.get(i);
			if(region.getRegionPoints().size() > lowerBound && region.getRegionPoints().size() < upperBound) {
				region.changeLabel(j);
				minimalRegions.add(j, region);
				j++;
			}
		}
		
		return minimalRegions;
	}

	/**
	 * @param imgPro Image to collect regions from.
	 * @return Regions of image.
	 */
	public static ArrayList<Region> getRegions(ImageProcessor imgPro) {
		imgPro = imgPro.duplicate();
		
		imgPro = BinaryConverter.convertBinary(imgPro, 2);
		
		return sequentialLabeling(imgPro, 50, 10000);
	}
	
}
