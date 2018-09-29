package uoit.ca.dsproject;


import java.util.Date;

//Block represent a task
public class Block{

    //List<User> users = new ArrayList<User>();
    //List<Timestamp> timestamps = new ArrayList<Timestamp>();
    public String hash;
    public String previousHash;
    public String data;
    private long timeStamp;
    public int nonce;

    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(previousHash +Long.toString(timeStamp)+data);
        return calculatedHash;
    }


    public Block(String data, String previousHash){
        this.data = data;
        this.previousHash=previousHash;
        this.timeStamp = new Date().getTime();
        this.hash= calculateHash();

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

}