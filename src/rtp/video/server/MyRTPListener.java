package rtp.video.server;

import java.awt.Component;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.protocol.DataSource;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MyRTPListener implements ControllerListener {

	private Player player;
	private JFrame jf;
	private JPanel panel;

	public MyRTPListener(Player player, JFrame jf, JPanel panel) {
		this.player = player;
		this.jf = jf;
		this.panel = panel;
	}

	public synchronized void update(ReceiveStreamEvent evt) {
		if (evt instanceof NewReceiveStreamEvent) {
			ReceiveStream stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
			DataSource dataSource = stream.getDataSource();
			try {
				Player player = Manager.createPlayer(dataSource);
				player.addControllerListener(this);
				player.realize();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void controllerUpdate(ControllerEvent e) {
		if (e instanceof RealizeCompleteEvent) {
			Component comp ;
			if( (comp = player.getVisualComponent()) != null ) {
				panel.add("Center", comp);
			}
			if (( comp = player.getControlPanelComponent()) != null) {
				panel.add("South",comp);
			}
			
			jf.validate();
		}
		
	}

}
