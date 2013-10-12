package ar.thorium.queues;

import org.xml.sax.ContentHandler;

public interface XMLValidator extends ContentHandler, MessageValidator {

	int isValidMessage();
}
