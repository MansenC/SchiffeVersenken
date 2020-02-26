package de.gsh.mc.server;

import de.gsh.mc.SchiffeVersenken;

public class Packet01Disconnect implements Packet
{
	private static final long serialVersionUID = -4309849384625414391L;
	
	@Override public void handle(NetworkHandle network)
	{
		SchiffeVersenken.getInstance().setCursor(SchiffeVersenken.DEFAULT_CURSOR);
	}
}