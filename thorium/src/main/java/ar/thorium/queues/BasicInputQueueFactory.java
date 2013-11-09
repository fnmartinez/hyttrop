package ar.thorium.queues;

import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.queues.implementations.BasicInputQueue;
import ar.thorium.utils.BufferFactory;
import org.apache.log4j.Logger;

public class BasicInputQueueFactory extends InputQueueFactory {

    private static Logger logger = Logger.getLogger(BasicInputQueueFactory.class);

    private BufferFactory bufferFactory;
    private SimpleMessageValidator validator;

    BasicInputQueueFactory(BufferFactory bufferFactory, SimpleMessageValidator validator) {
        this.bufferFactory = bufferFactory;
        this.validator = validator;
    }

	@Override
	public InputQueue newInputQueue() throws QueueBuildingException {
        SimpleMessageValidator newValidator;

        try {
            newValidator = (SimpleMessageValidator)Class.forName(validator.getClass().getName()).newInstance();
        } catch (InstantiationException e1) {
            logger.error("An error occurred while creating an instance.", e1);
            throw new QueueBuildingException();
        } catch(IllegalAccessException e2){
            logger.error("An error occurred while creating an instance.", e2);
            throw new QueueBuildingException();
        } catch(ClassNotFoundException e3){
            logger.error("An error occurred while creating an instance.", e3);
            throw new QueueBuildingException();
        }

        logger.info("New input queue created.");
		return new BasicInputQueue(bufferFactory, newValidator);
	}

}
