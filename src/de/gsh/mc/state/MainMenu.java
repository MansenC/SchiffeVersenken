package de.gsh.mc.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.I18n;
import de.gsh.mc.Menu;

import static de.gsh.mc.I18n.a;

public class MainMenu extends Menu implements MouseListener, LangReloadListener, KeyListener
{
	private BufferedImage background;
	private Font headFont = new Font("arial", Font.BOLD, 50);
	private Font lowbobFont = new Font("arial", Font.PLAIN, 12);
	private Font buttonFont = new Font("arial", Font.PLAIN, 30);
	
	private String singleplayer;
	private String multiplayer;
	private String network;
	private String stats;
	private String options;
	private String help;
	private String exit;
	private String player;
	private String header;
	
	public MainMenu(SchiffeVersenken game)
	{
		super(game);
		
		onLangReload();
		I18n.addLRL(this);
	}
	
	public void transitMLG()
	{
		game.transitToState(new MLGMainMenu(game, 9));
	}
	
	@Override
	public void onLangReload()
	{
		singleplayer = a("mainMenu.singleplayer");
		multiplayer = a("mainMenu.multiplayer");
		network = a("mainMenu.network");
		stats = a("mainMenu.stats");
		options = a("mainMenu.options");
		help = a("mainMenu.help");
		exit = a("mainMenu.exit");
		player = a("mainMenu.player");
		header = a("mainMenu.header");
	}
	
	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { MainMenu }";
	}
	
	@Override
	public void initialize() throws Exception
	{
		game.setCursor(SchiffeVersenken.DEFAULT_CURSOR);
		
		BufferedImage load = ImageIO.read(new File("./img/", "mmbg.jpg"));
		BufferedImage then = new BufferedImage(SchiffeVersenken.WIDTH + 10, SchiffeVersenken.HEIGHT + 10, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(((double) SchiffeVersenken.WIDTH + 10) / load.getWidth(), ((double) SchiffeVersenken.HEIGHT + 10) / load.getHeight());
		background = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(load, then);
	}
	
	@Override
	public void render(Graphics2D g)
	{
		g.drawImage(background, 0, 0, null);
		g.setFont(headFont);
		g.setColor(new Color(100, 100, 255));
		g.drawString(header, 30, 80);
		g.setColor(Color.GRAY);
		g.setFont(buttonFont);
		g.drawString(player + game.getUserName(), 35, 115);
		g.setColor(Color.WHITE);
		g.setFont(lowbobFont);
		g.drawString("Made by Manuel Carofiglio, 2k16", 675, 640);
		g.drawString("Heiko", 836, 10);
		g.drawString("David", 836, 22);
		
		g.drawRect(30, 150, 250, 50);
		g.drawRect(30, 220, 250, 50);
		g.drawRect(30, 290, 250, 50);
		g.drawRect(30, 360, 250, 50);
		g.drawRect(30, 430, 250, 50);
		g.drawRect(30, 500, 250, 50);
		g.drawRect(30, 570, 250, 50);
		g.setFont(buttonFont);
		g.setColor(Color.white);
		g.drawString(singleplayer, 70, 185);
		g.drawString(multiplayer, 70, 255);
		g.drawString(network, 70, 325);
		g.drawString(stats, 70, 395);
		g.drawString(options, 70, 465);
		g.drawString(help, 70, 535);
		g.drawString(exit, 70, 605);
	}
	
	public BufferedImage createScreenshot(String title, String sp, String mp, String stats, String opt, String help,
			String exit, String player)
	{
		BufferedImage screen = new BufferedImage(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = screen.createGraphics();
		
		g.drawImage(background, 0, 0, null);
		g.setFont(headFont);
		g.setColor(new Color(100, 100, 255));
		g.drawString(title, 30, 80);
		g.setColor(Color.GRAY);
		g.setFont(buttonFont);
		g.drawString(player + game.getUserName(), 35, 115);
		g.setColor(Color.WHITE);
		g.setFont(lowbobFont);
		g.drawString("Mate by Manuel Carofiglio, 2k16", 675, 640);
		g.drawString("Heiko", 836, 10);
		g.drawString("David", 836, 22);
		
		g.drawRect(30, 150, 250, 50);
		g.drawRect(30, 220, 250, 50);
		g.drawRect(30, 290, 250, 50);
		g.drawRect(30, 360, 250, 50);
		g.drawRect(30, 430, 250, 50);
		g.drawRect(30, 500, 250, 50);
		g.drawRect(30, 570, 250, 50);
		g.setFont(buttonFont);
		g.setColor(Color.white);
		g.drawString(sp, 70, 185);
		g.drawString(mp, 70, 255);
		g.drawString(network, 70, 325);
		g.drawString(stats, 70, 395);
		g.drawString(opt, 70, 465);
		g.drawString(help, 70, 535);
		g.drawString(exit, 70, 605);
		g.dispose();
		
		return screen;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getX() < 30 || e.getX() > 280) // no button in sight
			return;
		
		int my = e.getY();
		
		game.transitToState(my >= 150 && my <= 200 ? new Singleplayer(game) : (my >= 220 && my <= 270 ?
				new Multiplayer(game) : (my >= 290 && my <= 340 ? new Network(game) :
					(my >= 360 && my <= 410 ? new Stats(game) : (my >= 500 && my <= 550 ? new Help(game) :
						(my >= 570 && my <= 620 ? new Exit(game) : null))))));
		
		if (my < 430 || my > 480)
			return;
		
		new Options().setVisible(true);
	}
	
	private String str = "";
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (!str.equalsIgnoreCase("mlg"))
			{
				str = "";
				return;
			}
			
			str = "";
			game.transitToState(new MLGMainMenu(game, 0));
			return;
		}
		
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_M:
			str += "m";
			break;
		case KeyEvent.VK_L:
			str += "l";
			break;
		case KeyEvent.VK_G:
			str += "g";
			break;
		}
	}
	
	@Override public void tick() {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void keyTyped(KeyEvent e) {}
	@Override public void keyReleased(KeyEvent e) {}
	
	public class MLGMainMenu extends Menu implements MouseListener
	{
		private BufferedImage sc, dwi, jt, bs, ide, zss, ide2, ib;
		
		private Image jc, ttte;
		
		public MLGMainMenu(SchiffeVersenken sv, int s)
		{
			super(sv);
			this.s = s;
			
			if (s == 10)
			{
				sc = MainMenu.this.createScreenshot("eZZ Get REKT!", "YOLO MODE", "SNIPER CLAN", "SANIC STATZ",
						"OPTZ", "HEALP", "EXXXIET", "M8: ");
				glix = jlix = gliy = Integer.MIN_VALUE;
				jliy = Integer.MAX_VALUE;
			}
		}
		
		private float s = 0;
		private double d;
		
		@Override
		public void render(Graphics2D g)
		{
			if (s < 4)
				g.drawImage(sc, 0, 0, null);
			
			if (s == 0)
			{
				sps();
				s++;
				return;
			}
			
			if (s == 1)
			{
				d += game.getDeltaTime();
				
				if (d < 1.5)
					return;
				
				d = 0;
				s++;
				return;
			}
			
			if (s == 2)
			{
				boolean gl = aG(g);
				boolean j = aJ(g);
				
				if (gl && j)
					s++;
				
				return;
			}
			
			if (s == 3)
			{
				aG(g);
				aJ(g);
				
				d += game.getDeltaTime();
				
				if (d < 1.5)
					return;
				
				d = 0;
				s += 0.5;
				bs = bS();
				
				return;
			}
			
			if (s == 3.5)
			{
				aG(g);
				aJ(g);
				
				g.setFont(new Font("arial", Font.BOLD, 100));
				g.setColor(Color.RED);
				g.drawString("What's That?!", 50, 300);
				d += game.getDeltaTime();
				
				if (d < 2)
					return;
				
				d = 0;
				s += 0.5;
				return;
			}
			
			if (s == 4)
			{
				if (!aZ(g))
					return;
				
				s++;
				return;
			}
			
			if (s == 5)
			{
				if (!aZ(g))
					return;
				
				s++;
				zss = zSS();
				return;
			}
			
			if (s >= 6 && s < 8)
				g.drawImage(zss, 0, 0, null);
			
			if (s == 6)
			{
				d += game.getDeltaTime();
				
				if (d < 1.5)
					return;
				
				d = 0;
				s++;
				pIA();
				return;
			}
			
			if (s == 7)
			{
				dI(g);
				d += game.getDeltaTime();
				
				if (d < 3.2)
					return;
				
				s++;
				ib = cIB();
				d = 0;
				
				return;
			}
			
			if (s == 8)
			{
				if (!sII(g))
					return;
				
				s++;
				return;
			}
			
			if (s == 9)
			{
				pJC();
				s++;
			}
			
			if (s == 10)
			{
				d += game.getDeltaTime();
				dJC(g);
				
				if (d < 4.5)
					return;
				
				s++;
				d = 0;
				pTTPT();
			}
			
			if (s == 11)
			{
				dTTTE(g);
				d += game.getDeltaTime();
				
				if (d < 4)
					return;
				
				d = 0;
				s++;
			}
			
			if (s == 12)
			{
				d += game.getDeltaTime();
				
				if (d < 1)
					return;
				
				s++;
				d = 0;
				sc = MainMenu.this.createScreenshot("eZZ Get REKT!", "YOLO MODE", "SNIPER CLAN", "SANIC STATZ",
						"OPTZ", "HEALP", "EXXXIET", "M8: ");
				return;
			}
			
			if (s == 13)
			{
				SchiffeVersenken.setMLG();
				game.getFrame().setTitle("MLG Sw4g eZZZ M8!");
				pS();
				s++;
				ide = rotateImage(scaleImage(ide, 19, 24), 180);
				return;
			}
			
			if (s == 14)
			{
				g.drawImage(sc, 0, 0, null);
				aG(g);
				aJ(g);
				g.drawImage(ide, 39, 90, null);
				
				return;
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (s != 14)
				return;
			
			MainMenu.this.mousePressed(e);
		}
		
		Random rnd = new Random();

		@Override
		public void tick()
		{
			if (s != 14 || rnd.nextInt(5000) != 0)
				return;
			
			pLVA();
		}

		@Override
		public void initialize() throws IOException
		{
			sc = MainMenu.this.createScreenshot(MainMenu.this.header, MainMenu.this.singleplayer,
					MainMenu.this.multiplayer, MainMenu.this.stats, MainMenu.this.options,
					MainMenu.this.help, MainMenu.this.exit, MainMenu.this.player);
			dwi = scaleImage(ImageIO.read(new File("./img/etc/", "g.png")), 200, 56);
			jt = scaleImage(ImageIO.read(new File("./img/etc/", "j.png")), 200, 160);
			ide = scaleImage(ImageIO.read(new File("./img/etc/", "ide.png")), 100, 100);
			ide2 = scaleImage(ide, 50, 50);
			
			jc = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "johncena.gif").toURI().toURL());
			ttte = Toolkit.getDefaultToolkit().createImage(new File("./img/etc/", "Thomas.gif").toURI().toURL());
		}
		
		private BufferedImage bS()
		{
			BufferedImage bs = new BufferedImage(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT,
					BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = bs.createGraphics();
			
			g.drawImage(sc, 0, 0, null);
			aG(g);
			aJ(g);
			g.dispose();
			
			return bs;
		}
		
		private BufferedImage cIB()
		{
			BufferedImage bs = new BufferedImage(860, 640, 2);
			
			Graphics2D g = bs.createGraphics();
			g.drawImage(zss, 0, 0, null);
			g.drawImage(ide, 550, 240, null);
			g.drawImage(ide, 270, 270, null);
			g.drawImage(rotateImage(ide, 25), 10, 310, null);
			g.drawImage(ide2, 530, 390, null);
			g.drawImage(ide2, 415, 430, null);
			g.dispose();
			
			return bs;
		}
		
		private void dTTTE(Graphics2D g)
		{
			g.drawImage(ttte, 200, 170, null);
		}
		
		private void dJC(Graphics2D g)
		{
			g.drawImage(jc, 150, 150, null);
		}
		
		private void dI(Graphics2D g)
		{
			g.drawImage(ide, 550, 240, null);
			g.drawImage(ide, 270, 270, null);
			g.drawImage(rotateImage(ide, 25), 10, 310, null);
			g.drawImage(ide2, 530, 390, null);
			g.drawImage(ide2, 415, 430, null);
		}
		
		private BufferedImage rotateImage(BufferedImage in, float degree)
		{
			double sin = Math.abs(Math.sin(Math.toRadians(degree)));
			double cos = Math.abs(Math.cos(Math.toRadians(degree)));
			int newWidth = (int) Math.floor(in.getWidth() * cos + in.getHeight() * sin);
			int newHeight = (int) Math.floor(in.getHeight() * cos + in.getWidth() * sin);
			
		    BufferedImage buffer = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		    Graphics2D g = buffer.createGraphics();
		    
		    g.translate((newWidth - in.getWidth()) / 2, (newHeight - in.getHeight()) / 2);
		    g.rotate(Math.toRadians(degree), in.getWidth() / 2, in.getHeight() / 2);
		    g.drawRenderedImage(in, null);
		    g.dispose();
		    
		    return buffer;
		}
		
		private double gsx = 412, gsy = 180, gfx = SchiffeVersenken.WIDTH, gfy = SchiffeVersenken.HEIGHT / 3,
				glix = gfx, gliy = gfy;
		
		private boolean aG(Graphics2D g)
		{
			if (glix <= gsx && gliy <= gsy)
			{
				g.drawImage(dwi, (int) gsx, (int) gsy, null);
				return true;
			}
			
			g.drawImage(dwi, (int) (glix = glix + (gsx - gfx) * game.getDeltaTime()),
					(int) (gliy = gliy + (gsy - gfy) * game.getDeltaTime()), null);
			
			return gsx <= 400 && gsy <= 200;
		}
		
		private double jsx = 210, jsy = 480, jfx = SchiffeVersenken.WIDTH, jfy = SchiffeVersenken.HEIGHT / 3,
				jlix = jfx, jliy = jfy;
		
		private boolean aJ(Graphics2D g)
		{
			if (jlix <= jsx && jliy >= jsy)
			{
				g.drawImage(jt, (int) jsx, (int) jsy, null);
				return true;
			}
			
			g.drawImage(jt, (int) (jlix = jlix + (jsx - jfx) * game.getDeltaTime()),
					(int) (jliy = jliy + (jsy - jfy) * game.getDeltaTime()), null);
			
			return jsx <= 400 && jsy <= 200;
		}
		
		private double zl = 1.0D;
		
		private boolean aZ(Graphics2D g)
		{
			g.scale(zl, zl);
			g.drawImage(bs, (int) ((zl - 1) * -42), (int) ((zl - 1) * -42), null);
			
			if (zl >= 8.0D)
				return true;
			
			zl += game.getDeltaTime() * 1.4;
			
			return zl >= 8.0D;
		}
		
		private double zsii = 1.0;
		
		private boolean sII(Graphics2D g)
		{
			g.scale(zsii, zsii);
			g.drawImage(ib, (int) ((zl - 1) * -42), (int) ((zl - 1) * -42), null);
			
			if (zsii >= 10.0D)
				return true;
			
			zsii += game.getDeltaTime() * 2;
			return zsii >= 10.0D;
		}
		
		private BufferedImage zSS()
		{
			BufferedImage bi = new BufferedImage(860, 640, 0x2);
			
			Graphics2D g = bi.createGraphics();
			g.scale(zl, zl);
			g.drawImage(bs, (int) ((zl - 1) * -42), (int) ((zl - 1) * -42), null);
			g.dispose();
			
			return bi;
		}
		
		private void pLVA()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Clip c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "leviosaaaa.wav")));
						c.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}
		
		private void sps()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Clip c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "h.wav")));
						c.start();
						
						Thread.sleep(2400L);
						
						c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "swed.wav")));
						c.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}
		
		private void pIA()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Clip c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "ic.wav")));
						c.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}
		
		private void pJC()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Clip c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "jc.wav")));
						c.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}
		
		private void pTTPT()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Clip c = AudioSystem.getClip();
						c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "ttpt.wav")));
						c.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}
		
		private void pS()
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (SchiffeVersenken.rN)
						return;
					
					SchiffeVersenken.rN = true;
					
					try
					{
						while (true)
						{
							Clip c = AudioSystem.getClip();
							c.open(AudioSystem.getAudioInputStream(new File("./audio/etc/", "sweds.wav")));
							((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-12);
							c.start();
							
							Thread.sleep(246000L);
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			
			t.setName("SwagThread");
			t.start();
		}

		@Override
		public String toString()
		{
			return "Hohoho bitches";
		}

		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
	}
}