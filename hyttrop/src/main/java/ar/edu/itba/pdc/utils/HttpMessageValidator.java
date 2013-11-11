package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.message.HttpMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.edu.itba.pdc.statistics.StatisticsWatcher;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
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
    public void putInput(byte[] incomming) {
        if (incomming == null || incomming.length == 0) return;
        if (logger.isDebugEnabled()) logger.debug("Receiving new package, length: "+ incomming.length);
        message = ArrayUtils.addAll(message, incomming);
    }

    @Override
    public Message getMessage() {
        try {
            if (logger.isTraceEnabled()) logger.trace("Trying to get a new message.");
            if (httpMessage == null) {
                boolean messageFound = false;
                int i;
                for (i = 0; i < message.length && !messageFound; i++) {
                    if (message[i] == (byte)'\r' && message[i+1] == (byte)'\n' && message[i+2] == (byte)'\r' && message[i+3] == (byte)'\n') {
                        messageFound = true;
                    }
                }
                if (messageFound) {
                    String[] headers = new String(Arrays.copyOfRange(message, 0, i)).split("\r\n");
                    this.httpMessage = HttpMessage.newMessage(headers[0]);

                    if (logger.isDebugEnabled()) logger.debug(httpMessage);

                    for(int j = 1; j < headers.length; j++) {
                    	int index = headers[j].indexOf(":");
                        httpMessage.setHeader(headers[j].substring(0, index).trim(), headers[j].substring(index+1).trim());
                    }
                    
                    if(httpMessage.containsHeader("Content-Encoding") && 
                			httpMessage.containsHeader("Content-Type") &&
                			httpMessage.getHeader("Content-Encoding").getValue().compareTo("gzip") == 0 &&
                			httpMessage.getHeader("Content-Type").getValue().compareTo("text/plain") == 0){
                		httpMessage.setGzipedStream();
                	}

                    // We check that the message next three bytes aren't the last ones.
                    if ( i + 3 <= message.length -1) {
                        if (logger.isDebugEnabled()) logger.debug("New message comes with body of " + (message.length - (i + 3)) + " bytes");
                        if (logger.isTraceEnabled()) logger.trace(new String(message));
                    	//message.length -1
                        httpMessage.appendToBody(Arrays.copyOfRange(message, i + 3, message.length));
                    }
                    message = new byte[0];
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("Appending " + message.length + " bytes to body");
                if (logger.isTraceEnabled()) logger.trace(new String(message));
                httpMessage.appendToBody(message);
                message = new byte[0];
            }
            if (messageFinilized(httpMessage)) {
                if (logger.isDebugEnabled()) logger.debug("Message finalized.");
                httpMessage.finalizeMessage();

                StatisticsWatcher w = StatisticsWatcher.getInstance();
                if(w.isRunning()){
                    w.updateBytesTransferred(httpMessage.getSize());
                }
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
        if (logger.isDebugEnabled()) logger.debug("HttpMessageValidator::messageFinilizaed; httpMessage: " + httpMessage);
    	if(httpMessage == null){
    		return false;
    	}
    	if(httpMessage.containsHeader("Content-Length")){
    		Integer length = Integer.parseInt(httpMessage.getHeader("Content-Length").getValue());
            if (logger.isDebugEnabled()) logger.debug("Content-Length value: " + length + " Message body size: " + httpMessage.getSize());

    		if(httpMessage.getSize().compareTo(length) == 0){
    			return true;
    		}else{
    			return false;
    		}
    	}
    	return true;
    }
}
