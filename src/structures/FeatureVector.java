package structures;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureVector implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashMap<String, ArrayList<Features>> featureVector;
	private int size;
	private TypeNode[] lookup;
	
	public FeatureVector(boolean isTraining) {
		readFVFromFile();
		if(featureVector == null) {
			featureVector = new HashMap<String, ArrayList<Features>>();
		}
		size = 0;
		if(isTraining == false) { // Build lookup table for classification
			lookup = new TypeNode[featureVector.entrySet().size()];
			int i = 0;
			for(Map.Entry entry : featureVector.entrySet()) {
				String type = (String) entry.getKey();
				ArrayList<Features> features = (ArrayList<Features>) entry.getValue();
				lookup[i] = new TypeNode(type, size, size+features.size());
				size += features.size();
				i++;
			}
		}
	}
	
	class TypeNode {
		String type;
		int startRange, endRange;
		public TypeNode (String type, int startRange, int endRange) {
			this.type = type;
			this.startRange = startRange;
			this.endRange = endRange;
		}
	}
	
	public void addFeature(String type, Features newFeatures) {
		if(featureVector.containsKey(type)) {
			ArrayList<Features> features = featureVector.get(type);
			features.add(newFeatures);
			size++;
		} else {
			ArrayList<Features> arrList = new ArrayList<Features>();
			arrList.add(newFeatures);
			featureVector.put(type, arrList);
			size++;
		}
	}
	
	public void writeFVToFile() {
		try {
			FileOutputStream fout = new FileOutputStream("features");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(featureVector);
			oos.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readFVFromFile() {
		try {
			FileInputStream fin = new FileInputStream("features");
			ObjectInputStream ois = new ObjectInputStream(fin);
			featureVector = (HashMap<String, ArrayList<Features>>) ois.readObject();
			ois.close();
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String classifyKNN(Features feat, int k) {
		final Double[] distanceMatrix = new Double[size];
		int completed = 0;
		for(Map.Entry entry : featureVector.entrySet()) {
			ArrayList<Features> features = (ArrayList<Features>) entry.getValue();
			for(int i = 0; i < features.size(); i++) {
				distanceMatrix[completed] = features.get(i).getDistanceFrom(feat);
				completed++;
			}
		}
		
		Integer[] indexes = new Integer[distanceMatrix.length];
		for(int i = 0; i < distanceMatrix.length; i++) {
			indexes[i] = i;
		}
		
		Arrays.sort(indexes, new Comparator<Integer>() {
			public int compare(Integer ind1, Integer ind2) {
				return distanceMatrix[ind1].compareTo(distanceMatrix[ind2]);
			}
		});
		
		// Vote which type each of the k closest matches are
		int[] votes = new int[featureVector.entrySet().size()];
		for(int i = 0; i < k; i++) {
			for(int j = 0; j < lookup.length; j++) {
				if(indexes[i] >= lookup[j].startRange && indexes[i] < lookup[j].endRange) {
					votes[j]++;
				}
			}
		}
		// Find winner
		int max = votes[0];
		int maxInd = 0;
		for(int i = 0; i < votes.length; i++) {
			if(votes[i] > max)  {
				max = votes[i];
				maxInd = i;
			}
		}
		
		return lookup[maxInd].type;
	}
	
	public void printFeatures() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("features.txt"));
			List<Features> featureList = featureVector.get("animal");
			writer.write("Animal features:" + System.lineSeparator());
			for(int i = 0; i < featureList.size(); i++) {
				Features thisFeat = featureList.get(i);
				String out = thisFeat.toString() + System.lineSeparator();
				writer.write(out);
			}
			writer.write("Nonanimal features:" + System.lineSeparator());
			featureList = featureVector.get("nonanimal");
			for(int i = 0; i < featureList.size(); i++) {
				Features thisFeat = featureList.get(i);
				String out = thisFeat.toString() + System.lineSeparator();
				writer.write(out);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
