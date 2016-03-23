package mystuff.tcpchat;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A class that creates a tcp socket to the desired ip and port and communicates through it with String messages.
 */
public class TCPClient {

    private String serverMessage;
    //public static String SERVERIP = "192.168.0.101"; //your computer IP address
    public static String SERVERIP = "172.16.147.144";
    public static int SERVERPORT = 4444;
    private MessageReceivedListener mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(MessageReceivedListener listener) {
        mMessageListener = listener;
    }

    public void setServer(String ip, int port){
        if(!ip.equals(""))
            SERVERIP = ip;
        SERVERPORT = port;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Closes the output stream and stops the listening on the socket
     */
    public void stopClient(){
        mRun = false;
        out.flush();
        out.close();
    }

    public void run() {

        mRun = true;

        try {
            //server's IP
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting... SERVERIP:"+SERVERIP+" PORT:"+SERVERPORT);

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);

            Log.e("TCP Client", "C: Connected! SERVERIP:"+SERVERIP+" PORT:"+SERVERPORT);


            try {

                //output to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //input from server
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //listen for response from server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived("S:"+serverMessage);
                    }
                    serverMessage = null;
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface MessageReceivedListener {
        void messageReceived(String message);
    }
}
