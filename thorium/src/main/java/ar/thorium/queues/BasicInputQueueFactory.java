package ar.thorium.queues;

import ar.thorium.queues.implementations.BasicInputQueue;
import ar.thorium.utils.BufferFactory;

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
