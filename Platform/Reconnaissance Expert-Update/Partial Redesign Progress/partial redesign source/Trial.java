import java.util.ArrayList;
import java.util.Collections;

/**
 * All instance fields must be initialized by constructor. 
 * Getters should not be used to modify data.
 * Using wrappers do be able to return null when the trial is tracking-only.
 * @author BenPinzone
 *
 */
public class Trial {
	
	//***********************************************
	//FUNDAMENTAL
	public static final int NUM_PICTURES = 4;
	
	//null if tracking-only practice
	private final boolean trackingOnly;
	
	/**
	 * 0 = top left image. 
	 * 1 = top right image. 
	 * 2 = bottom left image.
	 * 3 = bottom right image. 
	 * Strings are the full relative file path and file name.
	 */
	//null if tracking-only practice
	private final ArrayList<String> imageSet;
	
	//null if tracking-only practice
	private final ReconConstants.Alert alert;
	
	//null if tracking-only practice
	private final Boolean containsThreat;
	
	
	
	/**
	 * If trackingOnly_in is true,  the rest of the parameters must be null. 
	 * If trackingOnly_in is false, the rest of the parameters must be non-null.
	 * @param trackingOnly_in
	 * @param imageSet_in
	 * @param alert_in
	 * @param containsThreat_in
	 */
	public Trial(boolean trackingOnly_in, ArrayList<String> imageSet_in, ReconConstants.Alert alert_in, Boolean containsThreat_in){
		trackingOnly = trackingOnly_in;
		
		if(trackingOnly){
			imageSet = null;
			alert = null;
			containsThreat = null;
		}
		else{
			imageSet = imageSet_in;
			alert = alert_in;
			containsThreat = containsThreat_in;
			
		}
	}
	
	//***********************************************
	//GETTERS
	public boolean getTrackingOnly(){
		return trackingOnly;
	}
	public ArrayList<String> getImageSet(){
		return imageSet;
	}
	
	public ReconConstants.Alert getAlert(){
		return alert;
	}

	public boolean getContainsThreat(){
		return containsThreat;
	}
	
	
	//***********************************************
	//ADDITIONAL
	
	/**
	 * -1 = No threat in the image set. 
	 * @return A number corresponding to threat location. See description for encoding.
	 */
	public Integer getThreatLocation () {
		for (int i = 0; i < imageSet.size(); i++) {
			if (imageSet.get(i).contains("Target")) return i;
		}
		return null;
	}
	
	
	//***********************************************
	//***********************************************
	//***********************************************
	//***********************************************
	//***********************************************
	//Put "imageSet" tools elsewhere. Include: shuffling, settings threat position. 
	
//	public void shuffleImageSet(){
//		Collections.shuffle(imageSet);
//	}
//	/**
//	 * 
//	 * @param imageName Must be a full relative file path and file path name.
//	 */
//	public void addImageToSet(String imageName){
//		imageSet.add(imageName);
//	}
	
	
	
	
	

}
