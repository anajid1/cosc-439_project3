Name: Abdullah Najid
Email: anajid@emich.edu
Date: 10-21-2021
Class: COSC 439
Professor: Dr. Tehranipour

Project #2
Description: This project modifies Project #1 to allow multiple clients to join the server at the same time. This
    creates a chat room for multiple clients to communicate with each other. This is done by having 2 threads in the
    client program and multiple threads in the server program. The client program has one thread
    to manage receiving messages from the server: these messages could be from the server itself or redirected messages
    from other clients. The other thread in the client program is for sending messages to the server. On the server side
    you have one thread that gets client connections and we have a list of client handler threads which allows as many
    clients to join the server. A chat file is used to log messages from all the clients so that when new clients join
    they will be sent the chat log. Chat log is deleted when last client leaves server. Writing to the chat file is
    synchronized.

How to compile and run program: Simply compile ana_TCPClient.java and ana_TCPServer.java using javac in a terminal
    or using the built in compiler in an IDE. Run the program using java and the respective program name without any
    extension (.class or .java). It is important to run the server first and then as many clients as you want.
    Make sure you give the port number to the server program otherwise it assumes port number 20750 and similarly give
    the server's host address and port number to the client program using command line arguments -h and -p otherwise
    it assumes the server program is on the localhost at port 20750.

*Conclusion*
    Time: 7 hours
    Difficulties: None although that is because I had some experience with threads in OS class. Otherwise assignment
        would have been much more difficult.
    Bugs: No bugs that I know of. Client and server program should behave as expected as long as the user inputs are
        valid (no error checking).