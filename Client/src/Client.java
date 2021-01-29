import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Client 
{
	private static Socket socket;
	
	
	private static final Pattern PATTERN = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
	);
	
	public static boolean validateIpAddress(final String ipAdress) {
		return PATTERN.matcher(ipAdress).matches();
	}
	/*
	 * Application Client
	 */
	public static void askIp() {
		Scanner userInput = new Scanner(System.in);
		
		String ipAdress = "";
		boolean inputValid = false;
		
		System.out.print("Veulliez entrer l'adress IP \n");
		
		while(!inputValid) {
			ipAdress = userInput.nextLine();
			inputValid = validateIpAddress(ipAdress);
		}
		
		userInput.close();
		System.out.print(ipAdress);
	}
	
	public static void askPort() {
		Scanner userInput = new Scanner(System.in);
		
		int port = 0;
		boolean inputValid = false;
		
		System.out.print("Veuillez entrer le port \n");
		
		while(!inputValid) {
			port = userInput.nextInt();
			if(port >=  5000 && port <= 5050) {
				inputValid = true;
			}
		}	

		userInput.close();
		System.out.print(inputValid);
	} 
	
	public static void main(String[] args) throws Exception
	{
		askIp();
	
		askPort();
		
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
