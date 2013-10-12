package ar.thorium.queues;

import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.BufferFactory;

public class ValidatedInputQueueFactory extends InputQueueFactory {

	private MessageValidator validator;
	private BufferFactory bufferFactory;
	
	public ValidatedInputQueueFactory(MessageValidator validator,
			BufferFactory bufferFactory) {
		this.bufferFactory = bufferFactory;
		this.validator = validator;
	}

	@Override
	public InputQueue newInputQueue() throws QueueBuildingException {
		
		MessageValidator newValidator = null;
		
		try {
			newValidator = (MessageValidator)Class.forName(validator.getClass().getName()).newInstance();
		} catch (InstantiationException e1) {
			//TODO: mostrar el error correspondiente. LOGGER!!
			e1.printStackTrace();
		}
        catch(IllegalAccessException e2){
            e2.printStackTrace();
        }
        catch(ClassNotFoundException e3){
            e3.printStackTrace();
        }

		throw new QueueBuildingException();
	}

}
