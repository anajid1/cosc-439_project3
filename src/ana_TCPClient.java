import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ana_TCPClient {
    private static InetAddress host;
    public static boolean wantsToSend = true;

    /* Declare and initialize with hard coded values. May change from arguments. */
    private static String hostAddress = "localhost";
    private static String portNumber = "20750";

    /* Declare and initialize username to an empty string. Will prompt user for username if not provided. */
    private static String username = "";

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
        try {
            serverHandler(Integer.parseInt(portNumber));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* Method establishes a connection with server and prints any messages from the server which may be an
     * echo of a message from another client. */
    private static void serverHandler(int portNumber) throws IOException {
        Socket link = null;

        link = new Socket(host, portNumber);

        // Set up input and output streams for the connection
        BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
        PrintWriter out = new PrintWriter(link.getOutputStream(), true);

        // Send server username.
        out.println(username);

        MessageSender messageSender = new MessageSender(link, out);

        messageSender.start();

        String spaces = "  ";
        for(int i = 0; i < username.length(); i++)
    		spaces += " ";
        
        String response = "";
        response = in.readLine();
        while(!response.equals("DONE")) {
            System.out.print("\r" + spaces + "\r" + response + "\n");
            if(wantsToSend)
                System.out.print(username + "> ");
            response = in.readLine();
        }
    }

    public static class MessageSender extends Thread {
        Socket link;
        PrintWriter out;

        public MessageSender(Socket link, PrintWriter out) {
            this.link = link;
            this.out = out;
        }

        public void run() {
            // Set up stream for keyboard entry
            BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
            String message = "";

            do {
                System.out.print(username + "> ");
                try {
                    message = userEntry.readLine();
                } catch (IOException e) { e.printStackTrace(); }
                out.println(message);
            } while (!message.equals("DONE"));
            wantsToSend = false;
        }
    }
}
