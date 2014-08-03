package paxchecker;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author SunnyBat
 */
public class Email {

  private static String host, port, username, password, textEmail;
  private static List<String> emailList;
  private static Properties props = System.getProperties();
  private static Session l_session = null;

  /**
   * Initializes the Email class. Note that this should be run before any other method in the Email class is used.
   */
  public static void init() {
    setPort("587");
    emailSettings();
  }

  /**
   * Sets the current email address (username) for the program to use. Note that if the username does not contain an ending (&#64;site.net) or is
   * null, &#64;yahoo.com is automatically appended to the end of the username. If the site ending does not end in &#64;yahoo.com or &#64;gmail.com,
   * the program parses the SMTP server addess after the address ending and two colons.<br>
   * Example: User&#64;site.com::site.smtp.server<br>
   * This also (optionall) parses the site's SMTP port after the SMTP server address and a colon.<br>
   * Example: User&#64;site.com::site.smtp.server:123
   *
   * @param user The username to set
   */
  public static void setUsername(String user) {
    if (user == null) {
      username = "@yahoo.com";
      props.put("mail.smtp.user", getUsername());
      return;
    }
    if (!user.contains("@")) {
      user += "@yahoo.com";
      setHost("smtp.mail.yahoo.com");
    } else if (user.toLowerCase().contains("@gmail.com")) {
      setHost("smtp.gmail.com");
    } else if (user.toLowerCase().contains("@yahoo.com")) {
      setHost("smtp.mail.yahoo.com");
    } else {
      String extraInfo = null;
      try {
        extraInfo = user.toLowerCase().substring(user.indexOf("::"));
      } catch (Exception e) {
      }
      if (extraInfo == null) {
        ErrorHandler.showErrorWindow("Not Enough Information", "The SMTP server is required for non-Yahoo or non-GMail addresses. Please put ::SMTP.ser.ver:PORT after the email specified.", null);
        username = "@yahoo.com";
        return;
      }
      setHost(extraInfo.substring(0, extraInfo.indexOf(":")));
      if (extraInfo.indexOf(":") != -1) {
        setPort(extraInfo.substring(extraInfo.indexOf(":")));
      }
      user = user.substring(0, user.indexOf("::"));
//      System.out.println("ERROR: Yahoo or Google email required!");
//      System.exit(0);
    }
    System.out.println("Username = " + user);
    username = user;
    props.put("mail.smtp.user", getUsername());
  }

  public static String getUsername() {
    return username;
  }

  public static void setHost(String h) {
    host = h;
  }

  public static void setPort(String p) {
    port = p;
  }

  public static void setPassword(String pass) {
    password = pass;
    props.put("mail.smtp.password", password);
  }

  /**
   * Splits the given email address up into email and provider. This always returns an array with two values. Both, one or none of these values may be
   * null.
   *
   * @param emailToSplit The email address to split
   * @return An array with the email and provider separated
   */
  public static String[] splitEmail(String emailToSplit) {
    if (emailToSplit == null) {
      return new String[]{null, null};
    }
    if (emailToSplit.contains("@")) {
      String temp1 = emailToSplit.substring(0, emailToSplit.indexOf("@"));
      String temp2 = emailToSplit.substring(emailToSplit.indexOf("@"));
      return new String[]{temp1, temp2};
    } else {
      return new String[]{emailToSplit, null};
    }
  }

  /**
   * Sets the email address that will be mailed to. This method defaults to
   *
   * @mms.att.net if no extension is specified. While this can be called at any time, it is recommended to only call during Setup.
   * @param num
   */
  @Deprecated
  public static void setCellNum(String num) {
    if (!num.contains("@")) {
      num += "@mms.att.net";
    }
    textEmail = num;
    System.out.println("textEmail = " + textEmail);
  }

  public static String getProvider() {
    if (textEmail == null) {
      return "AT&T";
    }
    return getProvider(textEmail.substring(textEmail.indexOf("@") + 1));
  }

  public static String getProvider(String ending) {
    try {
      if (ending.startsWith("@")) {
        ending = ending.substring(1);
      }
      switch (ending) {
        case "mms.att.net":
          return "AT&T";
        case "vtext.com":
          return "Verizon";
        case "messaging.sprintpcs.com":
          return "Sprint";
        case "tmomail.net":
          return "T-Mobile";
        case "email.uscc.net":
          return "U.S. Cellular";
        default:
          return "[Other]";
      }
    } catch (Exception e) {
      return "AT&T";
    }
  }

  public static String getCarrierExtension(String carrier) {
    switch (carrier) {
      case "AT&T":
        return "@mms.att.net";
      case "Verizon":
        return "@vtext.com";
      case "Sprint":
        return "@messaging.sprintpcs.com";
      case "T-Mobile":
        return "@tmomail.net";
      case "U.S. Cellular":
        return "@email.uscc.net";
      default:
        System.out.println("ERROR: Unable to identify carrier. Using default AT&T.");
        return getCarrierExtension("AT&T");
    }
  }

  /**
   * <HTML>Sets {@link #textEmail} to the specified email address. If no [AT] symbol is in {@link num},
   * {@link carrier} is used to add the correct carrier email ending to the number. If an invalid carrier is specified, the method defaults to
   * AT&T.<br>
   * Note that this sets {@link #emailList} to null.</HTML>
   *
   * @param num
   * @param carrier
   */
  public static void setCellNum(String num, String carrier) {
    if (num == null) {
      textEmail = null;
      return;
    } else if (num.length() == 0) {
      textEmail = null;
      return;
    }
    if (!num.contains("@")) {
      num += getCarrierExtension(carrier);
    }
    textEmail = num;
    System.out.println("textEmail = " + textEmail);
    emailList = null;
  }

  /**
   * <HTML>Sets the current email list to the String specified. This parses every email address by splitting the String by ; (semicolons).<br>
   * Example String: 1234567890[AT]mms.att.net;2345678901[AT]vtext.net;3456789012[AT]carr.ier.com><br>
   * Note that [AT] should be one character. Javadocs are fun.<br>
   * Also note that this sets {@link #textEmail} to null.
   * </HTML>
   *
   * @param parseList The list of numbers to read through
   */
  @Deprecated
  public static void setCellList(String parseList) {
    setCellList(parseList, getProvider());
  }

  /**
   * <HTML>Sets the current email list to the String specified. This parses every email address by splitting the String by ; (semicolons).<br>
   * Example String: 1234567890[AT]mms.att.net;2345678901[AT]vtext.net;3456789012[AT]carr.ier.com><br>
   * Note that [AT] should be one character. Javadocs are fun.<br>
   * Also note that this sets {@link #textEmail} to null.
   * </HTML>
   *
   * @param parseList The list of numbers to read through
   * @param defaultCarrier The default carrier to email to, if none is specified
   */
  public static void setCellList(String parseList, String defaultCarrier) {
    emailList = new ArrayList<>();
    try {
      String[] parsed = parseList.split(";");
      for (int a = 0; a < parsed.length; a++) {
        System.out.println("Old Number [" + a + "]: " + parsed[a]);
        parsed[a] = parsed[a].trim();
        if (!parsed[a].contains("@")) {
          parsed[a] += getCarrierExtension(defaultCarrier);
        }
        parsed[a] = parsed[a].substring(0, parsed[a].indexOf("@")).replace("-", "") + parsed[a].substring(parsed[a].indexOf("@")); // Avoid replacing chars in @car.rier.ext
        parsed[a] = parsed[a].substring(0, parsed[a].indexOf("@")).replace("(", "") + parsed[a].substring(parsed[a].indexOf("@"));
        parsed[a] = parsed[a].substring(0, parsed[a].indexOf("@")).replace(")", "") + parsed[a].substring(parsed[a].indexOf("@"));
        System.out.println("New Number [" + a + "]: " + parsed[a]);
        emailList.add(parsed[a]);
      }
    } catch (Exception e) {
      emailList = null;
      ErrorHandler.showErrorWindow("ERROR parsing email addresses", "There was a problem reading the email address list specified. Please restart the program and enter a correct list.\nList provided: " + parseList, e);
    }
    textEmail = null;
  }

  /**
   * The email address to send a message to.
   *
   * @return The email address to send a message to, or null if {@link #setCellNum(java.lang.String, java.lang.String)} has not been called
   */
  public static String getTextEmail() {
    return textEmail;
  }

  /**
   * Gets the current List of all email addresses that will be emailed when a message is sent.
   *
   * @return The List<string> of all email addresses being emailed, or null if {@link #setCellList(java.lang.String)} has not been called
   */
  public static List<String> getEmailList() {
    return emailList;
  }

  /**
   * Sets the {@link #props} settings for the the current email address being used. Call every time the email provider (Yahoo, GMail) changes.
   */
  public static void emailSettings() {
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", port);
  }

  /**
   * Gets the current instance of the JavaMail session for {@link #props}. This should be called every time you send an email.
   */
  public static void createSession() {
    l_session = Session.getDefaultInstance(props);
  }

  /**
   * Sends a test email to every number put into the program and prints whether it was successful or not to the Status window.
   */
  public static boolean testEmail() {
    if (sendMessage("Test", "The test is successful. The PAX Checker is now set up to text your phone when the website updates!")) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setButtonStatusText("Text message successfully sent!");
        return true;
      }
    } else {
      if (PAXChecker.status != null) {
        PAXChecker.status.setButtonStatusText("There was an error sending your text message.");
      }
    }
    return false;
  }

  /**
   * Checks whether the program should send an email. If the username OR the email to send to is null (no valid address/number was given), it returns
   * false. If both are valid, it returns true.
   *
   * @return True if can send email, false if not.
   */
  public static boolean canSendEmail() {
    if (getUsername() != null && (textEmail != null || emailList != null)) {
      if (!getUsername().equals("@yahoo.com")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sends an email to the provided number(s) using the supplied login information. This should only be called once
   * {@link #setUsername(java.lang.String)}, {@link #setPassword(java.lang.String)}, and ({@link #setCellNum(java.lang.String, java.lang.String)} or
   * {@link #setCellList(java.lang.String)}) have been called.
   *
   * @param subject
   * @param msg
   * @return
   */
  public static boolean sendMessage(String subject, String msg) {
    if (!canSendEmail()) {
      return false;
    }
    createSession();
    try {
      MimeMessage message = new MimeMessage(l_session);
      message.setFrom(new InternetAddress(getUsername()));
      if (textEmail != null) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(textEmail));
      } else if (emailList != null) {
        ListIterator<String> lI = emailList.listIterator();
        while (lI.hasNext()) {
          message.addRecipient(Message.RecipientType.BCC, new InternetAddress(lI.next()));
        }
      }
      message.setSubject(subject);
      message.setText(msg);
      Transport transport = l_session.getTransport("smtp");
      transport.connect(host, getUsername(), password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      System.out.println("Message Sent");
    } catch (MessagingException mex) {
      mex.printStackTrace();
      ErrorHandler.showErrorWindow("ERROR", "The message was unable to be sent.", mex);
      return false;
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("ERROR", "An unknown error has occurred while attempting to send the message.", e);
      e.printStackTrace();
      return false;
    }//end catch block
    return true;
  }

  public static void sendEmailInBackground(final String title, final String message) {
    PAXChecker.startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          Email.sendMessage(title, message);
        } catch (Exception e) {
          System.out.println("Unable to send email in background!");
        }
      }
    }, "Send Email");
  }
}
