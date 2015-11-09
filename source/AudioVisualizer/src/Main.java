import javax.sound.sampled.AudioFileFormat;
import java.util.Scanner;

/**
 * Created by rahul_ram on 11/8/15.
 */
public class Main {
    public static void main(String[] args) {
        MicrophoneAnalyzer analyzer = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
        analyzer.open();
        while(true) {
            System.out.println(analyzer.getAudioVolume());
            System.out.println(analyzer.getFrequency());
            //You can even store and write the audio with this API
        }
    }
}
