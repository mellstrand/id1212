/**
 *
 * @author mellstrand
 */
package hangman.client;

import hangman.common.MessageTypes;
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class HangmanClient extends Thread {
    
    Socket socket;
    BufferedReader fromServer;
    PrintWriter toServer;
    
    Scanner scanner = new Scanner(System.in);
    
    public HangmanClient(Socket socket) {
	
	this.socket = socket;
	
	try {
	    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    toServer = new PrintWriter(socket.getOutputStream());
	} catch(IOException ioe) {
	    System.err.println(ioe);
	}
	
    }
    
    @Override
    public void run() {
	
	while(true) {
	    
	    try {
	    
		String inData = fromServer.readLine();
		if(inData==null || inData.equals("")) break;
		String[] requestToken = inData.split(" ");
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());
		
		switch(msgType) {
		    case STATUS:
			System.out.println(requestToken[1]);
			break;
		    case NEW:
			playAgain();
			break;
		    case GUESS:
			sendMessage(MessageTypes.GUESS, scanner.nextLine());
			break;
		    default:
		}	
		
	    } catch(IOException ioe) {
		System.err.println(ioe);
	    }
	}   
	
    }
    
    private void playAgain() {
	
	boolean run = true;
	
	System.out.println("Vill du spela igen? (y or n)");
	    
	while(run) {
	    
	    String userInput = scanner.nextLine();
	    if(userInput.equalsIgnoreCase("y")){
		run = false;
		sendMessage(MessageTypes.NEW, "");
	    } else if(userInput.equalsIgnoreCase("n")) {
		sendMessage(MessageTypes.END, "");
	    } else {
		System.out.println("Förstod inte, svara 'y' eller 'n': ");
	    }
	}
	
    }
    
    private void sendMessage(MessageTypes mt, String line) {
	toServer.println(mt.toString() +" "+ line);
	toServer.flush();
    }
}
