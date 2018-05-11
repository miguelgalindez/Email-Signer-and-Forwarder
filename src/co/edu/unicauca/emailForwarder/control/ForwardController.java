package co.edu.unicauca.emailForwarder.control;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import co.edu.unicauca.emailForwarder.logic.ForwardBO;

public class ForwardController {
	Properties configurationProperties;
	ArrayList<String> missingProperties;
	
	public ForwardController(String propertiesFilePath, boolean toForwarding, boolean toVerifySignature) {		
		this.configurationProperties=ForwardBO.getInstance().loadProperties(propertiesFilePath, toForwarding, toVerifySignature);		
	}		

	public void forwardEmails() throws Exception{
		if(this.configurationProperties!=null) {			
			EmailProviderController emailProviderController = new EmailProviderController(configurationProperties);
			SignatureController signatureController = new SignatureController(configurationProperties);			
			ForwardBO.getInstance().forwardEmails(emailProviderController, signatureController);
		}
		else
			System.err.println("[Email-Forwarder] Configuration properties couldn't be loaded. Check the log...");
	}
	
	public boolean verifySign(byte[] file, byte[] signature) throws Exception{
		if(this.configurationProperties!=null) {
			SignatureController signatureController = new SignatureController(configurationProperties);								
			return ForwardBO.getInstance().verifySign(file, signature, signatureController);
		}
		else
			throw new Exception("[Email-Forwarder] The signature couldn't be verified because the properties file couldn't be loaded. Check the log...");
	}
	
	public boolean verifySign(InputStream file, InputStream signature) throws Exception{		
		return this.verifySign(IOUtils.toByteArray(file), IOUtils.toByteArray(signature));
		
	}
}
