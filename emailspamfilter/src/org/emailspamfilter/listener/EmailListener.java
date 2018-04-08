/**
 * 
 */
/**
 * @author gabriel j. campbell
 *
 */
package org.emailspamfilter.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;

public class EmailListener {

	private Properties getServerProperties(String protocol,
	         String host, String port) {
	      Properties properties = new Properties();
	      properties.put(String.format("mail.%s.host",
	         protocol), host);
	      properties.put(String.format("mail.%s.port",
	         protocol), port);
	      properties.setProperty(
	         String.format("mail.%s.socketFactory.class",
	            protocol), "javax.net.ssl.SSLSocketFactory");
	      properties.setProperty(
	         String.format("mail.%s.socketFactory.fallback",
	            protocol), "false");
	      properties.setProperty(
	         String.format("mail.%s.socketFactory.port",
	            protocol), String.valueOf(port));

	      return properties;
	   }

	private Session createEmailServerSession(String protocol, String host,
	         String port, String userName, String password) {
		Properties properties = getServerProperties(protocol,
		         host, port);
		      return Session.getDefaultInstance(properties);
		
	}
	   public Optional<List<String[]>> getNewEmails(String protocol, String host,
		         String port, String userName, String password) {
	      List<String[]> messagesDetails = new ArrayList<>();
	      try {
	         Store store = createEmailServerSession(protocol,host,
	    	         port, userName, password).getStore(protocol);
	         store.connect(userName, password);

	         Folder inbox = store.getFolder("INBOX");
	         inbox.open(Folder.READ_WRITE);

	         int count = inbox.getMessageCount();
	         Message[] messages = inbox.getMessages(1, count);
	         for (Message message : messages) {
	            if (!message.getFlags().contains(Flags.Flag.SEEN)) {
	               Address[] fromAddresses = message.getFrom();
	               System.out.println("...................");
	               System.out.println("\t From: "
	                  + fromAddresses[0].toString());
	               System.out.println("\t To: "
	                  + parseAddresses(message
	                  .getRecipients(RecipientType.TO)));
	               System.out.println("\t CC: "
	                  + parseAddresses(message
	                  .getRecipients(RecipientType.CC)));
	               System.out.println("\t Subject: "
	                  + message.getSubject());
	               System.out.println("\t Sent Date:"
	                  + message.getSentDate().toString());
	               try {
	                  System.out.println(getTextFromMessage(message));
	                  messagesDetails.add(new String[]{(new Integer(message.getMessageNumber())).toString(),message.getSubject(), getTextFromMessage(message)});
	               } catch (Exception ex) {
	                  System.out.println("Error reading content!!");
	                  ex.printStackTrace();
	               }
	            }
	         }

	         inbox.close(false);
	         store.close();
	      } catch (NoSuchProviderException ex) {
	         System.out.println("No provider for protocol: "
	            + protocol);
	         ex.printStackTrace();
	      } catch (MessagingException ex) {
	         System.out.println("Could not connect to the message store");
	         ex.printStackTrace();
	      }
	      
	      return Optional.of(messagesDetails);
	   }
	   
	   public void moveEmailToSpamFolder(String protocol, String host,
		         String port, String userName, String password, Optional<Integer> messageNumber) {
		   if(messageNumber.isPresent()) {
		   try {
		         Store store = createEmailServerSession(protocol,host,
		    	         port, userName, password).getStore(protocol);
		         store.connect(userName, password);
		         		         
		         Folder inbox = store.getFolder("INBOX");
		         Folder spamFolder = store.getFolder("MySpam");
		         inbox.open(Folder.READ_WRITE);
		         spamFolder.open(Folder.READ_WRITE);
		         Message message = inbox.getMessage(messageNumber.get());
		         inbox.copyMessages(new Message[] {message}, spamFolder);
		         message.setFlag(Flag.DELETED, true);

		         inbox.close(true);
		         spamFolder.close(false);
		         store.close();
		      } catch (NoSuchProviderException ex) {
		         System.out.println("No provider for protocol: "
		            + protocol);
		         ex.printStackTrace();
		      } catch (MessagingException ex) {
		         System.out.println("Could not connect to the message store");
		         ex.printStackTrace();
		      }
		   }
	   }

	   private String parseAddresses(Address[] address) {

	      String listOfAddress = "";
	      if ((address == null) || (address.length < 1))
	         return null;
	      if (!(address[0] instanceof InternetAddress))
	         return null;

	      for (int i = 0; i < address.length; i++) {
	         InternetAddress internetAddress =
	            (InternetAddress) address[0];
	         listOfAddress += internetAddress.getAddress()+",";
	      }
	      return listOfAddress;
	   }
	   
	   private String getTextFromMessage(Message message) throws MessagingException, IOException {
		    String result = "";
		    if (message.isMimeType("text/plain")) {
		        result = message.getContent().toString();
		    } else if (message.isMimeType("multipart/*")) {
		        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
		        result = getTextFromMimeMultipart(mimeMultipart);
		    }
		    return result;
		}

		private String getTextFromMimeMultipart(
		        MimeMultipart mimeMultipart)  throws MessagingException, IOException{
		    String result = "";
		    int count = mimeMultipart.getCount();
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/plain")) {
		            result = result + "\n" + bodyPart.getContent();
		            break; // without break same text appears twice in my tests
		        } else if (bodyPart.isMimeType("text/html")) {
		            String html = (String) bodyPart.getContent();
		            result = result + "\n" + Jsoup.parse(html).text();
		        } else if (bodyPart.getContent() instanceof MimeMultipart){
		            result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
		        }
		    }
		    return result;
		}
}
