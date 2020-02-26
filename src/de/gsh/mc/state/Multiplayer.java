package de.gsh.mc.state;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.ai.BossAI;
import de.gsh.mc.ai.RandomAI;
import de.gsh.mc.Config;
import de.gsh.mc.GameField;
import de.gsh.mc.GameOptions;
import de.gsh.mc.I18n;
import de.gsh.mc.Menu;
import de.gsh.mc.PlayerProfile;

import static de.gsh.mc.GameField.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INPUT_VALUE_PROPERTY;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.UNINITIALIZED_VALUE;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class Multiplayer extends Menu implements MouseListener, LangReloadListener, MouseMotionListener, KeyListener
{
	private final Font initFond = new Font("arial", Font.PLAIN, 30);
	private final Font headerFont = new Font("arial", Font.BOLD, 40);
	private final Font wonFont = new Font("arial", Font.BOLD, 60);
	
	private GameField field;
	private MPMode theMode;
	private boolean selectingMode;
	private boolean enteringNames;
	private boolean ended;
	private boolean animating = false;
	public int[] clicksNeeded;
	public float[] timeUsed;
	private int maxClicks;
	private String msg = "";
	private Color c;
	private byte playerWon;
	private byte startPlayer;
	public int[] points;
	private PlayerProfile[] profiles;
	
	// I18n
	private String option;
	private String won;
	private String lost;
	
	private int mouseX, mouseY;
	private BufferedImage snipar, dts, mtnd;
	
	@Override
	public String toString()
	{
		return getClass().getName() + "@" + hashCode() + " -> { Multiplayer: field = " + field + ", "
				+ "theMode = " + theMode + ", selectingMode = " + selectingMode + ", enteringNames = " +
				enteringNames + ", ended = " + ended + ", animating = " + animating + ", maxClicks = "
				+ maxClicks + ", playerWon = " + playerWon + ", startPlayer = " + startPlayer + " }";
	}
	
	public Multiplayer(SchiffeVersenken game)
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
			option = I18n.a("multiplayer.option");
			won = I18n.a("multiplayer.won");
			lost = I18n.a("multiplayer.lost");
			return;
		}
		
		won = "eZZ M8! NAICE SKILZ!";
		lost = "BE MAD BRAH!!";
		option = "Ya gat da choize maen!";
	}
	
	public GameField getGameField()
	{
		return field;
	}

	@Override
	public void render(Graphics2D g)
	{
		if (field != null && field.isInvalidated())
			return;
		
		if (!ended && !animating && !selectingMode && field != null)
			timeUsed[field.getCurrentPlayer()] += game.getDeltaTime() * 1000;
		
		if (selectingMode)
		{
			drawSelect(g); // Faster reading :P #AllAboutEfficiency
			return;
		}
		
		if (field == null)
		{
			field = new GameField(theMode.players, startPlayer);
			
			if (field.isInvalidated())
				return;
			
			field.initialize();
			
			if (theMode == MPMode.BOT_EASY)
				field.setBot(new RandomAI(game, BLUE_CURSOR));
			
			if (theMode == MPMode.BOT_HARD)
				field.setBot(new BossAI(game, HELLRED_CURSOR));
		}
		
		if (enteringNames)
		{
			drawSelect(g);
			return;
		}
		
		if (ended)
		{
			field.render(g, clicksNeeded, maxClicks, toLong(), points, false);
			g.setFont(wonFont);
			g.setColor(c);
			g.drawString(msg, getCenter(g, msg), SchiffeVersenken.HEIGHT / 2);
			drawCursorToScreen(g, 0);
			
			animateShowShips(g);
			
			if (SchiffeVersenken.isMLG())
				g.drawImage(snipar, mouseX - 150, mouseY - 200, null);
			
			return;
		}
		
		int an = 0;
		
		if (animating && (an = field.transitToNextPlayer(g, clicksNeeded, GameOptions.getMaxTries(),
				toLong(), points)) > 0)
		{
			if (SchiffeVersenken.isMLG())
				g.drawImage(snipar, mouseX - 150, mouseY - 200, null);
			
			super.drawCursorToScreen(g, 0);
			return;
		}
		
		if (an == -1)
		{
			field.animState = 0;
			animating = false;
		}
		
		field.render(g, clicksNeeded, maxClicks, toLong(), points, true);

		if (SchiffeVersenken.isMLG())
			g.drawImage(snipar, mouseX - 150, mouseY - 200, null);
		
		super.drawCursorToScreen(g, 0);
	}
	
	private void drawSelect(Graphics2D g)
	{
		g.setColor(Color.RED);
		g.setFont(headerFont);
		g.drawString(option, 233, 60);
		
		g.setColor(Color.WHITE);
		g.setFont(initFond);
		
		int i = 0;
		
		for (MPMode mode : MPMode.values())
		{
			g.drawRect(280, 100 + 80 * i, 300, 50);
			g.drawString(mode.display, getCenter(g, mode.display), 135 + 80 * i++);
		}
		
		if (!SchiffeVersenken.isMLG())
			return;
		
		g.drawImage(dts, 0, 100, null);
		g.drawImage(mtnd, 560, 100, null);
	}

	@Override
	public void tick()
	{
		if (field == null || ended)
			return;
		
		for (int i = 0; i < theMode.players; i++)
		{
			if (clicksNeeded[i] < maxClicks)
				continue;
			
			ended = true;
			I18n.removeLRL(this);
			I18n.removeLRL(field);
			c = Color.RED;
			msg = String.format(lost, field.getPlayerNames()[field.getCurrentPlayer()]);
			animating = false;
			if (SchiffeVersenken.isMLG())
				SchiffeVersenken.pad(new File("./audio/etc/", "ws.wav"), 6);
			playerWon = field.getCurrentPlayer(); // Jep, won :D
			
			if (theMode.bot)
				return;
			
			for (int j = 0; j < theMode.players; j++)
			{
				PlayerProfile p = profiles[j];
				
				if (playerWon == j)
				{
					p.wins++;
					p.minTimeUsed = Math.min(p.minTimeUsed, toLong()[j]);
					p.clicksToMax = Math.min(p.clicksToMax, ((float) clicksNeeded[j]) / maxClicks);
				}
				else p.looses++;
				
				p.games++;
				p.lowScore = Math.min(p.lowScore, points[j]);
				p.topScore = Math.max(p.topScore, points[j]);
				p.save();
			}
			
			return;
		}
		
		if (!field.tick())
			return;
		
		ended = true;
		playerWon = field.getCurrentPlayer();
		msg = String.format(won, field.getPlayerNames()[field.getCurrentPlayer()]);
		c = Color.GREEN;
		I18n.removeLRL(this);
		I18n.removeLRL(field);
	}

	@Override
	public void initialize() throws IOException
	{
		System.out.println("Initializing multiplayer mode!");
		
		selectingMode = true;
		maxClicks = GameOptions.getMaxTries();
		snipar = ImageIO.read(new File("./img/etc/", "sniparCl4n.png"));
		dts = scaleImage(ImageIO.read(new File("./img/etc/", "dts.jpg")), 300, 500);
		mtnd = scaleImage(ImageIO.read(new File("./img/etc/", "mtdw.jpg")), 300, 500);
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (animating)
			return;
		
		if (selectingMode)
		{
			if (e.getX() < 280 || e.getY() > 580 || e.getY() < 100 || e.getY() > 550)
				return;
			
			if (e.getY() <= 150)
			{
				setMode(MPMode.BOT_EASY);
				return;
			}
			
			if (e.getY() >= 180 && e.getY() <= 230)
			{
				setMode(MPMode.BOT_HARD);
				return;
			}
	
			if (e.getY() >= 260 && e.getY() <= 310)
			{
				setMode(MPMode.PLAYERS_2);
				return;
			}
			
			if (e.getY() >= 340 && e.getY() <= 390)
			{
				setMode(MPMode.PLAYERS_3);
				return;
			}
			
			if (e.getY() >= 420 && e.getY() <= 470)
			{
				setMode(MPMode.PLAYERS_4);
				return;
			}
			
			if (e.getY() < 500)
				return;
			
			setMode(MPMode.MAIN_MENU);
			return;
		}
		
		if (e.getX() >= 10 && e.getX() <= 110 && e.getY() >= 10 && e.getY() <= 50)
		{
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if ((field.getCurrentPlayer() == 1 && (theMode == MPMode.BOT_EASY || theMode == MPMode.BOT_HARD)) || ended)
			return; // Not your turn!
		
		if (!field.isStarted())
		{
			if (SchiffeVersenken.isMLG())
				SchiffeVersenken.pad(new File("./audio/etc/", "h.wav"));
		
			field.start();
			return;
		}
		
		if (e.getX() < START_RASTER_X || e.getY() < START_RASTER_Y)
			return;
		
		int tileX = (e.getX() - START_RASTER_X) / TILE_DIM;
		int tileY = (e.getY() - START_RASTER_Y) / TILE_DIM;
		
		if (tileX > 9 || tileY > 9 || field.wasHit(tileX, tileY))
			return;
		
		System.out.println("Hitting (" + tileX + " | " + tileY + ")");
		
		if (field.getGameTensor()[field.getCurrentPlayer()][tileX][tileY] >= 2
				&& field.getRenderTensor()[field.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[field.getCurrentPlayer()] += 200 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
			{
				SchiffeVersenken.playAudio("hit");
				
				if (points[field.getCurrentPlayer()] - 200 * GameOptions.getRekt() < 9000 &&
						points[field.getCurrentPlayer()] >= 9000)
					SchiffeVersenken.pad(new File("./audio/etc/", "ont.wav"), 6);
			}
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
		}
		else if (field.getRenderTensor()[field.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[field.getCurrentPlayer()] -= 100 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
		}
		
		if (field.hit(tileX, tileY))
			clicksNeeded[field.getCurrentPlayer()]++;
		
		animating = true;
	}
	
	public void playAnimation()
	{
		animating = true;
		System.out.println("Animating to next state!");
	}
	
	private void setMode(MPMode mode)
	{
		if (mode == MPMode.MAIN_MENU)
		{
			game.transitToState(new MainMenu(game));
			return;
		}
		
		System.out.println("Setting multiplayer mode to " + mode);
		
		theMode = mode;
		selectingMode = false;
		clicksNeeded = new int[mode.players];
		timeUsed = new float[mode.players];
		points = new int[mode.players];
		profiles = new PlayerProfile[mode.players];
		profiles[0] = game.getPlayerProfile();
		
		if (mode == MPMode.BOT_EASY || mode == MPMode.BOT_HARD)
		{
			game.setCursor(SchiffeVersenken.INVISIBLE_CURSOR);
			return;
		}
		
		startPlayer = (byte) new Random().nextInt(mode.players);
		enteringNames = true;
		new NameEnterPanel(mode.players - 1);
	}
	
	public static enum MPMode
	{
		BOT_EASY("multiplayer.botEasy", (byte) 2, true),
		BOT_HARD("multiplayer.botHard", (byte) 2, true),
		PLAYERS_2("multiplayer.twoPlayers", (byte) 2),
		PLAYERS_3("multiplayer.threePlayers", (byte) 3),
		PLAYERS_4("multiplayer.fourPlayers", (byte) 4),
		MAIN_MENU("multiplayer.mainMenu", (byte) -1);
		
		protected final String display;
		protected final byte players;
		protected final boolean bot;
		
		private MPMode(String s, byte b)
		{
			this(s, b, false);
		}
		
		private MPMode(String display, byte players, boolean bot)
		{
			this.players = players;
			this.bot = bot;
			
			this.display = toMLG(display);
		}
		
		private String toMLG(String display)
		{
			if (!SchiffeVersenken.isMLG())
				return I18n.a(display);
			
			switch (display)
			{
			case "multiplayer.botEasy":
				return "eZZer REKKER";
			case "multiplayer.botHard":
				return "M8 FINISHER";
			case "multiplayer.twoPlayers":
				return "2 M8s";
			case "multiplayer.threePlayers":
				return "3 M8s";
			case "multiplayer.fourPlayers":
				return "4 M8s";
			case "multiplayer.mainMenu":
				return "Nah ya p'sey";
			}
			
			return null;
		}
	}
	
	private long[] toLong()
	{
		long[] n = new long[theMode.players];
		
		for (int i = 0; i < theMode.players; i++)
			n[i] = (long) timeUsed[i];
		
		return n;
	}
	
	private double wait = 0.0;
	private boolean done = false;
	private boolean beg = true;
	
	private void animateShowShips(Graphics2D g)
	{
		if (beg)
		{
			field.setShipsVisible(field.getCurrentPlayer());
			beg = false;
		}
		
		wait += game.getDeltaTime();
		
		if (wait >= 2.5 && !done)
		{
			if (field.transitToNextPlayer(g, clicksNeeded, maxClicks,
					toLong(), points) != -1)
				return;
			
			field.animState = 0;
			wait = 0;
			done = true;
			field.setShipsVisible(field.getCurrentPlayer());
			
			return;
		}
		
		if (wait < 2.5 || !done)
			return;
		
		done = false;
		wait = 0.0;
		
		if (field.getNextPlayer() != playerWon)
			return;
		
		game.transitToState(new MainMenu(game));
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() != KeyEvent.VK_P || (theMode != MPMode.BOT_EASY && theMode != MPMode.BOT_HARD))
			return;
		
		field.paused = !field.paused;
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {}
	@Override public void keyReleased(KeyEvent e) {}
	@Override public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}
	
	private final class NameEnterPanel extends JFrame implements ActionListener, PropertyChangeListener, LangReloadListener
	{
		private static final long serialVersionUID = -6894801966479964140L;

		private JPanel gui;
		private JPanel loginData;
		private JPanel labels;
		private JPanel fields;

		private final JLabel[] jlabels;
		private final JTextField[] jtfs;
		private JOptionPane optionPane;
		
		private final int names;
		
		private String accept, cancel, player, header, errorBase, errorNoUsername, errorNameTooShort;
		
		private NameEnterPanel(int am)
		{
			this.names = am;
			jtfs = new JTextField[am];
			jlabels = new JLabel[am];
			
			onLangReload();
			I18n.addLRL(this);
			
			init();
		}
		
		@Override
		public void onLangReload()
		{
			accept = I18n.a("multiplayer.namePanel.accept");
			cancel = I18n.a("multiplayer.namePanel.cancel");
			player = I18n.a("multiplayer.namePanel.player");
			header = I18n.a("multiplayer.namePanel.header");
			errorBase = I18n.a("multiplayer.namePanel.error.base");
			errorNoUsername = I18n.a("multiplayer.namePanel.error.noUsername");
			errorNameTooShort = I18n.a("multiplayer.namePanel.error.nameTooShort");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("User performed action: " + e.getActionCommand());
			optionPane.setValue(accept);
		}
		
		@Override
		public void dispose()
		{
			super.dispose();
			Multiplayer.this.selectingMode = true;
			Multiplayer.this.enteringNames = false;
			Multiplayer.this.theMode = null;
			Multiplayer.this.field = null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			String prop = evt.getPropertyName();
			
			if (!isVisible() || evt.getSource() != optionPane || !(VALUE_PROPERTY.equals(prop) ||
					INPUT_VALUE_PROPERTY.equals(prop)))
				return;

			Object value = optionPane.getValue();
			
			if (value.equals(cancel))
			{
				theMode = null;
				selectingMode = true;
				enteringNames = false;
				field = null;
				
				dispose();
				I18n.removeLRL(this);
				
				return;
			}
			
			if (!value.equals(accept))
				return;

			if (value == UNINITIALIZED_VALUE)
				return;
			
			System.out.println("TEST");

			optionPane.setValue(UNINITIALIZED_VALUE);
			
			System.out.println("Names entered!");
			
			String[] usernames = new String[names];
			
			for (int i = 0; i < names; i++)
				usernames[i] = jtfs[i].getText();
			
			String message = errorBase + System.lineSeparator();
			int i = 0;
			Config cfg = new Config(new File(".", "options.opt"));

			for (String stringUsername : usernames)
			{
				i++;
				if (stringUsername == null || stringUsername.isEmpty())
				{
					message += errorNoUsername + (i + 1) + System.lineSeparator();
					System.out.println("No Username given!");
					
					continue;
				}
				else if (stringUsername.length() < 3)
				{
					message += String.format(errorNameTooShort, stringUsername) + System.lineSeparator();
					System.out.println("Invalid username!");
	
					continue;
				}
				
				System.out.println("Performing user-add for user \"" + stringUsername + "\"");
				Multiplayer.this.field.appendUserName(stringUsername);
				Multiplayer.this.profiles[i] = new PlayerProfile(stringUsername);
				cfg.set("lastNameForPlayer" + i, stringUsername);
			}
			
			if (!message.equals(errorBase + System.lineSeparator()))
			{
				JOptionPane.showMessageDialog(this, message, errorBase, ERROR_MESSAGE);
				return;
			}
			
			System.out.println("Hiding " + getClass().getName());
			game.setCursor(SchiffeVersenken.INVISIBLE_CURSOR);
			super.setVisible(false);
			super.dispose();
			enteringNames = false;
			cfg.save(new File(".", "options.opt"));
			I18n.removeLRL(this);
		}

		private void init()
		{
			gui = new JPanel(new BorderLayout(2, 2));

			loginData = new JPanel(new BorderLayout(2, 2));

			labels = new JPanel(new GridLayout(0, 1, 1, 1));
			fields = new JPanel(new GridLayout(0, 1, 1, 1));
			
			Config cfg = new Config(new File(".", "options.opt"));

			for (int i = 0; i < names; i++)
			{
				jlabels[i] = new JLabel(player + (i + 2));
				jtfs[i] = new JTextField(10);
				jtfs[i].setText(cfg.get("lastNameForPlayer" + (i + 1)));
				jtfs[i].addActionListener(this);
	
				labels.add(jlabels[i]);
				fields.add(jtfs[i]);
			}

			loginData.add(labels, BorderLayout.CENTER);
			loginData.add(fields, BorderLayout.EAST);

			gui.add(loginData);

			optionPane = new JOptionPane(new Object[]
					{
						header, this.gui
					},
					QUESTION_MESSAGE,
					YES_NO_OPTION,
					null,
					new Object[]
					{
						accept, cancel
					},
					accept);

			super.setContentPane(optionPane);
			super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			optionPane.addPropertyChangeListener(this);

			super.setResizable(false);
			super.pack();
			super.setLocationRelativeTo(null);
			super.setVisible(true);
			
			System.out.println("Graphical instance created...");
		}
	}
}