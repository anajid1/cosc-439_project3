import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ana_TCPServer {
    private static ServerSocket serverSocket;
    private static FileWriter myFileWriter;
    public static ArrayList<ClientHandler> clientHandlerArrayList = new ArrayList<ClientHandler>();

    public static void main(String[] args) throws IOException {
        System.out.println("Opening port...\n");

        /* Try to create a socket with a port number. */
        try {
            /* Hard coded port number. */
            int portNumber = 20750;

            /* Check if any arguments were provided for a port number. */
            for(int i = 0; i < args.length; i += 2) {
                switch (args[i]) {
                    case "-p":
                        portNumber = Integer.parseInt(args[1]);
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

        while(true) {
            connectionGetter();
        }
    }

    private static void connectionGetter() throws IOException {
        Socket link = null;

        link = serverSocket.accept();

        long startTime = System.currentTimeMillis();

        /* Set up input and output streams for socket. */
        BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
        PrintWriter out = new PrintWriter(link.getOutputStream(), true);

        /* print local host name on console. */
        String hostName = InetAddress.getLocalHost().getHostName();
        System.out.println("Client has established a connection to " + hostName);

        /* First message from client is client's username. */
        String clientUsername = in.readLine();

        ClientHandler clientHandler = new ClientHandler(link, in, out, startTime, clientUsername, hostName);
        
        /* Determine if we need to create a chat log file; file is only created if there are no client's currently connected to server. */
        if(clientHandlerArrayList.isEmpty()) {
        	/* No current clients connected to server therefore no chat file exists. We will create one. */
        	myFileWriter = new FileWriter("ana_chat.txt");
        }

        clientHandlerArrayList.add(clientHandler);

        clientHandler.start();
    }

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

        public ClientHandler(Socket link, BufferedReader in, PrintWriter out, long startTime, String userName,
                             String hostName) {
            this.link = link;
            this.in = in;
            this.out = out;
            this.startTime = startTime;
            this.userName = userName;
            this.hostName = hostName;
        }

        public void run() {
            try {
                File file = new File("ana_chat.txt");
            	this.printChatText(file);
            	messageOut(userName + " has joined.");
                int numMessages = 0;
            	String message = "";
            	message = in.readLine();
            	while (!message.equals("DONE")) {
            		messageOut(userName + ": " + message);
                    numMessages++;
            		message = in.readLine();  /* Get the next message from the client. */
            	}

                messageOut(userName + " has left.");

            	/* Close file */
                if(clientHandlerArrayList.size() == 1) {
                    myFileWriter.close();
                }

                // Send a report back to client.
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

                System.out.println("Client has established a connection to " + hostName);

                if(clientHandlerArrayList.size() == 1) {
                    file.delete();
                }
                clientHandlerArrayList.remove(this);
			} catch (Exception e) { e.printStackTrace(); }
            
        }

        /* Method used by other ClientHandlers to send their messages to this. */
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
        	System.out.println(message);  /* Print message on the console. */
        	
        	writeToChatFile(message);
        	
        	/* Print message on all the client's console except the one who sent the message. */
            for(int i = 0; i < clientHandlerArrayList.size(); i++) {
                ClientHandler tempClientHandler = clientHandlerArrayList.get(i);
                if(!tempClientHandler.equals(this))
                    tempClientHandler.echo(message);
            }
        }
    }

}
