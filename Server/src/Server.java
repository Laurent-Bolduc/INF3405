import java.io.DataOutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.*;

public class Server {

	private static ServerSocket listener;
	private static String serverPath = System.getProperty("user.dir") + "\\";
	private static String serverAddress = "127.0.0.1";
	private static int port = 5000;

	// Fonction pour print sur le server
	private static void log(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) throws Exception {
		// compteur compte a chaque connexion dun client
		int clientNumber = 0;

		if (!(System.console() == null)) {
			// IP
			Server.log("Enter IP Address of the Server:");
			serverAddress = System.console().readLine();
			while (!Server.validateIpAddress(serverAddress)) {
				Server.log("Wrong IP Address. Enter another one:");
				serverAddress = System.console().readLine();
			}

			// Port
			Server.log("Enter Port for the server :");
			port = Integer.parseInt(System.console().readLine());
			while (!Server.validatePort(port)) {
				Server.log("Wrong Port. Should be between 5000 and 5050. Enter another one:");
				port = Integer.parseInt(System.console().readLine());
			}
		} else
			System.out.format("No console was found, default values were assigned%n");

		// Assotiation de ladresse et du port a la connexion
		InetAddress serverIP = InetAddress.getByName(serverAddress);

		// creation of listening socket
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverIP, port));

		System.out.format("The server is running on %s:%d%n", serverAddress, port);

		try {
			// Important: la fonction accept() est bloquante : attend qu'un prochain client
			// se connecte
			// Une nouvelle connection: on incremente le compteur clientNumber
			while (true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}

		} finally {
			// Fermeture de la connexion
			listener.close();
		}
	}

	private static class ClientHandler extends Thread {
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

			} catch (Exception e) {
				System.out.println("Error handling client#" + clientNumber + ": " + e);
			} 
		}

		public void commandSelector(DataInputStream in, DataOutputStream out) throws Exception {
			int command = 0;
			String line = "";
			String[] inputs = new String[] {};
			// wait for the client to enter a command
			while (command == 0) {
				if (line == "") {
					try {
						// read the command that the client entered and split it
						line = in.readUTF();
						inputs = line.split(" ");
						
						// write in the server console everything needed 
						System.out.println("[" + serverAddress + ":" + port + " - " + java.time.LocalDate.now() + "@"
								+ java.time.LocalTime.now().getHour() + ":" + java.time.LocalTime.now().getMinute()
								+ ":" + java.time.LocalTime.now().getSecond() + "] : " + line);
					} catch (Exception e) {

					}
				}
				
				if (inputs.length == 0)
					continue;

				// switch case with the different commands available
				// using the split line that the users entered
				switch (inputs[0]) {
					case "cd":
						cdCommand(out, inputs);
						break;
					case "ls":
						lsCommand(out, inputs, false, false);
						out.writeUTF("\n");
						break;
					case "mkdir":
						mkdirCommand(out, inputs);
						break;
					case "upload":
						uploadCommand(in, out, inputs);
						break;
					case "download":
						downloadCommand(out, inputs);
						break;
					case "exit":
						System.exit(0);
						break;
					case "help":
						helpCommand(out);
						break;
					default:
						out.writeUTF("Command not found, type help for help\n");
						break;
				}
				// reset the values
				line = "";
				inputs = new String[] {};
			}

		}

		public void cdCommand(DataOutputStream out, String[] inputs) throws Exception {
			// verifies that the user has entered a name of directory
			if (inputs.length == 1) {
				out.writeUTF("No directory name was typed\n");
				return;
			} else if (inputs[1].equals("..")) {
				// splits the path where there are "\" in the path if the second argument is ".."
				String[] splitPath = path.split("\\\\");
				String newPath = "";
				// create a new path with all the split elements except for the last one
				for (int i = 0; i < splitPath.length - 1; i++) {
					newPath += splitPath[i] + "\\";
				}
				path = newPath;
				System.out.println("HEAD on " + splitPath[splitPath.length - 2]);
			} else if (!lsCommand(out, inputs, true, false).contains(inputs[1])) {
				// error message if the directory is not found 
				out.writeUTF("No directory with that name was found\n");
				return;
			} else {
				// sets the new path if there is no problem
				path += inputs[1] + "\\";
				System.out.println("HEAD on " + inputs[1]);
			}
			out.writeUTF("The current directory is now " + path);
		}

		
		public void mkdirCommand(DataOutputStream out, String[] inputs) throws Exception {
			// verifies that the client entered the name of the new directory
			if (inputs.length == 1) {
				out.writeUTF("No directory name was typed\n");
				return;
			}
			
			// Creates the new file with the right path 
			File file = new File(path + inputs[1]);

			// Created the directory
			if (file.mkdir()) {
				// Success message
				out.writeUTF("Directory created\n");
			} else {
				// Failure message
				out.writeUTF("An Error has Occurred\n");
			}
		}

		// From
		// https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder#:~:text=Create%20a%20File%20object%2C%20passing,method%20to%20get%20the%20filename.
		public List<String> lsCommand(DataOutputStream out, String[] inputs, boolean isCd, boolean isDownload) throws Exception {
			File currentFolder = new File(path);
			// get all the files
			File[] listOfFiles = currentFolder.listFiles();
			List<String> files = new ArrayList<String>();
			// loop through all the files 
			for (int i = 0; i < listOfFiles.length; i++) {
				// returns a list of files for cd validation
				if (isCd) {
					if (listOfFiles[i].isDirectory())
						files.add(listOfFiles[i].getName());
				// returns a list of files for download validation
				} else if (isDownload) {
					if (listOfFiles[i].isFile())
						files.add(listOfFiles[i].getName());
				} else if (listOfFiles[i].isFile()) {
					// if it is a file, it prints as a file
					out.writeUTF("File " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					// if it is a directory, it prints as a directory
					out.writeUTF("Directory " + listOfFiles[i].getName());
				}
			}
			return files;
		}

		public void uploadCommand(DataInputStream in, DataOutputStream out, String[] inputs) throws Exception {
			// verifies that the user entered the name of the file to upload 
			if (inputs.length == 1) return;
			if (in.available() == 0) {
				TimeUnit.MILLISECONDS.sleep(100);
	            if (in.available() == 0) return;
	        }
			File file = new File(path + inputs[1]);
			
			FileOutputStream fos = new FileOutputStream(file);
	    	byte[] buffer = new byte[8192];

	        int count;
	        while ((count = in.read(buffer)) > 0) {
	            fos.write(buffer, 0, count);
	            if (in.available() == 0) {
	    			TimeUnit.MILLISECONDS.sleep(100);
	                if (in.available() == 0) break;
	            }
	        }
	        
	        fos.close();
		}

		private void downloadCommand(DataOutputStream out, String[] inputs) throws Exception {
			// verifies that the user entered the name of the file to download 
			if (inputs.length == 1) return;
			if (!lsCommand(out, inputs, false, true).contains(inputs[1])) {
				TimeUnit.MILLISECONDS.sleep(1000);
				out.writeUTF("No file with that name was found in the current folder");
				return;
			}
			File file = new File(path + inputs[1]);
		    // Get the size of the file
		    byte[] buffer = new byte[8192];
		    InputStream fis = new FileInputStream(file);
		    
		    int count;
	        while ((count = fis.read(buffer)) > 0) {
	            out.write(buffer, 0, count);
	        }
			TimeUnit.MILLISECONDS.sleep(500);

	        fis.close();
		}

		// prints the different commands that are available
		private void helpCommand(DataOutputStream out) throws Exception {
			out.writeUTF("ls : lists every files in current directory \ncd : change the current directory\nmkdir : create a new directory \ndownload : download a file from the Server to the Client \nupload : upload a file from the Client to the Server\nexit : end the server process\n");
		}
	}

	// pattern of an IP address
	private static final Pattern PATTERN = Pattern
			.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	// validation of Ip address
	public static boolean validateIpAddress(final String ipAdress) {
		return PATTERN.matcher(ipAdress).matches();
	}

	// validation of the port
	public static boolean validatePort(final int port) {
		if (port >= 5000 && port <= 5050) {
			return true;
		} else {
			return false;
		}
	}

}