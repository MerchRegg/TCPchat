package mystuff.tcpchat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServer {
    private String serverMessage;
    public static final String SERVERIP = NetworkUtils.getIPAddress(true);
    public static int SERVERPORT;
    private MessageReceivedListener mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPServer(MessageReceivedListener listener) {
        mMessageListener = listener;
    }

    public void setServerPort(int serverPort){
        SERVERPORT = serverPort;
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

    public void stopServer(){
        mRun = false;
        out.flush();
        out.close();
    }

    public void run() {

        mRun = true;

        try {

            Log.e("TCP Server", "S: Connecting... IP:"+SERVERIP+" PORT:"+SERVERPORT);

            //create a socket to make the connection with the server
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            //accept a new tcp connection
            Socket socket = serverSocket.accept();

            Log.e("TCP Server", "S: Connected");

            try {
                //output to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Server", "S: Sent.");

                Log.e("TCP Server", "S: Done.");

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

            Log.e("TCP", "S: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface MessageReceivedListener {
        void messageReceived(String message);
    }
}
