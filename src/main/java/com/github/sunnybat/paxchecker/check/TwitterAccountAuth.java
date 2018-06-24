package com.github.sunnybat.paxchecker.check;

/**
 *
 * @author SunnyBat
 */
public interface TwitterAccountAuth {

    public void setAuthUrl(String url);

    public void promptForAuthorizationPin();

    public String getAuthorizationPin();

    public void updateStatus(String status);

    public void authSuccess();

    public void authFailure();

}
