package ar.thorium.utils;

import ar.edu.itba.it.pdc.jabxy.network.queues.InputQueue;
import ar.edu.itba.it.pdc.jabxy.network.queues.OutputQueue;

public interface ChannelFacade {
	InputQueue inputQueue();
	OutputQueue outputQueue();
	void enableWriting();
	void enableReading();
}
