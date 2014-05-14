package paxchecker;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 *
 * @author SunnyBat
 */
public class Email {

  private static String host, port, username, password;
  private static Properties props = System.getProperties();
  private static Session l_session = null;

  public static void init() {
    host = "smtp.mail.yahoo.com";
    port = "587";
    emailSettings();
  }
  
  public static void setUsername(String user) {
    if (!user.contains("@")) {
      user += "@yahoo.com";
    } else if (!user.contains("@yahoo.com")) {
      System.out.println("ERROR: Yahoo email required!");
      System.exit(0);
    }
    System.out.println("Username = " + user);
    username = user;
    props.put("mail.smtp.user", username);
  }

  public static void setPassword(String pass) {
    if (pass == null) {
      System.out.println("No password?");
      return;
    }
    if (pass.length() < 8) {
      System.out.println("Password seems weak, >=8 characters is reocmmended.");
    }
    password = pass;
    props.put("mail.smtp.password", password);
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

  public static boolean sendMessage(String toEmail, String subject, String msg) {
    createSession();
    try {
      MimeMessage message = new MimeMessage(l_session);
      message.setFrom(new InternetAddress(username));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
      message.setSubject(subject);
      message.setText(msg);
      Transport transport = l_session.getTransport("smtp");
      transport.connect(host, username, password);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      System.out.println("Message Sent");
    } catch (MessagingException mex) {
      mex.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }//end catch block
    return true;
  }
}
