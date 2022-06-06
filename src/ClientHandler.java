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
               sendMessage("CLOSE");
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
      {
         System.out.println("Can't send kart");}
   }

   private void sendOwnKart()
   {
      sendMessage("own_kart_update");

      switch (kartType)
      {
         case "blue":
            sendKart(kartBlue);
            break;

         case "red":
            sendKart(kartRed);
            break;
      }
   }

   private void sendForeignKart()
   {
      //method sends foreign kart update and collision detection updates
      if(!kartBlue.isAlive()  && !kartRed.isAlive()) {
         sendMessage("collision_with_foreign_kart");
      }
      if(kartBlue.isAlive()  && kartRed.isAlive()) {
         sendMessage("foreign_kart_update");
      }
      if(!kartBlue.getKartColor().equals(kartType)  && !kartBlue.isAlive()) {
         sendMessage("collision_with_" + kartBlue.getCollisionArea());
      }
      if(!kartRed.getKartColor().equals(kartType)  && !kartRed.isAlive()) {
         sendMessage("collision_with_" + kartBlue.getCollisionArea());
      }

      switch (kartType)
      {
         case "Blue":
            sendKart(kartRed);
            break;

         case "Red":
            sendKart(kartBlue);
            break;
      }
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

   private void assignAvailableKart(String color)
   {
      //If desired kart is already taken,
      //assign another kart of different color
      Kart inputKart = null;

      try
      {
         inputKart = (Kart) objectInput.readObject();
      } catch (Exception e)
      {}

      switch (color)
      {
         case "Blue":
            kartType = color;
            inputKart = new Kart("Red");
            kartRed = inputKart;
            kartRed.initialPosition(425, 500);                 //New assigned Kart initial position
            kartRed.populateImageArray();                            //load kart image
            break;

         case "Red":
            kartType = color;
            inputKart = new Kart("Blue");
            kartBlue = inputKart;
            kartBlue.initialPosition(425, 550);                 //New assigned Kart initial position
            kartBlue.populateImageArray();                            //load kart image
            break;
      };
   }

   private boolean checkKartAvailability(String color)
   {
      if(color.equals("Blue"))
      {
         if (kartBlue == null)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         if(kartRed == null)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
   }

   public void kartCollision()
   {
      switch (kartType)
      {
         case "Blue":
            kartBlue.setAlive(false);
            break;
         case "Red":
            kartRed.setAlive(false);
      }
   }

   public void kartsCrash()
   {
      kartBlue.setAlive(false);
      kartRed.setAlive(false);
   }
   
   private void handleClientResponse(String response)
   {
      System.out.println("CLIENT " + kartType + " SAID: " + response);
   
      // "identify red" => [ "identify", "red" ]
      // "kart_update" => [ "kart_update" ]
      String[] responseParts = response.split(" ");

      if(responseParts[0].equals("ping")) {
         try {
            Thread.sleep(1000);
         } catch (Exception e) {
         }

         sendMessage("pong");
      }

      if(responseParts[0].equals("identify")) {
         if (checkKartAvailability(responseParts[1])) {
            kartType = responseParts[1];
            receiveKart();
         }
         else {
            assignAvailableKart(responseParts[1]);
            sendOwnKart();
         }
      }

      if (responseParts[0].equals("own_kart_update")) {
            receiveKart();
            sendForeignKart();
      }

      if (responseParts[0].equals("collision_with_track_edge")) {
         kartCollision();
         sendForeignKart();
      }

      if (responseParts[0].equals("collision_with_grass")) {
         kartCollision();
         sendForeignKart();
      }

      if (responseParts[0].equals("collision_with_foreign_kart")) {
         receiveKart();
         sendForeignKart();
      }
   }
}