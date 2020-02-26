package de.gsh.mc.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.gsh.mc.state.Network;

public class Server extends NetworkHandle
{
	private static final long serialVersionUID = 3216277912721823423L;
	
	private ServerSocket server;
	
	public Server(Network net, String addr, int port)
	{
		super(net, addr, port);
	}
	
	@Override
	protected Socket openSocket() throws IOException
	{
		server = new ServerSocket(port, 50, InetAddress.getAllByName(addr)[0]);
		return server.accept();
	}
	
	public void disconnect()
	{
		sendPacket(new Packet01Disconnect());
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
		server.close();
	}
}