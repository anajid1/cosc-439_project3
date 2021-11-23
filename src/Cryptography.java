/*
 * Name: Abdullah Najid
 * Date: 11-23-2021
 * Class: COSC 439
 * Professor: Dr. Tehranipour
 * Project #3
 * Cryptography: Helper class that has methods used by both the Client and Server class.
 */
public class Cryptography {

	/* Converts a String integer value to a binary 8-bit byte-pad. */
	public static String getBytePad(String key) {
    	String bytePad = "";
    	int keyInt = Integer.parseInt(key);					// Convert String to an int.
    	String pad = Integer.toBinaryString(keyInt);		// Methods converts int to a binary.
    	while(pad.length() < 8) {							// Append 0's so we have a binary of length 8.
    		pad = "0" + pad;
    	}
    	bytePad = pad.substring(pad.length()-8);			// If binary is too big just use right most 8 bits.
    	
    	return bytePad;
    }
	
	/* Converts a message to encrypted binary message by xor's with bytepad. */
	public static String encrypt(String bytePad, String message) {
		/* Converts each character to 8-bit binary ascii value appends them with all characters in messsage so in 
		 * the end the binary message will be 8 times as long as the original message. */
		// https://stackoverflow.com/questions/15606740/converting-a-character-to-its-binary-value
		String binaryMessage = "";
		for(int i = 0; i < message.length(); i++) {
			String byteString = Integer.toBinaryString(message.charAt(i));
			
			/* ASCII only goes from 0 -> 127 (1 to 7 total bits) so we will always need to add a binary 0. */
			while(byteString.length() != 8)				
				byteString = "0" + byteString;
			
			// Append 8-bit binary representation of 1 character in the message.
			binaryMessage += byteString;
		}
		
		String ecryptedBinaryMessage = xOR(bytePad, binaryMessage);
		
		return ecryptedBinaryMessage;
	}
	
	/* Returns decrypted message. */
	public static String decrypt(String bytePad, String encryptedBinary) {
		String decryptedMessage = "";
		
		String decryptedBinary = xOR(bytePad, encryptedBinary);
		
		/* Each 8 bits in decryptedBinary message is the ascii value of 1 character.
		 * Convert each 8 bits to character and append it to decryptedMessage string.
		 */
		for(int i = 0; i < decryptedBinary.length(); i += 8) {
			int charCode = Integer.parseInt((String) decryptedBinary.subSequence(i, i+8), 2);	// Convert to integer ascii value.
			String str = new Character((char)charCode).toString();								// Convert int ascii value to a single char String.
			decryptedMessage += str;															// Append single char string to the message.
		}
		
		return decryptedMessage;
	}
	
	/* Returns xor of a binaryMessage using bytePad. */
	public static String xOR(String bytePad, String binaryMessage) {
		String encryptedBinary = "";
		
		for(int i = 0; i < binaryMessage.length(); i += 8) {
			String binary = binaryMessage.substring(i, i+8);
		
			for(int j = 0; j < bytePad.length(); j++) {
				char x = binary.charAt(j);
				char y = bytePad.charAt(j);
				
				if(x == '1' && y == '0')
					encryptedBinary += "1";
				else if(y == '1' && x == '0')
					encryptedBinary += "1";
				else
					encryptedBinary += "0";
			}
		}
		
		return encryptedBinary;
	}
}
