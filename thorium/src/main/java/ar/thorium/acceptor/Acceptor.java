package ar.thorium.acceptor;

import java.io.IOException;

/**
 * This Interface provides basic operations to accept incoming connections to
 * the reactor pattern oriented Server for the framework provided.
 */
public interface Acceptor {

	/**
	 * Spawns a new thread of the Acceptor implementing this interface for which
	 * to operate.
	 * 
	 * @return Spawned thread for the concrete class that accepts incoming
	 *         messages for the server.
	 */
	Thread start();

	/**
	 * Sends the signal to implementing class to stop incoming connections.
	 */
	void stop();
}
