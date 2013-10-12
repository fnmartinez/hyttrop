package ar.thorium.queues.implementations;

import ar.edu.itba.it.pdc.jabxy.network.queues.OutputQueue;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;
import ar.edu.itba.it.pdc.jabxy.network.utils.ChannelFacade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Deque;
import java.util.LinkedList;

public class BasicOutputQueue implements OutputQueue {
	private final BufferFactory bufferFactory;
	private final Deque<ByteBuffer> queue;
	private ChannelFacade facade;
	private ByteBuffer active = null;

	public BasicOutputQueue(BufferFactory bufferFactory) {
		this.bufferFactory = bufferFactory;
		queue = new LinkedList<ByteBuffer>();
	}

	public synchronized boolean isEmpty() {
		return (active == null) && (queue.size() == 0);
	}

	public synchronized int drainTo(ByteChannel channel) throws IOException {
		int bytesWritten = 0;

		while (true) {
			if (active == null) {
				if (queue.size() == 0)
					break;

				active = queue.removeFirst();
				active.flip();
			}

			int rc = channel.write(active);
			bytesWritten += rc;

			if (!active.hasRemaining()) {
				bufferFactory.returnBuffer(active);
				active = null;
			}

			if (rc == 0)
				break;
		}

		return bytesWritten;
	}

	// -- not needed by framework

	public synchronized boolean enqueue(ByteBuffer byteBuffer) {
		if (byteBuffer.remaining() == 0) {
			return false;
		}

		if (queue.size() > 0) {
			ByteBuffer tail = queue.getLast();

			if (tail.hasRemaining()) {
				topUpBuffer(tail, byteBuffer);
			}
		}

		while (byteBuffer.hasRemaining()) {
			ByteBuffer newBuf = bufferFactory.newBuffer();

			topUpBuffer(newBuf, byteBuffer);

			queue.addLast(newBuf);
		}

		if (facade != null) {
			facade.enableWriting();
		}

		return true;
	}

	private void topUpBuffer(ByteBuffer dest, ByteBuffer src) {
		if (src.remaining() <= dest.remaining()) {
			dest.put(src);
		} else {
			// TODO: make this more efficient with buffer slice?
			while (dest.hasRemaining()) {
				dest.put(src.get());
			}
		}
	}

	@Override
	public void setChannelFacade(ChannelFacade channelFacade) {
		this.facade = channelFacade;
	}
}
