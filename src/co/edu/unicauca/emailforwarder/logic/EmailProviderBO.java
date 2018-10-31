package co.edu.unicauca.emailforwarder.logic;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import com.sun.mail.smtp.SMTPTransport;
import co.edu.unicauca.emailforwarder.model.Attachment;
import co.edu.unicauca.emailforwarder.model.Email;

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
	
	public ArrayList<Email> fetchEmails(Properties properties) throws Exception{                                                                  		 
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
                else if (contentType.contains("text/plain") || contentType.contains("TEXT/PLAIN") || contentType.contains("text/html") || contentType.contains("TEXT/HTML")) {
                    Object content = message.getContent();                    
                    email.setMessage(content != null ? content instanceof String ? content.toString() : "" : null);
                }                               
                emails.add(email);                
            }
            this.folderInbox.setFlags(arrayMessages, new Flags(Flags.Flag.SEEN), true);
            return emails;
		} catch (Exception e) {throw e;}
		finally {
			if(this.folderInbox!=null) try{ this.folderInbox.close(false);}catch(Exception ex) {ex.getStackTrace();};
			if(this.store!=null) try{ this.store.close();}catch(Exception ex) {ex.getStackTrace();};			
		}
    }
	
	private Message[] getNewMessages(Properties properties) throws Exception {			
		Session session = Session.getInstance(properties, null);
        this.store = session.getStore();
        this.store.connect(properties.getProperty("host"), properties.getProperty("forwarder.observedAccount.user"), properties.getProperty("forwarder.observedAccount.password"));
        this.folderInbox = this.store.getFolder("INBOX");
        this.folderInbox.open(Folder.READ_WRITE);
        SearchTerm searchCriteria=getSearchCriteria(properties);
        if(searchCriteria==null) 
        	throw new Exception("The search criteria couldn't be initialized.");
        
        return this.folderInbox.search(searchCriteria);	        	
	}
	
	private SearchTerm getSearchCriteria(Properties properties){
        try {
        	String[] observedSenders=properties.getProperty("forwarder.observedSenders").replaceAll(" ", "").split(",");        	       	        	       
	        SearchTerm fromTerm=null;
	        for(String observedSender : observedSenders) {
	        	if(fromTerm==null)
	        		fromTerm=new FromStringTerm(observedSender);
	        	else
	        		fromTerm=new OrTerm(fromTerm, new FromStringTerm(observedSender));
	        }	        	
	        	        
	        FlagTerm unread = new FlagTerm(new Flags(Flags.Flag.SEEN), false);	        
	        return new AndTerm(fromTerm, unread);
        }catch(Exception ex) {
        	ex.printStackTrace();
        	return null;
        }
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
                attachment.setByteArray(IOUtils.toByteArray(part.getInputStream()));
                email.addAttachment(attachment);                               
            } else                            
                messageContent += part.getContent() instanceof String ? part.getContent().toString() : ""; // this part may be the message content
        }                    
        email.setMessage(messageContent);
	}

	public void sendEmail(Email email, Properties configurationProperties) throws Exception{
		if(email.getMessage()!=null) {
			SMTPTransport transport=null;
			try {
				Session session = Session.getInstance(configurationProperties, null);
				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(configurationProperties.getProperty("forwarder.mailAccount.user"), configurationProperties.getProperty("forwarder.mailAccount.nameToDisplay")));
				/**
				 * Next, this method figures out the recipients of the email. If the email subject
				 * matches the regular expression, then the email is forwarded to the accounts
				 * specified by the property: 'forwarder.forwardTo'. All the emails that doesn't
				 * match the regular expression are forwarded to the accounts specified by the
				 * property: 'forwarder.forwardOtherEmailsTo'.
				 */
				String emailSubjectRegExp=configurationProperties.getProperty("forwarder.subjectRegExp");
				String[] receiversAddresses=null;
				if(emailSubjectRegExp!=null && email.getSubject().matches(emailSubjectRegExp))
					receiversAddresses=configurationProperties.getProperty("forwarder.forwardTo").replaceAll(" ", "").split(",");
				else
					receiversAddresses=configurationProperties.getProperty("forwarder.forwardOtherEmailsTo").replaceAll(" ", "").split(",");
				
				InternetAddress[] receivers=new InternetAddress[receiversAddresses.length];
				for(int i=0; i<receiversAddresses.length; i++)
					receivers[i]=new InternetAddress(receiversAddresses[i]);		       
		        msg.setRecipients(Message.RecipientType.TO, receivers);
		        
		        msg.setSubject(email.getSubject());
		        msg.setSentDate(new Date());
		 
		        // creates multi-part
		        Multipart multipart = new MimeMultipart();
		        
		        // creates message part
		        MimeBodyPart messageBodyPart = new MimeBodyPart();
		        messageBodyPart.setContent(email.getMessage(), "text/html");	 	        	      
		        multipart.addBodyPart(messageBodyPart);
		         
		        if (email.getAttachments() != null && email.getAttachments().size() > 0) {
		            for (Attachment attachment : email.getAttachments()) {                                              
		                ByteArrayDataSource bds = new ByteArrayDataSource(attachment.getByteArray(), this.detectMymeType(attachment.getName(), attachment.getByteArray()));
		                MimeBodyPart attachmentPart = new MimeBodyPart();	                
		                attachmentPart.setDataHandler(new DataHandler(bds)); 
		                attachmentPart.setFileName(attachment.getName());
		                multipart.addBodyPart(attachmentPart);
	
		                bds = new ByteArrayDataSource(attachment.getSignature(), this.detectMymeType(null, attachment.getSignature()));
		                MimeBodyPart signaturePart= new MimeBodyPart();
		                signaturePart.setDataHandler(new DataHandler(bds)); 
		                signaturePart.setFileName(attachment.getName()+".signature");
		                multipart.addBodyPart(signaturePart);
		            }
		        }	 
		        // sets the multi-part as e-mail's content
		        msg.setContent(multipart);
		        // sends the e-mail
		        transport = (SMTPTransport) session.getTransport("smtps");
		        transport.connect("smtp.gmail.com", configurationProperties.getProperty("forwarder.mailAccount.user"), configurationProperties.getProperty("forwarder.mailAccount.password"));
		        transport.sendMessage(msg, msg.getAllRecipients());	        	        
		        
			}catch(Exception ex) {
				System.err.println("[Email-Forwarder] The following email couldn't be sent:");
				printEmailMetaData(email);
				ex.printStackTrace();
				throw ex;
			}finally {if(transport!=null) try {transport.close();} catch (MessagingException e) {e.printStackTrace();}}
		}
		else {
			System.out.println("[Email-Forwarder] The following email wasn't sent because its content is not supported yet:");
			printEmailMetaData(email);
		}
	}
	
	public void sendEmails(ArrayList<Email> emails, Properties configurationProperties) throws Exception {
		if(configurationProperties!=null) {
			for(Email email : emails)
				this.sendEmail(email, configurationProperties);			
		}
		else
			throw new Exception("[Email-Forwarder] Mails couldn't be sent because the properties file couldn't be loaded. Check the log...");
	}
	
	private String detectMymeType(String fileName, byte[] byteArray) {
		try {
			TikaConfig tika = new TikaConfig();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);			
			return tika.getDetector().detect(TikaInputStream.get(byteArray), metadata).getBaseType().toString();
		}catch (Exception e) {e.printStackTrace(); return null;}
	}
	
	private void printEmailMetaData(Email email) {
		System.out.println("\t\t| Sender:\t"+email.getSender()+"\n\t\t| Subject:\t"+email.getSubject()+"\n\t\t| Sent Date:\t"+email.getSentDate());
	}
}