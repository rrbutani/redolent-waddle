import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

/**
 * Class LEDStripSim - Basic Simulator of addressable LEDs
 * 
 * @author  Rahul Butani
 * @version 0.0.0
 */
public class LEDStripSim extends JApplet implements MouseListener, MouseMotionListener, Runnable
{
    //Instance variables:
    private int x;
    private double [][][] strips;// = new int[2][120][3];
    
    //Display variables:
    int size    = 15;
    int padding = 3; //Must be less than size
    int offset;      //Is auto set
    
    public int wX, wY;
    String debug = "";
    
    private Thread driver = null;
    
    boolean driverPresent = false;
    
    public LEDStripSim()
    {
        strips = new double[27][120][4];
    }
    
    public LEDStripSim(int numStrips, int numPixels, int numParts, boolean driverPresent)
    {
        strips = new double[numStrips][numPixels][numParts];
        this.driverPresent = driverPresent;
    }
    
    public void init()
    {
        if(!driverPresent)
        {
            setFocusable(true);
            addMouseListener(this);
        }
        
        int dWidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        offset = Math.min(Math.abs( ((int)(dWidth - (size * strips[0].length)) / 2) ), 60);
        
        for(int strip = 0; strip < strips.length; strip++)
        {
            for(int pixel = 0; pixel < strips[0].length; pixel++)
            {
                for(int rgb = 0; rgb < strips[0][0].length; rgb++)
                {
                     strips[strip][pixel][rgb] = 255; //Set to white
                }
            }
        }
        
        wX = offset * 2 + strips[0].length * size;
        wY  = (int)(size * (1.5 * (double)strips.length + 3.5));
        resize(wX, wY);
        debug += "Init-ed ";
    }
    
    public void start()
    {
        driver = new Thread(this);
        debug += "| Started ";
    }
    
    public void subpaint(Graphics gT)
    {
        Graphics2D g = (Graphics2D) gT;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.clearRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        //Horizontal Mode
        for(int strip = 0; strip < strips.length; strip++)
        {
            g.setColor(Color.gray);
            g.fillRect(offset - 10 - padding, (int)(size * (1.5 * (double)strip + 2.0)), strips[strip].length * size + 10 + padding * 2, size + padding);
            
            for(int pixelNum = 0; pixelNum < strips[strip].length; pixelNum++)
            {
                g.setColor(Color.lightGray);
                g.fillRect(offset + pixelNum * 15 + padding - 1, (int)(size * (1.5 * (double)strip + 2.0)) + padding - 1, size - padding + 2, size - padding + 2);

                if(strips[strip][pixelNum].length == 4)
                    g.setColor(new Color((int)strips[strip][pixelNum][0], (int)strips[strip][pixelNum][1], (int)strips[strip][pixelNum][2], (int)strips[strip][pixelNum][3]));
                else if(strips[strip][pixelNum].length == 3)
                    g.setColor(new Color((int)strips[strip][pixelNum][0], (int)strips[strip][pixelNum][1], (int)strips[strip][pixelNum][2]));
                else
                    g.setColor(Color.black);
                g.fillOval(offset + pixelNum * 15 + padding, (int)(size * (1.5 * (double)strip + 2.0)) + padding, size - padding, size - padding);
            }
        }
        debug += " | Painted";
        //showStatus(debug);
    }
    
    @Override
    public void paint(Graphics g)
    {
        Graphics offScreenG;
        Image offScreenI = null;
        
        offScreenI = createImage(wX, wY);
        offScreenG = offScreenI.getGraphics();
        
        subpaint(offScreenG);
        
        g.drawImage(offScreenI, 0, 0, this);
        debug += " | Updated";
    }
    
    public void updateDisplay(double [][][] rgbData, boolean transition)
    {
        if(transition == true)
        {
            double [][][] diffs = new double [rgbData.length][rgbData[0].length][rgbData[0][0].length];
            for(int strip = 0; strip < rgbData.length; strip++)
            {
                for(int pixel = 0; pixel < rgbData[strip].length; pixel++)
                {
                    for(int part = 0; part < rgbData[strip][pixel].length; part++)
                    {
                        diffs[strip][pixel][part] = rgbData[strip][pixel][part] - strips[strip][pixel][part];
                        //rgbData[strip][pixel][part] = rgbData[strip][pixel][part] - strips[strip][pixel][part];
                    }
                }
            }
            
            //Linear, 10 step : 10 ms for now
            //Translates to a max of 100 updates a second (1000ms/10ms)
            //Acually, Linear, 5 step : 5 ms
            //Meaning 200 updates a second
            //This holds up the update until this is finished, so make sure the total tmie to transition is less than the time between updates on the driver end.
            for(int i = 0; i < 5/*10*/; i++)
            {
                for(int strip = 0; strip < rgbData.length; strip++)
                {
                    for(int pixel = 0; pixel < rgbData[strip].length; pixel++)
                    {
                        for(int part = 0; part < rgbData[strip][pixel].length; part++)
                        {
                            strips[strip][pixel][part] += diffs[strip][pixel][part]/5 /*10*/;
                        }
                    }
                }
                repaint();
                try
                {
                    Thread.sleep(1);
                } catch (InterruptedException e) {}
            }
        }
        else
        {
            strips = rgbData;
            repaint();
        }
    }
    
    public double [][][] updateDisplay2(double [][][] rgbData, boolean transition)
    {
        if(transition == true)
        {
            double [][][] diffs = new double [rgbData.length][rgbData[0].length][rgbData[0][0].length];
            for(int strip = 0; strip < rgbData.length; strip++)
            {
                for(int pixel = 0; pixel < rgbData[strip].length; pixel++)
                {
                    for(int part = 0; part < rgbData[strip][pixel].length; part++)
                    {
                        diffs[strip][pixel][part] = rgbData[strip][pixel][part] - strips[strip][pixel][part];
//                         System.out.println("I got " + rgbData[strip][pixel][part]);
//                         System.out.println("I had " + strips[strip][pixel][part]);
//                         System.out.println("I now " + (rgbData[strip][pixel][part] - strips[strip][pixel][part]));
                        //rgbData[strip][pixel][part] = rgbData[strip][pixel][part] - strips[strip][pixel][part];
//                         System.out.println();
                    }
                }
            }
            
            return rgbData;
            
            //Linear, 10 step : 10 ms for now
        }
        else
        {
            strips = rgbData;
            repaint();
            
            return rgbData;
        }
    }
    
    public void run()
    {
        for(int pixelNum = 0; pixelNum < strips[0].length; pixelNum++)
        {
            for(int strip = 0; strip < strips.length; strip++)
            {
                strips[strip][pixelNum][0] = 148; 
                strips[strip][pixelNum][1] = 19;
                strips[strip][pixelNum][2] = 191;
                strips[strip][pixelNum][3] = (int)((double)((double)pixelNum/(double)strips[0].length)*230) + 25;
                System.out.println("On pixel #" + pixelNum + ": " + (((double)((double)pixelNum/(double)strips[0].length)*230) + 25));
            }
            try
            {
                repaint();
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // recommended because catching InterruptedException clears interrupt flag
                Thread.currentThread().interrupt();
                // you probably want to quit if the thread is interrupted
                return;
            }
        }
        
        for(int t = 0; t < 1080; t++)
        {
            for(int pixelNum = 0; pixelNum < strips[0].length; pixelNum++)
            {
                for(int strip = 0; strip < strips.length; strip++)
                {
                    strips[strip][pixelNum][3] = (int)Math.abs(Math.sin(Math.toRadians(t)) * (double)255);
                }
            }
            try
            {
                repaint();
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // recommended because catching InterruptedException clears interrupt flag
                Thread.currentThread().interrupt();
                // you probably want to quit if the thread is interrupted
                return;
            }
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    @Override
    public void mouseDragged(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e)
    {
        debug += " | Mouse Left";
        repaint();
    }
    
    @Override
    public void mouseEntered(MouseEvent e)
    {
        debug += " | Mouse Caught";
        driver.start();
        repaint();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        debug = "";
        repaint();
    }
    //@Override
//     public void keyPressed(KeyEvent e)
//     {
//         debug += " | KeyPress caught";
//         repaint();
//         switch(e.getKeyChar())
//         {
//             case 'r': driver.start();
//                       showStatus("Thread started...");
//                       break;
//             default : showStatus("No action defined..");
//         }
//         debug += " | KeyPress caught";
//         repaint();
//     }
//     
//     @Override
//     public void keyReleased(KeyEvent e) {}
//     
//     @Override
//     public void keyTyped(KeyEvent e)
//     {
//         debug += " | KeyType caught";
//         repaint();
//         switch(e.getKeyChar())
//         {
//             case 'r': driver.start();
//                       showStatus("Thread started...");
//                       break;
//             default : showStatus("No action defined..");
//         }
//         debug += " | KeyType caught";
//         repaint();
//     }

//         for(int [][] strip : rgbData)
//         {
//             for(int [] pixel : strip)
//             {
//                 
//             }
//         }
}
