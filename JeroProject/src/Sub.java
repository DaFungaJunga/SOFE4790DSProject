import java.util.ArrayList;
import java.util.StringTokenizer;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Sub {
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
	public ZMQ.Socket getConnectionSub(){
		if(host==null){
			host = "localhost";
		}
		if(port ==null){
			port ="5556";
		}
		try (ZContext context = new ZContext()) {
			connectionSub = context.createSocket(SocketType.SUB);
            connectionSub.connect("tcp://"+host+":"+port);
			return connectionSub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public ZMQ.Socket getConnectionReq(){
		if(host==null){
			host = "localhost";
		}
		if(port ==null){
			port ="5555";
		}
		try (ZContext context = new ZContext()) {
			connectionReq = context.createSocket(SocketType.REQ);
            connectionReq.connect("tcp://"+host+":"+port);
			return connectionReq;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public void getSubscription(String sub){
		long startTime = System.currentTimeMillis();
		if (connectionSub==null){
			connectionSub = getConnectionSub();
		}
		String string = connectionSub.recvStr(0).trim();
		

		StringTokenizer sscanf = new StringTokenizer(string, " ");
		while (sscanf.hasMoreTokens()) {
            System.out.println(sscanf.nextToken(", "));
        }
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
	}
	public void getSubscriptionWithAck(String sub){
		long startTime = System.currentTimeMillis();
		if(connectionSub==null){
			connectionSub = getConnectionSub();	
		}
		if(connectionReq==null){
			connectionReq = getConnectionReq();
		}
		connectionSub.subscribe(sub.getBytes(ZMQ.CHARSET));
		
		String string = connectionSub.recvStr(0).trim();
		
		
		StringTokenizer sscanf = new StringTokenizer(string, " ");
		while (sscanf.hasMoreTokens()) {
            System.out.println(sscanf.nextToken(", "));
        }
		String ack = "ACK";
		connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
		
	}
	
	public void addSubscription(String sub){
		subscriptions.add(sub);
		String request = "ADD " + sub;
		connectionReq = getConnectionReq();
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}
	public void removeSubscription(String sub){
		subscriptions.remove(sub);
		String request = "REMOVE " + sub;
		connectionReq = getConnectionReq();
		connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);
	}

}
