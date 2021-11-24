import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.math.BigInteger;

/*
 * Name: Abdullah Najid
 * 
 * Class: COSC 439
 * Professor: Dr. Tehranipour
 * Project #3
 * Server Program: Give server program a port number with argument -p otherwise server assumes a default port number.
 * Server is threaded so multiple clients can join at the same time. Server acts as a chat room for the clients. Chat
 * history is recorded in a chat file so that when new clients join they will receive the chat history. 
 * Additionally messages are encrypted and decrypted using Diffie-Hellman key exchange using argument -g and -n and 
 * bit-level encryption.
 */
public class ana_TCPServer {
    /* Used to create socket for this server. */
    private static ServerSocket serverSocket;

    /* Used to write to a chat log file. */
    private static FileWriter myFileWriter;

    /* Used to have a reference for the multiple client handler threads. */
    public static ArrayList<ClientHandler> clientHandlerArrayList = new ArrayList<ClientHandler>();
    
    /* Hard coded port number. */
    public static int portNumber = 20750;
    
    /* g, n are used to calculate secret key. */
    private static int g = 1019;
    private static int n = 1823;

    /* Main method that creates a socket using either the predefined port number or one provided in args and then
     * keeps trying to get connections to clients.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Opening port...\n");

        /* Try to create a socket with a port number. */
        try {
            /* Check if any arguments were provided for a port number. */
            for(int i = 0; i < args.length; i += 2) {
                switch (args[i]) {
                    case "-p":
                        portNumber = Integer.parseInt(args[i+1]);
                        break;
                    case "-g":
                        g = Integer.parseInt(args[i+1]);
                        break;
                    case "-n":
                        n = Integer.parseInt(args[i+1]);
                        break;
                    default:
                        System.out.println("Invalid Arguments! \nTerminating Program...");
                        System.exit(1);
                }
            }
            
            /* Print g,n per rubric. */
            System.out.println("g: " + g);
            System.out.println("n: " + n);

            /* Create a server object. */
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }

        /* Keep getting connections to clients. */
        while(true) {
            connectionGetter();
        }
    }

    /* Method gets a connection, creates a client handler thread for the connection, and starts the thread. */
    /* Implements a handshake for client-server to calculate a secret key securely. Uses Cryptography class for its helper functions. */
    private static void connectionGetter() throws IOException {
        Socket link = null;

        /* Wait for a client to connect. */
        link = serverSocket.accept();

        /* Client has connected socket. */

        /* Get a record for the time to determine how long connection lasts. */
        long startTime = System.currentTimeMillis();

        /* Set up input and output streams for socket. */
        BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
        PrintWriter out = new PrintWriter(link.getOutputStream(), true);

        /* Print local host name on console for server's record. */
        String hostName = InetAddress.getLocalHost().getHostName();
        System.out.println("Client has established a connection to " + hostName);
        
        /********************** HANDSHAKE **********************/
        /* Send client g and n */
        out.println(g);
        out.println(n);
        
        /* Client sends back g^xmod(n). */
        BigInteger clientPartialKey = new BigInteger(in.readLine());
        
        /* Convert g & n to big integers for large number computations. */
        BigInteger gBig = new BigInteger(""+g);
        BigInteger nBig = new BigInteger(""+n);
        
        /* Generate y: 100 <= y <= 200 */
        Random rand = new Random();
        BigInteger y = new BigInteger("" + (rand.nextInt(101) + 100));
        
        /* g^ymod(n) send to client. */
        BigInteger serverPartialKey = gBig.modPow(y, nBig);
        out.println(serverPartialKey.toString());
       
        BigInteger keyBig = clientPartialKey.modPow(y, nBig);				// Calculate the key.
        
        /* Convert key to a string and print it. */
        String keyStr = keyBig.toString();
        System.out.println("Key: " + keyStr);
        
        /* Convert keyStr to 8-bit binary "byte-pad" and print it. */
        String bytePad = Cryptography.getBytePad(keyStr);
        System.out.println("Byte-Pad: " + bytePad);
        /********************** END HANDSHAKE **********************/
        /* All messages will now be encrypted/decrypted. */

        /* First message from client is client's user-name. */
        String clientUsername = Cryptography.decrypt(bytePad, in.readLine());

        /* Create client handler thread to manage the connection. */
        ClientHandler clientHandler = new ClientHandler(link, in, out, startTime, clientUsername, hostName, bytePad);
        
        /* Add thread to an array list so it can be referenced later. */
        createClientHandlerArrayList(clientHandler);

        /* Start the thread that will manage the connection to the client. */
        clientHandler.start();
    }
    
    /* Method adds a clientHandler thread to an array list. */
    private synchronized static void createClientHandlerArrayList(ClientHandler clientHandler) throws IOException {
    	/* Determine if we need to create a chat log file;
         * file is only created if there are no client's currently connected to server.
         */
        if(clientHandlerArrayList.isEmpty()) {
        	/* No current clients connected to server therefore no chat file exists. We will create one. */
        	myFileWriter = new FileWriter("ana_chat.txt");
        }

        clientHandlerArrayList.add(clientHandler);
    }
    

    /* Threaded class that will manage a client connection. */
    public static class ClientHandler extends Thread {
        private Socket link;
        private BufferedReader in;
        private PrintWriter out;
        private long startTime;
        private String userName;
        private String hostName;
        private String bytePad;

        /* Values will be used to calculate connection time to a client. */
        private final static int MS_IN_HOUR = 3600000;
        private final static int MS_IN_MINUTES = 60000;
        private final static int MS_IN_SECONDS = 1000;

        /* Constructor */
        public ClientHandler(Socket link, BufferedReader in, PrintWriter out, long startTime, String userName,
                             String hostName, String bytePad) {
            this.link = link;
            this.in = in;
            this.out = out;
            this.startTime = startTime;
            this.userName = userName;
            this.hostName = hostName;
            this.bytePad = bytePad;
        }

        /*
         * Method that receives messages from client and prints them to various sources: server console, other clients,
         * and chat file. Manages closing client connection when client wants to disconnect.
         * The messages received are all encrypted and must be decrypted to read. Messages sent are all encrypted
         * with the clients respective bytepad.
         */
        public void run() {
            try {
                /* Print out chat log; will be empty print if current client is first one to connect. */
                File file = new File("ana_chat.txt");
            	this.printChatText(file);

                /* Let current users know and soon to be users know that user has connected. */
            	messageOut(userName + " has joined.");

                /* Keep getting messages from client and echo them to various sources. */
                int numMessages = 0;
            	String message = "";
            	message = getDecryptedMessage();
            	while (!message.equals("DONE")) {
            		messageOut(userName + ": " + message);
                    numMessages++;
            		message = getDecryptedMessage();  /* Get the next message from the client. */
            	}

                /* Client wants to end connection. */

                /* Let various sources know client has left. */
                messageOut(userName + " has left.");

            	/* Close file if this is the last client connected to server. */
                if(clientHandlerArrayList.size() == 1) {
                    myFileWriter.close();
                }

                /*
                 * Send a report back to client.
                 */

                sendEncryptedMessage("Server received " + numMessages + " messages");

                // Get connection time and send it to client.
                long endTime = System.currentTimeMillis();
                // MS is added to name since unit of time is in milliseconds for better readability.
                long timeMS = endTime-startTime;

                /* Currently, timeMS holds total time. Eventually timeMS will only hold remaining time that can't fit into
                 * hours, minutes, and seconds.
                 */
                int timeHours = 0;
                int timeMinutes = 0;
                int timeSeconds = 0;

                // Get hours.
                if(timeMS >= MS_IN_HOUR) {
                    timeHours = (int) timeMS/MS_IN_HOUR;
                    timeMS = timeMS % MS_IN_HOUR;
                }

                // Get minutes.
                if(timeMS >= MS_IN_MINUTES) {
                    timeMinutes = (int) timeMS/MS_IN_MINUTES;
                    timeMS = timeMS % MS_IN_MINUTES;
                }

                // Get seconds.
                if(timeMS >= MS_IN_SECONDS) {
                    timeSeconds = (int) timeMS/MS_IN_SECONDS;
                    timeMS = timeMS % MS_IN_SECONDS;
                }

                // Send time to client.
                sendEncryptedMessage(timeHours + "::" + timeMinutes + "::" + timeSeconds + "::" + timeMS);

                /* Let client know there are no more messages from the server. */
                sendEncryptedMessage("DONE");

                /* Keep a record of client disconnecting on server console. */
                System.out.println(hostName + " has disconnected.");

                /* If client was last one on server delete the chat file. */
                if(clientHandlerArrayList.size() == 1) {
                    file.delete();
                }

                /* Remove client from list. */
                clientHandlerArrayList.remove(this);
			} catch (Exception e) { e.printStackTrace(); }
            
        }

        /* Method used by other ClientHandlers to send their messages to this client handler. */
        public void echo(String message) {
            sendEncryptedMessage(message);
        }
        
        /* Method to read from chat file. */
        private void printChatText(File file) throws FileNotFoundException {
        	Scanner myFileReader = new Scanner(file);
            while (myFileReader.hasNextLine())
                sendEncryptedMessage(myFileReader.nextLine());
            myFileReader.close();
        }
        
        /* Synchronized method to write to chat file. */
        private synchronized void writeToChatFile(String message) throws IOException {
        	myFileWriter.write(message + "\n");  /* Output message in chat file. */
        	myFileWriter.flush();  /* Flush the message instead of waiting till file closes to output message. */
        }
        
        /* Prints client's message to server console, chat file, and every client except the one who sent the message. */
        private void messageOut(String message) throws IOException {
        	System.out.println(message);
        	
        	writeToChatFile(message);

        	printToClients(message);
        }
        
        /* Method echos message to all ClientHandler threads in the array list except the current ClientHandler. */
        private synchronized void printToClients(String message) {
        	for(int i = 0; i < clientHandlerArrayList.size(); i++) {
                ClientHandler tempClientHandler = clientHandlerArrayList.get(i);
                if(!tempClientHandler.equals(this)) {
                    tempClientHandler.echo(message);
                }
            }
        }
        
        /* Method is just used to simplify and make code cleaner to read. */
        private void sendEncryptedMessage(String message) {
        	String encryptedMessage = Cryptography.encrypt(bytePad, message);
        	out.println(encryptedMessage);
        }
        
        /* Method is just used to simplify and make code cleaner to read. */
        private String getDecryptedMessage() throws IOException {
        	String encryptedMessage = in.readLine();
        	return Cryptography.decrypt(bytePad, encryptedMessage);
        }
    }

}
