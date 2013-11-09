package ar.thorium.queues;

import ar.thorium.utils.ChannelFacade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface OutputQueue {
	
	boolean isEmpty();
	int drainTo(ByteChannel channel) throws IOException;
	void setChannelFacade(ChannelFacade channelFacade);
	boolean enqueue(byte[] bytes);
	public boolean isClosed();
	
}
