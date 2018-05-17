package co.edu.unicauca.emailforwarder.control;

import java.util.ArrayList;
import java.util.Properties;

import co.edu.unicauca.emailforwarder.logic.SignatureBO;
import co.edu.unicauca.emailforwarder.model.Attachment;
import co.edu.unicauca.emailforwarder.model.Email;

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
		if(this.properties!=null)
			return SignatureBO.getInstance().verifySignature(attachment, signature, properties);
		throw new Exception("[Email-Forwarder] The signature couldn't be verified because the properties file couldn't be loaded. Check the log...");
	}
	
	public boolean verifySignature(byte[] file, byte[] signature) throws Exception {
		if(this.properties!=null)
			return SignatureBO.getInstance().verifySignature(file, signature, properties);
		throw new Exception("[Email-Forwarder] The signature couldn't be verified because the properties file couldn't be loaded. Check the log...");
	}
}
