package uoit.ca.dsproject;

import java.util.ArrayList;

public class User {
    public static ArrayList<BlockChain> blockChains=null;

    public static void startBlockChain(String task){
        blockChains = new ArrayList<BlockChain>();
        BlockChain newBlockChain= new BlockChain();
        newBlockChain.startBlock(task);
        blockChains.add(newBlockChain);
    }
    public static void addToBlockChain(String task, BlockChain newBlockChain) {
        newBlockChain.continueBlock(task);
    }
    public static BlockChain getBlockChain(String hash){
        for (BlockChain bc :blockChains){
            if (hash==bc.getHash()){
                return bc;
            }
        }
        return null;
    }
    public static String printBlockChain(BlockChain bc){
        return bc.printBlocks();
    }
}
