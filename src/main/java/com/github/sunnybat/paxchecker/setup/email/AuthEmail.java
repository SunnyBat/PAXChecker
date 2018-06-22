package com.github.sunnybat.paxchecker.setup.email;

import com.github.sunnybat.commoncode.email.account.EmailAccount;

/**
 *
 * @author SunnyBat
 */
public interface AuthEmail {

    public boolean isAuthenticated();

    public EmailAccount getEmailAccount();

    public void recordCurrentFields();

    public void resetChanges();

}
