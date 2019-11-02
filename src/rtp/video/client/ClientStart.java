package rtp.video.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.LogManager;

import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Manager;
import javax.media.Player;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.ClasspathChecker;
import net.sf.fmj.utility.JmfUtility;

public class ClientStart extends JFrame {
	static JPanel panel = new JPanel();
	public static JFrame jf = new JFrame();

	static void initUI() {
		jf.setTitle("JMF Video Player Example");
		jf.setSize(new Dimension(560, 480));
		jf.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(560, 410));
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.BLACK);
		JPanel panel1 = new JPanel();
		panel1.setPreferredSize(new Dimension(560, 40));
		JLabel ipLabel = new JLabel("ServerIP: ");
		JTextField ipText = new JTextField(15);
		JLabel portLabel = new JLabel("Server Port");
		JTextField portText = new JTextField(5);
		JButton button = new JButton("Start");
		portText.setText("1234");
		ipText.setText("localhost");
		panel1.add(ipLabel);
		panel1.add(ipText);
		panel1.add(portLabel);
		panel1.add(portText);
		panel1.add(button);
		jf.add(BorderLayout.NORTH, panel);
		jf.add(BorderLayout.SOUTH, panel1);
		jf.setResizable(true);
		jf.setDefaultCloseOperation(3);
		jf.setVisible(true);
	}


}
