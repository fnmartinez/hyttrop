package ar.thorium.queues;

public abstract class OutputQueueFactory {

	public static OutputQueueFactory newInstance() {
		return new BasicOutputFactory();
	}
	
	public abstract OutputQueue newOutputQueue();
}
