package de.gsh.mc.state;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import de.gsh.mc.GameOptions;
import de.gsh.mc.I18n;
import de.gsh.mc.SchiffeVersenken;

import static de.gsh.mc.I18n.a;

public class Options extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 3497610760548826777L;
	
	private JTextField uboote;
	private JTextField zerstörer;
	private JTextField kreuzer;
	private JTextField schlachter;
	private JTextField versuche;
	private JSlider volume;
	private JLabel volumeLabel;
	private JSlider score;
	private JLabel scoreLabel;
	private JComboBox<String> lang;
	private boolean cancel = false;

	public Options()
	{
		super(SchiffeVersenken.isMLG() ? "eZZ SADDINGZ" : a("options.header"));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(9, 2));
		setSize(350, 400);
		setResizable(false);
		addWindowListener(this);
		setLocationRelativeTo(null);
		
		JLabel label = new JLabel(SchiffeVersenken.isMLG() ? "HIDAZ" : a("options.submarines"));
		add(label);
		uboote = new DTJTF("" + GameOptions.getAmountShips()[0]);
		uboote.setDocument(new IntDocument(Byte.MAX_VALUE));
		add(uboote);
		label = new JLabel(SchiffeVersenken.isMLG() ? "MATHAFAKKAZZZ" : a("options.destroyer"));
		add(label);
		zerstörer = new DTJTF("" + GameOptions.getAmountShips()[1]);
		zerstörer.setDocument(new IntDocument(Byte.MAX_VALUE));
		add(zerstörer);
		label = new JLabel(SchiffeVersenken.isMLG() ? "KREUTZERSWAGGER" : a("options.kreutzer"));
		add(label);
		kreuzer = new DTJTF("" + GameOptions.getAmountShips()[2]);
		kreuzer.setDocument(new IntDocument(Byte.MAX_VALUE));
		add(kreuzer);
		label = new JLabel(SchiffeVersenken.isMLG() ? "REKKER" : a("options.battleships"));
		add(label);
		schlachter = new DTJTF("" + GameOptions.getAmountShips()[3]);
		schlachter.setDocument(new IntDocument(Byte.MAX_VALUE));
		add(schlachter);
		label = new JLabel(SchiffeVersenken.isMLG() ? "TRIEZ" : a("options.attempts"));
		add(label);
		versuche = new DTJTF("" + GameOptions.getMaxTries());
		versuche.setDocument(new IntDocument(Byte.MAX_VALUE));
		add(versuche);
		label = new JLabel(SchiffeVersenken.isMLG() ? "SPEAKA DA MATAFAKKAH" : a("options.language"));
		add(label);
		lang = new JComboBox<>(new String[] { "Deutsch", "English" });
		lang.setEditable(false);
		lang.setSelectedItem(I18n.getLocale() == Locale.GERMAN ? "Deutsch" : "English");
		add(lang);
		volumeLabel = new JLabel(SchiffeVersenken.isMLG() ? "YA WANT CHANGE" : a("options.volume") + ": "
				+ GameOptions.getRVolume());
		add(volumeLabel);
		volume = new JSlider(0, 100, GameOptions.getRVolume());
		
		volume.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				volumeLabel.setText(SchiffeVersenken.isMLG() ? "YA WANT CHANGE" : a("options.volume") + ": "
						+ volume.getValue());
			}
		});
		
		add(volume);
		scoreLabel = new JLabel(SchiffeVersenken.isMLG() ? "NOSCOPEMODIFIER" : a("options.scoreModifier") + ": *"
				+ GameOptions.getRekt());
		add(scoreLabel);
		score = new JSlider(1, 100, (int) (GameOptions.getRekt() * 10));
		
		score.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				scoreLabel.setText(SchiffeVersenken.isMLG() ? "NOSCOPEMODIFIER" : a("options.scoreModifier") + ": *"
						+ ((float) score.getValue()) / 10);
			}
		});
		
		add(score);
		
		JButton jbn = new JButton(SchiffeVersenken.isMLG() ? "REZTORE" : a("options.restoreDefaults"));
		
		jbn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GameOptions.loadDefaults();
				cancel = true;
				dispose();
			}
		});
		
		add(jbn);
		jbn = new JButton(SchiffeVersenken.isMLG() ? "HIDAYN" : a("options.cancel"));
		
		jbn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cancel = true;
				dispose();
			}
		});
		
		add(jbn);
	}
	
	@Override
	public void windowClosed(WindowEvent e)
	{
		if (cancel)
			return;
		
		byte[] ships = new byte[] { Byte.parseByte(uboote.getText()), Byte.parseByte(zerstörer.getText()),
				Byte.parseByte(kreuzer.getText()), Byte.parseByte(schlachter.getText())};
		
		if (ships[0] + ships[1] + ships[2] + ships[3] == 0)
		{
			JOptionPane.showMessageDialog(this, "Schiffanzahl nicht 0!"); // TODO
			return;
		}
		
		GameOptions.setSoundVolume(volume.getValue());
		GameOptions.setRekt(score.getValue());
		
		int max = Integer.parseInt(versuche.getText());
		
		if (max > 100)
		{
			JOptionPane.showMessageDialog(this, a("options.error.maxAttempts"));
			max = 100;
		}
		
		GameOptions.setMaxTries(max);
		String message = "";
		
		if (ships[0] > 10)
		{
			message += String.format(a("options.error.maxShips"), a("options.submarines"), "10")
					+ System.lineSeparator();
			ships[0] = 10;
		}
		if (ships[1] > 8)
		{
			message += String.format(a("options.error.maxShips"), a("options.destroyer"), "8")
					+ System.lineSeparator();
			ships[1] = 8;
		}
		if (ships[2] > 6)
		{
			message += String.format(a("options.error.maxShips"), a("options.kreutzer"), "6")
					+ System.lineSeparator();
			ships[2] = 6;
		}
		if (ships[3] > 4)
		{
			message += String.format(a("options.error.maxShips"), a("options.battleships"), "4")
					+ System.lineSeparator();
			ships[3] = 4;
		}
		
		if (!message.isEmpty())
			JOptionPane.showMessageDialog(this, message);
		
		GameOptions.setShips(ships);
		
		I18n.setLocale(lang.getSelectedItem().equals("Deutsch") ? Locale.GERMAN : Locale.ENGLISH);
	}
	
	static final class IntDocument extends PlainDocument
	{
		private static final long serialVersionUID = 1223902056215925157L;
		private final int maxValue;
		
		public IntDocument(int maxValue)
		{
			this.maxValue = maxValue;
		}
		
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{
			try
			{
				Integer.parseInt(str);
			}
			catch (Exception ex)
			{
				return;
			}
			
			if (Integer.parseInt(getText(0, getLength()) + str) > maxValue)
				return;
			
			super.insertString(offs, str, a);
		}
	}
	
	private static final class DTJTF extends JTextField {private static final long serialVersionUID = 1817978449229559201L;private final String d; public DTJTF(String d) { this.d = d; } @Override public String getText() { return super.getText().isEmpty() ? d : super.getText();}}

	@Override public void windowActivated(WindowEvent e) {}
	@Override public void windowClosing(WindowEvent e) {}
	@Override public void windowDeactivated(WindowEvent e) {}
	@Override public void windowDeiconified(WindowEvent e) {}
	@Override public void windowIconified(WindowEvent e) {}
	@Override public void windowOpened(WindowEvent e) {}
}