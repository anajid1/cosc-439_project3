import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ana_TCPServer {
    private static ServerSocket serverSocket;
    private static FileWriter myFileWriter;
    private static Scanner myFileReader;
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

        /* print local host name */
        String host = InetAddress.getLocalHost().getHostName();
        System.out.println("Client has established a connection to " + host);

        /* First message from client is client's username. */
        String clientUsername = in.readLine();

        ClientHandler clientHandler = new ClientHandler(link, in, out, startTime, clientUsername);
        
        /* Determine if we need to create a chat log file; file is only created if there are no client's currently connected to server. */
        if(clientHandlerArrayList.isEmpty()) {
        	/* No current clients connected to server therefore no chat file exists. We will create one. */
        	myFileWriter = new FileWriter("ana_chat.txt");
        }

        clientHandlerArrayList.add(clientHandler);

        clientHandler.start();
    }

    public static class ClientHandler extends Thread {
        Socket link;
        BufferedReader in;
        PrintWriter out;
        long startTime;
        String userName;

        public ClientHandler(Socket link, BufferedReader in, PrintWriter out, long startTime, String userName) {
            this.link = link;
            this.in = in;
            this.out = out;
            this.startTime = startTime;
            this.userName = userName;
        }

        public void run() {
        	try {
				messageOut(userName + " has joined.");
			} catch (IOException e) { }
        	
            String message = "";
            try {
                message = in.readLine();
            } catch (IOException e) { }

            while (!message.equals("DONE")) {
				try {
					messageOut(userName + " " + message); 
				} catch (IOException e) { }

                try {
                    message = in.readLine();  /* Get the next message from the client. */
                } catch (IOException e) { }
            }
        }

        /* Method used by other ClientHandlers to send their messages to their respective client. */
        public void echo(String message) {
            out.println(message);
        }
        
        /* Prints client's message to server console, chat file, and every client except the one who sent the message. */
        private void messageOut(String message) throws IOException {
        	System.out.println(message);  /* Print message on the console */
        	
        	myFileWriter.write(message + "\n"); /* Output message in chat file. */
        	
        	/* Print message on all the client's console except the one who sent the message. */
            for(int i = 0; i < clientHandlerArrayList.size(); i++) {
                ClientHandler tempClientHandler = clientHandlerArrayList.get(i);
                if(!tempClientHandler.equals(this))
                    tempClientHandler.echo(message);
            }
        }
    }

}
