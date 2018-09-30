package uoit.ca.dsproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public final int REQ_CODE = 1;

    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3000;
    TextView messageTv;
    InetAddress inetAddress;
    String ip;
    public static User serverTaskChains;

    {
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            ip = inetAddress.getHostAddress();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

  //use for viewChain
        messageTv = (TextView) findViewById(R.id.messageTv);
    }
//use for view Chain
    public void updateMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTv.append(message + "\n");
            }
        });
    }

    @Override
    public void onClick(View view) {
 //use for viewChain
        if (view.getId() == R.id.start_server) {
            Log.d(TAG, "Starting Server...");
            messageTv.setText("");
            updateMessage("Starting Server...");
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            return;
        }
        //use for viewChain
        /*
        if (view.getId() == R.id.send_data) {
            sendMessage("Hello from Server...");
        }*/
        if (view.getId() == R.id.send_data) {
            // Add to or start Taskchain
            Intent intentOwner = new Intent(MainActivity.this, SelectOwnerBlockChain.class);
            intentOwner.putExtra("taskchains", (Parcelable) serverTaskChains);
            startActivityForResult(intentOwner,REQ_CODE);
        }
        if (view.getId() == R.id.goToClient) {
            //go to ClientView
            Intent intentClient = new Intent(MainActivity.this,Client.class);
            startActivity(intentClient);

        }
    }

    private void sendMessage(String message) {
        try {
            if (null != tempClientSocket) {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(tempClientSocket.getOutputStream())),
                        true);
                out.println(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            serverTaskChains = data.getParcelableExtra("taskchains");
            sendMessage(new Gson().toJson(serverTaskChains));
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != serverSocket) {
                //may need to change
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateMessage("Server Started...");
            sendMessage(new Gson().toJson(serverTaskChains));

        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();
                    serverTaskChains = new Gson().fromJson(read,User.class);
                    Log.i(TAG, "Message Received from Client : " + read);

                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        updateMessage(getTime() + " | Client : " + read);
                        break;
                    }
                    updateMessage(getTime() + " | Client : " + read);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}

