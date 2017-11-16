/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-10
 * 
 * @ref https://kebomix.wordpress.com/2011/01/09/reservoir-sampling-java/
 */

package hangman.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ReservoirSampling {
    
    private int size;
    public File file;
    
    public ReservoirSampling(File file, int size) {
	
	this.file = file;
	this.size = size;
	
    }
    
    public List<String> getSample() throws FileNotFoundException, IOException {
	
	int count = 0;
	int randomNumber;
	String currentLine;
	
	List<String> reservoirList = new ArrayList<>(size);
	BufferedReader br = new BufferedReader(new FileReader(file));
	Random ra = new Random();
	
	while((currentLine = br.readLine()) != null) {
	    count++;
	    if(count <= size) {
		reservoirList.add(currentLine);
	    } else {
		randomNumber = (int)ra.nextInt(count);
		if(randomNumber < size) {
		    reservoirList.set(randomNumber, currentLine);
		}
	    }
	}
	
	return reservoirList;
    }
}
