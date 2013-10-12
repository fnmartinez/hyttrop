package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.queues.exceptions.QueueBuildingException;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;
import ar.edu.itba.it.pdc.jabxy.network.utils.SimpleBufferFactory;

public abstract class InputQueueFactory {
	
	private static BufferFactory defaultBufferFactory = new SimpleBufferFactory(1024);
	
	public static InputQueueFactory newInstance() {
		return new BasicInputQueueFactory(defaultBufferFactory);
	}
	
	public static InputQueueFactory newInstance(BufferFactory bufferFactory) {
		return new BasicInputQueueFactory(bufferFactory);
	}
	
	public static InputQueueFactory newInstance(MessageValidator validator) {
		return new ValidatedInputQueueFactory(validator, defaultBufferFactory);
	}
	
	public static InputQueueFactory newInstance(MessageValidator validator, BufferFactory bufferFactory) {
		return new ValidatedInputQueueFactory(validator, bufferFactory);
	}
	
	public abstract InputQueue newInputQueue() throws QueueBuildingException;

}
