package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.ServerChannelFacade;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerHandlerAdapter<T extends ServerEventHandler> extends
		AbstractHandlerAdapter<T, ChannelFacade, ServerHandlerAdapter<T>>
		implements ServerChannelFacade {

	private final Object stateChangeLock = new Object();
	private boolean shuttingDown = false;
	protected SelectionKey key;
	protected int interestOps = 0;
	protected int readyOps = 0;
	protected SelectableChannel channel;

	public ServerHandlerAdapter(
			Dispatcher<T, ChannelFacade, ServerHandlerAdapter<T>> dispatcher,
			InputQueue inputQueue, OutputQueue outputQueue, T clientHandler) {
		super(dispatcher, inputQueue, outputQueue, clientHandler);
	}

	// ------------------------------------------------------------
	// Implementation of Callable<HandlerAdapter> interface

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.thorium.handler.HandlerAdapter#call()
	 */
	@Override
	public HandlerAdapter<T> call() throws IOException {
		try {
			drainOutput();
			fillInput();

			ByteBuffer message;

			// must process all buffered messages because Selector will
			// not fire again for input that's already read and buffered
			if ((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
				while ((message = eventHandler.nextMessage(this)) != null) {
					eventHandler.handleInput(message, this);
				}
			}
		} finally {
			synchronized (stateChangeLock) {
				this.running = false;
			}
		}

		return this;
	}

	// --------------------------------------------------
	// Private helper methods

	// These three methods manipulate the private copy of the selection
	// interest flags. Upon completion, this local copy will be copied
	// back to the SelectionKey as the new interest set.
	private void enableWriteSelection() {
		modifyInterestOps(SelectionKey.OP_WRITE, 0);
	}

	private void disableWriteSelection() {
		modifyInterestOps(0, SelectionKey.OP_WRITE);
	}

	private void disableReadSelection() {
		modifyInterestOps(0, SelectionKey.OP_READ);
	}

	private void enableReadSelection() {
		modifyInterestOps(SelectionKey.OP_READ, 0);
	}

	// If there is output queued, and the channel is ready to
	// accept data, send as much as it will take.
	private void drainOutput() throws IOException {
		if (((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
				&& (!outputQueue.isEmpty())) {
			outputQueue.drainTo((ByteChannel) channel);
		}

		// Write selection is turned on when output data in enqueued,
		// turn it off when the queue becomes empty.
		if (outputQueue.isEmpty()) {
			disableWriteSelection();

			if (shuttingDown) {
				channel.close();
				eventHandler.stopped(this);
			}
		}
	}

	// Attempt to fill the input queue with as much data as the channel
	// can provide right now. If end-of-stream is reached, stop read
	// selection and shutdown the input side of the channel.
	private void fillInput() throws IOException {
		if (shuttingDown)
			return;

		int rc = inputQueue.fillFrom((ByteChannel) channel);

		if (rc == -1) {
			disableReadSelection();

			if (channel instanceof SocketChannel) {
				SocketChannel sc = (SocketChannel) channel;

				if (sc.socket().isConnected()) {
					try {
						sc.socket().shutdownInput();
					} catch (SocketException e) {
						// happens sometimes, ignore
					}
				}
			}

			shuttingDown = true;
			eventHandler.stopping(this);

			// cause drainOutput to run, which will close
			// the socket if/when the output queue is empty
			enableWriteSelection();
		}
	}

	public void setKey(SelectionKey key) {
		this.key = key;
		this.channel = key.channel();
		interestOps = key.interestOps();
	}

	public SelectionKey key() {
		return this.key;
	}

	@Override
	public void prepareToRun(SelectionKey key) {
		synchronized (stateChangeLock) {
			if (key.equals(this.key)) {
				interestOps = key.interestOps();
				readyOps = key.readyOps();
				running = true;
			} else {
				throw new IllegalArgumentException("This is not my key");
			}
		}
	}

	@Override
	public int getInterestOps() {
		return interestOps;
	}

	@Override
	public void modifyInterestOps(int opsToSet, int opsToReset) {
		this.interestOps = modifyInterestOps(interestOps, opsToSet, opsToReset);
	}

	public int getReadyOps() {
		return readyOps;
	}

	@Override
	public void confirmSelection(Object handle) {
		if (key != null && key.equals(this.key) && key.isValid()) {
			key.interestOps(interestOps);
		}
	}

	@Override
	public void enableWriting() {
		// TODO Auto-generated method stub
		enableWriteSelection();
		issueChange(key);
	}

	@Override
	public void enableReading() {
		enableReadSelection();
		issueChange(key);
	}

}
