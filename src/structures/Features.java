package structures;

import java.io.Serializable;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import control.BinaryConverter;
import control.RegionLabeler;

public class Features implements Serializable {
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	private int area;
	public double fatitude;
	public double density;
	public double[] irCentroid;
	public double[] irInvMoms;
	
	public Features(Region region, ImageProcessor irImgPro) {
		region.buildIRBox(irImgPro);
		ImageProcessor binaryIRBox = region.getBinaryIRBox();
//		if(region.isAnimal()){
//			ImagePlus imgp = new ImagePlus("IR-"+region.getLabel(), region.getIRBox());
//			imgp.show();
//			ImagePlus imgpp = new ImagePlus("IR-BW-"+region.getLabel(), binaryIRBox);
//			imgpp.show();
//		}
		width = binaryIRBox.getWidth();
		height = binaryIRBox.getHeight();
		area = width * height;
		
		//IRBox = IRBox.resize(400); // Base features off images of similar scale
		setFatitude(region);
		setDensity(region);
		irCentroid = getCentroid(binaryIRBox);
		irInvMoms = getInvariantMoments(binaryIRBox, irCentroid[0], irCentroid[1]);
	}
	
	private void setFatitude(Region region) {
		fatitude = (region.getRight()-region.getLeft())/(region.getTop()-region.getBottom());
	}
	
	private void setDensity(Region region) {
		density = fatitude/region.getArea();
	}
	
	private double[] getCentroid(ImageProcessor imgPro) {
		// Calculate centroid
		int m10 = 0;
		int m01 = 0;
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int pixel = imgPro.getPixel(x, y);
				if(pixel == RegionLabeler.BLACK) {
					pixel = 1;
				} else {
					pixel = 0;
				}
				m10 += pixel * x;
				m01 += pixel * y;
			}
		}
		
		double mom10Mean = m10 / area;
		double mom01Mean = m01 / area;
		
		return new double[]{mom10Mean, mom01Mean};
	}
	
	private double[] getInvariantMoments(ImageProcessor imgPro, double xCentroid, double yCentroid) {
		double n20 = scaleInvCentralMoment(imgPro, 2, 0, xCentroid, yCentroid);
		double n02 = scaleInvCentralMoment(imgPro, 0, 2, xCentroid, yCentroid);
		double n11 = scaleInvCentralMoment(imgPro, 1, 1, xCentroid, yCentroid);
		double n30 = scaleInvCentralMoment(imgPro, 3, 0, xCentroid, yCentroid);
		double n12 = scaleInvCentralMoment(imgPro, 1, 2, xCentroid, yCentroid);
		double n21 = scaleInvCentralMoment(imgPro, 2, 1, xCentroid, yCentroid);
		double n03 = scaleInvCentralMoment(imgPro, 0, 3, xCentroid, yCentroid);
		
		double invMom1 = n20 + n02;
		double invMom2 = Math.pow((n20 - n02), 2) + 4 * Math.pow(n11, 2);
		double invMom3 = Math.pow((n30 - 3 * n12), 2) + Math.pow((3 * n21 - n03), 2);
		double invMom4 = Math.pow((n30 + n12), 2) + Math.pow((n21 + n03), 2);
		double invMom5 = (n30 - 3 * n21) * (n30 + n12) * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2)) 
				+ (3 * n21 - n03) * (n21 + n03) * (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));
		double invMom6 = (n20 - n02) * (Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2)) + 4 * n11 * (n30 + n12) * (n21 + n03);
		double invMom7 = (3 * n21 - n03) * (n30 + n12) * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2)) - (n30 + 3 * n12) * (n21 + n03) 
				* (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));
		return new double[]{invMom1, invMom2, invMom3, invMom4, invMom5, invMom6, invMom7};
	}
	
	private double scaleInvCentralMoment(ImageProcessor imgPro, int p, int q, double xCentroid, double yCentroid) {
		double ctrM = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int pixel = imgPro.getPixel(x, y);
				if(pixel == BinaryConverter.BLACK) {
					pixel = 1;
				} else {
					pixel = 0;
				}
				ctrM += Math.pow((x-xCentroid), p) * Math.pow((y-yCentroid), q) * pixel; 
			}
		}
		// Central moment (p,q) found, now make scale invariant
		double ctrMSI = ctrM / Math.pow(area, 1+(p+q)/2);
		return ctrMSI;
	}
	
	public String toString() {
		return fatitude + "," + density + "," + irCentroid[0] + "," + irCentroid[1] + "," + irInvMoms[0] + "," + irInvMoms[1]
				 + "," + irInvMoms[2] + "," + irInvMoms[3] + "," + irInvMoms[4] + "," + irInvMoms[5] + "," + irInvMoms[6];
	}
	
	public double getDistanceFrom(Features feat) {
		double fatD = Math.pow(feat.fatitude - fatitude, 2);
		double densD = Math.pow(feat.density - density, 2);
		double[] irCentroidD = new double[irCentroid.length];
		for(int i = 0; i < irCentroid.length; i++) {
			irCentroidD[i] = Math.pow(feat.irCentroid[i] - irCentroid[i], 2);
		}
		double[] irInvMomsD = new double[irInvMoms.length];
		for(int i = 0; i < irInvMoms.length; i++) {
			irInvMomsD[i] = Math.pow(feat.irInvMoms[i] - irInvMoms[i], 2);
		}
		double sum = fatD + densD;
		for(int i = 0; i < irCentroidD.length; i++) {
			sum += irCentroidD[i];
		}
		for(int i = 0; i < irInvMomsD.length; i++) {
			sum += irInvMomsD[i];
		}
		return Math.sqrt(sum);
	}
}
