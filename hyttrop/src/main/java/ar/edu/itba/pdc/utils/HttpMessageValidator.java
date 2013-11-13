package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.message.HttpMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.edu.itba.pdc.statistics.StatisticsWatcher;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                for (i = 0; i < message.length -3  && !messageFound; i++) {
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
                    
                    removeSpecialHeaders();
                    
                    // We check that the message next three bytes aren't the last ones.
                    if ( i + 3 <= message.length -1) {
                        if (logger.isDebugEnabled()) logger.debug("New message comes with body of " + (message.length - (i + 3)) + " bytes");
                        if (logger.isTraceEnabled()) logger.trace(new String(message));
                    	//message.length -1
                        if(httpMessage.getChunked() || httpMessage.containsHeader("Transfer-Encoding") && httpMessage.getHeader("Transfer-Encoding").getValue().contains("chunked")){
                        	countChunked(i+3);
                        }
                        httpMessage.appendToBody(Arrays.copyOfRange(message, i + 3, message.length));
                    }
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("Appending " + message.length + " bytes to body");
                if (logger.isTraceEnabled()) logger.trace(new String(message));
                if(httpMessage.getChunked() || httpMessage.containsHeader("Transfer-Encoding") && httpMessage.getHeader("Transfer-Encoding").getValue().contains("chunked")){
                	countChunked(0);
                }
                httpMessage.appendToBody(message);
            }
            if (messageFinilized(httpMessage)) {
                if (logger.isInfoEnabled()) logger.info("Message finalized.");
                httpMessage.finalizeMessage();

                StatisticsWatcher w = StatisticsWatcher.getInstance();
                if(w.isRunning()){
                    w.updateBytesTransferred(httpMessage.getSize());
                }
            }
            message = new byte[0];
            return httpMessage;
        }catch (URISyntaxException e1){
            logger.error("There is something wrong with the URI syntax.", e1);
            return HttpResponseMessage.BAD_REQUEST;
        } catch (IOException e) {

            logger.error("There was an error with the origin server.");
            return HttpResponseMessage.INTERAL_SERVER_ERROR_RESPONSE;
        }
    }

    private void removeSpecialHeaders(){
    	if(httpMessage.containsHeader("Content-Type") &&
    			httpMessage.getHeader("Content-Type").getValue().compareTo("text/plain") == 0){
    		if(httpMessage.containsHeader("Content-Encoding") && 
    			httpMessage.getHeader("Content-Encoding").getValue().compareTo("gzip") == 0){
    			httpMessage.setGzipedStream();
    			httpMessage.removeHeader("Content-Encoding");
    		}
        	if(httpMessage.containsHeader("Transfer-Encoding") && 
        			httpMessage.getHeader("Transfer-Encoding").getValue().contains("chunked")){
        		httpMessage.removeHeader("Transfer-Encoding");
        		httpMessage.setChunked();
        		
        	}
    	}
    }
    
    private boolean messageFinilized(HttpMessage httpMessage) {
    	if(httpMessage.isFinalized()){
    		return true;
    	}
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
    	
    	if(httpMessage.getChunked() || httpMessage.containsHeader("Transfer-Encoding") && 
    			httpMessage.getHeader("Transfer-Encoding").getValue().contains("chunked")){
    		if (logger.isDebugEnabled()) logger.debug("Message size: " + httpMessage.getSize() + "Total chunked size: " + httpMessage.getChunkedSize());
    		if(httpMessage.getSize().compareTo(httpMessage.getChunkedSize()) == 0){
    			return true;
    		}else{
            	return false;
            }
    	}
    	return true;
    }
    
    private String[] countChunked(int startIndex){
    	StringBuilder hexa = null;
    	int startChain = 0;
    	int endChain = 0;
    	List<String> resp = new LinkedList<String>();
    	int state = 0;
    	if(httpMessage.getSize() == 0){
    		state = 2;
    		startChain = startIndex;
    		hexa = new StringBuilder();
    	}
    	for (int i = startIndex ; i < message.length; i++ ){
    		if(state == 0){
    			if(message[i] == '\r'){
    				startChain = i;
    				state = 1;
    			}
    		}else if(state == 1){
    			if(message[i] == '\n'){
    				state = 2;
    				hexa = new StringBuilder();
    			}else{
    				state = 0;
    			}
    		}else if(state == 2){
    			if(Character.isDigit((char)message[i]) || (message[i] >= 'a' && message[i] <='f')){
    				hexa.append((char)message[i]);
    			}else if(message[i] == '\r' && hexa.length() != 0){
    				state = 3;
    			}else{
    				state = 0;
    			}
    		}else if(state == 3){
    			if(message[i] == '\n'){
    				endChain = i;
    				resp.add(hexa.toString());
    				if(Integer.valueOf(hexa.toString(), 16).compareTo(0) == 0){
						endChain +=2;
					}
    				if(httpMessage.containsHeader("Content-Type") && 
    						httpMessage.getHeader("Content-Type").getValue().compareTo("text/plain") == 0){
    					
    					message = ArrayUtils.addAll(ArrayUtils.subarray(message, 0, startChain),
    							ArrayUtils.subarray(message, endChain+1, message.length));
    					i=startChain;
    				}else{
    					httpMessage.addChunkedSize(endChain+1 - startChain);
    				}
    				httpMessage.addChunkedSize(Integer.valueOf(hexa.toString(), 16));
    			}
    			state = 1;
    			
    		}
    	}
    	return null;
    }

}
