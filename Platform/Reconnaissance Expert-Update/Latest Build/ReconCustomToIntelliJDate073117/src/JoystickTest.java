
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;



/**
 * Outputs (console and file) useful information about the controller environment and the joystick button mapping configuration.
 * @author Kevin Li
 * @author BenPinzone
 *
 */
public class JoystickTest {

	
	public static void main(String[] args) { 
		PrintWriter joystickOut = null;
		try{
			joystickOut = new PrintWriter(new FileWriter("JOYSTICK_DATA.txt"));
		}
		
		catch(IOException e){
			
		}
		
		
		System.out.println("JInput version: " + Version.getVersion()); 
		joystickOut.println("JInput version: " + Version.getVersion()); 
		
		
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment(); 
		Controller[] cs = ce.getControllers(); 
		Controller joystick;
		
		for (int i = 0; i < cs.length; i++){
			//									important names
			System.out.println(i + ". " + cs[i].getName() + ", " + cs[i].getType() );
			joystickOut.println(i + ". " + cs[i].getName() + ", " + cs[i].getType() );
		}
		
		
		
		//----------------------------------------------------------

		
		//Find joystick input number
		joystick = null;
		String interestedDeviceName;
		if(OneButtonTracker.USE_JOYSTICK){
			interestedDeviceName = new String("Logitech Extreme 3D");
		}
		else{
			interestedDeviceName = new String("Wireless Controller");
		}
		
		
		for(Controller someController : cs){
			if(someController.getName().equals(interestedDeviceName)){
				joystick = someController;
			}
		}
		
		if(joystick == null){
			System.out.println("COULD NOT FIND JOYSTICK");
			joystickOut.println("COULD NOT FIND JOYSTICK");
			return;
		}
		

		//----------------------------------------------------------
		

		
		Component[] comps = joystick.getComponents(); //look. is this index REALLY hard coded?...
		//                   look. changed this index. used to be 2.
		System.out.println("Outputting data for: " + joystick.getName());
		joystickOut.println("Outputting data for: " + joystick.getName());
		
		System.out.println("Components: (" + comps.length + ")");
		joystickOut.println("Components: (" + comps.length + ")");
		
		for (int i = 0; i < comps.length; i++){
			System.out.println( i + ". " +
					comps[i].getName() + ", " +
					getIdentifierName(comps[i]) + ", " +
					(comps[i].isRelative() ? "relative" : "absolute") + ", " +
					(comps[i].isAnalog() ? "analog" : "digital") + ", " +
					comps[i].getDeadZone() + ", " + comps[i].getPollData());
			
			joystickOut.println( i + ". " +
					comps[i].getName() + ", " +
					getIdentifierName(comps[i]) + ", " +
					(comps[i].isRelative() ? "relative" : "absolute") + ", " +
					(comps[i].isAnalog() ? "analog" : "digital") + ", " +
					comps[i].getDeadZone() + ", " + comps[i].getPollData());
			//System.out.println(i + " " + comps[i].getPollData());
			
//			if(comps[i].getName().equals("x")){
//				componentIndex = 26;
//			}
			
			
		}
		
		joystickOut.close();
		
//		steering = comps[componentIndex];
		
		float[] componentsLastReading = new float[comps.length];
		for(float someFloat : componentsLastReading){
			someFloat = 0;
		}
		while(true){
			joystick.poll();
			for(int i = 0; i < comps.length; i++){
				float polledValue = comps[i].getPollData();
				if (Math.abs(polledValue - componentsLastReading[i]) > 0.1){
					System.out.println(comps[i].getIdentifier().getName() + ": " + polledValue);
					componentsLastReading[i] = polledValue;
				}
				
			}
			
		}
		
	
		
	 
	}	
	
	private static String getIdentifierName(Component comp)
	{
	 Component.Identifier id = comp.getIdentifier();
	 if (id == Component.Identifier.Button.UNKNOWN)
	 return "button"; // an unknown button
	 else if(id == Component.Identifier.Key.UNKNOWN)
	 return "key"; // an unknown key
	 else
	 return id.getName();
	} 
}