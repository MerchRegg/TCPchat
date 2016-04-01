package mystuff.tcpchat;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
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
    protected static final String RECEIVED_CLIENT_NAME = "ReceivedClientName";
    protected static final String SERVER_STARTED = "ServerStarted";
    protected static final String CLIENT_NAME = "clientName";
    private static final String TAG = "serverservice";
    private int port = 5678;
    private String ip = NetworkUtils.getIPAddress(true);
    private String myName;
    private String clientName = "unknown";

    private MainActivityBroadcastReceiver broadcastReceiver;
    private PrintWriter out;
    private BufferedReader in;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        port = intent.getIntExtra("port", port);
        myName = intent.getStringExtra("myname");
        String received = "";

        Intent intentToSend = new Intent();
        intentToSend.setAction(BROADCAST);

        //create broadcast receiver
        broadcastReceiver = new MainActivityBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(MainActivity.BROADCAST));

        if (intent != null) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d(TAG, "Created server socket at " + ip + ":" + port);

                //notify the main activity
                sendBroadcast(intentToSend.putExtra(SERVER_STARTED, true));
                Log.d(TAG, "Notified start to main activity");

                Socket socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                //output to the client
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
                //input from the client
                in = new BufferedReader(new InputStreamReader(inputStream));

                //Exchange names
                out.println("mynameis");
                out.println(myName);
                Log.d(TAG, "Trying to get client name..");
                //Try to get server name, it should be the first String sent
                if((received = in.readLine()).equals("mynameis")){
                    Log.d(TAG, "First message received should be the name..");
                    clientName = in.readLine();
                    Log.d(TAG, "clientName found: " + clientName);
                    broadcastClientName(intentToSend, clientName);
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
                unregisterReceiver(broadcastReceiver);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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

    private void broadcastClientName(Intent intentToSend, String name){
        Log.d(TAG, "broadcasting client name: " + name);
        intentToSend = new Intent();
        intentToSend.setAction(BROADCAST);
        intentToSend.putExtra(RECEIVED_CLIENT_NAME, true);
        intentToSend.putExtra(CLIENT_NAME, clientName);
        sendBroadcast(intentToSend);
    }

    private class MainActivityBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received a broadcast message");
            String message = intent.getStringExtra(MainActivity.MESSAGE_TO_SEND);
            out.println(message);
            Log.d(TAG, "New message sent: " + message);
        }
    }
}
