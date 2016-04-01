package mystuff.tcpchat;

import android.os.Bundle;
import android.util.Log;
import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A class that creates a tcp socket to the desired ip and port and communicates through it with String messages.
 */
public class TCPClient {

    private final String TAG = "tcpclient";
    private String serverMessage;
    private String myName = "Client";
    private String serverName;
    public static String SERVERIP = "192.168.0.101"; //your computer IP address
    //public static String SERVERIP = "172.16.147.144";
    public static int SERVERPORT = 6789;
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

    public void setName(String name){
        this.myName = name;
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
            //InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.d(TAG, "Connecting... SERVERIP:"+SERVERIP+" PORT:"+SERVERPORT);

            //create a socket to make the connection with the server
            int attempt = 3;
            Socket socket = null;
            while(attempt > 0) {
                try {
                    socket = new Socket(SERVERIP, SERVERPORT);
                    attempt = -1;
                } catch (ConnectException ex) {
                    ex.printStackTrace();
                    attempt--;
                }
            }
            if(socket == null){
                throw new ConnectException("Failed all attempts to connect!");
            }

            Log.d(TAG, "Connected! SERVERIP:"+SERVERIP+" PORT:"+SERVERPORT);


            try {

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                //output to the client
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
                //input from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                //Exchange names
                out.println("mynameis");
                out.println(myName);
                Log.d(TAG, "Trying to get server name..");
                //Try to get server name, it should be the first String sent
                if((serverMessage = in.readLine()).equals("mynameis")){
                    Log.d(TAG, "First message received should be the name..");
                    serverName = in.readLine();
                    Log.d(TAG, "serverName found: " + serverName);
                }
                else{
                    Log.d(TAG, "First message wasn't server name..");
                    mMessageListener.messageReceived(serverMessage);
                }

                //listen for response from server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }

                Log.d(TAG, "Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.d(TAG, "Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e(TAG, "Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface MessageReceivedListener {
        void messageReceived(String message);
        void receivedServerName(String name);
    }
}
