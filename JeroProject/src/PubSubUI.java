import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.codec.binary.Base64;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.JScrollPane;

public class PubSubUI extends JFrame {
	//UI variables
	private JPanel contentPane;
	private JTextField txtTopic;
	private JTextField txtHost;
	private JTextField txtPort;
	ZContext contextp;
	ZContext contextpe;
	
	ZContext contextr=null;
	ZContext contexts =null;
	
	ZContext contextse =null;
	ZContext contextre =null;
	
	PubSubUI frame;
	
	/*	
	 *   topics: 0:topic name, 1: subscribers count, 2: current publication acknowledgement count, 
	 *   3: Total Deliveries,  4:key
	 */
	//sub variables
	ArrayList<String> subscriptions = new ArrayList<String>();
	String host=null;
	String port=null; 
	ZMQ.Socket connectionSub=null;
	ZMQ.Socket connectionReq=null;
	JTextArea textAreaSub_1;
	
	//pub variables
	ArrayList<ArrayList<String>> topics = new ArrayList<ArrayList<String>>();
	//String host=null;
	//String port=null; 
	ZMQ.Socket connectionPub=null;
	ZMQ.Socket connectionRep=null;
	JTextArea textAreaPub_1;
	
	/**
	 * Pub methods
	 * 
	 */
	
	public void setHost(String h){
		host = h;
	}
	public void setPort(String p){
		port = p;
	}
	/*public void setPubTerminal(JTextArea terminal) {
		textAreaPub_1 = terminal;
	}*/
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
		if (connectionPub==null){
			contextp = new ZContext();
			//connectionPub = getConnectionPub(pub,context);
			connectionPub = contextp.createSocket(SocketType.PUB);
	        // connectionPub.connect("tcp://"+host+":"+port);
	        connectionPub.bind("tcp://*:5556");
	        String ipc = "ipc://test";
	        //connectionPub.bind("ipc://"+pub);
	        connectionPub.bind(ipc);
		}
       // while (!Thread.currentThread().isInterrupted()) {
    		long startTime = System.nanoTime();
    		String update = String.format(
                    "%s %s", pub, info
                );
    		System.out.println(update);
    		textAreaPub_1.setText(textAreaPub_1.getText() + update);
	        connectionPub.send(update, 0);
	        long endTime = System.nanoTime();
	        textAreaPub_1.setText(textAreaPub_1.getText() + "Execution time: " + (endTime - startTime) + " nanoseconds\n");
	        System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
	        textAreaPub_1.revalidate();textAreaPub_1.repaint();
	        repaint();
			Thread.sleep(1000);
       // }
       // context.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void publishEncrypted(String pub,String info){
		try{
		if (connectionPub==null){
			contextpe = new ZContext();

			//connectionPub = getConnectionPub(pub,contextp);
			connectionPub = contextpe.createSocket(SocketType.PUB);
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
		textAreaPub_1.setText(textAreaPub_1.getText() +encrypt(key,pub).length+"\n");
		System.out.println(encrypt(key,pub).length);

        //revalidate();repaint();
        addTopic(pub,contextpe,key);
        textAreaPub_1.setText(textAreaPub_1.getText() +key+"\n");
        //revalidate();repaint();
		new Thread(new Runnable() {
		    @Override public void run() {
		    	recieveMessage(pub);		        
		    }
		}).start();

		//while (!Thread.currentThread().isInterrupted()) {
			long startTime = System.nanoTime();
			String update = String.format(
                    "%s %s", encryptedPub, encryptedInfo
                );
			//System.out.println("4");

	        connectionPub.send(update, 0);
	        textAreaPub_1.setText(textAreaPub_1.getText() +update+"\n");
	        System.out.println(update);
	         //revalidate();repaint();


	        long endTime = System.nanoTime();
	        //recieveMessage(pub,contextr);
			//System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
			Thread.sleep(10000);
		//}
		//contextp.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void addTopic(String pub,ZContext context,String key){
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
		topic.add(key);;//key
		
		topics.add(topic);
	}
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
	public void recieveMessage(String pub){
		/*	
		 *   topics: 0:topic name, 1: subscribers count, 2: current publication acknowledgement count, 
		 *   3: Total Deliveries,  4:key
		 */
		try{
			if (contextr==null) {
			contextr = new ZContext();
			}

			//connectionRep = getConnectionPub(pub,contextr);
			connectionRep = contextr.createSocket(SocketType.REP);
	        connectionRep.bind("tcp://*:5555");
			while (!Thread.currentThread().isInterrupted()) {
				textAreaPub_1.setText(textAreaPub_1.getText() +"waiting to recieve message"+"\n");
	            System.out.println("waiting to recieve message");
	            byte[] byteString = connectionRep.recv(0);
	            String string = new String(byteString);
	            textAreaPub_1.setText(textAreaPub_1.getText() +"received String:"+string+"\n");
	            System.out.println("received String:"+string);
		         //revalidate();repaint();
	            StringTokenizer sscanf = new StringTokenizer(string, " ");
	            String str = sscanf.nextToken();
	            textAreaPub_1.setText(textAreaPub_1.getText() +"received Str:"+str+"\n");
	            System.out.println("received Str:"+str);
		         //revalidate();repaint();

	            if (str.equals("GET")){
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"Recieved GET"+"\n");
	            	System.out.println("Recieved GET");
			         //revalidate();repaint();
	            	String sub = sscanf.nextToken();
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"received Str:"+sub+"\n");
	            	System.out.println("received Str:"+sub);
			        // revalidate();repaint();

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
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"Sending key "+response+"\n");
	            	System.out.println("Sending key "+response);
			         //revalidate();repaint();
	            }
	            if (str.equals("ACK")){
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"Recieved ACK"+"\n");
	            	System.out.println("Recieved ACK");
			         //revalidate();repaint();

	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateReadCount = Integer.valueOf(topics.get(i).get(2))+1;
	            			int updateTotalCount = Integer.valueOf(topics.get(i).get(3))+1;

	            			topics.get(i).set(2,String.valueOf(updateReadCount));
	            			topics.get(i).set(3,String.valueOf(updateTotalCount));
	    	            	connectionRep.send("CON".getBytes(ZMQ.CHARSET), 0);
	            		}
	            	}
	            } 	
	            if(str.equals("ADD")){
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"Recieved ADD"+"\n");
	            	System.out.println("Recieved ADD");
			         //revalidate();repaint();
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
	            	textAreaPub_1.setText(textAreaPub_1.getText() +"Recieved Remove"+"\n");
	            	System.out.println("Recieved Remove");
			         //revalidate();repaint();
	            	String sub = sscanf.nextToken();
	            	for(int i=0;i<topics.size();i++){
	            		if(topics.get(i).get(0).equals(sub)){
	            			int updateCount = Integer.valueOf(topics.get(i).get(1))-1;
	            			topics.get(i).set(2,String.valueOf(updateCount));
	            		}
	            	}
	            }
	            textAreaPub_1.setText(textAreaPub_1.getText() + "Received " + string+"\n");
	            System.out.println( "Received " + string);
		         revalidate();repaint();
	            Thread.sleep(1000); //  Do some 'work'
	        }
			//contextr.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * Sub methods
	 */
	/*public void setSubTerminal(JTextArea terminal) {
		textAreaSub_1 = terminal;
	}*/
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
			if (connectionSub==null){
				contexts = new ZContext();
				connectionSub = getConnectionSub(contexts);
				
			}
			connectionSub.subscribe(sub.getBytes(ZMQ.CHARSET));
            //while (!Thread.currentThread ().isInterrupted ()) {
            	String string = connectionSub.recvStr(0).trim();
    			long startTime = System.nanoTime();
            	textAreaSub_1.setText(textAreaSub_1.getText() +"received String:"+string+"\n");
            	System.out.println("received String:"+string);
		         //revalidate();repaint();
    			StringTokenizer sscanf = new StringTokenizer(string, " ");
    			while (sscanf.hasMoreTokens()) {
    				String s = sscanf.nextToken(", ");
    				textAreaSub_1.setText(textAreaSub_1.getText() +s+"\n");
    				System.out.println(s);
			         //revalidate();repaint();
    			//}
			long endTime = System.nanoTime();
			textAreaSub_1.setText(textAreaSub_1.getText() +"Execution time: " + (endTime - startTime) + " nanoseconds"+"\n");
			System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
	         revalidate();repaint();
            }
			}catch(Exception e){
				e.printStackTrace();
		}
	}
	public void getSubscriptionEncrypted(String sub){
		try{
			if (contextse==null) {
			contextse = new ZContext();
			contextre = new ZContext();
			}
			long startTime = System.nanoTime();
			//connectionSub = getConnectionSub(contexts);
			connectionSub = contextse.createSocket(SocketType.SUB);
	        connectionSub.connect("tcp://localhost:5556");
			//connectionReq = getConnectionReq(context
			connectionReq = contextre.createSocket(SocketType.REQ);
            connectionReq.connect("tcp://localhost:5555");
			String request = "GET ".concat(sub);
			connectionReq.send(request.getBytes(ZMQ.CHARSET), 0);

			String key = connectionReq.recvStr(0).trim();
			String encryptedSub = new String(encrypt(key,sub));
			connectionSub.subscribe(encryptedSub.getBytes(ZMQ.CHARSET));
			textAreaSub_1.setText(textAreaSub_1.getText() +"Encryption Key: "+key+"\n");
			System.out.println("Encryption Key: "+key);
	         //revalidate();repaint();


			 while (!Thread.currentThread ().isInterrupted ()){
					byte[] stringBytes = connectionSub.recv(0);
					//System.out.println(stringBytes);
					String string = new String(stringBytes);
					//System.out.println(string);
					//System.out.println(stringBytes.toString());

					String[] arr = string.split(" ", 2);
					String encryptedPub = arr[0].trim();
					String encryptedInfo = arr[1].trim();
					textAreaSub_1.setText(textAreaSub_1.getText() +encryptedPub+"\n");
					System.out.println(encryptedPub);
			         //revalidate();repaint();

					//System.out.println("Before trim"+encryptedInfo);

				    //encryptedInfo = encryptedInfo.replaceAll("^\\s+", "");

					//System.out.println("After trim"+encryptedInfo);

						//String decryptedString = decrypt(keyBytes,key);
						//System.out.println("Decrypted String: "+decryptedString);
						//byte[] token = sscanf.nextToken().getBytes();
						//System.out.println("token:"+token);
					textAreaSub_1.setText(textAreaSub_1.getText() +encryptedInfo.getBytes().length+"\n");
					System.out.println(encryptedInfo.getBytes().length);
			         //revalidate();repaint();
					String decryptedString = decrypt(encryptedInfo.getBytes(),key);
					textAreaSub_1.setText(textAreaSub_1.getText() +"Decrypted String: "+decryptedString+"\n");
					System.out.println("Decrypted String: "+decryptedString);
			         //revalidate();repaint();
						
			            //System.out.println(sscanf.nextToken(", "));
			     
					String ack = "ACK ".concat(sub);
		
					connectionReq.send(ack.getBytes(ZMQ.CHARSET), 0);
					byte[] recv = connectionReq.recv(0);
					textAreaSub_1.setText(textAreaSub_1.getText() +recv+"\n");	
					System.out.println(recv);
			         //revalidate();repaint();
					long endTime = System.nanoTime();
					textAreaSub_1.setText(textAreaSub_1.getText() +"Execution time: " + (endTime - startTime) + " nanoseconds"+"\n");
					System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
			         revalidate();repaint();
			 	}
				//contextse.close();
				//contextre.close();
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
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PubSubUI frame = new PubSubUI();
					frame.validate();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public PubSubUI() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 667, 723);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtTopic = new JTextField();
		txtTopic.setBounds(72, 24, 207, 20);
		contentPane.add(txtTopic);
		txtTopic.setColumns(10);
		
		JLabel lblTopic = new JLabel("Topic:");
		lblTopic.setBounds(26, 27, 46, 14);
		contentPane.add(lblTopic);
		
		JLabel lblHost = new JLabel("Host:");
		lblHost.setBounds(26, 52, 46, 14);
		contentPane.add(lblHost);
		
		txtHost = new JTextField();
		txtHost.setBounds(72, 52, 207, 20);
		contentPane.add(txtHost);
		txtHost.setColumns(10);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(26, 90, 46, 14);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setBounds(72, 87, 207, 20);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(67, 567, 510, 106);
		contentPane.add(scrollPane_2);
		
		JTextArea terminal = new JTextArea();
		scrollPane_2.setViewportView(terminal);
		terminal.setLineWrap(true);
		terminal.setEditable(false);
		
		JRadioButton rdbtnRegPub = new JRadioButton("Regular Pub");
		rdbtnRegPub.setBounds(376, 37, 109, 23);
		contentPane.add(rdbtnRegPub);
		
		JRadioButton rdbtnNewPub = new JRadioButton("New Pub");
		rdbtnNewPub.setBounds(376, 74, 109, 23);
		contentPane.add(rdbtnNewPub);
		
		JRadioButton rdbtnNewSub = new JRadioButton("New Sub");
		rdbtnNewSub.setBounds(511, 74, 109, 23);
		contentPane.add(rdbtnNewSub);
		
		JRadioButton rdbtnRegSub = new JRadioButton("Regular Sub");
		rdbtnRegSub.setBounds(511, 37, 109, 23);
		contentPane.add(rdbtnRegSub);
		
		ButtonGroup groupReg = new ButtonGroup();
		groupReg.add(rdbtnRegPub);
		groupReg.add(rdbtnRegSub);
		
		ButtonGroup groupNew = new ButtonGroup();
		groupNew.add(rdbtnNewPub);
		groupNew.add(rdbtnNewSub);
		
		JTextArea textAreaMessage = new JTextArea();
		textAreaMessage.setWrapStyleWord(true);
		textAreaMessage.setLineWrap(true);
		textAreaMessage.setBounds(67, 137, 512, 156);
		contentPane.add(textAreaMessage);
		
		JLabel lblMessage = new JLabel("Message:");
		lblMessage.setBounds(11, 142, 46, 14);
		contentPane.add(lblMessage);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(67, 336, 512, 101);
		contentPane.add(scrollPane);
		
		textAreaPub_1 = new JTextArea();
		scrollPane.setViewportView(textAreaPub_1);
		textAreaPub_1.setLineWrap(true);
		textAreaPub_1.setEditable(false);
		
		JLabel lblResult = new JLabel("PubResult:");
		lblResult.setBounds(11, 341, 61, 14);
		contentPane.add(lblResult);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(67, 448, 512, 108);
		contentPane.add(scrollPane_1);
		
		textAreaSub_1 = new JTextArea();
		scrollPane_1.setViewportView(textAreaSub_1);
		textAreaSub_1.setLineWrap(true);
		textAreaSub_1.setEditable(false);
		
		JLabel lblSubresult = new JLabel("SubResult:");
		lblSubresult.setBounds(10, 453, 61, 14);
		contentPane.add(lblSubresult);
		
		JLabel lblTerminal = new JLabel("Terminal:");
		lblTerminal.setBounds(11, 572, 46, 14);
		contentPane.add(lblTerminal);
		
		JButton btnExecute = new JButton("Execute");
		btnExecute.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(groupReg.getSelection()!=null) {
						if(txtTopic.getText()!=null&&txtHost.getText()!=null&&txtPort.getText()!=null&&textAreaMessage.getText()!=null) {
							if(rdbtnRegPub.isSelected()) {
								//setPubTerminal(textAreaPub_1);
								setHost(txtHost.getText());
								setPort(txtPort.getText());
								publish(txtTopic.getText(), textAreaMessage.getText());
						         revalidate();repaint();
								/*try{
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
							       // while (!Thread.currentThread().isInterrupted()) {
							    		long startTime = System.nanoTime();
							    		String update = String.format(
							                    "%s %s", txtTopic.getText(), textAreaMessage.getText()
							                );
							    		System.out.println(update);
							    		textAreaPub_1.setText(textAreaPub_1.getText() + update);
								        connectionPub.send(update, 0);
								        long endTime = System.nanoTime();
								        textAreaPub_1.setText(textAreaPub_1.getText() + "Execution time: " + (endTime - startTime) + " nanoseconds");
								        System.out.println("Execution time: " + (endTime - startTime) + " nanoseconds");
								        contentPane.revalidate();
								        contentPane.repaint();
								        //revalidate();repaint();
										Thread.sleep(1000);
							        //}
							        context.close();
									}catch(Exception ex){
										ex.printStackTrace();
									}*/
							}
							else {
								//setSubTerminal(textAreaSub_1);
								setHost(txtHost.getText());
								setPort(txtPort.getText());
								getSubscription(txtTopic.getText());
								
							}
						}
						else {
							terminal.setText(terminal.getText() +"Not all fields have been filled");
					         //revalidate();repaint();
						}
					}
					else if(groupNew.getSelection()!=null) {
						if(txtTopic.getText()!=null&&txtHost.getText()!=null&&txtPort.getText()!=null&&textAreaMessage.getText()!=null) {
							if(rdbtnNewPub.isSelected()) {
								//setPubTerminal(textAreaPub_1);
								setHost(txtHost.getText());
								setPort(txtPort.getText());
								publishEncrypted(txtTopic.getText(), textAreaMessage.getText());
						         //revalidate();repaint();

							}
							else {
								//setSubTerminal(textAreaSub_1);
								setHost(txtHost.getText());
								setPort(txtPort.getText());
								getSubscriptionEncrypted(txtTopic.getText());
						         //revalidate();repaint();
							}
						}
						else {
							terminal.setText(terminal.getText() +"Not all fields have been filled");
					         revalidate();repaint();
						}
					}
					else {
						terminal.setText(terminal.getText() +"Radio Button has not been selected");
				         revalidate();repaint();
					}
					
			}catch(Exception e1) {
				terminal.setText(terminal.getText() +"Exception: "+ e1.getMessage());e1.printStackTrace();
		         revalidate();repaint();
		      }	
			}
			
		});
		btnExecute.setBounds(271, 304, 89, 23);
		contentPane.add(btnExecute);
	}
}
