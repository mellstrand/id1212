/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangman.client;

import java.net.*;
import java.io.*;


public class HangmanGame {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int TIMEOUT = 30000;
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
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
	
	try { 
	    socket = new Socket();
	    socket.connect(new InetSocketAddress(SERVER_NAME, SERVER_PORT), TIMEOUT);
/*
	    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    toServer = new PrintWriter(socket.getOutputStream());
	    toServer.println(name);
	    toServer.flush();
	    System.out.println(fromServer.readLine());
*/
	    new HangmanClient(socket);
	    
	} catch(Exception e) {
	    System.err.println(e);
	}
}
    
}
