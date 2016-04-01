package mystuff.tcpchat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GetData extends Activity {

    private TextView myNameView;
    private TextView clientIpView;
    private TextView clientPortView;
    private TextView serverPortView;
    private Button okButton;

    private final String TAG = "getdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);

        Log.d(TAG, "Started getdata");

        myNameView = (TextView) findViewById(R.id.myNameField);
        clientIpView = (TextView) findViewById(R.id.clientIpField);
        clientPortView = (TextView) findViewById(R.id.clientPortField);
        serverPortView = (TextView) findViewById(R.id.serverPortField);
        okButton = (Button) findViewById(R.id.okButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData(getIntent());
            }
        });
        Log.d(TAG, "Initialized getdata");
    }

    private void fetchData(Intent intent){
        try{
            Log.d(TAG, "Fetching data");
            String myName = myNameView.getText().toString();
            String clientIP = clientIpView.getText().toString();
            int clientPort = Integer.parseInt(clientPortView.getText().toString());
            int serverPort = Integer.parseInt(serverPortView.getText().toString());
            //FOR TESTING ONLY
            myName = "Agilulfo";
            clientIP = NetworkUtils.getIPAddress(true);
            clientPort = 6789;
            serverPort = 6789;
            if(!myName.equals("") && !clientIP.equals("") && clientPort > 1024 && serverPort > 1024){
                intent.putExtra("myName", myName);
                intent.putExtra("clientIP", clientIP);
                intent.putExtra("clientPort", clientPort);
                intent.putExtra("serverPort", serverPort);
                setResult(MainActivity.DATAOK, intent);
                Log.d(TAG, "Data fetched succesfully: name(" + myName + ") client(" + clientIP + ":" + clientPort + ") server(:" + serverPort + ")");
                Log.d(TAG, "RESULT: " + MainActivity.DATAOK);
                finish();
            }
            else{
                throw new IllegalArgumentException();
            }
        }
        catch(IllegalArgumentException e){
            Toast toast = Toast.makeText(this, "ERROR: You inserted invalid data!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
