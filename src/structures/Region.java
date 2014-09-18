package structures;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.util.LinkedList;

import control.BinaryConverter;

public class Region {
	private int label;
	private int left;
	private int right;
	private int top;
	private int bottom;
	private int boxLeft;
	private int boxRight;
	private int boxTop;
	private int boxBottom;
	private int offset;
	private int thickness;
	private boolean isAnimal;
	private LinkedList<Point> region;
	private ImageProcessor irBox;
	private ImageProcessor binaryIRBox;
	
	public Region(int label) {
		this.label = label;
		left = Integer.MAX_VALUE;
		right = 0;
		top = 0;
		bottom = Integer.MAX_VALUE;
		boxLeft = 0;
		boxRight = 0;
		boxTop = 0;
		boxBottom = 0;
		offset = 0;
		thickness = 0;
		isAnimal = false;
		region = new LinkedList<Point>();
		irBox = null;
		binaryIRBox = null;
	}
	
	public void addPoint(int x, int y) {
		region.add(new Point(x,y));
		if (x < left) left = x;
		if (x > right) right = x;
		if (y > top) top = y;
		if (y < bottom) bottom = y;
	}
	
	public void addRegion(Region region) {
		this.region.addAll(region.getRegionPoints());
		
		if(region.left < left) left = region.left;
		if(region.right > right) right = region.right;
		if(region.top > top) top = region.top;
		if(region.bottom < bottom) bottom = region.bottom;
	}
	
	public int getLabel() {
		return label;
	}
	
	public void changeLabel(int label) {
		this.label = label;
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getRight() {
		return right;
	}
	
	public int getTop() {
		return top;
	}
	
	public int getBottom() {
		return bottom;
	}
	
	public boolean isAnimal() {
		return isAnimal;
	}
	
	public void markAnimal() {
		isAnimal = true;
	}
	
	public void unmarkAnimal() {
		isAnimal = false;
	}
	
	public LinkedList<Point> getRegionPoints() {
		return region;
	}
	
	public void destroyRegion() {
		region.clear();
	}
	
	public int getArea() {
		return region.size();
	}
	
	public ImageProcessor getIRBox() {
		return irBox;
	}
	
	public ImageProcessor getBinaryIRBox() {
		return binaryIRBox;
	}
	
	private void buildBox(ImageProcessor imgPro) {
		offset = imgPro.getHeight()/100;
		thickness = offset/4;
		boxLeft = left - thickness - offset;
		boxRight = right + thickness + offset;
		boxTop = top + thickness + offset;
		boxBottom = bottom - thickness - offset;
	}
	
	public void buildIRBox(ImageProcessor imgPro) {
		buildBox(imgPro);
		ImageProcessor IRBox = new ColorProcessor(boxRight-boxLeft, boxTop-boxBottom);
		for(int y = boxBottom; y < boxTop; y++) {
			for(int x = boxLeft; x < boxRight; x++) {
				IRBox.putPixel(x-boxLeft, y-boxBottom, imgPro.getPixel(x, y));
			}
		}
		this.irBox = IRBox;
		this.binaryIRBox = BinaryConverter.convertBinary(IRBox.duplicate(), 0);
	}
	
	/**
	 * Draw bounding box of a region.
	 * @param imgPro Processor to be drawn on.
	 * @param region Region who's bounding box is to be drawn.
	 * @param color Color of the bounding box.
	 */
	public void drawRegionBox(ImageProcessor imgPro, int color) {
		buildBox(imgPro);
		// Draw top line
		for(int x = boxLeft; x <= boxRight; x++) {
			for(int y = boxTop-thickness; y <= boxTop; y++) {
				imgPro.putPixel(x, y, color);
			}
		}
		
		//Draw bottom line
		for(int x = boxLeft; x <= boxRight; x++) {
			for(int y = boxBottom+thickness; y >= boxBottom; y--) {
				imgPro.putPixel(x, y, color);
			}
		}
		
		//Draw left line
		for(int y = boxBottom; y <= boxTop; y++) {
			for(int x = boxLeft+thickness; x >= boxLeft; x--) {
				imgPro.putPixel(x, y, color);
			}
		}
		
		//Draw right line
		for(int y = boxBottom; y <= boxTop; y++) {
			for(int x = boxRight-thickness; x <= boxRight; x++) {
				imgPro.putPixel(x, y, color);
			}
		}
	}
}
