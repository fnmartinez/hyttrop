package ar.thorium.queues;

import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.queues.implementations.BasicInputQueue;
import ar.thorium.utils.BufferFactory;

public class BasicInputQueueFactory extends InputQueueFactory {

	private BufferFactory bufferFactory;
    private SimpleMessageValidator validator;
	
	BasicInputQueueFactory(BufferFactory bufferFactory, SimpleMessageValidator validator) {
		this.bufferFactory = bufferFactory;
        this.validator = validator;
	}

	@Override
	public InputQueue newInputQueue() throws QueueBuildingException {
        SimpleMessageValidator newValidator = null;

        try {
            newValidator = (SimpleMessageValidator)Class.forName(validator.getClass().getName()).newInstance();
        } catch (InstantiationException e1) {
            //TODO: mostrar el error correspondiente. LOGGER!!
            throw new QueueBuildingException();
        } catch(IllegalAccessException e2){
            throw new QueueBuildingException();
        } catch(ClassNotFoundException e3){
            throw new QueueBuildingException();
        }

		return new BasicInputQueue(bufferFactory, newValidator);
	}

}
