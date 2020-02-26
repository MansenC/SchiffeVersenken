import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingUtilities;

import de.gsh.mc.GameOptions;
import de.gsh.mc.LoginWindow;
import de.gsh.mc.SchiffeVersenken;

public class StartGame
{
	public static void main(String[] args)
	{
		System.setOut(new OutStream(System.out)); // Outputstream setzen!
		
		if (!new File("./logs/").exists())
			new File("./logs/").mkdirs();
		
		if (!new File("./audio/").exists())
		{
			System.out.println("Copying resources from jar...");
			
			try
			{
				Thread.sleep(1000L);
				
				copyResources();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				new File("./audio/etc/").delete();
				new File("./audio/").delete();
				new File("./img/etc/").delete();
				new File("./img/").delete();
				new File("./lang/").delete();
			}
		}
		
		GameOptions.loadOptions();
		
		SwingUtilities.invokeLater(() ->
		{
			Thread.currentThread().setName("MainQueue");
			LoginWindow.create();
		});
		
		Runtime.getRuntime().addShutdownHook(new SchiffeVersenken.ShutdownHook()); // Beim Beenden ausführen
		System.out.println("Initialization finished!");
	}
	
	private static void copyResources() throws IOException
	{
		new File("./audio/etc/").mkdirs();
		new File("./img/etc/").mkdirs();
		new File("./lang/").mkdirs();
		
		System.out.println("Created folders!");
		
		CodeSource source = StartGame.class.getProtectionDomain().getCodeSource();
		
		if (source == null)
			throw new IOException("CodeSource of jar is null!");
		
		ZipInputStream zis = null;
		
		try
		{
			zis = new ZipInputStream(source.getLocation().openStream());
			ZipEntry entry;
			
			while ((entry = zis.getNextEntry()) != null)
			{
				System.out.println("Reading entry " + entry);
				
				if (entry.getName().startsWith("de") || entry.getName().startsWith("META-INF")
						|| entry.getName().startsWith("StartGame") || !(entry.getName().endsWith(".wav")
						|| entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg")
						|| entry.getName().endsWith(".gif") || entry.getName().endsWith(".properties")))
					// Don't want to copy that!
				{
					System.out.println("Passing entry!");
					continue;
				}
				
				InputStream in = StartGame.class.getResourceAsStream(entry.getName());
				
				if (in == null)
					throw new IOException("Can't load " + entry.getName() + " as stream!");
				
				int read;
				byte[] buffer = new byte[4096];
				
				File f = new File(new File(source.getLocation().toURI().getPath()).getParentFile().getPath()
						.replace('\\', '/') + "/" + entry.getName());
				
				if (!f.exists())
					f.createNewFile();
				
				FileOutputStream out = new FileOutputStream(f);
				
				while ((read = in.read(buffer)) > 0)
					out.write(buffer, 0, read);
				
				in.close();
				out.close();
				
				System.out.println("File copied!");
			}
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (zis != null)
				zis.close();
		}
		
		System.out.println("All files copied!");
	}
	
	public static class OutStream extends PrintStream
	{
		private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss"); // Uhrzeitanzeige-Format
		private final BufferedWriter theWriter;
		
		public OutStream(PrintStream ps)
		{
			super(ps); // Stream übergeben
			
			File f = new File("./logs/", "log" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt");
			BufferedWriter bw;
			
			try
			{
				f.getParentFile().mkdirs();
				f.createNewFile();
				
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				bw = null;
			}
			
			theWriter = bw;
		}
		
		@Override
		public void println(String x)
		{
			super.println("[" + FORMAT.format(new Date()) + "][" + Thread.currentThread().getName() + "] " + x); // Derzeitigen Thread und die Uhrzeit vor der Ausgabe angeben
			
			try
			{
				theWriter.write("[" + FORMAT.format(new Date()) + "][" + Thread.currentThread().getName() + "] " + x
						+ System.lineSeparator());
				theWriter.flush();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		@Override
		public void println(Object x)
		{
			super.println("[" + FORMAT.format(new Date()) + "][" + Thread.currentThread().getName() + "] " + x); // Same here
			
			try
			{
				theWriter.write("[" + FORMAT.format(new Date()) + "][" + Thread.currentThread().getName() + "] " + x
						+ System.lineSeparator());
				theWriter.flush();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		@Override
		public void close()
		{
			super.close();
			
			try
			{
				theWriter.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		@Override
		public void flush()
		{
			super.flush();
			
			try
			{
				theWriter.flush();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}