package uoit.ca.dsproject;


import java.net.UnknownHostException;
import java.util.Date;
import java.net.InetAddress;

//Block represent a task
public class Block{

    //List<User> users = new ArrayList<User>();
    //List<Timestamp> timestamps = new ArrayList<Timestamp>();
    private String hash;
    private String previousHash;
    private String data;
    private long timeStamp;
    private int nonce;
    private String ip;

    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(previousHash +Long.toString(timeStamp)+data);
        return calculatedHash;
    }


    public Block(String data, String previousHash){
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.data = data;
        this.previousHash=previousHash;
        this.timeStamp = new Date().getTime();
        this.hash= calculateHash();
        try {
            this.ip = inetAddress.getHostAddress();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }
    //remove if unnecessary
    public void mineBlock(int difficulty){
        String target = new String (new char[difficulty]).replace('\0','0');
        while(!hash.substring(0,difficulty).equals(target)){
            nonce ++;
            hash = calculateHash();
        }
        System.out.print("Block Mined!!! :"+hash);
    }

    public String getHash() {
        return hash;
    }

    public String getIp() {
        return ip;
    }

    public String getPreviousHash() {
        return previousHash;
    }
}