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
    private Frame parent;                               //Frame containing panel

    public RaceTrack(Frame parent)
    {
        setLayout(null);                                //suppress default panel layout features
        setBounds(0,0,850,650);
        setBackground(Color.green);
        setFocusable(true);
        this.parent = parent;                           //initialise parent frame
        this.addKeyListener(this);                   //add key listener to this JPanel

        StartAnimation();
    }

    public void paintComponent(Graphics g)              //Draw racetrack and current kart locations only if animation is running
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
        Color c4 = Color.black;
        g.setColor( c4 );
        g.drawLine( 425, 500, 425, 600 ); // start line

        //Draw karts
        if (Client.getOwnKart() != null)        //Only draw foreign kart if it exists
        {
            Client.getOwnKart().getCurrentImage().paintIcon(this, g, Client.getOwnKart().getLocation().x, Client.getOwnKart().getLocation().y);
        }
        if (Client.getForeignKart() != null)        //Only draw foreign kart if it exists
         {
            Client.getForeignKart().getCurrentImage().paintIcon(this, g, Client.getForeignKart().getLocation().x,
                        Client.getForeignKart().getLocation().y);
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
    }   //function to stop animation when game is ended

    public void actionPerformed(ActionEvent event)
    {
        Client.sendOwnKart();       //send ownKart information to clientHandler everytime timer ticks

        //Call repaint function to update display
        repaint();

        Client.getOwnKart().displaceKart();         //update kart position
        collisionDetection();           // detect collisions (between karts and with edges)
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
        collisionBetweenKarts();
        collisionWithBoundaries();
    }

    public void collisionBetweenKarts()
    {
        if (Client.getForeignKart() != null)
        {
            //Collision detection between karts
            if ((Client.getForeignKart().getLocation().y >= Client.getOwnKart().getLocation().y &&
                    Client.getForeignKart().getLocation().y <= Client.getOwnKart().getLocation().y + 40)
                    || (Client.getForeignKart().getLocation().y + 40 >= Client.getOwnKart().getLocation().y &&
                    Client.getForeignKart().getLocation().y + 40 <= Client.getOwnKart().getLocation().y + 40))
            {   //if the karts collide vertically
                if (Client.getOwnKart().getLocation().x + 45 >= Client.getForeignKart().getLocation().x &&
                        !(Client.getOwnKart().getLocation().x >= Client.getForeignKart().getLocation().x + 45))
                {  //and if the karts collide horizontally
                    Client.getOwnKart().stopKart();
                    Client.getForeignKart().stopKart();
                    Client.sendCollisionDetected("collision_with_foreign_kart");
                    StopAnimation();
                    JOptionPane.showMessageDialog(this, "Karts crashed into each other!" +
                            " No winner for this race", "Collision Detected", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void collisionWithBoundaries()
    {
        //Collision detection between karts and racetrack bounds
        if(Client.getOwnKart().checkOuterCollision())
        {
            Client.getOwnKart().setCollisionArea("track_edge");
            Client.sendCollisionDetected("collision_with_track_edge");
            StopAnimation();
            JOptionPane.showMessageDialog(this, "Kart" + Client.getOwnKart().getKartColor() + " crashed!" +
                    " KartBlue wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
            endGame();
        }

        if(Client.getOwnKart().checkInnerCollision(new Rectangle( 150, 200, 550, 300 )))     //inner edge bounds
        {
            Client.getOwnKart().setCollisionArea("track_edge");
            Client.sendCollisionDetected("collision_with_grass");
            StopAnimation();
            JOptionPane.showMessageDialog(this, "Kart" + Client.getOwnKart().getKartColor() + " crashed!" +
                    " KartBlue wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
            endGame();
        }
    }

    public void endGame()
    {
        //Close Frame containing panel
        parent.ParentCloseMe();
    }
}
