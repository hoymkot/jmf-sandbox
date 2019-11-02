package rtp.video.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.LogManager;

import javax.media.DataSink;
import javax.media.Processor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.ClasspathChecker;
import net.sf.fmj.utility.JmfUtility;

public class ServerStart extends JFrame {

	
	public static JPanel panel = new JPanel();
	public static JFrame jf= new JFrame();
	public static void main(String args[]) throws SecurityException, IOException {
		
		// The following is require to register the plugins
		if (!ClasspathChecker.checkAndWarn()) {
			// JMF is ahead of us in the classpath. Let's do some things to make this go
			// more smoothly.

			// Let's register our own prefixes, etc, since they won't generally be if JMF is
			// in charge.
			RegistryDefaults.registerAll(RegistryDefaults.FMJ);
			// RegistryDefaults.unRegisterAll(RegistryDefaults.JMF); // TODO: this can be
			// used to make some things that work in FMJ but not in JMF, work, like
			// streaming mp3/ogg.
			// TODO: what about the removal of some/reordering?
		}
		
		System.setProperty("java.util.logging.config.file", "logging.properties");
		LogManager.getLogManager().readConfiguration();
		
		ServerStart test = new ServerStart() ;
		test.initUI();
	}
	
	static {
		System.setProperty("java.library.path", "D:/fmj-sf/native/win32-x86/");
		Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initUI() {
		jf.setTitle("FMJ receive video example");
		jf.setSize(560, 480);
		jf.setLayout(new BorderLayout());
		
		
		panel.setPreferredSize(new Dimension(560, 410));
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.BLACK);
		JPanel panel1 = new JPanel();
		panel1.setPreferredSize(new Dimension(560, 40));
		JLabel portLabel = new JLabel("Port");
		JTextField portText = new JTextField(5);
		portText.setText("1234");
		JButton button = new JButton("Start");
		panel1.add(portLabel);
		panel1.add(portText);
		panel1.add(button);
		jf.add(BorderLayout.NORTH, panel);
		jf.add(BorderLayout.SOUTH, panel1);
		jf.setResizable(true);
		jf.setDefaultCloseOperation(3);
		jf.setVisible(true);
		ServerReceive receive = new ServerReceive();
		receive.startReceive();
		Thread th = new Thread(new Runner());
		th.start();
	}
	
	static class Runner implements Runnable {
		Processor p;
		DataSink fw;

		public Runner(Processor p, DataSink filewriter) {
			this.p = p;
			fw = filewriter;
		}

		public Runner() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
				while(true) {
					;
				}
		}

	}
}
