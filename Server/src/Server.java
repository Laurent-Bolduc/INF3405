

public class Server {
	
	public static void main(String[] args)
	{
		
		ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4444);
            System.out.print("should work");
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(1);
        }
	}
}
