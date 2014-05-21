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
  private static List<String> emailList = new ArrayList<>();
  private static Properties props = System.getProperties();
  private static Session l_session = null;

  public static void init() {
    host = "smtp.mail.yahoo.com";
    port = "587";
    emailSettings();
  }

  public static void setUsername(String user) {
    if (user == null) {
      username = null;
      props.put("mail.smtp.user", username);
      return;
    }
    if (!user.contains("@")) {
      user += "@yahoo.com";
    } else if (user.toLowerCase().contains("@gmail.com")) {
      setHost("smtp.gmail.com");
    } else if (!user.toLowerCase().contains("@yahoo.com")) {
      System.out.println("ERROR: Yahoo or Google email required!");
      System.exit(0);
    }
    System.out.println("Username = " + user);
    username = user;
    props.put("mail.smtp.user", username);
  }

  public static String getUsername() {
    return username;
  }

  public static void setHost(String h) {
    host = h;
  }

  public static void setPassword(String pass) {
    if (pass.length() < 8) {
      System.out.println("Password seems weak, >=8 characters is recommended.");
    }
    password = pass;
    props.put("mail.smtp.password", password);
  }

  /**
   * Sets the email address that will be mailed to. This method defaults to
   *
   * @mms.att.net if no
   * extension is specified. While this can be called at any time, it is recommended to only call
   * during Setup.
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

  public static void setCellNum(String num, String carrier) {
    if (num == null) {
      textEmail = null;
      return;
    } else if (num.length() == 0) {
      textEmail = null;
      return;
    }
    if (!num.contains("@")) {
      switch (carrier) {
        case "AT&T":
          num += "@mms.att.net";
          break;
        case "Verizon":
          num += "@vtext.com";
          break;
        case "Sprint":
          num += "@messaging.sprintpcs.com";
          break;
        case "T-Mobile":
          num += "@tmomail.net";
          break;
        case "U.S. Cellular":
          num += "@email.uscc.net";
          break;
        default:
          System.out.println("ERROR: Unable to identify carrier. Using default AT&T.");
          setCellNum(num, "AT&T");
          return;
      }
    }
    textEmail = num;
    System.out.println("textEmail = " + textEmail);
    //emailList = null;
  }

  public static void setCellList(String parseList) {
    String[] parsed = parseList.split(";");
    for (int a = 0; a < parsed.length; a++) {
      emailList.add(parsed[a]);
    }
    textEmail = null;
  }

  public static String getTextEmail() {
    return textEmail;
  }

  public static List<String> getEmailList() {
    return emailList;
  }

  public static void emailSettings() {
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.port", port);
  }

  public static void createSession() {
    l_session = Session.getDefaultInstance(props);
  }

  public static boolean sendMessage(String subject, String msg) {
    if (username == null) {
      return false;
    }
    createSession();
    try {
      MimeMessage message = new MimeMessage(l_session);
      message.setFrom(new InternetAddress(username));
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
      transport.connect(host, username, password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      System.out.println("Message Sent");
    } catch (MessagingException mex) {
      mex.printStackTrace();
      ErrorManagement.showErrorWindow("ERROR", "The message was unable to be sent.", mex);
      return false;
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("ERROR", "An unknown error has occurred while attempting to send the message.", e);
      e.printStackTrace();
      return false;
    }//end catch block
    return true;
  }

  public static class multiThread extends Thread {

    private String s, m;

    public multiThread(String subject, String msg) {
      s = subject;
      m = msg;
    }

    @Override
    public void run() {
      sendMessage(s, m);
    }
  }
}
