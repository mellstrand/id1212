/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangmangame;

import java.net.*;
import java.io.*;


public class HangmanGame {

    private static final int SERVER_PORT = 5000;
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
    
    public HangmanGame(String name) {
	
	try {
	    socket = new Socket("localhost", SERVER_PORT);
	    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    toServer = new PrintWriter(socket.getOutputStream());
	    toServer.println(name);
	    toServer.flush();
	    System.out.println(fromServer.readLine());

	    new HangmanClient(socket);
	    
	} catch(Exception e) {
	    System.err.println(e);
	}

	
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	
	new HangmanGame(args[0]);
    }
    
}
