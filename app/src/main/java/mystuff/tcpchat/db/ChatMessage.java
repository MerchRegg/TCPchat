package mystuff.tcpchat.db;


import java.util.Date;

/**
 * Created by marco on 23/03/16.
 */
public class ChatMessage {
    private static int id = 0;
    private int _id;
    private String _text;
    private String _sender;
    private String _receiver;
    private String _time;

    public ChatMessage(){
        _id = -1;
        _text = "DEFAULT_MESSAGE";
        _sender = "SENDER";
        _receiver = "RECEIVER";
        _time = new Date().toString();
    };

    public ChatMessage(String text, String sender, String receiver, String time){
        this._id = -1;
        this._text = text;
        this._sender = sender;
        this._receiver = receiver;
        this._time = time;
    }
    public ChatMessage(int id, String text, String sender, String receiver, String time){
        this._id = id;
        this._text = text;
        this._sender = sender;
        this._receiver = receiver;
        this._time = time;
    }

    public String get_time() {
        return _time;
    }

    public int get_id() {
        return _id;
    }

    public String get_receiver() {
        return _receiver;
    }

    public String get_sender() {
        return _sender;
    }

    public String get_text() {
        return _text;
    }

    @Override
    public String toString() {
        return "<ID:'"+_id+"' text:'"+_text+"' sender:'"+_sender+"' receiver:'"+_receiver+"' date:'"+_time+"'>";
    }
}
