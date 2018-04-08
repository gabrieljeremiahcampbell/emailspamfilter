/**
 * 
 */
/**
 * @author gabriel j. campbell
 *
 */
package org.emailspamfilter.controller;

import java.util.List;

import org.emailspamfilter.listener.EmailListener;
import org.emailspamfilter.textclassifier.EmailTextClassifier;


public class EmailSpamController {

public static EmailListener emaillistener = new EmailListener();
	
	public static void main(String... args) throws InterruptedException{
		
	    
		while(true) {
			
			List<String[]> messageDetails = emaillistener.getNewEmails("imap", "imap.gmail.com", "993", "username", "password").get();
			
			messageDetails.stream().forEach(message -> emaillistener.moveEmailToSpamFolder("imap", "imap.gmail.com", "993", "username", "password",EmailTextClassifier.getInstance()
													.mailLanguageClassifier(message)));
			Thread.sleep(5000);
		}
	}

}
