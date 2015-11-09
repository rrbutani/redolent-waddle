import javax.swing.JFrame;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import java.util.Scanner;

public class TestProgram
{
    private static LEDStripSim display;
    private static JFrame frame;
    
    private static int numStrips = 4;
    private static int numPixels   = 120;
    
    private static double [][][] strips;
    
    public static void main(String [] args)
    {
        setupDisplay();
        startupSequence();
        //rainbow();

        MicrophoneAnalyzer analyzer = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
        analyzer.open();

        int vol = 0, freq = 0;

        while(true)
        {
            vol  = analyzer.getAudioVolume();
            freq = analyzer.getFrequency();
            // System.out.println(analyzer.getAudioVolume());
            // System.out.println(analyzer.getFrequency());
            // brightnessSet_StripRange(analyzer.getAudioVolume(), 100.0, 50.0, 0, 1);
            // numberSet_StripRange(analyzer.getAudioVolume(), 100.0, 0.0, 1, 2);
            // //brightnessSet(analyzer.getAudioVolume(), 100.0, 50.0);
            // colorSet_StripRange(Math.min(analyzer.getFrequency(), 4500.0), 4500.0, 1.0, 2, 3);

            // colorSet_StripRange(Math.min(analyzer.getFrequency(), 4500.0), 4500.0, 1.0, 2, 3);
            // numberSet_StripRange(Math.min(analyzer.getFrequency(), 4500.0), 4500.0, 0.0, 3, 4);

            // brightnessSet_StripRange(vol, 100.0, 50.0, 0, 1);
            numberSet_StripRange(vol, 100.0, 0.0, 1, 2);

            // colorSet_StripRange(Math.min(freq, 4500.0), 4500.0, 1.0, 2, 3);

            colorSet_StripRange(Math.min(freq, 4500.0), 4500.0, 1.0, 3, 4);
            numberSet_StripRange(Math.min(freq, 4500.0), 4500.0, 0.0, 3, 4);

            showStrips();
        }
        
        //pulse();
    }
    
    public static void setupDisplay()
    {
        System.out.println("Setting up local vars...");
        
        strips = new double[numStrips][numPixels][4];
        
        System.out.println("Starting display...");
        
        display = new LEDStripSim(numStrips, numPixels, 4, true);
        display.init();
        
        frame=new JFrame("LED Display");
        frame.add(display);
        
        frame.setSize(display.wX, display.wY + 20); //20 bc JFrames are bad
        frame.setVisible(true);
        
        for(int strip = 0; strip < strips.length; strip++)
        {
            for(int pixel = 0; pixel < strips[strip].length; pixel++)
            {
                for(int part = 0; part < strips[strip][pixel].length; part++)
                {
                    strips[strip][pixel][part] = 0.0;
                }
            }
        }
        showStrips();
    }
    
    public static void startupSequence()
    {
        for(int pixelNum = 0; pixelNum < strips[0].length; pixelNum++)
        {
            for(int strip = 0; strip < strips.length; strip++)
            {
                strips[strip][pixelNum][0] = 148.0; 
                strips[strip][pixelNum][1] = 19.0;
                strips[strip][pixelNum][2] = 191.0;
                strips[strip][pixelNum][3] = (int)((double)((double)pixelNum/(double)strips[0].length)*230) + 25;
                System.out.println("On pixel #" + pixelNum + ": " + strips[strip][pixelNum][3]);
            }
            try
            {
                showStrips();
                Thread.sleep(6); //50 updates a second
            } catch (InterruptedException e) {
                // recommended because catching InterruptedException clears interrupt flag
                Thread.currentThread().interrupt();
                // you probably want to quit if the thread is interrupted
                return;
            }
        }
    }
    
    public static void pulse()
    {
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
                showStrips();
                Thread.sleep(8);
            } catch (InterruptedException e) {
                // recommended because catching InterruptedException clears interrupt flag
                Thread.currentThread().interrupt();
                // you probably want to quit if the thread is interrupted
                return;
            }
        }
    }
    
    public static void rainbow()
    {
        for(int j = 0; j < 256 * 5; j++)
        {
            for(int strip = 0; strip < strips.length; strip++)
            {
                for(int pixel = 0; pixel < strips[strip].length; pixel++)
                {
                    strips[strip][pixel] = colorWheel( ( (pixel * 256 / strips[strip].length) + j) & 255);
                }
            }
            
            try
            {
                showStrips();
                Thread.sleep(8);
            } catch (InterruptedException e) {
                // recommended because catching InterruptedException clears interrupt flag
                Thread.currentThread().interrupt();
                // you probably want to quit if the thread is interrupted
                return;
            }
        }
    }

    public static void brightnessSet(double brightness, double scale, double min)
    {
        for(int strip = 0; strip < strips.length; strip++)
        {
            for(int pixel = 0; pixel < strips[strip].length; pixel++)
            {
                strips[strip][pixel][3] = (brightness/scale)*(255-min) + min;
            }
        }
    }

    public static void brightnessSet_StripRange(double brightness, double scale, double min, int start, int stop)
    {
        for(int strip = start; strip < stop; strip++)
        {
            for(int pixel = 0; pixel < strips[strip].length; pixel++)
            {
                strips[strip][pixel][3] = (brightness/scale)*(255-min) + min;
            }
        }
    }

    public static void colorSet_StripRange(double brightness, double scale, double min, int start, int stop)
    {
        for(int strip = start; strip < stop; strip++)
        {
            for(int pixel = 0; pixel < strips[strip].length; pixel++)
            {
                strips[strip][pixel] = colorWheel((int)((brightness/scale)*(255-min) + min));
            }
        }
    }

    public static void numberSet_StripRange(double brightness, double scale, double min, int start, int stop)
    {
        for(int strip = start; strip < stop; strip++)
        {
            for(int pixel = 0; pixel < ((int)((brightness/scale)*(strips[strip].length-min) + min)); pixel++)
            {
                strips[strip][pixel][3] = 255.0;
            }
            for(int pixel2 = ((int)((brightness/scale)*(strips[strip].length-min) + min)); pixel2 < strips[strip].length; pixel2++)
            {
                strips[strip][pixel2][3] = 0;
            }
        }
    }
    
    public static double [] colorWheel(int pos)
    {
        if(pos < 85)
            return new double[]{pos * 3.0, 255 - pos * 3.0, 0.0, 255.0};
        else if(pos < 170)
        {
            pos -= 85;
            return new double[]{255 - pos * 3.0, 0.0, pos * 3.0, 255.0};
        }
        else
        {
            pos -= 170;
            return new double[]{0.0, pos * 3.0, 255 - pos * 3.0, 255.0};
        }
    }
    //Display Operation Methods
    public static void showStrips()
    {
        //display.updateDisplay(strips);
//         for(int pixel = 0; pixel < 3/*diffed[0].length*/; pixel++)
//         {
//             System.out.println("Original @0," + pixel + ": " + strips[0][pixel][0] + "," + strips[0][pixel][1] + "," + strips[0][pixel][2] + "," + strips[0][pixel][3]);
// //             System.out.println("DIFFED v @0," + pixel + ": " + diffed[0][pixel][0] + "," + diffed[0][pixel][1] + "," + diffed[0][pixel][2] + "," + diffed[0][pixel][3] + "!!");
//         }
        
        
//         double [][][] diffed = display.updateDisplay2(strips, true);

display.updateDisplay(strips, true);
        
//         for(int pixel = 0; pixel < 3/*diffed[0].length*/; pixel++)
//         {
//             System.out.println("Original @0," + pixel + ": " + strips[0][pixel][0] + "," + strips[0][pixel][1] + "," + strips[0][pixel][2] + "," + strips[0][pixel][3]);
//             System.out.println("DIFFED v @0," + pixel + ": " + diffed[0][pixel][0] + "," + diffed[0][pixel][1] + "," + diffed[0][pixel][2] + "," + diffed[0][pixel][3] + "!!");
//         }
    }
}
