package ar.thorium.queues.implementations;

import ar.thorium.queues.InputQueue;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class BasicInputQueue implements InputQueue {
	private final BufferFactory bufferFactory;
	private final ByteBuffer emptyBuffer;
	private ByteBuffer buffer = null;
    private int bytesRead = 0;
    private SimpleMessageValidator validator;
    private static Logger logger = Logger.getLogger(BasicInputQueue.class);

    public BasicInputQueue(BufferFactory bufferFactory, SimpleMessageValidator validator) {
		this.bufferFactory = bufferFactory;
		emptyBuffer = ByteBuffer.allocate(0).asReadOnlyBuffer();
        this.validator = validator;
	}

	public synchronized int fillFrom(ByteChannel channel) throws IOException {
		if (buffer == null) {
			buffer = bufferFactory.newBuffer();
            logger.debug("New buffer created.");
		}
        int read = channel.read(buffer);
        if (read > 0) {
            bytesRead += read;
            buffer.flip();
            validator.putInput(buffer.asReadOnlyBuffer());
            buffer.clear();
            logger.debug("Buffer was filled from the channel.");
        }
		return read;
	}

	// -- not needed by framework

	public synchronized boolean isEmpty() {
		return (buffer == null) || (bytesRead == 0);
	}

	public synchronized int indexOf(byte b) {
		if (buffer == null) {
			return -1;
		}

		int pos = buffer.position();

		for (int i = 0; i < pos; i++) {
			if (b == buffer.get(i)) {
				return i;
			}
		}

		return -1;
	}

    public int size() {
        return this.buffer.remaining();
    }

	public synchronized ByteBuffer dequeueBytes(int count) {
		if ((buffer == null) || (buffer.position() == 0) || (count == 0)) {
            logger.debug("Buffer is empty while trying to deque bytes.");
			return emptyBuffer;
		}

		int size = Math.min(count, buffer.position());

		ByteBuffer result = ByteBuffer.allocate(size);

		buffer.flip();

		// TODO: Validate this
		// result.put(buffer.array(), 0, size);
		// buffer.position(size);
		// result.position(size);

		// TODO: this if() should be replaceable by the above
		if (buffer.remaining() <= result.remaining()) {
			result.put(buffer);
		} else {
			while (result.hasRemaining()) {
				result.put(buffer.get());
			}
		}

		if (buffer.remaining() == 0) {
			bufferFactory.returnBuffer(buffer);
			buffer = null;
		} else {
			buffer.compact();
		}

		result.flip();

        logger.debug("Bytes dequeued from buffer.");
		return (result);
	}

	public void discardBytes(int count) {

		dequeueBytes(count);
        logger.debug(count + " bytes discarded from buffer.");
	}

    @Override
    public Message getMessage() {
        Message message = validator.getMessage();
        if (message != null) {
            bytesRead = 0;
        }
        logger.debug("Message returned from input queue.");
        return message;
    }
}
