package de.gsh.mc.ai;

import static de.gsh.mc.GameField.START_RASTER_X;
import static de.gsh.mc.GameField.START_RASTER_Y;
import static de.gsh.mc.GameField.TILE_DIM;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import de.gsh.mc.BotAI;
import de.gsh.mc.GameField;
import de.gsh.mc.GameOptions;
import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.state.Multiplayer;

public class BossAI extends BotAI
{
	public static final String[] NAMES = new String[] { "Alfons", "Horst", "Peter", "Random", "Gieﬂbert", "Dominik Narr"
			,"Merlin Zauberer", "Apfelsine", "Bastardus", "Bastardus", "Bastardus" };
	
	// The game field is only referenced for the knowledge of whether the ai has sunken the ship or not
	
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
	private final byte[][] gameField;
	
	/*
	 * Knowledge is defined as the following:
	 * 
	  		-1: No knowledge at all (default)
	  		 0: Water/no ship possible
	  		 1: ship
	  		 2: ship border
	 */
	private final Multiplayer menu;
	private final byte[][] knowledge = new byte[10][10];
	private boolean tilesDefined = false;
	private Random random = new Random();
	
	private int tileX = -1;
	private int tileY = -1;
	
	private boolean hasHitShip = false;
	private int shipDirection = -1; // 0 up 1 down 2 left 3 right -1 undetermined
	
	private int shipX = -1, shipY = -1, hitsInDir = -1, shipLength = 1;
	private boolean[] triedDirections = new boolean[4];
	
	private byte[] remaining = GameOptions.getAmountShips();
	
	public BossAI(SchiffeVersenken game, BufferedImage cursor)
	{
		super(game, NAMES[new Random().nextInt(NAMES.length)], cursor);
		menu = (Multiplayer) game.getMenu();
		
		gameField = menu.getGameField().getGameTensor()[1];
		
		for (int x = 0; x < 10; x++)
		{
			for (int y = 0; y < 10; y++)
				knowledge[x][y] = -1;
		}
	}
	
	@Override
	public synchronized void tick(Graphics2D g, GameField field)
	{
		if (!tilesDefined)
		{
			determineNextHit();
			
			fromX = desX;
			fromY = desY;
			desX = START_RASTER_X + (TILE_DIM * tileX);
			desY = START_RASTER_Y + (TILE_DIM * tileY);
			
			tilesDefined = true;
		}
		
		if (!animateCursor(g))
			return;
		
		if (field.hit(tileX, tileY))
		{
			menu.clicksNeeded[field.getCurrentPlayer()]++;
			menu.points[field.getCurrentPlayer()] -= 100 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
			knowledge[tileX][tileY] = 0;
		}
		else
		{
			menu.points[field.getCurrentPlayer()] += 200 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
			{
				SchiffeVersenken.playAudio("hit");
				
				if (menu.points[field.getCurrentPlayer()] - 200 * GameOptions.getRekt() < 9000 && menu.points[field.getCurrentPlayer()] >= 9000)
					SchiffeVersenken.pad(new File("./audio/etc/", "ont.wav"), 6);
			}
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
			
			knowledge[tileX][tileY] = 1;
			
			if (!hasHitShip)
			{
				shipX = tileX;
				shipY = tileY;
				hasHitShip = true;
				hitsInDir = shipLength = 1;
			}
			else updateHitShip();
		}
		
		tilesDefined = false;
		SchiffeVersenken.playAudio(field.getGameTensor()[field.getCurrentPlayer()][tileX][tileY] < 2 ? "water" : "hit");
		
		if (field.tick())
			return;
		
		if (!(game.getMenu() instanceof Multiplayer))
			throw new RuntimeException("WTFITP");
		
		menu.playAnimation();
		return;
	}
	
	private void determineNextHit()
	{
		if (hasHitShip)
		{
			if (shipDirection == -1)
			{
				// We still have to try directions in which the ship can be (future feature: go by probability?)
				int nextDir;
				for (nextDir = 0; triedDirections[nextDir]; nextDir++) { System.err.println("Meh"); } // select the next one to try
				
				int x = 0, y = 0;
				
				do
				{
					x = nextDir > 1 ? shipX : shipX + (nextDir == 0 ? -1 : 1);
					y = nextDir < 2 ? shipY : shipY + (nextDir == 2 ? -1 : 1);
					triedDirections[nextDir++] = true;
					
					if (nextDir >= 4)
						break;
				}
				while (x < 0 || y < 0 || x >= 10 || y >= 10 || knowledge[x][y] != -1);
				
				if (nextDir == 5)
					throw new IllegalArgumentException("Checked all directions and found nothing to hit anymore. Error in updateHitShip?");
				
				tileX = x;
				tileY = y;
				return;
			}
			
			System.err.println("Ship direction: " + shipDirection);
			
			if (knowledge[tileX][tileY] != 1)
			{
				if (shipDirection % 2 == 0)
					shipDirection++;
				
				else shipDirection--;
				
				hitsInDir = 1;
				System.err.println("Yes, direction has changed. Now checking in direction " + shipDirection);
			}
			
			tileX = shipDirection > 1 ? shipX : shipX + (shipDirection == 0 ? -hitsInDir : hitsInDir);
			tileY = shipDirection < 2 ? shipY : shipY + (shipDirection == 2 ? -hitsInDir : hitsInDir);
			return;
		}
		
		// random hit based on the informations the ai has
		int smallestLength = -1;
		
		for (int i = 0; i < remaining.length; i++)
		{
			if (remaining[i] == 0)
				continue;
			
			smallestLength = i + 2;
		}
		
		if (smallestLength == -1)
			throw new IllegalArgumentException("Game over, I won?!");
		
		int hitX, hitY;
		
		do
		{
			// This creates a grid where no hits are next to each other
			hitX = random.nextInt(10);
			hitY = random.nextInt(5) * 2;
			
			if (hitX % 2 != 0)
				hitY++;
			
			if (knowledge[hitX][hitY] != -1)
				continue;
			
			// Now we need to check whether we can have a valid hit option on any direction. If either up/down or left/right is invalid, they get tagged as "no ship possible".
			// If both, the hit tile itself gets tagged, too.
			
			int maxHits = 1;
			boolean shipPossible = false;
			int min = -1, max = -1;
			
			for (int x = hitX; x >= 0; x--)
			{
				if (knowledge[x][hitY] != -1)
				{
					min = x;
					break;
				}
				
				maxHits++;
			}
			
			if (min == -1)
				min = 0;
			
			for (int x = hitX; x < 10; x++)
			{
				if (knowledge[x][hitY] != -1)
				{
					max = x;
					break;
				}
				
				maxHits++;
			}
			
			if (max == -1)
				max = 9;
			
			if (maxHits < smallestLength)
			{
				// now we set the tiles as no ship possible here
				
				for (int x = min; x <= max; x++)
					knowledge[x][hitY] = 0;
			}
			else shipPossible = true;
			
			// repeat this step with the y coordinate!
			maxHits = 1;
			min = max = -1;
			
			for (int y = hitY; y >= 0; y--)
			{
				if (knowledge[hitX][y] != -1)
				{
					min = y;
					break;
				}
				
				maxHits++;
			}
			
			if (min == -1)
				min = 0;
			
			for (int y = hitY; y < 10; y++)
			{
				if (knowledge[hitX][y] != -1)
				{
					max = y;
					break;
				}
				
				maxHits++;
			}
			
			if (max == -1)
				max = 9;
			
			if (maxHits < smallestLength)
			{
				for (int y = min; y <= max; y++)
					knowledge[hitX][y] = 0;
			}
			else shipPossible = true;
			
			if (!shipPossible)
			{
				knowledge[hitX][hitY] = 0;
				continue;
			}
			
			tileX = hitX;
			tileY = hitY;
			return;
		}
		while (true);
	}
	
	private void updateHitShip()
	{
		if (shipDirection == -1)
		{
			// This was the next hit so we determined the direction in which the ship lays
			// 0 means up (tileX > shipX) 1 means down (tileX < shipX) 2 means left (tileY < shipY) 3 means right (tileY > shipY)
			shipDirection = tileY - shipY == 0 ? (tileX - shipX == 1 ? 0 : 1) : (tileY - shipY == -1 ? 2 : 3);
			System.err.println("Ship direction: " + shipDirection);
		}
		
		hitsInDir++;
		shipLength++;
		
		// now we do check if the ship has been hit completely. For this we start at (shipX | shipY) and move into the direction. If all are declared correctly in information and correspond
		// to the information we can gain from the game field, and no hit can be performed in the negative direction, this ship was hit completely.
		
		int mod = shipDirection % 2 == 0 ? -1 : 1;
		int start = shipDirection < 2 ? shipX : shipY;
		boolean wholeShip = true;
		
		int min = -1, max = -1;
		System.err.println("Mod: " + mod);
		System.err.println("Start: " + start);
		
		for (int val = start; goOn(val); val += mod)
		{
			System.err.println("Check for " + val);
			
			if (isBothWater(val))
			{
				System.err.println("True");
				min = val;
				break;
			}
			
			if (!knowledgeDifferenceHit(val))
				continue;
			
			System.err.println("This was not the whole ship :(");
			wholeShip = false;
			break;
		}
		
		if (wholeShip)
		{
			System.err.println("I still have to check on the opposite site!");
			
			for (int val = start; goOn(val); val -= mod)
			{
				System.err.println("Check for " + val);
				
				if (isBothWater(val))
				{
					System.err.println("True");
					max = val;
					break;
				}
				
				if (!knowledgeDifferenceHit(val))
					continue;

				System.err.println("This was not the whole ship :(");
				wholeShip = false;
				break;
			}
		}
		
		if (!wholeShip)
			return;
		
		int temp = min;
		min = Math.min(min, max);
		max = Math.max(temp, max);
		
		// make a box that is exactly one larger in each direction than the ship itself and mark it as close to ship so we don't check it again
		int startX = Math.max(0, shipDirection < 2 ? min - 1 : tileX - 1);
		int startY = Math.max(0, shipDirection < 2 ? tileY - 1 : min - 1);
		int endX = Math.min(10, shipDirection < 2 ? min + 1 : tileX + 1);
		int endY = Math.min(10, shipDirection < 2 ? tileY + 1 : max + 1);
		
		for (int x = startX; x < endX; x++)
		{
			for (int y = startY; y < endY; y++)
			{
				if (x == startX && y == startY || x == startX && y == endY || x == endX && y == startY || x == endX && y == startY) // exclude corners
					continue;
				
				if (x != startX && x != endX && y != startY && y != endY)
				{
					knowledge[x][y] = 1;
					continue;
				}
				
				knowledge[x][y] = 2;
			}
		}
		
		hasHitShip = false;
		shipDirection = shipX = shipY = -1;
		remaining[shipLength - 2]--;
		triedDirections = new boolean[4];
		System.err.println("Ship sunk. What am I doing now?");
	}
	
	private boolean knowledgeDifferenceHit(int value)
	{
		boolean isX = shipDirection < 2;
		System.err.println("Checking: isX? " + isX);
		
		boolean val;
		if (isX)
			val = gameField[value][tileY] > 1 && knowledge[value][tileY] != 1;

		else val = gameField[tileX][value] > 1 && knowledge[tileX][value] != 1;
		
		System.err.println("knowledgeDifferenceHit for " + value + " is " + val);
		return val;
	}
	
	// In this case we mean the gameField is water and we either don't know anything about this tile or that it is not a ship. If the knowledge differs from the game field, this check helps fix it
	private boolean isBothWater(int value)
	{
		System.err.println("isBothWater @" + value + "?");
		boolean isX = shipDirection < 2;
		
		if (isX)
			return gameField[value][tileY] == 0 && knowledge[value][tileY] != 1;
		
		return gameField[tileX][value] == 0 && knowledge[tileX][value] != 1;
	}
	
	private boolean goOn(int value)
	{
		return shipDirection % 2 == 0 ? value >= 0 : value < 10;
	}
}