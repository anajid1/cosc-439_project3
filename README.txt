Name: Abdullah Najid
Email: anajid@emich.edu
Date: 11-23-2021
Class: COSC 439
Professor: Dr. Tehranipour

Project #3
Description: This project modifies Project #2 and makes messages between the clients and server encrypted. I used
	Diffie-Hellman key exchange to calulate a unique key between each client and the server. The key is used to 
	encrypt/decrypt messages via bit-wise xor. The helper class Cryptography has all the methods for implementing
	the encryption/decryption.

How to compile and run program: Simply compile ana_TCPClient.java, ana_TCPServer.java, and Cryptography.java using javac in a terminal
    or using the built in compiler in an IDE. Run the program using java and the respective program name without any
    extension (.class or .java). It is important to run the server first and then as many clients as you want.
    Make sure you give the port number to the server program otherwise it assumes port number 20750 and similarly give
    the server's host address and port number to the client program using command line arguments -h and -p otherwise
    it assumes the server program is on the localhost at port 20750.

*Conclusion*
    Time: 7 hours
    
    Bugs: No bugs that I know of. Client and server program should behave as expected as long as the user inputs are
        valid (no error checking).