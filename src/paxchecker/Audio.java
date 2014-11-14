package paxchecker;

import paxchecker.tickets.Checker;
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
  private static final lListener listener = new lListener();

  /**
   * Checks whether or not sound is currently enabled. This is set using {@link #setPlayAlarm(boolean)}.
   *
   * @return True to play sound, false to not
   */
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

  /**
   * Plays the alarm. Note that this checks {@link #soundEnabled()} to make sure it's supposed to play. This method only allows one sound to play at a
   * time, and resets the sound currently playing to the beginning.
   *
   * @return True if the alarm was successfully started, false if not
   */
  public static boolean playAlarm() {
    if (!soundEnabled()) {
      return false;
    }
    try {
      if (clip != null) {
        clip.stop();
        clip.setFramePosition(0);
      }
      clip = AudioSystem.getClip();
      clip.addLineListener(listener);
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

  private static class lListener implements LineListener {

    @Override
    public void update(LineEvent le) {
      if (le.getType() == LineEvent.Type.STOP) {
        Checker.setStatusInformationText("Finished playing alarm.");
        clip.removeLineListener(listener);
        clip.close();
      }
    }

  }

}
