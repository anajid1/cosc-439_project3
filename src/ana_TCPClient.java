import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Random;
import java.math.BigInteger;

/*
 * Name: Abdullah Najid
 * Date: 10-21-2021
 * Class: COSC 439
 * Professor: Dr. Tehranipour
 * Project #2
 * Client Program: Program connects to a server program. Give the client a host address, port number, and username via
 * command line arguments: -h, -p, and -u respectively; otherwise program will assume local host, port number 20750,
 * and prompt the user for a username. This class is threaded, main thread uses serverHandler method to receive
 * messages from the server. The MessageSender thread is used for user to send messages to server.
 * Important: Server must be running before you attempt to run the client program.
 */
public class ana_TCPClient {
    private static InetAddress host;  /* Used to connect to server. */

    /* Used by MessageSender thread to let ServerHandler thread know if it needs to print a new prompt. */
    public static boolean wantsToSend = true;

    /* Declare and initialize with hard coded values. May change from arguments. */
    private static String hostAddress = "localhost";
    private static String portNumber = "20750";

    /* Declare and initialize username to an empty string. Will prompt user for username if not provided. */
    private static String username = "";
    
    private static String bytePad = "";

    /* Get arguments and a username; call serverHandler method. */
    public static void main(String[] args) {
        /* Get 3 command line arguments: username (-u), server host address (-h), server port number (-p). */
        /* Go through each argument and change values for each respective variable. */
        for(int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-u":
                    username = args[i + 1];
                    break;
                case "-h":
                    hostAddress = args[i + 1];
                    break;
                case "-p":
                    portNumber = args[i + 1];
                    break;
                default:
                    System.out.println("Invalid Arguments! \nTerminating Program...");
                    System.exit(1);
            }
        }

        /* Determine if username was provided. */
        if(username.isEmpty()) {
            /* Get a username from user. */
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Please enter a username: ");
            username = keyboard.nextLine();
        }

        try {
            /* Get server IP-address */
            host = InetAddress.getByName(hostAddress);
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
        /* Run serverHandler method that will create and handle server connection. */
		serverHandler(Integer.parseInt(portNumber));
    }


    /* Method establishes a connection with server and prints any messages from the server which may be an
     * echo of a message from another client. */
    private static void serverHandler(int portNumber) {
        Socket link = null;

        try {
			link = new Socket(host, portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Set up input and output streams for the connection
        BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(link.getInputStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        PrintWriter out = null;
		try {
			out = new PrintWriter(link.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        /********************** HANDSHAKE **********************/
        BigInteger g = null;
		try {
			g = new BigInteger(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BigInteger n = null;
		try {
			n = new BigInteger(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        /* 100 <= x <= 200 */
        Random rand = new Random();
        BigInteger x = new BigInteger("" + (rand.nextInt(101) + 100));
        
        BigInteger clientPartialKey = g.modPow(x, n);
        out.println(clientPartialKey.toString());
        
        BigInteger serverPartialKey = null;
		try {
			serverPartialKey = new BigInteger(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        BigInteger keyBig = serverPartialKey.modPow(x, n);
        String keyStr = keyBig.toString();
        System.out.println("key = " + keyStr);
        
        bytePad = Cryptography.getBytePad(keyStr);
        System.out.println(bytePad);
        
        /* Send server user-name. */
        out.println(Cryptography.encrypt(bytePad, username));

        /* Create and start thread for prompting user for messages to send to server. */
        MessageSender messageSender = new MessageSender(link, out);
        messageSender.start();

        /* Calculate spaces to clear user prompt on current console line for a message. */
        String spaces = "  ";
        for(int i = 0; i < username.length(); i++)
    		spaces += " ";

        /* Keep printing messages from server till server sends DONE. */
        String response = "";
        try {
			response = decryptMessage(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        while(!response.equals("DONE")) {
            /* Clear current user prompt and put the message. */
            System.out.print("\r" + spaces + "\r" + response + "\n");
            if(wantsToSend)
                /* User still wants to send messages so print out a new prompt. */
                System.out.print(username + "> ");
            try {
				response = decryptMessage(in.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    

    /* Threaded class used to send messages to server. */
    public static class MessageSender extends Thread {
        Socket link;
        PrintWriter out;

        /* Constructor */
        public MessageSender(Socket link, PrintWriter out) {
            this.link = link;
            this.out = out;
        }

        /* Prompts user to send messages. */
        public void run() {
            // Set up stream for keyboard entry
            BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
            String message = "";

            /* Keep prompting and sending user messages till they enter DONE. */
            do {
                System.out.print(username + "> ");
                try {
                    message = userEntry.readLine();
                } catch (IOException e) { e.printStackTrace(); }
                out.println(enryptMessage(message));
            } while (!message.equals("DONE"));

            /* Let serverHandler thread know it doesn't need to reprint user prompt anymore. */
            wantsToSend = false;
        }
    }
    
    public static String decryptMessage(String encryptedMessage) {
    	return Cryptography.decrypt(bytePad, encryptedMessage);
    }
    
    public static String enryptMessage(String message) {
    	return Cryptography.encrypt(bytePad, message);
    }
}
