import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ana_TCPServer {
    private static ServerSocket serverSocket;
    FileWriter myWriter;
    Scanner myReader;

    public static void main(String[] args) throws IOException {
        System.out.println("Opening port...\n");

        // Try to create a socket with a port number.
        try {
            // Hard coded port number
            int portNumber = 20750;

            // Check if any arguments were provided for a port number.
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

            // Create a server object.
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

        // Set up input and output streams for socket
        BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
        PrintWriter out = new PrintWriter(link.getOutputStream(), true);

        // print local host name
        String host = InetAddress.getLocalHost().getHostName();
        System.out.println("Client has established a connection to " + host);

        // First message from client is client's username.
        String clientUsername = in.readLine();

        ClientHandler clientHandler = new ClientHandler(link, in, out, startTime, clientUsername);
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
            String message = "";
            try {
                message = in.readLine();
            } catch (IOException e) { e.printStackTrace(); }

            while (!message.equals("DONE")) {
                // Append client's username to their message and print it.
                String formatMessage = userName + ": " + message;
                System.out.println(formatMessage);

                try {
                    message = in.readLine();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

}
