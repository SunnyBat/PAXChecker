package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author SunnyBat
 */
public class Audio {

  private static boolean playSound;
  private static Clip clip;
  private static final LListener listener = new LListener();
  private static File alarmFile;

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
   * Sets the file to play when the alarm is triggered. Note that this currently only works with .WAV files.
   *
   * @param file The path (relative or absolute) to the File to play
   */
  public static void setAlarmFile(String file) {
    System.out.println("Alarmfile");
    setAlarmFile(new File(file));
  }

  /**
   * Sets the file to play when the alarm is triggered. Note that this currently only works with .WAV files.
   *
   * @param alarmFile The File to play
   */
  public static void setAlarmFile(File alarmFile) {
    if (!alarmFile.exists()) {
      System.out.println("Alarm file does not exist.");
    } else if (!alarmFile.getName().toLowerCase().endsWith(".wav")) {
      System.out.println("File is not a WAV file.");
      new ErrorBuilder()
          .setErrorTitle("Cannot use audio file")
          .setErrorMessage("Currently, the only supported alarm audio format is .WAV files. If you would like"
              + " to see support for other audio formats, let me know!")
          .buildWindow();
    } else {
      Audio.alarmFile = alarmFile;
      System.out.println("Set alarm file to " + alarmFile.getName());
    }
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
      InputStream audioSrc;
      if (alarmFile != null) {
        audioSrc = new FileInputStream(alarmFile);
      } else {
        audioSrc = Audio.class.getResourceAsStream("/resources/Alarm.wav");
      }
      InputStream bufferedIn = new BufferedInputStream(audioSrc);
      AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
      clip.open(inputStream);
      clip.start();
      return true;
    } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * A custom LineListener implementation used for stopping the default clip when it's told to stop
   */
  private static class LListener implements LineListener {

    @Override
    public void update(LineEvent le) {
      if (le.getType() == LineEvent.Type.STOP) {
        clip.removeLineListener(listener);
        clip.close();
      }
    }

  }

}
