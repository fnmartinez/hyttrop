package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.SelectorGuard;

import java.nio.channels.Selector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteBlockingGuard implements SelectorGuard {

	private final ReadWriteLock selectorGuard = new ReentrantReadWriteLock();
	private Selector selector;
	
	/**
	 * Called to acquire and then immediately release a write lock
	 * on the selectorGuard object. This method is only called by
	 * the selection thread and it has the effect of making that
	 * thread wait until all read locks have been released.
 	 */
	public void selectorBarrier()
	{
		selectorGuard.writeLock().lock();
		selectorGuard.writeLock().unlock();
	}

	// --------------------------------------------------------

	// Reader lock acquire/release, called by non-selector threads

	/**
	 * Grab a read lock on the selectorGuard object.  A handler thread
	 * calls this method when it wants to mutate the state of the
	 * Selector.  It must call releaserSelectorGuard when it is finished,
	 * because selection will not resume until all read locks have been
	 * released.
	 */
	public void acquireSelector()
	{
		selectorGuard.readLock().lock();
		selector.wakeup();
	}

	/**
	 * Undo a previous call to acquireSelectorGuard to indicate that
	 * the calling thread no longer needs access to the Selector object.
	 */
	public void releaseSelector()
	{
		selectorGuard.readLock().unlock();
	}


	@Override
	public void setSelector(Selector selector) {
		this.selector = selector;		
	}


	@Override
	public Selector getSelector() {
		return this.selector;
	}


}
