package com.github.sunnybat.paxchecker;

import java.io.*;
import java.util.Calendar;

/**
 *
 * @author Sunny
 */
public class SavePrintStream extends java.io.PrintStream {

  private final FileOutputStream fOut;

  public SavePrintStream(OutputStream out) throws FileNotFoundException {
    this(out, "PAXCheckerOutput " + Calendar.getInstance().get(Calendar.YEAR) + "-" // Slightly hacked-together
        + Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ".txt");
  }

  /**
   * Creates a new SavePrintStream. Wraps the given OutputStream. Saves all output to the given file.
   *
   * @param out The OutputStream to save
   * @param filePath The file to save to, either relative or absolute.
   * @throws java.io.FileNotFoundException If the given File does not exist
   */
  public SavePrintStream(OutputStream out, String filePath) throws FileNotFoundException {
    super(out);
    System.out.println("PrintStream!");
    File myFile = new File(filePath);
    int i = 2;
    while (myFile.exists()) {
      myFile = new File(filePath.substring(0, filePath.lastIndexOf(".")) + " (" + i++ + ")" + filePath.substring(filePath.lastIndexOf(".")));
    }
    fOut = new FileOutputStream(myFile);
  }

  @Override
  public void print(String s) {
    super.print(s);
    if (fOut != null) {
      if (s == null) {
        s = "null"; // Still save "null" to stream
      }
      try {
        fOut.write(s.getBytes());
        fOut.write(System.getProperty("line.separator").getBytes());
      } catch (IOException iOException) {
      }
    }
  }
}
