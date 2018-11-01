import java.util.ArrayList;
import java.util.StringTokenizer;

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
			connectionReq = context.createSocket(SocketType.REP);
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
	public void getSubscriptionWithAck(String sub){
		ZContext contexts = new ZContext();
		ZContext contextr = new ZContext();
		long startTime = System.nanoTime();
		System.out.println("1");

		if(connectionSub==null){
			connectionSub = getConnectionSub(contexts);	
		}
		System.out.println("2");

		if(connectionReq==null){
			connectionReq = getConnectionReq(contextr);
		}
		System.out.println("3");

		connectionSub.subscribe(sub.getBytes(ZMQ.CHARSET));
		 while (!Thread.currentThread ().isInterrupted ()){
				System.out.println("4");
			String string = connectionSub.recvStr(0).trim();
			
			System.out.println("5");
			StringTokenizer sscanf = new StringTokenizer(string, " ");
			while (sscanf.hasMoreTokens()) {
	            System.out.println(sscanf.nextToken(", "));
	        }
			String ack = "ACK"+ sub;
			System.out.println("6");

			connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
			System.out.println("7");

			long endTime = System.nanoTime();
			System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
		 }
		
	}
	
	public void addSubscription(String sub,ZContext context){
		subscriptions.add(sub);
		String request = "ADD " + sub;
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
	public void removeSubscription(String sub,ZContext context){
		subscriptions.remove(sub);
		String request = "REMOVE " + sub;
		connectionReq = getConnectionReq(context);
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
	

}
