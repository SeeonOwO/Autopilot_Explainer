/**
 * Represents information about the tracker during an instant of the game. 
 * Many of these are gathered for each trial. 
 * @author Kevin Li
 *
 */
public class TrackerEntry_old {
	public int trialNumber;
	public Tuple position;
	public Tuple joystickInput;
	public double absoluteTime;
	//if they are viewing the tracker, not images.
	public boolean onTracker; 
	
	public TrackerEntry_old (int counter, Tuple cursor, Tuple joy, boolean tracker) {
		trialNumber = counter;
		position = cursor; //relative to a 0,0 origin //see Physics line ~200 as to why a new one is not made here. Yes this inconsistency is dumb. Not a priority to fix.
		joystickInput = new Tuple(joy.x, joy.y); 
		absoluteTime = System.currentTimeMillis();
		onTracker = tracker;
	}
}
