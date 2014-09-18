package control;

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class BinaryConverter {
	private static final int CLRRESOLUTION = 65535; // Maximum number of values in picture
	public static final int BLACK = 0; // Value of black
	public static final int WHITE = 65535; // Value of white
	
	/**
	 * Constructs a histogram of an image.
	 * @param imgPro Image to build histogram from.
	 * @return Frequency array of each value in the image.
	 */
	private static int[] makeHistogram(ImageProcessor imgPro) {
		int[] histogram = new int[CLRRESOLUTION];
		for(int y = 0; y < imgPro.getHeight(); y++) {
			for(int x = 0; x < imgPro.getWidth(); x++) {
				histogram[imgPro.getPixel(x, y)]++;
			}
		}
		return histogram;
	}
	
	/**
	 * Uses Otsu's method to automatically cluster-based thresholding.
	 * @param histogram
	 * @param size
	 * @return
	 */
	private static int otsuThresh(int[] histogram, int size) {
		int weightedSum = 0;
		for(int i = 0; i < CLRRESOLUTION; i++) {
			weightedSum += i * histogram[i];
		}
		int backgroundWeight = 0;
		int foregroundWeight = 0;
		int runningWSum = 0;
		double backgroundMean = 0.0;
		double foregroundMean = 0.0;
		double variance = 0.0;
		double max = 0.0;
		double thresholdA = 0.0;
		double thresholdB = 0.0;
		for(int i = 0; i < CLRRESOLUTION; i++) {
			backgroundWeight += histogram[i];
			if(backgroundWeight == 0) continue;
			foregroundWeight = size - backgroundWeight;
			if(foregroundWeight == 0) break;
			runningWSum += i * histogram[i];
			backgroundMean = runningWSum / backgroundWeight;
			foregroundMean = (weightedSum - runningWSum) / foregroundWeight;
			variance = backgroundWeight * foregroundWeight * Math.pow(backgroundMean - foregroundMean, 2);
			if(variance >= max) {
				max = variance;
				thresholdA = i;
				if(variance > max) {
					thresholdB = i;
				}
			}
		}
		int ret = (int) ((thresholdA + thresholdB) / 2.0);
		return ret;
	}
	
	/**
	 * This needs to be fixed.
	 * @param list Set of numbers to find median.
	 * @param left Initialize with 0.
	 * @param right Size of the set less 1.
	 * @param k Half the size of the set.
	 * @return
	 */
	private static int medianOfMedians(int[] list, int left, int right) {
		int medians = (int) Math.ceil((right-left)/5.0);
		for(int i = 0; i <= medians; i++) {
			int subLeft = left + i*5;
			int subRight = subLeft+4;
			if(subRight > right) subRight = right;
			//System.out.println("subLeft: " +subLeft+ " subRight: " +subRight+ " k: " +(((subRight-subLeft+1)/2)+1+subLeft));
			int median = selectIndex(list, subLeft, subRight, ((subRight-subLeft)/2)+subLeft);
			swap(list, left+i, median);
		}
		//System.out.println("left: " +left+ " right: " +(left+medians-1));
		return selectIndex(list, left, left+medians-1, (medians/2)+left);
	}
	
	private static int selectIndex(int[] list, int left, int right, int k) {
		if(left == right) {
			return left;
		}
		int pivotIndex = (int) (left + Math.floor(Math.random() * (right-left+1)));
		pivotIndex = partition(list, left, right, pivotIndex);
		if(pivotIndex == k) {
			return pivotIndex;
		} else if (k < pivotIndex) {
			//System.out.println("fuck a duck: " +(pivotIndex-1));
			return selectIndex(list, left, pivotIndex-1, k);
		} else {
			//System.out.println("wikisucks");
			return selectIndex(list, pivotIndex+1, right, k);
		}
	}
	
	private static int partition(int[] list, int left, int right, int pivotIndex) {
		int pivotValue = list[pivotIndex];
		swap(list, pivotIndex, right);
		int storeIndex = left;
		for(int i = left; i < right; i++) {
			if(list[i] <= pivotValue) {
				swap(list, storeIndex, i);
				storeIndex++;
			}
		}
		swap(list, right, storeIndex);
		return storeIndex;
	}
	
	private static void swap(int[] list, int ind1, int ind2) {
		int temp = list[ind1];
		list[ind1] = list[ind2];
		list[ind2] = temp;
	}
	
	/**
	 * Generates statistics of local area.
	 * @param kernel Matrix to process statistics of.
	 * @param mode Selector of which statistic to use. 0: Mean, 1: Median, 2: Min/Max Mean.
	 * @return Mean, median, and mean of the min/max values.
	 */
	private static int localStat(int[][] kernel, int mode) {
		if(mode == 0) { // Use mean
			int sum = 0;
			for(int i = 0; i < kernel.length; i++) {
				for(int j = 0; j < kernel.length; j++) {
					sum += kernel[i][j];
				}
			}
			int mean = Math.round(sum/(kernel.length*kernel.length));
			return mean;
		} else if(mode == 1) { // Use median
			int[] flatKernel = new int[kernel.length*kernel.length];
			int place = 0;
			for(int i = 0; i < kernel.length; i++) {
				for(int j = 0; j < kernel.length; j++) {
					flatKernel[place] = kernel[i][j];
					place++;
				}
			}
			int index = medianOfMedians(flatKernel, 0, flatKernel.length-1);
			int medianMedian = flatKernel[index-1];
			return medianMedian;
		} else if(mode == 2) { // Use min/max mean
			int max = kernel[0][0];
			int min = kernel[0][0];
			for(int i = 0; i < kernel.length; i++) {
				for(int j = 0; j < kernel.length; j++) {
					if(kernel[i][j] < min) min = kernel[i][j];
					if(kernel[i][j] > max) max = kernel[i][j];
				}
			}
			return (max+min)/2;
		}
		return -1;
		
	}
	
	/**
	 * Threshold image based on a statistic of the local neighborhood.
	 * @param imgPro Image to threshold.
	 * @return Returns a binary image.
	 */
	private static ImageProcessor adaptiveThreshold(ImageProcessor imgPro) {
		int local = Options.local; // Find method for selecting neighborhood size (possibly ratio of image dimension)
		double brightMult = Options.brightMult; // Find method for selecting luminosity
		int method = Options.method;  // Find method for selecting statistical method 0:Mean, 1:Median, 2:MinMax
		// End temporary vars
		
		ImageProcessor smoothImg = new ShortProcessor(imgPro.getWidth(), imgPro.getHeight());
		smoothImg.setCalibrationTable(imgPro.getCalibrationTable());
		int cDist = local/2;
		int[][] kernel = new int[local][local]; 
		for(int i = 0; i < local; i++) { // Initial kernel
			for(int j = 0; j < local; j++) {
				kernel[i][j] = imgPro.getPixel(i, j);
			}
		}
		// Convolve image with local statistic, use mode to set which
		for(int y = 0+cDist; y < imgPro.getHeight()-cDist; y++) {
			for(int x = 0+cDist; x < imgPro.getWidth()-cDist; x++) {
				for(int i = -cDist; i < cDist; i++) {
					for(int j = -cDist; j < cDist; j++) {
						kernel[i+cDist][j+cDist] = imgPro.getPixel(x+i, y+j);
					}
				}
				if(imgPro.getPixel(x, y) >= localStat(kernel, method)*brightMult) {
					smoothImg.putPixel(x, y, BLACK);
				} else {
					smoothImg.putPixel(x, y, WHITE);
				}
			}
		}
		
		return smoothImg;
	}
	
	/**
	 * Converts image to black and white using various threshold methods.
	 * @param imgPro Image to convert.
	 * @param mode Method to use for thresholding. 0:ImageJ auto, 1:Otsu's Method, 2:Adaptive Threshold
	 * @return Binary image.
	 */
	public static ImageProcessor convertBinary(ImageProcessor imgPro, int mode) {
		imgPro = imgPro.convertToShort(false);
		// Scale image values from 0 to CLRRESOLUTION
		float[] cTable = new float[CLRRESOLUTION];
		for(int i = 0; i < cTable.length; i++) {
			cTable[i] = i;
		}
		imgPro.setCalibrationTable(cTable);
		
		int height = imgPro.getHeight();
		int width = imgPro.getWidth();
		int thresh = 0;
		// Set threshold
		if(mode == 0) { // Use built in automatic threshold
			thresh = imgPro.getAutoThreshold();
		} else if(mode == 1) { // Use Otsu's thresholding
			thresh = otsuThresh(makeHistogram(imgPro), imgPro.getPixelCount());
		} else if(mode == 2) { // Use adaptive thresholding
			imgPro = adaptiveThreshold(imgPro);
			return imgPro;
		} else {
			return imgPro;
		}
		
		for(int y = 0; y < height-1; y++) {
			for(int x = 0; x < width-1; x++) {
				int pixel = imgPro.getPixel(x, y);
				if(pixel < thresh) {
					imgPro.putPixel(x, y, WHITE);
				} else if(pixel >= thresh) {
					imgPro.putPixel(x, y, BLACK);
				}
			}
		}
		
		return imgPro;
	}
}
