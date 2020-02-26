package de.gsh.mc.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.I18n;
import de.gsh.mc.Menu;
import de.gsh.mc.PlayerProfile;

import static de.gsh.mc.I18n.a;

public class Stats extends Menu implements MouseListener, LangReloadListener
{
	private final Font nameFont = new Font("arial", Font.PLAIN, 30);
	private final Font headerFont = new Font("arial", Font.BOLD, 40);

	private String[] profiles;
	
	private boolean selectPlayer;
	private int page;
	private PlayerProfile displayingPlayer;
	private String date;
	
	private String profileHeader, wins, topScore, games, looses, lowestScore, minClicks, minTimeNeeded, notPlayed,
		neverWon, restore, mainMenu, back, header, sPage, prevPage, nextPage;
	
	public Stats(SchiffeVersenken game)
	{
		super(game);
		
		onLangReload();
		I18n.addLRL(this);
	}
	
	@Override
	public void onLangReload()
	{
		if (!SchiffeVersenken.isMLG())
		{
			profileHeader = a("stats.profile.header");
			header = a("stats.select.header");
			sPage = a("stats.select.page");
			prevPage = a("stats.select.prevPage");
			nextPage = a("stats.select.nextPage");
			wins = a("stats.profile.wins");
			topScore = a("stats.profile.topScore");
			games = a("stats.profile.games");
			looses = a("stats.profile.looses");
			lowestScore = a("stats.profile.lowestScore");
			minClicks = a("stats.profile.minClicks");
			minTimeNeeded = a("stats.profile.minTimeNeeded");
			notPlayed = a("stats.profile.notPlayed");
			neverWon = a("stats.profile.neverWon");
			restore = a("stats.profile.restore");
			mainMenu = a("stats.profile.mainMenu");
			back = a("stats.profile.back");
			return;
		}
		
		profileHeader = "M8: ";
		header = "CHOZE'N'M8!";
		sPage = "PAZE ";
		prevPage = "PREVBRAH";
		nextPage = "NIEXT";
		wins = "eZ REKS";
		games = "ez SCOPES";
		looses = "eZ GEREKS";
		topScore = "eZ MAX REK";
		lowestScore = "LOWBOB";
		minClicks = "MIN NOSCOPES";
		minTimeNeeded = "SANIC FAST!";
		notPlayed = neverWon = "eZ NEVER MLGD";
		restore = "eZ NAH KLIECK";
		mainMenu = "eZ MLGMenu";
		back = "eZ BAHG";
	}

	@Override
	public void render(Graphics2D g)
	{
		if (selectPlayer)
		{
			renderSelect(g);
			return;
		}
		
		if (displayingPlayer == null)
		{
			selectPlayer = true;
			return;
		}
		
		g.setFont(headerFont);
		g.setColor(Color.RED);
		g.drawString(profileHeader + displayingPlayer.name, getCenter(g, profileHeader + displayingPlayer.name), 60);
		
		g.setColor(Color.WHITE);
		g.setFont(nameFont);
		
		g.drawRect(180, 100, 250, 100);
		g.drawString(wins, 200, 140);
		g.drawString("" + displayingPlayer.wins, 200, 180);
		
		g.drawRect(180, 200, 250, 100);
		g.drawString(topScore, 200, 240);
		g.drawString("" + (displayingPlayer.topScore == Integer.MIN_VALUE ? notPlayed : displayingPlayer.topScore),
				200, 280);
		
		g.drawRect(180, 300, 250, 100);
		g.drawString(games, 200, 340);
		g.drawString("" + displayingPlayer.games, 200, 380);
		
		g.drawRect(430, 100, 250, 100);
		g.drawString(looses, 450, 140);
		g.drawString("" + displayingPlayer.looses, 450, 180);
		
		g.drawRect(430, 200, 250, 100);
		g.drawString(lowestScore, 450, 240);
		g.drawString("" + (displayingPlayer.lowScore == Integer.MAX_VALUE ? notPlayed : displayingPlayer.lowScore),
				450, 280);
		
		g.drawRect(430, 300, 250, 100);
		g.drawString(minClicks, 450, 340);
		g.drawString("" + (displayingPlayer.clicksToMax == Float.MAX_VALUE ? neverWon :
			displayingPlayer.clicksToMax * 100), 450, 380);
		
		g.drawRect(180, 400, 500, 100);
		g.drawString(minTimeNeeded, 200, 440);
		g.drawString("" + (displayingPlayer.minTimeUsed == Long.MAX_VALUE ? neverWon : date), 200, 480);
		
		g.drawRect(180, 500, 250, 50);
		g.drawString(restore, 200, 535);
		
		g.drawRect(430, 500, 250, 50); // Restore profile button
		g.drawString(mainMenu, 450, 535);
		
		g.drawRect(180, 550, 500, 50);
		g.drawString(back, getCenter(g, back), 585);
	}
	
	// 280, 100 + 80 * i, 250, 50
	private void renderSelect(Graphics2D g)
	{
		g.setFont(headerFont);
		g.setColor(Color.RED);
		g.drawString(header, getCenter(g, header), 50);
		
		g.setColor(Color.WHITE);
		g.setFont(nameFont);
		g.drawString(sPage + (page + 1), 10, 30);
		
		int length = page != profiles.length / 19 ? 19 : profiles.length - 19 * page;
		
		if (page != 0)
		{
			g.drawRect(30, 560, 250, 50);
			g.drawString(prevPage, 50, 595);
		}
		
		if (((length - 1) / 7 - 1) % 2 == 0)
		{
			for (int i = 0; i < Math.min(length / 2 + length % 2, 7); i++)
			{
				g.drawRect(130, 80 + 80 * i, 250, 50);
				g.drawString(profiles[page * 19 + i], 150, 115 + 80 * i);
			}
			
			for (int i = 0; i < Math.min(length / 2, 7); i++)
			{
				g.drawRect(420, 80 + 80 * i, 250, 50);
				g.drawString(profiles[page * 19 + i + length / 2 + length % 2], 450, 115 + 80 * i);
			}
			
			return;
		}
		
		if (length <= 7)
		{
			for (int i = 0; i < Math.min(length, 7); i++)
			{
				g.drawRect(280, 80 + 80 * i, 250, 50);
				g.drawString(profiles[page * 19 + i], 300, 115 + 80 * i);
			}
			
			return;
		}
		
		for (int i = 0; i < Math.min(length / 3, 7); i++)
		{
			g.drawRect(30, 80 + 80 * i, 250, 50);
			g.drawString(profiles[page * 19 + i], 60, 115 + 80 * i);
		}
		
		for (int i = 0; i < Math.min(length / 3 + (length % 3 != 0 ? 1 : 0), 7); i++) // Mid one
		{
			g.drawRect(310, 80 + 80 * i, 250, 50);
			g.drawString(profiles[page * 19 + i + length / 3], 330, 115 + 80 * i);
		}
		
		for (int i = 0; i < Math.min(length / 3, 7); i++)
		{
			g.drawRect(590, 80 + 80 * i, 250, 50);
			g.drawString(profiles[page * 19 + i + length * 2/3 + (length % 3 != 0 ? 1 : 0)], 610, 115 + 80 * i);
		}
		
		if (profiles.length <= 19)
			return;
		
		if (page == profiles.length / 19)
			return;
		
		g.drawRect(590, 560, 250, 50);
		g.drawString(nextPage, 610, 595);
	}

	@Override public void tick() {}

	@Override
	public void initialize()
	{
		selectPlayer = true;
		
		File f = new File("./profile/");
		profiles = f.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".prof");
			}
		});
		
		for (int i = 0; i < profiles.length; i++)
			profiles[i] = profiles[i].replace(".prof", "");
	}
	
	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { MenuStats }";
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (displayingPlayer != null)
		{
			/*
			 * 180, 500, 250, 100
			 * 430, 500, 250, 100
			 */
			
			if (e.getX() < 180 || e.getX() > 680 || e.getY() < 500 || e.getY() > 600)
				return;
			
			if (e.getY() >= 550)
			{
				displayingPlayer = null;
				selectPlayer = true;
				return;
			}
			
			if (e.getX() < 430)
			{
				displayingPlayer.restoreDefaults();
				game.transitToState(new MainMenu(game));
				return;
			}
			
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if (!selectPlayer)
			return;
		
		if (page != 0 && e.getX() >= 30 && e.getX() <= 280 && e.getY() >= 560 && e.getY() <= 610)
		{
			page--;
			return;
		}
		
		if (page != profiles.length / 19 && e.getX() >= 590 && e.getX() <= 840 && e.getY() >= 560 && e.getY() <= 610)
		{
			page++;
			return;
		}
		
		if (e.getY() >= 610 || e.getY() <= 80 || e.getX() < 30 || e.getX() > 640)
			return;
		
		int length = page != profiles.length / 19 ? 19 : profiles.length - 19 * page;
		
		if (length <= 7)
		{
			// Must be 7 in this...
			if (e.getX() <= 280 || e.getX() >= 530)
				return;
			
			// 80 + 80 * i, 50 -> There are the rectangles!
			
			for (int i = 0; i < length; i++)
			{
				if (e.getY() < 80 + 80 * i)
					return;
				
				if (e.getY() > 130 + 80 * i)
					continue;
				
				displayingPlayer = new PlayerProfile(profiles[page * 19 + i]);
				selectPlayer = false;
				date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
				
				return;
			}
			
			return;
		}
		
		if (length <= 14)
		{
			if (e.getX() < 130 || e.getX() > 670)
				return;
			
			if (e.getX() <= 380)
			{
				for (int i = 0; i < Math.min(length / 2 + length % 2, 7); i++)
				{
					if (e.getY() < 80 + 80 * i || e.getY() > 130 + 80 * i)
						continue;
					
					displayingPlayer = new PlayerProfile(profiles[page * 19 + i]);
					selectPlayer = false;
					date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
					
					return;
				}
				
				return;
			}
			
			if (e.getX() < 420)
				return;
			
			for (int i = 0; i < Math.min(length / 2, 7); i++)
			{
				if (e.getY() < 80 + 80 * i || e.getY() > 130 + 80 * i)
					continue;
				
				displayingPlayer = new PlayerProfile(profiles[page * 19 + i + length / 2 + length % 2]);
				selectPlayer = false;
				date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
				
				return;
			}
			
			return;
		}
		
		if (e.getX() <= 280)
		{
			for (int i = 0; i < Math.min(length / 3, 7); i++)
			{
				if (e.getY() < 80 + 80 * i || e.getY() > 130 + 80 * i)
					continue;
				
				displayingPlayer = new PlayerProfile(profiles[page * 19 + i]);
				selectPlayer = false;
				date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
				
				return;
			}
			
			return;
		}
		
		if (e.getX() < 310)
			return;
		
		if (e.getX() <= 560)
		{
			for (int i = 0; i < Math.min(length / 3 + (length % 3 != 0 ? 1 : 0), 7); i++)
			{
				if (e.getY() < 80 + 80 * i || e.getY() > 130 + 80 * i)
					continue;
				
				displayingPlayer = new PlayerProfile(profiles[page * 19 + i + length / 3]);
				selectPlayer = false;
				date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
				
				return;
			}
			
			return;
		}
		
		if (e.getX() < 590)
			return;
		
		for (int i = 0; i < Math.min(length / 3, 7); i++)
		{
			if (e.getY() < 80 + 80 * i || e.getY() > 130 + 80 * i)
				continue;
			
			displayingPlayer = new PlayerProfile(profiles[page * 19 + i + length * 2/3 + (length % 3 != 0 ? 1 : 0)]);
			selectPlayer = false;
			date = new SimpleDateFormat("mm:ss").format(new Date(displayingPlayer.minTimeUsed));
			
			return;
		}
	}
	
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}