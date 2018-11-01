package co.edu.unicauca.emailforwarder.logic;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import co.edu.unicauca.emailforwarder.model.Email;

public class EmailForwarderBO {
	private static EmailForwarderBO instance = null;
	
	
	public static EmailForwarderBO getInstance() {
		if(instance == null) {
			instance = new EmailForwarderBO();
		}
		return instance;
	}
	
	public Properties loadProperties(String propertiesFilePath, boolean toForwarding, boolean toVerifySignature) {
		Properties configurationProperties=this.loadPropertiesFile(propertiesFilePath, "co/edu/unicauca/emailforwarder/util/configuration-example.properties", toForwarding, toVerifySignature);
		if(configurationProperties!=null) {
			if(toVerifySignature)
				return configurationProperties;
			if(toForwarding) {
				Properties observedAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.observedAccount.propertiesFile"), "co/edu/unicauca/emailforwarder/util/email-account-example.properties", toForwarding, toVerifySignature);
				if(observedAccountProperties!=null) {
					configurationProperties.put("forwarder.observedAccount.user", observedAccountProperties.getProperty("user"));
					configurationProperties.put("forwarder.observedAccount.password", observedAccountProperties.getProperty("password"));					
				}
				else 
					return null; 
				
				Properties forwarderMailAccountProperties=loadPropertiesFile(configurationProperties.getProperty("forwarder.mailAccount.propertiesFile"), "co/edu/unicauca/emailforwarder/util/email-account-example.properties", toForwarding, toVerifySignature);
				if(forwarderMailAccountProperties!=null) {
					configurationProperties.put("forwarder.mailAccount.user", forwarderMailAccountProperties.getProperty("user"));
					configurationProperties.put("forwarder.mailAccount.password", forwarderMailAccountProperties.getProperty("password"));
					configurationProperties.put("forwarder.mailAccount.nameToDisplay", forwarderMailAccountProperties.getProperty("nameToDisplay", forwarderMailAccountProperties.getProperty("user")));
					return configurationProperties;
				}				
			}			
		}		
		return null;
	}
	
	public void forwardEmails(Properties configurationProperties) throws Exception {
		ArrayList<Email> emails = EmailProviderBO.getInstance().fetchEmails(configurationProperties);
		if(emails==null)
			System.err.println("[Email-Forwarder] There was a problem getting the emails. Check properties files and try again.");
		else if(emails.size()>0){
			System.out.println("[Email-Forwarder] There are "+emails.size()+" new emails");			
			SignatureBO.getInstance().signMailsAttachments(emails, configurationProperties);	
			System.out.println("[Email-Forwarder] Emails were successfully signed.");
			EmailProviderBO.getInstance().sendEmails(emails, configurationProperties);
			System.out.println("[Email-Forwarder] Email forwarding successfully completed.");							
		}
	}	
	
	private ArrayList<String> validatePropertiesFile(Properties properties, Properties propertiesExample, boolean toForwarding, boolean toVerifySignature) {
		ArrayList<String> missingProperties=new ArrayList<>();
		Set<String> keys = propertiesExample.stringPropertyNames();		
		for(String key : keys) {
			if(toForwarding && key.equals("forwarder.publicKeyFile")==false && (properties.getProperty(key)==null || properties.getProperty(key).equals("")))
				missingProperties.add(key);
			if (toVerifySignature && key.equals("forwarder.publicKeyFile") && (properties.getProperty(key)==null || properties.getProperty(key).equals("")))
				missingProperties.add(key);
		 }
		return missingProperties;			
	}

	private Properties loadPropertiesFile(String propertiesFilePath, String propertiesExampleFilePath, boolean toForwarding, boolean toVerifySignature) {
		InputStream propertiesInputStream=null;
		InputStream propertiesExampleInputStream=null;		
		try {
			propertiesInputStream = this.getInputStreamFromFilePath(propertiesFilePath);
			propertiesExampleInputStream=this.getInputStreamFromFilePath(propertiesExampleFilePath);
			if(propertiesInputStream!=null) {
				Properties properties = new Properties();
				Properties propertiesExample=new Properties();
				properties.load(propertiesInputStream);
				propertiesExample.load(propertiesExampleInputStream);
				ArrayList<String> missingProperties=this.validatePropertiesFile(properties, propertiesExample, toForwarding, toVerifySignature);
				
				if(missingProperties.size()==0)
					return properties;
				else
					System.err.println("[Email-Forwarder] Missing properties in "+propertiesFilePath+": "+missingProperties.toString());
			}
			else
				System.err.println("[Email-Forwarder] The file "+propertiesFilePath+" couldn't be found");
		} catch (Exception ex) {
			System.err.println("[Email-Forwarder] Failed to load properties file: "+propertiesFilePath);
			ex.printStackTrace();
		} finally {
			if (propertiesInputStream != null) try { propertiesInputStream.close();} catch (Exception e) { e.printStackTrace();}			
		}
		return null;
	}
	
	private InputStream getInputStreamFromFilePath(String filePath) throws Exception{
		// searching the file inside the project directory
		InputStream inputStream = EmailForwarderBO.class.getClassLoader().getResourceAsStream(filePath);
		if(inputStream!=null) 
			return inputStream;
		else {
			// seaching the file outside the project directory (operative system directory)
			return new FileInputStream(filePath);
		}
	}		
}
