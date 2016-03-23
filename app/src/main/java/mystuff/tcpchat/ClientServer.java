package mystuff.tcpchat;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ClientServer extends Activity {
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;
    private TCPServer mTcpServer;
    private int serverPort = 5555;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        arrayList = new ArrayList<>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button sendBtn = (Button)findViewById(R.id.send_button);

        //set the adapter for the list
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

        //start server
        new serverTask().execute("");
        //connect to server
        new clientTask().execute("");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    public class clientTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String[] message) {

            mTcpClient = new TCPClient(new TCPClient.MessageReceivedListener() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            /**
            mTcpClient.setServer(mTcpServer.SERVERIP, serverPort);
            mTcpClient.run();
             */

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }

    public class serverTask extends AsyncTask<String,String,TCPServer> {

        @Override
        protected TCPServer doInBackground(String[] message) {

            mTcpServer = new TCPServer(new TCPServer.MessageReceivedListener() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpServer.setServerPort(serverPort);
            mTcpServer.run();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTcpClient.setServer("", serverPort);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }
}
