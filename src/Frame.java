import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame
{
    public Frame()
    {
        //Set up Frame Visual look
        setTitle("Race Animation");
        setBounds(0,0,1000,1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        //Create container
        Container container = getContentPane();

        //Create a kart instance and add it to the container
        container.add(new RaceTrack());
    }
}
