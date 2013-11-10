package ar.thorium.queues.implementations;

import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class BasicOutputQueue implements OutputQueue {
    private static Logger logger = Logger.getLogger(BasicOutputQueue.class);
    private final float QUEUE_GROWTH_FACTOR = 1.0f;
	private byte[] queue;
    private int size;
	private ChannelFacade facade;
	private boolean close = false;

	public BasicOutputQueue() {
		queue = new byte[0];
        size = 0;
	}

	public synchronized boolean isEmpty() {
		return (size == 0);
	}
	
	public synchronized int drainTo(ByteChannel channel) throws IOException {
        if (logger.isTraceEnabled()) logger.trace("Draining output in channel " + channel);

        ByteBuffer bf = ByteBuffer.wrap(queue);
		int bytesWritten = channel.write(bf);

        if (logger.isTraceEnabled()) logger.trace(bytesWritten + " bytes written to channel " + channel);

        if (bytesWritten > 0) {
            resizeQueue(size - bytesWritten);
            size = queue.length;
        } else if (bytesWritten == -1) {
            close = true;
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
        if (logger.isDebugEnabled()) logger.debug("Enqueing " + bytes.length + " bytes");
        if (logger.isTraceEnabled()) logger.trace(new String(bytes));

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
        if (logger.isDebugEnabled()) logger.debug("Closing queue");
		return close;
	}
	
	@Override
	public void setChannelFacade(ChannelFacade facede) {
		this.facade = facede;
	}

    public byte[] getQueue(){
        return this.queue;
    }
}
