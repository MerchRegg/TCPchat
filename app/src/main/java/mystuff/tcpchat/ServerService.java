package mystuff.tcpchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServerService extends IntentService {
    private static final String TAG = "serverservice";
    private int port = 6789;

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
                while((received = in.readLine()) != null){
                    System.out.println(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
