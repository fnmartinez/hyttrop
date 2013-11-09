package ar.thorium.queues;

import ar.thorium.utils.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface InputQueue {

	int fillFrom(ByteChannel channel) throws IOException;
	boolean isEmpty();
    int size();
    boolean isClosed();
    Message getMessage();
}
