import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.lang.StringBuilder;



/**
 * A class that takes an ArrayList of Trials and initializes all the data for each trial given quotas from the ExpSettings object. 
 * After the ImageSetGenerator does its job, every trial will have all its members initialized. 
 * The "Generation" is either "random" or "input". Most of the methods deal with Random Generation.
 * <p>
 * In general, methods will receive a reference to the ArrayList of Trials, and modify it using member functions. 
 * Of course, the reference itself must never be re-assigned, because it is only a copy of a reference. 
 * 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 * 
 */
public class ImageSetGenerator {
	
	public static final String imageListFile = ""; //looks useless

	
	/**
	 * The Strings represent the FULL RELATIVE FILE PATH AND FILE NAME for a target image. Initialy contains all of the threat images.
	 */
	private Queue<String> targetImages = new LinkedList<String>();
	
	
	/**
	 * A PQ of bins of no-threat images. Bins with the most images will come first in the queue. Initially contains all of the no-threat images.
	 */
	private PriorityQueue<AbsentImageBin> absentImageBinQue = new PriorityQueue<AbsentImageBin>();
	
	
	private ExpSettings settings;
	
	public ImageSetGenerator (ExpSettings someSettings) {
		
		settings = someSettings;
		readImageListFile();
	}
	
	//*******************************************************************
	//GENERATE LIST/COLLECTION OF *ALL* IMAGE NAMES IN DATABASE.
	
	/**
	 * Reads "file_list.txt" and initializes "targetImages" and "absentImageBinQue". Generates the high level collections of images.
	 * Called by constructor.
	 * <p>
	 * Note: file_list.txt was manually edited to have "!" in front of all the image file paths that contain threats. 
	 * (So they could be processed easily...). These "!" are not actually seen anywhere else. Not great.
	 */
	private void readImageListFile () {	
		try {
			//now this is quite the interesting variable. look.
			HashMap<Character, AbsentImageBin> absentImageMap = new HashMap<Character, AbsentImageBin>();
			
			Scanner fin = new Scanner(new BufferedReader(new FileReader("file_list.txt")));
			String fileName; //probably the image file name.
			char map;
			
			while (fin.hasNextLine()) {
				fileName = fin.nextLine();
			
				//If the file name is a threat image
				if (fileName.charAt(0) == '!') {
					fileName = fileName.substring(1);
					targetImages.add(fileName);
				}
				//else the file name is a no-threat image
				else {
					map = mapName(fileName);
					if (!absentImageMap.containsKey(map)) {
						absentImageMap.put(map, new AbsentImageBin(map));
					}
					absentImageMap.get(map).add(fileName);
				}
			} //end while loop.
			
			fin.close();
			
			
			shuffleAndAddAbsentImageBins(absentImageMap); 
			
			//shuffle the targetImages list.
			Collections.shuffle((LinkedList<String>)targetImages);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper function of readImageListFile. Takes the HashMap it generated (for no-threat images), 
	 * and shuffles the images in every bin, then adds each bin to absentImageBinQue class member.
	 * 
	 * @param absentImageMap
	 */
	private void shuffleAndAddAbsentImageBins (HashMap<Character, AbsentImageBin> absentImageMap) {
		Iterator<Character> it = absentImageMap.keySet().iterator();
		AbsentImageBin someImageBin;
		while (it.hasNext()) {
			//get next bin
			someImageBin = absentImageMap.get(it.next());
			//shuffle
			someImageBin.shuffle();
			//add to high level binQue
			absentImageBinQue.add(someImageBin); //look! important!
		}
	}
	
	//*******************************************************************
	//RANDOM GENERATION

	/**
	 * The highest level method called to set up data for a list of trials with random generation. 
	 * Sets up the images, then the warnings (alert color and sound). The order of all the trials is then shuffled. 
	 * <p>
	 * Used only for random generation.
	 * @param allTrials
	 */
	public void setupTrials_Random (ArrayList<Trial_old> allTrials) {
		
		setupTrialsImages(allTrials);	
		setupTrialsWarnings(allTrials);
		
		Collections.shuffle(allTrials);
	}
	
	
	/**
	 * The high level method for setting up the trials' images. Two lists of trials are generated. One list has only threat images, 
	 * the other has only non-threat images. After both lists are generated, the contents of both are put into allTrials. 
	 * <p>
	 * IMPORTANT: The threat images come first in the final list. This is neccessary because of how the warning generation works. 
	 * <p>
	 * Used only for random generation.
	 * @param allTrials
	 */
	private void setupTrialsImages (ArrayList<Trial_old> allTrials) {
			
		//generate list of trials with threats
		ArrayList<Trial_old> presentOnly = generateTrialListOfImageType(true);
		distributeThreatImagePositions(presentOnly);
		//shuffle the order of the threat trials. Not really needed, because setupTrials_Random will shuffle them all later.
		Collections.shuffle(presentOnly); 
			
		//generate list of trials with no threats
		ArrayList<Trial_old> absentOnly = generateTrialListOfImageType(false);

		
		//add both the present and absent trial lists to the overall trials list.
		//NOTE: present trials come FIRST in the arraylist, followed by absent trials.
		//this is order IS NECCESSARY for setting up warnings. 
		for(Trial_old somePresentTrial : presentOnly){
			allTrials.add(somePresentTrial);
		}
		
		for (Trial_old someAbsentTrial : absentOnly){
			allTrials.add(someAbsentTrial);
		}
		//DO NOT SHUFFLE HERE. WARNING DATA MUST BE ADDED FIRST.
		
	}
	
	
	/**
	 * The high level method for setting up the trials' warnings. Sets them up based on the "quotas" in the ExpSettings object. 
	 * The "quotaList" member of the ExpSettings object is an ArrayList with a bit of hidden knowledge regarding its order. See its description where it is declared. 
	 * <p>
	 * Used only for random generation.
	 * @param allTrials
	 */
	private void setupTrialsWarnings (ArrayList<Trial_old> allTrials) {
		
		//note: this overallIndex system works because of the order placed on the trials during construction, before shuffle.
		int overallIndex = 0;
		for (int quotaScenarioCountIndex = 0; quotaScenarioCountIndex < settings.quotaList.size(); quotaScenarioCountIndex++) {
			//for every entry in the quota list.
			
			for (int j = 0; j < settings.quotaList.get(quotaScenarioCountIndex); j++) {
				
				//while j is less than the ENTRY DATA at quotaListEntryNum
				//loops however many times, specified by entry, then sets the colors and clips for trials.
				allTrials.get(overallIndex).color = settings.getColor(quotaScenarioCountIndex, settings.alarmIsBinary); //luckily, this is the only place that "getColor" is called. // yes, because quotaListIndexNum on some level specifies the color!
				allTrials.get(overallIndex).clip = settings.getAudioClip(quotaScenarioCountIndex, settings.alarmIsBinary); //luckily, this is the only place that "getAudioClip" is called. thank goodness.
				
				overallIndex++;
			}
		}
	}


	/**
	 * Goes through a list of threat trials, and evenly distributes which image the threat appears in. 
	 * Becuase of how trial image sets iare initially generated, the threat is in a random position. Apparently they want it to be perfectly distributed. 
	 * Need to evenly distribute that. Of course if the number of threat trials is not a multiple of 4, it will be uneven. 
	 * <p>
	 * Used only for random generation.
	 * @param presentOnly
	 * 
	 */
	private void distributeThreatImagePositions (ArrayList<Trial_old> presentOnly) {
		
		int positionToPlace = 0;
		for(int trialsDone = 0; trialsDone < presentOnly.size(); trialsDone++){
			presentOnly.get(trialsDone).reassignPresentImage(positionToPlace);
			
			positionToPlace++;
			if(positionToPlace == 4){
				positionToPlace = 0;
			}
		}
	}
	
	
	/**
	 * Generates a list of trials where: every trial contains a threat XOR every trial contains no threats.
	 * Depends on the parameter "present". This is the level in the code where you will actually see "new Trial()". 
	 * <p>
	 * If a trial contains a threat, it will contain at most one threat. 
	 * Therefore, regardless of value of "present", this method will utilize it's helper function "fillTrialWithAbsentImages". 
	 * It will fill every trial with either 3 or 4 no-threat images, depending of course on "present". 
	 * @param present
	 * @return The generated list of Trials.
	 */
	private ArrayList<Trial_old> generateTrialListOfImageType (boolean present) {
		

		ArrayList<Trial_old> trialListToGenerate = new ArrayList<Trial_old>();
		int trialListLength = present ? settings.present() : settings.absent();
		
		
		for (int i = 0; i < trialListLength; i++) {
			
			Trial_old someTrial = new Trial_old();
			
			//If a map is used for a threat image, don't use that map when filling trial with no-threat images.
			char illegalMap = '!';
			
			if (present) {

				if (targetImages.isEmpty()) {
					System.out.println("ran out of target images");
					return trialListToGenerate;
				}
				
				String targetImageName = targetImages.poll(); 
				illegalMap = mapName(targetImageName); 
				
				someTrial.addImageToTrial(targetImageName); 
				someTrial.containsEnemy = true; 
				
			}//end if
			
			fillTrialWithAbsentImages(someTrial, illegalMap);
			trialListToGenerate.add(someTrial); 
			
		
		}//end for loop
		
		return trialListToGenerate;
	}
	
	
	/**
	 * A helper function of generateTrialListOfImageType. Takes a trial and fills it with no-threat images. 
	 * <p>
	 * The images will not be taken from the map represented by illegalMap. 
	 * <p>
	 * Used only for random generation.
	 * @param someTrial
	 * @param illegalMap
	 * 
	 */
	private void fillTrialWithAbsentImages (Trial_old someTrial, char illegalMap) { 
		
		//A bin will be added to the waiting area if its map is illegal, or if an image is drawn from it. 
		//Bins in the waiting area will be added back to the main Queue once the trial is filled.
		ArrayList<AbsentImageBin> waitingArea = new ArrayList<AbsentImageBin>();
		
		while (someTrial.imageSet.size() < Trial_old.NUM_PICTURES) {
			
			AbsentImageBin tempImageBin = absentImageBinQue.poll();
			
			if (tempImageBin.map != illegalMap) {
				//The real task. 
				String imageName = tempImageBin.get();
				someTrial.addImageToTrial(imageName); 
			}
			
			//Put bin in waiting area.
			waitingArea.add(tempImageBin);
		}
		
		//Put bins from waiting area back into main Queue
		for (AbsentImageBin b : waitingArea) {
			absentImageBinQue.add(b);
		}
		
		//Shuffle the trials. Although, any threat images will be precisely placed later. 
		someTrial.shuffle(); 
		
	}
	
	
	/**
	 * Takes a full relative file path and file name of an image file and returns the map name. This is done by returning the first character of the file name.
	 * @param fileName The full relative file path and file name of an image file
	 * @return The map name
	 */
	private char mapName (String fileName) { //using only one character!
		for (int i = fileName.length() - 1; i >= 0; i--) {
			if (fileName.charAt(i) == '/') {
				return fileName.charAt(i + 1);
			}
		}
		return '?';
	}
	
	
	//*******************************************************************
	//INPUT GENERATION
	
	//LOOK. TODO: make sure they give a valid file name.
	/**
	 * Takes an image file name and returns the full relative file path and file name.
	 * @param fileName The image file name. Does NOT include path. 
	 * @return The full relative file path and file name. (of the file name passed in) 
	 */
	public static String generateFullFilePath(String fileName){
		
		//fileName ex:     NN_3_present.png
		//   			   GG_11_absentflip.png
		
		String letterCode = new String(fileName.substring(0, 2));
		StringBuilder filePath = new StringBuilder();
		filePath.append("Picture for experiment//");
		
		//threat/present image
		if(fileName.contains("present")){
			filePath.append("Target/");
			filePath.append(fileName);
			return filePath.toString();
		}
		
		//implicit else   
		//clear/absent image
		filePath.append(letterCode);
		filePath.append("_absent");
		
		if(fileName.contains("flip")){
			filePath.append("flip");
		}
		
		filePath.append("/");
		filePath.append(fileName);
		
		return filePath.toString();
	}
	
	/**
	 * Takes a trial's image set and returns true if any of the images contain a threat. 
	 * Recall: imageSet contains full relative file path and file name.
	 * <p>
	 * IMPORTANT: Works by looking if the image is in the "Target" folder. 
	 * @param imageSet
	 * @return Whether or not the image set contains a threat.
	 */
	public static boolean containsThreat(ArrayList<String> imageSet){
		//note: at this point, the imageSet contains FULL FILE PATH name.
		for(int imageSetIndex = 0; imageSetIndex < imageSet.size(); imageSetIndex++){
			if(imageSet.get(imageSetIndex).contains("Target")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Takes allTrials and sets up all of the trial data according to the inputImageSet and inputWarningSet objects in the ExpSettings object.
	 * @param allTrials
	 */
	public void setupTrials_Input(ArrayList<Trial_old> allTrials){
		
	
		ArrayList<String> workingImageSet = new ArrayList<String>();
		String workingImageFileName;
		String workingAlertToGive;
		
		//For every trial
		for(int trialsDone = 0; trialsDone < settings.experimentTrials; trialsDone++){
			
			//make new trial object
			Trial_old workingTrial = new Trial_old();
			
			//setup image set
			workingImageSet.clear();
			for(int imagesAdded = 0; imagesAdded < 4; imagesAdded++){
				//                             			     accessing position  accessing image	
				workingImageFileName = settings.inputImageSet.get(imagesAdded).get(trialsDone);
				workingImageFileName = generateFullFilePath(workingImageFileName);
				
				workingImageSet.add(workingImageFileName);
			}
			
			//setup alert name
			workingAlertToGive = settings.inputWarningSet.get(trialsDone);
			
			//pass all the working data to initialize the data for the single trial.
			setupSingleInputTrial(workingTrial, workingImageSet, workingAlertToGive);
			
			//add trial to list.
			allTrials.add(workingTrial);
			
		}//end loop for each trial.
	}//end method.
	
	/**
	 * A helper method of setupTrials_Input. Takes one trial, and the data that it is to be initilaized with, and initializes it. 
	 * @param someTrial
	 * @param imageNames
	 * @param alertToGive
	 */
	private void setupSingleInputTrial(Trial_old someTrial, ArrayList<String> imageNames, String alertToGive){
		//             0         1          2            3
		//image order: top left, top right, bottom left, bottom right
		//alertToGive must be one of the strings in the switch. lOOK. USER REQUIREMENT.
		
		
		//someTrial.imageSet = imageNames; //look, for some reason, this doesn't work. not sure why...
		//and it also doesn't work even if you make a "set imageSet" function within trial...someTrial.setImageSet(imageNames);
		
		//SETUP TRIAL IMAGES
		for(int i = 0; i < 4; i++){
			someTrial.addImageToTrial(imageNames.get(i));
		}
		
		//SETUP TRIAL THREAT STATUS
		someTrial.containsEnemy = containsThreat(imageNames);
				
		//SETUP TRIAL WARNING
		//note: need Java 7 to use Strings in switch
		switch(alertToGive){
			case "red" :  {
				someTrial.color = settings.getColor(0, false); //look. for now, I am just putting false in all these second parameters...it doesn't matter too much here. The trial is not concerned HOW it gets the color...
				someTrial.clip = settings.getAudioClip(0, false);     //you can fix this whole system after you get this working. 
			}break;
			
			case "amber" : {
				someTrial.color = settings.getColor(1, false);
				someTrial.clip = settings.getAudioClip(1, false);
			}break;
			
			case "lightGreen" : {
				someTrial.color = settings.getColor(2, false);
				someTrial.clip = settings.getAudioClip(2, false);
			}break;
			
			case "darkGreen" :
			case "green"     : {
				someTrial.color = settings.getColor(3, false);
				someTrial.clip = settings.getAudioClip(3, false);
			}
		}//end switch
		
	
	}
}
