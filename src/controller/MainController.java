package controller;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import model.Email;

public class MainController {

	private static void logEmailInfo(Email email) {	
		System.out.println("\n------------- Email Start -----------------------");
        System.out.println("\t From: " + email.getSender());
        System.out.println("\t Subject: " + email.getSubject());
        System.out.println("\t Sent Date: " + email.getSentDate());
        System.out.println("\t Message: " + email.getMessage());
        System.out.println("\t Attachments: " + email.getAttachments().size());
        System.out.println("\n-------------- Email End ------------------------\n");
	}
	
	private static void forwardEmails() {
		System.out.println("Getting emails...");
		EmailProviderController emailProviderController = new EmailProviderController();
		ArrayList<Email> emails = emailProviderController.getEmails();
		if(emails.size()==0)
			System.out.println("There's no new emails...");
		else
			for(Email email : emails)
				logEmailInfo(email);
	}
	
	public static void main(String[] args) {
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {				
            @Override
            public void run() {        		
                forwardEmails(); // This task will be executed every 5 minutes (300000 miliseconds)
            }
        }, 0, 300000);
	}
}
