package co.edu.unicauca.emailForwarder.control;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import co.edu.unicauca.emailForwarder.model.Attachment;
import co.edu.unicauca.emailForwarder.model.Email;

public class ForwardController {
	Properties configurationProperties;
	ArrayList<String> missingProperties;
	
	public ForwardController(Properties properties, boolean toForwarding, boolean toVerifySignature) {		
		this.configurationProperties=properties;
		if(toForwarding) {				
			Properties observedAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.observedAccount.propertiesFile"));
			configurationProperties.put("forwarder.observedAccount.user", observedAccountProperties.getProperty("user"));
			configurationProperties.put("forwarder.observedAccount.password", observedAccountProperties.getProperty("password"));
			
			Properties forwarderMailAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.mailAccount.propertiesFile"));
			configurationProperties.put("forwarder.mailAccount.user", forwarderMailAccountProperties.getProperty("user"));
			configurationProperties.put("forwarder.mailAccount.password", forwarderMailAccountProperties.getProperty("password"));
			configurationProperties.put("forwarder.mailAccount.nameToDisplay", forwarderMailAccountProperties.getProperty("nameToDisplay", forwarderMailAccountProperties.getProperty("user")));
		}
		
		this.validatePropertiesFile(toForwarding, toVerifySignature);
		if(this.missingProperties.size()>0) {
			this.configurationProperties=null;
			this.showMissingProperties();
		}
	}		

	public void forwardEmails() {
		if(this.configurationProperties!=null) {
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
		else
			this.showMissingProperties();
	}
	
	public boolean verifySign(byte[] file, byte[] signature) {
		if(this.configurationProperties!=null) {
			SignatureController signatureController = new SignatureController(configurationProperties);
			Attachment attachment=new Attachment();			
			attachment.setByteArray(file);						
			return signatureController.verifySignature(attachment, signature);
		}
		else {
			this.showMissingProperties();
			return false;
		}
	}
	
	private void validatePropertiesFile(boolean toForwarding, boolean toVerifySignature) {
		this.missingProperties=new ArrayList<>();
		Set<String> keys = this.loadPropertiesFile("co/edu/unicauca/emailForwarder/util/configuration-example.properties").stringPropertyNames();
		for(String key : keys) {
			if(toForwarding && key.equals("forwarder.publicKeyFile")==false && (this.configurationProperties.getProperty(key)==null || this.configurationProperties.getProperty(key).equals("")))
				this.missingProperties.add(key);
			if (toVerifySignature && key.equals("forwarder.publicKeyFile") && (this.configurationProperties.getProperty(key)==null || this.configurationProperties.getProperty(key).equals("")))
				this.missingProperties.add(key);
		 }			
	}
	
	private void showMissingProperties() {
		for(String missingProperty : this.missingProperties)
			System.err.println("Missing property: "+missingProperty);
		System.err.println("Please check your configuration properties File, make sure no property is missing and try again.");	
	}
	 
	private Properties loadPropertiesFile(String filePath) {			
		InputStream propertiesInputStream=null;
		Properties properties = new Properties();			
		try {
			propertiesInputStream = ForwardController.class.getClassLoader().getResourceAsStream(filePath);
			properties.load(propertiesInputStream);
			return properties;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (propertiesInputStream != null) try { propertiesInputStream.close();} catch (Exception e) { e.printStackTrace();}			
		}
		return null;
	}
}
