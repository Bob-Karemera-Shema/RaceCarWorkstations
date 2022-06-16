import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
   // Declare client socket
	private static Socket clientSocket = null;

	// Declare output stream to send to server
	private static DataOutputStream outputStream = null;
			
	// Declare input stream from server and string to store input received from server
	private static BufferedReader inputStream = null;
	private static String responseLine;
   
   private static ObjectOutput objectOutput = null;
   private static ObjectInput objectInput = null;
	
	// replace "localhost" with the remote server address, if needed
	// 5000 is the server port
	private static String serverHost = "localhost";

    //Declare kart objects for the client
   private static Kart ownKart = null;
   private static Kart foreignKart = null;

   //Declare frame window to display game
   private static Frame window = null;

   // "blue" / "red"
   private static String kartType;

	public static void main(String[] args)
	{
      String errorMessage = "Kart designation need to be provided as either 'Red' or 'Blue'.";

      kartType = JOptionPane.showInputDialog(null, "Enter kart of your choice, Red or Blue",
              "Kart Choice",JOptionPane.INFORMATION_MESSAGE);

      if (!kartType.equals(null))
      {
         if (!kartType.equals("Blue") && !kartType.equals("Red"))
         {
            System.err.println(errorMessage);
            return;
         }
      } else 
      {
         System.err.println(errorMessage);
         return;
      }

		// Create a socket on port 5000 and open input and output streams on that socket
		try
		{
			clientSocket = new Socket(serverHost, 5000);
			
			outputStream = new DataOutputStream(
				clientSocket.getOutputStream()
			);
			
			inputStream = new BufferedReader(
				new InputStreamReader(
					clientSocket.getInputStream()
				)
			);
         
         objectOutput = new ObjectOutputStream(
            clientSocket.getOutputStream()
         );
         
         objectInput = new ObjectInputStream(
            clientSocket.getInputStream()
         );
		} 
		catch (UnknownHostException e)
		{
			System.err.println("Don't know about host: " + serverHost);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't get I/O for the connection to: " + serverHost);
		}

		// Write data to the socket
		if ( clientSocket != null && outputStream != null && inputStream != null && objectOutput != null && objectInput != null )
        {
			try
			{
            initialise();           //initialise kart of choice
            do 
            {
               responseLine = receiveMessage();
               
   				if(responseLine != null)
   				{

                    if ( responseLine.equals("CLOSE") )
                    {
                        shutdownClient();
                        break;
                    }

                  handleServerResponse(responseLine);
   				}

            } while(true);
								
				// close the input/output streams and socket
				outputStream.close();
				inputStream.close();
            objectOutput.close();
            objectInput.close();
				clientSocket.close();
			}
			catch (UnknownHostException e)
			{
				System.err.println("Trying to connect to unknown host: " + e);
			}
			catch (IOException e)
			{
				System.err.println("IOException:  " + e);
			}
		}
	}
   
   private static void initialise() 
   {
      // initialise our client's own kart object
      ownKart = new Kart( kartType );

       window = new Frame();   //initialise frame object
       window.setVisible(true);//make window visible


       if(ownKart.getKartColor().equals("Red")) {
           ownKart.initialPosition(425, 500);                 //ownKart initial position
       }
       else{
           ownKart.initialPosition(425, 550);                 //ownKart initial position
       }
       ownKart.populateImageArray();                                //load kart 1 images
      sendMessage("identify " + kartType + " " +
              ownKart.getDirection() + " " +
              ownKart.getLocationX() + " " +
              ownKart.getLocationY() + " " +
              ownKart.getSpeed()
      );
   }

   public static Kart getOwnKart()
    {
        return ownKart;
    }               //return ownKart

    public static Kart getForeignKart()
    {
        return foreignKart;
    }       //return foreignKart
   
   public static void sendOwnKart()
   {
       //method to send own kart updates to server
      sendMessage("own_kart_update " +
              ownKart.getDirection() + " " +
              ownKart.getLocationX() + " " +
              ownKart.getLocationY() + " " +
              ownKart.getSpeed()
      );

      //sendKart();
   }

    private static void receiveOwnKart(String update)
    {
        String[] updateParts = update.split(" ");

        //update kart information received from the server
        ownKart.setImageIndex(Integer.parseInt(updateParts[1]));
        ownKart.setDirection(Integer.parseInt(updateParts[1]));
        ownKart.setLocationX(Integer.parseInt(updateParts[2]));
        ownKart.setLocationY(Integer.parseInt(updateParts[3]));
        ownKart.setSpeed(Float.parseFloat(updateParts[4]));
        ownKart.setKartColor(updateParts[5]);
        ownKart.populateImageArray();
    }

   private static void receiveForeignKart(String update)
   {
       String[] updateParts = update.split(" ");

       if(foreignKart == null) {
           switch (kartType) {
               case "Blue":
                   foreignKart = new Kart("Red");
                   foreignKart.initialPosition(425, 500);                 //foreignKart initial position
                   foreignKart.populateImageArray();                                //load kart 1 images
                   break;

               case "Red":
                   foreignKart = new Kart("Blue");
                   foreignKart.initialPosition(425, 550);                 //foreignKart initial position
                   foreignKart.populateImageArray();                                //load kart 1 images
                   break;
           }
       }

       if(updateParts[1] != null)
       {
           //update kart information received from the server
           foreignKart.setImageIndex(Integer.parseInt(updateParts[1]));
           foreignKart.setDirection(Integer.parseInt(updateParts[1]));
           foreignKart.setLocationX(Integer.parseInt(updateParts[2]));
           foreignKart.setLocationY(Integer.parseInt(updateParts[3]));
           foreignKart.setSpeed(Float.parseFloat(updateParts[4]));
       }
   }

   public static void deleteKarts()
   {
       //delete Karts when collision is detected
       ownKart = null;
       foreignKart = null;
   }
   
   private static String receiveMessage() 
   {
       //receive message from server
      try 
      {
         return inputStream.readLine();
      } catch (Exception e) 
      {
         return null;
      }
   }
   
   private static void sendMessage(String message) 
   {
       //send message to server
      try 
      {
         outputStream.writeBytes(message + "\n");
      } catch (Exception e) 
      {}
   }
   
   private static void handleServerResponse(String response) 
   {
       String[] responseParts = response.split(" ");
       //handle server response accordingly
      switch (responseParts[0])
      {
          case "pong":

          try { Thread.sleep(1000); } catch (Exception e) {}

          sendMessage("ping");

          break;

          case "own_kart_update":

              receiveOwnKart(response);

              break;

          case "foreign_kart_update":

            receiveForeignKart(response);

            break;
      }
   }
   
   public static void shutdownClient()
   {
       //method closes the window and end the program
       window.ParentCloseMe();
       sendMessage("CLOSE");
   }

    public static void playAgain()
    {
        deleteKarts();
        sendMessage("play_again");
        window.ParentCloseMe();
        initialise();
    }
}
