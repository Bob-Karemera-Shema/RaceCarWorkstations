import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Kart implements Serializable{
    private Point location;                             //current kart location
    private float speed;                                //current kart speed
    private int direction;                              //moving kart direction
    private int imageIndex;                         //current image index in loop
    private ImageIcon[] kartImages;                     //kart 2 image array
    private String kartColor;                           //kart color attribute
    private final int totalImages = 16;                 //number of images
    private boolean alive;
    private String collisionArea;
    private int lapCounter;
    private boolean threeQuaterWayMark;                 // helps ensure a kart has travelled three quarters of the track distance
                                                        // before reaching the finish line in order to increment laps
                                                        // done when the kart reaches the finish line
    public Kart(String kartColor)
    {
        speed = 0;                                      //kart starts at rest
        direction = 0;                                  //initial kart direction
        this.kartColor = kartColor;                     //Assign kart color
        kartImages = new ImageIcon[totalImages];       //initialise image array
        alive = true;                                   //kart is alive
        lapCounter = 1;
        threeQuaterWayMark = false;
        imageIndex = 0;
    }

    public void initialPosition(int x, int y)
    {
        location = new Point( x, y );                   //kart starting position
    }

    public void populateImageArray()
    {
        //Load images to display the kart as it moves on the screen
        if(kartColor.equals("Red"))
        {
            loadKartImages(1);
        }

        if(kartColor.equals("Blue"))
        {
            loadKartImages(2);
        }
    }

    public void loadKartImages(int folderNumber)
    {
        String message = "Kart Images not found";
        try
        {
            for (int i = 0; i < totalImages; i++)            //load kart images to array
            {
                kartImages[i] = new ImageIcon(getClass().getResource("images" + folderNumber + "/kart"
                        + kartColor + i + ".png"));
            }
        }
        catch (Exception e)
        {
            //notify player kart images were not found
            JOptionPane.showMessageDialog(null, message,
                    "Loading error",JOptionPane.ERROR_MESSAGE); //inform user
        }
    }

    public ImageIcon getCurrentImage()
    {
        //return current kart image
        return kartImages[imageIndex];
    }

    public void setKartColor(String color)
    {
        this.kartColor = color;
    }

    public String getKartColor()
    {
        //return current kart image
        return kartColor;
    }

    public boolean isAlive()
    {
        //return whether kart is alive or not
        return alive;
    }

    public String getCollisionArea()
    {
        //return area of collision
        return collisionArea;
    }

    public void displaceKart()
    {
        //displace kart image
        if(direction == 0)
        {
            location.x = (int) (location.x - 2 * speed);
        }
        else if(direction == 1)
        {
            location.x = (int) (location.x - 1.5 * speed);
            location.y = (int) (location.y - 0.5 * speed);
        }
        else if (direction == 2)
        {
            location.x = (int) (location.x - 1 * speed);
            location.y = (int) (location.y - 1 * speed);
        }
        else if (direction == 3)
        {
            location.x = (int) (location.x - 0.5 * speed);
            location.y = (int) (location.y - 1.5 * speed);
        }
        else if (direction == 4)
        {
            location.y = (int) (location.y - 2 * speed);
        }
        else if (direction == 5)
        {
            location.x = (int) (location.x + 0.5 * speed);
            location.y = (int) (location.y - 1.5 * speed);
        }
        else if (direction == 6)
        {
            location.x = (int) (location.x + 1 * speed);
            location.y = (int) (location.y - 1 * speed);
        }
        else if (direction == 7)
        {
            location.x = (int) (location.x + 1.5 * speed);
            location.y = (int) (location.y - 0.5 * speed);
        }
        else if (direction == 8)
        {
            location.x = (int) (location.x + 2 * speed);
        }
        else if (direction == 9)
        {
            location.x = (int) (location.x + 1.5 * speed);
            location.y = (int) (location.y + 0.5 * speed);
        }
        else if (direction == 10)
        {
            location.x = (int) (location.x + 1 * speed);
            location.y = (int) (location.y + 1 * speed);
        }
        else if (direction == 11)
        {
            location.x = (int) (location.x + 0.5 * speed);
            location.y = (int) (location.y + 1.5 * speed);
        }
        else if (direction == 12)
        {
            location.y = (int) (location.y + 2 * speed);
        }
        else if (direction == 13)
        {
            location.x = (int) (location.x - 0.5 * speed);
            location.y = (int) (location.y + 1.5 * speed);
        }
        else if (direction == 14)
        {
            location.x = (int) (location.x - 1 * speed);
            location.y = (int) (location.y + 1 * speed);
        }
        else if (direction == 15)
        {
            location.x = (int) (location.x - 1.5 * speed);
            location.y = (int) (location.y + 0.5 * speed);
        }
    }

    public void updateDirection(String newDirection)
    {
        //updates the direction of the kart while it moves
        if(newDirection.equals("left"))
        {
            direction--;

            if(direction < 0)
            {    direction = 15;    }

            imageIndex = direction;
        }
        if(newDirection.equals("right"))
        {
            direction++;

            if(direction > 15)
            {    direction = 0;     }

            imageIndex = direction;
        }
    }

    public void increaseSpeed()
    {
        speed += 1;
    }           //increase kart speed

    public void decreaseSpeed()                           //decrease kart speed
    {
        speed -= 1;
    }

    public void setImageIndex(int imageIndex) {
        //set kart image index
        this.imageIndex = imageIndex;
    }

    public void setDirection(int direction)
    {
        //set kart direction
        this.direction = direction;
    }

    public int getDirection()
    {
        //return kart image index
        return direction;
    }

    public void setSpeed(float speed)
    {
        //set kart direction
        this.speed = speed;
    }

    public float getSpeed()
    {
        //return kart image index
        return speed;
    }

    public Point getLocation()
    {
        return location;
    }       //return kart location

    public int getLocationX()
    {
        //return x-coordinate of kart location
        return location.x;
    }

    public int getLocationY()
    {
        //return y-coordinate of kart location
        return location.y;
    }

    public void stopKart()
    {
        speed = 0;
    }                 //set kart speed to zero

    public Rectangle getBounds(){                         //return kart bounds
        return new Rectangle(getLocation().x,getLocation().y,50,50);
    }

    public void setLocationX(int newX) { location.x = newX; }           //set kart X coordinate to new coordinate

    public void setLocationY(int newY) { location.y = newY; }           //set kart Y coordinate to new coordinate

    public void setAlive(boolean newLife) { alive = newLife;}           //set new kart alive status

    public void setCollisionArea(String area) { collisionArea = area;}           //set kart collision area

    public boolean checkOuterCollision()                    //check whether kart collides with outer bound
    {
        //( 50, 100, 750, 500 ) outer edge
        if(location.x + 10 < 50)
        {
            location.x = 50;
            stopKart();
            return true;
        }

        if(location.x > 750)
        {
            location.x = 750;
            stopKart();
            return true;
        }

        if(location.y + 10 < 100)
        {
            location.y = 100;
            stopKart();
            return true;
        }

        if(location.y > 550)
        {
            location.y = 550;
            stopKart();
            return true;
        }
        return false;
    }

    public boolean checkInnerCollision(Rectangle innerBound)
    {
        //method to check collision with grass
        if(getBounds().intersects(innerBound))
        {
            stopKart();
            return true;
        }
        return false;
    }

    public void updateLaps()
    {
        if(threeQuaterWayMark && location.x + 10 <= 425 && location.y > 400 &&
                location.y < 700 && direction == 0)
        {
            threeQuaterWayMark = false;
            lapCounter += 1;
        }

        if(location.x >= 650 && location.y >= 325 && location.y < 345)
        {
            threeQuaterWayMark = true;
        }
    }

    public int getLapCounter()
    {
        return lapCounter;
    }

    public void setKartLaps(int lapUpdate) {
        lapCounter = lapUpdate;
    }
}
