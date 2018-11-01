import java.util.ArrayList;
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
	//ZContext context = new ZContext();
	public void setHost(String h){
		host = h;
	}
	public void setPort(String p){
		port = p;
	}
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
			System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
			Thread.sleep(1000);
        }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void publishWithAck(String pub,String info){
		try{
		ZContext contextp = new ZContext();
		ZContext contextr = new ZContext();
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
		System.out.println("2");

		if (connectionRep==null){
			//connectionRep = getConnectionPub(pub,contextr);
			connectionRep = contextr.createSocket(SocketType.REP);
            connectionRep.connect("tcp://*:5555");
		}
		System.out.println("3");
		new Thread(new Runnable() {
		    @Override public void run() {
		    	recieveMessage(pub,contextr);		        
		    }
		}).start();
		while (!Thread.currentThread().isInterrupted()) {
			long startTime = System.nanoTime();
			String update = String.format(
                    "%s %s", pub, info
                );
			//System.out.println("4");

	        connectionPub.send(update, 0);
	        addTopic(pub,contextr);
			//System.out.println("5");

	        long endTime = System.nanoTime();
	        //recieveMessage(pub,contextr);
			//System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void addTopic(String pub,ZContext context){
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
	public void recieveMessage(String pub,ZContext context){
		try{
			while (!Thread.currentThread().isInterrupted()) {
	            String string = connectionRep.recvStr(0).trim();
	            StringTokenizer sscanf = new StringTokenizer(string, " ");
	            String str = sscanf.nextToken();
	            if (str=="ACK"){
	            	System.out.println("Recieved ACK");
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
	            	System.out.println("Recieved ADD");
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
	            	System.out.println("Recieved ADD");
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
