/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server;

import hangman.common.MessageTypes;
import hangman.common.Constants;
import hangman.file.ReservoirSampling;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author mellstrand
 * @date 2017-11-15
 */

class ClientHandler extends Thread {
    
    private final static int SAMPLE_SIZE = 10;
    
    Socket clientSocket;
    BufferedReader fromClient;
    PrintWriter toClient;
    ReservoirSampling rs;
    File wordFile;
    List<String> wordList;
    String correctWord;
    String guessString;
    int remainingAttempts;
    int clientScore;
    
    public ClientHandler(Socket clientSocket) {
	
	this.clientSocket = clientSocket;
	
	try {
	    wordFile = new File("words.txt");
	    fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    toClient = new PrintWriter(clientSocket.getOutputStream());

	} catch(IOException e) {
	    System.err.println(e);
	}
    }
    
    @Override
    public void run() {
	
	try {
	    String name = fromClient.readLine();
	    System.out.println(name + " joined the game server");

	    sendMessage(MessageTypes.INIT, "Welcome " +name+ " lets play");
	    
	    while(true) {
		
		String indata = fromClient.readLine();
		if(indata==null || indata.equals("")) break;
		String[] requestToken = indata.split(Constants.DELIMETER);
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());
		
		switch(msgType) {
		    
		    case INIT:
			initGame();
			sendMessage(MessageTypes.STATUS, statusString());
			break;
		    case NEW:
			newGame();
			sendMessage(MessageTypes.STATUS, statusString());
			break;
		    case GUESS:
			
			switch(checkString(requestToken[1])) {
			    case COMPLETE:
				updateScore(true);
				sendMessage(MessageTypes.NEW, "Game complete, new game?");
				break;
			    case FRAGMENT:
				sendMessage(MessageTypes.STATUS, statusString());
				break;
			    case FAILED:
				updateRemainingAttempts();
				sendMessage(MessageTypes.STATUS, statusString());
				break;
			}
			break;
		    case END:
			closeConnection();
			break;
		    default:
			   
		}
	    }
	    
	    System.out.println("Client " +name +" ended the session");
	
	} catch(IOException ioe) {
	    System.err.println(ioe);
	}
	
    }
    
    private String statusString() {
	
	StringJoiner joiner = new StringJoiner(Constants.DELIMETER);
        joiner.add("Word: "+guessString);
        joiner.add("Remaining Attempts: "+remainingAttempts);
	joiner.add("Score: "+clientScore);
        return joiner.toString();
    }
 
    private void initGame() throws IOException {
	clientScore = 0;
	rs = new ReservoirSampling(wordFile, SAMPLE_SIZE);
	wordList = rs.getSample();
	newGame();
    }
    
    private void newGame() throws IOException {
	setNewWord(getNewWord());
	setRemainingAttempts();
	initGuessString();
    } 
    
    private String getNewWord() throws IOException {

	if(!wordList.isEmpty()) {
	
	    String newWord = wordList.get(0);
	    wordList.remove(0);
	
	    return newWord;
	
	} else {
	    
	    wordList = rs.getSample();
	    String newWord = wordList.get(0);
	    wordList.remove(0);
	
	    return newWord;
	}
	
    }
    
    private void setNewWord(String newWord) {
	correctWord = newWord;
    }
    
    private void initGuessString() {
	char[] chars = new char[correctWord.length()];
	Arrays.fill(chars, '_');
	guessString = new String(chars);
    }
    
    private WordStatus checkString(String clientGuess) {
	
	boolean fragment = false;
	
	if(correctWord.contains(clientGuess)) {
	
	    if(clientGuess.length() == 1) {

		char[] temp = guessString.toCharArray();

		char guess = clientGuess.charAt(0);
		for(int i=0; i<correctWord.length(); i++) {
		    if(correctWord.charAt(i) == guess) {
			temp[i] = guess;
			fragment = true;
		    }
		}

		guessString = String.valueOf(temp);
		
		if(guessString.equalsIgnoreCase(correctWord)) 
		    return WordStatus.COMPLETE;
		else if(fragment)
		    return WordStatus.FRAGMENT;

	    } else if (clientGuess.length() == correctWord.length() && clientGuess.equalsIgnoreCase(correctWord)) {

		return WordStatus.COMPLETE;

	    }
	    
	}
	
	return WordStatus.FAILED;	
    }
    
    private void updateScore(boolean increase) {
	if (increase) {
	    clientScore++;
	} else {
	    clientScore--;
	}
    }
    
    private void setRemainingAttempts() {
	remainingAttempts = correctWord.length();
    }
    
    private void updateRemainingAttempts() {
	remainingAttempts--;
    }

    private void sendMessage(MessageTypes mt, String message) {
	StringJoiner joiner = new StringJoiner(Constants.DELIMETER);
        joiner.add(mt.toString());
        joiner.add(message);
	toClient.println(joiner.toString());
	toClient.flush();
    }
    
    private void closeConnection() throws IOException {
	fromClient.close();
	toClient.close();
	clientSocket.close();
	clientSocket = null;		
    }
}
