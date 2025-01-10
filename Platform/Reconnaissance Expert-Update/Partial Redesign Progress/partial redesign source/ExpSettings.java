
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//for graphics
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.Dimension;
import acm.graphics.GPoint;


/**
 * Keeps track of all of the settings for a single Experiment. 
 * By Experiment, I mean a single running of OneButtonTracker. 
 * @author BenPinzone
 *
 */
public class ExpSettings {
	
	public ExpSettings(){
		//variables marked final must be initialized in this constructor.
		loadAudioClips("audioclip_list.txt");
		
		dimension = calculateDimension();
		
		trackerRectBound = calculate_trackerRectBound();
		labelRectBound = calculate_labelRectBound();
		recommenderRectBound = calculate_recommenderRectBound();
		imagesRectBound = trackerRectBound; //tracker and images take up the same space. But the two elements will never be showing at the same time. 
		
		
		double trackerHalfWidth = trackerRectBound.getWidth() / 2;
		extremeInOldGraphicsCoords = trackerHalfWidth - (trackerGraphicBufferNormalized  * (trackerHalfWidth/1000)); //scaling to compute the scaling...
		
	}
	
	//*****************************************************
	//FOR GRAPHICS
	
	//Height ratios for elements. 
	private static final double trackerHeightProportion = 0.9;
	private static final double labelHeight = 65;
	private static final double recommenderHeightProportion = 0.1;
	
	//Bounds
	private final ElementRectBound trackerRectBound;
	private final ElementRectBound labelRectBound;
	private final ElementRectBound recommenderRectBound;
	private final ElementRectBound imagesRectBound;
	public ElementRectBound getTrackerRectBound()    { return trackerRectBound;}
	public ElementRectBound getLabelRectBound()      { return labelRectBound;}
	public ElementRectBound getRecommenderRectBound(){ return recommenderRectBound;}
	public ElementRectBound getImagesRectBound()     { return imagesRectBound;}
	
	//dimensions
	private final Dimension dimension;
	public Dimension getDimension(){ return dimension;}
	private Dimension calculateDimension(){
		
		Dimension maxDimension = new Dimension(0, 0);
		
		//taken from:
		//https://docs.oracle.com/javase/7/docs/api/java/awt/GraphicsConfiguration.html
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDeviceArray = graphicsEnvironment.getScreenDevices();
		
		for(int j = 0; j < graphicsDeviceArray.length; j++){
			
			GraphicsDevice graphicsDevice = graphicsDeviceArray[j];
			GraphicsConfiguration[] graphicsConfiguration = graphicsDevice.getConfigurations();
			
			for(int i = 0; i < graphicsConfiguration.length; i++){

				Dimension currentDim = graphicsConfiguration[i].getBounds().getSize();
				if(currentDim.height > maxDimension.height){
					maxDimension = currentDim;
				}
			
			}
		}
		
		maxDimension.setSize((int)(maxDimension.getWidth()), (int)(maxDimension.getHeight() * 0.92)); //resizing height by 95% makes everything visible. good. 
		return maxDimension;
	}
	
	private final double extremeInOldGraphicsCoords;
	public double getExtremeInOldGraphicsCoords(){ return extremeInOldGraphicsCoords;}
	
	private static final double trackerGraphicBufferNormalized = 120; //adjusted until the tracker stayed on screen. But yes, its still normalized.
	
	//defined in terms of labelHeight
	private ElementRectBound calculate_trackerRectBound(){
		
		double height = (dimension.getHeight() - labelHeight) * trackerHeightProportion;
		double width = height;
		
		double xPos = (dimension.getWidth() / 2) - (width / 2);
		double yPos = 0;
		
		return new ElementRectBound(new GPoint(xPos, yPos), width, height);
		
	}
	
	//defined in terms of trackerRectBound
	private ElementRectBound calculate_labelRectBound(){
		
		double height = labelHeight;
		double width = trackerRectBound.getWidth();
		
		double xPos = trackerRectBound.getBottomLeft().getX();
		double yPos = trackerRectBound.getBottomLeft().getY();
		
		return new ElementRectBound(new GPoint(xPos, yPos), width, height);
	}
	
	//defined in terms of labelRectBound
	private ElementRectBound calculate_recommenderRectBound(){
		
		double height = (dimension.getHeight() - labelHeight) * recommenderHeightProportion;
		double width = labelRectBound.getWidth();
		
		double xPos = labelRectBound.getBottomLeft().getX();
		double yPos = labelRectBound.getBottomLeft().getY();
		
		return new ElementRectBound(new GPoint(xPos, yPos), width, height);
	}
	
	
	
	
	//*****************************************************
	
	//REPLACEMENT STATUS OF ALL OF THIS: This is mostly done, but there is deeper hard-coded stuff!
	
	public boolean isControlRun;
	
	
	public boolean isUsingNewGraphics;
	
	//Both components of the buffet force will be multiplied by this number. For legacy settings, set to 1.0.
	public double trackerDifficultyMultiplier; //by default;

	/**
	 * The number of tracking only practice trials. Originally 30.
	 */
	public int trackingPTrials;
	
	/**
	 * The number of tracking and detection practicetrials Originally 8.
	 */
	public int combinedPTrials; 
	
	/**
	 * The number of experiment trials. Originally 100.
	 */
	public int experimentTrials; 
	
	/**
	 * True if the alarm is binary. False otherwise.
	 */
	public boolean alarmIsBinary;
	
	/**
	 * True if the participant receives a visual alert. False otherwise.
	 */
	public boolean visualAlert;
		
	/**
	 * True if the participant receives an auditory alert. False otherwise.
	 */
	public boolean auditoryAlert;
	
	/**
	 * True if the participant should receive only danger alerts. False otherwise.
	 */
	public boolean dangerAlertsOnly;
	
	/**
	 * The overall reliability of the recommender.
	 */
	public double overallReliability;
	
	
	/**
	 * The reliability profile displayed to the user. Info about reliability is displayed using the message UI components.
	 */
	public ReliabilityCalculator.ReliabilityProfile profile;
	
	//the number of trials between surveys
	/**
	 * The number of trials between surveys. 0 = no surveys. 1 = survey after every trial. 2 = survey after every other trial. 3 = after every third trial. etc. 
	 * <p>
	 * 
	 */
	//LOOK. DESCRIBE WHEN IT HAPPENS- tracking only, combined, experiment, etc. 
	public int surveyFreq;
	
	
	/**
	 * The number of experiment trials between survey reminders. Same usage and meaning as surveyFreq. Happens only during experiment trials. 
	 */
	public int surveyReminderFreq;
	
	
	/**
	 * True if there will be a survey reminder at the end of the experiment. False otherwise.
	 */
	public boolean endOfExpSurveyReminderEnabled;
	
	
	/**
	 * The number of experiment trials between breaks. Same usage and meaning as surveyFreq. Happens only during experiment trials.
	 */
	public int breakFreq;
	
	/**
	 * True if the image/warning generation is random. False otherwise. 
	 */
	public boolean genIsRandom;
	
	public String msg1;
	public String msg2;
	
	
	
	
	//*****************************
	//ABOUT THE quotaList STRUCTURE. 
	/** 
	 * Please Note: This Javadoc scrolls. Its quite long. 
	 * <p>
	 * The variable below has a very precise order to its elements. 
	 * Its data is populated in the SettingsDialog. Take a look at the call hierachy. 
	 * If the alarm is binary, its length is 4. 
	 * If the alarm is likelihood, its length is 8. 
	 * Regardless of the alarm type, each Integer represents the number of times a certain scenario will occur after all trials are complete. 
	 * For example, one scenario is "There is truly a threat, and the alert says possibly clear."
	 * Therefore, in a binary experiment, there are 4 scenarios. In a likelihood experiment, there are 8 scenarios.
	 * There is a scenario for every TrueState-AlertState combination.
	 * <p>
	 * The static final int fields below are named after each scenario, and their values are the indeces in the quotaList. 
	 * Of course, the quotaList is either for binary or likelihood. So during any given experiment, one set of scenario indeces will be irrelevent. 
	 * <p>
	 * It is important to understand that the order of this ArrayList is critical. 
	 * The scenarios where there is truly a threat always come first, followed by the scenarios where there is truly no threat. 
	 * This is especially important to know when viewing the code that populates the warning data for the trials. 
	 * ImageSetGenerator's method setupTrialWarnings depends on this specific structure. 
	*/
	public ArrayList<Integer> quotaList;
	
	//BELOW ARE THE INDEXES THAT ARE TO BE USED TO ACCESS ELEMENTS IN THE quotaList.
	
	//BINARY
	//true threat, alerted danger (hit)
	public static final int hitIndex = 0;
	//true threat, alerted clear (miss) 
	public static final int missIndex = 1; 
	//true clear, alerted danger (false alarm)
	public static final int falseAlarmIndex = 2;
	//true clear, alerted clear (correct rejection)
	public static final int correctRejectionIndex = 3;
	
	
	//LIKELIHOOD
	//naming convention: <trueState>Given<AlarmAdvisory>
	public static final int threatGivenDangerIndex = 0;   //hit
	public static final int threatGivenWarningIndex = 1;  //hit
	public static final int threatGivenPosClrIndex = 2;   //miss
	public static final int threatGivenClrIndex = 3;      //miss
	
	public static final int clearGivenDangerIndex = 4;   //false alarm
	public static final int clearGivenWarningIndex = 5;  //false alarm
	public static final int clearGivenPosClrIndex = 6;   //correct rejection
	public static final int clearGivenClrIndex = 7;      //correct rejection


	//*****************************************************
	//ABOUT THE inputImageSet STRUCTURE.
	
	/**
	 * Please Note: This Javadoc scrolls. Its quite long. 
	 * <p>
	 * This structure is only used when the image/warning generation is "input". (As opposed to random).
	 * It represents the images used for the experiment. It specifies both trial number and image position.
	 * The Strings are image file names. Just the file name, no relative file path. 
	 * <p>
	 * The inner list represents a list of images. Its length is always the number of experiment trials. 
	 * For example, the first inner list represents: {image0 for trial 0, image0 for trial 1, image0 for trial 2, ...}
	 * <p>
	 * The outer list represents the image position. Its length is always 4.
	 * For example, the outer list looks like: {List of images for position 0, ...position 1, ...position 2, ... position 3}
	 * <p>
	 * For example, if you wanted to access the image in location 2 in trial 6, use:   inputImageSet.get(2).get(6);
	 * See non-JavaDoc comments by declaration for further visual detail.
	 * It is structured this way because of the UI design, and how that data is extracted from the text fields.
	 */
	
	//VISUALLY...
	//position 0
		//image for trial 0
		//image for trial 1
		//iamge for trial 2
		//image for trial 3
		//image for trial 4
		//image for trial 5
	//position 1
		//...
	//position 2
		//...
	//position 3
		//....
	//done.
	public ArrayList<ArrayList<String>> inputImageSet;
	
	//*****************************************************
	
	
	/**
	 * This structure is only used when the image/warning generation is "input". (As opposted to random).
	 * It represents the warnings that are given in order of the trials. 
	 * For example, to get the warning given during trial 6, use: inputWarningSet.get(6);
	 */
	public ArrayList<String> inputWarningSet;
	


	
	//*****************************************************
	//everything below here is moved here from the QuotaSet class.
	//*****************************************************

	
	public static final Color[] BINARY_COLORS = new Color [] { Color.RED, Color.GREEN };
	public static final Color[] LIKELIHOOD_COLORS = new Color [] { Color.RED, new Color(255, 201, 14), new Color(181, 230, 29), Color.GREEN };
	public static final String[] RECOMMENDATION_STRINGS = new String [] { "DANGER", "CAUTION", "POSSIBLY_CLEAR", "CLEAR" };
	
	//The names of audio clip files.
	private ArrayList<String> clips = new ArrayList<String>();
	
	
	//returns the number of trials that have a threat
	public int present () {
		
		int count = 0;
		for (int i = 0; i < quotaList.size() / 2; i++) {
			count += quotaList.get(i);
		}
		return count;
	}
	
	//returns the number of trials that have no threat.
	public int absent () {
		int count = 0;
		for (int i = quotaList.size() / 2; i < quotaList.size(); i++) {
			count += quotaList.get(i);
		}
		return count;
	}
	
	//some things below should probably be made static
	//see next comment below for index convention.
	public Color getColor (int index, boolean isBinaryAlarm) {
									   //red,green     //red,amber,light green, green
		Color[] list = isBinaryAlarm ? BINARY_COLORS : LIKELIHOOD_COLORS;
		
		//this % functionality must be intended as a wrap around somewhere else...
		return list[index % list.length];
	}
	
	
	private void loadAudioClips (String fileName) {
		try {
			Scanner fin = new Scanner(new BufferedReader(new FileReader(fileName)));
			while (fin.hasNextLine()) {
				String s = fin.nextLine();
				clips.add(s);
				
			
			}
			fin.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//because of the order of "audioclip_list.txt"
	//indeces are:   0=danger, 1=caution, 2=possible, 3=clear. 
	public String getAudioClip (int index, boolean isBinaryAlarm) {
		if (isBinaryAlarm) {
			if (index % 2 == 0) return clips.get(0);
			else return clips.get(3);
		}
		else {
			return clips.get(index % 4);
		}
 	}
	
	
	
	
	

}
