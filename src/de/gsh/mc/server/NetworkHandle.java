package de.gsh.mc.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.state.Network;

public abstract class NetworkHandle implements Runnable, Closeable, Serializable
{
	private static final long serialVersionUID = 7540764679229950447L;
	
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected Socket socket;
	
	protected boolean connected;
	protected boolean connectionLost;
	
	protected final Thread theThread;
	protected final String addr;
	protected final int port;
	private final Network menuInstance;
	private boolean running = false;
	
	public NetworkHandle(Network menu, String address, int portNumber)
	{
		menuInstance = menu;
		addr = address;
		port = portNumber;
		running = true;
		
		theThread = new Thread(this);
		theThread.setName("IOThread-NetworkExecutor");
		theThread.setPriority(10);
		theThread.setDaemon(true);
		theThread.start();
	}
	
	public final String getIP()
	{
		return addr + ":" + port;
	}
	
	protected abstract Socket openSocket() throws IOException;
	
	public abstract void disconnect();
	
	@Override
	public final void run()
	{
		try
		{
			socket = openSocket();
			connect();
		}
		catch (IOException ex)
		{
			if (ex instanceof ConnectException)
			{
				menuInstance.display("network.noServer");
				return;
			}
			
			if (ex instanceof UnknownHostException)
			{
				menuInstance.display("network.noSuchHost");
				return;
			}
			
			ex.printStackTrace();
		}
		
		while (running)
		{
			try
			{
				Packet packet = (Packet) in.readObject();
				
				if (packet instanceof Packet01Disconnect)
				{
					disconnect();
					connectionLost = true;
					connected = false;
					
					break;
				}
				
				packet.handle(this);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				break;
			}
		}
	}
	
	protected final void connect() throws IOException
	{
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		connected = true;
		
		out.flush();
		out.writeObject(new Packet02Connect(SchiffeVersenken.getInstance().getUserName()));
		out.flush();
	}
	
	public final void sendPacket(Packet packet)
	{
		if (out == null)
			return;
		
		try
		{
			out.writeObject(packet);
			out.flush();
		}
		catch (IOException e)
		{
			if (e instanceof SocketException)
				return;
			
			e.printStackTrace();
		}
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public boolean connectionLost()
	{
		return connectionLost;
	}
	
	@Override
	public void close() throws IOException
	{
		running = false;
		
		if (out != null)
		{
			out.flush();
			out.close();
			in.close();
			socket.close();
		}
	}
	
	public Network getMenu()
	{
		return menuInstance;
	}
}