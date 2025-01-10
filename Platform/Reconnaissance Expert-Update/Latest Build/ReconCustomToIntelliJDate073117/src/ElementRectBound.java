import acm.graphics.GPoint;



/**
 * A class to represent and give useful information about a rectangular graphical area. 
 * Constructed by a topLeftAnchor, width, and height. 
 * It may be used to bound a single graphical object, but in most cases it is used to bound a collection of graphical objects. 
 * Created in order to manage program resizing. 
 * The GPoints should always represent graphical coordinates.
 * @author BenPinzone
 *
 */
public class ElementRectBound{
	
	
	
	//Defines the object
	private GPoint topLeft;
	private GPoint bottomRight;
	
	//Calculated and available
	private GPoint topRight;
	private GPoint bottomLeft;
	
	private double height;
	private double width;
	private GPoint center;

	public ElementRectBound(GPoint topLeftAnchor, double width_in, double height_in){
		
		width = width_in;
		height = height_in;
		
		topLeft = new GPoint(topLeftAnchor);
		bottomRight = new GPoint(topLeft.getX() + width, topLeft.getY() + height);
		
		topRight = new GPoint(bottomRight.getX(), topLeft.getY());
		bottomLeft = new GPoint(topLeft.getX(), bottomRight.getY());
		

		center = new GPoint(
			topLeft.getX() + (getWidth() / 2), 
		  	topLeft.getY() + (getHeight() / 2)
		);
		
	}
	
	public GPoint getTopLeft(){
		return topLeft;
	}
	public GPoint getBottomRight(){
		return bottomRight;
	}
	public GPoint getTopRight(){
		return topRight;
	}
	public GPoint getBottomLeft(){
		return bottomLeft;
	}

	public double getWidth(){
		return width;
	}
	

	public double getHeight(){
		return height;
	}
	
	public GPoint getCenter(){
		return center;
	}
	
	
	
}