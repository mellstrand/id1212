/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author mellstrand
 */
public class MessageListener implements Runnable {
    
    private final HangmanClient client;
    private final BufferedReader fromServer;
    private boolean receive;
    
    public MessageListener(HangmanClient client, BufferedReader fromServer) {
	this.client = client;
	this.fromServer = fromServer;
	receive = true;
    }
    
    @Override
    public void run() {
	try {
	    while(receive) {
		client.messageHandler(fromServer.readLine());
	    }
	} catch(IOException ioe) {
	    receive = false;
	    System.out.println(ioe);
	}
    }
    
}
