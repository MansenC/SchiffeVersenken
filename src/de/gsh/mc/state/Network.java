package de.gsh.mc.state;

import static de.gsh.mc.GameField.START_RASTER_X;
import static de.gsh.mc.GameField.START_RASTER_Y;
import static de.gsh.mc.GameField.TILE_DIM;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INPUT_VALUE_PROPERTY;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.UNINITIALIZED_VALUE;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import static javax.swing.JOptionPane.YES_NO_OPTION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import de.gsh.mc.GameField;
import de.gsh.mc.GameOptions;
import de.gsh.mc.I18n;
import de.gsh.mc.Menu;
import de.gsh.mc.SchiffeVersenken;
import de.gsh.mc.I18n.LangReloadListener;
import de.gsh.mc.client.Client;
import de.gsh.mc.server.NetworkHandle;
import de.gsh.mc.server.Packet03GameInitialized;
import de.gsh.mc.server.Packet04Hit;
import de.gsh.mc.server.Packet05CursorMove;
import de.gsh.mc.server.Packet06StatsChange;
import de.gsh.mc.server.Packet07GameStarted;
import de.gsh.mc.server.Packet08DoAnimation;
import de.gsh.mc.server.Packet09GameEnd;
import de.gsh.mc.server.Server;

public class Network extends Menu implements MouseListener, MouseMotionListener, LangReloadListener
{
	private NetworkHandle networkHandle;
	
	public String enemy = null;
	private boolean isServer;
	private boolean select = true;
	private boolean panel = false;
	private final Font headerFont = new Font("arial", Font.BOLD, 40);
	private final Font nameFont = new Font("arial", Font.PLAIN, 30);
	private final Font wonFont = new Font("arial", Font.BOLD, 60);
	public GameField theGameField;
	private byte starting;
	
	private byte enemyCursor;
	private int enemyCursorX, enemyCursorY;
	
	private long[] timeUsed = new long[2];
	private int[] clicksNeeded = new int[2];
	private int[] points = new int[2];
	public int maxClicks;
	private boolean animating = false;
	private boolean ended;
	private Color c;
	private String msg;
	
	private double wait = 0, wait2 = 0, wait3 = 0;
	private BufferedImage snipar;
	private int mouseX, mouseY;
	
	private String lost, won, header, host, connect, mainMenu, disconnect, waiting, username, listening,
		aboard, connected, enemyName;
	
	public Network(SchiffeVersenken game)
	{
		super(game);
		
		I18n.addLRL(this);
		onLangReload();
	}
	
	@Override
	public void onLangReload()
	{
		lost = I18n.a("network.lost");
		won = I18n.a("network.won");
		header = I18n.a("network.header");
		host = I18n.a("network.host");
		connect = I18n.a("network.connect");
		mainMenu = I18n.a("network.mainmenu");
		disconnect = I18n.a("network.disconnect");
		waiting = I18n.a("network.waiting");
		username = I18n.a("network.username");
		listening = I18n.a("network.listening");
		aboard = I18n.a("network.aboard");
		connected = I18n.a("network.connected");
		enemyName = I18n.a("network.enemyName");
	}
	
	private String display;
	
	public void display(String i18n)
	{
		display = I18n.a(i18n) + networkHandle.getIP();
		
		new Thread(() ->
		{
			try
			{
				Thread.sleep(2500L);
			}
			catch (Exception ex) {}
			
			game.transitToState(new MainMenu(game));
		}).start();
	}

	@Override
	public void render(Graphics2D g)
	{
		if (display != null)
		{
			g.setFont(headerFont);
			g.setColor(Color.WHITE);
			g.drawString(display, getCenter(g, display), 300);
			
			return;
		}
		
		if (!ended && !animating && theGameField != null)
			timeUsed[theGameField.getCurrentPlayer()] += game.getDeltaTime() * 1000;
		
		if (select || panel)
		{
			g.setFont(headerFont);
			g.setColor(Color.RED);
			g.drawString(header, getCenter(g, header), 70);
			g.setColor(Color.WHITE);
			g.drawRect(305, 100, 250, 50);
			g.drawRect(305, 180, 250, 50);
			g.drawRect(305, 260, 250, 50);
			g.setFont(nameFont);
			g.drawString(host, getCenter(g, host), 135);
			g.drawString(connect, getCenter(g, connect), 215);
			g.drawString(mainMenu, getCenter(g, mainMenu), 295);
			
			return;
		}
		
		if (!ended && networkHandle.connectionLost())
		{
			g.setFont(headerFont);
			g.setColor(Color.WHITE);
			wait3 += game.getDeltaTime();
			
			g.drawString(disconnect, getCenter(g, disconnect), 300);
			
			if (wait3 < 4)
				return;
			
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if (!ended && !networkHandle.isConnected())
		{
			g.setFont(headerFont);
			g.setColor(Color.WHITE);
			wait += game.getDeltaTime();
			String disp = waiting;
			
			for (int i = 0; i < ((int) (wait * 3)) % 10; i++)
				disp += ".";
			
			g.drawString(username + game.getUserName(), getCenter(g, username + game.getUserName()), 150);
			g.drawString(disp, getCenter(g, disp), 300);
			g.drawString(listening + networkHandle.getIP(), getCenter(g, listening + networkHandle.getIP()), 350);
			g.drawRect(305, 400, 250, 50);
			g.drawString(aboard, getCenter(g, aboard), 437);
			
			return;
		}
		
		if (ended)
		{
			theGameField.render(g, clicksNeeded, maxClicks, toLong(), points, false);
			g.setFont(wonFont);
			g.setColor(c);
			g.drawString(msg, getCenter(g, msg), SchiffeVersenken.HEIGHT / 2);
			drawCursorToScreen(g, 0);
			g.drawImage(enemyCursor == 0 ? BLUE_CURSOR : RED_CURSOR, enemyCursorX - 15, enemyCursorY - 10, null);
			
			return;
		}
		
		if (theGameField == null)
		{
			g.setFont(headerFont);
			g.setColor(Color.WHITE);
			g.drawString(connected, getCenter(g, connected), 300);
			
			if (enemy != null)
				g.drawString(enemyName + enemy, getCenter(g, enemyName + enemy), 340);
			
			wait2 += game.getDeltaTime();
			
			if (wait2 < 5 || !isServer)
				return;
			
			starting = (byte) new Random().nextInt(2);
			enemyCursor = (byte) (starting == 0 && isServer() ? 0 : 1);
			theGameField = new GameField((byte) 2, starting);
			maxClicks = GameOptions.getMaxTries();
			
			if (theGameField.isInvalidated())
				return;
			
			theGameField.initialize();

			theGameField.appendUserName(enemy);
			networkHandle.sendPacket(new Packet03GameInitialized(theGameField.getGameTensor(), starting, maxClicks,
					GameOptions.getAmountShips()));
			wait2 = 0;
			game.setCursor(SchiffeVersenken.INVISIBLE_CURSOR);
		}
		
		int an = 0;
		
		if (animating && (an = theGameField.transitToNextPlayer(g, clicksNeeded, GameOptions.getMaxTries(),
				toLong(), points)) > 0)
		{
			super.drawCursorToScreen(g, 0);
			g.drawImage(enemyCursor == 0 ? BLUE_CURSOR : RED_CURSOR, enemyCursorX - 15, enemyCursorY - 10, null);
			return;
		}
		
		if (an == -1)
		{
			theGameField.animState = 0;
			animating = false;
		}
		
		theGameField.render(g, clicksNeeded, maxClicks, toLong(), points, false);
		g.drawImage(enemyCursor == 0 ? BLUE_CURSOR : RED_CURSOR, enemyCursorX - 15, enemyCursorY - 10, null);
		drawCursorToScreen(g, enemyCursor);
		
		if (SchiffeVersenken.isMLG())
		{
			g.drawImage(snipar, mouseX - 150, mouseY - 200, null);
			g.drawImage(snipar, enemyCursorX - 150, enemyCursorY - 200, null);
		}
	}

	@Override
	public void tick()
	{
		if (theGameField == null || ended || maxClicks == 0)
			return;
		
		for (int i = 0; i < 2; i++)
		{	
			if (clicksNeeded[i] < maxClicks)
				continue;
			
			ended = true;
			I18n.removeLRL(theGameField);
			I18n.removeLRL(this);
			c = Color.RED;
			msg = String.format(lost, theGameField.getPlayerNames()[theGameField.getCurrentPlayer()]);
			animating = false;
			
			new Thread(() ->
			{
				try
				{
					Thread.sleep(4000L);
				}
				catch (Exception ex) {}
				
				game.transitToState(new MainMenu(game));
				closeConnection();
			}).start();
			
			return;
		}
		
		if (!theGameField.tick())
			return;
		
		ended = true;
		msg = String.format(won, theGameField.getPlayerNames()[theGameField.getCurrentPlayer()]);
		c = Color.GREEN;
		I18n.removeLRL(theGameField);
		I18n.removeLRL(this);
		
		new Thread(() ->
		{
			try
			{
				Thread.sleep(4000L);
			}
			catch (Exception ex) {}
			
			game.transitToState(new MainMenu(game));
			closeConnection();
		}).start();
	}
	
	private long[] toLong()
	{
		long[] n = new long[2];
		n[0] = (long) timeUsed[0];
		n[1] = (long) timeUsed[1];
		
		return n;
	}

	@Override
	public void initialize() throws Exception
	{
		snipar = ImageIO.read(new File("./img/etc/", "sniparCl4n.png"));
	}
	
	public void onConnect(String name)
	{
		enemy = name;
	}
	
	public boolean isServer()
	{
		return isServer;
	}
	
	public void doAnimation()
	{
		animating = true;
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		if (networkHandle == null || !networkHandle.isConnected() || networkHandle.connectionLost())
			return;
		
		networkHandle.sendPacket(new Packet05CursorMove(e.getX(), e.getY()));
		mouseX = e.getX();
		mouseY = e.getY();
	}
	
	public void drawCursor(int x, int y)
	{
		enemyCursorX = x;
		enemyCursorY = y;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (ended || animating)
			return;
		
		if (!select && !panel && !networkHandle.isConnected() && !networkHandle.connectionLost())
		{
			if (e.getX() < 305 || e.getX() > 555 || e.getY() < 400 || e.getY() > 450)
				return;
			
			try
			{
				networkHandle.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if (select && !panel)
		{
			if (e.getX() < 305 || e.getX() > 555 || e.getY() < 100 || e.getY() > 310)
				return;
			
			if (e.getY() < 150)
			{
				isServer = panel = true;
				select = false;
				new SelectionPanel();
				return;
			}
			
			if (e.getY() >= 180 && e.getY() <= 230)
			{
				isServer = select = false;
				panel = true;
				new SelectionPanel();
				return;
			}
			
			if (e.getY() < 260)
				return;
			
			game.transitToState(new MainMenu(game));
			return;
		}
		
		if (theGameField == null)
			return;
		
		if (e.getX() >= 10 && e.getX() <= 110 && e.getY() >= 10 && e.getY() <= 50)
		{
			game.transitToState(new MainMenu(game));
			closeConnection();
			return;
		}
		
		if (!theGameField.isStarted() && starting == 0)
		{
			theGameField.start();
			networkHandle.sendPacket(new Packet07GameStarted());
			return;
		}
		
		if (theGameField.getCurrentPlayer() != (isServer ? 0 : 1))
			return;
		
		if (e.getX() < START_RASTER_X || e.getY() < START_RASTER_Y)
			return;
		
		int tileX = (e.getX() - START_RASTER_X) / TILE_DIM;
		int tileY = (e.getY() - START_RASTER_Y) / TILE_DIM;
		
		if (tileX > 9 || tileY > 9 || theGameField.wasHit(tileX, tileY))
			return;
		
		System.out.println("Hitting (" + tileX + " | " + tileY + ")");
		
		if (theGameField.getGameTensor()[theGameField.getCurrentPlayer()][tileX][tileY] >= 2
				&& theGameField.getRenderTensor()[theGameField.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[theGameField.getCurrentPlayer()] += 200 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
			{
				SchiffeVersenken.playAudio("hit");
				
				if (points[theGameField.getCurrentPlayer()] - 200 * GameOptions.getRekt() < 9000 &&
						points[theGameField.getCurrentPlayer()] >= 9000)
					SchiffeVersenken.pad(new File("./audio/etc/", "ont.wav"), 6);
			}
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
		}
		else if (theGameField.getRenderTensor()[theGameField.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[theGameField.getCurrentPlayer()] -= 100 * GameOptions.getRekt();
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
		}
		
		if (theGameField.hit(tileX, tileY))
			clicksNeeded[theGameField.getCurrentPlayer()]++;
		
		networkHandle.sendPacket(new Packet04Hit(tileX, tileY));
		networkHandle.sendPacket(new Packet06StatsChange(points, clicksNeeded));
		
		if (theGameField.tick())
		{
			networkHandle.sendPacket(new Packet09GameEnd(game.getUserName()));
			return;
		}
		
		networkHandle.sendPacket(new Packet08DoAnimation());
		animating = true;
	}
	
	public void onGameEnd(String name)
	{
		ended = true;
		
		if (clicksNeeded[1 - theGameField.getCurrentPlayer()] < maxClicks)
		{
			I18n.removeLRL(theGameField);
			I18n.removeLRL(this);
			c = Color.GREEN;
			msg = String.format(won, theGameField.getPlayerNames()[0]);
			animating = false;
			new Thread(() ->
			{
				try
				{
					Thread.sleep(4000L);
				}
				catch (Exception ex) {}
				
				game.transitToState(new MainMenu(game));
				closeConnection();
			}).start();
			
			return;
		}
		
		msg = String.format(lost, name);
		c = Color.RED;
		I18n.removeLRL(theGameField);
		I18n.removeLRL(this);
		animating = false;
		
		new Thread(() ->
		{
			try
			{
				Thread.sleep(4000L);
			}
			catch (Exception ex) {}
			
			game.transitToState(new MainMenu(game));
			closeConnection();
		}).start();
	}
	
	public void onStatsChange(int[] points, int[] clicks)
	{
		this.points = points;
		clicksNeeded = clicks;
	}
	
	public void onGameStart()
	{
		theGameField.start();
	}
	
	public void onHit(int tileX, int tileY)
	{
		if (theGameField.getGameTensor()[theGameField.getCurrentPlayer()][tileX][tileY] >= 2
				&& theGameField.getRenderTensor()[theGameField.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[theGameField.getCurrentPlayer()] += 200;
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("hit");
			else SchiffeVersenken.pad(new File("./audio/etc/", "ht.wav"), 4);
		}
		else if (theGameField.getRenderTensor()[theGameField.getCurrentPlayer()][tileX][tileY] == -1)
		{
			points[theGameField.getCurrentPlayer()] -= 100;
			
			if (!SchiffeVersenken.isMLG())
				SchiffeVersenken.playAudio("water");
			else SchiffeVersenken.pad(new File("./audio/etc/", "sfaf.wav"), 4);
		}
		
		if (theGameField.hit(tileX, tileY))
			clicksNeeded[theGameField.getCurrentPlayer()]++;
	}
	
	public void closeConnection()
	{
		if (networkHandle == null)
			return;
		
		new Thread(() ->
		{
			try
			{
				networkHandle.disconnect();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}).start();
	}

	@Override
	public String toString()
	{
		return null;
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {}
	
	private final class SelectionPanel extends JFrame implements ActionListener, PropertyChangeListener, LangReloadListener
	{
		private static final long serialVersionUID = -6894801966479964140L;

		private JPanel gui;
		private JPanel loginData;
		private JPanel labels;
		private JPanel fields;
		
		private JTextField address;
		private JTextField port;
		private JOptionPane optionPane;
		
		private String accept, cancel, errorBase, header, errorAddressEmpty, errorPortEmpty;
		
		private SelectionPanel()
		{
			onLangReload();
			I18n.addLRL(this);
			
			init();
		}
		
		@Override
		public void onLangReload()
		{
			accept = I18n.a("multiplayer.namePanel.accept");
			cancel = I18n.a("multiplayer.namePanel.cancel");
			errorBase = I18n.a("multiplayer.namePanel.error.base");
			errorAddressEmpty = I18n.a("network.selectionPanel.error.addressEmpty");
			errorPortEmpty = I18n.a("network.selectionPanel.error.portEmpty");
			header = I18n.a("network.selectionPanel.header");
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
			Network.this.panel = false;
			Network.this.select = true;
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
				dispose();
				I18n.removeLRL(this);
				
				return;
			}
			
			if (!value.equals(accept))
				return;
			
			String msg = errorBase + System.lineSeparator();
			
			if (address.getText() == null || address.getText().isEmpty())
				msg += errorAddressEmpty;
			
			if (port.getText() == null || port.getText().isEmpty())
				msg += errorPortEmpty;
			
			if (!msg.equals(errorBase + System.lineSeparator()))
			{
				JOptionPane.showMessageDialog(this, msg, errorBase, ERROR_MESSAGE);
				return;
			}

			if (value == UNINITIALIZED_VALUE)
				return;
			
			optionPane.setValue(UNINITIALIZED_VALUE);
			
			if (!isServer)
				networkHandle = new Client(Network.this, address.getText(), Integer.parseInt(this.port.getText()));
			
			else networkHandle = new Server(Network.this, address.getText(), Integer.parseInt(this.port.getText()));
			
			super.setVisible(false);
			super.dispose();
			panel = false;
			I18n.removeLRL(this);
		}

		private void init()
		{
			gui = new JPanel(new BorderLayout(2, 2));

			loginData = new JPanel(new BorderLayout(2, 2));

			labels = new JPanel(new GridLayout(0, 1, 1, 1));
			fields = new JPanel(new GridLayout(0, 1, 1, 1));
			
			JLabel label = new JLabel("IP");
			labels.add(label);
			label = new JLabel("Port");
			labels.add(label);
			
			address = new JTextField(10);
			fields.add(address);
			port = new JTextField(10);
			port.setDocument(new Options.IntDocument(Short.MAX_VALUE));
			fields.add(port);

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