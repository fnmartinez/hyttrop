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

	private boolean finilized;
	protected Map<String, HttpHeader> headers;
	// protected InputStream exposedBody;
	protected ByteArrayQueue queue;
	// protected OutputStream privateBody;
	private Integer totalBodySize;
	private boolean specialGziped;
	private static Logger logger = Logger.getLogger(HttpMessage.class);

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
		byte[] arr = new byte[0];
		// this.privateBody = new ByteArrayOutputStream();
		// new
		// ByteArrayInputStream(((ByteArrayOutputStream)this.privateBody).toByteArray());
		// this.exposedBody = new PipedInputStream(
		// (PipedOutputStream) this.privateBody);
		this.finilized = false;
		this.specialGziped = false;
		this.totalBodySize = 0;
		this.queue = new ByteArrayQueue();
	}

	public void setGzipedStream() {
		this.specialGziped = true;
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

	public void appendToBody(byte[] bytes) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("Appending " + bytes.length + " to body");
		addSize(bytes.length);
		if (!specialGziped && headers.containsKey("Content-Type")) {
			if (headers.get("Content-Type").getValue().compareTo("text/plain") == 0) {
				this.queue.write(bytes);
				return;
			}
		}
		this.queue.write(bytes);
	}

	public boolean readyToSend() {
		if ((specialGziped && isFinilized()) || (!specialGziped)) {
			return true;
		}
		return false;
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

	public boolean isFinilized() {
		return finilized;
	}

	public void finilize() {
		this.finilized = true;
		if (specialGziped && isFinilized()) {
			try {
				this.queue.write(L33tTransformation.gzipedConvert(this.queue));
			} catch (IOException e) {
                logger.error(e);
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
}
