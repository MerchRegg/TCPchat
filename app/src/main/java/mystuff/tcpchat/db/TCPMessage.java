package mystuff.tcpchat.db;

/**
 * Created by marco on 25/03/16.
 */
public class TCPMessage {
    private long id;
    private String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return "<" + id + ", " + text + ">";
    }
}
