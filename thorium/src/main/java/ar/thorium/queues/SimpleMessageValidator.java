package ar.thorium.queues;

import java.nio.ByteBuffer;

public interface SimpleMessageValidator extends MessageValidator {

    void putInput(ByteBuffer byteBuffer);
}
