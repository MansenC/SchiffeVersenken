package de.gsh.mc.server;

public class Packet09GameEnd implements Packet
{
	private static final long serialVersionUID = 8354444134304384054L;
	
	private String name;
	
	public Packet09GameEnd() {}
	
	public Packet09GameEnd(String name)
	{
		this.name = name;
	}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().onGameEnd(name);
	}
}