package de.gsh.mc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n
{
	private static ResourceBundle currentBundle;
	private static final URLClassLoader LANG_CL;
	private static final List<LangReloadListener> LRLS = new ArrayList<LangReloadListener>();
	
	private I18n() {}
	
	public static void setLocale(Locale l) throws IllegalArgumentException
	{
		if (l != Locale.GERMAN && l != Locale.ENGLISH)
			throw new IllegalArgumentException("Unsupported Language!");
		
		currentBundle = ResourceBundle.getBundle("lang", l, LANG_CL);
		System.out.println("Loaded bundle: " + currentBundle);
		
		for (LangReloadListener lrl : LRLS)
			lrl.onLangReload();
	}
	
	public static void addLRL(LangReloadListener lrl)
	{
		if (lrl == null)
			return;
		
		LRLS.add(lrl);
	}
	
	public static void removeLRL(LangReloadListener lrl)
	{
		LRLS.remove(lrl);
	}
	
	public static String a(String key)
	{
		return currentBundle.getString(key);
	}
	
	public static Locale getLocale()
	{
		return currentBundle.getLocale();
	}
	
	public static interface LangReloadListener
	{
		void onLangReload();
	}
	
	static
	{
		URLClassLoader ucl = null;
		
		try
		{
			ucl = new URLClassLoader(new URL[] { new File("./lang").toURI()
					.toURL() });
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		LANG_CL = ucl;
		setLocale(new Config(new File(".", "options.opt")).getString("lang", "de").equalsIgnoreCase("de") ?
				Locale.GERMAN : Locale.ENGLISH);
	}
}