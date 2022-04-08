import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RaceTrack extends JPanel implements ActionListener, KeyListener
{
    private final int animationDelay = 100;             //animation delay in milliseconds
    private Timer animationTimer;                       //Timer tool for animation

    public RaceTrack()
    {
        setLayout(null);                                //suppress default panel layout features
        setBounds(0,0,850,650);
        setBackground(Color.green);
        setFocusable(true);
        Client.getOwnKart().initialPosition(425, 500);                             //initialise the position of the first kart object
        Client.getOwnKart().populateImageArray();                                        //load kart 1 images

        if (Client.getForeignKart() != null) {
            Client.getForeignKart().initialPosition(425, 600);                         //initialise the position of the second kart object
            Client.getForeignKart().populateImageArray();                                    //load kart 2 images
        }
        this.addKeyListener(this);

        StartAnimation();
    }

    public void paintComponent(Graphics g)              //Draw racetrack and current kart locations
    {
        super.paintComponent(g);

        //Draw racetrack
        Color c1 = Color.green;
        g.setColor( c1 );
        g.fillRect( 150, 200, 550, 300 ); // grass
        Color c2 = Color.white;
        g.setColor( c2 );
        g.fillRect( 50, 100, 750, 500 ); // outer edge
        Color c5 = Color.green;
        g.setColor( c5 );
        g.fillRect( 150, 200, 550, 300 ); // inner edge
        Color c3 = Color.yellow;
        g.setColor( c3 );
        g.drawRect( 100, 150, 650, 400 ); // mid-lane marker
        Color c4 = Color.white;
        g.setColor( c4 );
        g.drawLine( 425, 500, 425, 600 ); // start line

        //Draw karts
        Client.getOwnKart().getCurrentImage().paintIcon(this, g, Client.getOwnKart().getLocation().x, Client.getOwnKart().getLocation().y);

        if (Client.getForeignKart() != null)
            Client.getForeignKart().getCurrentImage().paintIcon(this, g, Client.getForeignKart().getLocation().x,
                Client.getForeignKart().getLocation().y);

        if(animationTimer.isRunning())                  //Only refreshes kart locations if timer is running
        {
            Client.getOwnKart().displaceKart();
            Client.sendOwnKart();
            collisionDetection();
        }
    }

    public void StartAnimation()
    {
        if(animationTimer == null)                      //Create timer if timer is not yet created
        {
            animationTimer = new Timer(animationDelay, this);
            animationTimer.start();
        }
        else if(!animationTimer.isRunning())            //Restart timer if it's already running
        {   animationTimer.restart();   }
    }

    public void StopAnimation()
    {
        animationTimer.stop();
    }

    public void actionPerformed(ActionEvent event)
    {
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP)                        //increase kart1 speed if up key is pressed
        {
            Client.getOwnKart().increaseSpeed();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)                //decrease kart1 speed if down key is pressed
        {
            Client.getOwnKart().decreaseSpeed();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)                //turn kart1 left if left key is pressed
        {
            Client.getOwnKart().updateDirection("left");
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)               //turn kart1 right if right key is pressed
        {
            Client.getOwnKart().updateDirection("right");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void collisionDetection()                //collision detection method
    {
        if (Client.getForeignKart() != null) {
            //Collision detection between karts
            if ((Client.getForeignKart().getLocation().y >= Client.getOwnKart().getLocation().y &&
                    Client.getForeignKart().getLocation().y <= Client.getOwnKart().getLocation().y + 40)
                    || (Client.getForeignKart().getLocation().y + 40 >= Client.getOwnKart().getLocation().y &&
                    Client.getForeignKart().getLocation().y + 40 <= Client.getOwnKart().getLocation().y + 40)) {   //if the karts collide vertically
                if (Client.getOwnKart().getLocation().x + 45 >= Client.getForeignKart().getLocation().x &&
                        !(Client.getOwnKart().getLocation().x >= Client.getForeignKart().getLocation().x + 45)) {  //and if the karts collide horizontally
                    Client.getOwnKart().stopKart();
                    Client.getForeignKart().stopKart();
                }
            }
        }
        //Collision detection between karts and racetrack bounds
        Client.getOwnKart().checkOuterCollision();
        Client.getOwnKart().checkInnerCollision(new Rectangle( 150, 200, 550, 300 ));     //inner edge bounds

        Client.getForeignKart().checkOuterCollision();
        Client.getForeignKart().checkInnerCollision(new Rectangle( 150, 200, 550, 300  ));    //inner edge bounds
    }
}
