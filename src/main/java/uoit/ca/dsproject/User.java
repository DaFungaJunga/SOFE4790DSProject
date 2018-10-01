package uoit.ca.dsproject;

import java.util.ArrayList;

public class User {
    private ArrayList<BlockChain> blockChains = new ArrayList<BlockChain>();
    private ArrayList<String> savedHashes = new ArrayList<String>();

    public void startBlockChain(String task) {
        BlockChain newBlockChain = new BlockChain();
        newBlockChain.startBlock(task);
        blockChains.add(newBlockChain);
    }

    public void addToBlockChain(String task, BlockChain newBlockChain) {
        newBlockChain.continueBlock(task);
    }

    public BlockChain getBlockChain(String hash) {
        for (BlockChain bc : blockChains) {
            if (hash == bc.getHash()) {
                return bc;
            }
        }
        return null;
    }
    public boolean findBlockChain(String hash) {
        for (BlockChain bc : blockChains) {
            if (hash == bc.getHash()) {
                return true;
            }
        }
        return false;
    }
    public void getSavedHashes() {
        savedHashes = null;
        for (BlockChain bc : blockChains) {
            savedHashes.add(bc.getHash());
        }
    }


    public String printBlockChain(BlockChain bc) {
        return bc.printBlocks();
    }

    public boolean replaceBlockChain(BlockChain newBC, String hash) {
        for (int i = 0; i < blockChains.size(); i++){
            if (hash == blockChains.get(i).getHash()) {
                blockChains.set(i, newBC);
                return true;
            }
        }
        return false;
    }
    public ArrayList<String> returnSavedHashes(){
        return savedHashes;
    }
}
