package co.edu.unicauca.emailforwarder.control;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

import co.edu.unicauca.emailforwarder.logic.EmailForwarderBO;
import co.edu.unicauca.emailforwarder.logic.SignatureBO;

public class EmailForwarderController {
	Properties configurationProperties;
	
	public EmailForwarderController(String propertiesFilePath, boolean toForwarding, boolean toVerifySignature) {		
		this.configurationProperties=EmailForwarderBO.getInstance().loadProperties(propertiesFilePath, toForwarding, toVerifySignature);		
	}		

	public void forwardEmails() throws Exception{
		if(this.configurationProperties!=null)
			EmailForwarderBO.getInstance().forwardEmails(this.configurationProperties);
		else
			System.err.println("[Email-Forwarder] Configuration properties couldn't be loaded. Check the log...");
	}
	
	public boolean verifySign(byte[] file, byte[] signature) throws Exception{
		if(this.configurationProperties!=null) {			
			return SignatureBO.getInstance().verifySignature(file, signature, this.configurationProperties);			
		}
		else
			throw new Exception("[Email-Forwarder] The signature couldn't be verified because the properties file couldn't be loaded. Check the log...");
	}
	
	public boolean verifySign(InputStream file, InputStream signature) throws Exception{		
		return this.verifySign(IOUtils.toByteArray(file), IOUtils.toByteArray(signature));
		
	}
	
	public boolean isSuccessfullySettingUp(){
		return this.configurationProperties!=null;
	}
}
