package de.gsh.mc.server;

public class Packet08DoAnimation implements Packet
{
	private static final long serialVersionUID = 4482834085884379714L;
	
	public Packet08DoAnimation() {}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().doAnimation();
	}
}