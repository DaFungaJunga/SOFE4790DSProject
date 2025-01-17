import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextArea;

import org.apache.commons.codec.binary.Base64;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Pub { 
	/*	
	 *   topics: 0:topic name, 1: subscribers count, 2: current publication acknowledgement count, 
	 *   3: Total Deliveries,  4:key
	 */
	ArrayList<ArrayList<String>> topics = new ArrayList<ArrayList<String>>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionPub=null;
	ZMQ.Socket connectionRep=null;
	JTextArea textAreaPub;
	//ZContext context = new ZContext();
	/**
	 * Set host address for socket
	 * 
	 * @param h the host address
	 */
	public void setHost(String h){
		host = h;
	}
	/**
	 * Set the port address for socket
	 * 
	 * @param p the port address
	 */
	public void setPort(String p){
		port = p;
	}
	/**
	 * Create the publisher connection with the given context
	 * 
	 * For testing purposes, host = * and port = 5556
	 * 
	 * @param pub the topic user is publishing to
	 * @param context the ZeroMQ context for a process
	 * @return the connection, null if connection could not be established with the given info
	 */
	public ZMQ.Socket getConnectionPub(String pub,ZContext context){
		try {
			connectionPub = context.createSocket(SocketType.PUB);
           // connectionPub.connect("tcp://"+host+":"+port);
            connectionPub.connect("tcp://*:5556");
            String ipc = "ipc://test";
            //connectionPub.bind("ipc://"+pub);
            connectionPub.bind(ipc);
			return connectionPub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Create the Reply connection with a given context
	 * @param context the ZeroMQ context for a process
	 * @return the connection, null if connection could not be established with the given info
	 */
	public ZMQ.Socket getConnectionRep(ZContext context){
		try{
			connectionPub = context.createSocket(SocketType.REP);
            connectionPub.connect("tcp://*:5555");
			return connectionPub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Method to periodically sent messages to subscribers on a given topic
	 * This method is the regular implementation of the Pub-Sub Arch
	 * 
	 * For testing purposes, host = * and port = 5556
	 * @param pub the topic to be published to
	 * @param info the message that will be published to a topic
	 */
	public void publish(String pub,String info){
		try{
		ZContext context = new ZContext();
		if (connectionPub==null){
			//connectionPub = getConnectionPub(pub,context);
			connectionPub = context.createSocket(SocketType.PUB);
	        // connectionPub.connect("tcp://"+host+":"+port);
	        connectionPub.bind("tcp://*:5556");
	        String ipc = "ipc://test";
	        //connectionPub.bind("ipc://"+pub);
	        connectionPub.bind(ipc);
		}
        while (!Thread.currentThread().isInterrupted()) {
    		long startTime = System.nanoTime();
    		String update = String.format(
                    "%s %s", pub, info
                );
	        connectionPub.send(update, 0);
	        long endTime = System.nanoTime();
	        System.out.println("Publisher Execution time: " + (endTime - startTime) + " nanoseconds");
	        
			Thread.sleep(1000);
        }
        context.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * This method uses encryption on the topic and message being sent.
	 * Key is generated based on topic
	 * A thread is created to recieve messages from the subscribers
	 * 
	 * @param pub the topic to be published to
	 * @param info the message that will be published to a top
	 */
	public void publishEncrypted(String pub,String info){
		try{
		ZContext contextp = new ZContext();
		System.out.println("1");
		if (connectionPub==null){
			//connectionPub = getConnectionPub(pub,contextp);
			connectionPub = contextp.createSocket(SocketType.PUB);
	        // connectionPub.connect("tcp://"+host+":"+port);
	        connectionPub.bind("tcp://*:5556");
	        String ipc = "ipc://test";
	        //connectionPub.bind("ipc://"+pub);
	        connectionPub.bind(ipc);
		}

		String key = applySha256(pub);
		String encryptedInfo = new String(encrypt(key,info));
		String test = decrypt(encryptedInfo.getBytes(),key);
		String encryptedPub = new String(encrypt(key,pub));
		System.out.println(encrypt(key,pub).length);
        addTopic(pub,contextp,key);
        System.out.println(key);
		new Thread(new Runnable() {
		    @Override public void run() {
		    	recieveMessage(pub);		        
		    }
		}).start();

		while (!Thread.currentThread().isInterrupted()) {
			long startTime = System.nanoTime();
			String update = String.format(
                    "%s %s", encryptedPub, encryptedInfo
                );
			//System.out.println("4");

	        connectionPub.send(update, 0);
	        System.out.println(update);

	        long endTime = System.nanoTime();
	        //recieveMessage(pub,contextr);
			System.out.println("Encryption Publisher Execution Time: " + (endTime - startTime) + " nanoseconds");
			Thread.sleep(1000);
		}
		contextp.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * This methods adds a given topic to an array 
	 * if there is not an existing entry, method will create a new one
	 * topics array keeps track of meaningful information on publications
	 * @param pub the topic user is publishing to
	 * @param context the ZeroMQ context for a process
	 * @param key the encryption key
	 */
	public void addTopic(String pub,ZContext context,String key){
		for(int i=0;i<topics.size();i++){
    		if(topics.get(i).get(0).equals(pub)){
    			topics.get(i).set(2,String.valueOf(0));
    			return;
    		}
    	}
		ArrayList<String> topic = new ArrayList<String>();
		topic.add(pub); //topic name
		topic.add("0"); //subscriber count
		topic.add("0");//current publication acknowledgement count
		topic.add("0");//total deliveries
		topic.add(key);;//key
		
		topics.add(topic);
	}
	/**
	 * This method applies Secure Hash Algorithm to a given string
	 * @param input a given string to be hashed
	 * @return hashed string
	 */
	public static String applySha256(String input){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            //for (int i = 0; i < hash.length; i++) {
            for (int i = 0; i < 16; i++) {

                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
	/**
	 * This method encrypts a given string based on a given key
	 * @param key the encryption key
	 * @param text the string to be encrypted
	 * @return a encrypted byte array
	 */
	public byte[] encrypt(String key, String text) {
		byte[] encrypted = null;
		byte[] encryptedByteValue = null;
		try {
			
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        encrypted = cipher.doFinal(text.getBytes());
        encryptedByteValue = new Base64().encode(encrypted);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return encryptedByteValue;
	}
	/**
	 * This method decrypts a given string based on a given key
	 * @param encrypted a byte array that is to be decrypted
	 * @param key the encryption key
	 * @return a ddecrypted string
	 */
	public String decrypt(byte[]encrypted,String key) {
		String decrypted = null;
		try {
			byte[] decodedValue=new Base64().decode(encrypted);
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);
        decrypted = new String(cipher.doFinal(decodedValue));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return decrypted;
	}
	/**
	 * This method waits to recieve a message from a subscriber and handles it accordingly
	 * It can recieve get,ack, add, and remove
	 * 
	 * @param pub the topic that is being published to
	 */
	public void recieveMessage(String pub){
		/*	
		 *   topics: 0:topic name, 1: subscribers count, 2: current publication acknowledgement count, 
		 *   3: Total Deliveries,  4:key
		 */
		try{
			ZContext contextr = new ZContext();

			//connectionRep = getConnectionPub(pub,contextr);
			connectionRep = contextr.createSocket(SocketType.REP);
	        connectionRep.bind("tcp://*:5555");
			while (!Thread.currentThread().isInterrupted()) {
				System.out.println("waiting to recieve message");
	            byte[] byteString = connectionRep.recv(0);
				long startTime = System.nanoTime();
	            String string = new String(byteString);
	            System.out.println("received String:"+string);
	            StringTokenizer sscanf = new StringTokenizer(string, " ");
	            String str = sscanf.nextToken();
	            System.out.println("received Str:"+str);

	            if (str.equals("GET")){
	            	System.out.println("Recieved GET");
	            	String sub = sscanf.nextToken();
	            	System.out.println("received Str:"+sub);
	            	String response = "NULL";
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateReadCount = Integer.valueOf(topics.get(i).get(2))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;
	            			int updateSubscriberCount = Integer.valueOf(topics.get(i).get(1))+1;
	            			
	            			topics.get(i).set(2,String.valueOf(updateReadCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	            			topics.get(i).set(1,String.valueOf(updateSubscriberCount));
	            			
	            			String key = topics.get(i).get(4);
	            			
	            			response = key;
	            		}
	            	}
	            	connectionRep.send(response.getBytes(ZMQ.CHARSET), 0);
	            	System.out.println("Sending key "+response);
	            }
	            if (str.equals("ACK")){
	            	System.out.println("Recieved ACK");
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateReadCount = Integer.valueOf(topics.get(i).get(2))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;

	            			topics.get(i).set(2,String.valueOf(updateReadCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	            			/*	
	            			 *   topics: 0:topic name, 1: subscribers count, 2: current publication acknowledgement count, 
	            			 *   3: Total Deliveries,  4:key
	            			 */
	            			System.out.println("Topic Name: "+topics.get(i).get(0)+"\n");
	            			System.out.println("Subscriber Count: "+topics.get(i).get(1)+"\n");
	            			System.out.println("Current Publication Acknowledgement Count: "+topics.get(i).get(2)+"\n");
	            			System.out.println("Total Deliveries"+topics.get(i).get(3)+"\n");
	            			System.out.println("Key"+topics.get(i).get(4)+"\n");
	    	            	connectionRep.send("CON".getBytes(ZMQ.CHARSET), 0);
	            		}
	            	}
	            } 	
	            if(str.equals("ADD")){
	            	System.out.println("Recieved ADD");
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateCount = Integer.valueOf(topics.get(i).get(1))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;

	            			topics.get(i).set(2,String.valueOf(updateCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	            		}
	            	}
	            }
	            if(str.equals("REMOVE")){
	            	System.out.println("Recieved ADD");
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateCount = Integer.valueOf(topics.get(i).get(1))-1;
	            			topics.get(i).set(2,String.valueOf(updateCount));
	            		}
	            	}
	            }
		        long endTime = System.nanoTime();
	            System.out.println("Received " + string);
				System.out.println("Message Processing Time Execution Time: " + (endTime - startTime) + " nanoseconds");
	            Thread.sleep(1000); //  Do some 'work'
	        }
			contextr.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
