package ar.thorium.queues.implementations;

import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.ChannelFacade;
import com.sun.deploy.util.ArrayUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Deque;
import java.util.LinkedList;

public class BasicOutputQueue implements OutputQueue {
    private final float QUEUE_GROWTH_FACTOR = 1.1f;
	private byte[] queue;
    private int size;
	private ChannelFacade facade;
	private boolean close = false;

	public BasicOutputQueue(BufferFactory bufferFactory) {
		queue = new byte[0];
        size = 0;
	}

	public synchronized boolean isEmpty() {
		return (size == 0);
	}
	
	public synchronized int drainTo(ByteChannel channel) throws IOException {
        ByteBuffer bf = ByteBuffer.wrap(queue);
		int bytesWritten = channel.write(bf);

        if (bytesWritten > 0) {
            resizeQueue(size - bytesWritten);
            size = queue.length;
        }

        return bytesWritten;
	}

    private void resizeQueue(int newSize) {
        if (newSize <= 0) {
            queue = new byte[0];
        } else {
            byte[] newQueue = new byte[newSize];
            if (newSize < size) {
                for (int i = 0; i < newQueue.length; i++) {
                    newQueue[i] = queue[size - newSize + i];
                }
                size = newSize;
            } else if (newSize > size) {
                for (int i = 0; i < queue.length; i++) {
                    newQueue[i] = queue[i];
                }
            }
            queue = newQueue;
        }
    }

	public synchronized boolean enqueue(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        if (bytes.length + size >= queue.length) {
            resizeQueue((int)((bytes.length + queue.length) * QUEUE_GROWTH_FACTOR));
        }
        for (int i = 0; i < bytes.length; i++) {
            queue[size + i] = bytes[i];
        }

		size += bytes.length;

		if (facade != null) {
			facade.enableWriting();
		}

		return true;
	}

	public synchronized boolean isClosed(){
		return this.close;
	}
	
	@Override
	public void setChannelFacade(ChannelFacade channelFacade) {
		this.facade = channelFacade;
	}
}
