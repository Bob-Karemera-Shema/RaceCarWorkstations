import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
   // Declare client socket
	private static Socket clientSocket = null;

	// Declare output stream and string to send to server 
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
      String errorMessage = "Kart designation need to be provided as either 'red' or 'blue'.";

      kartType = JOptionPane.showInputDialog(null, "Enter kart of your choice",
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
            window = new Frame();   //initialise frame object
            window.setVisible(true);//make window visible

            do 
            {
               responseLine = receiveMessage();
               
   				if(responseLine != null)
   				{
                  handleServerResponse(responseLine);
   				}
               
               if ( responseLine.equals("CLOSE") )
               {
                  shutdownClient();
                  break;
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

       if(ownKart.getKartColor().equals("Red")) {
           ownKart.initialPosition(425, 500);                 //ownKart initial position
       }
       else{
           ownKart.initialPosition(425, 550);                 //ownKart initial position
       }
       ownKart.populateImageArray();                                //load kart 1 images
      sendMessage("identify " + kartType);
      sendKart();
   }

   public static Kart getOwnKart()
    {
        return ownKart;
    }               //return ownKart

    public static void setOwnKart(Kart updateKart) { ownKart = updateKart; }        //update ownKart

    public static Kart getForeignKart()
    {
        return foreignKart;
    }       //return foreignKart

   private static void sendKart() 
   {
       //serializes kart objects and sends them to the server
      try 
      {
         objectOutput.writeObject( ownKart );
         objectOutput.flush();
      } catch (Exception e)
      {
          e.printStackTrace();
      }
   }
   
   public static void sendOwnKart()
   {
       //method to send own kart updates to server
      sendMessage("own_kart_update");
      sendKart();
   }

    private static void receiveOwnKart()
    {
        //deserializes ownKart object received from the server
        try
        {
            ownKart = (Kart) objectInput.readObject();
        } catch (Exception e)
        {}
    }

   private static void receiveForeignKart() 
   {
       //deserializes foreignKart object received from the server
      try 
      {
         foreignKart = (Kart) objectInput.readObject();
      } catch (Exception e) 
      {
      }
   }

   private static void deleteForeignKart()
   {
       //delete foreignKart when collision is detected
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

   public static void sendCollisionDetected(String message)
    {
        //send detected collision to server
        sendMessage(message);
        sendKart();
        shutdownClient();
    }

    private static void foreignKartCollision(String collisionArea)
    {
        //inform client foreign kart has collided with track edge or grass.
        int response = JOptionPane.showConfirmDialog(window, "kart" + foreignKart.getKartColor() + " crashed into "+ collisionArea
                + "! " + "You won\n .Would you like to continue driving around?","Collision Detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if(response == JOptionPane.NO_OPTION)
        {  shutdownClient();  }
        else {  deleteForeignKart(); }
    }

    private static void kartsCollision()
    {
        //inform client foreign kart has collided with own kart.
        int response = JOptionPane.showConfirmDialog(window, "kart" + foreignKart.getKartColor() + " crashed into you! You won\n"
                + "Would you like to continue driving around?","Collision Detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if(response == JOptionPane.NO_OPTION)
        {  shutdownClient();  }
        else {  deleteForeignKart(); }
    }
   
   private static void handleServerResponse(String response) 
   {
       //handle server response accordingly
      switch (response) 
      {
          case "pong":

          try { Thread.sleep(1000); } catch (Exception e) {}

          sendMessage("ping");

          break;

          case "own_kart_update":

              receiveOwnKart();

              break;

          case "foreign_kart_update":
         
            receiveForeignKart();
            
            break;

          case "collision_with_track_edge":
              foreignKartCollision("track");
              receiveForeignKart();
              break;
          case "collision_with_foreign_kart":
              kartsCollision();
              receiveForeignKart();
              break;
          case "collision_with_grass":
              foreignKartCollision("grass");
              receiveForeignKart();
              break;
      }
   }
   
   private static void shutdownClient() 
   {
       //method closes the window and end the program
       window.ParentCloseMe();
       sendMessage("CLOSE");
   }
}
