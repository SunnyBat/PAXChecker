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
public class KeyboardManager {

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
      myRobot.keyPress(keyCode);
      myRobot.keyRelease(keyCode);
      if (shouldShift(c)) {
        myRobot.keyRelease(KeyEvent.VK_SHIFT);
      }
    }
  }

  public static void enter() {
    typeString("\n");
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
    return '?';
  }

}
