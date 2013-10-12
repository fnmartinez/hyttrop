package ar.thorium.dispatcher;

import java.nio.channels.Selector;

public interface SelectorGuard {
	
	void setSelector(Selector selector);
	
	Selector getSelector();

	void selectorBarrier();
	
	void acquireSelector();
	
	void releaseSelector();
}
