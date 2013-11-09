package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.message.HttpMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HttpMessageValidator implements SimpleMessageValidator {

    private byte[] message;
    private HttpMessage httpMessage;
    private static Logger logger = Logger.getLogger(HttpMessageValidator.class);


    public HttpMessageValidator() {
        this.httpMessage = null;
        this.message = new byte[0];
    }

    @Override
    public void putInput(ByteBuffer byteBuffer) {
        if (httpMessage == null) {
            logger.debug("Receiving a message.");
            byte[] incoming = new byte[byteBuffer.limit()];
            byteBuffer.get(incoming);
            message = ArrayUtils.addAll(message, incoming);
        } else {
            logger.debug("Starting to receive a new message.");
            message = new byte[byteBuffer.limit()];
            byteBuffer.get(message);
        }
    }

    @Override
    public Message getMessage() {
        try {
            logger.debug("Trying to get a new message.");
            if (httpMessage == null) {
                boolean messageFound = false;
                int i;
                for (i = 0; i < message.length && !messageFound; i++) {
                    if (message[i] == (byte)'\r' && message[i+1] == (byte)'\n' && message[i+2] == (byte)'\r' && message[i+3] == (byte)'\n') {
                        messageFound = true;
                    }
                }
                if (messageFound) {
                    logger.debug("A complete message was found.");
                    String[] headers = new String(Arrays.copyOfRange(message, 0, i)).split("\r\n");

                    this.httpMessage = HttpMessage.newMessage(headers[0]);

                    for(int j = 1; j < headers.length; j++) {
                        String[] headerValue = headers[j].split(":");
                        httpMessage.setHeader(headerValue[0].trim(), headerValue[1].trim());
                    }
                    
                    if(httpMessage.containsHeader("Content-Encoding") && 
                			httpMessage.containsHeader("Content-Type") &&
                			httpMessage.getHeader("Content-Encoding").getValue().compareTo("gzip") == 0 &&
                			httpMessage.getHeader("Content-Type").getValue().compareTo("text/plain") == 0){
                		httpMessage.setGzipedStream();
                	}

                    // We check that the message next three bytes aren't the last ones.
                    if ( i + 3 <= message.length -1) {
                    	//message.length -1
                        httpMessage.appendToBody(Arrays.copyOfRange(message, i + 3, message.length));
                    }
                    message = new byte[0];
                }
            } else {
                httpMessage.appendToBody(message);
                logger.debug("All bytes appended to body.");
                message = new byte[0];
            }
            if (messageFinilized(httpMessage)) {
                logger.debug("Message finalized.");
                httpMessage.finilize();
            }
            return httpMessage;
        }catch (URISyntaxException e1){
            logger.error("There is something wrong with the URI syntax.", e1);
            return HttpResponseMessage.BAD_REQUEST;
        } catch (IOException e) {

            logger.error("There was an error with the origin server.");
            return HttpResponseMessage.INTERAL_SERVER_ERROR_RESPONSE;
        }
    }

    private boolean messageFinilized(HttpMessage httpMessage) {
    	if(httpMessage == null){
    		return false;
    	}
    	if(httpMessage.containsHeader("Content-Length")){
    		Integer length = Integer.parseInt(httpMessage.getHeader("Content-Length").getValue());
            logger.info("Content: " + length + " message: "+ httpMessage.getSize());
    		if(httpMessage.getSize().compareTo(length) == 0){
    			return true;
    		}else{
    			return false;
    		}
    	}
    	return true;
    }
}
