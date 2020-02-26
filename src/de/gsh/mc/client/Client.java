package de.gsh.mc.client;

import java.io.IOException;
import java.net.Socket;

import de.gsh.mc.server.NetworkHandle;
import de.gsh.mc.server.Packet01Disconnect;
import de.gsh.mc.state.Network;

public class Client extends NetworkHandle
{
	private static final long serialVersionUID = 5255099634724791524L;

	public Client(Network net, String addr, int port)
	{
		super(net, addr, port);
	}
	
	@Override
	protected Socket openSocket() throws IOException
	{
		return new Socket(addr, port);
	}
	
	@Override
	public void disconnect()
	{
		sendPacket(new Packet01Disconnect());
	}
}