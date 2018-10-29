package co.edu.unicauca.emailforwarder.test;

import co.edu.unicauca.emailforwarder.control.EmailForwarderController;

public class Test {

	public static void main(String[] args) throws Exception {
		System.out.println("Running test...");
		EmailForwarderController emailForwarderController = new EmailForwarderController("co/edu/unicauca/emailforwarder/test/email-forwarder-configuration.properties", true, false);
		emailForwarderController.forwardEmails();
		System.out.println("Test completed");
	}

}
