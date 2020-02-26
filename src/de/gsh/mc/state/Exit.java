package de.gsh.mc.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.Menu;

public class Exit extends Menu implements MouseListener, LangReloadListener
{
	private BufferedImage background; // Hintergrund-Bild
	private Font headFont = new Font("arial", Font.BOLD, 50); // Font für die Überschrift
	private Font buttonFont = new Font("arial", Font.PLAIN, 30); // Font für die Knöpfe
	
	// I18n
	private String yes;
	private String no;
	private String header;
	
	public Exit(SchiffeVersenken game)
	{
		super(game); // Game-Instanz übergeben
		
		onLangReload();
		I18n.addLRL(this);
	}
	
	@Override
	public void onLangReload()
	{
		yes = I18n.a("exit.yes");
		no = I18n.a("exit.no");
		header = I18n.a("exit.header");
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
			
			g.drawString("NOH YA WONT KWIET!!!!", getCenter(g, "NOH YA WONT KWIET!!!!"), 300);
			return;
		}
		
		g.drawImage(background, 0, 0, null); // Hintergrund an der stelle (0|0) malen
		g.setFont(headFont); // Font auf den Head-Font setzen
		g.setColor(new Color(100, 100, 255)); // Farbe auf ein hellblau setzen
		g.drawString(header, 30, 80); // Wirklich beenden? an der stelle (30|80) malen
		g.setColor(Color.WHITE); // Farbe auf weiß ändern
		g.drawRect(30, 150, 250, 50); // Button kasten 1 malen
		g.drawRect(30, 220, 250, 50); // Button kasten 2 malen
		g.setColor(Color.BLACK); // Farbe auf schwarz ändern
		g.setFont(buttonFont); // Font auf den Button-Font setzen
		g.drawString(no, 70, 185); // Nein zeichnen
		g.drawString(yes, 70, 255); // Ja zeichnen
	}

	@Override public void tick() {} // Keine Tick-Methode benötigt!

	@Override
	public void initialize() throws Exception
	{
		background = scaleImage(ImageIO.read(new File("./img/", "mmbg.jpg")), SchiffeVersenken.WIDTH + 10, SchiffeVersenken.HEIGHT + 10); // Bild skalieren und laden
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getX() < 30 || e.getX() > 280 || e.getY() < 150 || e.getY() > 270) // Dort ist kein Button!
			return;
		
		if (e.getY() <= 200)
		{
			game.transitToState(new MainMenu(game)); // Nein wurde geklickt -> Zum MainMenu wechseln!
			return;
		}
		
		if (e.getY() <= 220) // Ab dort ist kein Button mehr
			return;
		
		System.exit(0); // Beenden
	}

	// Force-Implemented methods
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}

	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { MenuExit }";
	}
}