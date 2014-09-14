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

  private static String host, port, username, password;
  private static Session l_session = null;
  private static final Properties props = System.getProperties();
  private static final List<EmailAddress> addressList = new ArrayList<>();

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
    if (user.length() < 3) {
      System.out.println("ERROR: Usernae is too short!");
      username = "@yahoo.com";
      props.put("mail.smtp.user", getUsername());
      return;
    } else if (!user.contains("@")) {
      user += "@yahoo.com";
      setHost("smtp.mail.yahoo.com");
    } else if (user.toLowerCase().contains("@gmail.com")) {
      setHost("smtp.gmail.com");
    } else if (user.toLowerCase().contains("@yahoo.com")) {
      setHost("smtp.mail.yahoo.com");
    } else {
      String extraInfo = null;
      try {
        extraInfo = user.toLowerCase().substring(user.indexOf("::") + 2);
      } catch (Exception e) {
      }
      if (extraInfo == null) {
        ErrorHandler.showErrorWindow("Not Enough Information", "The SMTP server is required for non-Yahoo or non-GMail addresses. Please put ::SMTP.ser.ver:PORT after the email specified.", null);
        username = "@yahoo.com";
        return;
      }
      setHost(extraInfo.substring(0, extraInfo.indexOf(":")));
      if (extraInfo.contains(":")) {
        setPort(extraInfo.substring(extraInfo.indexOf(":") + 1));
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

  public static String getProvider(String ending) {
    try {
      if (ending.startsWith("@")) {
        ending = ending.substring(1);
      }
      switch (ending) {
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

  public static String getCarrierExtension(String carrier) {
    switch (carrier) {
      case "AT&T (MMS)":
        return "@mms.att.net";
      case "AT&T (SMS)":
        return "@txt.att.net";
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
        return getCarrierExtension("AT&T (MMS)");
    }
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
      return false;
    }
    createSession();
    try {
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
      address = temp + address.substring(address.indexOf("@"));
      System.out.println("New Number: " + address);
      completeAddress = address;
    }

    public boolean isValid() {
      System.out.println("completeAddress = " + completeAddress);
      return completeAddress != null && completeAddress.contains("@");
    }
  }

  public static void addEmailAddress(String add) {
    if (add.contains(";")) {
      System.out.println("String contains ;");
      addEmailAddress(convertToList(add));
    } else {
      addEmailAddress(new EmailAddress(add));
    }
  }

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

  public static void addEmailAddress(List<EmailAddress> add) {
    Iterator<EmailAddress> myIt = add.iterator();
    while (myIt.hasNext()) {
      addEmailAddress(myIt.next());
    }
  }

  public static void removeEmailAddress(EmailAddress remove) {
    if (remove == null) {
      return;
    }
    addressList.remove(remove);
  }

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

  public static String convertToString(List<EmailAddress> list) {
    StringBuilder builder = new StringBuilder();
    Iterator<EmailAddress> myIt = list.iterator();
    while (myIt.hasNext()) {
      builder.append(myIt.next().getCompleteAddress());
      builder.append("; ");
    }
    return builder.toString();
  }

  public static List<EmailAddress> getAddressList() {
    return addressList;
  }
}
