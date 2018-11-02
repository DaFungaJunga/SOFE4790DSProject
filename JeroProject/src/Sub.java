import java.security.Key;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

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
	public void setHost(String h){
		host = h;
	}
	public void setPort(String p){
		port = p;
	}
	public ZMQ.Socket getConnectionSub(ZContext context){
		if(host==null){
			host = "localhost";
		}
		if(port ==null){
			port ="5556";
		}
		try{
			System.out.println("beforec ");
			connectionSub = context.createSocket(SocketType.SUB);
            connectionSub.connect("tcp://localhost:5556");
    		System.out.println("after c");

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
    			System.out.println("received String:"+string);
    			StringTokenizer sscanf = new StringTokenizer(string, " ");
    			while (sscanf.hasMoreTokens()) {
    				System.out.println(sscanf.nextToken(", "));
    			}
			long endTime = System.nanoTime();
			System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
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
			System.out.println("1");
	
			//connectionSub = getConnectionSub(contexts);
			connectionSub = contexts.createSocket(SocketType.SUB);
	        connectionSub.connect("tcp://localhost:5556");
			
			System.out.println("2");
	
			//connectionReq = getConnectionReq(context
			connectionReq = contextr.createSocket(SocketType.REQ);
            connectionReq.connect("tcp://localhost:5555");
			System.out.println("3");
			
			String request = "GET ".concat(sub);
			connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
			System.out.println("3 part 2");
			byte[] reply = connectionReq.recv(0); //Key in bytes
			System.out.println("3 part 3");
			String key = new String(reply,ZMQ.CHARSET);
			String encryptedSub = new String(encrypt(key,sub));
			connectionSub.subscribe(encryptedSub.getBytes(ZMQ.CHARSET));
			System.out.println("Encryption Key: "+key);
			contextr.close();

			 while (!Thread.currentThread ().isInterrupted ()){
					System.out.println("4");
					String string = connectionSub.recvStr(0).trim();
					String decryptedString = decrypt(reply,string);
					System.out.println("Decrypted String: "+decryptedString);
					System.out.println("5");
					StringTokenizer sscanf = new StringTokenizer(string, " ");
					while (sscanf.hasMoreTokens()) {
			            System.out.println(sscanf.nextToken(", "));
			        }
					String ack = "ACK".concat(sub);
					System.out.println("6");
		
					connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
					System.out.println("7");
		
					long endTime = System.nanoTime();
					System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
			 	}
				contexts.close();
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
		try {
			
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        encrypted = cipher.doFinal(text.getBytes());

		}catch(Exception e) {
			e.printStackTrace();
		}
		return encrypted;
	}
	public String decrypt(byte[]encrypted,String key) {
		String decrypted = null;
		try {
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);
        decrypted = new String(cipher.doFinal(encrypted));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return decrypted;
	}

}
