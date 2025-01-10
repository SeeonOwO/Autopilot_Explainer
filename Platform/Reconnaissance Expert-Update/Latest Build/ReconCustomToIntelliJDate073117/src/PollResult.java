import java.util.ArrayList;

/**
 * Stores result of a poll for a single trial.
 * The Arraylist size is 1 if it is a control run, 3 otherwise. 
 * In order, the Integers represent: Confidence In Performing Task, Perceived Reliability, Trust. 
 * 
 * @author Kevin Li
 */
public class PollResult {
	public ArrayList<Integer> results;
	public int trialNumber;
	
	public PollResult (int counter) {
		results = new ArrayList<Integer>();
		trialNumber = counter;
	}
}
