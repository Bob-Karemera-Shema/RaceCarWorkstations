import java.io.*;
import java.net.*;

public class Server
{
	public static void main( String args[] )
	{
      int maxClients = 2;
      int activeClients = 0;
   
		// Declare a server socket and a client socket for the server
		ServerSocket service = null;
		Socket server = null;
      
      ClientHandler[] handlers = new ClientHandler[maxClients];
     
		// Try to open a server socket on port 5000
		try
		{
			service = new ServerSocket(5000);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}  

		// Create a socket object from the ServerSocket to listen and accept
		// connections. Open input and output streams
		try
		{
         do 
         {
   			server = service.accept();
           
   			ClientHandler handler = new ClientHandler(server);
            
            Thread t = new Thread(handler);
            t.start();
            
            handlers[activeClients] = handler;
            
            activeClients++;
            
            if (activeClients == maxClients) 
            {
               break;
            }
         } while (true);
		}  
		catch (IOException e)
		{
			System.out.println(e);
		}
      
      while (true) 
      {
         // keep server open and alive as long as we have active client connections
         boolean allClientsAreActive = false;
         
         for (int i = 0; i < activeClients; i++) 
         {
            ClientHandler handler = handlers[i];
            
            if (handler.isAlive()) 
            {
               allClientsAreActive = true;
               break;
            }
         }
         
         if (!allClientsAreActive) 
         {
            break;
         }
         
         try { Thread.sleep(1000); } catch (InterruptedException e) {}
      }
	}
}
