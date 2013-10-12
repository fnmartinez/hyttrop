package ar.thorium.queues;

import ar.edu.itba.it.pdc.jabxy.network.queues.exceptions.QueueBuildingException;
import ar.edu.itba.it.pdc.jabxy.network.queues.implementations.XMLInputQueue;
import ar.edu.itba.it.pdc.jabxy.network.utils.BufferFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

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
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e1) {
			//TODO: mostrar el error correspondiente. LOGGER!!
			e1.printStackTrace();
		}
		
		if (XMLValidator.class.isAssignableFrom(newValidator.getClass())) {
			try {
				return new XMLInputQueue(bufferFactory, ((XMLValidator)newValidator));
			} catch (SAXException e) {
				throw new QueueBuildingException(e);
			} catch (ParserConfigurationException e) {
				throw new QueueBuildingException(e);
			}
		}
		
		throw new QueueBuildingException();
	}

}
