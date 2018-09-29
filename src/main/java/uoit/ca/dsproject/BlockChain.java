package uoit.ca.dsproject;


import com.google.gson.GsonBuilder;

import java.util.ArrayList;
// Download gson.2.6.2.jar
public class BlockChain{
    public static ArrayList<Block> blockChain;

    /*public static void main(String[] args){
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

    }*/
    public static Boolean validation(ArrayList<Block> blockChain){
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
    public static void startBlock(String task){
        blockChain = new ArrayList<Block>();
        blockChain.add(new Block(task,"0"));
    }
    public static void continueBlock(String task){
        if (validation(blockChain)){
            blockChain.add(new Block(task,blockChain.get(blockChain.size()-1).previousHash));

        }
        System.out.println("BLOCK NOT VALID");
    }
    public static String printBlocks(){
        String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        //System.out.println("Chain=");
        return blockChainJson;
    }
    public static String getHash(){
        return blockChain.get(0).hash;
    }
    //need to find other users
    //blockListener
    //blockSender
    //method for  blockChain.add()
    //each block has their own hash but the chain has a whole as a hash for access
}