package mystuff.tcpchat;

/**
 * A custom Exception class
 */
public class StartClientException extends Exception {
    public StartClientException(String text){
        super(text);
    }
}
