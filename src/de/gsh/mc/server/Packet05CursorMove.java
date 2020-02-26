package de.gsh.mc.server;

public class Packet05CursorMove implements Packet
{
	private static final long serialVersionUID = -364084551898020397L;
	
	private int newX, newY;
	
	public Packet05CursorMove() {}
	
	public Packet05CursorMove(int x, int y)
	{
		newX = x;
		newY = y;
	}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().drawCursor(newX, newY);
	}
}