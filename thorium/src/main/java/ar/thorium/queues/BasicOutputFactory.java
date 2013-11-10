package ar.thorium.queues;

import ar.thorium.queues.implementations.BasicOutputQueue;
import ar.thorium.utils.BufferFactory;
import org.apache.log4j.Logger;

public class BasicOutputFactory extends OutputQueueFactory {

    private static Logger logger = Logger.getLogger(BasicOutputFactory.class);

	@Override
	public OutputQueue newOutputQueue() {
		return new BasicOutputQueue();
	}

}
