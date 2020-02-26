package de.gsh.mc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JFrame;

import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.state.MainMenu;
import de.gsh.mc.state.Network;

public class SchiffeVersenken extends Canvas implements Runnable, FocusListener
{
	private static final long serialVersionUID = 908791867589047502L;
	
	public static boolean rN;
	public static final int WIDTH = 860;
	public static final int HEIGHT = 640;
	public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	public static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
	public static final Cursor INVISIBLE_CURSOR;
	
	private static SchiffeVersenken instance;
	private static boolean mlg;
	
	private final Thread processingThread = new Thread(this);
	private final JFrame frame;
	private final String theUserName;
	private PlayerProfile theProfile;
	
	private static SpriteSheet texSheet;
	Menu currentMenu;
	
	boolean isFocused;
	private boolean running;
	private double frameDelta;
	private double delta;
	private double nanos = 1000000000.0D / 60.0D; // Options
	
	public SchiffeVersenken(JFrame frame, String userName)
	{
		this.frame = frame;
		theUserName = userName;
		theProfile = new PlayerProfile(userName);
	}
	
	public void start() throws Exception
	{
		System.out.println("Game starting!");
		
		instance = this; // Dis was baad!
		
		if (running)
			return;
		
		try
		{
			texSheet = new SpriteSheet(ImageIO.read(new File("./img/", "textures.png")), 32);
			System.out.println("SpriteSheet loaded!");
		}
		catch (IOException e)
		{
			System.err.println("Severe exception loading main-sprite!");
			e.printStackTrace();
		}
		
		transitToState(new MainMenu(this));
		System.out.println("Joined the Main Menu");
		running = true;
		
		sdg = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "snoopie.gif").toURI().toURL());
		htg = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "toad.gif").toURI().toURL());
		htg2 = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "toad2.gif").toURI().toURL());
		wg = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "wtf.gif").toURI().toURL());
		wowg = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "FUCKIT.gif").toURI().toURL());
		
		// LAST!
		
		processingThread.setPriority(Thread.MAX_PRIORITY);
		processingThread.setName("GameLoop");
		processingThread.start();
		
		System.out.println("GameLoop started.");
	}
	
	@Override
	public void run()
	{
		long last = System.nanoTime();
		long timer = System.currentTimeMillis();
		
		while (running)
		{
			long now = System.nanoTime();
			delta += (now - last) / nanos;
			frameDelta = (now - last) / nanos / 60;
			last = now;
			
			if (delta >= 1)
			{
				delta--;
				
				try
				{
					currentMenu.tick();
				}
				catch (Exception ex)
				{
					System.err.println("Exception while tick-execution!");
					ex.printStackTrace();
					continue;
				}
			}

			render();
			
			if (System.currentTimeMillis() - timer > 1000)
			{
				timer = System.currentTimeMillis();
			}
		}
	}
	
	private void render()
	{
		BufferStrategy bs = getBufferStrategy();
		
		if (bs == null)
		{
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		// Screen Putzfrau
		g.drawImage(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB), 0, 0, getWidth(), getHeight(), this);
		
		currentMenu.render((Graphics2D) g);
		
		if (mlg)
			cc(g);
		
		// LAST!
		g.dispose();
		bs.show();
	}
	
	private final Color r = Color.RED;
	private final Color y = Color.YELLOW;
	private double rat = 0.0;
	private double x = 0.0;
	private Image sdg, htg, wg, wowg, htg2;
	
	private void cc(Graphics g)
	{
		rat = Math.sin(x);
		x += getDeltaTime();
		
		BufferedImage bi = new BufferedImage(870, 650, 2);
		Graphics2D g2 = bi.createGraphics();
		Color c = new Color((int) Math.abs((rat * r.getRed()) + ((1 - rat) * y.getRed())), (int) Math.min(Math.abs((
				rat * r.getGreen()) + ((1 - rat) * y.getGreen())), 255), (int) Math.abs((rat * r.getBlue()) + ((1 - rat)
						* y.getBlue())), 50);
		g2.setColor(c);
		g2.fillRect(0, 0, 870, 650);
		g2.dispose();
		
		g.drawImage(sdg, 150, 290, null);
		g.drawImage(htg, 360, 0, null);
		g.drawImage(htg, 460, 0, null);
		g.drawImage(htg, 560, 0, null);
		g.drawImage(htg, 660, 0, null);
		g.drawImage(htg, 760, 0, null);
		g.drawImage(htg2, 0, 540, null);
		g.drawImage(wg, 540, 0, null);
		g.drawImage(wowg, 560, 471, null);
		g.drawImage(bi, 0, 0, null);
	}
	
	public static SpriteSheet getTexSheet()
	{
		return texSheet;
	}
	
	public static class ShutdownHook extends Thread
	{
		public void run()
		{
			if (instance.currentMenu instanceof Network)
				((Network) instance.currentMenu).closeConnection();
			
			GameOptions.saveOptions();
			
			for (PlayerProfile pp : PlayerProfile.EVER_LOADED.values())
				pp.save();
			
			System.out.println("System exiting!");
		}
	}
	
	private boolean focusWasOnThis = false;
	protected int calls = 0;
	
	@Override
	public void focusGained(FocusEvent e)
	{
		if (!focusWasOnThis && e.getComponent() == this)
			focusWasOnThis = true;
		
		calls++;
		
		System.out.println("Focus gained on " + e.getComponent());
		isFocused = true;
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		System.out.println("Focus lost on " + e.getComponent());
		isFocused = false;
	}
	
	public boolean isFocused()
	{
		return isFocused;
	}
	
	public Menu getMenu()
	{
		return currentMenu;
	}
	
	public double getDeltaTime()
	{
		return frameDelta;
	}
	
	public JFrame getFrame()
	{
		return frame;
	}
	
	public String getUserName()
	{
		return theUserName;
	}
	
	public PlayerProfile getPlayerProfile()
	{
		return theProfile;
	}
	
	public void transitToState(Menu menu)
	{
		if (menu == null || currentMenu == menu)
			return;
		
		if (currentMenu instanceof MouseListener)
		{
			frame.removeMouseListener((MouseListener) currentMenu);
			removeMouseListener((MouseListener) currentMenu);
		}
		
		if (currentMenu instanceof MouseMotionListener)
			removeMouseMotionListener((MouseMotionListener) currentMenu);
		
		if (currentMenu instanceof KeyListener)
			removeKeyListener((KeyListener) currentMenu);
		
		if (currentMenu instanceof LangReloadListener)
			I18n.removeLRL((LangReloadListener) currentMenu);
		
		if (menu instanceof MainMenu && mlg)
		{
			((MainMenu) menu).transitMLG();
			return;
		}
		
		System.out.println("Transiting from menu " + currentMenu + " to menu " + menu);
		
		currentMenu = menu;
		isFocused = true;
		
		if (currentMenu instanceof KeyListener)
			addKeyListener((KeyListener) currentMenu);
		
		if (currentMenu instanceof MouseMotionListener)
			addMouseMotionListener((MouseMotionListener) currentMenu);
		
		if (!(currentMenu instanceof MouseListener))
			return;
		
		frame.addMouseListener((MouseListener) currentMenu);
		addMouseListener((MouseListener) currentMenu);
		System.out.println("MouseListener " + menu + " added!");
	}
	
	public static SchiffeVersenken getInstance()
	{
		return instance;
	}
	
	public static synchronized void pad(final File f, final int val)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Clip c = AudioSystem.getClip();
					c.open(AudioSystem.getAudioInputStream(f));
					((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(val);
					c.start();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	public static synchronized void pad(final File f)
	{
		pad(f, -10);
	}
	
	public static synchronized void playAudio(final String name)
	{
		if (GameOptions.getSoundVolume() == -80.0) // Jep
			return;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Clip c = AudioSystem.getClip();
					c.open(AudioSystem.getAudioInputStream(new File("./audio/", name + ".wav")));
					((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(GameOptions.getSoundVolume());
					
					// The sound format is no bug like this; It's really gross with this format using decibels!
					System.out.println("Playing clip \"" + name + "\" with volume of " + GameOptions.getSoundVolume());
					
					c.start();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	public static boolean isMLG()
	{
		return mlg;
	}
	
	public static void setMLG()
	{
		mlg = true;
	}
	
	static
	{
		INVISIBLE_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1,
				BufferedImage.TRANSLUCENT), new Point(0, 0), "InvisibleCursor");
	}
}