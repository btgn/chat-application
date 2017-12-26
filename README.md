# Chat Application For Remote Message Exchange

## **Requirements**
    * Any Operating System that spports Java

## **Description**
An application that allows the user to exchange messages between two or more hosts connected over an intranet. Application based on Computer Networks can help in increasing the standard of co-operation. In addition it converts the complex concept of sockets into a user friendly environment. This application can have further potential such as data transfer using file sharing protocols such as FTP (File Transfer Protocols).

This application allows the user to understand the depth of Socket Programming using the TCP Protocols by setting up both the client side and server side at a single host alone. The application first keeps listening for connections in order to exchange messages. It also allows the user to execute all the commands just based on the in built commands at the same time.


## **Methodology**
The user interaction from one host to another host is based on the UNIX shell kind of commands. There are several in-built commands for the application to work efficiently based on the inputs given by the user. They are all listed below:

1.	help: Displays information about all the commands available for user interface options.
2.	myip: Displays the IP address of this process i.e., the currently IP address of the computer.
3.	myport: Displays the port number on which the process is listening for incoming connections.
4.	connect <destination> <port no>: This command establishes a new TCP connection to the specified <destination> at the specified <port number>. 
5.	list: Displays the list of all the connections that the current host is connected to, by specifying with a unique ID to all the IP address as well as the Port Number.

        Ex:
            |id: |IP address  |Port No.|
            |----|------------|--------|
            |1:  |192.168.1.44|4332    |
            |2:  |192.168.1.42|4320    |
            |3:  |192.168.1.94|4545    |
            
6.	terminate <connection id>: It will terminate the connection listed under the specified id number specified when we use the list command.
7.	send <connection id> <message>: Sends a message with the specified ID along with the message enclosed along with the connection ID number. On successfully sending the message from one host to another host the message is displayed on the connected host as well as we get a notification on the executed host as message delivered and another message at the connected host saying received message.
8.	 exit: The exit command closes all the connections and terminated the program. Once executed all the other peers are also updated by removing the peer that has left the connection or has called the exit command.
