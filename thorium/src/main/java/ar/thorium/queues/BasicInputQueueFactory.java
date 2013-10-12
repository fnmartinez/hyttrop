package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.queues.implementations.BasicInputQueue;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;

public class BasicInputQueueFactory extends InputQueueFactory {

	private BufferFactory bufferFactory;
	
	BasicInputQueueFactory(BufferFactory bufferFactory) {
		this.bufferFactory = bufferFactory;
	}
	@Override
	public InputQueue newInputQueue() {
		return new BasicInputQueue(bufferFactory);
	}

}
