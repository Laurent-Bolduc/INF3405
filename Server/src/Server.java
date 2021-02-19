import java.io.DataOutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;

import javax.imageio.spi.ImageInputStreamSpi;

public class Server {
	
	private static ServerSocket listener;
	private static String serverPath = System.getProperty("user.dir") + "\\";

	//Fonction pour print sur le server
	private static void log(String message) {
        System.out.println(message);
    }

	public static void main(String[] args) throws Exception
	{
		// compteur compte a chaque connexion dun client
		int clientNumber = 0;
		
		String serverAddress = "127.168.0.1";
		int port = 5000;
		
		
		if (!(System.console() == null)) {
			//IP
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
 		InetAddress serverIP = InetAddress.getByName(serverAddress);
 		
 		//creation of listening socket
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
			
			
		} finally {
			// Fermeture de la connexion
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private int clientNumber;
		private String path = new String(serverPath);
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		public void run() {
			try {
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Hello from server - you are client#" + clientNumber);		 
				commandSelector(in, out);

				} catch(Exception e) {
				System.out.println("Error handling client#" + clientNumber + ": " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Couldn't close the socket, what's going on?");
				}
				System.out.println("Connection with client#" + clientNumber + " closed");
			}
		}
		
		
		public void commandSelector(DataInputStream in, DataOutputStream out) throws Exception {
			int command = 0;
			String line = "";
			String[] inputs = new String[] {};
			while(command == 0) {

				if (line == "") {
					try {
					line = in.readUTF();
					inputs = line.split(" ");
					//Debug
					System.out.println(line);
					} catch(Exception e) {
					}
				}
				if (inputs.length == 0) continue;
				
				switch(inputs[0]) {
				case "cd":
					cdCommand(out, inputs);
					break;
				case "ls":
					lsCommand(out, inputs, false);
					out.writeUTF("\n");
					break;
				case "mkdir":
					mkdirCommand(out, inputs);
					break;
				case "upload":
					uploadCommand(out, inputs);
					break;
				case "download":
					downloadCommand(out, inputs);
					break;
				case "exit":
					System.exit(0);
				case "help":
					helpCommand(out);
				default:
					out.writeUTF("Command not found, type help for help\n");
					break;
				}
				line = "";
				inputs = new String[] {};
			}
			
		}
		
		public void cdCommand(DataOutputStream out, String[] inputs) throws Exception {
			if (inputs.length == 1) {
				out.writeUTF("No directory name was typed\n");
				return;
			}
			else if (inputs[1].equals("..")) {
				String[] splitPath = path.split("\\\\");
				String newPath = "";
				for (int i = 0; i < splitPath.length - 1; i++) {
					newPath += splitPath[i] + "\\";
				}
				path = newPath;
				System.out.println("HEAD on " + splitPath[splitPath.length - 2]);				
			}
			else if (!lsCommand(out, inputs, true).contains(inputs[1])) {
				out.writeUTF("No directory with that name was found\n");
				return;
			}
			else {
				path += inputs[1] + "\\";
				System.out.println("HEAD on " + inputs[1]);
			}
			out.writeUTF("The current directory is now " + path);
		}
		
		public void mkdirCommand(DataOutputStream out, String[] inputs) throws Exception {			
			if (inputs.length == 1) {
				out.writeUTF("No directory name was typed\n");
				return;
			}
			File file = new File(path + inputs[1]);
			
			if(file.mkdir()) {
				System.out.println("work");
				out.writeUTF("Directory created\n");
			} else {
				System.out.println("pas work");
				out.writeUTF("An Error has Occurred\n");
			}
		}
		
		
		// From https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder#:~:text=Create%20a%20File%20object%2C%20passing,method%20to%20get%20the%20filename.
		public List<String> lsCommand(DataOutputStream out, String[] inputs, boolean isCd) throws Exception {
			File currentFolder = new File(path);
			File[] listOfFiles = currentFolder.listFiles();
			List<String> directories = new ArrayList<String>();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (isCd) {
					if (listOfFiles[i].isDirectory())
						directories.add(listOfFiles[i].getName());
				} 
				else if (listOfFiles[i].isFile()) {
					System.out.println("File " + listOfFiles[i].getName());
					out.writeUTF("File " + listOfFiles[i].getName());
				} 
				else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
					out.writeUTF("Directory " + listOfFiles[i].getName());
			  }
			}
			return directories;
		}
		
		//Fonciton pour upload un fichier
		public void uploadCommand(DataOutputStream out, String[] inputs) throws Exception {
			//create socket
			if (inputs.length == 1) {
				out.writeUTF("No directory name was typed\n");
				return;
			}else {
				String fileName = inputs[1];
				String filePath = path+fileName;
				
				System.out.println(fileName);
				System.out.println(filePath);
				
				File file = new File(filePath);
				long length = file.length();
				System.out.println(length);
			
				byte[] buff = new byte[16*2024];
				DataOutputStream output =  new DataOutputStream(socket.getOutputStream());
	    		FileInputStream input = new FileInputStream(file.toString());
	    		
	    		output.writeLong(file.length());
	    		int data;
	    		while ((data = input.read(buff)) > 0) {
	    			output.write(buff, 0, data);
	    		}
	    		input.close();
	    		System.out.println( fileName + " has been uploaded successfully.");
			}

			
		}
		
		private void downloadCommand(DataOutputStream out, String[] inputs) throws Exception{
			if( inputs.length ==1) {
				out.writeUTF("No file name was typed\n");
				return;
			}else {
				String fileName = inputs[1];
				String filePath = path+fileName;
				File file = new File(filePath);
				
				if(!(file.isFile())) {
					out.writeUTF(fileName + " does not exist\n");
					return;
				}else {
					byte[] buff = new byte[16*2024];
					DataOutputStream output =  new DataOutputStream(socket.getOutputStream());
		    		FileInputStream input = new FileInputStream(file.toString());
		    		
		    		int fileDataSize = input.read();
		    		int read = 0;
		    		
		    		while(fileDataSize > 0 && (read = input.read(buff)) > 0) {
		    			output.write(buff, 0, read);
		    			fileDataSize -= read;
		    		}
		    		output.close();
		    		System.out.println(fileName + " downloaded successfully.");	
				}	
			}
		}
		
		private void helpCommand(DataOutputStream out) throws Exception {
			
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