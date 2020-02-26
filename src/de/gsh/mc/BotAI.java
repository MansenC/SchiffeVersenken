package de.gsh.mc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Die BotAI ist die Superklasse für alle Instanzen eines Bots. Hier ist die Cursor-Animation gegeben, desweiteren
 * wird hier die Tick-Methode für die Logik des Bots bereit gestellt.
 * 
 * @category Bot
 * @since 0.43
 * @version 1.0
 * 
 * @author Manuel Carofiglio
 * 
 * @see BossAI
 * @see RandomAI
 */
public abstract class BotAI
{
	// Logik-Variablen
	private final String name; // Der Name des Bots
	private final BufferedImage cursor; // Das Cursor-Image des Bots
	protected final SchiffeVersenken game; // Die Spielinstanz
	
	// Cursor-Animationsvariablen
	private double lastMouseX = 0; // Letzte Mausposition X
	private double lastMouseY = 0; // Letzte Mausposition Y
	protected int desX; // Zu erreichende Mausposition X
	protected int desY; // Zu erreichende Mausposition Y
	protected int fromX = (int) lastMouseX; // Ausgangsposition X
	protected int fromY = (int) lastMouseY; // Ausgangsposition Y
	
	/**
	 * Erstellt eine neue Instanz des Bots. Die Instanz des Spiels wird für Logik-Operationen benötigt, der Name
	 * als Kennzeichen für das Spielfeld und der Cursor in Form des BufferedImages als Cursor-Textur
	 */
	public BotAI(SchiffeVersenken game, String name, BufferedImage cursor)
	{
		this.game = game; // Spielinstanz setzen
		this.name = name; // Namen setzen
		this.cursor = cursor; // Cursor-Bild setzen
	}
	
	/**
	 * Animiert den Cursor. Die Variablen können nur von erbenden Klassen überschrieben werden.
	 * Die benötigte Zeit für eine Cursor-Bewegung ist fest. Sie variiert nur in sehr geringem Maße und wird
	 * über die Delta-Time des Spiels berechnet.
	 * 
	 * @return Wahr, wenn der Cursor an der richtigen Stelle ist.
	 */
	protected final boolean animateCursor(Graphics2D g)
	{
		if ((fromX >= desX && lastMouseX <= desX && fromY >= desY && lastMouseY <= desY)		// Schauen, ob der
				|| (fromX >= desX && lastMouseX <= desX && fromY <= desY && lastMouseY >= desY)	// Cursor schon an
				|| (fromX <= desX && lastMouseX >= desX && fromY <= desY && lastMouseY >= desY)	// der gewünschten Stelle
				|| (fromX <= desX && lastMouseX >= desX && fromY >= desY && lastMouseY <= desY))// ist
		{
			g.drawImage(cursor, desX, desY, null); // Wenn ja, dann da zeichnen
			return true; // Abbrechen & wahr wiedergeben -> Cursor ist angekommen
		}
		
		g.drawImage(cursor, (int) (lastMouseX = lastMouseX + (desX - fromX) * game.getDeltaTime()),
				(int) (lastMouseY = lastMouseY + (desY - fromY) * game.getDeltaTime()), null); // Cursor zeichnen & lastMouse-Positionen neu setzen
		
		return (fromX >= desX && lastMouseX <= desX && fromY >= desY && lastMouseY <= desY)
				|| (fromX >= desX && lastMouseX <= desX && fromY <= desY && lastMouseY >= desY)
				|| (fromX <= desX && lastMouseX >= desX && fromY <= desY && lastMouseY >= desY)
				|| (fromX <= desX && lastMouseX >= desX && fromY >= desY && lastMouseY <= desY); //Wiedergeben, ob der Cursor nun angekommen ist
	}
	
	/**
	 * Hier wird die Logik des Bots berechnet, also sein derzeitiger Status. Dieser kann z.B. das Cursor-Zeichnen
	 * oder das Berechnen des nächsten Zuges sein.
	 * 
	 * @param g Graphics zum Zeichnen
	 * @param field Das GameField für Logik-berechnungen
	 */
	protected abstract void tick(Graphics2D g, GameField field); // Logik der BotAI berechnen
	
	/**
	 * Gibt den Namen des Bots für die Erkennung im GameField wieder.
	 * 
	 * @return Der Name des Bots
	 */
	public final String getName()
	{
		return name; // Namen wiedergeben -> Anzeige links im Feld
	}
	
	public String toString()
	{
		return super.toString() + " { " + name + " }";
	}
}