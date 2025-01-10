import java.util.ArrayList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Collects data in order to diagnose problems with the game loop mechanics and its timing. 
 * <p>
 * See comments at declaration for further detail on interested data.
 * @author BenPinzone
 *
 */
/*Specifically, I was interesting in collecting:
 * The average time elapsed between the start of each loop. (Often referred to as the average delta time.)
 * The average loop completion time. 
 * The number of loops that occur. 
 * The number of loops that occur in which the ( % n) condition is true.
 * 
 */
public class LoopDiagnostic {
	
	public class TrialDiagnostic{
		
		public int trialNum;
		
		public double trialStartTime;
		public double trialStopTime;
		
		public double sumOfDeltaTimes = 0; //implement this, take the intelliJ, do more testing. I want to know the average delta time.
		public double sumOfLoopCompletionTimes = 0;
		
		public int numLoops = 0;
		public int numMod33 = 0;
		public int numMod50 = 0;
		public int numMod10 = 0;
		
		public TrialDiagnostic(int trial_in){
			trialNum = trial_in;
		}
		
	}
	
	public ArrayList<TrialDiagnostic> trialDiagnostics = new ArrayList<TrialDiagnostic>();
	
	//add working variables here.
	public double loopStartTime_w;
	public double lastLoopStartTime_w;
	
	public boolean running_w;
	public boolean lastRunning_w;
	
	public void outputToFile(){
		PrintWriter out;
		try{
			out = new PrintWriter(new FileWriter("LOOP_DIAGNOSTIC_OUTPUT.csv"));
			
			out.print("Trial Num,Trial Start Time,Trial Stop Time,Total Time,loops,33loops,50loops,10loops");
			out.println();
			
			for(TrialDiagnostic td : trialDiagnostics){
				
				out.print(td.trialNum + "," + td.trialStartTime + "," + td.trialStopTime + "," + (td.trialStopTime - td.trialStartTime) + "," 
						+ td.numLoops + "," + td.numMod33 + "," + td.numMod50 + "," + td.numMod10);
				out.println();	
			}
			out.close();
		}
		catch(IOException e){
			
		}
	}
	
	public void addTrial(int trialNum_in){
		trialDiagnostics.add(new TrialDiagnostic(trialNum_in));
	}
	
	public LoopDiagnostic.TrialDiagnostic getBack(){
		return trialDiagnostics.get(trialDiagnostics.size() - 1);
	}

}
