package mystuff.tcpchat.database;


public class ChatMessage {
    private String text;
    private String date;
    private String sender;
    private String receiver;

    ChatMessage(String text, String sender, String receiver, String date){
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
    }

    String getText(){
        return this.text;
    }

    String getDate(){
        return this.date;
    }

    String getSender(){
        return this.sender;
    }

    String getReceiver(){
        return this.receiver;
    }
}
