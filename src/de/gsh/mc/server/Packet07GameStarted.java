package de.gsh.mc.server;

public class Packet07GameStarted implements Packet
{
	private static final long serialVersionUID = -6187417695058387867L;
	
	public Packet07GameStarted() {}

	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().onGameStart();
	}
}