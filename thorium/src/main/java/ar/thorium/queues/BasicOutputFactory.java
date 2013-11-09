package ar.thorium.queues;

import ar.thorium.queues.implementations.BasicOutputQueue;
import ar.thorium.utils.BufferFactory;
import org.apache.log4j.Logger;

public class BasicOutputFactory extends OutputQueueFactory {

	private BufferFactory bufferFactory;

    private static Logger logger = Logger.getLogger(BasicOutputFactory.class);


    public BasicOutputFactory(BufferFactory bufferFactory) {
		this.bufferFactory = bufferFactory;
	}

	@Override
	public OutputQueue newOutputQueue() {
        logger.info("New output queue created.");
		return new BasicOutputQueue(bufferFactory);
	}

}
