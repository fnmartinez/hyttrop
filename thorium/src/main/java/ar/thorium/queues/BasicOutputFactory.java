package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.queues.implementations.BasicOutputQueue;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;

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
