package ar.edu.itba.pdc.message;

import ar.edu.itba.pdc.utils.ByteArrayQueue;
import ar.edu.itba.pdc.utils.L33tConversion;
import ar.thorium.utils.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class HttpMessage implements Message {

	private boolean finilized;
	protected Map<String, HttpHeader> headers;
	// protected InputStream exposedBody;
	protected ByteArrayQueue queue;
	// protected OutputStream privateBody;
	private Integer totalBodySize;
	private boolean specialGziped;

	public static HttpMessage newMessage(String firstLine)
			throws URISyntaxException, IOException {

		String[] firstLineArray = firstLine.split(" ");

		if (firstLine.startsWith("HTTP")) {
			HttpResponseMessage responseMessage;
			String protocol = firstLineArray[0];
			HttpStatusCode statusCode = HttpStatusCode.getStatusCode(Integer
					.parseInt(firstLineArray[1]));
			if (statusCode.equals(HttpStatusCode.SC_UNKNOWN)) {
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < firstLineArray.length; i++) {
					sb.append(firstLineArray[i]);
				}
				responseMessage = new HttpResponseMessage(protocol, statusCode,
						Integer.parseInt(firstLineArray[1]), sb.toString());
			} else {
				responseMessage = new HttpResponseMessage(protocol, statusCode);
			}
			return responseMessage;
		} else if (firstLineArray.length == 3) {
			HttpRequestMessage requestMessage;
			HttpMethod method = HttpMethod.getMethod(firstLineArray[0].trim());
			String protocol = firstLineArray[2].trim();
			if (method.equals(HttpMethod.UNKNOWN)) {
				requestMessage = new HttpRequestMessage(method, new URI("*"),
						protocol, firstLine);
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
		addSize(bytes.length);
		if (!specialGziped && headers.containsKey("Content-Type")) {
			if (headers.get("Content-Type").getValue().compareTo("text/plain") == 0) {
				this.queue.write(L33tConversion.convert(bytes));
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
				this.queue.write(L33tConversion.gzipedConvert(this.queue));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
