package ar.edu.itba.pdc.administration;

import ar.thorium.utils.Message;

public class AdminMessage implements Message {
    private String message;

    public AdminMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
