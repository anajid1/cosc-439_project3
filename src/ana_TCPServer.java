import java.io.*;
import java.net.*;

public class ana_TCPServer {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
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
    }

}
