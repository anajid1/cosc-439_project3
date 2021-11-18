
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
	
	public static String encrypt(String bytePad, String message) {
		String binaryMessage = "";
		
		// https://stackoverflow.com/questions/15606740/converting-a-character-to-its-binary-value
		for(int i = 0; i < message.length(); i++)
			binaryMessage += "0" + Integer.toBinaryString(message.charAt(i));
		System.out.println("binaryMessage = " + binaryMessage);
		
		String ecryptedBinaryMessage = xOR(bytePad, binaryMessage);
		
		
		return ecryptedBinaryMessage;
	}
	
	public static String decrypt(String bytePad, String encryptedBinary) {
		String decryptedMessage = "";
		
		String decryptedBinary = xOR(bytePad, encryptedBinary);
		
		for(int i = 0; i < decryptedBinary.length(); i += 8) {
			int charCode = Integer.parseInt((String) decryptedBinary.subSequence(i, i+8), 2);
			String str = new Character((char)charCode).toString();
			decryptedMessage += str;
		}
		
		return decryptedMessage;
	}
	
	public static String xOR(String bytePad, String binaryMessage) {
		String encryptedBinary = "";
		
		for(int i = 0; i < binaryMessage.length(); i += 8) {
			String binary = binaryMessage.substring(i, i+8);
			
			System.out.println(binary);
			for(int j = 0; j < bytePad.length(); j++) {
				char x = binary.charAt(j);
				char y = bytePad.charAt(j);
				
				System.out.print("x = " + x + "; y = " + y);
				
				if(x == '1' && y == '0')
					encryptedBinary += "1";
				else if(y == '1' && x == '0')
					encryptedBinary += "1";
				else
					encryptedBinary += "0";
			}
			System.out.println("\nencryptedBinary = " + encryptedBinary);
		}
		
		return encryptedBinary;
	}
}
