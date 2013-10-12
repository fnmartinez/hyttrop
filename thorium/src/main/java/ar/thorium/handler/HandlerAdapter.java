package ar.thorium.handler;

import java.nio.channels.SelectionKey;
import java.util.concurrent.Callable;

public interface HandlerAdapter<T extends EventHandler> extends
		Callable<HandlerAdapter<T>> {

	void prepareToRun(SelectionKey key);

	/**
	 * Called in order to collect the channels and keys that this
	 * adapter has.
	 * @return if this adapter is still usable
	 */
	boolean isDead();

	/**
	 * Called when registering, but before the handler is active
	 */
	void registering();

	/**
	 *  Called when the handler is registered, but before the first message
	 */
	void registered();

	/**
	 * Called when unregistration has been requested, but while the
	 * handler is still active and able to interact with the framework.
	 * Extension Point: This implementation simply calls through to
	 * the client handler, which may or may not be running. Either
	 * the client code must take steps to protect its internal state,
	 * or logic could be added here to wait until the handler finishes.
	 */
	void unregistering();

	/**
	 * Called when the handler has been unregistered and is no longer active.
	 * If unregistering() waits for the handler to finish, then this
	 * one should be safe. If not, then this function has the same
	 * concurrency concerns as does unregistering().
	 */
	void unregistered();

	/**
	 * Called either when an unhandled exception occurred or when a connection
	 * is finished, in order to dispose the resources that this handler has.
	 */
	void die();

	/**
	 * Sets the EventHandler that this adapter adapts.
	 * @param handler
	 */
	void setHandler(T handler);

	/**
	 * Called when the dispatcher is attending the status change issued by 
	 * this adapter. This operation ought to change the key interest operations
	 * that has issued the status change. The handle that is given by parameter
	 * is the same that was used to issue the status change. Is given as an 
	 * optional way to identify the key in case of multiples key per adapter.
	 * @param handle representing the key that issued the status change in the
	 * adapter. Might be null if such was given to the dispatcher when enqueing
	 * the status change.
	 */
	void confirmSelection(Object handle);

}