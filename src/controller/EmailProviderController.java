package controller;

import java.util.ArrayList;
import java.util.Properties;
import logic.EmailProviderBO;
import model.Email;

public class EmailProviderController {
	
	String host;
	String port; 
	String user; 
	String password;
	Properties configurationProperties;	
	
	public EmailProviderController() {
		this.configurationProperties=EmailProviderBO.getInstance().loadPropertiesFile("./src/util/configuration.properties");
		Properties accountProperties=EmailProviderBO.getInstance().loadPropertiesFile(configurationProperties.getProperty("email.account.propertiesFile"));
		this.configurationProperties.put("user", accountProperties.getProperty("user"));
		this.configurationProperties.put("password", accountProperties.getProperty("password"));		
	}
	
	public ArrayList<Email> getEmails(){
		return EmailProviderBO.getInstance().getEmails(configurationProperties);
	}
}
