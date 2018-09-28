
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
// Download gson.2.6.2.jar
public class User{
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static int difficulty = 0;

    public static void main(String[] args){
        System.out.println("creating first block");
        blockChain.add(new Block("test 1","0"));
        System.out.println("mining first block");
        blockChain.get(0).mineBlock(difficulty);
        System.out.println("done mining first block");
        blockChain.add(new Block("test 2", blockChain.get(blockChain.size()-1).hash));
        blockChain.get(1).mineBlock(difficulty);

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        System.out.println("Chain=");
        System.out.println(blockchainJson);

    }
    public static Boolean validation(){
        Block currentBlock;
        Block previousBlock;
        for(int i=1;i<blockChain.size();i++){
            currentBlock= blockChain.get(i);
            previousBlock=blockChain.get(i-1);

            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("Current Hashes not equal");
                return false;
            }
            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("Previous Hashes not equal");
                return false;
            }

        }
       return true;
    }
    //need to find other users
    //blockListener
    //blockSender
    //method for  blockChain.add()
    //each block has their own hash but the chain has a whole as a hash for access
}