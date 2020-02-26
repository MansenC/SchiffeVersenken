package de.gsh.mc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayerProfile
{
	public static final Map<String, PlayerProfile> EVER_LOADED = new HashMap<String, PlayerProfile>();
	
	public final String name;
	
	public int topScore; // Done
	public int lowScore; // Done
	public int games;
	public int wins; // Done
	public int looses; // Done
	public float clicksToMax;
	public long minTimeUsed;
	
	public PlayerProfile(String name)
	{
		this.name = name;
		
		Config c = new Config(new File("profile/", name + ".prof"));
		topScore = c.getSaveInt("topScore", Integer.MIN_VALUE);
		lowScore = c.getSaveInt("lowScore", Integer.MAX_VALUE);
		games = c.getSaveInt("games", 0);
		wins = c.getSaveInt("wins", 0);
		looses = c.getSaveInt("losses", 0);
		clicksToMax = c.getSaveFloat("clicksToMax", Float.MAX_VALUE);
		minTimeUsed = c.getSaveLong("minTimeUsed", Long.MAX_VALUE);
		
		EVER_LOADED.put(name, this);
	}
	
	public void restoreDefaults()
	{
		topScore = Integer.MIN_VALUE;
		lowScore = Integer.MAX_VALUE;
		games = 0;
		wins = 0;
		looses = 0;
		clicksToMax = Float.MAX_VALUE;
		minTimeUsed = Long.MAX_VALUE;
	}
	
	public void save()
	{
		Config c = new Config(new File("./profile/", name + ".prof"));
		
		c.set("topScore", topScore);
		c.set("lowScore", lowScore);
		c.set("games", games);
		c.set("wins", wins);
		c.set("looses", looses);
		c.set("clicksToMax", clicksToMax);
		c.set("minTimeUsed", minTimeUsed);
		
		c.save(new File("./profile/", name + ".prof"));
	}
}