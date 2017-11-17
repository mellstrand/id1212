/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client;

import hangman.common.MessageTypes;
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
 * @date 2017-11-16
 */
public class ServerHandler {
    
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int TIMEOUT = 30000; //30 sekunder
    
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
    
    /**
     * Setting up the connection to the server and creates in/out streams
     * 
     * @param name - Name of the player
     * @param client - Client who sets up the connection
     */
    public void connect(String name, HangmanClient client) {
	CompletableFuture.runAsync(() -> {

	    try {
		    socket = new Socket();
		    socket.connect(new InetSocketAddress(SERVER_NAME, SERVER_PORT), TIMEOUT);

		    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    toServer = new PrintWriter(socket.getOutputStream());

		    new Thread(new MessageListener(client, fromServer)).start();
		    
		    transmit(name);
		    

		} catch(IOException ieo) {
		    System.err.println(ieo);
		}
	});
    }
    
    /**
     * To disconnect from server
     */
    public void disconnect() {
	try {
	    socket.close();
	    socket = null;
	} catch(IOException ioe) {
	    System.out.println("Connection closed, terminating...");
	}
 }
    
    /**
     * To send server a message
     * 
     * @param msg - message to send 
     */
    public void transmit(String msg) {
	CompletableFuture.runAsync(() -> {
	    toServer.println(msg);
	    toServer.flush();
	});
    }
    
    /**
     * Receive message from server
     * 
     * @return - message from server
     * @throws IOException - if connection problem
     */
    
    /*
    public String receive() throws IOException {
	return fromServer.readLine();
    }
    */
    
}
