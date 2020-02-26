package de.gsh.mc;

import java.awt.image.BufferedImage;

public class SpriteSheet
{
	private final BufferedImage sprite; // Die Textur mit allen Bildern in sich
	private final int texSize; // Die Größe einer Textur, die im Sprite enthalten ist
	
	public SpriteSheet(BufferedImage sprite, int texSize)
	{
		this.sprite = sprite; // Sprite-Textur setzen
		this.texSize = texSize; // Texturgröße setzen
	}
	
	// 0 - row-count
	public BufferedImage getImage(int row, int column, int rCount, int cCount)
	{
		return sprite.getSubimage(column * texSize, row * texSize, cCount * texSize, rCount * texSize);
	}
}