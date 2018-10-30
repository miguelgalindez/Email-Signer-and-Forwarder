package co.edu.unicauca.emailforwarder.logic;

import java.io.File;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Properties;

import co.edu.unicauca.emailforwarder.model.Attachment;
import co.edu.unicauca.emailforwarder.model.Email;

public class SignatureBO {
	private static SignatureBO instance = null;	
	
	public static SignatureBO getInstance() {
		if(instance == null) {
			instance = new SignatureBO();
		}
		return instance;
	}
	
	public void signMailsAttachments(ArrayList<Email> emails, Properties properties) throws Exception{
		if(properties!=null) {
			for(Email email : emails)
				for(Attachment attachment : email.getAttachments())
					attachment.setSignature(this.sign(attachment.getByteArray(), properties));
		}
		else
			throw new Exception("[Email-Forwarder] Mails attachments couldn't be signed because the properties file couldn't be loaded. Check the log...");
	}
	
	private byte[] sign(byte[] data, Properties properties) throws InvalidKeyException, Exception{
		Signature rsa = Signature.getInstance("SHA256withRSA"); 
		rsa.initSign(this.getPrivateKey(properties.getProperty("forwarder.privateKeyFile")));
		rsa.update(data);
		return rsa.sign();
	}	
	
	public boolean verifySignature(byte[] file, byte[] signature, Properties properties) throws Exception{
		if(properties!=null) {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(this.getPublicKey(properties.getProperty("forwarder.publicKeyFile")));
			sig.update(file);		
			return sig.verify(signature);
		}
		else
			throw new Exception("[Email-Forwarder] The signature couldn't be verified because the properties file couldn't be loaded. Check the log...");			
	}	
	
	public PrivateKey getPrivateKey(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);		 
	}

	public PublicKey getPublicKey(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}
	
	
}
