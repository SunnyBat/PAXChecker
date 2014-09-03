package paxchecker;

import paxchecker.GUI.ErrorWindow;

/**
 *
 * @author SunnyBat
 */
public class ErrorHandler {

  private static byte errorWindowCount = 0;
  private static ErrorWindow errorWindow;
  private static boolean fatalError;

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when
   * the program encounters an error that impedes its function, not for notifications to the user.
   *
   * @param message The error message to display to the user
   */
  public static void showErrorWindow(String message) {
    showErrorWindow("PAXChecker: Error", "ERROR", message, null);
  }

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when
   * the program encounters an error that impedes its function, not for notifications to the user.
   *
   * @param message The error message to display to the user
   * @param t       The error to display
   */
  public static void showErrorWindow(String message, Throwable t) {
    showErrorWindow("PAXChecker: Error", "ERROR", message, t);
  }

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when
   * the program encounters an error that impedes its function, not for notifications to the user.
   *
   * @param title   The title of the error message
   * @param message The error message to display to the user
   * @param t       The error to display
   */
  public static void showErrorWindow(String title, String message, Throwable t) {
    showErrorWindow("PAXChecker: Error", title, message, t);
  }

  /**
   * Displays a window clearly indicating something has gone wrong. This should be used only when
   * the program encounters an error that impedes its function, not for notifications to the user.
   *
   * @param windowTitle The title of the window (displayed on the taskbar)
   * @param title       The title of the error message
   * @param message     The error message to display to the user
   * @param t           The error to display
   */
  public static void showErrorWindow(String windowTitle, String title, String message, Throwable t) {
    if (errorWindowCount > 10) {
      System.out.println("Stopped showing error windows -- too many!");
      return;
    }
    errorWindow = new ErrorWindow();
    errorWindow.setTitle(windowTitle);
    errorWindow.JLTitle.setText(title);
    errorWindow.JTAError.setText(message);
    errorWindow.setVisible(true);
    if (t != null) {
      errorWindow.JBError.setEnabled(true);
      errorWindow.myError = t;
      t.printStackTrace();
    }
    errorWindowCount++;
  }

  /**
   * Shows the error information of t. It outputs all the information into an {@link Error} window.
   * This should only be accessible from a currently open {@link Error}.
   *
   * @param t The error object
   */
  public static void detailedReport(Throwable t) {
    errorWindow = new ErrorWindow();
    errorWindow.setTitle("Error Information");
    errorWindow.JLTitle.setText("StackTrace Information:");
    errorWindow.JTAError.setLineWrap(false);
    String message = t.toString() + "\n";
    StackTraceElement[] eE = t.getStackTrace();
    for (int a = 0; a < eE.length; a++) {
      message += "at ";
      message += eE[a];
      message += "\n";
    }
    errorWindow.JTAError.setText(message);
    errorWindow.JTAError.setCaretPosition(0);
    errorWindow.setVisible(true);
    errorWindowCount++;
    System.out.println(t.getMessage());
    t.printStackTrace();
  }

  /**
   * Calculates how many error windows are open once an error window is closed. This method unlocks
   * the {@link Signin} window once ALL the error windows are closed. This should ONLY be called
   * when an error window is closed, otherwise unexpected results could occur.
   */
  public static void errWindowClosed() {
    if (--errorWindowCount == 0) {
      if (canExit()) {
        System.exit(0); // Kill all threads
      }
    }
  }

  public static void fatalError() {
    fatalError = true;
    if (PAXChecker.setup != null) {
      PAXChecker.setup.dispose();
    }
    if (PAXChecker.tickets != null) {
      PAXChecker.tickets.dispose();
    }
    if (PAXChecker.status != null) {
      PAXChecker.status.dispose();
    }
    if (PAXChecker.update != null) {
      PAXChecker.update.dispose();
    }
  }

  private static boolean canExit() {
    if (errorWindowCount == 0) {
      if (fatalError) {
        return true;
      }
      int validCount = 0;
      if (PAXChecker.setup != null) {
        if (!PAXChecker.setup.isVisible()) {
          validCount++;
        }
      } else {
        validCount++;
      }
      if (PAXChecker.tickets != null) {
        if (!PAXChecker.tickets.isVisible()) {
          validCount++;
        }
      } else {
        validCount++;
      }
      if (PAXChecker.update != null) {
        if (!PAXChecker.update.isVisible()) {
          validCount++;
        }
      } else {
        validCount++;
      }
      if (PAXChecker.status != null) {
        if (!PAXChecker.status.isDisplayable()) {
          validCount++;
        }
      } else {
        validCount++;
      }
      if (validCount == 4) {
        return true;
      }
    }
    return false;
  }
}
