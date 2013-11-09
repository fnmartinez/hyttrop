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
    private int bytesRead;
    private SimpleMessageValidator validator;
    private static Logger logger = Logger.getLogger(BasicInputQueue.class);
    private boolean closed;

    public BasicInputQueue(BufferFactory bufferFactory, SimpleMessageValidator validator) {
		this.bufferFactory = bufferFactory;
        this.bytesRead = 0;
        this.validator = validator;
        this.closed = false;
	}

	public synchronized int fillFrom(ByteChannel channel) throws IOException {
        int fillRead = 0;
        int read;
        do {
            ByteBuffer buffer = bufferFactory.newBuffer();
            logger.debug("New buffer created.");
            read = channel.read(buffer);
            logger.debug("Buffer was filled from the channel with " + read + ".");
            if (read > 0) {
                fillRead += read;
                bytesRead += read;
                validator.putInput(buffer.array());
            }
        } while (read > 0);
        if (read == -1) {
            closed = true;
        }
        return fillRead;
	}

	// -- not needed by framework

	public synchronized boolean isEmpty() {
		return size() == 0;
	}

    public int size() {
        return bytesRead;
    }

    public boolean isClosed() {
        return closed;
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
