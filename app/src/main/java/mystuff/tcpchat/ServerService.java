package mystuff.tcpchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ServerService extends IntentService {
    protected static final String BROADCAST = "ServerServiceBroadcast";
    private static final String TAG = "serverservice";
    private int port = 5678;
    private String ip = NetworkUtils.getIPAddress(true);
    private String senderName = "unknown";
    private String receiverName = "unknown";

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        port = intent.getIntExtra("port", port);
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

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String received = "";
                while((received = in.readLine()) != null){
                    System.out.println(received);
                    Log.d(TAG, "received a message!");
                    intentToSend = new Intent();
                    intentToSend.setAction(BROADCAST);
                    intentToSend.putExtra("text", received);
                    intentToSend.putExtra("sender", senderName);
                    intentToSend.putExtra("receiver", receiverName);
                    intentToSend.putExtra("date", new Date().toString());
                    sendBroadcast(intentToSend);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
