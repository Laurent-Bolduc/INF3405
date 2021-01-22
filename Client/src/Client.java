import java.io.DataInputStream;
import java.net.Socket;

public class Client 
{
	private static Socket socket;
	
	/*
	 * Application Client
	 */
	public static void main(String[] args) throws Exception
	{
		// Adresse et port du serveur
		String serverAddress = "127.0.0.1";
		int port = 5000;
		
		// Création d'une nouvelle connexion avec le serveur
		socket = new Socket(serverAddress, port);
		
		System.out.format("The server is running on %s:%d%n", serverAddress, port);
		
		// Création d'un canal entrant pour recevoir les messages envoyés par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		// Attente de la réception d'un message envoyé par le serveur sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		// Fermeture de la connexion avec le serveur
		socket.close();
	}
}
