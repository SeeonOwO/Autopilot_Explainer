
public class TrackerConstants {
	
	//revamp all this graphics stuff so that all graphics info for the specific one button tracker mode is in one place.
	
	public static final double TRIAL_LENGTH_MS = 10000; //original: 10000.

	
	public static final double JOYSTICK_SENSITIVITY = 0.3; //0.3
	
	//tracker difficulties multipliers
	public static final double EASY_MULTIPLIER = 0.75;
	public static final double NORMAL_MULTIPLIER = 1.0;
	public static final double HARD_MULTIPLIER = 2.5;
	

	//when this was "CURSOR_SIZE" its original value was 30.
	public static final double CURSOR_SIZE_NORMALIZED = 2 * 53.5714285714; //   30 * (1000/560); //in [-1000, 1000] units  //used for both the graphical size, and the threshold for being "steady"
	
	//when this was "CROSS_SIZE" its original value was 20.
	public static final double CROSS_SIZE_NORMALIZED  = 2 * 35.7142857143; //    20 * (1000/560); //in [-1000, 1000] units
	
	//for visual alerts. The color of the alert given will be more opaque.
	public static final int INACTIVE_ALARM_ALPHA = 30;
	public static final int ACTIVE_ALARM_ALPHA = 255;
	
	//for initial message page
	public static final int LINE_HEIGHT = 30;
	public static final int INIT_SCREEN_BUFFER = 100;
	
	
	//Used for scoring.
	//old
	//public static final double[] RMS_THRESHOLDS = new double[] {16.5, 22.3, 28.1, 33.9, 39.7, 45.5, 51.3, 57.1, 62.9, 68.7};
	
	//These are scaled to use new positioning system. Multiplied by (1000/560) ~ 1.78571
	public static final double[] RMS_SCORE_THRESHOLDS = new double[] {29.464, 39.821, 50.179, 60.536, 70.893, 81.250, 91.607, 101.964, 112.321, 122.679};
	
	//The number of questions in a single poll.
	public static final int NUM_POLL_QUESTIONS = 3;
	
	//Data about the image base: (experiment images only)
	//numLetters * (because flips) * numImagesForEachLetter - (deficiencies in M and R)
	public static final int numAbsentImagesInDatabase = 20 * 2 * 20 - (3*2 + 1*2); // = 792     792 / 4 = 198
	public static final int numPresentImagesInDatabase = 60;
	
	
	/**
	 * Takes in_1 and converts it from scale 1 to scale 2.
	 * Requires max_1 > min_1 and max_2 > min_2 and in_1 is in [min_1, max_1]
	 * Ex: -560, 560, 200, -1000, 1000 -> 357.1428. 
	 * 
	 * @param min_1
	 * @param max_1
	 * @param in_1
	 * @param min_2
	 * @param max_2
	 * @return
	 */
	public static double linearScale(double min_1, double max_1, double in_1, double min_2, double max_2){
		
		double span_1 = max_1 - min_1;
		double span_2 = max_2 - min_2;
		
		double in_1_spanPercent = (in_1 - min_1) / span_1;
		
		return min_2 + (in_1_spanPercent * span_2);
	}
	
}
