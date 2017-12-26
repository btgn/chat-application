package chat.src.chat;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class Chat {
	
	static Selector ReadSelector;
	static Selector WriteSelector;
	static Selector Selector;
	static ArrayList<SocketChannel> activeChanels = new ArrayList<SocketChannel>();
	static ArrayList<String> Peers;
	static int listeningPort;
	static int newConnectionPort;
	static String lastConnectionIp = null;
	static int lastConnectionPort;
	
	
public static void main(final String args[]) throws IOException { 
		
		ReadSelector = Selector.open();
		WriteSelector = Selector.open();

		Peers = new ArrayList<String>();
		  
		/*final String []port = {"1001", "1002", "1003"};*/
		
			String input =null;
			Scanner sc=new Scanner(System.in);  
			
					
		/*if (port.length != 3) {
            System.err.println(
                "Usage: <port number>");
            System.exit(1);
        }
		else{*///start the listening thread
			listeningPort = Integer.parseInt(args[0]);
			new Thread(new Runnable() {
                public void run() {
                	try {
                		ServerSocketChannel channel = ServerSocketChannel.open();
            			channel.configureBlocking(false);
            			channel.bind(new InetSocketAddress(Integer.parseInt(args[0])));

                		while(true){
                			
                			SocketChannel inChannel = channel.accept();
                			if(inChannel !=null){

                				inChannel.configureBlocking(false);
                				inChannel.register(ReadSelector, SelectionKey.OP_READ );
                				inChannel.register(WriteSelector, SelectionKey.OP_WRITE);
                				activeChanels.add(inChannel);
                				
                				String [] peerInfo = inChannel.getRemoteAddress().toString().split(":");
                				String ip = peerInfo[0].substring(1, peerInfo[0].length());
                				

                				System.out.println(ip + " Connected");
                				
                			}
                		}
                	}catch(BindException ex){
                		System.out.println("Can not use port " + args[0] + "Select another port and then restart      \n");
                		System.exit(1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                   }
                }).start();
            	
		new Thread(new Runnable(){
				public void run(){
					Set<SelectionKey> readyKeys;
					Iterator<SelectionKey> selectedKeysIterator;
					ByteBuffer buffer = ByteBuffer.allocate(100);
					SocketChannel channel;
					int bytesRead =0;
					int remoteListeningPort =0;
					String [] peerInfo;
					String ip;

					try {

						while(true){
						int channelReady = ReadSelector.selectNow();
						readyKeys = ReadSelector.selectedKeys();
						selectedKeysIterator = readyKeys.iterator();
						
						if(channelReady !=0){
						while(selectedKeysIterator.hasNext()){
							SelectionKey key = selectedKeysIterator.next();
									
								channel = (SocketChannel) key.channel();
						try{
								bytesRead = channel.read(buffer);
						} catch (IOException e) {
							
							System.out.println(parseChannelIp(channel) + "            closed the connection\n");
							selectedKeysIterator.remove();
							terminateConnection(getChannelIdWithIp(parseChannelIp(channel)));
							
							break;
						}
								peerInfo =channel.getRemoteAddress().toString().split(":");
									
							ip = peerInfo[0].substring(1, peerInfo[0].length());
								
								for(int i =0; i < Peers.size(); i++){
									if(Peers.get(i).startsWith(ip)){
										remoteListeningPort = Integer.parseInt(Peers.get(i).split(":")[1]);
									}
								}

								String message="";
							
								while (bytesRead != 0) {
									buffer.flip();  
									while(buffer.hasRemaining()){
										message +=((char)buffer.get());	
									}	
									message = message.trim();
									if(message.startsWith(("port:"))){
										String info [] = message.split(":");
										Peers.add(ip + ":"  + info[1]);	
									}
									else if(message.trim().isEmpty()){
										int connectionId = getChannelIdWithIp(ip);	
										terminateConnection(connectionId);
										System.out.println(ip + "Connection closed\n");									
									}
									else{
										System.out.print("\nMessage received from " + ip + "\n"
												+ "Sender’s Port: " + remoteListeningPort
												+ "\nMessage: " + message + "\n");									
									}

									buffer.clear(); 
									if(message.trim().isEmpty())
										bytesRead =0;
									else
									bytesRead = channel.read(buffer);
								}
								bytesRead =0;
							selectedKeysIterator.remove();
						}
						
						}//end of 1+ channels
					}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

		//end of listening threads
			
		
		do{
			System.out.println(" Enter Help for commands :");
        input = sc.nextLine();
	
	   	if(input.equals("help")){
	   		System.out.println("myip      display IP address");
	   		System.out.println("myport    Show the port number ");
	   		System.out.println("connect   connect to another Peer");
	   		System.out.println("send      send messages to Peer");
	   		System.out.println("list      List all the connected Peers");
	   		System.out.println("terminate Terminate the connection");
	   		System.out.println("exit      Terminates the connections and exit the program\n");
	   		
	   	}
	   	else if(input.equals("myip")){
	   		if(getOwnIp()!= null)
	   			System.out.println("My IP address: " + getOwnIp() + "\n");
	   	}
	   	else if(input.equals("myport")){
	   		System.out.println("My port:  "+args[0] + "\n");
	   	}
	   	else if(input.startsWith("connect")){ // eg. connect '8.8.8.8' 4545
	   		connect(input, newConnectionPort);
	   		newConnectionPort++;
	   	}
	   		
	   	else if(input.startsWith("Send")){
	   	try{
	   		String inputParse[] =  input.split(" "); 
	        String userInput = inputParse[2];		
	   		  // int charsToSkip = inputParse[0].length() + inputParse[1].length() + 2;

	           int id = Integer.parseInt(inputParse[1]);
	         //  userInput = input.substring(charsToSkip, input.length());
	           
	           
	           	if(getChannelIpWithId(id) !=null){
	           		sendMessage(id, inputParse[2]);
	           		System.out.println("Message sent to  " + getChannelIpWithId(id) + ": <" + userInput + ">\n");
	           	}
	           	
	          
	   	}catch(Exception e){
	   		System.out.println("Invalid input!  Try again...\n");
	   	}
	           }
	   	
	   	else if(input.equals("List")){
	   		System.out.println("ID\tIP\t\tPort No.");
	   		for(int i =0; i <Peers.size(); i++){
	   			String [] infoParse = Peers.get(i).split(":");
	   			System.out.println(i+1 + "\t" + infoParse[0] + "\t" + infoParse[1]);
	   		}
	   		System.out.println("\n");
	   	}
	   	else if(input.startsWith("Terminate")){
	   		String inputParse[] =  input.split("\\s");
	   		int  id;
	   		try{
	   			id = Integer.parseInt(inputParse[1]);
	   			terminateConnection(id);
		   		}catch(Exception e){
	   			System.out.println("Invalid command format....So Try again...\n");
	   		}
	   	}
	   	else if(input.equals("Exit")){
	 	      System.exit(1);
	   	}
	   	else{
	   		System.out.println("Your input is incorrect!!!! So type again");
	   	}
	   	
	}while(!input.equals("Exit"));

}
	
	public static void sendMessage(int id, String message) throws IOException{//check if a channel is ready to be written to
		int channelsReady =0;
			
			try {
					channelsReady = Chat.WriteSelector.select();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(channelsReady >0){
			
			Set<SelectionKey> readyKeys = WriteSelector.selectedKeys();
			Iterator<SelectionKey> selectedKeysIterator = readyKeys.iterator();
			ByteBuffer buffer = ByteBuffer.allocate(100);
			buffer.put(message.getBytes());
			buffer.flip();
			
			while(selectedKeysIterator.hasNext()){
				SelectionKey key = selectedKeysIterator.next();
				
				if(id ==0){
					
					ByteBuffer bf = ByteBuffer.allocate(50);
					bf.put(("port:" + String.valueOf(listeningPort)).getBytes());
					bf.flip();
					
					for(int i =0; i < Peers.size(); i++){
						for(int j =0; j < activeChanels.size(); j++){
							if(lastConnectionIp.equals(parseChannelIp(activeChanels.get(j)))){
								activeChanels.get(j).write(bf);
							}
						}
					}
				}
				else{
					if(parseChannelIp((SocketChannel)key.channel()).equals(getChannelIpWithId(id))){//right channel to write to
									SocketChannel channel = (SocketChannel)key.channel();
						try {
							channel.write(buffer);

						} catch (IOException e) {
							System.out.println("Couldn't send the message to " + getChannelIpWithId(id) +
									"\nPlease try again...");
							e.printStackTrace();
						}
					}
					}

				selectedKeysIterator.remove();
			}
			}
			else{			
				System.out.println("Sending failed...." );
			}
	}
	
	public static String parseChannelIp(SocketChannel channel){
		String ip = null;
		String rawIp =null;  
		try {
			rawIp = channel.getRemoteAddress().toString().split(":")[0];
			ip = rawIp.substring(1, rawIp.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
	
	public static String getChannelIpWithId(int id){//find channel ip provided the id
		String [] infoParse;
		try{
			infoParse = Chat.Peers.get(id-1).split(":");
		}catch(Exception e){
			System.out.println("Invlid ID number! The specified ID not found. Please try again...\n");
			return null;
		}
			return infoParse[0];
	}
	

	
	public static String getOwnIp(){
		String whatismyip;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			whatismyip =ip.getHostAddress();
			 
			
			InputStream is = new ByteArrayInputStream(whatismyip.getBytes());

			// taken from stackoverflow
			//http://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		return in.readLine(); 
		} catch (IOException e) {
			System.out.println("Could not get IP address. Please try again later...\n");
			return null;
		}
	}
	
	public static void connect(String input, int localPort){
		String[] splited = input.split("\\s");
		int portnum = 0;
		String ip =null;
		
		try{
			portnum= Integer.parseInt(splited[2]);
			ip =splited[1];
		
		if(!ip.equals(getOwnIp())){
		SocketChannel channel = SocketChannel.open();
		System.out.print("Connecting...\n");	
		channel.connect(new InetSocketAddress(ip, portnum));
	
		while(!channel.isConnected()){
			System.out.print(".");
		}
		
		lastConnectionIp = ip;
		lastConnectionPort = portnum;
		
		channel.configureBlocking(false);
		channel.register(ReadSelector, SelectionKey.OP_READ );
		channel.register(WriteSelector, SelectionKey.OP_WRITE );
		activeChanels.add(channel);
		
        Chat.Peers.add(ip + ":" + portnum);
        System.out.println("\nConnected to client: " + ip + "\n");
              
        sendMessage(0, "port:" + String.valueOf(listeningPort));//broadcast own port to peer
        
		}
		else{
			System.out.println("Error!!! You can not connect to yourself...so Please try again...\n");
		}
		
		}catch(SocketException e){
			System.out.println("Could not connect to " + ip + "\nPlease try again...\n");
		
		}catch(Exception e){
			System.out.println("Invalid format. Please try again...\n");
			
		}
	}
	
	public static void terminateConnection(int id){
		if(getChannelIpWithId(id) !=null){
		for(SocketChannel channel: Chat.activeChanels){
			if(getChannelIpWithId(id).equals(parseChannelIp(channel))){

				try {
					removeActiveChannel(getChannelIpWithId(id));
					removePeerInfo(id);
					
					channel.close();
					System.out.println("Connetion was terminated with " + id + "\n");
					break;
				} catch (IOException e) {
					System.out.println("Error!! Could not close the connection " + id + " Please try again.....\n");
				}
			}
		}
		}
		else{
			
		}
	}
	
	public static void removePeerInfo(int id){
			Peers.remove(id -1);

	}
	
	public static void removePeerInfo(String ip){
		for(int i = 0; i < Peers.size(); i++){
			if(Peers.get(i).startsWith(ip)){
				Peers.remove(i);
				break;
			}
		}
	}
	public static void removeActiveChannel(String ip){
		for(SocketChannel channel: activeChanels){
			if(parseChannelIp(channel).equals(ip)){
				activeChanels.remove(channel);
				break;
			}
		}
	}
	
	public static int getChannelIdWithIp(String ip){
		int id =-1;
		for(int i =0; i <Peers.size(); i++){
			if(Peers.get(i).startsWith(ip)){
				id = i +1;
				break;
			}
		}
		return id;
	}
}
