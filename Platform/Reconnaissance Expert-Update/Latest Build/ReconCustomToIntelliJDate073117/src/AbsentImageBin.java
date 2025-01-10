//change in absentImage
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

//bin as in we're done with them? or bin as in we're going to pick from here?
//looks more like a hat that we pick from, and if we dont like what we picked, we put it in the waiting room, then put them back. 

/**
 * A class that represents a bin (or collection) of no-threat images. 
 * Each bin is identified by a character representing what map the images are from.
 * All the images in a single bin are from the same map. 
 * 
 * @author Kevin Li - Wrote original core functionality.
 * @author Ben Pinzone - Made many improvements, optimizations, improved readability, re-wrote portions, documented, etc. I'd be happy to answer questions: benpinzone7@gmail.com
 */
public class AbsentImageBin implements Comparable<AbsentImageBin> {

	/**
	 * A Queue of Strings, where each string is the full relative file path and file name of an image. Its true type is a LinkedList
	 */
	public Queue<String> bin;
	
	/**
	 * The map name of the bin. Refers to both a world map and the data structure map index.
	 */
	public char map; 
	
	public AbsentImageBin (char mapName) {
		map = mapName;
		bin = new LinkedList<String>(); 
	}
	
	/**
	 * Adds String s to the Queue of Strings (the bin). s should be a full relative file path and file name of an image.
	 * @param s
	 */
	public void add (String s) {
		bin.add(s);
	}
	
	/**
	 * Shuffles the order of the images within the bin. 
	 */
	public void shuffle () {
		
		Collections.shuffle((LinkedList<String>)bin);
	}
	/**
	 * Gets the image string at the head of the bin. Also removes it from the bin.
	 * @return
	 */
	public String get() {
		return bin.poll();
	}
	
	/**
	 * For use with a PriorityQueue of Images. Bins with the most images remaining are the "greatest".
	 * When bins have an equal number of images, their ordering is random.
	 */
	public int compareTo (AbsentImageBin other) {
		if (bin.size() == other.bin.size()) {
			return (Math.random() > 0.5)? 1:-1;
		}
		else {
			return other.bin.size() - bin.size();
		}
	}
}
