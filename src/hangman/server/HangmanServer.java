/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangman.server;

import java.net.*;
import java.io.*;


public class HangmanServer {
    
    private static final int SERVER_PORT = 5000;
    
    
    public void main(String[] args) {
	
	try {
	    ServerSocket sock = new ServerSocket(SERVER_PORT,100);
	    while (true)
		new ClientHandler(sock.accept()).start();

	} catch(IOException e) {
	    System.err.println(e);
	}
	
    }
}


