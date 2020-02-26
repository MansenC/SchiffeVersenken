package de.gsh.mc.server;

public class Packet06StatsChange implements Packet
{
	private static final long serialVersionUID = -6781868727838108506L;
	
	private int[] points, clicks;
	
	public Packet06StatsChange() {}
	
	public Packet06StatsChange(int[] points, int[] clicks)
	{
		this.points = points;
		this.clicks = clicks;
	}
	
	@Override
	public void handle(NetworkHandle network) throws Exception
	{
		network.getMenu().onStatsChange(points, clicks);
	}
}