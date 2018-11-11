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
	/*
	 * use add/remove sub
	 */
	ArrayList<String> subscriptions = new ArrayList<String>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionSub=null;
	ZMQ.Socket connectionReq=null;
	JTextArea textAreaSub;
	public void setHost(String h){
		host = h;
	}
	public void setPort(String p){
		port = p;
	}
	public void setSubTerminal(JTextArea terminal) {
		textAreaSub = terminal;
	}
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
            	textAreaSub.setText(textAreaSub.getText() +"received String:"+string);
    			StringTokenizer sscanf = new StringTokenizer(string, " ");
    			while (sscanf.hasMoreTokens()) {
    				textAreaSub.setText(textAreaSub.getText() +sscanf.nextToken(", "));
    			}
			long endTime = System.nanoTime();
			textAreaSub.setText(textAreaSub.getText() +"Execution time: " + (endTime - startTime) + " nanoseconds");
            }
			}catch(Exception e){
				e.printStackTrace();
		}
	}
	public void getSubscriptionEncrypted(String sub){
		try{
			ZContext contexts = new ZContext();
			ZContext contextr = new ZContext();
			long startTime = System.nanoTime();
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
			textAreaSub.setText(textAreaSub.getText() +"Encryption Key: "+key);

			 while (!Thread.currentThread ().isInterrupted ()){
					byte[] stringBytes = connectionSub.recv(0);
					//System.out.println(stringBytes);
					String string = new String(stringBytes);
					//System.out.println(string);
					//System.out.println(stringBytes.toString());

					String[] arr = string.split(" ", 2);
					String encryptedPub = arr[0].trim();
					String encryptedInfo = arr[1].trim();
					textAreaSub.setText(textAreaSub.getText() +encryptedPub);
					//System.out.println("Before trim"+encryptedInfo);

				    //encryptedInfo = encryptedInfo.replaceAll("^\\s+", "");

					//System.out.println("After trim"+encryptedInfo);

						//String decryptedString = decrypt(keyBytes,key);
						//System.out.println("Decrypted String: "+decryptedString);
						//byte[] token = sscanf.nextToken().getBytes();
						//System.out.println("token:"+token);
					textAreaSub.setText(textAreaSub.getText() +encryptedInfo.getBytes().length);
					String decryptedString = decrypt(encryptedInfo.getBytes(),key);
					textAreaSub.setText(textAreaSub.getText() +"Decrypted String: "+decryptedString);
						
			            //System.out.println(sscanf.nextToken(", "));
			     
					String ack = "ACK ".concat(sub);
		
					connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
					textAreaSub.setText(textAreaSub.getText() +connectionReq.recv(0));		
					long endTime = System.nanoTime();
					textAreaSub.setText(textAreaSub.getText() +"Execution time: " + (endTime - startTime) + " nanoseconds");
			 	}
				contexts.close();
				contextr.close();
			}catch(Exception e) {
			 e.printStackTrace();
		 }	
	}
	
	public void addSubscription(String sub,ZContext context){
		subscriptions.add(sub);
		String request = "ADD ".concat(sub);
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
	public void removeSubscription(String sub,ZContext context){
		subscriptions.remove(sub);
		String request = "REMOVE ".concat(sub);
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
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
