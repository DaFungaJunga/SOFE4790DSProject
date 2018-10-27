import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Pub { 
	/*	
	 *   topics:      0:topic name 1: subscribers count 2: current publication acknowledgement count 3: Total Deliveries
	 */
	ArrayList<ArrayList<String>> topics = new ArrayList<ArrayList<String>>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionPub=null;
	ZMQ.Socket connectionRep=null;
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
		if (connectionRep==null){
			connectionRep = getConnectionPub(pub);
		}
        connectionPub.send(info, 0);
        addTopic(pub);
        byte[] reply =connectionRep.recv(0);
        long endTime = System.currentTimeMillis();
        recieveMessage(pub);
		System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");
	}
	public void addTopic(String pub){
		for(int i=0;i<topics.size();i++){
    		if(topics.get(i).get(0)==pub){
    			topics.get(i).set(2,String.valueOf(0));
    			return;
    		}
    	}
		ArrayList<String> topic = new ArrayList<String>();
		topic.add(pub); //topic name
		topic.add("0"); //subscriber count
		topic.add("0");//current publication acknowledgement count
		topic.add("0");//total deliveries
		topics.add(topic);
	}
	public void recieveMessage(String pub){
		try{
			while (!Thread.currentThread().isInterrupted()) {
	            String string = connectionRep.recvStr(0).trim();
	            StringTokenizer sscanf = new StringTokenizer(string, " ");
	            String str = sscanf.nextToken();
	            if (str=="ACK"){
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0)==sub){
	            			int updateReadCount = Integer.valueOf(topics.get(i).get(2))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;

	            			topics.get(i).set(2,String.valueOf(updateReadCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	            			return;
	            		}
	            	}
	            } 	
	            if(str=="ADD"){
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0)==sub){
	            			int updateCount = Integer.valueOf(topics.get(i).get(1))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;

	            			topics.get(i).set(2,String.valueOf(updateCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	            			return;
	            		}
	            	}
	            }
	            if(str=="REMOVE"){
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0)==sub){
	            			int updateCount = Integer.valueOf(topics.get(i).get(1))-1;
	            			topics.get(i).set(2,String.valueOf(updateCount));
	            			return;
	            		}
	            	}
	            }
	            System.out.println( "Received " + string);
	            Thread.sleep(1000); //  Do some 'work'
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
