
public class testMainPub {
	/**
	 * Method to test Publisher and Encrypted Publisher methods choosing only one with its counterpart in testMainSub
	 * 
	 * @param args no arguments are used
	 */
	public static void main(String[] args)
    {
		Pub pub = new Pub();
		pub.publishEncrypted("test","test"); //publish test into info

    }
}
