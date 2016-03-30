package mystuff.tcpchat.database;


public class ChatMessage {
    private int id;
    private String text;
    private String date;
    private String sender;
    private String receiver;

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

    public String toString(){
        return "<ID:'" + id + "' text:'" + text + "' sender:'" + sender + "' receiver:'" + receiver + "' date:'" + date + "'>";
    }

    public String display(){
        return sender + ": " + text + "\n\n\t ("+date+")";
    }
}
