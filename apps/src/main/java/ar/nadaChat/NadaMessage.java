package ar.nadaChat;

import ar.thorium.utils.Message;

public class NadaMessage implements Message {

    private String message;

    public NadaMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
