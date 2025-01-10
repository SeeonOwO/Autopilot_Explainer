//Do NOT use a tool to "fix" indendation. If you do, make sure it only does it on a selected portion and not the whole file. 

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.program.GraphicsProgram;
import net.java.games.input.Controller;
import net.java.games.input.Component;
import net.java.games.input.ControllerEnvironment;

import java.lang.StringBuilder;

//Graphics Overview
//Looks like (0,0) is in top left of application.
//EVERYTHING IS TOP LEFT ANCHOR. including individual GObjects.

/**
 * This is the highest level driver for the entire game/program.
 * See DOCUMENTATION for an explanation of call hierarchy and flow of control. 
 * 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 * 
 *
 */
public class OneButtonTracker extends GraphicsProgram implements MouseMotionListener {
	//Because OneButtonTracker is a GraphicsProgram, remember it has a "getGCanvas" method. Read up about GraphicsProgram and its fields/methods.
	
	private static final long serialVersionUID = 1L;
	
	//These are meaningless because the program is dynamically resized to the largest screen on start.
	public static final int APPLICATION_HEIGHT = 1000; 
	public static final int APPLICATION_WIDTH = 1200;
	
	//*****************************************************************
	//EXPERIMENT SETTINGS DATA. 
	private ExpSettings settings;
	private ArrayList<Trial_old> allTrials = new ArrayList<Trial_old>();
	private Physics p;
	private AudioPlayer audio = new AudioPlayer();
	private String fileName;
	//*****************************************************************
	//CONTROLLER STUFF
	//if not using joystick, using ps4 controller. not made final to shut up warnings.
	public static boolean USE_JOYSTICK = true; 
	private Controller joystick;
	private Component joystickXAxisComponent;
	private Component joystickYAxisComponent;
	private Component joystickTriggerComponent;
	private Component joystickSwitchComponent;
	
	
	//*****************************************************************
	//EXPERIMENT STATE DATA 
	/**
	 * Represents the *current* trial number. Incremented in setupNewTrial. It will be 1 by the time the experiment/practice starts. 
	 */
	private int counter = 0; 
	
	/**
	 * Ensures that the main portion of the primary game loop does not start to run before the "start" button is hit. 
	 * Must be voltatile so the while loop is not optimized.
	 */
	private volatile boolean readyToStart = false;
	
	private boolean inPracticeMode = true;
	
	//The 3 variables below allow the program to keep track of pausing, and managing time regardless of pausing.
	private boolean running = false;		
	private double pauseStart;
	private double pauseBank = 0; //set to zero after every round 
	
	//Below are lower level than those in the ExpSettings object. These are re-evaluated after every trial. 
	///For example, even if visual alerts are ON in settings, you would not give one during tracking-only. 
	//See setupNewTrial to see how there are managed.
	private boolean giveVisualAlert;
	private boolean giveAuditoryAlert;
	
	
	//*****************************************************************
	//DATA COLLECTION
	public DataAggregator dataAggregator;
	
	//NOTE: "_temp" means that this field is: (1) Changed, (2) Copied elsewhere for collection, and (3) Reset after every trial. 
	//In general, these will be used to collect data for each trial, 
	//then their values will be copied into an Entry, then reset for the next trial.
	private boolean triggerWasPulled_temp = false;  
	private double timeSpent_temp; 
	
	private int onTrackerScreenChecks_temp = 0;     
	private int totalScreenChecks_temp = 0;  
	
	private int toggleCount_temp = 0;    
	private double trialStartTime_temp;  
	
	//*****************************************************************
	//IMPORTANT GRAPHICS OBJECTS
	GraphicsManager gm;

	//*****************************************************************
	//LESS IMPORTANT GRAPHICS OBJECTS

	private JButton toPractice;
	private ArrayList<String> practiceText = new ArrayList<String>(); //looks almost useless.
	private JButton close = new JButton("Close");
	
	//*****************************************************************
	//LOOP DIAGNOSTICS
	//private LoopDiagnostic loopDiag = new LoopDiagnostic();
	//*****************************************************************
	
	
	
	

	
	public static void main(String[] args) {
		new OneButtonTracker().start();
	}

	//invoked when <OneButtonTracker>.start() is called.
	public void init(){
		settings = new ExpSettings();
		UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 20)); //setup font for OptionPane.
		initializeControllers();
		fileName = (String) JOptionPane.showInputDialog(this, "File name (no extension):", "Setup", JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		// code execution will not continue until the user hits START in this vvv dialog box
		SettingsDialog d = new SettingsDialog(new JFrame(), settings);
		d.setVisible(true);
		
		
		gm = new GraphicsManager(this, settings.isUsingNewGraphics, settings.getExtremeInOldGraphicsCoords(), settings);
		
		// look. looks like it will be important for when you customize image
		// input.
		try {
			Scanner fin = new Scanner(new BufferedReader(new FileReader(
					settings.isControlRun ? "controlPracticeText" : settings.alarmIsBinary ? "practiceText" : "likelihoodPracticeText")));
			while (fin.hasNextLine()) {
				practiceText.add(fin.nextLine());
			}
			
			fin.close();
			populatePracticeTrialData(); //important.
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		initIntroScreen();
	}

	// screen that explains the scenario.
	private void initIntroScreen() {
		try {
			Scanner fin = new Scanner(new BufferedReader(new FileReader("initScreenText")));
			GLabel temp;
			for (int i = 0; fin.hasNextLine(); i++) {
				temp = new GLabel(fin.nextLine());
				temp.setFont(new Font("Arial", Font.PLAIN, 24));
				temp.setLocation(TrackerConstants.INIT_SCREEN_BUFFER,
						i * TrackerConstants.LINE_HEIGHT + TrackerConstants.INIT_SCREEN_BUFFER);
				add(temp);
			}
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// look! this could be bad, what if you dont want to practice?
		toPractice = new JButton("Start");
		toPractice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				practice();
			}
		});
		
		this.add(toPractice, 
				TrackerConstants.INIT_SCREEN_BUFFER, 
				8 * TrackerConstants.LINE_HEIGHT + TrackerConstants.INIT_SCREEN_BUFFER
		);
	}

	
	//Run when practice button on introScreen is hit.
	private void practice() {
		this.removeAll();
		initMainScreen();
		inPracticeMode = true;
		
		running = true;
		readyToStart = true; 
	}

	// add all the elements to the screen
	private void initMainScreen() {
		this.removeAll();
		//last argument being true represents being in practice.
		dataAggregator = new DataAggregator(System.currentTimeMillis(), fileName, settings.overallReliability, settings.alarmIsBinary, settings.isControlRun, true);
		
		
		//***The order of these init methods DOES matter. Each method initializes their respective objects AND ADDS them to the GraphicsProgram's canvas. (But they will not neccessarily be visible.)
		
		gm.images.init(); //after this call, imagesOnDisplay will be empty.
		gm.otherInfo.initTrialAndScoreLabels(settings.getLabelRectBound(), counter, settings.experimentTrials, inPracticeMode, practiceText.get(0), "", ""); //practice text looks almost useless in this case.
		//i kind of want to make the above call come after the below call...Messages get buried if they happen to overlap. not neccessary though. 
		
		
		
		gm.trackerGraphic.init(settings.getTrackerRectBound());
		gm.recommender.init(settings.getRecommenderRectBound());
		gm.otherInfo.initTimer(settings.getTrackerRectBound());

		p = new Physics(settings);

		this.add(close, settings.getTrackerRectBound().getTopRight().getX() - 100, 20); //mixing regular java UI components with acm Graphics class.
	}

	//used for both practice and experiment.
	private void putUpNextImageSetAndWarnings() {
		
		//recall: at this point, "counter" has already been incremented.
		//If we want the data for trial 2 (the second trial, not 3rd trial), then we need allTrials.get(1). 
		
		//REMOVE IMAGES
		gm.images.removeImagesFromCanvas();
	
		//GET TRIAL DATA
		Trial_old t = allTrials.get(counter - 1);  //gets this data even if it is tracking only, and thats fine.
		
		//IMAGES
		gm.images.init();
		//if not practice OR in combine practice
		if (!inPracticeMode || counter > settings.trackingPTrials) {
			for (String s : t.imageSet) {
				//look! adding images to use!!!
				gm.images.addImageFromTrialToDisplayList(s);
			}
		}
		
		//eh. I'll allow driver to access manager data here.
		if (!gm.images.imagesOnDisplay.isEmpty()){
			gm.images.addImagesToCanvas(settings.getImagesRectBound());
		}
		
		
		//VISUAL WARNING
		//Note: The effects of this may not be seen because the recommender could be disabled. 
		//Note: The graphics manager handles making the green invisible when we are in danger-alerts-only mode. 
		gm.recommender.changeColor(t.color, settings.alarmIsBinary);
		
		
		//AUDITORY WARNING
		if(giveAuditoryAlert){
			
			if(settings.dangerAlertsOnly && settings.alarmIsBinary){
				if(t.color == ExpSettings.BINARY_COLORS[0]){
					audio.play(t.clip); // look. audio plays here. this is where warning is given.
					audio = new AudioPlayer();
				}
				
			}
			else{
				audio.play(t.clip); // look. audio plays here. this is where warning is given.
				audio = new AudioPlayer();
			}
			
		}
	
	}

	private void initializeControllers() {
		
		joystick = null;
		joystickXAxisComponent = null;
		joystickYAxisComponent = null;
		joystickTriggerComponent = null;
		joystickSwitchComponent = null;
		
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] cs = ce.getControllers();
		
		//Find joystick input number
		
		for(int i = 0; i < cs.length; i++){
			if(cs[i].getName().equals("Logitech Extreme 3D") && USE_JOYSTICK){
				joystick = cs[i];
				
				for(Component someComponent : joystick.getComponents()){
					String componentIdentifierName = someComponent.getIdentifier().getName();
					switch(componentIdentifierName){
						case "y" :
							joystickYAxisComponent = someComponent;
							break;
							
						case "x" : 
							joystickXAxisComponent = someComponent;
							break;
							
						case "1" : 
							//button 2
							joystickSwitchComponent = someComponent;
							break;
							
						case "0" :  
							//button 1
							joystickTriggerComponent = someComponent;
							break;
							
					}//end switch
				}//end component loop
			}//end found joystick block
			
			//else use ps4 controller
			if(cs[i].getName().equals("Wireless Controller") && !USE_JOYSTICK){
				joystick = cs[i];
				
				for(Component someComponent : joystick.getComponents()){
					String componentIdentifierName = someComponent.getIdentifier().getName();
					switch(componentIdentifierName){
						case "y" :
							joystickYAxisComponent = someComponent;
							break;
							
						case "x" : 
							joystickXAxisComponent = someComponent;
							break;
							
						case "0" : 
							//button: square
							joystickSwitchComponent = someComponent;
							break;
							
						case "5" :  
							//button: right bumper
							joystickTriggerComponent = someComponent;
							break;
							
					}//end switch
				}//end component loop
				
			}
			
		}//end controller search loop
		
		if(joystick                 == null ||
		   joystickXAxisComponent   == null ||
		   joystickYAxisComponent   == null ||
		   joystickTriggerComponent == null ||
		   joystickSwitchComponent  == null    ){
			
			JOptionPane.showMessageDialog(this, "Error setting up joystick. Please quit the application, re-plug, and try again.", "Error Setting Up Joystick", JOptionPane.ERROR_MESSAGE);
			
		}
		
		System.out.println("ATTENTION: Please note, Failed to enumerate errors are a known issue.");
		System.out.println("I am working on fixing them, but they seem to have no effect. Please ignore them.");
		System.out.println("As long as separate pop-up with a red x has not appeared, then the joystick will work.");
		
	}

	private void populatePracticeTrialData() throws IOException {
		
		allTrials.clear();
		
		//note: These trials are always in the same order. <someTrial>.shuffle() shuffles the image positions within a single trial.
		Scanner fin = new Scanner(new BufferedReader(new FileReader("practiceImageSets.txt")));
		String[] splittedLine;
		Trial_old tempTrial;
		
		//Set up Tracking-only trials
		for(int trackingTrialsSetup = 0; trackingTrialsSetup < settings.trackingPTrials; trackingTrialsSetup++){
			tempTrial = new Trial_old();
			tempTrial.color = Color.WHITE; //arbitrary, this color will not display bc visual recommender will be disabled during tracking-only trials;
			tempTrial.clip = null; //arbitrary. 
			allTrials.add(tempTrial);
		}	
		
		
		//Note: the following section depends on the hard-coded file "practiceImageSets.txt". This file MUST have exactly 8 lines. Example line: 1P 2A 3A 4A bi:red   li:amber
		
		//Set up combined trials.
		int combinedTrialsSetup;
		for(combinedTrialsSetup = 0; combinedTrialsSetup < 8 && fin.hasNextLine(); combinedTrialsSetup++){
			
			tempTrial = new Trial_old();
			tempTrial.containsEnemy = false;
			splittedLine = fin.nextLine().split("\\s+"); //split by an number of white spaces.  "abc   bx s"   -> {"abc", "bx, "s"}. see java.util.regex.Pattern
			
			//Add image data to trial
			for(int numImagesAdded = 0; numImagesAdded < 4; numImagesAdded++){
				
				String mapName = splittedLine[numImagesAdded];
				if(mapName.contains("P")){
					tempTrial.containsEnemy = true;
				}
				tempTrial.addImageToTrial("Picture for practice/" + mapName + ".png");	
			}
			
			//Add color and clip data to trial.
			int alertPositionInSplittedString = settings.alarmIsBinary? 4 : 5; //(column numbers in file)
			//Get the alarm mode/warning from splittedLine. Split that string by ":" . Get the second component which is the warning.
			String alertToGive = (splittedLine[alertPositionInSplittedString].split(":"))[1]; //Example for line 5: If position is 4, gets the "red" out of "bi:red". If position is 5, gets the "amber" out of "li:amber"
			
			switch(alertToGive){
				//both
				case "red" : 
					tempTrial.color = Color.RED;
					tempTrial.clip = "sounds/danger.wav";
					break;
				
				//likelihood only
				case "amber" :
					tempTrial.color = ExpSettings.LIKELIHOOD_COLORS[1];
					tempTrial.clip = "sounds/caution.wav";
					break;
				
				//likelihood only
				case "lightGreen" : 
					tempTrial.color = ExpSettings.LIKELIHOOD_COLORS[2];
					tempTrial.clip = "sounds/possible.wav";
					break;
				
				//likelihood only
				case "darkGreen" : 
					
				//binary only
				case "green" : 
					tempTrial.color = Color.GREEN;
					tempTrial.clip = "sounds/clear.wav";
					break;
				
			
			}//end switch
			
			
			tempTrial.shuffle(); //shuffle image positions.
			allTrials.add(tempTrial);
		}//end for loop for each combined trial to setup
		
		assert(combinedTrialsSetup == 8 && !fin.hasNextLine());
		fin.close();
	}

	
	//important.
	//this does much more than load images. it sets up the warnings as well. 
	private void populateExperimentTrialData() {
		
		allTrials.clear();
		
		//important.
		ImageSetGenerator generator = new ImageSetGenerator(settings);
				
		//if the image/warning generation is random
		if(settings.genIsRandom){
			generator.setupTrials_Random(allTrials);
		}
		//else the generation is input by user.
		else{
			generator.setupTrials_Input(allTrials);
		}
	}

	//in charge of modifying the DataAggregator's entry list.
	//formerly known as "addEntry"
	private void pushEntryToAggregator() {
		dataAggregator.addEntry(
			new Entry(
				allTrials.get(counter - 1), 
				triggerWasPulled_temp, 
				trialStartTime_temp,
				counter, 
				this.onTrackerScreenChecks_temp * 1.0 / this.totalScreenChecks_temp, 
				this.toggleCount_temp,
				inPracticeMode && counter <= settings.trackingPTrials, 
				timeSpent_temp
			)//end- Entry constructor
		); //end- add call
	}


	//*************************************************
	
	public void mouseMoved(MouseEvent e) {
		/*
		 * if (mousePos != null) { p.mouseDiff = new Tuple(e.getX() -
		 * mousePos.x, e.getY() - mousePos.y); } mousePos = new Tuple(e.getX(),
		 * e.getY());
		 */
	}


	
	private void pause() {
		running = false;
		pauseStart = System.currentTimeMillis();
	}

	//doesn't get run over and over again.
	private void unpause() {
		running = true;
		pauseBank += System.currentTimeMillis() - pauseStart;
	}



	//************************************
	//poll functions
	
	//called by increment trial number
	private void displayAndLogPolls() {
		ArrayList<Dictionary<Integer, JLabel>> labels = new ArrayList<Dictionary<Integer, JLabel>>();
		String[] messages = new String[TrackerConstants.NUM_POLL_QUESTIONS];
		Dictionary<Integer, JLabel> temp;
		for (int i = 0; i < TrackerConstants.NUM_POLL_QUESTIONS; i++) {
			temp = new Hashtable<Integer, JLabel>();
			switch (i) {
			case 0: // UGLY UGLY HOTFIX, WILL CHANGE EVENTUALLY. or not.
				messages[0] = "<html>How confident are you in completing both tasks <b>without</b> the detector?</html>";
				
//				temp.put(0, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0<br>Not confident at all</html>"));
				temp.put(0, new JLabel("Not confident at all"));
				
//				temp = addIntermediateValues(temp); //look. got rid of these to remove numbers.
				
//				temp.put(100, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;100<br>Absolutely confident</html>"));
				temp.put(100, new JLabel("Absolutely confident"));
				break;
			case 1:
				messages[1] = "How accurate is the automated threat detector?";
				
//				temp.put(0, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0<br>Not accurate at all</html>"));
				temp.put(0, new JLabel("Not accurate at all"));
				
//				temp = addIntermediateValues(temp);
				
//				temp.put(100, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;100<br>Absolutely accurate</html>"));
				temp.put(100, new JLabel("Absolutely accurate"));
				
				break;
			default:
				messages[2] = "How much do you trust the automated threat detector?";
				
//				temp.put(0, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0<br>I don't trust it at all</html>"));
				temp.put(0, new JLabel("I don't trust it at all"));
				
//				temp = addIntermediateValues(temp);
				
//				temp.put(100, new JLabel(
//						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;100<br>I absolutely trust it</html>"));
				temp.put(100, new JLabel("I absolutely trust it")); //if you want multiple lines of text, you must use a JTextArea, then figure out how to use those with sliders
			}
			labels.add(temp); //interesting.
		}
		dataAggregator.addPollResult(displayPoll(messages, labels)); //in charge of modifying the DataAggregator's poll list.
	}

	//called by displayAndLogPolls
//	private Dictionary<Integer, JLabel> addIntermediateValues(Dictionary<Integer, JLabel> dict) {
//		for (int i = 10; i <= 90; i += 10) {
//			dict.put(i, new JLabel(Integer.toString(i)));
//		}
//		return dict;
//	}
	
	//called by displayAndLogPolls
	private PollResult displayPoll(String[] message, ArrayList<Dictionary<Integer, JLabel>> labels) {
		JFrame parent = new JFrame();
		JOptionPane optionPane = new JOptionPane();
		JSlider[] sliders = new JSlider[settings.isControlRun ? 1 : TrackerConstants.NUM_POLL_QUESTIONS]; // only one poll on control run
																					
		Object[] thingsOnOptionPane = new Object[TrackerConstants.NUM_POLL_QUESTIONS * 2];
		for (int i = 0; i < sliders.length; i++) {
			sliders[i] = getSlider(optionPane, labels.get(i));
			sliders[i].setMajorTickSpacing(10);
			
			sliders[i].setPaintTicks(false); //important 
			
			
			sliders[i].setValue(getPreviousPollResult(i));
			sliders[i].addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					JSlider slider = (JSlider) ce.getSource();
					if (!slider.getValueIsAdjusting()) {
						slider.setToolTipText(Integer.toString(slider.getValue()));
					}
				}
			});
			thingsOnOptionPane[i * 2] = message[i];
			thingsOnOptionPane[i * 2 + 1] = sliders[i];
		}
		optionPane.setMessage(thingsOnOptionPane);
		optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = optionPane.createDialog(parent, "Question");
		dialog.setVisible(true);
		PollResult p = new PollResult(counter);
		for (int i = 0; i < sliders.length; i++) {
			p.results.add(sliders[i].getValue());
		}
		return p;
	}
	
	//called by displayPoll
	private int getPreviousPollResult(int i) {
		ArrayList<PollResult> res = dataAggregator.pollResults;
		if (res.isEmpty()) {
			return 50;
		}
		return res.get(res.size() - 1).results.get(i);
	}

	//called by displayPoll
	private JSlider getSlider(final JOptionPane optionPane, Dictionary<Integer, JLabel> labels) {
		JSlider slider = new JSlider();
		slider.setMajorTickSpacing(25);
		slider.setPaintTicks(true); //this looks useless because of line ~546 above.
		slider.setPaintLabels(true);  
		slider.setLabelTable(labels);
		slider.setValue(50);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					optionPane.setInputValue(new Integer(theSlider.getValue()));
				}
			}
		};
		slider.addChangeListener(changeListener);
		return slider;
	}

	//***********************************
	
	public static String getRecommendationString(Color c) {
		for (int i = 0; i < ExpSettings.LIKELIHOOD_COLORS.length; i++) {
			if (c.equals(ExpSettings.LIKELIHOOD_COLORS[i])) {
				return ExpSettings.RECOMMENDATION_STRINGS[i];
			}
		}
		return "???"; // unclear what occurred
	}

	private String formatScore(double d, boolean addPlus) {
		
		NumberFormat f = new DecimalFormat("#0.0");
		String s = f.format(d);
		if (addPlus && (int) d >= 0)
			s = "+" + s;
		return s;
	}

	
	//uses HTML formatting to create a table. (To Learn: https://www.w3schools.com/html/html_tables.asp )
	//This could be greatly improved later. Maybe make an HTML tools class. 
	private void displayRoundFeedback() {
	
		Entry last = dataAggregator.getMostRecentEntry();
				
		StringBuilder str = new StringBuilder();
		str.append("<html><font size=10>");
		if(!(inPracticeMode && counter <= settings.trackingPTrials)){
			
			str.append("<table>");
			
			//new row: true state
			str.append("<tr><td>True state:</td><td>");
			if(last.trial.containsEnemy){
				str.append("DANGER");
			} else{
				str.append("CLEAR");
			}
			
			str.append("</td></tr>");
			
			if(!settings.isControlRun){
				//new row: recommendation identification
				str.append("<tr><td>Recommendation:</td><td>");
				str.append(getRecommendationString(last.trial.color));
				str.append(" - ");
				if(((getRecommendationString(last.trial.color) == "DANGER" || getRecommendationString(last.trial.color) == "CAUTION") == last.trial.containsEnemy)){
					str.append("<font color=green><b>CORRECT</b></font>");
				} else{
					str.append("<font color=red><b>INCORRECT</b></font>");
				}
				
				str.append("</td></tr>");
			}
			
			//new row: user identification
			str.append("<tr><td>Your identification:</td><td>");
			if(last.triggerWasPulled){
				str.append("DANGER");
			} else{
				str.append("CLEAR");
			}
			str.append(" - ");
			if(last.triggerWasPulled == last.trial.containsEnemy){
				str.append("<font color=green><b>CORRECT</b></font>");
			} else{
				str.append("<font color=red><b>INCORRECT</b></font>");
			}
			str.append("</td></tr>" );
			
			//new row: detection score
			str.append("<tr><td>Your detection score:</td><td>");
			str.append(formatScore(dataAggregator.getTotalDetectionScore(), false));
			str.append(" <b>(");
			str.append(formatScore(last.getDetectionScore(), true));
			str.append(")</b> </td></tr>");
		}
		
		//new row: tracker score
		str.append("<tr><td>Your tracker score:</td><td>");
		str.append(formatScore(dataAggregator.getTotalTrackerScore(), false));
		str.append(" <b>(");
		str.append(formatScore(last.getTrackerScore(), true) );
		str.append(")</b> </td></tr>");
		
		//new row: total score
		str.append("<tr><td>Total score:</td>");
		str.append(formatScore(dataAggregator.getTotalCombinedScore(), false));
		str.append(" <b>(");
		str.append(formatScore(last.getCombinedScore(), true) );
		str.append(")</b></td></tr></table></font></html>");
	
		JOptionPane.showMessageDialog(this, new JLabel(str.toString()), "Results", JOptionPane.PLAIN_MESSAGE);
		
	}

	
	
	//Not moving "countdown" and "createTemporary..." to the graphics manager because they also deal with audio.
	//Also, issue caused because then the new AudioPlayer() will be asigned into the copy of the reference, not the original reference.
	//audio stuff must stay together in this class.
	private void countdown() {
		audio.play("sounds/ready.wav");
		audio = new AudioPlayer();
		createTemporaryDuplicateLabelForDuration("READY", 500);
		createTemporaryDuplicateLabelForDuration("SET", 500);
		createTemporaryDuplicateLabelForDuration("GO!", 500);
	}
	
	private void createTemporaryDuplicateLabelForDuration(String s, double duration) {
		GLabel temp = new GLabel(s);
		temp.setFont(new Font("Arial", Font.PLAIN, 120));
		temp.setColor(Color.RED);
		

		ElementRectBound trackerBound = settings.getTrackerRectBound();
		double xPos = trackerBound.getCenter().getX() - (temp.getWidth() / 2);
		double yPos = trackerBound.getCenter().getY();
		
		temp.setLocation(xPos, yPos);
		add(temp);
		repaint();
		double start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < duration) {
			if (System.currentTimeMillis() % 1000 == 0)
				System.out.print(""); //?
		}
		remove(temp);
	}
	
	
	
	private void closeProgram() {
		this.exit();
	}

	
	
	///***********
	//The primary methods of the game loop are below. 
	//************
	
	
	// look. important function
	public void run() {

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dataAggregator != null)
					dataAggregator.closeAll();
				closeProgram();
			}
		});
	
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				
				dataAggregator.closeAll();
			}
		});
		
		
		
		
		//neccessary because the action listener for the start button may not execute immediately. 
		//In other words, "run" method will start no matter what after "initIntroScreen", even if the start button has not yet been pressed
		while(!readyToStart){ 
		
		}
		
		trialStartTime_temp = System.currentTimeMillis(); //look. remove this later.
		
		boolean initialized = false;
		boolean pressedButton = false;
		double lastPressed = System.currentTimeMillis();
		TrackerEntry_old te;
		double loopTime; 
		double lastTrackerCheck = System.currentTimeMillis();
		
		setupNewTrial(); 

		
		//FOR MANAGING GAME LOOP MECHANICS
		double loopStartTime = System.currentTimeMillis();
		double lastLoopStartTime = loopStartTime; //inaccurate, but needs initializing. One of the first 30 updates will be missed for the first trial.
		
		boolean runningThisFrame = true; //init value
		boolean runningLastFrame = true;
		
		while (true) { 
			
			//LOOP DIAGNOSTICS
//			loopDiag.getBack().numLoops++;
//			
//			loopDiag.lastRunning_w = loopDiag.running_w;
//			loopDiag.running_w = running;
//			
//			if(loopDiag.lastRunning_w && loopDiag.running_w){
//				loopDiag.lastLoopStartTime_w = loopDiag.loopStartTime_w;
//				loopDiag.loopStartTime_w = System.currentTimeMillis();
//				
//				loopDiag.getBack().sumOfDeltaTimes += (loopDiag.loopStartTime_w - loopDiag.lastLoopStartTime_w);
//				//System.out.println(System.currentTimeMillis() % 100000);
//			}
			
			//Manage game loop mechanics.
			runningLastFrame = runningThisFrame;
			runningThisFrame = running;
			if(runningLastFrame && runningThisFrame){
				
				lastLoopStartTime = loopStartTime;
				loopStartTime = System.currentTimeMillis();
			}
			
			
			
			
			
			// it is unclear why this is required, but something needs to check the running variable.
			//when i take it out, it still works. Not sure of this problem.
			
			System.out.print(running ? "" : ""); //this is probably slowing done the game loop
			
			if (audio.playCompleted)
				audio.close();
			if (running && (lastLoopStartTime != loopStartTime)) {
				//this solves the replication problem, but a skip is still possible.  
				

				
				if (!initialized) {
					initialized = true;
					addMouseListeners();
					addKeyListeners();
				}
				loopTime = System.currentTimeMillis();
	

				// CHECK JOYSTICK INPUTS
				if (loopTime % 33 == 0) { 
					
					//LOOP DIAGNOSTICS
//					loopDiag.getBack().numMod33++;
					
					
					joystick.poll();
					
					
					
					if (!pressedButton) {
						// TRIGGER
						if (joystickTriggerComponent.getPollData() > 0.5 &&
							!getInTrackingOnlyPractice() ) {
							audio.play("sounds/ack.wav");
							audio = new AudioPlayer();
							pressedButton = true;
							
							
							triggerWasPulled_temp = true; 
							timeSpent_temp = System.currentTimeMillis() - trialStartTime_temp;
						}
						
					}
					// ............................................................................

					// looks like the button to SWITCH SCREENS
					// refactory period so holding down the toggle doesn't continually switch.
					if (joystickSwitchComponent.getPollData() > 0.5 &&
						System.currentTimeMillis() - lastPressed > 500 &&
						!getInTrackingOnlyPractice() ){

						lastPressed = System.currentTimeMillis();

						toggleCount_temp++;
						if (gm.trackerGraphic.trackerGraphicIsUp) {
							gm.changeToImageScreen();
						} else {
							gm.changeToTrackerScreen(giveVisualAlert);
						}
						if (!inPracticeMode){
							dataAggregator.addAndPrintToggle(this.trialStartTime_temp, this.counter, gm.trackerGraphic.trackerGraphicIsUp);
						}
					}

					// ACTUALLY MOVING CURSOR AROUND.
					
					p.setMovementInput (
						joystickXAxisComponent.getPollData(), 
						joystickYAxisComponent.getPollData()
						
					);
					
					
					
					p.computeDifferentialAndSetNewPosition();
					
					
					gm.trackerGraphic.update(p.differential.x, p.differential.y);


					
					
				} // end joystick inputs

				// MAKING TRACKER ENTIRES.
				if (loopTime % 50 == 0 && counter >= 1 && loopTime - lastTrackerCheck > 20) { //shouldn't need this lastTrackerCheck business when we re-write.
					
					//LOOP DIAGNOSTICS
//					loopDiag.getBack().numMod50++;
					
					lastTrackerCheck = System.currentTimeMillis();
					te = new TrackerEntry_old(counter, p.cursor, p.movementInput, gm.trackerGraphic.trackerGraphicIsUp);
					dataAggregator.addTrackerEntry(te); // seldom do this. //In charge of modifying the DataAggregator's tracker list.
					this.totalScreenChecks_temp++;
					if (gm.trackerGraphic.trackerGraphicIsUp){
						this.onTrackerScreenChecks_temp++;
					}
				}

				// UPDATING TIMER
				if (loopTime % 10 == 0) {
					
					//LOOP DIAGNOSTICS
//					loopDiag.getBack().numMod10++;

					
					gm.otherInfo.updateTimer(trialStartTime_temp, pauseBank);
					

					// IMPORTANT
					// end of trial. prep for next
					 
					if (TrackerConstants.TRIAL_LENGTH_MS - (System.currentTimeMillis() - trialStartTime_temp - pauseBank) <= 0) {
						
						//LOOP DIAGNOSTICS
//						loopDiag.getBack().trialStopTime = System.currentTimeMillis();
						
						
						//why a new physics object for every round? so the buffet force isn't always the same.
						p = new Physics(settings); //LOOK. Im guessing that first null pointer exception is happening because this should be somewhere else.
						pressedButton = false; //when the joystick input is checking, the trigger is only checked if this is false.
						
						
						teardownFinishedTrial();
						setupNewTrial();
						
						
						
						
					}
				} // end updating timer

			} // end "running"
			
			
			
//			if(loopDiag.lastRunning_w && loopDiag.running_w){
//				loopDiag.getBack().sumOfLoopCompletionTimes += (System.currentTimeMillis() - loopDiag.loopStartTime_w);
//				
//			}
			
			
			
			
		} // end main loop
	}// end "run" method
	
	
	
	//Runs BEFORE counter gets incremented. This makes perfect sense. "counter" represents the trial number that just finished. The first time this runs, "counter" is 1. 
	private void teardownFinishedTrial(){
		

		assert(counter >= 1); //with new driver structuring, this should always be true. Eliminated odd gap period before start.
		
		pause();
		
		//MAKE ENTRY
		pushEntryToAggregator(); //old condition:if (!inPracticeMode || counter >= 1)
		
		
		// PLAY SOUNDS
		// if not in practice OR you are in combined practice.
		if (!inPracticeMode || (inPracticeMode && counter > settings.trackingPTrials)) {  //redundant, but more clear.
			if (dataAggregator.getMostRecentEntry().getCombinedScore() > 0) {
				audio.play("sounds/goodjob.wav");
				audio = new AudioPlayer();
			} else {
				audio.play("sounds/lousyjob.wav");
				audio = new AudioPlayer();
			}
		}

		// DISPLAY ROUND FEEDBACK
		displayRoundFeedback(); //old condition:   if (counter >= 1)
		

		// DISPLAY AND LOG POLLS AND REMINDERS
		// if in experiment
		 if(!inPracticeMode){ 
			 
			 //survey
			 if(settings.surveyFreq != 0 && counter % settings.surveyFreq == 0){ //settings.questionFreq 0 -> no surveys. 1 -> After every trial.
				 displayAndLogPolls(); //old condition:  if ((!inPracticeMode && counter % settings.questionFreq == 0) || (counter > settings.trackingPTrials))
			 } 
			 
			 //survey reminder
			 if(settings.surveyReminderFreq != 0 && counter % settings.surveyReminderFreq == 0){ 
				 JOptionPane.showMessageDialog(this, "Please take the survey.", "Survey Reminder", JOptionPane.PLAIN_MESSAGE);
			 } 
		 }
		 //else if in combined practice.
		 else if (inPracticeMode && counter > settings.trackingPTrials){  //redundant, more clear.
			 //survey
			 //notice -->                            v
			 if(settings.surveyFreq != 0 && (counter - settings.trackingPTrials) % settings.surveyFreq == 0){ //settings.questionFreq 0 -> no surveys. 1 -> After every trial.
				 displayAndLogPolls(); //old condition:  if ((!inPracticeMode && counter % settings.questionFreq == 0) || (counter > settings.trackingPTrials))
			 }
			 
		 }
		 
		 
		// EXIT EXPERIMET
		// if in experiment and all trials completed.
		if (!inPracticeMode && counter == settings.experimentTrials) {
			running = false;
			dataAggregator.closeAll();
			
			
			gm.otherInfo.setScoreLabel("Score: " + formatScore(dataAggregator.getTotalCombinedScore(), false) + "/" + getScoreDenominator(counter) /*12 * (counter) */);
			
			
			String message;
			if(settings.endOfExpSurveyReminderEnabled){
				message = new String("Thank you for participating. Please take the survey.");
			} else{
				message = new String("Thank you for participating.");
			}
			
			JOptionPane.showMessageDialog(this, message, "End of experiment", JOptionPane.PLAIN_MESSAGE);
			
			//LOOP DIAGNOSTICS
//			loopDiag.outputToFile();
			
			this.exit();
		}

		// TAKE A BREAK
		if (!inPracticeMode && counter % settings.breakFreq == 0 && settings.breakFreq != 0 ) { //settings.breakFreq = 0 -> no breaks.
			JOptionPane.showMessageDialog(this, "You may take a short break before continuing.", "Break", JOptionPane.PLAIN_MESSAGE);
		}

		// SWITCH FROM PRACTICE INTO EXPERIMENT
		// if (in practice mode and you completed all the practice trials)
		if (inPracticeMode && counter == settings.trackingPTrials + settings.combinedPTrials) {
			
			inPracticeMode = false;
			counter = 0; // will get incremented before next trial starts. 
			
			populateExperimentTrialData(); 
			JOptionPane.showMessageDialog(this, "The practice phase is over. You are about to begin the experiment.", "End of practice", JOptionPane.PLAIN_MESSAGE);
			
			new PracticeDataOutput(dataAggregator).output();
			dataAggregator = new DataAggregator(System.currentTimeMillis(), fileName, settings.overallReliability, settings.alarmIsBinary, settings.isControlRun, false);
			dataAggregator.printSettings(settings);
			
			
			
			//look. really, this should go into setupNewTrial, and that method should have a way of knowing if we just began experiment. 
			//this is a very detailed improvement that could be made later, but this works right now. 
			gm.otherInfo.setMessage1(settings.msg1);
			gm.otherInfo.setMessage2(settings.msg2);
		}
		
		
		
	}
	
	private void setupNewTrial(){
		
		counter++;// LOOK. counter incremented here. Now in order to work properly for the first trial, counter must actually be initialized to 0.
		
	
		//LOOP DIAGNOSTIC
//		loopDiag.addTrial(counter);
		
		
		//**********************************************************
		//MANAGING ALERT MODALITY
		//Control Run
		if(settings.isControlRun){
			giveVisualAlert = false;
			giveAuditoryAlert = false;
		}
		//Not Control Run
		else{
			//Practice Mode
			if(inPracticeMode){ 
				if(counter == 1){
					giveVisualAlert = false;
					giveAuditoryAlert = false;
				}
				else if(counter == settings.trackingPTrials + 1){
					giveVisualAlert = settings.visualAlert;
					giveAuditoryAlert = settings.auditoryAlert;
				}
			}
			//Experiment
			else{
				if(counter == 1){
					giveVisualAlert = settings.visualAlert;
					giveAuditoryAlert = settings.auditoryAlert;
				}
				
			}
		}
		
		//filled colors will still change while the recommender is not visible.
		gm.recommender.setAllVisible(giveVisualAlert); 
		//*********************************************************
		
		
		
		//SET LABELS.
		int trialsDenominator = inPracticeMode? (settings.trackingPTrials + settings.combinedPTrials) : settings.experimentTrials;
		gm.otherInfo.setTrialNumberLabel("Trial: " + counter + "/" + trialsDenominator);
		gm.otherInfo.setScoreLabel("Score: " + formatScore(dataAggregator.getTotalCombinedScore(), false) + "/" + getScoreDenominator(counter - 1) /*12 * (counter - 1)*/  ); //look. hard coded scoring stuff
		
		//Reset screen
		gm.trackerGraphic.reset();
		gm.changeToTrackerScreen(giveVisualAlert);
		
		//Let them know they can now view images if appropriate
		if(getInCombinedPractice() && counter == settings.trackingPTrials + 1){
			JOptionPane.showMessageDialog(this, "You have finished the tracking-only practice trials. You can now switch screens to view images.", "Images Available", JOptionPane.PLAIN_MESSAGE);
		}
				
		//Resume
		countdown();
		unpause();
		pauseBank = 0;
		
		
		
		//Give Warning
		putUpNextImageSetAndWarnings(); //this actually plays the warning audio clip as well.
	
		//reset data gathering
		resetDataCollectedEachRound();	
		
		
		//LOOP DIAGNOSTIC
//		loopDiag.getBack().trialStartTime = trialStartTime;
		
	}
	
	private void resetDataCollectedEachRound(){
		
		triggerWasPulled_temp = false; 
		timeSpent_temp = TrackerConstants.TRIAL_LENGTH_MS;
		totalScreenChecks_temp = 0;
		onTrackerScreenChecks_temp = 0;
		toggleCount_temp = 0;
		trialStartTime_temp = System.currentTimeMillis();
		
	}
	
	//for conveinience. I want more detail than inPractice at some points, so I'll build off it. 
	private boolean getInTrackingOnlyPractice(){
		return inPracticeMode && counter <= settings.trackingPTrials;
	}
	private boolean getInCombinedPractice(){
		return inPracticeMode && counter > settings.trackingPTrials;
	}
	private boolean getInExperiment(){
		return !inPracticeMode;
	}
	
	//look. fix this so it uses the getters above.
	private String getScoreDenominator(int trialsCompleted){
		
		
		int possiblePoints;
		
		if(getInExperiment()){
			possiblePoints = trialsCompleted * 12;
			
		}
		else{
			if(trialsCompleted <= settings.trackingPTrials){
				possiblePoints = trialsCompleted * 10;
			}
			else{
				possiblePoints = (settings.trackingPTrials * 10) + ((trialsCompleted - settings.trackingPTrials) * 12);
				
			}
		}
		
		return possiblePoints + ".0";
	}
	
	
}
