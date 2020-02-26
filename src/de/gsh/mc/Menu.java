package de.gsh.mc;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public abstract class Menu
{
	protected final SchiffeVersenken game; // Spielinstanz
	protected static final BufferedImage RED_CURSOR; // Roter Cursor
	protected static final BufferedImage BLUE_CURSOR; // Blauer Cursor
	protected static final BufferedImage HELLRED_CURSOR; // Höllenroter Cursor
	
	public Menu(SchiffeVersenken game)
	{
		this.game = game; // Spielinstanz setzen
		
		try
		{
			initialize(); // Initiieren, durch einen Java-Bug muss dass hier aufgerufen werden!
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public abstract void render(Graphics2D g); // Methode zum Ausrendern des Menüs
	
	public abstract void tick(); // Methode für die berechnungen des Menüs
	
	public abstract void initialize() throws Exception; // Initialisierung des Menüs
	
	protected final void drawCursorToScreen(Graphics2D g, int color)
	{
		Point locAt = game.getFrame().getLocation(); // Fenster-Location bestimmen
		Point mouse = MouseInfo.getPointerInfo().getLocation(); // Maus-Location bestimmen
		
		g.drawImage(color == 0 ? RED_CURSOR : BLUE_CURSOR, mouse.x - locAt.x - 18, mouse.y - locAt.y - 40, null); // Cursor an der Stelle zeichnen
	}
	
	public static BufferedImage scaleImage(BufferedImage in, int newWidth, int newHeight)
	{
		BufferedImage then = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB); // Skaliertes leeres Bild zeichnen
		AffineTransform at = new AffineTransform(); // Neues AffineTransform erstellen
		at.scale(((double) newWidth) / in.getWidth(), ((double) newHeight) / in.getHeight()); // Bild skalieren lassen
		return new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(in, then); // Das Bild herausfiltern
	}
	
	public static final int getCenter(Graphics g, String text)
	{
		return (int) ((SchiffeVersenken.WIDTH - g.getFontMetrics().getStringBounds(text, g).getWidth()) / 2);
	}
	
	public abstract String toString();
	
	static
	{
		RED_CURSOR = SchiffeVersenken.getTexSheet().getImage(0, 0, 1, 1); // Roten Cursor laden
		BLUE_CURSOR = SchiffeVersenken.getTexSheet().getImage(1, 0, 1, 1); // Blauen Cursor laden
		HELLRED_CURSOR = SchiffeVersenken.getTexSheet().getImage(2, 0, 1, 1); // Höllenroten Cursor laden
	}
}