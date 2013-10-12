package ar.thorium.queues.implementations;

import ar.edu.itba.it.pdc.jabxy.network.queues.InputQueue;
import ar.edu.itba.it.pdc.jabxy.network.queues.XMLValidator;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;
import com.google.common.base.Charsets;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import uk.org.retep.niosax.NioSaxParser;
import uk.org.retep.niosax.NioSaxParserFactory;
import uk.org.retep.niosax.NioSaxParserHandler;
import uk.org.retep.niosax.NioSaxSource;
import uk.org.retep.util.state.TriState;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.util.Deque;

public class XMLInputQueue extends ValidatedInputQueue implements InputQueue, ContentHandler, NioSaxParserHandler {

	private NioSaxParser parser;
	private NioSaxSource source;
	private XMLValidator validator;
	private Deque<ByteBuffer> inputs;
	private Deque<ByteBuffer> validatedElements;
	private BufferFactory bufferFactory;
	private String leftOver;

	public XMLInputQueue(BufferFactory bufferFactory, XMLValidator validator)
			throws SAXException, ParserConfigurationException {
		super(bufferFactory, validator);

		NioSaxParserFactory factory = NioSaxParserFactory.getInstance();
		this.parser = factory.newInstance(this);
//		this.parser.startDocument();
		this.leftOver = "";
	}

	@Override
	public synchronized int fillFrom(ByteChannel channel) throws IOException {
		ByteBuffer buffer = this.bufferFactory.newBuffer();
		int bytesRead = channel.read(buffer);

		buffer.flip();
		this.source.setByteBuffer(buffer);
		try {
			this.parser.parse(this.source);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		
		this.inputs.push(buffer);
		
		this.source.compact();
		return bytesRead;
	}

	private void getValidated(){
		
		if(this.validator.isValidMessage() != -1){
			int size = 0;
			for(ByteBuffer b : this.inputs){
				size += b.limit();
			}
			
			ByteBuffer concatenation = ByteBuffer.allocate(size);
			int allocatedSize = 0;
			
			while(!this.inputs.isEmpty()){
				ByteBuffer b = this.inputs.poll();
				int bSize = b.limit();
				allocatedSize += bSize;
				concatenation.put(b.array(), allocatedSize, bSize);
			}
			
			validatedElements.push(concatenation);
		}
	}
	
	@Override
	public synchronized boolean isEmpty() {
		return validatedElements.isEmpty();
	}

	@Override
	public int indexOf(byte b) {
		return 0;
	}

	@Override
	public ByteBuffer dequeueBytes(int count) {
		ByteBuffer result = ByteBuffer.allocate(count);
		while (!this.validatedElements.isEmpty()) {
			ByteBuffer e = this.validatedElements.poll();
			e.get(result.array());
			if(count < e.limit()){
				validatedElements.addFirst(e);
			}else{
				count -= e.limit();
			}
		}

		return result;
	}
	
	@Override
	public ByteBuffer dequeueValidatedMessage() {
		return this.validatedElements.poll();
	}

	@Override
	public void discardBytes(int count) {
		this.leftOver = this.leftOver.substring(count);
	}

	// XXX: ContentHandler interface Overridal

	@Override
	public void setDocumentLocator(Locator locator) {
		this.validator.setDocumentLocator(locator);
		getValidated();
	}

	@Override
	public void startDocument() throws SAXException {
		this.validator.startDocument();
		getValidated();
	}

	@Override
	public void endDocument() throws SAXException {
		this.validator.endDocument();
		getValidated();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		this.validator.startPrefixMapping(prefix, uri);
		getValidated();
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		this.validator.endPrefixMapping(prefix);
		getValidated();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (StringUtils.isEmpty(localName) && StringUtils.isEmpty(qName)) {
			throw new SAXException(
					"Namespaces and qualified names not available.");
		}
		this.validator.startElement(uri, localName, qName, atts);
		getValidated();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (StringUtils.isEmpty(localName) && StringUtils.isEmpty(qName)) {
			throw new SAXException(
					"Namespaces and qualified names not not avilable");
		}
		this.validator.endElement(uri, localName, qName);
		getValidated();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		this.validator.characters(ch, start, length);
		getValidated();
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		this.validator.ignorableWhitespace(ch, start, length);
		getValidated();
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		this.validator.processingInstruction(target, data);
		getValidated();
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		this.validator.skippedEntity(name);
		getValidated();
	}

	@Override
	public void xmlDeclaration(String versionInfo, String encoding,
			TriState standalone) {
		// TODO Auto-generated method stub
		if (!Charset.forName(encoding).equals(Charsets.UTF_8)) {
			// Cortar conexión
		}
		
	}
}
