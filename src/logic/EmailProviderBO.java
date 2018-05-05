package logic;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.AndTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import model.Attachment;
import model.Email;

public class EmailProviderBO {
	private static EmailProviderBO instance = null;
	Folder folderInbox;
	Store store;
	
	public static EmailProviderBO getInstance() {
		if(instance == null) {
			instance = new EmailProviderBO();
		}
		return instance;
	}
	
	public EmailProviderBO() {
		this.folderInbox=null;
		this.store=null;
	}
	
	public ArrayList<Email> getEmails(Properties properties) {                                                                  		 
		try {      	
            Message[] arrayMessages = this.getNewMessages(properties); 
            ArrayList<Email> emails=new ArrayList<>();
            
            for (int i = 0; i < arrayMessages.length; i++) {
                Message message = arrayMessages[i];
                Email email=new Email();                                               
				email.setSender(message.getFrom()[0].toString());				
                email.setSubject(message.getSubject());
                email.setSentDate(message.getSentDate());                
 
                String contentType = message.getContentType();                
 
                if (contentType.contains("multipart"))
                    this.processMultiPart(message, email);
                else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    Object content = message.getContent();                    
                    email.setMessage(content != null ? content instanceof String ? content.toString() : "" : null);
                }                               
                emails.add(email);                
            }
            return emails;
		} catch (Exception e) {e.printStackTrace();}
		finally {
			if(this.folderInbox!=null) try{ this.folderInbox.close(false);}catch(Exception ex) {ex.getStackTrace();};
			if(this.store!=null) try{ this.store.close();}catch(Exception ex) {ex.getStackTrace();};
		}
        return null;
    }	

	private void processMultiPart(Message message, Email email) throws Exception {		
		Multipart multiPart = (Multipart) message.getContent();
        int numberOfParts = multiPart.getCount();
        String messageContent = "";
        for (int partCount = 0; partCount < numberOfParts; partCount++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                // this part is an attachment
            	Attachment attachment = new Attachment();
            	attachment.setName(part.getFileName());
                attachment.setInputStream(part.getInputStream());                            
                email.addAttachment(attachment);
                
                part.saveFile("/home/miguel/Documentos/attachments" + File.separator + attachment.getName());
            } else                            
                messageContent += part.getContent() instanceof String ? part.getContent().toString() : ""; // this part may be the message content
        }                    
        email.setMessage(messageContent);
	}
	
	private Message[] getNewMessages(Properties properties) throws Exception {			
		Session session = Session.getInstance(properties, null);
        this.store = session.getStore();
        this.store.connect(properties.getProperty("host"), properties.getProperty("user"), properties.getProperty("password"));

        // opens the inbox folder
        this.folderInbox = this.store.getFolder("INBOX");
        this.folderInbox.open(Folder.READ_ONLY);

        // fetches new messages from server
        SearchTerm searchCriteria=getSearchCriteria();
        if(searchCriteria==null) 
        	throw new Exception("The search criteria couldn't be initialized.");
        
        return this.folderInbox.search(searchCriteria);	        	
	}
	
	private SearchTerm getSearchCriteria(){
        try {
	        FlagTerm unread = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
	        FromTerm fromTerm = new FromTerm(new InternetAddress("miguelgalindez@unicauca.edu.co"));
	
	        Calendar cal = Calendar.getInstance();
	        /* Set date to 1 day back from now */
	        cal.roll(Calendar.DATE, false);
	        ReceivedDateTerm latest = new ReceivedDateTerm(DateTerm.GT, cal.getTime());	        
	        SearchTerm andTerm = new AndTerm(unread, latest);
	
	        SearchTerm notFromTerm = new NotTerm(new FromTerm(new InternetAddress("black_listed_domain.com")));
	
	        SubjectTerm subjectTerm = new SubjectTerm("Notable");
	        OrTerm orTerm = new OrTerm(fromTerm, new OrTerm(andTerm, subjectTerm));
	        AndTerm andTerm1 = new AndTerm(orTerm, notFromTerm);
	        /**
	         * (FROM_pritomkucse@gmail.com OR (NOT_REED AND YESTERDAY) OR SUBJECT_CONTAINS_Notable) AND NOT_FROM_*@black_listed_domain.com
	         */
	        return andTerm1;
        }catch(Exception ex) {
        	ex.printStackTrace();
        	return null;
        }
    }

	public Properties loadPropertiesFile(String filePath) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new java.io.FileInputStream(filePath);		
			properties.load(input);
			return properties;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) try { input.close();} catch (Exception e) { e.printStackTrace();}			
		}
		return null;
	}
}
