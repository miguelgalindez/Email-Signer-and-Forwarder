package model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Attachment {
	String name;
	InputStream inputStream;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public byte[] getBytes() throws IOException {
		return IOUtils.toByteArray(this.inputStream);
	}
}
