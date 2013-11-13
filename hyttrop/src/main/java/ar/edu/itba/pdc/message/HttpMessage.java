package ar.edu.itba.pdc.message;

import ar.edu.itba.pdc.utils.ByteArrayQueue;
import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.thorium.utils.Message;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class HttpMessage implements Message {

	private boolean finalized;
	protected Map<String, HttpHeader> headers;
	// protected InputStream exposedBody;
	protected ByteArrayQueue queue;
	// protected OutputStream privateBody;
	private Integer totalBodySize;
	private boolean specialGziped;
	private static Logger logger = Logger.getLogger(HttpMessage.class);
	private int chunkedSize;
	private boolean chunked;
	private L33tTransformation transformation;
	private ByteArrayQueue gzipQueue;

    public static HttpMessage newMessage(String firstLine) throws URISyntaxException, IOException {

        if (logger.isTraceEnabled()) logger.trace("HttpMessage:newMessage; firstLine:" + firstLine);
        String[] firstLineArray = firstLine.split(" ");

        if (firstLine.startsWith("HTTP")) {
            HttpResponseMessage responseMessage;
            String protocol = firstLineArray[0];
            HttpStatusCode statusCode = HttpStatusCode.getStatusCode(Integer.parseInt(firstLineArray[1]));
            if (logger.isDebugEnabled()) logger.debug("The new message is a response of kind " + statusCode.toString());
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
            if (logger.isDebugEnabled()) logger.debug("The new message is a request of kind " + method.toString());
            if (method.equals(HttpMethod.UNKNOWN)) {
                requestMessage = new HttpRequestMessage(method, new URI("*"), protocol);
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
		this.finalized = false;
		this.specialGziped = false;
		this.totalBodySize = 0;
		this.queue = new ByteArrayQueue();
		this.chunkedSize = 0;
		this.chunked = false;
		this.transformation = new L33tTransformation();
		this.gzipQueue = null;
	}

	public void setGzipedStream() {
		this.specialGziped = true;
	}
	
	public void setChunked(){
		this.chunked = true;
	}
	
	public boolean getChunked(){
		return this.chunked;
	}

	public Collection<HttpHeader> getHeaders() {
		return headers.values();
	}

	public HttpHeader getHeader(final String key) {
		return headers.get(key);
	}

	public ByteArrayQueue getBody() {
		return this.queue;
	}
	
	public void chunkedAppendToBody(byte[] bytes) throws IOException{
		this.chunkedSize-= bytes.length;
		this.appendToBody(bytes);
	}

	public synchronized void appendToBody(byte[] bytes) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("Appending " + bytes.length + " to body");
		addSize(bytes.length);
			if(headers.containsKey("Content-Type") && 
					headers.get("Content-Type").getValue().compareTo("text/plain") == 0){
				if (!specialGziped) {
					transformation.transform(bytes);
					this.queue.write(bytes);
				}else{
					if(gzipQueue == null){
						gzipQueue = new ByteArrayQueue();
					}
					transformation.addElements(bytes);
					byte[] data;
					while((data = transformation.gzipedConvert(false)).length != 0){
						this.queue.write(data);
					}
				}
			}else{
				this.queue.write(bytes);
			}
		
	}
	
	public boolean getSpecialGziped(){
		return this.specialGziped;
	}

	public boolean readyToSend() {
		if ((specialGziped && isFinalized()) || (!specialGziped)) {
			return true;
		}
		return true;
	}

	public void setHeader(final String field, final String content) {
        if (logger.isTraceEnabled()) logger.trace("Adding header " + field + " with content \"" + content + "\"");
		headers.put(field, new HttpHeader(field, content));
	}

	private synchronized void addSize(int size) {
		totalBodySize += size;
	}

	public synchronized Integer getSize() {
		return this.totalBodySize;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void finalizeMessage() throws IOException {
		this.finalized = true;
		if(specialGziped){
			byte[] data;
			while((data = transformation.gzipedConvert(true)).length != 0){
				this.queue.write(data);
			}
		}
	}

	public boolean containsHeader(String header) {
		return headers.containsKey(header);
	}

	public void removeHeader(String header) {
		if (containsHeader(header)) {
			headers.remove(header);
		}
	}
	
	public void addChunkedSize(int size){
		this.chunkedSize += size;
	}
	
	public int getChunkedSize(){
		return this.chunkedSize;
	}
}
