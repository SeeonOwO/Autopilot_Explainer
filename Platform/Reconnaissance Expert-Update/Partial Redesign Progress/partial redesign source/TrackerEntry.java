
public class TrackerEntry {
	
	private final Tuple position;
	private final Tuple joystickInput;

	private final long absoluteTime;
	private final long relativeTime; //relative to start of trial.
	
	private final boolean isViewingTracker;
	
	public TrackerEntry(Tuple position_in, Tuple joystickInput_in, boolean isViewingTracker_in, long trialStartTime){
		position = new Tuple(position_in);
		joystickInput = new Tuple(joystickInput_in);
		absoluteTime = System.currentTimeMillis();
		relativeTime = absoluteTime - trialStartTime;
		isViewingTracker = isViewingTracker_in;
	}
	
	public Tuple getPosition(){
		return position;
	}
	public Tuple getJoystickInput(){
		return joystickInput;
	}
	public long getAbsoluteTime(){
		return absoluteTime;
	}
	
	public boolean getIsViewingTracker(){
		return isViewingTracker;
	}
	
	

}
