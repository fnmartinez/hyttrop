package ar.thorium.queues.implementations;

import ar.thorium.queues.InputQueue;
import ar.thorium.queues.SimpleMessageValidator;
import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class BasicInputQueue implements InputQueue {
	private final BufferFactory bufferFactory;
    private int bytesRead;
    private SimpleMessageValidator validator;


	public BasicInputQueue(BufferFactory bufferFactory, SimpleMessageValidator validator) {
		this.bufferFactory = bufferFactory;
        this.bytesRead = 0;
        this.validator = validator;
	}

	public synchronized int fillFrom(ByteChannel channel) throws IOException {
        int fillRead = 0;
        int read;
        do {
            ByteBuffer buffer = bufferFactory.newBuffer();
            read = channel.read(buffer);
            if (read > 0) {
                fillRead += read;
                bytesRead += read;
                validator.putInput(buffer.array());
            }
        } while (read > 0);
		return fillRead;
	}

	// -- not needed by framework

	public synchronized boolean isEmpty() {
		return size() == 0;
	}

    public int size() {
        return this.bytesRead;
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
