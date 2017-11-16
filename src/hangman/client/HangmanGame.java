/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangman.client;

import java.net.*;
import java.io.*;


public class HangmanGame {

    String name;
    
    /**
     * @param args the command line arguments
     */
    public void main(String[] args) {
	
	if(!(args[0] == null)) {
	    name = args[0];
	} else {
	    name = "DefaultPlayer";
	}
	
	new HangmanClient(name);
 
    }
}
