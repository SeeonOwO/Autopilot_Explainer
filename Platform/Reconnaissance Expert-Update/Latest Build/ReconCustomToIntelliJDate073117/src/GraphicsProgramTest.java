
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.Color;
import java.awt.Font;

import java.awt.Rectangle;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import acm.graphics.GRect;
import acm.graphics.GArc;
import acm.graphics.GOval;
import acm.graphics.GPolygon;
import acm.graphics.GObject;
import acm.graphics.GCompound;
import acm.program.GraphicsProgram;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import acm.graphics.GPoint;

import acm.graphics.GCanvas;

import java.util.HashMap;
import java.util.ArrayList;

import java.awt.Dimension;
import java.awt.Toolkit;


/**
 * Used to test various graphics ideas and concepts before implementing. Not used in program. 
 * 
 *
 */
public class GraphicsProgramTest extends GraphicsProgram {
	
	Toolkit kit = Toolkit.getDefaultToolkit();
	Dimension d = kit.getScreenSize();
	
	//looks like you can use resize to change, and also it takes a while, so pause. But I dont like the pause...
	//will only need to call resize once though. Might work weird when dragging though...Interesting problem.
	public static final int APPLICATION_HEIGHT = 800; //not really used, window will change size during init based on monitor dimensions..
	public static final int APPLICATION_WIDTH = 800;
	
	public static final int APPLICATION_X = 0;
	public static final int APPLICATION_Y = 0;
	
	 
	
	
	
	public static void main (String[] args) {
		new GraphicsProgramTest().start();
	}
	
	public void init() {
		
		Dimension maxDimension = new Dimension(0, 0);
		
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDeviceArray = graphicsEnvironment.getScreenDevices();
		
		for(int j = 0; j < graphicsDeviceArray.length; j++){
			
			GraphicsDevice graphicsDevice = graphicsDeviceArray[j];
			GraphicsConfiguration[] graphicsConfiguration = graphicsDevice.getConfigurations();
			
			for(int i = 0; i < graphicsConfiguration.length; i++){

				Dimension currentDim = graphicsConfiguration[i].getBounds().getSize();
				if(currentDim.height > maxDimension.height){
					maxDimension = currentDim;
				}
			
			}
		}
		
		
		maxDimension.setSize((int)(maxDimension.getWidth()), (int)(maxDimension.getHeight() * 0.95)); //resizing height by 95% makes everything visible. good. 
		
		
//		Toolkit kit = Toolkit.getDefaultToolkit();
//		Dimension d = kit.getScreenSize();
		
		
		d = maxDimension;
		
		int screenWidth = (int)(d.width);
		int screenHeight = (int)(d.height);
		
		setSize(screenWidth, screenHeight);
		
		GRect topLeft = new GRect(0, 0, screenWidth / 2, screenHeight / 2);
		topLeft.setColor(Color.RED);
		topLeft.setFilled(true);	
		add(topLeft);
		
		GRect topRight = new GRect(screenWidth / 2, 0, screenWidth / 2, screenHeight / 2);
		topRight.setColor(Color.BLUE);
		topRight.setFilled(true);	
		add(topRight);
		
		GRect bottomRight = new GRect(screenWidth / 2, screenHeight / 2, screenWidth / 2, screenHeight / 2);
		bottomRight.setColor(Color.RED);
		bottomRight.setFilled(true);	
		add(bottomRight);
		
		GRect bottomLeft = new GRect(0, screenHeight / 2, screenWidth / 2, screenHeight / 2);
		bottomLeft.setColor(Color.BLUE);
		bottomLeft.setFilled(true);	
		add(bottomLeft);
		
		
		
		
		
	}
	
	public void run () {
		
		
		
		
//		double loopTime;
//		while(true){
//			loopTime = System.currentTimeMillis();
//			if(loopTime % 10 == 0){
//				joystick.poll();
//				xPos += joystick.getComponents()[12].getPollData() * joystickMult;
//				yPos += joystick.getComponents()[13].getPollData() * joystickMult;
//				
//				
//			}	
//		}
	}
	
//	private void updateGraphics(double someX, double someY){
//		//using these setVisible calls is necessary because they are the fastest way to change appearance.
//		
//		//calculations
//		am.recalculateAll(someX, someY);
//		
//		//retrieve new data
//		GArc newArc = am.getSkyArc();
//		GPolygon newTriangle = am.getCompensationTriangle();
//		
//		//prep/stage
//		newArc.setVisible(false);
//		newTriangle.setVisible(false);
//		add(newArc);
//		add(newTriangle);
//		
//		for(GObject line : cursorObjects){
//			line.sendToFront();
//		}
//		
//		//do change
//		newArc.setVisible(true);
//		arc.setVisible(false);
//		newTriangle.setVisible(true);
//		triangle.setVisible(false);
//		
//		//clean up
//		remove(arc);
//		remove(triangle);
//		
//		//reassign
//		arc = newArc;
//		triangle = newTriangle;
//		
//	}
//	
//
//	
//	private void initializeControllers() {
//		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
//		Controller[] cs = ce.getControllers();
//		
//		//Find joystick input number
//		boolean joystickFound = false;
//		for(int i = 0; i < cs.length; i++){
//			if(cs[i].getName().equals("Logitech Extreme 3D")){
//				joystick = cs[i];
//				joystickFound = true;
//			}
//		}
//		if(!joystickFound){
//			JOptionPane.showMessageDialog(this, "Error setting up joystick. Please quit the application, re-plug, and try again.", "Error Setting Up Joystick", JOptionPane.ERROR_MESSAGE);
//			
//		}
//		
//	}
}
