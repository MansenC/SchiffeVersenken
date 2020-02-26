package de.gsh.mc.server;

public class Packet02Connect implements Packet
{
	private static final long serialVersionUID = -4205616229950642510L;
	
	private String userName;
	
	public Packet02Connect() {}
	
	public Packet02Connect(String name)
	{
		userName = name;
	}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().onConnect(userName);
	}
}