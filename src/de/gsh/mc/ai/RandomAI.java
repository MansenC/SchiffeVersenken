package de.gsh.mc.ai;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import de.gsh.mc.BotAI;
import de.gsh.mc.GameField;
import de.gsh.mc.GameOptions;
import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.state.Multiplayer;

import static de.gsh.mc.GameField.*;

public class RandomAI extends BotAI
{
	public static final String[] NAMES = new String[] { "Alfons", "Horst", "Peter", "Random", "Gieﬂbert", "Dominik Narr"
			,"Merlin Zauberer", "Apfelsine"};
	
	private boolean tilesDefined = false;
	private int tileX, tileY;
	
	public RandomAI(SchiffeVersenken game, BufferedImage cursor)
	{
		super(game, NAMES[new Random().nextInt(NAMES.length)], cursor);
	}
	
	@Override
	public void tick(Graphics2D g, GameField field)
	{
		if (!tilesDefined)
		{
			Random rand = new Random();
			int tileX;
			int tileY;
			
			while (true)
			{
				this.tileX = tileX = rand.nextInt(10);
				this.tileY = tileY = rand.nextInt(10);
				
				if (field.wasHit(tileX, tileY) || field.getGameTensor()[field.getCurrentPlayer()][tileX][tileY] == 1)
					continue;
				
				break;
			}
			
			fromX = desX;
			fromY = desY;
			desX = START_RASTER_X + (TILE_DIM * tileX);
			desY = START_RASTER_Y + (TILE_DIM * tileY);
			
			tilesDefined = true;
		}
		
		if (!animateCursor(g))
			return;
		
		System.out.println("Bot " + getName() + " is hitting (" + tileX + " | " + tileY + ")");
		
		if (field.hit(tileX, tileY))
		{
			((Multiplayer) game.getMenu()).clicksNeeded[field.getCurrentPlayer()]++;
			((Multiplayer) game.getMenu()).points[field.getCurrentPlayer()] -= 100 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
		}
		else
		{
			((Multiplayer) game.getMenu()).points[field.getCurrentPlayer()] += 200 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
			{
				SchiffeVersenken.playAudio("hit");
				
				if (((Multiplayer) game.getMenu()).points[field.getCurrentPlayer()] - 200 * GameOptions.getRekt() < 9000
						&& ((Multiplayer) game.getMenu()).points[field.getCurrentPlayer()] >= 9000)
					SchiffeVersenken.pad(new File("./audio/etc/", "ont.wav"), 6);
			}
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
		}
		
		tilesDefined = false;
		
		SchiffeVersenken.playAudio(field.getGameTensor()[field.getCurrentPlayer()][tileX][tileY] < 2 ? "water"
				: "hit");
		
		if (field.tick())
			return;
		
		if (!(game.getMenu() instanceof Multiplayer))
			throw new RuntimeException("HTFITP");
		
		((Multiplayer) game.getMenu()).playAnimation();
		return;
	}
}