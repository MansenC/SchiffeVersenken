package de.gsh.mc.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.GameField;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.SpriteSheet;
import de.gsh.mc.Menu;

/**
 * Das GameField ist die Klassen-Instanz für die grafische Darstellung des Spielfeldes.
 */
public class FlippedGameField extends GameField implements LangReloadListener
{
	public static final int START_RASTER_X = 178;
	public static final int START_RASTER_Y = 68;
	public static final int TILE_DIM = 56;
	
	public FlippedGameField(byte[][][] gameField, byte startingPlayer, byte[] hitShips)
	{
		super(gameField, startingPlayer, hitShips);
	}

	public void render(Graphics2D g, int[] clicksNeeded, int maxClicks, long[] startAt, int points[],
			boolean bott)
	{
		g.drawImage(background, 0, 0, null);
		
		g.setFont(textFont);
		g.setColor(Color.GRAY);
		g.fillRect(10, 10, 100, 40);
		g.setColor(Color.WHITE);
		g.drawString(mainMenu, 25, 35);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(10, 10, 100, 40);
		
		g.setColor(Color.WHITE);
		g.fillRect(10, 60, 100, 320);
		
		g.setColor(Color.BLACK);
		g.drawRect(10, 60, 100, 320);
		
		for (int y = 0; y < 2; y++)
		{
			g.drawString(player + (y + 1) + ":", 12, 160 * (y % 2) + 75);
			g.drawLine(10, 160 * (y % 2) + 80, 110, 160 * (y % 2) + 80);
			g.setColor(y == 1 - turnedPlayer ? Color.RED : Color.BLACK);
			g.drawString(playerNames[y], 12, 160 * (y % 2) + 95);
			g.setColor(Color.BLACK);
			g.drawLine(10, 160 * (y % 2) + 100, 110, 160 * (y % 2) + 100);
			g.drawString(clicks, 12, 160 * (y % 2) + 115);
			g.drawLine(10, 160 * (y % 2) + 120, 110, 160 * (y % 2) + 120);
			g.setColor(clicksNeeded[1 - y] < (maxClicks / 4 * 3) ? Color.black : (clicksNeeded[1 - y] <
					(maxClicks / 16 * 15) ? Color.ORANGE : Color.RED));
			g.drawString("" + clicksNeeded[1 - y], 12, 160 * (y % 2) + 135);
			g.setColor(Color.BLACK);
			g.drawString("|", 55, 160 * (y % 2) + 135);
			g.setColor(Color.RED);
			g.drawString("    " + maxClicks, 52, 160 * (y % 2) + 135);
			g.setColor(Color.BLACK);
			g.drawLine(10, 160 * (y % 2) + 140, 110, 160 * (y % 2) + 140);
			g.drawString(time, 12, 160 * (y % 2) + 155);
			g.drawLine(10, 160 * (y % 2) + 160, 110, 160 * (y % 2) + 160);
			g.drawString(started ? DATE_FORMAT.format(new Date(startAt[1 - y]))
					: "00:00", 12, 160 * (y % 2) + 175);
			g.drawLine(10, 160 * (y % 2) + 180, 110, 160 * (y % 2) + 180);
			g.drawString(this.points, 12, 160 * (y % 2) + 195);
			g.drawLine(10, 160 * (y % 2) + 200, 110, 160 * (y % 2) + 200);
			g.drawString("" + points[1 - y], 12, 160 * (y % 2) + 215);
			g.drawLine(10, 160 * (y % 2) + 219, 110, 160 * (y % 2) + 219);
			g.drawLine(10, 160 * (y % 2) + 220, 110, 160 * (y % 2) + 220);
			g.drawLine(10, 160 * (y % 2) + 221, 110, 160 * (y % 2) + 221);
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
				
				if (renderTensor[1 - turnedPlayer][tileX][tileY] >= 2)
				{
					g.drawImage(shipImages[renderTensor[1 - turnedPlayer][tileX][tileY] - 2], curX, curY, null);
					g.drawImage(hit, curX, curY, null);
					continue;
				}
				
				if (renderTensor[1 - turnedPlayer][tileX][tileY] == 0)
				{
					g.drawImage(water, curX, curY, null);
					continue;
				}
				
				if (renderTensor[1 - turnedPlayer][tileX][tileY] != 1)
					continue;
				
				g.drawImage(hit, curX, curY, null);
			}
		}
		
		if (!started)
		{
			g.setFont(headFont);
			g.setColor(Color.BLUE);
			String msg = String.format(playerStarts, playerNames[1 - turnedPlayer]);
			g.drawString(msg, Menu.getCenter(g, msg), 240);
			g.drawString(start, Menu.getCenter(g, start), 295);
		}
	}
	
	public boolean tick()
	{
		for (int i : toHitShips[1 - turnedPlayer])
		{
			if (i != 0)
				continue;
			
			return false;
		}
		
		for (int i : toHitShips[1 - turnedPlayer])
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
		playerNames = new String[2];
		playerNames[0] = SchiffeVersenken.getInstance().getUserName();
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
				turnedPlayer = (byte) (turnedPlayer == 1 ? 0 : 1);
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
	
	public boolean hit(int tileX, int tileY)
	{
		if (renderTensor[1 - turnedPlayer][tileX][tileY] != -1)
			return false;
		
		byte type = gameTensor[1 - turnedPlayer][tileX][tileY];
		
		if (type == 0 || type == 1)
		{
			renderTensor[1 - turnedPlayer][tileX][tileY] = 0;
			return true;
		}
		
		if (type > 1 && type < 6)
		{
			for (int y = tileY + 1; y <= 9; y++) // Check all lower
			{
				if (gameTensor[1 - turnedPlayer][tileX][y] == 0 || gameTensor[1 - turnedPlayer][tileX][y] == 1)
					break; //#AllHasBeenSeen
				
				if (renderTensor[1 - turnedPlayer][tileX][y] != -1)
					continue; // Been hit
				
				renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // Not all been hit -> declared as a hit
				
				return false; // #Break
			}
			
			renderTensor[1 - turnedPlayer][tileX][tileY] = gameTensor[1 - turnedPlayer][tileX][tileY]; // One of the easy ways: Ship type was hit last!
			toHitShips[1 - turnedPlayer][renderTensor[1 - turnedPlayer][tileX][tileY] - 2]--;
			
			return false;
		}
		
		if (type > 5 && type < 10)
		{
			for (int x = tileX + 1; x <= 9; x++) // Check all lower
			{
				if (gameTensor[1 - turnedPlayer][x][tileY] == 0 || gameTensor[1 - turnedPlayer][x][tileY] == 1)
					break; //#AllHasBeenSeen
				
				if (renderTensor[1 - turnedPlayer][x][tileY] != -1)
					continue; // Been hit
				
				renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // Not all been hit -> declared as a hitturnedPlayer = turnedPlayer + 1 == players ? 0 : turnedPlayer++;
				
				return false; // #Break
			}
			
			renderTensor[1 - turnedPlayer][tileX][tileY] = gameTensor[1 - turnedPlayer][tileX][tileY]; // One of the easy ways: Ship type was hit last!
			toHitShips[1 - turnedPlayer][renderTensor[1 - turnedPlayer][tileX][tileY] - 6]--;
			
			return false;
		}
		
		if (tileY != 9 && gameTensor[1 - turnedPlayer][tileX][tileY + 1] == 10 || tileY != 0 && gameTensor[1 - turnedPlayer][tileX][tileY - 1] >= 2)
		{
			for (int y = tileY + 1; y < 10; y++)
			{
				if (gameTensor[1 - turnedPlayer][tileX][y] == 1)
					break; // So must be water or 1
				
				if (renderTensor[1 - turnedPlayer][tileX][y] == 1)
					continue; // So was hit before -> move to next
				
				renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
				
				return false;
			}
			
			for (int y = tileY - 1; y >= 0; y--)
			{
				if (gameTensor[1 - turnedPlayer][tileX][y] == 1)
					break;
				
				if (renderTensor[1 - turnedPlayer][tileX][y] == 1)
					continue;
				
				renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
				
				return false;
			}
			
			renderTensor[1 - turnedPlayer][tileX][tileY] = 1;
			
			for (int y = tileY - 1; y >= 0; y--)
			{
				if (y != 0 && gameTensor[1 - turnedPlayer][tileX][y - 1] != 1)
					continue;
				
				renderTensor[1 - turnedPlayer][tileX][y] = gameTensor[1 - turnedPlayer][tileX][y];
				toHitShips[1 - turnedPlayer][renderTensor[1 - turnedPlayer][tileX][y] - 2]--;
				
				return false;
			}
			
			return false;
		}
		
		for (int x = tileX + 1; x < 10; x++)
		{
			if (gameTensor[1 - turnedPlayer][x][tileY] == 1)
				break; // So must be water or 1
			
			if (renderTensor[1 - turnedPlayer][x][tileY] == 1)
				continue; // So was hit before -> move to next
			
			renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
			
			return false;
		}
		
		for (int x = tileX - 1; x >= 0; x--)
		{
			if (gameTensor[1 - turnedPlayer][x][tileY] == 1)
				break;
			
			if (renderTensor[1 - turnedPlayer][x][tileY] == 1)
				continue;
			
			renderTensor[1 - turnedPlayer][tileX][tileY] = 1; // So not every single one was set!
			
			return false;
		}
		
		renderTensor[1 - turnedPlayer][tileX][tileY] = 1;
		
		for (int x = tileX - 1; x >= 0; x--)
		{
			if (x != 0 && gameTensor[1 - turnedPlayer][x - 1][tileY] != 1)
				continue;
			
			renderTensor[1 - turnedPlayer][x][tileY] = gameTensor[1 - turnedPlayer][x][tileY];
			toHitShips[1 - turnedPlayer][renderTensor[1 - turnedPlayer][x][tileY] - 6]--;
			
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
		return renderTensor[1 - turnedPlayer][x][y] != -1;
	}
	
	public String[] getPlayerNames()
	{
		String[] pn = new String[2];
		pn[0] = playerNames[1];
		pn[1] = playerNames[0];
		
		return pn;
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
	
	/**
	 * If bot: bot always 1!
	 */
	public byte getCurrentPlayer()
	{
		return turnedPlayer;
	}
	
	public byte getNextPlayer()
	{
		return (byte) (turnedPlayer + 1 == 2 ? 0 : 1);
	}
	
	public byte[][][] getGameTensor()
	{
		byte[][][] cop = new byte[2][10][10];
		
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 10; j++)
				for (int k = 0; k < 10; k++)
					cop[1 - i][j][k] = gameTensor[i][j][k];
		
		return cop;
	}
	
	public byte[][][] getRenderTensor()
	{
		byte[][][] cop = new byte[2][10][10];
		
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 10; j++)
				for (int k = 0; k < 10; k++)
					cop[1 - i][j][k] = renderTensor[i][j][k];
		
		return cop;
	}
}