/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author mellstrand
 */
public class ServerHandler {
    
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int TIMEOUT = 30000; //30 sekunder
    
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
    
    public void connect() {
	
	CompletableFuture.runAsync(() -> {
	    try {
		socket = new Socket();
		socket.connect(new InetSocketAddress(SERVER_NAME, SERVER_PORT), TIMEOUT);

		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toServer = new PrintWriter(socket.getOutputStream());

	    } catch(IOException ieo) {
		System.err.println(ieo);
	    }
	});
    }
    
    public void disconnet() throws IOException {
	socket.close();
	socket = null;
	
    }
    
    public void transmit(String msg) {
	CompletableFuture.runAsync(() -> { 
	    toServer.println(msg);
	    toServer.flush();
	});
	
    }
    
    public String receive() throws IOException {
	return fromServer.readLine();
    }
    
}
