import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Frame extends JFrame
{
    public Frame()
    {
        //Set up Frame Visual look
        setTitle("Race Game");
        setBounds(0,0,1000,1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create container
        Container container = getContentPane();

        //Create a racetrack instance
        container.add(new RaceTrack(this));
    }

    public void ParentCloseMe(){
        //method to close the frame
        // Hide the frame
        setVisible(false);
        // If the frame is no longer needed, call dispose
        dispose();
    }
}
