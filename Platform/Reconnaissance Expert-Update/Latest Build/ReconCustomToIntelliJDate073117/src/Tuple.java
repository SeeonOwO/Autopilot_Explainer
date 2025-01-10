import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;



public class Tuple {
	public double x;
	public double y;
	public static Tuple ORIGIN_TUPLE = new Tuple(0,0);
	
	public Tuple (double a, double b) {
		x = a;
		y = b;
	}
	
	//used only to produce Entry-canvasPosOnScreen field. seems useless.
	public Tuple(Point locationOnScreen) {
		x = locationOnScreen.getX();
		y = locationOnScreen.getY();
	}
	
	public double innerProduct () {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	public double distance (Tuple other) {
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
	}
	
	
	/**
	 * Returns a new tuple that is this tuple with it's components multiplied by s. Does not modify this tuple.
	 * @param s
	 * @return
	 */
	public Tuple scalarMultiple_new (double s) {
		return new Tuple (x * s, y * s);
	}
	

	/**
	 * Modifies this tuple to be this tuple with its components multiplied by s.
	 * @param s
	 */
	public void scalarMultiple_mod(double s){
		x = x * s;
		y = y * s;
	}
	
	/**
	 * Returns a new Tuple that is the sum of this Tuple and other Tuple. Does not modify this Tuple or other Tuple. 
	 * @param other
	 * @return
	 */
	public Tuple add_new (Tuple other) {
		return new Tuple (x + other.x, y + other.y);
	}
	
	
	/**
	 * Modifies this Tuple to be the sum of this tuple and other tuple. 
	 * @param other
	 */
	public void add_mod(Tuple other){
		x = x + other.x;
		y = y + other.y;
	}
	
	/**
	 * Returns a new Tuple that is this tuple minus other tuple. Does not modify this tuple or other tuple. 
	 * @param other
	 * @return
	 */
	public Tuple subtract_new (Tuple other) {
		return new Tuple (x - other.x, y - other.y);
	}
	
	/**
	 * Modifies this tuple to be this tuple minus other tuple. Does not modify other tuple. 
	 * @param other
	 */
	public void subtract_mod(Tuple other){
		x = x - other.x;
		y = y - other.y;
	}
	
	
	public void setComponents(double x_in, double y_in){
		x = x_in;
		y = y_in;
	}
	
	
	
	public String toString () {
		NumberFormat f = new DecimalFormat("#0.0000");
		return f.format(x) + " " + f.format(y);
	}
	
	public String toStringComma(){
		NumberFormat f = new DecimalFormat("#0.0000");
		return f.format(x) + "," + f.format(y);
	}
	
	
}
