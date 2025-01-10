
 
/**
 * Stores information about and results of a single trial. 
 * This is different from Trial type, because this class gives information about how the user responded, when the trial occured, etc. 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 *
 */
public class Entry { 
	
	/**
	 * The Trial that this Entry corresponds to.
	 */
	public Trial trial;
	
	/**
	 * Whether or not the user pulled the trigger.
	 */
	public boolean triggerWasPulled;
	
	/**
	 * How long the user took to pull the trigger. (10000 if not pulled).
	 */
	public double timeSpent;

	/**
	 * The trial number the Entry corresponds to.
	 */
	public int trialNumber;
	
	/**
	 * The start time of the trial corresponding to this entry.
	 */
	public double trialAbsoluteStartTime;

	/**
	 * Whether or not the trial included the detection task.
	 */
	public boolean noDetection;
	
	/**
	 * The proportion of the time that the user was viewing the tracker during the trial.
	 */
	public double onTrackerScreenProportion;
	
	/**
	 * The number of times the user toggled screens during the trial. 
	 */
	public int toggleCount;
	
	/**
	 * The root mean square distance from the origin to the cursor. Used for scoring.
	 */
	public double rms = 0;
	
	//constructor: function used to create a new instance of this class
	public Entry (Trial trial_in, 
				  boolean triggerWasPulled_in, 
				  double trialAbsoluteStartTime_in, 
				  int trialNumber_in, 
				  double onTrackerScreenProportion_in, 
				  int toggleCount_in, 
				  boolean noDetection_in, 
				  double timeSpent_in) {
		
		trial = trial_in;
		triggerWasPulled = triggerWasPulled_in;
		trialAbsoluteStartTime = trialAbsoluteStartTime_in;
		trialNumber = trialNumber_in;
		onTrackerScreenProportion = onTrackerScreenProportion_in;
		toggleCount = toggleCount_in;
		noDetection = noDetection_in;
		timeSpent = timeSpent_in;

	}
	
	
	
	//look. scoring stuff.
	//out of 2
	public double getDetectionScore () {
		if (noDetection){
			return 0;
		}
		double score = 0;
		
		if (triggerWasPulled == trial.containsEnemy) {
			score += 5;
			
			if(trial.containsEnemy){
				score -= 5 * timeSpent / 10000;
			}
		}
		
		return Math.max(score, 0); 
	}
	
	//out of 10
	public double getTrackerScore () {
		for (int i = 0; i < TrackerConstants.RMS_SCORE_THRESHOLDS.length; i++) {
			if (rms < TrackerConstants.RMS_SCORE_THRESHOLDS[i]) {
				return 10 - i;
			}
		}
		return 0;
		
	}
	
	public double getCombinedScore () { 
		return getDetectionScore() + getTrackerScore();
	}

	public String getRecommendationString () {
		for (int i = 0; i < ExpSettings.RECOMMENDATION_STRINGS.length; i++) {
			if (trial.color == ExpSettings.LIKELIHOOD_COLORS[i]) {
				return ExpSettings.RECOMMENDATION_STRINGS[i];
			}
		}
		return "???"; //unclear what occurred
	}

}
