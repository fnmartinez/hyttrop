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

	private final Dispatcher dispatcher;
	private final EventHandlerFactory eventHandlerFactory;
	private final ServerSocketChannel listenSocket;
	private final Listener listener;
	private final List<Thread> threads = new ArrayList<Thread>();
	private static Logger logger = Logger.getLogger(BasicSocketAcceptor.class);
	private volatile boolean running = true;

	public BasicSocketAcceptor(int port,
			EventHandlerFactory eventHandlerFactory, Dispatcher dispatcher) throws IOException {
		this(new InetSocketAddress(port), eventHandlerFactory, dispatcher);
	}
	
	public BasicSocketAcceptor(InetSocketAddress listenAddress,
			EventHandlerFactory eventHandlerFactory, Dispatcher dispatcher) throws IOException{
		this.dispatcher = dispatcher;
		this.eventHandlerFactory = eventHandlerFactory;
		this.listenSocket = ServerSocketChannel.open();
		this.listenSocket.bind(listenAddress);
		this.listenSocket.configureBlocking(true);
		this.listener = new Listener();
	}

	@Override
	public synchronized Thread newThread() {
		Thread thread = new Thread(listener);
		threads.add(thread);
		thread.start();
		
		thread = dispatcher.start();
		threads.add(thread);

		return thread;
	}

	@Override
	public synchronized void shutdown() {

        logger.info("-------------- Shutting down acceptor " + this.toString() + " --------------");
        running = false;

        for(Thread t : this.threads) {
            if (t.isAlive()) {
                t.interrupt();
            }
        }

        for(Thread t : this.threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logger.error("An error occurred.", e);
            }
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

}
