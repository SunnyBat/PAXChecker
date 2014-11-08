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

  private static String password;
  private static final Properties props = System.getProperties();
  private static final List<EmailAddress> addressList = new ArrayList<>();

  /**
   * Initializes the Email class. Note that this should be run before any other method in the Email class is used.
   */
  public static void init() {
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
   * @param username The username to set
   */
  public static void setUsername(String username) {
    if (username == null) {
      props.put("mail.smtp.user", "@yahoo.com");
      return;
    }
    if (username.length() < 3) {
      System.out.println("ERROR: Username is too short!");
      props.put("mail.smtp.user", "@yahoo.com");
      return;
    } else if (!username.contains("@")) {
      username += "@yahoo.com";
      setHost("smtp.mail.yahoo.com");
    } else if (username.toLowerCase().contains("@gmail.com")) {
      setHost("smtp.gmail.com");
    } else if (username.toLowerCase().contains("@yahoo.com")) {
      setHost("smtp.mail.yahoo.com");
    } else {
      if (!username.contains("::")) {
        System.out.println("ERROR parsing host -- no semicolon found!");
        ErrorHandler.showErrorWindow("ERROR Using Custom Host", "Unable to parse the SMTP server from the given address (" + username + ")! Please make sure this was input correctly!", null);
        props.put("mail.smtp.user", "@yahoo.com");
        return;
      }
      String extraInfo = null;
      try {
        extraInfo = username.toLowerCase().substring(username.indexOf("::") + 2);
        System.out.println("extraInfo: " + extraInfo);
      } catch (Exception e) {
        System.out.println("Error parsing extra info in email.");
      }
      if (extraInfo == null) {
        ErrorHandler.showErrorWindow("Not Enough Information", "The SMTP server is required for non-Yahoo or non-GMail addresses. Please put ::SMTP.ser.ver:PORT after the email specified.", null);
        props.put("mail.smtp.user", "@yahoo.com");
        return;
      }
      String tempHost;
      try {
        if (extraInfo.contains(":")) {
          tempHost = extraInfo.substring(0, extraInfo.indexOf(":"));
          setPort(extraInfo.substring(extraInfo.indexOf(":") + 1));
          System.out.println("Port set to " + getPort());
        } else {
          tempHost = extraInfo;
        }
        setHost(tempHost);
      } catch (Exception e) {
        System.out.println("ERROR parsing host -- no semicolon found!");
        ErrorHandler.showErrorWindow("ERROR Using Custom Host", "Unable to parse the smtp server from the given address (" + extraInfo + ")! Please make sure this was input correctly!", e);
        props.put("mail.smtp.user", "@yahoo.com");
        return;
      }
      username = username.substring(0, username.indexOf("::"));
//      System.out.println("ERROR: Yahoo or Google email required!");
//      System.exit(0);
    }
    System.out.println("Username = " + username);
    props.put("mail.smtp.user", username);
  }

  /**
   * Gets the username currently set.
   *
   * @return
   */
  public static String getUsername() {
    return (String) props.get("mail.smtp.user");
  }

  /**
   * Sets the current host used by the JavaMail API. The host is the TLS/SSL email server to use. Note that this also explicitly trusts said host.
   * This opens up the program to a potential MItM attack, however it does fix the SSL certificate errors that have been popping up.
   *
   * @param h The host address to connect to
   */
  public static void setHost(String h) {
    if (h == null) {
      System.out.println("Cannot set host to null!");
      return;
    }
    props.put("mail.smtp.ssl.trust", h);
    System.out.println("Host set to " + h);
  }

  /**
   * Gets the currently set host. Note that this just returns the set trusted host in the Properties.
   *
   * @return The currently-set host, or null if none has been set.
   */
  public static String getHost() {
    return (String) props.get("mail.smtp.ssl.trust");
  }

  /**
   * Sets the port used by JavaMail. This port is used to access the email server.
   *
   * @param p The port to use
   */
  public static void setPort(String p) {
    if (p == null) {
      System.out.println("Cannot set port to null!");
      return;
    }
    props.put("mail.smtp.port", p);
    System.out.println("Port set to " + p);
  }

  /**
   * Gets the port currently set.
   *
   * @return The port currently set
   */
  public static String getPort() {
    return (String) props.get("mail.smtp.port");
  }

  /**
   * Encrypts the given password and saves the encrypted password in memory for later use.
   *
   * @param pass The password to encrypt and save for later
   * @see #getPassword()
   */
  public static void setPassword(String pass) {
    try {
      password = Encryption.encrypt(pass);
      props.put("mail.smtp.password", password);
    } catch (Exception e) {
      System.out.println("ERROR encrypting password!");
      ErrorHandler.showErrorWindow("ERROR encrypting password", "An error has occurred while attempting to encrypt your password.", e);
    }
  }

  /**
   * Decrypts the currently set password and returns it as a String.
   *
   * @return The plaintext password given to the program
   * @see #setPassword(java.lang.String)
   */
  public static String getPassword() {
    try {
      return Encryption.decrypt(password);
    } catch (Exception e) {
      System.out.println("ERROR decrypting password!");
      ErrorHandler.showErrorWindow("ERROR decrypting password", "And error has occurred while attempting to decrypt your password.", e);
    }
    return null;
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
   * Gets the provider name for a given cell number ending. Note that this is NOT case sensitive. This returns [Other] if no match is found.
   *
   * @param ending The email ending to check
   * @return The provider name of the given email ending
   */
  public static String getProvider(String ending) {
    try {
      if (ending.startsWith("@")) {
        ending = ending.substring(1);
      }
      switch (ending.toLowerCase()) {
        case "mms.att.net":
          return "AT&T (MMS)";
        case "txt.att.net":
          return "AT&T (SMS)";
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
      return "AT&T (MMS)";
    }
  }

  /**
   * Gets the email ending for a given carrier. Note that this defaults to AT&T (MMS) if an invalid carrier is specified. This is not case sensitive.
   *
   * @param carrier
   * @return
   */
  public static String getCarrierExtension(String carrier) {
    switch (carrier.toLowerCase()) {
      case "at&t (mms)":
        return "@mms.att.net";
      case "at&t (sms)":
        return "@txt.att.net";
      case "verizon":
        return "@vtext.com";
      case "sprint":
        return "@messaging.sprintpcs.com";
      case "t-mobile":
        return "@tmomail.net";
      case "u.s. cellular":
        return "@email.uscc.net";
      default:
        System.out.println("ERROR: Unable to identify carrier. Using default AT&T.");
        return getCarrierExtension("AT&T (MMS)");
    }
  }

  /**
   * Sets the {@link #props} settings for the the current email address being used. Call every time the email provider (Yahoo, GMail) changes.
   */
  public static void emailSettings() {
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", "587");
  }

  /**
   * Gets the current instance of the JavaMail session for {@link #props}. This should be called every time you send an email.
   *
   * @return The JavaMail Session with the currently set properties
   */
  public static Session createSession() {
    return Session.getInstance(props);
  }

  /**
   * Sends a test email to every number put into the program and prints whether it was successful or not to the Status window.
   *
   * @return True if email is sent successfully, false if not
   */
  public static boolean testEmail() {
    if (sendMessage("Test", "The test is successful. The PAX Checker is now set up to text your phone when the website updates!")) {
      Checker.setStatusInformationText("Text message successfully sent!");
      return true;
    } else {
      Checker.setStatusInformationText("There was an error sending your text message.");
    }
    return false;
  }

  /**
   * Checks whether the program should send an email. If the username OR the email to send to is null (no valid address/number was given), it returns
   * false. If both are valid, it returns true.
   *
   * @return True if should send email, false if not.
   */
  public static boolean shouldSendEmail() {
    return getUsername() != null && !getUsername().equals("@yahoo.com") && !getAddressList().isEmpty();
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
    if (!shouldSendEmail()) {
      System.out.println("Unable to send email: Program not properly configured.");
      return false;
    }
    Session l_session = createSession();
    try {
      System.out.println("Initializing message...");
      MimeMessage message = new MimeMessage(l_session);
      message.setFrom(new InternetAddress(getUsername()));
      if (getAddressList().size() == 1) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(getAddressList().get(0).getCompleteAddress()));
      } else {
        ListIterator<EmailAddress> lI = getAddressList().listIterator();
        while (lI.hasNext()) {
          EmailAddress address = lI.next();
          System.out.println("Address: " + address.getCompleteAddress());
          message.addRecipient(Message.RecipientType.BCC, new InternetAddress(address.getCompleteAddress()));
        }
      }
      message.setSubject(subject);
      message.setText(msg);
      System.out.println("Message created. Getting Transport session.");
      Transport transport = l_session.getTransport("smtps");
      System.out.println("Transport created. Loggin in...");
      transport.connect(getHost(), getUsername(), getPassword());
      System.out.println("Logged in. Sending message...");
      transport.sendMessage(message, message.getAllRecipients());
      System.out.println("Message Sent!");
      transport.close();
    } catch (MessagingException mex) {
      mex.printStackTrace();
      ErrorHandler.showErrorWindow("ERROR", "The message was unable to be sent.", mex);
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      ErrorHandler.showErrorWindow("ERROR", "An unknown error has occurred while attempting to send the message.", e);
      return false;
    }//end catch block
    System.out.println("Finished sending message.");
    return true;
  }

  /**
   * Sends an email in the background with the given information. Note that this starts a new background Thread and will NOT block until an email is
   * sent.
   *
   * @param title
   * @param message
   */
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

  /**
   * A new implementation of email addresses to email when a new event is found. The goal of this is to make it easier to read and write separate
   * email addresses. This should be used as much as possible to avoid having String conversions and highly-likely IndexOutOfBoundsExceptions and
   * NullPointerExceptions being thrown. This will also free up the cluttered code in Email.java and the Setup GUI.
   */
  public static final class EmailAddress {

    private String completeAddress;

    public EmailAddress(String address) {
      setEmailAddress(address);
    }

    public String getCompleteAddress() {
      return completeAddress;
    }

    public String getAddressWithoutCarrier() {
      if (completeAddress == null) {
        return null; // Return blank? Null is more descriptive...
      }
      return completeAddress.substring(0, completeAddress.indexOf("@"));
    }

    public String getCarrierName() {
      if (completeAddress == null) {
        return null; // Return blank? Null is more descriptive...
      }
      return getProvider(completeAddress.substring(completeAddress.indexOf("@")));
    }

    public String getCarrierEnding() {
      if (completeAddress == null) {
        return null; // Return blank? Null is more descriptive...
      }
      return completeAddress.substring(completeAddress.indexOf("@"));
    }

    public void setEmailAddress(String address) {
      if (address == null) {
        System.out.println("ERROR: Address is null!");
        return;
      } else if (address.length() < 5) { // Seriously if it's less than ~10 characters it's probably invalid, but 4 or less is just absurd.
        System.out.println("ERROR: Email address is too short!");
        return;
      } else if (!address.contains("@")) {
        System.out.println("NOTE: Address " + address + " does not contain ending! Adding AT&T ending...");
        address += getCarrierExtension("AT&T (MMS)");
      }
      System.out.println("Old Number: " + address);
      address = address.trim();
      String temp = address.substring(0, address.indexOf("@"));
      temp = temp.replace("-", ""); // Avoid replacing chars in @car.rier.ext
      temp = temp.replace("(", "");
      temp = temp.replace(")", "");
      temp = temp.replace(" ", ""); // Emails can technically have "\ " in them, but that's a huge fringe case. If it comes up, fix it.
      address = temp + address.substring(address.indexOf("@"));
      System.out.println("New Number: " + address);
      completeAddress = address;
    }

    public boolean isValid() {
      System.out.println("completeAddress = " + completeAddress);
      return completeAddress != null && completeAddress.contains("@");
    }
  }

  /**
   * Adds the given email address to the list of addresses to send to. Note that this does NOT check for duplicates.
   *
   * @param add The address to add to the list
   */
  public static void addEmailAddress(String add) {
    if (add == null) {
      System.out.println("EmailAddress is NULL!");
      return;
    } else if (add.length() < 5) {
      System.out.println("EmailAddress is TOO SHORT!");
      return;
    }
    if (add.contains(";")) {
      System.out.println("String contains ;");
      addEmailAddress(convertToList(add));
    } else {
      addEmailAddress(new EmailAddress(add));
    }
  }

  /**
   * Adds the given email address to the list of addresses to send to. Note that this does NOT check for duplicates.
   *
   * @param add The address to add to the list
   */
  public static void addEmailAddress(EmailAddress add) {
    if (add == null) {
      System.out.println("EmailAddress is NULL!");
      return;
    } else if (!add.isValid()) {
      System.out.println("EmailAddress is INVALID!");
      return;
    }
    addressList.add(add);
  }

  /**
   * Adds the given email addresses to the list of addresses to send to. Note that this does NOT check for duplicates.
   *
   * @param add The addresses to add to the list
   */
  public static void addEmailAddress(List<EmailAddress> add) {
    Iterator<EmailAddress> myIt = add.iterator();
    while (myIt.hasNext()) {
      addEmailAddress(myIt.next());
    }
  }

  public static void removeEmailAddress(String remove) {
    if (remove == null) {
      return;
    }
    Iterator<EmailAddress> myIt = getAddressList().iterator();
    while (myIt.hasNext()) {
      EmailAddress next = myIt.next();
      if (next.getCompleteAddress().matches(remove)) {
        removeEmailAddress(next);
        System.out.println("Removed email address " + remove + " from the list of addresses.");
        return;
      }
    }
    System.out.println("Could not remove address " + remove + " from list of addresses.");
  }

  /**
   * Removes the given email address from the list of addresses to send to. Note that removing
   *
   * @param remove The email address to remove.
   */
  public static void removeEmailAddress(EmailAddress remove) {
    if (remove == null) {
      return;
    }
    addressList.remove(remove);
  }

  /**
   * Converts the given addresses to a List\<EmailAddress\> of addresses. Note that these addresses should be separated with semicolons (";").
   *
   * @param addresses The addresses to convert to a list
   * @return A List\<EmailAddress\> of addresses parsed from the given String
   */
  public static List<EmailAddress> convertToList(String addresses) {
    List<EmailAddress> tempList = new ArrayList<>();
    EmailAddress temp;
    String[] split = addresses.split(";");
    for (String split1 : split) {
      temp = new EmailAddress(split1.trim());
      if (temp.isValid()) {
        tempList.add(temp);
      } else {
        System.out.println("ERROR: Temp address " + split1 + " is not valid!");
      }
    }
    return tempList;
  }

  /**
   * Converts a given List<EmailAddress> to a String containing the raw addresses. Note that each address will be separated by semicolons (";").
   *
   * @param list The List to convert
   * @return A String, separated by semicolons, of the given List
   */
  public static String convertToString(List<EmailAddress> list) {
    StringBuilder builder = new StringBuilder();
    Iterator<EmailAddress> myIt = list.iterator();
    while (myIt.hasNext()) {
      builder.append(myIt.next().getCompleteAddress());
      builder.append("; ");
    }
    return builder.toString();
  }

  /**
   * Gets the current List of addresses that the program will send texts to. This List is final and cannot be reassigned.
   *
   * @return The List of addresses the program will use
   */
  public static List<EmailAddress> getAddressList() {
    return addressList;
  }
}
