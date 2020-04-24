// referencia sobre listas: http://tutorials.jenkov.com/java-collections/list.html

import java.io.*;
import java.net.*;
import java.util.*;
 
public class kompart {
	static Thread _threadHeartbeat;
	static Thread _threadReceiveMessage;
	static int _defaultHeartBeat = 10;
    
	public static void main(String[] args) throws IOException {
		if(args.length != 2){
			System.out.println("Usage: java kompart <server IP> <type>");
			System.out.println("Available types: s (run as server) and p (run as peer)");
			return;
		}

		if(!args[1].toLowerCase().equals("s") && !args[1].toLowerCase().equals("p")){
			System.out.println("Unrecognized type: " + args[1]);
			System.out.println("\nUsage: java kompart <server IP> <type>");
			System.out.println("Available types: s (run as server) and p (run as peer)");
			return;
		}

		if(args[1].toLowerCase().equals("s"))
			RunServer();
		else
			RunPeer(args[0]);
	
	}

	public static void RunServer()  throws IOException {
		String received = null;
		String message = null;

		InetAddress address;
		int port;
		byte[] text = new byte[32768];
		ArrayList<InetAddress> peersToRemove = new ArrayList<>();
		
		DatagramSocket socket = new DatagramSocket(4500);
		DatagramPacket packet;

		HashMap<InetAddress,Peer> peers = new HashMap<>();
		int timerHeartbeat = 5;

		while (true) {
			try {
				timerHeartbeat--;
				// receive datagram
				packet = new DatagramPacket(text, text.length);
				socket.setSoTimeout(500);
				socket.receive(packet);

				// process received datagram
				received = new String(packet.getData(), 0, packet.getLength());
				address = packet.getAddress();
				port = packet.getPort();
				
				// store text as array
				String[] splittedReceived = received.split(" ");

				if(splittedReceived[0].equalsIgnoreCase("register")){
					if(splittedReceived[1].equals("peer")){
						// registry peer

						// Check if peer has registered before
						if(peers.containsKey(address)){
							message = "Peer " + address + " already registered.";
						} else {
							// add IP to map of peers
							peers.put(address,new Peer(address, port, _defaultHeartBeat));
							message = "Peer registered.";
						}
					} else if(splittedReceived[1].equals("file")) {
						// registry file

						// Check if peer has registered before
						if(peers.containsKey(address)){
							// Check if file has registered before
							if(peers.get(address).GetFiles().containsKey(splittedReceived[2])){
								message = "File " + splittedReceived[2] + " already registered.";
							} else {
								KFile kf = new KFile(splittedReceived[2], splittedReceived[3]);
								peers.get(address).GetFiles().put(splittedReceived[2], kf);
								message = "File " + splittedReceived[2] + " registered.";
							}
						} else {
							message = "Peer " + address + " is not registered.";
						}
					} else {
						// unrecognized command
						message = "Unrecognized command.";
					}
					// send response to peer
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
					System.out.println("sending package: " + message);
				} else if(splittedReceived[0].equalsIgnoreCase("list") && splittedReceived[1].equalsIgnoreCase("files")) {
					// List all available files
					Boolean hasFiles = false;
					message = "Available files: \n";

					for (Map.Entry<InetAddress,Peer> peer : peers.entrySet()) {
						if(peer.getKey() != address){
							message += peer.getValue().GetAddress() + "\n";
							for(Map.Entry<String,KFile> file : peer.getValue().GetFiles().entrySet()){
								message += "\t- filename: " + file.getValue().GetFileName() + " , hash: " + file.getValue().GetHash();
								hasFiles = true;
							}
						}
					}

					if(!hasFiles){
						message = "No registered files.";
					}
					
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, packet.getAddress(), packet.getPort());
					socket.send(packet);
					System.out.println("sending package: " + message);
				} else if(splittedReceived[0].equalsIgnoreCase("disconnect")){
					if(peers.containsKey(address)){
						peers.remove(address);
						// set message to peer
						message = "Peer removed.";
					} else {
						message = "Peer not registered.";
					}
					
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
					System.out.println("sending package: " + message);
				} else if(splittedReceived[0].equalsIgnoreCase("exit")){
					if(peers.containsKey(address)){
						peers.remove(address);
						// set message to peer
						message = "exit";
					} else {
						message = "exit";
					}

					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
					System.out.println("sending package: " + message);
				} else if(splittedReceived[0].equalsIgnoreCase("break")){
					message = "exit";
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
					System.out.println("sending package: " + message);
					break;
				} else if(splittedReceived[0].equalsIgnoreCase("heartbeat")){
					address = packet.getAddress();
					if(peers.get(address) != null){
						peers.get(address).SetHeartbeat(_defaultHeartBeat);
					}
				} else {
					// send response to peer
					message = "Unrecognized command.";
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
					System.out.println("sending package: " + message);
				}
			} catch (IOException e) {
				if(timerHeartbeat <= 0){
					timerHeartbeat = 5;
					for (Map.Entry<InetAddress,Peer> pair : peers.entrySet()) {
						// decrement hearbeat;
						int hearbeat = pair.getValue().GetHeartbeat();
						pair.getValue().SetHeartbeat(hearbeat-1);

						// mark peers to remove if heartbeat less than 0
						if(pair.getValue().GetHeartbeat() <= 0){
							peersToRemove.add(pair.getKey());
						}
					}

					// remove peers
					for (InetAddress inetAddress : peersToRemove) {
						System.out.println("removing: " + inetAddress);
						peers.remove(inetAddress);
					}

					// clean peer's list to remove
					peersToRemove.clear();

					// print registered users
					System.out.println("Registered users:");
					for (Map.Entry<InetAddress,Peer> pair : peers.entrySet()) {
						System.out.println(pair.getValue().GetAddress() + " hearbeat: " + pair.getValue().GetHeartbeat());
					}
				}
			}
		}

		socket.close();
		System.out.println("Server closed...");
	}
	
	public static void RunPeer(String serverIp)  throws IOException {
		String input = "";
		Scanner in = new Scanner(System.in);
		byte[] text = new byte[32768];
		byte[] textSend = new byte[32768];

		// create socket datagram
		DatagramSocket socket = new DatagramSocket();

		System.out.println("Welcome to Kompart P2P.\nType 'register peer' to be registered.\n");
		do{
			input = in.nextLine();
			if(!input.equals("register peer")){
				System.out.println("Type 'register peer' to be registered.\n");
			}
		} while(!input.equals("register peer"));

		// send packet
		textSend = input.getBytes();
		InetAddress address = InetAddress.getByName(serverIp);
		DatagramPacket packet = new DatagramPacket(textSend, textSend.length, address, 4500);
		socket.send(packet);

		// Start heartbeat thread
		_threadHeartbeat = StartHeartbeat(address, socket);
		_threadHeartbeat.start();

		// Start receive messages thread
		_threadReceiveMessage = new ReceiveMessagesThread(socket);
		_threadReceiveMessage.start();

		while (true) {
			try {
				if(input.equalsIgnoreCase("break") || input.equalsIgnoreCase("exit")){
					// close socket
					socket.close();
					// close scanner
					in.close();
					break;
				}

				Thread.sleep(1000);
				ShowCommands();
				System.out.print("> ");
				input = in.nextLine();

				textSend = input.getBytes();
				address = InetAddress.getByName(serverIp);
				packet = new DatagramPacket(textSend, textSend.length, address, 4500);
				socket.send(packet);
			} catch (IOException e) {
				
			} catch(InterruptedException ie){

			}
		}

		_threadHeartbeat.interrupt();
		_threadReceiveMessage.interrupt();
		System.out.println("\nProgram closed.\n");
		System.exit(0);
	}

	public static Thread StartHeartbeat(InetAddress endereco, DatagramSocket ds) throws IOException {
		return new Thread() {
			
			@Override
			public void run() {
				try{
					String input = "";
					int heartbeat = 5;

					byte[] textSend = new byte[32768];
					DatagramSocket socket = ds;
					DatagramPacket pacote;
					
					while(true){
						heartbeat--;
						if(heartbeat <= 0){				
							heartbeat = 5;
							input = "heartbeat";
							textSend = input.getBytes();
							pacote = new DatagramPacket(textSend, textSend.length, endereco, 4500);
							socket.send(pacote);
						}
						Thread.sleep(1000);
					}
				}
				catch(SocketException se){
					se.printStackTrace();
				} catch(IOException ioe){
					ioe.printStackTrace();
				} catch (InterruptedException e) {
					
				}
			}
		};
	}
    
    public static class Peer {
        InetAddress addr;
		int port;
		int heartbeat;
		Boolean isActive;
		HashMap<String,KFile> files;
        
        public Peer(InetAddress address, int port, int heartbeat){
            SetAddress(address);
			SetPort(port);
			SetHeartbeat(heartbeat);
			SetIsActive(true);
			files = new HashMap<>();
		}
		
        public InetAddress GetAddress(){
            return this.addr;
        }

        public int GetPort(){
            return this.port;
		}
		
		public int GetHeartbeat(){
            return this.heartbeat;
		}

		public Boolean GetIsActive(){
            return this.isActive;
		}

		public HashMap<String,KFile> GetFiles(){
            return this.files;
		}

        public void SetAddress(InetAddress address){
            this.addr = address;
        }

        public void SetPort(int port){
            this.port = port;
		}
		
		public void SetHeartbeat(int heartbeat){
            this.heartbeat = heartbeat;
		}

		public void SetIsActive(Boolean isActive){
            this.isActive = isActive;
		}
    }

	public static class KFile{
		String filename;
		String hash;

		public KFile(String file, String hash){
			SetFileName(file);
			SetHash(hash);
		}

		public String GetFileName(){
			return this.filename;
		}

		public String GetHash(){
			return this.hash;
		}

		public void SetFileName(String fileName){
			this.filename = fileName;
		}

		public void SetHash(String hash){
			this.hash = hash;
		}
	}

	public static class ReceiveMessagesThread extends Thread {
		protected DatagramSocket socket = null;
		
		public ReceiveMessagesThread(DatagramSocket ds) throws IOException {
			socket = ds;
		}

		public void run(){
			while (true) {
				try {
					// Thread.sleep(1000);
					byte[] text = new byte[32768];

					// get response
					DatagramPacket packet = new DatagramPacket(text, text.length);
					socket.setSoTimeout(500);
					socket.receive(packet);

					// show response
					String response = new String(packet.getData(), 0, packet.getLength());
					
					if(response.equals("exit"))
						break;

					System.out.println("[server] " + response);
					System.out.println("\n");
				} catch (IOException e) {
					
				}
			}
			socket.close();
		}
	}

	public static void ShowCommands(){
		System.out.println("\nAvailable options:");
		System.out.println("\tregister peer");
		System.out.println("\tregister file <path/filename>");
		System.out.println("\tlist files");
		System.out.println("\tdisconnect");
		System.out.println("\texit");
	}
}
