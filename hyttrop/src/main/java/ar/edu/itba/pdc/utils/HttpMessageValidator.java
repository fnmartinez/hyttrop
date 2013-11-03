package ar.edu.itba.pdc.utils;

import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;
import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: facundo
 * Date: 02/11/13
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class HttpMessageValidator implements SimpleMessageValidator {

    private byte[] message;
    private HttpMessage httpMessage;

    public HttpMessageValidator() {
        this.httpMessage = null;
    }

    @Override
    public void putInput(ByteBuffer byteBuffer) {
        byte[] incomming = new byte[byteBuffer.limit()];
        byteBuffer.get(incomming);
        message = ArrayUtils.addAll(message, incomming);
    }

    @Override
    public Message getMessage() {
        if (httpMessage == null) {
            boolean messageFound = false;
            for (int i = 0; i < message.length && !messageFound; i++) {
                if (message[i] == (byte)'\n' && message[i+1] == (byte)'\r' && message[i+2] == (byte)'\n' && message[i+3] == (byte)'\r') {
                    messageFound = true;
                }
            }
            if (messageFound) {
                httpMessage = new HttpMessage();
                String[] headers = new String(message).split("\n\r");
                for(String header : headers) {
                    String[] headerValue = header.split(":");
                    httpMessage.setHeader(headerValue[0], headerValue[1]);
                }
            }
        } else {


        }
        return httpMessage;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
