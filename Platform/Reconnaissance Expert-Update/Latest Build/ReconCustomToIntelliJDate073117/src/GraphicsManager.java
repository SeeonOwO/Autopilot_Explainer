import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GArc;
import acm.graphics.GPolygon;
import acm.graphics.GPoint;

import java.awt.Color;
import java.awt.Font;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JFrame;


/**
 * Manages the graphics for an instance of OneButtonTracker. 
 * Design Concept: You give GraphicsManager your canvas, settings, and data, and it will display it. 
 * Usage in driver: Access your manager, then the sub-manager you want to talk to, then give a command. 
 * The driver can also grab data from this GraphicsManager.
 * @author BenPinzone
 *
 */
public class GraphicsManager {
	
	//OneButtonTracker inherits from GraphicsProgram, which has a GCanvas object. Calls such as add, remove, etc, get re-directed from the GraphicsProgram to the GCanvas.
	OneButtonTracker programCanvas;

	double extremeInOldGraphicsCoords;
	
	public OtherInfo otherInfo = new OtherInfo();
	public Recommender recommender = new Recommender();
	public Images images = new Images();
	public TrackerGraphic trackerGraphic;
	
	//really it shouldn't have the setings. The settings are not a member of it. 
	private ExpSettings settings;

	private JFrame waitFrame;

	public GraphicsManager(OneButtonTracker program_in, boolean useNewGraphics, double extreme_in, ExpSettings settings_in){
		waitFrame = new JFrame();
		waitFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		waitFrame.getContentPane().setBackground(Color.black);
		waitFrame.setVisible(false);

		programCanvas = program_in;
		extremeInOldGraphicsCoords = extreme_in;
		settings = settings_in;
		if(useNewGraphics){
			trackerGraphic = new NewTrackerGraphic();
		}
		else{
			trackerGraphic = new OldTrackerGraphic();
		}
	}
	
	public void changeToTrackerScreen(boolean giveVisualAlert_in){
		/*
		waitFrame.setVisible(true);
		long start = new Date().getTime();
		while(new Date().getTime() - start < 500L){}
		waitFrame.setVisible(false);
		*/

		
		images.setAllVisible(false);
		recommender.setAllVisible(giveVisualAlert_in);
		trackerGraphic.setAllVisible(true);
		
		if(settings.isUsingNewGraphics){
			otherInfo.setTimerColor(Color.BLACK);
		}
		else{
			otherInfo.setTimerColor(Color.WHITE);
		}
	}
	
	public void changeToImageScreen() throws InterruptedException {
		/*
		waitFrame.setVisible(true);
		long start = new Date().getTime();
		while(new Date().getTime() - start < 500L){}
		waitFrame.setVisible(false);
		*/

		recommender.setAllVisible(false);
		trackerGraphic.setAllVisible(false);
		images.setAllVisible(true);
		
		//want black for both graphics. 
		otherInfo.setTimerColor(Color.BLACK);
	}

	public class OtherInfo{
		

		public GLabel timer;
		public GLabel trialNumber;
		public GLabel score;
		public GLabel msg1;
		public GLabel msg2;
		//no close button. Its not a GObject.
		
		//**********************************************************
		//TIMER
		public void initTimer(ElementRectBound trackerBound){
			
			if (otherInfo.timer == null) {
				otherInfo.timer = new GLabel("0");
				
				if(settings.isUsingNewGraphics){
					otherInfo.setTimerColor(Color.BLACK);
				}
				else{
					otherInfo.setTimerColor(Color.WHITE);
				}
				
				otherInfo.timer.setFont(new Font("Arial", Font.BOLD, 14));
				
				
				double xPos = trackerBound.getBottomLeft().getX() + 5; //the 5's are just to give a buffer
				double yPos = trackerBound.getBottomLeft().getY() - 5;
				
				otherInfo.timer.setLocation(xPos, yPos); 
			
				
				
				programCanvas.add(otherInfo.timer);
			}
		}
		
		public void setTimerColor(Color c){
			otherInfo.timer.setColor(c);
		}
		public void sendTimerToFront(){
			otherInfo.timer.sendToFront();
		}
		
		public void updateTimer(double startTime_in, double pauseBank_in){
			
			// timer initialization
			if (otherInfo.timer == null) {
				initTimer(settings.getTrackerRectBound());
			}

			// update label (nice) improve this. look.
			otherInfo.timer.setLabel(
				"Time left: " + 
				Double.toString(
						(int) (
							100 * (
									TrackerConstants.TRIAL_LENGTH_MS - (System.currentTimeMillis() - startTime_in - pauseBank_in)
							) 
							/ 1000d
						) 
						/ 100d
				)
			);
			
		}
		//**********************************************************
		//Labels
		//Requires: bound must be tall enough to hold two lines of text.
		public void initTrialAndScoreLabels(ElementRectBound bound, int counter_in, int expTrials_in, boolean inPracticeMode_in, String practiceText0_in, String message1_in, String message2_in){
			
			//"bound" and "getBounds" are not related.		
			Font myFont = new Font("Arial", Font.PLAIN, 24);
		
			//TRIAL LABEL
			trialNumber = new GLabel("Trial: " + counter_in + "/" + expTrials_in);
			if (inPracticeMode_in) {
				trialNumber.setLabel(practiceText0_in);
			}
			trialNumber.setFont(myFont);
			trialNumber.setLocation(bound.getTopLeft().getX(), bound.getTopLeft().getY() + trialNumber.getBounds().getHeight());
			
			//SCORE LABEL
			score = new GLabel("");
			score.setFont(myFont);
			score.setLocation(trialNumber.getLocation().getX(), trialNumber.getLocation().getY() + trialNumber.getBounds().getHeight());
			programCanvas.add(trialNumber);
			programCanvas.add(score);
			
			//Message stuff
			//MESSAGE 1 LABEL
			if (settings.confidentMode){
				msg1 = new GLabel("Confidence of Detecting Enemy: ");
				msg1.setFont(myFont);
				msg1.setLocation(trialNumber.getLocation().getX(), score.getLocation().getY() + score.getBounds().getHeight());
				programCanvas.add(msg1);
			
			//MESSAGE 2 LABEL
				msg2 = new GLabel(message2_in);
				msg2.setFont(myFont);
				msg2.setLocation(trialNumber.getX()+msg1.getBounds().getWidth(), msg1.getY());
				programCanvas.add(msg2);
			}
			
		}
		
		public void setMessage1(String s){ 
			msg1.setLabel(s);
		}
		public void setMessage2(String s){ 
			msg2.setLabel(s);
		}
		
		public String getMessage1(){ 
			return msg1.getLabel();
		}
		public String getMessage2(){ 
			return msg2.getLabel();
		}
		
		
		//These methods are short, but I made them because in general, I want the driver to avoid accessing the manager's fields directly.
		public void setScoreLabel(String text){
			score.setLabel(text);
		}
		
		public void setTrialNumberLabel(String text){
			trialNumber.setLabel(text);
		}
		
		//look. edit this to include messages.
		public void setAllVisible(boolean isVisible){
			if(timer != null){
				timer.setVisible(isVisible);
			}
			if(trialNumber != null){
				trialNumber.setVisible(isVisible);
			}
			if(score != null){
				score.setVisible(isVisible);
			}
			if(msg1 != null){
				msg1.setVisible(isVisible);
			}
			if(msg2 != null){
				msg2.setVisible(isVisible);
			}
		}
		
	}
	
	public class Recommender{
		public GRect[] automationRecommendation;

		public GRect back;
		public double originalWidth;
		public double originHeight;
		
		//refactor later along with all other mass-visiblity-changing methods for consistency.
		public void setAllVisible(boolean isVisible){
			back.setVisible(isVisible);
			for(int i = 0; i < automationRecommendation.length; i++){
				GRect rect = automationRecommendation[i];
				if(rect != null){
					if(i == 1 && settings.alarmIsBinary && settings.dangerAlertsOnly){ //binary check is probably not neccessary. Just being safe.
						rect.setVisible(false);
					}
					else{
						rect.setVisible(isVisible);
					}
				}
			}
			
		}
		
		
		public void init(ElementRectBound bound){
			
			automationRecommendation = new GRect[settings.alarmIsBinary ? 1 : 4];
			
			double startX = bound.getTopLeft().getX();
			double startY = bound.getTopLeft().getY()+bound.getHeight()/2;
			double width = bound.getWidth();
			double height = bound.getHeight()/2;
			originalWidth =  bound.getWidth();
			originHeight = bound.getHeight()/2;

			Color c;
			for (int i = 0; i < automationRecommendation.length; i++) {
				back = new GRect(startX, startY, width, height);
				back.setFilled(true);
				back.setFillColor(Color.lightGray);
				programCanvas.add(back);

				automationRecommendation[i] = new GRect(startX, startY, width, height);
				automationRecommendation[i].setFilled(true);
				c = settings.isControlRun ? Color.WHITE : settings.alarmIsBinary ? ExpSettings.BINARY_COLORS[i] : ExpSettings.LIKELIHOOD_COLORS[i];
				automationRecommendation[i].setColor(c);
				automationRecommendation[i].setFillColor(modifyAlpha(c, TrackerConstants.INACTIVE_ALARM_ALPHA));
				programCanvas.add(automationRecommendation[i]);

				if (settings.confidentMode){
					GRect bar = new GRect(startX+width/2, startY, width/200, height);
					bar.setFilled(true);
					bar.setFillColor(Color.black);
					programCanvas.add(bar);

					/*
					for(int j=1;j<10;j++){
						double gap = width/10;
						GRect b = new GRect(startX+gap*j, startY, width/200, height);
						b.setFilled(true);
						b.setFillColor(Color.black);
						programCanvas.add(b);
					}
					*/

				}
				/*
				if (i < 1){
					startX += width + 1;
				}
				if (i == 1){
					startX -= width + 1;
					startY += height + 1;
				}
				if (i == 2){
					startX += width + 1;
				}
				*/
			}
			
			//assuming there is at least one tracking only practice trial. LOOK. 
			setAllVisible(false);
		}
		
		
		
		public void changeColor(Color c, boolean isBinaryAlarm_in) {
			
			for (int i = 0; i < automationRecommendation.length; i++) {
				if ((isBinaryAlarm_in && ExpSettings.BINARY_COLORS[i].equals(c))
					 || !isBinaryAlarm_in && ExpSettings.LIKELIHOOD_COLORS[i].equals(c)) {
					
					automationRecommendation[i].setFillColor(
							modifyAlpha(c, TrackerConstants.ACTIVE_ALARM_ALPHA)
					);
				} else {

					automationRecommendation[i].setFillColor(Color.lightGray);


				}
			}

		}

		public void changeColorNorm(Color c, boolean isBinaryAlarm_in){
			if (isBinaryAlarm_in) {
				automationRecommendation[0].setFillColor(modifyAlpha(c, TrackerConstants.ACTIVE_ALARM_ALPHA));
			}
		}

		public void changeColorBasedOnConfidence(double conf, boolean isBinaryAlarm_in){
			if (isBinaryAlarm_in && !settings.confidentMode){
				if (conf > 0.0) {
					automationRecommendation[0].setFillColor(modifyAlpha(Color.red, TrackerConstants.ACTIVE_ALARM_ALPHA));
				}else{
					automationRecommendation[0].setFillColor(modifyAlpha(Color.green, TrackerConstants.ACTIVE_ALARM_ALPHA));
				}
			}
			else {
				if (isBinaryAlarm_in /*&& conf > 0.0*/) {
					double tempWidth = originalWidth * conf;
					automationRecommendation[0].setSize(tempWidth, originHeight);
					automationRecommendation[0].setFillColor(modifyAlpha(Color.red, TrackerConstants.ACTIVE_ALARM_ALPHA));
				} else {
					automationRecommendation[0].setSize(originalWidth, originHeight);
					automationRecommendation[0].setFillColor(modifyAlpha(Color.green, TrackerConstants.ACTIVE_ALARM_ALPHA));
				}
			}
		}
		
		public Color modifyAlpha(Color c, int alpha) {
			return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		}

	}
	
	//This was never modified to use ElementRectBound. Not a priority for now.
	public class Images {
		public ArrayList<GImage> imagesOnDisplay;
		
		public void setAllVisible(boolean isVisible){
			for(GImage img : imagesOnDisplay){
				if(img != null){
					img.setVisible(isVisible);
				}
			}
		}
		
		public void init(){
			imagesOnDisplay = new ArrayList<GImage>();
		}
		
		//called by putUpNextImageSetAndWarnings
		public void addImagesToCanvas(ElementRectBound bound) {
			
			
			//actual image files are 1021 x 690 which is 1.4797101449
			
			if(imagesOnDisplay.size() != 4){
				System.out.println("Error in GraphicsManager-Images-addImagesToCanvas");
				JOptionPane.showMessageDialog(new JFrame(), "Error loading images. There are not 4 images in the image set.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			double buffer = 10;
			double centerX = bound.getCenter().getX();
			double centerY = bound.getCenter().getY();
			
			double xPos = 0;
			double yPos = 0;
			double width = (bound.getWidth() - buffer) / 2;
			double height = width / 1.4797101449 ;
			
			for(int i = 0; i < 4; i++){
				
				switch(i){
				
					//top left image
					case 0 : 
						xPos = centerX - width  - (buffer / 2);
						yPos = centerY - height - (buffer / 2);
						break;
					
					//top right image
					case 1 : 
						xPos = centerX + (buffer / 2);
						yPos = centerY - height - (buffer / 2);
						break;
					
					//bottom left image
					case 2 : 
						xPos = centerX - width  - (buffer / 2);
						yPos = centerY + (buffer / 2);
						break;
					
					//bottom right image
					case 3 : 
						xPos = centerX + (buffer / 2);
						yPos = centerY + (buffer / 2);
						break;
				}
				imagesOnDisplay.get(i).setBounds(xPos, yPos, width, height);
				imagesOnDisplay.get(i).setVisible(false);
				programCanvas.add(imagesOnDisplay.get(i));
				
			}
			
		}
		
		public void removeImagesFromCanvas(){
			for (GImage im : imagesOnDisplay) {
				programCanvas.remove(im);
				im.getImage().flush();
			}
		}
		
		public void addImageFromTrialToDisplayList(String s){
			imagesOnDisplay.add(new GImage(s));
		}
		
		
	}
	
	public abstract class TrackerGraphic{
		
		public boolean trackerGraphicIsUp;
		
		public abstract void init(ElementRectBound bound);
		public abstract void setAllVisible(boolean isVisible);
	
		//TAKES A DIFFERENTIAL.
		public abstract void update(double x, double y);
		public abstract void reset();
		
	}
	
	public class OldTrackerGraphic extends TrackerGraphic {
		public GRect blueRect;
		public GRect brownRect;
		
		ArrayList<GObject> cursorSwarm;
		ArrayList<GLine> crossSwarm;
		
		ElementRectBound bound;
		
	    //REQUIRES: the ElementRectBound is large enough so that the cursor and cross swarm will be able to fit.
		@Override
		public void init(ElementRectBound bound_in){
			
			bound = bound_in;
		
			blueRect = new GRect(
				bound.getTopLeft().getX(), 
				bound.getTopLeft().getY(), 
				bound.getWidth(), 
				(bound.getHeight() / 2)
			);
			
			blueRect.setColor(new Color(95, 166, 195));
			blueRect.setFilled(true);
			programCanvas.add(blueRect);
			
	
			brownRect = new GRect(
				bound.getTopLeft().getX(), 
				bound.getCenter().getY(), 
				bound.getWidth(),
				(bound.getHeight() / 2)
			);
			brownRect.setColor(new Color(170, 140, 100));
			brownRect.setFilled(true);
			programCanvas.add(brownRect);
			
			initCursorSwarm(bound.getCenter());
			initCrossSwarm(bound.getCenter());
			
			trackerGraphicIsUp = true;
			
		}
		
		@Override
		public void setAllVisible(boolean isVisible){
			
			trackerGraphicIsUp = isVisible;
			
			if(blueRect != null){
				blueRect.setVisible(isVisible);
			}
			if(brownRect != null){
				brownRect.setVisible(isVisible);
			}
			
			for(GObject obj : cursorSwarm){
				if(obj != null){
					obj.setVisible(isVisible);
				}
			}
			
			for(GLine line : crossSwarm){
				if(line != null){
					line.setVisible(isVisible);
				}
			}
		}
		
		
		
		
		//TAKES A DIFFERENTIAL.
		//This differential is on the scale [-1000,1000]
		
		@Override
		public void update(double x, double y){
			//scale the differential.
			//Assuming the graphics scale is square, as seen in TrackerConstants line ~ 85
			
			x = x * extremeInOldGraphicsCoords / 1000.0;
			y = y * extremeInOldGraphicsCoords / 1000.0;
			
			moveCursorSwarm(x, -y);
		}
		
		@Override 
		public void reset(){
			resetCursorSwarm();
		}
		
		//****************************************************
		//CURSOR SWARM METHODS
		private void initCursorSwarm(GPoint origin) {
			cursorSwarm = new ArrayList<GObject>();
			double unit = (TrackerConstants.CURSOR_SIZE_NORMALIZED) * (extremeInOldGraphicsCoords / 1000);
			double sep = unit * 3d / 4;
			
			Tuple o = new Tuple(origin.getX(), origin.getY());
			
			double outerCircleX = o.x - unit;
			double outerCircleY = o.y - unit;
			double outerCircleWidthAndHeight = unit * 2;
			cursorSwarm.add(new GOval(outerCircleX,     outerCircleY,     outerCircleWidthAndHeight,     outerCircleWidthAndHeight    )); //looks like unit is radius of oval, among other things. So TOP LEFT anchor.
			cursorSwarm.add(new GOval(outerCircleX + 1, outerCircleY + 1, outerCircleWidthAndHeight - 2, outerCircleWidthAndHeight - 2)); //inner circle. 
			cursorSwarm.add(new GOval(outerCircleX + (0.25 * outerCircleWidthAndHeight),     outerCircleY + (0.25 * outerCircleWidthAndHeight),     outerCircleWidthAndHeight * 0.5,     outerCircleWidthAndHeight * 0.5));
			cursorSwarm.add(new GOval(outerCircleX + (0.25 * outerCircleWidthAndHeight) + 1, outerCircleY + (0.25 * outerCircleWidthAndHeight) + 1, outerCircleWidthAndHeight * 0.5 - 2, outerCircleWidthAndHeight * 0.5 - 2));
			cursorSwarm.add(new GLine(o.x - sep,   o.y,   o.x - sep - unit / 2,   o.y)); //constructed from end points.x0 y0, x1 y1
			cursorSwarm.add(new GLine(o.x + sep,   o.y,   o.x + sep + unit / 2,   o.y));
			cursorSwarm.add(new GLine(o.x, o.y - sep, o.x, o.y - sep - unit / 2));
			cursorSwarm.add(new GLine(o.x, o.y + sep, o.x, o.y + sep + unit / 2));
			for (GObject s : cursorSwarm) {
				s.setColor(Color.GREEN);
				programCanvas.add(s);
				
			}
		}
		
		private void resetCursorSwarm() {
			
			//recall: physics.cursor gets reset because every round, a new physics object is created......look. important...
			
			for (GObject g : cursorSwarm) {
				programCanvas.remove(g);
			}

			cursorSwarm = new ArrayList<GObject>();
			initCursorSwarm(bound.getCenter());
			
			otherInfo.trialNumber.sendToFront();
			otherInfo.score.sendToFront();
		}
		
		//takes a differential
		private void moveCursorSwarm(double x, double y) {
			
			for (GObject g : cursorSwarm) {
				g.move(x, y);
			}
		}
		//****************************************************
		//CROSS SWARM METHODS
		
		private void initCrossSwarm(GPoint origin) {
			Tuple o = new Tuple(origin.getX(), origin.getY());
			double unit = (TrackerConstants.CROSS_SIZE_NORMALIZED / 2) * (extremeInOldGraphicsCoords / 1000); //second factor is for scaling.
			double sep  = (TrackerConstants.CROSS_SIZE_NORMALIZED * 3) * (extremeInOldGraphicsCoords / 1000);
			crossSwarm = new ArrayList<GLine>();
			addCross_crossSwarm(o, unit);
			for (int i = 1; i <= 3; i++) {
				addCross_crossSwarm(new Tuple(o.x - sep * i, o.y), unit / 2);
				addCross_crossSwarm(new Tuple(o.x + sep * i, o.y), unit / 2);
				addCross_crossSwarm(new Tuple(o.x,           o.y - sep * i), unit / 2);
				addCross_crossSwarm(new Tuple(o.x,           o.y + sep * i), unit / 2);
			}
			addInwardDashes_crossSwarm(new Tuple(o.x - sep, o.y), sep, unit / 2, false);
			addInwardDashes_crossSwarm(new Tuple(o.x + sep, o.y), sep, unit / 2, false);
			addInwardDashes_crossSwarm(new Tuple(o.x,       o.y - sep), sep, unit / 2, true);
			addInwardDashes_crossSwarm(new Tuple(o.x,       o.y + sep), sep, unit / 2, true);
			
			for (GLine g : crossSwarm) {
				g.setColor(Color.YELLOW);
				programCanvas.add(g);
			}
			
			otherInfo.trialNumber.sendToFront();
			otherInfo.score.sendToFront();
		}
		
		//adds ONE of the tinary yellow crosses
		private void addCross_crossSwarm(Tuple loc, double arm) {
			crossSwarm.add(new GLine(loc.x - arm, loc.y, loc.x + arm, loc.y));
			crossSwarm.add(new GLine(loc.x, loc.y - arm, loc.x, loc.y + arm));
			
		}
		
		//adds ONE of the L shapes.
		private void addInwardDashes_crossSwarm(Tuple center, double sep, double unit, boolean horiz) {
			if (horiz) {
				crossSwarm.add(new GLine(center.x - sep, center.y, center.x - sep + unit, center.y));
				crossSwarm.add(new GLine(center.x + sep, center.y, center.x + sep - unit, center.y));
			} else {
				crossSwarm.add(new GLine(center.x, center.y - sep, center.x, center.y - sep + unit));
				crossSwarm.add(new GLine(center.x, center.y + sep, center.x, center.y + sep - unit));
			}
			
		}
		
		
	}
	
	//remember, this depends on receiving x, y coordinates. 
	public class NewTrackerGraphic extends TrackerGraphic {
		
		public AttitudeManager am;
		
		public GOval circle;
		public GArc arc;
		public GPolygon triangle;
		public ArrayList<GObject> cursorSwarm;
		//other cool stuff.
		//red recoording dot, perhaps lights to indicate that which aspects are level enough to earn points.
		
		
		@Override
		public void init(ElementRectBound bound){
			am = new AttitudeManager(bound.getTopLeft(), bound.getWidth(), bound.getHeight(), new Color(170, 140, 100), new Color(95, 166, 195));
			circle = am.getGroundCircle();
			arc = am.getSkyArc();
			triangle = am.getCompensationTriangle();
			cursorSwarm = am.getCursorObjects();
			
			programCanvas.add(circle);
			programCanvas.add(arc);
			programCanvas.add(triangle);
			
			for(GObject line : cursorSwarm){
				programCanvas.add(line);
			}
			
			for(GObject line : cursorSwarm){
				line.setVisible(true);
			}
			
		}
		
		
		@Override
		public void setAllVisible(boolean isVisible){
			trackerGraphicIsUp = isVisible;
			circle.setVisible(isVisible);
			arc.setVisible(isVisible);
			triangle.setVisible(isVisible);
			for(GObject object : cursorSwarm){
				object.setVisible(isVisible);
			}
		}
		
		
		
		
		//TAKES A DIFFERENTIAL.
		//You'll have to fix this method and/or the AttitudeManager to work with this.
		@Override
		public void update(double x, double y){
			
			//calculations
			am.recalculateAll(x, y);
			
			//retrieve new data
			GArc newArc = am.getSkyArc();
			GPolygon newTriangle = am.getCompensationTriangle();
			
			//prep/stage
			newArc.setVisible(false);
			newTriangle.setVisible(false);
			programCanvas.add(newArc);
			programCanvas.add(newTriangle);
			
			for(GObject line : cursorSwarm){
				line.sendToFront();
			}
			
			//do change
			newArc.setVisible(trackerGraphicIsUp);
			arc.setVisible(false);
			newTriangle.setVisible(trackerGraphicIsUp);
			triangle.setVisible(false);
			
			//clean up
			programCanvas.remove(arc);
			programCanvas.remove(triangle);
			
			//reassign
			arc = newArc;
			triangle = newTriangle;
			
			
		}
		
		@Override
		public void reset(){
			am.reset();
			update(0.0, 0.0);
		}
	}

}
