package minigame.ui;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class MusicPlayer {
    private static byte[] music=null;
    private static AudioFormat format;
    private static Thread bgmThread;
    public static void playBackground(){
        if (music==null){
            try {
                InputStream is=MusicPlayer.class.getClassLoader().getResource("res/music.wav").openStream();
                BufferedInputStream bis=new BufferedInputStream(is);
                AudioInputStream ais=AudioSystem.getAudioInputStream(bis);
                format=ais.getFormat();
                music=new byte[ais.available()];
                System.out.println(music.length);
                ais.read(music);
                ais.close();
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }

        bgmThread=new Thread(()->{
            SourceDataLine sd;
            DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
            try {
                sd= ((SourceDataLine) AudioSystem.getLine(info));
                sd.open(format, 48000);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
            int buffSize= sd.getBufferSize();
            byte[] data=music;
            int pos=0,max= data.length;
            sd.start();
            while (pos<max-buffSize&&bgmThread!=null){
                sd.write(data,pos, buffSize);
                pos+=buffSize;
            }
        });
        bgmThread.start();
    }
    public static void stopBgm(){
        bgmThread=null;
    }
}
