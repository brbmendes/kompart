import java.io.*;
import java.net.*;
import java.util.*;

public class EchoListClient {
	static Thread threadHeartbeat;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Uso: java EchoListClient <server ip>");
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
		InetAddress endereco = InetAddress.getByName(args[0]);
		DatagramPacket pacote = new DatagramPacket(textoSend, textoSend.length, endereco, 4500);
		socket.send(pacote);

		// Inicia thread do Heartbeat
		threadHeartbeat = StartHeartbeat(endereco);
		threadHeartbeat.start();

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
				endereco = InetAddress.getByName(args[0]);
				pacote = new DatagramPacket(textoSend, textoSend.length, endereco, 4500);
				socket.send(pacote);
			} catch (IOException e) {
				
			}
		}
		threadHeartbeat.interrupt();
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
}
