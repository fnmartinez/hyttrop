package ar.thorium.utils;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface ProxyChannelFacade extends ChannelFacade {
	void redirectToInput();
	int getInputInterestOps();
	void modifyInputInterestOps(int opsToSet, int opsToReset);
	void redirectToOutput();
	int getOutputInterestOps();
	void modifyOutputInterestOps(int opsToSet, int opsToReset);
	void connectOutput(String url, int port) throws ClosedChannelException;
	SelectableChannel channel1();
	SelectableChannel channel2();
	SelectableChannel outputChannel();
	SelectionKey outputKey();
	SelectionKey inputKey();
	void setOutputKey(SelectionKey key);
	void setInputKey(SelectionKey key);
}
