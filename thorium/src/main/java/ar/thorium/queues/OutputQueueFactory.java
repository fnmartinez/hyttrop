package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;
import ar.edu.itba.it.pdc.jabxy.network.utils.SimpleBufferFactory;

public abstract class OutputQueueFactory {

	private static BufferFactory defaultBufferFactory = new SimpleBufferFactory(1024);
	
	public static OutputQueueFactory newInstance() {
		return new BasicOutputFactory(defaultBufferFactory);
	}
	
	public abstract OutputQueue newOutputQueue();
}
