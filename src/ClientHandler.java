import java.lang.*;
import java.io.*;
import java.net.*;

class ClientHandler implements Runnable
{
   private Socket server = null;
   // Declare an input stream and String to store message from client		
	private BufferedReader inputStream;
	private String line;
	// Declare an output stream to client		
	private DataOutputStream outputStream;
   
   private ObjectInput objectInput = null;
   private ObjectOutput objectOutput = null;

   private static Kart kartBlue = null;
   private static Kart kartRed = null;
   
   // "blue" / "red"
   private String kartType;

   private boolean alive = true;

   public ClientHandler(Socket server)
   {
      this.server = server;
   }
   
   public void run()
   {
      try 
      {
         inputStream = new BufferedReader(
   			new InputStreamReader(
   				server.getInputStream()
   			)
   		);
   		
   		outputStream = new DataOutputStream(
   			server.getOutputStream()
   		);
         
         objectInput = new ObjectInputStream(
            server.getInputStream()
         );
         
         objectOutput = new ObjectOutputStream(
            server.getOutputStream()
         );
                  
         do
         {
            line = receiveMessage();
         
   			if(line != null)
   			{
   				handleClientResponse(line);
   			}
            
            if ( line.equals("CLOSE") )
            {
               break;
            }
            
            try { Thread.sleep(1); } catch (InterruptedException e) {}
         } while(true);
   		
   		// Comment out/remove the outputStream and server close statements if server should remain live
   		outputStream.close();
   		inputStream.close();
         objectOutput.close();
         objectInput.close();
   		server.close();
      } 
      catch (Exception e) 
      {
         System.out.println("TCPClientHandler Exception: " + e.getMessage());
      }
      
      alive = false;
   }
   
   public boolean isAlive() 
   {
      return alive;
   }
   
   private void sendMessage(String message) 
   {
      try 
      {
         outputStream.writeBytes(message + "\n");
      } catch (Exception e) 
      {}
   }
   
   private String receiveMessage() 
   {
      try 
      {
         return inputStream.readLine();
      } catch (Exception e) 
      {
         return null;
      }
   }
   
   private void sendKart(Kart kartToSend) 
   {
      try 
      {
         objectOutput.writeObject(kartToSend);
         objectOutput.flush();
      } catch (Exception e) 
      {}
   }

   private void sendForeignKart()
   {
      Kart kartToSend = null;

      sendMessage("foreign_kart_update");

      switch (kartType)
      {
         case "Blue":
            kartToSend = kartRed;
            break;

         case "Red":
            kartToSend = kartBlue;
            break;
      }

      sendKart(kartToSend);
   }
   
   private void receiveKart()
   {
      Kart inputKart = null;
      
      try 
      {
         inputKart = (Kart) objectInput.readObject();
      } catch (Exception e) 
      {}
      
      switch (kartType) 
      {
         case "Blue":
            kartBlue = inputKart;
            break;
            
         case "Red":
            kartRed = inputKart;
            break;
      }
   }
   
   private void handleClientResponse(String response)
   {
      System.out.println("CLIENT " + kartType + " SAID: " + response);
   
      // "identify red" => [ "identify", "red" ]
      // "kart_update" => [ "kart_update" ]
      String[] responseParts = response.split(" ");
      
      switch (responseParts[0]) 
      {
         case "identify":
            
            kartType = responseParts[1];
            
            receiveKart();
            
            break;
            
         case "own_kart_update":
            
            receiveKart();
            sendForeignKart();

            break;
      }
   }
}