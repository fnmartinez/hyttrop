package ar.thorium.queues;

import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.BufferFactory;
import ar.thorium.utils.SimpleBufferFactory;

public abstract class InputQueueFactory {
	
	private static BufferFactory defaultBufferFactory = new SimpleBufferFactory(1024*6);
	
	public static InputQueueFactory newInstance(MessageValidator validator) {
		return new BasicInputQueueFactory(defaultBufferFactory, (SimpleMessageValidator)validator);
	}
	
	public static InputQueueFactory newInstance(SimpleMessageValidator validator, BufferFactory bufferFactory) {
		return new BasicInputQueueFactory(bufferFactory, validator);
	}
	
	public abstract InputQueue newInputQueue() throws QueueBuildingException;

}
