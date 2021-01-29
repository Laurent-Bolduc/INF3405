import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class Server {
	
	private static ServerSocket listener;
	
	//Fonction pour print sur le server
	private static void log(String message) {
        System.out.println(message);
    }
	

	public static void main(String[] args) throws Exception
	{
		// compteur compte a chaque connexion dun client
		int clientNumber = 0;
		
		//IP address 
		String serverAddress = "127.168.0.1";
		int port = 5000;
		if (!(System.console() == null)) {
			Server.log("Enter IP Address of the Server:");
			serverAddress = System.console().readLine();
			while (!Server.validateIpAddress(serverAddress)){
	            Server.log("Wrong IP Address. Enter another one:");
	        	serverAddress = System.console().readLine();
	        }
			
			//Port
	        Server.log("Enter Port for the server :");
	        port = Integer.parseInt(System.console().readLine());
	        while (!Server.validatePort(port)){
	            Server.log("Wrong Port. Should be between 5000 and 5050. Enter another one:");
	            port = Integer.parseInt(System.console().readLine());
	        }
		} 
		else 
			System.out.format("No console was found, default values were assigned%n");
        
		
		//Assotiation de ladresse et du port a la connexion
     // creation de la connexion pour communiquer avec les clients
 		InetAddress serverIP = InetAddress.getByName(serverAddress);
 		listener = new ServerSocket();
 		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverIP, port));
		
		System.out.format("The server is running on %s:%d%n", serverAddress, port);
		
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
	
	//Fonctions en lien avec la validation de l'adresse IP
	private static final Pattern PATTERN = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	public static boolean validateIpAddress(final String ipAdress) {
		return PATTERN.matcher(ipAdress).matches();
	}
	
	
	//Fonctions en lien avec la validation du port entre 5000 et 5500
		public static boolean validatePort(final int port) {
			if (port >= 5000 && port <= 5500){
				return true;
			}
			else {
				return false;
			}
		}
   

	
}