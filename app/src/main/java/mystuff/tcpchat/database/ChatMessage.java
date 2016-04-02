package mystuff.tcpchat.database;

/**
 * Defines the object used in this app as message.
 */
public class ChatMessage {
    private int id;
    private String text;
    private String date;
    private String sender;
    private String receiver;

    /**
     * Creates a new instance of ChatMessage class with specified data
     * @param id int, the id used in the database if the message is read from it.
     * @param text String, the real text of the message
     * @param sender String, the sender of the message
     * @param receiver String, the receiver of the message
     * @param date String, the time when the message was sent
     */
    public ChatMessage(int id, String text, String sender, String receiver, String date){
        this.id = id;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
    }

    public String getText(){
        return this.text;
    }

    public String getDate(){
        return this.date;
    }

    public String getSender(){
        return this.sender;
    }

    public String getReceiver(){
        return this.receiver;
    }

    @Override
    public String toString(){
        return "<ID:'" + id + "' text:'" + text + "' sender:'" + sender + "' receiver:'" + receiver + "' date:'" + date + "'>";
    }

    /**
     * Used instead of toString() to visualize better the message, displays only the text and the date
     * @return String, a two lines String of the ChatMessage
     */
    public String display(){
        return sender + ": " + text + "\n\n\t ("+date+")";
    }
}
