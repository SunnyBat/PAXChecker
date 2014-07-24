/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 *
 * @author Sunnybat
 */
public class KeyboardHandler {

  private static Robot myRobot;
  private static final char[] shiftChars = {'~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '{', '}', '|', ':', '"', '<', '>', '?'};

  public static void init() {
    try {
      myRobot = new Robot();
      myRobot.setAutoDelay(5);
    } catch (AWTException e) {
      System.out.println("ERROR in KeyboardManager.init(): AWTException!");
      e.printStackTrace();
    }
  }

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

  public static void newLine() {
    typeString("\n");
  }

  /**
   * Used for Reddit Live line submission. This tabs down, hits Enter, waits two seconds, then tabs back up. This hits the "Make Update" button on
   * Reddit Live.
   */
  public static void submitLine() {
    typeString("\t\n");
    try {
      Thread.sleep(2000);
    } catch (InterruptedException interruptedException) {
    }
    myRobot.keyPress(KeyEvent.VK_SHIFT);
    typeString("\t");
    myRobot.keyRelease(KeyEvent.VK_SHIFT);
  }

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

  public static void typeLinkNotification(String link) {
    if (!PAXChecker.shouldTypeLink()) {
      return;
    }
    typeString("The PAXChecker has detected a new Showclix event for " + Browser.getExpo());
    newLine();
    typeString("Event link: " + link);
    submitLine();
    typeString("PAXChecker v" + PAXChecker.VERSION + " information can be found at " + PAXChecker.REDDIT_THREAD_LINK);
    submitLine();
  }

}
