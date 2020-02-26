package de.gsh.mc.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.GameField;
import de.gsh.mc.GameOptions;
import de.gsh.mc.I18n;
import de.gsh.mc.Menu;
import de.gsh.mc.PlayerProfile;

import static de.gsh.mc.GameField.*;

/*
 * FIXME Bleeding - initializing objects in constructor or non-constant in class makes them be null on initialize-call!
 * Probably a garbage-collector bug, removing the object from the memory thinking it's not used ._.
 * #FixThisPlsJavaFudgeWhyUDuTisToMeMehMakesLifeHarderSoWhy
 */
public class Singleplayer extends Menu implements MouseListener, LangReloadListener, MouseMotionListener
{	
	private final Font wonFont = new Font("arial", Font.BOLD, 60);
	
	private GameField field; // Why u not initialize - so much not wow of ya, not so nice maen
	private int clicksNeeded;
	private int maxClicks;
	private long startAt;
	private boolean won;
	private boolean lost;
	private int points = 0;
	private PlayerProfile profile;
	
	private String wonTxt;
	private String lostTxt;
	
	private BufferedImage snipar;
	private int mouseX, mouseY;
	
	public Singleplayer(SchiffeVersenken game)
	{
		super(game);
		
		onLangReload();
		I18n.addLRL(this);
		profile = game.getPlayerProfile();
	}
	
	@Override
	public void onLangReload()
	{
		if (!SchiffeVersenken.isMLG())
		{
			wonTxt = I18n.a("singleplayer.won");
			lostTxt = I18n.a("singleplayer.lost");
			return;
		}
		
		wonTxt = "eZZ M8! NAICE SKILZ!";
		lostTxt = "BE MAD BRAH!!";
	}
	
	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { Singleplayer: field = " + field
				+ ", clicksNeeded = " + clicksNeeded + ", startAt = " + startAt + ", won = " + won 
				+ ", lost = " + lost + ", points = " + points + " }";
	}

	@Override
	public void render(Graphics2D g)
	{
		field.render(g, new int[] { clicksNeeded }, maxClicks, new long[] { won || lost ? startAt :
			System.currentTimeMillis() - startAt }, new int[] { points }, false);
		
		if (SchiffeVersenken.isMLG())
			g.drawImage(snipar, mouseX - 150, mouseY - 200, null);
		
		if (lost)
		{
			g.setFont(wonFont);
			g.setColor(Color.RED);
			g.drawString(lostTxt, getCenter(g, lostTxt), SchiffeVersenken.HEIGHT / 2);
			
			field.setShipsVisible((byte) 0);
		}
		else if (won)
		{
			g.setFont(wonFont);
			g.setColor(Color.GREEN);
			g.drawString(wonTxt, getCenter(g, wonTxt), SchiffeVersenken.HEIGHT / 2);
		}
		
		drawCursorToScreen(g, 0); // Last
	}

	@Override
	public void tick()
	{
		if (lost || won)
			return;
		
		if (clicksNeeded >= maxClicks)
		{
			System.out.println("Player " + game.getUserName() + " lost the game!");
			lost = true;
			I18n.removeLRL(this);
			I18n.removeLRL(field);
			System.out.println(profile + " : " + profile.looses);
			profile.looses++;
			System.out.println(profile.looses);
			profile.lowScore = Math.min(profile.lowScore, points);
			profile.topScore = Math.max(profile.topScore, points);
			profile.save();
			
			if (SchiffeVersenken.isMLG())
				SchiffeVersenken.pad(new File("./audio/etc/", "sv.wav"));
			
			new Thread(() ->
			{
				try
				{
					Thread.sleep(4000L);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				if (!game.getMenu().equals(this))
					return;
				
				game.transitToState(new MainMenu(game));
			}).start();
			
			return;
		}
		
		if (!field.tick())
			return;
		
		startAt = System.currentTimeMillis() - startAt;
		won = true;
		I18n.removeLRL(this);
		I18n.removeLRL(field);
		profile.wins++;
		profile.lowScore = Math.min(profile.lowScore, points);
		profile.topScore = Math.max(profile.topScore, points);
		profile.minTimeUsed = Math.min(profile.minTimeUsed, startAt);
		profile.clicksToMax = Math.min(profile.clicksToMax, ((float) clicksNeeded) / maxClicks);
		profile.save();
		
		if (SchiffeVersenken.isMLG())
		{
			new Thread(() ->
			{
				try
				{
					for (int i = 0; i < 50; i++)
					{
						SchiffeVersenken.pad(new File("./audio/etc/", "wow.wav"), 2);
						Thread.sleep(new Random().nextInt(100));
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}).start();
		}
		
		System.out.println("Player " + game.getUserName() + " won the game!");
		
		new Thread(() ->
		{
			try
			{
				Thread.sleep(4000L);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			if (!game.getMenu().equals(this))
				return;
			
			game.transitToState(new MainMenu(game));
		}).start();
	}

	@Override
	public void initialize() throws IOException
	{
		System.out.println("Initializing menu singleplayer!");
		
		field = new GameField((byte) 1);
		game.setCursor(SchiffeVersenken.INVISIBLE_CURSOR);
		field.initialize();
		maxClicks = GameOptions.getMaxTries();
		snipar = ImageIO.read(new File("./img/etc/", "sniparCl4n.png"));
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getX() >= 10 && e.getX() <= 110 && e.getY() >= 10 && e.getY() <= 50)
		{
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if (won || lost)
			return;
		
		if (!field.isStarted())
		{
			if (SchiffeVersenken.isMLG())
				SchiffeVersenken.pad(new File("./audio/etc/", "h.wav"));
			
			startAt = System.currentTimeMillis();
			field.start();
			profile.games++;
			
			System.out.println("Singleplayer-game started!");
			
			return;
		}
		
		if (e.getX() < START_RASTER_X || e.getY() < START_RASTER_Y)
			return;
		
		int tileX = (e.getX() - START_RASTER_X) / TILE_DIM;
		int tileY = (e.getY() - START_RASTER_Y) / TILE_DIM;
		
		if (tileX > 9 || tileY > 9)
			return;
		
		System.out.println("Hitting (" + tileX + " | " + tileY + ")");
		
		if (field.getGameTensor()[field.getCurrentPlayer()][tileX][tileY] >= 2
				&& field.getRenderTensor()[field.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points += 200 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
			{
				SchiffeVersenken.playAudio("hit");
				
				if (points - 200 * GameOptions.getRekt() < 9000 && points >= 9000)
					SchiffeVersenken.pad(new File("./audio/etc/", "ont.wav"), 6);
			}
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
		}
		else if (field.getRenderTensor()[field.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points -= 100 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
		}
		
		if (field.hit(tileX, tileY))
			clicksNeeded++;
		
		
	}
	
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}
}