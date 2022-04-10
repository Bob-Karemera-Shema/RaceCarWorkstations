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
    private Kart ownKart = null;
    private Kart foreignKart = null;
    private Frame parent;                               //Frame containing panel

    public RaceTrack(Frame parent)
    {
        setLayout(null);                                //suppress default panel layout features
        setBounds(0,0,850,650);
        setBackground(Color.green);
        setFocusable(true);
        this.parent = parent;
        ownKart = Client.getOwnKart();

        if(ownKart.getKartColor().equals("Red")) {
            ownKart.initialPosition(425, 500);                 //initialise own kart the position
        }
        else{
            ownKart.initialPosition(425, 550);                 //initialise own kart the position
        }
        ownKart.populateImageArray();                                        //load kart 1 images

        foreignKart = Client.getForeignKart();                      //try to get foreign kart if available
        this.addKeyListener(this);

        StartAnimation();
    }

    public void paintComponent(Graphics g)              //Draw racetrack and current kart locations
    {
        if(animationTimer.isRunning())                  //Only refreshes kart locations if timer is running
        {
            ownKart = Client.getOwnKart();
            foreignKart = Client.getForeignKart();

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
        ownKart.getCurrentImage().paintIcon(this, g, ownKart.getLocation().x, ownKart.getLocation().y);

        if (foreignKart != null)
        {
            foreignKart.getCurrentImage().paintIcon(this, g, foreignKart.getLocation().x,
                    foreignKart.getLocation().y);
        }
            ownKart.displaceKart();
            Client.setOwnKart(ownKart);
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
            ownKart.increaseSpeed();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)                //decrease kart1 speed if down key is pressed
        {
            ownKart.decreaseSpeed();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)                //turn kart1 left if left key is pressed
        {
            ownKart.updateDirection("left");
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)               //turn kart1 right if right key is pressed
        {
            ownKart.updateDirection("right");
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
        if (foreignKart != null)
        {
            //Collision detection between karts
            if ((foreignKart.getLocation().y >= ownKart.getLocation().y && foreignKart.getLocation().y <= ownKart.getLocation().y + 40)
                    || (foreignKart.getLocation().y + 40 >= ownKart.getLocation().y &&
                    foreignKart.getLocation().y + 40 <= ownKart.getLocation().y + 40))
            {   //if the karts collide vertically
                if (ownKart.getLocation().x + 45 >= foreignKart.getLocation().x &&
                        !(ownKart.getLocation().x >= foreignKart.getLocation().x + 45))
                {  //and if the karts collide horizontally
                    ownKart.stopKart();
                    foreignKart.stopKart();
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
        if(ownKart.checkOuterCollision())
        {
            StopAnimation();
            JOptionPane.showMessageDialog(this, "Kart" + ownKart.getKartColor() + " crashed!" +
                    " KartBlue wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
            endGame();
        }

        if(ownKart.checkInnerCollision(new Rectangle( 150, 200, 550, 300 )))     //inner edge bounds
        {
            StopAnimation();
            JOptionPane.showMessageDialog(this, "Kart" + ownKart.getKartColor() + " crashed!" +
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
