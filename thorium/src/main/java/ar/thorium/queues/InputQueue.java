package ar.thorium.queues;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface InputQueue {

	int fillFrom(ByteChannel channel) throws IOException;

	boolean isEmpty();
	int indexOf(byte b);
	ByteBuffer dequeueBytes(int count);
	void discardBytes(int count);
}
