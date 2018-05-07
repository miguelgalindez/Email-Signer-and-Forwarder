package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import model.Attachment;
import model.Email;

public class ForwardController {
	static Properties configurationProperties;
	
	private static void forwardEmails(Properties configurationProperties) {
		System.out.println("Getting emails...");
		EmailProviderController emailProviderController = new EmailProviderController(configurationProperties);
		ArrayList<Email> emails = emailProviderController.getEmails();
		if(emails==null)
			System.err.println("There was a problem getting the emails. Check properties files and try again.");
		else if(emails.size()==0)
			System.out.println("There are no new emails.");
		else {
			System.out.println("There are "+emails.size()+" new emails");
			SignatureController signatureController = new SignatureController(configurationProperties);
			if(signatureController.signMailsAttachments(emails)) {	
				System.out.println("Emails were successfully signed.");
				if(emailProviderController.sendEmails(emails))
					System.out.println("Emails were successfully sent.");
				else
					System.err.println("There was a problem sending emails.");
			}
			else
				System.err.println("There was a problem signing emails");
		}
	}
	
	public static boolean verifySign(String file) {
		InputStream is=null;
		try {			
			SignatureController signatureController = new SignatureController(configurationProperties);
			Attachment attachment=new Attachment();
			is= new FileInputStream(file);
			attachment.setByteArray(IOUtils.toByteArray(is));
			is.close();
			is=new FileInputStream(file+".signature");
			return signatureController.verifySignature(attachment, IOUtils.toByteArray(is));
		}catch(Exception ex) { ex.printStackTrace();}
		 finally { try {is.close();} catch (IOException e) {e.printStackTrace();}}
		return false;
	}
	
	private static Properties loadProperties() {		
		Properties configurationProperties=loadPropertiesFile("./src/util/configuration.properties");
		Properties observedAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.observedAccount.propertiesFile"));
		configurationProperties.put("forwarder.observedAccount.user", observedAccountProperties.getProperty("user"));
		configurationProperties.put("forwarder.observedAccount.password", observedAccountProperties.getProperty("password"));
		
		Properties forwarderMailAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.mailAccount.propertiesFile"));
		configurationProperties.put("forwarder.mailAccount.user", forwarderMailAccountProperties.getProperty("user"));
		configurationProperties.put("forwarder.mailAccount.password", forwarderMailAccountProperties.getProperty("password"));
		return configurationProperties;
	}
	 
	private static Properties loadPropertiesFile(String filePath) {
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
	
	public static void main(String[] args) {				
		configurationProperties=loadProperties();
		forwardEmails(configurationProperties);
		
		/*
		 * System.out.println("Connectors: "+verifySign("/home/miguel/Descargas/connectors.pdf"));
		 * System.out.println("irregular verbs: "+verifySign("/home/miguel/Descargas/irregular verbs.pdf"));
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {				
            @Override
            public void run() {        		
                forwardEmails(); // This task will be executed every 5 minutes (300000 miliseconds)
            }
        }, 0, 300000);
        */        
	}
}
