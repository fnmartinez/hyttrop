package ar.thorium.queues;

import ar.thorium.queues.MessageValidator;

import java.nio.ByteBuffer;

public interface SimpleMessageValidator extends MessageValidator {

    void putInput(ByteBuffer byteBuffer);
}
