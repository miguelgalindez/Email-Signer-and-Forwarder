package co.edu.unicauca.emailforwarder.model;

import java.util.ArrayList;
import java.util.Date;

public class Email {
	String sender;
	String subject;
	Date sentDate;
	String message;
	ArrayList<Attachment> attachments;
	
	public Email() {
		attachments=new ArrayList<>();
	}
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getSentDate() {
		return sentDate;
	}
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ArrayList<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(ArrayList<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	public void addAttachment(Attachment attachment) {
		this.attachments.add(attachment);
	}
}
