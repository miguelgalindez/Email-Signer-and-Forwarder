package controller;

import java.util.ArrayList;
import java.util.Properties;
import logic.EmailProviderBO;
import model.Email;

public class EmailProviderController {	
	Properties configurationProperties;	
	
	public EmailProviderController(Properties properties) {
		this.configurationProperties=properties;
	}
	
	public ArrayList<Email> getEmails(){
		if(configurationProperties!=null)
			return EmailProviderBO.getInstance().getEmails(configurationProperties);
		return null;
	}

	public boolean sendEmails(ArrayList<Email> emails) {
		if(this.configurationProperties!=null) {
			for(Email email : emails) {
				if(EmailProviderBO.getInstance().sendEmail(email, configurationProperties)==false)
					return false;
			}
			return true;
		}
		return false;
	}
}
