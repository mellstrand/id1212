/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangman.client;


public class HangmanPlay {
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	
	String name;
	
	if(!(args.length == 0)) {
	    name = args[0];
	} else {
	    name = "DefaultPlayer";
	}
	
	new HangmanClient("TEST").start();
 
    }
}
