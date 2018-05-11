package co.edu.unicauca.emailForwarder.control;

import java.util.ArrayList;
import java.util.Properties;

import co.edu.unicauca.emailForwarder.logic.EmailProviderBO;
import co.edu.unicauca.emailForwarder.model.Email;

public class EmailProviderController {	
	Properties configurationProperties;	
	
	public EmailProviderController(Properties properties) {
		this.configurationProperties=properties;
	}
	
	public ArrayList<Email> getEmails() throws Exception{
		if(configurationProperties!=null)
			return EmailProviderBO.getInstance().getEmails(configurationProperties);
		else
			throw new Exception("[Email-Forwarder] Mails couldn't be gotten because the properties file couldn't be loaded. Check the log...");
	}

	public void sendEmails(ArrayList<Email> emails) throws Exception {
		if(this.configurationProperties!=null) {
			for(Email email : emails)
				EmailProviderBO.getInstance().sendEmail(email, configurationProperties);			
		}
		else
			throw new Exception("[Email-Forwarder] Mails couldn't be sent because the properties file couldn't be loaded. Check the log...");
	}
}
