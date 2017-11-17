/**
 *
 * @author mellstrand
 * @date 2017-11-16
 */
package hangman.server;

/**
 * Defines different states when checking a guess
 */
public enum WordStatus {
    /**
     * When correct word has been guessed
     */
    COMPLETE,
    /**
     * When a character is correct, not the whole word is correct yet
     */
    FRAGMENT,
    /**
     * When guessed character is not amongst the characters of the correct word
     */
    FAILED
    
}
