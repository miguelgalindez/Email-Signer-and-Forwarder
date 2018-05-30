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
import java.util.Properties;

import co.edu.unicauca.emailforwarder.model.Attachment;

public class SignatureBO {
	private static SignatureBO instance = null;	
	
	public static SignatureBO getInstance() {
		if(instance == null) {
			instance = new SignatureBO();
		}
		return instance;
	}
	
	
	public byte[] sign(Attachment attachment, Properties properties) throws Exception{		
		return this.sign(attachment.getByteArray(), properties);
	}
	
	public boolean verifySignature(Attachment attachment, byte[] signature, Properties properties) throws Exception{				
		return this.verifySignature(attachment.getByteArray(), signature, properties);		
	}
	
	public boolean verifySignature(byte[] file, byte[] signature, Properties properties) throws Exception{						
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(this.getPublicKey(properties.getProperty("forwarder.publicKeyFile")));
		sig.update(file);		
		return sig.verify(signature);
	}
	
	private byte[] sign(byte[] data, Properties properties) throws InvalidKeyException, Exception{
		Signature rsa = Signature.getInstance("SHA256withRSA"); 
		rsa.initSign(this.getPrivateKey(properties.getProperty("forwarder.privateKeyFile")));
		rsa.update(data);
		return rsa.sign();
	}
	/*
	public PrivateKey getPrivateKey(String filename) throws Exception {					
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());  		
		PBEKeySpec pbeSpec = new PBEKeySpec("unicauca".toCharArray());
	    EncryptedPrivateKeyInfo pkinfo = new EncryptedPrivateKeyInfo(keyBytes);
	    SecretKeyFactory skf = SecretKeyFactory.getInstance(pkinfo.getAlgName());
	    Key secret = skf.generateSecret(pbeSpec);
	    PKCS8EncodedKeySpec keySpec = pkinfo.getKeySpec(secret);
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    return kf.generatePrivate(keySpec);
	}
	*/
	
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