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
    private int port = 6789;
    private String senderName = "unknown";
    private String receiverName = "unknown";

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();


                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String received = "";
                Intent intentToSend = new Intent();
                while((received = in.readLine()) != null){
                    System.out.println(received);
                    Log.d(TAG, "received a message!");
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
