
//used to compute the next tracker location using joystick input
/**
 * Manages the positioning data for the cursor. 
 * <p>
 * Calculates the random buffet force, 
 * calculates a differential based on buffet force and user input, 
 * ensures the position stays within certain bounds (clamps the position). 
 * 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Re-wrote in order for it to work after fixing game loop timing mechanics. I'd be happy to answer questions: benpinzone7@gmail.com
 * 
 *
 */
public class Physics {
	

	private static final double C_X = (1 + Math.sqrt(5)) / 2;
	private static final double C_Y = Math.PI / 2;
	
	//These should NOT be adjusted in order to change difficulty. 
	private static final int NUM_PHASE_ANGLES = 6;
	private static final double A1_MOMENTUM = 0.2; // 0.2
	private static final double A2_REPEL = 0.0001;  //0.0001
	private static final double A4_BUFFET = 0.1; //0.1
	
	private static final double TIME_STEP = 0.015; // 0.015
	private double[] phaseAngles = new double[NUM_PHASE_ANGLES];
	
	//Incremented every time a new differential is computed. Used for calculating the buffet force.
	private long timeUnitsPast = 0;
	
	
	
	//************************************************************************
	public Tuple cursorPrev = new Tuple(0, 0);
	public Tuple cursor = new Tuple(0, 0); //The cursor position relative to Tuple origin. If the cursor is in the middle of the screen, this will be 0,0. 
	
	//The data grabbed from the joystick. Components are between in [0.0, 1.0]. Formerly mouseDiff.
	public Tuple movementInput = new Tuple(0, 0);
	public Tuple differential = new Tuple(0,0);
	
	//temporary tuples for calculations
	private Tuple a1Term_temp = new Tuple(0,0);
	private Tuple a2Term_temp = new Tuple(0,0);
	private Tuple a4BuffetTerm_temp = new Tuple(0,0);
	private Tuple a4InputTerm_temp = new Tuple(0,0);
	
	private ExpSettings settings;
	
	
	public Physics (ExpSettings someSettings) {
		settings = someSettings;
		calculatePhaseAngles();
	
	}
	
	
	/**
	 * Initializes the phaseAngles object to be an array of random doubles between 0 and 2*pi Radians.
	 */
	private void calculatePhaseAngles () {
		for (int i = 0; i < phaseAngles.length; i++) {
			phaseAngles[i] = Math.random() * Math.PI * 2; //between 0 and 2Pi radians.
		}
	}
	
	

	/**
	 * Computes differential movement. Updates Physic's differential and cursor appropiately. 
	 * Will not allow cursor's components' magnitudes to be greater than 1000. 
	 * 
	 */
	public void computeDifferentialAndSetNewPosition () {
		
		//differential = a1(cursor - cursorPrev) + a2(cursorPrev) + a4_buffet(buffet) + joystick sensitivity(input)

		timeUnitsPast++;
		
		//CALCULATE DIFFERENTIAL.
		a1Term_temp.setComponents(0, 0);
		a1Term_temp.add_mod(cursor);
		a1Term_temp.subtract_mod(cursorPrev);
		a1Term_temp.scalarMultiple_mod(A1_MOMENTUM );

		a2Term_temp.setComponents(0, 0);
		a2Term_temp.add_mod(cursorPrev);
		a2Term_temp.scalarMultiple_mod(A2_REPEL);

		a4BuffetTerm_temp.setComponents(0, 0);
		a4BuffetTerm_temp.add_mod(buffetForce());
		a4BuffetTerm_temp.scalarMultiple_mod(A4_BUFFET);

		a4InputTerm_temp.setComponents(0, 0);
		a4InputTerm_temp.add_mod(movementInput);
		a4InputTerm_temp.scalarMultiple_mod(TrackerConstants.JOYSTICK_SENSITIVITY);
		//if the difficulty is higher, give them a little more control.
		if(settings.trackerDifficultyMultiplier > 1.1){
			a4InputTerm_temp.scalarMultiple_mod(
					TrackerConstants.linearScale(1.1, 3.0, settings.trackerDifficultyMultiplier, 1, 2)
			);
		}
		
		differential.setComponents(0, 0);
		differential.add_mod(a1Term_temp);
		differential.add_mod(a2Term_temp);
		differential.add_mod(a4BuffetTerm_temp);
		differential.add_mod(a4InputTerm_temp);
		differential.scalarMultiple_mod(4); //1000 / 250
		
		
		
		//MODIFY THE DIFFERENTIAL IF IT WILL CAUSE THE CURSOR TO GO OFF THE SCREEN. 
		//ENSURES THE cursor IS ALWAYS IN [-1000, 1000] IN BOTH X AND Y.
		//this could be improved. 
		double safetyFactor = 1.01;
		
		//exceeding left boundary
		if(cursor.x + differential.x < -1000.0){
			double correctionDifferential = -1000.0 - (cursor.x + differential.x); //is positive.
			differential.x += (correctionDifferential * safetyFactor); 
			
		}//exceeding right bounary
		else if (cursor.x + differential.x > 1000.0){ //not great bc accessing oneButtonTracker. look. should fix this later
			double correctionDifferential = 1000.0 - (cursor.x + differential.x); //is negative.
			differential.x += (correctionDifferential * safetyFactor);
		}
		
		//Exceeding bottom boundary
		if(cursor.y + differential.y < -1000){
			double correctionDifferential = -1000 - (cursor.y + differential.y); //is positive
			differential.y += (correctionDifferential * safetyFactor);
		}
		//exceeding top boundary
		else if(cursor.y + differential.y > 1000){
			double correctionDifferential = 1000 - (cursor.y + differential.y); //is negative
			differential.y += (correctionDifferential * safetyFactor);
		}
		//done potentially correcting differential.
		
		
		//TAKE EFFECT TO CURSOR. 
		//The graphics update methods take differentials, so they will access the differential for this frame. 
		cursorPrev.x = cursor.x;
		cursorPrev.y = cursor.y;
		
		//Cannot make this more efficient as above, because this reference will be copied on line 11 of TrackerEntry.java
		//We actually DO need to store all these tuples into new objects bc we will store them in TrackerEntry in order to compute rms. 
		//Could still be made more efficient later with some work.
		cursor = new Tuple(cursor.x + differential.x, cursor.y + differential.y);

		
		
	}
	
	/**
	 * Computes and returns a new Tuple representing the Buffet Force.
	 * @return
	 */
	private Tuple buffetForce () {
		
		//NEW
		Tuple force = new Tuple(0, 0);
		for(int i = 0; i < NUM_PHASE_ANGLES; i++){
			force.x += Math.pow(C_X, -1 * i) * Math.cos(Math.pow(C_X, i) * (timeUnitsPast * TIME_STEP) + phaseAngles[i]);
			force.y += Math.pow(C_Y, -1 * i) * Math.sin(Math.pow(C_Y, i) * (timeUnitsPast * TIME_STEP) + phaseAngles[i]);
		}
		
		//Difficulty customization
		force.scalarMultiple_mod(settings.trackerDifficultyMultiplier);
		
		return force;
		
	}

	/**
	 * Sets movementInput
	 * @param x_in
	 * @param y_in
	 */
	public void setMovementInput(double x_in, double y_in){
		movementInput.x = x_in;
		movementInput.y = y_in;
	}
	

	
	
}

	