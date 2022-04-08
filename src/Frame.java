import javax.swing.*;
import java.awt.*;

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
        new RaceTrack();
    }
}
