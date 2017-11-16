/**
 *
 * @author mellstrand
 */
package hangman.client;

import hangman.common.Constants;
import hangman.common.MessageTypes;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringJoiner;


public class HangmanClient implements Runnable {
    
    String name;
    ServerHandler serverHandler;
    Scanner scanner = new Scanner(System.in);
    
    public HangmanClient(String name) {
	
	this.name = name;
    }
    
    public void start() {
	
        serverHandler = new ServerHandler();
	serverHandler.connect(name);
	new Thread(this).start();
    }
    
    @Override
    public void run() {
	
	while(true) {
	    
	    try {
	    
		String inData = serverHandler.receive();
		if(inData==null || inData.equals("")) break;
		String[] requestToken = inData.split(Constants.DELIMETER);
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());
		
		switch(msgType) {
		    case STATUS:
			print(Arrays.copyOfRange(requestToken, 1, requestToken.length));
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

    	StringJoiner joiner = new StringJoiner(Constants.DELIMETER);
        joiner.add(mt.toString());
        joiner.add(line);
	serverHandler.transmit(joiner.toString());
    }
    
    private void print(String... parts) {
	for (String part: parts) {
          System.out.println(part);
        }
    }
}
