package ar.thorium.queues;

import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.SimpleBufferFactory;

public abstract class OutputQueueFactory {

	private static BufferFactory defaultBufferFactory = new SimpleBufferFactory(1024);
	
	public static OutputQueueFactory newInstance() {
		return new BasicOutputFactory(defaultBufferFactory);
	}
	
	public abstract OutputQueue newOutputQueue();
}
