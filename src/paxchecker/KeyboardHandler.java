package paxchecker;

import paxchecker.browser.Browser;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 *
 * @author Sunnybat
 */
public class KeyboardHandler {

  private static boolean shouldTypeLink;
  private static Robot myRobot;
  private static final char[] shiftChars = {'~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '{', '}', '|', ':', '"', '<', '>', '?'};
  private static final String REDDIT_THREAD_LINK = "https://redd.it/2g9vo7";

  /**
   * Required to use KeyboardHandler. This initializes the KeyboardHandler and sets the Robot typing delay to 5ms between each key action.
   */
  public static void init() {
    try {
      myRobot = new Robot();
      myRobot.setAutoDelay(5);
    } catch (AWTException e) {
      System.out.println("ERROR in KeyboardManager.init(): AWTException!");
      e.printStackTrace();
    }
  }

  /**
   * Sets whether or not to type the link found.
   *
   * @param type True to type the link, false to not
   */
  public static void setTypeLink(boolean type) {
    shouldTypeLink = type;
  }

  /**
   * Gets whether or not the program should type the link.
   *
   * @return True to type the link, false to not
   */
  public static boolean shouldTypeLink() {
    return shouldTypeLink;
  }

  /**
   * Types out the given String. Note that this will most likely only support keys already on the keyboard. Any character not able to be typed on the
   * keyboard with one keypress (not including a SHIFT modifier) has a high probability of showing up as ? instead of the character. This should
   * generally only be used to type out URLs or notifications. This has a 10ms delay between each character typed (5ms between keydown and keyup
   * events).
   *
   * @param type The String to type out
   */
  public static void typeString(String type) {
    for (int a = 0; a < type.length(); a++) {
      char c = type.charAt(a);
      int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
      System.out.println("keyCode = " + keyCode);
      if (shouldShift(c)) {
        System.out.println("Changing...");
        keyCode = KeyEvent.getExtendedKeyCodeForChar(getCharUnshifted(c));
        System.out.println("New char = " + keyCode);
        myRobot.keyPress(KeyEvent.VK_SHIFT);
      }
      try {
        myRobot.keyPress(keyCode);
        myRobot.keyRelease(keyCode);
      } catch (java.lang.IllegalArgumentException e) {
        System.out.println("ERROR: Invalid character typed! Keycode: " + keyCode + " -- char = " + ((char) c));
        typeString("?"); // If this throws an error, there's something SERIOUSLY wrong, and the program should just burn
        continue;
      }
      if (shouldShift(c)) {
        myRobot.keyRelease(KeyEvent.VK_SHIFT);
      }
    }
  }

  /**
   * Presses the ENTER key twice. This is mainly for Reddit, where two line breaks in the raw comment text makes a new paragraph (one line break is
   * equivalent to a space).
   */
  public static void newLine() {
    typeString("\n\n");
  }

  /**
   * Used for Reddit Live line submission. This tabs down, hits Enter, waits two seconds, then tabs back up. This hits the "Make Update" button on
   * Reddit Live.
   */
  public static void submitLine() {
    submitLine(2000);
  }

  /**
   * Submits the given line. Note that this method is almost purely for Reddit Live Threads, but can be used for anything that submits using SHIFT+TAB
   * then ENTER.
   *
   * @param millis The milliseconds to wait before pressing ENTER.
   */
  public static void submitLine(int millis) {
    typeString("\t\n");
    try {
      Thread.sleep(millis);
    } catch (InterruptedException iE) {
    }
    myRobot.keyPress(KeyEvent.VK_SHIFT);
    typeString("\t");
    myRobot.keyRelease(KeyEvent.VK_SHIFT);
  }

  /**
   * Checks whether or not the SHIFT key should be held down to produce a character. Note that this only checks for keys on a standard QWERTY keyboard
   * and does not account for any special characters on other keyboards.
   *
   * @param c The character to check for a SHIFT modifier
   * @return True if SHIFT is held to produce the given character, false if not
   * @see #getCharUnshifted(char)
   */
  private static boolean shouldShift(char c) {
    if (Character.isLowerCase(c)) {
      return false;
    } else if (Character.isUpperCase(c)) {
      return true;
    }
    for (int a = 0; a < shiftChars.length; a++) {
      if (c == shiftChars[a]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the character to press when SHIFT is held down to produce the given character. Note that this only checks of keys on a standard QWERTY
   * keyboard and does not account for any special characters on other keyboards. Note that if the character is already unshifted, this will return a
   * / instead of the same character.
   *
   * @param c
   * @return The unshifted character, or ? if character is already unshifted
   * @see #shouldShift(char)
   */
  private static char getCharUnshifted(char c) {
    if (Character.isUpperCase(c)) {
      System.out.println("Char shifted: " + c + " -> " + Character.toLowerCase(c));
      return Character.toLowerCase(c);
    }
    switch (c) {
      case '?':
        return '/';
      case '~':
        return '`';
      case '|':
        return '\\';
      case '!':
        return '1';
      case '@':
        return '2';
      case '#':
        return '3';
      case '$':
        return '4';
      case '%':
        return '5';
      case '^':
        return '6';
      case '&':
        return '7';
      case '*':
        return '8';
      case '(':
        return '9';
      case ')':
        return '0';
      case '_':
        return '-';
      case '+':
        return '=';
      case '{':
        return '[';
      case '}':
        return ']';
      case ':':
        return ';';
      case '"':
        return '\'';
      case '<':
        return ',';
      case '>':
        return '.';
    }
    System.out.println("No char found");
    return '/';
  }

  /**
   * Types out the link notification for the Reddit Live thread. Note that this really should only be used in the Reddit Live thread, as this program
   * TABS then ENTERS, which chould cause undesirable results in other programs.
   *
   * @param link The link to type out
   */
  public static void typeLinkNotification(String link) {
    if (!shouldTypeLink()) {
      return;
    }
    typeString("The PAXChecker has detected a new Showclix event for " + Browser.getExpo());
    newLine();
    typeString("Event link: " + link);
    newLine();
    typeString("[PAXChecker v" + PAXChecker.VERSION + " information can be found here](" + REDDIT_THREAD_LINK + ")");
    submitLine(30000);
  }

}
