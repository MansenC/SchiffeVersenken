package de.gsh.mc.server;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.client.FlippedGameField;
import de.gsh.mc.state.Network;

public class Packet03GameInitialized implements Packet // One way packet from server to client!
{
	private static final long serialVersionUID = -7379329014797748588L;
	
	private byte[][][] gameTensor;
	private byte starting;
	private int maxClicks;
	private byte[] hitShips;
	
	public Packet03GameInitialized() {}
	
	public Packet03GameInitialized(byte[][][] syncGameFields, byte starting, int maxClicks, byte[] hitShips)
	{
		gameTensor = syncGameFields;
		this.starting = (byte) (1 - starting);
		this.maxClicks = maxClicks;
		this.hitShips = hitShips;
	}

	@Override
	public void handle(NetworkHandle network) throws Exception 
	{
		if (network.getMenu().isServer())
			throw new Exception("Packet sent to server! " + toString());
		
		Network net = network.getMenu();
		
		net.theGameField = new FlippedGameField(gameTensor, starting, hitShips);
		net.theGameField.initialize(net.enemy);
		net.maxClicks = maxClicks;
		SchiffeVersenken.getInstance().setCursor(SchiffeVersenken.INVISIBLE_CURSOR);
	}
}