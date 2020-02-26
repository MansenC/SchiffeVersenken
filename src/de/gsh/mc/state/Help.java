package de.gsh.mc.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.Menu;

public class Help extends Menu implements LangReloadListener, MouseListener
{
	private String[] lines; // Nur für Merlin
	private String mainMenu;
	private final Font theFont = new Font("arial", Font.PLAIN, 20);
	
	public Help(SchiffeVersenken game)
	{
		super(game);
		
		I18n.addLRL(this);
		onLangReload();
	}
	
	@Override
	public void onLangReload()
	{
		mainMenu = I18n.a("multiplayer.mainMenu"); // Schnorr
		lines = new String[Integer.parseInt(I18n.a("help.lines"))];
		
		for (int i = 0; i < lines.length; i++)
			lines[i] = I18n.a("help.line" + i);
	}

	private boolean is;
	
	@Override
	public void render(Graphics2D g)
	{
		if (SchiffeVersenken.isMLG())
		{
			if (!is)
			{
				is = true;
				
				new Thread(() ->
				{
					try
					{
						Thread.sleep(1500L);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					game.transitToState(new MainMenu(game));
				}).start();
			}
			
			g.drawString("NAH HIELP FOAR YAO!!!!", getCenter(g, "NAH HIELP FOAR YAO!!!!"), 300);
			return;
		}
		
		g.setFont(theFont);
		g.setColor(Color.WHITE);
		
		for (int i = 0; i < lines.length; i++)
			g.drawString(lines[i], getCenter(g, lines[i]), 100 + 30 * i);
		
		g.drawRect(305, 500, 250, 50);
		g.drawString(mainMenu, getCenter(g, mainMenu), 530);
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getX() < 305 || e.getX() > 555 || e.getY() < 500 || e.getY() > 550)
			return;
		
		game.transitToState(new MainMenu(game));
	}

	@Override public void tick() {}
	@Override public void initialize() {}
	
	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { MenuHelp }";
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}