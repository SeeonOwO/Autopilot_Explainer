import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Timestamp;

import java.util.ArrayList;

/**
 * Collects and outputs data gathered from the experiment. 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 * 
 *
 */
public class DataAggregator {
	
	//TESTING/DEBUGGING. Public so they can also be used by PracticeDataOutput.
	public static boolean trackerTestOutputEnabled = false;
	public static boolean entryTestOutputEnabled = false;
	public static boolean toggleTestOutputEnabled = false;
	public static boolean pollResultTestOutputEnabled = false;
	
	public ArrayList<Entry> entryList; //important. //notee: Entry has a Trial field.
	public ArrayList<PollResult> pollResults; //important
	public double aggregatorStartTime;
	public ArrayList<TrackerEntry> trackerData;  //important
	public String fileNameBase;
	public double reliability;
	public boolean isBinaryAlarm;
	public boolean isControl;
	//public NumberFormat f = new DecimalFormat("#0.00");
	public boolean isPracticeRun;
	
	public PrintWriter detectionOut;
	public PrintWriter pollOut;
	public PrintWriter trackerOut;
	public PrintWriter toggleOut;
	public long firstToggleTime = -1;
	
	//additions
	public PrintWriter settingsOut;
	
	//Two of these are made. One for practice, one for experiment. 
	//Although, there is a separate(?) (are both used for practice?) class for outputting practice data. "PracticeDataOutput.java", 
	//practice data output OR practiceData output?
	//yes, this data aggregator is used by practice for output as well. just take a look at the call to addPollResult in displayAndLogPolls.
	
	//*********************************************************************************
	//FILE / NAME MANAGEMENT
	public DataAggregator (double startTime, String file, double r, boolean binary, boolean control, boolean practice) {
		fileNameBase = "results" + File.separator + file;
		entryList   = new ArrayList<Entry>();
		pollResults = new ArrayList<PollResult>();
		trackerData = new ArrayList<TrackerEntry>();
		aggregatorStartTime = startTime;
		reliability = r;
		isBinaryAlarm = binary;
		isControl = control;
		modifyFileNameBase();
		isPracticeRun = practice;
		openAllFiles ();
	}
	
	private void openAllFiles () {
		try {
			if (!isPracticeRun) {
				File toggleFile = new File(fileNameBase + "toggle_output.csv");
				toggleFile.getParentFile().mkdirs();
				
				toggleOut    = new PrintWriter(new FileWriter(toggleFile));
				detectionOut = new PrintWriter(new FileWriter(fileNameBase + "detection_output.csv")); //look
				trackerOut   = new PrintWriter(new FileWriter(fileNameBase + "tracker_output.csv"));
				pollOut      = new PrintWriter(new FileWriter(fileNameBase + "poll_output.csv"));
				
				Timestamp t = new Timestamp(System.currentTimeMillis());
				detectionOut.println(t);
				trackerOut.println(t);
				pollOut.println(t);
				
				settingsOut = new PrintWriter(new FileWriter(fileNameBase + "settings_output.csv"));
				
				printColumnHeaders();
				
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeAll () {
		if (!isPracticeRun) {
			pollOut.close();
			detectionOut.close();
			trackerOut.close();
			toggleOut.close();
			settingsOut.close();
		}
	}
	
	
	private void modifyFileNameBase () {
		//reliability will be rounded, but thats OK. look.
		fileNameBase += "_" + (isControl ? "c" : (isBinaryAlarm ? "b" : "l") + "_" + Integer.toString((int)(reliability * 100))) + File.separator;
	}
	
	private String stripFileName (String fileName) {
		String[] split = fileName.split("/");
		return split[split.length - 1];
	}
	
	
	//*********************************************************************************
	//COLUMN HEADERS
	private void printColumnHeaders(){
		
		detectionOut.println(
				"Trial Number,Time Stamp,Relative Start Time,"
				+ "Threat Present,Threat Position,Warning Generated,Trigger Pulled,"
				+ "Time Until Trigger Pull (10000 if not pulled),Time Until First Toggle,Detection Score,"
				+ "Tracking Score,RMS,On Tracker Screen Proportion,Toggle Count,Image 0, Image 1,Image 2,Image 3"
		); 
		
		trackerOut.println("Trial Number,Time Stamp,Relative Start Time,Cursor X (Relative to Origin),Cursor Y(Relative to Origin),Joystick X,Joystick Y");
		
		toggleOut.println("Trial Number,Time Stamp,Relative Start Time,Switched To Tracker");
		
		pollOut.println("Trial Number,Confidence In Performing Task,Perceived Reliability,Trust");
	}
	
	
	//*********************************************************************************
	//ENTRIES
	//formerly known as "add"
	//essentially called from tearDownFinishedTrial
	public void addEntry (Entry e) {
		if (entryList.size() == 0 || entryList.get(entryList.size() - 1).trialNumber != e.trialNumber) {//take only first answer
			
			entryList.add(e);
			this.computeLastEntryRms(e.trialNumber);
			
			if (!isPracticeRun) {
				printEntry(e);
			}
		}
	}
	
	public void printEntry (Entry e) {
		if (firstToggleTime == -1) {
			firstToggleTime = (long) (e.trialAbsoluteStartTime - 1);
		}
		
		
		
		
		PrintWriter fout = detectionOut;
		fout.print(
			e.trialNumber + "," + 
			new Timestamp((long)e.trialAbsoluteStartTime) + "," +  //look al these were spaces.
			(e.trialAbsoluteStartTime - this.aggregatorStartTime) + "," + 
			e.trial.containsEnemy + "," + 
			e.trial.targetLocationPlus() + "," +
			e.getRecommendationString() + "," + 
			e.triggerWasPulled + "," + 
			e.timeSpent + "," + 
			(firstToggleTime - e.trialAbsoluteStartTime) + "," + 
			(e.getDetectionScore()) + "," +
			(e.getTrackerScore()) + "," + 
			(e.rms) + ","
		);

		fout.print(e.onTrackerScreenProportion + ",");
		fout.print(e.toggleCount + ",");
		for (String s : e.trial.imageSet) {
			fout.print(stripFileName(s) + ",");
		}
		fout.println();
		
		
		if(entryTestOutputEnabled){
			
			System.out.println("");
			System.out.println("Trial number: " + e.trialNumber);
			System.out.println("Time stamp: " + new Timestamp((long)e.trialAbsoluteStartTime));
			System.out.println("relative time: " + (e.trialAbsoluteStartTime - this.aggregatorStartTime));
			System.out.println("contains enemy: " + e.trial.containsEnemy);
			System.out.println("target location: " + e.trial.targetLocation());
			System.out.println("recommender string: " + e.getRecommendationString());
			System.out.println("trigger was pulled: " + e.triggerWasPulled);
			System.out.println("time spent: " + e.timeSpent);
			System.out.println("time until first toggle: " + (firstToggleTime - e.trialAbsoluteStartTime));
			System.out.println("detection score: " + (e.getDetectionScore()));
			System.out.println("tracker score: " + (e.getTrackerScore()));
			System.out.println("rms: " + e.rms);
			
			System.out.println("on tracker proportion: " + e.onTrackerScreenProportion);
			System.out.println("toggle count: " + e.toggleCount);
			
			System.out.println("images:");
			for (String s : e.trial.imageSet) {
				System.out.println("   " + stripFileName(s));
			}
			System.out.println("-----------------------------------------------------------------------");
			
			
		}
		firstToggleTime = -1;
	}
	
	public void computeLastEntryRms(int trial) {
		double quadraticSum = 0;
		int count = 0;
		for (int i = trackerData.size() - 1; i >= 0; i--) {
			if (trackerData.get(i).trialNumber != trial) {
				break; //sigh
			}
			quadraticSum += Math.pow(trackerData.get(i).position.x, 2) + Math.pow(trackerData.get(i).position.y, 2);
			count++;
		}
		this.getMostRecentEntry().rms = Math.sqrt(quadraticSum / count);
	}
	
	public Entry getMostRecentEntry () {
		//look
		//System.out.println("Entry list size: " +  entryList.size());
		return entryList.get(entryList.size() - 1);
	}
	
	//*********************************************************************************
	//TOGGLES
	//called from main game loop.
	public void addAndPrintToggle (double trialStartTime, int trial, boolean toTracker) {
		long time = System.currentTimeMillis();
		
		toggleOut.println(trial + "," + new Timestamp(time) + "," + (time - trialStartTime) + "," + toTracker);
		
		if(toggleTestOutputEnabled){
			System.out.println(trial + "," + new Timestamp(time) + "," + (time - trialStartTime) + "," + toTracker);
		}
		
		if (this.firstToggleTime == -1) {
			firstToggleTime = time;
		}
	}
	
	
	//*********************************************************************************
	//POLLS
	//essentially called from tearDownFinishedTrial
	public void addPollResult (PollResult p) {
		pollResults.add(p);
		if (!isPracticeRun) {
			printPollResult(p);
		}
	}
	
	public void printPollResult (PollResult p) {
		PrintWriter fout = pollOut;
		fout.print(p.trialNumber);
		for (int r : p.results) {
			fout.print("," + r);
		}
		fout.println();
		
		if(pollResultTestOutputEnabled){
			System.out.print(p.trialNumber);
			for (int r : p.results) {
				System.out.print("," + r);
			}
			System.out.println();
			
		}
		
		
	}
	
	//*********************************************************************************
	//TRACKER ENTRIES
	//called from main game loop.
	public void addTrackerEntry (TrackerEntry t) {
		trackerData.add(t);
		if (!isPracticeRun) {
			printTrackerEntry(t);
		}
	}
	
	public void printTrackerEntry (TrackerEntry t) {
		PrintWriter fout = trackerOut;
		fout.println(
				t.trialNumber + "," + 
				new Timestamp((long)t.absoluteTime) + "," +
				(t.absoluteTime - this.aggregatorStartTime) + "," + 
				t.position.toStringComma() + "," +
				t.joystickInput.toStringComma()
		);
		
		if(trackerTestOutputEnabled){
			System.out.println(
					t.trialNumber + ",   " + 
					(t.absoluteTime - this.aggregatorStartTime) + ",   " + 
					t.position.toStringComma() + ",   " +
					t.joystickInput.toStringComma()
			);
		}
		
		
	}
	
	//*********************************************************************************
	//EXPERIMENT SETTINGS
	//look.
	public void printSettings(ExpSettings someSettings){
		PrintWriter fout = settingsOut;
		fout.println("Is Control Run," + someSettings.isControlRun);
		fout.println("Is Using New Graphics,"    + someSettings.isUsingNewGraphics);
		fout.println("Tracking Practice Trials," + someSettings.trackingPTrials);
		fout.println("Tracking And Detection Practice Trials," + someSettings.combinedPTrials);
		fout.println("Experiment Trials,"  + someSettings.experimentTrials);
		fout.println("Alarm Is Binary,"    + someSettings.alarmIsBinary);
		fout.println("Visual Alert Enabled,"   + someSettings.visualAlert);
		fout.println("Auditory Alert Enabled," + someSettings.auditoryAlert);
		fout.println("Danger Alerts Only Enabled," + someSettings.dangerAlertsOnly);
		fout.println("Reliability Profile," + someSettings.profile);
		fout.println("Overall Reliability %,"       + (int)(someSettings.overallReliability*100));
		fout.println("Hit Rate %,"                  + (int)(ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.HIT_RATE,                 someSettings.quotaList, someSettings.alarmIsBinary)*100));
		fout.println("Correct Rejection Rate %,"    + (int)(ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.CORRECT_REJECTION_RATE,    someSettings.quotaList, someSettings.alarmIsBinary)*100));
		fout.println("Positive Predictive Value %," + (int)(ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.POSITIVE_PREDICTIVE_VALUE, someSettings.quotaList, someSettings.alarmIsBinary)*100));
		fout.println("Negative Predictive Value %," + (int)(ReliabilityCalculator.calculate_reliability(ReliabilityCalculator.ReliabilityType.NEGATIVE_PREDICTIVE_VALUE, someSettings.quotaList, someSettings.alarmIsBinary)*100));
		fout.println("Tracking Difficulty Multiplier," + someSettings.trackerDifficultyMultiplier);
		fout.println("Survey Frequency," + someSettings.surveyFreq);
		fout.println("Survey Reminder Frequency (Exp only),"      + someSettings.surveyReminderFreq);
		fout.println("End Of Experiment Survey Reminder Enabled," + someSettings.endOfExpSurveyReminderEnabled);
		fout.println("Break Frequency,"      + someSettings.breakFreq);
		fout.println("Generation Is Random," + someSettings.genIsRandom);
		fout.println("Message 1," + someSettings.msg1);
		fout.println("Message 2," + someSettings.msg2);
		
		
		if(someSettings.alarmIsBinary){
			fout.println("Quota Hit,"  + someSettings.quotaList.get(ExpSettings.hitIndex));
			fout.println("Quota Miss," + someSettings.quotaList.get(ExpSettings.missIndex));
			fout.println("Quota False Alarm," + someSettings.quotaList.get(ExpSettings.falseAlarmIndex));
			fout.println("Quota Correction Rejection," + someSettings.quotaList.get(ExpSettings.correctRejectionIndex));
		}
		else{
			fout.println("Quota Threat Given Danger,"  + someSettings.quotaList.get(ExpSettings.threatGivenDangerIndex));
			fout.println("Quota Threat Given Warning," + someSettings.quotaList.get(ExpSettings.threatGivenWarningIndex));
			fout.println("Quota Threat Given PosClr,"  + someSettings.quotaList.get(ExpSettings.threatGivenPosClrIndex));
			fout.println("Quota Threat Given Clr,"     + someSettings.quotaList.get(ExpSettings.threatGivenClrIndex));
			
			fout.println("Quota Clear Given Danger,"  + someSettings.quotaList.get(ExpSettings.clearGivenDangerIndex));
			fout.println("Quota Clear Given Warning," + someSettings.quotaList.get(ExpSettings.clearGivenWarningIndex));
			fout.println("Quota Clear Given PosClr,"  + someSettings.quotaList.get(ExpSettings.clearGivenPosClrIndex));
			fout.println("Quota Clear Given Clr,"     + someSettings.quotaList.get(ExpSettings.clearGivenClrIndex));
		}
	}
	//*********************************************************************************
	//SCORE
	public double getTotalCombinedScore () {
		double total = 0;
		for (Entry e : entryList) {
			total += e.getCombinedScore();
		}
		return total;
	}
	
	public double getTotalTrackerScore () {
		double total = 0;
		for (Entry e : entryList) {
			total += e.getTrackerScore();
		}
		return total;
	}
	
	public double getTotalDetectionScore () {
		double total = 0;
		for (Entry e : entryList) {
			total += e.getDetectionScore();
		}
		return total;
	}
	
	
	
	
}
