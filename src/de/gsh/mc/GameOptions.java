package de.gsh.mc;

import java.io.File;
import java.util.Locale;

public class GameOptions
{
	private static byte[] ships;
	private static int maxTries;
	private static int volume;
	private static float scoreModifier;
	
	private GameOptions() {}
	
	public static void loadOptions()
	{
		System.out.println("Loading options...");
		
		if (!new File(".", "options.opt").exists())
		{
			loadDefaults();
			System.out.println("File not created! Loading default options.");
			return;
		}
		
		Config c = new Config(new File(".", "options.opt"));
		
		if (c.get("ships") == null)
		{
			loadDefaults();
			System.out.println("Options file was invalid!");
			return;
		}
		
		String[] ln = c.get("ships").split(";");
		ships = new byte[] { Byte.parseByte(ln[0]), Byte.parseByte(ln[1]), Byte.parseByte(ln[2]),
				Byte.parseByte(ln[3]) };
		maxTries = Integer.parseInt(c.get("maxTries"));
		volume = Integer.parseInt(c.get("volume"));
		scoreModifier = Float.parseFloat(c.get("scoreModifier"));
		System.out.println("Options applied!");
	}
	
	public static void saveOptions()
	{
		Config c = new Config(new File(".", "options.opt"));
		
		c.set("ships", ships[0] + ";" + ships[1] + ";" + ships[2] + ";" + ships[3]);
		c.set("maxTries", maxTries);
		c.set("volume", volume);
		c.set("scoreModifier", scoreModifier);
		
		c.set("lang", I18n.getLocale() == Locale.GERMAN ? "de" : "en");
		
		c.save(new File(".", "options.opt"));
		System.out.println("Options saved to file!");
	}
	
	public static byte[] getAmountShips()
	{
		return new byte[] { ships[0], ships[1], ships[2], ships[3] };
	}
	
	public static void setShips(byte[] shipas)
	{
		ships = shipas;
	}
	
	public static int getMaxTries()
	{
		return maxTries;
	}
	
	public static void setMaxTries(int max)
	{
		maxTries = max;
	}
	
	public static void loadDefaults()
	{
		ships = new byte[] { 4, 3, 2, 1 };
		maxTries = 50;
		volume = 75;
		scoreModifier = 1;
	}
	
	// -80 bis 6.0206
	public static float getSoundVolume()
	{
		return -80 + volume * 0.860206F;
	}
	
	public static int getRVolume()
	{
		return (int) volume;
	}
	
	public static void setSoundVolume(int percent)
	{
		volume = percent;
	}
	
	public static float getRekt()
	{
		return scoreModifier;
	}
	
	public static void setRekt(int rek)
	{
		scoreModifier = ((float) rek) / 10;
	}
}