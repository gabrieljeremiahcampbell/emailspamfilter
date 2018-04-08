package org.emailspamfilter.textclassifier;

import java.util.List;
import java.util.Optional;

import edu.stanford.nlp.simple.Sentence;

public class EmailTextClassifier {

	private static EmailTextClassifier emailTextClassifierSingleton = null;

	private EmailTextClassifier() {
		super();
	}
	
	 public static EmailTextClassifier getInstance() {
	      if(emailTextClassifierSingleton == null) {
	    	  emailTextClassifierSingleton = new EmailTextClassifier();
	      }
	      return emailTextClassifierSingleton;
	   }
	
	public Optional<Integer> mailLanguageClassifier(String[] args) {
	    
		String text = args[2];
		
		Sentence simpleSentence = new Sentence(text);
	    simpleSentence.regexner("/home/gabriel/Projects/emailspamfilter/emailspamfilter_ner.txt", true);
	    
	    /*
	    List<String> emailSubject = simpleSentence.mentions("SPAM");
	    
	    List<String> emailBody = simpleSentence.mentions("SPAM");
	    
	    List<String> emailTextAttachments = simpleSentence.mentions("SPAM");
	    
	    List<String[]> spamEmails =
	    		emailSubject.stream()
	    				.flatMap(i -> emailBody.stream()
	    									.filter(j -> text.indexOf(i) - text.indexOf(j) > 0)
	    									.flatMap(j -> emailTextAttachments.stream()
	    											.map(k -> new String[]{i, j, k})
	    											)
							    )
							    .collect(toList());
		return Optional.of(spamEmails);
		*/
		
		List<String> emailBody = simpleSentence.mentions("SPAMITEM");
		
		if(!emailBody.isEmpty())
			return Optional.of(new Integer(args[0]));
		else
			return Optional.empty();
		
	}
}
