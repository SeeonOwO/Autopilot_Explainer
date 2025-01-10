

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

/*
 * TODO: output scores
 */
/**
 * Outputs data (to a file) from the practice trials.
 * @author Kevin Li
 *
 */
public class PracticeDataOutput {
	public DataAggregator entries;
	
	public PracticeDataOutput (DataAggregator d) {
		entries = d;
	}
	
	public void output () {
		String fileName = entries.fileNameBase + "practice.csv";
		try {
			File temp = new File(fileName);
			temp.getParentFile().mkdirs();
			PrintWriter fout = new PrintWriter(new FileWriter(temp));
			fout.println("SUMMARY");
			fout.println("Trial Number,Time Stamp,Relative Start Time,RMS"); //previously, there was "mean distance" before. RMs. But it was actually the % of the timer the cursor captured the middle of the screen. now deleted.
			double[] rms = computeRms();
			Entry en;
			for (int i = 0; i < entries.entryList.size(); i++) {
				en = entries.entryList.get(i);
				fout.println(  en.trialNumber + "," 
							 + new Timestamp((long)en.trialAbsoluteStartTime) + "," 
							 + (en.trialAbsoluteStartTime - entries.aggregatorStartTime) + "," 
							 + rms[i]
				);
			}
			
			fout.println("DETAIL");
			fout.println("Trial Number,Time Stamp,Relative Start Time,Cursor X (Relative to Origin),Cursor Y(Relative to Origin),Joystick X,Joystick Y");
			for (TrackerEntry_old t : entries.trackerData) {
				
				fout.println(  
					 t.trialNumber + "," 
				     + new Timestamp((long)t.absoluteTime) + "," 
				     + (t.absoluteTime - entries.aggregatorStartTime) + "," 
				     + t.position.toStringComma() + "," 
				     + t.joystickInput.toStringComma()
				);
				
				if(DataAggregator.trackerTestOutputEnabled){
					System.out.println(  
						 t.trialNumber + "," 
					     + new Timestamp((long)t.absoluteTime) + "," 
					     + (t.absoluteTime - entries.aggregatorStartTime) + "," 
					     + t.position.toStringComma() + "," 
					     + t.joystickInput.toStringComma()
					);
				}
				
			}
			fout.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double[] computeRms () {
		double[] quadraticSums = new double[entries.trackerData.get(entries.trackerData.size() - 1).trialNumber];
		int[] counts = new int[quadraticSums.length];
		for (TrackerEntry_old t : entries.trackerData) {
			quadraticSums[t.trialNumber - 1] += Math.pow(t.position.x, 2) + Math.pow(t.position.y, 2);
			counts[t.trialNumber - 1]++;
		}
		for (int i = 0; i < quadraticSums.length; i++) {
			quadraticSums[i] = Math.sqrt(quadraticSums[i] / counts[i]);
		}
		return quadraticSums;
	}
}
