import java.io.DataOutputStream;
import java.util.regex.Pattern;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private static ServerSocket listener;
	
	public static void main(String[] args) throws Exception
	{
		// compteur compte a chaque connexion dun client
		int clientNumber = 0;
		
		//Adresse et port du serveur
		String serverAddress = "127.0.0.1";
		int serverPort = 5000;
		
		
		while (!Server.validateIpAddress(serverAddress)){
            Server.log("Wrong IP Address. Enter another one:");
        	serverAddress = System.console().readLine();
        }
		
		// creation de la connexion pour communiquer avec les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		
		
		//Assotiation de ladresse et du port a la connexion
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			// Important: la fonction accept() est bloquante : attend qu'un prochain client se connecte
			// Une nouvelle connection: on incremente le compteur clientNumber
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
			
			
		}finally {
			// Fermeture de la connexion
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private int clientNumber;
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				out.writeUTF("Hello from server - you are client#" + clientNumber);
			}catch(IOException e) {
				System.out.println("Error handling client#" + clientNumber + ": " + e);
			}finally {
				try {
					socket.close();
				}catch (IOException e) {
					System.out.println("Couldn't close the socket, what's going on?");
				}
				System.out.println("Connection with client#" + clientNumber + " closed");
			}
		}
	}
	
	private static final Pattern PATTERN = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	public static boolean validateIpAddress(final String ipAdress) {
		return PATTERN.matcher(ipAdress).matches();
	}
	
   private static void log(String message) {
        System.out.println(message);
    }

	
}