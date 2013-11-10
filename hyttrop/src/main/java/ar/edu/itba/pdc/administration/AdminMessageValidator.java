package ar.edu.itba.pdc.administration;

import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.Message;

public class AdminMessageValidator implements SimpleMessageValidator{

    private byte[] message;

    public AdminMessageValidator() {
        this.message = new byte[512];
    }

    @Override
    public void putInput(byte[] bytes) {
        for(int i = 0; i < bytes.length && i < 512; i++) {
            this.message[i] = bytes[i];
        }
    }

    @Override
    public Message getMessage() {
        String s = new String(this.message);
        int pos;
        if ((pos = s.indexOf('\n')) >= 0) {
            return new AdminMessage(new String(s.substring(0, pos)));
        }
        return null;
    }
}
