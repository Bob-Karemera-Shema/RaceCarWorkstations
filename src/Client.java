import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

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
   
   private static Kart ownKart = null;
   private static Kart foreignKart = null;

   // "blue" / "red"
   private static String kartType;

	public static void main(String[] args)
	{
      String errorMessage = "Kart designation need to be provided as either 'red' or 'blue'.";

      kartType = JOptionPane.showInputDialog(null, "Enter kart of your choice",
              "Kart Choice",JOptionPane.WARNING_MESSAGE);

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
            initialise();
            new Frame().setVisible(true);

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
   
      sendMessage("identify " + kartType);
      sendKart();
   }

   public static Kart getOwnKart()
    {
        return ownKart;
    }

    public static void setOwnKart(Kart updateKart) { ownKart = updateKart; }

    public static Kart getForeignKart()
    {
        return foreignKart;
    }

   private static void sendKart() 
   {
      try 
      {
         objectOutput.writeObject( ownKart );
         objectOutput.flush();
      } catch (Exception e)
      {}
   }
   
   public static void sendOwnKart()
   {
      sendMessage("own_kart_update");
      sendKart();
      receiveForeignKart();
   }
   
   private static void receiveForeignKart() 
   {
      try 
      {
         foreignKart = (Kart) objectInput.readObject();
      } catch (Exception e) 
      {}
   }
   
   private static String receiveMessage() 
   {
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
      try 
      {
         outputStream.writeBytes(message + "\n");
      } catch (Exception e) 
      {}
   }
   
   private static void handleServerResponse(String response) 
   {
      switch (response) 
      {
         case "foreign_kart_update":
         
            receiveForeignKart();
            
            break;
      }
   }
   
   private static void shutdownClient() 
   {
      // shutdown script
   }
}
