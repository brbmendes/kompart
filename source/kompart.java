// referencia sobre listas: http://tutorials.jenkov.com/java-collections/list.html

import java.io.*;
import java.net.*;
import java.util.*;
 
public class kompart {
	static Thread _threadHeartbeat;
	static int _defaultHeartBeat;
    
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

				if(splittedReceived[0].equalsIgnoreCase("registry")){
					if(splittedReceived[1].equals("peer")){
						// registry peer

						// Check if peer has registered before
						if(peers.containsKey(address)){
							message = "Peer " + address + " already registered.";
						} else {
							// add IP to map of peers
							peers.put(address,new Peer(address, port, _defaultHeartBeat));

							// send response to peer
							message = "Peer registered.";
						}

						text = message.getBytes();
						packet = new DatagramPacket(text, text.length, address, port);
						socket.send(packet);
					} else {
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
						text = message.getBytes();
						packet = new DatagramPacket(text, text.length, address, port);
						socket.send(packet);
					}
				} else if(splittedReceived[0].equalsIgnoreCase("list") && splittedReceived[1].equalsIgnoreCase("files")) {
					// List all available files
					message = "Available files: \n";
					byte[] originalMessage = message.getBytes();
					
					for (Map.Entry<InetAddress,Peer> peer : peers.entrySet()) {
						if(peer.getKey() != address){
							message += peer.getValue().GetAddress() + "\n";
							for(Map.Entry<String,KFile> file : peer.getValue().GetFiles().entrySet()){
								message += "\t- filename: " + file.getValue().GetFileName() + " , hash: " + file.getValue().GetHash();
							}
						}
					}

					if(Arrays.equals(originalMessage, message.getBytes())){
						message = "No registered files.";
					}
					
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, packet.getAddress(), packet.getPort());
					socket.send(packet);
				} else if(splittedReceived[0].equalsIgnoreCase("exit")){
					if(peers.containsKey(address)){
						peers.remove(address);
					}
					// send response to peer
					message = "Peer removed.";
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
				} else if(splittedReceived[0].equalsIgnoreCase("break")){
					break;
				} else if(splittedReceived[0].equalsIgnoreCase("heartbeat")){
					address = packet.getAddress();
					peers.get(address).SetHeartbeat(_defaultHeartBeat);
				} else {
					// send response to peer
					message = "Unrecognized command.";
					text = message.getBytes();
					packet = new DatagramPacket(text, text.length, address, port);
					socket.send(packet);
				}
			} catch (IOException e) {
				if(timerHeartbeat <= 0){
					timerHeartbeat = 5;
					for (Map.Entry<InetAddress,Peer> pair : peers.entrySet()) {
						// decrement hearbeat;
						int hearbeat = pair.getValue().GetHeartbeat();
						pair.getValue().SetHeartbeat(hearbeat-1);
						System.out.println(pair.getValue().GetAddress() + " hearbeat: " + pair.getValue().GetHeartbeat());
						// mark peers to remove if heartbeat equals 0
						if(pair.getValue().GetHeartbeat() == 0){
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
		System.out.println("Servidor finalizado...");
	}
	
	public static void RunPeer(String serverIp)  throws IOException {
		int a = 2;
		System.out.println("entrou no client. ip: " + serverIp);
		if(a == 2){
			return;
		}
		
		String input = "";
		Scanner in = new Scanner(System.in);
		byte[] texto = new byte[32768];
		byte[] textoSend = new byte[32768];
		// cria um socket datagrama
		DatagramSocket socket = new DatagramSocket();

		System.out.println("Bem vindo ao chat.\nPara logar digite 'login nickname'\n");
		do{
			input = in.nextLine();
			if(!input.contains("login")){
				System.out.println("Para logar digite 'login nickname'\n");
			}
		} while(!input.contains("login"));

		// envia um pacote
		textoSend = input.getBytes();
		InetAddress endereco = InetAddress.getByName(serverIp);
		DatagramPacket pacote = new DatagramPacket(textoSend, textoSend.length, endereco, 4500);
		socket.send(pacote);

		// Inicia thread do Heartbeat
		_threadHeartbeat = StartHeartbeat(endereco);
		_threadHeartbeat.start();

		while (true) {
			try {
				if(input.equalsIgnoreCase("break")){
					// fecha o socket
					socket.close();
					// fecha o scanner
					in.close();
					break;
				}
				
				texto = new byte[32768];
				// obtem a resposta
				pacote = new DatagramPacket(texto, texto.length);
				socket.setSoTimeout(500);
				socket.receive(pacote);
				
				// mostra a resposta
				String resposta = new String(pacote.getData(), 0, pacote.getLength());
				System.out.println(resposta);
				
				if(input.equalsIgnoreCase("exit")){
					// fecha o socket
					socket.close();
					// fecha o scanner
					in.close();
					break;
				}

				// o client fica preso na input, esperando digitar.
				
				input = in.nextLine();

				textoSend = input.getBytes();
				endereco = InetAddress.getByName(serverIp);
				pacote = new DatagramPacket(textoSend, textoSend.length, endereco, 4500);
				socket.send(pacote);
			} catch (IOException e) {
				
			}
		}
		_threadHeartbeat.interrupt();
		System.out.println("\nPrograma encerrado.\n");
	}

	public static Thread StartHeartbeat(InetAddress endereco) throws IOException {
		return new Thread() {
    
			@Override
			public void run() {
				try{
					String input = "";
					int heartbeat = 5;

					byte[] textoSend = new byte[32768];
					DatagramSocket socket = new DatagramSocket();
					DatagramPacket pacote;
					
					while(true){
						heartbeat--;
						if(heartbeat <= 0){				
							heartbeat = 5;
							input = "heartbeat";
							textoSend = input.getBytes();
							pacote = new DatagramPacket(textoSend, textoSend.length, endereco, 4500);
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
}
