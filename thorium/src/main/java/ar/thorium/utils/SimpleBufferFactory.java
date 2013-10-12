package ar.thorium.utils;

import java.nio.ByteBuffer;

public class SimpleBufferFactory implements BufferFactory {

	private int capacity;

	public SimpleBufferFactory(int capacity) {
		this.capacity = capacity;
	}

	public ByteBuffer newBuffer() {
		return (ByteBuffer.allocate(capacity));
	}

	public void returnBuffer(ByteBuffer buffer) {

	}

}
