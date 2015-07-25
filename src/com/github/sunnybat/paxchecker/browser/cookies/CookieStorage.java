package com.github.sunnybat.paxchecker.browser.cookies;

import com.github.sunnybat.paxchecker.browser.Browser;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author SunnyBat
 */
public class CookieStorage {

  public static void saveCookies(URL url) {
    HttpURLConnection httpCon = Browser.setUpConnection(url);
  }

}
