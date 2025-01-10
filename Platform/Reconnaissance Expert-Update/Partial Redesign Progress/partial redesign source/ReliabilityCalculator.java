import java.util.ArrayList;
 
/**
 * Calculates a single reliability of a certain type. Must tell it what type to calculate, and the quotaSet containing the data.
 * @author BenPinzone
 *
 */
public class ReliabilityCalculator {
	
	
	/**
	 * Used to identify which reliability type should be calculated. 
	 * These descriptions come from signal theory. 
	 * Each of these has their own calculation method.
	 * See individual methods to learn about what each represents and how it is calculated. 
	 * @author BenPinzone
	 *
	 */
	enum ReliabilityType{
		OVERALL, HIT_RATE, CORRECT_REJECTION_RATE, POSITIVE_PREDICTIVE_VALUE, NEGATIVE_PREDICTIVE_VALUE;
	}
	
	/* NOTE ON ENUMS
	Recall, enums are nothing more than syntactical shortcut. The above is a shortcut for:
	public static class ReliabilityType {
	    //"static" - the entity belongs to the class itself, not an instance of the class.
	    public static final ReliabilityType OVERALL = new ReliabilityType();
	    public static final ReliabilityType HIT_RATE = new ReliabilityType();
	    public static final ReliabilityType CORRECT_REJECTION_RATE = new ReliabilityType();
	    public static final ReliabilityType POSITIVE_PREDICTIVE_VALUE = new ReliabilityType();
	    public static final ReliabilityType NEGATIVE_PREDICTIVE_VALUE = new ReliabilityType();
	  
	    private ReliabilityType(){}
	  
	}
	 */
	
 
	/**
	 * Used to identify which reliability profile will be displayed to the user. 
	 * In other words, what kind of information we tell them about the accuracy of the recommender.
	 * <p>
	 * If the profile is Overall, we display the overall reliability. 
	 * If the profile is None,    we display no information. 
	 * If the profile is Rate,    we display the HitRate and CorrectRejectionRate.
	 * If the profile is Predictive value, we display the PositivePredictiveValue and the NegativePredictiveValue
	 * @author BenPinzone
	 *
	 */
	enum ReliabilityProfile{
		OVERALL, NONE, RATE, VALUE;
		
		@Override
		public String toString(){
			
			if(this == OVERALL){
				return "Overall";
			}
			if(this == NONE){
				return "None";
			}
			if(this == RATE){
				return "Rate";
			}
			return "Predictive Value";
			
		}
	}
	
	/**
	 * Returns a reliability of the type specified by reliabilityType.
	 * @param reliabilityType
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return a reliability of the type specified by reliabilityType.
	 */
	public static double calculate_reliability(ReliabilityType reliabilityType, ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		switch (reliabilityType){
		
			case OVERALL : 
				return calculate_overall(quotaList, alarmIsBinary); 
			
			case HIT_RATE : 
				return calculate_hit_rate(quotaList, alarmIsBinary);
			
			case CORRECT_REJECTION_RATE : 
				return calculate_correction_rejection_rate(quotaList, alarmIsBinary);
			
			case POSITIVE_PREDICTIVE_VALUE : 
				return calculate_positive_predictive_value(quotaList, alarmIsBinary);
			
			case NEGATIVE_PREDICTIVE_VALUE : 
			
				return calculate_negative_predictive_value(quotaList, alarmIsBinary);
			
			default : 
				//should never happen
				return 0.0;
		}	
	}
	
	/**
	 * Returns the detectors overall reliablity. 
	 * Calculated: (Hits + Correct Rejections) / (Hits + False Alarms + Misses + Correct Rejections)
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return the detectors overall reliablity. 
	 */
	private static double calculate_overall(ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		double correct = 0;
		double total = 0;
		
		if(alarmIsBinary){
			correct += quotaList.get(ExpSettings.hitIndex);
			correct += quotaList.get(ExpSettings.correctRejectionIndex);
			
			total += quotaList.get(ExpSettings.hitIndex);
			total += quotaList.get(ExpSettings.correctRejectionIndex);
			total += quotaList.get(ExpSettings.falseAlarmIndex);
			total += quotaList.get(ExpSettings.missIndex);
			
		}
		else{
			correct += quotaList.get(ExpSettings.threatGivenDangerIndex);
			correct += quotaList.get(ExpSettings.threatGivenWarningIndex);
			correct += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			correct += quotaList.get(ExpSettings.clearGivenClrIndex);
			
			total += quotaList.get(ExpSettings.threatGivenDangerIndex);
			total += quotaList.get(ExpSettings.threatGivenWarningIndex);
			total += quotaList.get(ExpSettings.threatGivenPosClrIndex);
			total += quotaList.get(ExpSettings.threatGivenClrIndex);
			total += quotaList.get(ExpSettings.clearGivenDangerIndex);
			total += quotaList.get(ExpSettings.clearGivenWarningIndex);
			total += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			total += quotaList.get(ExpSettings.clearGivenClrIndex);
		}
		
		
		return correct / total;
	}
	
	/**
	 * Returns the detectors reliability GIVEN that the true state is threat. "Given" used as in conditional probability.
	 * Calculated: Hits / (Hits + Misses)
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return the detectors reliability GIVEN that the true state is threat.
	 */
	private static double calculate_hit_rate(ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		double correct = 0;
		double total = 0;
		
		if(alarmIsBinary){
			correct += quotaList.get(ExpSettings.hitIndex);

			total += quotaList.get(ExpSettings.hitIndex);
			total += quotaList.get(ExpSettings.missIndex);
			
		}
		else{
			correct += quotaList.get(ExpSettings.threatGivenDangerIndex);
			correct += quotaList.get(ExpSettings.threatGivenWarningIndex);

			total += quotaList.get(ExpSettings.threatGivenDangerIndex);
			total += quotaList.get(ExpSettings.threatGivenWarningIndex);
			total += quotaList.get(ExpSettings.threatGivenPosClrIndex);
			total += quotaList.get(ExpSettings.threatGivenClrIndex);
		}
		
		
		return correct / total;
	}
	
	/**
	 * Returns the detectors reliability GIVEN that the true state is no threat. "Given" used as in conditional probability.
	 * Calculated: Correct Rejections / (Correct Rejections + False Alarms)
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return the detectors reliability GIVEN that the true state is no threat.
	 */
	private static double calculate_correction_rejection_rate(ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		double correct = 0;
		double total = 0;
		
		if(alarmIsBinary){

			correct += quotaList.get(ExpSettings.correctRejectionIndex);
		
			total += quotaList.get(ExpSettings.correctRejectionIndex);
			total += quotaList.get(ExpSettings.falseAlarmIndex);	
		}
		else{
			
			correct += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			correct += quotaList.get(ExpSettings.clearGivenClrIndex);
			
			total += quotaList.get(ExpSettings.clearGivenDangerIndex);
			total += quotaList.get(ExpSettings.clearGivenWarningIndex);
			total += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			total += quotaList.get(ExpSettings.clearGivenClrIndex);
		}
		
		
		return correct / total;
	}
	
	
	/**
	 * Returns the detectors reliability GIVEN that the detector has reported danger. "Given" used as in conditional probability.
	 * Calculated: Hits / (Hits + False Alarms)
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return the detectors reliability GIVEN that the detector has reported danger.
	 */
	private static double calculate_positive_predictive_value(ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		double correct = 0;
		double total = 0;
		
		if(alarmIsBinary){
			correct += quotaList.get(ExpSettings.hitIndex);
			
			total += quotaList.get(ExpSettings.hitIndex);
			total += quotaList.get(ExpSettings.falseAlarmIndex);
		}
		else{
			correct += quotaList.get(ExpSettings.threatGivenDangerIndex);
			correct += quotaList.get(ExpSettings.threatGivenWarningIndex);

			total += quotaList.get(ExpSettings.threatGivenDangerIndex);
			total += quotaList.get(ExpSettings.threatGivenWarningIndex);
			total += quotaList.get(ExpSettings.clearGivenDangerIndex);
			total += quotaList.get(ExpSettings.clearGivenWarningIndex);
		
		}
		
		
		return correct / total;
	}
	
	
	/**
	 * Returns the detectors reliability GIVEN that the detector has reported clear. "Given" used as in conditional probability.
	 * Calculated: Correct Rejections / (Correction Rejections + Misses)
	 * @param quotaList
	 * @param alarmIsBinary
	 * @return the detectors reliability GIVEN that the detector has reported clear.
	 */
	private static double calculate_negative_predictive_value(ArrayList<Integer> quotaList, boolean alarmIsBinary){
		
		double correct = 0;
		double total = 0;
		
		if(alarmIsBinary){
			correct += quotaList.get(ExpSettings.correctRejectionIndex);
			
			total += quotaList.get(ExpSettings.correctRejectionIndex);
			total += quotaList.get(ExpSettings.missIndex);
			
		}
		else{
			correct += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			correct += quotaList.get(ExpSettings.clearGivenClrIndex);
			
			total += quotaList.get(ExpSettings.threatGivenPosClrIndex);
			total += quotaList.get(ExpSettings.threatGivenClrIndex);
			total += quotaList.get(ExpSettings.clearGivenPosClrIndex);
			total += quotaList.get(ExpSettings.clearGivenClrIndex);
		}
		
		
		return correct / total;
	}
	
	
	

}
