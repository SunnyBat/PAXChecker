package com.github.sunnybat.paxchecker.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A class to load resources that have been downloaded after the program is run.
 *
 * @author SunnyBat
 */
public class ResourceLoader {

  /**
   * Attempts to load the given file from the PAXChecker resources.
   *
   * @param fileName The name of the file to load
   * @return The InputStream containing the File
   * @throws FileNotFoundException If the file does not exist
   */
  public static InputStream loadResource(String fileName) throws FileNotFoundException {
    File inputFile = new File(ResourceConstants.RESOURCE_LOCATION + fileName);
    InputStream in = new java.io.FileInputStream(inputFile);
    return in;
  }

}
