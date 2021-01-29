import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Client 
{
	private static Socket socket;
	
	//Fonction pour print sur le server
	private static void log(String message) {
        System.out.println(message);
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
		//IP address 
		String serverAddress = "127.168.0.1";
		int port = 5000;
		if (!(System.console() == null)) {
			Client.log("Enter IP Address of the Server:");
			serverAddress = System.console().readLine();
			while (!Client.validateIpAddress(serverAddress)){
				Client.log("Wrong IP Address. Enter another one:");
	        	serverAddress = System.console().readLine();
	        }
			
			//Port
	        Client.log("Enter Port for the server :");
	        port = Integer.parseInt(System.console().readLine());
	        while (!Client.validatePort(port)){
	        	Client.log("Wrong Port. Should be between 5000 and 5050. Enter another one:");
	            port = Integer.parseInt(System.console().readLine());
	        }
		} 
		else 
			System.out.format("No console was found, default values were assigned%n");
		
		// Création d'une nouvelle connexion avec le serveur
		socket = new Socket(serverAddress, port);
		
		System.out.format("The server is running on %s:%d%n", serverAddress, port);
		
		// Création d'un canal entrant pour recevoir les messages envoyés par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// Attente de la réception d'un message envoyé par le serveur sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		while (true) {
			out.writeUTF(System.console().readLine());		 
		}

		
		// Fermeture de la connexion avec le serveur
	}
	
	private static final Pattern PATTERN = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
	);
	
	//Fonctions en lien avec la validation de l'adresse IP
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
