package de.gsh.mc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Config oder auch Configuration ist ein Datei-Speicher der verschiedene Datentypen speichern und laden kann.
 * 
 * @author Manuel Carofiglio
 * @category FileSystem
 * @since 0.7
 * @version 1.0
 * 
 * @see Map
 * @see BufferedReader
 * @see BufferedWriter
 * @see GameOptions
 */
public class Config
{
	private final Map<String, String> table;
	
	public Config(File f)
	{
		table = new HashMap<String, String>();
		
		try
		{
			load(f);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void save(File f)
	{
		try
		{
			if (!f.exists())
			{
				if (f.getParentFile() != null)
					f.getParentFile().mkdirs();
				
				f.createNewFile();
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			
			for (Map.Entry<String, String> entry : table.entrySet())
				bw.write(entry.getKey() + "=" + entry.getValue() + System.lineSeparator());
			
			bw.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void set(String path, Object value)
	{
		if (value instanceof ArrayList)
		{
			setList(path, (ArrayList<String>) value);
			return;
		}
		
		table.put(path, value == null ? "null" : value.toString());
	}
	
	public void setList(String path, List<String> list)
	{
		if (list.isEmpty())
		{
			table.remove(path);
			return;
		}
		
		String str = "";
		
		for (String x : list)
			str += x + ";";
		
		table.put(path, str.substring(0, str.length() - 1));
	}
	
	public String get(String path)
	{
		return table.get(path);
	}
	
	public String getString(String path, String def)
	{
		if (!table.containsKey(path))
			return def;
		
		return table.get(path);
	}
	
	public int getInt(String path)
	{
		return Integer.parseInt(table.get(path));
	}
	
	public long getLong(String path)
	{
		return Long.parseLong(table.get(path));
	}
	
	public int getSaveInt(String path, int base)
	{
		if (!table.containsKey(path))
		{
			table.put(path, "" + base);
			return base;
		}
		
		return getInt(path);
	}
	
	public long getSaveLong(String path, long base)
	{
		if (!table.containsKey(path))
		{
			table.put(path, "" + base);
			return base;
		}
		
		return getLong(path);
	}
	
	public float getSaveFloat(String path, float base)
	{
		if (!table.containsKey(path))
		{
			table.put(path, "" + base);
			return base;
		}
		
		return Float.parseFloat(table.get(path));
	}
	
	public List<String> getStringList(String path)
	{
		if (!table.containsKey(path))
			return new ArrayList<String>();
		
		return new ArrayList<String>(Arrays.asList(table.get(path).split(";")));
	}
	
	private void load(File f) throws IOException
	{
		if (!f.exists())
		{
			if (f.getParentFile() != null)
				f.getParentFile().mkdirs();
			
			f.createNewFile();
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String ln;
		
		while ((ln = br.readLine()) != null)
			table.put(ln.split("=")[0], ln.split("=")[1]);
		
		br.close();
	}
}