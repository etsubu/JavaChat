package Server;

/**
 * InvalidChannelNameException is thrown when a Channel object was initializes with invalid name
 * 
 * @author etsubu
 * @version 19 Apr 2019
 *
 */
public class InvalidChannelNameException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Initializes InvalidChannelNameException
     */
    public InvalidChannelNameException() {
        super();
    }
    
    /**
     * Initializes InvalidChannelNameException
     * @param message Channel that was rejected
     */
    public InvalidChannelNameException(String message) {
        super(message);
    }

}
