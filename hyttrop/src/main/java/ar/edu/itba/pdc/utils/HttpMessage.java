package ar.edu.itba.pdc.utils;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Set;

import ar.thorium.utils.Message;

public class HttpMessage implements Message {

	private HashMap<String, String> header;
	private StringBuilder body;
	
	public HttpMessage(){
        this.body = new StringBuilder();
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
	

    public String getBody() {
        return this.body.toString();
    }

    public void addBody(byte[] bytes) {
        this.body.append(new String(bytes));
    }

	public void setHeader(final String field, final String content){
		header.put(field, content);
	}

	
}
