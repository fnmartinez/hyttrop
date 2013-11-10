package ar.thorium.queues.implementations;

import ar.thorium.queues.InputQueue;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.Message;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public class BasicInputQueue implements InputQueue {
    private static Logger logger = Logger.getLogger(BasicInputQueue.class);

    private final BufferFactory bufferFactory;
    private int bytesRead;
    private SimpleMessageValidator validator;
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
        ByteBuffer buffer = bufferFactory.newBuffer();
        read = channel.read(buffer);
        if (logger.isDebugEnabled()) logger.debug(read + " bytes read from channel.");
        if (read > 0) {
            fillRead += read;
            bytesRead += read;
            validator.putInput(Arrays.copyOfRange(buffer.array(), 0, read));
        }
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
        return message;
    }
}
