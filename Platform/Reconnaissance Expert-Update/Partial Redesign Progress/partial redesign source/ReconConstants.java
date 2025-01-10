import java.awt.Color;

public class ReconConstants {
	
	public static final double[] RMS_SCORE_THRESHOLDS = new double[] {29.5, 39.8, 50.2, 60.5, 70.9, 81.3, 91.6, 101.9, 112.3, 122.7};
	
	
	//LOOK. THIS DEPENDS ON THE AUDIO FILES.
	enum Alert{
		DANGER, CAUTION, POSSIBLY_CLEAR, CLEAR;
		
		//NAMES
		private static final String DANGER_NAME   = new String("DANGER");
		private static final String CAUTION_NAME  = new String("CAUTION");
		private static final String POSSIBLE_NAME = new String("POSSBILY_CLEAR");
		private static final String CLEAR_NAME    = new String("CLEAR");
		
		//COLORS
		private static final Color RED = Color.RED;
		private static final Color YELLOW = new Color(255, 201, 14);
		private static final Color LIGHT_GREEN = new Color(181, 230, 29);
		private static final Color DARK_GREEN  = Color.GREEN;
		
		//AUDIO CLIPS
		//LOOK. INTERACTING WITH FILE SYSTEM.
		private static final String DANGER_CLIP   = new String("sounds/danger.wav");
		private static final String CAUTION_CLIP  = new String("sounds/caution.wav");
		private static final String POSSIBLE_CLIP = new String("sounds/possible.wav");
		private static final String CLEAR_CLIP    = new String("sounds/clear.wav");
		
		public String getName(){
			if(this == DANGER)  return DANGER_NAME;
			if(this == CAUTION) return CAUTION_NAME;
			if(this == POSSIBLY_CLEAR) return POSSIBLE_NAME;
			return CLEAR_NAME;
		}
		
		public Color getColor(){
			if(this == DANGER)  return RED;
			if(this == CAUTION) return YELLOW;
			if(this == POSSIBLY_CLEAR) return LIGHT_GREEN;
			return DARK_GREEN;
		}
		
		public String getAudioClip(){
			if(this == DANGER)  return DANGER_CLIP;
			if(this == CAUTION) return CAUTION_CLIP;
			if(this == POSSIBLY_CLEAR) return POSSIBLE_CLIP;
			return CLEAR_CLIP;	
		}
		
		
		
	}//end Alert enum

}

