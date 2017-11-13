/**
 *
 * @author mellstrand
 */
package hangmangame;

import java.io.*;
import java.net.*;

public class HangmanClient {
    
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
    
    public HangmanClient(Socket socket) {
	
	this.socket = socket;
	
    }
}
