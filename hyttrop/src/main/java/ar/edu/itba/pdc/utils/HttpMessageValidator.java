package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.message.HttpMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HttpMessageValidator implements SimpleMessageValidator {

    private byte[] message;
    private HttpMessage httpMessage;

    public HttpMessageValidator() {
        this.httpMessage = null;
        this.message = new byte[0];
    }

    @Override
    public void putInput(ByteBuffer byteBuffer) {
        if (httpMessage == null) {
            byte[] incomming = new byte[byteBuffer.limit()];
            byteBuffer.get(incomming);
            message = ArrayUtils.addAll(message, incomming);
        } else {
            message = new byte[byteBuffer.limit()];
            byteBuffer.get(message);
        }
    }

    @Override
    public Message getMessage() {
        try {
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

                    for(int j = 1; j < headers.length; j++) {
                        String[] headerValue = headers[j].split(":");
                        httpMessage.setHeader(headerValue[0].trim(), headerValue[1].trim());
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
                System.out.println("All bytes appended to Message");
                message = new byte[0];
            }
            if (messageFinilized(httpMessage)) {
                httpMessage.finilize();
            }
            return httpMessage;
        }catch (URISyntaxException e1){
            return HttpResponseMessage.BAD_REQUEST;
        } catch (IOException e) {
            return HttpResponseMessage.INTERAL_SERVER_ERROR_RESPONSE;
        }
    }

    private boolean messageFinilized(HttpMessage httpMessage) {
    	if(httpMessage == null){
    		System.out.println("FALSE");
    		return false;
    	}
    	if(httpMessage.containsHeader("Content-Length")){
    		Integer length = Integer.parseInt(httpMessage.getHeader("Content-Length").getValue());
    		System.out.println("Content: " + length + " message: "+ httpMessage.getSize());
    		if(httpMessage.getSize().compareTo(length) == 0){
    			System.out.println("message finalized");
    			return true;
    		}
    	}
    	return true;
    }
}
