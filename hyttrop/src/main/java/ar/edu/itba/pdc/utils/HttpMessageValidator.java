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
                    if (message[i] == (byte)'\n' && message[i+1] == (byte)'\r' && message[i+2] == (byte)'\n' && message[i+3] == (byte)'\r') {
                        messageFound = true;
                    }
                }
                if (messageFound) {
                    String[] headers = new String(message).split("\n\r");

                    this.httpMessage = HttpMessage.newMessage(headers[0]);

                    for(String header : headers) {
                        String[] headerValue = header.split(":");
                        httpMessage.setHeader(headerValue[0].trim(), headerValue[1].trim());
                    }
                    httpMessage.appendToBody(Arrays.copyOfRange(message, i, message.length - 1));
                    message = new byte[0];
                }
            } else {
                httpMessage.appendToBody(message);
                message = new byte[0];
                if (messageFinilized(httpMessage)) {
                    httpMessage.finilize();
                }
            }
            return httpMessage;
        }catch (URISyntaxException e1){
            return HttpResponseMessage.BAD_REQUEST;
        } catch (IOException e) {
            return HttpResponseMessage.INTERAL_SERVER_ERROR_RESPONSE;
        }
    }

    private boolean messageFinilized(HttpMessage httpMessage) {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }
}
