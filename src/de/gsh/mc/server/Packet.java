package de.gsh.mc.server;

import java.io.Serializable;

public interface Packet extends Serializable
{
	public void handle(NetworkHandle network) throws Exception;
}