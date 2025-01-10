
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.ButtonGroup;

import java.util.ArrayList;
import java.util.Arrays;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Dimension;

import java.text.DecimalFormat;



//NEEDS TO BE RE-DESIGNED FOR CONSISTENCY. DO IT UNDER LISTENER MODEL. Startign from top down maybe. 
//Ex: can't enabled alert modalities if control run is on. Think of the best way. 
//You can put text directly with JRadioButtons... 
//fix maybe have control run and alert modality in the same panel. 
//also, strike a balance between when to throw an error dialog on pressing start, and when to correct the error for them.
//maybe have background of conflicting panels turn red. 


/**
 * Represents the experiment settings window. 
 * The data gathered from this dialog will be pushed into the ExpSettings object that is passed in to the constructor.
 * See furthur detail on the UI design near declaration.
 * @author Ben Pinzone
 *
 */
public class SettingsDialog extends JDialog implements ActionListener {
	
	/* UI DESGN CONCEPT OVERVIEW
	 * At the highest level, the frame contains only the mainPanel. 
	 * The mainPanel contains two sub-panels: leftPanel and rightPanel. 
	 * leftPanel and rightPanel both contain many special sub-panels. See below for the description of special sub-panels.
	 * The special sub-panels are always stacked vertically. 
	 * leftPanel has a single vertical stack of special sub-panels, as does rightPanel.
	 * PLEASE NOTE: *Every* panel is managed by the GridBagLayout. See: https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
	 */
	
	/*
	 * SPECIAL SUB PANELS
	 * These are panels that I have made inherit from JPanel and given extra functionality. 
	 * The idea is that I want a JPanel that I can grab data from. 
	 * Essentially, each special sub panel is a JPanel that has UI elements appropriate to the data being gathered,
	 * 	and they have getter/setters for the information they are collecting.  
	 */
	
	/*
	 * General Notes
	 * A button's selection status gets changed before the actionPerformed method runs.
	 */
	
	// The ExpSettings that data from this UI will be pushed to on exit.
	private ExpSettings someSettings;

	//HIGH LEVEL PANELS
	private JPanel mainPanel;
	private GridBagConstraints main_gbc;
	
	private JPanel leftPanel;
	private GridBagConstraints left_gbc;
	
	private JPanel rightPanel;
	private GridBagConstraints right_gbc;
	

	//******************************************
	// SPECIAL SUB-PANELS
	
	private ControlRunPanel controlRunPanel = new ControlRunPanel();
	private LegacyPanel legacyPanel = new LegacyPanel();
	private GraphicsModePanel graphicsModePanel = new GraphicsModePanel();
	private TrackerDifficultyPanel trackerDifficultyPanel = new TrackerDifficultyPanel();
	private TrialPanel trialPanel = new TrialPanel();
	private AlarmAlertPanel alarmAlertPanel = new AlarmAlertPanel();
	private SurveyPanel surveyPanel = new SurveyPanel();
	private BreakPanel breakPanel = new BreakPanel();
	private BinaryQuotaPanel binaryQuotaPanel = new BinaryQuotaPanel();
	private LikelihoodQuotaPanel likelihoodQuotaPanel = new LikelihoodQuotaPanel();
	private GenerationTypePanel pictureWarningGenPanel = new GenerationTypePanel();
	private GenerationPanel generationPanel = new GenerationPanel();
	private CalculatedReliabilityPanel calculatedReliabilityPanel = new CalculatedReliabilityPanel();
	private MessagePanel messagePanel = new MessagePanel();
	private ReliabilityProfilePanel reliabilityProfilePanel = new ReliabilityProfilePanel();
	
	//******************************************
	//TRACK DYNAMIC PANELS
	
	//Keeps track of which quota panel is displaying. 
	//True if binary quota panel is diplaying, false if likelihood quota panel is displaying
	private boolean binaryQuotaPanelDisplaying; 
	private int quotaPanelXPos = 0;
	private int quotaPanelYPos; //keeps track of the quota panel's Y position within the grid layout
	
	//Keeps track of if the generation panel is displaying.
	private boolean generationPanelDisplaying;
	private int generationPanelXPos = 0;
	private int generationPanelYPos; //keeps track of the generation panel's Y position within the grid layout.
	
	//******************************************
	
	//Button pressed when the settings are good. Input checking will be done.
	//If input is back, the dialog will not close.
	JButton startButton = new JButton("Start");
	

	public SettingsDialog(Frame parent, ExpSettings settings_in) {

		super(parent, "Experiment Settings", true);
		someSettings = settings_in;

		placeComponents();
		
		//two of these, just so both tables get populated right away.
		setToLegacy(70, false);
		setToLegacy(70, true); 
		mainPanel.revalidate(); //look. may need to change.
		
		//when the start button is clicked, SettingDialog's actionPerformed method will run.
		startButton.addActionListener(this);

		getContentPane().add(mainPanel);
		pack();
			
	}
	
	/**
	 * Adds someComponent to somePanel at position x, y in the gridLayout. 
	 * somePanel MUST be managed by GridBagLayout. 
	 * someConstraint MUST be associated with somePanel. 
	 * Modifies gridx and gridy of someConstraint.
	 * @param somePanel
	 * @param someComponent
	 * @param someConstraint
	 * @param x
	 * @param y
	 */
	private void CustomAdd(JPanel somePanel, Component someComponent, GridBagConstraints someConstraint, int x, int y){
		someConstraint.gridx = x;
		someConstraint.gridy = y;
		somePanel.add(someComponent, someConstraint);
	}
	
	private void placeComponents(){
		
		//Overview: Main panel contains left and right subpanels. Left and right subpanels both contain the special private class panels. 
		
		//setup main panel and its layout.
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		main_gbc = new GridBagConstraints();
		main_gbc.insets = new Insets(4, 4, 4, 4); //spacing between components
		
		//setup left panel and its layout
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		left_gbc = new GridBagConstraints();
		left_gbc.insets = new Insets(1, 1, 1, 1);
		
		//setup left panel and its layout
		rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());
		right_gbc = new GridBagConstraints();
		right_gbc.insets = new Insets(1, 1, 1, 1);
		
		//***************
		//SETUP LEFT PANEL
		int leftYCon = 0;
		left_gbc.anchor = GridBagConstraints.NORTHWEST;
		
		CustomAdd(leftPanel, controlRunPanel, left_gbc, 0, leftYCon);
		leftYCon++;
								
		CustomAdd(leftPanel, legacyPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, graphicsModePanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, trackerDifficultyPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, trialPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, alarmAlertPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, surveyPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		CustomAdd(leftPanel, breakPanel, left_gbc, 0, leftYCon);
		leftYCon++;
		
		//*****************
		//SETUP RIGHT PANEL
		int rightYCon = 0;
		right_gbc.anchor = GridBagConstraints.NORTHWEST;
		
		CustomAdd(rightPanel, likelihoodQuotaPanel, right_gbc, 0, rightYCon); //do not separate this line from the one below it.
		binaryQuotaPanelDisplaying = false;
		quotaPanelYPos = rightYCon++; //do not separate this line from the one above it. 
		rightYCon++;
		
		CustomAdd(rightPanel, pictureWarningGenPanel, right_gbc, 0, rightYCon);
		rightYCon++;
		
		//The following line is commented out because by default, we do not want to display the generation panel.
		//CustomAdd(rightPanel, generationPanel, gbc, 0, rightYCon); //do not separate this line from the one below it.
		generationPanelDisplaying = false; //if the above line is uncommented, make this true.
		generationPanelYPos = rightYCon; //do not separate this line from the one above it.
		rightYCon++;
		
		CustomAdd(rightPanel, calculatedReliabilityPanel, right_gbc, 0, rightYCon);
		rightYCon++;
		
		CustomAdd(rightPanel, messagePanel, right_gbc, 0, rightYCon);
		rightYCon++;
		
		CustomAdd(rightPanel, reliabilityProfilePanel, right_gbc, 0, rightYCon);
		rightYCon++;
		
		//****************
		//SETUP MAIN PANEL
		main_gbc.anchor = GridBagConstraints.NORTHWEST;
		
		CustomAdd(mainPanel, leftPanel, main_gbc, 0, 0);
		
		CustomAdd(mainPanel, rightPanel, main_gbc, 1, 0);
		
		main_gbc.anchor = GridBagConstraints.EAST;
		CustomAdd(mainPanel, startButton, main_gbc, 1, 1);
		main_gbc.anchor = GridBagConstraints.NORTHWEST;
		
		
		
		
		
	}
	

	//Run when startButton is hit.
	//look! modify to include whatever will be done to process "input" image/warning generation.
	public void actionPerformed(ActionEvent e) {
		
		//extract
		someSettings.isControlRun = controlRunPanel.getIsControlRun();
		
		someSettings.isUsingNewGraphics = graphicsModePanel.getIsUsingNewGraphics();
		
		someSettings.trackingPTrials  = trialPanel.getTrackPrac();
		someSettings.combinedPTrials  = trialPanel.getCombPrac();
		someSettings.experimentTrials = trialPanel.getTrials();
		
		someSettings.alarmIsBinary    = alarmAlertPanel.getIsBinaryMode();
		someSettings.visualAlert      = alarmAlertPanel.getVisualAlertEnabled();
		someSettings.auditoryAlert    = alarmAlertPanel.getAudioAlertEnabled();
		someSettings.dangerAlertsOnly = alarmAlertPanel.getDangerAlertsOnly();
		someSettings.confidentMode = alarmAlertPanel.getConfidentMode();

		someSettings.overallReliability = calculatedReliabilityPanel.getReliability(ReliabilityCalculator.ReliabilityType.OVERALL);
		
		someSettings.trackerDifficultyMultiplier = trackerDifficultyPanel.getMultiplier(); 
		
		someSettings.surveyFreq = surveyPanel.getSurveyFrequency();
		someSettings.surveyReminderFreq = surveyPanel.getSurveyReminderFrequency();
		someSettings.endOfExpSurveyReminderEnabled = surveyPanel.getEndOfExpReminderEnabled();
		
		someSettings.breakFreq   = breakPanel.getFreqency();
		someSettings.genIsRandom = pictureWarningGenPanel.getIsRandomGen();
		
		someSettings.msg1 = messagePanel.getMessage1();
		someSettings.msg2 = messagePanel.getMessage2();
		
		someSettings.profile = reliabilityProfilePanel.getReliabilityProfile();
		
		
		//reference
		//binary quota indeces: //0 numHit, 1 numMiss, 2 numFalseAlarm, 3 numCorrectRejection.
		
		//likelihood quota indeces: //<0#threat, alert=danger> <1#threat, alert=warning> <2#threat, alert=posClear> <3#threat, alert=clear> 
		//     ,<4#clear, alert=danger> <5#clear, alert=warning> <6#clear, alert=posClr> <7#clear, alert=clear>
		
		
		
		//QUOTA LIST
		//binary
		if(someSettings.alarmIsBinary){
			someSettings.quotaList = binaryQuotaPanel.getBinaryQuotaList();
		}
		
		//likelihood
		else{
			someSettings.quotaList = likelihoodQuotaPanel.getLikelihoodQuotaList();	
		}
		
		//IMAGE/WARNING GENERATION.
		if(!someSettings.genIsRandom){
			someSettings.inputImageSet = generationPanel.getInputImageSet();
			someSettings.inputWarningSet = generationPanel.getInputWarningSet();
			
		}
		else{
			someSettings.inputImageSet = null;
			someSettings.inputWarningSet = null;
		}
		
		if(validateInput()){
			dispose();	
		}
		
		
	
	}
	
	//returns true if settings are valid, false otherwise.
	private boolean validateInput(){
		
		//TODO - make sure that trial numbers, quotas, and input image/warning names ALL line up...
		
		//after each requirement, allGood will be anded witih itself and the current eval, and allGood will be returned at the end.
		//so if any conditions are false, it will be stored in allGood
		
		
		//reference
		//binary quota indeces: //0 numHit, 1 numMiss, 2 numFalseAlarm, 3 numCorrectRejection.
		
		//likelihood quota indeces: //<0#threat, alert=danger> <1#threat, alert=warning> <2#threat, alert=posClear> <3#threat, alert=clear> 
		//     ,<4#clear, alert=danger> <5#clear, alert=warning> <6#clear, alert=posClr> <7#clear, alert=clear>
		
		
		//Requirements:
		//LOOK. ADD MORE REQUIREMENTS!!!! todo.
		/* #1 Number of Experimental trials must match the sum of quota table.
		 * #2 If the generation is input, then make sure the images and warnings input line up with quotas.
		 * 	//wo#2.5 If the generation is input, then the number of rows (for every pane) matches the number of experiment trials. It properly ignores newlines that have no content.
		 * #3 tracker difficulty is between 0.5 and 3.0
		 * #4 Number of combined practice trials is <= 8
		 * #5 Must have at least 1 tracking only practiec trial, combined practice trial, and experiment trial.
		 * 
		 * #6 If generation is random, make sure we have enough images to provide for the number of trials, threat images, etc. Based solely on image counts and the quota table. 
		 * 			(not done for "input", because "input" should have complete freedom as long as the images exist.
		 * 
		 * #7 Danger alerts only and likelihood alarm mode cannot both be enabled.
		 * 
		 * 
		 *  We must have enough images to provide for the number of trials, especially present images.
		 *  if generation is input, then their file names and warnings must match the quota text fields.
		 * 
		 *  other restrictions? like image repeats? anythign else?
		 */
		
		//this if-else structure could be more efficient.
		
		
		//#1 
		int totalTrialsByTable = 0;
		for(int i = 0; i < someSettings.quotaList.size(); i++){
			totalTrialsByTable += someSettings.quotaList.get(i);
		}
		if(!(totalTrialsByTable == trialPanel.getTrials())){
			JOptionPane.showMessageDialog(this, "Number of Experiment Trials and Quota Table do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		
		//if generation is input
		if(!pictureWarningGenPanel.getIsRandomGen()){ //could have done someSettings.genIsRandom
			
			//#2.5   (#2.5 is checked first because if it is violated, #2 could have trouble parsing.)
			for(int positionEvaluating = 0; positionEvaluating < 4; positionEvaluating++){
				if(someSettings.inputImageSet.get(positionEvaluating).size() != someSettings.experimentTrials){
					JOptionPane.showMessageDialog(this, "The number of rows in an image generation panel does not match the number of Experiment Trials.", "Input Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				if(someSettings.inputWarningSet.size() != someSettings.experimentTrials){
					JOptionPane.showMessageDialog(this, "The number of rows in the warning generation panel does not match the number of Experiment Trials.", "Input Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			
			//#2
			if(!validateGenerationInputQuotasMatch()){
				JOptionPane.showMessageDialog(this, "Quota Table and Input Generation data do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			
		}
		
		//#3
		if(someSettings.trackerDifficultyMultiplier < 0.5 || someSettings.trackerDifficultyMultiplier > 3.0){
			JOptionPane.showMessageDialog(this, "Tracker Difficulty Multiplier must be in [0.5, 3.0]", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//#4
		if(someSettings.combinedPTrials > 8){
			JOptionPane.showMessageDialog(this, "Number of Combined Practice Trials must be 8 or less.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//#5
		//WARNING: If you want to change this requirement, there are many implications for data gathering. Be extremely careful if you change this. It will not work as is. 
		if(someSettings.trackingPTrials < 1){
			JOptionPane.showMessageDialog(this, "Number of Tracking-Only Practice Trials must be at least 1.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//WARNING: If you want to change this requirement, there are many implications for data gathering. Be extremely careful if you change this. It will not work as is.
		if(someSettings.combinedPTrials < 1){
			JOptionPane.showMessageDialog(this, "Number of Tracking+Detection Practice Trials must be at least 1.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//WARNING: If you want to change this requirement, there are many implications for data gathering. Be extremely careful if you change this. It will not work as is.
		if(someSettings.experimentTrials < 1){
			JOptionPane.showMessageDialog(this, "Number of Experiment Trials must be at least 1.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//#6
		if(someSettings.genIsRandom){
			int numPresentImagesNeeded = someSettings.present(); //only valid because during random generation, there is at most one threat in a set of 4 images.
			int numAbsentImagesNeeded = someSettings.experimentTrials * 4 - numPresentImagesNeeded; 
			if(numPresentImagesNeeded > TrackerConstants.numPresentImagesInDatabase){
				JOptionPane.showMessageDialog(this, "There are not enough threat images in the database to support your request, based on the quota table", "Input Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(numAbsentImagesNeeded > TrackerConstants.numAbsentImagesInDatabase){
				JOptionPane.showMessageDialog(this, "There are not enough clear images in the database to support your request, based on the quota table", "Input Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
		}
		
		//#7
		if(!someSettings.alarmIsBinary && someSettings.dangerAlertsOnly){
			JOptionPane.showMessageDialog(this, "Danger Alerts Only mode can only be used with Binary Alarm mode.", "Input Error", JOptionPane.ERROR_MESSAGE);
		}
		
		
		
		
		
		
		return true;
		
	}
	
	private boolean validateGenerationInputQuotasMatch(){
	
		
		//LOOK. TODO. Make sure that the number of trials matches the number of lines. ei, each position array is equal to that number of trials.
		
		//someSettings.inputImageSet = generationPanel.getInputImageSet();
		//someSettings.inputWarningSet = generationPanel.getInputWarningSet();

		//visually...
		//position 0
			//image for trial 0
			//image for trial 1
			//iamge for trial 2
			//image for trial 3
			//image for trial 4
			//image for trial 5
			//...
		//position 1
			//image for trial 0
			//image for trial 1
			//iamge for trial 2
			//image for trial 3
			//image for trial 4
			//image for trial 5
			//...
		//position2 
			//image for trial 0
			//image for trial 1
			//iamge for trial 2
			//image for trial 3
			//image for trial 4
			//image for trial 5
			//...
		//position3
			//image for trial 0
			//image for trial 1
			//iamge for trial 2
			//image for trial 3
			//image for trial 4
			//image for trial 5
			//...
		
		
		//Each element of array will be decremented each time a trial of a certain type is seen. In order to pass, all entries must be zero at the end.
		int[] copyOfCorrectQuotaList = new int[someSettings.quotaList.size()];   
		
		//Must do this because quotaList stores objects of type Integer, which are immutable. Also, it seems as if the toArray method of ArrayList is not smart enough to handle automatic wrappign/unwrapping of Integer.
		for(int i = 0; i < copyOfCorrectQuotaList.length; i++){
			copyOfCorrectQuotaList[i] = someSettings.quotaList.get(i);
		}
		
		
		ArrayList<String> workingImageSet = new ArrayList<String>();
		String workingImageFileName;
		String workingAlertToGive;
		boolean workingImageSetContainsThreat;
		
		
		//yes, some of this code is copy-paste. I'd rather keep it separated though. 
		for(int trialsSeen = 0; trialsSeen < someSettings.experimentTrials; trialsSeen++){
			
			workingImageSet.clear();
			
			//WORKING IMAGE SET
			for(int imagesAdded = 0; imagesAdded < 4; imagesAdded++){
				//                             			     accessing position  accessing image	
				workingImageFileName = someSettings.inputImageSet.get(imagesAdded).get(trialsSeen);
				if(!workingImageFileName.endsWith(".png")){
					JOptionPane.showMessageDialog(this, "Invalid file name in trial row: " + (trialsSeen + 1) + ". Must end in .png", "Input Error", JOptionPane.ERROR_MESSAGE);
					return false;
					
				}
				workingImageFileName = ImageSetGenerator.generateFullFilePath(workingImageFileName);
				
				workingImageSet.add(workingImageFileName);
			}
			
			//WORKING ALERT TO GIVE
			workingAlertToGive = someSettings.inputWarningSet.get(trialsSeen);
			//WORKING IMAGE SET CONTAINS THREAT
			workingImageSetContainsThreat = ImageSetGenerator.containsThreat(workingImageSet);
			
			
			//DECREMENTING TO CHECK NUMBERS.
			switch(workingAlertToGive){
			
				//used for both binary and likelihood indication.
				case "red" : {
					//binary
					if(someSettings.alarmIsBinary){
						//threat
						if(workingImageSetContainsThreat){
							copyOfCorrectQuotaList[ExpSettings.hitIndex]--;
						}
						//clear
						else{
							copyOfCorrectQuotaList[ExpSettings.falseAlarmIndex]--;
						}
					}
					//likelihood
					else{
						//threat
						if(workingImageSetContainsThreat){
							copyOfCorrectQuotaList[ExpSettings.threatGivenDangerIndex]--;
						}
						//clear
						else{
							copyOfCorrectQuotaList[ExpSettings.clearGivenDangerIndex]--;
						}
						
					}
					
					
				}break;
				
				//used only for likelihood
				case "amber" : {
					if(someSettings.alarmIsBinary){
						System.out.println("Bad because alarm is incorrectly set to binary.");
						return false;
					}
					
					//threat
					if(workingImageSetContainsThreat){
						copyOfCorrectQuotaList[ExpSettings.threatGivenWarningIndex]--;
					}
					//clear
					else{
						copyOfCorrectQuotaList[ExpSettings.clearGivenWarningIndex]--;
					}
					
				}break;
				
				//used only or likelihood
				case "lightGreen" : {
					if(someSettings.alarmIsBinary){
						System.out.println("Bad because alarm is incorrectly set to binary.");
						return false;
					}
					
					if(workingImageSetContainsThreat){
						copyOfCorrectQuotaList[ExpSettings.threatGivenPosClrIndex]--;
					}
					else{
						copyOfCorrectQuotaList[ExpSettings.clearGivenPosClrIndex]--;
					}
					
				}break;
				
				//used only or likelihood
				case "darkGreen" : {
					if(someSettings.alarmIsBinary){
						System.out.println("Bad because alarm is incorrectly set to binary.");
						return false;}
					
					if(workingImageSetContainsThreat){
						copyOfCorrectQuotaList[ExpSettings.threatGivenClrIndex]--;
					}
					else{
						copyOfCorrectQuotaList[ExpSettings.clearGivenClrIndex]--;
					}
					
				}break;
				
				//used only for binary
				case "green" : {
					if(!someSettings.alarmIsBinary){
						System.out.println("Bad because alarm is incorrectly set to likelihood.");
						return false;
					}
					
					if(workingImageSetContainsThreat){
						copyOfCorrectQuotaList[ExpSettings.missIndex]--;
					}
					else{
						copyOfCorrectQuotaList[ExpSettings.correctRejectionIndex]--;
					}
					
				}break;
			}//end switch
			
			
		}//end for loop.
		
		//if input is good, then all elements of copyOfCorrectQuotaList should be zero.
		for(int i = 0; i < copyOfCorrectQuotaList.length; i++){
			if(copyOfCorrectQuotaList[i] != 0){
				System.out.println("Bad because quota set at: " + i + " did not reach zero. The original number was: " + someSettings.quotaList.get(i));
				System.out.println("It was only decremented to: " + copyOfCorrectQuotaList[i]);
				return false;
			}
		}
		
		return true;
	
	}


	//*******************************************************
	//*******************************************************
	/* A review of "lambda syntax". 
	 * They are not true lambdas. 
	 * It is a shortcut for creating an anonymous class in the special case that the class is implementing an interface that contains a single method stub.
	 * An anonymous class is one defined in the new expression that is creating the instance of the class.
	 * 
	 * Example:
	 * b.addActionListener(e -> {
	 * 		//some code
	 * });
	 * 
	 * Is just a syntactical shortcut for
	 * 
	 * b.addActionListener(new ActionListener(){
	 * 		public void actionPerormed(ActionEvent e){
	 * 			//some code
	 * 		}
	 * });
	 * 
	 */
	//*******************************************************
	//HERE ARE THE SUB PANELS DECLARED AS INNER CLASSES
	
	//HOW ALL THESE PANELS TALK AND LISTEN TO EACH OTHER COULD USE SOME RE-DESIGNING.
	//YES. IT NEEDS RE-DESIGNING. 
	
	
	
	//A private inner class.
	
	private class ControlRunPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel controlRunLabel = new JLabel("Control Run:");
		private JLabel yesLabel = new JLabel("Yes");
		private JLabel noLabel = new JLabel("No");
		
		private JRadioButton yesButton = new JRadioButton();
		private JRadioButton noButton = new JRadioButton();
		private ButtonGroup controlButtonGroup = new ButtonGroup();
		
		
		
		public ControlRunPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons(); 
			
		}
		
		private void placeComponents(){
			CustomAdd(this, controlRunLabel, localCon, 0, 0);
			CustomAdd(this, yesButton, localCon, 1, 0);
			CustomAdd(this, yesLabel, localCon, 2, 0);
			CustomAdd(this, noButton, localCon, 3, 0);
			CustomAdd(this, noLabel, localCon, 4, 0);
		}
		
		public boolean getIsControlRun(){
			return yesButton.isSelected();
		}
		
		private void setupButtons(){
			
			controlButtonGroup.add(yesButton);
			controlButtonGroup.add(noButton);
			
			noButton.setSelected(true);
			
			yesButton.addActionListener(e -> {
				alarmAlertPanel.setAudioAlertEnabled(false);
				alarmAlertPanel.setVisualAlertEnabled(false);
				alarmAlertPanel.setDangerAlertsOnly(false);
				
				messagePanel.setMessage1("");
				messagePanel.setMessage2("");
				reliabilityProfilePanel.setReliabilityProfile(ReliabilityCalculator.ReliabilityProfile.NONE);
			});
			
			noButton.addActionListener(e -> {
				alarmAlertPanel.setAudioAlertEnabled(true);
				alarmAlertPanel.setVisualAlertEnabled(true);
			});
			
		}
		
		public void setIsControlRun(boolean enabled){
			if(enabled){
				yesButton.setSelected(true);
			}
			else{
				noButton.setSelected(true);
			}
		}
	}
	
	//good
	private class LegacyPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel legacySettingsLabel = new JLabel("Legacy Settings:");
		
		private JButton binary70Button = new JButton("Binary 70");
		private JButton binary80Button = new JButton("Binary 80");
		private JButton binary90Button = new JButton("Binary 90");
		
		private JButton likelihood70Button = new JButton("Likelihood 70");
		private JButton likelihood80Button = new JButton("Likelihood 80");
		private JButton likelihood90Button = new JButton("Likelihood 90");
		
		public LegacyPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons();
		}
		
		private void placeComponents(){
			//row 0
			CustomAdd(this, legacySettingsLabel, localCon, 0, 0);
			
			//col 1
			CustomAdd(this, binary70Button, localCon, 0, 1);
			CustomAdd(this, binary80Button, localCon, 0, 2);
			CustomAdd(this, binary90Button, localCon, 0, 3);
			
			//col 2
			CustomAdd(this, likelihood70Button, localCon, 1, 1);
			CustomAdd(this, likelihood80Button, localCon, 1, 2);
			CustomAdd(this, likelihood90Button, localCon, 1, 3);

			this.binary70Button.setVisible(false);
			this.binary80Button.setVisible(false);
			this.binary90Button.setVisible(false);

			this.likelihood70Button.setVisible(false);
			this.likelihood80Button.setVisible(false);
			this.likelihood90Button.setVisible(false);


		}
		
		private void setupButtons(){
						
			//another use of lambda syntax
			//slightly interesting because legacyButtonListener is an anonymous class that implements ActionListener. 
			//Current type is ActionListener, true type is anonymous.(Name of subclass is not known).
			
			ActionListener legacyButtonListener = e -> {
				
				JButton someButton = (JButton)e.getSource();
				
				if(someButton == binary70Button){
					setToLegacy(70, true);
				}
				else if(someButton == binary80Button){
					setToLegacy(80, true);
				}
				else if(someButton == binary90Button){
					setToLegacy(90, true);
				}
				else if(someButton == likelihood70Button){
					setToLegacy(70, false);
				}
				else if(someButton == likelihood80Button){
					setToLegacy(80, false);
				}
				else if(someButton == likelihood90Button){
					setToLegacy(90, false);
				}		
			};
			
			binary70Button.addActionListener(legacyButtonListener);
			binary80Button.addActionListener(legacyButtonListener);
			binary90Button.addActionListener(legacyButtonListener);
			
			likelihood70Button.addActionListener(legacyButtonListener);
			likelihood80Button.addActionListener(legacyButtonListener);
			likelihood90Button.addActionListener(legacyButtonListener);
		}
		
		
	}
	
	
	private class GraphicsModePanel extends JPanel{
		
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel graphicsModeLabel = new JLabel("Graphics Mode:");
		private JLabel legacyLabel = new JLabel("Legacy");
		private JLabel newLabel = new JLabel("New");
		
		private JRadioButton legacyButton = new JRadioButton();
		private JRadioButton newButton = new JRadioButton();
		private ButtonGroup graphicsButtonGroup = new ButtonGroup();
		
		public GraphicsModePanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons(); 
			
		}
		
		private void placeComponents(){
			CustomAdd(this, graphicsModeLabel, localCon, 0, 0);
			CustomAdd(this, legacyButton, localCon, 1, 0);
			CustomAdd(this, legacyLabel, localCon, 2, 0);
			CustomAdd(this, newButton, localCon, 3, 0);
			CustomAdd(this, newLabel, localCon, 4, 0);
			
		}
		
		private void setupButtons(){
			graphicsButtonGroup.add(legacyButton);
			graphicsButtonGroup.add(newButton);
			legacyButton.setSelected(true);
			
		}
		
		//returns true if we are using new graphics.
		public boolean getIsUsingNewGraphics(){
			return newButton.isSelected();
		}
		
		public void setIsUsingNewGraphics(boolean isUsing){
			if(isUsing){
				newButton.setSelected(true);
			}
			else{
				legacyButton.setSelected(true);
			}
		}
		
	
	}
	
	//good
	private class TrialPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		// number of tracker only practice trials
		private JLabel numTrackPracLabel = new JLabel("Number of tracking practice trials: ");
		private JTextField numTrackPracInput = new JTextField("", 3);

		// number of track AND detection practice trials
		private JLabel numTrackAndDetLabel = new JLabel("Number of tracking+detection practice trials: ");
		private JTextField numTrackAndDetInput = new JTextField("", 3);

		// number of experiment trials
		private JLabel numExpLabel = new JLabel("Number of experimental trials: ");
		private JTextField numExpInput = new JTextField("", 3);
		
		
		public TrialPanel(){
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
		}
		
		private void placeComponents(){

			//change these to use custom add if you want.
			CustomAdd(this, numTrackPracLabel, localCon, 0, 0);
			// input text field
			CustomAdd(this, numTrackPracInput, localCon, 1, 0);
			// number of combined practice
			// label
			CustomAdd(this, numTrackAndDetLabel, localCon, 0, 1);
			// input text field
			CustomAdd(this, numTrackAndDetInput, localCon, 1, 1);
			// number of trials
			// label
			CustomAdd(this, numExpLabel, localCon, 0, 2);
			// input textfield
			CustomAdd(this, numExpInput, localCon, 1, 2);

		}
		
		//number of tracking only practice trials getter/setters
		public void setTrackPrac(int n){
			numTrackPracInput.setText(String.valueOf(n));
		}
		public int getTrackPrac(){
			return Integer.parseInt(numTrackPracInput.getText());
		}
		
		//number of combined practice trials getter/setters
		public void setCombPrac(int n){
			numTrackAndDetInput.setText(String.valueOf(n));
		}
		public int getCombPrac(){
			return Integer.parseInt(numTrackAndDetInput.getText());
		}
		
		//number of trials getter/setters
		public void setTrials(int n){
			numExpInput.setText(String.valueOf(n));
		}
		public int getTrials(){
			return Integer.parseInt(numExpInput.getText());
		}
	}
	
	//ok
	private class AlarmAlertPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		// alarm type - elements
		private JLabel alarmTypeLabel = new JLabel("Alarm Type: ");
		private JLabel likelihoodLabel = new JLabel("Likelihood");
		private JLabel binaryLabel = new JLabel("Binary");
		// in a group
		private ButtonGroup alarmTypeGroup = new ButtonGroup();
		private JRadioButton likelihoodButton = new JRadioButton();
		private JRadioButton binaryButton = new JRadioButton();
		

		// alert modality - elements
		private JLabel alertModalityLabel = new JLabel("Alert Modality: ");
		private JLabel visualLabel = new JLabel("Visual");
		private JLabel auditoryLabel = new JLabel("Auditory");
		// not in a group.
		private JRadioButton visualButton = new JRadioButton();
		private JRadioButton auditoryButton = new JRadioButton();
		
		//danger alerts only - elements
		private JLabel alertConditionLabel = new JLabel("Alert Condition: ");
		private JLabel dangerOnlyLabel = new JLabel("Danger Alerts Only (Binary Mode Only)");
		private JRadioButton dangerOnlyButton = new JRadioButton();

		private JLabel confidentModeLabel = new JLabel("With Confidence");
		private JRadioButton confidentModeButton = new JRadioButton();
		
		
		public AlarmAlertPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons();
		}
		
		private void placeComponents(){
			
			// alarm type
			// label
			CustomAdd(this, alarmTypeLabel, localCon, 0, 0);
			// likelihood button
			CustomAdd(this, likelihoodButton, localCon, 1, 0);
			// likelihood label
			CustomAdd(this, likelihoodLabel, localCon, 2, 0);
			// binary button
			CustomAdd(this, binaryButton, localCon, 3, 0);
			// binary label
			CustomAdd(this, binaryLabel, localCon, 4, 0);

			// alert modality
			// label
			//localCon.gridy = 1; 99% sure i dotn need this. not sure why its still here.
			CustomAdd(this, alertModalityLabel, localCon, 0, 1);
			// visual button
			CustomAdd(this, visualButton, localCon, 1, 1);
			// visual label
			CustomAdd(this, visualLabel, localCon, 2, 1);
			// auditory button
			CustomAdd(this, auditoryButton, localCon, 3, 1);
			// auditory label
			CustomAdd(this, auditoryLabel, localCon, 4, 1);
			
			//danger only alerts
			CustomAdd(this, alertConditionLabel, localCon, 0, 2);
			CustomAdd(this, dangerOnlyButton, localCon, 1, 2);
			localCon.gridwidth = 3;
			CustomAdd(this, dangerOnlyLabel, localCon, 2, 2);
			localCon.gridwidth = 1;
			dangerOnlyLabel.setVisible(false);
			dangerOnlyButton.setVisible(false);

			CustomAdd(this, confidentModeButton, localCon, 1, 3);
			localCon.gridwidth = 3;
			CustomAdd(this, confidentModeLabel, localCon, 2, 3);
			localCon.gridwidth = 1;

			
			
		}
		
		private void setupButtons(){
			
			alarmTypeGroup.add(likelihoodButton);
			alarmTypeGroup.add(binaryButton);
			
			//*******
			likelihoodButton.addActionListener(e -> {
			
				setDangerAlertsOnly(false);
				if(binaryQuotaPanelDisplaying){
					switchToLikelihoodQuotaPanel();
				}
				calculatedReliabilityPanel.recalculate();
				reliabilityProfilePanel.updateReliabilityMessages();
				
			});
			
			//*******
			binaryButton.addActionListener(e -> {
			
				if(!binaryQuotaPanelDisplaying){
					switchToBinaryQuotaPanel();
				}
				calculatedReliabilityPanel.recalculate();
				reliabilityProfilePanel.updateReliabilityMessages();
				
			});
			
			
			//*******
			ActionListener isControlListener = e -> {
			
				if(alarmAlertPanel.getAudioAlertEnabled() || alarmAlertPanel.getVisualAlertEnabled()){
					controlRunPanel.setIsControlRun(false);
				}
				else{
					controlRunPanel.setIsControlRun(true);
					setDangerAlertsOnly(false);
					reliabilityProfilePanel.setReliabilityProfile(ReliabilityCalculator.ReliabilityProfile.NONE);
					reliabilityProfilePanel.updateReliabilityMessages();
				}
		
			};
			
			
			visualButton.addActionListener(isControlListener);
			auditoryButton.addActionListener(isControlListener);
			
			dangerOnlyButton.addActionListener(e ->{
				if(likelihoodButton.isSelected() || controlRunPanel.getIsControlRun()){
					dangerOnlyButton.setSelected(false);
				}
			});
				
		}
		
		public boolean getIsBinaryMode(){
			return binaryButton.isSelected();
		}
		
		public void setIsBinaryMode(boolean isBinary){
			if(isBinary){
				binaryButton.setSelected(true);
			}
			else{
				likelihoodButton.setSelected(true);
			}
		}
		
		public boolean getAudioAlertEnabled(){
			return auditoryButton.isSelected();
		}
		public void setAudioAlertEnabled(boolean isEnabled){
			auditoryButton.setSelected(isEnabled);
		}
		
		public boolean getVisualAlertEnabled(){
			return visualButton.isSelected();
		}
		public void setVisualAlertEnabled(boolean isEnabled){
			visualButton.setSelected(isEnabled);
		}

		public boolean getConfidentMode(){return confidentModeButton.isSelected(); }
		public boolean getDangerAlertsOnly(){
			return dangerOnlyButton.isSelected();
		}
		public void setDangerAlertsOnly(boolean enabled){
			dangerOnlyButton.setSelected(enabled);
		}
		
	}
	
	//good
	private class TrackerDifficultyPanel extends JPanel {
		
		//uses sub panels
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel trackerDifficultyLabel = new JLabel("Tracker Difficulty Multiplier (0.5 - 3.0): ");
		private JTextField multiplierField = new JTextField("", 6);
		
		private JButton easyButton = new JButton("Easy");
		private JButton normalButton = new JButton("Normal");
		private JButton hardButton = new JButton("Hard");
		
		public TrackerDifficultyPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons();
		}
		
		private void placeComponents(){
			
			JPanel topPanel = new JPanel();
			JPanel bottomPanel = new JPanel();
			
			topPanel.setLayout(new GridBagLayout());
			bottomPanel.setLayout(new GridBagLayout());
			
			GridBagConstraints subCon = new GridBagConstraints(); // shared
			subCon.insets = new Insets(1, 1, 1, 1);
			subCon.anchor = GridBagConstraints.WEST;
			
			CustomAdd(topPanel, trackerDifficultyLabel, subCon, 0, 0);
			CustomAdd(topPanel, multiplierField, subCon, 1, 0);
			
			CustomAdd(bottomPanel, easyButton, subCon, 0, 0);
			CustomAdd(bottomPanel, normalButton, subCon, 1, 0);
			CustomAdd(bottomPanel, hardButton, subCon, 2, 0);
			
			CustomAdd(this, topPanel, localCon, 0, 0);
			CustomAdd(this, bottomPanel, localCon, 0, 1);
				
		}
		
		private void setupButtons(){
			
			//I do not want this to be anonymous, becuase it is used more than once. Therefore not using lambda. 
			ActionListener difficultyListener = e -> {
				
				JButton someButton = (JButton)e.getSource();
				if(someButton == easyButton){
					setMultiplier(TrackerConstants.EASY_MULTIPLIER);
				}
				else if(someButton == normalButton){
					setMultiplier(TrackerConstants.NORMAL_MULTIPLIER);
				}
				else if(someButton == hardButton){
					setMultiplier(TrackerConstants.HARD_MULTIPLIER);
				}
			};
			
			easyButton.addActionListener(difficultyListener);
			normalButton.addActionListener(difficultyListener);
			hardButton.addActionListener(difficultyListener);
		}
		
		public double getMultiplier(){
			return Double.parseDouble(multiplierField.getText());
		}
		
		public void setMultiplier(double multiplier){
			multiplierField.setText(String.valueOf(multiplier));
		}
		
		
	}
	
	//good
	//re-name to survey panel
	private class SurveyPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		// trust questionnaire frequency
		private JLabel surveyLabel = new JLabel("Trust survey every");
		private JLabel surveyTrialsLabel = new JLabel("trials. (0 = No surveys)");
		private JTextField surveyInput = new JTextField("", 3);
		
		//new
		private JLabel reminderEveryLabel = new JLabel("Survey Reminder every");
		private JLabel reminderEveryTrialsLabel = new JLabel("exp. trials. (0 = No reminders)");
		private JTextField reminderEveryInput = new JTextField("", 3);
		
		//new
		private JLabel endofAllTrialsReminder = new JLabel("End of all trials survey reminder");
		private JRadioButton endOfAllTrialsReminderButton = new JRadioButton();
		
		
		public SurveyPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();	
		}
		
		private void placeComponents(){
			
			
			
			
			// trust questionnaire
			// label
			CustomAdd(this, surveyLabel, localCon, 0, 0);
			// input
			CustomAdd(this, surveyInput, localCon, 1, 0);
			// "trials" label
			CustomAdd(this, surveyTrialsLabel, localCon, 2, 0);
			
			//frequency
			CustomAdd(this, reminderEveryLabel, localCon, 0, 1);
			CustomAdd(this, reminderEveryInput, localCon, 1, 1);
			CustomAdd(this, reminderEveryTrialsLabel, localCon, 2, 1);
			
			//end of experiment reminder
			CustomAdd(this, endOfAllTrialsReminderButton, localCon, 1, 2);
			CustomAdd(this, endofAllTrialsReminder, localCon, 2, 2);
			
			
		}
		
		//survey frequency
		public int getSurveyFrequency(){
			return Integer.parseInt(surveyInput.getText());
		}
		public void setSurveyFrequency(int n){
			surveyInput.setText(String.valueOf(n));
		}
		
		//survey reminder frequency
		public int getSurveyReminderFrequency(){
			return Integer.parseInt(reminderEveryInput.getText());
		}
		public void setSurveyReminderFrequency(int n){
			reminderEveryInput.setText(String.valueOf(n));
		}
		
		//end of experiment survey reminder
		public boolean getEndOfExpReminderEnabled(){
			return endOfAllTrialsReminderButton.isSelected();
		}
		public void setEndOfExpReminderEnabled(boolean isEnabled){
			endOfAllTrialsReminderButton.setSelected(isEnabled);
		}
		
		
		
		
	}
	
	//good
	private class BreakPanel extends JPanel{
		
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel breakEveryLabel = new JLabel("Break every");
		private JLabel trialsLabel = new JLabel("exp. trials. (0 = No breaks)");
		private JTextField breakInput = new JTextField("", 3);
		
		public BreakPanel(){
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
		}
		
		private void placeComponents(){
			
			// label
			CustomAdd(this, breakEveryLabel, localCon, 0, 0);
			// input
			CustomAdd(this, breakInput, localCon, 1, 0);
			// "trials" label
			CustomAdd(this, trialsLabel, localCon, 2, 0);
			
		}
		
		public int getFreqency(){
			return Integer.parseInt(breakInput.getText());
		}
		public void setFreqency(int n){
			breakInput.setText(String.valueOf(n));
		}
		
	}
	
	//goood
	private class BinaryQuotaPanel extends JPanel{
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		JLabel quotasLabel = new JLabel("Quotas");
		
		JLabel tableCornerLabel = new JLabel("Given v | True >");
		JLabel threatLabel = new JLabel("Threat");
		JLabel clearLabel = new JLabel("Clear");
		
		JLabel dangerLabel = new JLabel("Danger");
		JTextField numHitField = new JTextField("999", 4);
		JTextField numFalseAlarmField = new JTextField("", 4);
		
		JLabel clearLabel2 = new JLabel("Clear"); //cannot add the same label in two different places
		JTextField numMissField = new JTextField("", 4);
		JTextField numCorrectRejectionField = new JTextField("", 4);
		
		public BinaryQuotaPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
		
		}
		
		private void placeComponents(){
			
			CustomAdd(this, quotasLabel, localCon, 0, 0);
			
			CustomAdd(this, tableCornerLabel, localCon, 0, 1);
			CustomAdd(this, threatLabel, localCon, 1, 1);
			CustomAdd(this, clearLabel,  localCon, 2, 1);
			
			CustomAdd(this, dangerLabel,        localCon, 0, 2);
			CustomAdd(this, numHitField,        localCon, 1, 2);
			CustomAdd(this, numFalseAlarmField, localCon, 2, 2);
			
			CustomAdd(this, clearLabel2, localCon,  0, 3);
			CustomAdd(this, numMissField, localCon, 1, 3);
			CustomAdd(this, numCorrectRejectionField, localCon, 2, 3);
			
		}
		
		//LEGACY SUPPORT
		//Parameters will be set/gotten from a list.
		//The parameters are in the list in the FOLLOWING ORDER:
		//<NumThreatAndAlertedDanger> <num threat and alerted clear> <num clear and alerted danger> <num clear and alerted clear>
		// numHit                      numMiss                     numFalseAlarm                  numCorrectRejection
		//indexes
		//0 numHit, 1 numMiss, 2 numFalseAlarm, 3 numCorrectRejection.
		public ArrayList<Integer> getBinaryQuotaList(){
			
			ArrayList<Integer> binaryQuotaOut = new ArrayList<Integer>();
			
			binaryQuotaOut.add(Integer.parseInt(numHitField.getText()));
			binaryQuotaOut.add(Integer.parseInt(numMissField.getText()));
			binaryQuotaOut.add(Integer.parseInt(numFalseAlarmField.getText()));
			binaryQuotaOut.add(Integer.parseInt(numCorrectRejectionField.getText()));
			
			
			return binaryQuotaOut;
				
		}
		
		//0 numHit, 1 numMiss, 2 numFalseAlarm, 3 numCorrectRejection.
		public void setBinaryQuotaList(ArrayList<Integer> binaryQuotaIn){
			
			numHitField.setText(String.valueOf(binaryQuotaIn.get(0)));
			numMissField.setText(String.valueOf(binaryQuotaIn.get(1)));
			numFalseAlarmField.setText(String.valueOf(binaryQuotaIn.get(2)));
			numCorrectRejectionField.setText(String.valueOf(binaryQuotaIn.get(3)));
			
			
		}
		
	}
	
	//good
	private class LikelihoodQuotaPanel extends JPanel{
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		JLabel quotasLabel = new JLabel("Quotas");
		
		JLabel tableCornerLabel = new JLabel("Given v | True >");
		JLabel threatLabel = new JLabel("Threat");
		JLabel clearLabel = new JLabel("Clear");
		
		JLabel dangerLabel = new JLabel("Danger");
		JTextField numThreatGivenDangerField  = new JTextField("", 4);
		JTextField numClearGivenDangerField  = new JTextField("", 4);
		
		JLabel warningLabel = new JLabel("Warning");
		JTextField numThreatGivenWarningField = new JTextField("", 4);
		JTextField numClearGivenWarningField = new JTextField("", 4);
		
		JLabel posClearLabel = new JLabel("Possibly Clear");
		JTextField numThreatGivenPosClrField = new JTextField("", 4);
		JTextField numClearGivenPosClrField  = new JTextField("", 4);
		
		JLabel clearLabel2 = new JLabel("Clear"); //cannot add the same label in two different places
		JTextField numThreatGivenClrField    = new JTextField("", 4);
		JTextField numClearGivenClrField     = new JTextField("", 4);
		
		
		public LikelihoodQuotaPanel(){
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			
		}
		

		
		private void placeComponents(){
			CustomAdd(this, quotasLabel, localCon, 0, 0);
			
			CustomAdd(this, tableCornerLabel, localCon, 0, 1);
			CustomAdd(this, threatLabel, localCon, 1, 1);
			CustomAdd(this, clearLabel,  localCon, 2, 1);
			
			CustomAdd(this, dangerLabel, localCon, 0, 2);
			CustomAdd(this, numThreatGivenDangerField, localCon, 1, 2);
			CustomAdd(this, numClearGivenDangerField, localCon, 2, 2);
			
			CustomAdd(this, warningLabel, localCon, 0, 3);
			CustomAdd(this, numThreatGivenWarningField, localCon, 1, 3);
			CustomAdd(this, numClearGivenWarningField, localCon, 2, 3);
			
			CustomAdd(this, posClearLabel, localCon, 0, 4);
			CustomAdd(this, numThreatGivenPosClrField, localCon, 1, 4);
			CustomAdd(this, numClearGivenPosClrField, localCon, 2, 4);
			
			CustomAdd(this, clearLabel2, localCon, 0, 5);
			CustomAdd(this, numThreatGivenClrField, localCon, 1, 5);
			CustomAdd(this, numClearGivenClrField, localCon, 2, 5);
			
		}
		
		//LEGACY SUPPORT
		//Parameters will be set/gotten from a list.
		//The parameters are in the list in the FOLLOWING ORDER:
		//<#threat, alert=danger> <#threat, alert=warning> <#threat, alert=posClear> <#threat, alert=clear> 
		//     ,<#clear, alert=danger> <#clear, alert=warning> <#clear, alert=posClr> <#clear, alert=clear>
		
		public ArrayList<Integer> getLikelihoodQuotaList(){
			
			ArrayList<Integer> likelihoodQuotaOut = new ArrayList<Integer>();
			

			likelihoodQuotaOut.add(Integer.parseInt(numThreatGivenDangerField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numThreatGivenWarningField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numThreatGivenPosClrField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numThreatGivenClrField.getText()));
			
			likelihoodQuotaOut.add(Integer.parseInt(numClearGivenDangerField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numClearGivenWarningField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numClearGivenPosClrField.getText()));
			likelihoodQuotaOut.add(Integer.parseInt(numClearGivenClrField.getText()));
			
			return likelihoodQuotaOut;
		}
		
		public void setLikelihoodQuotaList(ArrayList<Integer> likelihoodQuotaIn){
			
			numThreatGivenDangerField.setText(String.valueOf(likelihoodQuotaIn.get(0)));
			numThreatGivenWarningField.setText(String.valueOf(likelihoodQuotaIn.get(1)));
			numThreatGivenPosClrField.setText(String.valueOf(likelihoodQuotaIn.get(2)));
			numThreatGivenClrField.setText(String.valueOf(likelihoodQuotaIn.get(3)));
			
			numClearGivenDangerField.setText(String.valueOf(likelihoodQuotaIn.get(4)));
			numClearGivenWarningField.setText(String.valueOf(likelihoodQuotaIn.get(5)));
			numClearGivenPosClrField.setText(String.valueOf(likelihoodQuotaIn.get(6)));
			numClearGivenClrField.setText(String.valueOf(likelihoodQuotaIn.get(7)));
			
		}
	}

	//good
	private class GenerationTypePanel extends JPanel{

		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		// picture generation
		JLabel pictureGenerationLabel = new JLabel("Image/Alert Generation: ");
		JLabel randomLabel = new JLabel("Random");
		JLabel inputLabel = new JLabel("Input");
		// in a group
		ButtonGroup picGenGroup = new ButtonGroup();
		JRadioButton randomButton = new JRadioButton();
		JRadioButton inputButton  = new JRadioButton();
		
		
		public GenerationTypePanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupButtons();
			
		}
		
		private void placeComponents(){
			
			CustomAdd(this, pictureGenerationLabel, localCon, 0, 0);
			CustomAdd(this, randomButton, localCon, 1, 0);
			CustomAdd(this, randomLabel, localCon, 2, 0);
			CustomAdd(this, inputButton, localCon, 3, 0);
			CustomAdd(this, inputLabel, localCon, 4, 0);
		}
		
		private void setupButtons(){
			
			picGenGroup.add(randomButton);
			picGenGroup.add(inputButton);
			
			//random button
			randomButton.addActionListener(e ->{
				
				if(generationPanelDisplaying){
					removeGenerationPanel();
				}
			});
			
			//input button
			inputButton.addActionListener(e ->{
				
				if(!generationPanelDisplaying){
					addGenerationPanel();
				}
			});
		}
		
		public boolean getIsRandomGen(){
			return randomButton.isSelected();
		}
		
		public void setIsRandomGen(boolean isRandom){
			if(isRandom){
				randomButton.setSelected(true);
			}
			else{
				inputButton.setSelected(true);
			}
		}
	}
	
	//note: only used when image/warning generation is INPUT, NOT random.
	//good
	private class GenerationPanel extends JPanel {
		
		private static final int scrollPaneWidth = 150; //adjusted such that the longest file name (not whole path), just fits in the viewport
		private static final int scrollPaneHeight = 150;  
		
		private Dimension scrollPanelDim = new Dimension(scrollPaneWidth, scrollPaneHeight);
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		//Position 0
		private JLabel p0Label = new JLabel("Top Left");
		private JTextArea p0Text = new JTextArea();
		private JScrollPane p0Pane;
		
		//Position 1
		private JLabel p1Label = new JLabel("Top Right");
		private JTextArea p1Text = new JTextArea();
		private JScrollPane p1Pane;
		
		//position 2
		private JLabel p2Label = new JLabel("Bottom Left");
		private JTextArea p2Text = new JTextArea();
		private JScrollPane p2Pane;
		
		//position 3
		private JLabel p3Label = new JLabel("Bottom Right");
		private JTextArea p3Text = new JTextArea();
		private JScrollPane p3Pane;
		
		//warning generation
		private JLabel alertLabel = new JLabel("Alert");
		private JTextArea alertText = new JTextArea();
		private JScrollPane alertPane;
		
		public GenerationPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			setupScrollPanes();
			placeComponents();
			
		}
		
		private void setupScrollPanes(){
			p0Pane = new JScrollPane(p0Text);
			p1Pane = new JScrollPane(p1Text);
			p2Pane = new JScrollPane(p2Text);
			p3Pane = new JScrollPane(p3Text);
			p0Pane.setPreferredSize(scrollPanelDim);
			p1Pane.setPreferredSize(scrollPanelDim);
			p2Pane.setPreferredSize(scrollPanelDim);
			p3Pane.setPreferredSize(scrollPanelDim);
			
			alertPane = new JScrollPane(alertText);
			alertPane.setPreferredSize(scrollPanelDim);
			
		}
		
		private void placeComponents(){
			
			//row 0
			CustomAdd(this, p0Label, localCon, 0, 0);
			CustomAdd(this, p1Label, localCon, 1, 0);
			CustomAdd(this, p2Label, localCon, 2, 0);
			CustomAdd(this, p3Label, localCon, 3, 0);
			CustomAdd(this, alertLabel, localCon, 4, 0);
			
			//row 1
			CustomAdd(this, p0Pane, localCon, 0, 1);
			CustomAdd(this, p1Pane, localCon, 1, 1);
			CustomAdd(this, p2Pane, localCon, 2, 1);
			CustomAdd(this, p3Pane, localCon, 3, 1);
			CustomAdd(this, alertPane, localCon, 4, 1);
			
		}
		
		//note, the inner list will contain image file names, WITHOUT the complete file path.
		//see ExpSettings field "inputImageSet" for further detail.
		public ArrayList<ArrayList<String>> getInputImageSet(){
			
			ArrayList<ArrayList<String>> inputImageSet = new ArrayList<ArrayList<String>>();
			
			//looks like these are smart enough to get rid of extra newlines at the end and not fill the string arrays with "" "" "" etc.
			String[] pos0Lines = p0Text.getText().split("\\n");
			String[] pos1Lines = p1Text.getText().split("\\n");
			String[] pos2Lines = p2Text.getText().split("\\n");
			String[] pos3Lines = p3Text.getText().split("\\n");
			
			//image data
			inputImageSet.clear();
			inputImageSet.add(new ArrayList<String>(Arrays.asList(pos0Lines)));
			inputImageSet.add(new ArrayList<String>(Arrays.asList(pos1Lines)));
			inputImageSet.add(new ArrayList<String>(Arrays.asList(pos2Lines)));
			inputImageSet.add(new ArrayList<String>(Arrays.asList(pos3Lines)));
			
			return inputImageSet;
		}
		
		//untested.
		public ArrayList<String> getInputWarningSet(){
			
			ArrayList<String> inputWarningSet = new ArrayList<String>();
			
			String[] warningLines = alertText.getText().split("\\n");
			
			//warning data
			inputWarningSet.clear();
			inputWarningSet.addAll(Arrays.asList(warningLines));
			
			return inputWarningSet;
		}
	}
	
	//good
	//CLEAN THIS UP!!!!!
	private class CalculatedReliabilityPanel extends JPanel {
		
		DecimalFormat decimalFormat = new DecimalFormat("#.#####");
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		
		//LOOK. MAKE THIS DESCRIPTOR CHANGES ELSEWHERE
		//LOOK.
		//overall reliability
		private JLabel o_rel_label = new JLabel("Overall: ");
		private JLabel o_rel_num_label = new JLabel();
		private double o_rel;
		
		//hit rate reliability
		private JLabel hr_rel_label = new JLabel("Hit Rate:");
		private JLabel hr_rel_num_label = new JLabel();
		private double hr_rel;
		

		//correct rejection rate reliability
		private JLabel crr_rel_label = new JLabel("Correct Rejection Rate: ");
		private JLabel crr_rel_num_label = new JLabel(); 
		private double crr_rel;
		
		//positive predicted value reliability
		private JLabel ppv_rel_label = new JLabel("Positive Predicted Value: ");
		private JLabel ppv_rel_num_label = new JLabel();
		private double ppv_rel;
		
		//negative predicted value reliability
		private JLabel npv_rel_label = new JLabel("Negative Predicted Value: ");
		private JLabel npv_rel_num_label = new JLabel(); 
		private double npv_rel;
		
		
		public CalculatedReliabilityPanel(){
			
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
			setupDocumentListener();
			this.setVisible(false);
		}
		
		//Goes into the binaryQuotaPanel and likelihoodQuotaPanel and gives their text fields' documents the listener created here.
		private void setupDocumentListener(){
			
			DocumentListener docListener = new DocumentListener(){
				
				//add checks in here to make sure that the entered text can be read as a double....
				@Override
				public void changedUpdate(DocumentEvent e){}
				
				@Override
				public void insertUpdate(DocumentEvent e){
					try{
						recalculate();
						reliabilityProfilePanel.updateReliabilityMessages();
					}
					catch(NumberFormatException ex){
						//happens when the text field can't be parsed as a number, its fine to do nothing, it will recalculate as soon as all fields are valid numbers.
					}
				}
				@Override
				public void removeUpdate(DocumentEvent e){
					try{
						recalculate();
						reliabilityProfilePanel.updateReliabilityMessages();
					}
					catch(NumberFormatException ex){	
					}
				}
				
			};
			
			binaryQuotaPanel.numHitField.getDocument().addDocumentListener(docListener);
			binaryQuotaPanel.numFalseAlarmField.getDocument().addDocumentListener(docListener);
			binaryQuotaPanel.numMissField.getDocument().addDocumentListener(docListener);
			binaryQuotaPanel.numCorrectRejectionField.getDocument().addDocumentListener(docListener);
			
			likelihoodQuotaPanel.numThreatGivenDangerField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numClearGivenDangerField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numThreatGivenWarningField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numClearGivenWarningField.getDocument().addDocumentListener(docListener);
			
			likelihoodQuotaPanel.numThreatGivenPosClrField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numClearGivenPosClrField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numThreatGivenClrField.getDocument().addDocumentListener(docListener);
			likelihoodQuotaPanel.numClearGivenClrField.getDocument().addDocumentListener(docListener);
			
		}
		
		private void placeComponents(){
			
			CustomAdd(this, o_rel_label, localCon, 0, 0);
			CustomAdd(this, o_rel_num_label, localCon, 1, 0);
			
			CustomAdd(this, hr_rel_label, localCon, 0, 1);
			CustomAdd(this, hr_rel_num_label, localCon, 1, 1);
			
			CustomAdd(this, crr_rel_label, localCon, 0, 2);
			CustomAdd(this, crr_rel_num_label, localCon, 1, 2);
			
			CustomAdd(this, ppv_rel_label, localCon, 0, 3);
			CustomAdd(this, ppv_rel_num_label, localCon, 1, 3);
			
			CustomAdd(this, npv_rel_label, localCon, 0, 4);
			CustomAdd(this, npv_rel_num_label, localCon, 1, 4);
			
			
		}
		
		public void setReliabilityLabel(double reliability, ReliabilityCalculator.ReliabilityType reliabilityType){
			
			switch(reliabilityType){
				case OVERALL : {
					o_rel_num_label.setText(String.valueOf(decimalFormat.format(reliability)));
				}break;
				case HIT_RATE : {
					hr_rel_num_label.setText(String.valueOf(decimalFormat.format(reliability)));	
				}break;
				case CORRECT_REJECTION_RATE : {
					crr_rel_num_label.setText(String.valueOf(decimalFormat.format(reliability)));
				}break;
				case POSITIVE_PREDICTIVE_VALUE : {
					ppv_rel_num_label.setText(String.valueOf(decimalFormat.format(reliability)));
				}break;
				case NEGATIVE_PREDICTIVE_VALUE : {
					npv_rel_num_label.setText(String.valueOf(decimalFormat.format(reliability)));
				}break;

			}
			
			
		}
		
		public void recalculate(){
			
			
			//LOOK. TODO Make this code more efficient. Store the quotaList and the boolean. Can do this in half the code. 
			//look. It was a slight decision here in what indication of binry vs likelihood I shoudld use.
			if(binaryQuotaPanelDisplaying){
				
				o_rel   = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.OVERALL, binaryQuotaPanel.getBinaryQuotaList(), true);
				hr_rel  = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.HIT_RATE, binaryQuotaPanel.getBinaryQuotaList(), true);
				crr_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.CORRECT_REJECTION_RATE, binaryQuotaPanel.getBinaryQuotaList(), true);
				ppv_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.POSITIVE_PREDICTIVE_VALUE, binaryQuotaPanel.getBinaryQuotaList(), true);
				npv_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.NEGATIVE_PREDICTIVE_VALUE, binaryQuotaPanel.getBinaryQuotaList(), true);
				
			}
			
			else{
				o_rel   = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.OVERALL, likelihoodQuotaPanel.getLikelihoodQuotaList(), false);
				hr_rel  = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.HIT_RATE, likelihoodQuotaPanel.getLikelihoodQuotaList(), false);
				crr_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.CORRECT_REJECTION_RATE, likelihoodQuotaPanel.getLikelihoodQuotaList(), false);
				ppv_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.POSITIVE_PREDICTIVE_VALUE, likelihoodQuotaPanel.getLikelihoodQuotaList(), false);
				npv_rel = ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.NEGATIVE_PREDICTIVE_VALUE, likelihoodQuotaPanel.getLikelihoodQuotaList(), false);
				
			}
			
			
			
			setReliabilityLabel(o_rel,   ReliabilityCalculator.ReliabilityType.OVERALL);
			setReliabilityLabel(hr_rel,  ReliabilityCalculator.ReliabilityType.HIT_RATE);
			setReliabilityLabel(crr_rel, ReliabilityCalculator.ReliabilityType.CORRECT_REJECTION_RATE);
			setReliabilityLabel(ppv_rel, ReliabilityCalculator.ReliabilityType.POSITIVE_PREDICTIVE_VALUE);
			setReliabilityLabel(npv_rel, ReliabilityCalculator.ReliabilityType.NEGATIVE_PREDICTIVE_VALUE);
			
			
		}

		public double getReliability(ReliabilityCalculator.ReliabilityType reliabilityType){
			
			switch(reliabilityType){
				case OVERALL : {
					return o_rel;
				}
				case HIT_RATE : {
					return hr_rel;	
				}
				case CORRECT_REJECTION_RATE : {
					return crr_rel;
				}
				case POSITIVE_PREDICTIVE_VALUE : {
					return ppv_rel;
				}
				case NEGATIVE_PREDICTIVE_VALUE : {
					return npv_rel;
				}
				
				default : {
					//should never happen
					return 0.0; 
				}

			}
			
		}
		
	}
	
	private class MessagePanel extends JPanel {
		
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel msg1Label = new JLabel("Line 1 Message:");
		private JTextField msg1Field = new JTextField("", 30);
		
		private JLabel msg2Label = new JLabel("Line 2 Message:");
		private JTextField msg2Field = new JTextField("", 30);
		
		public MessagePanel(){
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			placeComponents();
		}
		
		private void placeComponents(){
			CustomAdd(this, msg1Label, localCon, 0, 0);
			CustomAdd(this, msg1Field, localCon, 1, 0);
			
			CustomAdd(this, msg2Label, localCon, 0, 1);
			CustomAdd(this, msg2Field, localCon, 1, 1);
			this.msg1Label.setVisible(false);
			this.msg1Field.setVisible(false);
			this.msg2Label.setVisible(false);
			this.msg2Field.setVisible(false);

		}
		
		public String getMessage1(){
			return msg1Field.getText();
		}
		public void setMessage1(String s){
			msg1Field.setText(s);
		}
		public String getMessage2(){
			return msg2Field.getText();
		}
		public void setMessage2(String s){
			msg2Field.setText(s);
		}
		
	}
	
	
	private class ReliabilityProfilePanel extends JPanel {
		//layout management objects
		private GridBagLayout layout = new GridBagLayout();
		private GridBagConstraints localCon = new GridBagConstraints();
		
		private JLabel profileLabel = new JLabel("Level of Explanations To Display During Experiment Trials:");
		private ButtonGroup profileButtonGroup = new ButtonGroup();

		//overall
		private JRadioButton overallButton = new JRadioButton();
		private JLabel overallLabel = new JLabel("Level 4");
		
		//rate. Based on true state
		private JRadioButton rateButton = new JRadioButton();
		private JLabel rateLabel = new JLabel("Level 3");
		
		//value. Based on what detector says
		private JRadioButton valueButton = new JRadioButton();
		private JLabel valueLabel = new JLabel("Level 2");
		
		//none
		private JRadioButton noneButton = new JRadioButton();
		private JLabel noneLabel = new JLabel("Level 1");
		
		
		
		
 		public ReliabilityProfilePanel(){
			setLayout(layout);
			localCon.insets = new Insets(1, 1, 1, 1);
			localCon.anchor = GridBagConstraints.WEST;
			
			placeComponents();
			setupButtons();
		}
 		
 		private void setupButtons(){
 			profileButtonGroup.add(overallButton);
 			profileButtonGroup.add(rateButton);
 			profileButtonGroup.add(valueButton);
 			profileButtonGroup.add(noneButton);
 			
 			ActionListener profileButtonListener = new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e){
 					updateReliabilityMessages();
 					if(controlRunPanel.getIsControlRun()){
 						setReliabilityProfile(ReliabilityCalculator.ReliabilityProfile.NONE);
 						updateReliabilityMessages();
 					}
 				}
 			};

 			overallButton.addActionListener(profileButtonListener);
 			rateButton.addActionListener(profileButtonListener);
 			valueButton.addActionListener(profileButtonListener);
 			noneButton.addActionListener(profileButtonListener);

			overallButton.setVisible(false);
			overallLabel.setVisible(false);
 		}
		
		private void placeComponents(){
			
			localCon.gridwidth = 2;
			CustomAdd(this, profileLabel, localCon, 0, 0);
			localCon.gridwidth = 1;
			
			CustomAdd(this, overallButton, localCon, 0, 1);
			CustomAdd(this, overallLabel, localCon, 1, 1);
			
			CustomAdd(this, rateButton, localCon, 0, 2);
			CustomAdd(this, rateLabel, localCon, 1, 2);
			
			CustomAdd(this, valueButton, localCon, 0, 3);
			CustomAdd(this, valueLabel, localCon, 1, 3);
			
			CustomAdd(this, noneButton, localCon, 0, 4);
			CustomAdd(this, noneLabel, localCon, 1, 4);
			
		}
		
		public ReliabilityCalculator.ReliabilityProfile getReliabilityProfile(){
			if(overallButton.isSelected()){
				return ReliabilityCalculator.ReliabilityProfile.OVERALL;
			}
			if(rateButton.isSelected()){
				return ReliabilityCalculator.ReliabilityProfile.RATE;
			}
			if(valueButton.isSelected()){
				return ReliabilityCalculator.ReliabilityProfile.VALUE;
			}
			//else none is selected
			return ReliabilityCalculator.ReliabilityProfile.NONE;
			
		}
		
		public void setReliabilityProfile(ReliabilityCalculator.ReliabilityProfile profile){
			switch (profile){
				case OVERALL : {
					overallButton.setSelected(true);
				}break;
				case NONE : {
					noneButton.setSelected(true);			
				}break;
				case RATE : {
					rateButton.setSelected(true);
				}break;
				case VALUE : {
					valueButton.setSelected(true);
				}break;
			}
		}
		
		public void updateReliabilityMessages(){
			
			
				
			//LOOK. THESE DESCRIPTORS ARE IMPORTANT.
			if(overallButton.isSelected()){
				//TRUNCATES to nearest integer for now. LOOK.
				messagePanel.setMessage1("4");
				messagePanel.setMessage2("4");
			}
			else if (rateButton.isSelected()){
				
				//TRUNCATES to nearest integer for now. LOOK.
				int threatReliability = (int)(calculatedReliabilityPanel.getReliability(ReliabilityCalculator.ReliabilityType.HIT_RATE) * 100);
				int noThreatReliability = (int)(calculatedReliabilityPanel.getReliability(ReliabilityCalculator.ReliabilityType.CORRECT_REJECTION_RATE) * 100);
				messagePanel.setMessage1("3");
				messagePanel.setMessage2("3");
			}
			else if (valueButton.isSelected()){
				//TRUNCATES to nearest integer for now. LOOK.
				int clearReliability = (int)(calculatedReliabilityPanel.getReliability(ReliabilityCalculator.ReliabilityType.NEGATIVE_PREDICTIVE_VALUE) * 100);
				int dangerReliability  = (int)(calculatedReliabilityPanel.getReliability(ReliabilityCalculator.ReliabilityType.POSITIVE_PREDICTIVE_VALUE) * 100);
				messagePanel.setMessage1("2");
				messagePanel.setMessage2("2");
			}
			else if(noneButton.isSelected()){
				messagePanel.setMessage1("1");
				messagePanel.setMessage2("1");
			}
			
			
		}
	}
	
	
	
	//*******************************************************
	//*******************************************************
	//*******************************************************
	
	//HERE ARE LEGACY PRE-SETTINGS
 	private void setToLegacy(int reliability, boolean isBinary){
		
		controlRunPanel.setIsControlRun(false);
		
		graphicsModePanel.setIsUsingNewGraphics(false);
		
		trackerDifficultyPanel.setMultiplier(1.0);
		
		trialPanel.setTrackPrac(1);
		trialPanel.setCombPrac(1);
		trialPanel.setTrials(40);
		
		alarmAlertPanel.setIsBinaryMode(isBinary);
		alarmAlertPanel.setAudioAlertEnabled(true);
		alarmAlertPanel.setVisualAlertEnabled(true);
		alarmAlertPanel.setDangerAlertsOnly(false);
		
		//double check that your order wtih respect to likelihoodQuotas.txt is correct.
		
		//binary
		//yes, this is hardcoded. Yes, its supposed to be. These are legacy settings.
		if(isBinary){
			
			switchToBinaryQuotaPanel();
			
			
			switch(reliability){
				case 70 : {
					binaryQuotaPanel.setBinaryQuotaList(new ArrayList<Integer>(Arrays.asList(10, 2, 8, 20)));
				}break;
				
				case 80 : {
					binaryQuotaPanel.setBinaryQuotaList(new ArrayList<Integer>(Arrays.asList(21, 9, 11, 59)));
				}break;
				
				case 90 : {
					binaryQuotaPanel.setBinaryQuotaList(new ArrayList<Integer>(Arrays.asList(29, 1, 11, 59)));
				}break;
				
			}
			
			
		}
		//likelihood
		else{
			
			switchToLikelihoodQuotaPanel();
			
			switch(reliability){
				case 70 : {
					likelihoodQuotaPanel.setLikelihoodQuotaList(new ArrayList<Integer>(Arrays.asList(5, 4, 6, 15, 5, 6, 11, 48)));
				}break;
				
				case 80 : {
					likelihoodQuotaPanel.setLikelihoodQuotaList(new ArrayList<Integer>(Arrays.asList(15, 6, 4, 5, 5, 6, 11, 48)));
				}break;
				
				case 90 : {
					likelihoodQuotaPanel.setLikelihoodQuotaList(new ArrayList<Integer>(Arrays.asList(28, 1, 1, 0, 5, 6, 11, 48)));
				}break;
			
			}
			
		}
		
		surveyPanel.setSurveyFrequency(1);
		surveyPanel.setSurveyReminderFrequency(0);
		surveyPanel.setEndOfExpReminderEnabled(false);
		
		breakPanel.setFreqency(50);
		
		pictureWarningGenPanel.setIsRandomGen(true);
		if(generationPanelDisplaying){
			removeGenerationPanel();
		}
		
		calculatedReliabilityPanel.recalculate();
		
		messagePanel.setMessage1("");
		messagePanel.setMessage2("");
		
		calculatedReliabilityPanel.recalculate();
		reliabilityProfilePanel.setReliabilityProfile(ReliabilityCalculator.ReliabilityProfile.NONE);
		reliabilityProfilePanel.updateReliabilityMessages();
		
	}
	
	
	
	

	//********************
	//Below are methods for managing which panels are showing. Ex: binary quota vs likelihood quota. 
	private void switchToLikelihoodQuotaPanel(){
		
		rightPanel.remove(binaryQuotaPanel);
		CustomAdd(rightPanel, likelihoodQuotaPanel, right_gbc, quotaPanelXPos, quotaPanelYPos);
		binaryQuotaPanelDisplaying = false;
		rightPanel.revalidate();
		SettingsDialog.this.pack();
		
	}
	
	private void switchToBinaryQuotaPanel(){
		
		rightPanel.remove(likelihoodQuotaPanel);
		CustomAdd(rightPanel, binaryQuotaPanel, right_gbc, quotaPanelXPos, quotaPanelYPos);
		binaryQuotaPanelDisplaying = true;
		rightPanel.revalidate();
		SettingsDialog.this.pack();
		
	}
	
	private void removeGenerationPanel(){
		
		generationPanelDisplaying = false;
		rightPanel.remove(generationPanel);
		rightPanel.revalidate();
		pack();
	}
	
	private void addGenerationPanel(){
		
		generationPanelDisplaying = true;
		CustomAdd(rightPanel, generationPanel, right_gbc, generationPanelXPos, generationPanelYPos);
		rightPanel.revalidate();
		pack();
	}
	
	

}
