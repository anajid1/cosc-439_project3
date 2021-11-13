import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Name: Abdullah Najid
 * Date: 10-21-2021
 * Class: COSC 439
 * Professor: Dr. Tehranipour
 * Project #2
 * Server Program: Give server program a port number with argument -p otherwise server assumes a default port number.
 * Server is threaded so multiple clients can join at the same time. Server acts as a chat room for the clients. Chat
 * history is recorded in a chat file so that when new clients join they will receive the chat history.
 */
public class ana_TCPServer {
    /* Used to create socket for this server. */
    private static ServerSocket serverSocket;

    /* Used to write to a chat log file. */
    private static FileWriter myFileWriter;

    /* Used to have a reference for the multiple client handler threads. */
    public static ArrayList<ClientHandler> clientHandlerArrayList = new ArrayList<ClientHandler>();

    /* Main method that creates a socket using either the predefined port number or one provided in args and then
     * keeps trying to get connections to clients.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Opening port...\n");

        /* Try to create a socket with a port number. */
        try {
            /* Hard coded port number. */
            int portNumber = 20750;
            
            /* g, n are used to calculate secret key. */
            int g = 1019;
            int n = 1823;

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

        /* First message from client is client's username. */
        String clientUsername = in.readLine();

        /* Create client handler thread to manage the connection. */
        ClientHandler clientHandler = new ClientHandler(link, in, out, startTime, clientUsername, hostName);
        
        /* Determine if we need to create a chat log file;
         * file is only created if there are no client's currently connected to server.
         */
        if(clientHandlerArrayList.isEmpty()) {
        	/* No current clients connected to server therefore no chat file exists. We will create one. */
        	myFileWriter = new FileWriter("ana_chat.txt");
        }

        clientHandlerArrayList.add(clientHandler);

        /* Start the thread that will manage the connection to the client. */
        clientHandler.start();
    }

    /* Threaded class that will manage a client connection. */
    public static class ClientHandler extends Thread {
        private Socket link;
        private BufferedReader in;
        private PrintWriter out;
        private long startTime;
        private String userName;
        private String hostName;

        /* Values will be used to calculate connection time to a client. */
        private final static int MS_IN_HOUR = 3600000;
        private final static int MS_IN_MINUTES = 60000;
        private final static int MS_IN_SECONDS = 1000;

        /* Constructor */
        public ClientHandler(Socket link, BufferedReader in, PrintWriter out, long startTime, String userName,
                             String hostName) {
            this.link = link;
            this.in = in;
            this.out = out;
            this.startTime = startTime;
            this.userName = userName;
            this.hostName = hostName;
        }

        /*
         * Method that receives messages from client and prints them to various sources: server console, other clients,
         * and chat file. Manages closing client connection when client wants to disconnect.
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
            	message = in.readLine();
            	while (!message.equals("DONE")) {
            		messageOut(userName + ": " + message);
                    numMessages++;
            		message = in.readLine();  /* Get the next message from the client. */
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

                out.println("Server received " + numMessages + " messages");

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
                out.println(timeHours + "::" + timeMinutes + "::" + timeSeconds + "::" + timeMS);

                /* Let client know there are no more messages from the server. */
                out.println("DONE");

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
            out.println(message);
        }
        
        /* Method to read from chat file. */
        private void printChatText(File file) throws FileNotFoundException {
        	Scanner myFileReader = new Scanner(file);
            while (myFileReader.hasNextLine())
                out.println(myFileReader.nextLine());
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

            for(int i = 0; i < clientHandlerArrayList.size(); i++) {
                ClientHandler tempClientHandler = clientHandlerArrayList.get(i);
                if(!tempClientHandler.equals(this))
                    tempClientHandler.echo(message);
            }
        }
    }

}
