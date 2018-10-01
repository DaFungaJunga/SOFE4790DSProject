package uoit.ca.dsproject;


import com.google.gson.GsonBuilder;

import java.util.ArrayList;
// Download gson.2.6.2.jar
public class BlockChain{
    private static ArrayList<Block> blockChain;

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
    public Boolean validation(ArrayList<Block> blockChain){
        Block currentBlock;
        Block previousBlock;
        for(int i=1;i<blockChain.size();i++){
            currentBlock= blockChain.get(i);
            previousBlock=blockChain.get(i-1);

            if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
                System.out.println("Current Hashes not equal");
                return false;
            }
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash())){
                System.out.println("Previous Hashes not equal");
                return false;
            }

        }
        return true;
    }
    public void startBlock(String task){
        blockChain = new ArrayList<Block>();
        blockChain.add(new Block(task,"0"));
    }
    public void continueBlock(String task){
        if (validation(blockChain)){
            blockChain.add(new Block(task,blockChain.get(blockChain.size()-1).getPreviousHash()));

        }
        System.out.println("BLOCK NOT VALID");
    }
    public String printBlocks(){
        //System.out.println("Chain=");
        return new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
    }
    public static String getHash(){
        return blockChain.get(0).getPreviousHash();
    }
    //need to find other users
    //blockListener
    //blockSender
    //method for  blockChain.add()
    //each block has their own hash but the chain has a whole as a hash for access
}