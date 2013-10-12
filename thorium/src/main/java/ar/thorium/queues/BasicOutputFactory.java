package ar.thorium.queues;

import ar.thorium.queues.implementations.BasicOutputQueue;
import ar.thorium.utils.BufferFactory;

public class BasicOutputFactory extends OutputQueueFactory {

	private BufferFactory bufferFactory;
	
	public BasicOutputFactory(BufferFactory bufferFactory) {
		this.bufferFactory = bufferFactory;
	}

	@Override
	public OutputQueue newOutputQueue() {
		return new BasicOutputQueue(bufferFactory);
	}

}
