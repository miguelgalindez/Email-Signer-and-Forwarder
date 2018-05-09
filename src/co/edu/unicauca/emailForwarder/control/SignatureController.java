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
	
	public boolean signMailsAttachments(ArrayList<Email> emails) {
		if(this.properties!=null) {
			for(Email email : emails) {
				if(this.signMailAttachments(email)==false)
					return false;
			}
			return true;				
		}
		return false;
	}
	
	public boolean signMailAttachments(Email email) {
		if(this.properties!=null) {
			for(Attachment attachment : email.getAttachments())
				attachment.setSignature(SignatureBO.getInstance().sign(attachment, properties));
			return true;
		}
		return false;
	}
	
	public boolean verifySignature(Attachment attachment, byte[] signature) {
		if(this.properties!=null)
			return SignatureBO.getInstance().verifySignature(attachment, signature, properties); 
		return false;
	}
}
