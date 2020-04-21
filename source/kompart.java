// referencia sobre listas: http://tutorials.jenkov.com/java-collections/list.html

import java.io.*;
import java.net.*;
import java.util.*;
 
public class kompart {

    
	public static void main(String[] args) throws IOException {
		String recebido = null;
		String mensagem = null;

		InetAddress endereco;
		int porta;
		byte[] texto = new byte[32768];
		ArrayList<InetAddress> clientsToRemove = new ArrayList<>();
		
		DatagramSocket socket = new DatagramSocket(4500);
		DatagramPacket pacote;

		HashMap<InetAddress,Client> clientes = new HashMap<>();
		int timerHeartbeat = 5;

		while (true) {
			try {
				timerHeartbeat--;
				// recebe datagrama
				pacote = new DatagramPacket(texto, texto.length);
				socket.setSoTimeout(500);
				socket.receive(pacote);

				// processa o que foi recebido
				recebido = new String(pacote.getData(), 0, pacote.getLength());
				endereco = pacote.getAddress();
				porta = pacote.getPort();

				// armazena na variável dados o texto enviado
				String[] dados = recebido.split(" ");

				if(dados[0].equalsIgnoreCase("login")){
					endereco = pacote.getAddress();
					porta = pacote.getPort();

					// adiciona o nick e o endereço do cliente no dicionario
					clientes.put(endereco,new Client(dados[1],endereco,porta, 10));

					// envia a resposta de volta ao cliente
					mensagem = "Usuario logado. Digite 'list'.";
					texto = mensagem.getBytes();
					pacote = new DatagramPacket(texto, texto.length, endereco, porta);
					socket.send(pacote);
				} else if(dados[0].equalsIgnoreCase("list")) {
					// percorre o dicionario e informa os clientes que estão logados
					mensagem = "Usuarios logados: ";
					for (Map.Entry<InetAddress,Client> pair : clientes.entrySet()) {
						if(pair.getKey() != endereco){
							mensagem += pair.getValue().nickname + ", ";
						}
					}
					texto = mensagem.getBytes();
					pacote = new DatagramPacket(texto, texto.length, endereco, porta);
					socket.send(pacote);
				} else if(dados[0].equalsIgnoreCase("exit")){
					if(clientes.containsKey(endereco)){
						clientes.remove(endereco);
					}
					System.out.print("Usuarios logados: ");
					for (Map.Entry<InetAddress,Client> pair : clientes.entrySet()) {
						System.out.println(pair.getValue().GetNickname() + "hearbeat: " + pair.getValue().GetHeartbeat());
					}
					// envia a resposta de volta ao cliente
					mensagem = "Usuario deslogado com sucesso.";
					texto = mensagem.getBytes();
					pacote = new DatagramPacket(texto, texto.length, endereco, porta);
					socket.send(pacote);
				} else if(dados[0].equalsIgnoreCase("break")){
					break;
				} else if(dados[0].equalsIgnoreCase("heartbeat")){
					endereco = pacote.getAddress();
					clientes.get(endereco).SetHeartbeat(10);
				} else if(dados[0].startsWith("/")){
					String nick = dados[0].split("/")[1];
					System.out.println(nick);
					for (Map.Entry<InetAddress,Client> pair : clientes.entrySet()) {
						if(pair.getValue().nickname.equalsIgnoreCase(nick)){
							// pega end do destinatario		
							InetAddress endDest = pair.getValue().GetEndereco();
							// pega porta to destinatario
							int portaDest = pair.getValue().GetPorta();
							// pega end do remetente
							endereco = pacote.getAddress();
							// pega mensagem e concatena com o nick
							String msg = new String(pacote.getData(), 0, pacote.getLength());
							int indexOf = msg.indexOf(" ", 0);
							msg = msg.substring(indexOf);
							msg = "Msg de " + clientes.get(endereco).GetNickname() + ": " + msg;
							// converte a mensagem
							texto = msg.getBytes();
							// envia pacote
							pacote = new DatagramPacket(texto, texto.length, endDest, portaDest);
							socket.send(pacote);
							break;
						}
					}
				} else {
					// envia a resposta de volta ao cliente
					mensagem = "Comando não identificado. Tente 'login nick' ou list.";
					texto = mensagem.getBytes();
					pacote = new DatagramPacket(texto, texto.length, endereco, porta);
					socket.send(pacote);
				}
			} catch (IOException e) {
				if(timerHeartbeat <= 0){
					timerHeartbeat = 5;
					for (Map.Entry<InetAddress,Client> pair : clientes.entrySet()) {
						// decrementa o hearbeat;
						int hearbeat = pair.getValue().GetHeartbeat();
						pair.getValue().SetHeartbeat(hearbeat-1);
						System.out.println(pair.getValue().GetNickname() + " hearbeat: " + pair.getValue().GetHeartbeat());
						// marca os clients para remover se heartbeat for 0
						if(pair.getValue().GetHeartbeat() == 0){
							clientsToRemove.add(pair.getKey());
						}
					}

					// remove os clientes
					for (InetAddress inetAddress : clientsToRemove) {
						System.out.println("removendo: " + inetAddress);
						clientes.remove(inetAddress);
					}

					// limpa a lista de clientes para remover
					clientsToRemove.clear();

					System.out.println("Usuarios logados:");
					for (Map.Entry<InetAddress,Client> pair : clientes.entrySet()) {
						System.out.println(pair.getValue().GetNickname() + " hearbeat: " + pair.getValue().GetHeartbeat());
					}
				}
				
			}
		}

		socket.close();
		System.out.println("Servidor finalizado...");
    }
    
    public static class Peer {
        String nick;
        InetAddress addr;
		int port;
		int heartbeat;
		Boolean isActive;
		Hashmap<String,File>;
        
        public Peer(String nickname, InetAddress address, int port, int heartbeat){
            SetNickname(nickname);
            SetAddress(address);
			SetPort(port);
			SetHeartbeat(heartbeat);
        }
        
        public String GetNickname(){
            return this.nick;
        }

        public InetAddress GetAddress(){
            return this.addr;
        }

        public int GetPort(){
            return this.port;
        }

        public void SetNickname(String nickname){
            this.nick = nickname;
        }

        public void SetAddress(InetAddress address){
            this.addr = address;
        }

        public void SetPort(int port){
            this.port = port;
		}
		
		public int GetHeartbeat(){
            return this.heartbeat;
		}
		
		public void SetHeartbeat(int heartbeat){
            this.heartbeat = heartbeat;
		}
    }

	public static class File{
		String filename;
        String path;
		String hash;
		byte[] size;

		public File(String file, String path, String hash, byte[] size){
			SetFileName(file);
			SetPath(path);
			SetHash(hash);
			SetSize(size);
		}

		public String GetFileName(){
			return this.filename;
		}

		public String GetPath(){
			return this.path;
		}

		public String GetHash(){
			return this.path;
		}

		public byte[] GetSize(){
			return this.size;
		}

		public void SetFileName(String fileName){
			this.filename = fileName;
		}

		public void SetPath(String path){
			this.path = path;
		}

		public void SetHash(String hash){
			this.hash = hash;
		}

		public void SetSize(byte[] size){
			this.size = size;
		}

	}
}
