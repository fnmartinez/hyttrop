package ar.nadaChat;

import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: facundo
 * Date: 02/11/13
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class NadaValidator implements SimpleMessageValidator {

    private List<Byte> message;

    public NadaValidator() {
        this.message = new ArrayList<Byte>();
    }

    @Override
    public void putInput(ByteBuffer byteBuffer) {
        for(byte b : byteBuffer.array()) {
            this.message.add(b);
        }
    }

    @Override
    public Message getMessage() {
        int pos;
        if ((pos = this.message.indexOf((byte)'\n')) >= 0) {
            return new NadaMessage(Arrays.toString(this.message.subList(0, pos).toArray()));
        }
        return null;
    }
}
