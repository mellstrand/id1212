/*
 * @author mellstrand
 * @date 2017-11-14
 */

package hangman.common;

public enum MessageTypes {
    
    /*
     * To set up the game in the beginning
     */
    INIT,
    /*
     * To start a new game
     */
    NEW,
    /*
     * When guessing a letter or a word
     */
    GUESS,
    /*
     *
     */
    STATUS,
    /*
     * Closing the connection
     */
    END
    
}
