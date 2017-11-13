/**
 *
 * @author Tobias Mellstrand
 * @date 2017-11-09
 */

package hangmangame;

import java.net.*;
import java.io.*;
import java.util.List;


public class HangmanServer {
    
    private static final int SERVER_PORT = 5000;
    
    public HangmanServer() {
	try {
	    ServerSocket sock = new ServerSocket(SERVER_PORT,100);
	    while (true)
		new ClientHandler(sock.accept()).start();

	} catch(IOException e) {
	    System.err.println(e);
	}
	
    }
    
    public void main(String[] args) {
	new HangmanServer();
    }
}


class ClientHandler extends Thread {
    
    Socket clientSocket;
    BufferedReader fromClient;
    PrintWriter toClient;
    ReservoirSampling rs;
    File wordFile;
    List<String> wordList;
    String correctWord;
    String guessString;
    int remainingAttempts;
    int score;
    
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
    
    public void run() {
	
	try {
	    String namn = fromClient.readLine();
	    System.out.println(namn);
	    toClient.println("Hello, "+namn);
	    toClient.flush();

	    while(true) {
		String indata = fromClient.readLine();
		if(indata==null || indata.equals("")) break;
		String[] requestToken = indata.split(" ");
		if(requestToken[0].equals("INIT")) {
		    initGame();
		} else if(requestToken[0].equals("NEW")) {
		    newGame();
		} else if(requestToken[0].equals("GUESS")) {
		    if(checkString(requestToken[1])) {
			updateScore(true);
			sendMessage("Game complete, new game?");
		    } else {
			updateRemainingAttempts();
			// SEND GAME STATUS MESSAGE
		    }
		} else if(requestToken[0].equals("END")) {
		    clientSocket.close();
		}
	    }
	    
	    System.out.println("Client " +namn +" ended the session");
	
	} catch(Exception e) {
	    System.err.println(e);
	}
	
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
	StringBuilder temp = new StringBuilder(correctWord.length()).append('_');
	guessString = temp.toString();
	//guessString = new String(new char[correctWord.length()]).replace('\0', '-');
    }
    
    private boolean checkString(String clientGuess) {
	
	if(correctWord.contains(clientGuess)) {
	
	    if(clientGuess.length() == 1) {

		char[] temp = guessString.toCharArray();

		char guess = clientGuess.charAt(0);
		for(int i=0; i<correctWord.length(); i++) {
		    if(correctWord.charAt(i) == guess) {
			temp[i] = guess;
		    }
		}

		guessString = String.valueOf(temp);

		return guessString.equalsIgnoreCase(correctWord);

	    } else if (clientGuess.length() == correctWord.length()) {

		return clientGuess.equalsIgnoreCase(correctWord);

	    }
	    
	}
	
	return false;	
    }
    
    private void updateScore(boolean in) {
	if (in == true) {
	    score++;
	} else {
	    score--;
	}
    }
    
    private void setRemainingAttempts() {
	remainingAttempts = correctWord.length();
    }
    
    private void updateRemainingAttempts() {
	remainingAttempts--;
    }

    private void newGame() throws IOException {
	setNewWord(getNewWord());
	setRemainingAttempts();
	initGuessString();
    }
    
    private void initGame() throws IOException {
	score = 0;
	rs = new ReservoirSampling(wordFile);
	wordList = rs.getSample();
	newGame();
	/*
	setNewWord(getNewWord());
	setRemainingAttempts();
	initGuessString();
	*/
    }
    
    private void sendMessage(String message) {
	toClient.println(message);
	toClient.flush();
    }
}
