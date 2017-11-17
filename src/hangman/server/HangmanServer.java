/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangman.server;

import java.net.*;
import java.io.*;

/**
 * Starts the server for the game
 */
public class HangmanServer {
    
    private static final int SERVER_PORT = 5000;
    
    public static void main(String[] args) {
	
	try {
	    ServerSocket sock = new ServerSocket(SERVER_PORT,100);
	    System.out.println("Server running...");
	    while (true)
		new ClientHandler(sock.accept()).start();

	} catch(IOException e) {
	    System.err.println(e);
	}
    }
}


