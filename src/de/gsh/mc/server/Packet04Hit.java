package de.gsh.mc.server;

public class Packet04Hit implements Packet
{
	private static final long serialVersionUID = 9134264120927149529L;
	
	private int tileX, tileY;
	
	public Packet04Hit() {}
	
	public Packet04Hit(int x, int y)
	{
		tileX = x;
		tileY = y;
	}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().onHit(tileX, tileY);
	}
}