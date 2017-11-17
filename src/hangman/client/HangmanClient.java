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


public class HangmanClient implements Runnable {
    
    private static final String PROMPT = ">> ";
    private ServerHandler serverHandler;
    private final Scanner scanner = new Scanner(System.in);
    private String name;
    private boolean connected = false;
    
    public HangmanClient(String name) {
	
	this.name = name;
    }
    
    public void start() {
	
	System.out.println("DEBUG: start-method");
	if(connected) {
	    return;
	}
        serverHandler = new ServerHandler();
	//serverHandler.connect(name);
	new Thread(this).start();
	//run();
    }
    
    /**
     * Receives messages from the server and interprets them
     * and perform the correct action to it
     */
    @Override
    public void run() {
	
	serverHandler.connect(name, this);
	connected = true;
	
	while(connected) {
	    
		
	    try {
		String userInput = readUserInput();
		if(userInput==null || userInput.equals("")) break;
		String[] requestToken = userInput.split(Constants.DELIMETER);
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());
		
		switch(msgType) {
		  case NEW:
			sendMessage(MessageTypes.NEW);
			break;
		    case END:
			sendMessage(MessageTypes.END);
			quitPlaying();
			break;
		    default:
			printLocal("Dont understand the request \n Usage 'NEW' or 'END'");
		}
	    } catch(IOException ioe) {
		connected = false;
		System.err.println(ioe);
	    }
	
	}   
	
    }
    
    /**
     * Handles play again interactions
     */
    private void playAgain() throws IOException{
	
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
    private synchronized void printLocal(String... parts) {
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
     * Client wants to stop playing, terminate program
     *
     * @throws IOException - When connection problem
     */
    private void quitPlaying() throws IOException {
	//sendMessage(MessageTypes.END);
	serverHandler.disconnect();
	System.exit(0);
    }
    
    public void messageHandler(String serverMessage) throws IOException {
	
	if(!(serverMessage==null || serverMessage.equals(""))) {
		String[] requestToken = serverMessage.split(Constants.DELIMETER);
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
		
	}
	printLocal("No message, problem something");
    }
    
}
