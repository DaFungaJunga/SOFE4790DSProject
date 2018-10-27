import java.util.ArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Pub {
	ArrayList<String> subscribers = new ArrayList<String>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionPub=null;
	ZMQ.Socket connectionRep=null;
	int publishedMessages;
	int acknowledgedMessages;
	public void setHost(String h){
		host = h;
	}
	public void setPort(String p){
		port = p;
	}
	public ZMQ.Socket getConnectionPub(String pub){
		if(host==null){
			host = "*";
		}
		if(port ==null){
			port ="5556";
		}
		try (ZContext context = new ZContext()) {
			connectionPub = context.createSocket(SocketType.PUB);
            connectionPub.connect("tcp://"+host+":"+port);
            connectionPub.bind("ipc://"+pub);

			return connectionPub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public ZMQ.Socket getConnectionRep(){
		if(host==null){
			host = "*";
		}
		if(port ==null){
			port ="5555";
		}
		try (ZContext context = new ZContext()) {
			connectionPub = context.createSocket(SocketType.REP);
            connectionPub.connect("tcp://"+host+":"+port);
			return connectionPub;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public void publish(String pub,String info){
		long startTime = System.currentTimeMillis();
		if (connectionPub==null){
			connectionPub = getConnectionPub(pub);
		}
        connectionPub.send(info, 0);
        long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
	}
	public void publishWithAck(String pub,String info){
		long startTime = System.currentTimeMillis();
		if (connectionPub==null){
			connectionPub = getConnectionPub(pub);
		}
        connection.send(info, 0);
        publishedMessages++;
        byte[] reply =connection.recv(0);
        long endTime = System.currentTimeMillis();
        if(new String(reply, ZMQ.CHARSET)=="ACK"){
        	acknowledgedMessages++;
        	System.out.println("Published: "+String.valueOf(publishedMessages)+" Acknowledged: "+String.valueOf(acknowledgedMessages) );
        }
		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
	}
	public void recieveMessage(String pub){
		
	}
}
