package ar.thorium.acceptor.implementations;

import ar.thorium.acceptor.Acceptor;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandlerFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceptor implementation for a single dispatcher type.
 * 
 */
public class BasicSocketAcceptor implements Acceptor {

    private static final Logger logger = Logger.getLogger(BasicSocketAcceptor.class);
    private final Dispatcher dispatcher;
    private final EventHandlerFactory eventHandlerFactory;
    private ServerSocketChannel listenSocket;
    private InetSocketAddress bindingAddress;
    private Listener listener;
    private Thread listenerThread;
    private volatile boolean running = true;

	public BasicSocketAcceptor(String hostname, int port,
			EventHandlerFactory eventHandlerFactory, Dispatcher dispatcher) throws IOException {
		this(new InetSocketAddress(hostname, port), eventHandlerFactory, dispatcher);
	}
	
	public BasicSocketAcceptor(InetSocketAddress bindingAddress,
			EventHandlerFactory eventHandlerFactory, Dispatcher dispatcher) throws IOException{
        this.bindingAddress = bindingAddress;
		this.dispatcher = dispatcher;
		this.eventHandlerFactory = eventHandlerFactory;
		this.listenSocket = ServerSocketChannel.open();
		this.listener = new Listener();
	}

	@Override
	public synchronized Thread start() {
        try {
            this.listenSocket.bind(this.bindingAddress);
            this.listenSocket.configureBlocking(true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
		listenerThread = new Thread(listener);
		listenerThread.start();
		
		dispatcher.start();

		return listenerThread;
	}

	@Override
	public synchronized void stop() {

        logger.info("-------------- Shutting down acceptor " + this.toString() + " --------------");
        running = false;

        if (listenerThread.isAlive()) {
            listenerThread.interrupt();
        }

        try {
            listenerThread.join();
        } catch (InterruptedException ie) {
            logger.error("An error ocurred while joining the listener thread", ie);
        }

		try {
			listenSocket.close();
		} catch (IOException e) {
			logger.error("Caught an exception shutting down", e);
		}
	}

	private class Listener implements Runnable {
		public void run() {
            try {
                logger.info("-------------- Starting listener at " + listenSocket.getLocalAddress() + "--------------");
            } catch (IOException e) {
                logger.fatal("Cannot run acceptor.", e);
                throw new UnknownError();
            }
            while (running) {
				try {
					SocketChannel client = listenSocket.accept();

					if (client == null) {
                        continue;
                    }

                    logger.info("New connection from " + client.getRemoteAddress());
                    dispatcher.registerChannel(client,
							eventHandlerFactory.newHandler());

				} catch (ClosedByInterruptException e) {
					logger.fatal("ServerSocketChannel closed by interrupt: "
							+ e, e);
					return;

				} catch (ClosedChannelException e) {
					logger.fatal(
							"Exiting, serverSocketChannel is closed: " + e, e);
					return;

				} catch (Throwable t) {
					logger.fatal("Exiting, Unexpected Throwable doing accept: "
							+ t, t);

					try {
						listenSocket.close();
					} catch (Throwable e1) { /* nothing */
					}

					return;
				}
			}
		}
	}

    public InetSocketAddress getBindingAddress() {
        return this.bindingAddress;
    }

    public void setBindingAddress(InetSocketAddress bindingAddress) {
        this.bindingAddress = bindingAddress;
    }
}
