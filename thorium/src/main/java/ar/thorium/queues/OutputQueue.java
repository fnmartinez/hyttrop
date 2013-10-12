package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.utils.ChannelFacade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface OutputQueue {
	
	boolean isEmpty();
	int drainTo(ByteChannel channel) throws IOException;
	void setChannelFacade(ChannelFacade channelFacade);

	boolean enqueue(ByteBuffer byteBuffer);
	
}
