
public class Cryptography {

	public static String getBytePad(String key) {
    	String bytePad = "";
    	int keyInt = Integer.parseInt(key);
    	String pad = Integer.toBinaryString(keyInt);
    	while(pad.length() < 8) {
    		pad = "0" + pad;
    	}
    	bytePad = pad.substring(pad.length()-8);
    	
    	return bytePad;
    }
	
	public static void encrypt(String bytePad) {
		
	}
}
