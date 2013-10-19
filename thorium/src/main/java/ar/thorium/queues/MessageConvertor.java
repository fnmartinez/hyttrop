package ar.thorium.queues;

import java.nio.ByteBuffer;

public interface MessageValidator {

	int isValidMessage(ByteBuffer message);
}
