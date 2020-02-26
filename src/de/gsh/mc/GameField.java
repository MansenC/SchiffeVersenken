package de.gsh.mc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.GameOptions;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.SpriteSheet;
import de.gsh.mc.state.MainMenu;
import de.gsh.mc.state.Multiplayer;
import de.gsh.mc.Menu;

/**
 * Das GameField ist die Klassen-Instanz für die grafische Darstellung des Spielfeldes.
 */
public class GameField implements LangReloadListener
{
	protected static final String X = "ABCDEFGHIJ";
	public static final int START_RASTER_X = 178;
	public static final int START_RASTER_Y = 68;
	public static final int TILE_DIM = 56;
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss");
	
	/*
	 * 0: Wasser
	 * 1: Nahe schiff
	 * 2: 2er Schiff Vert
	 * 3: 3er Schiff Vert
	 * 4: 4er Schiff Vert
	 * 5: 5er Schiff Vert
	 * 6: 2er Schiff Hori
	 * 7: 3er Schiff Hori
	 * 8: 4er Schiff Hori
	 * 9: 5er Schiff Hori
	 * 10: Schiff
	 */
	protected final byte gameTensor[][][];
	
	/*
	 * -1: Nichts zum Rendern
	 * 0: Wasser
	 * 1: Treffer
	 * 2-9 siehe oben
	 */
	// Aufgestockt: Von der Matrix zum Tensor der 3ten Dimension
	protected final byte renderTensor[][][];
	protected final Font defFont = new Font("arial", Font.PLAIN, 30);
	protected final Font headFont = new Font("arial", Font.BOLD, 50);
	protected final Font textFont = new Font("arial", Font.PLAIN, 15);
	private final byte players;
	
	protected BufferedImage hit;
	protected BufferedImage water;
	protected BufferedImage background;
	protected BufferedImage[] shipImages;
	
	protected byte[][] toHitShips;
	protected boolean started = false;
	protected byte turnedPlayer = 0;
	private BotAI bot;
	protected String[] playerNames;
	private boolean invalidated;
	public boolean paused;
	
	// I18n
	protected String mainMenu;
	protected String player;
	protected String clicks;
	protected String time;
	protected String points;
	protected String playerStarts;
	protected String start;
	
	public GameField(byte players)
	{
		this(players, (byte) 0);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " -> { GameField: players = " + players + ", started = " + started +
				", turnedPlayer = " + turnedPlayer + ", bot = " + bot + ", invalidated = " + invalidated + " }";
	}
	
	public GameField(byte[][][] gameField, byte startingPlayer, byte[] hitShips)
	{
		players = 2;
		gameTensor = new byte[2][10][10];
		gameTensor[1] = gameField[0]; // #Flipper
		gameTensor[0] = gameField[1];
		renderTensor = new byte[2][10][10];
		turnedPlayer = (byte) (1 - startingPlayer);
		toHitShips = new byte[2][];
		toHitShips[0] = toHitShips[1] = hitShips;
		
		onLangReload();
		I18n.addLRL(this);
		
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				renderTensor[0][i][j] = renderTensor[1][i][j] = -1;
	}
	
	public GameField(byte players, byte startingPlayer)
	{
		this.players = players;
		gameTensor = new byte[players][10][10];
		renderTensor = new byte[players][10][10];
		turnedPlayer = startingPlayer;
		onLangReload();
		I18n.addLRL(this);
		
		doPlacement(0);
	}
	
	public void onLangReload()
	{
		if (!SchiffeVersenken.isMLG())
		{
			mainMenu = I18n.a("gameField.mainMenu");
			player = I18n.a("gameField.players");
			clicks = I18n.a("gameField.clicks");
			time = I18n.a("gameField.time");
			points = I18n.a("gameField.points");
			playerStarts = I18n.a("gameField.playerStarts");
			start = I18n.a("gameField.start");
			return;
		}
		
		mainMenu = "Stahp ya p'sey!";
		player = "M8 ";
		clicks = "Noscopes:";
		time = "Time:";
		points = "SKILL:";
		playerStarts = "M8 %s starts!";
		start = "SCOPE YA P'SEY";
	}
	
	private void doPlacement(int idx)
	{
		if (idx >= 25)
		{
			JOptionPane.showMessageDialog(SchiffeVersenken.getInstance(), I18n.a("gameField.error.notAllShips"));
			
			if (players != 1)
			{
				SchiffeVersenken.getInstance().transitToState(new MainMenu(SchiffeVersenken.getInstance()));
				invalidated = true;
				return;
			}
			
			toHitShips = new byte[1][4];
			
			for (int x = 0; x < 10; x++)
			{
				for (int y = 0; y < 10; y++)
				{
					byte value = gameTensor[0][x][y];
					
					if (value < 2 || value == 10)
						continue;
					
					toHitShips[0][value % 4 == 0 ? 3 : value % 4 - 2]++;
				}
			}
			
			return;
		}
		
		for (int h = 0; h < players; h++)
		{
			for (int i = 0; i < 10; i++)
			{
				for (int j = 0; j < 10; j++)
				{
					gameTensor[h][i][j] = 0;
					renderTensor[h][i][j] = -1;
				}
			}
		}
		
		byte[] ships = GameOptions.getAmountShips();
		toHitShips = new byte[players][ships.length];
		
		for (int i = 0; i < players; i++)
			for (int j = 0; j < toHitShips[i].length; j++)
				toHitShips[i][j] = ships[j];
		
		Random rand = new Random();
		
		for (int cp = 0; cp < players; cp++)
		{
			for (int shipLength = 0; shipLength < ships.length; shipLength++)
			{
				int gothrough = 0;
				
				placement:
					for (int ship = 0; ship < ships[shipLength]; ship++)
					{
						gothrough++;
						
						if (gothrough >= 1000)
						{
							System.err.println("Invalid shipplacement!");
							doPlacement(++idx);
							return; // XXX Keine REKURSION! Abbruch direkt nach der Methode!
						}
						
						boolean vert = rand.nextBoolean();
						int tileX = rand.nextInt(vert ? 10 : 9 - shipLength);
						int tileY = rand.nextInt(vert ? 9 - shipLength : 10);
						
						for (int i = 0; i < shipLength + 2; i++)
						{
							if (gameTensor[cp][vert ? tileX : tileX + i][vert ? tileY + i : tileY] == 0)
								continue;
							
							ship--;
							continue placement;
						}
						
						if (vert)
						{
							for (int x = Math.max(tileX - 1, 0); x <= Math.min(tileX + 1, 9); x++)
							{
								for (int y = Math.max(tileY - 1, 0); y < Math.min(tileY + shipLength + 3, 10); y++)
								{
									if ((x == tileX - 1 && y == tileY - 1)
											|| (x == tileX - 1 && y == tileY + shipLength + 2)
											|| (x == tileX + 1 && y == tileY - 1)
											|| (x == tileX + 1 && y == tileY + shipLength + 2))
										continue;
									
									gameTensor[cp][x][y] = 1;
								}
							}
							
							gameTensor[cp][tileX][tileY] = (byte) (shipLength + 2);
							
							for (int y = tileY + 1; y < tileY + shipLength + 2; y++)
								gameTensor[cp][tileX][y] = 10;
							
							continue;
						}
						
						for (int x = Math.max(tileX - 1, 0); x < Math.min(tileX + shipLength + 3, 10); x++)
						{
							for (int y = Math.max(tileY - 1, 0); y <= Math.min(tileY + 1, 9); y++)
							{
								if ((y == tileY - 1 && x == tileX - 1)
										|| (y == tileY - 1 && x == tileX + shipLength + 2)
										|| (y == tileY + 1 && x == tileX - 1)
										|| (y == tileY + 1 && x == tileX + shipLength + 2))
									continue;
								
								gameTensor[cp][x][y] = 1;
							}
						}
						
						gameTensor[cp][tileX][tileY] = (byte) (shipLength + 6);
						
						for (int x = tileX + 1; x < tileX + shipLength + 2; x++)
							gameTensor[cp][x][tileY] = 10;
					}
			}
		}
	}
	
	public byte[][][] getGameTensor()
	{
		byte[][][] cop = new byte[gameTensor.length][10][10];
		
		for (int i = 0; i < cop.length; i++)
			for (int j = 0; j < 10; j++)
				for (int k = 0; k < 10; k++)
					cop[i][j][k] = gameTensor[i][j][k];
		
		return cop;
	}
	
	public byte[][][] getRenderTensor()
	{
		byte[][][] cop = new byte[renderTensor.length][10][10];
		
		for (int i = 0; i < cop.length; i++)
			for (int j = 0; j < 10; j++)
				for (int k = 0; k < 10; k++)
					cop[i][j][k] = renderTensor[i][j][k];
		
		return cop;
	}
	
	protected BufferedImage rotateImage(BufferedImage in, float degree)
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
	
	protected BufferedImage rotateImageXZ(BufferedImage image, float degree)
	{
		BufferedImage newImage = new BufferedImage((int) (1 + image.getWidth() * Math.cos(Math.toRadians(degree))),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (int x = 0; x < newImage.getWidth(); x++)
			for (int y = 0; y < newImage.getHeight(); y++)
				newImage.setRGB(x, y, image.getRGB(x * image.getWidth() / newImage.getWidth(), y));
		
		return newImage;
	}

	public void render(Graphics2D g, int[] clicksNeeded, int maxClicks, long[] startAt, int points[],
			boolean bott)
	{
		if (invalidated)
			return;
		
		g.drawImage(background, 0, 0, null);
		
		g.setFont(textFont);
		g.setColor(Color.GRAY);
		g.fillRect(10, 10, 100, 40);
		g.setColor(Color.WHITE);
		g.drawString(mainMenu, 25, 35);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(10, 10, 100, 40);
		
		g.setColor(Color.WHITE);
		g.fillRect(10, 60, 100, 160 * (players == 3 || players == 4 ? 2 : players));
		
		if (players >= 3)
			g.fillRect(745, 60, 100, 160 * (players - 2));
		
		g.setColor(Color.BLACK);
		g.drawRect(10, 60, 100, 160 * (players == 3 || players == 4 ? 2 : players));
		
		if (players >= 3)
			g.drawRect(745, 60, 100, 160 * (players - 2));
		
		int x = 10;
		
		for (int y = 0; y < players; y++)
		{
			if (y == 2)
				x = 745;
			
			g.drawString(player + (y + 1) + ":", x + 2, 160 * (y % 2) + 75);
			g.drawLine(x, 160 * (y % 2) + 80, x + 100, 160 * (y % 2) + 80);
			g.setColor(y == turnedPlayer ? Color.RED : Color.BLACK);
			g.drawString(playerNames[y], x + 2, 160 * (y % 2) + 95);
			g.setColor(Color.BLACK);
			g.drawLine(x, 160 * (y % 2) + 100, x + 100, 160 * (y % 2) + 100);
			g.drawString(clicks, x + 2, 160 * (y % 2) + 115);
			g.drawLine(x, 160 * (y % 2) + 120, x + 100, 160 * (y % 2) + 120);
			g.setColor(clicksNeeded[y] < (maxClicks / 4 * 3) ? Color.black : (clicksNeeded[y] < (maxClicks / 16 * 15) ?
					Color.ORANGE : Color.RED));
			g.drawString("" + clicksNeeded[y], x + 2, 160 * (y % 2) + 135);
			g.setColor(Color.BLACK);
			g.drawString("|", x + 45, 160 * (y % 2) + 135);
			g.setColor(Color.RED);
			g.drawString("    " + maxClicks, x + 42, 160 * (y % 2) + 135);
			g.setColor(Color.BLACK);
			g.drawLine(x, 160 * (y % 2) + 140, x + 100, 160 * (y % 2) + 140);
			g.drawString(time, x + 2, 160 * (y % 2) + 155);
			g.drawLine(x, 160 * (y % 2) + 160, x + 100, 160 * (y % 2) + 160);
			g.drawString(started ? DATE_FORMAT.format(new Date(startAt[y]))
					: "00:00", x + 2, 160 * (y % 2) + 175);
			g.drawLine(x, 160 * (y % 2) + 180, x + 100, 160 * (y % 2) + 180);
			g.drawString(this.points, x + 2, 160 * (y % 2) + 195);
			g.drawLine(x, 160 * (y % 2) + 200, x + 100, 160 * (y % 2) + 200);
			g.drawString("" + points[y], x + 2, 160 * (y % 2) + 215);
			g.drawLine(x, 160 * (y % 2) + 219, x + 100, 160 * (y % 2) + 219);
			g.drawLine(x, 160 * (y % 2) + 220, x + 100, 160 * (y % 2) + 220);
			g.drawLine(x, 160 * (y % 2) + 221, x + 100, 160 * (y % 2) + 221);
		}
		
		g.setColor(Color.BLACK);
		g.setFont(defFont);
		
		for (int i = 1; i < 10; i++)
			g.drawString(Integer.toString(i), 143, 53 + 56 * i);
		
		g.drawString("10", 135, 613);
		
		for (int i = 0; i < 10; i++)
			g.drawString(Character.toString(X.charAt(i)), 196 + 56 * i , 53);
		
		for (int tileX = 0; tileX < 10; tileX++)
		{
			for (int tileY = 0; tileY < 10; tileY++)
			{
				int curX = START_RASTER_X + tileX * TILE_DIM;
				int curY = START_RASTER_Y + tileY * TILE_DIM;
				
				if (renderTensor[turnedPlayer][tileX][tileY] >= 2)
				{
					g.drawImage(shipImages[renderTensor[turnedPlayer][tileX][tileY] - 2], curX, curY, null);
					g.drawImage(hit, curX, curY, null);
					continue;
				}
				
				if (renderTensor[turnedPlayer][tileX][tileY] == 0)
				{
					g.drawImage(water, curX, curY, null);
					continue;
				}
				
				if (renderTensor[turnedPlayer][tileX][tileY] != 1)
					continue;
				
				g.drawImage(hit, curX, curY, null);
			}
		}
		
		if (!started)
		{
			g.setFont(headFont);
			g.setColor(Color.BLUE);
			
			if (players != 1)
			{
				String msg = String.format(playerStarts, playerNames[turnedPlayer]);
				g.drawString(msg, Menu.getCenter(g, msg), 240);
			}
			
			g.drawString(start, Menu.getCenter(g, start), 295);
		}
		
		if (!paused && bott && bot != null && turnedPlayer == 1)
			bot.tick(g, this);
	}
	
	public boolean tick()
	{
		if (invalidated)
			return false;
		
		for (int i : toHitShips[turnedPlayer])
		{
			if (i <= 0)
				continue;
			
			return false;
		}
		
		return true;
	}
	
	public void initialize(String nd)
	{
		shipImages = new BufferedImage[8];
		SpriteSheet sheet = SchiffeVersenken.getTexSheet();
		playerNames = new String[players];
		playerNames[0] = SchiffeVersenken.getInstance().getUserName();
		
		if (playerNames.length > 1)
			playerNames[1] = nd;
		
		if (!SchiffeVersenken.isMLG())
		{
			hit = Menu.scaleImage(sheet.getImage(0, 1, 1, 1), TILE_DIM, TILE_DIM);
			water = Menu.scaleImage(sheet.getImage(1, 1, 1, 1), TILE_DIM, TILE_DIM);
		}
		else
		{
			try
			{
				water = Menu.scaleImage(ImageIO.read(new File("./img/etc/", "Sanic2.png")), TILE_DIM, TILE_DIM);
				hit = Menu.scaleImage(ImageIO.read(new File("./img/etc/", "snipha.png")), TILE_DIM, TILE_DIM);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		for (int i = 2; i <= 5; i++)
		{
			shipImages[i - 2] = Menu.scaleImage(sheet.getImage(0, i, i, 1), TILE_DIM, TILE_DIM * i);
			shipImages[i + 2] = rotateImage(shipImages[i - 2], 90);
		}
		
		try
		{
			background = ImageIO.read(new File("./img/", "GameBack.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void initialize()
	{
		initialize(null);
	}
	
	public int animState = 0;
	private double wait = 0;
	private float degree = 0.0F;
	private BufferedImage snapshot;
	private BufferedImage newScreen;
	
	public int transitToNextPlayer(Graphics2D g, int[] neededClicks, int maxClicks,
			long startAt[], int[] points)
	{
		
		if (animState == 0)
		{
			wait += SchiffeVersenken.getInstance().getDeltaTime();
			
			if (wait < 0.2)
				return animState;
			
			animState++;
			wait = 0;
		}
		
		if (animState == 1)
		{
			if (snapshot == null)
				snapshot = getScreenshot(neededClicks, maxClicks, startAt, points, true);
			
			if (newScreen == null)
			{
				turnedPlayer = (byte) (turnedPlayer + 1 == players ? 0 : turnedPlayer + 1);
				newScreen = getScreenshot(neededClicks, maxClicks, startAt, points, true);
			}
			
			g.drawImage(new BufferedImage(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT, BufferedImage.TYPE_INT_ARGB),
					0, 0, Color.black, null);
			g.drawImage(rotateImageXZ(snapshot, Math.min(degree += SchiffeVersenken.getInstance().getDeltaTime() * 40,
					89.9F)), 0, 0, null);
			g.drawImage(rotateImageXZ(newScreen, 90 - degree), (int) (snapshot.getWidth() *
					Math.cos(Math.toRadians(degree))), 0, null);
			
			if (degree <= 90)
				return animState;
			
			animState = -1;
			degree = 0;
			snapshot = newScreen = null;
		}
		
		return animState;
	}
	
	private class MotivationField extends JFrame
	{
		private static final long serialVersionUID = -8945521007789768891L;
		
		public MotivationField()
		{
			super("Just DO IT!");
			super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			new Thread(() ->
			{
				try
				{
					Thread.sleep(2000L);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				while (isVisible())
				{
					SchiffeVersenken.pad(new File("./audio/etc/", "jdi.wav"), 6);
					
					try
					{
						Thread.sleep(5100L);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}).start();
			
			try
			{
				add(new JLabel(new ImageIcon(new File("./img/etc/", "shia.gif").toURI().toURL())));
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			
			pack();
		}
		
		@Override
		public void dispose()
		{
			GameField.this.motivationField = null;
			super.dispose();
		}
	}
	
	private MotivationField motivationField;
	
	public boolean hit(int tileX, int tileY)
	{
		if (players == 2 && SchiffeVersenken.getInstance().getMenu() instanceof Multiplayer && SchiffeVersenken.isMLG()
				&& (motivationField == null || !motivationField.isVisible()) &&
				((Multiplayer) SchiffeVersenken.getInstance().getMenu()).points[1] > ((Multiplayer)
						SchiffeVersenken.getInstance().getMenu()).points[0])
		{
			motivationField = new MotivationField();
			motivationField.setVisible(true);
		}
		
		if (invalidated)
			return false;
		
		if (renderTensor[turnedPlayer][tileX][tileY] != -1)
			return false;
		
		byte type = gameTensor[turnedPlayer][tileX][tileY];
		
		if (type == 0 || type == 1)
		{
			renderTensor[turnedPlayer][tileX][tileY] = 0;
			return true;
		}
		
		if (type > 1 && type < 6)
		{
			for (int y = tileY + 1; y <= 9; y++) // Check all lower
			{
				if (gameTensor[turnedPlayer][tileX][y] == 0 || gameTensor[turnedPlayer][tileX][y] == 1)
					break; //#AllHasBeenSeen
				
				if (renderTensor[turnedPlayer][tileX][y] != -1)
					continue; // Been hit
				
				renderTensor[turnedPlayer][tileX][tileY] = 1; // Not all been hit -> declared as a hit
				
				return false; // #Break
			}
			
			renderTensor[turnedPlayer][tileX][tileY] = gameTensor[turnedPlayer][tileX][tileY]; // One of the easy ways: Ship type was hit last!
			toHitShips[turnedPlayer][renderTensor[turnedPlayer][tileX][tileY] - 2]--;
			
			return false;
		}
		
		if (type > 5 && type < 10)
		{
			for (int x = tileX + 1; x <= 9; x++) // Check all lower
			{
				if (gameTensor[turnedPlayer][x][tileY] == 0 || gameTensor[turnedPlayer][x][tileY] == 1)
					break; //#AllHasBeenSeen
				
				if (renderTensor[turnedPlayer][x][tileY] != -1)
					continue; // Been hit
				
				renderTensor[turnedPlayer][tileX][tileY] = 1; // Not all been hit -> declared as a hitturnedPlayer = turnedPlayer + 1 == players ? 0 : turnedPlayer++;
				
				return false; // #Break
			}
			
			renderTensor[turnedPlayer][tileX][tileY] = gameTensor[turnedPlayer][tileX][tileY]; // One of the easy ways: Ship type was hit last!
			toHitShips[turnedPlayer][renderTensor[turnedPlayer][tileX][tileY] - 6]--;
			
			return false;
		}
		
		if (tileY != 9 && gameTensor[turnedPlayer][tileX][tileY + 1] == 10 || tileY != 0 && gameTensor[turnedPlayer][tileX][tileY - 1] >= 2)
		{
			for (int y = tileY + 1; y < 10; y++)
			{
				if (gameTensor[turnedPlayer][tileX][y] == 1)
					break; // So must be water or 1
				
				if (renderTensor[turnedPlayer][tileX][y] == 1)
					continue; // So was hit before -> move to next
				
				renderTensor[turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
				
				return false;
			}
			
			for (int y = tileY - 1; y >= 0; y--)
			{
				if (gameTensor[turnedPlayer][tileX][y] == 1)
					break;
				
				if (renderTensor[turnedPlayer][tileX][y] == 1)
					continue;
				
				renderTensor[turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
				
				return false;
			}
			
			renderTensor[turnedPlayer][tileX][tileY] = 1;
			
			for (int y = tileY - 1; y >= 0; y--)
			{
				if (y != 0 && gameTensor[turnedPlayer][tileX][y - 1] != 1)
					continue;
				
				renderTensor[turnedPlayer][tileX][y] = gameTensor[turnedPlayer][tileX][y];
				toHitShips[turnedPlayer][renderTensor[turnedPlayer][tileX][y] - 2]--;
				
				return false;
			}
			
			return false;
		}
		
		for (int x = tileX + 1; x < 10; x++)
		{
			if (gameTensor[turnedPlayer][x][tileY] == 1)
				break; // So must be water or 1
			
			if (renderTensor[turnedPlayer][x][tileY] == 1)
				continue; // So was hit before -> move to next
			
			renderTensor[turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
			
			return false;
		}
		
		for (int x = tileX - 1; x >= 0; x--)
		{
			if (gameTensor[turnedPlayer][x][tileY] == 1)
				break;
			
			if (renderTensor[turnedPlayer][x][tileY] == 1)
				continue;
			
			renderTensor[turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
			
			return false;
		}
		
		renderTensor[turnedPlayer][tileX][tileY] = 1;
		
		for (int x = tileX - 1; x >= 0; x--)
		{
			if (x != 0 && gameTensor[turnedPlayer][x - 1][tileY] != 1)
				continue;
			
			renderTensor[turnedPlayer][x][tileY] = gameTensor[turnedPlayer][x][tileY];
			toHitShips[turnedPlayer][renderTensor[turnedPlayer][x][tileY] - 6]--;
			
			return false;
		}
		
		return false;
	}
	
	public boolean isStarted()
	{
		return started;
	}
	
	public void start()
	{
		started = true;
	}
	
	public boolean wasHit(int x, int y)
	{
		return renderTensor[turnedPlayer][x][y] != -1;
	}
	
	public boolean isInvalidated()
	{
		return invalidated;
	}
	
	public void setBot(BotAI bot)
	{
		this.bot = bot;
		playerNames[1] = bot.getName();
	}
	
	public String[] getPlayerNames()
	{
		return playerNames;
	}
	
	public void setShipsVisible(byte player)
	{
		for (int x = 0; x < 10; x++)
		{
			for (int y = 0; y < 10; y++)
			{
				if (gameTensor[player][x][y] == 10)
					renderTensor[player][x][y] = 1;
				
				else if (gameTensor[player][x][y] < 2)
					continue;
				
				else renderTensor[player][x][y] = gameTensor[player][x][y];
			}
		}
	}
	
	public final BufferedImage getScreenshot(int[] clicksNeeded, int maxClicks, long[] startAt, int points[],
			boolean bott)
	{
		BufferedImage img = new BufferedImage(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT,
				BufferedImage.TYPE_INT_ARGB);
		
		render((Graphics2D) img.getGraphics(), clicksNeeded, maxClicks, startAt, points, bott);
		
		return img;
	}
	
	/**
	 * If bot: bot always 1!
	 */
	public byte getCurrentPlayer()
	{
		return turnedPlayer;
	}
	
	public byte getNextPlayer()
	{
		return (byte) (turnedPlayer + 1 == players ? 0 : turnedPlayer + 1);
	}
	
	public void appendUserName(String name)
	{
		for (int i = 0; i < players; i++)
		{
			if (playerNames[i] != null)
				continue;
			
			playerNames[i] = name;
			return;
		}
	}
}