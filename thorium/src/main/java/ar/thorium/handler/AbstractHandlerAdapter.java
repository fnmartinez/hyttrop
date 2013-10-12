package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;

import java.nio.channels.SelectionKey;

public abstract class AbstractHandlerAdapter<H extends EventHandler, F extends ChannelFacade, A extends HandlerAdapter<H>> implements HandlerAdapter<H>, ChannelFacade {

	protected final Dispatcher<H, F, A> dispatcher;
	protected final InputQueue inputQueue;
	protected final OutputQueue outputQueue;
	protected final Object stateChangeLock = new Object();
	protected H eventHandler;
	protected volatile boolean running = false;
	protected volatile boolean dead = false;

	public AbstractHandlerAdapter(Dispatcher<H, F, A> dispatcher,
			InputQueue inputQueue, OutputQueue outputQueue,
			H eventHandler) {
		this.dispatcher = dispatcher;
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.eventHandler = eventHandler;
		this.outputQueue.setChannelFacade(this);
	}

	@Override
	public abstract HandlerAdapter<H> call() throws Exception;
	
	@Override
	public abstract void prepareToRun(SelectionKey key);
	
	@Override
	public InputQueue inputQueue() {
		return this.inputQueue;
	}

	@Override
	public OutputQueue outputQueue() {
		return this.outputQueue;
	}

	@Override
	public void setHandler(H handler) {
		this.eventHandler = handler;
	}

	public H getHandler() {
		return this.eventHandler;
	}

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public void registering() {
		eventHandler.starting(this);
	}

	@Override
	public void registered() {
		eventHandler.started(this);

	}

	@Override
	public void unregistering() {
		eventHandler.stopping(this);
	}

	@Override
	public void unregistered() {
		eventHandler.stopped(this);
	}

	@Override
	public void die() {
		this.dead = true;
	}
	
	protected int modifyInterestOps(int ops, int opsToSet, int opsToReset) {
		ops = (ops | opsToSet) & (~opsToReset);
		return ops; 
	}
	
	@SuppressWarnings("unchecked")
	protected void issueChange(SelectionKey key) {
		synchronized (stateChangeLock) {
			if (!running) {
				dispatcher.enqueueStatusChange((A) this, key);
			}
		}
	}
}
