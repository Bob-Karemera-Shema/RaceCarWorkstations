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
                  
         do
         {
            line = receiveMessage();
         
   			if(line != null)
   			{
               if ( line.equals("CLOSE") )
               {
                  sendMessage("CLOSE");
                  endSession();
                  alive = false;
                  break;
               }

   				handleClientResponse(line);
   			}
            
            try { Thread.sleep(1); } catch (InterruptedException e) {}
         } while(true);
   		
   		// Comment out/remove the outputStream and server close statements if server should remain live
   		outputStream.close();
   		inputStream.close();
   		server.close();
      } 
      catch (Exception e) 
      {
         System.out.println("TCPClientHandler Exception: " + e.getMessage());
      }
   }
   
   public boolean isAlive() 
   {
      return alive;
   }

   public void endSession()
   {
      switch (kartType)
      {
         case "Blue":
            kartBlue = null;
            break;
         case "Red":
            kartRed = null;
            break;
      }
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

   private void sendOwnKart()
   {

      switch (kartType)
      {
         case "blue":
            sendMessage("own_kart_update " +
                    kartBlue.getDirection() + " " +
                    kartBlue.getLocationX() + " " +
                    kartBlue.getLocationY() + " " +
                    kartBlue.getSpeed() + " " +
                    kartBlue.getKartColor()
            );
            break;

         case "red":
            sendMessage("own_kart_update " +
                    kartRed.getDirection() + " " +
                    kartRed.getLocationX() + " " +
                    kartRed.getLocationY() + " " +
                    kartRed.getSpeed() + " " +
                    kartBlue.getKartColor()
            );
            break;
      }
   }

   private void sendForeignKart()
   {
      //method sends foreign kart update

      switch (kartType)
      {
         case "Blue":
            sendMessage("foreign_kart_update " +
                    kartRed.getDirection() + " " +
                    kartRed.getLocationX() + " " +
                    kartRed.getLocationY() + " " +
                    kartRed.getSpeed()
            );
            break;

         case "Red":
            sendMessage("foreign_kart_update " +
                    kartBlue.getDirection() + " " +
                    kartBlue.getLocationX() + " " +
                    kartBlue.getLocationY() + " " +
                    kartBlue.getSpeed()
            );
            break;
      }
   }
   
   private void receiveKart(String update)
   {
      String[] updateParts = update.split(" ");

      //update kart information received from the client
      switch (kartType) 
      {
         case "Blue":
            kartBlue.setImageIndex(Integer.parseInt(updateParts[1]));
            kartBlue.setDirection(Integer.parseInt(updateParts[1]));
            kartBlue.setLocationX(Integer.parseInt(updateParts[2]));
            kartBlue.setLocationY(Integer.parseInt(updateParts[3]));
            kartBlue.setSpeed(Float.parseFloat(updateParts[4]));
            break;
            
         case "Red":
            kartRed.setImageIndex(Integer.parseInt(updateParts[1]));
            kartRed.setDirection(Integer.parseInt(updateParts[1]));
            kartRed.setLocationX(Integer.parseInt(updateParts[2]));
            kartRed.setLocationY(Integer.parseInt(updateParts[3]));
            kartRed.setSpeed(Float.parseFloat(updateParts[4]));
            break;
      }
   }

   private void assignAvailableKart(String color)
   {
      //If desired kart is already taken,
      //assign another kart of different color
      Kart inputKart = null;

      switch (color)
      {
         case "Blue":
            kartType = "Red";
            inputKart = new Kart("Red");
            kartRed = inputKart;
            kartRed.initialPosition(425, 500);                 //New assigned Kart initial position
            kartRed.populateImageArray();                            //load kart image
            break;

         case "Red":
            kartType = "Blue";
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
            kartBlue = new Kart("Blue");
            kartBlue.initialPosition(425, 550);                 //Kart initial position
            kartBlue.populateImageArray();                            //load kart image
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
            kartRed = new Kart("Red");
            kartRed.initialPosition(425, 500);                 //Kart initial position
            kartRed.populateImageArray();                            //load kart image
            return true;
         }
         else
         {
            return false;
         }
      }
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
         }
         else {
            assignAvailableKart(responseParts[1]);
            sendOwnKart();
         }
      }

      if (responseParts[0].equals("own_kart_update")) {
         receiveKart(response);

         switch (kartType){
            case "Blue":

               if(kartRed != null) {
                  sendForeignKart();
               }

               break;

            case "Red":

               if(kartBlue != null) {
                  sendForeignKart();
               }

               break;
         }
      }
   }
}