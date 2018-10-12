package uoit.ca.dsproject;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SelectOtherBlockChain extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = Client.class.getSimpleName();

    public static final int SERVERPORT = 3000;

    public static String SERVER_IP = "YOUR_SERVER_IP";
    ClientThread clientThread;
    Thread thread;
    TextView messageTv;
    EditText editHash;
    EditText editIP;
    Spinner spinner;
    EditText taskData;
    public static User clientTaskChains;
    public static ArrayList<String> savedHashes;
    public static void writeJson( BlockChain serverTaskchain){
        try(Writer writer = new FileWriter("clientTaskchains")){

            Gson gson = new GsonBuilder().create();
            gson.toJson(serverTaskchain,writer);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void readJson(){
        try(Reader reader = new FileReader("clientTaskchains")){
            Gson gson = new GsonBuilder().create();
            clientTaskChains =gson.fromJson(reader,User.class);
        }catch (Exception e) {
            clientTaskChains = new User();
            e.printStackTrace();
        }
    }
    public static void getAllHashes(){
        clientTaskChains.getSavedHashes();
        savedHashes = clientTaskChains.;
    }
    //need to restrict the saving of all server block chains, it is okay to send it all
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageTv = findViewById(R.id.messageTv);
        editHash = findViewById(R.id.editHash);
        editIP = findViewById(R.id.editIP);
        spinner = findViewById(R.id.spinner);
        taskData = findViewById(R.id.taskData);

        readJson();
    }

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
        if (view.getId() == R.id.goToStartC) {
            Intent intentViewChain = new Intent(SelectOtherBlockChain.this,ViewChain.class);
            String ip = SelectOtherBlockChain.this.editIP.getText().toString();
            String hash = SelectOtherBlockChain.this.editHash.getText().toString();
            String task =SelectOtherBlockChain.this.taskData.getText().toString();
            String selectedHash =(String)spinner.getSelectedItem();
            //need populate hash spinner *************************************************************************
            SERVER_IP = ip;
            messageTv.setText("");
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            if(clientTaskChains.findBlockChain(hash)){
                clientTaskChains.startBlockChain(task);
                clientThread.sendMessage(new Gson().toJson(clientTaskChains));
                intentViewChain.putExtra("taskchains", (Parcelable) clientTaskChains);
                startActivity(intentViewChain);
            }
        }
        if (view.getId() == R.id.goToAddC) {
            Intent intentViewChain = new Intent(SelectOtherBlockChain.this,ViewChain.class);
            String ip = SelectOtherBlockChain.this.editIP.getText().toString();
            String hash = SelectOtherBlockChain.this.editHash.getText().toString();
            String task =SelectOtherBlockChain.this.taskData.getText().toString();
            SERVER_IP = ip;
            messageTv.setText("");
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            BlockChain bc = clientTaskChains.getBlockChain(hash);
            if(bc !=null) {
                clientTaskChains.addToBlockChain(task,bc);
                clientThread.sendMessage(new Gson().toJson(clientTaskChains));
                intentViewChain.putExtra("taskchains", (Parcelable) clientTaskChains);
                startActivity(intentViewChain);
            }
        }
        if (view.getId() == R.id.goToClient) {
            finish();
        }
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    Log.i(TAG, "Waiting for message from server...");

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    Log.i(TAG, "Message received from the server : " + message);

                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        updateMessage(getTime() + " | Server : " + message);
                        break;
                    }

                    updateMessage(getTime() + " | Server : " + message);
                    clientTaskChains = new Gson().fromJson(message,User.class);

                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(String message) {
            try {
                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }
}

