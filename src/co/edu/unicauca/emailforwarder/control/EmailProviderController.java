package co.edu.unicauca.emailforwarder.control;

import java.util.ArrayList;
import java.util.Properties;

import co.edu.unicauca.emailforwarder.logic.EmailProviderBO;
import co.edu.unicauca.emailforwarder.model.Email;

public class EmailProviderController {	
	Properties configurationProperties;	
	
	public EmailProviderController(Properties properties) {
		this.configurationProperties=properties;
	}
	
	public ArrayList<Email> fetchEmails() throws Exception{
		if(configurationProperties!=null)
			return EmailProviderBO.getInstance().fetchEmails(configurationProperties);
		else
			throw new Exception("[Email-Forwarder] Mails couldn't be fetched because the properties file couldn't be loaded. Check the log...");
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
