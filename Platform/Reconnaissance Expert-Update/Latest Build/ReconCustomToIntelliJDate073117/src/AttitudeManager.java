import acm.graphics.GPoint;
import acm.graphics.GOval;
import acm.graphics.GLine;
import acm.graphics.GObject;

import java.awt.Color;

import acm.graphics.GArc;
import acm.graphics.GPolygon;

import java.util.ArrayList;

/**
 * A class to manage a GUI that represents the "attitude indicator" instrument on an aerial vehicle. 
 * See https://techoctave.com/images/artificial-horizon-attitude-indicator.png
 * <p>
 * See visual documentation in the DOCUMENTATION folder in the project root directory. 
 * @author BenPinzone
 *
 */
public class AttitudeManager {
	
	//All angles are relative to the positive x-axis as in the unit circle. 
	//Except horizPtsSepAng, which is the angle between horizon points relative to each other. 
	
	private Tuple currentPosition = new Tuple(0,0);
	public void setCurrentPosition(double x, double y){
		currentPosition.x = x;
		currentPosition.y = y;
	}
	
	//************************************************************************
	//FUNDAMENTAL

	//Represents whether plane is gaining or losign altitude. 
	//scaled according to Y value.
	//The angular separation between the horizon points. 
	//180 Degrees -> Front to back of plane is perfectly level. Steady altitude. 
	//0 Degrees   -> Front of plane is lower  than back of plane. You see entirely ground. 
	//360 Degrees -> Front of plane is higher than back of plane. You see entirely sky.
	private double horizPtsSepAng = HORIZ_PTS_SEP_ANG_LEVEL;
	
	//Represents whether the plane is turning or not.
	//scaled according to X value.
	//90 Degrees  -> Aircraft is not turning.   Wings are perfectly horizontal. 
	//0 Degrees   -> Aircraft is turning right. Wings are perfectly vertical. Left wing is high. Right wing is low. 
	//180 Degrees -> Aircraft is turning left.  Wings are perfectly vertical. Left wing is low.  Right wing is high.
	private double tiltAng = TILT_ANG_LEVEL;
	
	//************************************************************************
	//CALCULATED
	
	//The angle at which the right horizon point lies. (After final calculation).
	//Of course, this parameter does not uniquely identify the state of the plane. 
	//However, if tiltAng is 90 degrees, then the point (described by this angle) and quadrantPtForTriangle are the same. 
	private double rightHorizPtAng;
		
	//The point at which the top right vertex of the triangle would lie, before rotation due to tiltAng. 
	//Components should always be positive. 
	//Point is relative to center of circle.
	private GPoint quadrantPtForTriangle = new GPoint(); 
	
	//***********************************************************************
	//ANGLE LIMIT DATA
	
	//horizPtsSepAng when the plane is perfectly level.
	private static final int HORIZ_PTS_SEP_ANG_LEVEL = 180;
	
	//the maximum deviation that horizPtsSepAng can have from 180 in either direction.
	//Arbitrarily decided based on the smallest portion of ground/sky that should always remain visible.
	//Ex: If this is 140, then horizPtsSepAng will always be in [40, 320]
	private static final int HORIZ_PTS_SEP_ANG_MAX_DEVIATION = 140;
	
	//tiltAng when the plane is perfectly level.
	private static final int TILT_ANG_LEVEL = 90;
	
	//the maximum deviation that tiltAng can have from 90 in either direction. 
	private static final int TILT_ANG_MAX_DEVIATION = 90;
	
	//************************************************************************
	//FOR SCALING
	//limits for currX and currY. Used not for boudning, but for scaling.
	private static final double MAX_X_MAGNITUDE = 1000; 
	private static final double MAX_Y_MAGNITUDE = 1000; 
		
		
	//************************************************************************
	//GRAPHICAL OBJECTS
	
	//world view
	private GOval groundCircle;
	private GArc skyArc;
	private GPolygon compensationTriangle;
	
	//cross hair
	private ArrayList<GObject> cursorObjects = new ArrayList<GObject>();

	//************************************************************************
	//GRAPHICAL OBJECT POSITIONING DATA
	// ca = circle and arc
	
	//given data
	private GPoint caAnchor; 
	private double caWidth;
	private double caHeight;
	//calculated data
	private GPoint caCenter;
	private double caRadius;
	//colors
	private Color groundColor;
	private Color skyColor;
	private Color cursorColor = Color.YELLOW;
	
	//0.5 -> horizontal length of cursor will be half the width of the circle.
	//0.1 -> horizontal length of cursor will be 10%  the width of the circle.
	private static final double cursorWidthInPropOfTotalWidth = 0.5; 
	
	
	public AttitudeManager(GPoint anchor_in, double width_in, double height_in, Color groundColor_in, Color skyColor_in){
		
		//input
		caAnchor = anchor_in;
		caWidth = width_in;
		caHeight = height_in;
		groundColor = groundColor_in;
		skyColor = skyColor_in;
		
		//calculate
		caCenter = new GPoint(caAnchor.getX() + (caWidth / 2), caAnchor.getY() + (caHeight / 2));
		caRadius = caWidth / 2;
		
		//init circle
		groundCircle = new GOval(caAnchor.getX(), caAnchor.getY(), caWidth, caHeight);
		groundCircle.setFillColor(groundColor); 
		groundCircle.setColor(groundColor);
		groundCircle.setFilled(true);
		
		//init arc
		skyArc = new GArc(caAnchor.getX(), caAnchor.getY(), caWidth, caHeight, 0, 180);
		skyArc.setFillColor(skyColor);
		skyArc.setFilled(true);
		
		//init the triangle
		compensationTriangle = new GPolygon(caCenter.getX(), caCenter.getY()); //vertices will be added relative to this origin
		compensationTriangle.addVertex(0, 0);
		compensationTriangle.addVertex(caRadius, 0); //right
		compensationTriangle.addVertex(-caRadius, 0); //left
		compensationTriangle.setFillColor(groundColor);
		compensationTriangle.setFilled(true);
		
		initCursor();
	}	
	
	private void initCursor(){
		
		//Cursor is 8 units across.
		//Picturing the units. NOT the graphical coords.
		//* = center of circle.
//		|---|---|---|---|---|---|---|---|---|
//		|---|---|---|---|---|---|---|---|---|
//		|-A-|---|---|-B-|-*-|-D-|---|---|-E-|
//		|---|---|---|---|-C-|---|---|---|---|
//		|---|---|---|---|---|---|---|---|---|
		
		double ppu = (caWidth * cursorWidthInPropOfTotalWidth) / 8;
		
		GPoint ptA = new GPoint(caCenter.getX() - ppu * 4, caCenter.getY()); 
		GPoint ptB = new GPoint(caCenter.getX() - ppu,     caCenter.getY()); 
		GPoint ptC = new GPoint(caCenter.getX(),           caCenter.getY() + ppu);
		GPoint ptD = new GPoint(caCenter.getX() + ppu,     caCenter.getY());
		GPoint ptE = new GPoint(caCenter.getX() + ppu * 4, caCenter.getY());
	
		GLine leftLine   = new GLine(ptA.getX(), ptA.getY(),     ptB.getX(), ptB.getY());
		GLine leftLine_t = new GLine(ptA.getX(), ptA.getY() + 1, ptB.getX(), ptB.getY() + 1); //to add thickness
		
		GLine leftLineSlope   = new GLine(ptB.getX(), ptB.getY(),     ptC.getX(), ptC.getY());
		GLine leftLineSlope_t = new GLine(ptB.getX(), ptB.getY() + 1, ptC.getX(), ptC.getY() + 1); //to add thickness
		
		GLine rightLineSlope   = new GLine(ptC.getX(), ptC.getY(),     ptD.getX(), ptD.getY());
		GLine rightLineSlope_t = new GLine(ptC.getX(), ptC.getY() + 1, ptD.getX(), ptD.getY() + 1); //to add thickness
		
		GLine rightLine   = new GLine(ptD.getX(), ptD.getY(),     ptE.getX(), ptE.getY());
		GLine rightLine_t = new GLine(ptD.getX(), ptD.getY() + 1, ptE.getX(), ptE.getY() + 1); //to add thickness
		
		cursorObjects.add(leftLine);
		cursorObjects.add(leftLine_t);
		
		cursorObjects.add(leftLineSlope);
		cursorObjects.add(leftLineSlope_t);
		
		cursorObjects.add(rightLine);
		cursorObjects.add(rightLine_t);
		
		cursorObjects.add(rightLineSlope);
		cursorObjects.add(rightLineSlope_t);
		
		for(GObject line : cursorObjects){
			line.setColor(cursorColor);
		}
	}
	
	//requires magnitude of someY to be <= maxYMag. Should be ensured by caller.
	//scales horizPtsAngSep in the following way:
	//value of someY:           maxYMagnitude .......................0.......-maxYMagnitude
	//value of horizPtsAngSep:  horizPtsSepAng_maxDeviation+180.....180......180 - horizPtsSepAng_maxDeviation
	private void calculate_horizPtsSepAng(){
		//look. change this to use TrackerConstant's linearScale
		//probably a more efficient way to do this. Would probably also be less clear.
		double percentOfMagnitude;
		
		if(currentPosition.y > 0){
			percentOfMagnitude = currentPosition.y / MAX_Y_MAGNITUDE;
			horizPtsSepAng = HORIZ_PTS_SEP_ANG_LEVEL + (HORIZ_PTS_SEP_ANG_MAX_DEVIATION*percentOfMagnitude);
		}
		else{
			percentOfMagnitude = -currentPosition.y / MAX_Y_MAGNITUDE;
			horizPtsSepAng = HORIZ_PTS_SEP_ANG_LEVEL - (HORIZ_PTS_SEP_ANG_MAX_DEVIATION*percentOfMagnitude);
		}
		
	}
	
	private void calculate_tiltAng(){
		//look. change this to use TrackerConstant's linearScale
		double percentOfMagnitude;
		if(currentPosition.x > 0){
			percentOfMagnitude = currentPosition.x / MAX_X_MAGNITUDE;
			tiltAng = TILT_ANG_LEVEL + (TILT_ANG_MAX_DEVIATION * percentOfMagnitude);
			
		}
		else{
			percentOfMagnitude = -currentPosition.x / MAX_X_MAGNITUDE;
			tiltAng = TILT_ANG_LEVEL - (TILT_ANG_MAX_DEVIATION * percentOfMagnitude);
		}
	}
	
	private void calculate_rightHorizPtAng_and_quadrantPtForTriangle(){
	
		rightHorizPtAng = tiltAng - (horizPtsSepAng / 2);
		
		double quadrantPointAngle = (HORIZ_PTS_SEP_ANG_LEVEL - horizPtsSepAng) / 2;
		
		double quadrantPointX = caRadius * Math.cos(Math.toRadians(quadrantPointAngle)); //relative to center of circle
		double quadrantPointY = -caRadius * Math.sin(Math.toRadians(quadrantPointAngle));
		
		quadrantPtForTriangle.setLocation(quadrantPointX, quadrantPointY);


	}
	
	private void updateShapeData(){
		
		
		skyArc = new GArc(caAnchor.getX(), caAnchor.getY(), caWidth, caHeight, rightHorizPtAng, horizPtsSepAng);
		skyArc.setFillColor(skyColor);
		skyArc.setColor(skyColor);
		skyArc.setFilled(true);
		
		
		compensationTriangle = new GPolygon(caCenter.getX(), caCenter.getY());
		compensationTriangle.addVertex(0, 0);
		compensationTriangle.addVertex(quadrantPtForTriangle.getX(), quadrantPtForTriangle.getY()); 
		compensationTriangle.addVertex(-quadrantPtForTriangle.getX(), quadrantPtForTriangle.getY()); 
		compensationTriangle.rotate(tiltAng - TILT_ANG_LEVEL);
		compensationTriangle.setFilled(true);
		
		if(horizPtsSepAng <= 180){
			compensationTriangle.setFillColor(groundColor); //fill
			compensationTriangle.setColor(groundColor); //border
			
		}
		else{
			compensationTriangle.setFillColor(skyColor); //fill
			compensationTriangle.setColor(skyColor); //border
		}
			
		
	}
	
	
	//TAKES A DIFFERENTIAL. OF THE [-1000, 1000] SCALE.
	public void recalculateAll(double xDiff, double yDiff){
		
		currentPosition.x += xDiff;
		currentPosition.y += yDiff;
		calculate_horizPtsSepAng();
		calculate_tiltAng();
		calculate_rightHorizPtAng_and_quadrantPtForTriangle();
		updateShapeData();
	}
	
	public void reset(){
		setCurrentPosition(0,0);
		recalculateAll(0.0, 0.0);
	}
	
	
	public GOval getGroundCircle(){
		return groundCircle;
	}
	public GArc getSkyArc(){
		return skyArc;
	}
	public GPolygon getCompensationTriangle(){
		return compensationTriangle;
	}
	public ArrayList<GObject> getCursorObjects(){
		return cursorObjects;
	}
	
}
