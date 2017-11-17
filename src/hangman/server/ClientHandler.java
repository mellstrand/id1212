/**
 *
 * @author mellstrand
 * @date 2017-11-15
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
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Class that handles each client that connects to the server for playing
 */
class ClientHandler extends Thread {
    
    private final static int SAMPLE_SIZE = 10;
    private Socket clientSocket;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private ReservoirSampling rs;
    private File wordFile;
    private List<String> wordList;
    private String correctWord;
    private String guessString;
    private LinkedList<Character> guessedCharacters;
    private int remainingAttempts;
    private int clientScore;
    private boolean clientPlaying;
	
    
    /**
     * Constructor, opens word file and input/output streams for communication
     * 
     * @param clientSocket - Socket for communication with client
     */
    public ClientHandler(Socket clientSocket) {
	
	this.clientSocket = clientSocket;
	
	try {
	    wordFile = new File("words.txt");
	    fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    toClient = new PrintWriter(clientSocket.getOutputStream());
	    guessedCharacters = new LinkedList<>();
	    clientPlaying = true;
	    
	} catch(IOException e) {
	    System.err.println(e);
	}
    }
    
    /**
     * Receives messages from client and interprets them to perform correct action
     */
    @Override
    public void run() {
	
	try {
	    String name = fromClient.readLine();
	    System.out.println(name + " joined the game server");

	    sendMessage(MessageTypes.INIT, "SERVER: Welcome " +name);
	    
	    while(clientPlaying) {
		
		String indata = fromClient.readLine();
		//DEBUG/INFO message
		System.out.println("Message from: "+name+ " - " +indata);

		if(indata==null || indata.equals("")) break;
		String[] requestToken = indata.split(Constants.TCP_DELIMETER);
		MessageTypes msgType = MessageTypes.valueOf(requestToken[0].toUpperCase());	
/*
		try {
		    sleep(10000);
		} catch(Exception e) {
		    //Do nothing
		}
*/
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
			try {
			    switch(checkString(requestToken[1])) {
				case COMPLETE:
				    updateScore(true);
				    sendMessage(MessageTypes.NEW, "SERVER: Correct! Word was: "+correctWord+ ". New game?");
				    break;
				case FRAGMENT:
				    sendMessage(MessageTypes.STATUS, statusString());
				    break;
				case FAILED:
				    if(updateRemainingAttempts()){
					sendMessage(MessageTypes.STATUS, statusString());
				    } else {
					updateScore(false);
					sendMessage(MessageTypes.NEW, "SERVER: No more attempts, correct word was: "+correctWord+".", "Try with a new word?");
				    }
				    break;
				case PREVIOUS:
				    sendMessage(MessageTypes.STATUS, "SERVER: Already guessed character", statusString());
				    break;
			    }
			
			} catch(ArrayIndexOutOfBoundsException e) {
				sendMessage(MessageTypes.STATUS, "SERVER: No guess, make a new one!", statusString());
			}
			break;
		    case END:
			closeConnection();
			break;
			   
		}
	    }
	    
	    System.out.println(name +" ended the session");
	
	} catch(IOException ioe) {
	    closeConnection();
	    System.err.println(ioe);
	}
	
    }
    
    /**
     * Creating game status string to send to client
     * 
     * @return a String with game info
     */
    private String statusString() {
	
	StringJoiner joiner = new StringJoiner(Constants.TCP_DELIMETER);
        joiner.add("Word: "+guessString);
        joiner.add("Remaining Attempts: "+remainingAttempts);
	joiner.add("Score: "+clientScore);
        return joiner.toString();
    }
 
    /**
     * Starting of a game
     * 
     * @throws IOException when ReservoirSampling cant read the word file
     */
    private void initGame() throws IOException {
	clientScore = 0;
	rs = new ReservoirSampling(wordFile, SAMPLE_SIZE);
	wordList = rs.getSample();
	newGame();
    }
    
    /**
     * When the client wants to guess another word
     * 
     * @throws IOException when ReservoirSampling cant read the word file
     */
    private void newGame() throws IOException {
	setNewWord(getNewWord());
	setRemainingAttempts();
	initGuessString();
	guessedCharacters.clear();
    } 
    
    /**
     * Get a new word from the sampling
     * 
     * @return the new word to guess
     * @throws IOException if the ResevoirSampling class could not read the file
     */
    private String getNewWord() throws IOException {

	if(!wordList.isEmpty()) {
	
	    String newWord = wordList.get(0);
	    wordList.remove(0);
	
	    return newWord.toLowerCase();
	
	} else {
	    
	    wordList = rs.getSample();
	    String newWord = wordList.get(0);
	    wordList.remove(0);
	
	    return newWord.toLowerCase();
	}
	
    }
    
    /**
     * Stores the new word in a String variable
     * 
     * @param newWord - to be used as the word to guess 
     */
    private void setNewWord(String newWord) {
	correctWord = newWord;
    }
    
    /**
     * Creates a new string with as many underscores 
     * as characters in the word to guess
     */
    private void initGuessString() {
	char[] chars = new char[correctWord.length()];
	Arrays.fill(chars, '_');
	guessString = new String(chars);
    }
    
    /**
     * Checks the guess to the correct word.
     * 
     * @param clientGuess - guess from the client, one character or a whole word
     * @return enum WordStatus, COMPLETE for the whole word, 
     *				FRAGEMENT if a character is correct
     *				FAILED if wrongly guessed
     *				PREVIOUS if client has already guessed that char
     */
    private GuessStatus checkString(String clientGuess) {
	
	if(clientGuess.length() == 1) {
	    
	    char guess = clientGuess.charAt(0);	    
	    if( !(guessedCharacters.contains(guess)) ) {
		
		guessedCharacters.add(guess);	
		boolean fragment = false;
		char[] temp = guessString.toCharArray();

		for(int i=0; i<correctWord.length(); i++) {
		    if(correctWord.charAt(i) == guess) {
			temp[i] = guess;
			fragment = true;
		    }
		}
		guessString = String.valueOf(temp);
		
		if(guessString.equalsIgnoreCase(correctWord)) 
		    return GuessStatus.COMPLETE;
		else if(fragment)
		    return GuessStatus.FRAGMENT;
	    } else {
		return GuessStatus.PREVIOUS;
	    }
	    
	} else if (clientGuess.length() == correctWord.length() && clientGuess.equalsIgnoreCase(correctWord)) {
	    return GuessStatus.COMPLETE;
	} 
	
	return GuessStatus.FAILED;
    }
    
    /**
     * Updating the game score
     * 
     * @param increase - true to increase the score, i.e. when completing a game
     *			 else decrease when losing a game
     */
    private void updateScore(boolean increase) {
	if (increase) {
	    clientScore++;
	} else {
	    clientScore--;
	}
    }
    
    /**
     * Sets number of attempts to number of characters in word to guess
     */
    private void setRemainingAttempts() {
	remainingAttempts = correctWord.length();
    }
    
    /**
     * Decreasing number of attempts a client has by one when guessing wrong
     * 
     * @return - true for values different (higher) from zero.
     *		 false when zero, i.e. out of attempts
     */
    private boolean updateRemainingAttempts() {
	return --remainingAttempts != 0;
    }

    /**
     * Send messages to client
     * 
     * @param mt - enum MessageTypes, for specifying different types of communication
     * @param messages - messages to be sent
     */
    private void sendMessage(MessageTypes mt, String... messages) {
	StringJoiner joiner = new StringJoiner(Constants.TCP_DELIMETER);
        joiner.add(mt.toString());
        for(String message : messages) {
	   joiner.add(message); 
	}
	toClient.println(joiner.toString());
	toClient.flush();
    }
    
    /**
     * Close connection
     */
    private void closeConnection() {
	try {
	    clientSocket.close();
	    clientSocket = null;
	} catch(IOException ioe) {
	    System.out.println(ioe);
	}
	clientPlaying = false;
			
    }
}
