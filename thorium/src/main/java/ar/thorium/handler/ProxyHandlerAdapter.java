package ar.thorium.handler;

import ar.edu.itba.it.pdc.jabxy.network.dispatcher.Dispatcher;
import ar.edu.itba.it.pdc.jabxy.network.dispatcher.implementation.NioProxyDispatcher;
import ar.edu.itba.it.pdc.jabxy.network.queues.InputQueue;
import ar.edu.itba.it.pdc.jabxy.network.queues.OutputQueue;
import ar.edu.itba.it.pdc.jabxy.network.utils.ProxyChannelFacade;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ProxyHandlerAdapter<T extends ProxyEventHandler> extends
		AbstractHandlerAdapter<T, ProxyChannelFacade, ProxyHandlerAdapter<T>>
		implements ProxyChannelFacade {

	protected SelectableChannel channel1 = null;
	protected SelectionKey inputKey = null;
	protected int outputInterestOps = 0;
	protected int outputReadyOps = 0;
	protected SelectableChannel channel2 = null;
	protected SelectionKey outputKey = null;
	protected int inputInterestOps = 0;
	protected int inputReadyOps = 0;
	protected SelectableChannel outputChannel = null;
	private boolean shuttingDown = false;
	private ProxyHandlerAdapter<T> sibling;

	public ProxyHandlerAdapter(
			Dispatcher<T, ProxyChannelFacade, ProxyHandlerAdapter<T>> dispatcher,
			InputQueue inputQueue, OutputQueue outputQueue, T eventHandler)
			throws SAXException, ParserConfigurationException {
		super(dispatcher, inputQueue, outputQueue, eventHandler);
		sibling = null;
	}

	private ProxyHandlerAdapter(
			Dispatcher<T, ProxyChannelFacade, ProxyHandlerAdapter<T>> dispatcher,
			InputQueue inputQueue, OutputQueue outputQueue, T eventHandler,
			SelectableChannel inputChannel, SelectableChannel outputChannel,
			ProxyHandlerAdapter<T> sibling) {
		super(dispatcher, inputQueue, outputQueue, eventHandler);
		this.channel1 = inputChannel;
		this.channel2 = outputChannel;
		this.sibling = sibling;
	}

	@Override
	public HandlerAdapter<T> call() throws IOException {
		if ((outputReadyOps & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
			this.eventHandler.handleConnection(this);
		}
		try {
			drainOutput();
			fillInput();
			
			ByteBuffer message;
			
			if((inputReadyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
				while((message = eventHandler.nextMessage(this)) != null) {
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

	private void fillInput() throws IOException {
		if (shuttingDown) {
			return;
		}
		
		int rc = inputQueue.fillFrom((ByteChannel) channel1);
		
		if (rc == -1) {
			modifyInputInterestOps(0, SelectionKey.OP_READ);
			
			if (channel1 instanceof SocketChannel) {
				SocketChannel sc = (SocketChannel) channel1;
				
				if (sc.socket().isConnected()) {
					try {
						sc.socket().shutdownInput();
					} catch (SecurityException e) {
						
					}
				}
			}
			
			shuttingDown = true;
			eventHandler.stopping(this);
			
			this.enableWriting();
		}
	}

	private void drainOutput() throws IOException {
		if ((outputReadyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && !outputQueue.isEmpty() && outputChannel.isOpen()) {
			outputQueue.drainTo((ByteChannel) outputChannel);
		}
		
		if (outputQueue.isEmpty()) {
			modifyOutputInterestOps(0, SelectionKey.OP_WRITE);
			
			if (shuttingDown) {
				if (outputChannel.isOpen()) {
					outputChannel.close();
				}
				eventHandler.stopped(this);
			}
		}
	}

	public ProxyHandlerAdapter<T> getSibling() {
		if (this.sibling == null) {
			this.sibling = new ProxyHandlerAdapter<T>(this.dispatcher,
					this.inputQueue, this.outputQueue, (T) this.eventHandler,
					this.channel2, this.channel1, this);
		}
		return this.sibling;
	}

	@Override
	public void redirectToInput() {
		this.outputChannel = channel1;
	}

	@Override
	public void redirectToOutput() {
		this.outputChannel = channel2;
	}

	@Override
	public void connectOutput(String url, int port)
			throws ClosedChannelException {
		try {
			this.channel2 = SocketChannel.open().bind(
					new InetSocketAddress(url, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		((NioProxyDispatcher<T>) this.dispatcher).registerSibling(this);

	}

	@Override
	public void prepareToRun(SelectionKey key) {
		synchronized (stateChangeLock) {
			if (key.equals(inputKey)) {
				inputInterestOps = key.interestOps();
				inputReadyOps = key.readyOps();
			} else if (key.equals(outputKey)) {
				outputInterestOps = key.interestOps();
				outputReadyOps = key.readyOps();
			} else {
				throw new IllegalArgumentException("This is not my key");
			}
			running = true;
		}
	}

	@Override
	public void setInputKey(SelectionKey key) {
		this.inputKey = key;

	}

	@Override
	public void setOutputKey(SelectionKey key) {
		this.outputKey = key;
	}

	@Override
	public int getInputInterestOps() {
		return inputInterestOps;
	}

	@Override
	public void modifyInputInterestOps(int opsToSet, int opsToReset) {
		this.inputInterestOps = modifyInterestOps(inputInterestOps, opsToSet,
				opsToReset);
	}

	@Override
	public int getOutputInterestOps() {
		return outputInterestOps;
	}

	@Override
	public void modifyOutputInterestOps(int opsToSet, int opsToReset) {
		this.outputInterestOps = modifyInterestOps(outputInterestOps, opsToSet,
				opsToReset);
	}

	public SelectableChannel channel1() {
		return channel1;
	}

	public SelectableChannel channel2() {
		return channel2;
	}

	public SelectableChannel outputChannel() {
		return outputChannel;
	}

	@Override
	public SelectionKey outputKey() {
		return this.outputKey;
	}

	@Override
	public SelectionKey inputKey() {
		return this.inputKey;
	}

	@Override
	public void confirmSelection(Object handle) {
		SelectionKey key = (SelectionKey) handle;
		if (key == null) {
			return;
		}
		if (key.equals(inputKey) && key.isValid()) {
			key.interestOps(inputInterestOps);
			return;
		}

		if (key.equals(outputKey) && key.isValid()) {
			key.interestOps(outputInterestOps);
			return;
		}

		throw new IllegalArgumentException("This is not my key");
	}

	@Override
	public void enableWriting() {
		modifyOutputInterestOps(SelectionKey.OP_WRITE, 0);
		issueChange(outputKey);
	}

	@Override
	public void enableReading() {
		modifyInputInterestOps(SelectionKey.OP_READ, 0);
		issueChange(inputKey);
	}
}
