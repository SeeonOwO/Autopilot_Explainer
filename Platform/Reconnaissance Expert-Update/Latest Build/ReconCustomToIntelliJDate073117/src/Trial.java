//import java.applet.AudioClip; //unused look.
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

//contains all information about one trial
/**
 * Represents the fundamental information neccessary for the game to present a trial. 
 * Information about when the trial occurs is not stored here. 
 * Entry contains information about the result of a trial and other data gathered.
 * This is only the bare bones information about what was presented to the user during the trial. 
 * 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 * 
 */
public class Trial {
	
	public static final int NUM_PICTURES = 4;
	
	//image data
	//Each String is a full relative file path and file name.
	public ArrayList<String> imageSet;
	
	//warning data
	public Color color;
	public String clip;

	public String confidence;
	
	//truth data
	public boolean containsEnemy;
	
	public Trial () {
		imageSet = new ArrayList<String>();
		containsEnemy = false;
		confidence = "";
	}
	
	/**
	 * Shuffles the trials imageSet
	 */
	public void shuffle () {
		Collections.shuffle(imageSet);
	}
	
	/**
	 * 
	 * @param imageName Must be a full relative file path and file path name.
	 */
	public void addImageToTrial (String imageName) {
		imageSet.add(imageName);
	}
	
	/**
	 * -1 = No threat in the image set. 
	 *  0 = Threat is in top left image. 
	 *  1 = Threat is in top right image. 
	 *  2 = Threat is in bottom left image.
	 *  3 = Threat is in bottom right image.
	 * @return A number corresponding to threat location. See description for encoding.
	 */
	public int targetLocation () {
		for (int i = 0; i < imageSet.size(); i++) {
			if (imageSet.get(i).contains("Target")) return i;

		}
		return -1;
	}

	public int targetLocationPlus(){
		for (int i = 0; i < imageSet.size(); i++) {
			if (imageSet.get(i).contains("TP") || imageSet.get(i).contains("FP")) return i;

		}
		return -1;
	}
	
	public String toString() {
		String totalString = "";
		for (String s : imageSet) {
			totalString += s + "\n";
		}
		totalString += OneButtonTracker.getRecommendationString(color);
		return totalString;
	}
	
	/**
	 * Moves the threat image to the image location specified by the index.
	 * REQUIRES: this trial contains a threat.
	 * @param index
	 */
	public void reassignPresentImage (int index) {
		//First removes the threat image from the set. That image is returned by "remove". 
		//This will shift images left in the ArrayList as needed.
		//Then, it inserts the threat iamge again, but at the specified index. 
		//Other images will shift right in the ArrayList as neccessary.
		imageSet.add(index, imageSet.remove(targetLocationPlus()));
	}
	
	//For testing purposes only.
	public void printInfo(int i ){
		System.out.println("---");
		System.out.println("Trial: " + i + " " + this);
		System.out.println("Image Set: " + imageSet);
		System.out.println("Color: " +color);
		System.out.println("Clip: " + clip);
		System.out.println("Contains enemy: " +containsEnemy);
		System.out.println("---");
	}
}
