package co.edu.unicauca.emailforwarder.logic;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import co.edu.unicauca.emailforwarder.control.EmailProviderController;
import co.edu.unicauca.emailforwarder.control.SignatureController;
import co.edu.unicauca.emailforwarder.model.Email;

public class ForwardBO {
	private static ForwardBO instance = null;
	
	
	public static ForwardBO getInstance() {
		if(instance == null) {
			instance = new ForwardBO();
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
	
	public void forwardEmails(EmailProviderController emailProviderController, SignatureController signatureController) throws Exception {
		System.out.println("[Email-Forwarder] Getting emails...");
		ArrayList<Email> emails = emailProviderController.fetchEmails();
		if(emails==null)
			System.err.println("[Email-Forwarder] There was a problem getting the emails. Check properties files and try again.");
		else if(emails.size()==0)
			System.out.println("[Email-Forwarder] There are no new emails.");
		else {
			System.out.println("[Email-Forwarder] There are "+emails.size()+" new emails");			
			signatureController.signMailsAttachments(emails);	
			System.out.println("[Email-Forwarder] Emails were successfully signed.");
			emailProviderController.sendEmails(emails);
			System.out.println("[Email-Forwarder] Email forwarding successfully completed.");							
		}
	}	
	
	private ArrayList<String> validatePropertiesFile(Properties properties, Properties propertiesExample, boolean toForwarding, boolean toVerifySignature) {		
		//this.printKeys(properties, propertiesExample);
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
		InputStream inputStream = ForwardBO.class.getClassLoader().getResourceAsStream(filePath);
		if(inputStream!=null) return inputStream;
		// seaching the file outside the project directory (operative system directory)
		else {
			//this.printFileContent(filePath);
			return new FileInputStream(filePath);
		}
	}	
	/*
	private void printFileContent(String filePath) throws Exception {
		System.out.println("[Email-Forwarder] Printing file content "+filePath);
		FileReader fileReader = new FileReader(filePath);
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        while((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }   
        bufferedReader.close();
        System.out.println("------------------------------------------------------------------------");
	}
	
	private void printKeys(Properties prop,  Properties propExample) {
		Set<String> keys = prop.stringPropertyNames();
		Set<String> keysExample = propExample.stringPropertyNames();
		System.out.println("\n[Email-Forwarder]keys for prop:");
		for(String key : keys)
			System.out.println("[Email-Forwarder] "+key+" "+prop.getProperty(key));
		
		System.out.println("\n[Email-Forwarder] Keys for propExample:");
		for(String key : keysExample)
			System.out.println("[Email-Forwarder] "+key+" "+propExample.getProperty(key));
	}*/
}
