package ar.edu.itba.pdc.utils;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Set;

import ar.thorium.utils.Message;

public class HttpMessage implements Message {

	private HashMap<String, String> header;
	private BufferedReader body;
	
	public HttpMessage(){
		super();
	}
	
	public Set<String> getHeaderFields(){
		return header.keySet();
	}
	
	public String getHeaderContent(final String key){
		return header.get(key);
	}
	
	public boolean existsHeaderField(final String key){
		return header.containsKey(key);
	}
	
	public BufferedReader getBody(){
		return body;
	}
	
	public void setHeader(final String field, final String content){
		header.put(field, content);
	}
	
	public void setBody(final BufferedReader body){
		this.body = body;
	}
	
}
