package ar.thorium.utils;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

public class SimpleBufferFactory implements BufferFactory {

	private int capacity;
    private static Logger logger = Logger.getLogger(SimpleBufferFactory.class);


    public SimpleBufferFactory(int capacity) {
		this.capacity = capacity;
	}

	public ByteBuffer newBuffer() {
        logger.info("New simple buffer created.");
		return (ByteBuffer.allocate(capacity));
	}

	public void returnBuffer(ByteBuffer buffer) {

	}

}
