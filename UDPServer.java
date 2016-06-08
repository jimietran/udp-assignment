import java.io.* ;
import java.net.* ;

public class UDPServer {

	public static void main(String[] args) throws Exception {
		
		ServerSocket serverSocket;
		
        boolean bool = true;

        try {
        	
            serverSocket = new ServerSocket(9876);
            
            while (bool) {
            	UDPRequest request = new UDPRequest(serverSocket.accept());
            	Thread thread = new Thread(request);
            	thread.start();
            }
            
            serverSocket.close();
            
        } catch (IOException e) {
        	
        	throw new IOException("Cannot find port.");
        	
        }
        
	}

}
