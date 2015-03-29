package com.github.sunnybat.paxchecker;

import java.io.*;

/**
 *
 * @author Sunny
 */
public class SavePrintStream extends java.io.PrintStream {

  private final FileOutputStream fOut;

  public SavePrintStream(OutputStream out) throws FileNotFoundException {
    this(out, "PAXCheckerOutput.txt");
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
    FileOutputStream temp = null;
    temp = new FileOutputStream(filePath);
    fOut = temp;
  }

  @Override
  public void print(String s) {
    super.print(s);
    if (fOut != null) {
      try {
        fOut.write(s.getBytes());
        fOut.write(System.getProperty("line.separator").getBytes());
      } catch (IOException iOException) {
      }
    }
  }
}
