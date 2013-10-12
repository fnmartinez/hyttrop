package ar.thorium.utils;

import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;

public interface ChannelFacade {
	InputQueue inputQueue();
	OutputQueue outputQueue();
	void enableWriting();
	void enableReading();
    int getInterestOps();
    void modifyInterestOps(int opsToSet, int opsToReset);
}
