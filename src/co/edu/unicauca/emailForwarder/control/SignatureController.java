package co.edu.unicauca.emailForwarder.control;

import java.util.ArrayList;
import java.util.Properties;

import co.edu.unicauca.emailForwarder.logic.SignatureBO;
import co.edu.unicauca.emailForwarder.model.Attachment;
import co.edu.unicauca.emailForwarder.model.Email;

public class SignatureController {
	Properties properties;
	
	public SignatureController(Properties properties) {
		this.properties=properties;
	}
	
	public void signMailsAttachments(ArrayList<Email> emails) throws Exception{
		if(this.properties!=null) {
			for(Email email : emails)
				this.signMailAttachments(email);			
		}
		else
			throw new Exception("[Email-Forwarder] Mails attachments couldn't be signed because the properties file couldn't be loaded. Check the log...");
	}
	
	public void signMailAttachments(Email email) throws Exception{		
		for(Attachment attachment : email.getAttachments())
			attachment.setSignature(SignatureBO.getInstance().sign(attachment, properties));				
	}
	
	public boolean verifySignature(Attachment attachment, byte[] signature) throws Exception {		
		return SignatureBO.getInstance().verifySignature(attachment, signature, properties); 		
	}
}
