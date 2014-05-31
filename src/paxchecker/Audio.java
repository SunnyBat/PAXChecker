package paxchecker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.*;

/**
 *
 * @author SunnyBat
 */
public class Audio {

  private static boolean playSound;
  private static Clip clip;
  
  public static boolean soundEnabled() {
    return playSound;
  }

  /**
   * Sets whether to play the alarm sound when an update is found. This can be called at any time.
   *
   * @param play True to play sound, false to not
   */
  public static void setPlayAlarm(boolean play) {
    playSound = play;
  }

  public static boolean playAlarm() {
    if (!playSound) {
      return false;
    }
    try {
      if (clip != null) {
        clip.stop();
        clip.setFramePosition(0);
      }
      clip = AudioSystem.getClip();
      InputStream audioSrc = PAXChecker.class.getResourceAsStream("/resources/Alarm.wav");
      InputStream bufferedIn = new BufferedInputStream(audioSrc);
      AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
      clip.open(inputStream);
      clip.start();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

}
