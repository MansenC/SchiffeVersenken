package de.gsh.mc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import static javax.swing.JOptionPane.*;

public final class LoginWindow extends JFrame implements ActionListener, PropertyChangeListener
{
	private static final long serialVersionUID = -6894801966479964140L;

	private JPanel gui;
	private JPanel loginData;
	private JPanel labels;
	private JPanel fields;

	private JLabel labelUsername;
	private JComboBox<String> username;
	private JOptionPane optionPane;
	
	private LoginWindow()
	{
		super("Login");
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		System.out.println("User performed action: " + e.getActionCommand());
		optionPane.setValue(I18n.a("loginWindow.login"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		String prop = evt.getPropertyName();

		if (isVisible()
				&& (evt.getSource() == this.optionPane)
				&& (VALUE_PROPERTY.equals(prop) || INPUT_VALUE_PROPERTY.equals(prop)))
		{
			Object value = this.optionPane.getValue();

			if (value == UNINITIALIZED_VALUE)
				return;

			optionPane.setValue(UNINITIALIZED_VALUE);

			if (value.equals(I18n.a("loginWindow.login")))
			{
				System.out.println("Preparing login...");
				
				String stringUsername = (String) username.getSelectedItem();

				if (stringUsername == null || stringUsername.isEmpty())
				{
					JOptionPane.showMessageDialog(this, I18n.a("loginWindow.error.noUsername"),
							I18n.a("loginWindow.error.base"), ERROR_MESSAGE);
					
					System.out.println("No Username given!");
					
					return;
				}
				else if (stringUsername.length() < 3)
				{
					JOptionPane.showMessageDialog(this, String.format(I18n.a("loginWindow.error.nameTooShort"),
							stringUsername), I18n.a("loginWindow.error.base"), ERROR_MESSAGE);
					
					System.out.println("Invalid username!");

					return;
				}
				else
				{
					super.dispose();
					performLogin(stringUsername);
				}
			}
			else super.dispose();
		}
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		
		for (Thread t : Thread.getAllStackTraces().keySet())
			System.out.println(t.getName() + " : " + t.isDaemon());
	}

	private void init()
	{
		gui = new JPanel(new BorderLayout(3, 2));

		loginData = new JPanel(new BorderLayout(3, 2));

		labels = new JPanel(new GridLayout(0, 1, 1, 1));
		fields = new JPanel(new GridLayout(0, 1, 1, 1));

		labelUsername = new JLabel(I18n.a("loginWindow.username"));
		
		List<String> strList = new Config(new File(".", "options.opt")).getStringList("usernames");
		username = new JComboBox<String>(strList.toArray(new String[strList.size()]));
		username.setEditable(true);
		username.setSelectedItem(new Config(new File(".", "options.opt")).get("lastUsername"));

		labels.add(labelUsername);
		fields.add(username);

		loginData.add(labels, BorderLayout.CENTER);
		loginData.add(fields, BorderLayout.EAST);

		gui.add(loginData);

		optionPane = new JOptionPane(new Object[]
				{
					I18n.a("loginWindow.header"), gui
				},
				QUESTION_MESSAGE,
				YES_NO_OPTION,
				null,
				new Object[]
				{
					I18n.a("loginWindow.login"), I18n.a("loginWindow.cancel")
				},
				I18n.a("loginWindow.login"));

		super.setContentPane(optionPane);

		super.setDefaultCloseOperation(EXIT_ON_CLOSE);

		super.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent ce)
			{
				LoginWindow.this.username.requestFocusInWindow();
			}
		});
		
		username.addActionListener(this);
		optionPane.addPropertyChangeListener(this);

		super.setResizable(false);
		super.pack();
		super.setLocationRelativeTo(null);
		super.setVisible(true);
		
		System.out.println("Graphical instance created...");
	}
	
	private void performLogin(String username)
	{
		System.out.println("Performing login for user \"" + username + "\"");
		
		Config c = new Config(new File(".", "options.opt"));
		List<String> usernames = c.getStringList("usernames");
		if (!usernames.contains(username))
			usernames.add(username);
		
		c.setList("usernames", usernames);
		c.set("lastUsername", username);
		c.save(new File(".", "options.opt"));
		
		System.out.println("Hiding " + getClass().getName());
		
		super.setVisible(false);
		
		JFrame frame = new JFrame(I18n.a("game.title")); // Den Frame des Spiels erstellen
		SchiffeVersenken game = new SchiffeVersenken(frame, username); // Neue instanz des Spiels erzeugen
		
		System.out.println("Game instance created!");
		
		game.setPreferredSize(new Dimension(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT)); // Größe des Feldes setzen
		game.setMinimumSize(new Dimension(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT));
		game.setMaximumSize(new Dimension(SchiffeVersenken.WIDTH, SchiffeVersenken.HEIGHT));
		game.addFocusListener(game);
		
		System.out.println("Game format was set up!");
		
		frame.add(game); // Das Spiel hinzufügen
		frame.pack(); // Den Frame so groß machen, dass er das Spiel in sich zeigen kann
		frame.setResizable(false); // Größeninvariabel setzen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beim Schließen auch die VM schließen
		frame.setLocationRelativeTo(null); // Zentriert setzen
		frame.addFocusListener(game);
		
		System.out.println("JFrame was set up!");
		
		try
		{
			game.start(); // Das Spiel starten
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		frame.setVisible(true); // Sichtbar machen
		game.requestFocus();
		System.out.println("Game visible!");
		
		super.dispose();
	}

	public static void create()
	{
		new LoginWindow().init();
	}
}