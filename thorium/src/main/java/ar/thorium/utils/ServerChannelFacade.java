package ar.thorium.utils;


public interface ServerChannelFacade extends ChannelFacade {
	int getInterestOps();
	void modifyInterestOps(int opsToSet, int opsToReset);
}
