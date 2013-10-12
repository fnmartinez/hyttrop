package ar.thorium.utils;

import java.nio.ByteBuffer;

public interface BufferFactory {

	ByteBuffer newBuffer();
	void returnBuffer(ByteBuffer buffer);
}
