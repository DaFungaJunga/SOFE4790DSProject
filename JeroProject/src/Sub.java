import java.security.Key;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextArea;

import org.apache.commons.codec.binary.Base64;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Sub {
	ArrayList<String> subscriptions = new ArrayList<String>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionSub=null;
	ZMQ.Socket connectionReq=null;
	JTextArea textAreaSub;
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
	 * Create the subscriber connection with the given context
	 * 
	 * For testing purposes, host = localhost and port = 5556
	 * @param context the ZeroMQ context for a process
	 * @return the connection, null if connection could not be established with the given info
	 */
	public ZMQ.Socket getConnectionSub(ZContext context){
		if(host==null){
			host = "localhost";
		}
		if(port ==null){
			port ="5556";
		}
		try{
			connectionSub = context.createSocket(SocketType.SUB);
            connectionSub.connect("tcp://localhost:5556");
			return connectionSub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Create the request connection with the given context
	 * 
	 * For testing purposes, host = localhost and port = 5556
	 * @param context the ZeroMQ context for a process
	 * @return the connection, null if connection could not be established with the given info
	 */
	public ZMQ.Socket getConnectionReq(ZContext context){
		if(host==null){
			host = "localhost";
		}
		if(port ==null){
			port ="5555";
		}
		try {
			connectionReq = context.createSocket(SocketType.REQ);
            connectionReq.connect("tcp://localhost:5555");
			return connectionReq;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * This method waits to receive a message from a publisher that they subscribe to 
	 * 
	 * @param sub the topic they would like to subscribe to 
	 */
	public void getSubscription(String sub){
		try{
			ZContext context = new ZContext();
			if (connectionSub==null){
				connectionSub = getConnectionSub(context);
				
			}
			connectionSub.subscribe(sub.getBytes(ZMQ.CHARSET));
            while (!Thread.currentThread ().isInterrupted ()) {
    			long startTime = System.nanoTime();
            	String string = connectionSub.recvStr(0).trim();
            	System.out.println("received String:"+string);
    			StringTokenizer sscanf = new StringTokenizer(string, " ");
    			while (sscanf.hasMoreTokens()) {
    				System.out.println(sscanf.nextToken(", "));
    			}
			long endTime = System.nanoTime();
			System.out.println("Subscriber Execution time: " + (endTime - startTime) + " nanoseconds");
            }
			}catch(Exception e){
				e.printStackTrace();
		}
	}
	/**
	 * This method waits to receive the key in order to subscribe to a published topic
	 * When it receives a message, it sends an ACK to the Publisher
	 * 
	 * @param sub the topic to be subscribed to
	 */
	public void getSubscriptionEncrypted(String sub){
		try{
			ZContext contexts = new ZContext();
			ZContext contextr = new ZContext();
			//connectionSub = getConnectionSub(contexts);
			connectionSub = contexts.createSocket(SocketType.SUB);
	        connectionSub.connect("tcp://localhost:5556");
			//connectionReq = getConnectionReq(context
			connectionReq = contextr.createSocket(SocketType.REQ);
            connectionReq.connect("tcp://localhost:5555");
			String request = "GET ".concat(sub);
			connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);

			String key = connectionReq.recvStr(0).trim();
			String encryptedSub = new String(encrypt(key,sub));
			connectionSub.subscribe(encryptedSub.getBytes(ZMQ.CHARSET));
			System.out.println("Encryption Key: "+key);

			 while (!Thread.currentThread ().isInterrupted ()){
					long startTime = System.nanoTime();
					byte[] stringBytes = connectionSub.recv(0);
					//System.out.println(stringBytes);
					String string = new String(stringBytes);
					//System.out.println(string);
					//System.out.println(stringBytes.toString());

					String[] arr = string.split(" ", 2);
					String encryptedPub = arr[0].trim();
					String encryptedInfo = arr[1].trim();
					System.out.println(encryptedPub);
					//System.out.println("Before trim"+encryptedInfo);

				    //encryptedInfo = encryptedInfo.replaceAll("^\\s+", "");

					//System.out.println("After trim"+encryptedInfo);

						//String decryptedString = decrypt(keyBytes,key);
						//System.out.println("Decrypted String: "+decryptedString);
						//byte[] token = sscanf.nextToken().getBytes();
						//System.out.println("token:"+token);
					System.out.println(encryptedInfo.getBytes().length);
					String decryptedString = decrypt(encryptedInfo.getBytes(),key);
					System.out.println("Decrypted String: "+decryptedString);
						
			            //System.out.println(sscanf.nextToken(", "));
			     
					String ack = "ACK ".concat(sub);
		
					connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
					System.out.println(connectionReq.recv(0));		
					long endTime = System.nanoTime();
					System.out.println("Encrypted Subscriber Execution Time: " + (endTime - startTime) + " nanoseconds");
			 	}
				contexts.close();
				contextr.close();
			}catch(Exception e) {
			 e.printStackTrace();
		 }	
	}
	/**
	 * This method adds the subscriber to the publisher's subscriber list
	 * 
	 * @param sub the topic to be subscribed to
	 * @param context the ZeroMQ context for a process
	 */
	public void addSubscription(String sub,ZContext context){
		subscriptions.add(sub);
		String request = "ADD ".concat(sub);
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
	/**
	 * This method removes the subscriber to the publisher's subscriber list
	 * 
	 * @param sub the topic to be subscribed to
	 * @param context the ZeroMQ context for a pro
	 */
	public void removeSubscription(String sub,ZContext context){
		subscriptions.remove(sub);
		String request = "REMOVE ".concat(sub);
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
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

}
