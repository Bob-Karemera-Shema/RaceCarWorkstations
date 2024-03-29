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
    private JLabel raceBoardTitle;                           //Race information title label
    private JLabel ownKartLaps;                           //Race information label
    private JLabel foreignKartLaps;                           //Race information label
    private JButton exitButton;                         //Exit button
    private JLabel waiting;
    private JFrame parent;

    public RaceTrack(Frame parent)
    {
        setLayout(null);                                //suppress default panel layout features
        setBounds(0,0,850,650);
        setBackground(Color.green);
        setFocusable(true);
        this.addKeyListener(this);                   //add key listener to this JPanel
        this.parent = parent;

        raceBoardTitle = new JLabel("Race Information");
        raceBoardTitle.setBounds(50,600,100,50);

        ownKartLaps = new JLabel("Kart " + Client.getOwnKart().getKartColor() + " laps:");
        ownKartLaps.setBounds(50,650,100,50);

        foreignKartLaps = new JLabel("Foreign Kart laps:");
        foreignKartLaps.setBounds(50,700,100,50);
        foreignKartLaps.setVisible(false);

        exitButton = new JButton("Exit");
        exitButton.setBounds(800,30,100,50);
        exitButton.setBackground(Color.white);
        exitButton.addActionListener(this);

        waiting = new JLabel("Waiting for second player to connect...");
        waiting.setBounds(320,300,300,100);

        add(raceBoardTitle);
        add(ownKartLaps);
        add(foreignKartLaps);
        add(exitButton);
        add(waiting);

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
        Color c3 = Color.black;
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
             remove(waiting);
             foreignKartLaps.setVisible(true);
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
        if(event.getSource() == animationTimer)
        {
            repaint();        //Call repaint function to update display
            updateRaceInformation();       //update race information displayed on the screen
            Client.getOwnKart().displaceKart();         //update kart position
            Client.sendOwnKart();       //send ownKart information to clientHandler everytime kart is displaced
            checkWinner();                 //check whether a kart has won the raceBlue
            collisionDetection();           // detect collisions (between karts and with edges)
        }
        if(event.getSource() == exitButton)
        {
            StopAnimation();
            Client.shutdownClient();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(Client.getForeignKart() != null)
        {
            //condition that prevents local racer from racing until another racer is connected
            if (e.getKeyCode() == KeyEvent.VK_UP)                        //increase kart1 speed if up key is pressed
            {
                Client.getOwnKart().increaseSpeed();
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN)                //decrease kart1 speed if down key is pressed
            {
                Client.getOwnKart().decreaseSpeed();
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT)                //turn kart1 left if left key is pressed
            {
                Client.getOwnKart().updateDirection("left");
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT)               //turn kart1 right if right key is pressed
            {
                Client.getOwnKart().updateDirection("right");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void updateRaceInformation()
    {
        Client.getOwnKart().updateLaps();
        ownKartLaps.setText("Kart " + Client.getOwnKart().getKartColor() + " Laps: " + Client.getOwnKart().getLapCounter());

        if(Client.getForeignKart() != null)
        {
            foreignKartLaps.setText("Kart " + Client.getForeignKart().getKartColor() + " Laps: " + Client.getForeignKart().getLapCounter());
        }
    }

    private void collisionDetection()                //collision detection method
    {
        collisionBetweenKarts();
        collisionWithBoundaries();
    }

    private void collisionBetweenKarts()
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
                    JOptionPane.showMessageDialog(this, "Karts crashed into each other!" +
                            " No winner for this race", "Collision Detected", JOptionPane.ERROR_MESSAGE);
                    StopAnimation();
                }
            }
        }
    }

    private void collisionWithBoundaries()
    {
        //Collision detection between karts and racetrack bounds
        if(Client.getOwnKart().checkOuterCollision())
        {
            Client.getOwnKart().stopKart();
            JOptionPane.showMessageDialog(this, "Kart" + Client.getOwnKart().getKartColor() + " crashed!" +
                    " Kart "+ Client.getForeignKart().getKartColor() +" wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
            StopAnimation();
        }

        if(Client.getOwnKart().checkInnerCollision(new Rectangle( 150, 200, 550, 300 )))     //inner edge bounds
        {
            Client.getOwnKart().stopKart();
            JOptionPane.showMessageDialog(this, "Kart" + Client.getOwnKart().getKartColor() + " crashed!" +
                    " Kart" + Client.getForeignKart().getKartColor() + " wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
            StopAnimation();
        }

        if(Client.getForeignKart() != null)
        {
            if (Client.getForeignKart().checkOuterCollision()) {
                JOptionPane.showMessageDialog(this, "Kart" + Client.getForeignKart().getKartColor() + " crashed!" +
                        " Kart " + Client.getOwnKart().getKartColor() + " wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
                StopAnimation();
            }

            if (Client.getForeignKart().checkInnerCollision(new Rectangle(150, 200, 550, 300)))     //inner edge bounds
            {
                JOptionPane.showMessageDialog(this, "Kart" + Client.getForeignKart().getKartColor() + " crashed!" +
                        " Kart " + Client.getOwnKart().getKartColor() + " wins.", "Collision Detected", JOptionPane.INFORMATION_MESSAGE);
                StopAnimation();
            }
        }
    }

    private void checkWinner()
    {
        //the first kart to complete three laps wins the race
        if(Client.getForeignKart() != null) {
            if (Client.getOwnKart().getLapCounter() == 4 && Client.getForeignKart().getLapCounter() < 4) {
                JOptionPane.showMessageDialog(this, "Kart" + Client.getOwnKart().getKartColor() + " wins!"
                        , "Race Finished", JOptionPane.INFORMATION_MESSAGE);
                StopAnimation();
            }


            if (Client.getOwnKart().getLapCounter() < 4 && Client.getForeignKart().getLapCounter() == 4) {
                JOptionPane.showMessageDialog(this, "Kart" + Client.getForeignKart().getKartColor() + " wins!"
                        , "Race Finished", JOptionPane.INFORMATION_MESSAGE);
                StopAnimation();
            }

            if (Client.getOwnKart().getLapCounter() == 4 && Client.getForeignKart().getLapCounter() == 4) {
                JOptionPane.showMessageDialog(this, "The race ends in a tie. No winner!"
                        , "Race Finished", JOptionPane.INFORMATION_MESSAGE);
                StopAnimation();
            }
        }
    }
}
