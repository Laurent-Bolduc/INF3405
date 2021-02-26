import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;


public class Client 
{
	private static Socket socket;
	private static String clientPath = System.getProperty("user.dir") + "\\";
	
	
	//Fonction pour print sur le server
	private static void log(String message) {
        System.out.println(message);
    }
	
	public static void main(String[] args) throws Exception
	{
		//IP address 
		String serverAddress = "127.0.0.1";
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
		
		// Cr�ation d'une nouvelle connexion avec le serveur
		socket = new Socket(serverAddress, port);
		
		System.out.format("The server is running on %s:%d%n", serverAddress, port);
		
		// Cr�ation d'un canal entrant pour recevoir les messages envoy�s par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// Attente de la r�ception d'un message envoy� par le serveur sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		while (true) {
			System.out.print("> ");
			String currentCommand = "";
			currentCommand = System.console().readLine();
			if (currentCommand == "") continue;
			out.writeUTF(currentCommand);
			commandCompletion(in, out, currentCommand);
			TimeUnit.MILLISECONDS.sleep(500);
			while(in.available() != 0)
			{
				String serverComs = in.readUTF();
				if (serverComs.isEmpty()) break;
				System.out.println(serverComs);
			}
		}

		// Fermeture de la connexion avec le serveur
	}
	
	public static void commandCompletion(DataInputStream in, DataOutputStream out, String currentCommand) throws Exception {
		String[] inputs = new String[] {};
		try { inputs = currentCommand.split(" "); } catch(Exception e) {}

		if (inputs.length == 0) return;
			
		switch(inputs[0]) {
			case "upload":
				uploadFile(out, inputs);
				break;
			case "download":
				downloadFile(in, inputs);
				break;
			case "exit":
				System.exit(0);
				break;
			default:
				break;
		}
	}
	
    // Upload file to server from where client jar file is run to where server jar file is run.
    private static void uploadFile(DataOutputStream out, String[] inputs) throws Exception {
		// verifies that the user entered the name of the file to upload 
		if (inputs.length == 1) {
			System.out.println("No file name was typed");
			return;
		}
		
		//checks if the file is present
		if (!fileSearch().contains(inputs[1])) {
			TimeUnit.MILLISECONDS.sleep(1000);
			System.out.println("No file with that name was found on the client");
			return;
		}
		File file = new File(clientPath + "\\" + inputs[1]);
	    // Get the size of the file
	    byte[] buffer = new byte[8192];
	    InputStream fis = new FileInputStream(file);
	    
	    int count;
        //reads the file to write in the socket
        while ((count = fis.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
		TimeUnit.MILLISECONDS.sleep(500);

        fis.close();
		System.out.println("File uploaded successfully");

    }
	
    // Download file from server from where server location to where client jar file is run.
    private static void downloadFile(DataInputStream in, String[] inputs) throws Exception {    	
		// verifies that the user entered the name of the file to download 
		if (inputs.length == 1) {
			System.out.println("No file name was typed");
			return;
		}
		
		//checks if the server found the file
        if (in.available() == 0) {
			TimeUnit.MILLISECONDS.sleep(100);
            if (in.available() == 0) {
            	System.out.println(in.readUTF());
            	return;
            }
        }
    	
    	FileOutputStream fos = new FileOutputStream(clientPath + "\\" + inputs[1]);
    	byte[] buffer = new byte[8192];

        int count;
        
        //reads the socket to write in the file
        while ((count = in.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
            if (in.available() == 0) {
    			TimeUnit.MILLISECONDS.sleep(100);
                if (in.available() == 0) break;
            }
        }
        
        
        fos.close();
		System.out.println("File downloaded successfully");
    }
	
    private static List<String> fileSearch() {
    	File currentFolder = new File(clientPath);
		// get all the files
		File[] listOfFiles = currentFolder.listFiles();
		List<String> files = new ArrayList<String>();
		// loop through all the files 
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile())
				files.add(listOfFiles[i].getName());
		}
		return files;
    }
    
	private static final Pattern PATTERN = Pattern.compile(
			"^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
	);
	
	//Fonctions en lien avec la validation de l'adresse IP
	public static boolean validateIpAddress(final String ipAdress) {
		return PATTERN.matcher(ipAdress).matches();
	}
	
	//Fonctions en lien avec la validation du port entre 5000 et 5050
	public static boolean validatePort(final int port) {
		if (port >= 5000 && port <= 5050){
			return true;
		}
		else {
			return false;
		}
	}
}
