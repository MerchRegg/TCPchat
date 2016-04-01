package mystuff.tcpchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import mystuff.tcpchat.database.ChatMessage;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ServerService extends IntentService {
    protected static final String BROADCAST = "ServerServiceBroadcast";
    private static final String TAG = "serverservice";
    private int port = 5678;
    private String ip = NetworkUtils.getIPAddress(true);
    private String myName;
    private String clientName = "unknown";

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        port = intent.getIntExtra("port", port);
        myName = intent.getStringExtra("myname");
        String received = "";
        Log.d(TAG, "Created server socket at " + ip + ":" + port);

        Intent intentToSend = new Intent();
        intentToSend.setAction(BROADCAST);

        if (intent != null) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                //notify the main activity
                sendBroadcast(intentToSend.putExtra("ServerStarted", true));
                Log.d(TAG, "Notified start to main activity");

                Socket socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                //output to the client
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
                //input from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                //Exchange names
                out.println("mynameis");
                out.println(myName);
                Log.d(TAG, "Trying to get client name..");
                //Try to get server name, it should be the first String sent
                if((received = in.readLine()).equals("mynameis")){
                    Log.d(TAG, "First message received should be the name..");
                    clientName = in.readLine();
                    Log.d(TAG, "clientName found: " + clientName);
                }
                else{
                    Log.d(TAG, "First message wasn't server name..");
                    broadcastMessage(intentToSend, received);
                }


                /*
                //Try to get server name, it should be the first object sent
                ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                Object firstReceived = objectIn.readObject();
                clientName = ((Bundle) firstReceived).getString("myname");
                if(clientName != null){
                    Log.d(TAG, "First received was name! " + clientName);
                }
                else{
                    Log.d(TAG, "First received wasn't name.. " + clientName);
                    clientName = "unknown";
                    broadcastMessage(intentToSend, (String) firstReceived);
                }
                objectIn.close();
                */


                while((received = in.readLine()) != null){
                    Log.d(TAG, "received a message!");
                    broadcastMessage(intentToSend, received);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(Intent intentToSend, String text){
        intentToSend = new Intent();
        intentToSend.setAction(BROADCAST);
        intentToSend.putExtra("text", text);
        intentToSend.putExtra("sender", clientName);
        intentToSend.putExtra("receiver", myName);
        intentToSend.putExtra("date", new Date().toString());
        sendBroadcast(intentToSend);
    }
}
