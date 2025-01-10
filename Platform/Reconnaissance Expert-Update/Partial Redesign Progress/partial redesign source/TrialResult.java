import java.util.ArrayList;
/**
 * 
 * Do NOT modify data you receive from getter methods.
 * This object should be made just before the next trial starts, gets unpaused, etc. 
 * It has methods for building/interaction as the trial goes on. 
 * 
 * It is the user's responsibility to call the calculateAll method before using any of the getters.
 * 
 * @author BenPinzone
 *
 */


public class TrialResult {
	
	public TrialResult(long dataCollectorCreationTime, int trialNumber_in, Trial trial_in){
		
		absoluteTrialStartTime = System.currentTimeMillis();
		relativeTrialStartTime = absoluteTrialStartTime - dataCollectorCreationTime;
		trialNumber = trialNumber_in;
		trial = trial_in;
		
		trackerEntries = new ArrayList<TrackerEntry>();
		onTrackerScreenProportion = null;
		triggerEntry = null;
		toggleEntries = new ArrayList<ToggleEntry>();
		userIsCorrect = null;
		scoreEntry = null;
		rms = null;
	}
	

	/**
	 * Absolute Trial Start Time.
	 * Determined: At construction.
	 * Retrieval: Getter.
	 * 
	 */
	private final long absoluteTrialStartTime;
	public long getAbsoluteTrialStartTime(){ 
		return absoluteTrialStartTime;
	}
	//***************************************************************************
	
	/**
	 * Relative Trial Start Time (relative to data collector creation time).
	 * Determined: At construction.
	 * Retrieval: Getter.
	 * 
	 */
	private final long relativeTrialStartTime; 
	public long getRelativeTrialStartTime(){ 
		return relativeTrialStartTime;
	}
	//***************************************************************************
	
	/**
	 * Trial Number.
	 * Determined: At construction.
	 * Retrieval: Getter
	 */
	private final int trialNumber;
	public int getTrialNumber(){
		return trialNumber;
	}
	//***************************************************************************
	
	/**
	 * Trial.
	 * Determined: At construction.
	 * Retrieval: Getter
	 */
	private final Trial trial;
	public Trial getTrial(){
		return trial;
	}
	//***************************************************************************
	
	/**
	 * Tracker Entries.
	 * Determined: By interaction method.
	 * Retrieval: Getter.
	 */
	private final ArrayList<TrackerEntry> trackerEntries;
	public ArrayList<TrackerEntry> getTrackerEntries(){
		return trackerEntries;
	}
	public TrackerEntry getMostRecentTrackerEntry(){
		if(trackerEntries.isEmpty()){
			return null;
		}
		return trackerEntries.get(trackerEntries.size() - 1);
		
	}
	//Interaction
	/**
	 * Tuples will be copied. The Tuples you give this method are free to change. 
	 * @param position
	 * @param joystickInput
	 * @param wasViewingTracker
	 */
	public void logTrackerEntry(Tuple position, Tuple joystickInput, boolean wasViewingTracker){
		trackerEntries.add(new TrackerEntry(position, joystickInput, wasViewingTracker));
	}
	//***************************************************************************
	/**
	 * On Tracker Screen Proportion.
	 * Determined: Calculated (trackerEntries).
	 * Retrieval: Getter
	 */
	private Double onTrackerScreenProportion;
	public Double getOnTrackerScreenProportion(){
		return onTrackerScreenProportion;
	}
	//Calculation
	private void calculateOnTrackerScreenProportion(){
		int onTrackerChecks = 0;
		int totalChecks = 0;
		for(TrackerEntry someTrackerEntry : trackerEntries){
			totalChecks++;
			if(someTrackerEntry.getWasViewingTracker()){
				onTrackerChecks++;
			}
		}
		
		onTrackerScreenProportion =  ((double)(onTrackerChecks)) / totalChecks; 
	}
	//***************************************************************************
	
	
	/**
	 * Trigger Entry (null if trigger never pulled).
	 * Determined: By interaction method.
	 * Retrieval: Getter.
	 * This will be null if the trigger was never pulled.
	 */
	private TriggerEntry triggerEntry;
	public TriggerEntry getTriggerEntry(){
		return triggerEntry;
	}
	public boolean getTriggerWasPulled(){
		return triggerEntry != null;
	}
	//Interaction
	public void logTriggerEntry(){
		if(triggerEntry == null){
			triggerEntry = new TriggerEntry();
		}
	}
	

	//***************************************************************************
	
	/**
	 * Toggle Entries (null if no toggles).
	 * Determined: By interaction method.
	 * Retrieval: Getter.
	 */
	private final ArrayList<ToggleEntry> toggleEntries;
	public ArrayList<ToggleEntry> getTogglesEntries(){
		return toggleEntries;
	}
	public Long getTimeUntilFirstToggle(){
		if(toggleEntries == null){
			return null;
		}
		else{
			return toggleEntries.get(0).getTimeData().getRelativeCreationTime();
		}
	}
	//***************************************************************************
	
	/**
	 * User Is Correct. 
	 * Determined: Calculated (triggerEntry and trial).
	 * Retrieval: Getter.
	 */
	private Boolean userIsCorrect;
	public Boolean getUserIsCorrect(){
		return userIsCorrect;
	}
	private void calculateUserIsCorrect(){
		if(getTriggerWasPulled() == getTrial().getContainsThreat()){
			userIsCorrect = true;
		}
		else{
			userIsCorrect = false;
		}
	}
	//***************************************************************************
	
	/**
	 * Score Entry.
	 * Determined: Calculated (rms, triggerEntry, trial, userIsCorrect).
	 * Retrieval: Getter.
	 */
	private ScoreEntry scoreEntry;
	public ScoreEntry getScoreEntry(){
		return scoreEntry;
	}

	private void calculateScoreEntry(){
		
		//TRACKING SCORE
		double trackingScore = 0;

		for (int i = 0; i < TrackerConstants.RMS_SCORE_THRESHOLDS.length; i++) {
			if (rms < TrackerConstants.RMS_SCORE_THRESHOLDS[i]) {
				trackingScore++;
			}
		}
		
		
		
		
		
		
		
		
		double detectionScore;
		
		
	}
	//***************************************************************************
	
	/**
	 * RMS.
	 * Determined: Calculated (trackerEntries).
	 * Retrieval: Getter.
	 */
	private Double rms;
	public Double getRms(){
		return rms;
	}
	//***************************************************************************
	
	
	
	
	
	
	//*******************************************************************************
	//INNER CLASSES
	
	public class TimeData{
		
		//Absolute Creation Time
		private final long absoluteCreationTime;
		public long getAbsoluteCreationTime(){
			return absoluteCreationTime;
		}
		
		//Relative Creation Time (relative to trial start)
		private final long relativeCreationTime;
		public long getRelativeCreationTime(){
			return relativeCreationTime;
		}
		
		public TimeData(){
			absoluteCreationTime = System.currentTimeMillis();
			relativeCreationTime = absoluteCreationTime - TrialResult.this.getAbsoluteTrialStartTime();
		}
		
	}
	
	//public or private?
	public class TrackerEntry{
		
		//Time Data
		private final TimeData timeData;
		public TimeData getTimeData(){
			return timeData;
		}
		
		//Position
		private final Tuple position;
		public Tuple getPosition(){
			return position;
		}
		
		//Joystick Input
		private final Tuple joystickInput;
		public Tuple getJoystickInput(){
			return joystickInput;
		}
		
		//Is Viewing Tracker
		private final boolean wasViewingTracker;
		public boolean getWasViewingTracker(){
			return wasViewingTracker;
		}
		
		public TrackerEntry(Tuple pos_in, Tuple joy_in, boolean wasViewingTracker_in){
			timeData = new TimeData();
			position = new Tuple(pos_in);
			joystickInput = new Tuple(joy_in);
			wasViewingTracker = wasViewingTracker_in;
			
		}	
	}
	
	
	public class TriggerEntry{
		
		//Time Data
		private final TimeData timeData;
		public TimeData getTimeData(){
			return timeData;
		}
		public TriggerEntry(){
			timeData = new TimeData();
		}
	}
	
	public class ToggleEntry{
		
		//Time Data
		private final TimeData timeData;
		public TimeData getTimeData(){
			return timeData;
		}
		
		//Switched To Tracker
		private final boolean switchedToTracker;
		public boolean getSwitchedToTracker(){
			return switchedToTracker;
		}
		
		public ToggleEntry(boolean switchedToTracker_in){
			timeData = new TimeData();
			switchedToTracker = switchedToTracker_in;
		}
	}
	

	public class ScoreEntry{
		
		//Tracking Score
		private final double trackingScore;
		public double getTrackingScore(){
			return trackingScore;
		}
		
		//Detection Score
		private final double detectionScore;
		public double getDetectionScore(){
			return detectionScore;
		}
		
		//Combined Score
		private final double combinedScore;
		public double getCombinedScore(){
			return combinedScore;
		}
		
		public ScoreEntry(double t, double d, double c){
			trackingScore = t;
			detectionScore = d;
			combinedScore = c;
		}
		
	}
	

	

}
