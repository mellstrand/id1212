/**
 *
 * @author mellstrand
 * @date 2017-11-16
 */
package hangman.client;

import hangman.common.Constants;
import hangman.common.MessageTypes;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringJoiner;


public class HangmanClient {
    
    private static final String PROMPT = ">> ";
    String name;
    ServerHandler serverHandler;
    Scanner scanner = new Scanner(System.in);
    
    public HangmanClient(String name) {
	
	this.name = name;
    }
    
    public void start() {
	
        serverHandler = new ServerHandler();
	serverHandler.connect(name);
	//new Thread(this).start();
	run();
    }
    
    /**
     * Receives messages from the server and interprets them
     * and perform the correct action to it
     */
    public void run() {
	
	while(true) {
	    
	    try {
	    
		String inData = serverHandler.receive();
		if(inData==null || inData.equals("")) break;
		String[] requestToken = inData.split(Constants.DELIMETER);
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());
		
		switch(msgType) {
		    case INIT:
			sendMessage(MessageTypes.INIT, " ");
			printLocal("Connected to the server, lets play.");
			break;
		    case STATUS:
			printLocal(Arrays.copyOfRange(requestToken, 1, requestToken.length));
			sendMessage(MessageTypes.GUESS, readUserInput());
			break;
		    case NEW:
			printLocal(Arrays.copyOfRange(requestToken, 1, requestToken.length));
			playAgain();
			break;
		    case GUESS:
			sendMessage(MessageTypes.GUESS, readUserInput());
			break;
		    default:
		}	
		
	    } catch(IOException ioe) {
		System.err.println(ioe);
	    }
	}   
	
    }
    
    /**
     * Handles play again interactions
     */
    private void playAgain() {
	
	boolean run = true;
	
	printLocal("Answer with 'y' or 'n'");
	    
	while(run) {
	    
	    String userInput = readUserInput();
	    if(userInput.equalsIgnoreCase("y")){
		run = false;
		sendMessage(MessageTypes.NEW);
	    } else if(userInput.equalsIgnoreCase("n")) {
		quitPlaying();
	    } else {
		printLocal("Usage: 'y' or 'n'");
	    }
	}
	
    }
    
    /**
     * Send message consisting of only type of message
     * 
     * @param mt - enum MessageTypes, to specify type of message
     */
    private void sendMessage(MessageTypes mt) {
	serverHandler.transmit(mt.toString());
    }
    
    /**
     * Joins type and message to one string with DELIMETER and sends to server
     * 
     * @param mt - enum MessageTypes, to specify type of message
     * @param line  - message to server
     */
    private void sendMessage(MessageTypes mt, String line) {

    	StringJoiner joiner = new StringJoiner(Constants.DELIMETER);
        joiner.add(mt.toString());
        joiner.add(line);
	serverHandler.transmit(joiner.toString());
    }
    
    /**
     * For printing local messages, i.e. on client side
     * 
     * @param parts - String to be printed 
     */
    private void printLocal(String... parts) {
	for (String part: parts) {
          System.out.println(part);
        }
    }
    
    /**
     * Print prompt and read user input from the console
     * 
     * @return The user input
     */
    private String readUserInput() {
	System.out.print(PROMPT);
	return scanner.nextLine();
    }
    
    /**
     * When wanting to stop playing, terminate
     */
    private void quitPlaying() {
	sendMessage(MessageTypes.END);
	//serverHandler.disconnect();
	System.exit(0);
    }
    
}
