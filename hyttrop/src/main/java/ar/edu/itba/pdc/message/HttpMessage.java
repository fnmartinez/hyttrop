package ar.edu.itba.pdc.message;

import ar.thorium.utils.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpMessage implements Message {

	private boolean finilized;
    protected Map<String, HttpHeader> headers;
	protected PipedInputStream exposedBody;
    protected PipedOutputStream privateBody;
    private Integer totalBodySize;

    public static HttpMessage newMessage(String firstLine) throws URISyntaxException, IOException {

        String[] firstLineArray = firstLine.split(" ");

        if (firstLine.startsWith("HTTP")) {
            HttpResponseMessage responseMessage;
            String protocol = firstLineArray[0];
            HttpStatusCode statusCode = HttpStatusCode.getStatusCode(Integer.parseInt(firstLineArray[1]));
            if (statusCode.equals(HttpStatusCode.SC_UNKNOWN)) {
                StringBuilder sb = new StringBuilder();
                for(int i = 2; i < firstLineArray.length; i++) {
                    sb.append(firstLineArray[i]);
                }
                responseMessage = new HttpResponseMessage(protocol, statusCode, Integer.parseInt(firstLineArray[1]), sb.toString());
            } else {
                responseMessage = new HttpResponseMessage(protocol, statusCode);
            }
            return responseMessage;
        } else if (firstLineArray.length == 3) {
            HttpRequestMessage requestMessage;
            HttpMethod method = HttpMethod.getMethod(firstLineArray[0].trim());
            String protocol = firstLineArray[2].trim();
            if (method.equals(HttpMethod.UNKNOWN)) {
                requestMessage = new HttpRequestMessage(method, new URI("*"), protocol, firstLine);
            } else {
                URI uri = new URI(firstLineArray[1].trim());
                requestMessage = new HttpRequestMessage(method, uri, protocol);
            }
            return requestMessage;
        } else {
            return null;
        }
    }

	public HttpMessage() throws IOException {
        this.headers = new HashMap<>();
        this.privateBody = new PipedOutputStream();
        this.exposedBody = new PipedInputStream(this.privateBody);
        this.finilized = false;
        this.totalBodySize = 0;
	}
	
	public Collection<HttpHeader> getHeaders(){
		return headers.values();
	}
	
	public HttpHeader getHeader(final String key){
		return headers.get(key);
	}

    public InputStream getBody() {
        return this.exposedBody;
    }

    public void appendToBody(byte[] bytes) throws IOException {
    	addSize(bytes.length);
        this.privateBody.write(bytes);
    }

	public void setHeader(final String field, final String content){
		headers.put(field, new HttpHeader(field, content));
	}

	private synchronized void addSize(int size){
		totalBodySize += size;
	}
	
	public synchronized Integer getSize(){
		return this.totalBodySize;
	}
	
    public boolean isFinilized() {
        return finilized;
    }

    public void finilize() {
        this.finilized = true;
    }
    
    public boolean containsHeader(String header){
    	return headers.containsKey(header);
    }
}
